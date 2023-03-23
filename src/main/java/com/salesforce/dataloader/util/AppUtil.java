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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;

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
    
    public static final String DATALOADER_VERSION;
    public static final String DATALOADER_SHORT_VERSION;
    public static final String MIN_JAVA_VERSION;
    
    public static final String SYS_PROP_LOG4J2_CONFIG_FILE = "log4j2.configurationFile";
    public static final String LOG_CONF_DEFAULT = "log-conf.xml";
    private static Logger logger;
    
    static {
        Properties versionProps = new Properties();
        try {
            versionProps.load(Controller.class.getClassLoader().getResourceAsStream("com/salesforce/dataloader/version.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        DATALOADER_VERSION=versionProps.getProperty("dataloader.version");
        String[] versionParts = DATALOADER_VERSION.split("\\.");
        DATALOADER_SHORT_VERSION=versionParts[0];
        MIN_JAVA_VERSION=versionProps.getProperty("java.min.version");

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
        link = Controller.class.getResourceAsStream(extractionArtifact);
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
            if (extractionDestination.isFile() && extractionDestination.exists()) {
                // Do not overwrite existing artifacts
                continue;
            } else {
                extractionDestination.getParentFile().mkdirs();
                extractionDestination = new java.io.File(destDirName, childArtifactName);
            }
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

    public static void showBanner() {
        System.out.println(Messages.getMessage(AppUtil.class, "banner", DATALOADER_SHORT_VERSION, MIN_JAVA_VERSION));
    }
    
    public static synchronized void initializeLog(Map<String, String> argsMap) throws FactoryConfigurationError, IOException {
        setConfigurationsDir(argsMap);
        // check the environment variable for log4j
        String log4jConfigFilePath = System.getenv("LOG4J_CONFIGURATION_FILE");
        if (log4jConfigFilePath == null || log4jConfigFilePath.isEmpty()) {
            // check the system property for log4j2
            log4jConfigFilePath = System.getProperty(SYS_PROP_LOG4J2_CONFIG_FILE);
        }
        
        if (log4jConfigFilePath == null || log4jConfigFilePath.isEmpty()) { // use the default
            log4jConfigFilePath = Paths.get(AppUtil.getConfigurationsDir(), LOG_CONF_DEFAULT).toString();
        }
       
        Path p = Paths.get(log4jConfigFilePath);
        File logConfFile;
        if (p.isAbsolute()) {
            logConfFile = Paths.get(log4jConfigFilePath).toFile();
        } else {
            logConfFile = Paths.get(System.getProperty("user.dir"), log4jConfigFilePath).toFile();
        }

        String log4jConfigFileAbsolutePath = logConfFile.getAbsolutePath();
        if (!logConfFile.exists()) {
            AppUtil.extractFromJar("/" + LOG_CONF_DEFAULT, logConfFile);
        }
        System.setProperty(SYS_PROP_LOG4J2_CONFIG_FILE, log4jConfigFileAbsolutePath);

        // Uncomment code block to check that logger is using the config file
        /*
         * 

        logger = LogManager.getLogger(AppUtil.class);

        LoggerContext loggerContext = (LoggerContext) LogManager.getContext();
        String logConfigLocation = loggerContext.getConfiguration().getConfigurationSource().getLocation();
        if (logConfigLocation == null) {
            logger.error("Unable to initialize logging using log4j2 config file at "
                    + log4jConfigFileAbsolutePath
                    + ". All error messages will be logged on STDOUT.");
        } else {
            logger.info("Using log4j2 configuration file at location: " + logConfigLocation);
        }
        */
        logger = LogManager.getLogger(AppUtil.class);
        logger.info(Messages.getString("AppUtil.logInit")); //$NON-NLS-1$
    }
    
    private static String getDefaultConfigDir() {
        return AppUtil.getDirContainingClassJar(Config.class) 
                + "/"
                + Config.CONFIG_DIR_DEFAULT_VALUE;
    }

    public static Map<String, String> getArgMapFromArgArray(String[] argArray){
        Map<String, String> argMap = new HashMap<>();
        if (argArray != null) {
            //Process name=value config setting
            Arrays.stream(argArray).forEach(arg ->
            {
                String[] nameValuePair = arg.split("=", 2);
                if (nameValuePair.length == 2)
                    argMap.put(nameValuePair[0], nameValuePair[1]);
            });
        }
        return argMap;
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
