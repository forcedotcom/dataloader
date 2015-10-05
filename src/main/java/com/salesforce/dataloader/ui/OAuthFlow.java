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
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.model.OAuthToken;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.List;

/**
 * the web dialog for hosting oauth flows
 */
public class OAuthFlow extends Dialog {
    private final Config config;

    public OAuthFlow(Shell parent, Config config) {
        super(parent);
        this.config = config;
    }


    public boolean open() throws UnsupportedEncodingException {
        // Create the dialog window
        Display display = getParent().getDisplay();
        final Shell shell = new Shell(getParent(), getStyle());

        shell.setLayout(new FormLayout());

        // Create the composite to hold the buttons and text field
        Composite controls = new Composite(shell, SWT.NONE);
        FormData data = new FormData();
        data.top = new FormAttachment(0, 0);
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        controls.setLayoutData(data);

        // Create the web browser
        final Browser browser = new Browser(shell, SWT.NONE);
        data = new FormData(800,600);
        data.top = new FormAttachment(controls);
        data.bottom = new FormAttachment(100, 0);
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        browser.setLayoutData(data);

        browser.addProgressListener(new OAuthBrowserListener(browser, shell));
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
        // Return the sucess
        return true;
    }

    private class OAuthBrowserListener implements ProgressListener {
        private final Browser browser;
        private final Shell shell;

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
                URIBuilder uri = new URIBuilder(url);
                List<NameValuePair> queryParams = uri.getQueryParams();
                for(NameValuePair queryParam: queryParams){
                    if (queryParam.getName().toLowerCase().equals("code")){
                        String code = queryParam.getValue();
                        URL fetchToken = new URL(config.getString(Config.OAUTH_SERVER) + "/services/oauth2/token");
                        HttpURLConnection urlConnection = (HttpURLConnection) fetchToken.openConnection();
                        urlConnection.setRequestMethod("POST");
                        String parameters = "code=" + URLEncoder.encode(code, "UTF-8") + "&grant_type=authorization_code&client_id=" + config.getString(Config.OAUTH_CLIENTID) + "&client_secret=" + config.getString(Config.OAUTH_CLIENTSECRET) + "&redirect_uri=" + URLEncoder.encode(config.getString(Config.OAUTH_REDIRECTURI), "UTF-8");
                        byte[] postData = parameters.getBytes(Charset.forName("UTF-8"));
                        urlConnection.setDoOutput( true );
                        urlConnection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
                        urlConnection.setRequestProperty( "charset", "utf-8");
                        urlConnection.setRequestProperty( "Content-Length", Integer.toString( postData.length ));
                        urlConnection.setUseCaches( false );
                        urlConnection.setInstanceFollowRedirects(true);
                        DataOutputStream wr = null;
                        try{
                            wr = new DataOutputStream( urlConnection.getOutputStream());
                            wr.write( postData );
                        }
                        finally{
                            if (wr != null){
                                wr.close();
                            }
                        }
                        StringBuilder builder = new StringBuilder();
                        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                        for ( int c = in.read(); c != -1; c = in.read() ) {
                            builder.append((char) c);
                        }

                        String jsonTokenResult = builder.toString();
                        Gson gson = new GsonBuilder()
                                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                .create();
                        OAuthToken token = gson.fromJson(jsonTokenResult, OAuthToken.class);
                        config.setValue(Config.OAUTH_ACCESSTOKEN, token.getAccessToken());
                        config.setValue(Config.OAUTH_REFRESHTOKEN, token.getRefreshToken());
                        shell.close();
                        shell.dispose();
                        break;
                    }
                }
        } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
