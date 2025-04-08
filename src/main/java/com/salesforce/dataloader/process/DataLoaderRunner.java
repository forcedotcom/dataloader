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
package com.salesforce.dataloader.process;

/**
 * @author Lexi Viripaeff
 * @input DataLoaderRunner -------------- @ * ----------------
 */

import com.salesforce.dataloader.install.Installer;
import com.salesforce.dataloader.security.EncryptionUtil;
import com.salesforce.dataloader.ui.UIUtils;
import com.salesforce.dataloader.util.AppUtil;
import com.salesforce.dataloader.util.ExitException;

import java.io.File;
import java.io.FileFilter;

import com.salesforce.dataloader.util.DLLogManager;
import org.apache.logging.log4j.Logger;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.client.transport.HttpTransportImpl;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.controller.Controller;

public class DataLoaderRunner extends Thread {
    private static final String LOCAL_SWT_DIR = "./target/";
    private static final String PATH_SEPARATOR = System.getProperty("path.separator");
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static Logger logger = DLLogManager.getLogger(DataLoaderRunner.class);
    private static int exitCode = AppUtil.EXIT_CODE_NO_ERRORS;
    private static final String REMAINING_ARG_KEY_PREFIX = "__REMAINING_ARG__";
    private static int remainingArgsCount = 0;

    public void run() {
        // called just before the program closes
        HttpTransportImpl.closeHttpClient();
    }

    private static void processArgsForBatchMode(String[] args, Map<String, String> argsMap) {
        validateBatchModeArgs(args, argsMap);
        setConfigDirIfMissing(argsMap);
        setProcessNameIfMissing(argsMap);
    }
    
    private static void validateBatchModeArgs(String[] args, Map<String, String> argsMap) {
        if (!argsMap.containsKey(AppConfig.CLI_OPTION_CONFIG_DIR_PROP) && args.length < 2) {
            System.err.println(getBatchModeUsageMessage());
            System.exit(AppUtil.EXIT_CODE_CLIENT_ERROR);
        }
    }
    
    private static void setConfigDirIfMissing(Map<String, String> argsMap) {
        if (!argsMap.containsKey(AppConfig.CLI_OPTION_CONFIG_DIR_PROP)
                && argsMap.get(REMAINING_ARG_KEY_PREFIX + 0) != null) {
            argsMap.put(AppConfig.CLI_OPTION_CONFIG_DIR_PROP, argsMap.get(REMAINING_ARG_KEY_PREFIX + 0));
        }
    }
    
    private static void setProcessNameIfMissing(Map<String, String> argsMap) {
        if (!argsMap.containsKey(AppConfig.PROP_PROCESS_NAME)
                && argsMap.get(REMAINING_ARG_KEY_PREFIX + 1) != null) {
            argsMap.put(AppConfig.PROP_PROCESS_NAME, argsMap.get(REMAINING_ARG_KEY_PREFIX + 1));
        }
    }
    
    private static String getBatchModeUsageMessage() {
        return """
                Usage: process <configuration folder> [batch process bean id]
    
                      configuration folder -- required -- folder that contains configuration files,
                          i.e. config.properties, process-conf.xml, database-conf.xml
    
                      batch process bean id -- optional -- id of a batch process bean in process-conf.xml,
                          for example:
    
                              process ../myconfigdir AccountInsert
    
                      If process bean id is not specified, the value of the property process.name in config.properties
                      will be used to run the process instead of process-conf.xml,
                          for example:
    
                              process ../myconfigdir
                """;
    }

    public synchronized static Map<String, String> configureRunModeAndGetArgsMap(String[] argArray) {
        Map<String, String> commandArgsMap = new HashMap<>();
        if (argArray == null) {
            return commandArgsMap;
        }
    
        parseArguments(argArray, commandArgsMap);
        if (AppUtil.getAppRunMode() == AppUtil.APP_RUN_MODE.BATCH) {
            processArgsForBatchMode(argArray, commandArgsMap);
        }
        return commandArgsMap;
    }
    
    private static void parseArguments(String[] argArray, Map<String, String> commandArgsMap) {
        DataLoaderRunner.remainingArgsCount = 0;
        Arrays.stream(argArray).forEach(arg -> {
            String[] nameValuePair = arg.split("=", 2);
            if (nameValuePair.length == 2) {
                handleNameValuePair(nameValuePair, commandArgsMap);
            } else if (!arg.startsWith("-")) {
                commandArgsMap.put(REMAINING_ARG_KEY_PREFIX + DataLoaderRunner.remainingArgsCount++, arg);
            }
        });
    }
    
    private static void handleNameValuePair(String[] nameValuePair, Map<String, String> commandArgsMap) {
        if (nameValuePair[0].equalsIgnoreCase(AppConfig.CLI_OPTION_RUN_MODE)) {
            AppUtil.setAppRunMode(nameValuePair[1]);
        } else {
            commandArgsMap.put(nameValuePair[0], nameValuePair[1]);
        }
    }

