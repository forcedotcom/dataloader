/*
 * Copyright (c) 2015, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.salesforce.dataloader.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;

/**
 * com.salesforce.dataloader.util
 *
 * @author xbian
 * @since
 */
public class AppUtil {


    public enum OSType {
        WINDOWS,
        LINUX,
        MACOSX,
        UNKNOWN
    }
    
    public enum APP_RUN_MODE {
        BATCH,
        UI,
        INSTALL,
        ENCRYPT
    }
    
    public static final String DATALOADER_VERSION;
    public static final String DATALOADER_SHORT_VERSION;
    public static final String MIN_JAVA_VERSION;
    private static APP_RUN_MODE appRunMode = APP_RUN_MODE.UI;
    private static Logger logger;
    
    static {
        Properties versionProps = new Properties();
        try {
            versionProps.load(AppUtil.class.getClassLoader().getResourceAsStream("com/salesforce/dataloader/version.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        DATALOADER_VERSION=versionProps.getProperty("dataloader.version");
        String[] versionParts = DATALOADER_VERSION.split("\\.");
        DATALOADER_SHORT_VERSION=versionParts[0];
        MIN_JAVA_VERSION=versionProps.getProperty("java.min.version");

    }
    
    public static void initializeAppConfig(Map<String, String> argsMap) throws FactoryConfigurationError, IOException {
        setConfigurationsDir(argsMap);
        LoggingUtil.initializeLog(argsMap);
        logger = LogManager.getLogger(AppUtil.class);
    }

    public static OSType getOSType() throws SecurityException {
        String osName = System.getProperty(OS_NAME);

        if (osName != null) {
            if (osName.contains("Windows")) {
                return OSType.WINDOWS;
            }

            if (osName.contains("Linux")) {
                return OSType.LINUX;
            }

            if (osName.contains("OS X")) {
                return OSType.MACOSX;
            }

            // determine another OS here
        }

        return OSType.UNKNOWN;
    }

    public static final String OS_NAME = "os.name";
    
    public static String getFullPathOfJar(Class<?> aClass) {
        CodeSource codeSource = aClass.getProtectionDomain().getCodeSource();
        if (codeSource != null && codeSource.getLocation() != null) {
            try {
                String jarFilePath = codeSource.getLocation().toURI().toString();
                return jarFilePath.substring(jarFilePath.indexOf('/'));
            } catch (URISyntaxException e) {
                return null;
            }
        } else {
          String path = aClass.getResource(aClass.getSimpleName() + ".class").getPath();
          String jarFilePath = path.substring(path.indexOf(":") + 1, path.indexOf("!"));
          try {
              jarFilePath = URLDecoder.decode(jarFilePath, StandardCharsets.UTF_8.name());
          } catch (UnsupportedEncodingException e) {
              // fail silently;
          }
          return jarFilePath;
        }
    }
    
    public static String getDirContainingClassJar(Class<?> aClass) {
        File jarFile = new File(getFullPathOfJar(aClass));
        return jarFile.getParentFile().getAbsolutePath();
    }
    
    public static void extractFromJar(String extractionArtifact, File extractionDestination) throws IOException {
        InputStream link;
        link = AppUtil.class.getResourceAsStream(extractionArtifact);
        String parentDirStr = extractionDestination.getAbsoluteFile().getParent();
        File parentDir = Paths.get(parentDirStr).toFile();
        Files.createDirectories(parentDir.getAbsoluteFile().toPath());
        if (extractionDestination.exists()) {
            extractionDestination.delete();
        }
        Files.copy(link, extractionDestination.getAbsoluteFile().toPath());
    }
    
    public static void extractDirFromJar(String extractionPrefix, String destDirName, boolean flatten) throws IOException, URISyntaxException {
        String jarPath = getFullPathOfJar(AppUtil.class);
        java.util.jar.JarFile jarfile = new java.util.jar.JarFile(new java.io.File(jarPath)); //jar file path(here sqljdbc4.jar)
        java.util.Enumeration<java.util.jar.JarEntry> enu= jarfile.entries();
        while(enu.hasMoreElements())
        {
            java.util.jar.JarEntry jarEntry = enu.nextElement();

            if (!jarEntry.toString().startsWith(extractionPrefix)
                || jarEntry.isDirectory() 
                || jarEntry.getName().startsWith("\\.")) {
                continue;
            }
            logger.debug(jarEntry.getName());
            
            String childArtifactName = jarEntry.getName();
            if (flatten) {
                childArtifactName = childArtifactName.substring(extractionPrefix.length());
            }
            File extractionDestination = new File(destDirName, childArtifactName);
            if (extractionDestination.exists()) {
                logger.debug("File " + extractionDestination + " won't be extracted from jar as it already exists");
                continue;
            } else {
                extractionDestination.getParentFile().mkdirs();
                extractionDestination = new java.io.File(destDirName, childArtifactName);
            }
            extractionDestination.delete(); // just in case it is a dangling symlink
            java.io.InputStream is = jarfile.getInputStream(jarEntry);
            java.io.FileOutputStream fo = new java.io.FileOutputStream(extractionDestination);
            while(is.available() > 0)
            {
                fo.write(is.read());
            }
            fo.close();
            is.close();
            if (extractionDestination.getName().endsWith(".bat")
                || extractionDestination.getName().endsWith(".sh")
                || extractionDestination.getName().endsWith(".command")
                || !extractionDestination.getName().contains("\\.")) {
                extractionDestination.setExecutable(true);
            }
        }
    }

    public static APP_RUN_MODE getAppRunMode() {
        return appRunMode;
    }
    
    private static String configurationsDir = null;
    public static synchronized String getConfigurationsDir() {
        if (configurationsDir == null) {
            setConfigurationsDir(null);
        }
        return configurationsDir;
    }
    
    private static synchronized void setConfigurationsDir(Map<String, String> argsMap) {
        if (argsMap != null && argsMap.containsKey(Config.CLI_OPTION_CONFIG_DIR_PROP)) {
            configurationsDir = argsMap.get(Config.CLI_OPTION_CONFIG_DIR_PROP);
        } else {
            if (configurationsDir == null) {
                // first time invocation and configurationsDir is not set through argsMap
                configurationsDir = System.getProperty(Config.CLI_OPTION_CONFIG_DIR_PROP);
            }
            if (configurationsDir != null && !configurationsDir.isEmpty()) {
                return;
            }
            // first time invocation, configurationsDir is not set through argsMap or through system property
            configurationsDir = getDefaultConfigDir();
        }
        System.setProperty(Config.CLI_OPTION_CONFIG_DIR_PROP, configurationsDir);
    }
    
    public static String getConfigsDir() {
        return configurationsDir;
    }

    public static void showBanner() {
        System.out.println(Messages.getMessage(AppUtil.class, "banner", DATALOADER_SHORT_VERSION, MIN_JAVA_VERSION));
    }
    
    
    private static String getDefaultConfigDir() {
        return AppUtil.getDirContainingClassJar(Config.class) 
                + "/"
                + Config.CONFIG_DIR_DEFAULT_VALUE;
    }

    private static final String OTHER_ARGS_KEY = "__OTHER_ARGS__";
    private static int otherArgsCount = 0;
    public synchronized static Map<String, String> convertCommandArgsArrayToArgMap(String[] argArray){
        Map<String, String> commandArgsMap = new HashMap<String, String>();
        if (argArray == null) {
            return commandArgsMap;
        }

        if (argArray != null) {
            //Process name=value config setting
            otherArgsCount = 0;
            Arrays.stream(argArray).forEach(arg ->
            {
                String[] nameValuePair = arg.split("=", 2);
                if (nameValuePair.length == 2) {
                    if (nameValuePair[0].equalsIgnoreCase(Config.CLI_OPTION_RUN_MODE)) {
                        setAppRunMode(nameValuePair[1]);
                    } else {
                        commandArgsMap.put(nameValuePair[0], nameValuePair[1]);
                    }
                } else {
                    commandArgsMap.put(OTHER_ARGS_KEY + otherArgsCount++, arg);
                }
            });
        }
        return commandArgsMap;
    }
    
    private static void setAppRunMode(String modeStr) {
        if (modeStr == null) return;
        switch (modeStr) {
            case "batch":
                appRunMode = APP_RUN_MODE.BATCH;
                break;
            case "install":
                appRunMode = APP_RUN_MODE.INSTALL;
                break;
            case "encrypt":
                appRunMode = APP_RUN_MODE.ENCRYPT;
                break;
            default:
                appRunMode = APP_RUN_MODE.UI;
        }
    }
    
    public static String[] convertCommandArgsMapToArgsArray(Map<String, String> argsMap) {
        if (argsMap == null) {
            return null;
        }
        ArrayList<String> argsList = new ArrayList<String>();
        ArrayList<String> argKeysList = new ArrayList<String>(argsMap.keySet());
        Collections.sort(argKeysList);
        for (String argKey : argKeysList) {
            if (argKey.startsWith(OTHER_ARGS_KEY)) {
                argsList.add(argsMap.get(argKey));
            } else {
                String argVal = argsMap.get(argKey);
                argsList.add(argKey + "=" + argVal);
            }
        }
        return argsList.toArray(new String[0]);
    }
    
    public static boolean isRunningOnMacOS() {
        return getOSType() == OSType.MACOSX;
    }
    
    public static boolean isRunningOnWindows() {
        return getOSType() == OSType.WINDOWS;
    }
    
    public static boolean isRunningOnLinux() {
        return getOSType() == OSType.LINUX;
    }
}
