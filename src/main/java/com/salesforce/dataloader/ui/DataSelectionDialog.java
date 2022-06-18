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
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.salesforce.dataloader.client.PartnerClient;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataReader;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import com.salesforce.dataloader.util.DAORowUtil;
import com.sforce.soap.partner.LimitInfo;
import com.sforce.ws.ConnectionException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class DataSelectionDialog extends Dialog {
    private final Logger logger = LogManager.getLogger(MappingPage.class);
    private String message;
    private boolean success;
    private Controller controller;
    private Button ok;
    private Label label;

    /**
     * InputDialog constructor
     *
     * @param parent
     *            the parent
     */
    public DataSelectionDialog(Shell parent, Controller controller) {
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
    public DataSelectionDialog(Shell parent, int style) {
        // Let users override the default styles
        super(parent, style);
        setText(Labels.getString("DataSelectionDialog.title")); //$NON-NLS-1$
        setMessage(Labels.getString("DataSelectionDialog.message")); //$NON-NLS-1$
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
        final Shell shell = new Shell(getParent(), getStyle());
        shell.setText(getText());
        shell.setImage(UIUtils.getImageRegistry().get("sfdc_icon")); //$NON-NLS-1$
        createContents(shell);
        shell.pack();
        shell.open();
        Display display = getParent().getDisplay();
        BusyIndicator.showWhile(display, new Thread() {
            @Override
            public void run() {
                try {
                    controller.setFieldTypes();
                    controller.setReferenceDescribes();

                    String daoPath = controller.getConfig().getString(Config.DAO_NAME);
                    File file = new File(daoPath);

                    if (!file.exists() || !file.canRead()) {
                        success = false;
                        ok.setEnabled(true);
                        label.setText(Labels.getString("DataSelectionDialog.errorRead")); //$NON-NLS-1$
                        shell.setText(Labels.getString("DataSelectionDialog.titleError"));
                        return;
                    }

                    try {
                        controller.createDao();
                    } catch (DataAccessObjectInitializationException e) {
                        success = false;
                        ok.setEnabled(true);
                        label.setText(Labels.getString("DataSelectionDialog.errorRead")); //$NON-NLS-1$
                        shell.setText(Labels.getString("DataSelectionDialog.titleError"));
                        return;
                    }
                    DataReader dataReader = (DataReader)controller.getDao();

                    List header = null;
                    int totalRows = 0;
                    try {
                        dataReader.checkConnection();
                        dataReader.open();

                        String error = DAORowUtil.validateColumns(dataReader);
                        if(error != null && error.length() != 0) {
                            int response = UIUtils.errorMessageBox(shell, error);
                            // in case user doesn't want to continue, treat this as an error
                            if(response != SWT.YES) {
                                success = false;
                                ok.setEnabled(true);
                                label.setText(Labels.getString("DataSelectionDialog.errorCSVFormat")); //$NON-NLS-1$
                                shell.setText(Labels.getString("DataSelectionDialog.titleError"));
                                logger.error(Labels.getString("DataSelectionDialog.errorCSVFormat"));
                                return;
                            }
                        }

                        totalRows = dataReader.getTotalRows();

                        if ((header = dataReader.getColumnNames())== null || header.size() == 0) {
                            success = false;
                            ok.setEnabled(true);
                            label.setText(Labels.getString("DataSelectionDialog.errorCSVFormat")); //$NON-NLS-1$
                            shell.setText(Labels.getString("DataSelectionDialog.titleError"));
                            logger.error(Labels.getString("DataSelectionDialog.errorCSVFormat"));
                            return;
                        }

                    } catch (DataAccessObjectException e) {
                        success = false;
                        ok.setEnabled(true);
                        label.setText(Labels.getString("DataSelectionDialog.errorCSVFormat") + "  " + e.getMessage()); //$NON-NLS-1$
                        logger.error(Labels.getString("DataSelectionDialog.errorCSVFormat"));
                        Point size = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                        label.setSize(shell.getClientArea().width, size.y);
                        shell.setText(Labels.getString("DataSelectionDialog.titleError"));
                        shell.pack();
                        shell.redraw();
                        return;
                    } finally {
                        dataReader.close();
                    }
                    success = true;
                    ok.setEnabled(true);
                    LimitInfo apiLimitInfo;
                    String apiLimitInfoStr = "";
                    PartnerClient partnerClient = controller.getPartnerClient();
                    if (partnerClient != null) {
                        apiLimitInfo = partnerClient.getAPILimitInfo();
                        if (apiLimitInfo != null) {
                            apiLimitInfoStr = "\n    "
                                    + Labels.getFormattedString("Operation.currentAPIUsage", apiLimitInfo.getCurrent())
                                    + "\n    "
                                    + Labels.getFormattedString("Operation.apiLimit", apiLimitInfo.getLimit()
                                    + "\n    "
                                    + Labels.getFormattedString("Operation.apiVersion", partnerClient.getAPIVersion()));
                            logger.debug(apiLimitInfoStr);
                            // Set the description
                       }
                    }
                    // Set the description
                    label.setText(Labels.getFormattedString(
                            "DataSelectionDialog.initSuccess", String.valueOf(totalRows))
                            + "\n\n    "
                            + Labels.getString("AdvancedSettingsDialog.batchSize")
                            + " "
                            + controller.getConfig().getString(Config.LOAD_BATCH_SIZE)
                            + "\n    "
                            + Labels.getString("AdvancedSettingsDialog.startRow")
                            + " "
                            + controller.getConfig().getString(Config.LOAD_ROW_TO_START_AT)
                            + apiLimitInfoStr); //$NON-NLS-1$
                    label.getParent().pack();
                } catch (ConnectionException ex) {
                    success = false;
                    ok.setEnabled(true);
                    label.setText(Labels.getString("DataSelectionDialog.errorEntity")); //$NON-NLS-1$
                    shell.setText(Labels.getString("DataSelectionDialog.titleError"));
                    return;
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
    private void createContents(final Shell shell) {

        GridLayout layout = new GridLayout(2, false);
        layout.verticalSpacing = 10;
        shell.setLayout(layout);

        label = new Label(shell, SWT.WRAP);
        label.setText(message);
        GridData labelData = new GridData();
        labelData.horizontalSpan = 2;
        labelData.widthHint = 400;
        label.setLayoutData(labelData);

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
