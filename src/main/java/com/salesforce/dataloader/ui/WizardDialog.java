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

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.controller.Controller;

public abstract class WizardDialog extends BaseDialog {

    public WizardDialog(Shell parent, Controller controller) {
        super(parent, controller);
    }

    @Override
    protected void createContents(Shell shell) {
        // TODO Auto-generated method stub

    }
    
    protected void setShellBounds(Shell dialogShell) {
        Rectangle shellBounds = dialogShell.getBounds();
        Rectangle persistedWizardBounds = UIUtils.getPersistedWizardBounds(getController().getAppConfig());
        shellBounds.x = persistedWizardBounds.x + AppConfig.DIALOG_X_OFFSET;
        shellBounds.y = persistedWizardBounds.y + AppConfig.DIALOG_Y_OFFSET;
        dialogShell.setBounds(shellBounds);
    }
    
    protected Rectangle getPersistedDialogBounds() {
        AppConfig appConfig = getController().getAppConfig();
        Rectangle wizardBounds = UIUtils.getPersistedWizardBounds(appConfig);
        int xOffset = wizardBounds.x + AppConfig.DIALOG_X_OFFSET;
        int yOffset = wizardBounds.y + AppConfig.DIALOG_Y_OFFSET;
        int width = wizardBounds.width;
        int height = wizardBounds.height;
        if (appConfig != null) {
            try {
                xOffset = appConfig.getInt(AppConfig.WIZARD_X_OFFSET) + AppConfig.DIALOG_X_OFFSET;
                yOffset = appConfig.getInt(AppConfig.WIZARD_Y_OFFSET) + AppConfig.DIALOG_Y_OFFSET;
                width = appConfig.getInt(AppConfig.DIALOG_BOUNDS_PREFIX + getClass().getSimpleName() + AppConfig.DIALOG_WIDTH_SUFFIX);
                height = appConfig.getInt(AppConfig.DIALOG_BOUNDS_PREFIX + getClass().getSimpleName() + AppConfig.DIALOG_HEIGHT_SUFFIX);
            } catch (Exception ex) {
                // no op
            }
        }
        if (width == 0) {
            width = wizardBounds.width;
        }
        if (height == 0) {
            height = wizardBounds.height;
        }
        return new Rectangle(xOffset, yOffset, width, height);
    }

}
