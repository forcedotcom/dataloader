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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.Map;

import com.salesforce.dataloader.config.Config;
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
    
    public static String getDirContainingClassJar(Class<?> aClass) {
        CodeSource codeSource = aClass.getProtectionDomain().getCodeSource();
    
        File jarFile = null;
    
        if (codeSource != null && codeSource.getLocation() != null) {
            try {
                jarFile = new File(codeSource.getLocation().toURI());
            } catch (URISyntaxException e) {
                return null;
            }
        } else {
          String path = aClass.getResource(aClass.getSimpleName() + ".class").getPath();
          String jarFilePath = path.substring(path.indexOf(":") + 1, path.indexOf("!"));
          try {
              jarFilePath = URLDecoder.decode(jarFilePath, "UTF-8");
          } catch (UnsupportedEncodingException e) {
              // fail silently;
          }
          jarFile = new File(jarFilePath);
        }
        return jarFile.getParentFile().getAbsolutePath();
    }
    
    public static void extractFromJar(String extractionArtifact, File extractionDestination) {
        try {
            InputStream link;
            link = Controller.class.getResourceAsStream(extractionArtifact);
            String parentDirStr = extractionDestination.getAbsoluteFile().getParent();
            File parentDir = Paths.get(parentDirStr).toFile();
            Files.createDirectories(parentDir.getAbsoluteFile().toPath());
            Files.copy(link, extractionDestination.getAbsoluteFile().toPath());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    private static String configurationsDir = null;
    public static synchronized String getConfigurationsDir() {
        if (configurationsDir == null) {
            setConfigurationsDir(null);
        }
        return configurationsDir;
    }
    
    public static synchronized void setConfigurationsDir(Map<String, String> argsMap) {
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

    private static String getDefaultConfigDir() {
        return AppUtil.getDirContainingClassJar(Config.class) 
                + "/" 
                + Config.CONFIG_DIR_DEFAULT_VALUE;
    }
}
