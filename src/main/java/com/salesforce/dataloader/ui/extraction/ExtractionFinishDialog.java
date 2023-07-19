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

package com.salesforce.dataloader.ui.extraction;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import com.salesforce.dataloader.ui.*;

public class ExtractionFinishDialog extends Dialog {
    private String message;
    private Label label;
    private Controller controller;
    private Button ok;

    /**
     * InputDialog constructor
     *
     * @param parent
     *            the parent
     */
    public ExtractionFinishDialog(Shell parent, Controller controller) {
        this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
        this.controller = controller;
    }

    /**
     * InputDialog constructor
     *
     * @param parent
     *            the parent
     * @param style
     *            the style
     */
    public ExtractionFinishDialog(Shell parent, int style) {
        // Let users override the default styles
        super(parent, style);
        setText(Labels.getString("ExtractionFinishDialog.title"));   //$NON-NLS-1$
    }

    /**
     * Gets the message
     *
     * @return String
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message
     *
     * @param message
     *            the new message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Opens the dialog and returns the input
     *
     * @return String
     */
    public boolean open() {
        // Create the dialog window
        Shell shell = new Shell(getParent(), getStyle());
        shell.setText(getText());
        shell.setImage(UIUtils.getImageRegistry().get("sfdc_icon")); //$NON-NLS-1$
        createContents(shell);
        shell.pack();
        shell.open();
        Display display = getParent().getDisplay();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        // Return the sucess
        return true;
    }

    /**
     * Creates the dialog's contents
     *
     * @param shell
     *            the dialog window
     */
    private void createContents(final Shell shell) {

        GridData data;

        GridLayout layout = new GridLayout(2, false);
        layout.verticalSpacing = 10;
        layout.marginHeight = 20;
        shell.setLayout(layout);

        Label labelInfo = new Label(shell, SWT.NONE);
        labelInfo.setImage(UIUtils.getImageRegistry().get("info")); //$NON-NLS-1$
        data = new GridData();
        labelInfo.setLayoutData(data);

        label = new Label(shell, SWT.NONE);
        label.setText(message);
        data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
        label.setLayoutData(data);

        //the bottow separator
        Label labelSeparatorBottom = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        data.widthHint = 350;
        labelSeparatorBottom.setLayoutData(data);

        Composite buttonComp = new Composite(shell, SWT.NONE);
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        data.horizontalSpan = 2;
        buttonComp.setLayoutData(data);

        // error status output is optional
        boolean enableStatusOutput = controller.getConfig().getBoolean(Config.ENABLE_EXTRACT_STATUS_OUTPUT);
        if (enableStatusOutput) {
            layout = new GridLayout(3, false);
        } else {
            layout = new GridLayout(2, false);
        }
        buttonComp.setLayout(layout);

        Button viewExtraction = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        viewExtraction.setLayoutData(data);
        viewExtraction.setText(Labels.getString("ExtractionFinishDialog.viewExtraction"));  //$NON-NLS-1$
        viewExtraction.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openViewer(controller.getConfig().getString(Config.DAO_NAME));
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });

        // error status output is optional
        if (enableStatusOutput) {
            Button viewErrors = new Button(buttonComp, SWT.PUSH | SWT.FLAT | SWT.FLAT);
            data = new GridData(GridData.HORIZONTAL_ALIGN_END);
            data.widthHint = 75;
            viewErrors.setLayoutData(data);
            viewErrors.setText(Labels.getString("LoadFinishDialog.viewErrors")); //$NON-NLS-1$
            viewErrors.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    openViewer(controller.getConfig().getString(Config.OUTPUT_ERROR));
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {}
            });
        }

        ok = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
        ok.setText(Labels.getString("UI.ok")); //$NON-NLS-1$
        ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                shell.close();
            }
        });
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        data.widthHint = 75;
        ok.setLayoutData(data);


        shell.setDefaultButton(ok);
    }

    private void openViewer(String filename) {
        CSVViewerDialog dlg = new CSVViewerDialog(getParent(), controller, false, true);
        dlg.setNumberOfRows(200000);
        dlg.setFileName(filename);
        try {
            dlg.open();
        } catch (DataAccessObjectInitializationException e) {
            UIUtils.errorMessageBox(getParent(), e);
        }
    }
}