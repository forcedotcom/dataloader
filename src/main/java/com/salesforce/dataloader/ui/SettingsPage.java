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
import com.salesforce.dataloader.controller.Controller;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

/**
 * Describe your class here.
 * 
 * @author Lexi Viripaeff
 * @since 6.0
 */
public class SettingsPage extends WizardPage {

    private final Controller controller;

    private AuthenticationRunner authenticator;
    private OAuthLoginDefaultControl defaultControl;
    private UsernamePasswordLoginStandardControl standardControl;
    private UsernamePasswordLoginAdvancedControl advancedControl;
    private Grid12 grid;
    private Composite control;

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
        control = new Composite(parent, SWT.FILL);
        grid = new Grid12(control, 40);
        authenticator = new AuthenticationRunner(getShell(), config, controller, this::authenticationCompleted);

        Button[] layouts = new Button[3];
        grid.createPadding(2);
        layouts[0] = grid.createButton(2, SWT.RADIO, Labels.getString("SettingsPage.loginDefault"));
        layouts[1] = grid.createButton(4, SWT.RADIO, Labels.getString("SettingsPage.loginStandard"));
        layouts[2] = grid.createButton(2, SWT.RADIO, Labels.getString("SettingsPage.loginAdvanced"));
        grid.createPadding(2);

        defaultControl = new OAuthLoginDefaultControl(control, SWT.FILL, authenticator);
        defaultControl.setLayoutData(grid.createCell(12));
        standardControl = new UsernamePasswordLoginStandardControl(control, SWT.FILL, authenticator);
        standardControl.setLayoutData(grid.createCell(12));
        advancedControl = new UsernamePasswordLoginAdvancedControl(control, SWT.FILL, authenticator);
        advancedControl.setLayoutData(grid.createCell(12));

        setControl(control);

        layouts[0].addListener(SWT.Selection, this::selectDefault);
        layouts[1].addListener(SWT.Selection, this::selectStandard);
        layouts[2].addListener(SWT.Selection, this::selectAdvanced);

        //turn off oauth options if no configured environments found
        if (config.getStrings(Config.OAUTH_ENVIRONMENTS).size() > 0) {
            layouts[0].setSelection(true);
            selectDefault(null);
        } else {
            grid.hide(layouts[0]);
            layouts[1].setSelection(true);
            selectStandard(null);
        }
        if (!config.getBoolean(Config.SFDC_INTERNAL)){
            grid.hide(layouts[2]);
            if (!layouts[0].getVisible()){
                //no options other than standard so don't present them
                grid.hide(layouts[1]);
            }
        }
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
            setPageComplete(false);
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
        return (!controller.loginIfSessionExists() || controller.getEntityDescribes() == null || controller
                .getEntityDescribes().isEmpty());
    }

    private void authenticationCompleted(Boolean success){
        if (success){
            loadDataSelectionPage(controller);
        }
        else{
            setPageComplete(false);
        }
    }

    private void selectAdvanced(Event event) {
        show(advancedControl);
    }

    private void selectStandard(Event event) {
        show(standardControl);
    }

    private void selectDefault(Event event) {
        show(defaultControl);
    }

    private void show(Composite showControl) {
        grid.hide(defaultControl);
        grid.hide(standardControl);
        grid.hide(advancedControl);
        grid.show(showControl);

        control.layout(false);
    }
}
