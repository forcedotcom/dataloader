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
import com.salesforce.dataloader.util.OAuthFlowNotEnabledException;
import com.salesforce.dataloader.client.transport.SimplePostFactory;
import com.salesforce.dataloader.client.transport.SimplePostInterface;
import org.apache.http.message.BasicNameValuePair;
import java.io.ByteArrayOutputStream;

/**
 * Utility class to handle OAuth flows (PKCE and Device Flow) for both UI and batch modes.
 */
public class OAuthFlowHandler {
    private static final Logger logger = DLLogManager.getLogger(OAuthFlowHandler.class);
    private final AppConfig appConfig;
    private final Consumer<String> statusConsumer;
    private final Controller controller;
    private final Runnable loginButtonEnabler;

    public OAuthFlowHandler(AppConfig appConfig, Consumer<String> statusConsumer, Controller controller, Runnable loginButtonEnabler) {
        this.appConfig = appConfig;
        this.statusConsumer = statusConsumer;
        this.controller = controller;
        this.loginButtonEnabler = loginButtonEnabler;
    }

    /**
     * Handles the OAuth login process, attempting PKCE flow first if not disabled,
     * falling back to device flow if PKCE is not supported or fails.
     *
     * @return true if login was successful, false otherwise
     */
    public boolean handleOAuthLogin() {
        logger.info("Starting OAuth flow");
        appConfig.setLastOAuthFlow("PKCE");

        String deviceLoginFromBrowser = appConfig.getString(AppConfig.PROP_OAUTH_LOGIN_FROM_BROWSER_DEVICE_OAUTH);
        boolean deviceLoginFromBrowserEnabled = "true".equalsIgnoreCase(deviceLoginFromBrowser);
        logger.debug("Device login from browser setting: " + deviceLoginFromBrowser);
        logger.debug("Device login from browser enabled: " + deviceLoginFromBrowserEnabled);

        if (deviceLoginFromBrowserEnabled) {
            logger.info("Device login from browser is enabled, using device flow");
            return handleDeviceFlow();
        }

        logger.info("Checking if PKCE flow is enabled in Connected App...");
        boolean pkceEnabled = isPkceFlowEnabled();
        logger.info("PKCE flow enabled: " + pkceEnabled);
        logger.info("Checking if browser flow is enabled in Connected App...");
        boolean browserEnabled = isBrowserFlowEnabled();
        logger.info("Browser flow enabled: " + browserEnabled);
        logger.info("Checking if device flow is enabled in Connected App...");
        boolean deviceEnabled = isDeviceFlowEnabled();
        logger.info("Device flow enabled: " + deviceEnabled);

        if (pkceEnabled) {
            logger.info("PKCE flow is enabled, launching browser for PKCE login");
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
                    if (controller != null) {
                        try {
                            if (controller.login()) {
                                controller.saveConfig();
                                Display.getDefault().asyncExec(() -> controller.updateLoaderWindowTitleAndCacheUserInfoForTheSession());
                                if (loginButtonEnabler != null) {
                                    Display.getDefault().asyncExec(loginButtonEnabler);
                                }
                                return true;
                            }
                        } catch (Exception e) {
                            logger.error("Failed to update controller's login state after PKCE flow", e);
                            if (statusConsumer != null) {
                                statusConsumer.accept(Labels.getString("OAuthLoginControl.statusControllerUpdateError"));
                            }
                            if (loginButtonEnabler != null) {
                                Display.getDefault().asyncExec(loginButtonEnabler);
                            }
                            return false;
                        }
                    }
                    if (loginButtonEnabler != null) {
                        Display.getDefault().asyncExec(loginButtonEnabler);
                    }
                    return true;
                }
            } catch (Exception e) {
                logger.error("PKCE flow failed: " + e.getMessage(), e);
                if (statusConsumer != null) {
                    statusConsumer.accept(Labels.getString("OAuthLoginControl.statusPKCEFailedFallbackBrowser"));
                }
                if (loginButtonEnabler != null) {
                    Display.getDefault().asyncExec(loginButtonEnabler);
                }
                return false;
            }
        } else if (browserEnabled) {
            logger.info("Browser flow is enabled, launching browser for login");
            if (statusConsumer != null) {
                statusConsumer.accept(Labels.getString("OAuthLoginControl.statusAttemptingBrowser"));
            }
            try {
                OAuthBrowserFlow browserFlow = new OAuthBrowserFlow(appConfig, false, statusConsumer);
                if (browserFlow.performOAuthFlow()) {
                    logger.info("Browser flow completed successfully");
                    if (statusConsumer != null) {
                        statusConsumer.accept(Labels.getString("OAuthLoginControl.statusBrowserSuccess"));
                    }
                    if (controller != null) {
                        try {
                            appConfig.setLastOAuthFlow("Browser");
                            boolean loginSuccess = controller.login();
                            logger.info("controller.login() after browser flow returned: " + loginSuccess);
                            if (loginSuccess) {
                                controller.saveConfig();
                                Display.getDefault().asyncExec(() -> controller.updateLoaderWindowTitleAndCacheUserInfoForTheSession());
                                if (loginButtonEnabler != null) {
                                    Display.getDefault().asyncExec(loginButtonEnabler);
                                }
                                return true;
                            } else {
                                logger.error("controller.login() returned false after browser flow. UI will not advance.");
                            }
                        } catch (Exception e) {
                            logger.error("Failed to update controller's login state after browser flow", e);
                            if (statusConsumer != null) {
                                statusConsumer.accept(Labels.getString("OAuthLoginControl.statusControllerUpdateError"));
                            }
                            if (loginButtonEnabler != null) {
                                Display.getDefault().asyncExec(loginButtonEnabler);
                            }
                            return false;
                        }
                    }
                    if (loginButtonEnabler != null) {
                        Display.getDefault().asyncExec(loginButtonEnabler);
                    }
                    return true;
                }
            } catch (Exception e) {
                logger.error("Browser flow failed: " + e.getMessage(), e);
                if (statusConsumer != null) {
                    statusConsumer.accept(Labels.getString("OAuthLoginControl.statusBrowserFailedFallbackDevice"));
                }
                if (loginButtonEnabler != null) {
                    Display.getDefault().asyncExec(loginButtonEnabler);
                }
                return false;
            }
        } else if (deviceEnabled) {
            logger.info("Device flow is enabled, launching device flow");
            if (statusConsumer != null) {
                statusConsumer.accept(Labels.getString("OAuthLoginControl.statusAttemptingDevice"));
            }
            boolean deviceResult = handleDeviceFlow();
            appConfig.setLastOAuthFlow("Device");
            if (loginButtonEnabler != null) {
                Display.getDefault().asyncExec(loginButtonEnabler);
            }
            return deviceResult;
        } else {
            logger.error("None of the supported OAuth flows are enabled in the Connected App");
            if (statusConsumer != null) {
                statusConsumer.accept(Labels.getString("OAuthLoginControl.statusNoOAuthFlowsEnabled"));
            }
            if (loginButtonEnabler != null) {
                Display.getDefault().asyncExec(loginButtonEnabler);
            }
            return false;
        }
        // Defensive return for compiler
        return false;
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

    private boolean isPkceFlowNotEnabledError(String error) {
        if (error == null) return false;
        return error.contains("unsupported_grant_type") || error.contains("invalid_grant") || error.contains("invalid_request") || error.contains("PKCE") || error.contains("code_challenge") || error.contains("redirect_uri_mismatch");
    }

    private boolean isBrowserFlowNotEnabledError(String error) {
        if (error == null) return false;
        return error.contains("unsupported_grant_type");
    }

    private boolean isPkceFlowEnabled() {
        try {
            String tokenUrl = appConfig.getAuthEndpointForCurrentEnv() + "/services/oauth2/token";
            // Use dummy code and PKCE params
            String dummyCode = "dummy";
            String codeVerifier = "dummyverifier";
            String codeChallenge = "dummychallenge";
            SimplePostInterface client = SimplePostFactory.getInstance(appConfig, tokenUrl,
                new BasicNameValuePair("grant_type", "authorization_code"),
                new BasicNameValuePair("client_id", appConfig.getEffectiveClientIdForCurrentEnv()),
                new BasicNameValuePair("code", dummyCode),
                new BasicNameValuePair("redirect_uri", "http://localhost:7171/OauthRedirect"),
                new BasicNameValuePair("code_challenge", codeChallenge),
                new BasicNameValuePair("code_challenge_method", "S256")
            );
            client.post();
            // Log the full response content, decompressing if needed
            String error = getErrorFromResponse(client);
            String fullResponse = null;
            try {
                InputStream in = client.getInput();
                if (in != null) {
                    // Check for gzip encoding
                    String encoding = null;
                    try {
                        encoding = client.getClass().getMethod("getContentEncoding").invoke(client).toString();
                    } catch (Exception e) {
                        // ignore, fallback to no encoding
                    }
                    InputStream responseStream = in;
                    if (encoding != null && encoding.toLowerCase().contains("gzip")) {
                        responseStream = new java.util.zip.GZIPInputStream(in);
                    }
                    ByteArrayOutputStream result = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    for (int length; (length = responseStream.read(buffer)) != -1; ) {
                        result.write(buffer, 0, length);
                    }
                    fullResponse = result.toString(StandardCharsets.UTF_8.name());
                }
            } catch (Exception e) {
                logger.debug("Exception reading PKCE pre-flight response: ", e);
            }
            logger.info("PKCE pre-flight error response: " + error);
            logger.info("PKCE pre-flight full response: " + fullResponse);
            // Only treat as enabled if error is not one of the known negatives
            if (error != null) {
                if (error.contains("unsupported_grant_type") ||
                    error.contains("invalid_client") ||
                    error.contains("invalid_client_credentials")) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("Exception during PKCE pre-flight check", e);
            return false;
        }
    }

    private boolean isBrowserFlowEnabled() {
        try {
            String tokenUrl = appConfig.getAuthEndpointForCurrentEnv() + "/services/oauth2/token";
            // Use dummy code, NO PKCE params
            String dummyCode = "dummy";
            SimplePostInterface client = SimplePostFactory.getInstance(appConfig, tokenUrl,
                new BasicNameValuePair("grant_type", "authorization_code"),
                new BasicNameValuePair("client_id", appConfig.getEffectiveClientIdForCurrentEnv()),
                new BasicNameValuePair("code", dummyCode),
                new BasicNameValuePair("redirect_uri", "http://localhost:7171/OauthRedirect")
            );
            client.post();
            String error = getErrorFromResponse(client);
            // Only treat as enabled if error is not one of the known negatives
            if (error != null) {
                if (error.contains("unsupported_response_type") ||
                    error.contains("invalid_client") ||
                    error.contains("invalid_client_credentials")) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isDeviceFlowEnabled() {
        try {
            String tokenUrl = appConfig.getAuthEndpointForCurrentEnv() + "/services/oauth2/token";
            SimplePostInterface client = SimplePostFactory.getInstance(appConfig, tokenUrl,
                new BasicNameValuePair("response_type", "device_code"),
                new BasicNameValuePair(AppConfig.CLIENT_ID_HEADER_NAME, appConfig.getEffectiveClientIdForCurrentEnv()),
                new BasicNameValuePair("scope", "api")
            );
            client.post();
            String error = getErrorFromResponse(client);
            logger.info("Device flow pre-flight error response: " + error);
            // Only treat as enabled if error is not one of the known negatives
            if (error != null) {
                if (error.contains("unsupported_grant_type") ||
                    error.contains("invalid_client") ||
                    error.contains("invalid_client_credentials")) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("Exception during device flow pre-flight check", e);
            return false;
        }
    }

    private String getErrorFromResponse(SimplePostInterface client) {
        try {
            InputStream in = client.getInput();
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int length; (length = in.read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }
            String response = result.toString(StandardCharsets.UTF_8.name());
            if (response.contains("error")) {
                return response;
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private boolean isFlowNotEnabledError(String error) {
        if (error == null) return false;
        return error.contains("unsupported_grant_type") || error.contains("invalid_grant") || error.contains("invalid_request") || error.contains("PKCE") || error.contains("code_challenge");
    }
} 