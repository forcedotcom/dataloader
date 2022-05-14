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
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.widgets.Shell;

/**
 * The common class for listening to browser events use in oauth flows.
 */
public abstract class OAuthBrowserListener implements ProgressListener {
    private static Logger logger = LogManager.getLogger(Config.class);
    protected final Browser browser;
    protected final Shell shell;
    protected final Config config;
    private String reasonPhrase;
    private int statusCode;
    private boolean result;

    public OAuthBrowserListener(Browser browser, Shell shell, Config config) {
        this.browser = browser;
        this.shell = shell;
        this.config = config;
    }

    @Override
    public abstract void changed(ProgressEvent progressEvent);

    public void completed(ProgressEvent progressEvent) {
        logger.debug("SWT Browser engine is " + 
            browser.evaluate("var ua = navigator.userAgent, tem, M = ua.match(/(opera|chrome|safari|firefox|msie|trident(?=\\/))\\/?\\s*(\\d+)/i) || []; if (/trident/i.test(M[1])) { tem = /\\brv[ :]+(\\d+)/g.exec(ua) || []; return 'IE ' + (tem[1] || ''); } if (M[1] === 'Chrome') { tem = ua.match(/\\b(OPR|Edge)\\/(\\d+)/); if (tem != null) return tem.slice(1).join(' ').replace('OPR', 'Opera'); } M = M[2] ? [M[1], M[2]] : [navigator.appName, navigator.appVersion, '-?']; if ((tem = ua.match(/version\\/(\\d+)/i)) != null) M.splice(1, 1, tem[1]); return M.join(' ');"));
        logger.debug("SWT Browser engine's userAgent property is " + 
                browser.evaluate("return navigator.userAgent;"));
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean getResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public void doSimpleErrorHandling(String url, Exception e, Logger logger){
        logger.error("Failed to retrieve oauth token. Bad Url result:" + url, e);
        setReasonPhrase("Invalid response from login server");
        setResult(false);
        shell.close();
        shell.dispose();
    }
}
