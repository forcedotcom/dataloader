/*
 * Copyright (c) 2012, salesforce.com, inc. All rights reserved. Redistribution and use in source and binary forms, with
 * or without modification, are permitted provided that the following conditions are met: Redistributions of source code
 * must retain the above copyright notice, this list of conditions and the following disclaimer. Redistributions in
 * binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. Neither the name of salesforce.com, inc. nor the
 * names of its contributors may be used to endorse or promote products derived from this software without specific
 * prior written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.salesforce.dataloader.integration;

import java.io.File;
import java.io.IOException;

import autoitx4java.AutoItX;

import org.testng.Assert;
import org.testng.annotations.*;

import com.jacob.com.LibraryLoader;

/**
 * This class tests the Windows Installer. Dependencies are JACOB, AutoIt and AutoItX4Java.
 * 
 * @author Jeff Lai
 * @since 24.0
 */
public class InstallTest {

    private AutoItX autoIt;
    private static final String startMenuDirPath = System.getProperty("user.home") + File.separator + "Start Menu"
            + File.separator + "Programs" + File.separator + "salesforce.com" + File.separator + "dataloader";
    private static final String desktopDirPath = System.getProperty("user.home") + File.separator + "Desktop";
    private String installPath;

    @BeforeClass
    public void classSetup() {
        String osArch = System.getProperty("os.arch");
        String jacobDllFileName = null;
        if (osArch.equals("x86")) {
            jacobDllFileName = "jacob-1.14.3-x86.dll";
        } else if (osArch.equals("amd64")) {
            jacobDllFileName = "jacob-1.14.3-x64.dll";
        } else {
            Assert.fail("unsupported os architecture: " + osArch);
        }
        File dll = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository"
                + File.separator + "net" + File.separator + "sf" + File.separator + "jacob-project" + File.separator
                + "jacob" + File.separator + "1.14.3" + File.separator + jacobDllFileName);
        System.setProperty(LibraryLoader.JACOB_DLL_PATH, dll.getAbsolutePath());
        autoIt = new AutoItX();
    }

    @Test
    public void testRunInstaller() throws InterruptedException {
        String installerPath = System.getProperty("basedir") + File.separator + "target" + File.separator
                + System.getProperty("exeName");
        Assert.assertTrue(new File(installerPath).exists(), "dataloader installer does not exist at " + installerPath);
        autoIt.run(installerPath);
        String dataloaderSetup = "dataloader Setup";
        autoIt.winActivate(dataloaderSetup);
        autoIt.winWaitActive(dataloaderSetup);
        Assert.assertTrue(autoIt.winExists(dataloaderSetup), "dataloader setup window not found");
        boolean click = autoIt.controlClick(dataloaderSetup, "I &Agree", "1");
        Assert.assertTrue(click, "failed to click the I Agree button");
        click = autoIt.controlClick(dataloaderSetup, "&Next >", "1");
        Assert.assertTrue(click, "failed to click on the Next button");
        String fullWinText = autoIt.winGetText(dataloaderSetup);
        installPath = getInstallPathFromWinText(fullWinText);
        click = autoIt.controlClick(dataloaderSetup, "&Install", "1");
        Assert.assertTrue(click, "failed to click on the Install button");
        waitForControlActive(dataloaderSetup, "&Close", "1", 120000);
        click = autoIt.controlClick(dataloaderSetup, "&Close", "1");
        Thread.sleep(3000L);
        Assert.assertTrue(click, "failed to click on the Close button");
        Assert.assertFalse(autoIt.winExists(dataloaderSetup), "dataloader setup window not closed");
    }

    public String getInstallPathFromWinText(String fullWinText) {
        String[] lines = fullWinText.split("\n");
        for (String line : lines) {
            if (line.contains("salesforce.com\\dataloader")) return line;
        }
        return null;
    }

    @DataProvider(name = "dataloaderShortcutPaths")
    public String[][] dataloaderShortcutPaths() {
        return new String[][] { { startMenuDirPath + File.separator + "Dataloader.lnk" },
                { desktopDirPath + File.separator + "Dataloader.lnk" } };
    }

    @Test(dependsOnMethods = { "testRunInstaller" }, groups = { "shortcut" }, dataProvider = "dataloaderShortcutPaths")
    public void testDataloaderShortcut(String shortcutPath) throws IOException, InterruptedException {
        Assert.assertTrue(new File(shortcutPath).exists(), "dataloader start menu shortcut does not exist at "
                + shortcutPath);
        openShortcut(shortcutPath);
        Thread.sleep(3000L);
        String welcomeWindow = "[CLASS:#32770]";
        String dataloaderWindow = "[CLASS:SWT_Window0]";
        String welcomeWindowHandle = autoIt.winGetHandle(welcomeWindow);
        Assert.assertTrue(autoIt.winExists(welcomeWindow), "dataloader welcome window not found");
        Assert.assertTrue(autoIt.winExists(dataloaderWindow), "dataloader window not found");
        boolean click = autoIt.controlClick(welcomeWindow, "Cancel", "[CLASS:Button; INSTANCE:8]");
        Assert.assertTrue(click, "failed to click the Cancel button");
        Thread.sleep(3000L);
        autoIt.winClose(dataloaderWindow);
        Thread.sleep(3000L);
        Assert.assertFalse(autoIt.winExists(welcomeWindowHandle), "dataloader welcome window not closed");
        Assert.assertFalse(autoIt.winExists(dataloaderWindow), "dataloader window not closed");
    }

    @Test(dependsOnMethods = { "testRunInstaller" }, groups = { "shortcut" })
    public void testUninstallShortcut() throws IOException, InterruptedException {
        String dataloaderUninstall = openUninstaller();
        autoIt.winClose(dataloaderUninstall);
        Thread.sleep(3000L);
        Assert.assertFalse(autoIt.winExists(dataloaderUninstall), "dataloader uninstall window not closed");
    }

    @Test(dependsOnGroups = { "shortcut" })
    public void testRunUninstaller() throws IOException, InterruptedException {
        String dataloaderUninstall = openUninstaller();
        boolean click = autoIt.controlClick(dataloaderUninstall, "&Next >", "1");
        Assert.assertTrue(click, "failed to click on the Next button");
        click = autoIt.controlClick(dataloaderUninstall, "&Uninstall", "1");
        Assert.assertTrue(click, "failed to click on the Uninstall button");
        waitForControlActive(dataloaderUninstall, "&Close", "1", 120000);
        click = autoIt.controlClick(dataloaderUninstall, "&Close", "1");
        Thread.sleep(3000L);
        Assert.assertTrue(click, "failed to click on the Close button");
        Assert.assertFalse(autoIt.winExists(dataloaderUninstall), "dataloader uninstall window not closed");
        Assert.assertFalse(new File(startMenuDirPath).exists(), "start menu shortcuts not deleted");
        Assert.assertFalse(new File(desktopDirPath + File.separator + "Dataloader.lnk").exists(),
                "desktop shortcut not deleted");
        Assert.assertFalse(new File(installPath).exists(), "program files directory not deleted");
    }

    private String openUninstaller() throws IOException {
        String shortcutPath = startMenuDirPath + File.separator + "Uninstall Dataloader.lnk";
        Assert.assertTrue(new File(shortcutPath).exists(), "uninstall start menu shortcut does not exist at "
                + shortcutPath);
        openShortcut(shortcutPath);
        String dataloaderUninstall = "dataloader Uninstall";
        autoIt.winActivate(dataloaderUninstall);
        autoIt.winWaitActive(dataloaderUninstall);
        Assert.assertTrue(autoIt.winExists(dataloaderUninstall), "dataloader uninstall window not found");
        return dataloaderUninstall;
    }

    private void openShortcut(String shortcutPath) throws IOException {
        Runtime.getRuntime().exec("cmd /c \"" + shortcutPath + "\"");
    }

    private void waitForControlActive(String title, String text, String control, long maxWaitTime)
            throws InterruptedException {
        long timeWaited = 0;
        while (true) {
            if (timeWaited >= maxWaitTime) {
                Assert.fail("waited " + timeWaited + " milliseconds but control with id " + control + " is not active");
            } else if (autoIt.controlCommandIsEnabled(title, text, control)) {
                break;
            } else {
                Thread.sleep(2000L);
                timeWaited += 2000L;
            }
        }
    }

}
