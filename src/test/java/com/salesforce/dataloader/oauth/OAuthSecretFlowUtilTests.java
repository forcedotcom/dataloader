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

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.salesforce.dataloader.ConfigTestBase;
import com.salesforce.dataloader.client.SimplePost;
import com.salesforce.dataloader.client.SimplePostFactory;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.model.OAuthToken;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.mockito.Mockito.*;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.function.Function;

/**
 * Created by rmazzeo on 12/9/15.
 */
public class OAuthSecretFlowUtilTests extends ConfigTestBase {

    private SimplePost mockSimplePost;
    private Config config;
    private ArrayList<String> existingOAuthEnvironments;
    private String oauthServer;
    private String oauthClientId;
    private String oauthRedirectUri;
    private String existingEndPoint;
    private Function<SimplePostFactory.Criteria, SimplePost> existingConstructor;

    @Before
    public void testSetup(){
        config = getController().getConfig();
        existingOAuthEnvironments = config.getStrings(Config.AUTH_ENVIRONMENTS);
        existingEndPoint = config.getAuthEndpoint();
        oauthServer = "https://OAUTH_PARTIAL_SERVER";
        oauthClientId = "CLIENTID";
        oauthRedirectUri = "https://REDIRECTURI";
        mockSimplePost = mock(SimplePost.class);

        config.setValue(Config.AUTH_ENVIRONMENTS, "Testing");
        config.setOAuthEnvironmentString("Testing", Config.OAUTH_PARTIAL_SERVER, oauthServer);
        config.setOAuthEnvironmentString("Testing", Config.OAUTH_PARTIAL_CLIENTID, oauthClientId);
        config.setOAuthEnvironmentString("Testing", Config.OAUTH_PARTIAL_REDIRECTURI, oauthRedirectUri);
        config.setOAuthEnvironment("Testing");

        existingConstructor = SimplePostFactory.getConstructor();
        SimplePostFactory.setConstructor(c -> mockSimplePost);
    }

    @After
    public void testCleanup(){
        config.setValue(Config.AUTH_ENVIRONMENTS, existingOAuthEnvironments.toArray(new String[0]));
        config.setAuthEndpoint(existingEndPoint);
        SimplePostFactory.setConstructor(existingConstructor);
    }

    @Test
    public void testGetStartUrl(){
        try {
            config.setValue(Config.OAUTH_CLIENTID, "CLIENTID");
            String expected = "https://OAUTH_PARTIAL_SERVER/services/oauth2/authorize"
                    + "?response_type=code"
                    + "&display=popup"
                    + "&" + config.getClientIdNameValuePair()
                    + "&" + "redirect_uri=" + URLEncoder.encode("https://REDIRECTURI",  StandardCharsets.UTF_8.name());
            String actual = OAuthSecretFlowUtil.getStartUrlImpl(config);

            Assert.assertEquals( "OAuth Token Flow returned the wrong url", expected, actual);
        } catch (UnsupportedEncodingException e) {
            Assert.fail("could not get start url" + e.toString());
        }
    }

    @Test
    public void testInvalidInitialReponseUrl(){
        try {
            String expected = null;
            String actual = OAuthSecretFlowUtil.handleInitialUrl( "https://OAUTH_PARTIAL_SERVER/services/oauth2/authorize?doit=1");
            Assert.assertEquals("OAuthToken should not have handled this", expected, actual);

        } catch (URISyntaxException e) {
            Assert.fail("Could not handle the url:" + e.toString());
        }
    }

    @Test
    public void testValidInitialResponseUrl(){
        try {
            String expected = "TOKEN";
            String actual = OAuthSecretFlowUtil.handleInitialUrl( "https://OAUTH_PARTIAL_SERVER/services/oauth2/authorize?code=TOKEN&instance_url=https://INSTANCEURL");
            Assert.assertEquals("OAuthToken should not have handled this", expected, actual);

        } catch (URISyntaxException e) {
            Assert.fail("Could not handle the url:" + e.toString());
        }
    }

    @Test
    public void testValidSecondResponseAccessToken(){
        try {

            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();
            OAuthToken token = new OAuthToken();
            token.setAccessToken("ACCESS");
            token.setInstanceUrl("https://INSTANCEURL");
            String jsonToken = gson.toJson(token);
            InputStream input = new ByteArrayInputStream(jsonToken.getBytes(StandardCharsets.UTF_8));
            when(mockSimplePost.getInput()).thenAnswer(i -> input);
            when(mockSimplePost.isSuccessful()).thenReturn(true);

            @SuppressWarnings("unused")
            SimplePost simplePost = OAuthSecretFlowUtil.handleSecondPost("simplePost", config);

            String expected = "ACCESS";
            String actual = config.getString(Config.OAUTH_ACCESSTOKEN);
            when(mockSimplePost.isSuccessful()).thenReturn(true);

            Assert.assertEquals("Access token was not set", expected, actual);

        } catch (ParameterLoadException | IOException e) {
            Assert.fail("Could not handle second request:" + e.toString());
        }
    }

    @Test
    public void testValidSecondResponseRefreshToken(){
        try {

            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();
            OAuthToken token = new OAuthToken();
            token.setRefreshToken("REFRESHTOKEN");
            token.setInstanceUrl("https://INSTANCEURL");
            String jsonToken = gson.toJson(token);
            InputStream input = new ByteArrayInputStream(jsonToken.getBytes(StandardCharsets.UTF_8));
            when(mockSimplePost.getInput()).thenAnswer(i -> input);
            when(mockSimplePost.isSuccessful()).thenReturn(true);

            @SuppressWarnings("unused")
            SimplePost simplePost = OAuthSecretFlowUtil.handleSecondPost("simplePost", config);

            String expected = "REFRESHTOKEN";
            String actual = config.getString(Config.OAUTH_REFRESHTOKEN);

            Assert.assertEquals("Access token was not set", expected, actual);

        } catch (ParameterLoadException | IOException e) {
            Assert.fail("Could not handle second request:" + e.toString());
        }
    }

    @Test
    public void testValidSecondResponseInstanceUrl(){
        try {

            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();
            OAuthToken token = new OAuthToken();
            token.setInstanceUrl("https://INSTANCEURL");
            String jsonToken = gson.toJson(token);
            InputStream input = new ByteArrayInputStream(jsonToken.getBytes(StandardCharsets.UTF_8));
            when(mockSimplePost.getInput()).thenAnswer(i -> input);
            when(mockSimplePost.isSuccessful()).thenReturn(true);

            @SuppressWarnings("unused")
            SimplePost simplePost = OAuthSecretFlowUtil.handleSecondPost("simplePost", config);

            String expected = "https://INSTANCEURL";
            String actual = config.getString(Config.OAUTH_INSTANCE_URL);;

            Assert.assertEquals("Access token was not set", expected, actual);

        } catch (ParameterLoadException | IOException e) {
            Assert.fail("Could not handle second request:" + e.toString());
        }
    }
}
