/*
 * Copyright (c) 2012, salesforce.com, inc.
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

import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.*;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.tools.VersionInfo;
import com.sforce.ws.transport.*;

/**
 * 
 * This class implements the Transport interface for WSC with HttpClient in order to properly 
 * work with NTLM proxies.  The existing JdkHttpTransport in WSC does not work with NTLM 
 * proxies when compiled on Java 1.6
 *
 * @author Jeff Lai
 * @since 25.0.2
 */
public class HttpClientTransport implements Transport {
    
    private ConnectorConfig config;
    private boolean successful;
    private HttpPost post;
    private OutputStream output;
    private ByteArrayOutputStream entityByteOut;
    
    public HttpClientTransport() {
    }
    
    public HttpClientTransport(ConnectorConfig config) {
        setConfig(config);
    }

    @Override
    public void setConfig(ConnectorConfig config) {
        this.config = config;
    }

    @Override
    public OutputStream connect(String url, String soapAction) throws IOException {
        if (soapAction == null) {
            soapAction = "";
        }

        HashMap<String, String> header = new HashMap<String, String>();

        header.put("SOAPAction", "\"" + soapAction + "\"");
        header.put("Content-Type", "text/xml; charset=UTF-8");
        header.put("Accept", "text/xml");
        
        return connect(url, header);
    }

    @Override
    public InputStream getContent() throws IOException {
        DefaultHttpClient client = new DefaultHttpClient();
        
        if (config.getProxyUsername() != null && !config.getUsername().equals("")) {
            String proxyPassword = config.getProxyPassword() == null ? "" : config.getProxyPassword();
            
            Credentials credentials;
            
            if (config.getNtlmDomain() != null && !config.getNtlmDomain().equals("")) {
                String computerName = InetAddress.getLocalHost().getCanonicalHostName();
                credentials = new NTCredentials(config.getProxyUsername(), proxyPassword, computerName, config.getNtlmDomain());
            } else {
                credentials = new UsernamePasswordCredentials(config.getProxyUsername(), proxyPassword);
            }
            
            client.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
        }
        
        InputStream input = null;
        
        byte[] entityBytes = entityByteOut.toByteArray();
        HttpEntity entity = new ByteArrayEntity(entityBytes);
        post.setEntity(entity);
        
        try {
            HttpResponse response = client.execute(post);
            
            if (response.getStatusLine().getStatusCode() > 399) {
                successful = false;
            } else {
                successful = true;
            }
            
            // copy input stream data into a new input stream because releasing the connection will close the input stream
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            IOUtils.copy(response.getEntity().getContent(), bOut);
            input = new ByteArrayInputStream(bOut.toByteArray());

            if (response.containsHeader("Content-Encoding") && response.getHeaders("Content-Encoding")[0].getValue().equals("gzip")) {
                input = new GZIPInputStream(input);
            }
   
        } finally {
            post.releaseConnection();
        }
        
        return input;
    }

    @Override
    public boolean isSuccessful() {
        return successful;
    }

    @Override
    public OutputStream connect(String endpoint, HashMap<String, String> httpHeaders) throws IOException {
        return connect(endpoint, httpHeaders, true);
    }

    @Override
    public OutputStream connect(String endpoint, HashMap<String, String> httpHeaders, boolean enableCompression) throws IOException {
        post = new HttpPost(endpoint);
        
        for (String name : httpHeaders.keySet()) {
            post.addHeader(name, httpHeaders.get(name));
        }
        
        post.addHeader("User-Agent", VersionInfo.info());
        
        if (enableCompression) {
            post.addHeader("Content-Encoding", "gzip");
            post.addHeader("Accept-Encoding", "gzip");
        }

        entityByteOut = new ByteArrayOutputStream();
        output = entityByteOut;
        
        if (config.getMaxRequestSize() > 0) {
            output = new LimitingOutputStream(config.getMaxRequestSize(), output);
        }

        if (enableCompression && config.isCompression()) {
            output = new GZIPOutputStream(output);
        }

        if (config.isTraceMessage()) {
            output = config.teeOutputStream(output);
        }

        if (config.hasMessageHandlers()) {
            URL url = new URL(endpoint);
            output = new MessageHandlerOutputStream(config, url, output);
        }

        return output;
    }

}
