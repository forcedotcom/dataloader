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

public class Installer {
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

    public static void main(String[] args) {
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
            try {
                extractOSSpecificArtifactsFromJar();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                logger.fatal("Unable to extract OS-specific files from uber jar");
                System.exit(-1);
            }
            logger.debug("going to select installation directory");
            selectInstallationDir();
            logger.debug("going to copy artifacts");
            copyArtifacts();
            try {
                configureOSSpecificArtifactsPostCopy();
            } catch (Exception ex) {
                logger.fatal(ex.getMessage());
                System.exit(-1);
            }
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
    }
        
    private static void selectInstallationDir() {
        
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
    
    private static void copyArtifacts() {
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
                    try {
                        logger.debug("going to delete " + INSTALLATION_ABSOLUTE_PATH);
                        FileUtils.deleteDirectory(new File(INSTALLATION_ABSOLUTE_PATH));
                    } catch (IOException e) {
                        logger.fatal("Unable to delete existing Data Loader installation at " + INSTALLATION_ABSOLUTE_PATH);
                        logger.fatal(e.getMessage());
                        System.exit(-1);
                    }
                    break;
                } else if (input.toLowerCase().equals("no") || input.toLowerCase().equals("n")) {
                    System.out.println("Data Loader installation is quitting.");
                    System.exit(0);                  
                } else {
                    System.out.println("Type Yes or No.");
                }
            }
        }
        String installationSourceDir = ".";
        try {
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
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.fatal("Unable to copy installation artifacts from " 
                + installationSourceDir + " to " +  INSTALLATION_ABSOLUTE_PATH);
            System.exit(-1);
        }
    }
    
    private static String promptAndGetUserInput(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            prompt = "Provide input: ";
        }
        System.out.print(prompt);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input = "";
        // Reading data using readLine
        try {
            input = reader.readLine();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return input;

    }
    
    private static void deleteFilesFromDir(String directoryName, String filePattern) throws IOException {
        File directory = new File(directoryName);
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
        public void create();
    }
    
    private static void createShortcut(String prompt, ShortcutCreatorInterface shortcutCreator) {
        for (;;) {
            System.out.println("");
            String input = promptAndGetUserInput(prompt);
            if ("yes".toLowerCase().equals(input) || "y".toLowerCase().equals(input)) {
                shortcutCreator.create();
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
                        public void create() {
                            try {
                                createShortcutOnWindows(CREATE_DEKSTOP_SHORTCUT_ON_WINDOWS);
                            } catch (Exception ex) {
                                System.err.println(ex.getMessage());
                            }
                        }
            });
        } else if (AppUtil.isRunningOnMacOS()) {
            createShortcut(PROMPT,
                    new ShortcutCreatorInterface() {
                        public void create() {
                            try {
                                createSymLink(USERHOME + "/Desktop/DataLoader " + AppUtil.DATALOADER_VERSION,
                                        INSTALLATION_ABSOLUTE_PATH + "/dataloader.app");
                            } catch (Exception ex) {
                                System.err.println(ex.getMessage());
                            }
                        }
            });
        }
    }
    
    private static void createAppsDirShortcut() {
        final String PROMPT = "Do you want to create a shortcut in Applications directory? [Yes/No] ";

        if (AppUtil.isRunningOnMacOS()) {
            createShortcut(PROMPT,
                    new ShortcutCreatorInterface() {
                        public void create() {
                            try {
                                createSymLink("/Applications/DataLoader " + AppUtil.DATALOADER_VERSION,
                                        INSTALLATION_ABSOLUTE_PATH + "/dataloader.app");
                            } catch (Exception ex) {
                                System.err.println(ex.getMessage());
                            }
                        }
            });
        }
    }
    
    private static void createStartMenuShortcut() {
        final String PROMPT = "Do you want to create a Start menu shortcut? [Yes/No] ";

        if (AppUtil.isRunningOnWindows()) {
            createShortcut(PROMPT,
                    new ShortcutCreatorInterface() {
                        public void create() {
                            try {
                                String APPDATA = System.getenv("APPDATA");
                                String SALESFORCE_START_MENU_DIR = APPDATA + "\\Microsoft\\Windows\\Start Menu\\Programs\\Salesforce\\" ;
                                createDir(SALESFORCE_START_MENU_DIR);
                                createShortcutOnWindows(CREATE_START_MENU_SHORTCUT_ON_WINDOWS);
                            } catch (Exception ex) {
                                System.err.println(ex.getMessage());
                            }
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
    
    private static void createShortcutOnWindows(final String shortcutCommand) throws IOException {
        String redirectWinCmdOutputStr = "";
        if (logger.getLevel() == Level.DEBUG) {
            redirectWinCmdOutputStr = " > debug.txt 2>&1";
        }
        String command = "cmd /c call \"" + INSTALLATION_ABSOLUTE_PATH + "\\util\\util.bat\"" 
                + " " + shortcutCommand + " \"" + INSTALLATION_ABSOLUTE_PATH + "\"" + redirectWinCmdOutputStr;
        logger.debug("going to execute windows command: ");
        logger.debug(command);
        Process p = Runtime.getRuntime().exec(command);
        try {
            int exitVal = p.waitFor();
            logger.debug("windows command exited with exit code: " + exitVal);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            logger.error(e.getMessage());
        }
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
        // create <installation root>/dataloader.app/Contents
        logger.debug("going to create dataloader.app directory");
        createDir(MACOS_PACKAGE_BASE);

        // create Contents, MacOS, and Resources directories in dataloader.app
        logger.debug("going to create " + MACOS_PACKAGE_BASE + "/MacOS");
        createDir(MACOS_PACKAGE_BASE + "/MacOS");

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
            extractWindowsArtifactsFromJar();
        } else {
            // mac artifact work for both linux and mac
            extractMacOSArtifactsFromJar();
        }
    }
    
    private static void extractWindowsArtifactsFromJar() throws IOException {
        final String BASEDIR = "/win";
        // dataloader.bat
        File script = extractArtifactFromJar(BASEDIR, "dataloader.bat");
        script.setExecutable(true);
        
        // dataloader.ico
        extractArtifactFromJar(BASEDIR, "dataloader.ico");
        
        // bin/process.bat
        script = extractArtifactFromJar(BASEDIR, "bin/process.bat");
        script.setExecutable(true);
        
        // bin/encrypt.bat
        script = extractArtifactFromJar(BASEDIR, "bin/encrypt.bat");
        script.setExecutable(true);        
    }

    private static void extractMacOSArtifactsFromJar() throws URISyntaxException, IOException {
        final String BASEDIR = "/mac";
        // dataloader_console
        File script = extractArtifactFromJar(BASEDIR, "dataloader_console");
        script.setExecutable(true);
        
        // dataloader.app/Contents/PkgInfo
        extractArtifactFromJar(BASEDIR, "dataloader.app/Contents/PkgInfo");

        // dataloader.app/Contents/Info.plist
        extractArtifactFromJar(BASEDIR, "dataloader.app/Contents/Info.plist");
        
        // dataloader.app/Contents/Resources/dataloader.icns
        extractArtifactFromJar(BASEDIR, "dataloader.app/Contents/Resources/dataloader.icns");
    }
    
    private static File extractArtifactFromJar(String baseDir, String fileToExtract) throws IOException {
        if (!fileToExtract.startsWith("/")) {
            fileToExtract = "/" + fileToExtract;
        }
        File extractionFile = new File(TOBE_INSTALLED_ABSOLUTE_PATH + fileToExtract);
        AppUtil.extractFromJar(baseDir + fileToExtract, extractionFile);
        return extractionFile;
    }

    private static void extractDirFromJar(File rootDir) throws URISyntaxException, IOException {
        for (File nextFile : rootDir.listFiles()) {
            if (nextFile.isDirectory()) {
                extractDirFromJar(nextFile);
            } else {
                // extract the file
                logger.debug("going to extract " + nextFile.getPath() + " from jar");
                AppUtil.extractFromJar(nextFile.getPath(),
                        new File(Installer.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
            }
        }
    }
}
