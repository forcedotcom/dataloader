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
    private Text labelNulls;
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

    private Button buttonShowWelcomeScreen;
    private Button buttonShowLoaderUpgradeScreen;
    private Button buttonOutputExtractStatus;
    private Button buttonSortExtractFields;
    private Button buttonLimitQueryResultColumnsToFieldsInQuery;
    private Button buttonReadUtf8;
    private Button buttonWriteUtf8;
    private Button buttonEuroDates;
    private Button buttonTruncateFields;
    private Text   labelTruncateFields;
    private Button buttonFormatPhoneFields;
    private Button buttonKeepAccountTeam;
    private Button buttonUpdateWithExternalId;
    private Text   labelUpdateWithExternalId;
    private Button buttonCacheDescribeGlobalResults;
    private Button buttonIncludeRTFBinaryDataInQueryResults;
    private Button buttonUseSOAPApi;
    private Button buttonUseBulkV1Api;
    private Button buttonUseBulkV2Api;
    private Button buttonBulkApiSerialMode;
    private Button buttonBulkApiZipContent;
    private Text  labelBulkApiSerialMode;
    private Text  labelBulkApiZipContent;
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
            for (Control child : comp.getChildren()) {
                if (enabled && comp == this.soapApiOptionsComposite) {
                    setEnabled(child, !this.buttonUpdateWithExternalId.getSelection());
                } else {
                    setEnabled(child, enabled);
                }
            }
            if (enabled && comp == this.soapApiOptionsComposite) {
                setEnabled(buttonUpdateWithExternalId, true);
                setEnabled(labelUpdateWithExternalId, true);
                setEnabled(buttonNulls, true);
                setEnabled(labelNulls, true);
                setEnabled(buttonTruncateFields, true);
                setEnabled(labelTruncateFields, true);
            }
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
        if (apiOptionsComposite != null) {
            setEnabled(apiOptionsComposite, isEnabled);
        }
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
        Text dialogMessage = new Text(topComp, SWT.NONE | SWT.READ_ONLY);
        dialogMessage.setText(getMessage());
        dialogMessage.setToolTipText(Labels.getString("AdvancedSettingsDialog.TooltipMessage"));
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.heightHint = 30;
        data.widthHint = 370;

        Font f = dialogMessage.getFont();
        FontData[] farr = f.getFontData();
        FontData fd = farr[0];
        fd.setStyle(SWT.BOLD);
        dialogMessage.setFont(new Font(Display.getCurrent(), fd));

        dialogMessage.setLayoutData(data);
        dialogMessage.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

        Label labelSeparator = new Label(topComp, SWT.SEPARATOR | SWT.HORIZONTAL);
        data = new GridData(GridData.FILL_HORIZONTAL);
      //  data.heightHint = 10;
        labelSeparator.setBackground(getParent().getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
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
        Text labelShowWelcomeScreen = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelShowWelcomeScreen.setText(Labels.getString("AdvancedSettingsDialog.showWelcomeScreen")); //$NON-NLS-1$
        labelShowWelcomeScreen.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        buttonShowWelcomeScreen = new Button(restComp, SWT.CHECK);
        buttonShowWelcomeScreen.setSelection(!config.getBoolean(Config.HIDE_WELCOME_SCREEN));

        // Hide welcome screen
        Text labelShowLoaderUpgradeScreen = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelShowLoaderUpgradeScreen.setText(Labels.getString("AdvancedSettingsDialog.showLoaderUpgradeScreen")); //$NON-NLS-1$
        labelShowLoaderUpgradeScreen.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        buttonShowLoaderUpgradeScreen = new Button(restComp, SWT.CHECK);
        buttonShowLoaderUpgradeScreen.setSelection(config.getBoolean(Config.SHOW_LOADER_UPGRADE_SCREEN));

        blank = new Label(restComp, SWT.NONE);
        data = new GridData();
        data.horizontalSpan = 2;
        data.heightHint = 15;
        blank.setLayoutData(data);

        labelSeparator = new Label(restComp, SWT.SEPARATOR | SWT.HORIZONTAL);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalIndent = 100;
        data.horizontalSpan = 2;
        labelSeparator.setLayoutData(data);
        
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
                
                // get default batch size for Bulk v2 and set it
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

        Text labelKeepAccountTeam = new Text(this.soapApiOptionsComposite, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
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
        buttonKeepAccountTeam.setToolTipText(Labels.getString("AdvancedSettingsDialog.TooltipKeepAccountTeam"));
        labelKeepAccountTeam.setToolTipText(Labels.getString("AdvancedSettingsDialog.TooltipKeepAccountTeam"));

        // update using external id
        labelUpdateWithExternalId = new Text(this.soapApiOptionsComposite, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelUpdateWithExternalId.setText(Labels.getString("AdvancedSettingsDialog.updateWithExternalId"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        data.grabExcessHorizontalSpace = true;
        labelUpdateWithExternalId.setLayoutData(data);

        boolean updateWithExternalId = config.getBoolean(Config.UPDATE_WITH_EXTERNALID);
        buttonUpdateWithExternalId = new Button(this.soapApiOptionsComposite, SWT.CHECK);
        buttonUpdateWithExternalId.setSelection(updateWithExternalId);
        data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        data.grabExcessHorizontalSpace = true;
        buttonUpdateWithExternalId.setLayoutData(data);
        buttonUpdateWithExternalId.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                setEnabled(soapApiOptionsComposite, true);
           }
        });

        //insert Nulls
        labelNulls = new Text(this.soapApiOptionsComposite, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelNulls.setText(Labels.getString("AdvancedSettingsDialog.insertNulls")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        data.grabExcessHorizontalSpace = true;
        labelNulls.setLayoutData(data);
        buttonNulls = new Button(this.soapApiOptionsComposite, SWT.CHECK);
        buttonNulls.setSelection(config.getBoolean(Config.INSERT_NULLS));

        //Field truncation
        labelTruncateFields = new Text(this.soapApiOptionsComposite, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelTruncateFields.setText(Labels.getString("AdvancedSettingsDialog.allowFieldTruncation"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        data.grabExcessHorizontalSpace = true;
        labelTruncateFields.setLayoutData(data);

        buttonTruncateFields = new Button(this.soapApiOptionsComposite, SWT.CHECK);
        buttonTruncateFields.setSelection(config.getBoolean(Config.TRUNCATE_FIELDS));
        
        //insert compression
        Text labelCompression = new Text(soapApiOptionsComposite, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelCompression.setText(Labels.getString("AdvancedSettingsDialog.compression")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelCompression.setLayoutData(data);
        buttonCompression = new Button(soapApiOptionsComposite, SWT.CHECK);
        buttonCompression.setSelection(config.getBoolean(Config.NO_COMPRESSION));
        buttonCompression.setToolTipText(Labels.getString("AdvancedSettingsDialog.TooltipCompression"));
        labelCompression.setToolTipText(Labels.getString("AdvancedSettingsDialog.TooltipCompression"));

        //timeout size
        Text labelTimeout = new Text(soapApiOptionsComposite, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelTimeout.setText(Labels.getString("AdvancedSettingsDialog.timeout")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelTimeout.setLayoutData(data);

        textTimeout = new Text(soapApiOptionsComposite, SWT.BORDER);
        textTimeout.setText(config.getString(Config.TIMEOUT_SECS));
        textTimeout.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent event) {
                event.doit = Character.isISOControl(event.character) || Character.isDigit(event.character);
            }
        });
        data = new GridData();
        textTimeout.setTextLimit(4);
        GC gc = new GC(textTimeout);
        Point textSize = gc.textExtent("8");
        gc.dispose();
        data.widthHint = 4 * textSize.x;
        textTimeout.setLayoutData(data);
        textTimeout.setToolTipText(Labels.getString("AdvancedSettingsDialog.TooltipTimeout"));
        labelTimeout.setToolTipText(Labels.getString("AdvancedSettingsDialog.TooltipTimeout"));

        // Bulk API serial concurrency mode setting
        this.bulkApiOptionsComposite = new Composite(restComp, SWT.None);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        data.grabExcessHorizontalSpace = true;
        this.bulkApiOptionsComposite.setLayoutData(data);
        layout = new GridLayout(2, true);
        layout.verticalSpacing = 10;
        this.bulkApiOptionsComposite.setLayout(layout);

        labelBulkApiSerialMode = new Text(this.bulkApiOptionsComposite, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
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
        labelBulkApiZipContent = new Text(this.bulkApiOptionsComposite, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
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

        Text labelBatch = new Text(importBatchSizeComposite, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
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
        Text labelQueryBatch = new Text(exportBatchSizeComposite, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
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
        
        labelSeparator = new Label(restComp, SWT.SEPARATOR | SWT.HORIZONTAL);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalIndent = 100;
        data.horizontalSpan = 2;
        labelSeparator.setLayoutData(data);

        blank = new Label(restComp, SWT.NONE);
        data = new GridData();
        data.horizontalSpan = 2;
        data.heightHint = 15;
        blank.setLayoutData(data);
        
        //assignment rules
        Text labelRule = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelRule.setText(Labels.getString("AdvancedSettingsDialog.assignmentRule")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelRule.setLayoutData(data);

        textRule = new Text(restComp, SWT.BORDER);
        data = new GridData();
        textRule.setTextLimit(18);
        data.widthHint = 18 * textSize.x;
        textRule.setLayoutData(data);
        textRule.setText(config.getString(Config.ASSIGNMENT_RULE));
        textRule.setToolTipText(Labels.getString("AdvancedSettingsDialog.TooltipAssignmentRule"));
        labelRule.setToolTipText(Labels.getString("AdvancedSettingsDialog.TooltipAssignmentRule"));

        //endpoint
        Text labelEndpoint = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
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
        Text labelResetUrl = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelResetUrl.setText(Labels.getString("AdvancedSettingsDialog.resetUrlOnLogin")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelResetUrl.setLayoutData(data);
        buttonResetUrl = new Button(restComp, SWT.CHECK);
        buttonResetUrl.setSelection(config.getBoolean(Config.RESET_URL_ON_LOGIN));

        // enable/disable sort of fields to extract
        Text labelSortExtractFields = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelSortExtractFields.setText(Labels.getString("AdvancedSettingsDialog.sortQueryFieldsInExtraction")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelSortExtractFields.setLayoutData(data);

        buttonSortExtractFields = new Button(restComp, SWT.CHECK);
        buttonSortExtractFields.setSelection(config.getBoolean(Config.SORT_EXTRACT_FIELDS));
        
        // enable/disable limiting query result columns to fields specified in the SOQL query
        Text labelLimitQueryResultColumnsToFieldsInQuery = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelLimitQueryResultColumnsToFieldsInQuery.setText(Labels.getString(this.getClass().getSimpleName() + ".limitOutputToQueryFields")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelLimitQueryResultColumnsToFieldsInQuery.setLayoutData(data);
       
        buttonLimitQueryResultColumnsToFieldsInQuery = new Button(restComp, SWT.CHECK);
        buttonLimitQueryResultColumnsToFieldsInQuery.setSelection(config.getBoolean(Config.LIMIT_OUTPUT_TO_QUERY_FIELDS));

        //enable/disable output of success file for extracts
        Text labelOutputExtractStatus = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelOutputExtractStatus.setText(Labels.getString("AdvancedSettingsDialog.outputExtractStatus")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelOutputExtractStatus.setLayoutData(data);

        buttonOutputExtractStatus = new Button(restComp, SWT.CHECK);
        buttonOutputExtractStatus.setSelection(config.getBoolean(Config.ENABLE_EXTRACT_STATUS_OUTPUT));

        //utf-8 for loading
        Text labelReadUTF8 = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelReadUTF8.setText(Labels.getString("AdvancedSettingsDialog.readUTF8")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelReadUTF8.setLayoutData(data);

        buttonReadUtf8 = new Button(restComp, SWT.CHECK);
        buttonReadUtf8.setSelection(config.getBoolean(Config.READ_UTF8));

        //utf-8 for extraction
        Text labelWriteUTF8 = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelWriteUTF8.setText(Labels.getString("AdvancedSettingsDialog.writeUTF8")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelWriteUTF8.setLayoutData(data);

        buttonWriteUtf8 = new Button(restComp, SWT.CHECK);
        buttonWriteUtf8.setSelection(config.getBoolean(Config.WRITE_UTF8));

        //European Dates
        Text labelEuropeanDates = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelEuropeanDates.setText(Labels.getString("AdvancedSettingsDialog.useEuropeanDateFormat")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelEuropeanDates.setLayoutData(data);

        buttonEuroDates = new Button(restComp, SWT.CHECK);
        buttonEuroDates.setSelection(config.getBoolean(Config.EURO_DATES));

        //format phone fields on the client side
        Text labelFormatPhoneFields = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelFormatPhoneFields.setText(Labels.getString("AdvancedSettingsDialog.formatPhoneFields"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelFormatPhoneFields.setLayoutData(data);

        buttonFormatPhoneFields = new Button(restComp, SWT.CHECK);
        buttonFormatPhoneFields.setSelection(config.getBoolean(Config.FORMAT_PHONE_FIELDS));

        Text labelCsvCommand = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelCsvCommand.setText(Labels.getString("AdvancedSettingsDialog.useCommaAsCsvDelimiter"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelCsvCommand.setLayoutData(data);
        buttonCsvComma = new Button(restComp, SWT.CHECK);
        buttonCsvComma.setSelection(config.getBoolean(Config.CSV_DELIMITER_COMMA));

        Text labelTabCommand = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelTabCommand.setText(Labels.getString("AdvancedSettingsDialog.useTabAsCsvDelimiter"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelTabCommand.setLayoutData(data);
        buttonCsvTab = new Button(restComp, SWT.CHECK);
        buttonCsvTab.setSelection(config.getBoolean(Config.CSV_DELIMITER_TAB));

        Text labelOtherDelimiterValue = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelOtherDelimiterValue.setText(Labels.getString("AdvancedSettingsDialog.csvOtherDelimiterValue"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelOtherDelimiterValue.setLayoutData(data);
        textUploadCSVDelimiterValue = new Text(restComp, SWT.BORDER);
        textUploadCSVDelimiterValue.setText(config.getString(Config.CSV_DELIMITER_OTHER_VALUE));
        data = new GridData();
        data.widthHint = 15 * textSize.x;
        textUploadCSVDelimiterValue.setLayoutData(data);

        Text labelQueryResultsDelimiter = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
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
        Text labelIncludeRTFBinaryDataInQueryResults = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelIncludeRTFBinaryDataInQueryResults.setText(Labels.getString("AdvancedSettingsDialog.includeRTFBinaryDataInQueryResults"));
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelIncludeRTFBinaryDataInQueryResults.setLayoutData(data);

        boolean includeRTFBinaryDataInQueryResults = config.getBoolean(Config.INCLUDE_RICH_TEXT_FIELD_DATA_IN_QUERY_RESULTS);
        buttonIncludeRTFBinaryDataInQueryResults = new Button(restComp, SWT.CHECK);
        buttonIncludeRTFBinaryDataInQueryResults.setSelection(includeRTFBinaryDataInQueryResults);

        // Cache DescribeGlobal results across operations
        Text labelCacheDescribeGlobalResults = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
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
        Text labelProxyHost = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelProxyHost.setText(Labels.getString("AdvancedSettingsDialog.proxyHost")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelProxyHost.setLayoutData(data);

        textProxyHost = new Text(restComp, SWT.BORDER);
        textProxyHost.setText(config.getString(Config.PROXY_HOST));
        data = new GridData(GridData.FILL_HORIZONTAL);
        textProxyHost.setLayoutData(data);

        //Proxy Port
        Text labelProxyPort = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
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
        Text labelProxyUsername = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelProxyUsername.setText(Labels.getString("AdvancedSettingsDialog.proxyUser")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelProxyUsername.setLayoutData(data);

        textProxyUsername = new Text(restComp, SWT.BORDER);
        textProxyUsername.setText(config.getString(Config.PROXY_USERNAME));
        data = new GridData();
        data.widthHint = 20 * textSize.x;
        textProxyUsername.setLayoutData(data);

        //Proxy Password
        Text labelProxyPassword = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelProxyPassword.setText(Labels.getString("AdvancedSettingsDialog.proxyPassword")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelProxyPassword.setLayoutData(data);

        textProxyPassword = new Text(restComp, SWT.BORDER | SWT.PASSWORD);
        textProxyPassword.setText(config.getString(Config.PROXY_PASSWORD));
        data = new GridData();
        data.widthHint = 20 * textSize.x;
        textProxyPassword.setLayoutData(data);


        //proxy NTLM domain
        Text labelProxyNtlmDomain = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
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
        
        Text loginFromBrowserCheckboxText = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        loginFromBrowserCheckboxText.setText(Labels.getString("AdvancedSettingsDialog.loginFromBrowser"));
        loginFromBrowserCheckboxText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        boolean doLoginFromBrowser = config.getBoolean(Config.OAUTH_LOGIN_FROM_BROWSER);
        buttonLoginFromBrowser = new Button(restComp, SWT.CHECK);
        buttonLoginFromBrowser.setSelection(doLoginFromBrowser);
        
        Text clientIdInProductionText = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        clientIdInProductionText.setText(Labels.getString("AdvancedSettingsDialog.partnerClientIdInProduction"));
        clientIdInProductionText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        this.textProductionPartnerClientID = new Text(restComp, SWT.NONE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        textProductionPartnerClientID.setLayoutData(data);
    	String clientId = config.getOAuthEnvironmentString(Config.OAUTH_PROD_ENVIRONMENT_VAL, Config.OAUTH_PARTIAL_PARTNER_CLIENTID);
    	this.textProductionPartnerClientID.setText(clientId);
        
        clientIdInProductionText = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        clientIdInProductionText.setText(Labels.getString("AdvancedSettingsDialog.bulkClientIdInProduction"));
        clientIdInProductionText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        this.textProductionBulkClientID = new Text(restComp, SWT.NONE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        textProductionBulkClientID.setLayoutData(data);
        clientId = config.getOAuthEnvironmentString(Config.OAUTH_PROD_ENVIRONMENT_VAL, Config.OAUTH_PARTIAL_BULK_CLIENTID);
        this.textProductionBulkClientID.setText(clientId);
        
        Text clientIdInSandboxText = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        clientIdInSandboxText.setText(Labels.getString("AdvancedSettingsDialog.partnerClientIdInSandbox"));
        clientIdInSandboxText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        this.textSandboxPartnerClientID = new Text(restComp, SWT.NONE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        textSandboxPartnerClientID.setLayoutData(data);
    	clientId = config.getOAuthEnvironmentString(Config.OAUTH_SB_ENVIRONMENT_VAL, Config.OAUTH_PARTIAL_PARTNER_CLIENTID);
    	this.textSandboxPartnerClientID.setText(clientId);
        
        clientIdInSandboxText = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
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

        Text labelRowToStart = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
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
        
        Text closeWizardOnFinishCheckboxText = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        closeWizardOnFinishCheckboxText.setText(Labels.getString("AdvancedSettingsDialog.closeWizardOnFinish"));
        closeWizardOnFinishCheckboxText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        boolean closeWizardOnFinish = config.getBoolean(Config.WIZARD_CLOSE_ON_FINISH);
        buttonCloseWizardOnFinish = new Button(restComp, SWT.CHECK);
        buttonCloseWizardOnFinish.setSelection(closeWizardOnFinish);

        Text labelWizardWidthAndHeight = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
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

        Text populateResultsFolderOnWizardFinishStepText = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        populateResultsFolderOnWizardFinishStepText.setText(Labels.getString("AdvancedSettingsDialog.populateResultsFolderOnFinishStepOfWizard"));
        populateResultsFolderOnWizardFinishStepText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        boolean populateResultsFolderOnFinishStep = config.getBoolean(Config.WIZARD_POPULATE_RESULTS_FOLDER_WITH_PREVIOUS_OP_RESULTS_FOLDER);
        buttonPopulateResultsFolderOnWizardFinishStep = new Button(restComp, SWT.CHECK);
        buttonPopulateResultsFolderOnWizardFinishStep.setSelection(populateResultsFolderOnFinishStep);
        
        blankAgain = new Label(restComp, SWT.NONE);
        data = new GridData();
        data.horizontalSpan = 2;
        blankAgain.setLayoutData(data);

        Text labelConfigDir = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelConfigDir.setText(Labels.getString("AdvancedSettingsDialog.configDir")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelConfigDir.setLayoutData(data);
        Text textConfigDirLocation = new Text(restComp, SWT.LEFT);
        textConfigDirLocation.setText(AppUtil.getConfigurationsDir()); //$NON-NLS-1$
        textConfigDirLocation.setEditable(false);

        Text labelLoggingConfigFile = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        labelLoggingConfigFile.setText(Labels.getString("AdvancedSettingsDialog.loggingConfigFile")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        labelLoggingConfigFile.setLayoutData(data);
        
        String log4j2ConfFile = LoggingUtil.getLoggingConfigFile();
        Text textLoggingFileName = new Text(restComp, SWT.LEFT | SWT.READ_ONLY);
        textLoggingFileName.setText(log4j2ConfFile); //$NON-NLS-1$
        
        Text labelLoggingLevel = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
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

        Text labelLoggingFile = new Text(restComp, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
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
                config.setValue(Config.HIDE_WELCOME_SCREEN, !buttonShowWelcomeScreen.getSelection());
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
                config.setValue(Config.UPDATE_WITH_EXTERNALID, buttonUpdateWithExternalId.getSelection());
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
        createLabel(parent, labelKey, null);
        
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


    private Text createLabel(Composite parent, String labelKey, String tooltip) {
        Text l = new Text(parent, SWT.RIGHT | SWT.WRAP | SWT.READ_ONLY);
        l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        l.setText(Labels.getString(labelKey));
        if (tooltip != null) {
            l.setToolTipText(tooltip);
        }
        return l;
    }
}
