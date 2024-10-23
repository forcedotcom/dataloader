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
import java.util.Map;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.client.HttpClientTransport;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.controller.Controller;

public class DataLoaderRunner extends Thread {
    private static final String LOCAL_SWT_DIR = "./target/";
    private static final String PATH_SEPARATOR = System.getProperty("path.separator");
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static Logger logger;
    private static int exitCode = AppUtil.EXIT_CODE_NO_ERRORS;

    public void run() {
        // called just before the program closes
        HttpClientTransport.closeHttpClient();
    }

    public static void main(String[] args) {
        try {
            runApp(args, null);
            System.exit(exitCode);
        } catch (ExitException ex) {
            System.exit(ex.getExitCode());
        } finally {
            if (logger != null) {
                logger.debug("Number of server API invocations = " + HttpClientTransport.getServerInvocationCount());
            }
        }
        System.exit(exitCode);
    }
    
    public static IProcess runApp(String[] args, ILoaderProgress monitor) {
        Controller controller = null;
        Runtime.getRuntime().addShutdownHook(new DataLoaderRunner());
        try {
            controller = Controller.getInstance(AppUtil.convertCommandArgsArrayToArgMap(args));
        } catch (FactoryConfigurationError | Exception ex) {
            ex.printStackTrace();
            System.exit(AppUtil.EXIT_CODE_CLIENT_ERROR);
        }
        if (AppUtil.getAppRunMode() == AppUtil.APP_RUN_MODE.BATCH) {
            return ProcessRunner.runBatchMode(AppUtil.convertCommandArgsArrayToArgMap(args), monitor);
        } else if (AppUtil.getAppRunMode() == AppUtil.APP_RUN_MODE.ENCRYPT) {
            EncryptionUtil.main(args);
        } else {
            Map<String, String> argsMap = AppUtil.convertCommandArgsArrayToArgMap(args);
            /* Run in the UI mode, get the controller instance with batchMode == false */
            logger = DLLogManager.getLogger(DataLoaderRunner.class);
            Installer.install(argsMap);
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
                AppUtil.showBanner();
                rerunWithSWTNativeLib(args);
            }
        }
        return null;
    }
    
    public static void setExitCode(int codeVal) {
        exitCode = codeVal;
    }
    
    private static void rerunWithSWTNativeLib(String[] args) {
        String javaExecutablePath = null;
        try {
            javaExecutablePath = ProcessHandle.current()
                .info()
                .command()
                .orElseThrow();
        } catch (Exception e) {
            // fail silently
        }
        if (javaExecutablePath == null) {
            javaExecutablePath = System.getProperty("java.home")
                    + FILE_SEPARATOR + "bin" + FILE_SEPARATOR + "java";
        }
        // java command is the first argument
        ArrayList<String> jvmArgs = new ArrayList<String>(128);
        logger.debug("java executable path: " + javaExecutablePath);
        jvmArgs.add(javaExecutablePath);

        // JVM options
        // set -XstartOnFirstThread for MacOS
        String osName = System.getProperty("os.name").toLowerCase();
        if ((osName.contains("mac")) || (osName.startsWith("darwin"))) {
            jvmArgs.add("-XstartOnFirstThread");
            logger.debug("added JVM arg -XstartOnFirstThread");
        }
        
        // set JVM arguments
        // add JVM arguments specified in the command line
        jvmArgs.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
        File swtJarFileHandle = getSWTJarFileHandle();
        if (swtJarFileHandle == null) {
            logger.error("Unable to find SWT jar for " 
                    + System.getProperty("os.name") + " : "
                    + System.getProperty("os.arch"));
            System.exit(AppUtil.EXIT_CODE_CLIENT_ERROR);
        }
        
        // set classpath
        String classpath = System.getProperty("java.class.path");
        String SWTJarPath = swtJarFileHandle.getAbsolutePath();
        if (classpath != null && !classpath.isBlank()) {
            classpath = SWTJarPath + PATH_SEPARATOR + classpath;
        } else {
            classpath = SWTJarPath;
        }
        jvmArgs.add("-cp");
        jvmArgs.add(classpath);
        logger.debug("set java.class.path=" + classpath);
        
        // specify name of the class with main method
        jvmArgs.add(DataLoaderRunner.class.getName());
        logger.debug("added class to execute - " + DataLoaderRunner.class.getName());
        
        // specify application arguments
        logger.debug("added following arguments:");
        for (int i = 0; i < args.length; i++) {
          jvmArgs.add(args[i]);
          logger.debug("    " + args[i]);
        }
        
        // add the argument to indicate that JAVA_LIB_PATH has the folder containing SWT native libraries
        jvmArgs.add(AppConfig.CLI_OPTION_SWT_NATIVE_LIB_IN_JAVA_LIB_PATH + "=true");
        logger.debug("    " + AppConfig.CLI_OPTION_SWT_NATIVE_LIB_IN_JAVA_LIB_PATH + "=true");
        
        // set System proxy info as proxy server defaults
        String proxyHost = null;
        int proxyPort = 0;
        Proxy systemProxy = AppUtil.getSystemHttpsProxy(args);
        if (systemProxy != null) {
            InetSocketAddress addr = (InetSocketAddress) systemProxy.address();
    
            if (addr != null) {
                proxyHost = addr.getHostName();
                proxyPort = addr.getPort();
                jvmArgs.add(AppConfig.CLI_OPTION_SYSTEM_PROXY_HOST + "=" + proxyHost);
                jvmArgs.add(AppConfig.CLI_OPTION_SYSTEM_PROXY_PORT + "=" + proxyPort);
            }
        }
        AppUtil.exec(jvmArgs, null);
    }
    
    private static String constructSwtJarNameFromOSAndArch(boolean skipOSAndArch) {
        String swtJarPrefix = "swt";
        String swtJarSuffix = "*.jar";
        
        String osNameStr = getOSName();
        String archStr = System.getProperty("os.arch");

        if (archStr.toLowerCase().contains("amd")) {
            archStr = "x86_64";
        }
        
        // e.g. swtwin32_x86_64*.jar or swtmac_aarch64*.jar
        String pathStr = swtJarPrefix 
                + (skipOSAndArch ? "" : osNameStr + "_" + archStr)
                + swtJarSuffix;
        
        return pathStr;
    }

    private static String getOSName() {
        String osNameProperty = System.getProperty("os.name");
    
        if (osNameProperty == null) {
            throw new RuntimeException("os.name property is not set");
        }
        else {
            osNameProperty = osNameProperty.toLowerCase();
        }
    
        if (osNameProperty.contains("win")) {
           return "win32";
        } else if (osNameProperty.contains("mac")) {
           return "mac";
        } else if (osNameProperty.contains("linux") || osNameProperty.contains("nix")) {
           return "linux";
        } else {
           throw new RuntimeException("Unknown OS name: " + osNameProperty);
        }
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
        String[]parentDirOfSWTDirArray = 
                    {AppUtil.getDirContainingClassJar(DataLoaderRunner.class)
                     , "."
                     , ".."
                     , LOCAL_SWT_DIR
                    };
        Boolean[] skipOSAndArchInFileNameConstructionArray = {Boolean.FALSE, Boolean.TRUE};
        for (String parentDirOfSwtDir : parentDirOfSWTDirArray) {
            if (parentDirOfSwtDir == null) {
                continue;
            }
            for (Boolean skipOSAndArchVal : skipOSAndArchInFileNameConstructionArray) {
                File swtJarFileHandle = getSWTJarFileHandleFromDir(parentDirOfSwtDir, skipOSAndArchVal);
                if (swtJarFileHandle != null) {
                    return swtJarFileHandle;
                }
                // look into sub-directories
                swtJarFileHandle = getSWTJarFileHandleFromDir(parentDirOfSwtDir + "/*", skipOSAndArchVal);
                if (swtJarFileHandle != null) {
                    return swtJarFileHandle;
                }
            }
        }

        // try to get it from the CLASSPATH
        Map<String, String>envVars = System.getenv();
        String classPathStr = envVars.get("CLASSPATH");
        File swtJarFileHandle = getSWTJarFileFromClassPath(classPathStr);
        if (swtJarFileHandle != null) {
            return swtJarFileHandle;
        }
        classPathStr = System.getProperty("java.class.path");
        return getSWTJarFileFromClassPath(classPathStr);
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
