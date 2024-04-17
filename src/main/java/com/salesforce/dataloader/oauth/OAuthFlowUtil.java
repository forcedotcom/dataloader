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

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.utils.URIBuilder;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.model.OAuthToken;

/*
 * Utility class that handles communication with OAuth service.
 * It decouples OAuth UI related classes from OAuth protocol handling.
 */
public class OAuthFlowUtil {
    public static String getStartUrlImpl(Config config) throws UnsupportedEncodingException {
        return config.getString(Config.OAUTH_SERVER) +
                "/services/oauth2/authorize?response_type=token&display=popup&client_id=" +
                config.getString(Config.OAUTH_CLIENTID) + "&redirect_uri=" +
                URLEncoder.encode(config.getString(Config.OAUTH_REDIRECTURI), StandardCharsets.UTF_8.name());
    }
    

    public static Map<String, String> getQueryParameters(String url) throws URISyntaxException {
        url = url.replace("#","?");
        Map<String, String> params = new HashMap<>();
        new URIBuilder(url).getQueryParams().stream().forEach(kvp -> params.put(kvp.getName(), kvp.getValue()));
        return params;
    }
    
    public static boolean handleCompletedUrl(String url, Config config) throws URISyntaxException {
        Map<String, String> params = getQueryParameters(url);

        if (params.containsKey("access_token")){
            //we don't use most of this but I still like to track what we should get
            OAuthToken token = new OAuthToken();
            token.setInstanceUrl(params.get("instance_url"));
            token.setId(params.get("id"));
            token.setAccessToken(params.get("access_token"));

            //optional parameters
            if (params.containsKey("refresh_token")) {
                token.setRefreshToken(params.get("refresh_token"));
            }

            //currently unused parameters
            if (params.containsKey("scope")) {
                token.setScope(params.get("scope"));
            }
            if (params.containsKey("signature")) {
                token.setSignature(params.get("signature"));
            }
            if (params.containsKey("token_type")) {
                token.setTokenType(params.get("token_type"));
            }
            if (params.containsKey("issued_at")) {
                String issued_at = params.get("issued_at");
                if (issued_at != null && !issued_at.equals("")) {
                    token.setIssuedAt(Long.valueOf(issued_at));
                }
            }


            config.setValue(Config.OAUTH_ACCESSTOKEN, token.getAccessToken());
            config.setValue(Config.OAUTH_REFRESHTOKEN, token.getRefreshToken());
            config.setValue(Config.ENDPOINT, token.getInstanceUrl());

            return true;
        }

        return false;
    }
}
