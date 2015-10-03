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

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.util.ExceptionUtil;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.soap.partner.fault.LoginFault;
import com.sforce.ws.ConnectionException;

/**
 * Describe your class here.
 * 
 * @author Lexi Viripaeff
 * @since 6.0
 */
public class SettingsPage extends WizardPage {

    private final Controller controller;
    private Text textPassword;
    private Text textUsername;
    private Button isSessionIdLogin;
    private Text textSessionId;
    private Text textEndpoint;
    private Label loginLabel;

    // logger
    private static Logger logger = Logger.getLogger(SettingsPage.class);
    private Button isOAuthIdLogin;
    private Text textOAuthId;
    private Text textOAuthSecret;
    private Text textOAuthEndpoint;
    private AuthenticatorControl authenticator;

    public SettingsPage(Controller controller) {
        super(Labels.getString("SettingsPage.title"), Labels.getString("SettingsPage.titleMsg"), UIUtils.getImageRegistry().getDescriptor("splashscreens")); //$NON-NLS-1$ //$NON-NLS-2$

        this.controller = controller;

        setPageComplete(false);

        // Set the description
        setDescription(Labels.getString("SettingsPage.enterUsernamePassword")); //$NON-NLS-1$


    }

    @Override
    public void createControl(Composite parent) {
        getShell().setImage(UIUtils.getImageRegistry().get("sfdc_icon")); //$NON-NLS-1$

        Config config = controller.getConfig();
        Composite control = new Composite(parent, SWT.FILL);
        Grid12 grid = new Grid12(control, 40);
        authenticator = new AuthenticatorControl(config, controller);

        LoginDefaultControl defaultControl = new LoginDefaultControl(control, SWT.FILL, authenticator);
        defaultControl.setLayoutData(grid.createCell(12));
        LoginStandardControl standardControl = new LoginStandardControl(control, SWT.FILL, authenticator);
        standardControl.setLayoutData(grid.createCell(12));
        LoginAdvancedControl advancedControl = new LoginAdvancedControl(control, SWT.FILL, authenticator);
        advancedControl.setLayoutData(grid.createCell(12));

        setControl(control);

//        GridLayout gridLayout = new GridLayout();
//        gridLayout.numColumns = 3;
//        gridLayout.marginHeight = 30;
//        comp.setLayout(gridLayout);
//
//        Label labelUsername = new Label(comp, SWT.RIGHT);
//        labelUsername.setText(Labels.getString("SettingsPage.username")); //$NON-NLS-1$
//
//        textUsername = new Text(comp, SWT.BORDER);
//        textUsername.setText(config.getString(Config.USERNAME));
//
//        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
//        data.widthHint = 150;
//        textUsername.setLayoutData(data);
//
//        // Consume the 2 cells to the right of txtUsername and txtPassword
//        Composite composite2 = new Composite(comp, SWT.NONE);
//        data = new GridData();
//        data.verticalSpan = 2;
//        composite2.setLayoutData(data);
//
//        Label labelPassword = new Label(comp, SWT.RIGHT);
//        labelPassword.setText(Labels.getString("SettingsPage.password")); //$NON-NLS-1$
//
//        textPassword = new Text(comp, SWT.BORDER | SWT.PASSWORD);
//        // don't want to cache the password
//        config.setValue(Config.PASSWORD, ""); //$NON-NLS-1$
//        textPassword.setText(config.getString(Config.PASSWORD));
//
//        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
//        data.widthHint = 150;
//        textPassword.setLayoutData(data);
//
//        //endpoint
//        Label labelEndpoint = new Label(comp, SWT.RIGHT);
//        labelEndpoint.setText(Labels.getString("SettingsPage.instServerUrl")); //$NON-NLS-1$
//
//        textEndpoint = new Text(comp, SWT.BORDER);
//        textEndpoint.setText(config.getString(Config.ENDPOINT));
//        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
//        data.widthHint = 150;
//        textEndpoint.setLayoutData(data);
//
//        if(config.getBoolean(Config.SFDC_INTERNAL)) {
//            //spacer
//            Label spacer = new Label(comp, SWT.NONE);
//            data = new GridData();
//            data.horizontalSpan = 3;
//            data.widthHint = 15;
//            spacer.setLayoutData(data);
//
//            //lIsSessionLogin checkbox
//            Label labelIsSessionIdLogin = new Label(comp, SWT.RIGHT);
//            labelIsSessionIdLogin.setText(Labels.getString("SettingsPage.isSessionIdLogin")); //$NON-NLS-1$
//
//            isSessionIdLogin = new Button(comp, SWT.CHECK);
//            isSessionIdLogin.setSelection(config.getBoolean(Config.SFDC_INTERNAL_IS_SESSION_ID_LOGIN));
//            data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
//            data.horizontalSpan = 2;
//            isSessionIdLogin.setLayoutData(data);
//            isSessionIdLogin.addSelectionListener(new SelectionAdapter(){
//                @Override
//                public void widgetSelected(SelectionEvent event) {
//                    reconcileLoginCredentialFieldsEnablement();
//                }
//            });
//
//            //sessionId
//            Label labelSessionId = new Label(comp, SWT.RIGHT);
//            labelSessionId.setText(Labels.getString("SettingsPage.sessionId")); //$NON-NLS-1$
//
//            textSessionId = new Text(comp, SWT.BORDER);
//            textSessionId.setText(config.getString(Config.SFDC_INTERNAL_SESSION_ID));
//            data = new GridData(SWT.FILL, SWT.CENTER, true, false);
//            data.widthHint = 150;
//            textSessionId.setLayoutData(data);
//
//            // consume the 2 cells to the right of textSessionId & textEndpoint
//            composite2 = new Composite(comp, SWT.NONE);
//            data = new GridData();
//            data.verticalSpan = 2;
//            composite2.setLayoutData(data);
//
//        }
//        if(config.getBoolean(Config.OAUTH)) {
//            //spacer
//            Label spacer = new Label(comp, SWT.NONE);
//            data = new GridData();
//            data.horizontalSpan = 3;
//            data.widthHint = 15;
//            spacer.setLayoutData(data);
//
//            Label labelIsOAuthLogin = new Label(comp, SWT.RIGHT);
//            labelIsOAuthLogin.setText(Labels.getString("SettingsPage.isOAuthLogin")); //$NON-NLS-1$
//
//            isOAuthIdLogin = new Button(comp, SWT.CHECK);
//            isOAuthIdLogin.setSelection(config.getBoolean(Config.OAUTH));
//            data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
//            data.horizontalSpan = 2;
//            isOAuthIdLogin.setLayoutData(data);
//            isOAuthIdLogin.addSelectionListener(new SelectionAdapter(){
//                @Override
//                public void widgetSelected(SelectionEvent event) {
//                    reconcileLoginCredentialFieldsEnablement();
//                }
//            });
//
//            //OAuthId
//            Label labelOAuthId = new Label(comp, SWT.RIGHT);
//            labelOAuthId.setText(Labels.getString("SettingsPage.OAuthClientId")); //$NON-NLS-1$
//
//            textOAuthId = new Text(comp, SWT.BORDER);
//            textOAuthId.setText(config.getString(Config.OAUTH_CLIENTID));
//            data = new GridData(SWT.FILL, SWT.CENTER, true, false);
//            data.widthHint = 150;
//            textOAuthId.setLayoutData(data);
//
//            // consume the 2 cells to the right of textOAuthId & textEndpoint
//            composite2 = new Composite(comp, SWT.NONE);
//            data = new GridData();
//            data.verticalSpan = 2;
//            composite2.setLayoutData(data);
//
//            //OAuthId
//            Label labelOAuthSecret = new Label(comp, SWT.RIGHT);
//            labelOAuthSecret.setText(Labels.getString("SettingsPage.OAuthClientSecret")); //$NON-NLS-1$
//
//            textOAuthSecret = new Text(comp, SWT.BORDER);
//            textOAuthSecret.setText(config.getString(Config.OAUTH_CLIENTKEY));
//            data = new GridData(SWT.FILL, SWT.CENTER, true, false);
//            data.widthHint = 150;
//            textOAuthSecret.setLayoutData(data);
//
//
//
//            //endpoint
//            labelEndpoint = new Label(comp, SWT.RIGHT);
//            labelEndpoint.setText(Labels.getString("SettingsPage.OAuthServer")); //$NON-NLS-1$
//
//            textOAuthEndpoint = new Text(comp, SWT.BORDER);
//            textOAuthEndpoint.setText(config.getString(Config.OAUTH_SERVER));
//            data = new GridData(SWT.FILL, SWT.CENTER, true, false);
//            data.widthHint = 150;
//            textOAuthEndpoint.setLayoutData(data);
//
//        }
//
//        if (config.getBoolean(Config.OAUTH) || config.getBoolean(Config.SFDC_INTERNAL)){
//            reconcileLoginCredentialFieldsEnablement();
//        }
//
//        loginLabel = new Label(comp, SWT.NONE);
//        data = new GridData(GridData.FILL_HORIZONTAL);
//        data.horizontalSpan = 3;
//        data.widthHint = 220;
//        loginLabel.setLayoutData(data);
//
//        Button loginButton = new Button(comp, SWT.PUSH);
//        loginButton.setText(Labels.getString("SettingsPage.login")); //$NON-NLS-1$
//        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
//        data.horizontalSpan = 2;
//        data.widthHint = 75;
//        loginButton.setLayoutData(data);
//        final LoginButtonSelectionListener loginListener = new LoginButtonSelectionListener();
//        loginButton.addSelectionListener(loginListener);
//        parent.getShell().setDefaultButton(loginButton);
//
//        Composite composite5 = new Composite(comp, SWT.NONE);
//        data = new GridData();
//        data.horizontalSpan = 2;
//        composite5.setLayoutData(data);
//
//        setControl(comp);
//
//        // respond to enter key on username and password box
//        textUsername.addKeyListener(new UsernamePasswordKeyListener(loginListener));
//        textPassword.addKeyListener(new UsernamePasswordKeyListener(loginListener));
    }

