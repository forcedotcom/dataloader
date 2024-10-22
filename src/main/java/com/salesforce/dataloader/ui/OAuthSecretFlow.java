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

import com.salesforce.dataloader.client.SimplePost;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.oauth.OAuthSecretFlowUtil;

import org.apache.logging.log4j.Logger;
import com.salesforce.dataloader.util.DLLogManager;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.widgets.Shell;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

/**
 * the oauth authorization_code. this is normally reserved for server to server communications as it involves storing
 * a secret. we provide it hear for users to create their own connected app for oauth. The advantage to this flow is
 * it prompts for authentication but not authorization (once it's been authorized at least once).
 */
public class OAuthSecretFlow extends OAuthFlow {
    protected static Logger logger = DLLogManager.getLogger(OAuthSecretFlow.class);

    public OAuthSecretFlow(Shell parent, AppConfig appConfig) {
        super(parent, appConfig);
    }

    @Override
    protected OAuthBrowserListener getOAuthBrowserListener(Shell shell, Browser browser, AppConfig appConfig) {
        return new OAuthSecretBrowserListener(browser, shell, appConfig);
    }

    @Override
    public String getStartUrl(AppConfig appConfig) throws UnsupportedEncodingException {
        return OAuthSecretFlowUtil.getStartUrlImpl(appConfig);
    }

    public static class OAuthSecretBrowserListener extends OAuthBrowserListener {

        public OAuthSecretBrowserListener(Browser browser, Shell shell, AppConfig appConfig) {
            super(browser, shell, appConfig);
        }

        @Override
        public void changed(ProgressEvent progressEvent) {

        }

        @Override
        public void completed(ProgressEvent progressEvent) {
            super.completed(progressEvent);
            String url = browser.getUrl();
            try {
                String code = OAuthSecretFlowUtil.handleInitialUrl(url);

                if (code != null) {
                    SimplePost client = OAuthSecretFlowUtil.handleSecondPost(code, appConfig);
                    setReasonPhrase(client.getReasonPhrase());
                    setStatusCode(client.getStatusCode());
                    setResult(client.isSuccessful());
                    shell.close();
                    shell.dispose();
                }
            } catch (URISyntaxException | IOException | ParameterLoadException e) {
                doSimpleErrorHandling(url, e, logger);
            }
        }
    }
}
