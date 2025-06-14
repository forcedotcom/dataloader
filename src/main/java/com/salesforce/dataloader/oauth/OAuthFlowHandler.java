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

import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.util.OAuthBrowserDeviceLoginRunner;
import com.salesforce.dataloader.util.OAuthBrowserFlow;
import com.salesforce.dataloader.util.DLLogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Utility class to handle OAuth flows (PKCE and Device Flow) for both UI and batch modes.
 */
public class OAuthFlowHandler {
    private static final Logger logger = DLLogManager.getLogger(OAuthFlowHandler.class);
    private final AppConfig appConfig;
    private final Consumer<String> statusConsumer;
    private final Controller controller;

    public OAuthFlowHandler(AppConfig appConfig, Consumer<String> statusConsumer, Controller controller) {
        this.appConfig = appConfig;
        this.statusConsumer = statusConsumer;
        this.controller = controller;
    }

    /**
     * Handles the OAuth login process, attempting PKCE flow first if not disabled,
     * falling back to device flow if PKCE is not supported or fails.
     *
     * @return true if login was successful, false otherwise
     */
    public boolean handleOAuthLogin() {
        logger.info("Starting OAuth flow");
        
        // Check if device login from browser is explicitly enabled
        String deviceLoginFromBrowser = appConfig.getString(AppConfig.PROP_OAUTH_LOGIN_FROM_BROWSER_DEVICE_OAUTH);
        boolean deviceLoginFromBrowserEnabled = "true".equalsIgnoreCase(deviceLoginFromBrowser);
        logger.debug("Device login from browser setting: " + deviceLoginFromBrowser);
        logger.debug("Device login from browser enabled: " + deviceLoginFromBrowserEnabled);

        try {
            // If device login from browser is explicitly enabled, use device flow directly
            if (deviceLoginFromBrowserEnabled) {
                logger.info("Device login from browser is enabled, using device flow");
                return handleDeviceFlow();
            }

            // If device login from browser is not enabled, try PKCE first
            logger.debug("Device login from browser is not enabled, checking PKCE support");
            if (checkPKCESupport()) {
                logger.info("PKCE is supported by connected app, attempting PKCE flow");
                OAuthBrowserFlow pkceFlow = new OAuthBrowserFlow(appConfig);
                if (pkceFlow.performOAuthFlow()) {
                    logger.info("PKCE flow completed successfully");
                    // Update controller's login state
                    if (controller != null) {
                        try {
                            if (controller.login()) {
                                controller.saveConfig();
                                controller.updateLoaderWindowTitleAndCacheUserInfoForTheSession();
                                return true;
                            }
                        } catch (Exception e) {
                            logger.error("Failed to update controller's login state", e);
                            return false;
                        }
                    }
                    return true;
                }
                logger.info("PKCE flow did not complete successfully, falling back to device flow");
            }

            // If PKCE failed or is not supported, try device flow
            logger.info("Starting device flow");
            return handleDeviceFlow();

        } catch (Exception e) {
            logger.error("OAuth flow failed with unexpected error: " + e.getMessage(), e);
            if (statusConsumer != null) {
                statusConsumer.accept("OAuth flow failed. Please try again.");
            }
            return false;
        }
    }

    /**
     * Checks if PKCE is supported by the connected app.
     *
     * @return true if PKCE is supported, false otherwise
     */
    private boolean checkPKCESupport() {
        try {
            // Find an available port for PKCE
            int pkcePort;
            try (ServerSocket socket = new ServerSocket(0)) {
                pkcePort = socket.getLocalPort();
            } catch (Exception e) {
                logger.error("Failed to find available port for PKCE", e);
                return false;
            }

            // Check if PKCE is supported by making a test request
            String testUrl = appConfig.getAuthEndpointForCurrentEnv() + "/services/oauth2/authorize";
            String clientId = appConfig.getEffectiveClientIdForCurrentEnv();
            String redirectUri = "http://localhost:" + pkcePort + "/OauthRedirect";

            // Build test URL with PKCE parameters
            StringBuilder testUrlBuilder = new StringBuilder(testUrl);
            testUrlBuilder.append("?response_type=code");
            testUrlBuilder.append("&client_id=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8.name()));
            testUrlBuilder.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.name()));
            testUrlBuilder.append("&scope=").append(URLEncoder.encode("api refresh_token", StandardCharsets.UTF_8.name()));
            testUrlBuilder.append("&code_challenge=test");
            testUrlBuilder.append("&code_challenge_method=S256");

            // Make a test request with PKCE parameters
            URL url = new URL(testUrlBuilder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try {
                int responseCode = conn.getResponseCode();
                // If we get a 400 or 401, PKCE is not supported
                if (responseCode == 400 || responseCode == 401) {
                    String errorResponse = readErrorResponse(conn);
                    if (errorResponse != null && (
                        errorResponse.contains("redirect_uri_mismatch") ||
                        errorResponse.contains("invalid_request") ||
                        errorResponse.contains("unsupported_grant_type"))) {
                        logger.info("PKCE not supported by connected app (error: " + errorResponse + ")");
                        return false;
                    }
                }
                return true;
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            logger.error("Error checking PKCE support: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Handles the device flow OAuth process.
     *
     * @return true if device flow was successful, false otherwise
     */
    private boolean handleDeviceFlow() {
        try {
            OAuthBrowserDeviceLoginRunner deviceFlow = new OAuthBrowserDeviceLoginRunner(appConfig, true);

            // Wait for login process to complete
            while (deviceFlow.getLoginStatus() == OAuthBrowserDeviceLoginRunner.LoginStatus.WAIT) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error("Device flow interrupted", e);
                    return false;
                }
            }

            if (deviceFlow.getLoginStatus() == OAuthBrowserDeviceLoginRunner.LoginStatus.SUCCESS) {
                logger.info("Device flow completed successfully");
                // Update controller's login state
                if (controller != null) {
                    try {
                        if (controller.login()) {
                            controller.saveConfig();
                            controller.updateLoaderWindowTitleAndCacheUserInfoForTheSession();
                            return true;
                        }
                    } catch (Exception e) {
                        logger.error("Failed to update controller's login state", e);
                        return false;
                    }
                }
                if (statusConsumer != null) {
                    statusConsumer.accept("OAuth device flow completed successfully.");
                }
                return true;
            } else {
                logger.error("Device flow failed");
                if (statusConsumer != null) {
                    statusConsumer.accept("OAuth device flow failed. Please try again.");
                }
                return false;
            }
        } catch (Exception e) {
            logger.error("Device flow failed with error: " + e.getMessage(), e);
            if (statusConsumer != null) {
                statusConsumer.accept("OAuth device flow failed. Please try again.");
            }
            return false;
        }
    }

    /**
     * Reads the error response from an HTTP connection.
     */
    private String readErrorResponse(HttpURLConnection conn) {
        try {
            InputStream in = conn.getErrorStream();
            if (in == null) {
                return null;
            }
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            return response.toString();
        } catch (Exception e) {
            logger.debug("Error reading error response", e);
            return null;
        }
    }
} 