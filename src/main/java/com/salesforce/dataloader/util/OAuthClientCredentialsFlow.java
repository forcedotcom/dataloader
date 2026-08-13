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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.Logger;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.salesforce.dataloader.client.transport.SimplePostFactory;
import com.salesforce.dataloader.client.transport.SimplePostInterface;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.model.OAuthToken;

/**
 * OAuth 2.0 Client Credentials Flow for unattended batch authentication.
 */
public class OAuthClientCredentialsFlow {
    private static final Logger logger = DLLogManager.getLogger(OAuthClientCredentialsFlow.class);
    private static final String TOKEN_ENDPOINT_PATH = "/services/oauth2/token";
    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private final AppConfig appConfig;

    public OAuthClientCredentialsFlow(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    /**
     * Exchanges the configured External Client App credentials for an access token.
     *
     * @return true when an access token is obtained
     * @throws ParameterLoadException if configuration is incomplete or the token request fails
     */
    public boolean performOAuthFlow() throws ParameterLoadException {
        String clientId = requireValue(
                appConfig.getECAClientIdForCurrentEnv(),
                getECAPropertyName(AppConfig.PROP_ECA_CLIENT_ID_PROD, AppConfig.PROP_ECA_CLIENT_ID_SANDBOX));
        String clientSecret = requireValue(
                appConfig.getECAClientSecretForCurrentEnv(),
                getECAPropertyName(AppConfig.PROP_ECA_CLIENT_SECRET_PROD, AppConfig.PROP_ECA_CLIENT_SECRET_SANDBOX));

        String tokenUrl = appConfig.getAuthEndpointForCurrentEnv() + TOKEN_ENDPOINT_PATH;
        SimplePostInterface client = SimplePostFactory.getInstance(
                appConfig,
                tokenUrl,
                new BasicNameValuePair("grant_type", AppConfig.OAUTH_FLOW_CLIENT_CREDENTIALS),
                new BasicNameValuePair("client_id", clientId),
                new BasicNameValuePair("client_secret", clientSecret));

        try {
            client.post();
        } catch (ParameterLoadException e) {
            throw e;
        } catch (IOException e) {
            throw new ParameterLoadException("OAuth client credentials token request failed", e);
        }

        if (!client.isSuccessful()) {
            throw new ParameterLoadException(buildTokenRequestError(client.getInput()));
        }

        OAuthToken token = parseToken(client.getInput());
        if (token == null || isBlank(token.getAccessToken()) || isBlank(token.getInstanceUrl())) {
            throw new ParameterLoadException("OAuth client credentials token response is missing access_token or instance_url");
        }

        appConfig.setValue(AppConfig.PROP_OAUTH_ACCESSTOKEN, token.getAccessToken());
        appConfig.setValue(AppConfig.PROP_OAUTH_INSTANCE_URL, token.getInstanceUrl());
        appConfig.setAuthEndpointForCurrentEnv(token.getInstanceUrl());
        appConfig.setLastOAuthFlow("Client Credentials");

        logger.info("OAuth Client Credentials Flow completed successfully using External Client App");
        return true;
    }

    private String getECAPropertyName(String productionPropertyName, String sandboxPropertyName) {
        return AppConfig.SERVER_PROD_ENVIRONMENT_VAL.equals(
                appConfig.getString(AppConfig.PROP_SELECTED_SERVER_ENVIRONMENT))
                        ? productionPropertyName
                        : sandboxPropertyName;
    }

    private static String requireValue(String value, String propertyName) throws ParameterLoadException {
        if (isBlank(value)) {
            throw new ParameterLoadException("Missing required parameter: " + propertyName);
        }
        return value;
    }

    private static OAuthToken parseToken(InputStream inputStream) throws ParameterLoadException {
        if (inputStream == null) {
            throw new ParameterLoadException("OAuth client credentials token response is empty");
        }
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, OAuthToken.class);
        } catch (Exception e) {
            throw new ParameterLoadException("Failed to parse OAuth client credentials token response", e);
        }
    }

    private static String buildTokenRequestError(InputStream inputStream) {
        String message = "OAuth client credentials token request was rejected by Salesforce";
        if (inputStream == null) {
            return message;
        }
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            Map<?, ?> response = GSON.fromJson(reader, Map.class);
            Object error = response == null ? null : response.get("error");
            Object description = response == null ? null : response.get("error_description");
            if (description != null && !description.toString().isBlank()) {
                return message + ": " + description;
            }
            if (error != null && !error.toString().isBlank()) {
                return message + ": " + error;
            }
        } catch (Exception e) {
            logger.debug("Could not parse OAuth client credentials error response", e);
        }
        return message;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
