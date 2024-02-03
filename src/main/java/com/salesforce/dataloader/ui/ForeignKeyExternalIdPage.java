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

import java.util.*;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.client.DescribeRefObject;
import com.salesforce.dataloader.client.ReferenceEntitiesDescribeMap;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dyna.RelationshipField;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;

/**
 * Page for selecting external id fields for the foreign key objects
 * 
 * @author Alex Warshavsky
 * @since 8.0
 */
public class ForeignKeyExternalIdPage extends LoadPage {

    private final Map<String,Combo> extIdSelections = new HashMap<String,Combo>();
    private final Map<String,Combo> parentSelections = new HashMap<String,Combo>();
    private Composite containerComp;
    private ScrolledComposite scrollComp;
    private ReferenceEntitiesDescribeMap referenceObjects;
    private boolean hasParentEntitiesWithIdLookupField = false;


    public ForeignKeyExternalIdPage(Controller controller) {
        super("ForeignKeyExternalIdPage", controller); //$NON-NLS-1$
        // Mark this page as completed as the selected sObject may not have any foreign key
        setPageComplete();
    }

    @Override
    public void createControl(Composite parent) {
        containerComp = new Composite(parent, SWT.NONE);
        containerComp.setLayout(new FillLayout());

        setControl(containerComp);
        setupPage();
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

        GridLayout gridLayout = new GridLayout(3, false);
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
        parentSelections.clear();
        this.hasParentEntitiesWithIdLookupField = false;
        if(referenceObjects != null) {
            List<String> sortedRelationshipList = new ArrayList<>(referenceObjects.keySet());
            Collections.sort(sortedRelationshipList);
            for(String relationshipName : sortedRelationshipList) {
                OperationInfo operation = controller.getConfig().getOperationInfo();
                Field childField = referenceObjects.getParentSObject(relationshipName).getChildField();
                boolean isCreateableOrUpdateable = true;
                if (childField != null) {
                    switch (operation) {
                        case insert:
                            if (!childField.isCreateable()) {
                                isCreateableOrUpdateable = false;
                            }
                            break;
                        case update:
                            if (!childField.isUpdateable()) {
                                isCreateableOrUpdateable = false;
                            }
                            break;
                        default:
                            break;
                    }
                }
                if (isCreateableOrUpdateable) {
                    createObjectExtIdUi(comp, relationshipName);
                    hasParentEntitiesWithIdLookupField = true;
                }
            }
        }
        scrollComp.setMinSize(comp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        containerComp.layout();
    }

    /**
     * Create UI components for one object: label and a combo box with external id fields
     * @param comp
     * @param relationshipName
     */
    private void createObjectExtIdUi(Composite comp, String relationshipName) {
        RelationshipField relField = new RelationshipField(relationshipName, false);

        // Add parent dropdown
        if (relField.getParentObjectName() == null) {
            // shouldn't happen
            return;
        }
        Combo parentCombo = this.parentSelections.get(relField.getRelationshipName());
        if (parentCombo == null) {
            // first parent object
            Label labelExtId = new Label(comp, SWT.RIGHT);
            labelExtId.setText(relField.getRelationshipName() + ":");
            labelExtId.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

            parentCombo = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
            GridData parentData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
            parentData.widthHint = 150;
            parentCombo.setLayoutData(parentData);
            parentCombo.add(relField.getParentObjectName());
            parentCombo.select(0);
            parentCombo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    String parentName = ((Combo)event.widget).getText();
                    Combo parentLookupIdFieldCombo = extIdSelections.get(relField.toFormattedRelationshipString());
                    parentLookupIdFieldCombo.removeAll();
                    RelationshipField selectedRelationship = new RelationshipField(parentName, relField.getRelationshipName());
                    populateParentLookupFieldCombo(parentLookupIdFieldCombo, selectedRelationship);
                }
            });
            parentSelections.put(relField.getRelationshipName(), parentCombo);
            // Add the ext id dropdown
            Combo extIdCombo = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);

            // The width comes out based on number of pixels, not characters
            GridData extIdData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
            extIdData.widthHint = 150;
            extIdCombo.setLayoutData(extIdData);
            populateParentLookupFieldCombo(extIdCombo, relField);
            extIdSelections.put(relField.toFormattedRelationshipString(), extIdCombo);
        } else {
            parentCombo.add(relField.getParentObjectName());
        }
    }
    
    private void populateParentLookupFieldCombo(Combo extIdCombo, RelationshipField relField) {
        DescribeRefObject extIdInfo = referenceObjects.getParentSObject(relField.toFormattedRelationshipString());

        // get object's ext id info & set combo box to list of external id fields
        // set the objects reference information
        List<String> fieldList = new ArrayList<String>(extIdInfo.getParentObjectFieldMap().keySet());
        // add default selection "not selected" to the list to allow users to go back to it
        fieldList.add(Labels.getString("ForeignKeyExternalIdPage.defaultComboText"));
        UIUtils.setComboItems(extIdCombo, fieldList, Labels.getString("ForeignKeyExternalIdPage.defaultComboText"));
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

        MappingPage nextPage = (MappingPage)getWizard().getPage(MappingPage.class.getSimpleName()); //$NON-NLS-1$
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
        for(String relationshipNameInCombo : extIdSelections.keySet()) {
            Combo combo = extIdSelections.get(relationshipNameInCombo);
            String lookupFieldInParent = combo.getText();
            RelationshipField relationshipField = getSelectedParentSObjectForLookupField(relationshipNameInCombo, lookupFieldInParent);
            // make sure that the item selection has occurred and that the default text is not displayed anymore
            if(relationshipField != null) {
                DescribeRefObject refDescribe = referenceObjects.getParentSObject(relationshipField.toFormattedRelationshipString());
                Field relatedField = new Field();
                Field parentField = refDescribe.getParentObjectFieldMap().get(lookupFieldInParent);
                Field childField = refDescribe.getChildField();
                relatedField.setName(relationshipField.toString());
                String childFieldLabel = childField.getLabel();
                String[] childFieldLabelParts = childFieldLabel.split(" \\(.+\\)$");
                relatedField.setLabel(childFieldLabelParts[0] + " (" + parentField.getLabel() + ")");
                relatedField.setCreateable(childField.isCreateable());
                relatedField.setUpdateable(childField.isUpdateable());
                relatedField.setType(FieldType.reference);
                String[] refToArray = new String[1];
                refToArray[0] = refDescribe.getParentObjectName();
                relatedField.setReferenceTo(refToArray);
                relatedFields.put(relationshipField.toString(),relatedField);
            }
        }

        return relatedFields;
    }
    
    private RelationshipField getSelectedParentSObjectForLookupField(String relationshipNameInCombo, String selectedIdLookupField) {
        if(selectedIdLookupField != null && selectedIdLookupField.length() > 0
                && ! selectedIdLookupField.equals(Labels.getString("ForeignKeyExternalIdPage.defaultComboText"))) {
            RelationshipField possibleParentSObjectField = new RelationshipField(relationshipNameInCombo, false);
            Combo parentSObjectCombo = this.parentSelections.get(possibleParentSObjectField.getRelationshipName());
            if (parentSObjectCombo == null) {
                return null;
            }
            String actualParentSObjectName = parentSObjectCombo.getText();
            String fullRelationshipName = RelationshipField.formatAsString(
                    actualParentSObjectName
                    , possibleParentSObjectField.getRelationshipName()
                    , selectedIdLookupField);
            RelationshipField actualParentSObjectField = new RelationshipField(fullRelationshipName, true);
            return actualParentSObjectField;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.ui.LoadPage#setupPage()
     */
    @Override
    public boolean setupPagePostLogin() {
        // set the required data
        setReferenceObjects();
        
        // Add the foreign key entity external id fields dropdowns
        createFkExtIdUi();
        return true;
    }

    public void setReferenceObjects() {
        this.referenceObjects = controller.getReferenceDescribes();
    }
    
    @Override
    public void setPageComplete() {
        setPageComplete(true);
    }
    
    @Override
    protected OperationPage getNextPageOverride(){
        setupPage();
        if (!hasParentEntitiesWithIdLookupField) {
            // nothing to display, go to the next page
            return (OperationPage)getNextPage();
        }
        return this;
    }
    
    @Override
    protected OperationPage getPreviousPageOverride(){
        if (!hasParentEntitiesWithIdLookupField) {
            // nothing to display, go to the previous page
            return (OperationPage)getPreviousPage();
        }
        return this;
    }
    
    public boolean setupPage() {
        boolean success = super.setupPage();
        if (this.controller != null && this.controller.isLoggedIn()) {
            String message = Labels.getFormattedString(this.getClass().getSimpleName() + ".pageMessage", this.controller.getConfig().getString(Config.ENTITY));
            this.setMessage(message);
        }
        return success;
    }

}