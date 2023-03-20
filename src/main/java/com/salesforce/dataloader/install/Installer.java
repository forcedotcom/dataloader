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
package com.salesforce.dataloader.install;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.salesforce.dataloader.util.AppUtil;

public class Installer extends Thread {
    private static final String USERHOME=System.getProperty("user.home");
    private static final String PATH_SEPARATOR = System.getProperty("file.separator");
    private static final String CREATE_DEKSTOP_SHORTCUT_ON_WINDOWS = ":createDesktopShortcut";
    private static final String CREATE_START_MENU_SHORTCUT_ON_WINDOWS = ":createStartMenuShortcut";

    private static String INSTALLATION_ABSOLUTE_PATH;
    private static Logger logger;
    private final static String TOBE_INSTALLED_ABSOLUTE_PATH;
    
    static {
        TOBE_INSTALLED_ABSOLUTE_PATH = AppUtil.getDirContainingClassJar(Installer.class);
    }
    
    public void run() {
        System.out.println("Data Loader installation is quitting.");
    }

    public static void main(String[] args) {
        try {
            Runtime.getRuntime().addShutdownHook(new Installer());
            try {
                AppUtil.initializeLog(AppUtil.getArgMapFromArgArray(args));
            } catch (FactoryConfigurationError | IOException e1) {
                System.err.println("Unable to initialize log: " + e1.getMessage());
            }
            logger = LogManager.getLogger(Installer.class);
            boolean hideBanner = false;
            boolean skipCopyArtifacts = false;
            boolean skipCreateDesktopShortcut = false;
            boolean skipCreateStartMenuShortcut = false;
            boolean skipCreateAppsDirShortcut = false;
            
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-b":
                        hideBanner = true;
                        continue;
                    case "-c":
                        skipCopyArtifacts = true;
                        continue;
                    case "-d":
                        skipCreateDesktopShortcut = true;
                        continue;
                    case "-s":
                        skipCreateStartMenuShortcut = true;
                        continue;
                    case "-a":
                        skipCreateAppsDirShortcut = true;
                        continue;
                    default:
                        continue;
                }
            }
            if (!hideBanner) {
                logger.debug("going to show banner");
                AppUtil.showBanner();
            }
            if (!skipCopyArtifacts) {
                logger.debug("going to extract artifacts from jar");
                extractOSSpecificArtifactsFromJar();
                logger.debug("going to select installation directory");
                selectInstallationDir();
                logger.debug("going to copy artifacts");
                copyArtifacts();
                configureOSSpecificArtifactsPostCopy();
            }
            if (!skipCreateDesktopShortcut) {
                logger.debug("going to create desktop shortcut");
                createDesktopShortcut();
            }
            if (!skipCreateStartMenuShortcut && AppUtil.isRunningOnWindows()) {
                logger.debug("going to create start menu shortcut");
                createStartMenuShortcut();
            }
            if (!skipCreateAppsDirShortcut && AppUtil.isRunningOnMacOS()) {
                logger.debug("going to create Applications directory shortcut");
                createAppsDirShortcut();
            }
        } catch (Exception ex) {
            handleException(ex, Level.FATAL);
            System.exit(-1);            
        }
    }
        
    private static void selectInstallationDir() throws IOException {
        
        System.out.println("Data Loader installation requires you to provide an installation directory to create a version-specific subdirectory for the installation artifacts.");
        System.out.println("It uses '" + USERHOME + PATH_SEPARATOR + "<relative path>' as the installation directory if you provide a relative path for the installation directory.");
        System.out.println("");
        
        String installationDir = promptAndGetUserInput("Provide the installation directory [default: dataloader] : ");
        if (installationDir.isBlank()) {
            installationDir = "dataloader";
        }
        logger.debug("installation directory: " + installationDir);
        String installationPathSuffix = installationDir + PATH_SEPARATOR + "v" + AppUtil.DATALOADER_VERSION;
        if (installationDir.startsWith(PATH_SEPARATOR) 
             || (AppUtil.isRunningOnWindows() && installationDir.indexOf(':') == 1 && installationDir.indexOf(PATH_SEPARATOR) == 2)) {
            // Absolute path specified. 
            // Absolute path on Mac and Linux start with PATH_SEPARATOR
            // Absolute path on Windows starts with <Single character drive letter>:\. For example, "C:\"
            INSTALLATION_ABSOLUTE_PATH = installationPathSuffix;
        } else {
            INSTALLATION_ABSOLUTE_PATH = USERHOME + PATH_SEPARATOR + installationPathSuffix;
        }
        logger.debug("installation directory absolute path: " + INSTALLATION_ABSOLUTE_PATH);
        System.out.println("Data Loader v" + AppUtil.DATALOADER_VERSION + " will be installed in: " + INSTALLATION_ABSOLUTE_PATH);
    }
    
    private static void copyArtifacts() throws Exception {
        Path installationDirPath = Paths.get(INSTALLATION_ABSOLUTE_PATH);
        if (Files.exists(installationDirPath)) {
            for (;;) {
                System.out.println("");
                System.out.println("Do you want to overwrite previously installed versions of Data Loader");
                System.out.println("v" + AppUtil.DATALOADER_VERSION + " and configurations in '" + INSTALLATION_ABSOLUTE_PATH + "'?");
                System.out.println("If not, installation will quit and you can restart installation using");
                String input = promptAndGetUserInput("another directory.[Yes/No] ");
                if (input.toLowerCase().equals("yes") || input.toLowerCase().equals("y")) {
                    System.out.println("Deleting existing Data Loader v" + AppUtil.DATALOADER_VERSION + "... ");
                    logger.debug("going to delete " + INSTALLATION_ABSOLUTE_PATH);
                    FileUtils.deleteDirectory(new File(INSTALLATION_ABSOLUTE_PATH));
                    break;
                } else if (input.toLowerCase().equals("no") || input.toLowerCase().equals("n")) {
                    System.exit(0);                  
                } else {
                    System.out.println("Type Yes or No.");
                }
            }
        }
        String installationSourceDir = ".";
        installationSourceDir = new File(Installer.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI()).getParent();
        logger.debug("going to create " + INSTALLATION_ABSOLUTE_PATH);
        createDir(INSTALLATION_ABSOLUTE_PATH);
        logger.debug("going to copy contents of " + installationSourceDir + " to " + INSTALLATION_ABSOLUTE_PATH);
        
        FileUtils.copyDirectory(new File(installationSourceDir), new File(INSTALLATION_ABSOLUTE_PATH));
        
        logger.debug("going to delete \\.* files from " + INSTALLATION_ABSOLUTE_PATH);
        deleteFilesFromDir(INSTALLATION_ABSOLUTE_PATH, "\\.*");
        logger.debug("going to delete install.* files from " + INSTALLATION_ABSOLUTE_PATH);
        deleteFilesFromDir(INSTALLATION_ABSOLUTE_PATH, "install.(.*)");
        logger.debug("going to delete META-INF from " + INSTALLATION_ABSOLUTE_PATH);
        deleteFilesFromDir(INSTALLATION_ABSOLUTE_PATH, "META-INF");
        logger.debug("going to delete zip files from " + INSTALLATION_ABSOLUTE_PATH);
        deleteFilesFromDir(INSTALLATION_ABSOLUTE_PATH, ".*.zip");
    }
    
    private static String promptAndGetUserInput(String prompt) throws IOException {
        if (prompt == null || prompt.isBlank()) {
            prompt = "Provide input: ";
        }
        System.out.print(prompt);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input = "";
        // Reading data using readLine
        input = reader.readLine();
        return input;
    }
    
    private static void deleteFilesFromDir(String directoryName, String filePattern) throws IOException {
        File directory = new File(directoryName);
        if (!directory.exists()) {
            return;
        }
        final File[] files = directory.listFiles( new FilenameFilter() {
            @Override
            public boolean accept( final File dir,
                                   final String name ) {
                boolean match = name.matches(filePattern);
                return match;
            }
        } );
        for ( final File file : files ) {
            if (file.isDirectory()) {
                FileUtils.deleteDirectory(file);
            } else if ( !file.delete() ) {
                logger.error("Can't remove " + file.getAbsolutePath());
            }
        }
    }
    
    interface ShortcutCreatorInterface {
        public void create() throws Exception;
    }
    
    private static void createShortcut(String prompt, ShortcutCreatorInterface shortcutCreator) {
        for (;;) {
            System.out.println("");
            String input = "";
            try {
                input = promptAndGetUserInput(prompt);
            } catch (IOException e) {
                System.err.println("Can't read your response. Try again.");
                handleException(e, Level.ERROR);
            }
            if ("yes".toLowerCase().equals(input) || "y".toLowerCase().equals(input)) {
                try {
                    shortcutCreator.create();
                } catch (Exception ex) {
                    System.err.println("Unable to create shortcut");
                    handleException(ex, Level.ERROR);
                }
                break;
            } else if ("no".toLowerCase().equals(input) || "n".toLowerCase().equals(input)) {
                return;                  
            } else {
                System.out.println("Type Yes or No.");
            }
        }
    }
    
    private static void createDesktopShortcut() {
        final String PROMPT = "Do you want to create a Desktop shortcut? [Yes/No] ";
        if (AppUtil.isRunningOnWindows()) {
            createShortcut(PROMPT,
                    new ShortcutCreatorInterface() {
                        public void create() throws Exception {
                            createShortcutOnWindows(CREATE_DEKSTOP_SHORTCUT_ON_WINDOWS);
                        }
            });
        } else if (AppUtil.isRunningOnMacOS()) {
            createShortcut(PROMPT,
                    new ShortcutCreatorInterface() {
                        public void create()  throws Exception {
                                createSymLink(USERHOME + "/Desktop/DataLoader " + AppUtil.DATALOADER_VERSION,
                                        INSTALLATION_ABSOLUTE_PATH + "/dataloader.app");
                        }
            });
        }
    }
    
    private static void createAppsDirShortcut() {
        final String PROMPT = "Do you want to create a shortcut in Applications directory? [Yes/No] ";

        if (AppUtil.isRunningOnMacOS()) {
            createShortcut(PROMPT,
                    new ShortcutCreatorInterface() {
                        public void create() throws Exception {
                            createSymLink("/Applications/DataLoader " + AppUtil.DATALOADER_VERSION,
                                        INSTALLATION_ABSOLUTE_PATH + "/dataloader.app");
                        }
            });
        }
    }
    
    private static void createStartMenuShortcut() {
        final String PROMPT = "Do you want to create a Start menu shortcut? [Yes/No] ";

        if (AppUtil.isRunningOnWindows()) {
            createShortcut(PROMPT,
                    new ShortcutCreatorInterface() {
                        public void create() throws Exception {
                            String APPDATA = System.getenv("APPDATA");
                            String SALESFORCE_START_MENU_DIR = APPDATA + "\\Microsoft\\Windows\\Start Menu\\Programs\\Salesforce\\" ;
                            createDir(SALESFORCE_START_MENU_DIR);
                            createShortcutOnWindows(CREATE_START_MENU_SHORTCUT_ON_WINDOWS);
                        }
            });
        }
    }
    
    private static void createSymLink(String symlink, String target) throws IOException {
        Path symlinkPath = Paths.get(symlink);
        if (Files.exists(symlinkPath)) {
            logger.debug("going to delete existing symlink: " + symlink);
            Files.delete(symlinkPath);
        }
        logger.debug("going to create symlink: " + symlink + " pointing to " + target);
        Files.createSymbolicLink(symlinkPath, Paths.get(target));
    }
    
    private static void createShortcutOnWindows(final String shortcutCommand) throws IOException, InterruptedException {
        String redirectWinCmdOutputStr = "";
        if (logger.getLevel() == Level.DEBUG) {
            redirectWinCmdOutputStr = " > debug.txt 2>&1";
        }
        String command = "cmd /c call \"" + INSTALLATION_ABSOLUTE_PATH + "\\util\\util.bat\"" 
                + " " + shortcutCommand + " \"" + INSTALLATION_ABSOLUTE_PATH + "\"" + redirectWinCmdOutputStr;
        logger.debug("going to execute windows command: ");
        logger.debug(command);
        Process p = Runtime.getRuntime().exec(command);
        int exitVal = p.waitFor();
        logger.debug("windows command exited with exit code: " + exitVal);
    }
    
    private static void configureOSSpecificArtifactsPostCopy() throws IOException {
        if (AppUtil.isRunningOnWindows()) {
            configureWindowsArtifactsPostCopy();
        } else if (AppUtil.isRunningOnMacOS()) {
            configureMacOSArtifactsPostCopy();
        } else if (AppUtil.isRunningOnLinux()) {
            configureLinuxArtifactsPostCopy();
        }
    }
    
    private static void configureMacOSArtifactsPostCopy() throws IOException {
        final String MACOS_PACKAGE_BASE = INSTALLATION_ABSOLUTE_PATH + "/dataloader.app/Contents";
        final String PATH_TO_DL_EXECUTABLE_ON_MAC = MACOS_PACKAGE_BASE + "/MacOS/dataloader";
 
        // delete unnecessary artifacts
        logger.debug("going to delete dataloader.ico from " + INSTALLATION_ABSOLUTE_PATH);
        deleteFilesFromDir(INSTALLATION_ABSOLUTE_PATH, "dataloader.ico");
        deleteFilesFromDir(INSTALLATION_ABSOLUTE_PATH, "swtlinux(.*)");
        deleteFilesFromDir(INSTALLATION_ABSOLUTE_PATH, "swtwin(.*)");
        deleteFilesFromDir(INSTALLATION_ABSOLUTE_PATH + "/util", "(.*).bat");

        // create a soft link from <INSTALLATION_ABSOLUTE_PATH>/dataloader.app/Contents/MacOS/dataloader to 
        // <INSTALLATION_ABSOLUTE_PATH>/dataloader_console
        logger.debug("going to create symlink from " 
                    + 
                    PATH_TO_DL_EXECUTABLE_ON_MAC
                    + " to "
                    + INSTALLATION_ABSOLUTE_PATH + "/dataloader_console");
        logger.debug("going to create " + MACOS_PACKAGE_BASE + "/MacOS");
        createDir(MACOS_PACKAGE_BASE + "/MacOS");
        createSymLink(PATH_TO_DL_EXECUTABLE_ON_MAC,
                        INSTALLATION_ABSOLUTE_PATH + "/dataloader_console");
    }
    
    private static void configureWindowsArtifactsPostCopy() throws IOException {
        deleteFilesFromDir(INSTALLATION_ABSOLUTE_PATH, "swtmac(.*)");
        deleteFilesFromDir(INSTALLATION_ABSOLUTE_PATH, "swtlinux(.*)");
        deleteFilesFromDir(INSTALLATION_ABSOLUTE_PATH + "/util", "(.*).sh");
    }
    
    private static void configureLinuxArtifactsPostCopy() throws IOException {
        deleteFilesFromDir(INSTALLATION_ABSOLUTE_PATH, "swtmac(.*)");
        deleteFilesFromDir(INSTALLATION_ABSOLUTE_PATH, "swtwin(.*)");
        Files.move(Paths.get(INSTALLATION_ABSOLUTE_PATH + "/dataloader_console"),
                Paths.get(INSTALLATION_ABSOLUTE_PATH + "/dataloader.sh"));
        deleteFilesFromDir(INSTALLATION_ABSOLUTE_PATH + "/util", "(.*).bat");
    }

    private static void createDir(String dirPath) throws IOException {
        Files.createDirectories(Paths.get(dirPath));
    }

    private static void extractOSSpecificArtifactsFromJar() throws URISyntaxException, IOException {
        if (AppUtil.isRunningOnWindows()) {
            AppUtil.extractDirFromJar("win", TOBE_INSTALLED_ABSOLUTE_PATH, true);
       } else {
            AppUtil.extractDirFromJar("mac", TOBE_INSTALLED_ABSOLUTE_PATH, true);
       }
        AppUtil.extractDirFromJar("samples", TOBE_INSTALLED_ABSOLUTE_PATH, false);
    }
    
    private static void handleException(Exception ex, Level level) {
        if (logger != null) {
            logger.log(level, ex.getMessage());
            if (logger.getLevel() == Level.DEBUG) {
                ex.printStackTrace();
            }
        } else {
            ex.printStackTrace();
        }
    }
}
