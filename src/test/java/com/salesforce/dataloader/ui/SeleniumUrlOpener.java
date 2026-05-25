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