    public static void main(String[] args) {
        try {
            Map<String, String> argsMap = configureRunModeAndGetArgsMap(args);
            if (!argsMap.containsKey(AppConfig.CLI_OPTION_SWT_NATIVE_LIB_IN_JAVA_LIB_PATH)) {
                AppUtil.showBanner();
            }
            runApp(argsMap, args, null);
        } catch (ExitException ex) {
            exitCode = ex.getExitCode();
        } finally {
            logServerInvocationCount();
        }
        System.exit(exitCode);
    }
    
    private static void logServerInvocationCount() {
        if (logger != null) {
            logger.debug("Number of server API invocations = " + HttpTransportImpl.getServerInvocationCount());
        }
    }
    
    public static IProcess runApp(String[] args, ILoaderProgress monitor) {
        return runApp(DataLoaderRunner.configureRunModeAndGetArgsMap(args), args, monitor);
    }
    
    private static IProcess runApp(Map<String, String> argsMap, String[] args, ILoaderProgress monitor) {
        Controller controller = null;
        Runtime.getRuntime().addShutdownHook(new DataLoaderRunner());
        try {
            controller = Controller.getInstance(argsMap);
        } catch (FactoryConfigurationError | Exception ex) {
            logger.fatal(ex);
            System.exit(AppUtil.EXIT_CODE_CLIENT_ERROR);
        }
        if (AppUtil.getAppRunMode() == AppUtil.APP_RUN_MODE.BATCH) {
            return ProcessRunner.runBatchMode(argsMap, monitor);
        } else if (AppUtil.getAppRunMode() == AppUtil.APP_RUN_MODE.ENCRYPT) {
            EncryptionUtil.main(args);
        } else {
            /* Run in the UI mode, get the controller instance with batchMode == false */
            if (argsMap.containsKey(AppConfig.CLI_OPTION_SWT_NATIVE_LIB_IN_JAVA_LIB_PATH) 
                && "true".equalsIgnoreCase(argsMap.get(AppConfig.CLI_OPTION_SWT_NATIVE_LIB_IN_JAVA_LIB_PATH))){
                try {
                    String defaultBrowser = System.getProperty("org.eclipse.swt.browser.DefaultType");
                    if (defaultBrowser == null) {
                        logger.debug("org.eclipse.swt.browser.DefaultType not set for UI mode on Windows");
                    } else {
                        logger.debug("org.eclipse.swt.browser.DefaultType set to " + defaultBrowser + " for UI mode on Windows");
                    }
                    controller.createAndShowGUI();
                } catch (Exception e) {
                    UIUtils.errorMessageBox(new Shell(new Display()), e);
                }
            } else { // SWT_NATIVE_LIB_IN_JAVA_LIB_PATH not set
                Installer.install(argsMap);
                rerunWithSWTNativeLib(argsMap, args);
            }
        }
        return null;
    }
    
    public static void setExitCode(int codeVal) {
        exitCode = codeVal;
    }

    private static void rerunWithSWTNativeLib(Map<String, String> argsMap, String[] args) {
        String javaExecutablePath = getJavaExecutablePath();
        List<String> jvmArgs = buildJvmArguments(javaExecutablePath, argsMap, args);
        AppUtil.exec(jvmArgs, null);
    }
    
    private static String getJavaExecutablePath() {
        try {
            return ProcessHandle.current().info().command().orElseThrow();
        } catch (Exception e) {
            return System.getProperty("java.home") + FILE_SEPARATOR + "bin" + FILE_SEPARATOR + "java";
        }
    }
    
    private static List<String> buildJvmArguments(String javaExecutablePath, Map<String, String> argsMap, String[] args) {
        List<String> jvmArgs = new ArrayList<>();
        jvmArgs.add(javaExecutablePath);
        addJvmOptions(jvmArgs);
        addClasspath(jvmArgs);
        jvmArgs.add(DataLoaderRunner.class.getName());
        addApplicationArguments(jvmArgs, args, argsMap);
        return jvmArgs;
    }
    
    private static void addJvmOptions(List<String> jvmArgs) {
        if (AppUtil.isRunningOnMacOS()) {
            jvmArgs.add("-XstartOnFirstThread");
        }
        jvmArgs.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
    }
    
    private static void addClasspath(List<String> jvmArgs) {
        String classpath = System.getProperty("java.class.path");
        File swtJarFile = getSWTJarFileHandle();
        if (swtJarFile != null) {
            classpath = swtJarFile.getAbsolutePath() + PATH_SEPARATOR + classpath;
        }
        jvmArgs.add("-cp");
        jvmArgs.add(classpath);
    }
    
