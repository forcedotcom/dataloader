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
package com.salesforce.dataloader.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.salesforce.dataloader.ConfigTestBase;
import com.salesforce.dataloader.exception.ConfigInitializationException;

public class ConfigTest extends ConfigTestBase {
    private static Logger logger = LogManager.getLogger(ConfigTest.class);

    public ConfigTest() {
        super();
    }

    @Test
    public void testProperty_SELECTED_AUTH_ENVIRONMENT() {
        try {
            AppConfig appConfig = AppConfig.getInstance(getTestConfig());
            appConfig.setOAuthEnvironment(AppConfig.PROD_ENVIRONMENT_VAL);
            String selectedAuthEnv = appConfig.getString(AppConfig.SELECTED_AUTH_ENVIRONMENT);
            assertEquals(selectedAuthEnv, AppConfig.PROD_ENVIRONMENT_VAL);
        } catch (ConfigInitializationException 
                | FactoryConfigurationError
                | IOException e) {
            logger.error("Failed to get config instance: " + e.getMessage());
            fail("Failed to get config instance:", e);
        }
    }

    @Test
    public void testProperty_OAUTH_CLIENTID() {
        try {
            AppConfig appConfig = AppConfig.getInstance(getTestConfig());
            appConfig.setOAuthEnvironment(AppConfig.PROD_ENVIRONMENT_VAL);
            String configuredClientId = appConfig.getString(AppConfig.OAUTH_CLIENTID);
            if (appConfig.getBoolean(AppConfig.BULK_API_ENABLED) 
                    || appConfig.getBoolean(AppConfig.BULKV2_API_ENABLED)) {
                assertTrue(configuredClientId != null && configuredClientId.startsWith("DataLoaderBulkUI"));
            } else {
                assertTrue(configuredClientId != null && configuredClientId.startsWith("DataLoaderPartnerUI"));
            }
        } catch (ConfigInitializationException 
                | FactoryConfigurationError
                | IOException e) {
            logger.error("Failed to get config instance: " + e.getMessage());
            fail("Failed to get config instance:", e);
        }
    }
    
    @Test
    public void testProperty_OAUTH_CLIENTSECRET() {
        try {
            AppConfig appConfig = AppConfig.getInstance(getTestConfig());
            appConfig.setOAuthEnvironment(AppConfig.PROD_ENVIRONMENT_VAL);
            String configuredClientSecret = appConfig.getString(AppConfig.OAUTH_CLIENTSECRET);
            assertTrue(configuredClientSecret == null || configuredClientSecret.isBlank());
        } catch (ConfigInitializationException 
                | FactoryConfigurationError
                | IOException e) {
            logger.error("Failed to get config instance: " + e.getMessage());
            fail("Failed to get config instance:", e);
        }
    }
    
    @Test
    public void testProperty_OAUTH_SERVER() {
        try {
            AppConfig appConfig = AppConfig.getInstance(getTestConfig());
            appConfig.setOAuthEnvironment(AppConfig.PROD_ENVIRONMENT_VAL);
            String configuredOAuthServer = appConfig.getString(AppConfig.OAUTH_SERVER);
            String expectedOAuthServer = getProperty("test.endpoint");
            if (expectedOAuthServer == null || expectedOAuthServer.isBlank()) {
                logger.info("Expected prefix is " + expectedOAuthServer);
                logger.info("Actual prefix is " + configuredOAuthServer);
            }
            assertTrue(configuredOAuthServer.startsWith(expectedOAuthServer));
            
            appConfig.setOAuthEnvironment(AppConfig.SB_ENVIRONMENT_VAL);
            configuredOAuthServer = appConfig.getString(AppConfig.OAUTH_SERVER);
            assertTrue(configuredOAuthServer.startsWith(AppConfig.DEFAULT_ENDPOINT_URL_SANDBOX));
        } catch (ConfigInitializationException 
                | FactoryConfigurationError
                | IOException e) {
            logger.error("Failed to get config instance: " + e.getMessage());
            fail("Failed to get config instance:", e);
        }
    }
    
    
    @Test
    public void testProperty_OAUTH_REDIRECTURI() {
        try {
            Map<String, String> testConfigMap = getTestConfig();
            AppConfig appConfig = AppConfig.getInstance(testConfigMap);
            appConfig.setOAuthEnvironment(AppConfig.PROD_ENVIRONMENT_VAL);
            String configuredOAuthRedirectURI = appConfig.getString(AppConfig.OAUTH_REDIRECTURI);
            String expectedOAuthRedirectURIPrefix = getProperty("test.endpoint");
            if (expectedOAuthRedirectURIPrefix == null || expectedOAuthRedirectURIPrefix.isBlank()) {
                logger.info("Expected prefix is " + expectedOAuthRedirectURIPrefix);
                logger.info("Actual prefix is " + configuredOAuthRedirectURI);
            }
            assertTrue(configuredOAuthRedirectURI.startsWith(expectedOAuthRedirectURIPrefix));
            assertTrue(configuredOAuthRedirectURI.endsWith(AppConfig.OAUTH_REDIRECT_URI_SUFFIX));
        } catch (ConfigInitializationException 
                | FactoryConfigurationError
                | IOException e) {
            logger.error("Failed to get config instance: " + e.getMessage());
            fail("Failed to get config instance:", e);
        }
    }
}