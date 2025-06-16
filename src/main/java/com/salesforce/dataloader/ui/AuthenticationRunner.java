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

import com.salesforce.dataloader.client.transport.HttpTransportInterface;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.model.LoginCriteria;
import com.salesforce.dataloader.util.ExceptionUtil;
import com.salesforce.dataloader.util.OAuthBrowserFlow;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;

import org.apache.logging.log4j.Logger;
import com.salesforce.dataloader.util.DLLogManager;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.util.Iterator;
import java.util.function.Consumer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.ServerSocket;
import com.salesforce.dataloader.oauth.OAuthFlowHandler;

/**
 * AuthenticationRunner is the UI orchestration of logging in.
 */
public class AuthenticationRunner {
    private static Logger logger = DLLogManager.getLogger(AuthenticationRunner.class);

    private final AppConfig appConfig;
    private final Controller controller;
    private final String nestedException = "nested exception is:";
    private final Shell shell;
    private Consumer<String> authStatusChangeConsumer;
    private LoginCriteria criteria;


    public AuthenticationRunner(Shell shell, AppConfig appConfig, Controller controller) {
        this.shell = shell;
        this.appConfig = appConfig;
        this.controller = controller;
    }

    public AppConfig getConfig() {
        return appConfig;
    }



    public void login(LoginCriteria criteria, Consumer<String> messenger) {
        this.authStatusChangeConsumer = messenger;
        this.criteria = criteria;

        criteria.updateConfig(appConfig);

        BusyIndicator.showWhile(Display.getDefault(), new Thread(this::loginAsync));
    }

    private void loginAsync() {
        try {
            // Check if OAuth is required
            if (requiresOAuthLogin(appConfig)) {
                OAuthFlowHandler oauthHandler = new OAuthFlowHandler(appConfig, this::updateStatus, controller);
                if (oauthHandler.handleOAuthLogin()) {
                    updateStatus("OAuth login successful");
                    return;
                }
                updateStatus("OAuth login failed");
                return;
            }

            // If OAuth is not required, proceed with username/password login
            updateStatus("Logging in...");
            PartnerConnection conn = login();
            if (conn != null) {
                updateStatus("Login successful");
            } else {
                updateStatus("Login failed");
            }
        } catch (Exception e) {
            logger.error("Login failed", e);
            updateStatus("Login failed: " + e.getMessage());
        }
    }

    private void updateStatus(String status) {
        authStatusChangeConsumer.accept(status);
    }

    private boolean requiresOAuthLogin(AppConfig config) {
        return criteria.getMode() == LoginCriteria.OAuthLogin;
    }

    private PartnerConnection login() {
        try {
            if (controller.login() && controller.getEntityDescribes() != null) {
                controller.saveConfig();
                controller.updateLoaderWindowTitleAndCacheUserInfoForTheSession();
                return controller.getLoginClient().getConnection();
            }
        } catch (Exception e) {
            logger.error("Error during username+password login", e);
        }
        return null;
    }

    private String getSoqlResultsAsString(String prefix, String soql, PartnerConnection conn) throws ConnectionException {
        QueryResult result = conn.query(soql);
        final SObject[] sfdcResults = result.getRecords();
        String debugInfo = prefix;
        if (sfdcResults != null) {
            for (SObject sobj : sfdcResults) {
                Iterator<XmlObject> fields = sobj.getChildren();
                if (fields == null) continue;
                String fieldsStr = "    ";
                boolean isIdInFields = false;
                while (fields.hasNext()) {
                    XmlObject field = fields.next();
                    if ("type".equalsIgnoreCase(field.getName().getLocalPart())) {
                        continue;
                    }
                    if (field.getValue() == null || field.getValue().toString().isBlank()) {
                        continue;
                    }
                    if ("id".equalsIgnoreCase(field.getName().getLocalPart())) {
                        if (isIdInFields) {
                            continue;
                        }
                        isIdInFields = true;
                    }
                    fieldsStr += field.getName().getLocalPart() + " = " + field.getValue() + "  ";
                }
                debugInfo += "\n" + fieldsStr;
            }
        }
        return debugInfo;
    }

    private void handleError(Throwable e, String message) {
        if (message == null || message.length() < 1) {
            authStatusChangeConsumer.accept(Labels.getString("LoginPage.invalidLogin"));
            logger.error(Labels.getString("LoginPage.invalidLogin"));
        } else {
            int x = message.indexOf(nestedException);
            if (x >= 0) {
                x += nestedException.length();
                message = message.substring(x);
            }
            authStatusChangeConsumer.accept(message.replace('\n', ' ').trim());
            logger.error(message);
        }
        logger.debug("\n" + ExceptionUtil.getStackTraceString(e));
    }

    private String readErrorResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}
