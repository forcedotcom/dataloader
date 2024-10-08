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
import java.util.Map.Entry;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.MappingInitializationException;
import com.salesforce.dataloader.mapping.LoadMapper;
import com.salesforce.dataloader.ui.mapping.MappingContentProvider;
import com.salesforce.dataloader.ui.mapping.MappingLabelProvider;
import com.sforce.soap.partner.Field;

/**
 * Describe your class here.
 *
 * @author Lexi Viripaeff
 * @since 6.0
 */
public class MappingPage extends LoadPage {

    private TableViewer mappingTblViewer;
    private Map<String, Field> relatedFields;
    private Label mappingLabel;

    public MappingPage(Controller controller) {
        super("MappingPage", controller); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void createControl(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        GridData data;

        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.horizontalSpacing = 10;
        gridLayout.marginHeight = 15;
        comp.setLayout(gridLayout);

        Button buttonExisting = new Button(comp, SWT.PUSH | SWT.FLAT);
        buttonExisting.setText(Labels.getString("MappingPage.selectExisting")); //$NON-NLS-1$
        buttonExisting.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
                dlg.setFilterExtensions(new String[] { "*.sdl" });
                String filename = dlg.open();
                if (filename != null && !filename.isBlank()) { //$NON-NLS-1$
                    LoadMapper mapper = (LoadMapper)controller.getMapper();
                    mapper.clearMappings();
                    try {
                        mapper.putPropertyFileMappings(filename);
                        updateMappingTblViewer();
                    } catch (MappingInitializationException e) {
                        logger.error(Labels.getString("MappingPage.errorLoading"), e); //$NON-NLS-1$
                        UIUtils.errorMessageBox(getShell(), e);
                    }
                }
            }
        });

        Button buttonCreateNew = new Button(comp, SWT.PUSH | SWT.FLAT);
        buttonCreateNew.setText(Labels.getString("MappingPage.createEdit")); //$NON-NLS-1$
        final MappingPage thisPage = this;
        buttonCreateNew.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                MappingDialog dlg = new MappingDialog(getShell(), controller, thisPage);
                dlg.setMapper((LoadMapper)controller.getMapper());
                dlg.setUnmappedSobjectFields(autoMatchFieldsAndGetUnmappedFields());
                dlg.open();
            }
        });

        Label blankLabel2 = new Label(comp, SWT.NONE);
        data = new GridData();
        data.horizontalSpan = 2;
        data.heightHint = 15;
        blankLabel2.setLayoutData(data);


        Label sep3 = new Label(comp, SWT.HORIZONTAL | SWT.SEPARATOR);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        sep3.setLayoutData(data);

        mappingLabel = new Label(comp, SWT.NONE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        mappingLabel.setLayoutData(data);

        //  mapping field table viewer
        mappingTblViewer = new TableViewer(comp, SWT.FULL_SELECTION);
        mappingTblViewer.setContentProvider(new MappingContentProvider());
        mappingTblViewer.setLabelProvider(new MappingLabelProvider());

        //  Set up the mapping table
        Table mappingTable = mappingTblViewer.getTable();
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        mappingTable.setLayoutData(data);

        // Add the first column - name
        TableColumn csvFieldColumn = new TableColumn(mappingTable, SWT.LEFT);
        csvFieldColumn.setText(Labels.getString("MappingPage.fileColumn")); //$NON-NLS-1$
       //Add the second column - label
        TableColumn sobjectFieldColumn = new TableColumn(mappingTable, SWT.LEFT);
        sobjectFieldColumn.setText(Labels.getString("MappingPage.fieldName")); //$NON-NLS-1$

        //update the model
        updateMappingTblViewer();
        packMappingColumns();

        // Turn on the header and the lines
        mappingTable.setHeaderVisible(true);
        mappingTable.setLinesVisible(true);

        setControl(comp);
        setupPage();
    }

    public void packMappingColumns() {
        if (mappingTblViewer != null) {
            Table mappingTable = mappingTblViewer.getTable();
            //  Pack the columns
            for (int i = 0, n = mappingTable.getColumnCount(); i < n; i++) {
                mappingTable.getColumn(i).pack();
            }
        }
        refreshMapping();
    }

    /**
     * Responsible for updating the mapping model
     */
    public void updateMappingTblViewer() {
        // Set the table viewer's input
        if (mappingTblViewer != null) {
            mappingTblViewer.setInput(controller.getMapper());

            //start scrolled up, dumb swt
            Table table = mappingTblViewer.getTable();
            if (table.getItemCount() > 0) {
                table.showItem(table.getItem(0));
            }
        }
        packMappingColumns();
        mappingTblViewer.getControl().pack();
    }
    
    public Field[] autoMatchFieldsAndGetUnmappedFields() {
        Field[] sforceFields = getMappableSalesforceFields();
        if (sforceFields == null || sforceFields.length == 0) {
            return sforceFields;
        }
        ArrayList<Field> fieldList = new ArrayList<Field>(Arrays.asList(sforceFields));
        //first match on name, then label
        ListIterator<Field> iterator = fieldList.listIterator();
        Field sobjectField;
        LoadMapper mapper = (LoadMapper)controller.getMapper();
        while (iterator.hasNext()) {
            sobjectField = iterator.next();
            String sobjectFieldName = sobjectField.getName();
            String sobjectFieldLabel = sobjectField.getLabel();
            String daoColName = null;

            if (mapper.hasDaoColumn(sobjectFieldName)) {
                daoColName = sobjectFieldName;
            } else if (mapper.hasDaoColumn(sobjectFieldLabel)) {
                daoColName = sobjectFieldLabel;
            }

            if(daoColName != null) {
                // don't overwrite the fields that already have been mapped
                String prevMappedFieldName = mapper.getMapping(daoColName, false, true);
                if(prevMappedFieldName == null || prevMappedFieldName.length() == 0) {
                    mapper.putMapping(daoColName, sobjectFieldName);
                }
                iterator.remove();
            }
        }
        return fieldList.toArray(new Field[fieldList.size()]);
    }
    
    Field[] getMappableSalesforceFields() {
        ArrayList<Field> mappableFieldList = new ArrayList<Field>();
        AppConfig appConfig = controller.getAppConfig();
        OperationInfo operation = appConfig.getOperationInfo();
        String extIdField = appConfig.getString(AppConfig.IDLOOKUP_FIELD);
        if(extIdField == null) {
            extIdField = "";
        } else {
            extIdField = extIdField.toLowerCase();
        }

        for (Field field : getFieldTypes()) {
            boolean isMappable = false;
            switch (operation) {
            case insert:
                if (field.isCreateable()) {
                    isMappable = true;
                }
                break;
            case delete:
                if (controller.getAppConfig().isRESTAPIEnabled()
                        && Controller.getAPIMajorVersion() >= 61
                        && controller.getAppConfig().getBoolean(AppConfig.DELETE_WITH_EXTERNALID) 
                        && field.isIdLookup()) {
                    isMappable = true;
                }
                // do not break here. Continue to cover id field.
            case undelete:
            case hard_delete:
                if (field.getType().toString().toLowerCase().equals("id")) {
                    isMappable = true;
                }
                break;
            case upsert:
                if (field.isUpdateable() || field.isCreateable()
                        // also add idLookup-Fields (such as Id, Name) IF they are used as extIdField in this upsert
                        // (no need to add them otherwise, if they are not updateable/createable)
                        || (field.isIdLookup() && extIdField.equals(field.getName().toLowerCase()))) {
                    isMappable = true;
                }
                break;
            case update:
                if (field.isUpdateable() || field.getType().toString().toLowerCase().equals("id")) {
                    isMappable = true;
                }
                break;
            default:
                throw new UnsupportedOperationException();
            }
            if(isMappable) {
                mappableFieldList.add(field);
            }
        }

        return mappableFieldList.toArray(new Field[mappableFieldList.size()]);
    }

    private void refreshMapping() {
        if (mappingTblViewer != null) {
            mappingTblViewer.refresh();
        }
        UIUtils.setTableColWidth(this.mappingTblViewer.getTable());
    }

    public Field[] getFieldTypes() {
        Field[] result;
        Field[] fields = controller.getFieldTypes().getFields();
        if (controller.getAppConfig().getOperationInfo().isDelete()) {
            ArrayList<Field> refFieldList = new ArrayList<Field>();
            for (Field field : fields) {
                if (controller.getAppConfig().isRESTAPIEnabled()
                && Controller.getAPIMajorVersion() >= 61
                && controller.getAppConfig().getBoolean(AppConfig.DELETE_WITH_EXTERNALID) 
                && field.isIdLookup()) {
                    refFieldList.add(field);
                } else if (field.getType().toString().equalsIgnoreCase("id")) {
                    refFieldList.add(field);
                }
            }
            result = refFieldList.toArray(new Field[1]);
        } else {
            if(relatedFields != null) {
                result = addRelatedFields(fields);
            } else {
                result = fields;
            }
        }
        return result;
    }

    /**
     * Add fields for the related objects
     * @param fields
     */
    public void setRelatedFields(Map<String,Field> fields) {
        this.relatedFields = fields;
    }

    /**
     * @param fields
     */
    private Field[] addRelatedFields(Field[] fields) {
        List<Field> relatedFieldList = new ArrayList<Field>();
        for(Entry<String,Field> relatedFieldInfo : relatedFields.entrySet()) {
            Field lookupField = relatedFieldInfo.getValue();
            relatedFieldList.add(lookupField);
        }
        relatedFieldList.addAll(Arrays.asList(fields));
        return relatedFieldList.toArray(fields);
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.ui.LoadPage#setupPage()
     */
    @Override
    public boolean setupPagePostLogin() {
        if (controller.getMapper() == null) {
            return true; // further processing is not possible
        }
        autoMatchFieldsAndGetUnmappedFields();
        updateMappingTblViewer();
        return true;
    }

    @Override
    public void setPageComplete() {
        // no validations performed currently
        setPageComplete(true);
    }
}
