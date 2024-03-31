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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * LoginStandardControl is the way to login to the api
 */
public class UsernamePasswordLoginControl extends Composite {

    private final Button loginButton;
    private final Text userName;
    private final Text password;
    private final Text loginUrl;
    private final AuthenticationRunner authentication;
    private final Label loginLabel;
    private final Text sessionId;
    private final boolean isInternal;

    public UsernamePasswordLoginControl(Composite parent, int style, AuthenticationRunner authentication, boolean isInternal) {
        super(parent, style);
        this.isInternal = isInternal;
        this.authentication = authentication;
        GridData data = new GridData(GridData.FILL_BOTH);
        this.setLayoutData(data);
        GridLayout layout = new GridLayout(2, true);
        layout.verticalSpacing = 10;
        this.setLayout(layout);

        Label usernameLabel = new Label(this, SWT.RIGHT);
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        usernameLabel.setLayoutData(data);
        usernameLabel.setText(Labels.getString("LoginPage.username"));
        userName = new Text(this, SWT.LEFT | SWT.BORDER);
        userName.setText(authentication.getConfig().getString(Config.USERNAME));
       data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        userName.setLayoutData(data);

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

        Label pwdOrSessionIdLabel = new Label(this, SWT.RIGHT | SWT.WRAP);
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        pwdOrSessionIdLabel.setLayoutData(data);
        data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        if (isInternal) {
            pwdOrSessionIdLabel.setText(Labels.getString("LoginPage.sessionId"));
            sessionId = new Text(this, SWT.LEFT | SWT.BORDER);
            sessionId.setText(authentication.getConfig().getString(Config.SFDC_INTERNAL_SESSION_ID));
            sessionId.setLayoutData(data);
            password = null;
        } else {
            pwdOrSessionIdLabel.setText(Labels.getString("LoginPage.password"));
            password = new Text(this, SWT.PASSWORD | SWT.LEFT | SWT.BORDER);
            password.setText("");
            password.setLayoutData(data);
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
            sessionId = null;
        }

        Label serverURLLabel = new Label(this, SWT.RIGHT | SWT.WRAP);
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        serverURLLabel.setLayoutData(data);
        serverURLLabel.setText(Labels.getString("LoginPage.instServerUrl"));
        loginUrl = new Text(this, SWT.LEFT | SWT.BORDER);
        loginUrl.setText(authentication.getConfig().getString(Config.ENDPOINT));
        data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        loginUrl.setLayoutData(data);

        @SuppressWarnings("unused")
        Label emptyLabel = new Label(this, SWT.RIGHT);
        emptyLabel.setText("");
        loginButton = new Button(this, SWT.PUSH | SWT.CENTER | SWT.FLAT);
        loginButton.setText("    " + Labels.getString("LoginPage.login") + "    ");
        loginButton.addListener(SWT.Selection, this::loginButton_Clicked);
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        loginButton.setLayoutData(data);
        
        loginLabel = new Label(this, SWT.LEFT | SWT.WRAP);
        loginLabel.setText("");
        data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        data.horizontalSpan = 2;
        loginLabel.setLayoutData(data);
    }

    private void loginButton_Clicked(Event event) {
        attempt_login();
    }
    
    private void attempt_login() {
        LoginCriteria criteria = null;
        if (isInternal) {
            criteria = new LoginCriteria(LoginCriteria.UsernamePasswordLoginAdvanced);
            criteria.setSessionId(sessionId.getText());
        } else {
            criteria = new LoginCriteria(LoginCriteria.UsernamePasswordLoginStandard);
            criteria.setPassword(password.getText());
        }
        criteria.setInstanceUrl(loginUrl.getText());
        criteria.setUserName(userName.getText());
        authentication.login(criteria, this::setLoginStatus);
    }
    
    private void setLoginStatus(String statusStr) {
        if (Labels.getString("LoginPage.loginSuccessful").equalsIgnoreCase(statusStr)) {
            loginButton.setEnabled(false);
        }
        loginLabel.setText(statusStr);
    }
}
