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

import org.eclipse.jface.resource.ImageDescriptor;

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.ui.uiActions.OperationUIAction;
import com.sforce.async.OperationEnum;

/**
 * Enum containing data and utility methods for data loader operations.
 * 
 * @author Colin Jarvis
 */
public enum OperationInfo {

    insert(InsertAction.class, BulkLoadAction.class, BulkLoadAction.class, OperationInfoUIHelper.insert),
    update(UpdateAction.class, BulkLoadAction.class, BulkLoadAction.class, OperationInfoUIHelper.update),
    upsert(UpsertAction.class, BulkLoadAction.class, BulkLoadAction.class, OperationInfoUIHelper.upsert),
    delete(DeleteAction.class, BulkLoadAction.class, BulkLoadAction.class, OperationInfoUIHelper.delete),
    undelete(UndeleteAction.class, null, null, OperationInfoUIHelper.undelete),
    hard_delete(null, BulkLoadAction.class, BulkLoadAction.class, OperationInfoUIHelper.hard_delete),
    extract(PartnerExtractAction.class, BulkExtractAction.class, BulkExtractAction.class, OperationInfoUIHelper.extract),
    extract_all(PartnerExtractAllAction.class, BulkExtractAction.class, BulkExtractAction.class, OperationInfoUIHelper.extract_all);

    /** all operations, in order */
    public static final OperationInfo[] ALL_OPERATIONS_IN_ORDER = 
            { insert, update, upsert, delete, undelete, hard_delete, extract, extract_all };

    private static final Logger logger = LogManager.getLogger(OperationInfo.class);

    private final Class<? extends IAction> partnerAPIActionClass;
    private final Class<? extends IAction> bulkAPIActionClass;
    private final Class<? extends IAction> bulkV2APIActionClass;
    private final OperationInfoUIHelper uiHelper;

    private OperationInfo(Class<? extends IAction> partnerAPIActionClass, Class<? extends IAction> bulkAPIActionClass, Class<? extends IAction> bulkV2APIActionClass,
            OperationInfoUIHelper uiHelper) {

        this.partnerAPIActionClass = partnerAPIActionClass;
        this.bulkAPIActionClass = bulkAPIActionClass;
        this.bulkV2APIActionClass = bulkAPIActionClass;
        this.uiHelper = uiHelper;
    }

    public boolean bulkAPIEnabled() {
        return this.bulkAPIActionClass != null;
    }
    
    public boolean bulkV2APIEnabled() {
        return this.bulkV2APIActionClass != null;
    }

    public boolean partnerAPIEnabled() {
        return this.partnerAPIActionClass != null;
    }

    public IAction instantiateAction(Controller ctl, ILoaderProgress loaderProgress) {
        logger.info(Messages.getMessage(getClass(), "createAction", this));
        Class<? extends IAction> cls = this.partnerAPIActionClass;
        if (ctl.getConfig().isBulkAPIEnabled() && bulkAPIEnabled()) {
            cls = this.bulkAPIActionClass;
        } else if (ctl.getConfig().isBulkV2APIEnabled() && bulkV2APIEnabled()) {
            cls = this.bulkV2APIActionClass;
        }
        try {
            return cls.getConstructor(Controller.class, ILoaderProgress.class).newInstance(ctl, loaderProgress);
        } catch (Exception e) {
            throw unsupportedInstantiation(e, cls);
        }
    }

    private RuntimeException unsupportedInstantiation(Exception e, Class<?> cls) {
        final String message = Messages
                .getMessage(getClass(), "errorOperationInstantiation", this, String.valueOf(cls));
        logger.fatal(message);
        return new UnsupportedOperationException(message, e);
    }

    public String getIconName() {
        return this.uiHelper.getIconName();
    }

    public String getIconLocation() {
        return this.uiHelper.getIconLocation();
    }

    public String getMenuLabel() {
        return this.uiHelper.getMenuLabel();
    }

    public String getToolTipText() {
        return this.uiHelper.getToolTipText();
    }

    public String getLabel() {
        return this.uiHelper.getLabel();
    }

    public OperationUIAction createUIAction(Controller ctl) {
        return new OperationUIAction(ctl, this);
    }

    public OperationEnum getBulkOperationEnum() {
        switch (this) {
        case insert:
            return OperationEnum.insert;
        case update:
            return OperationEnum.update;
        case upsert:
            return OperationEnum.upsert;
        case delete:
            return OperationEnum.delete;
        case hard_delete:
            return OperationEnum.hardDelete;
        case extract:
            return OperationEnum.query;
        case extract_all:
            return OperationEnum.queryAll;
        default:
            return OperationEnum.valueOf(name());
        }
    }

    public boolean isDelete() {
        return this == delete || this == hard_delete;
    }
    
    public boolean isUndelete() {
        return this == undelete;
    }
    
    public int getDialogIdx() {
        return this.uiHelper.getDialogIdx();
    }

    public boolean isOperationAllowed(Config cfg) {
        if (cfg.isBulkAPIEnabled()) {
            return this.bulkAPIActionClass != null;
        } else if (cfg.isBulkV2APIEnabled()) {
            return this.bulkV2APIActionClass != null;
        }
        return this.partnerAPIActionClass != null;
    }

    public ImageDescriptor getIconImageDescriptor() {
        return this.uiHelper.getIconImageDescriptor();
    }

    public String getInfoMessageForDataSelectionPage() {
        return this.uiHelper.getInfoMessageForDataSelectionPage();
    }

    public boolean isExtraction() {
        return this == extract || this == extract_all;
    }
    
    public OperationInfoUIHelper getUIHelper() {
        return this.uiHelper;
    }

}