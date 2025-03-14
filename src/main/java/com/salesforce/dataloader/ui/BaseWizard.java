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

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;

import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.controller.Controller;

public abstract class BaseWizard extends Wizard {

    private final Controller controller;
    private final WizardPage finishPage;

    protected BaseWizard(Controller ctl, OperationInfo info) {
        this.controller = ctl;

        getConfig().setValue(AppConfig.PROP_OPERATION, info.name());
        this.finishPage = setupPages();
        // Set the dialog window title
        setWindowTitle(getLabel("windowTitle"));

    }

    protected abstract LoginPage createLoginPage();

    protected abstract WizardPage setPages();

    private WizardPage setupPages() {
        if (LoginPage.isNeeded(getController())) addPage(createLoginPage());

        return setPages();
    }

    protected Controller getController() {
        return this.controller;
    }

    protected AppConfig getConfig() {
        return getController().getAppConfig();
    }

    protected String getLabel(String name) {
        return Labels.getString(getLabelSection() + "." + name);
    }

    protected String getLabelSection() {
        return getClass().getSimpleName();
    }

    protected WizardPage getFinishPage() {
        return finishPage;
    }
    
    protected boolean closeWizardPagePostSuccessfulFinish() {
        return getConfig().getBoolean(AppConfig.PROP_WIZARD_CLOSE_ON_FINISH);
    }

}
