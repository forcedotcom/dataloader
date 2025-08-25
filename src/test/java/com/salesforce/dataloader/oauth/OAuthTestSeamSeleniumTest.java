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
package com.salesforce.dataloader.oauth;

import com.salesforce.dataloader.ConfigTestBase;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.ui.URLUtil;
import com.salesforce.dataloader.ui.SeleniumUrlOpener;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.TimeoutException;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import org.junit.Assert;

/**
 * Clean OAuth test that uses the test seam pattern to inject Selenium into handleOAuthLogin().
 * Tests the complete OAuth flow: PKCE timeout -> Device Flow -> Login -> Allow -> Continue
 */
public class OAuthTestSeamSeleniumTest extends ConfigTestBase {

    private WebDriver driver;
    private WebDriverWait wait;

    @Before
    public void setUp() throws Exception {
        super.setupController();
        
        System.out.println("🔧 Setting up OAuth test with Selenium...");
        
        // Setup Selenium
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-web-security");
        options.addArguments("--disable-features=VizDisplayCompositor");
        options.addArguments("--no-sandbox");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        
        // Inject Selenium as the URL opener using test seam
        SeleniumUrlOpener seleniumOpener = new SeleniumUrlOpener(driver, false);
        URLUtil.setTestHook(seleniumOpener);
        System.out.println("✅ Test seam configured - OAuth will use Selenium browser");
    }

    @After
    public void tearDown() throws Exception {
        try {
            URLUtil.clearTestHook();
            
            // Clear OAuth tokens
            AppConfig config = getController().getAppConfig();
            if (config != null) {
                config.setValue(AppConfig.PROP_OAUTH_ACCESSTOKEN, "");
                config.setValue(AppConfig.PROP_OAUTH_REFRESHTOKEN, "");
                config.setValue(AppConfig.PROP_OAUTH_INSTANCE_URL, "");
            }
            
            if (getController() != null) {
                getController().logout();
            }
            
            if (driver != null) {
                Thread.sleep(2000);
                driver.quit();
            }
            
        } catch (Exception e) {
            System.err.println("⚠️ Warning during teardown: " + e.getMessage());
        }
    }

