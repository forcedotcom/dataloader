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

package com.salesforce.dataloader.oauth;

import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.ui.Labels;
import com.salesforce.dataloader.util.DLLogManager;
import com.salesforce.dataloader.util.OAuthServerFlow;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.widgets.Display;

import java.util.function.Consumer;

/**
 * Utility class to handle OAuth flow (WebServer with Proof Key for Code Exchange(PKCE)) for both UI and batch modes.
 */
public class OAuthFlowHandler {
    private static final Logger logger = DLLogManager.getLogger(OAuthFlowHandler.class);
    private final AppConfig appConfig;
    private final Consumer<String> statusConsumer;
    private final Controller controller;
    private final Runnable loginButtonEnabler;

    public OAuthFlowHandler(AppConfig appConfig, Consumer<String> statusConsumer, Controller controller, Runnable loginButtonEnabler) {
        this.appConfig = appConfig;
        this.statusConsumer = statusConsumer;
        this.controller = controller;
        this.loginButtonEnabler = loginButtonEnabler;
    }

    /**
     * Handles Web Server OAuth flow leveraging Proof Key for Code Exchange(PKCE).
     *
     * @return true if login was successful, false otherwise
     */
    public boolean handleOAuthLogin() {
        logger.info("Starting OAuth flow");
        appConfig.setLastOAuthFlow("PKCE");

        logger.info("Attempting OAuth WebServer with PKCE flow, launching browser for login");
        if (statusConsumer != null) {
            statusConsumer.accept(Labels.getString("OAuthLoginControl.statusAttemptingPKCE"));
        }
        try {
            OAuthServerFlow pkceFlow = new OAuthServerFlow(appConfig, true, statusConsumer);
            if (pkceFlow.performOAuthFlow()) {
                logger.info("WebServer with PKCE flow completed successfully");
                if (statusConsumer != null) {
                    statusConsumer.accept(Labels.getString("OAuthLoginControl.statusPKCESuccess"));
                }
                if (controller != null) {
                    try {
                        if (controller.login()) {
                            controller.saveConfig();
                            Display.getDefault().asyncExec(() -> controller.updateLoaderWindowTitleAndCacheUserInfoForTheSession());
                            if (loginButtonEnabler != null) {
                                Display.getDefault().asyncExec(loginButtonEnabler);
                            }
                            return true;
                        }
                    } catch (Exception e) {
                        logger.error("Failed to update controller's login state after WebServer with PKCE flow", e);
                        if (statusConsumer != null) {
                            statusConsumer.accept(Labels.getString("OAuthLoginControl.statusControllerUpdateError"));
                        }
                        if (loginButtonEnabler != null) {
                            Display.getDefault().asyncExec(loginButtonEnabler);
                        }
                        return false;
                    }
                }
                if (loginButtonEnabler != null) {
                    Display.getDefault().asyncExec(loginButtonEnabler);
                }
                return true;
            } 
            else {
                logger.error("OAuth WebServer with PKCE flow failed ");
                if (statusConsumer != null) {
                    statusConsumer.accept("OAuth WebServer with PKCE flow failed. Please try again or use Username Password.");
                }
                return false;
            }
        } catch (Exception e) {
            logger.error("OAuth WebServer with PKCE flow failed: " + e.getMessage(), e);
            if (statusConsumer != null) {
                statusConsumer.accept(Labels.getString("OAuthLoginControl.statusPKCEFailedFallbackBrowser"));
            }
            if (loginButtonEnabler != null) {
                Display.getDefault().asyncExec(loginButtonEnabler);
            }
            return false;
        }
    }
} 