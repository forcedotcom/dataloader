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

import java.util.ArrayList;

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
public class UsernamePasswordLoginControl extends Composite {

    private final Button loginButton;
    private final Text userName;
    private final Text password;
    private final AuthenticationRunner authRunner;
    private final Label loginStatusLabel;
    private final Text sessionId;
    private final boolean authUsingSessionId;
    private final LoginPage loginPage;
    private Combo envDropdown;

    public UsernamePasswordLoginControl(Composite parent, int style, LoginPage loginPage, AuthenticationRunner authRunner, boolean authUsingSessionId) {
        super(parent, style);
        this.authUsingSessionId = authUsingSessionId;
        this.authRunner = authRunner;
        this.loginPage = loginPage;
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
        userName.setText(authRunner.getConfig().getString(AppConfig.PROP_USERNAME));
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

        Text pwdOrSessionIdLabel = new Text(this, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        pwdOrSessionIdLabel.setLayoutData(data);
        data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        if (authUsingSessionId) {
            pwdOrSessionIdLabel.setText(Labels.getString("LoginPage.sessionId"));
            sessionId = new Text(this, SWT.LEFT | SWT.BORDER);
            sessionId.setText(authRunner.getConfig().getString(AppConfig.PROP_SFDC_INTERNAL_SESSION_ID));
            sessionId.setLayoutData(data);
            password = null;
        } else {
            pwdOrSessionIdLabel.setText(Labels.getString("LoginPage.password"));
            pwdOrSessionIdLabel.setToolTipText(Labels.getString("LoginPage.TooltipPassword"));
            password = new Text(this, SWT.PASSWORD | SWT.LEFT | SWT.BORDER);
            password.setText("");
            password.setToolTipText(Labels.getString("LoginPage.TooltipPassword"));
            password.setLayoutData(data);
            password.addKeyListener(new KeyListener() {
                @Override
                public void keyReleased(KeyEvent e){}
                @Override
                public void keyPressed(KeyEvent e) {
                    if (!"".equals(loginStatusLabel.getText())) {
                        loginStatusLabel.setText(""); // clear the login status text
                    }
                    if (e.character == '\r') {
                        attempt_login();
                    }
                }
            });
            sessionId = null;
        }
        
        Label envLabel = new Label(this, SWT.RIGHT | SWT.WRAP);
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        envLabel.setLayoutData(data);
        envLabel.setText(Labels.getString("LoginPage.environment"));
        ArrayList<String> environments = authRunner.getConfig().getStrings(AppConfig.PROP_SERVER_ENVIRONMENTS);

        envDropdown = new Combo(this, SWT.DROP_DOWN | SWT.BORDER);
        data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        envDropdown.setLayoutData(data);
        for (String label: environments) {
            envDropdown.add(label);
        }
        String currentEnvironment = authRunner.getConfig().getString(AppConfig.PROP_SELECTED_SERVER_ENVIRONMENT);
        if (environments.contains(currentEnvironment)) {
            envDropdown.setText(currentEnvironment);
        }

        @SuppressWarnings("unused")
        Label emptyLabel = new Label(this, SWT.RIGHT);
        emptyLabel.setText("");
        loginButton = new Button(this, SWT.PUSH | SWT.CENTER | SWT.FLAT);
        loginButton.setText(Labels.getString("LoginPage.login"));
        loginButton.addListener(SWT.Selection, this::loginButton_Clicked);
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        int widthHint = UIUtils.getControlWidth(loginButton);
        Point minSize = loginButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        data.widthHint = Math.max(widthHint, minSize.x);
        loginButton.setLayoutData(data);
        
        loginStatusLabel = new Label(this, SWT.LEFT | SWT.WRAP);
        loginStatusLabel.setText("");
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        GC gc = new GC(loginStatusLabel);
        Point TEXT_SIZE = gc.textExtent("A");
        gc.dispose();
        data.heightHint = TEXT_SIZE.y * 3;
        loginStatusLabel.setLayoutData(data);
    }

    private void loginButton_Clicked(Event event) {
        attempt_login();
    }
    
    private void attempt_login() {
        LoginCriteria criteria = null;
        if (authUsingSessionId) {
            criteria = new LoginCriteria(LoginCriteria.SessionIdLogin);
            criteria.setSessionId(sessionId.getText());
        } else {
            criteria = new LoginCriteria(LoginCriteria.UsernamePasswordLogin);
            criteria.setPassword(password.getText());
        }
        criteria.setUserName(userName.getText());
        criteria.setEnvironment(envDropdown.getText());
        authRunner.login(criteria, this::setLoginStatus);
    }
    
    private void setLoginStatus(String statusStr) {
        if (this.loginPage.controller.isLoggedIn()) {
            loginButton.setEnabled(false);
        }
        loginStatusLabel.setText(statusStr);
        this.loginPage.setPageComplete();
    }
}
