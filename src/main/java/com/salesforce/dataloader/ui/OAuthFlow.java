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


import com.salesforce.dataloader.config.Config;

import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuthFlow is basic instrumentation of delegating authentication to an external web browser using OAuth2
 * Currently derived classes include authorization_code flow and token flow. authorization_flow would be setup
 * by individuals using connected apps as it requires storing a secret which cannot be done for the normal login
 */
public abstract class OAuthFlow extends Dialog {
    protected static Logger logger = LogManager.getLogger(OAuthFlow.class);
    protected final Config config;
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
        Shell shell = new Shell(getParent(), SWT.RESIZE | SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.FILL);
        GridLayout layout = new GridLayout(1, false);
        shell.setLayout(layout);

        // Create the web browser
        Browser browser = new Browser(shell, SWT.NONE);
        GridData data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        data.widthHint = 400;
        data.heightHint = 600;
        browser.setLayoutData(data);

        OAuthBrowserListener listener = getOAuthBrowserListener(shell, browser, config);
        browser.addProgressListener(listener);
        browser.setUrl(getStartUrl(config));

        shell.pack();
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        reasonPhrase = listener.getReasonPhrase();
        statusCode = listener.getStatusCode();


        return listener.getResult();
    }

    protected abstract OAuthBrowserListener getOAuthBrowserListener(Shell shell, Browser browser, Config config);

    public abstract String getStartUrl(Config config) throws UnsupportedEncodingException;

    public static Map<String, String> getQueryParameters(String url) throws URISyntaxException {
        url = url.replace("#","?");
        Map<String, String> params = new HashMap<>();
        new URIBuilder(url).getQueryParams().stream().forEach(kvp -> params.put(kvp.getName(), kvp.getValue()));
        return params;
    }
}
