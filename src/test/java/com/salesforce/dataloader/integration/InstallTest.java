package com.salesforce.dataloader.integration;

import java.io.File;

import autoitx4java.AutoItX;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.jacob.com.LibraryLoader;

public class InstallTest {
    
    @BeforeClass
    public void classSetup() {
        File dll = new File(System.getProperty("user.home") + File.separator + ".m2"
               + File.separator + "repository" + File.separator + "net" + File.separator + "sf"
               + File.separator + "jacob-project" + File.separator + "jacob" + File.separator + "1.14.3"
               + File.separator + "jacob-1.14.3-x86.dll");
        System.setProperty(LibraryLoader.JACOB_DLL_PATH, dll.getAbsolutePath());
    }
    
    @Test
    public void testRunInstaller() {
        AutoItX autoIt = new AutoItX();
        autoIt.run(System.getProperty("basedir") + File.separator + "target" + File.separator
                + System.getProperty("project.name") + "-" + System.getProperty("project.version") + ".exe");
        String dataloaderSetup = "dataloader Setup";
        autoIt.winActivate(dataloaderSetup);
        autoIt.winWaitActive(dataloaderSetup);
        Assert.assertTrue(autoIt.winExists(dataloaderSetup), "dataloader setup window not found");
    }

}
