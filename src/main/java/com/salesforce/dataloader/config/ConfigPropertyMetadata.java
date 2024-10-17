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
package com.salesforce.dataloader.config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import com.salesforce.dataloader.dao.csv.CSVFileWriter;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import com.salesforce.dataloader.model.Row;
import com.salesforce.dataloader.ui.Labels;
import com.salesforce.dataloader.util.AppUtil;

/*
 * Data class capturing information about configuration property (aka Setting).
 */
public class ConfigPropertyMetadata {
    private static final HashMap<String, ConfigPropertyMetadata> propertiesMap = new HashMap<String, ConfigPropertyMetadata>();
    
    private final String name;
    private String defaultValue = "";
    private boolean readOnly = false;
    private boolean encrypted = false;
    private boolean internal = false;
    private boolean commandLineOption = false;
    private String uiLabelTemplate = "";
    private String uiTooltipTemplate = "";
    private String description = "";

    static {
        Field[] appConfigFields = AppConfig.class.getDeclaredFields();
        for (Field configField : appConfigFields) {
            if ((configField.getName().startsWith("PROP_") && !configField.getName().startsWith("PROP_SFDC_INTERNAL")) 
                || configField.getName().startsWith("CLI_OPTION_")) {
                String propName;
                try {
                    propName = configField.get(null).toString();
                } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
                    continue;
                }
                ConfigPropertyMetadata configProp = new ConfigPropertyMetadata(propName);
                configProp.setEncrypted(AppConfig.isEncryptedProperty(propName));
                configProp.setReadOnly(AppConfig.isReadOnlyProperty(propName));
                configProp.setInternal(AppConfig.isInternalProperty(propName));
                propertiesMap.put(propName, configProp);
            }
        }
        Field[] appUtilFields = AppUtil.class.getDeclaredFields();
        for (Field configField : appUtilFields) {
            if (configField.getName().startsWith("CLI_OPTION_")) {
                String propName;
                try {
                    propName = configField.get(null).toString();
                } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
                    continue;
                }
                ConfigPropertyMetadata configProp = new ConfigPropertyMetadata(propName);
                configProp.setReadOnly(true);
                propertiesMap.put(propName, configProp);
            }
        }
    }

    public ConfigPropertyMetadata(String propName) {
        this.name = propName;
        this.uiLabelTemplate = Labels.getString("AdvancedSettingsDialog.uiLabel." + propName);
        if (this.uiLabelTemplate == null 
                || (this.uiLabelTemplate.startsWith("!") && this.uiLabelTemplate.endsWith("!"))) {
            this.uiLabelTemplate = "";
        }
        String tooltipText = null;
        tooltipText = Labels.getString("AdvancedSettingsDialog.uiTooltip." + propName);
        if (tooltipText != null && tooltipText.startsWith("!") && tooltipText.endsWith("!")) {
            tooltipText = null;
        }
        if (tooltipText == null 
                || (tooltipText.startsWith("!") && tooltipText.endsWith("!"))) {
            tooltipText = null;
        }
        if (tooltipText == null) {
            this.uiTooltipTemplate = "";
        } else {
            this.uiTooltipTemplate = tooltipText;
        };
        String description = null;
        description = Labels.getString("AppConfig.property.description." + propName);
        if (description != null && description.startsWith("!") && description.endsWith("!")) {
            description = null;
        }
        if (description == null 
                || (description.startsWith("!") && description.endsWith("!"))) {
            description = null;
        }
        if (description == null) {
            this.description = "";
        } else {
            this.description = description;
        };
    }
    
    public static HashMap<String, ConfigPropertyMetadata> getPropertiesMap() {
        return propertiesMap;
    }
    public String getDefaultValue() {
        return defaultValue;
    }
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    public boolean isReadOnly() {
        return readOnly;
    }
    public void setReadOnly(boolean readonly) {
        this.readOnly = readonly;
    }
    public boolean isEncrypted() {
        return encrypted;
    }
    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }
    public boolean isCommandLineOption() {
        return commandLineOption;
    }
    public void setCommandLineOption(boolean commandLineOption) {
        this.commandLineOption = commandLineOption;
    }
    public String getUiLabelTemplate() {
        return uiLabelTemplate;
    }
    public String getUiTooltip() {
        return uiTooltipTemplate;
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }
    
    public String getName() {
        return this.name;
    }
    
    private static final String CSV_FILENAME = "properties.csv";
    
    private static final String COL_PROPERTY_NAME = "property name";
    private static final String COL_UI_LABEL = "Entry in Settings Dialog";
    private static final String COL_DESCRIPTION = "Tooltip/Description";
    private static final String COL_DEFAULT_VAL = "Default value";
    private static final String COL_IS_READ_ONLY = "Settable through Settings dialog?";
    private static final String COL_IS_COMMAND_LINE_OPTION = "Command line option only?";
    private static final String COL_IS_ENCRYPTED = "encrypted?";

    public static void printCSV(AppConfig appConfig) {
        String propsFilename = appConfig.constructConfigFilePath(CSV_FILENAME);
        File propsFile = new File(propsFilename);
        if (propsFile.exists()) {
            // delete existing file
            propsFile.delete();
        }
        try {
            propsFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        CSVFileWriter csvWriter = new CSVFileWriter(propsFilename, appConfig, AppUtil.COMMA);
        ArrayList<String> colHeaders = new ArrayList<String>();
        colHeaders.add(COL_PROPERTY_NAME);
        colHeaders.add(COL_UI_LABEL);
        colHeaders.add(COL_DESCRIPTION);
        colHeaders.add(COL_DEFAULT_VAL);
        colHeaders.add(COL_IS_READ_ONLY);
        colHeaders.add(COL_IS_COMMAND_LINE_OPTION);
        colHeaders.add(COL_IS_ENCRYPTED);

        try {
            csvWriter.open();
            csvWriter.setColumnNames(colHeaders);
        } catch (DataAccessObjectInitializationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        try {
            for (ConfigPropertyMetadata propMD : propertiesMap.values()) {
                if (propMD.isInternal()) {
                    continue;
                }
                Row row = new Row();
                row.put(COL_PROPERTY_NAME, propMD.getName());
                row.put(COL_UI_LABEL, propMD.getUiLabelTemplate().isBlank() ? "None" : propMD.getUiLabelTemplate());
                String description = propMD.getDescription();
                if (description == null || description.isBlank()) {
                    description = propMD.getUiTooltip();
                }
                row.put(COL_DESCRIPTION, description);
                row.put(COL_DEFAULT_VAL, propMD.getDefaultValue());
                row.put(COL_IS_READ_ONLY, propMD.isReadOnly());
                row.put(COL_IS_COMMAND_LINE_OPTION, propMD.isCommandLineOption());
                row.put(COL_IS_ENCRYPTED, propMD.isEncrypted());
                try {
                    csvWriter.writeRow(row);
                } catch (DataAccessObjectException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            csvWriter.close();
        }
    }

    public String getDescription() {
        return this.description;
    }
}