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
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.springframework.util.StringUtils;

public class CSVChooserDialog extends WizardDialog {
    private Text textRows;
    private Button buttonSelect;
    private Button buttonSuccess;
    private Button buttonError;

    /**
     * InputDialog constructor
     *
     * @param parent
     *            the parent
     */
    public CSVChooserDialog(Shell parent, Controller controller) {
        // Pass the default styles here
        super(parent, controller);
    }

    /**
     * Creates the dialog's contents
     *
     * @param shell
     *            the dialog window
     */
    protected void createContents(final Shell shell) {
        GridData data;
        GridLayout layout = new GridLayout(1, false);
        layout.verticalSpacing = 10;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        shell.setLayout(layout);

        data = new GridData(GridData.FILL_HORIZONTAL);
        data.heightHint = 50;
        data.widthHint = 400;

        Composite topComp = new Composite(shell, SWT.NONE);
        layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        topComp.setLayout(layout);
        topComp.setLayoutData(data);

        Label blank = new Label(topComp, SWT.NONE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.heightHint = 10;
        blank.setLayoutData(data);
        blank.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

        // Show the message
        Label label = new Label(topComp, SWT.NONE);
        label.setText(getMessage());
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.heightHint = 30;
        data.widthHint = 370;

        Font f = label.getFont();
        FontData[] farr = f.getFontData();
        FontData fd = farr[0];
        fd.setStyle(SWT.BOLD);
        label.setFont(new Font(Display.getCurrent(), fd));

        label.setLayoutData(data);
        label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

        Label labelSeparator = new Label(topComp, SWT.SEPARATOR | SWT.HORIZONTAL);
        data = new GridData(GridData.FILL_HORIZONTAL);
        labelSeparator.setLayoutData(data);

        Composite restComp = new Composite(shell, SWT.NONE);
        data = new GridData(GridData.FILL_BOTH);
        restComp.setLayoutData(data);
        layout = new GridLayout(2, false);
        layout.verticalSpacing = 15;
        restComp.setLayout(layout);

        //Csv size
        Label labelRows = new Label(restComp, SWT.RIGHT);
        labelRows.setText(Labels.getString("CSVChooser.numberRows")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelRows.setLayoutData(data);

        textRows = new Text(restComp, SWT.BORDER);
        textRows.setText("1000"); //$NON-NLS-1$
        textRows.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent event) {
                event.doit = Character.isISOControl(event.character) || Character.isDigit(event.character);
            }
        });

        data = new GridData();
        data.widthHint = 90;
        textRows.setLayoutData(data);

        //label Select file
        Label labelSelect = new Label(restComp, SWT.RIGHT);
        labelSelect.setText(Labels.getString("CSVChooser.selectOpen")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelSelect.setLayoutData(data);
        buttonSelect = new Button(restComp, SWT.PUSH | SWT.FLAT);
        buttonSelect.setText(Labels.getString("CSVChooser.openCsv")); //$NON-NLS-1$
        buttonSelect.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // User has selected to open a single file
                FileDialog dlg = new FileDialog(shell, SWT.OPEN);
                String fn = dlg.open();
                if (fn != null) {
                    openViewer(fn, false, false);
                }
            }
        });

        //success
        Label labelSuccess = new Label(restComp, SWT.RIGHT);
        labelSuccess.setText(Labels.getString("CSVChooser.lastSuccess")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelSuccess.setLayoutData(data);
        buttonSuccess = new Button(restComp, SWT.PUSH | SWT.FLAT);
        buttonSuccess.setText(Labels.getString("CSVChooser.openSuccess")); //$NON-NLS-1$
        buttonSuccess.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                String successFilePath = getController().getConfig().getString(Config.OUTPUT_SUCCESS);
                if(StringUtils.hasText(successFilePath)) {
                    openViewer(successFilePath, true, false);
                } else {
                    UIUtils.infoMessageBox(shell, Messages.getString("CSVChooser.noSucessOrErrorFile"));
                }
            }
        });

        //success
        Label labelError = new Label(restComp, SWT.RIGHT);
        labelError.setText(Labels.getString("CSVChooser.lastError")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelError.setLayoutData(data);
        buttonError = new Button(restComp, SWT.PUSH | SWT.FLAT);
        buttonError.setText(Labels.getString("CSVChooser.openError")); //$NON-NLS-1$
        buttonError.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                String errorFilePath = getController().getConfig().getString(Config.OUTPUT_ERROR);
                if(StringUtils.hasText(errorFilePath)) {
                    openViewer(errorFilePath, true, false);
                } else {
                    UIUtils.infoMessageBox(shell, Messages.getString("CSVChooser.noSucessOrErrorFile"));
                }
            }
        });

        //the bottow separator
        Label labelSeparatorBottom = new Label(restComp, SWT.SEPARATOR | SWT.HORIZONTAL);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        labelSeparatorBottom.setLayoutData(data);

        //ok cancel buttons
        new Label(restComp, SWT.NONE);

        Composite buttonComp = new Composite(restComp, SWT.NONE);
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        buttonComp.setLayoutData(data);
        buttonComp.setLayout(new GridLayout(2, false));

        // Create the OK button and add a handler
        // so that pressing it will set input
        // to the entered value
        Button ok = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
        ok.setText(Labels.getString("UI.close")); //$NON-NLS-1$
        ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                shell.close();
            }
        });
        data = new GridData();
        data.widthHint = 75;
        ok.setLayoutData(data);


        // Set the OK button as the default, so
        // user can type input and press Enter
        // to dismiss
        shell.setDefaultButton(ok);
    }

    private void openViewer(String filename, boolean ignoreDelimiterConfig, boolean isQueryOperationResult) {
        // 
        CSVViewerDialog dlg = new CSVViewerDialog(getParent(), getController(), ignoreDelimiterConfig, isQueryOperationResult);
        dlg.setNumberOfRows(Integer.parseInt(textRows.getText()));
        dlg.setFileName(filename);
        try {
            dlg.open();
        } catch (DataAccessObjectInitializationException e) {
            UIUtils.errorMessageBox(getParent(), e);
        }
    }
}