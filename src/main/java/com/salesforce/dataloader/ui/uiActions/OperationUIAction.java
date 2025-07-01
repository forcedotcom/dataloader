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
package com.salesforce.dataloader.ui.uiActions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;

import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.ui.LoaderWindow;
import com.salesforce.dataloader.ui.LoaderWizardDialog;
import com.salesforce.dataloader.ui.LoginPage;
import java.net.HttpURLConnection;
import java.net.URL;

public class OperationUIAction extends Action {
    private final Controller controller;
    private final OperationInfo opInfo;

    public OperationUIAction(Controller controller, OperationInfo info) {
        super(info.getMenuLabel(), info.getIconImageDescriptor());
        setToolTipText(info.getToolTipText());

        this.controller = controller;
        this.opInfo = info;

        setEnabled(info.isOperationAllowed(controller.getAppConfig()));
    }

    @Override
    public void run() {
        LoaderWindow loaderWindow = LoaderWindow.getApp();
        Shell shell = loaderWindow != null ? loaderWindow.getShell() : null;
        if (shell == null) return;

        // If not logged in or session is not present, show login dialog immediately
        com.salesforce.dataloader.client.SessionInfo session = controller.getPartnerClient().getSession();
        if (session == null || session.getSessionId() == null || session.getSessionId().isEmpty() || session.getUserInfoResult() == null) {
            System.out.println("[SessionCheck] No valid session. Showing login dialog immediately.");
            LoaderWizardDialog dlg = new LoaderWizardDialog(shell, new LoginWizard(controller), controller.getAppConfig());
            dlg.open();
            return;
        }

        // Optionally show busy cursor
        shell.getDisplay().asyncExec(() -> shell.setCursor(shell.getDisplay().getSystemCursor(org.eclipse.swt.SWT.CURSOR_WAIT)));

        new Thread(() -> {
            boolean sessionValid = true;
            HttpURLConnection conn = null;
            try {
                // Use a direct REST API call for the session check
                String instanceUrl = controller.getAppConfig().getString("sfdc.oauth.instanceUrl");
                if (instanceUrl == null || instanceUrl.isEmpty()) {
                    // Try to get from PartnerConnection's service endpoint
                    try {
                        String serviceEndpoint = controller.getPartnerClient().getConnection().getConfig().getServiceEndpoint();
                        if (serviceEndpoint != null && !serviceEndpoint.isEmpty()) {
                            URL endpointUrl = new URL(serviceEndpoint);
                            instanceUrl = endpointUrl.getProtocol() + "://" + endpointUrl.getHost();
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
                if (instanceUrl == null || instanceUrl.isEmpty()) {
                    sessionValid = false;
                } else {
                    String apiVersion = controller.getAppConfig().getString("sfdc.apiVersion");
                    if (apiVersion == null || apiVersion.isEmpty()) apiVersion = "64.0";
                    String limitsUrl = instanceUrl + "/services/data/v" + apiVersion + "/limits";
                    URL url = new URL(limitsUrl);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(3000); // 3 seconds
                    conn.setReadTimeout(3000); // 3 seconds
                    conn.setRequestMethod("GET");
                    String sessionId = controller.getPartnerClient().getSession().getSessionId();
                    conn.setRequestProperty("Authorization", "Bearer " + sessionId);
                    conn.setRequestProperty("Accept", "application/json");
                    int responseCode = conn.getResponseCode();
                    if (responseCode == 401 || responseCode == 403) {
                        sessionValid = false;
                    }
                }
            } catch (Exception e) {
                sessionValid = false;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            final boolean finalSessionValid = sessionValid;

            shell.getDisplay().asyncExec(() -> {
                shell.setCursor(null);
                if (!finalSessionValid) {
                    controller.logout();
                    LoaderWizardDialog dlg = new LoaderWizardDialog(shell, new LoginWizard(controller), controller.getAppConfig());
                    dlg.open();
                } else {
                    controller.clearMapper();
                    LoaderWizardDialog dlg = new LoaderWizardDialog(shell, opInfo.getUIHelper().instantiateWizard(controller), controller.getAppConfig());
                    dlg.open();
                }
            });
        }).start();
    }

    // Minimal wizard to show LoginPage as a wizard
    private static class LoginWizard extends Wizard {
        private final Controller controller;
        private LoginPage loginPage;
        public LoginWizard(Controller controller) {
            this.controller = controller;
            setWindowTitle("Login");
        }
        @Override
        public void addPages() {
            loginPage = new LoginPage(controller);
            addPage(loginPage);
        }
        @Override
        public boolean performFinish() { return false; }
        @Override
        public boolean canFinish() { return false; }
    }

}
