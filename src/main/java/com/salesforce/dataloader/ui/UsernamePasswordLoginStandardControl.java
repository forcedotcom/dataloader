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
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.*;

/**
 * LoginStandardControl is the way to login to the api
 */
public class UsernamePasswordLoginStandardControl extends Composite {

    private final Grid12 grid;
    private final Button loginButton;
    private final Text userName;
    private final Text password;
    private final Text instanceUrl;
    private final AuthenticationRunner authentication;
    private final Label loginLabel;

    public UsernamePasswordLoginStandardControl(Composite parent, int style, AuthenticationRunner authentication) {
        super(parent, style);
        this.authentication = authentication;
        grid = new Grid12(this, 40, 20);

        grid.createLabel(4, Labels.getString("SettingsPage.username"));
        userName = grid.createText(6, SWT.BORDER | SWT.FILL, authentication.getConfig().getString(Config.USERNAME));
        userName.addKeyListener(new KeyListener() {
            @Override
            public void keyReleased(KeyEvent e){}
            @Override
            public void keyPressed(KeyEvent e){
                if (e.character == '\r') {
                    password.setFocus();
                }
            }
        });
        grid.createPadding(2);

        grid.createLabel(4, Labels.getString("SettingsPage.password"));
        password = grid.createText(6, SWT.PASSWORD | SWT.BORDER, "");
        password.addKeyListener(new KeyListener() {
            @Override
            public void keyReleased(KeyEvent e){}
            @Override
            public void keyPressed(KeyEvent e) {
                if (!"".equals(loginLabel.getText())) {
                    loginLabel.setText(""); // clear the login status text
                }
                if (e.character == '\r') {
                    attempt_login();
                }
            }
        });
        grid.createPadding(2);

        grid.createLabel(4, Labels.getString("SettingsPage.instServerUrl"));
        instanceUrl = grid.createText(6, SWT.BORDER, authentication.getConfig().getString(Config.ENDPOINT));
        grid.createPadding(2);

        @SuppressWarnings("unused")
        Label emptyLabel = grid.createLabel(8, "");
        loginButton = grid.createButton(2, SWT.PUSH | SWT.FILL | SWT.FLAT, Labels.getString("SettingsPage.login"));
        loginButton.addListener(SWT.Selection, this::loginButton_Clicked);
        grid.createPadding(2);
        loginLabel = grid.createLabel(10, "");
    }

    private void loginButton_Clicked(Event event) {
        attempt_login();
    }
    
    private void attempt_login() {
        LoginCriteria criteria = new LoginCriteria(LoginCriteria.UsernamePasswordLoginStandard);
        criteria.setInstanceUrl(instanceUrl.getText());
        criteria.setUserName(userName.getText());
        criteria.setPassword(password.getText());
        authentication.login(criteria, this::setLoginStatus);
    }
    
    private void setLoginStatus(String statusStr) {
        if (Labels.getString("SettingsPage.loginSuccessful").equalsIgnoreCase(statusStr)) {
            loginButton.setEnabled(false);
        }
        loginLabel.setText(statusStr);
    }
}
