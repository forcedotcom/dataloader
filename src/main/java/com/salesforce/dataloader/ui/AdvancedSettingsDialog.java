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
import com.salesforce.dataloader.config.LastRun;
import com.salesforce.dataloader.controller.Controller;
import com.sforce.soap.partner.Connector;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static com.salesforce.dataloader.ui.UIUtils.isValidHttpsUrl;

public class AdvancedSettingsDialog extends Dialog {
    private String message;
    private String input;
    private Controller controller;
    private Text textBatch;
    private Text textQueryBatch;
    private Text textSplitterValue;
    private Button buttonNulls;
    private Text textRule;
    private Text textEndpoint;
    private Button buttonCompression;
    private Button buttonResetUrl;
    private Text textTimeout;
    private Text textRowToStart;
    private Text textProxyHost;
    private Text textProxyPort;
    private Text textProxyNtlmDomain;
    private Text textProxyUsername;
    private Text textProxyPassword;
    private Text textTimezone;

    private final String defaultServer;

    private final Logger logger = Logger.getLogger(AdvancedSettingsDialog.class);
    private Button buttonHideWelcomeScreen;
    private Button buttonOutputExtractStatus;
    private Button buttonReadUtf8;
    private Button buttonWriteUtf8;
    private Button buttonEuroDates;
    private Button buttonTruncateFields;
    private Button buttonUseBulkApi;
    private Button buttonBulkApiSerialMode;
    private Button buttonBulkApiZipContent;
    private Button buttonCsvComma;
    private Button buttonCsvTab;
    private Button buttonCsvOther;
    private Button buttonBulkQueryPKChunking;
    private Text textBulkQueryChunkSize;

