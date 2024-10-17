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
import com.salesforce.dataloader.controller.Controller;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

/**
 * Describe your class here.
 * 
 * @author Lexi Viripaeff
 * @since 6.0
 */
public class LoginPage extends OperationPage {

    private AuthenticationRunner authenticator;
    private OAuthLoginControl oauthControl;
    private UsernamePasswordLoginControl unamePwdLoginControl;
    private UsernamePasswordLoginControl sessionIdLoginControl;
    private Grid12 grid;
    private Composite control;
    private String nextPageName = DataSelectionPage.class.getSimpleName();

    public LoginPage(Controller controller) {
        super("LoginPage", controller); //$NON-NLS-1$ //$NON-NLS-2$
        setPageComplete();
    }

    @Override
    public void createControl(Composite parent) {
        getShell().setImage(UIUtils.getImageRegistry().get("sfdc_icon")); //$NON-NLS-1$

        AppConfig appConfig = controller.getAppConfig();
        control = new Composite(parent, SWT.FILL);
        grid = new Grid12(control, 40, false, true);
        authenticator = new AuthenticationRunner(getShell(), appConfig, controller);

        Button[] layouts = new Button[3];
        grid.createPadding(2);
        layouts[0] = grid.createButton(2, SWT.RADIO, Labels.getString("LoginPage.loginDefault"));
        layouts[1] = grid.createButton(4, SWT.RADIO, Labels.getString("LoginPage.loginStandard"));
        layouts[2] = grid.createButton(2, SWT.RADIO, Labels.getString("LoginPage.loginAdvanced"));
        grid.createPadding(2);

        oauthControl = new OAuthLoginControl(control, SWT.FILL, this, authenticator);
        oauthControl.setLayoutData(grid.createCell(12));
        unamePwdLoginControl = new UsernamePasswordLoginControl(control, SWT.FILL, this, authenticator, false);
        unamePwdLoginControl.setLayoutData(grid.createCell(10));
        sessionIdLoginControl = new UsernamePasswordLoginControl(control, SWT.FILL, this, authenticator, true);
        sessionIdLoginControl.setLayoutData(grid.createCell(12));

        setControl(control);

        layouts[0].addListener(SWT.Selection, this::selectOAuth);
        layouts[1].addListener(SWT.Selection, this::selectUnamePwd);
        layouts[2].addListener(SWT.Selection, this::selectSessionId);

        //turn off oauth options if no configured environments found
        if (appConfig.getStrings(AppConfig.PROP_SERVER_ENVIRONMENTS).size() > 0) {
            layouts[0].setSelection(true);
            selectOAuth(null);
        } else {
            grid.hide(layouts[0]);
            layouts[1].setSelection(true);
            selectUnamePwd(null);
        }
        if (!appConfig.getBoolean(AppConfig.PROP_SFDC_INTERNAL)){
            grid.hide(layouts[2]);
            if (!layouts[0].getVisible()){
                //no options other than standard so don't present them
                grid.hide(layouts[1]);
            }
        }
        setupPage();
    }
    
    public void setNextPageName(String name) {
        this.nextPageName = name;
    }
    
    /**
     * Loads DataSelectionPage. To be overridden by subclasses for special behavior.
     *
     * @param controller
     */
    private void loadDataSelectionPage(Controller controller) {
        ((OperationPage)getWizard().getPage(this.nextPageName)).setupPage(); //$NON-NLS-1$
        if (canFlipToNextPage()) {
            IWizardPage page = getNextPage();
            if (page != null) {
                getContainer().showPage(page);
            }
            // following is a hack to correctly resize the list showing
            // sObjects in DataSelectionPage on Mac.
            // --- Start ---
            Rectangle shellBounds = this.getShell().getBounds();
            shellBounds.height++;
            this.getShell().setBounds(shellBounds);
            shellBounds.height--;
            this.getShell().setBounds(shellBounds);
            // --- End ----
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

    public static boolean isNeeded(Controller controller) {
        return (!controller.isLoggedIn());
    }

    private void selectSessionId(Event event) {
        show(sessionIdLoginControl);
    }

    private void selectUnamePwd(Event event) {
        show(unamePwdLoginControl);
    }

    private void selectOAuth(Event event) {
        show(oauthControl);
    }

    private void show(Composite showControl) {
        grid.hide(oauthControl);
        grid.hide(unamePwdLoginControl);
        grid.hide(sessionIdLoginControl);
        grid.show(showControl);

        control.layout(false);
    }
    
    @Override
    public void setPageComplete() {
        setPageComplete(controller.isLoggedIn());
        if (controller.isLoggedIn()){
            loadDataSelectionPage(controller);
        }
    }

    @Override
    protected boolean setupPagePostLogin() {
        return true;
    }

    @Override
    protected String getConfigInfo() {
        return "";
    }
}
