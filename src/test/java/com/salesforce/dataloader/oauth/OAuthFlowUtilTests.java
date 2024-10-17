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

import com.salesforce.dataloader.ConfigTestBase;
import com.salesforce.dataloader.config.AppConfig;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * test the oauth token flow as best we can. sadly there is SWT here which isn't very testable for unit tests
 */
public class OAuthFlowUtilTests extends ConfigTestBase {

    private AppConfig appConfig;
    private ArrayList<String> existingOAuthEnvironments;
    private String oauthServer;
    private String oauthClientId;
    private String oauthRedirectUri;
    private String existingEndPoint;

    @Before
    public void testSetup(){
        appConfig = getController().getAppConfig();
        existingOAuthEnvironments = appConfig.getStrings(AppConfig.PROP_SERVER_ENVIRONMENTS);
        existingEndPoint = appConfig.getAuthEndpointForCurrentEnv();
        oauthServer = "https://OAUTH_PARTIAL_SERVER";
        oauthClientId = "CLIENTID";
        oauthRedirectUri = "REDIRECTURI";

        appConfig.setValue(AppConfig.PROP_SERVER_ENVIRONMENTS, "Testing");
        appConfig.setOAuthEnvironmentString("Testing", AppConfig.CLIENTID_LITERAL, oauthClientId);
        appConfig.setOAuthEnvironmentString("Testing", AppConfig.OAUTH_PARTIAL_REDIRECTURI, oauthRedirectUri);
        appConfig.setServerEnvironment("Testing");
    }

    @After
    public void testCleanup(){
        appConfig.setValue(AppConfig.PROP_SERVER_ENVIRONMENTS, existingOAuthEnvironments.toArray(new String[0]));
        appConfig.setAuthEndpointForCurrentEnv(existingEndPoint);
    }

    @Test
    public void testGetStartUrl(){
        try {
            String expected = appConfig.getAuthEndpointForCurrentEnv() + "/services/oauth2/authorize"
                    + "?response_type=token"
                    + "&display=popup"
                    + "&" + appConfig.getClientIdNameValuePair()
                    + "&redirect_uri=" 
                    + URLEncoder.encode(appConfig.getAuthEndpointForCurrentEnv()
                    + "services/oauth2/success", StandardCharsets.UTF_8.name());
            String actual = OAuthFlowUtil.getStartUrlImpl(appConfig);

            Assert.assertEquals( "OAuth Token Flow returned the wrong url", expected, actual);
        } catch (UnsupportedEncodingException e) {
            Assert.fail("could not get start url" + e.toString());
        }
    }

    @Test
    public void testInvalidReponseUrl(){
        try {
            Boolean condition = OAuthFlowUtil.handleCompletedUrl( "https://OAUTH_PARTIAL_SERVER/services/oauth2/authorize?doit=1", appConfig);
            Assert.assertFalse("OAuthToken should not have handled this", condition);

        } catch (URISyntaxException e) {
            Assert.fail("Could not handle the url:" + e.toString());
        }
    }

    @Test
    public void testValidResponseUrl(){
        try {
            Boolean condition = OAuthFlowUtil.handleCompletedUrl( "https://OAUTH_PARTIAL_SERVER/services/oauth2/authorize#access_token=TOKEN&instance_url=https://INSTANCEURL", appConfig);
            Assert.assertTrue("OAuthToken should have handled this", condition);

        } catch (URISyntaxException e) {
            Assert.fail("Could not handle the url:" + e.toString());
        }
    }

    @Test
    public void testValidResponseUrlSetsAccessToken(){
        try {
            OAuthFlowUtil.handleCompletedUrl( "https://OAUTH_PARTIAL_SERVER/services/oauth2/authorize#access_token=TOKEN&instance_url=https://INSTANCEURL", appConfig);
            String expected = "TOKEN";
            String actual = appConfig.getString(AppConfig.PROP_OAUTH_ACCESSTOKEN);

            Assert.assertEquals("Incorrect access token found in config", expected, actual);
        } catch (URISyntaxException e) {
            Assert.fail("Could not handle the url:" + e.toString());
        }
    }

    @Test
    public void testValidResponseUrlSetsRefreshToken(){
        try {
            OAuthFlowUtil.handleCompletedUrl( "https://OAUTH_PARTIAL_SERVER/services/oauth2/authorize#access_token=TOKEN&refresh_token=REFRESHTOKEN&instance_url=https://INSTANCEURL", appConfig);
            String expected = "REFRESHTOKEN";
            String actual = appConfig.getString(AppConfig.PROP_OAUTH_REFRESHTOKEN);

            Assert.assertEquals("Incorrect refresh token found in config", expected, actual);
        } catch (URISyntaxException e) {
            Assert.fail("Could not handle the url:" + e.toString());
        }
    }

    @Test
    public void testValidResponseUrlSetsEndPoint(){
        try {
            OAuthFlowUtil.handleCompletedUrl( "https://OAUTH_PARTIAL_SERVER/services/oauth2/authorize#access_token=TOKEN&instance_url=https://INSTANCEURL", appConfig);
            String expected = "https://INSTANCEURL";
            String actual = appConfig.getString(AppConfig.PROP_OAUTH_INSTANCE_URL);

            Assert.assertEquals("Incorrect refresh token found in config", expected, actual);
        } catch (URISyntaxException e) {
            Assert.fail("Could not handle the url:" + e.toString());
        }
    }
}
