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
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.util.AppUtil;

public class Installer extends Thread {
    private static final String USERHOME=System.getProperty("user.home");
    private static final String PATH_SEPARATOR = System.getProperty("file.separator");
    private static final String CREATE_DEKSTOP_SHORTCUT_ON_WINDOWS = ":createDesktopShortcut";
    private static final String CREATE_START_MENU_SHORTCUT_ON_WINDOWS = ":createStartMenuShortcut";

    private static Logger logger;
    
    public void run() {
        System.out.println(Messages.getMessage(Installer.class, "exitMessage"));
    }

    public static void install(String[] args) {
        try {
            String installationDir = ".";
            final String INSTALL_COMPLETED_FILE = ".sf_dl_install";
            Runtime.getRuntime().addShutdownHook(new Installer());
            setLogger();
            installationDir = new File(Installer.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getParent();
            Path installFilePath = Paths.get(installationDir + PATH_SEPARATOR + INSTALL_COMPLETED_FILE);
            if (Files.exists(installFilePath) && AppUtil.getAppRunMode() != AppUtil.APP_RUN_MODE.INSTALL) {
                // installation completed
                return;
            }
            boolean hideBanner = false;
            boolean skipCopyArtifacts = false;
            boolean skipCreateDesktopShortcut = false;
            boolean skipCreateStartMenuShortcut = false;
            boolean skipCreateAppsDirShortcut = false;
            
            if (!hideBanner) {
                logger.debug("going to show banner");
                AppUtil.showBanner();
            }
            
            skipCopyArtifacts = promptCurrentInstallationFolder();
            if (!skipCopyArtifacts) {
                logger.debug("going to select installation folder");
                installationDir = selectInstallationDir();
                logger.debug("going to copy artifacts");
                copyArtifacts(installationDir);
            }
            extractInstallationArtifactsFromJar(installationDir);
            if (!skipCreateDesktopShortcut) {
                logger.debug("going to create desktop shortcut");
                createDesktopShortcut(installationDir);
            }
            if (!skipCreateStartMenuShortcut && AppUtil.isRunningOnWindows()) {
                logger.debug("going to create start menu shortcut");
                createStartMenuShortcut(installationDir);
            }
            if (!skipCreateAppsDirShortcut && AppUtil.isRunningOnMacOS()) {
                logger.debug("going to create Applications folder shortcut");
                createAppsDirShortcut(installationDir);
            }
            installFilePath = Paths.get(installationDir + PATH_SEPARATOR + INSTALL_COMPLETED_FILE);
            Files.createFile(installFilePath);
            if (AppUtil.isRunningOnWindows()) {
                Files.setAttribute(installFilePath, "dos:hidden", true);
            }
            if (!skipCopyArtifacts) {
                System.exit(0);
            }
        } catch (Exception ex) {
            handleException(ex, Level.FATAL);
            System.exit(-1);            
        }
    }
    
    private static boolean promptCurrentInstallationFolder() throws IOException {
        String currentExecutionFolder = AppUtil.getDirContainingClassJar(Installer.class);
        return loopingYesNoPrompt(Messages.getMessage(Installer.class, "promptCurrentInstallationFolder", currentExecutionFolder));
    }
        
    private static String selectInstallationDir() throws IOException {
        String installationDir = "";
        System.out.println(Messages.getMessage(Installer.class, "initialMessage", USERHOME + PATH_SEPARATOR));
        String installationDirRoot = promptAndGetUserInput(Messages.getMessage(Installer.class, "promptInstallationFolder"));
        if (installationDirRoot.isBlank()) {
            installationDirRoot = "dataloader";
        }
        logger.debug("installation folder: " + installationDirRoot);
        String installationPathSuffix = installationDirRoot + PATH_SEPARATOR + "v" + AppUtil.DATALOADER_VERSION;
        if (installationDirRoot.startsWith(PATH_SEPARATOR) 
             || (AppUtil.isRunningOnWindows() && installationDirRoot.indexOf(':') == 1 && installationDirRoot.indexOf(PATH_SEPARATOR) == 2)) {
            // Absolute path specified. 
            // Absolute path on Mac and Linux start with PATH_SEPARATOR
            // Absolute path on Windows starts with <Single character drive letter>:\. For example, "C:\"
            installationDir = installationPathSuffix;
        } else {
            installationDir = USERHOME + PATH_SEPARATOR + installationPathSuffix;
        }
        logger.debug("installation folder absolute path: " + installationDir);
        System.out.println(Messages.getMessage(Installer.class, "installationDirConfirmation", AppUtil.DATALOADER_VERSION, installationDir));
        return installationDir;
    }
    
    private static void copyArtifacts(String installationDir) throws Exception {
        Path installationDirPath = Paths.get(installationDir);
        if (Files.exists(installationDirPath)) {
            final String prompt = Messages.getMessage(Installer.class, "overwriteInstallationDirPrompt", AppUtil.DATALOADER_VERSION, installationDir);
            if (loopingYesNoPrompt(prompt)) {
                System.out.println(Messages.getMessage(Installer.class, "deletionInProgressMessage", AppUtil.DATALOADER_VERSION));
                Messages.getMessage(Installer.class, "initialMessage");
                logger.debug("going to delete " + installationDir);
                FileUtils.deleteDirectory(new File(installationDir));
            } else {
                System.exit(0);
            }
        }
        String installationSourceDir = ".";
        installationSourceDir = new File(Installer.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI()).getParent();
        logger.debug("going to create " + installationDir);
        createDir(installationDir);
        logger.debug("going to copy contents of " + installationSourceDir + " to " + installationDir);
        
        String classpath = System.getProperty("java.class.path");
        String[] jars = classpath.split(System.getProperty("path.separator"));
        String dataloaderJar = null;
        for (String jar : jars) {
            if (jar.contains("dataloader")) {
                dataloaderJar = jar;
                break;
            }
        }
        if (dataloaderJar == null) {
            logger.fatal("Did not find Data Loader jar in the installation artifacts. Unable to install Data Loader");
            System.exit(-1);
        }
        FileUtils.copyFileToDirectory(new File(dataloaderJar), new File(installationDir));
        logger.debug("going to delete \\.* files from " + installationDir);
        deleteFilesFromDir(installationDir, "\\.*");
        logger.debug("going to delete install.* files from " + installationDir);
        deleteFilesFromDir(installationDir, "install.(.*)");
        logger.debug("going to delete META-INF from " + installationDir);
        deleteFilesFromDir(installationDir, "META-INF");
        logger.debug("going to delete zip files from " + installationDir);
        deleteFilesFromDir(installationDir, ".*.zip");
    }
    
    private static boolean loopingYesNoPrompt(String prompt) throws IOException {
        for (;;) {
            System.out.println("");
            String input = promptAndGetUserInput(prompt);
            if (input == null || input.isBlank()) {
                System.out.println(Messages.getMessage(Installer.class, "reprompt"));
            } else if (Messages.getMessage(Installer.class, "promptAnswerYes").toLowerCase().startsWith(input.toLowerCase())) {
                return true;
            } else if (Messages.getMessage(Installer.class, "promptAnswerNo").toLowerCase().startsWith(input.toLowerCase())) {
                return false;
            } else {
                System.out.println(Messages.getMessage(Installer.class, "reprompt"));
            }
        }
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
    
    private static void deleteFilesFromDir(String folderName, String filePattern) throws IOException {
        File folder = new File(folderName);
        if (!folder.exists()) {
            return;
        }
        final File[] files = folder.listFiles( new FilenameFilter() {
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
    
    private static void createShortcut(String prompt, ShortcutCreatorInterface shortcutCreator, String success) {
        for (;;) {
            System.out.println("");
            String input = "";
            try {
                input = promptAndGetUserInput(prompt);
            } catch (IOException e) {
                System.err.println(Messages.getMessage(Installer.class, "responseReadError"));
                handleException(e, Level.ERROR);
            }
            if (input == null || input.isBlank()) {
                System.out.println(Messages.getMessage(Installer.class, "reprompt"));
            } else if (Messages.getMessage(Installer.class, "promptAnswerYes").toLowerCase().startsWith(input.toLowerCase())) {
                try {
                    shortcutCreator.create();
                    if (success != null && !success.isBlank()) {
                        System.out.println(success);
                    }
                } catch (Exception ex) {
                    System.err.println(Messages.getMessage(Installer.class, "shortcutCreateError"));
                    handleException(ex, Level.ERROR);
                }
                break;
            } else if (Messages.getMessage(Installer.class, "promptAnswerNo").toLowerCase().startsWith(input.toLowerCase())) {
                return;                  
            } else {
                System.out.println(Messages.getMessage(Installer.class, "reprompt"));
            }
        }
    }
    
    private static void createDesktopShortcut(String installationDir) {
        final String PROMPT = Messages.getMessage(Installer.class, "createDesktopShortcutPrompt");
        final String SUCCESS = Messages.getMessage(Installer.class, "successCreateDesktopShortcut");
        if (AppUtil.isRunningOnWindows()) {
            createShortcut(PROMPT,
                    new ShortcutCreatorInterface() {
                        public void create() throws Exception {
                            createShortcutOnWindows(CREATE_DEKSTOP_SHORTCUT_ON_WINDOWS, installationDir);
                        }
            }, SUCCESS);
        } else if (AppUtil.isRunningOnMacOS()) {
            createShortcut(PROMPT,
                    new ShortcutCreatorInterface() {
                        public void create()  throws Exception {
                                createSymLink(USERHOME + "/Desktop/DataLoader " + AppUtil.DATALOADER_VERSION,
                                        installationDir + "/dataloader.app", true);
                        }
            }, SUCCESS);
        }
    }
    
    private static void createAppsDirShortcut(String installationDir) {
        final String PROMPT =  Messages.getMessage(Installer.class, "createApplicationsDirShortcutPrompt");
        final String SUCCESS =  Messages.getMessage(Installer.class, "successCreateApplicationsDirShortcut");

        if (AppUtil.isRunningOnMacOS()) {
            createShortcut(PROMPT,
                    new ShortcutCreatorInterface() {
                        public void create() throws Exception {
                            createSymLink("/Applications/DataLoader " + AppUtil.DATALOADER_VERSION,
                                    installationDir + "/dataloader.app", true);
                        }
            }, SUCCESS);
        }
    }
    
    private static void createStartMenuShortcut(String installationDir) {
        final String PROMPT = Messages.getMessage(Installer.class, "createStartMenuShortcutPrompt");
        final String SUCCESS = Messages.getMessage(Installer.class, "successCreateStartMenuShortcut");

        if (AppUtil.isRunningOnWindows()) {
            createShortcut(PROMPT,
                    new ShortcutCreatorInterface() {
                        public void create() throws Exception {
                            String APPDATA = System.getenv("APPDATA");
                            String SALESFORCE_START_MENU_DIR = APPDATA + "\\Microsoft\\Windows\\Start Menu\\Programs\\Salesforce\\" ;
                            createDir(SALESFORCE_START_MENU_DIR);
                            createShortcutOnWindows(CREATE_START_MENU_SHORTCUT_ON_WINDOWS, installationDir);
                        }
            }, SUCCESS);
        }
    }
    
    private static void createSymLink(String symlink, String target, boolean deleteExisting) throws IOException {
        Path symlinkPath = Paths.get(symlink);
        if (Files.exists(symlinkPath)) {
            if (deleteExisting) {
                logger.debug("Deleting existing symlink " + symlink);
                Files.delete(symlinkPath);
            } else {
                logger.debug("Symlink " + symlink + " exists. Skipping linking it to " + target);
                return;
            }
        }
        logger.debug("going to create symlink: " + symlink + " pointing to " + target);
        Files.createSymbolicLink(symlinkPath, Paths.get(target));
    }
    
    private static void createShortcutOnWindows(final String shortcutCommand, String installationDir) throws IOException, InterruptedException {
        String redirectWinCmdOutputStr = "";
        if (logger.getLevel() == Level.DEBUG) {
            redirectWinCmdOutputStr = " > debug.txt 2>&1";
        }
        String command = "cmd /c call \"" + installationDir + "\\util\\util.bat\"" 
                + " " + shortcutCommand + " \"" + installationDir + "\"" + redirectWinCmdOutputStr;
        logger.debug("going to execute windows command: ");
        logger.debug(command);
        Process p = Runtime.getRuntime().exec(command);
        int exitVal = p.waitFor();
        logger.debug("windows command exited with exit code: " + exitVal);
    }
    
    private static void configureOSSpecificInstallationArtifactsPostCopy(String installationDir) throws IOException {
        if (AppUtil.isRunningOnWindows()) {
            configureWindowsArtifactsPostCopy(installationDir);
        } else if (AppUtil.isRunningOnMacOS()) {
            configureMacOSArtifactsPostCopy(installationDir);
        } else if (AppUtil.isRunningOnLinux()) {
            configureLinuxArtifactsPostCopy(installationDir);
        }
    }
    
    private static void configureMacOSArtifactsPostCopy(String installationDir) throws IOException {
        final String MACOS_PACKAGE_BASE = installationDir + "/dataloader.app/Contents";
        final String PATH_TO_DL_EXECUTABLE_ON_MAC = MACOS_PACKAGE_BASE + "/MacOS/dataloader";
 
        // delete unnecessary artifacts
        logger.debug("going to delete dataloader.ico from " + installationDir);
        deleteFilesFromDir(installationDir + "/util", "(.*).bat");

        // create a soft link from <INSTALLATION_ABSOLUTE_PATH>/dataloader.app/Contents/MacOS/dataloader to 
        // <INSTALLATION_ABSOLUTE_PATH>/dataloader_console
        logger.debug("going to create symlink from " 
                    + 
                    PATH_TO_DL_EXECUTABLE_ON_MAC
                    + " to "
                    + installationDir + "/dataloader_console");
        logger.debug("going to create " + MACOS_PACKAGE_BASE + "/MacOS");
        createDir(MACOS_PACKAGE_BASE + "/MacOS");
        createSymLink(PATH_TO_DL_EXECUTABLE_ON_MAC,
                installationDir + "/dataloader_console", true);
    }
    
    private static void configureWindowsArtifactsPostCopy(String installationDir) throws IOException {
        deleteFilesFromDir(installationDir + "/util", "(.*).sh");
    }
    
    private static void configureLinuxArtifactsPostCopy(String installationDir) throws IOException {
        try {
            if (Files.exists(Paths.get(installationDir + "/dataloader_console"))) {
                Files.move(Paths.get(installationDir + "/dataloader_console"),
                    Paths.get(installationDir + "/dataloader.sh"));
            }
        } catch (InvalidPathException ex) {
            // do nothing - dataloader_console not found in the path
        }
        deleteFilesFromDir(installationDir + "/util", "(.*).bat");
    }

    private static void createDir(String dirPath) throws IOException {
        Files.createDirectories(Paths.get(dirPath));
    }

    public static void extractInstallationArtifactsFromJar(String installationDir) throws URISyntaxException, IOException {
        setLogger();
        AppUtil.extractDirFromJar("samples", installationDir, false);
        AppUtil.extractDirFromJar("configs", installationDir, false);
        String osSpecificExtractionPrefix = "mac/";
        if (AppUtil.isRunningOnWindows()) {
            osSpecificExtractionPrefix = "win/";
        } else if (AppUtil.isRunningOnLinux()) {
            osSpecificExtractionPrefix = "linux/";
        }
        AppUtil.extractDirFromJar(osSpecificExtractionPrefix, installationDir, true);
        configureOSSpecificInstallationArtifactsPostCopy(installationDir);
    }
    
    private static void handleException(Throwable ex, Level level) {
        if (logger != null) {
            logger.log(level, "Installer :", ex);
        } else {
            ex.printStackTrace();
        }
    }

    private static void setLogger() {
        if (logger == null) {
            logger = LogManager.getLogger(Installer.class);
        }
    }
}