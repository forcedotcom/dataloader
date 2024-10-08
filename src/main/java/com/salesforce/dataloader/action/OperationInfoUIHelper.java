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
package com.salesforce.dataloader.action;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;

import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.ui.*;
import com.salesforce.dataloader.ui.LoadWizard.DeleteWizard;
import com.salesforce.dataloader.ui.LoadWizard.HardDeleteWizard;
import com.salesforce.dataloader.ui.LoadWizard.InsertWizard;
import com.salesforce.dataloader.ui.LoadWizard.UndeleteWizard;
import com.salesforce.dataloader.ui.LoadWizard.UpdateWizard;
import com.salesforce.dataloader.ui.LoadWizard.UpsertWizard;
import com.salesforce.dataloader.ui.extraction.ExtractAllWizard;
import com.salesforce.dataloader.ui.extraction.ExtractionWizard;
import com.salesforce.dataloader.ui.uiActions.OperationUIAction;
import com.sforce.async.OperationEnum;

/**
 * Enum containing data and utility methods for data loader operations.
 * 
 * @author Colin Jarvis
 */
public enum OperationInfoUIHelper {

    insert(InsertWizard.class),
    update(UpdateWizard.class),
    upsert(UpsertWizard.class),
    delete(DeleteWizard.class),
    undelete(UndeleteWizard.class),
    hard_delete(HardDeleteWizard.class),
    extract(ExtractionWizard.class),
    extract_all(ExtractAllWizard.class);

    /** all operations, in order */
    public static final OperationInfoUIHelper[] ALL_OPERATIONS_IN_ORDER = { insert, update, upsert, delete, hard_delete,
        extract, extract_all };

    private static final Logger logger = LogManager.getLogger(OperationInfoUIHelper.class);

    private final Class<? extends Wizard> wizardClass;

    private OperationInfoUIHelper(Class<? extends Wizard> wizardClass) {
        this.wizardClass = wizardClass;
    }

    private RuntimeException unsupportedInstantiation(Exception e, Class<?> cls) {
        final String message = Messages
                .getMessage(getClass(), "errorOperationInstantiation", this, String.valueOf(cls));
        logger.fatal(message);
        return new UnsupportedOperationException(message, e);
    }

    public String getIconName() {
        if (this == hard_delete) return delete.getIconName();
        if (this == upsert) return update.getIconName();
        if (this == undelete) return update.getIconName();
        if (this == extract_all) return extract.getIconName();
        return name() + "_icon";
    }

    public String getIconLocation() {
        if (this == hard_delete) return delete.getIconLocation();
        if (this == extract_all) return extract.getIconLocation();
        return "img/icons/icon_" + name() + ".gif";
    }

    public String getMenuLabel() {
        return Labels.getString(name() + ".UIAction.menuText");
    }

    public String getToolTipText() {
        return Labels.getString(name() + ".UIAction.tooltipText");
    }

    public String getLabel() {
        return Labels.getString("UI." + name());
    }

    public Wizard instantiateWizard(Controller ctl) {
        logger.info(Messages.getMessage(getClass(), "creatingWizard", this));
        try {
            return ((Class<? extends Wizard>)this.wizardClass).getConstructor(Controller.class).newInstance(ctl);
        } catch (Exception e) {
            throw unsupportedInstantiation(e, this.wizardClass);
        }
    }

    public OperationUIAction createUIAction(Controller ctl, OperationInfo operation) {
        return new OperationUIAction(ctl, operation);
    }

    public OperationEnum getOperationEnum() {
        switch (this) {
        case hard_delete:
            return OperationEnum.hardDelete;
        case extract:
            return OperationEnum.query;
        default:
            return OperationEnum.valueOf(name());
        }
    }

    public boolean isDelete() {
        return this == delete || this == hard_delete;
    }

    public int getDialogIdx() {
        return IDialogConstants.CLIENT_ID + ordinal() + 1;
    }

    public boolean isOperationAllowed(AppConfig cfg) {
        // all operations are always allowed except hard delete, which requires bulk api
        return this != hard_delete || cfg.isBulkAPIEnabled() || cfg.isBulkV2APIEnabled();
    }

    public Image getIconImage() {
        Image result = UIUtils.getImageRegistry().get(getIconName());
        if (result == null) { throw new NullPointerException(name() + ": cannot find image: " + getIconName() + ", "
                + getIconLocation()); }
        return result;
    }

    public ImageDescriptor getIconImageDescriptor() {
        ImageDescriptor result = UIUtils.getImageRegistry().getDescriptor(getIconName());
        if (result == null) { throw new NullPointerException(name() + ": cannot find image descriptor: "
                + getIconName() + ", " + getIconLocation()); }
        return result;
    }

    public String getInfoMessageForDataSelectionPage() {
        if (this == hard_delete) return Labels.getString("DataSelectionPage." + name());
        return null;
    }

    public boolean isExtraction() {
        return this == extract || this == extract_all;
    }

}