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

import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.ControllerInitializationException;
import com.salesforce.dataloader.ui.UIUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.salesforce.dataloader.client.HttpClientTransport;

public class DataLoaderRunner extends Thread {

    private static final String UI = "ui";
    private static final String RUN_MODE = "run.mode";
    private static final String RUN_MODE_BATCH = "batch";
    private static final String GMT_FOR_DATE_FIELD_VALUE = "datefield.usegmt";
    private static final String SWT_NATIVE_LIB_IN_JAVA_LIB_PATH = "swt.nativelib.inpath";
    private static final String LOCAL_SWT_DIR = "target/";
    private static boolean useGMTForDateFieldValue = false;
    private static Map<String, String> argNameValuePair;

    private static boolean isBatchMode() {        
        return argNameValuePair.containsKey(RUN_MODE) ?
                RUN_MODE_BATCH.equalsIgnoreCase(argNameValuePair.get(RUN_MODE)) : false;
    }
    
    public static boolean doUseGMTForDateFieldValue() {
        return useGMTForDateFieldValue;
    }
    
    private static void setUseGMTForDateFieldValue() {
        if (argNameValuePair.containsKey(GMT_FOR_DATE_FIELD_VALUE)) {
            if ("false".equalsIgnoreCase(argNameValuePair.get(GMT_FOR_DATE_FIELD_VALUE))) {
                useGMTForDateFieldValue = false;
            }
        }
    }
    
    public static void setUseGMTForDateFieldValue(boolean doUseGMT) {
        useGMTForDateFieldValue = doUseGMT;
    }

    public void run() {
        // called just before the program closes
        HttpClientTransport.closeConnections();
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new DataLoaderRunner());
        argNameValuePair = Controller.getArgMapFromArgArray(args);
        Controller.setConfigDir(args);
        setUseGMTForDateFieldValue();
        if (argNameValuePair.containsKey(SWT_NATIVE_LIB_IN_JAVA_LIB_PATH) 
                && "true".equalsIgnoreCase(argNameValuePair.get(SWT_NATIVE_LIB_IN_JAVA_LIB_PATH))){
            /* Run in the UI mode, get the controller instance with batchMode == false */
            try {
                String SWTDirStr = System.getProperty("java.library.path");
                if (SWTDirStr == null 
                        || SWTDirStr.isBlank() 
                        || SWTDirStr.equalsIgnoreCase("null")
                        || !(Files.exists(Paths.get(SWTDirStr)))) {
                    System.err.println("Unable to find SWT directory: " + SWTDirStr);
                    System.err.println("Native JRE for " 
                      + System.getProperty("os.name") + " : "
                      + System.getProperty("os.arch") + " not supported.");
                    System.err.println("Try JRE for the supported platform in emulation mode.");
                    System.exit(-1);
                }
                if (isBatchMode()) {
                    ProcessRunner.runBatchMode(args);
                } else {
                    Controller controller = Controller.getInstance(UI, false, args);
                    controller.createAndShowGUI();
                }
            } catch (ControllerInitializationException e) {
                UIUtils.errorMessageBox(new Shell(new Display()), e);
            }
        } else { // SWT_NATIVE_LIB_IN_JAVA_LIB_PATH not set
            rerunWithSWTNativeLib(args);
        }
    }
    
    private static void rerunWithSWTNativeLib(String[] args) {
        String separator = System.getProperty("file.separator");
        String classpath = System.getProperty("java.class.path");
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
                    + separator + "bin" + separator + "java";
        }
        // java command is the first argument
        ArrayList<String> jvmArgs = new ArrayList<String>(128);
        jvmArgs.add(javaExecutablePath);

        // JVM options
        // set -XstartOnFirstThread for MacOS
        String osName = System.getProperty("os.name").toLowerCase();
        if ((osName.contains("mac")) || (osName.startsWith("darwin"))) {
            jvmArgs.add("-XstartOnFirstThread");
        }
        
        // set JVM arguments
        String SWTDir = getSWTDir();
        jvmArgs.add("-Djava.library.path=" + SWTDir);
        jvmArgs.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
        
        // set classpath
        String pathSeparator = System.getProperty("path.separator");
        if (classpath != null && !classpath.isBlank()) {
            classpath = classpath + pathSeparator;
        }
        classpath = classpath + getSWTJarPath();
        jvmArgs.add("-cp");
        jvmArgs.add(classpath);
        
        // specify name of the class with main method
        jvmArgs.add(DataLoaderRunner.class.getName());
        
        // specify application arguments
        for (int i = 0; i < args.length; i++) {
          jvmArgs.add(args[i]);
        }
        
        // add the argument to indicate that JAVA_LIB_PATH has the directory containing SWT native libraries
        jvmArgs.add(SWT_NATIVE_LIB_IN_JAVA_LIB_PATH + "=true");
        ProcessBuilder processBuilder = new ProcessBuilder(jvmArgs);
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;

            while ((line = br.readLine()) != null) {
              System.out.println(line);
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    private static String buildPathStringFromOSAndArch(String prefix, String suffix, String version, String separator) {
        prefix = prefix == null ? "" : prefix;
        suffix = suffix == null ? "" : suffix;
        
        String osNameStr = getOSName();
        String archStr = System.getProperty("os.arch");

        if ((osNameStr.equalsIgnoreCase("win32")|| osNameStr.equalsIgnoreCase("linux"))
             && archStr.toLowerCase().contains("amd")) {
            archStr = "x86_64";
        }
        String pathStr = prefix 
                + osNameStr + "_" + archStr
                + separator + version
                + suffix;
        
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
    
    private static String getSWTDir() {
        String SWTDirStr = buildPathStringFromOSAndArch("swt", "", "", "");
        if (Files.exists(Paths.get(SWTDirStr))) {
            return SWTDirStr;
        }
        
        // Look in the parent directory - batch mode
        SWTDirStr = buildPathStringFromOSAndArch("../swt", "", "", "");
        if (Files.exists(Paths.get(SWTDirStr))) {
            return SWTDirStr;
        }
        
        SWTDirStr = buildPathStringFromOSAndArch(LOCAL_SWT_DIR + "swt", "", "", "");

        if (SWTDirStr == null) {
            System.err.println("Unable to find SWT directory for " 
                    + System.getProperty("os.name") + " : "
                    + System.getProperty("os.arch"));
            System.exit(-1); // did not find SWT directory. Can't continue execution.
        }
        return SWTDirStr;
    }
    
    private static String getSWTJarPath() {
        String SWTDirStr = getSWTDir();
        String SWTJarStr = buildPathStringFromOSAndArch("swt", "*.jar", "", "");
        
        File dir = new File(SWTDirStr);
        FileFilter fileFilter = new WildcardFileFilter(SWTJarStr);
        File[] files = dir.listFiles(fileFilter);
        if (files == null || files.length == 0) { // no jar file starting with swt found
            System.err.println("Unable to find SWT jar for " 
                    + System.getProperty("os.name") + " : "
                    + System.getProperty("os.arch"));
            System.exit(-1);
        }
        return files[0].getPath();
    }
}
