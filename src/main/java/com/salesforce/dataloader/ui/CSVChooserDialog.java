/*
 * Copyright (c) 2012, salesforce.com, inc.
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;

public class CSVChooserDialog extends Dialog {
    private String message;
    private String input;
    private Controller controller;
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
    public CSVChooserDialog(Shell parent, int style) {
        // Let users override the default styles
        super(parent, style);
        setText(Labels.getString("CSVChooser.title")); //$NON-NLS-1$
        setMessage(Labels.getString("CSVChooser.message")); //$NON-NLS-1$
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
     * Gets the input
     *
     * @return String
     */
    public String getInput() {
        return input;
    }

    /**
     * Sets the input
     *
     * @param input
     *            the new input
     */
    public void setInput(String input) {
        this.input = input;
    }

    /**
     * Opens the dialog and returns the input
     *
     * @return String
     */
    public String open() {
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
        // Return the entered value, or null
        return input;
    }

    /**
     * Creates the dialog's contents
     *
     * @param shell
     *            the dialog window
     */
    private void createContents(final Shell shell) {

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
        label.setText(message);
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
        buttonSelect = new Button(restComp, SWT.PUSH);
        buttonSelect.setText(Labels.getString("CSVChooser.openCsv")); //$NON-NLS-1$
        buttonSelect.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // User has selected to open a single file
                FileDialog dlg = new FileDialog(shell, SWT.OPEN);
                String fn = dlg.open();
                if (fn != null) {
                    openViewer(fn);
                }

            }
        });

        //success
        Label labelSuccess = new Label(restComp, SWT.RIGHT);
        labelSuccess.setText(Labels.getString("CSVChooser.lastSuccess")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelSuccess.setLayoutData(data);
        buttonSuccess = new Button(restComp, SWT.PUSH);
        buttonSuccess.setText(Labels.getString("CSVChooser.openSuccess")); //$NON-NLS-1$
        buttonSuccess.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                openViewer(controller.getConfig().getString(Config.OUTPUT_SUCCESS));
            }
        });

        //success
        Label labelError = new Label(restComp, SWT.RIGHT);
        labelError.setText(Labels.getString("CSVChooser.lastError")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelError.setLayoutData(data);
        buttonError = new Button(restComp, SWT.PUSH);
        buttonError.setText(Labels.getString("CSVChooser.openError")); //$NON-NLS-1$
        buttonError.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                openViewer(controller.getConfig().getString(Config.OUTPUT_ERROR));
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
        Button ok = new Button(buttonComp, SWT.PUSH);
        ok.setText(Labels.getString("UI.close")); //$NON-NLS-1$
        ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {

                input = "OK"; //$NON-NLS-1$
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

    private void openViewer(String filename) {
        CSVViewerDialog dlg = new CSVViewerDialog(getParent(), controller);
        dlg.setNumberOfRows(Integer.parseInt(textRows.getText()));
        dlg.setFileName(filename);
        try {
            dlg.open();
        } catch (DataAccessObjectInitializationException e) {
            UIUtils.errorMessageBox(getParent(), e);
        }
    }
}