    /**
     * InputDialog constructor
     *
     * @param parent the parent
     */
    public AdvancedSettingsDialog(Shell parent, Controller controller) {
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
        setText(Labels.getString("AdvancedSettingsDialog.title")); //$NON-NLS-1$
        setMessage(Labels.getString("AdvancedSettingsDialog.message")); //$NON-NLS-1$
        this.controller = controller;

        URI uri;
        String server = "";
        try {
            uri = new URI(Connector.END_POINT);
            server = uri.getScheme() + "://" + uri.getHost(); //$NON-NLS-1$
        } catch (URISyntaxException e) {
            logger.error(e);
        }
        defaultServer = server;
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
     * @param message the new message
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
     * @param input the new input
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

    private final Map<Button, Boolean> oldBulkAPIDependencies = new HashMap<Button, Boolean>();

    private void initBulkApiSetting(boolean enabled) {
        setButtonEnabled(Config.BULK_API_SERIAL_MODE, buttonBulkApiSerialMode, enabled);
        setButtonEnabled(Config.BULK_API_ZIP_CONTENT, buttonBulkApiZipContent, enabled);
        setButtonEnabled(Config.INSERT_NULLS, buttonNulls, !enabled);
        setButtonEnabled(Config.TRUNCATE_FIELDS, buttonTruncateFields, !enabled);
    }

    private void setButtonEnabled(String configKey, Button b, boolean enabled) {
        Boolean previousValue = oldBulkAPIDependencies.put(b, b.getSelection());
        b.setSelection(enabled ? (previousValue != null ? previousValue : this.controller.getConfig().getBoolean(
                configKey)) : false);
        b.setEnabled(enabled);
    }

    /**
     * Creates the dialog's contents
     *
     * @param shell the dialog window
     */
    private void createContents(final Shell shell) {

        final Config config = controller.getConfig();

        // Create the ScrolledComposite to scroll horizontally and vertically
        ScrolledComposite sc = new ScrolledComposite(shell, SWT.H_SCROLL | SWT.V_SCROLL);

        // Create the parent Composite container for the three child containers
        Composite container = new Composite(sc, SWT.NONE);
        GridLayout containerLayout = new GridLayout(1, false);
        container.setLayout(containerLayout);
        shell.setLayout(new FillLayout());

        GridData data;
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.heightHint = 50;
        data.widthHint = 400;

        // START TOP COMPONENT

        Composite topComp = new Composite(container, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
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

        // END TOP COMPONENT

        // START MIDDLE COMPONENT

        Composite restComp = new Composite(container, SWT.NONE);
        data = new GridData(GridData.FILL_BOTH);
        restComp.setLayoutData(data);
        layout = new GridLayout(2, false);
        layout.verticalSpacing = 10;
        restComp.setLayout(layout);

        // Hide welecome screen
        Label labelHideWelcomeScreen = new Label(restComp, SWT.RIGHT);
        labelHideWelcomeScreen.setText(Labels.getString("AdvancedSettingsDialog.hideWelcomeScreen")); //$NON-NLS-1$
        labelHideWelcomeScreen.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        buttonHideWelcomeScreen = new Button(restComp, SWT.CHECK);
        buttonHideWelcomeScreen.setSelection(config.getBoolean(Config.HIDE_WELCOME_SCREEN));

        //batch size
        Label labelBatch = new Label(restComp, SWT.RIGHT);
        labelBatch.setText(Labels.getString("AdvancedSettingsDialog.batchSize")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelBatch.setLayoutData(data);

        textBatch = new Text(restComp, SWT.BORDER);
        textBatch.setText(config.getString(Config.LOAD_BATCH_SIZE));
        textBatch.setTextLimit(8);
        textBatch.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent event) {
                event.doit = Character.isISOControl(event.character) || Character.isDigit(event.character);
            }
        });
        data = new GridData();
        data.widthHint = 50;
        textBatch.setLayoutData(data);

        //insert Nulls
        Label labelNulls = new Label(restComp, SWT.RIGHT);
        labelNulls.setText(Labels.getString("AdvancedSettingsDialog.insertNulls")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelNulls.setLayoutData(data);
        buttonNulls = new Button(restComp, SWT.CHECK);
        buttonNulls.setSelection(config.getBoolean(Config.INSERT_NULLS));

        //assignment rules
        Label labelRule = new Label(restComp, SWT.RIGHT);
        labelRule.setText(Labels.getString("AdvancedSettingsDialog.assignmentRule")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelRule.setLayoutData(data);

        textRule = new Text(restComp, SWT.BORDER);
        textRule.setTextLimit(18);
        data = new GridData();
        data.widthHint = 115;
        textRule.setLayoutData(data);
        textRule.setText(config.getString(Config.ASSIGNMENT_RULE));

        //endpoint
        Label labelEndpoint = new Label(restComp, SWT.RIGHT);
        labelEndpoint.setText(Labels.getString("AdvancedSettingsDialog.serverURL")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelEndpoint.setLayoutData(data);

        textEndpoint = new Text(restComp, SWT.BORDER);
        data = new GridData();
        data.widthHint = 250;
        textEndpoint.setLayoutData(data);
        String endpoint = config.getString(Config.ENDPOINT);
        if ("".equals(endpoint)) { //$NON-NLS-1$
            endpoint = defaultServer;
        }
        textEndpoint.setText(endpoint);

        //reset url on login
        Label labelResetUrl = new Label(restComp, SWT.RIGHT);
        labelResetUrl.setText(Labels.getString("AdvancedSettingsDialog.resetUrlOnLogin")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelResetUrl.setLayoutData(data);
        buttonResetUrl = new Button(restComp, SWT.CHECK);
        buttonResetUrl.setSelection(config.getBoolean(Config.RESET_URL_ON_LOGIN));

        //insert compression
        Label labelCompression = new Label(restComp, SWT.RIGHT);
        labelCompression.setText(Labels.getString("AdvancedSettingsDialog.compression")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelCompression.setLayoutData(data);
        buttonCompression = new Button(restComp, SWT.CHECK);
        buttonCompression.setSelection(config.getBoolean(Config.NO_COMPRESSION));

        //timeout size
        Label labelTimeout = new Label(restComp, SWT.RIGHT);
        labelTimeout.setText(Labels.getString("AdvancedSettingsDialog.timeout")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelTimeout.setLayoutData(data);

        textTimeout = new Text(restComp, SWT.BORDER);
        textTimeout.setTextLimit(4);
        textTimeout.setText(config.getString(Config.TIMEOUT_SECS));
        textTimeout.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent event) {
                event.doit = Character.isISOControl(event.character) || Character.isDigit(event.character);
            }
        });
        data = new GridData();
        data.widthHint = 30;
        textTimeout.setLayoutData(data);

        //extraction batch size
        Label labelQueryBatch = new Label(restComp, SWT.RIGHT);
        labelQueryBatch.setText(Labels.getString("ExtractionInputDialog.querySize")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelQueryBatch.setLayoutData(data);

        textQueryBatch = new Text(restComp, SWT.BORDER);
        textQueryBatch.setText(config.getString(Config.EXTRACT_REQUEST_SIZE));
        textQueryBatch.setTextLimit(4);
        textQueryBatch.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent event) {
                event.doit = Character.isISOControl(event.character) || Character.isDigit(event.character);
            }
        });
        data = new GridData();
        data.widthHint = 30;
        textQueryBatch.setLayoutData(data);

        //enable/disable output of success file for extracts
        Label labelOutputExtractStatus = new Label(restComp, SWT.RIGHT);
        labelOutputExtractStatus.setText(Labels.getString("AdvancedSettingsDialog.outputExtractStatus")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelOutputExtractStatus.setLayoutData(data);

        buttonOutputExtractStatus = new Button(restComp, SWT.CHECK);
        buttonOutputExtractStatus.setSelection(config.getBoolean(Config.ENABLE_EXTRACT_STATUS_OUTPUT));

        //utf-8 for loading
        Label labelReadUTF8 = new Label(restComp, SWT.RIGHT);
        labelReadUTF8.setText(Labels.getString("AdvancedSettingsDialog.readUTF8")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelReadUTF8.setLayoutData(data);

        buttonReadUtf8 = new Button(restComp, SWT.CHECK);
        buttonReadUtf8.setSelection(config.getBoolean(Config.READ_UTF8));

        //utf-8 for extraction
        Label labelWriteUTF8 = new Label(restComp, SWT.RIGHT);
        labelWriteUTF8.setText(Labels.getString("AdvancedSettingsDialog.writeUTF8")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelWriteUTF8.setLayoutData(data);

        buttonWriteUtf8 = new Button(restComp, SWT.CHECK);
        buttonWriteUtf8.setSelection(config.getBoolean(Config.WRITE_UTF8));

        //European Dates
        Label labelEuropeanDates = new Label(restComp, SWT.RIGHT);
        labelEuropeanDates.setText(Labels.getString("AdvancedSettingsDialog.useEuropeanDateFormat")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelEuropeanDates.setLayoutData(data);

        buttonEuroDates = new Button(restComp, SWT.CHECK);
        buttonEuroDates.setSelection(config.getBoolean(Config.EURO_DATES));

        //Field truncation
        Label labelTruncateFields = new Label(restComp, SWT.RIGHT);
        labelTruncateFields.setText(Labels.getString("AdvancedSettingsDialog.allowFieldTruncation"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelTruncateFields.setLayoutData(data);

        buttonTruncateFields = new Button(restComp, SWT.CHECK);
        buttonTruncateFields.setSelection(config.getBoolean(Config.TRUNCATE_FIELDS));

        Label labelCsvCommand = new Label(restComp, SWT.RIGHT);
        labelCsvCommand.setText(Labels.getString("AdvancedSettingsDialog.useCommaAsCsvDelimiter"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelCsvCommand.setLayoutData(data);
        buttonCsvComma = new Button(restComp, SWT.CHECK);
        buttonCsvComma.setSelection(config.getBoolean(Config.CSV_DELIMETER_COMMA));

        Label labelTabCommand = new Label(restComp, SWT.RIGHT);
        labelTabCommand.setText(Labels.getString("AdvancedSettingsDialog.useTabAsCsvDelimiter"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelTabCommand.setLayoutData(data);
        buttonCsvTab = new Button(restComp, SWT.CHECK);
        buttonCsvTab.setSelection(config.getBoolean(Config.CSV_DELIMETER_TAB));

        Label labelOtherCommand = new Label(restComp, SWT.RIGHT);
        labelOtherCommand.setText(Labels.getString("AdvancedSettingsDialog.useOtherAsCsvDelimiter"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelOtherCommand.setLayoutData(data);
        buttonCsvOther = new Button(restComp, SWT.CHECK);
        buttonCsvOther.setSelection(config.getBoolean(Config.CSV_DELIMETER_OTHER));

        Label labelOtherDelimiterValue = new Label(restComp, SWT.RIGHT);
        labelOtherDelimiterValue.setText(Labels.getString("AdvancedSettingsDialog.csvOtherDelimiterValue"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelOtherDelimiterValue.setLayoutData(data);
        textSplitterValue = new Text(restComp, SWT.BORDER);
        textSplitterValue.setText(config.getString(Config.CSV_DELIMETER_OTHER_VALUE));
        data = new GridData();
        data.widthHint = 25;
        textSplitterValue.setLayoutData(data);

        // Enable Bulk API Setting
        Label labelUseBulkApi = new Label(restComp, SWT.RIGHT);
        labelUseBulkApi.setText(Labels.getString("AdvancedSettingsDialog.useBulkApi")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelUseBulkApi.setLayoutData(data);

        boolean useBulkAPI = config.getBoolean(Config.BULK_API_ENABLED);
        buttonUseBulkApi = new Button(restComp, SWT.CHECK);
        buttonUseBulkApi.setSelection(useBulkAPI);
        buttonUseBulkApi.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                boolean enabled = buttonUseBulkApi.getSelection();
                // update batch size when this setting changes
                int newDefaultBatchSize = controller.getConfig().getDefaultBatchSize(enabled);
                logger.info("Setting batch size to " + newDefaultBatchSize);
                textBatch.setText(String.valueOf(newDefaultBatchSize));
                // make sure the appropriate check boxes are enabled or disabled
                initBulkApiSetting(enabled);
            }
        });

        // Bulk API serial concurrency mode setting
        Label labelBulkApiSerialMode = new Label(restComp, SWT.RIGHT);
        labelBulkApiSerialMode.setText(Labels.getString("AdvancedSettingsDialog.bulkApiSerialMode")); //$NON-NLS-1$
        labelBulkApiSerialMode.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        buttonBulkApiSerialMode = new Button(restComp, SWT.CHECK);
        buttonBulkApiSerialMode.setSelection(config.getBoolean(Config.BULK_API_SERIAL_MODE));
        buttonBulkApiSerialMode.setEnabled(useBulkAPI);

        // Bulk API serial concurrency mode setting
        Label labelBulkApiZipContent = new Label(restComp, SWT.RIGHT);
        labelBulkApiZipContent.setText(Labels.getString("AdvancedSettingsDialog.bulkApiZipContent")); //$NON-NLS-1$
        labelBulkApiZipContent.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        buttonBulkApiZipContent = new Button(restComp, SWT.CHECK);
        buttonBulkApiZipContent.setSelection(config.getBoolean(Config.BULK_API_SERIAL_MODE));
        buttonBulkApiZipContent.setEnabled(useBulkAPI);

        Label labelBulkQueryPKChunking = new Label(restComp, SWT.RIGHT);
        labelBulkQueryPKChunking.setText(Labels.getString("AdvancedSettingsDialog.bulkQueryPKChunking"));
        labelBulkQueryPKChunking.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        buttonBulkQueryPKChunking = new Button(restComp, SWT.CHECK);
        buttonBulkQueryPKChunking.setSelection(config.getBoolean(Config.BULK_QUERY_PK_CHUNKING));
        buttonBulkQueryPKChunking.setEnabled(useBulkAPI);

        Label labelBulkQueryChunkSize = new Label(restComp, SWT.RIGHT);
        labelBulkQueryChunkSize.setText(Labels.getString("AdvancedSettingsDialog.bulkQueryChunkSize"));
        labelBulkQueryChunkSize.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        textBulkQueryChunkSize = new Text(restComp, SWT.BORDER);
        textBulkQueryChunkSize.setText(config.getString(Config.BULK_QUERY_CHUNK_SIZE));
        textBulkQueryChunkSize.setTextLimit(6);
        textBulkQueryChunkSize.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent event) {
                event.doit = Character.isISOControl(event.character) || Character.isDigit(event.character);
            }
        });
        textBulkQueryChunkSize.setEnabled(useBulkAPI);

        // timezone
        textTimezone = createTextInput(restComp, "AdvancedSettingsDialog.timezone", Config.TIMEZONE, TimeZone.getDefault().getID(), 200);

        // proxy Host
        Label labelProxyHost = new Label(restComp, SWT.RIGHT);
        labelProxyHost.setText(Labels.getString("AdvancedSettingsDialog.proxyHost")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelProxyHost.setLayoutData(data);

        textProxyHost = new Text(restComp, SWT.BORDER);
        textProxyHost.setText(config.getString(Config.PROXY_HOST));
        data = new GridData();
        data.widthHint = 250;
        textProxyHost.setLayoutData(data);

        //Proxy Port
        Label labelProxyPort = new Label(restComp, SWT.RIGHT);
        labelProxyPort.setText(Labels.getString("AdvancedSettingsDialog.proxyPort")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelProxyPort.setLayoutData(data);

        textProxyPort = new Text(restComp, SWT.BORDER);
        textProxyPort.setText(config.getString(Config.PROXY_PORT));
        textProxyPort.setTextLimit(4);
        textProxyPort.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent event) {
                event.doit = Character.isISOControl(event.character) || Character.isDigit(event.character);
            }
        });
        data = new GridData();
        data.widthHint = 25;
        textProxyPort.setLayoutData(data);

        //Proxy Username
        Label labelProxyUsername = new Label(restComp, SWT.RIGHT);
        labelProxyUsername.setText(Labels.getString("AdvancedSettingsDialog.proxyUser")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelProxyUsername.setLayoutData(data);

        textProxyUsername = new Text(restComp, SWT.BORDER);
        textProxyUsername.setText(config.getString(Config.PROXY_USERNAME));
        data = new GridData();
        data.widthHint = 120;
        textProxyUsername.setLayoutData(data);

        //Proxy Password
        Label labelProxyPassword = new Label(restComp, SWT.RIGHT);
        labelProxyPassword.setText(Labels.getString("AdvancedSettingsDialog.proxyPassword")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelProxyPassword.setLayoutData(data);

        textProxyPassword = new Text(restComp, SWT.BORDER | SWT.PASSWORD);
        textProxyPassword.setText(config.getString(Config.PROXY_PASSWORD));
        data = new GridData();
        data.widthHint = 120;
        textProxyPassword.setLayoutData(data);


        //proxy NTLM domain
        Label labelProxyNtlmDomain = new Label(restComp, SWT.RIGHT);
        labelProxyNtlmDomain.setText(Labels.getString("AdvancedSettingsDialog.proxyNtlmDomain")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelProxyNtlmDomain.setLayoutData(data);

        textProxyNtlmDomain = new Text(restComp, SWT.BORDER);
        textProxyNtlmDomain.setText(config.getString(Config.PROXY_NTLM_DOMAIN));
        data = new GridData();
        data.widthHint = 250;
        textProxyNtlmDomain.setLayoutData(data);

        //////////////////////////////////////////////////
        //Row to start At

        Label blankAgain = new Label(restComp, SWT.NONE);
        data = new GridData();
        data.horizontalSpan = 2;
        blankAgain.setLayoutData(data);

        //Row to start AT
        Label labelLastRow = new Label(restComp, SWT.NONE);

        String lastBatch = controller.getConfig().getString(LastRun.LAST_LOAD_BATCH_ROW);
        if (lastBatch.equals("")) { //$NON-NLS-1$
            lastBatch = "0"; //$NON-NLS-1$
        }

        labelLastRow.setText(Labels.getFormattedString("AdvancedSettingsDialog.lastBatch", lastBatch)); //$NON-NLS-1$
        data = new GridData();
        data.horizontalSpan = 2;
        labelLastRow.setLayoutData(data);

        Label labelRowToStart = new Label(restComp, SWT.RIGHT);
        labelRowToStart.setText(Labels.getString("AdvancedSettingsDialog.startRow")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelRowToStart.setLayoutData(data);

        textRowToStart = new Text(restComp, SWT.BORDER);
        textRowToStart.setText(config.getString(Config.LOAD_ROW_TO_START_AT));
        data = new GridData();
        data.widthHint = 75;
        textRowToStart.setLayoutData(data);
        textRowToStart.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent event) {
                event.doit = Character.isISOControl(event.character) || Character.isDigit(event.character);
            }
        });

        // now that we've created all the buttons, make sure that buttons dependent on the bulk api
        // setting are enabled or disabled appropriately
        initBulkApiSetting(useBulkAPI);

        //the bottow separator
        Label labelSeparatorBottom = new Label(restComp, SWT.SEPARATOR | SWT.HORIZONTAL);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        labelSeparatorBottom.setLayoutData(data);

        //ok cancel buttons
        new Label(restComp, SWT.NONE);

        // END MIDDLE COMPONENT

        // START BOTTOM COMPONENT

        Composite buttonComp = new Composite(restComp, SWT.NONE);
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        buttonComp.setLayoutData(data);
        buttonComp.setLayout(new GridLayout(2, false));

        // Create the OK button and add a handler
        // so that pressing it will set input
        // to the entered value
        Button ok = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
        ok.setText(Labels.getString("UI.ok")); //$NON-NLS-1$
        ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                Config config = controller.getConfig();

                String currentTextEndpoint = textEndpoint.getText();
                if (currentTextEndpoint != null && !currentTextEndpoint.isEmpty() && !isValidHttpsUrl(currentTextEndpoint)) {
                    MessageDialog alert = new MessageDialog(getParent().getShell(), "Warning", null,
                            Labels.getFormattedString("AdvancedSettingsDialog.serverURLInfo", currentTextEndpoint),
                            MessageDialog.ERROR, new String[]{"OK"}, 0);
                    alert.open();
                    return;

                }
                //set the configValues
                config.setValue(Config.HIDE_WELCOME_SCREEN, buttonHideWelcomeScreen.getSelection());
                config.setValue(Config.INSERT_NULLS, buttonNulls.getSelection());
                config.setValue(Config.LOAD_BATCH_SIZE, textBatch.getText());
                if (!buttonCsvComma.getSelection()
                        && !buttonCsvTab.getSelection()
                        && (!buttonCsvOther.getSelection()
                        || textSplitterValue.getText() == null
                        || textSplitterValue.getText().length() == 0)) {
                    return;
                }
                config.setValue(Config.CSV_DELIMETER_OTHER_VALUE, textSplitterValue.getText());
                config.setValue(Config.CSV_DELIMETER_COMMA, buttonCsvComma.getSelection());
                config.setValue(Config.CSV_DELIMETER_TAB, buttonCsvTab.getSelection());
                config.setValue(Config.CSV_DELIMETER_OTHER, buttonCsvOther.getSelection());

                config.setValue(Config.EXTRACT_REQUEST_SIZE, textQueryBatch.getText());
                config.setValue(Config.ENDPOINT, currentTextEndpoint);
                config.setValue(Config.ASSIGNMENT_RULE, textRule.getText());
                config.setValue(Config.LOAD_ROW_TO_START_AT, textRowToStart.getText());
                config.setValue(Config.RESET_URL_ON_LOGIN, buttonResetUrl.getSelection());
                config.setValue(Config.NO_COMPRESSION, buttonCompression.getSelection());
                config.setValue(Config.TRUNCATE_FIELDS, buttonTruncateFields.getSelection());
                config.setValue(Config.TIMEOUT_SECS, textTimeout.getText());
                config.setValue(Config.ENABLE_EXTRACT_STATUS_OUTPUT, buttonOutputExtractStatus.getSelection());
                config.setValue(Config.READ_UTF8, buttonReadUtf8.getSelection());
                config.setValue(Config.WRITE_UTF8, buttonWriteUtf8.getSelection());
                config.setValue(Config.EURO_DATES, buttonEuroDates.getSelection());
                config.setValue(Config.TIMEZONE, textTimezone.getText());
                config.setValue(Config.PROXY_HOST, textProxyHost.getText());
                config.setValue(Config.PROXY_PASSWORD, textProxyPassword.getText());
                config.setValue(Config.PROXY_PORT, textProxyPort.getText());
                config.setValue(Config.PROXY_USERNAME, textProxyUsername.getText());
                config.setValue(Config.PROXY_NTLM_DOMAIN, textProxyNtlmDomain.getText());
                config.setValue(Config.BULK_API_ENABLED, buttonUseBulkApi.getSelection());
                config.setValue(Config.BULK_API_SERIAL_MODE, buttonBulkApiSerialMode.getSelection());
                config.setValue(Config.BULK_API_ZIP_CONTENT, buttonBulkApiZipContent.getSelection());
                config.setValue(Config.BULK_QUERY_PK_CHUNKING, buttonBulkQueryPKChunking.getSelection());
                config.setValue(Config.BULK_QUERY_CHUNK_SIZE, textBulkQueryChunkSize.getText());

                controller.saveConfig();
                controller.logout();

                input = Labels.getString("UI.ok"); //$NON-NLS-1$
                shell.close();
            }
        });
        data = new GridData();
        data.widthHint = 75;
        ok.setLayoutData(data);

        // Create the cancel button and add a handler
        // so that pressing it will set input to null
        Button cancel = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
        cancel.setText(Labels.getString("UI.cancel")); //$NON-NLS-1$
        cancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                input = null;
                shell.close();
            }
        });

        // END BOTTOM COMPONENT

        data = new GridData();
        data.widthHint = 75;
        cancel.setLayoutData(data);

        // Set the OK button as the default, so
        // user can type input and press Enter
        // to dismiss
        shell.setDefaultButton(ok);

        // Set the child as the scrolled content of the ScrolledComposite
        sc.setContent(container);

        // Set the minimum size
        sc.setMinSize(768, 1024);

        // Expand both horizontally and vertically
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);
    }

    private Text createTextInput(Composite parent, String labelKey, String configKey, String defaultValue, int widthHint) {
        // TODO: use this method to create all text inputs
        createLabel(parent, labelKey);
        final Text t = new Text(parent, SWT.BORDER);
        final GridData gd = new GridData();
        if (widthHint > 0) gd.widthHint = widthHint;
        t.setLayoutData(gd);
        String val = controller.getConfig().getString(configKey);
        if ("".equals(val) && defaultValue != null) val = defaultValue;
        t.setText(String.valueOf(val));
        return t;
    }


    private void createLabel(Composite parent, String labelKey) {
        Label l = new Label(parent, SWT.RIGHT);
        l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        l.setText(Labels.getString(labelKey));
    }
}
