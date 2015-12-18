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
import com.salesforce.dataloader.model.LoginCriteria;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

import java.util.ArrayList;

/**
 * Login default control is the oauth login
 */
public class LoginDefaultControl extends Composite {
    private final Button loginButton;
    private final CCombo environment;
    private final AuthenticationRunner authenticator;
    private final Label loginLabel;

    public LoginDefaultControl(Composite parent, int style, AuthenticationRunner authenticator) {
        super(parent, style);
        this.authenticator = authenticator;

        Grid12 grid =  new Grid12(this, 40, 20);

        grid.createLabel(4, Labels.getString("SettingsPage.environment"));
        ArrayList<String> environments = authenticator.getConfig().getStrings(Config.OAUTH_ENVIRONMENTS);
        environment = grid.createCombo(6, SWT.DROP_DOWN | SWT.BORDER, environments);
        String currentEnvironment = authenticator.getConfig().getString(Config.OAUTH_ENVIRONMENT);
        if (environments.contains(currentEnvironment)) {
            environment.setText(currentEnvironment);
        }

        grid.createPadding(2);

        loginLabel = grid.createLabel(10, "");
        loginButton = grid.createButton(2, SWT.PUSH | SWT.FILL, Labels.getString("SettingsPage.login"));
        loginButton.addListener(SWT.Selection, this::loginButton_Clicked);
    }

    private void loginButton_Clicked(Event event) {
        LoginCriteria criteria = new LoginCriteria(LoginCriteria.Default);
        criteria.setEnvironment(environment.getText());
        authenticator.login(criteria, loginLabel::setText);
    }
}
