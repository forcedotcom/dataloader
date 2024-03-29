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
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.ui.BaseDialog;
import com.salesforce.dataloader.ui.Labels;
import com.sforce.ws.ConnectionException;

public class ExtractionDataSelectionDialog extends BaseDialog {
    private boolean success;
    private Button ok;
    private Label label;

    /**
     * InputDialog constructor
     *
     * @param parent
     *            the parent
     */
    public ExtractionDataSelectionDialog(Shell parent, Controller controller) {
        // Pass the default styles here
        super(parent, controller);
    }

    /**
     * Opens the dialog and returns the input
     *
     * @return String
     */
    public boolean open() {
        // Create the dialog window
        Shell shell = super.openAndGetShell();
        Display display = shell.getDisplay();
        BusyIndicator.showWhile(display, new Thread() {
            @Override
            public void run() {
                try {
                    getController().setFieldTypes();
                    getController().setReferenceDescribes();
                    success = true;
                    ok.setEnabled(true);
                    label.setText(Labels.getString("ExtractionDataSelectionDialog.success")); //$NON-NLS-1$
                    label.getParent().pack();
                    ;
                } catch (ConnectionException ex) {
                    success = false;
                    ok.setEnabled(true);
                    label.setText(Labels.getString("ExtractionDataSelectionDialog.errorValidating")); //$NON-NLS-1$
                }
            }
        });

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        // Return the sucess
        return success;
    }

    /**
     * Creates the dialog's contents
     *
     * @param shell
     *            the dialog window
     */
    protected void createContents(final Shell shell) {
        GridData data;

        GridLayout layout = new GridLayout(2, false);
        layout.verticalSpacing = 10;
        shell.setLayout(layout);

        label = new Label(shell, SWT.NONE);
        label.setText(getMessage());
        data = new GridData();
        data.horizontalSpan = 2;
        data.widthHint = 250;
        label.setLayoutData(data);

        //the bottow separator
        Label labelSeparatorBottom = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        labelSeparatorBottom.setLayoutData(data);

        //ok cancel buttons
        new Label(shell, SWT.NONE);

        ok = new Button(shell, SWT.PUSH | SWT.FLAT);
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
        ok.setEnabled(false);

        shell.setDefaultButton(ok);
    }
}