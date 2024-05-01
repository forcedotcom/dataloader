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

package com.salesforce.dataloader.ui.mapping;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.ui.Labels;
import com.salesforce.dataloader.ui.WizardDialog;
import com.salesforce.dataloader.ui.mapping.MappingDropAdapter.MAPPING_CHOICE;

public class MappingDropActionDialog extends WizardDialog {
    private Button replace;
    private Button add;
    private Button cancel;
    private Label label;
    private MappingDropAdapter dropAdapter;
    private String csvField, currentlyMappedSforceFields;

    /**
     * InputDialog constructor
     *
     * @param parent
     *            the parent
     */
    public MappingDropActionDialog(MappingDropAdapter dropAdapter, Controller controller,
            String csvField, String currentlyMappedSforceFields, String newSforceField) {
        super(dropAdapter.getMappingDialog().getParent(), controller);
        this.setText(Labels.getFormattedString(this.getClass().getSimpleName() + ".title",
                new String[] {csvField, newSforceField})); //$NON-NLS-1$
        this.dropAdapter = dropAdapter;
        this.csvField = csvField;
        this.currentlyMappedSforceFields = currentlyMappedSforceFields;
    }

    /**
     * Creates the dialog's contents
     *
     * @param shell
     *            the dialog window
     */
    protected void createContents(final Shell shell) {

        final int WIDTH_HINT = 500;
        GridLayout layout = new GridLayout(1, false);
        layout.verticalSpacing = 10;
        shell.setLayout(layout);

        label = new Label(shell, SWT.WRAP);
        label.setText(Labels.getFormattedString("MappingDropActionDialog.message", 
                new String[] {csvField, currentlyMappedSforceFields}));
        GridData labelData = new GridData(GridData.FILL_HORIZONTAL);
        labelData.widthHint = WIDTH_HINT;
        label.setLayoutData(labelData);

        //the bottom separator
        Label labelSeparatorBottom = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData sepData = new GridData(GridData.FILL_HORIZONTAL);
        labelSeparatorBottom.setLayoutData(sepData);

        //buttons
        Composite comp1 = new Composite(shell, SWT.NONE);
        comp1.setLayout(new GridLayout(3, false));
        GridData comp1Data = new GridData(GridData.FILL_HORIZONTAL);
        comp1Data.widthHint = WIDTH_HINT;
        comp1.setLayoutData(comp1Data);

        replace = new Button(comp1, SWT.PUSH | SWT.FLAT);
        replace.setText(Labels.getString("MappingDropActionDialog.replaceAction")); //$NON-NLS-1$
        replace.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                dropAdapter.performDropAction(MAPPING_CHOICE.REPLACE);
                // replace current mapping with new
                shell.close();
            }
        });
        GridData buttonData = new GridData();
        replace.setLayoutData(buttonData);
        
        add = new Button(comp1, SWT.PUSH | SWT.FLAT);
        add.setText(Labels.getString("MappingDropActionDialog.addAction")); //$NON-NLS-1$
        add.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                dropAdapter.performDropAction(MAPPING_CHOICE.ADD);
                // add new mapping
                shell.close();
            }
        });
        buttonData = new GridData();
        add.setLayoutData(buttonData);

        cancel = new Button(comp1, SWT.PUSH | SWT.FLAT);
        cancel.setText(Labels.getString("UI.cancel")); //$NON-NLS-1$
        cancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                dropAdapter.performDropAction(MAPPING_CHOICE.CANCEL);
                // cancel operation, keep current mappng as-is
                shell.close();
            }
        });
        buttonData = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
        buttonData.widthHint = 75;
        cancel.setLayoutData(buttonData);
        
        shell.setDefaultButton(replace);
    }
}
