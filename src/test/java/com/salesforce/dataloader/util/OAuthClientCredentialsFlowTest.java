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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.http.Header;
import org.apache.http.message.BasicNameValuePair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.salesforce.dataloader.client.transport.SimplePostFactory;
import com.salesforce.dataloader.client.transport.SimplePostInterface;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.exception.ParameterLoadException;

public class OAuthClientCredentialsFlowTest {
    private Function<SimplePostFactory.Criteria, SimplePostInterface> originalPostConstructor;
    private AppConfig appConfig;

    @Before
    public void setUp() throws Exception {
        originalPostConstructor = SimplePostFactory.getConstructor();
        Path configDir = Files.createTempDirectory("dataloader-client-credentials-test");
        Map<String, String> args = new HashMap<>();
        args.put(AppConfig.CLI_OPTION_CONFIG_DIR_PROP, configDir.toString());
        appConfig = AppConfig.getInstance(args);
        appConfig.setValue(AppConfig.PROP_SELECTED_SERVER_ENVIRONMENT, AppConfig.SERVER_PROD_ENVIRONMENT_VAL);
        appConfig.setValue(AppConfig.PROP_ECA_CLIENT_ID_PROD, "client-id");
        appConfig.setValue(AppConfig.PROP_ECA_CLIENT_SECRET_PROD, "client-secret");
    }

    @After
    public void tearDown() {
        SimplePostFactory.setConstructor(originalPostConstructor);
    }

    @Test
    public void testSuccessfulClientCredentialsFlow() throws Exception {
        SimplePostFactory.setConstructor(criteria -> {
            assertEquals(appConfig.getAuthEndpointForCurrentEnv() + "/services/oauth2/token", criteria.endpoint);
            assertEquals("client_credentials", findPair(criteria.pairs, "grant_type"));
            assertEquals("client-id", findPair(criteria.pairs, "client_id"));
            assertEquals("client-secret", findPair(criteria.pairs, "client_secret"));
            return new StubSimplePost(true,
                    "{\"access_token\":\"token-value\",\"instance_url\":\"https://example.my.salesforce.com\"}");
        });

        assertTrue(new OAuthClientCredentialsFlow(appConfig).performOAuthFlow());
        assertEquals("token-value", appConfig.getString(AppConfig.PROP_OAUTH_ACCESSTOKEN));
        assertEquals("https://example.my.salesforce.com", appConfig.getString(AppConfig.PROP_OAUTH_INSTANCE_URL));
        assertEquals("Client Credentials", appConfig.getLastOAuthFlow());
    }

    @Test
    public void testMissingClientSecretFailsBeforeTokenRequest() {
        appConfig.setValue(AppConfig.PROP_ECA_CLIENT_SECRET_PROD, "");
        SimplePostFactory.setConstructor(criteria -> {
            throw new AssertionError("Token endpoint must not be called when configuration is incomplete");
        });

        ParameterLoadException exception = assertThrows(
                ParameterLoadException.class,
                () -> new OAuthClientCredentialsFlow(appConfig).performOAuthFlow());

        assertTrue(exception.getMessage().contains(AppConfig.PROP_ECA_CLIENT_SECRET_PROD));
    }

    @Test
    public void testRejectedTokenRequestReturnsSalesforceErrorDescription() {
        SimplePostFactory.setConstructor(criteria -> new StubSimplePost(false,
                "{\"error\":\"invalid_client\",\"error_description\":\"invalid client credentials\"}"));

        ParameterLoadException exception = assertThrows(
                ParameterLoadException.class,
                () -> new OAuthClientCredentialsFlow(appConfig).performOAuthFlow());

        assertTrue(exception.getMessage().contains("invalid client credentials"));
    }

    private static String findPair(BasicNameValuePair[] pairs, String name) {
        for (BasicNameValuePair pair : pairs) {
            if (name.equals(pair.getName())) {
                return pair.getValue();
            }
        }
        return null;
    }

    private static class StubSimplePost implements SimplePostInterface {
        private final boolean successful;
        private final byte[] response;

        StubSimplePost(boolean successful, String response) {
            this.successful = successful;
            this.response = response.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public void post() throws IOException, ParameterLoadException {
        }

        @Override
        public boolean isSuccessful() {
            return successful;
        }

        @Override
        public InputStream getInput() {
            return new ByteArrayInputStream(response);
        }

        @Override
        public int getStatusCode() {
            return successful ? 200 : 400;
        }

        @Override
        public String getReasonPhrase() {
            return successful ? "OK" : "Bad Request";
        }

        @Override
        public Header[] getResponseHeaders(String headerName) {
            return new Header[0];
        }

        @Override
        public void addBasicNameValuePair(BasicNameValuePair pair) {
        }
    }
}
