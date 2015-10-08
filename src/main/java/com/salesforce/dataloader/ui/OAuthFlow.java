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
package com.salesforce.dataloader.ui;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.salesforce.dataloader.client.SimplePost;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.model.OAuthToken;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Optional;

/**
 * the web dialog for hosting oauth flows
 */
public class OAuthFlow extends Dialog {
    private final Config config;
    private static Logger logger = Logger.getLogger(OAuthFlow.class);
    private String reasonPhrase;
    private int statusCode;

    public OAuthFlow(Shell parent, Config config) {
        super(parent);
        this.config = config;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean open() throws UnsupportedEncodingException {
        // Create the dialog window
        Display display = getParent().getDisplay();
        Shell shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.FILL);
        Grid12 grid = new Grid12(shell, 30, 600);

        // Create the web browser
        Browser browser = new Browser(shell, SWT.NONE);
        browser.setLayoutData(grid.createCell(12));

        OAuthBrowserListener listener = new OAuthBrowserListener(browser, shell);
        browser.addProgressListener(listener);
        browser.setUrl(config.getString(Config.OAUTH_SERVER) +
                "/services/oauth2/authorize?response_type=code&display=popup&client_id=" +
                config.getString(Config.OAUTH_CLIENTID) + "&redirect_uri=" +
                URLEncoder.encode(config.getString(Config.OAUTH_REDIRECTURI), "UTF-8"));

        shell.pack();
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        return listener.getResult();
    }

    private class OAuthBrowserListener implements ProgressListener {
        private final Browser browser;
        private final Shell shell;
        private boolean result;

        public OAuthBrowserListener(Browser browser, Shell shell) {
            this.browser = browser;
            this.shell = shell;
        }

        @Override
        public void changed(ProgressEvent progressEvent) {

        }

        @Override
        public void completed(ProgressEvent progressEvent) {
            String url = browser.getUrl();
            try {
                Optional<NameValuePair> codeParam = new URIBuilder(url).getQueryParams().stream()
                        .filter(q -> q.getName().toLowerCase().equals("code")).findFirst();
                if (!codeParam.isPresent()){
                    return;
                }

                String code = codeParam.get().getValue();
                String server = config.getString(Config.OAUTH_SERVER) + "/services/oauth2/token";
                SimplePost client = new SimplePost(config, server,
                        new BasicNameValuePair("grant_type", "authorization_code"),
                        new BasicNameValuePair("code", code),
                        new BasicNameValuePair("client_id", config.getString(Config.OAUTH_CLIENTID)),
                        new BasicNameValuePair("client_secret",  config.getString(Config.OAUTH_CLIENTSECRET)),
                        new BasicNameValuePair("redirect_uri",  config.getString(Config.OAUTH_REDIRECTURI))
                );
                client.post();

                reasonPhrase = client.getReasonPhrase();
                statusCode = client.getStatusCode();

                if (client.isSuccessful()) {

                    StringBuilder builder = new StringBuilder();
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInput(), "UTF-8"));
                    for (int c = in.read(); c != -1; c = in.read()) {
                        builder.append((char) c);
                    }

                    String jsonTokenResult = builder.toString();
                    Gson gson = new GsonBuilder()
                            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                            .create();
                    OAuthToken token = gson.fromJson(jsonTokenResult, OAuthToken.class);
                    config.setValue(Config.OAUTH_ACCESSTOKEN, token.getAccessToken());
                    config.setValue(Config.OAUTH_REFRESHTOKEN, token.getRefreshToken());
                    result = true;
                }

                shell.close();
                shell.dispose();
            } catch (URISyntaxException | ParameterLoadException | IOException e) {
                logger.error("Failed to retrieve oauth token.", e);
            }
        }

        public boolean getResult() {
            return result;
        }
    }
}
