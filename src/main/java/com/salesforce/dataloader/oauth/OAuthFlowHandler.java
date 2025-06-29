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
import com.salesforce.dataloader.ui.Labels;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.widgets.Display;

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

        // If device login from browser is explicitly enabled, use device flow directly
        if (deviceLoginFromBrowserEnabled) {
            logger.info("Device login from browser is enabled, using device flow");
            return handleDeviceFlow();
        }

        // Always attempt PKCE flow first
        logger.info("Attempting PKCE flow first");
        if (statusConsumer != null) {
            statusConsumer.accept(Labels.getString("OAuthLoginControl.statusAttemptingPKCE"));
        }
        try {
            OAuthBrowserFlow pkceFlow = new OAuthBrowserFlow(appConfig, true, statusConsumer);
            if (pkceFlow.performOAuthFlow()) {
                logger.info("PKCE flow completed successfully");
                if (statusConsumer != null) {
                    statusConsumer.accept(Labels.getString("OAuthLoginControl.statusPKCESuccess"));
                }
                // Update controller's login state
                if (controller != null) {
                    try {
                        if (controller.login()) {
                            controller.saveConfig();
                            Display.getDefault().asyncExec(() -> controller.updateLoaderWindowTitleAndCacheUserInfoForTheSession());
                            return true;
                        }
                    } catch (Exception e) {
                        logger.error("Failed to update controller's login state after PKCE flow", e);
                        if (statusConsumer != null) {
                            statusConsumer.accept(Labels.getString("OAuthLoginControl.statusControllerUpdateError"));
                        }
                        return false;
                    }
                }
                return true;
            } else {
                logger.warn("PKCE flow did not complete successfully, attempting browser flow without PKCE");
                if (statusConsumer != null) {
                    statusConsumer.accept(Labels.getString("OAuthLoginControl.statusPKCEFailedFallbackBrowser"));
                }
                // Fallback: try browser flow without PKCE
                OAuthBrowserFlow browserFlow = new OAuthBrowserFlow(appConfig, false, statusConsumer);
                if (browserFlow.performOAuthFlow()) {
                    logger.info("Browser flow (no PKCE) completed successfully");
                    if (statusConsumer != null) {
                        statusConsumer.accept(Labels.getString("OAuthLoginControl.statusBrowserSuccess"));
                    }
                    if (controller != null) {
                        try {
                            if (controller.login()) {
                                controller.saveConfig();
                                Display.getDefault().asyncExec(() -> controller.updateLoaderWindowTitleAndCacheUserInfoForTheSession());
                                return true;
                            }
                        } catch (Exception e) {
                            logger.error("Failed to update controller's login state after browser flow", e);
                            if (statusConsumer != null) {
                                statusConsumer.accept(Labels.getString("OAuthLoginControl.statusControllerUpdateError"));
                            }
                            return false;
                        }
                    }
                    return true;
                } else {
                    logger.warn("Browser flow (no PKCE) did not complete successfully, falling back to device flow");
                    if (statusConsumer != null) {
                        statusConsumer.accept(Labels.getString("OAuthLoginControl.statusBrowserFailedFallbackDevice"));
                    }
                    logger.info("Starting device flow");
                    return handleDeviceFlow();
                }
            }
        } catch (com.salesforce.dataloader.exception.ParameterLoadException e) {
            logger.error("OAuth flow failed due to configuration error: " + e.getMessage(), e);
            if (statusConsumer != null) {
                statusConsumer.accept(Labels.getFormattedString("OAuthLoginControl.pkceConfigErrorMessage", e.getMessage()));
            }
            return false; // Do not attempt device flow
        } catch (Exception e) {
            // Check for known PKCE errors to trigger fallback
            String msg = e.getMessage();
            if (msg != null && (msg.contains("unsupported_grant_type") || msg.contains("invalid_grant") || msg.contains("invalid_request") || msg.contains("PKCE") || msg.contains("code_challenge"))) {
                logger.warn("PKCE not supported or failed with known error, attempting browser flow without PKCE: " + msg);
                if (statusConsumer != null) {
                    statusConsumer.accept(Labels.getString("OAuthLoginControl.statusPKCEFailedFallbackBrowser"));
                }
                try {
                    OAuthBrowserFlow browserFlow = new OAuthBrowserFlow(appConfig, false, statusConsumer);
                    if (browserFlow.performOAuthFlow()) {
                        logger.info("Browser flow (no PKCE) completed successfully");
                        if (statusConsumer != null) {
                            statusConsumer.accept(Labels.getString("OAuthLoginControl.statusBrowserSuccess"));
                        }
                        if (controller != null) {
                            try {
                                if (controller.login()) {
                                    controller.saveConfig();
                                    Display.getDefault().asyncExec(() -> controller.updateLoaderWindowTitleAndCacheUserInfoForTheSession());
                                    return true;
                                }
                            } catch (Exception ex) {
                                logger.error("Failed to update controller's login state after browser flow", ex);
                                if (statusConsumer != null) {
                                    statusConsumer.accept(Labels.getString("OAuthLoginControl.statusControllerUpdateError"));
                                }
                                return false;
                            }
                        }
                        return true;
                    } else {
                        logger.warn("Browser flow (no PKCE) did not complete successfully, falling back to device flow");
                        if (statusConsumer != null) {
                            statusConsumer.accept(Labels.getString("OAuthLoginControl.statusBrowserFailedFallbackDevice"));
                        }
                        logger.info("Starting device flow");
                        return handleDeviceFlow();
                    }
                } catch (Exception ex) {
                    logger.error("Unexpected error during browser OAuth flow: " + ex.getMessage(), ex);
                    if (statusConsumer != null) {
                        statusConsumer.accept(Labels.getString("OAuthLoginControl.statusUnexpectedError"));
                    }
                    return false;
                }
            } else {
                logger.error("Unexpected error during PKCE/browser OAuth flow: " + e.getMessage(), e);
                if (statusConsumer != null) {
                    statusConsumer.accept(Labels.getString("OAuthLoginControl.statusUnexpectedError"));
                }
                return false;
            }
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
            int timeoutSeconds = appConfig.getOAuthTimeoutSeconds();
            int waited = 0;
            while (deviceFlow.getLoginStatus() == OAuthBrowserDeviceLoginRunner.LoginStatus.WAIT && waited < timeoutSeconds) {
                try {
                    Thread.sleep(1000);
                    waited++;
                } catch (InterruptedException e) {
                    logger.error("Device flow interrupted", e);
                    return false;
                }
            }
            if (deviceFlow.getLoginStatus() == OAuthBrowserDeviceLoginRunner.LoginStatus.WAIT) {
                logger.error("Device flow timed out after " + timeoutSeconds + " seconds");
                if (statusConsumer != null) {
                    statusConsumer.accept("OAuth device flow timed out. Please try again or check your configuration.");
                }
                return false;
            } else if (deviceFlow.getLoginStatus() == OAuthBrowserDeviceLoginRunner.LoginStatus.SUCCESS) {
                logger.info("Device flow completed successfully");
                // Update controller's login state
                if (controller != null) {
                    try {
                        if (controller.login()) {
                            controller.saveConfig();
                            Display.getDefault().asyncExec(() -> controller.updateLoaderWindowTitleAndCacheUserInfoForTheSession());
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