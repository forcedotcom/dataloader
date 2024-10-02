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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.http.message.BasicNameValuePair;

import com.salesforce.dataloader.client.SimplePost;
import com.salesforce.dataloader.client.SimplePostFactory;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.util.OAuthBrowserLoginRunner;

/*
 * Utility class that handles communication with OAuth service.
 * It decouples OAuth UI related classes from OAuth protocol handling.
 */
public class OAuthSecretFlowUtil {
    public static String getStartUrlImpl(Config config) throws UnsupportedEncodingException {
        return config.getString(Config.OAUTH_SERVER) +
                "/services/oauth2/authorize"
                + "?response_type=code"
                + "&display=popup"
                + "&" + config.getClientIdNameValuePair()
                + "&redirect_uri="
                + URLEncoder.encode(config.getString(Config.OAUTH_REDIRECTURI), StandardCharsets.UTF_8.name());
    }

    public static SimplePost handleSecondPost(String code, Config config) throws IOException, ParameterLoadException {
        String server = config.getString(Config.OAUTH_SERVER) + "/services/oauth2/token";
        SimplePost client = SimplePostFactory.getInstance(config, server,
                new BasicNameValuePair("grant_type", "authorization_code"),
                new BasicNameValuePair("code", code),
                new BasicNameValuePair(Config.CLIENT_ID_HEADER_NAME, config.getString(Config.OAUTH_CLIENTID)),
                new BasicNameValuePair("client_secret", config.getString(Config.OAUTH_CLIENTSECRET)),
                new BasicNameValuePair("redirect_uri", config.getString(Config.OAUTH_REDIRECTURI))
        );
        client.post();
        if (client.isSuccessful()) {
            OAuthBrowserLoginRunner.processSuccessfulLogin(client.getInput(), config);
        }
        return client;
    }

    public static String handleInitialUrl(String url) throws URISyntaxException {
        Map<String, String> queryParameters = OAuthFlowUtil.getQueryParameters(url);
        return queryParameters.get("code");
    }
}