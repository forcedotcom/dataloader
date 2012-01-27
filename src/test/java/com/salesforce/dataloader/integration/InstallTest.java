package com.salesforce.dataloader.integration;

import java.io.File;

import autoitx4java.AutoItX;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.jacob.com.LibraryLoader;

public class InstallTest {
    
    @BeforeClass
    public void classSetup() {
        File dll = new File(System.getProperty("user.home") + File.pathSeparator + ".m2"
               + File.pathSeparator + "repository" + File.pathSeparator + "net" + File.pathSeparator + "sf"
               + File.pathSeparator + "jacob-project" + File.pathSeparator + "jacob" + File.pathSeparator + "1.14.3"
               + File.pathSeparator + "jacob-1.14.3-x86.dll");
        System.setProperty(LibraryLoader.JACOB_DLL_PATH, dll.getAbsolutePath());
    }
    
    @Test
    public void testRunInstaller() {
        AutoItX autoIt = new AutoItX();
        autoIt.run("notepad.exe");
    }

}
