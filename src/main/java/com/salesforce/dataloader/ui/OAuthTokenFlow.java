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
import com.salesforce.dataloader.oauth.OAuthFlowUtil;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.widgets.Shell;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;


/**
 * the oauth token flow. this is normally used for client to server where the client is not a secured environment
 * as it does not involve a secret. We use this as our standard login for SF oauth. The disadvantage to this flow is
 * it prompts for authentication and authorization everytime.
 */
public class OAuthTokenFlow extends OAuthFlow {
    public OAuthTokenFlow(Shell parent, Config config) {
        super(parent, config);
    }

    @Override
    protected OAuthBrowserListener getOAuthBrowserListener(Shell shell, Browser browser, Config config) {
        return new OAuthTokenBrowserLister(shell, browser, config);
    }

    @Override
    public String getStartUrl(Config config) throws UnsupportedEncodingException {
        return OAuthFlowUtil.getStartUrlImpl(config);
    }

    public static class OAuthTokenBrowserLister extends OAuthBrowserListener {
        public OAuthTokenBrowserLister(Shell shell, Browser browser, Config config) {
            super(browser, shell, config);
        }

        @Override
        public void changed(ProgressEvent progressEvent) {

        }

        @Override
        public void completed(ProgressEvent progressEvent) {
            super.completed(progressEvent);
            try {
                boolean handled = OAuthFlowUtil.handleCompletedUrl(browser.getUrl(), config);
                if (handled) {
                    setResult(true);
                    shell.close();
                    shell.dispose();
                }
            } catch (URISyntaxException e) {
                doSimpleErrorHandling(browser.getUrl(), e, logger);
            }
        }
    }
}
