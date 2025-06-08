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
import com.salesforce.dataloader.ui.URLUtil;
import com.salesforce.dataloader.util.DLLogManager;
import com.salesforce.dataloader.client.transport.SimplePostFactory;
import com.salesforce.dataloader.client.transport.SimplePostInterface;
import org.apache.http.NameValuePair;
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

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

/**
 * OAuth 2.0 Authorization Code Flow with PKCE implementation for browser-based authentication.
 * This follows the same pattern as Salesforce CLI for maximum compatibility.
 */
public class OAuthBrowserFlow {
    private static final Logger logger = DLLogManager.getLogger(OAuthBrowserFlow.class);
    
    private static final int DEFAULT_PORT = 1717; // Same as Salesforce CLI
    private static final int DEFAULT_TIMEOUT_SECONDS = 300; // 5 minutes
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
    
    public OAuthBrowserFlow(AppConfig appConfig) {
        this.appConfig = appConfig;
        this.port = findAvailablePort();
    }
    
    /**
     * Performs the OAuth 2.0 Authorization Code Flow with PKCE.
     * 
     * @return true if OAuth flow completed successfully, false otherwise
     */
    public boolean performOAuthFlow() {
        return performOAuthFlow(DEFAULT_TIMEOUT_SECONDS);
    }
    
    /**
     * Performs the OAuth 2.0 Authorization Code Flow with PKCE.
     * 
     * @param timeoutSeconds Maximum time to wait for authorization
     * @return true if OAuth flow completed successfully, false otherwise
     */
    public boolean performOAuthFlow(int timeoutSeconds) {
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
        String clientId = appConfig.getClientIDForCurrentEnv();
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
    private boolean exchangeCodeForTokens() {
        try {
            logger.info("Exchanging authorization code for tokens");
            
            // Create token request
            String tokenUrl = appConfig.getAuthEndpointForCurrentEnv() + "/services/oauth2/token";
            String clientId = appConfig.getClientIDForCurrentEnv();
            String redirectUri = "http://localhost:" + port + REDIRECT_URI_PATH;
            
            // Make token request using existing HTTP client infrastructure (public client with PKCE)
            SimplePostInterface client = SimplePostFactory.getInstance(
                appConfig, 
                tokenUrl,
                new BasicNameValuePair("grant_type", "authorization_code"),
                new BasicNameValuePair("client_id", clientId),
                new BasicNameValuePair("code", authorizationCode),
                new BasicNameValuePair("redirect_uri", redirectUri),
                new BasicNameValuePair("code_verifier", codeVerifier)
            );
            
            client.post();
            
            if (!client.isSuccessful()) {
                logger.error("Token exchange failed with status: " + client.getStatusCode() + " - " + client.getReasonPhrase());
                logErrorResponse(client.getInput());
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
                
                logger.info("OAuth tokens obtained successfully");
                return true;
            } else {
                logger.error("Failed to obtain OAuth tokens: " + response);
                return false;
            }
            
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
                    sendErrorResponse(exchange, "OAuth authorization failed: " + params.get("error_description"));
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
        return "<!DOCTYPE html>\n" +
               "<html>\n" +
               "<head>\n" +
               "    <title>OAuth Authorization - Salesforce Data Loader</title>\n" +
               "    <style>\n" +
               "        body { font-family: Arial, sans-serif; text-align: center; padding: 50px; background-color: #f4f6f9; }\n" +
               "        .container { max-width: 600px; margin: 0 auto; background: white; padding: 40px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n" +
               "        .title { color: #007bff; font-size: 24px; margin-bottom: 20px; }\n" +
               "        .message { color: #6c757d; font-size: 16px; line-height: 1.5; }\n" +
               "    </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "    <div class=\"container\">\n" +
               "        <div class=\"title\">🔐 OAuth Authorization</div>\n" +
               "        <div class=\"message\">\n" +
               "            A Salesforce login page should have opened in your browser.<br/><br/>\n" +
               "            Please complete your login and authorize the Data Loader.<br/>\n" +
               "            You will be redirected back here automatically after authorization.\n" +
               "        </div>\n" +
               "    </div>\n" +
               "</body>\n" +
               "</html>";
    }
    
    /**
     * Sends a success response after OAuth completion.
     */
    private void sendSuccessResponse(HttpExchange exchange) throws IOException {
        String response = "<!DOCTYPE html>\n" +
                         "<html><head><title>Success</title></head>\n" +
                         "<body style='font-family:Arial;text-align:center;padding:50px;'>\n" +
                         "<h1 style='color:green;'>✓ Authorization Successful!</h1>\n" +
                         "<p>You can now close this browser window and return to Data Loader.</p>\n" +
                         "</body></html>";
        
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
        String response = "<!DOCTYPE html>\n" +
                         "<html><head><title>Error</title></head>\n" +
                         "<body style='font-family:Arial;text-align:center;padding:50px;'>\n" +
                         "<h1 style='color:red;'>❌ Authorization Failed</h1>\n" +
                         "<p>" + message + "</p>\n" +
                         "</body></html>";
        
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
    private static int findAvailablePort() {
        // First try the default port used by Salesforce CLI
        try (ServerSocket socket = new ServerSocket(DEFAULT_PORT)) {
            return DEFAULT_PORT;
        } catch (IOException e) {
            // If default port is not available, try nearby ports
            for (int tryPort = DEFAULT_PORT + 1; tryPort < DEFAULT_PORT + 100; tryPort++) {
                try (ServerSocket socket = new ServerSocket(tryPort)) {
                    return tryPort;
                } catch (IOException ignored) {
                    // Port not available, try next
                }
            }
        }
        
        // If we can't find any port in the preferred range, let the system assign one
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            return DEFAULT_PORT; // Fallback
        }
    }
} 