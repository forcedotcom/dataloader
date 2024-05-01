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
import com.salesforce.dataloader.util.AppUtil;
import com.salesforce.dataloader.util.LoggingUtil;
import com.sforce.soap.partner.Connector;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class AdvancedSettingsDialog extends BaseDialog {
    private Text textImportBatchSize;
    private Text textExportBatchSize;
    private Text textUploadCSVDelimiterValue;
    private Text textQueryResultsDelimiterValue;
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
    private Button buttonLocalSystemTimezone;
    private Text textProductionPartnerClientID;
    private Text textSandboxPartnerClientID;
    private Text textProductionBulkClientID;
    private Text textSandboxBulkClientID;
    private Text textWizardWidth;
    private Text textWizardHeight;
    private final String defaultServer;

    private Button buttonHideWelcomeScreen;
    private Button buttonShowLoaderUpgradeScreen;
    private Button buttonOutputExtractStatus;
    private Button buttonSortExtractFields;
    private Button buttonLimitQueryResultColumnsToFieldsInQuery;
    private Button buttonReadUtf8;
    private Button buttonWriteUtf8;
    private Button buttonEuroDates;
    private Button buttonTruncateFields;
    private Button buttonFormatPhoneFields;
    private Button buttonKeepAccountTeam;
    private Button buttonCacheDescribeGlobalResults;
    private Button buttonIncludeRTFBinaryDataInQueryResults;
    private Button buttonUseSOAPApi;
    private Button buttonUseBulkV1Api;
    private Button buttonUseBulkV2Api;
    private Button buttonBulkApiSerialMode;
    private Button buttonBulkApiZipContent;
    private Label labelBulkApiSerialMode;
    private Label labelBulkApiZipContent;
    private Button buttonCsvComma;
    private Button buttonCsvTab;
    private Button buttonLoginFromBrowser;
    private Button buttonCloseWizardOnFinish;
    private Button buttonPopulateResultsFolderOnWizardFinishStep;
    private static final String[] LOGGING_LEVEL = { "ALL", "DEBUG", "INFO", "WARN", "ERROR", "FATAL" };
    private Combo comboLoggingLevelDropdown;
    private Composite soapApiOptionsComposite;
    private Composite bulkApiOptionsComposite;
    private Composite exportBatchSizeComposite;
    private Composite importBatchSizeComposite;
    
    /**
     * InputDialog constructor
     *
     * @param parent the parent
     */
    public AdvancedSettingsDialog(Shell parent, Controller controller) {
        super(parent, controller);

        URI uri;
        String server = "";
        try {
            uri = new URI(Connector.END_POINT);
            server = uri.getScheme() + "://" + uri.getHost(); //$NON-NLS-1$
        } catch (URISyntaxException e) {
            logger.error("", e);
        }
        defaultServer = server;
    }

    private final Map<Button, Composite> apiOptionsMap = new HashMap<Button, Composite>();
    private boolean useBulkAPI = false;
    private boolean useBulkV2API = false;
    private boolean useSoapAPI = false;
    
    
    private void setEnabled(Label label, boolean isEnabled) {
        int color = isEnabled ? SWT.COLOR_BLACK : SWT.COLOR_GRAY;
        label.setForeground(getParent().getDisplay().getSystemColor(color));
    }
    
    private void setEnabled(Control ctrl, boolean enabled) {
        if (ctrl instanceof Composite) {
            Composite comp = (Composite) ctrl;
            for (Control c : comp.getChildren())
                setEnabled(c, enabled);
        } else if (ctrl instanceof Label) {
            setEnabled((Label)ctrl, enabled);
        } else { // Button, Checkbox, Dropdown list etc
            ctrl.setEnabled(enabled);
        }
    }
    
    private void setAllApiOptions() {
        for (Button apiButton : apiOptionsMap.keySet()) {
            enableApiOptions(apiButton, false);
        }
        Button selectedButton = useBulkAPI ? this.buttonUseBulkV1Api : (useBulkV2API ? this.buttonUseBulkV2Api : this.buttonUseSOAPApi);
        enableApiOptions(selectedButton, true);
        setEnabled(this.exportBatchSizeComposite, useSoapAPI);
        setEnabled(this.importBatchSizeComposite, !useBulkV2API);
    }
    
    private void enableApiOptions(Button apiButton, boolean isEnabled) {
        Composite apiOptionsComposite = apiOptionsMap.get(apiButton);
        setEnabled(apiOptionsComposite, isEnabled);
    }
    
    private void initializeAllApiOptions() {
        apiOptionsMap.put(buttonUseSOAPApi, soapApiOptionsComposite);
        apiOptionsMap.put(buttonUseBulkV1Api, bulkApiOptionsComposite);
        setAllApiOptions();
    }

    /**
     * Creates the dialog's contents
     *
     * @param shell the dialog window
     */
    protected void createContents(final Shell shell) {        
        final Config config = getController().getConfig();
        GridData data;
        
        GridLayout layout = new GridLayout(1, false);
        layout.verticalSpacing = 10;
        shell.setLayout(layout);
        data = new GridData(GridData.FILL_BOTH);
        shell.setLayoutData(data);

        // Create the ScrolledComposite to scroll horizontally and vertically
        ScrolledComposite sc = new ScrolledComposite(shell, SWT.H_SCROLL | SWT.V_SCROLL);
        data = new GridData(GridData.FILL_BOTH);
        data.heightHint = 600;
        sc.setLayoutData(data);
        
        // Create the parent Composite container for the three child containers
        Composite container = new Composite(sc, SWT.NONE);
        GridLayout containerLayout = new GridLayout(1, false);
        container.setLayout(containerLayout);

        data = new GridData(GridData.FILL_HORIZONTAL);
        data.heightHint = 50;
        data.widthHint = 400;

        // START TOP COMPONENT
        Composite topComp = new Composite(container, SWT.NONE);
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

        // END TOP COMPONENT

        // START MIDDLE COMPONENT

        Composite restComp = new Composite(container, SWT.NONE);
        data = new GridData(GridData.FILL_BOTH);
        restComp.setLayoutData(data);
        layout = new GridLayout(2, true);
        layout.verticalSpacing = 10;
        restComp.setLayout(layout);

        // Hide welcome screen
        Label labelHideWelcomeScreen = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelHideWelcomeScreen.setText(Labels.getString("AdvancedSettingsDialog.hideWelcomeScreen")); //$NON-NLS-1$
        labelHideWelcomeScreen.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        buttonHideWelcomeScreen = new Button(restComp, SWT.CHECK);
        buttonHideWelcomeScreen.setSelection(config.getBoolean(Config.HIDE_WELCOME_SCREEN));

        // Hide welcome screen
        Label labelShowLoaderUpgradeScreen = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelShowLoaderUpgradeScreen.setText(Labels.getString("AdvancedSettingsDialog.showLoaderUpgradeScreen")); //$NON-NLS-1$
        labelShowLoaderUpgradeScreen.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        buttonShowLoaderUpgradeScreen = new Button(restComp, SWT.CHECK);
        buttonShowLoaderUpgradeScreen.setSelection(config.getBoolean(Config.SHOW_LOADER_UPGRADE_SCREEN));

        Composite apiChoiceComposite = new Composite(restComp, SWT.None);
        layout = new GridLayout(3, true);
        layout.verticalSpacing = 10;
        apiChoiceComposite.setLayout(layout);
        data = new GridData();
        data.horizontalSpan = 2;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        apiChoiceComposite.setLayoutData(data);
        
        // Enable Bulk API Setting
        useBulkAPI = config.getBoolean(Config.BULK_API_ENABLED) && !config.getBoolean(Config.BULKV2_API_ENABLED);
        useBulkV2API = config.getBoolean(Config.BULK_API_ENABLED) && config.getBoolean(Config.BULKV2_API_ENABLED);
        useSoapAPI = !useBulkAPI && !useBulkV2API;

        buttonUseSOAPApi = new Button(apiChoiceComposite, SWT.RADIO);
        buttonUseSOAPApi.setSelection(useSoapAPI);
        buttonUseSOAPApi.setText(Labels.getString("AdvancedSettingsDialog.useSOAPApi"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        data.grabExcessHorizontalSpace = true;
        buttonUseSOAPApi.setLayoutData(data);
        buttonUseSOAPApi.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                useSoapAPI = buttonUseSOAPApi.getSelection();
                if (!useSoapAPI) {
                    return;
                }
                useBulkAPI = false;
                useBulkV2API = false;
                setAllApiOptions();
                
                // update batch size when this setting changes
                int newDefaultBatchSize = getController().getConfig().getDefaultImportBatchSize(false, false);
                logger.info("Setting batch size to " + newDefaultBatchSize);
                textImportBatchSize.setText(String.valueOf(newDefaultBatchSize));
            }
        });
        
        buttonUseBulkV1Api = new Button(apiChoiceComposite, SWT.RADIO);
        buttonUseBulkV1Api.setSelection(useBulkAPI);
        buttonUseBulkV1Api.setText(Labels.getString("AdvancedSettingsDialog.useBulkV1Api"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        data.grabExcessHorizontalSpace = true;
        buttonUseBulkV1Api.setLayoutData(data);
        buttonUseBulkV1Api.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                useBulkAPI = buttonUseBulkV1Api.getSelection();
                if (!useBulkAPI) {
                    return;
                }
                useSoapAPI = false;
                useBulkV2API = false;
                setAllApiOptions();
                
                // update batch size when this setting changes
                int newDefaultBatchSize = getController().getConfig().getDefaultImportBatchSize(true, false);
                logger.info("Setting batch size to " + newDefaultBatchSize);
                textImportBatchSize.setText(String.valueOf(newDefaultBatchSize));
            }
        });
        
        // Enable Bulk API 2.0 Setting
        buttonUseBulkV2Api = new Button(apiChoiceComposite, SWT.RADIO);
        buttonUseBulkV2Api.setSelection(useBulkV2API);
        buttonUseBulkV2Api.setText(Labels.getString("AdvancedSettingsDialog.useBulkV2Api"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        data.grabExcessHorizontalSpace = true;
        buttonUseBulkV2Api.setLayoutData(data);
        buttonUseBulkV2Api.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                useBulkV2API = buttonUseBulkV2Api.getSelection();
                if (!useBulkV2API) {
                    return;
                }
                useSoapAPI = false;
                useBulkAPI = false;
                setAllApiOptions();
                
                // get default batch size for Bulk v1 and set it
                int newDefaultBatchSize = getController().getConfig().getDefaultImportBatchSize(true, true);
                logger.info("Setting batch size to " + newDefaultBatchSize);
                textImportBatchSize.setText(String.valueOf(newDefaultBatchSize));
            }
        });
        
        
        // SOAP API - Keep Account team setting
        this.soapApiOptionsComposite = new Composite(restComp, SWT.None);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        data.grabExcessHorizontalSpace = true;
        this.soapApiOptionsComposite.setLayoutData(data);
        layout = new GridLayout(2, true);
        layout.verticalSpacing = 10;
        this.soapApiOptionsComposite.setLayout(layout);

        Label labelKeepAccountTeam = new Label(this.soapApiOptionsComposite, SWT.RIGHT | SWT.WRAP);
        labelKeepAccountTeam.setText(Labels.getString("AdvancedSettingsDialog.keepAccountTeam"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        data.grabExcessHorizontalSpace = true;
        labelKeepAccountTeam.setLayoutData(data);

        boolean keepAccountTeam = config.getBoolean(Config.PROCESS_KEEP_ACCOUNT_TEAM);
        buttonKeepAccountTeam = new Button(this.soapApiOptionsComposite, SWT.CHECK);
        buttonKeepAccountTeam.setSelection(keepAccountTeam);
        data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        data.grabExcessHorizontalSpace = true;
        buttonKeepAccountTeam.setLayoutData(data);
        buttonKeepAccountTeam.setToolTipText(Labels.getString("AdvancedSettingsDialog.keepAccountTeamHelp"));
        labelKeepAccountTeam.setToolTipText(Labels.getString("AdvancedSettingsDialog.keepAccountTeamHelp"));
        if (useBulkAPI) {
            buttonKeepAccountTeam.setSelection(false);
        }
        // Bulk API serial concurrency mode setting
        this.bulkApiOptionsComposite = new Composite(restComp, SWT.None);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        data.grabExcessHorizontalSpace = true;
        this.bulkApiOptionsComposite.setLayoutData(data);
        layout = new GridLayout(2, true);
        layout.verticalSpacing = 10;
        this.bulkApiOptionsComposite.setLayout(layout);

        labelBulkApiSerialMode = new Label(this.bulkApiOptionsComposite, SWT.RIGHT | SWT.WRAP);
        labelBulkApiSerialMode.setText(Labels.getString("AdvancedSettingsDialog.bulkApiSerialMode")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        data.grabExcessHorizontalSpace = true;
        labelBulkApiSerialMode.setLayoutData(data);
        buttonBulkApiSerialMode = new Button(this.bulkApiOptionsComposite, SWT.CHECK);
        buttonBulkApiSerialMode.setSelection(config.getBoolean(Config.BULK_API_SERIAL_MODE));
        data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        data.grabExcessHorizontalSpace = true;
        buttonBulkApiSerialMode.setLayoutData(data);

        // Bulk API serial concurrency mode setting
        labelBulkApiZipContent = new Label(this.bulkApiOptionsComposite, SWT.RIGHT | SWT.WRAP);
        labelBulkApiZipContent.setText(Labels.getString("AdvancedSettingsDialog.bulkApiZipContent")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        data.grabExcessHorizontalSpace = true;
        labelBulkApiZipContent.setLayoutData(data);
        buttonBulkApiZipContent = new Button(this.bulkApiOptionsComposite, SWT.CHECK);
        buttonBulkApiZipContent.setSelection(config.getBoolean(Config.BULK_API_ZIP_CONTENT));
        data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        data.grabExcessHorizontalSpace = true;
        buttonBulkApiZipContent.setLayoutData(data);

        //SOAP and Bulk API - batch size
        this.importBatchSizeComposite = new Composite(restComp, SWT.None);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        data.grabExcessHorizontalSpace = true;
        this.importBatchSizeComposite.setLayoutData(data);
        layout = new GridLayout(2, true);
        layout.verticalSpacing = 10;
        this.importBatchSizeComposite.setLayout(layout);

        Label labelBatch = new Label(importBatchSizeComposite, SWT.RIGHT | SWT.WRAP);
        labelBatch.setText(Labels.getString("AdvancedSettingsDialog.importBatchSize")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        data.grabExcessHorizontalSpace = true;
        labelBatch.setLayoutData(data);

        textImportBatchSize = new Text(importBatchSizeComposite, SWT.BORDER);
        textImportBatchSize.setText(Integer.toString(config.getImportBatchSize()));
        textImportBatchSize.setEnabled(!useBulkV2API);
        textImportBatchSize.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent event) {
                event.doit = Character.isISOControl(event.character) || Character.isDigit(event.character);
            }
        });
        data = new GridData();
        GC gc = new GC(textImportBatchSize);
        Point textSize = gc.textExtent("8");
        gc.dispose();
        textImportBatchSize.setTextLimit(8);
        data.widthHint = 8 * textSize.x;
        textImportBatchSize.setLayoutData(data);

        //SOAP API - extraction batch size
        this.exportBatchSizeComposite = new Composite(restComp, SWT.None);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        data.grabExcessHorizontalSpace = true;
        this.exportBatchSizeComposite.setLayoutData(data);
        layout = new GridLayout(2, true);
        layout.verticalSpacing = 10;
        this.exportBatchSizeComposite.setLayout(layout);
        Label labelQueryBatch = new Label(exportBatchSizeComposite, SWT.RIGHT | SWT.WRAP);
        labelQueryBatch.setText(Labels.getString("ExtractionInputDialog.exportBatchSize")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        data.grabExcessHorizontalSpace = true;
        labelQueryBatch.setLayoutData(data);

        textExportBatchSize = new Text(exportBatchSizeComposite, SWT.BORDER);
        textExportBatchSize.setText(config.getString(Config.EXPORT_BATCH_SIZE));
        textExportBatchSize.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent event) {
                event.doit = Character.isISOControl(event.character) || Character.isDigit(event.character);
            }
        });
        data = new GridData();
        textExportBatchSize.setTextLimit(4);
        data.widthHint = 4 * textSize.x;
        textExportBatchSize.setLayoutData(data);

        initializeAllApiOptions();
        
        blank = new Label(restComp, SWT.NONE);
        data = new GridData();
        data.horizontalSpan = 2;
        data.heightHint = 15;
        blank.setLayoutData(data);
        
        //insert Nulls
        Label labelNulls = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelNulls.setText(Labels.getString("AdvancedSettingsDialog.insertNulls")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelNulls.setLayoutData(data);
        buttonNulls = new Button(restComp, SWT.CHECK);
        buttonNulls.setSelection(config.getBoolean(Config.INSERT_NULLS));

        //assignment rules
        Label labelRule = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelRule.setText(Labels.getString("AdvancedSettingsDialog.assignmentRule")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelRule.setLayoutData(data);

        textRule = new Text(restComp, SWT.BORDER);
        data = new GridData();
        textRule.setTextLimit(18);
        data.widthHint = 18 * textSize.x;
        textRule.setLayoutData(data);
        textRule.setText(config.getString(Config.ASSIGNMENT_RULE));

        //endpoint
        Label labelEndpoint = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelEndpoint.setText(Labels.getString("AdvancedSettingsDialog.serverURL")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelEndpoint.setLayoutData(data);

        textEndpoint = new Text(restComp, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 30 * textSize.x;
        textEndpoint.setLayoutData(data);
        String endpoint = config.getString(Config.ENDPOINT);
        if ("".equals(endpoint)) { //$NON-NLS-1$
            endpoint = defaultServer;
        }
        textEndpoint.setText(endpoint);

        //reset url on login
        Label labelResetUrl = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelResetUrl.setText(Labels.getString("AdvancedSettingsDialog.resetUrlOnLogin")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelResetUrl.setLayoutData(data);
        buttonResetUrl = new Button(restComp, SWT.CHECK);
        buttonResetUrl.setSelection(config.getBoolean(Config.RESET_URL_ON_LOGIN));

        //insert compression
        Label labelCompression = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelCompression.setText(Labels.getString("AdvancedSettingsDialog.compression")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelCompression.setLayoutData(data);
        buttonCompression = new Button(restComp, SWT.CHECK);
        buttonCompression.setSelection(config.getBoolean(Config.NO_COMPRESSION));

        //timeout size
        Label labelTimeout = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelTimeout.setText(Labels.getString("AdvancedSettingsDialog.timeout")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelTimeout.setLayoutData(data);

        textTimeout = new Text(restComp, SWT.BORDER);
        textTimeout.setText(config.getString(Config.TIMEOUT_SECS));
        textTimeout.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent event) {
                event.doit = Character.isISOControl(event.character) || Character.isDigit(event.character);
            }
        });
        data = new GridData();
        textTimeout.setTextLimit(4);
        data.widthHint = 4 * textSize.x;
        textTimeout.setLayoutData(data);

        // enable/disable sort of fields to extract
        Label labelSortExtractFields = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelSortExtractFields.setText(Labels.getString("AdvancedSettingsDialog.sortQueryFieldsInExtraction")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelSortExtractFields.setLayoutData(data);

        buttonSortExtractFields = new Button(restComp, SWT.CHECK);
        buttonSortExtractFields.setSelection(config.getBoolean(Config.SORT_EXTRACT_FIELDS));
        
        // enable/disable limiting query result columns to fields specified in the SOQL query
        Label labelLimitQueryResultColumnsToFieldsInQuery = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelLimitQueryResultColumnsToFieldsInQuery.setText(Labels.getString(this.getClass().getSimpleName() + ".limitOutputToQueryFields")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelLimitQueryResultColumnsToFieldsInQuery.setLayoutData(data);
       
        buttonLimitQueryResultColumnsToFieldsInQuery = new Button(restComp, SWT.CHECK);
        buttonLimitQueryResultColumnsToFieldsInQuery.setSelection(config.getBoolean(Config.LIMIT_OUTPUT_TO_QUERY_FIELDS));

        //enable/disable output of success file for extracts
        Label labelOutputExtractStatus = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelOutputExtractStatus.setText(Labels.getString("AdvancedSettingsDialog.outputExtractStatus")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelOutputExtractStatus.setLayoutData(data);

        buttonOutputExtractStatus = new Button(restComp, SWT.CHECK);
        buttonOutputExtractStatus.setSelection(config.getBoolean(Config.ENABLE_EXTRACT_STATUS_OUTPUT));

        //utf-8 for loading
        Label labelReadUTF8 = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelReadUTF8.setText(Labels.getString("AdvancedSettingsDialog.readUTF8")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelReadUTF8.setLayoutData(data);

        buttonReadUtf8 = new Button(restComp, SWT.CHECK);
        buttonReadUtf8.setSelection(config.getBoolean(Config.READ_UTF8));

        //utf-8 for extraction
        Label labelWriteUTF8 = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelWriteUTF8.setText(Labels.getString("AdvancedSettingsDialog.writeUTF8")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelWriteUTF8.setLayoutData(data);

        buttonWriteUtf8 = new Button(restComp, SWT.CHECK);
        buttonWriteUtf8.setSelection(config.getBoolean(Config.WRITE_UTF8));

        //European Dates
        Label labelEuropeanDates = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelEuropeanDates.setText(Labels.getString("AdvancedSettingsDialog.useEuropeanDateFormat")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelEuropeanDates.setLayoutData(data);

        buttonEuroDates = new Button(restComp, SWT.CHECK);
        buttonEuroDates.setSelection(config.getBoolean(Config.EURO_DATES));

        //Field truncation
        Label labelTruncateFields = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelTruncateFields.setText(Labels.getString("AdvancedSettingsDialog.allowFieldTruncation"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelTruncateFields.setLayoutData(data);

        buttonTruncateFields = new Button(restComp, SWT.CHECK);
        buttonTruncateFields.setSelection(config.getBoolean(Config.TRUNCATE_FIELDS));

        //format phone fields on the client side
        Label labelFormatPhoneFields = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelFormatPhoneFields.setText(Labels.getString("AdvancedSettingsDialog.formatPhoneFields"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelFormatPhoneFields.setLayoutData(data);

        buttonFormatPhoneFields = new Button(restComp, SWT.CHECK);
        buttonFormatPhoneFields.setSelection(config.getBoolean(Config.FORMAT_PHONE_FIELDS));

        Label labelCsvCommand = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelCsvCommand.setText(Labels.getString("AdvancedSettingsDialog.useCommaAsCsvDelimiter"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelCsvCommand.setLayoutData(data);
        buttonCsvComma = new Button(restComp, SWT.CHECK);
        buttonCsvComma.setSelection(config.getBoolean(Config.CSV_DELIMITER_COMMA));

        Label labelTabCommand = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelTabCommand.setText(Labels.getString("AdvancedSettingsDialog.useTabAsCsvDelimiter"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelTabCommand.setLayoutData(data);
        buttonCsvTab = new Button(restComp, SWT.CHECK);
        buttonCsvTab.setSelection(config.getBoolean(Config.CSV_DELIMITER_TAB));

        Label labelOtherDelimiterValue = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelOtherDelimiterValue.setText(Labels.getString("AdvancedSettingsDialog.csvOtherDelimiterValue"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelOtherDelimiterValue.setLayoutData(data);
        textUploadCSVDelimiterValue = new Text(restComp, SWT.BORDER);
        textUploadCSVDelimiterValue.setText(config.getString(Config.CSV_DELIMITER_OTHER_VALUE));
        data = new GridData();
        data.widthHint = 15 * textSize.x;
        textUploadCSVDelimiterValue.setLayoutData(data);

        Label labelQueryResultsDelimiter = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelQueryResultsDelimiter.setText(Labels.getString("AdvancedSettingsDialog.queryResultsDelimiterValue"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelQueryResultsDelimiter.setLayoutData(data);
        textQueryResultsDelimiterValue = new Text(restComp, SWT.BORDER);
        textQueryResultsDelimiterValue.setText(config.getString(Config.CSV_DELIMITER_FOR_QUERY_RESULTS));
        textQueryResultsDelimiterValue.setTextLimit(1);
        data = new GridData();
        data.widthHint = 5 * textSize.x;
        textQueryResultsDelimiterValue.setLayoutData(data);
        
        
        // include image data for Rich Text Fields in query results
        // Config.INCLUDE_RICH_TEXT_FIELD_DATA_IN_QUERY_RESULTS
        Label labelIncludeRTFBinaryDataInQueryResults = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelIncludeRTFBinaryDataInQueryResults.setText(Labels.getString("AdvancedSettingsDialog.includeRTFBinaryDataInQueryResults"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelIncludeRTFBinaryDataInQueryResults.setLayoutData(data);

        boolean includeRTFBinaryDataInQueryResults = config.getBoolean(Config.INCLUDE_RICH_TEXT_FIELD_DATA_IN_QUERY_RESULTS);
        buttonIncludeRTFBinaryDataInQueryResults = new Button(restComp, SWT.CHECK);
        buttonIncludeRTFBinaryDataInQueryResults.setSelection(includeRTFBinaryDataInQueryResults);

        // Cache DescribeGlobal results across operations
        Label labelCacheDescribeGlobalResults = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelCacheDescribeGlobalResults.setText(Labels.getString("AdvancedSettingsDialog.cacheDescribeGlobalResults"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelCacheDescribeGlobalResults.setLayoutData(data);

        boolean cacheDescribeGlobalResults = config.getBoolean(Config.CACHE_DESCRIBE_GLOBAL_RESULTS);
        buttonCacheDescribeGlobalResults = new Button(restComp, SWT.CHECK);
        buttonCacheDescribeGlobalResults.setSelection(cacheDescribeGlobalResults);        
        
        Label empty = new Label(restComp, SWT.NONE);
        data = new GridData();
        data.horizontalSpan = 2;
        empty.setLayoutData(data);

        empty = new Label(restComp, SWT.NONE);
        data = new GridData();
        data.horizontalSpan = 2;
        empty.setLayoutData(data);

        // timezone
        textTimezone = createTimezoneTextInput(restComp, "AdvancedSettingsDialog.timezone", Config.TIMEZONE, TimeZone.getDefault().getID(), 30 * textSize.x);
        
        empty = new Label(restComp, SWT.NONE);
        data = new GridData();
        data.horizontalSpan = 2;
        empty.setLayoutData(data);
        
        // proxy Host
        Label labelProxyHost = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelProxyHost.setText(Labels.getString("AdvancedSettingsDialog.proxyHost")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelProxyHost.setLayoutData(data);

        textProxyHost = new Text(restComp, SWT.BORDER);
        textProxyHost.setText(config.getString(Config.PROXY_HOST));
        data = new GridData(GridData.FILL_HORIZONTAL);
        textProxyHost.setLayoutData(data);

        //Proxy Port
        Label labelProxyPort = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelProxyPort.setText(Labels.getString("AdvancedSettingsDialog.proxyPort")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelProxyPort.setLayoutData(data);

        textProxyPort = new Text(restComp, SWT.BORDER);
        textProxyPort.setText(config.getString(Config.PROXY_PORT));
        textProxyPort.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent event) {
                event.doit = Character.isISOControl(event.character) || Character.isDigit(event.character);
            }
        });
        data = new GridData();
        textProxyPort.setTextLimit(5);
        data.widthHint = 5 * textSize.x;
        textProxyPort.setLayoutData(data);

        //Proxy Username
        Label labelProxyUsername = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelProxyUsername.setText(Labels.getString("AdvancedSettingsDialog.proxyUser")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelProxyUsername.setLayoutData(data);

        textProxyUsername = new Text(restComp, SWT.BORDER);
        textProxyUsername.setText(config.getString(Config.PROXY_USERNAME));
        data = new GridData();
        data.widthHint = 20 * textSize.x;
        textProxyUsername.setLayoutData(data);

        //Proxy Password
        Label labelProxyPassword = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelProxyPassword.setText(Labels.getString("AdvancedSettingsDialog.proxyPassword")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelProxyPassword.setLayoutData(data);

        textProxyPassword = new Text(restComp, SWT.BORDER | SWT.PASSWORD);
        textProxyPassword.setText(config.getString(Config.PROXY_PASSWORD));
        data = new GridData();
        data.widthHint = 20 * textSize.x;
        textProxyPassword.setLayoutData(data);


        //proxy NTLM domain
        Label labelProxyNtlmDomain = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelProxyNtlmDomain.setText(Labels.getString("AdvancedSettingsDialog.proxyNtlmDomain")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelProxyNtlmDomain.setLayoutData(data);

        textProxyNtlmDomain = new Text(restComp, SWT.BORDER);
        textProxyNtlmDomain.setText(config.getString(Config.PROXY_NTLM_DOMAIN));
        data = new GridData(GridData.FILL_HORIZONTAL);
        textProxyNtlmDomain.setLayoutData(data);
        
        empty = new Label(restComp, SWT.NONE);
        data = new GridData();
        data.horizontalSpan = 2;
        empty.setLayoutData(data);
        
        Label loginFromBrowserCheckboxText = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        loginFromBrowserCheckboxText.setText(Labels.getString("AdvancedSettingsDialog.loginFromBrowser"));
        loginFromBrowserCheckboxText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        boolean doLoginFromBrowser = config.getBoolean(Config.OAUTH_LOGIN_FROM_BROWSER);
        buttonLoginFromBrowser = new Button(restComp, SWT.CHECK);
        buttonLoginFromBrowser.setSelection(doLoginFromBrowser);
        
        Label clientIdInProductionText = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        clientIdInProductionText.setText(Labels.getString("AdvancedSettingsDialog.partnerClientIdInProduction"));
        clientIdInProductionText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        this.textProductionPartnerClientID = new Text(restComp, SWT.NONE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        textProductionPartnerClientID.setLayoutData(data);
    	String clientId = config.getOAuthEnvironmentString(Config.OAUTH_PROD_ENVIRONMENT_VAL, Config.OAUTH_PARTIAL_PARTNER_CLIENTID);
    	this.textProductionPartnerClientID.setText(clientId);
        
        clientIdInProductionText = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        clientIdInProductionText.setText(Labels.getString("AdvancedSettingsDialog.bulkClientIdInProduction"));
        clientIdInProductionText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        this.textProductionBulkClientID = new Text(restComp, SWT.NONE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        textProductionBulkClientID.setLayoutData(data);
        clientId = config.getOAuthEnvironmentString(Config.OAUTH_PROD_ENVIRONMENT_VAL, Config.OAUTH_PARTIAL_BULK_CLIENTID);
        this.textProductionBulkClientID.setText(clientId);
        
        Label clientIdInSandboxText = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        clientIdInSandboxText.setText(Labels.getString("AdvancedSettingsDialog.partnerClientIdInSandbox"));
        clientIdInSandboxText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        this.textSandboxPartnerClientID = new Text(restComp, SWT.NONE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        textSandboxPartnerClientID.setLayoutData(data);
    	clientId = config.getOAuthEnvironmentString(Config.OAUTH_SB_ENVIRONMENT_VAL, Config.OAUTH_PARTIAL_PARTNER_CLIENTID);
    	this.textSandboxPartnerClientID.setText(clientId);
        
        clientIdInSandboxText = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        clientIdInSandboxText.setText(Labels.getString("AdvancedSettingsDialog.bulkClientIdInSandbox"));
        clientIdInSandboxText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        this.textSandboxBulkClientID = new Text(restComp, SWT.NONE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        textSandboxBulkClientID.setLayoutData(data);
        clientId = config.getOAuthEnvironmentString(Config.OAUTH_SB_ENVIRONMENT_VAL, Config.OAUTH_PARTIAL_BULK_CLIENTID);
        this.textSandboxBulkClientID.setText(clientId);       
        //////////////////////////////////////////////////
        //Row to start At

        Label blankAgain = new Label(restComp, SWT.NONE);
        data = new GridData();
        data.horizontalSpan = 2;
        blankAgain.setLayoutData(data);
        
        String lastBatch = getController().getConfig().getString(LastRun.LAST_LOAD_BATCH_ROW);
        if (lastBatch.equals("")) { //$NON-NLS-1$
            lastBatch = "0"; //$NON-NLS-1$
        }

        Label labelRowToStart = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelRowToStart.setText(Labels.getString("AdvancedSettingsDialog.startRow")
                + "\n("
                + Labels.getFormattedString("AdvancedSettingsDialog.lastBatch", lastBatch)
                + ")"); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelRowToStart.setLayoutData(data);

        textRowToStart = new Text(restComp, SWT.BORDER);
        textRowToStart.setText(config.getString(Config.LOAD_ROW_TO_START_AT));
        data = new GridData();
        textRowToStart.setTextLimit(15);
        data.widthHint = 15 * textSize.x;
        textRowToStart.setLayoutData(data);
        textRowToStart.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent event) {
                event.doit = Character.isISOControl(event.character) || Character.isDigit(event.character);
            }
        });

        // now that we've created all the buttons, make sure that buttons dependent on the bulk api
        // setting are enabled or disabled appropriately
       // enableBulkRelatedOptions(useBulkAPI);
        
        blankAgain = new Label(restComp, SWT.NONE);
        data = new GridData();
        data.horizontalSpan = 2;
        blankAgain.setLayoutData(data);
        
        Label closeWizardOnFinishCheckboxText = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        closeWizardOnFinishCheckboxText.setText(Labels.getString("AdvancedSettingsDialog.closeWizardOnFinish"));
        closeWizardOnFinishCheckboxText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        boolean closeWizardOnFinish = config.getBoolean(Config.WIZARD_CLOSE_ON_FINISH);
        buttonCloseWizardOnFinish = new Button(restComp, SWT.CHECK);
        buttonCloseWizardOnFinish.setSelection(closeWizardOnFinish);

        Label labelWizardWidthAndHeight = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelWizardWidthAndHeight.setText(Labels.getString("AdvancedSettingsDialog.wizardWidthAndHeight"));
        labelWizardWidthAndHeight.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        Composite widthAndHeightComp = new Composite(restComp,  SWT.NONE);
        data = new GridData(GridData.FILL_BOTH);
        widthAndHeightComp.setLayoutData(data);
        layout = new GridLayout(3, false);
        widthAndHeightComp.setLayout(layout);
        textWizardWidth = new Text(widthAndHeightComp, SWT.BORDER);
        textWizardWidth.setText(config.getString(Config.WIZARD_WIDTH));
        data = new GridData();
        textWizardWidth.setTextLimit(4);
        data.widthHint = 4 * textSize.x;
        textWizardWidth.setLayoutData(data);
        textWizardWidth.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent event) {
                event.doit = Character.isISOControl(event.character) || Character.isDigit(event.character);
            }
        });
        
        Label labelMultiplySymbol = new Label(widthAndHeightComp, SWT.CENTER);
        labelMultiplySymbol.setText("x");
        labelMultiplySymbol.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));

        textWizardHeight = new Text(widthAndHeightComp, SWT.BORDER);
        textWizardHeight.setText(config.getString(Config.WIZARD_HEIGHT));
        textWizardHeight.setTextLimit(4);
        data.widthHint = 4 * textSize.x;
        textWizardHeight.setLayoutData(data);
        textWizardHeight.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent event) {
                event.doit = Character.isISOControl(event.character) || Character.isDigit(event.character);
            }
        });

        Label populateResultsFolderOnWizardFinishStepText = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        populateResultsFolderOnWizardFinishStepText.setText(Labels.getString("AdvancedSettingsDialog.populateResultsFolderOnFinishStepOfWizard"));
        populateResultsFolderOnWizardFinishStepText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        boolean populateResultsFolderOnFinishStep = config.getBoolean(Config.WIZARD_POPULATE_RESULTS_FOLDER_WITH_PREVIOUS_OP_RESULTS_FOLDER);
        buttonPopulateResultsFolderOnWizardFinishStep = new Button(restComp, SWT.CHECK);
        buttonPopulateResultsFolderOnWizardFinishStep.setSelection(populateResultsFolderOnFinishStep);
        
        blankAgain = new Label(restComp, SWT.NONE);
        data = new GridData();
        data.horizontalSpan = 2;
        blankAgain.setLayoutData(data);
        
        Label labelLoggingConfigFile = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelLoggingConfigFile.setText(Labels.getString("AdvancedSettingsDialog.loggingConfigFile")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelLoggingConfigFile.setLayoutData(data);
        
        String log4j2ConfFile = LoggingUtil.getLoggingConfigFile();
        Text textLoggingFileName = new Text(restComp, SWT.LEFT);
        textLoggingFileName.setText(log4j2ConfFile); //$NON-NLS-1$
        textLoggingFileName.setEditable(false);
        
        Label labelLoggingLevel = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelLoggingLevel.setText(Labels.getString("AdvancedSettingsDialog.loggingLevel")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelLoggingLevel.setLayoutData(data);

        comboLoggingLevelDropdown = new Combo(restComp, SWT.DROP_DOWN);
        comboLoggingLevelDropdown.setItems(LOGGING_LEVEL);
        String currentLoggingLevel = LoggingUtil.getLoggingLevel().toUpperCase();
        if (currentLoggingLevel == null || currentLoggingLevel.isBlank()) {
            currentLoggingLevel= LoggingUtil.getLoggingLevel();
        }
        int currentLoggingLevelIndex = 0;
        for (String level : LOGGING_LEVEL) {
            if (currentLoggingLevel.equals(level)) {
                break;
            }
            currentLoggingLevelIndex++;
        }
        if (currentLoggingLevelIndex == LOGGING_LEVEL.length) {
            currentLoggingLevelIndex = 1;
        }
        comboLoggingLevelDropdown.select(currentLoggingLevelIndex);
        if (log4j2ConfFile == null || !log4j2ConfFile.endsWith(".properties")) {
            comboLoggingLevelDropdown.setEnabled(false); // Can't modify current setting
        }

        Label labelConfigDir = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelConfigDir.setText(Labels.getString("AdvancedSettingsDialog.configDir")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelConfigDir.setLayoutData(data);
        Text textConfigDirLocation = new Text(restComp, SWT.LEFT);
        textConfigDirLocation.setText(AppUtil.getConfigurationsDir()); //$NON-NLS-1$
        textConfigDirLocation.setEditable(false);

        Label labelLoggingFile = new Label(restComp, SWT.RIGHT | SWT.WRAP);
        labelLoggingFile.setText(Labels.getString("AdvancedSettingsDialog.latestLoggingFile")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelLoggingFile.setLayoutData(data);
        Text textLoggingFileLocation = new Text(restComp, SWT.LEFT);
        textLoggingFileLocation.setText(LoggingUtil.getLatestLoggingFile()); //$NON-NLS-1$
        textLoggingFileLocation.setEditable(false);

        //the bottow separator
        Label labelSeparatorBottom = new Label(sc, SWT.SEPARATOR | SWT.HORIZONTAL);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        labelSeparatorBottom.setLayoutData(data);

        //ok cancel buttons
        new Label(sc, SWT.NONE);

        // END MIDDLE COMPONENT

        // START BOTTOM COMPONENT

        Composite buttonComp = new Composite(shell, SWT.NONE);
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
                Config config = getController().getConfig();

                String currentTextEndpoint = textEndpoint.getText();
                if (currentTextEndpoint != null && !currentTextEndpoint.isEmpty() && !AppUtil.isValidHttpsUrl(currentTextEndpoint)) {
                    MessageDialog alert = new MessageDialog(getParent().getShell(), "Warning", null,
                            Labels.getFormattedString("AdvancedSettingsDialog.serverURLInfo", currentTextEndpoint),
                            MessageDialog.ERROR, new String[]{"OK"}, 0);
                    alert.open();
                    return;

                }
                //set the configValues
                config.setValue(Config.HIDE_WELCOME_SCREEN, buttonHideWelcomeScreen.getSelection());
                config.setValue(Config.SHOW_LOADER_UPGRADE_SCREEN, buttonShowLoaderUpgradeScreen.getSelection());
                config.setValue(Config.INSERT_NULLS, buttonNulls.getSelection());
                config.setValue(Config.IMPORT_BATCH_SIZE, textImportBatchSize.getText());
                boolean isOtherDelimiterSpecified = textUploadCSVDelimiterValue.getText() != null
                                                    && textUploadCSVDelimiterValue.getText().length() != 0;
                if (!buttonCsvComma.getSelection()
                        && !buttonCsvTab.getSelection()
                        && !isOtherDelimiterSpecified) {
                    MessageDialog alert = new MessageDialog(getParent().getShell(), "Warning", null,
                            Labels.getString("AdvancedSettingsDialog.checkUploadDelimiterCheckbox"),
                            MessageDialog.ERROR, new String[]{"OK"}, 0);
                    alert.open();
                    return;
                }
                config.setValue(Config.CSV_DELIMITER_OTHER_VALUE, textUploadCSVDelimiterValue.getText());
                String queryResultsDelimiterStr = textQueryResultsDelimiterValue.getText();
                if (queryResultsDelimiterStr.length() == 0) {
                    queryResultsDelimiterStr = AppUtil.COMMA; // set to default
                }
                config.setValue(Config.CSV_DELIMITER_FOR_QUERY_RESULTS, queryResultsDelimiterStr);
                config.setValue(Config.CSV_DELIMITER_COMMA, buttonCsvComma.getSelection());
                config.setValue(Config.CSV_DELIMITER_TAB, buttonCsvTab.getSelection());
                config.setValue(Config.CSV_DELIMITER_OTHER, isOtherDelimiterSpecified);

                config.setValue(Config.EXPORT_BATCH_SIZE, textExportBatchSize.getText());
                config.setValue(Config.ENDPOINT, currentTextEndpoint);
                config.setValue(Config.ASSIGNMENT_RULE, textRule.getText());
                config.setValue(Config.LOAD_ROW_TO_START_AT, textRowToStart.getText());
                config.setValue(Config.RESET_URL_ON_LOGIN, buttonResetUrl.getSelection());
                config.setValue(Config.NO_COMPRESSION, buttonCompression.getSelection());
                config.setValue(Config.TRUNCATE_FIELDS, buttonTruncateFields.getSelection());
                config.setValue(Config.FORMAT_PHONE_FIELDS, buttonFormatPhoneFields.getSelection());
                config.setValue(Config.TIMEOUT_SECS, textTimeout.getText());
                config.setValue(Config.SORT_EXTRACT_FIELDS, buttonSortExtractFields.getSelection());
                config.setValue(Config.LIMIT_OUTPUT_TO_QUERY_FIELDS, buttonLimitQueryResultColumnsToFieldsInQuery.getSelection());
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
                config.setValue(Config.PROCESS_KEEP_ACCOUNT_TEAM, buttonKeepAccountTeam.getSelection());
                config.setValue(Config.CACHE_DESCRIBE_GLOBAL_RESULTS, buttonCacheDescribeGlobalResults.getSelection());
                config.setValue(Config.INCLUDE_RICH_TEXT_FIELD_DATA_IN_QUERY_RESULTS, buttonIncludeRTFBinaryDataInQueryResults.getSelection());

                // Config requires Bulk API AND Bulk V2 API settings enabled to use Bulk V2 features
                // This is different from UI. UI shows them as mutually exclusive.
                config.setValue(Config.BULK_API_ENABLED, useBulkAPI || useBulkV2API);
                config.setValue(Config.BULK_API_SERIAL_MODE, buttonBulkApiSerialMode.getSelection());
                config.setValue(Config.BULK_API_ZIP_CONTENT, buttonBulkApiZipContent.getSelection());
                config.setValue(Config.BULKV2_API_ENABLED, useBulkV2API);
                config.setValue(Config.OAUTH_LOGIN_FROM_BROWSER, buttonLoginFromBrowser.getSelection());
                config.setValue(Config.WIZARD_CLOSE_ON_FINISH, buttonCloseWizardOnFinish.getSelection());
                config.setValue(Config.WIZARD_WIDTH, textWizardWidth.getText());
                config.setValue(Config.WIZARD_HEIGHT, textWizardHeight.getText());

                config.setValue(Config.WIZARD_POPULATE_RESULTS_FOLDER_WITH_PREVIOUS_OP_RESULTS_FOLDER, buttonPopulateResultsFolderOnWizardFinishStep.getSelection());
                LoggingUtil.setLoggingLevel(LOGGING_LEVEL[comboLoggingLevelDropdown.getSelectionIndex()]);
                String clientIdVal = textProductionPartnerClientID.getText();
                if (clientIdVal != null && !clientIdVal.strip().isEmpty()) {
                    String propName = Config.OAUTH_PREFIX + Config.OAUTH_PROD_ENVIRONMENT_VAL + "." + Config.OAUTH_PARTIAL_PARTNER_CLIENTID;
                    String currentClientIdVal = config.getString(propName);
                    if (!clientIdVal.equals(currentClientIdVal)) {
                        config.setValue(propName, clientIdVal);
                        getController().logout();
                    }
                }
                clientIdVal = textSandboxPartnerClientID.getText();
                if (clientIdVal != null && !clientIdVal.strip().isEmpty()) {
                    String propName = Config.OAUTH_PREFIX + Config.OAUTH_SB_ENVIRONMENT_VAL + "." + Config.OAUTH_PARTIAL_PARTNER_CLIENTID;
                    String currentClientIdVal = config.getString(propName);
                    if (!clientIdVal.equals(currentClientIdVal)) {
                    	config.setValue(propName, clientIdVal);
                        getController().logout();
                    }
                }
                clientIdVal = textProductionBulkClientID.getText();
                if (clientIdVal != null && !clientIdVal.strip().isEmpty()) {
                    String propName = Config.OAUTH_PREFIX + Config.OAUTH_PROD_ENVIRONMENT_VAL + "." + Config.OAUTH_PARTIAL_BULK_CLIENTID;
                    String currentClientIdVal = config.getString(propName);
                    if (!clientIdVal.equals(currentClientIdVal)) {
                        config.setValue(propName, clientIdVal);
                        getController().logout();
                    }
                }
                clientIdVal = textSandboxBulkClientID.getText();
                if (clientIdVal != null && !clientIdVal.strip().isEmpty()) {
                    String propName = Config.OAUTH_PREFIX + Config.OAUTH_SB_ENVIRONMENT_VAL + "." + Config.OAUTH_PARTIAL_BULK_CLIENTID;
                    String currentClientIdVal = config.getString(propName);
                    if (!clientIdVal.equals(currentClientIdVal)) {
                        config.setValue(propName, clientIdVal);
                        getController().logout();
                    }
                }
                getController().saveConfig();
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
        
        empty = new Label(buttonComp, SWT.NONE);
        data = new GridData();
        data.horizontalSpan = 2;
        empty.setLayoutData(data);

        // Set the child as the scrolled content of the ScrolledComposite
        sc.setContent(container);

        // Set the minimum size
        sc.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
              Rectangle r = sc.getClientArea();
              sc.setMinSize(container.computeSize(r.width, SWT.DEFAULT));
            }
          });
        sc.setAlwaysShowScrollBars(true);

        // Expand both horizontally and vertically
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);
        shell.redraw();
    }

    private Text createTimezoneTextInput(Composite parent, String labelKey, String configKey, String defaultValue, int widthHint) {
        createLabel(parent, labelKey);
        
        Composite timezoneComp = new Composite(parent, SWT.RIGHT);
        GridData data = new GridData(GridData.FILL_BOTH);
        timezoneComp.setLayoutData(data);
        GridLayout layout = new GridLayout(2, false);
        layout.verticalSpacing = 10;
        timezoneComp.setLayout(layout);

        final Text t = new Text(timezoneComp, SWT.BORDER);
        final GridData gd = new GridData();
        if (widthHint > 0) gd.widthHint = widthHint;
        t.setLayoutData(gd);
        String val = getController().getConfig().getString(configKey);
        if ("".equals(val) && defaultValue != null) val = defaultValue;
        t.setText(String.valueOf(val));
        
        buttonLocalSystemTimezone = new Button(timezoneComp, SWT.PUSH | SWT.FLAT);
        buttonLocalSystemTimezone.setText(Labels.getString("AdvancedSettingsDialog.setClientSystemTimezone")); //$NON-NLS-1$
        buttonLocalSystemTimezone.setToolTipText(Labels.getString("AdvancedSettingsDialog.TooltipSetClientSystemTimezone"));
        buttonLocalSystemTimezone.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                t.setText(TimeZone.getDefault().getID());
            }
        });
        return t;
    }


    private void createLabel(Composite parent, String labelKey) {
        Label l = new Label(parent, SWT.RIGHT | SWT.WRAP);
        l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        l.setText(Labels.getString(labelKey));
    }
}
