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

package com.salesforce.dataloader.ui.extraction;

import java.io.*;
import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;

import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.ui.*;

/**
 * This class represents the wizard for adding entries to the address book
 */
public class ExtractionWizard extends BaseWizard {

    private static final Logger logger = LogManager.getLogger(ExtractionWizard.class);

    /**
     * ExtractionWizard constructor
     */
    public ExtractionWizard(Controller controller) {
        this(controller, OperationInfo.extract);
    }

    protected ExtractionWizard(Controller controller, OperationInfo opInfo) {
        super(controller, opInfo);
    }

    @Override
    protected WizardPage setPages() {
        final Controller controller = getController();
        addPage(new ExtractionDataSelectionPage(controller ));
        ExtractionPage soqlPage = new ExtractionSOQLPage(controller);
        addPage(soqlPage);
        WizardPage finishPage = soqlPage;
        if (getConfig().getBoolean(Config.ENABLE_EXTRACT_STATUS_OUTPUT)) {
            //need to reference the finish page for performFinish()
            finishPage = new ExtractionFinishPage(controller);
            addPage(finishPage);
        }
        return finishPage;
    }

    @Override
    protected OperationPage getFinishPage() {
        return (OperationPage)super.getFinishPage();
    }

    private boolean validateExtractionPath(String filePath) {
        File file = new File(filePath);

        //if it doesn't exist and we can create it, its valid
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    UIUtils.errorMessageBox(getShell(), Labels.getString("LoadWizard.errorFileCreate") + filePath);
                    return false;
                }
            } catch (IOException iox) {
                UIUtils.errorMessageBox(getShell(), Labels.getString("LoadWizard.errorFileCreate") + filePath);
                return false;
            }
            return true;
        }

        //if it does exist and can be written to
        if (!file.canWrite()) {
            UIUtils.errorMessageBox(getShell(), Labels.getString("LoadWizard.errorFileWrite") + filePath);
            return false;
        }

        FileWriter fileOut = null;
        try {
            fileOut = new FileWriter(filePath);
        } catch (IOException e) {
            UIUtils.errorMessageBox(getShell(), Labels.getString("LoadWizard.errorFileWrite") + filePath);
            return false;
        } finally {
            if (fileOut != null) {
                try {
                    fileOut.close();
                } catch (IOException e1) {}
            }
        }

        return true;
    }

    @Override
    public boolean performFinish() {
        if (!validateExtractionPath(getConfig().getString(Config.DAO_NAME))) {
            return false;
        }

        if (!getFinishPage().finishAllowed()) {
            return false;
        }

        try {
            int val = UIUtils.warningConfMessageBox(getShell(),
                    getLabel("confFirstLine") + System.getProperty("line.separator") + getLabel("confSecondLine"));
            if (val == SWT.YES) {
                ProgressMonitorDialog dlg = new ProgressMonitorDialog(getShell());
                dlg.run(true, true, new SWTLoadRunable(getController()));
            } else {
                return false;
            }

        } catch (InvocationTargetException e) {
            logger.error(Labels.getString("LoadWizard.errorAction"), e);
            UIUtils.errorMessageBox(getContainer().getShell(), e.getCause() != null ? e.getCause() : e);
            return false;
        } catch (InterruptedException e) {
            logger.error(Labels.getString("LoadWizard.errorAction"), e);
            UIUtils.errorMessageBox(getShell(), e.getCause() != null ? e.getCause() : e);
            return false;
        }

        return getController().isLastOperationSuccessful() && closeWizardPagePostSuccessfulFinish();
    }

    @Override
    protected LoginPage createLoginPage() {
        LoginPage loginPage = new LoginPage(getController());
        loginPage.setNextPageName(ExtractionDataSelectionPage.class.getSimpleName());
        return loginPage;
    }
}
