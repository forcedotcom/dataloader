package com.salesforce.dataloader.util;

import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.client.transport.SimplePostFactory;
import com.salesforce.dataloader.client.transport.SimplePostInterface;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.FieldNamingPolicy;

/**
 * JWT Bearer flow implementation for unattended / batch authentication.
 */
public class JwtBearerFlow {

    private static final Logger logger = DLLogManager.getLogger(JwtBearerFlow.class);

    private final AppConfig appConfig;

    public JwtBearerFlow(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    /**
     * Builds, signs and exchanges a JWT for an access token.
     */
    public TokenResponse obtainAccessToken() throws Exception {
        String assertion = buildSignedJwt();
        return exchangeJwtForToken(assertion);
    }

    private String buildSignedJwt() throws Exception {
        String clientId = appConfig.getEffectiveClientIdForCurrentEnv();
        String username = appConfig.getString(AppConfig.PROP_OAUTH_JWT_USERNAME);
        String audience = appConfig.getAuthEndpointForCurrentEnv();
        if (audience.endsWith("/")) {
            audience = audience.substring(0, audience.length() - 1);
        }

        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("Client ID (iss) is not configured");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("JWT username (sub) is not configured");
        }

        // Header
        String headerJson = "{\"alg\":\"RS256\"}";
        String header = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));

        // Claims
        long exp = (System.currentTimeMillis() / 1000L) + 180; // 3 minutes
        String claimsJson = String.format(
                "{\"iss\":\"%s\",\"sub\":\"%s\",\"aud\":\"%s\",\"exp\":%d}",
                escapeJson(clientId),
                escapeJson(username),
                escapeJson(audience),
                exp
        );
        String claims = base64UrlEncode(claimsJson.getBytes(StandardCharsets.UTF_8));

        // Sign
        String unsigned = header + "." + claims;
        PrivateKey privateKey = loadPrivateKey();
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(unsigned.getBytes(StandardCharsets.UTF_8));
        byte[] sigBytes = signature.sign();
        String sig = base64UrlEncode(sigBytes);

        return unsigned + "." + sig;
    }

    private PrivateKey loadPrivateKey() throws Exception {
        String keyPath = appConfig.getString(AppConfig.PROP_OAUTH_JWT_PRIVATE_KEY);
        if (keyPath == null || keyPath.isBlank()) {
            throw new IllegalStateException("JWT private key file path is not configured");
        }

        String pem = Files.readString(Paths.get(keyPath), StandardCharsets.UTF_8);
        pem = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                 .replace("-----END PRIVATE KEY-----", "")
                 .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                 .replace("-----END RSA PRIVATE KEY-----", "")
                 .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    private TokenResponse exchangeJwtForToken(String assertion) throws Exception {
        String tokenUrl = appConfig.getAuthEndpointForCurrentEnv();
        if (!tokenUrl.endsWith("/")) {
            tokenUrl += "/";
        }
        tokenUrl += "services/oauth2/token";

        List<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer"));
        params.add(new BasicNameValuePair("assertion", assertion));

        SimplePostInterface client = SimplePostFactory.getInstance(
                appConfig,
                tokenUrl,
                params.toArray(new BasicNameValuePair[0])
        );

        client.post();

        if (!client.isSuccessful()) {
            logErrorResponse(client.getInput());
            throw new RuntimeException("JWT token request failed with status " + client.getStatusCode());
        }

        Map<?, ?> response = parseJsonResponse(client.getInput());
        if (response == null || !response.containsKey("access_token")) {
            throw new RuntimeException("JWT token response did not contain access_token");
        }

        String accessToken = (String) response.get("access_token");
        String instanceUrl = (String) response.get("instance_url");

        logger.info("JWT Bearer authentication succeeded");
        return new TokenResponse(accessToken, instanceUrl);
    }

    private Map<?, ?> parseJsonResponse(InputStream inputStream) throws Exception {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        return gson.fromJson(builder.toString(), Map.class);
    }

    private void logErrorResponse(InputStream inputStream) {
        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            logger.error("JWT token error response: " + sb);
        } catch (Exception e) {
            logger.debug("Could not read JWT error response", e);
        }
    }

    private static String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private static String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static class TokenResponse {
        public final String accessToken;
        public final String instanceUrl;

        public TokenResponse(String accessToken, String instanceUrl) {
            this.accessToken = accessToken;
            this.instanceUrl = instanceUrl;
        }
    }
}
