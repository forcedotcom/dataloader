/*
 * Copyright (c) 2015, salesforce.com, inc.
 * All rights reserved.
 */
package com.salesforce.dataloader.ui;

/**
 * Interface for opening URLs, allowing test seams for browser automation.
 * Production uses Desktop.browse(), tests can inject Selenium WebDriver.
 */
public interface UrlOpener {
    /**
     * Opens the specified URL.
     * @param url The URL to open
     * @throws Exception if the URL cannot be opened
     */
    void open(String url) throws Exception;
} 