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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.client.DescribeRefObject;
import com.salesforce.dataloader.client.ReferenceEntitiesDescribeMap;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dyna.ParentIdLookupFieldFormatter;
import com.salesforce.dataloader.dyna.ParentSObjectFormatter;
import com.salesforce.dataloader.exception.RelationshipFormatException;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;

/**
 * Page for selecting external id fields for the foreign key objects
 * 
 * @author Alex Warshavsky
 * @since 8.0
 */
public class ChooseLookupFieldForRelationshipPage extends LoadPage {

    private final Map<String,Combo> parentLookupFieldSelectionMap = new HashMap<String,Combo>();
    private final Map<String,Combo> parentSObjectMap = new HashMap<String,Combo>();
    private Composite containerComp;
    private ScrolledComposite scrollComp;
    private ReferenceEntitiesDescribeMap parentSObjectDescribeMap;
    private boolean hasParentEntitiesWithIdLookupField = false;


    public ChooseLookupFieldForRelationshipPage(Controller controller) {
        super("ChooseLookupFieldForRelationshipPage", controller); //$NON-NLS-1$
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

        GridLayout gridLayout = new GridLayout(5, false);
        gridLayout.horizontalSpacing = 10;
        gridLayout.marginHeight = 20;
        gridLayout.verticalSpacing = 7;
        comp.setLayout(gridLayout);

        scrollComp.setMinSize(comp.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        // set scrolling specific to data
        ScrollBar scrollBar = scrollComp.getVerticalBar();
        scrollBar.setIncrement(20);
        scrollBar.setPageIncrement(20 * 5);

        parentLookupFieldSelectionMap.clear();
        parentSObjectMap.clear();
        this.hasParentEntitiesWithIdLookupField = false;
        if(parentSObjectDescribeMap != null) {
            Label relNameHeader = new Label(comp, SWT.RIGHT);
            relNameHeader.setText(Labels.getString(getClass().getSimpleName() + ".relationshipHeader"));
            relNameHeader.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            Font f = relNameHeader.getFont();
            FontData[] farr = f.getFontData();
            FontData fd = farr[0];
            fd.setStyle(SWT.BOLD);
            relNameHeader.setFont(new Font(Display.getCurrent(), fd));

            Label parentObjectSeparator = new Label(comp, SWT.CENTER);
            parentObjectSeparator.setText(ParentSObjectFormatter.NEW_FORMAT_RELATIONSHIP_NAME_SEPARATOR_CHAR);
            parentObjectSeparator.setFont(new Font(Display.getCurrent(), fd));

            Label parentObjectNameHeader = new Label(comp, SWT.RIGHT);
            parentObjectNameHeader.setText(Labels.getString(
                    getClass().getSimpleName() + ".parentObjectHeader"));
            parentObjectNameHeader.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
            parentObjectNameHeader.setFont(new Font(Display.getCurrent(), fd));

            Label parentLookupFieldSeparator = new Label(comp, SWT.CENTER);
            parentLookupFieldSeparator.setText(ParentIdLookupFieldFormatter.NEW_FORMAT_PARENT_IDLOOKUP_FIELD_SEPARATOR_CHAR);
            parentLookupFieldSeparator.setFont(new Font(Display.getCurrent(), fd));

            Label idLookupFieldNameHeader = new Label(comp, SWT.RIGHT);
            idLookupFieldNameHeader.setText(Labels.getString(
                    getClass().getSimpleName() + ".parentLookupFieldHeader"));
            idLookupFieldNameHeader.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
            idLookupFieldNameHeader.setFont(new Font(Display.getCurrent(), fd));

            List<String> sortedRelationshipList = new ArrayList<>(parentSObjectDescribeMap.keySet());
            Collections.sort(sortedRelationshipList);
            for(String relationshipName : sortedRelationshipList) {
                OperationInfo operation = controller.getAppConfig().getOperationInfo();
                Field childField = parentSObjectDescribeMap.getParentSObject(relationshipName).getChildField();
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
                    createUIToMapParentLookupFieldChoices(comp, relationshipName);
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
    private void createUIToMapParentLookupFieldChoices(Composite comp, String relationshipName) {
        ParentSObjectFormatter relField;
        try {
            relField = new ParentSObjectFormatter(relationshipName);
        } catch (RelationshipFormatException e) {
            logger.error(e.getMessage());
            return;
        }

        // Add parent dropdown
        if (relField.getParentObjectName() == null) {
            // shouldn't happen
            return;
        }
        
        Combo parentSObjectCombo = this.parentSObjectMap.get(relField.getRelationshipName());
        if (parentSObjectCombo == null) {
            // first parent object
            Label relName = new Label(comp, SWT.RIGHT);
            relName.setText(relField.getRelationshipName());
            relName.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

            Label parentObjectSeparator = new Label(comp, SWT.CENTER);
            parentObjectSeparator.setText(ParentSObjectFormatter.NEW_FORMAT_RELATIONSHIP_NAME_SEPARATOR_CHAR);

            parentSObjectCombo = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
            GridData parentData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
            parentData.widthHint = 150;
            parentSObjectCombo.setLayoutData(parentData);
            parentSObjectCombo.add(relField.getParentObjectName());
            parentSObjectCombo.select(0);
            parentSObjectCombo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    String parentName = ((Combo)event.widget).getText();
                    Combo parentLookupIdFieldCombo = parentLookupFieldSelectionMap.get(relField.toString());
                    parentLookupIdFieldCombo.removeAll();
                    ParentSObjectFormatter selectedRelationship;
                    try {
                        selectedRelationship = new ParentSObjectFormatter(parentName, relField.getRelationshipName());
                    } catch (RelationshipFormatException e) {
                        // TODO Auto-generated catch block
                        logger.error(e.getMessage());
                        return;
                    }
                    populateParentLookupFieldCombo(parentLookupIdFieldCombo, selectedRelationship);
                }
            });
            parentSObjectMap.put(relField.getRelationshipName(), parentSObjectCombo);
            Label parentLookupFieldSeparator = new Label(comp, SWT.CENTER);
            parentLookupFieldSeparator.setText(ParentIdLookupFieldFormatter.NEW_FORMAT_PARENT_IDLOOKUP_FIELD_SEPARATOR_CHAR);
            // Add the ext id dropdown
            Combo parentLookupFieldCombo = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);

            // The width comes out based on number of pixels, not characters
            GridData extIdData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
            extIdData.widthHint = 150;
            parentLookupFieldCombo.setLayoutData(extIdData);
            populateParentLookupFieldCombo(parentLookupFieldCombo, relField);
            parentLookupFieldSelectionMap.put(relField.toString(), parentLookupFieldCombo);
        } else {
            parentSObjectCombo.add(relField.getParentObjectName());
        }
    }
    
