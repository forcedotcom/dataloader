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

import java.io.File;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataAccessObjectFactory;
import com.salesforce.dataloader.dao.DataReader;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.MappingInitializationException;
import com.salesforce.dataloader.util.DAORowUtil;

public class DataSelectionDialog extends BaseDialog {
    private Button ok;
    private Label label;
    private ContentLimitLink contentNoteLimitLink;
    private String delimiterList = "";
    private String daoNameStr;
    private String sObjectName;

    /**
     * InputDialog constructor
     *
     * @param parent
     *            the parent
     */
    public DataSelectionDialog(Shell parent, Controller controller, String daoNameStr, String sObjectName) {
        super(parent, controller);
        this.daoNameStr = daoNameStr;
        this.sObjectName = sObjectName;
        if (controller.getConfig().getBoolean(Config.CSV_DELIMITER_COMMA)) {
            this.delimiterList = " ','";
        }
        if (controller.getConfig().getBoolean(Config.CSV_DELIMITER_TAB)) {
            this.delimiterList += " '<tab>'";
        }
        if (controller.getConfig().getBoolean(Config.CSV_DELIMITER_OTHER)) {
            String otherDelimiters = controller.getConfig().getString(Config.CSV_DELIMITER_OTHER_VALUE);
            for (char c : otherDelimiters.toCharArray()) {
                this.delimiterList += " '" + c + "'";
            }
        }
    }
    
    private void handleCSVReadError(Shell shell, String errorText) {
        success = false;
        ok.setEnabled(true);
        Point size = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        label.setSize(shell.getClientArea().width, size.y);
        label.setText(errorText); //$NON-NLS-1$ 
        logger.error(errorText);
        
        shell.setText(Labels.getString("DataSelectionDialog.titleError"));
        shell.pack();
        shell.redraw();
    }
    
    // called from BaseDialog.open()
    protected void processingWithBusyIndicator(Shell shell) {
        try {
            getController().initializeOperation(
                    DataAccessObjectFactory.CSV_READ_TYPE,
                    daoNameStr,
                    sObjectName);
        } catch (MappingInitializationException e) {
            handleCSVReadError(shell, 
                    Labels.getString("DataSelectionDialog.errorRead")
                    + Labels.getFormattedString("DataSelectionDialog.errorReadExceptionDetails", 
                            e.getMessage())
                );
            return;
        }

        String daoPath = getController().getConfig().getString(Config.DAO_NAME);
        File file = new File(daoPath);

        if (!file.exists() || !file.canRead()) {
            handleCSVReadError(shell, 
                    Labels.getString("DataSelectionDialog.errorRead")
                    + Labels.getString("DataSelectionDialog.errorReadPermissionDetails")
                );
            return;
        }
        DataReader dataReader = (DataReader)getController().getDao();

        List<String> header = null;
        int totalRows = 0;
        try {
            dataReader.checkConnection();
            dataReader.open();

            String error = DAORowUtil.validateColumns(dataReader);
            if(error != null && error.length() != 0) {
                int response = UIUtils.errorMessageBox(shell, error);
                // in case user doesn't want to continue, treat this as an error
                if(response != SWT.YES) {
                    handleCSVReadError(shell, 
                            Labels.getString("DataSelectionDialog.errorCSVFormat")
                            + Labels.getFormattedString("DataSelectionDialog.errorCSVDetails",
                                    delimiterList));
                    return;
                }
            }

            totalRows = dataReader.getTotalRows();

            if ((header = dataReader.getColumnNames())== null || header.size() == 0) {
                handleCSVReadError(shell, 
                        Labels.getString("DataSelectionDialog.errorCSVFormat")
                        + Labels.getFormattedString("DataSelectionDialog.errorCSVDetails",
                                delimiterList));
                return;
            }

        } catch (DataAccessObjectException e) {
            handleCSVReadError(shell, 
                    Labels.getString("DataSelectionDialog.errorCSVFormat")
                    + Labels.getFormattedString("DataSelectionDialog.errorCSVDetails",
                            delimiterList));
            return;
        } finally {
            dataReader.close();
        }
        success = true;
        ok.setEnabled(true);
        String apiInfoStr = getController().getAPIInfo();
        
        // Set the description
        label.setText(Labels.getFormattedString(
                "DataSelectionDialog.initSuccess", String.valueOf(totalRows))
                + "\n\n"
                + Labels.getString("AdvancedSettingsDialog.importBatchSize")
                + " "
                + getController().getConfig().getString(String.valueOf(getController().getConfig().getImportBatchSize()))
                + "\n"
                + Labels.getString("AdvancedSettingsDialog.startRow")
                + " "
                + getController().getConfig().getString(Config.LOAD_ROW_TO_START_AT)
                + "\n"
                + apiInfoStr
            ); //$NON-NLS-1$
        
         label.getParent().pack();
    }

    /**
     * Creates the dialog's contents
     *
     * @param shell
     *            the dialog window
     */
    protected void createContents(final Shell shell) {

        GridLayout layout = new GridLayout(2, false);
        layout.verticalSpacing = 10;
        shell.setLayout(layout);

        label = new Label(shell, SWT.WRAP);
        label.setText(getMessage());
        GridData labelData = new GridData();
        labelData.horizontalSpan = 2;
        labelData.widthHint = 400;
        label.setLayoutData(labelData);
        
        contentNoteLimitLink = new ContentLimitLink(shell, SWT.WRAP, getController());
        GridData linkData = new GridData();
        linkData.horizontalSpan = 2;
        linkData.widthHint = 400;
        contentNoteLimitLink.setLayoutData(linkData);

        //the bottom separator
        Label labelSeparatorBottom = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData sepData = new GridData(GridData.FILL_HORIZONTAL);
        sepData.horizontalSpan = 2;
        labelSeparatorBottom.setLayoutData(sepData);

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
        GridData buttonData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        buttonData.widthHint = 75;
        ok.setLayoutData(buttonData);
        ok.setEnabled(false);

        shell.setDefaultButton(ok);
    }
}
