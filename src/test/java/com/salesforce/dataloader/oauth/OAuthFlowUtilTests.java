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
import com.salesforce.dataloader.config.Config;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * test the oauth token flow as best we can. sadly there is SWT here which isn't very testable for unit tests
 */
public class OAuthFlowUtilTests extends ConfigTestBase {

    private Config config;
    private ArrayList<String> existingOAuthEnvironments;
    private String oauthServer;
    private String oauthClientId;
    private String oauthRedirectUri;
    private String existingEndPoint;

    @Before
    public void testSetup(){
        config = getController().getConfig();
        existingOAuthEnvironments = config.getStrings(Config.AUTH_ENVIRONMENTS);
        existingEndPoint = config.getAuthEndpoint();
        oauthServer = "http://OAUTH_PARTIAL_SERVER";
        oauthClientId = "CLIENTID";
        oauthRedirectUri = "REDIRECTURI";

        config.setValue(Config.AUTH_ENVIRONMENTS, "Testing");
        config.setOAuthEnvironmentString("Testing", Config.OAUTH_PARTIAL_SERVER, oauthServer);
        config.setOAuthEnvironmentString("Testing", Config.OAUTH_PARTIAL_CLIENTID, oauthClientId);
        config.setOAuthEnvironmentString("Testing", Config.OAUTH_PARTIAL_REDIRECTURI, oauthRedirectUri);
        config.setOAuthEnvironment("Testing");
    }

    @After
    public void testCleanup(){
        config.setValue(Config.AUTH_ENVIRONMENTS, existingOAuthEnvironments.toArray(new String[0]));
        config.setAuthEndpoint(existingEndPoint);
    }

    @Test
    public void testGetStartUrl(){
        try {
            String expected = "http://OAUTH_PARTIAL_SERVER/services/oauth2/authorize?response_type=token&display=popup&client_id=CLIENTID&redirect_uri=REDIRECTURI";
            String actual = OAuthFlowUtil.getStartUrlImpl(config);

            Assert.assertEquals( "OAuth Token Flow returned the wrong url", expected, actual);
        } catch (UnsupportedEncodingException e) {
            Assert.fail("could not get start url" + e.toString());
        }
    }

    @Test
    public void testInvalidReponseUrl(){
        try {
            Boolean condition = OAuthFlowUtil.handleCompletedUrl( "http://OAUTH_PARTIAL_SERVER/services/oauth2/authorize?doit=1", config);
            Assert.assertFalse("OAuthToken should not have handled this", condition);

        } catch (URISyntaxException e) {
            Assert.fail("Could not handle the url:" + e.toString());
        }
    }

    @Test
    public void testValidResponseUrl(){
        try {
            Boolean condition = OAuthFlowUtil.handleCompletedUrl( "http://OAUTH_PARTIAL_SERVER/services/oauth2/authorize#access_token=TOKEN", config);
            Assert.assertTrue("OAuthToken should have handled this", condition);

        } catch (URISyntaxException e) {
            Assert.fail("Could not handle the url:" + e.toString());
        }
    }

    @Test
    public void testValidResponseUrlSetsAccessToken(){
        try {
            OAuthFlowUtil.handleCompletedUrl( "http://OAUTH_PARTIAL_SERVER/services/oauth2/authorize#access_token=TOKEN", config);
            String expected = "TOKEN";
            String actual = config.getString(Config.OAUTH_ACCESSTOKEN);

            Assert.assertEquals("Incorrect access token found in config", expected, actual);
        } catch (URISyntaxException e) {
            Assert.fail("Could not handle the url:" + e.toString());
        }
    }

    @Test
    public void testValidResponseUrlSetsRefreshToken(){
        try {
            OAuthFlowUtil.handleCompletedUrl( "http://OAUTH_PARTIAL_SERVER/services/oauth2/authorize#access_token=TOKEN&refresh_token=REFRESHTOKEN", config);
            String expected = "REFRESHTOKEN";
            String actual = config.getString(Config.OAUTH_REFRESHTOKEN);

            Assert.assertEquals("Incorrect refresh token found in config", expected, actual);
        } catch (URISyntaxException e) {
            Assert.fail("Could not handle the url:" + e.toString());
        }
    }

    @Test
    public void testValidResponseUrlSetsEndPoint(){
        try {
            OAuthFlowUtil.handleCompletedUrl( "http://OAUTH_PARTIAL_SERVER/services/oauth2/authorize#access_token=TOKEN&instance_url=INSTANCEURL", config);
            String expected = "INSTANCEURL";
            String actual = config.getString(Config.OAUTH_INSTANCE_URL);

            Assert.assertEquals("Incorrect refresh token found in config", expected, actual);
        } catch (URISyntaxException e) {
            Assert.fail("Could not handle the url:" + e.toString());
        }
    }
}
