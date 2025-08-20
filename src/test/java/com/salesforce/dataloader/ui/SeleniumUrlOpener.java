/*
 * Copyright (c) 2015, salesforce.com, inc.
 * All rights reserved.
 */
package com.salesforce.dataloader.ui;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * Test implementation of UrlOpener that uses Selenium WebDriver.
 * This allows tests to control browser navigation instead of opening system browser.
 */
public final class SeleniumUrlOpener implements UrlOpener {
    private final WebDriver driver;
    private final boolean openInNewTab;

    public SeleniumUrlOpener(WebDriver driver) {
        this(driver, false);
    }

    public SeleniumUrlOpener(WebDriver driver, boolean openInNewTab) {
        this.driver = driver;
        this.openInNewTab = openInNewTab;
    }

    @Override
    public void open(String url) throws Exception {
        System.out.println("🎯 SeleniumUrlOpener: Navigating to " + url);
        
        if (openInNewTab) {
            // Open in new tab and switch to it
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", url);
            
            // Switch to the new tab
            String originalWindow = driver.getWindowHandle();
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            System.out.println("🎯 SeleniumUrlOpener: Opened in new tab and switched");
        } else {
            // Open in current tab
            driver.get(url);
            System.out.println("🎯 SeleniumUrlOpener: Opened in current tab");
        }
    }
} 