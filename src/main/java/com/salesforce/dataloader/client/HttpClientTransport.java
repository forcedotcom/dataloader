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

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;

import com.salesforce.dataloader.exception.HttpClientTransportException;
import com.salesforce.dataloader.util.AppUtil;
import com.sforce.async.AsyncApiException;
import com.sforce.ws.ConnectorConfig;

import com.sforce.ws.tools.VersionInfo;
import com.sforce.ws.transport.*;

import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class implements the Transport interface for WSC with HttpClient in order to properly work
 * with NTLM proxies.  The existing JdkHttpTransport in WSC does not work with NTLM proxies when
 * compiled on Java 1.6
 *
 * @author Jeff Lai
 * @since 25.0.2
 */
public class HttpClientTransport implements HttpTransportInterface {

    
    private static final String AUTH_HEADER_VALUE_PREFIX = "Bearer ";
    private static final String AUTH_HEADER_FOR_JSON = "Authorization";
    private static final String AUTH_HEADER_FOR_XML = "X-SFDC-Session";
    private static final String USER_AGENT_HEADER = "User-Agent";

    private static ConnectorConfig currentConfig = null;
    private boolean successful;
    private HttpRequestBase httpMethod = null;
    private OutputStream output;
    private ByteArrayOutputStream entityByteOut;
    private static CloseableHttpClient currentHttpClient = null;
    private static boolean reuseConnection = true;
    private static long serverInvocationCount = 0;
    private static Logger logger = LogManager.getLogger(HttpClientTransport.class);
    private HttpResponse httpResponse;
    
    public HttpClientTransport() {
    }

    public HttpClientTransport(ConnectorConfig newConfig) {
        setConfig(newConfig);
    }

    @Override
    public synchronized void setConfig(ConnectorConfig newConfig) {
        if (!areEquivalentConfigs(currentConfig, newConfig) && currentHttpClient != null) {
            try {
                currentHttpClient.close();
            } catch (IOException ex) {
                // do nothing
            }
            currentHttpClient = null;
        }
        currentConfig = newConfig;
    }
    
    @Override
    public OutputStream connect(String url, String soapAction) throws IOException {
        if (soapAction == null) {
            soapAction = "";
        }

        HashMap<String, String> header = new HashMap<String, String>();

        header.put("SOAPAction", "\"" + soapAction + "\"");
        header.put("Content-Type", "text/xml; charset=" + StandardCharsets.UTF_8.name());
        header.put("Accept", "text/xml");

        return connect(url, header);
    }
        
    private boolean areEquivalentConfigs(ConnectorConfig config1, ConnectorConfig config2) {
        if (config1 == null && config2 == null) {
            return true;
        } else if (config1 == null || config2 == null) {
            // one of the configs is null, other isn't. They can't be equal.
            return false;
        } else if (!config1.equals(config2)) {
            return false;
        }
        return true;
    }
    
    private boolean isHttpClientInitialized = false;
    
    private void setAuthAndClientHeadersForHttpMethod() {
        if (this.httpMethod != null 
                && currentConfig.getSessionId() != null 
                && !currentConfig.getSessionId().isBlank()) {
            this.httpMethod.removeHeaders(AUTH_HEADER_FOR_JSON);
            this.httpMethod.removeHeaders(AUTH_HEADER_FOR_XML);
            this.httpMethod.removeHeaders(USER_AGENT_HEADER);
            this.httpMethod.addHeader(AUTH_HEADER_FOR_JSON, AUTH_HEADER_VALUE_PREFIX + currentConfig.getSessionId());
            this.httpMethod.addHeader(AUTH_HEADER_FOR_XML, AUTH_HEADER_VALUE_PREFIX + currentConfig.getSessionId());
            this.httpMethod.addHeader(USER_AGENT_HEADER, VersionInfo.info());
        }
    }
    
