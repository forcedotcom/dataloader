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

package com.salesforce.dataloader.util;

import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.ui.Labels;
import com.salesforce.dataloader.ui.URLUtil;
import com.salesforce.dataloader.client.transport.SimplePostFactory;
import com.salesforce.dataloader.client.transport.SimplePostInterface;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.FieldNamingPolicy;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

/**
 * OAuth 2.0 Authorization Code Flow with PKCE implementation for browser-based authentication.
 * This follows the same pattern as Salesforce CLI for maximum compatibility.
 */
public class OAuthBrowserFlow {
    private static final Logger logger = DLLogManager.getLogger(OAuthBrowserFlow.class);
    
    private static final String REDIRECT_URI_PATH = "/OauthRedirect"; // Same as Salesforce CLI
    
    private final AppConfig appConfig;
    private HttpServer server;
    private String authorizationCode;
    private String state;
    private String codeVerifier;
    private String codeChallenge;
    private CountDownLatch authLatch = new CountDownLatch(1);
    private boolean isRunning = false;
    private int port;
    
    public OAuthBrowserFlow(AppConfig appConfig) throws ParameterLoadException {
        this.appConfig = appConfig;
		this.port = findAvailablePort();
		if (this.port == 0) {
			throw new ParameterLoadException("No available port found for OAuth callback server");
		}
		logger.debug("Using port " + this.port + " for OAuth callback server");
    }
    
    /**
     * Performs the OAuth 2.0 Authorization Code Flow with PKCE.
     * 
     * @return true if OAuth flow completed successfully, false otherwise
     */
    public boolean performOAuthFlow() throws com.salesforce.dataloader.exception.ParameterLoadException {
        int timeout = appConfig.getOAuthTimeoutSeconds();
        return performOAuthFlow(timeout);
    }
    
    /**
     * Performs the OAuth 2.0 Authorization Code Flow with PKCE.
     * 
     * @param timeoutSeconds Maximum time to wait for authorization
     * @return true if OAuth flow completed successfully, false otherwise
     */
    public boolean performOAuthFlow(int timeoutSeconds) throws com.salesforce.dataloader.exception.ParameterLoadException {
        try {
            // Step 1: Generate PKCE parameters
            generatePKCEParameters();
            
            // Step 2: Start local HTTP server
            startCallbackServer();
            
            // Step 3: Build authorization URL
            String authUrl = buildAuthorizationUrl();
            logger.info("Opening browser for OAuth authorization: " + authUrl);
            
            // Step 4: Open browser
            URLUtil.openURL(authUrl);
            
            // Step 5: Wait for authorization callback
            logger.info("Waiting for OAuth authorization (timeout: " + timeoutSeconds + " seconds)...");
            boolean authReceived = authLatch.await(timeoutSeconds, TimeUnit.SECONDS);
            
            if (authReceived && authorizationCode != null) {
                // Step 6: Exchange authorization code for tokens
                return exchangeCodeForTokens();
            } else {
                logger.warn("OAuth authorization timed out or failed");
                return false;
            }
            
        } catch (com.salesforce.dataloader.exception.ParameterLoadException e) {
            // Let ParameterLoadException propagate to handler
            throw e;
        } catch (Exception e) {
            logger.error("OAuth browser flow failed", e);
            return false;
        } finally {
            stopCallbackServer();
        }
    }
    
    /**
     * Generates PKCE (Proof Key for Code Exchange) parameters for enhanced security.
     */
    private void generatePKCEParameters() throws Exception {
        // Generate code verifier (43-128 characters, URL-safe)
        SecureRandom random = new SecureRandom();
        byte[] codeVerifierBytes = new byte[32];
        random.nextBytes(codeVerifierBytes);
        codeVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifierBytes);
        