    private static class UsernamePasswordKeyListener implements KeyListener {

        private final LoginButtonSelectionListener listener;

        private UsernamePasswordKeyListener(
                LoginButtonSelectionListener listener) {
            super();
            this.listener = listener;
        }

        @Override
        public void keyReleased(KeyEvent arg0) {
            if(arg0.keyCode == 13) {
                listener.widgetSelected(null);
            }
        }

        @Override
        public void keyPressed(KeyEvent arg0) {
            // do nothing on press, only on release.
        }
    }

    // non-static since it needs access to SettingsPage member variables
    private class LoginButtonSelectionListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent event) {
            Config config = controller.getConfig();
            config.setValue(Config.USERNAME, textUsername.getText());
            config.setValue(Config.PASSWORD, textPassword.getText());
            config.setValue(Config.ENDPOINT, textEndpoint.getText());

            if(config.getBoolean(Config.SFDC_INTERNAL)) {
                config.setValue(Config.SFDC_INTERNAL_IS_SESSION_ID_LOGIN, isSessionIdLogin.getSelection());
                config.setValue(Config.SFDC_INTERNAL_SESSION_ID, textSessionId.getText());
            }
            if(config.getBoolean(Config.OAUTH)) {
                config.setValue(Config.OAUTH_SERVER, textOAuthEndpoint.getText());
                config.setValue(Config.OAUTH_CLIENTID, textOAuthId.getText());
                config.setValue(Config.OAUTH_CLIENTKEY, textOAuthSecret.getText());
            }
            controller.saveConfig();

            if(config.getBoolean(Config.OAUTH)) {
                if (isOAuthIdLogin.getSelection()) {
                    OAuthFlow flow = new OAuthFlow(getShell(), config);
                    flow.open();
                }
            }
        }
    }

    /**
     * Need to subclass this function to prevent the getNextPage() function being called before the button is clicked.
     */
    @Override
    public boolean canFlipToNextPage() {
        return isPageComplete();
    }

    /**
     * Returns the next page, login.
     * 
     * @return IWizardPage
     */

    @Override
    public IWizardPage getNextPage() {

        return super.getNextPage();

    }

    /**
     * Loads DataSelectionPage. To be overridden by subclasses for special behavior.
     * 
     * @param controller
     */
    protected void loadDataSelectionPage(Controller controller) {
        DataSelectionPage selection = (DataSelectionPage)getWizard().getPage(Labels.getString("DataSelectionPage.data")); //$NON-NLS-1$
        if(selection.setupPage()) {
            setPageComplete(true);
        } else {
            // this shouldn't relly happen since client is logged in and entity describes are set
            loginLabel.setText(Labels.getString("SettingsPage.invalidLogin")); //$NON-NLS-1$
            setPageComplete(false);
        }
    }

    /**
     * Enables or disables username/password or sessionId/serverUrl
     * text fields depending on if isSessionIdLogin is checked.
     */
    private void reconcileLoginCredentialFieldsEnablement() {
        boolean isSesssionLogin = isSessionIdLogin != null && isSessionIdLogin.getSelection();
        boolean isOAuthLogin = isOAuthIdLogin != null && isOAuthIdLogin.getSelection();
        textUsername.setEnabled(!isSesssionLogin && !isOAuthLogin);
        textPassword.setEnabled(!isSesssionLogin && !isOAuthLogin);

        if (textSessionId != null) textSessionId.setEnabled(isSesssionLogin);

        if (textOAuthId!= null) textOAuthId.setEnabled(isOAuthLogin);
        if (textOAuthSecret!= null) textOAuthSecret.setEnabled(isOAuthLogin);
        if (textOAuthEndpoint!= null) textOAuthEndpoint.setEnabled(isOAuthLogin);
    }

    public static boolean isNeeded(Controller controller) {
        return (!controller.loginIfSessionExists() || controller.getEntityDescribes() == null || controller
                .getEntityDescribes().isEmpty());
    }
}
