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

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.sforce.ws.tools.VersionInfo;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

/**
 * simplied http client transport for posts (used for oauth)
 */
public class DefaultSimplePost implements SimplePost {

    public static final int PROXY_AUTHENTICATION_REQUIRED = 407;

    private boolean successful;
    private Config config;
    private String endpoint;
    private BasicNameValuePair[] pairs;
    private InputStream input;
    private int statusCode;
    private String reasonPhrase;

    DefaultSimplePost(Config config, String endpoint, BasicNameValuePair... pairs) {
        this.config = config;
        this.endpoint = endpoint;
        this.pairs = pairs;
    }

    @Override
    public void post() throws IOException, ParameterLoadException {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        HttpPost post = new HttpPost(endpoint);
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(Arrays.asList(pairs));

        int proxyPort = config.getInt(Config.PROXY_PORT);
        String proxyHostName = config.getString(Config.PROXY_HOST);
        String proxyUser = config.getString(Config.PROXY_USERNAME);
        String proxyPassword = config.getString(Config.PROXY_PASSWORD);
        String ntlmDomain = config.getString(Config.PROXY_NTLM_DOMAIN);
        proxyHostName = proxyHostName != null ? proxyHostName.trim() : "";
        proxyUser = proxyUser != null ? proxyUser.trim() : "";
        proxyPassword = proxyPassword != null ? proxyPassword.trim() : "";
        ntlmDomain = ntlmDomain != null ? ntlmDomain.trim() : "";

        post.addHeader("User-Agent", VersionInfo.info());
        post.setEntity(entity);

        //proxy
        if (proxyHostName.length() > 0) {
            InetSocketAddress proxyAddress = new InetSocketAddress(proxyHostName, proxyPort);
            HttpHost proxyHost = new HttpHost(proxyAddress.getHostName(), proxyAddress.getPort(), "http");
            AuthScope scope = new AuthScope(proxyAddress.getHostName(), proxyAddress.getPort(), null, null);
            Credentials credentials = new UsernamePasswordCredentials(proxyUser, proxyPassword);

            if (ntlmDomain.length() > 0) {
                credentials = new NTCredentials(proxyUser, proxyPassword, InetAddress.getLocalHost().getCanonicalHostName(), ntlmDomain);
            }

            RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
            requestConfigBuilder.setProxy(proxyHost);
            post.setConfig(requestConfigBuilder.build());

            CredentialsProvider credentialsprovider = new BasicCredentialsProvider();
            credentialsprovider.setCredentials(scope, credentials);
            httpClientBuilder.setDefaultCredentialsProvider(credentialsprovider).build();
        }

        try (CloseableHttpClient httpClient = httpClientBuilder.build()) {

            if (ntlmDomain.length() > 0) {
                // need to send a HEAD request to trigger NTLM authentication
                HttpHead head = new HttpHead("http://salesforce.com");
                try (CloseableHttpResponse ignored = httpClient.execute(head)) {
                }
            }
            try (CloseableHttpResponse response = httpClient.execute(post)) {

                successful = response.getStatusLine().getStatusCode() < 400;
                statusCode = response.getStatusLine().getStatusCode();
                reasonPhrase = response.getStatusLine().getReasonPhrase();

                // copy input stream data into a new input stream because releasing the connection will close the input stream
                ByteArrayOutputStream bOut = new ByteArrayOutputStream();
                try (InputStream inStream = response.getEntity().getContent()) {
                    IOUtils.copy(inStream, bOut);
                    input = new ByteArrayInputStream(bOut.toByteArray());
                    if (response.containsHeader("Content-Encoding") && response.getHeaders("Content-Encoding")[0].getValue().equals("gzip")) {
                        input = new GZIPInputStream(input);
                    }
                }

            }
        }
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
}