        // Generate code challenge (SHA256 hash of code verifier)
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] challengeBytes = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
        codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(challengeBytes);
        
        // Generate state parameter for CSRF protection
        byte[] stateBytes = new byte[16];
        random.nextBytes(stateBytes);
        state = Base64.getUrlEncoder().withoutPadding().encodeToString(stateBytes);
        
        logger.debug("Generated PKCE parameters - state: " + state);
    }
    
    /**
     * Starts the local HTTP server to receive OAuth callback.
     */
    private void startCallbackServer() throws IOException {
        IOException lastException = null;
        
        // Try to start server on the selected port first
        for (int attempt = 0; attempt < 100; attempt++) {
            try {
                int tryPort = (port + attempt);
                server = HttpServer.create(new InetSocketAddress("localhost", tryPort), 0);
                server.createContext(REDIRECT_URI_PATH, new CallbackHandler());
                server.createContext("/", new InstructionsHandler());
                server.setExecutor(null);
                server.start();
                isRunning = true;
                port = tryPort; // Update port to the one that actually worked
                logger.info("OAuth callback server started on localhost:" + port);
                return;
            } catch (IOException e) {
                lastException = e;
                // Port not available, try next port
                if (server != null) {
                    try {
                        server.stop(0);
                    } catch (Exception ignored) {}
                    server = null;
                }
            }
        }
        
        // If we get here, no port was available
        throw new IOException("Could not find available port for OAuth callback server", lastException);
    }
    
    /**
     * Stops the local HTTP server.
     */
    private void stopCallbackServer() {
        if (server != null && isRunning) {
            server.stop(0);
            isRunning = false;
            logger.info("OAuth callback server stopped");
        }
    }
    
    /**
     * Builds the OAuth authorization URL with PKCE parameters.
     */
    private String buildAuthorizationUrl() throws Exception {
        String baseUrl = appConfig.getAuthEndpointForCurrentEnv();
        String clientId = appConfig.getEffectiveClientIdForCurrentEnv();
        String redirectUri = "http://localhost:" + port + REDIRECT_URI_PATH;
        
        StringBuilder authUrl = new StringBuilder();
        authUrl.append(baseUrl).append("/services/oauth2/authorize");
        authUrl.append("?response_type=code");
        authUrl.append("&client_id=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8.name()));
        authUrl.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.name()));
        authUrl.append("&scope=").append(URLEncoder.encode("api refresh_token", StandardCharsets.UTF_8.name()));
        authUrl.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8.name()));
        authUrl.append("&code_challenge=").append(URLEncoder.encode(codeChallenge, StandardCharsets.UTF_8.name()));
        authUrl.append("&code_challenge_method=S256");
        
        return authUrl.toString();
    }
    
    /**
     * Exchanges the authorization code for access and refresh tokens.
     */
    private boolean exchangeCodeForTokens() throws com.salesforce.dataloader.exception.ParameterLoadException {
        logger.info("Exchanging authorization code for tokens");
        try {
            // Create token request
            String tokenUrl = appConfig.getAuthEndpointForCurrentEnv() + "/services/oauth2/token";
            String clientId = appConfig.getEffectiveClientIdForCurrentEnv();
            String clientSecret = appConfig.getEffectiveClientSecretForCurrentEnv();
            String redirectUri = "http://localhost:" + port + REDIRECT_URI_PATH;
            
            // Build token request parameters
            List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("grant_type", "authorization_code"));
            params.add(new BasicNameValuePair("client_id", clientId));
            params.add(new BasicNameValuePair("code", authorizationCode));
            params.add(new BasicNameValuePair("redirect_uri", redirectUri));
            params.add(new BasicNameValuePair("code_verifier", codeVerifier));
            
            // Add client secret if using External Client App (confidential client)
            if (appConfig.isExternalClientAppConfigured() && clientSecret != null && !clientSecret.trim().isEmpty()) {
                params.add(new BasicNameValuePair("client_secret", clientSecret));
                logger.debug("Using confidential client (External Client App) with client secret");
            } else {
                logger.debug("Using public client (Connected App) with PKCE only");
            }
            
            // Make token request using existing HTTP client infrastructure
            SimplePostInterface client = SimplePostFactory.getInstance(
                appConfig, 
                tokenUrl,
                params.toArray(new BasicNameValuePair[0])
            );
            
            client.post();
            
            if (!client.isSuccessful()) {
                String errorResponse = null;
                try {
                    ByteArrayOutputStream result = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    InputStream in = client.getInput();
                    for (int length; (length = in.read(buffer)) != -1; ) {
                        result.write(buffer, 0, length);
                    }
                    errorResponse = result.toString(StandardCharsets.UTF_8.name());
                } catch (Exception e) {
                    // ignore
                }
                logger.error("Token exchange failed with status: " + client.getStatusCode() + " - " + client.getReasonPhrase());
                logErrorResponse(client.getInput());
                if (errorResponse != null && errorResponse.contains("invalid_client")) {
                    if (appConfig.isExternalClientAppConfigured() && 
                        (appConfig.getECAClientSecretForCurrentEnv() == null || appConfig.getECAClientSecretForCurrentEnv().isEmpty())) {
                        logger.error("External Client App (ECA) is configured, but the client secret (Consumer Secret) is missing in Data Loader configuration. " +
                                     "Please set the ECA client secret or uncheck 'Require Secret for Web Server Flow' in your Salesforce Connected App.");
                        throw new com.salesforce.dataloader.exception.ParameterLoadException("Missing ECA client secret for confidential flow.");
                    }
                }
                return false;
            }
            
            // Parse JSON response
            Map<?, ?> response = parseJsonResponse(client.getInput());
            
            if (response != null && response.containsKey("access_token")) {
                String accessToken = (String) response.get("access_token");
                String refreshToken = (String) response.get("refresh_token");
                String instanceUrl = (String) response.get("instance_url");
                
                // Configure AppConfig with OAuth tokens
                appConfig.setValue(AppConfig.PROP_OAUTH_ACCESSTOKEN, accessToken);
                if (refreshToken != null) {
                    appConfig.setValue(AppConfig.PROP_OAUTH_REFRESHTOKEN, refreshToken);
                }
                if (instanceUrl != null) {
                    appConfig.setAuthEndpointForCurrentEnv(instanceUrl);
                    appConfig.setValue(AppConfig.PROP_OAUTH_INSTANCE_URL, instanceUrl);
                }
                
                logger.info("OAuth tokens obtained successfully using " + 
                    (appConfig.isExternalClientAppConfigured() ? "External Client App" : "Connected App"));
                return true;
            } else {
                logger.error("Failed to obtain OAuth tokens: " + response);
                return false;
            }
        } catch (com.salesforce.dataloader.exception.ParameterLoadException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to exchange authorization code for tokens", e);
            return false;
        }
    }
    
    /**
     * Handles the OAuth callback from Salesforce.
     */
    private class CallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            logger.debug("Received OAuth callback: " + exchange.getRequestURI());
            
            try {
                Map<String, String> params = parseQueryString(query);
                
                // Check for error
                if (params.containsKey("error")) {
                    logger.error("OAuth error: " + params.get("error") + " - " + params.get("error_description"));
                    String errorMessage = Labels.getFormattedString("OAuthBrowserFlow.error.authFailed", params.get("error_description"));
                    sendErrorResponse(exchange, errorMessage);
                    authLatch.countDown();
                    return;
                }
                
                // Verify state parameter (CSRF protection)
                String returnedState = params.get("state");
                if (!state.equals(returnedState)) {
                    logger.error("State parameter mismatch - possible CSRF attack");
                    sendErrorResponse(exchange, "Authorization failed: invalid state parameter");
                    authLatch.countDown();
                    return;
                }
                
                // Extract authorization code
                authorizationCode = params.get("code");
                if (authorizationCode != null && !authorizationCode.trim().isEmpty()) {
                    logger.info("Authorization code received successfully");
                    sendSuccessResponse(exchange);
                    authLatch.countDown();
                } else {
                    logger.error("No authorization code received");
                    sendErrorResponse(exchange, "Authorization failed: no code received");
                    authLatch.countDown();
                }
                
            } catch (Exception e) {
                logger.error("Error handling OAuth callback", e);
                sendErrorResponse(exchange, "Error processing authorization callback");
                authLatch.countDown();
            }
        }
    }
    
    /**
     * Handles requests to the root path with instructions.
     */
    private class InstructionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = buildInstructionsPage();
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
    
    /**
     * Builds the instructions page HTML.
     */
    private String buildInstructionsPage() {
        return Labels.getString("OAuthBrowserFlow.instructionsPage");
    }
    
    /**
     * Sends a success response after OAuth completion.
     */
    private void sendSuccessResponse(HttpExchange exchange) throws IOException {
        String response = Labels.getString("OAuthBrowserFlow.successResponse");
        
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
    
    /**
     * Sends an error response.
     */
    private void sendErrorResponse(HttpExchange exchange, String message) throws IOException {
        String response = Labels.getFormattedString("OAuthBrowserFlow.errorResponseTemplate", message);
        
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(400, response.getBytes(StandardCharsets.UTF_8).length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
    
    /**
     * Parses query string into key-value pairs.
     */
    private Map<String, String> parseQueryString(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.trim().isEmpty()) {
            return params;
        }
        
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                try {
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8.name());
                    String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name());
                    params.put(key, value);
                } catch (Exception e) {
                    logger.debug("Error decoding query parameter: " + pair, e);
                }
            }
        }
        return params;
    }
    
    /**
     * Parse JSON response from OAuth endpoint.
     */
    private Map<?, ?> parseJsonResponse(InputStream inputStream) {
        try {
            StringBuilder builder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8.name()));
            for (int c = bufferedReader.read(); c != -1; c = bufferedReader.read()) {
                builder.append((char) c);
            }
            
            String jsonResponse = builder.toString();
            logger.debug("OAuth response: " + jsonResponse);
            
            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();
            
            return gson.fromJson(jsonResponse, Map.class);
            
        } catch (Exception e) {
            logger.error("Failed to parse JSON response", e);
            return null;
        }
    }
    
    /**
     * Log error response for debugging.
     */
    private void logErrorResponse(InputStream inputStream) {
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int length; (length = inputStream.read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }
            String response = result.toString(StandardCharsets.UTF_8.name());
            logger.error("Error response: " + response);
        } catch (Exception e) {
            logger.debug("Could not read error response", e);
        }
    }
    
    /**
     * Finds an available port for the callback server.
     */
    private int findAvailablePort() {
        int preferredPort;
        try {
            preferredPort = appConfig.getInt(AppConfig.PROP_OAUTH_PKCE_PORT);
        } catch (Exception e) {
            logger.debug("Error reading OAuth PKCE port configuration, using default", e);
            preferredPort = AppConfig.DEFAULT_OAUTH_PKCE_PORT; // Default fallback
        }
        
        try (ServerSocket socket = new ServerSocket(preferredPort)) {
            return preferredPort;
        } catch (Exception e) {
			logger.fatal("PKCE port " + preferredPort + " is not available, searching for another port", e);
        }
        return 0;
    }
} 