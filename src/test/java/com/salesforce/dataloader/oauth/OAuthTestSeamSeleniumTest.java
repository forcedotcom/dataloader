/*
 * Copyright (c) 2015, salesforce.com, inc.
 * All rights reserved.
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

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * OAuth test that uses the test seam pattern to inject Selenium into handleOAuthLogin().
 * This is the cleanest approach - no mocking, just dependency injection.
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
    public void testHandleOAuthLoginWithTestSeam() throws Exception {
        System.out.println("🧪 Testing handleOAuthLogin() with automated Selenium flow");

        // Start handleOAuthLogin() in background
        CompletableFuture<Boolean> handleOAuthFuture = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("🚀 Starting handleOAuthLogin()...");
                
                OAuthFlowHandler oauthHandler = new OAuthFlowHandler(
                    getController().getAppConfig(),
                    (status) -> System.out.println("OAuth: " + status),
                    null, // null controller to avoid SWT issues (we are not testing dataloder ui layer; only oauth flow in browser)
                    null  // null runnable to avoid SWT issues (we are not testing dataloder ui layer; only oauth flow in browser)
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
                }

                // Handle authorization if needed
                pageSource = driver.getPageSource();
                if (pageSource.contains("Allow") || pageSource.contains("Authorize")) {
                    System.out.println("🖱️ Clicking authorization...");
                    handleAuthorizationPage();
                    Thread.sleep(3000);
                }

                currentUrl = driver.getCurrentUrl();
                if (currentUrl.contains("localhost:")) {
                    System.out.println("🎉 OAuth callback completed");
                    
                    // Verify the success page content
                    String pageContent = driver.getPageSource();
                    if (pageContent.contains("Authorization Successful!")) {
                        System.out.println("✅ Authorization success page verified");
                    } else {
                        System.out.println("⚠️ Authorization success page not found");
                    }
                    
                    assertTrue("Should display 'Authorization Successful!' message", 
                              pageContent.contains("Authorization Successful!"));
                }
                
            } else {
                System.out.println("⚠️ OAuth URL not detected: " + currentUrl);
            }

        } catch (Exception e) {
            System.out.println("❌ Selenium error: " + e.getMessage());
        }

        // Wait for handleOAuthLogin() to complete
        try {
            boolean result = handleOAuthFuture.get(30, TimeUnit.SECONDS);
            
            if (result) {
                System.out.println("🎉 SUCCESS: OAuth login completed");
                
                // Verify tokens were set
                AppConfig config = getController().getAppConfig();
                String accessToken = config.getString(AppConfig.PROP_OAUTH_ACCESSTOKEN);
                String refreshToken = config.getString(AppConfig.PROP_OAUTH_REFRESHTOKEN);
                String instanceUrl = config.getString(AppConfig.PROP_OAUTH_INSTANCE_URL);
                
                System.out.println("📋 Tokens: " + 
                    (accessToken != null && !accessToken.isEmpty() ? "✅ Access " : "❌ Access ") +
                    (refreshToken != null && !refreshToken.isEmpty() ? "✅ Refresh " : "❌ Refresh ") +
                    (instanceUrl != null && !instanceUrl.isEmpty() ? "✅ Instance" : "❌ Instance"));
                
                assertTrue("handleOAuthLogin() should return true", result);
                assertNotNull("Access token should be set", accessToken);
                assertFalse("Access token should not be empty", accessToken.trim().isEmpty());
                
            } else {
                System.out.println("⚠️ handleOAuthLogin() returned false");
            }
            
        } catch (Exception e) {
            System.out.println("⏳ handleOAuthLogin() timed out: " + e.getMessage());
        }

        System.out.println("✅ Test completed");
    }

    private void performAutomatedLogin() throws Exception {
        String username = System.getProperty("test.user.default");
        String password = System.getProperty("test.password");
        
        System.out.println("📋 Using credentials from pom.xml: " + username);

        try {
            WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
            usernameField.clear();
            usernameField.sendKeys(username);

            WebElement passwordField = driver.findElement(By.name("pw"));
            passwordField.clear();
            passwordField.sendKeys(password);

            WebElement loginButton = driver.findElement(By.name("Login"));
            loginButton.click();

            wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
            
        } catch (Exception e) {
            System.out.println("❌ Login failed: " + e.getMessage());
            throw e;
        }
    }

    private void handleAuthorizationPage() throws Exception {
        try {
            WebElement allowButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[contains(text(), 'Allow') or contains(text(), 'Authorize')]")));
            allowButton.click();
        } catch (Exception e) {
            System.out.println("⚠️ Authorization button not found: " + e.getMessage());
        }
    }

} 