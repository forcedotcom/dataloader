/*
 * Copyright (c) 2012, salesforce.com, inc.
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

import java.util.*;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import com.salesforce.dataloader.client.DescribeRefObject;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dyna.ObjectField;
import com.sforce.soap.partner.Field;

/**
 * Page for selecting external id fields for the foreign key objects
 * 
 * @author Alex Warshavsky
 * @since 8.0
 */
public class ForeignKeyExternalIdPage extends LoadPage {

    private final Controller controller;
    private final Map<String,Combo> extIdSelections = new HashMap<String,Combo>();
    private Composite containerComp;
    private ScrolledComposite scrollComp;
    private Map<String, DescribeRefObject> referenceObjects;

    public ForeignKeyExternalIdPage(Controller controller) {
        super(Labels.getString("ForeignKeyExternalIdPage.title"), //$NON-NLS-1$
                Labels.getString("ForeignKeyExternalIdPage.message"), //$NON-NLS-1$
                UIUtils.getImageRegistry().getDescriptor("splashscreens")); //$NON-NLS-1$

        this.controller = controller;

        // Set the description
        setDescription(Labels.getString("ForeignKeyExternalIdPage.description"));  //$NON-NLS-1$

        setPageComplete(true);
    }

    @Override
    public void createControl(Composite parent) {
        containerComp = new Composite(parent, SWT.NONE);
        containerComp.setLayout(new FillLayout());

        setControl(containerComp);

        setPageComplete(true);
    }

    private void createFkExtIdUi() {
        getShell().setImage(UIUtils.getImageRegistry().get("sfdc_icon")); //$NON-NLS-1$

        if(scrollComp != null) {
            scrollComp.dispose();
        }
        scrollComp = new ScrolledComposite(containerComp, SWT.V_SCROLL);
        scrollComp.setExpandHorizontal(true);
        scrollComp.setExpandVertical(true);
        Composite comp = new Composite(scrollComp, SWT.NONE);
        scrollComp.setContent(comp);

        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.horizontalSpacing = 10;
        gridLayout.marginHeight = 20;
        gridLayout.verticalSpacing = 7;
        comp.setLayout(gridLayout);

        scrollComp.setMinSize(comp.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        // set scrolling specific to data
        ScrollBar scrollBar = scrollComp.getVerticalBar();
        scrollBar.setIncrement(20);
        scrollBar.setPageIncrement(20 * 5);

        extIdSelections.clear();
        if(referenceObjects != null) {
            for(String relationshipName : referenceObjects.keySet()) {
                createObjectExtIdUi(comp, relationshipName);
            }
        }
        scrollComp.setMinSize(comp.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        containerComp.layout();

        setPageComplete(true);
    }

    /**
     * Create UI components for one object: label and a combo box with external id fields
     * @param comp
     * @param relationshipName
     */
    private void createObjectExtIdUi(Composite comp, String relationshipName) {
        Label labelExtId = new Label(comp, SWT.RIGHT);
        labelExtId.setText(relationshipName);
        labelExtId.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        // Add the ext id dropdown
        Combo extIdCombo = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);

        // The width comes out based on number of pixels, not characters
        GridData extIdData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        extIdData.widthHint = 150;
        extIdCombo.setLayoutData(extIdData);

        // get object's ext id info & set combo box to list of external id fields
        // set the objects reference information
        DescribeRefObject extIdInfo = referenceObjects.get(relationshipName);
        List<String> fieldList = new ArrayList<String>(extIdInfo.getFieldInfoMap().keySet());
        // add default selection "not selected" to the list to allow users to go back to it
        fieldList.add(Labels.getString("ForeignKeyExternalIdPage.defaultComboText"));
        UIUtils.setComboItems(extIdCombo, fieldList, Labels.getString("ForeignKeyExternalIdPage.defaultComboText"));

        extIdSelections.put(relationshipName, extIdCombo);
    }

    /**
     * Returns the next page, sets the external id
     * 
     * @return IWizardPage
     */
    @Override
    public MappingPage getNextPage() {
        // save data from this page and remember field selections for mapping
        Map<String,Field> relatedFields = saveExtIdData();

        MappingPage nextPage = (MappingPage)getWizard().getPage(Labels.getString("MappingPage.title")); //$NON-NLS-1$
        nextPage.setRelatedFields(relatedFields);
        nextPage.setupPage();
        return nextPage;
    }

    /**
     * Save the data from this page in the config and return the selected external id field info
     * @return selected objects external id field info
     */
    private Map<String, Field> saveExtIdData() {
        Map<String,Field> relatedFields = new HashMap<String,Field>();

        // foreign key references (if any set)
        Map<String,String> extIdReferences = new HashMap<String,String>();
        for(String relationshipName : extIdSelections.keySet()) {
            Combo combo = extIdSelections.get(relationshipName);
            String extIdFieldName = combo.getText();
            // make sure that the item selection has occurred and that the default text is not displayed anymore
            if(extIdFieldName != null && extIdFieldName.length() > 0
                    && ! extIdFieldName.equals(Labels.getString("ForeignKeyExternalIdPage.defaultComboText"))) {
                DescribeRefObject refObjectInfo = referenceObjects.get(relationshipName);
                extIdReferences.put(relationshipName, ObjectField.formatAsString(refObjectInfo.getObjectName(), extIdFieldName));
                relatedFields.put(relationshipName,refObjectInfo.getFieldInfoMap().get(extIdFieldName));
            }
        }

        return relatedFields;
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.ui.LoadPage#setupPage()
     */
    @Override
    boolean setupPage() {
        // set the required data
        setReferenceObjects();

        // Add the foreign key entity external id fields dropdowns
        createFkExtIdUi();

        return true;
    }

    public void setReferenceObjects() {
        this.referenceObjects = controller.getReferenceDescribes();
    }
}