    private void populateParentLookupFieldCombo(Combo extIdCombo, ParentSObjectFormatter relField) {
        DescribeRefObject extIdInfo = parentSObjectDescribeMap.getParentSObject(relField.toString());

        // get object's ext id info & set combo box to list of external id fields
        // set the objects reference information
        List<String> fieldList = new ArrayList<String>(extIdInfo.getParentObjectFieldMap().keySet());
        // add default selection "not selected" to the list to allow users to go back to it
        String defaultListItemStr = Labels.getString(getClass().getSimpleName() + ".defaultComboText");
        fieldList.add(defaultListItemStr);
        UIUtils.setComboItems(extIdCombo, fieldList, defaultListItemStr);
    }

    /**
     * Returns the next page, sets the external id
     * 
     * @return IWizardPage
     */
    @Override
    public MappingPage getNextPage() {
        // save data from this page and remember field selections for mapping
        Map<String,Field> relatedFields = saveParentIdLookupFieldData();

        MappingPage nextPage = (MappingPage)getWizard().getPage(MappingPage.class.getSimpleName()); //$NON-NLS-1$
        nextPage.setRelatedFields(relatedFields);
        nextPage.setupPage();
        return nextPage;
    }

    /**
     * Save the data from this page in the config and return the selected external id field info
     * @return selected objects external id field info
     */
    private Map<String, Field> saveParentIdLookupFieldData() {
        Map<String,Field> relatedFields = new HashMap<String,Field>();

        // foreign key references (if any set)
        for(String parentAndRelNameInCombo : parentLookupFieldSelectionMap.keySet()) {
            Combo combo = parentLookupFieldSelectionMap.get(parentAndRelNameInCombo);
            String lookupFieldInParent = combo.getText();
            ParentIdLookupFieldFormatter relationshipField = getSelectedParentSObjectForLookupField(parentAndRelNameInCombo, lookupFieldInParent);
            // make sure that the item selection has occurred and that the default text is not displayed anymore
            if(relationshipField != null
                    && !relationshipField.getParentFieldName().equalsIgnoreCase(Labels.getString(getClass().getSimpleName() + ".defaultComboText"))) {
                DescribeRefObject refDescribe = parentSObjectDescribeMap.getParentSObject(relationshipField.getParent().toString());
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
    
    private ParentIdLookupFieldFormatter getSelectedParentSObjectForLookupField(String parentAndRelNameInCombo, String selectedIdLookupField) {
        if(selectedIdLookupField != null && selectedIdLookupField.length() > 0
                && ! selectedIdLookupField.equals(Labels.getString("ForeignKeyExternalIdPage.defaultComboText"))) {
            ParentSObjectFormatter selectedParentSObjectField;
            try {
                selectedParentSObjectField = new ParentSObjectFormatter(parentAndRelNameInCombo);
            } catch (RelationshipFormatException e) {
                // TODO Auto-generated catch block
                logger.error(e.getMessage());
                return null;
            }
            Combo parentSObjectCombo = this.parentSObjectMap.get(selectedParentSObjectField.getRelationshipName());
            if (parentSObjectCombo == null) {
                return null;
            }
            String actualParentSObjectName = parentSObjectCombo.getText();
            ParentIdLookupFieldFormatter actualParentSObjectField;
            try {
                actualParentSObjectField = new ParentIdLookupFieldFormatter(
                        actualParentSObjectName
                        , selectedParentSObjectField.getRelationshipName()
                        , selectedIdLookupField);
            } catch (RelationshipFormatException e) {
                // TODO Auto-generated catch block
                logger.error(e.getMessage());
                return null;
            }
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
        this.parentSObjectDescribeMap = controller.getReferenceDescribes();
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
            String message = Labels.getFormattedString(this.getClass().getSimpleName() + ".pageMessage", this.controller.getAppConfig().getString(AppConfig.PROP_ENTITY));
            this.setMessage(message);
        }
        return success;
    }

}