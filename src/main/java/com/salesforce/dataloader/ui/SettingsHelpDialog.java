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

import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.config.ConfigPropertyMetadata;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import com.salesforce.dataloader.exception.ParameterLoadException;

public class SettingsHelpDialog extends BaseDialog {
    private Shell dialogShell;

    public SettingsHelpDialog(Shell parent, Controller controller) {
        super(parent, controller);
    }

    @Override
    protected void createContents(Shell shell) {
        // Create the dialog window
        dialogShell = shell;
        GridLayout layout = new GridLayout(1, false);
        dialogShell.setLayout(layout);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        int widthHint = 600;
        int heightHint = 600;
        try {
            widthHint = AppConfig.getCurrentConfig().getInt(AppConfig.PROP_WIZARD_WIDTH);
            heightHint = AppConfig.getCurrentConfig().getInt(AppConfig.PROP_WIZARD_HEIGHT);
        } catch (ParameterLoadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        data.widthHint = widthHint;
        data.heightHint = heightHint;
        dialogShell.setLayoutData(data);
        
        //display salesforce logo
        Label titleImage = new Label(shell, SWT.CENTER);
        Color background = JFaceColors.getBannerBackground(shell.getDisplay());
        shell.setBackground(background);
        titleImage.setBackground(background);
        titleImage.setImage(UIUtils.getImageRegistry().get("logo"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        titleImage.setLayoutData(data);
        
        // Create the web browser
        Browser browser = new Browser(dialogShell, SWT.NONE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        if (heightHint > 100) {
            data.heightHint = heightHint - 100;
        }
        browser.setLayoutData(data);
        browser.setText(Labels.getString("SettingsHelpDialog.settingsHelp"));
       
        String fullPathOfPropsCSV = ConfigPropertyMetadata.getFullPathToPropsFile(getController().getAppConfig());
        Link showPropertiesCSVLink = new Link(dialogShell, SWT.NONE);
        showPropertiesCSVLink.setText(Labels.getFormattedString("SettingsHelpDialog.getPropsCSV", fullPathOfPropsCSV));
        showPropertiesCSVLink.addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent arg0) {                
            }
            @Override
            public void mouseDown(MouseEvent arg0) {
                CSVViewerDialog dlg = new CSVViewerDialog(getParent(), getController(), true, true);
                dlg.setNumberOfRows(200);
                dlg.setFileName(fullPathOfPropsCSV);
                try {
                    dlg.open();
                } catch (DataAccessObjectInitializationException e) {
                    UIUtils.errorMessageBox(getParent(), e);
                }
            }
            @Override
            public void mouseUp(MouseEvent arg0) {
            }
        });
        
        // vertical spacer
        new Label(dialogShell, SWT.NONE);

        Button ok = new Button(dialogShell, SWT.PUSH | SWT.FLAT);
        ok.setText(Labels.getString("UI.ok")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        data.widthHint = 75;
        ok.setLayoutData(data);
        ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                dialogShell.close();
            }
        });
    }
}