    private synchronized void initializeHttpClient() throws UnknownHostException {
        if (isHttpClientInitialized) {
            // already initialized.
            return;
        }
        closeConnections();
        httpMethod = null;
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().useSystemProperties();
        
        if (currentConfig != null
                && currentConfig.getProxy() != null
                && currentConfig.getProxy().address() != null) {
            String proxyUser = currentConfig.getProxyUsername() == null ? "" : currentConfig.getProxyUsername();
            String proxyPassword = currentConfig.getProxyPassword() == null ? "" : currentConfig.getProxyPassword();

            InetSocketAddress proxyAddress = (InetSocketAddress) currentConfig.getProxy().address();
            HttpHost proxyHost = new HttpHost(proxyAddress.getHostName(), proxyAddress.getPort(), "http");
            httpClientBuilder.setProxy(proxyHost);

            CredentialsProvider credentialsprovider = new BasicCredentialsProvider();
            AuthScope scope = new AuthScope(proxyAddress.getHostName(), proxyAddress.getPort(), null, null);

            Credentials credentials;
            if (AppUtil.getOSType() == AppUtil.OSType.WINDOWS) {
                String computerName = InetAddress.getLocalHost().getCanonicalHostName();
                credentials = new NTCredentials(proxyUser, proxyPassword, computerName, currentConfig.getNtlmDomain());
            } else {
                credentials = new UsernamePasswordCredentials(proxyUser, proxyPassword);
            }
            // based on answer at https://stackoverflow.com/questions/6962047/apache-httpclient-4-1-proxy-authentication
            credentialsprovider.setCredentials(scope, credentials);
            httpClientBuilder.setDefaultCredentialsProvider(credentialsprovider);
            httpClientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
            currentHttpClient = httpClientBuilder.build();
            if (AppUtil.getOSType() == AppUtil.OSType.WINDOWS) {
                /*
                try (CloseableHttpResponse ignored = currentHttpClient.execute(new HttpHead(AppUtil.DATALOADER_DOWNLOAD_URL))) {
                    logger.debug("able to ping salesforce service");
                } catch (Exception e) {
                   logger.info("Unable to use NTCredentials for proxy. Switching to UsernamePasswordCredentials");
                   credentials = new UsernamePasswordCredentials(proxyUser, proxyPassword);
                   credentialsprovider.setCredentials(scope, credentials);
                }
                */
            }
        }
        if (currentHttpClient == null) {
            currentHttpClient = httpClientBuilder.build();
        }
        isHttpClientInitialized = true;
    }
    