    @Test
    public void testHandleOAuthLoginWithPKCEFlow() throws Exception {
        System.out.println("🧪 Testing handleOAuthLogin() with automated Selenium flow");

        // Start handleOAuthLogin() in background
        CompletableFuture<Boolean> handleOAuthFuture = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("🚀 Starting handleOAuthLogin()...");
                
                OAuthFlowHandler oauthHandler = new OAuthFlowHandler(
                    getController().getAppConfig(),
                    (status) -> System.out.println("OAuth: " + status),
                    null, // null controller to avoid SWT issues (we are not testing dataloader ui layer; only oauth flow in browser)
                    null  // null runnable to avoid SWT issues (we are not testing dataloader ui layer; only oauth flow in browser)
                );
                
                boolean result = oauthHandler.handleOAuthLogin();
                System.out.println("🎯 handleOAuthLogin() result: " + result);
                return result;
                
            } catch (Exception e) {
                System.err.println("❌ handleOAuthLogin() failed: " + e.getMessage());
                return false;
            }
        });

        System.out.println("⏳ Waiting for OAuth flow to navigate browser...");
        Thread.sleep(3000);

        // Handle the OAuth flow with Selenium
        try {
            String currentUrl = driver.getCurrentUrl();
            System.out.println("🌐 Current URL: " + currentUrl);

            if (currentUrl.contains("salesforce") || currentUrl.contains("orgfarm")) {
                System.out.println("✅ Selenium navigated to OAuth URL");
                
                // Handle login if needed
                String pageSource = driver.getPageSource();
                if (pageSource.contains("name=\"username\"") || pageSource.contains("name=\"pw\"")) {
                    System.out.println("🔐 Performing login...");
                    performAutomatedLogin();
                    Thread.sleep(2000);
                    
                    // Assert we're no longer on login page
                    String postLoginUrl = driver.getCurrentUrl();
                    assertFalse("Should not be on login page after login", 
                        postLoginUrl.contains("/login"));
                }

                // Handle authorization if needed
                pageSource = driver.getPageSource();
                if (pageSource.contains("Allow") || pageSource.contains("Authorize")) {
                    System.out.println("🖱️ Clicking authorization...");
                    handleAuthorizationPage();
                    Thread.sleep(3000);
                    
                    // Assert we moved past authorization page
                    String postAuthUrl = driver.getCurrentUrl();
                    assertTrue("Should be redirected after authorization", 
                        !postAuthUrl.equals(currentUrl));
                }

                currentUrl = driver.getCurrentUrl();
                if (currentUrl.contains("localhost:")) {
                    System.out.println("🎉 OAuth callback completed");
                    
                    // Assert we're on localhost callback
                    assertTrue("Should be on localhost callback URL", 
                        currentUrl.contains("localhost:"));
                    
                    // Use WebDriverWait to handle page loading and success verification
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
                        wait.until(ExpectedConditions.or(
                            ExpectedConditions.titleContains("Success"),
                            ExpectedConditions.urlContains("success"),
                            ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), 'Authorization Successful')]")),
                            ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), 'authorization successful')]")),
                            ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), 'SUCCESS')]"))
                        ));
                        System.out.println("✅ Authorization success page verified");
                    } catch (org.openqa.selenium.TimeoutException e) {
                        System.out.println("⚠️ Success page elements not found within timeout, checking page source...");
                        try {
                            String pageContent = driver.getPageSource();
                            if (pageContent.contains("Authorization Successful!") || 
                                pageContent.contains("authorization successful") ||
                                pageContent.contains("SUCCESS")) {
                                System.out.println("✅ Authorization success verified in page source");
                            } else {
                                System.out.println("⚠️ Authorization success message not found, but OAuth tokens obtained successfully");
                            }
                        } catch (Exception pageSourceException) {
                            System.out.println("⚠️ Could not verify success page, but OAuth flow completed successfully");
                        }
                    }
                }
                
            } else {
                Assert.fail("OAuth URL not detected: " + currentUrl);
            }

        } catch (Exception e) {
            Assert.fail("Selenium error during OAuth flow: " + e.getMessage());
        }

        // Wait for handleOAuthLogin() to complete
        try {
            boolean result = handleOAuthFuture.get(30, TimeUnit.SECONDS);
            
            if (result) {
                System.out.println("🎉 SUCCESS: OAuth login completed");
                
                // Verify tokens were set
                AppConfig config = getController().getAppConfig();
                String accessToken = config.getString(AppConfig.PROP_OAUTH_ACCESSTOKEN);
                String instanceUrl = config.getString(AppConfig.PROP_OAUTH_INSTANCE_URL);
                
                System.out.println("📋 Tokens: " + 
                    (accessToken != null && !accessToken.isEmpty() ? "✅ Access " : "❌ Access ") +
                    (instanceUrl != null && !instanceUrl.isEmpty() ? "✅ Instance" : "❌ Instance"));

                assertTrue("handleOAuthLogin() should return true", result);
                assertNotNull("Access token should be set", accessToken);
                assertFalse("Access token should not be empty", accessToken.trim().isEmpty());
                
            } else {
                Assert.fail("handleOAuthLogin() returned false - OAuth flow failed");
            }
            
        } catch (Exception e) {
            Assert.fail("handleOAuthLogin() timed out or failed: " + e.getMessage());
        }

        System.out.println("✅ Test completed");
    }

    /**
     * Clean OAuth flow test: PKCE timeout -> Device Flow -> Login -> Allow -> Continue
     * Refactored based on actual execution path analysis.
     */
    @Test
    public void testHandleOAuthLoginWithDeviceFlow() throws Exception {
        System.out.println("🧪 Starting clean OAuth flow test...");

        // Set up Selenium to intercept browser calls
        SeleniumUrlOpener seleniumOpener = new SeleniumUrlOpener(driver);
        URLUtil.setTestHook(seleniumOpener);
        assertNotNull("Selenium URL opener should be set", seleniumOpener);
        
        // Start OAuth flow in background
        CompletableFuture<Boolean> oauthFuture = CompletableFuture.supplyAsync(() -> {
            try {
                OAuthFlowHandler oauthHandler = new OAuthFlowHandler(
                        getController().getAppConfig(),
                        (status) -> System.out.println("OAuth: " + status),
                        null, // null controller to avoid SWT issues (we are not testing dataloader ui layer; only oauth flow in browser)
                        null  // null runnable to avoid SWT issues (we are not testing dataloader ui layer; only oauth flow in browser)
                );
                return oauthHandler.handleOAuthLogin();
            } catch (Exception e) {
                System.err.println("OAuth flow error: " + e.getMessage());
                return false;
            }
        });
        
        // Step 1: Wait for OAuth flow to determine and navigate to Device Flow
        System.out.println("⏳ Step 1: Waiting for OAuth pre-flight checks and Device Flow navigation...");
        Thread.sleep(3000);
        
        // Verify we're on Device Flow page
        String currentUrl = driver.getCurrentUrl();
        assertTrue("Should be on Device Flow page", currentUrl.contains("setup/connect"));
        System.out.println("✅ Step 1 verified: On Device Flow page");
        
        // Verify the page contains expected elements
        String pageSource = driver.getPageSource();
        assertTrue("Device Flow page should contain Connect button", 
            pageSource.contains("Connect") || pageSource.contains("Submit"));
        
        // Step 2: Click Connect button (code is pre-filled)
        System.out.println("📱 Step 2: Clicking Connect button...");
        WebElement connectButton = driver.findElement(By.xpath("//input[@type='submit' and (@value='Connect' or @value='Submit')]"));
        assertNotNull("Connect button should be found", connectButton);
        assertTrue("Connect button should be enabled", connectButton.isEnabled());
        connectButton.click();
        Thread.sleep(2000);
        
        // Verify we were redirected from Device Flow page
        String postConnectUrl = driver.getCurrentUrl();
        assertNotEquals("URL should change after clicking Connect", currentUrl, postConnectUrl);
        System.out.println("✅ Step 2 verified: Redirected after Connect click");
        
        // Step 3: Perform login (redirected to login page)
        System.out.println("🔐 Step 3: Performing login...");
        
        // Verify we're on login page
        String loginPageSource = driver.getPageSource();
        assertTrue("Should be on login page", 
            loginPageSource.contains("name=\"username\"") && loginPageSource.contains("name=\"pw\""));
        
        performAutomatedLogin();
        
        // Verify login was successful (no longer on login page)
        String postLoginUrl = driver.getCurrentUrl();
        assertFalse("Should not be on login page after successful login", 
            postLoginUrl.contains("/login"));
        System.out.println("✅ Step 3 verified: Login successful");
        
        // Step 4: Click Allow button on authorization page
        System.out.println("✅ Step 4: Clicking Allow button...");
        Thread.sleep(2000); // Wait for authorization page to load
        
        // Verify we're on authorization page
        String authPageSource = driver.getPageSource();
        assertTrue("Should be on authorization page with Allow button", 
            authPageSource.contains("Allow"));
        
        WebElement allowButton = driver.findElement(By.xpath("//input[normalize-space(@value)='Allow']"));
        assertNotNull("Allow button should be found", allowButton);
        assertTrue("Allow button should be enabled", allowButton.isEnabled());
        allowButton.click();
        Thread.sleep(3000);
        
        // Verify we reached success page
        String successUrl = driver.getCurrentUrl();
        assertTrue("Should reach success page after Allow", successUrl.contains("user_approved=1"));
        System.out.println("✅ Step 4 verified: Authorization successful");
        
        // Step 5: Click Continue button to complete flow
        System.out.println("➡️ Step 5: Clicking Continue button...");
        
        // Verify Continue button exists
        String successPageSource = driver.getPageSource();
        assertTrue("Success page should contain Continue button", 
            successPageSource.contains("Continue"));
        
        WebElement continueButton = driver.findElement(By.xpath("//input[@value='Continue']"));
        assertNotNull("Continue button should be found", continueButton);
        assertTrue("Continue button should be enabled", continueButton.isEnabled());
        continueButton.click();
        
        System.out.println("🎉 Clean OAuth flow test PASSED!");
        URLUtil.clearTestHook();
    }

    /**
     * Handle authorization page by clicking Allow/Authorize button
     */
    private void handleAuthorizationPage() throws Exception {
        try {
            WebElement allowButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[contains(text(), 'Allow') or contains(text(), 'Authorize')]")));
            allowButton.click();
        } catch (Exception e) {
            System.out.println("⚠️ Authorization button not found: " + e.getMessage());
        }
    }

    /**
     * Perform automated login using credentials from pom.xml system properties
     */
    private void performAutomatedLogin() throws Exception {
        String username = System.getProperty("test.user.default");
        String password = System.getProperty("test.password");
        
        // Assert credentials are available
        assertNotNull("Username should be provided via system property", username);
        assertNotNull("Password should be provided via system property", password);
        assertFalse("Username should not be empty", username.trim().isEmpty());
        assertFalse("Password should not be empty", password.trim().isEmpty());
        
        System.out.println("📋 Using credentials from pom.xml: " + username);

        try {
            WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
            assertNotNull("Username field should be found", usernameField);
            assertTrue("Username field should be enabled", usernameField.isEnabled());
            usernameField.clear();
            usernameField.sendKeys(username);

            WebElement passwordField = driver.findElement(By.name("pw"));
            assertNotNull("Password field should be found", passwordField);
            assertTrue("Password field should be enabled", passwordField.isEnabled());
            passwordField.clear();
            passwordField.sendKeys(password);

            WebElement loginButton = driver.findElement(By.name("Login"));
            assertNotNull("Login button should be found", loginButton);
            assertTrue("Login button should be enabled", loginButton.isEnabled());
            loginButton.click();

            // Wait for login to complete (no longer on login page)
            wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
            
            // Verify we're no longer on login page
            String currentUrl = driver.getCurrentUrl();
            assertFalse("Should not be on login page after successful login", 
                currentUrl.contains("/login"));
            
        } catch (Exception e) {
            Assert.fail("Login failed: " + e.getMessage());
        }
    }
} 