    private static void addApplicationArguments(List<String> jvmArgs, String[] args, Map<String, String> argsMap) {
        for (String arg : args) {
            jvmArgs.add(arg);
        }
        jvmArgs.add(AppConfig.CLI_OPTION_SWT_NATIVE_LIB_IN_JAVA_LIB_PATH + "=true");
        Proxy systemProxy = AppUtil.getSystemHttpsProxy(argsMap);
        if (systemProxy != null && systemProxy.address() instanceof InetSocketAddress) {
            InetSocketAddress addr = (InetSocketAddress) systemProxy.address();
            jvmArgs.add(AppConfig.CLI_OPTION_SYSTEM_PROXY_HOST + "=" + addr.getHostName());
            jvmArgs.add(AppConfig.CLI_OPTION_SYSTEM_PROXY_PORT + "=" + addr.getPort());
        }
    }
    private static String constructSwtJarNameFromOSAndArch(boolean skipOSAndArch) {
        String swtJarPrefix = "swt";
        String swtJarSuffix = "*.jar";
        
        String osTypeStr = "";
        if (AppUtil.getOSType() == AppUtil.OSType.WINDOWS) {
            osTypeStr = "win32";
        } else if (AppUtil.getOSType() == AppUtil.OSType.MACOSX) {
            osTypeStr = "mac";
        } else if (AppUtil.getOSType() == AppUtil.OSType.LINUX) {
            osTypeStr = "linux";
        } else {
            throw new RuntimeException("Unknown OS");
        }
        String archStr = System.getProperty("os.arch");

        if (archStr.toLowerCase().contains("amd")) {
            archStr = "x86_64";
        }
        
        // e.g. swtwin32_x86_64*.jar or swtmac_aarch64*.jar
        String pathStr = swtJarPrefix 
                + (skipOSAndArch ? "" : osTypeStr + "_" + archStr)
                + swtJarSuffix;
        
        return pathStr;
    }
    
    private static File getSWTJarFileHandleFromWildcard(String parentDirStr, String childFileStr) {
        if (parentDirStr == null || parentDirStr.isBlank() || childFileStr == null || childFileStr.isBlank()) {
            return null;
        }
        if (parentDirStr.contains("*")) {
            // path to the file has a wildcard. Assume that it is present only at the parent folder level
            String[] subpaths = parentDirStr.split("\\*");
            File grandparentDir = new File(subpaths[0]);
            File[] possibleParentDirs = grandparentDir.listFiles();
            for (File possibleParentDir : possibleParentDirs) {
                if (possibleParentDir.isDirectory()) {
                    File possibleSWTJarFile = getSWTJarFileHandleFromWildcard(
                            possibleParentDir.getAbsolutePath(), childFileStr);
                    if (possibleSWTJarFile != null) {
                        return possibleSWTJarFile;
                    }
                }
            }
        }
        File parentDir = new File(parentDirStr);
        if (!parentDir.exists()) {
            return null;
        }
        FileFilter fileFilter = WildcardFileFilter.builder().setWildcards(childFileStr).get();
        File[] files = parentDir.listFiles(fileFilter);
        if (files != null && files.length > 0) {
            return files[0];
        }
        return null;
    }

    private static File getSWTJarFileHandle() {
        for (String parentDir : getParentDirectories()) {
            for (boolean skipOSAndArch : new boolean[]{false, true}) {
                File swtJarFile = searchSWTJarInDirectory(parentDir, skipOSAndArch);
                if (swtJarFile != null) {
                    return swtJarFile;
                }
            }
        }
        return searchSWTJarInClasspath();
    }
    
    private static String[] getParentDirectories() {
        return new String[]{
            AppUtil.getDirContainingClassJar(DataLoaderRunner.class),
            ".",
            "..",
            LOCAL_SWT_DIR
        };
    }
    
    private static File searchSWTJarInDirectory(String parentDir, boolean skipOSAndArch) {
        if (parentDir == null) {
            return null;
        }
        File swtJarFile = getSWTJarFileHandleFromDir(parentDir, skipOSAndArch);
        if (swtJarFile == null) {
            swtJarFile = getSWTJarFileHandleFromDir(parentDir + "/*", skipOSAndArch);
        }
        return swtJarFile;
    }
    
    private static File searchSWTJarInClasspath() {
        File swtJarFile = getSWTJarFileFromClassPath(System.getenv("CLASSPATH"));
        return (swtJarFile != null) ? swtJarFile : getSWTJarFileFromClassPath(System.getProperty("java.class.path"));
    }
    
    private static File getSWTJarFileHandleFromDir(String dir, boolean skipOSAndArchVal) {
        String swtJarPathWithParentDirWildcard = constructSwtJarNameFromOSAndArch(skipOSAndArchVal);
        File swtJarFileHandle = getSWTJarFileHandleFromWildcard(dir, swtJarPathWithParentDirWildcard);
        if (swtJarFileHandle != null) {
            logger.debug("Found SWT jar at " + swtJarFileHandle.getAbsolutePath());
        }
        return swtJarFileHandle;
    }
    
    private static File getSWTJarFileFromClassPath(String classPathStr) {
        if (classPathStr == null) {
            return null;
        }
        logger.debug("CLASSPATH = " + classPathStr);
        if (classPathStr.toLowerCase().contains("swt")) {
            String[] pathValues = classPathStr.split(PATH_SEPARATOR);
            for (String pathVal : pathValues) {
                if (pathVal.toLowerCase().contains("swt")) {
                    File swtFile = new File(pathVal);
                    if (swtFile.exists()) {
                        return swtFile;
                    }
                }
            }
        }
        return null;
    }
}