    @Override
    public synchronized InputStream getContent() throws IOException {
        serverInvocationCount++;
        initializeHttpClient();
        if (this.httpMethod instanceof HttpEntityEnclosingRequestBase
            && ((HttpEntityEnclosingRequestBase)this.httpMethod).getEntity() == null) {
            byte[] entityBytes = entityByteOut.toByteArray();
            HttpEntity entity = new ByteArrayEntity(entityBytes);
            currentConfig.setUseChunkedPost(false);
            ((HttpEntityEnclosingRequestBase)this.httpMethod).setEntity(entity);
        }
        InputStream input = new ByteArrayInputStream(new byte[1]);
        try {
            HttpClientContext context = HttpClientContext.create();
            RequestConfig config = RequestConfig.custom().setExpectContinueEnabled(currentConfig.useChunkedPost()).build();
            context.setRequestConfig(config);
    
            if (currentConfig.getNtlmDomain() != null && !currentConfig.getNtlmDomain().equals("")) {
                // need to send a HEAD request to trigger NTLM authentication
                try (CloseableHttpResponse ignored = currentHttpClient.execute(new HttpHead("http://salesforce.com"))) {
                } catch (Exception ex) {
                    logger.error(ex.getMessage());
                    throw ex;
                }
            }
    
            try (CloseableHttpResponse response = currentHttpClient.execute(this.httpMethod, context)) {
                successful = true;
                httpResponse = response;
                if (response.getStatusLine().getStatusCode() > 399) {
                    successful = false;
                    if (response.getStatusLine().getStatusCode() == 407) {
                        throw new RuntimeException(response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
                    }
                }
                // copy input stream data into a new input stream because releasing the connection will close the input stream
                ByteArrayOutputStream bOut = new ByteArrayOutputStream();
                if (response.getEntity() != null) {
                    try (InputStream inStream = response.getEntity().getContent()) {
                        IOUtils.copy(inStream, bOut);
                        input = new ByteArrayInputStream(bOut.toByteArray());
                        if (response.containsHeader("Content-Encoding") && response.getHeaders("Content-Encoding")[0].getValue().equals("gzip")) {
                            input = new GZIPInputStream(input);
                        }
                    }
                }
            }
        } finally {
            if (isReuseConnection()) {
                closeConnections();
            }
        }
        return input;
    }

    public HttpResponse getHttpResponse() {
        return this.httpResponse;
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
        return connect(endpoint, httpHeaders, enableCompression, SupportedHttpMethodType.POST);
    }
    
    @Override
    public OutputStream connect(String endpoint, HashMap<String, String> httpHeaders, boolean enableCompression,
            SupportedHttpMethodType httpMethod) throws IOException {
        return doConnect(endpoint, httpHeaders, enableCompression, httpMethod, null, null);
    }

    @Override
    public void connect(String endpoint, HashMap<String, String> httpHeaders, boolean enableCompression,
            SupportedHttpMethodType httpMethod, InputStream contentInputStream, String contentEncoding)
            throws IOException {
        doConnect(endpoint, httpHeaders, enableCompression, httpMethod, contentInputStream, contentEncoding);
    }

    public static long getServerInvocationCount() {
        return serverInvocationCount;
    }
    
    public static void resetServerInvocationCount() {
        serverInvocationCount = 0;
    }
    
    private OutputStream doConnect(String endpoint,
                                    HashMap<String, String> httpHeaders, 
                                    boolean enableCompression,
                                    SupportedHttpMethodType httpMethodType,
                                    InputStream requestInputStream, 
                                    String contentTypeStr) throws IOException {
        initializeHttpClient();
        switch (httpMethodType) {
            case GET :
                this.httpMethod = new HttpGet(endpoint);
                break;
            case PATCH :
                this.httpMethod = new HttpPatch(endpoint);
                break;
            case PUT :
                this.httpMethod = new HttpPut(endpoint);
                break;
            case DELETE :
                this.httpMethod = new HttpDelete(endpoint);
                break;
            default:
                this.httpMethod = new HttpPost(endpoint);
        }
        if (httpHeaders != null) {
            for (String name : httpHeaders.keySet()) {
                this.httpMethod.addHeader(name, httpHeaders.get(name));
            }
        }
        setAuthAndClientHeadersForHttpMethod();
        if (requestInputStream != null) {
            ContentType contentType = ContentType.DEFAULT_TEXT;
            if (contentTypeStr != null) {
                contentType = ContentType.create(contentTypeStr);
            }
            BufferedHttpEntity entity = new BufferedHttpEntity(new InputStreamEntity(requestInputStream, contentType));
            currentConfig.setUseChunkedPost(true);
            if (this.httpMethod instanceof HttpEntityEnclosingRequestBase) {
                ((HttpEntityEnclosingRequestBase)this.httpMethod).setEntity(entity);
            }
            return null;
        }

        if (enableCompression && currentConfig.isCompression()) {
            this.httpMethod.addHeader("Content-Encoding", "gzip");
            this.httpMethod.addHeader("Accept-Encoding", "gzip");
        }

        entityByteOut = new ByteArrayOutputStream();
        output = entityByteOut;

        if (currentConfig.getMaxRequestSize() > 0) {
            output = new LimitingOutputStream(currentConfig.getMaxRequestSize(), output);
        }

        if (enableCompression && currentConfig.isCompression()) {
            output = new GZIPOutputStream(output);
        }

        if (currentConfig.isTraceMessage()) {
            output = currentConfig.teeOutputStream(output);
        }

        if (currentConfig.hasMessageHandlers()) {
            URL url = new URL(endpoint);
            output = new MessageHandlerOutputStream(currentConfig, url, output);
        }

        return output;
    }
    
    public static void closeConnections() {
        if (currentHttpClient != null) {
            try {
                currentHttpClient.close();
            } catch (IOException ex) {
                // do nothing
            }
            currentHttpClient = null;
        }
    }
    
    public static void setReuseConnection(boolean reuse) {
        reuseConnection = reuse;
    }
    
    public static boolean isReuseConnection() {
        return reuseConnection;
    }
    
    public InputStream httpGet(String urlStr) throws IOException, AsyncApiException, HttpClientTransportException {
        InputStream in = null;
        initializeHttpClient();
        connect(urlStr, null, false, SupportedHttpMethodType.GET, null, null);
        in = getContent();
        return in;
    }
}