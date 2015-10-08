package com.salesforce.dataloader.client;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.sforce.ws.tools.VersionInfo;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

/**
 * simplied http client transport for posts (used for oauth)
 */
public class SimplePost {

    public static final int PROXY_AUTHENTICATION_REQUIRED = 407;

    private boolean successful;
    private Config config;
    private String endpoint;
    private BasicNameValuePair[] pairs;
    private InputStream input;
    private int statusCode;
    private String reasonPhrase;

    public SimplePost(Config config, String endpoint, BasicNameValuePair... pairs) {
        this.config = config;
        this.endpoint = endpoint;
        this.pairs = pairs;
    }

    public void post() throws IOException, ParameterLoadException {
        DefaultHttpClient client = new DefaultHttpClient();
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
        if (proxyHostName.length()>0) {
            InetSocketAddress proxyAddress =  new InetSocketAddress(proxyHostName, proxyPort);
            HttpHost proxyHost = new HttpHost(proxyAddress.getHostName(), proxyAddress.getPort(), "http");
            AuthScope scope = new AuthScope(proxyAddress.getHostName(), proxyAddress.getPort(), null, null);
            Credentials credentials= new UsernamePasswordCredentials(proxyUser, proxyPassword);

            if (ntlmDomain.length() > 0) {
                credentials = new NTCredentials(proxyUser, proxyPassword, InetAddress.getLocalHost().getCanonicalHostName(), ntlmDomain);
            }

            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHost);
            client.getCredentialsProvider().setCredentials(scope, credentials);
        }

        try {
            if (ntlmDomain.length() > 0) {
                // need to send a HEAD request to trigger NTLM authentication
                HttpHead head = new HttpHead("http://salesforce.com");
                client.execute(head);
                head.releaseConnection();
            }
            HttpResponse response = client.execute(post);

            successful = response.getStatusLine().getStatusCode() < 400;
            statusCode = response.getStatusLine().getStatusCode();
            reasonPhrase = response.getStatusLine().getReasonPhrase();

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
    }

    public boolean isSuccessful() {
        return successful;
    }

    public InputStream getInput() {
        return input;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }
}
