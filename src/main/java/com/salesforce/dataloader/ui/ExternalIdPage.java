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

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;

/**
 * Describe your class here.
 * 
 * @author Lexi Viripaeff
 * @since 7.0
 */
public class ExternalIdPage extends LoadPage {

    private Composite comp;
    private Combo extIdFieldCombo;
    private Label labelExtId;
    private Label labelExtIdInfo;

    public ExternalIdPage(Controller controller) {
        super("ExternalIdPage", controller); //$NON-NLS-1$
    }

    @Override
    public void createControl(Composite parent) {
        getShell().setImage(UIUtils.getImageRegistry().get("sfdc_icon")); //$NON-NLS-1$

        comp = new Composite(parent, SWT.NONE);

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 20;
        gridLayout.verticalSpacing = 7;

        comp.setLayout(gridLayout);

        // Add external id field label and dropdown
        createExtIdFieldUi();

        setControl(comp);
    }

    private void createExtIdFieldUi() {
        labelExtIdInfo = new Label(comp, SWT.LEFT | SWT.WRAP);
        final GridData extIdInfoData = new GridData(GridData.FILL_HORIZONTAL);
        labelExtIdInfo.setLayoutData(extIdInfoData);
        getShell().addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(Event e) {
                extIdInfoData.widthHint = comp.getBounds().width;
            }
        });

        // separate message from selection
        new Label(comp, SWT.NONE);

        labelExtId = new Label(comp, SWT.LEFT);

        // Add the ext id dropdown
        extIdFieldCombo = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
        extIdFieldCombo.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                setPageComplete();
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {

            }
        });
        GridData extIdFieldData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        // The width comes out based on number of pixels, not characters
        extIdFieldData.widthHint = 150;
        extIdFieldCombo.setLayoutData(extIdFieldData);

    }

    public boolean setExtIdCombo() {
        // make sure to start with fresh data if entity selection has changed
        extIdFieldCombo.removeAll();
        extIdFieldCombo.setEnabled(true);

        // entity is known at this time, set the correct combo label
        labelExtId.setText(Labels.getFormattedString("ExternalIdPage.externalIdComboText", controller.getConfig().getString(Config.ENTITY))); //$NON-NLS-1$

        DescribeSObjectResult fieldTypes = controller.getFieldTypes();
        Field[] fields = fieldTypes.getFields();
        ArrayList<String> extIdFields = new ArrayList<String>();
        for(Field field : fields) {
            // every idLookup field can be used for upserts, including Id and Name
            if(field.isIdLookup()) {
                extIdFields.add(field.getName());
            }
        }
        String[] extIdNames = UIUtils.setComboItems(extIdFieldCombo, extIdFields, null);
        // If there's only one external id (should be 'Id'), then don't offer selection,
        // simple show the only one available greyed out option.  Also, set the appropriate message below the combo.
        // Since the selection is already made, then the page selection is complete
        if(extIdFieldCombo.getItemCount() == 1) {
            extIdFieldCombo.setEnabled(false);
            extIdFieldCombo.setText(extIdNames[0]);
            labelExtIdInfo.setText(Labels.getFormattedString("ExternalIdPage.externalIdInfoNoExtId", controller.getConfig().getString(Config.ENTITY))); //$NON-NLS-1$
        } else {
            labelExtIdInfo.setText(Labels.getFormattedString("ExternalIdPage.externalIdInfoExtIdExists", controller.getConfig().getString(Config.ENTITY))); //$NON-NLS-1$
        }
        setPageComplete();
        comp.layout();

        if (extIdNames.length > 0 ) {
            return true;
        }
        return false;
    }

    /**
     * Returns the next page, sets the external id
     * 
     * @return IWizardPage
     */
    @Override
    public LoadPage getNextPage() {
        // save the data from selection(s)
        saveExtIdData();

        // prepare next page
        LoadPage nextPage = null;
        ChooseLookupFieldForRelationshipPage fkExtIdPage = (ChooseLookupFieldForRelationshipPage) getWizard().getPage(ChooseLookupFieldForRelationshipPage.class.getSimpleName()); //$NON-NLS-1$
        if(controller.getReferenceDescribes().size() > 0) {
            nextPage = fkExtIdPage;
        } else {
            nextPage = (LoadPage)getWizard().getPage(MappingPage.class.getSimpleName()); //$NON-NLS-1$
        }
        nextPage.setupPage();
        nextPage.setPageComplete();
        return nextPage;
    }

    /**
     * Save the data from this page in the config
     * @param selectedObjects
     */
    private void saveExtIdData() {
        Config config = controller.getConfig();

        // external id field
        String extIdField = extIdFieldCombo.getText();
        config.setValue(Config.IDLOOKUP_FIELD, extIdField);

        controller.saveConfig();
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.ui.LoadPage#setupPage()
     */
    @Override
    public boolean setupPagePostLogin() {
        if (!setExtIdCombo()) {
            //if there is no external id, don't let them continue.
            MessageBox msg = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            msg.setMessage(Labels.getString("ExternalIdPage.errorExternalIdRequired"));
            msg.setText(Labels.getString("ExternalIdPage.errorExternalIdRequiredTitle"));
            msg.open();
            return false;
        }
        return true;
    }

    @Override
    public void setPageComplete() {
        String fieldName = extIdFieldCombo.getText();
        if (fieldName != null && !fieldName.equals("") ) { //$NON-NLS-1$
            setPageComplete(true);
        } else {
            setPageComplete(false);
        }        
    }
}