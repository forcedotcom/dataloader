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

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.MappingInitializationException;
import com.salesforce.dataloader.mapping.LoadMapper;
import com.salesforce.dataloader.mapping.Mapper;

/**
 * Describe your class here.
 * 
 * @author Lexi Viripaeff
 * @since 6.0
 */
public class FinishPage extends LoadPage {

    private DirectoryFieldEditor dirFE;
    private ContentLimitLink contentNoteLimitLink;

    public FinishPage(Controller controller) {
        this("FinishPage", controller); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public FinishPage(String name, Controller controller) {
        super(name, controller); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    @Override
    public void createControl(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        //comp.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.horizontalSpacing = 10;
        gridLayout.marginHeight = 20;
        comp.setLayout(gridLayout);

        Label label = new Label(comp, SWT.CENTER);
        label.setText(Labels.getString("FinishPage.overwritten")); //$NON-NLS-1$

        Composite dirComp = new Composite(comp, SWT.NONE);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        dirComp.setLayoutData(data);

        dirFE = new DirectoryFieldEditor(Labels.getString("FinishPage.output"), Labels.getString("FinishPage.chooseDir"), dirComp); //$NON-NLS-1$ //$NON-NLS-2$
        dirFE.setStringValue(controller.getAppConfig().getString(AppConfig.OUTPUT_STATUS_DIR));

        Text textField = dirFE.getTextControl(dirComp);
        textField.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent arg0) {
                setPageComplete();
            }
            
        });

        contentNoteLimitLink = new ContentLimitLink(comp, SWT.WRAP, getController());
        
        hook_createControl(comp);
        setControl(comp);
        setupPage();
    }

    protected void hook_createControl(Composite comp) {}

    public String getOutputDir() {
        return dirFE.getStringValue();
    }
    @Override
    public boolean canFlipToNextPage() {
        // this is always the last page, disable next
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.ui.LoadPage#setupPage()
     */
    @Override
    protected boolean setupPagePostLogin() {
        try {
            verifySettings();
            if (!controller.getAppConfig().getBoolean(AppConfig.WIZARD_POPULATE_RESULTS_FOLDER_WITH_PREVIOUS_OP_RESULTS_FOLDER)) {
                dirFE.setStringValue(null); // clear previous selection
            }
        } catch (final MappingInitializationException e) {
            final FinishPage page = this;
            Display.getDefault().syncExec(new Thread() {
                @Override
                public void run() {
                    UIUtils.errorMessageBox(page.getShell(), e.getMessage());
                }
            });
            return false;
        }

        setPageComplete();
        IWizardContainer wizardContainer = this.getContainer();
        if (wizardContainer != null) {
            wizardContainer.updateButtons();
        }
        contentNoteLimitLink.setVisible();
        if (!controller.saveConfig()) return false;
        return true;
    }

    private void verifySettings() throws MappingInitializationException {
        if (!getController().getAppConfig().getOperationInfo().isExtraction()) {
            Mapper mapper = (LoadMapper)getController().getMapper();
            if (mapper != null) {
                ((LoadMapper)getController().getMapper()).verifyMappingsAreValid();
            }
        }
    }

    public boolean finishAllowed() {
        return true;
    }

    protected Controller getController() {
        return this.controller;
    }

    public void setPageComplete() {
        String outputDir = getOutputDir();
        if (outputDir == null || outputDir.isBlank() || !this.isCurrentPage()) {
            setPageComplete(false);
        } else {
            setPageComplete(true);
        }
    }
}