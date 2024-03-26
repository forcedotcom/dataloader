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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * LoginStandardControl is the way to login to the api
 */
public class UsernamePasswordLoginStandardControl extends Composite {

    private final Button loginButton;
    private final Text userName;
    private final Text password;
    private final Text instanceUrl;
    private final AuthenticationRunner authentication;
    private final Label loginLabel;

    public UsernamePasswordLoginStandardControl(Composite parent, int style, AuthenticationRunner authentication) {
        super(parent, style);
        this.authentication = authentication;
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        this.setLayoutData(data);
        GridLayout layout = new GridLayout(2, false);
        layout.verticalSpacing = 10;
        this.setLayout(layout);

        final int MAX_CHARS_IN_TEXT = 40;
        Label usernameLabel = new Label(this, SWT.RIGHT);
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        usernameLabel.setLayoutData(data);
        usernameLabel.setText(Labels.getString("SettingsPage.username"));
        userName = new Text(this, SWT.LEFT | SWT.BORDER);
        userName.setText(authentication.getConfig().getString(Config.USERNAME));
        userName.setTextLimit(MAX_CHARS_IN_TEXT);
        GC gc = new GC(userName);
        final Point TEXT_SIZE = gc.textExtent("8");
        gc.dispose();
        data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        data.widthHint = MAX_CHARS_IN_TEXT * TEXT_SIZE.x;
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

        Label passwordLabel = new Label(this, SWT.RIGHT | SWT.WRAP);
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        passwordLabel.setLayoutData(data);
        passwordLabel.setText(Labels.getString("SettingsPage.password"));
        password = new Text(this, SWT.PASSWORD | SWT.LEFT | SWT.BORDER);
        password.setText("");
        password.setTextLimit(MAX_CHARS_IN_TEXT);
        data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        data.widthHint = MAX_CHARS_IN_TEXT * TEXT_SIZE.x;
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

        Label serverURLLabel = new Label(this, SWT.RIGHT | SWT.WRAP);
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        serverURLLabel.setLayoutData(data);
        serverURLLabel.setText(Labels.getString("SettingsPage.instServerUrl"));
        instanceUrl = new Text(this, SWT.LEFT | SWT.BORDER);
        instanceUrl.setText(authentication.getConfig().getString(Config.ENDPOINT));
        instanceUrl.setTextLimit(MAX_CHARS_IN_TEXT);
        data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        data.widthHint = MAX_CHARS_IN_TEXT * TEXT_SIZE.x;
        instanceUrl.setLayoutData(data);

        @SuppressWarnings("unused")
        Label emptyLabel = new Label(this, SWT.RIGHT);
        emptyLabel.setText("");
        loginButton = new Button(this, SWT.PUSH | SWT.CENTER | SWT.FLAT);
        loginButton.setText(Labels.getString("SettingsPage.login"));
        loginButton.addListener(SWT.Selection, this::loginButton_Clicked);
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        data.widthHint = Labels.getString("SettingsPage.login").length() * 2 * TEXT_SIZE.x;
        loginButton.setLayoutData(data);
        
        loginLabel = new Label(this, SWT.LEFT);
        data = new GridData();
        data.horizontalSpan = 2;
        loginLabel.setLayoutData(data);
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
