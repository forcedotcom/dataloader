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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.exception.ConfigInitializationException;


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
    public static final String COMMA = ",";
    public static final String TAB = "\t";
    public static final String DATALOADER_VERSION;
    public static final String DATALOADER_SHORT_VERSION;
    public static final String MIN_JAVA_VERSION;
    public static final String CLI_OPTION_GMT_FOR_DATE_FIELD_VALUE = "datefield.usegmt";
    public static final String CLI_OPTION_SWT_NATIVE_LIB_IN_JAVA_LIB_PATH = "swt.nativelib.inpath";
    public static final String CLI_OPTION_CONFIG_DIR_PROP = "salesforce.config.dir";
    public static final String CONFIG_DIR_DEFAULT_VALUE = "configs";
    public static final String DATALOADER_DOWNLOAD_URL = "https://developer.salesforce.com/tools/data-loader";
    
    private static APP_RUN_MODE appRunMode = APP_RUN_MODE.UI;
    private static Logger logger = null;
    private static String latestDownloadableDataLoaderVersion;
    
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
    
    public static String[] initializeAppConfig(String[] args) throws FactoryConfigurationError, IOException, ConfigInitializationException {
        Map<String, String> argsMap = convertCommandArgsArrayToArgMap(args);
        setConfigurationsDir(argsMap);
        LoggingUtil.initializeLog(argsMap);
        logger = LogManager.getLogger(AppUtil.class);
        setUseGMTForDateFieldValue(argsMap);
        latestDownloadableDataLoaderVersion = _getLatestAvailableAppVersionFromWebsite();
        return convertCommandArgsMapToArgsArray(argsMap);
    }

    public static String getLatestDownloadableDataLoaderVersion() {
        return latestDownloadableDataLoaderVersion;
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

    private static void setUseGMTForDateFieldValue(Map<String, String> argMap) {
        if (argMap.containsKey(AppUtil.CLI_OPTION_GMT_FOR_DATE_FIELD_VALUE)) {
            if ("true".equalsIgnoreCase(argMap.get(AppUtil.CLI_OPTION_GMT_FOR_DATE_FIELD_VALUE))) {
                useGMTForDateFieldValue = true;
            }
        }
    }
    
    public static void setUseGMTForDateFieldValue(boolean val) {
        useGMTForDateFieldValue = val;
    }
    
    private static boolean useGMTForDateFieldValue;
    public static boolean isUseGMTForDateFieldValue() {
        return useGMTForDateFieldValue;
    }
    private static String configurationsDir = null;
    public static synchronized String getConfigurationsDir() {
        if (configurationsDir == null) {
            setConfigurationsDir(null);
        }
        return configurationsDir;
    }
    
    private static synchronized void setConfigurationsDir(Map<String, String> argsMap) {
        if (argsMap != null && argsMap.containsKey(CLI_OPTION_CONFIG_DIR_PROP)) {
            configurationsDir = argsMap.get(CLI_OPTION_CONFIG_DIR_PROP);
        } else {
            if (configurationsDir == null) {
                // first time invocation and configurationsDir is not set through argsMap
                configurationsDir = System.getProperty(CLI_OPTION_CONFIG_DIR_PROP);
            }
            if (configurationsDir != null && !configurationsDir.isEmpty()) {
                return;
            }
            // first time invocation, configurationsDir is not set through argsMap or through system property
            configurationsDir = getDefaultConfigDir();
        }
        System.setProperty(CLI_OPTION_CONFIG_DIR_PROP, configurationsDir);
    }
    
    private static String getDefaultConfigDir() {
        return AppUtil.getDirContainingClassJar(Config.class) 
                + System.getProperty("file.separator")
                + CONFIG_DIR_DEFAULT_VALUE;
    }

    public static void showBanner() {
        System.out.println(Messages.getMessage(AppUtil.class, "banner", DATALOADER_SHORT_VERSION, MIN_JAVA_VERSION));
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
        if (getAppRunMode() == APP_RUN_MODE.BATCH) {
            processArgsForBatchMode(argArray, commandArgsMap);
        }
        return commandArgsMap;
    }
    
    
    private static void processArgsForBatchMode(String[] args, Map<String,String> argsMap) {
        if (!argsMap.containsKey(AppUtil.CLI_OPTION_CONFIG_DIR_PROP) && args.length < 2) {
            // config folder must be specified in the first argument
            System.err.println(
                    "Usage: process <configuration folder> [batch process bean id]\n"
                    + "\n"
                    + "      configuration folder -- required -- folder that contains configuration files,\n"
                    + "          i.e. config.properties, process-conf.xml, database-conf.xml\n"
                    + "\n"
                    + "      batch process bean id -- optional -- id of a batch process bean in process-conf.xml,\n"
                    + "          for example:\n"
                    + "\n"
                    + "              process ../myconfigdir AccountInsert\n"
                    + "\n"
                    + "      If process bean id is not specified, the value of the property process.name in config.properties\n"
                    + "      will be used to run the process instead of process-conf.xml,\n"
                    + "          for example:\n"
                    + "\n"
                    + "              process ../myconfigdir");
            System.exit(-1);
        }
        if (!argsMap.containsKey(AppUtil.CLI_OPTION_CONFIG_DIR_PROP)) {
            argsMap.put(AppUtil.CLI_OPTION_CONFIG_DIR_PROP, args[0]);
        }
        if (!argsMap.containsKey(Config.PROCESS_NAME) 
                && args.length > 2
                && !args[1].contains("=")) {
            // second argument must be process name
            argsMap.put(Config.PROCESS_NAME, args[1]);
        }
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
    
    // Run a command in the native system while redirecting its stdout and stderr
    public static int exec(List<String> command, String exceptionMessage) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        if (exceptionMessage == null) {
           exceptionMessage = Messages.getString("AppUtil.processExecutionError");
        }
        int exitVal = -1;
        try {
            Process process = processBuilder.start();
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;

            while ((line = br.readLine()) != null) {
              System.out.println(line);
            }

            exitVal = process.waitFor();
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            logger.error(exceptionMessage, e);
        }
        return exitVal;
    }
    
    private static final String DL_DOWNLOADABLE_REGEX = "[0-9]+\\.[0-9]+\\.[0-9]+\\.zip";
    private static String _getLatestAvailableAppVersionFromWebsite() {
        try {
            Builder requestBuilder = HttpRequest.newBuilder(new URI(DATALOADER_DOWNLOAD_URL));
            HttpRequest request = requestBuilder.GET().build();
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .proxy(ProxySelector.getDefault())
                    .build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                logger.info("Unable to check for the latest available data loader version. Response code = " + response.statusCode());
                return DATALOADER_VERSION;
            }
            String responseContent = response.body();
            
            Pattern htmlTagInRichTextPattern = Pattern.compile(DL_DOWNLOADABLE_REGEX);
            Matcher matcher = htmlTagInRichTextPattern.matcher(responseContent);
            String downloadableVersion = DATALOADER_VERSION;
            if (matcher.find()) {
                downloadableVersion = matcher.group();
                downloadableVersion = downloadableVersion.substring(0, downloadableVersion.lastIndexOf("."));
            }
            return downloadableVersion;
        } catch (Exception e) {
            logger.info("Unable to check for the latest available data loader version: " + e.getMessage());
            return DATALOADER_VERSION;
        }
    }
}
