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

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.eclipse.swt.SWT;

import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.ProcessInitializationException;

/**
 * This class represents the wizard for adding entries to the address book
 */
public abstract class LoadWizard extends BaseWizard {

    private static final Logger logger = LogManager.getLogger(LoadWizard.class);

    /**
     * LoadWizard constructor
     */
    public LoadWizard(Controller controller, OperationInfo operation) {
        super(controller, operation);
    }

    /**
     * This is a hook to do any further validation before running
     * @return boolean
     */

    protected boolean wizardhook_validateFinish() {
        return true;
    }

    @Override
    public boolean canFinish() {
        return super.canFinish() && getFinishPage().finishAllowed();
    }


    @Override
    public boolean performFinish() {
        // validate the status output
        String outputDirName = getFinishPage().getOutputDir();
        File statusDir = new File(outputDirName);
        if (!statusDir.exists() || !statusDir.isDirectory()) {
            UIUtils.errorMessageBox(getShell(), Labels.getString("LoadWizard.errorValidFolder")); //$NON-NLS-1$
            return false;
        }
        // set the files for status output
        try {
            getController().setStatusFiles(outputDirName, false, true);
            getController().saveConfig();
        } catch (ProcessInitializationException e) {
            UIUtils.errorMessageBox(getShell(), e);
            return false;
        }

        int val = UIUtils.warningConfMessageBox(getShell(), getConfirmationText());

        if (val != SWT.YES) { return false; }

        if (!wizardhook_validateFinish()) { return false; }

        try {
            DLProgressMonitorDialog dlg = new DLProgressMonitorDialog(getShell());
            dlg.run(true, true, new SWTLoadRunable(getController()));

        } catch (InvocationTargetException e) {
            logger.error(Labels.getString("LoadWizard.errorAction"), e); //$NON-NLS-1$
            UIUtils.errorMessageBox(getShell(), e.getCause() != null ? e.getCause() : e);
            return false;
        } catch (InterruptedException e) {
            logger.error(Labels.getString("LoadWizard.errorAction"), e); //$NON-NLS-1$
            UIUtils.errorMessageBox(getShell(), e.getCause() != null ? e.getCause() : e);
            return false;
        }

        return closeWizardPagePostSuccessfulFinish();
    }

    @Override
    protected FinishPage setPages() {
        Controller controller = getController();

        addPage(new DataSelectionPage(controller));

        hook_additionalLoadWizardPages();

        addPage(new MappingPage(controller));

        FinishPage finishPage = createFinishPage();
        addPage(finishPage);
        return finishPage;
    }

    @Override
    protected LoginPage createLoginPage() {
        return new LoginPage(getController());
    }

    protected FinishPage createFinishPage() {
        return new FinishPage(getController());
    }

    @Override
    protected FinishPage getFinishPage() {
        return (FinishPage)super.getFinishPage();
    }

    protected void hook_additionalLoadWizardPages() {}

    private String getConfirmationText() {
        return getLabel("confFirstLine") 
                + System.getProperty("line.separator") //$NON-NLS-1$ //$NON-NLS-2$
                + System.getProperty("line.separator") //$NON-NLS-1$ //$NON-NLS-2$
                + getLabel("confSecondLine"); //$NON-NLS-1$
    }

    public static final class DeleteWizard extends LoadWizard {
        public DeleteWizard(Controller controller) {
            super(controller, OperationInfo.delete);
        }
        
        @Override
        protected void hook_additionalLoadWizardPages() {
            super.hook_additionalLoadWizardPages();
            if (getController().getConfig().isRESTAPIEnabled()
                && Controller.getAPIMajorVersion() >= 61
                && getController().getConfig().getBoolean(Config.DELETE_WITH_EXTERNALID)) {
                addPage(new ExternalIdPage(getController()));
            }
        }
        
        @Override
        public boolean wizardhook_validateFinish() {
            int button = UIUtils.warningConfMessageBox(getShell(), getLabel("validateFirstLine") //$NON-NLS-1$
                    + System.getProperty("line.separator") + getLabel("validateSecondLine")); //$NON-NLS-1$ //$NON-NLS-2$
            return button == SWT.YES;
        }
    }
    
    public static final class UndeleteWizard extends LoadWizard {
        public UndeleteWizard(Controller controller) {
            super(controller, OperationInfo.undelete);
        }
    }

    public static final class HardDeleteWizard extends LoadWizard {
        public HardDeleteWizard(Controller controller) {
            super(controller, OperationInfo.hard_delete);
        }

        @Override
        protected HardDeleteFinishPage createFinishPage() {
            return new HardDeleteFinishPage(getController());
        }
    }

    public static final class UpdateWizard extends LoadWizard {
        public UpdateWizard(Controller controller) {
            super(controller, OperationInfo.update);
        }
        
        @Override
        protected void hook_additionalLoadWizardPages() {
            super.hook_additionalLoadWizardPages();
            if (getController().getConfig().isRESTAPIEnabled()
                && Controller.getAPIMajorVersion() >= 61) {
                addPage(new ExternalIdPage(getController()));
            }
            addPage(new ChooseLookupFieldForRelationshipPage(getController()));
        }
    }

    public static final class UpsertWizard extends LoadWizard {
        public UpsertWizard(Controller controller) {
            super(controller, OperationInfo.upsert);
        }

        @Override
        protected void hook_additionalLoadWizardPages() {
            super.hook_additionalLoadWizardPages();
            addPage(new ExternalIdPage(getController()));
            addPage(new ChooseLookupFieldForRelationshipPage(getController()));
        }
    }

    public static final class InsertWizard extends LoadWizard {
        public InsertWizard(Controller controller) {
            super(controller, OperationInfo.insert);
        }
        
        @Override
        protected void hook_additionalLoadWizardPages() {
            super.hook_additionalLoadWizardPages();
            addPage(new ChooseLookupFieldForRelationshipPage(getController()));
        }
    }

}
