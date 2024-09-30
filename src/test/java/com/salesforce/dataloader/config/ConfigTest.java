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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
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
            Config config = Config.getInstance(getTestConfig());
            config.setOAuthEnvironment(Config.PROD_ENVIRONMENT_VAL);
            String selectedAuthEnv = config.getString(Config.SELECTED_AUTH_ENVIRONMENT);
            assertEquals(selectedAuthEnv, Config.PROD_ENVIRONMENT_VAL);
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
            Config config = Config.getInstance(getTestConfig());
            config.setOAuthEnvironment(Config.PROD_ENVIRONMENT_VAL);
            String configuredClientId = config.getString(Config.OAUTH_CLIENTID);
            if (config.getBoolean(Config.BULK_API_ENABLED) 
                    || config.getBoolean(Config.BULKV2_API_ENABLED)) {
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
            Config config = Config.getInstance(getTestConfig());
            config.setOAuthEnvironment(Config.PROD_ENVIRONMENT_VAL);
            String configuredClientSecret = config.getString(Config.OAUTH_CLIENTSECRET);
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
            Config config = Config.getInstance(getTestConfig());
            config.setOAuthEnvironment(Config.PROD_ENVIRONMENT_VAL);
            String configuredOAuthServer = config.getString(Config.OAUTH_SERVER);
            String expectedOAuthServer = System.getProperty("test.endpoint");
            if (expectedOAuthServer == null || expectedOAuthServer.isBlank()) {
                config = Config.getInstance(null);
                expectedOAuthServer = "https://testendpoint";
                logger.info("Expected prefix is " + expectedOAuthServer);
                logger.info("Actual prefix is " + configuredOAuthServer);            }
            assertTrue(configuredOAuthServer.startsWith(expectedOAuthServer));
            
            config.setOAuthEnvironment(Config.SB_ENVIRONMENT_VAL);
            configuredOAuthServer = config.getString(Config.OAUTH_SERVER);
            assertTrue(configuredOAuthServer.startsWith(Config.DEFAULT_ENDPOINT_URL_SANDBOX));
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
            Config config = Config.getInstance(getTestConfig());
            config.setOAuthEnvironment(Config.PROD_ENVIRONMENT_VAL);
            String configuredOAuthRedirectURI = config.getString(Config.OAUTH_REDIRECTURI);
            String expectedOAuthRedirectURIPrefix = System.getProperty("test.endpoint");
            if (expectedOAuthRedirectURIPrefix == null || expectedOAuthRedirectURIPrefix.isBlank()) {
                expectedOAuthRedirectURIPrefix = "https://testendpoint";
                logger.info("Expected prefix is " + expectedOAuthRedirectURIPrefix);
                logger.info("Actual prefix is " + configuredOAuthRedirectURI);
            }
            assertTrue(configuredOAuthRedirectURI.startsWith(expectedOAuthRedirectURIPrefix));
            assertTrue(configuredOAuthRedirectURI.endsWith(Config.OAUTH_REDIRECT_URI_SUFFIX));
        } catch (ConfigInitializationException 
                | FactoryConfigurationError
                | IOException e) {
            logger.error("Failed to get config instance: " + e.getMessage());
            fail("Failed to get config instance:", e);
        }
    }
}
