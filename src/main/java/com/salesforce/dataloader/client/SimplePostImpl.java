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

package com.salesforce.dataloader.client;

import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.util.AppUtil;
import com.sforce.ws.ConnectorConfig;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;

/**
 * simplied http client transport for posts (used for oauth)
 */
public class SimplePostImpl implements SimplePostInterface {

    private boolean successful;
    private AppConfig appConfig;
    private String endpoint;
    private BasicNameValuePair[] pairs;
    private InputStream input;
    private int statusCode;
    private String reasonPhrase;
    private CloseableHttpResponse response;

    SimplePostImpl(AppConfig appConfig, String endpoint, BasicNameValuePair... pairs) {
        this.appConfig = appConfig;
        this.endpoint = endpoint;
        this.pairs = pairs;
    }
    
    public void addBasicNameValuePair(BasicNameValuePair pair) {
        BasicNameValuePair[] newPairs = new BasicNameValuePair[pairs.length + 1];
        for (int i=0; i < pairs.length; i++) {
            newPairs[i] = pairs[i];
        }
        newPairs[pairs.length] = pair;
        pairs = newPairs;
    }

    @Override
    public void post() throws IOException, ParameterLoadException {
        ConnectorConfig connConfig = new ConnectorConfig();
        AppUtil.setConnectorConfigProxySettings(appConfig, connConfig);
        HttpTransportImpl clientTransport = HttpTransportImpl.getInstance();
        clientTransport.setConfig(connConfig);
        this.input = clientTransport.simplePost(endpoint, null, pairs);
        successful = clientTransport.isSuccessful();
    }

    @Override
    public boolean isSuccessful() {
        return successful;
    }

    @Override
    public InputStream getInput() {
        return input;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getReasonPhrase() {
        return reasonPhrase;
    }
    
    public Header[] getResponseHeaders(String headerName) {
        return response.getHeaders(headerName);
    }
}
