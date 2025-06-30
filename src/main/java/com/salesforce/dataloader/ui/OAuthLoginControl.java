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

import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.model.LoginCriteria;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;

/**
 * Login default control is the oauth login
 */
public class OAuthLoginControl extends Composite {
    private final Button loginButton;
    private LoginPage loginPage;
    protected final Combo environment;
    protected final AuthenticationRunner authRunner;
    protected final Text loginStatusText;
    private final ProgressBar spinner;
    private final org.eclipse.swt.layout.GridData spinnerGridData;
    private boolean loginInProgress = false;

    public OAuthLoginControl(Composite parent, int style, LoginPage loginPage, AuthenticationRunner authRunner) {
        super(parent, style);
        this.authRunner = authRunner;
        this.loginPage = loginPage;

        Grid12 grid =  new Grid12(this, 40, false, true);

        grid.createLabel(4, Labels.getString("LoginPage.environment"));
        ArrayList<String> environments = authRunner.getConfig().getStrings(AppConfig.PROP_SERVER_ENVIRONMENTS);
        environment = grid.createCombo(6, SWT.DROP_DOWN | SWT.BORDER, environments);
        String currentEnvironment = authRunner.getConfig().getString(AppConfig.PROP_SELECTED_SERVER_ENVIRONMENT);
        if (environments.contains(currentEnvironment)) {
            environment.setText(currentEnvironment);
        }

        grid.createPadding(2);

        @SuppressWarnings("unused")
        Label emptyLabel = grid.createLabel(8, "");
        loginButton = grid.createButton(2, SWT.PUSH | SWT.FILL | SWT.FLAT, Labels.getString("LoginPage.login"));
        loginButton.addListener(SWT.Selection, this::loginButton_Clicked);
        grid.createPadding(2);
        
        grid.createPadding(1);
        spinner = new ProgressBar(this, SWT.INDETERMINATE);
        spinnerGridData = (org.eclipse.swt.layout.GridData) grid.createCell(10);
        spinner.setLayoutData(spinnerGridData);
        spinner.setVisible(false);
        spinnerGridData.heightHint = 24; // Default spinner height
        grid.createPadding(1);

        loginStatusText = new Text(this, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP | SWT.BORDER);
        loginStatusText.setLayoutData(grid.createCell(10));
        loginStatusText.setBackground(getBackground());
        loginStatusText.setText("\n\n\n");
    }

    protected void loginButton_Clicked(Event event) {
        loginButton.setEnabled(false); // Disable immediately to indicate progress
        spinner.setVisible(true); // Show spinner
        spinnerGridData.exclude = false; // Ensure spinner is included in layout
        spinnerGridData.heightHint = 24; // Reset spinner area to original height
        spinner.getParent().layout();
        loginInProgress = true;
        LoginCriteria criteria = new LoginCriteria(LoginCriteria.OAuthLogin);
        criteria.setEnvironment(environment.getText());
        authRunner.login(criteria, this::setLoginStatus, this::onLoginFlowComplete);
    }

    /**
     * Called by the flow handler's Runnable after all OAuth flows are complete (success or failure).
     */
    public void onLoginFlowComplete() {
        loginInProgress = false;
        spinner.setVisible(false);
        spinnerGridData.exclude = true;
        spinner.getParent().layout();
        // Only re-enable if not logged in (i.e., login failed or timed out)
        if (this.loginPage.controller.isLoggedIn()) {
            loginButton.setEnabled(false);
            // Advance the wizard after successful login on the UI thread
            org.eclipse.swt.widgets.Display.getDefault().asyncExec(() -> this.loginPage.setPageComplete());
        } else {
            loginButton.setEnabled(true);
        }
        loginStatusText.getParent().layout();
    }

    private void setLoginStatus(String statusStr) {
        // Always expand the status area for multi-line errors
        loginStatusText.setText(statusStr);
        loginStatusText.setTopIndex(0);
        loginStatusText.getParent().layout();
        // Do not touch spinner or button here; only in onLoginFlowComplete
    }
}
