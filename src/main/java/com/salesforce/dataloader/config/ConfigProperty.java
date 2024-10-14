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

import java.util.HashMap;

import com.salesforce.dataloader.ui.Labels;

/*
 * Data class capturing information about configuration property (aka Setting).
 */
public class ConfigProperty {
    private static final HashMap<String, ConfigProperty> propertiesMap = new HashMap<String, ConfigProperty>();
    
    private final String name;
    private String defaultValue = "";
    private String value = "";
    private boolean readonly;
    private boolean encrypted;
    private boolean commandLineOption;
    private final String uiLabelTemplate;
    private final String uiTooltipTemplate;

    public ConfigProperty(String name) {
        this.name = name;
        this.uiLabelTemplate = Labels.getString("AdvancedSettingsDialog.uiLabel." + name);
        String tooltipText = null;
        tooltipText = Labels.getString("AdvancedSettingsDialog.uiTooltip." + name);
        if (tooltipText != null && tooltipText.startsWith("!") && tooltipText.endsWith("!")) {
            tooltipText = null;
        }
        String[] propArg = {this.name};
        try {
            if (tooltipText == null) {
                tooltipText = Labels.getFormattedString("AdvancedSettingsDialog.TooltipPropertyName", propArg);
            } else {
                tooltipText += "\n\n";
                tooltipText += Labels.getFormattedString("AdvancedSettingsDialog.TooltipPropertyName", propArg);
            }
        } catch (java.util.MissingResourceException e) {
            // do nothing
        }
        if (tooltipText != null && tooltipText.startsWith("!") && tooltipText.endsWith("!")) {
            tooltipText = null;
        }
        if (tooltipText == null) {
            this.uiTooltipTemplate = "";
        } else {
            this.uiTooltipTemplate = tooltipText;
        };
    }
    
    public static HashMap<String, ConfigProperty> getPropertiesMap() {
        return propertiesMap;
    }
    public String getDefaultValue() {
        return defaultValue;
    }
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public boolean isReadonly() {
        return readonly;
    }
    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
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
}