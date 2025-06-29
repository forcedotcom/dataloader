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

import java.util.ArrayList;

/**
 * Login default control is the oauth login
 */
public class OAuthLoginControl extends Composite {
    private final Button loginButton;
    private LoginPage loginPage;
    protected final Combo environment;
    protected final AuthenticationRunner authRunner;
    protected final Label loginStatusLabel;
    private final ProgressBar spinner;

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
        spinner.setLayoutData(grid.createCell(10));
        spinner.setVisible(false);
        grid.createPadding(1);

        loginStatusLabel = grid.createLeftLabel(10, "\n\n\n");
    }

    protected void loginButton_Clicked(Event event) {
        loginButton.setEnabled(false); // Disable immediately to indicate progress
        spinner.setVisible(true); // Show spinner
        LoginCriteria criteria = new LoginCriteria(LoginCriteria.OAuthLogin);
        criteria.setEnvironment(environment.getText());
        authRunner.login(criteria, this::setLoginStatus);
    }
    private void setLoginStatus(String statusStr) {
        spinner.setVisible(false); // Hide spinner when login completes
        // Only re-enable if not logged in (i.e., login failed or timed out)
        if (this.loginPage.controller.isLoggedIn()) {
            loginButton.setEnabled(false);
        } else {
            loginButton.setEnabled(true);
        }
        loginStatusLabel.setText(statusStr);
        loginPage.setPageComplete();
    }
}
