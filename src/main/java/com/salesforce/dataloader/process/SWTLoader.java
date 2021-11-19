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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;


/* based on https://github.com/CGJennings/jar-loader */

public class SWTLoader {
    private static Instrumentation inst;
    private static boolean loadedViaPreMain = false;

    private SWTLoader() {
    }

    public static synchronized void addToClassPath(File jarFile) throws IOException {
        
        if (jarFile == null) {
            throw new NullPointerException();
        }
        if (!jarFile.exists()) {
            throw new FileNotFoundException(jarFile.getAbsolutePath());
        }
        if (!jarFile.canRead()) {
            throw new IOException("can't read jar: " + jarFile.getAbsolutePath());
        }
        if (jarFile.isDirectory()) {
            throw new IOException("not a jar: " + jarFile.getAbsolutePath());
        }

        // add the jar using instrumentation, or fall back to reflection
        if (inst != null) {
            inst.appendToSystemClassLoaderSearch(new JarFile(jarFile));
            return;
        }
    }

    public static synchronized boolean isSupported() {
        try {
            return inst != null;
        } catch (Throwable t) {
        }
        return false;
    }

    /**
     * Returns a string that describes the strategy being used to add JAR files
     * to the class path. This is meant mainly to assist with debugging and
     * diagnosing client issues.
     *
     * @return returns {@code "none"} if no strategy was found, otherwise a
     *     short describing the method used; the value {@code "reflection"}
     *     indicates that a fallback not compatible with Java 9+ is being used
     */
    public static synchronized String getStrategy() {
        String strat = "none";
        if (inst != null) {
            strat = loadedViaPreMain ? "agent" : "agent (main)";
        } else {
            try {
                if (isSupported()) {
                    strat = "reflection";
                }
            } catch (Throwable t) {
            }
        }
        return strat;
    }

    /**
     * Called by the JRE. <em>Do not call this method from user code.</em>
     *
     * <p>
     * This method is automatically invoked when the JRE loads this class as an
     * agent using the option {@code -javaagent:jarPathOfThisClass}.
     *
     * <p>
     * For this to work the {@code MANIFEST.MF} file <strong>must</strong>
     * include the line {@code Premain-Class: ca.cgjennings.jvm.JarLoader}.
     *
     * @param agentArgs agent arguments; currently ignored
     * @param instrumentation provided by the JRE
     */
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        loadedViaPreMain = true;
        agentmain(agentArgs, instrumentation);
    }

    /**
     * Called by the JRE. <em>Do not call this method from user code.</em>
     *
     * <p>
     * This method is called when the agent is attached to a running process. In
     * practice, this is not how JarLoader is used, but it is implemented should
     * you need it.
     *
     * <p>
     * For this to work the {@code MANIFEST.MF} file <strong>must</strong>
     * include the line {@code Agent-Class: ca.cgjennings.jvm.JarLoader}.
     *
     * @param agentArgs agent arguments; currently ignored
     * @param instrumentation provided by the JRE
     */
    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation");
        }
        if (inst == null) {
            inst = instrumentation;
        }
    }
    
    public static String buildNameFromOSAndArch(String prefix, String suffix) {
        prefix = prefix == null ? "" : prefix;
        suffix = suffix == null ? "" : suffix;
        
        String osNameStr = getOSName();
        String archStr = System.getProperty("os.arch");
        if (osNameStr.equalsIgnoreCase("win32") && archStr.toLowerCase().contains("amd")) {
            archStr = "x86_64";
        }

        return prefix 
               + osNameStr + "_" + archStr
               + suffix;       
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
    
    public static String getSWTDir() {
        String SWTDirStr = buildNameFromOSAndArch("swt", "");
        return SWTDirStr;
    }
}