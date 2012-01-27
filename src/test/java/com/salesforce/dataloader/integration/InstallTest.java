package com.salesforce.dataloader.integration;

import java.io.File;
import java.lang.Runtime;
import autoitx4java.AutoItX;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.jacob.com.LibraryLoader;

public class InstallTest {

    private AutoItX autoIt;
    
    @BeforeClass
    public void classSetup() {
        File dll = new File(System.getProperty("user.home") + File.separator + ".m2"
               + File.separator + "repository" + File.separator + "net" + File.separator + "sf"
               + File.separator + "jacob-project" + File.separator + "jacob" + File.separator + "1.14.3"
               + File.separator + "jacob-1.14.3-x86.dll");
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
		click = autoIt.controlClick(dataloaderSetup, "&Install", "1");
		Assert.assertTrue(click, "failed to click on the Install button");
		waitForControlActive(dataloaderSetup, "&Close", "1", 120000);
		click = autoIt.controlClick(dataloaderSetup, "&Close", "1");
		Assert.assertTrue(click, "failed to click on the Close button");
		Assert.assertFalse(autoIt.winExists(dataloaderSetup), "dataloader setup window not closed");
    }
	
    @Test(dependsOnMethods = {"testRunInstaller"})
    public void testStartMenuDataloaderShortCut() throws IOException {
	    String shortcutPath = System.getProperty("user.home") + File.separator + "Start Menu" 
			+ File.separator + "Programs" + File.separator + "salesforce.com" + File.separator + "dataloader"
			+ File.separator + "Dataloader.lnk";
		Assert.assertTrue(new File(shortcutPath).exists(), "dataloader start menu shortcut does not exist at "
			+ dataloaderPath);
		Runtime.getRuntime().exec(shortcutPath);
//		Assert.assertTrue(autoIt.winExists("[CLASS:#32770]"), "dataloader welcome window does not found");
	}
	
	private void waitForControlActive(String title, String text, String control, long maxWaitTime) throws InterruptedException {
	    long timeWaited = 0;
		while(true)	{
		    if (timeWaited >= maxWaitTime) {
			    Assert.fail("waited " + timeWaited + " milliseconds but control with id " + control + " is not active");
		    } else if (autoIt.controlCommandIsEnabled(title, text, control)) {
			    break;
			} else {
			    Thread.sleep(5000L);
				timeWaited += 5000L;
			}
		}
	}

}
