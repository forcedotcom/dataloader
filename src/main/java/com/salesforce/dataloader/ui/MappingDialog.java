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
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.mapping.LoadMapper;
import com.salesforce.dataloader.ui.mapping.*;
import com.salesforce.dataloader.util.AppUtil;
import com.sforce.soap.partner.Field;

/**
 * This class creates the mapping dialog
 */
public class MappingDialog extends WizardDialog {
    //the two tableViewers
    private TableViewer sforceTblViewer;
    private TableViewer mappingTblViewer;

    // field Constants
    public static final int FIELD_NAME = 0;
    public static final int FIELD_LABEL = 1;
    public static final int FIELD_TYPE = 2;

    public static final int MAPPING_DAO = 0;
    public static final int MAPPING_SFORCE = 1;

    //the current list of fields
    private Field[] sforceFields;

    //all the fields
    private Field[] allFields;

    //restore for the old values on cancel
    private Properties restore;

    private LoadMapper mapper;
    private Field[] sforceFieldInfo;
    private MappingPage page;
    private HashSet<String> mappedFields;
    private Shell parentShell;
    private Shell dialogShell;
    private Text sforceFieldsSearch;

    public void setSforceFieldInfo(Field[] sforceFieldInfo) {
        this.sforceFieldInfo = sforceFieldInfo;
    }

    public void setSforceFields(Field[] newFields) {
        this.sforceFields = newFields;
    }

    public void setMapper(LoadMapper mapper) {
        this.mapper = mapper;
        this.restore = new Properties();
        this.mappedFields = new HashSet<String>();
        Map<String, String> elements = mapper.getMappingWithUnmappedColumns(true);
        for (Map.Entry<String, String> entry : elements.entrySet()) {
            String localColumn = entry.getKey();
            String sfdcColumn = entry.getValue();
            if (sfdcColumn != null) {
                this.restore.put(localColumn, sfdcColumn);
                this.mappedFields.add(sfdcColumn);
            }
        }
    }

    /**
     * MappingDialog constructor
     *
     * @param parent
     */
    public MappingDialog(Shell parent, Controller controller, MappingPage page) {
        // Pass the default styles here
        super(parent, controller);
        this.page = page;
        this.parentShell = parent;
    }
    
    public Shell getParent() {
        return this.parentShell;
    }

    /**
     * Creates the dialog's contents
     *
     * @param shell
     *            the dialog window
     */
    protected void createContents(final Shell shell) {
        this.dialogShell = shell;
        shell.setImage(UIUtils.getImageRegistry().get("sfdc_icon")); //$NON-NLS-1$
        shell.setLayout(new GridLayout(1, false));
        GridData data;

        //top label
        Label label = new Label(shell, SWT.NONE);
        label.setText(Labels.getString("MappingDialog.matchlabel")); //$NON-NLS-1$
        
        //buttons
        Composite comp = new Composite(shell, SWT.NONE);
        comp.setLayout(new GridLayout(2, false));

        Button buttonClear = new Button(comp, SWT.PUSH | SWT.FLAT);
        buttonClear.setText(Labels.getString("MappingDialog.clearMapping")); //$NON-NLS-1$
        buttonClear.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                updateSforce();
                clearMapping();
                packMappingColumns();
                packSforceColumns();
            }
        });

        Button buttonMatch = new Button(comp, SWT.PUSH | SWT.FLAT);
        buttonMatch.setText(Labels.getString("MappingDialog.autoMatch")); //$NON-NLS-1$
        buttonMatch.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                autoMatchFields();
            }
        });
        
        sforceFieldsSearch = new Text(shell, SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH);
        sforceFieldsSearch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        sforceFieldsSearch.addListener(SWT.KeyUp, new Listener() {
            public void handleEvent(Event e) {
                sforceTblViewer.refresh();
            }
        });

        ///////////////////////////////////////////////
        //InitializeSforceViewer
        ///////////////////////////////////////////////
        initializeSforceViewer(shell, sforceFieldsSearch);

        Label sep1 = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
        sep1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite compMid = new Composite(shell, SWT.NONE);
        compMid.setLayout(new GridLayout(2, false));

        new Label(compMid, SWT.NONE).setText(Labels.getString("MappingDialog.dragFields")); //$NON-NLS-1$
        new Label(compMid, SWT.NONE).setImage(UIUtils.getImageRegistry().get("downArrow")); //$NON-NLS-1$

        Label sep2 = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
        sep2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        ///////////////////////////////////////////////
        //InitializeMappingViewer
        ///////////////////////////////////////////////
        initializeMappingViewer(shell);

        Label sep3 = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
        sep3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        //  buttons
        Composite comp2 = new Composite(shell, SWT.NONE);
        comp2.setLayout(new GridLayout(3, false));

        // Create the OK button and add a handler
        // so that pressing it will set input
        // to the entered value
        Button ok = new Button(comp2, SWT.PUSH | SWT.FLAT);
        ok.setText(Labels.getString("UI.ok")); //$NON-NLS-1$
        data = new GridData();
        data.widthHint = 75;
        ok.setLayoutData(data);
        ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                //refresh the mapping page view
                page.updateMapping();
                page.setPageComplete();
                shell.close();
            }
        });

        Button buttonSave = new Button(comp2, SWT.PUSH | SWT.FLAT);
        buttonSave.setText(Labels.getString("MappingDialog.saveMapping")); //$NON-NLS-1$
        data = new GridData();
        data.widthHint = Math.max(75, buttonSave.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
        buttonSave.setLayoutData(data);
        buttonSave.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                FileDialog dlg = new FileDialog(shell, SWT.SAVE);
                dlg.setFilterExtensions(new String[] { "*.sdl" });
                String filename = dlg.open();
                boolean cancel = false;

                if (filename == null || "".equals(filename)) { //$NON-NLS-1$
                    cancel = true;
                }

                // check if file already exists
                while (!cancel && new File(filename).exists()) {
                    int selectedButton = UIUtils.warningConfMessageBox(shell, Labels.getString("UI.fileAlreadyExists"));
                    if (selectedButton == SWT.YES) {
                        break;
                    } else if (selectedButton == SWT.NO) {
                        filename = dlg.open();
                        if (filename == null || "".equals(filename)) { //$NON-NLS-1$
                            cancel = true;
                        }
                    }
                }

                if (!cancel) {
                    try {
                        mapper.save(filename);
                    } catch (IOException e1) {
                        logger.error(Labels.getString("MappingDialog.errorSave"), e1); //$NON-NLS-1$
                    }
                    
                }

                //refresh the mapping page view
                page.updateMapping();
                page.setPageComplete();
                shell.close();
            }
        });

        // Create the cancel button and add a handler
        // so that pressing it will set input to null
        Button cancel = new Button(comp2, SWT.PUSH | SWT.FLAT);
        cancel.setText(Labels.getString("UI.cancel")); //$NON-NLS-1$
        data = new GridData();
        data.widthHint = 75;
        cancel.setLayoutData(data);
        cancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {

                //revert the Mapping back
                mapper.clearMap();
                mapper.putPropertyFileMappings(restore);

                shell.close();
            }
        });

        // Set the OK button as the default, so
        // user can type input and press Enter
        // to dismiss
        shell.setDefaultButton(ok);
    }

    private void initializeMappingViewer(Shell shell) {
        GridData data;
        //  sforce field table viewer
        mappingTblViewer = new TableViewer(shell, SWT.FULL_SELECTION);
        mappingTblViewer.setContentProvider(new MappingContentProvider());
        mappingTblViewer.setLabelProvider(new MappingLabelProvider());

        data = new GridData(GridData.FILL_BOTH);

        //add drop support
        int ops = DND.DROP_MOVE;
        Transfer[] transfers = new Transfer[] { TextTransfer.getInstance() };
        mappingTblViewer.addDropSupport(ops, transfers, new MappingDropAdapter(mappingTblViewer, this, getController()));

        //add drag support
        mappingTblViewer.addDragSupport(ops, transfers, new SforceDragListener(mappingTblViewer, this));

        //  Set up the sforce table
        Table mappingTable = mappingTblViewer.getTable();
        Rectangle shellBounds = getPersistedDialogBounds();
        data = new GridData(GridData.FILL_BOTH);
        data.widthHint = shellBounds.width;
        data.heightHint = shellBounds.height / 3;
       // data.heightHint = shellBounds.height;
        mappingTable.setLayoutData(data);

        //add key listener to process deletes
        mappingTable.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent event) {
                //\u007f is delete
                if (event.character == '\u007f' || event.character == '\b') {
                    IStructuredSelection selection = (IStructuredSelection)mappingTblViewer.getSelection();
                    for (Iterator<?> it = selection.iterator(); it.hasNext();) {
                        @SuppressWarnings("unchecked")
                        Map.Entry<String, String> elem = (Entry<String, String>)it.next();
                        String oldSforce = elem.getValue();
                        if (oldSforce != null && oldSforce.length() > 0) {
                            //clear the sforce
                            replenishMappedSforceFields(oldSforce);
                            elem.setValue("");
                            mapper.removeMapping(elem.getKey());
                            packMappingColumns();
                        }
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent event) {}
        });

        // Add the first column - column header in CSV file
        TableColumn csvFieldsCol = new TableColumn(mappingTable, SWT.LEFT);
        String headerStr = Labels.getString("MappingDialog.fileColumn");
        csvFieldsCol.setText(headerStr); //$NON-NLS-1$
        csvFieldsCol.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (mappingTblViewer.getComparator() == null) {
                    mappingTblViewer.setComparator(new MappingViewerComparator());
                }
                ((MappingViewerComparator)mappingTblViewer.getComparator()).doSort(MAPPING_DAO);
                mappingTblViewer.refresh();
            }
        });

        //Add the second column - name of Salesforce object field
        TableColumn sforceFieldNamesCol = new TableColumn(mappingTable, SWT.LEFT);
        headerStr = Labels.getString("MappingDialog.sforceFieldName");
        sforceFieldNamesCol.setText(headerStr); //$NON-NLS-1$
        sforceFieldNamesCol.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (mappingTblViewer.getComparator() == null) {
                    mappingTblViewer.setComparator(new MappingViewerComparator());
                }
                ((MappingViewerComparator)mappingTblViewer.getComparator()).doSort(MAPPING_SFORCE);
                mappingTblViewer.refresh();
            }
        });

        //update the model
        updateMapping();

        packMappingColumns();

        // Turn on the header and the lines
        mappingTable.setHeaderVisible(true);
        mappingTable.setLinesVisible(true);

        //start scrolled up, dumb swt
        if (mappingTable.getItemCount() > 0) {
            mappingTable.showItem(mappingTable.getItem(0));
        }
    }

    private void initializeSforceViewer(Shell shell, Text sforceFieldsSearch) {
        GridData data;

        //sforce field table viewer
        sforceTblViewer = new TableViewer(shell, SWT.FULL_SELECTION);
        sforceTblViewer.setContentProvider(new SforceContentProvider());
        sforceTblViewer.setLabelProvider(new SforceLabelProvider(this.getController()));
        sforceTblViewer.setComparator(new SforceViewerComparator());
        sforceTblViewer.addFilter(new SforceFieldsFilter(sforceFieldsSearch));

        //add drag support
        int ops = DND.DROP_MOVE;
        Transfer[] transfers = new Transfer[] { TextTransfer.getInstance() };
        sforceTblViewer.addDragSupport(ops, transfers, new MappingDragListener(sforceTblViewer, this));

        //add drop support
        sforceTblViewer.addDropSupport(ops, transfers, new SforceDropAdapter(sforceTblViewer, this));

        // Set up the sforce table
        Table sforceTable = sforceTblViewer.getTable();
        data = new GridData(GridData.FILL_BOTH);
        Rectangle shellBounds = getPersistedDialogBounds();
        data.widthHint = shellBounds.width;
        data.heightHint = shellBounds.height / 3;
        sforceTable.setLayoutData(data);

        // Add the first column - name
        TableColumn tc = new TableColumn(sforceTable, SWT.LEFT);
        tc.setText(Labels.getString("MappingDialog.sforceFieldName")); //$NON-NLS-1$
        tc.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                ((SforceViewerComparator)sforceTblViewer.getComparator()).doSort(FIELD_NAME);
                sforceTblViewer.refresh();
            }
        });

        //Add the second column - label
        tc = new TableColumn(sforceTable, SWT.LEFT);
        tc.setText(Labels.getString("MappingDialog.sforceFieldLabel")); //$NON-NLS-1$
        tc.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                ((SforceViewerComparator)sforceTblViewer.getComparator()).doSort(FIELD_LABEL);
                sforceTblViewer.refresh();
            }
        });

        //  Add the third column - type
        tc = new TableColumn(sforceTable, SWT.LEFT);
        tc.setText(Labels.getString("MappingDialog.sforceFieldType")); //$NON-NLS-1$
        tc.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                ((SforceViewerComparator)sforceTblViewer.getComparator()).doSort(FIELD_TYPE);
                sforceTblViewer.refresh();
            }
        });

        //update the model
        updateSforce();

        //pack the columns
        packSforceColumns();

        // Turn on the header and the lines
        sforceTable.setHeaderVisible(true);
        sforceTable.setLinesVisible(true);

        //start scrolled up, dumb swt
        if (sforceTable.getItemCount() > 0) {
            sforceTable.showItem(sforceTable.getItem(0));
        }

    }
    
    protected void setShellBounds(Shell dialogShell) {
        Rectangle shellBounds = getPersistedDialogBounds();
        dialogShell.setBounds(shellBounds);
        dialogShell.addListener(SWT.Resize, this::persistedDialogShellBoundsChanged);
        packMappingColumns();
        packSforceColumns();
    }

    private void autoMatchFields() {

        ArrayList<Field> fieldList = new ArrayList<Field>(Arrays.asList(sforceFields));
        //first match on name, then label
        ListIterator<Field> iterator = fieldList.listIterator();
        Field field;
        while (iterator.hasNext()) {
            field = iterator.next();
            String fieldName = field.getName();
            String fieldLabel = field.getLabel();
            String mappingSource = null;

            // field is already mapped
            // TODO: check with lexi if this is intended use of automatch
            if(mappedFields.contains(fieldName)) {
                continue;
            }

            if (mapper.hasDaoColumn(fieldName)) {
                mappingSource = fieldName;
            } else if (mapper.hasDaoColumn(fieldLabel)) {
                mappingSource = fieldLabel;
            }

            if(mappingSource != null) {
                // don't overwrite the fields that already have been mapped
                String oldFieldName = mapper.getMapping(mappingSource);
                if(oldFieldName == null || oldFieldName.length() == 0) {
                    mapper.putMapping(mappingSource, fieldName);
                }
                iterator.remove();
            }
        }

        this.sforceFields = fieldList.toArray(new Field[fieldList.size()]);

        sforceTblViewer.setInput(this.sforceFields);
        updateMapping();
        //pack the columns
        packMappingColumns();
        packSforceColumns();
        
    }

    public void packMappingColumns() {
        Table mappingTable = mappingTblViewer.getTable();
        //  Pack the columns
        for (int i = 0, n = mappingTable.getColumnCount(); i < n; i++) {
            mappingTable.getColumn(i).pack();
        }
        mappingTblViewer.refresh();
        mappingTable.redraw();
        UIUtils.setTableColWidth(mappingTable);
    }

    private void packSforceColumns() {
        Table sforceTable = sforceTblViewer.getTable();
        //  Pack the columns
        for (int i = 0, n = sforceTable.getColumnCount(); i < n; i++) {
            sforceTable.getColumn(i).pack();
        }
        sforceTblViewer.refresh();
        sforceTable.redraw();
        UIUtils.setTableColWidth(sforceTable);

    }

    public void replenishMappedSforceFields(String fieldNameList) {
        String[] fieldNameListArray = fieldNameList.split(AppUtil.COMMA);
        ArrayList<Field> fieldList = new ArrayList<Field>(Arrays.asList(this.sforceFields));
        for (String fieldNameToReplenish : fieldNameListArray) {
            fieldNameToReplenish = fieldNameToReplenish.strip();
            //find the Field object to add to the current list.
            Field field;
            for (int i = 0; i < allFields.length; i++) {
                field = allFields[i];
                if (field.getName().equals(fieldNameToReplenish)) {
                    fieldList.add(field);
                }
            }
        }
        this.sforceFields = fieldList.toArray(new Field[fieldList.size()]);
        sforceTblViewer.setInput(this.sforceFields);
        packSforceColumns();
        return;
    }

    /**
     * Responsible for updating the sforce model
     */
    private void updateSforce() {

        ArrayList<Field> mappableFieldList = new ArrayList<Field>();
        ArrayList<Field> allFieldList = new ArrayList<Field>();
        Field field;
        Config config = getController().getConfig();
        OperationInfo operation = config.getOperationInfo();
        String extIdField = config.getString(Config.IDLOOKUP_FIELD);
        if(extIdField == null) {
            extIdField = "";
        } else {
            extIdField = extIdField.toLowerCase();
        }

        for (int i = 0; i < sforceFieldInfo.length; i++) {

            field = sforceFieldInfo[i];
            boolean isMappable = false;
            switch (operation) {
            case insert:
                if (field.isCreateable()) {
                    isMappable = true;
                }
                break;
            case delete:
                if (getController().getConfig().isRESTAPIEnabled()
                        && Controller.getAPIMajorVersion() >= 61
                        && getController().getConfig().getBoolean(Config.DELETE_WITH_EXTERNALID) 
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
            // only add the field to mappings if it's not already used in mapping
            if(isMappable) {
                if(!mappedFields.contains(field.getName())) {
                    mappableFieldList.add(field);
                }
                // this list is for all fields in case map is reset
                allFieldList.add(field);
            }
        }

        this.sforceFields = mappableFieldList.toArray(new Field[mappableFieldList.size()]);
        allFields = allFieldList.toArray(new Field[allFieldList.size()]);

        // Set the table viewer's input
        sforceTblViewer.setInput(this.sforceFields);
    }

    /**
     * Clears the mapping
     */
    private void clearMapping() {
        mapper.clearMap();
        // restore the fields in sforceTblViewer that were mapped before
        for(String fieldName : mappedFields) {
            replenishMappedSforceFields(fieldName);
        }
        mappedFields.clear();
        packMappingColumns();
    }

    /**
     * Responsible for updating the mapping model
     */
    private void updateMapping() {

        // Set the table viewer's input
        mappingTblViewer.setInput(mapper);
    }

    public LoadMapper getMapper() {
        return this.mapper;
    }
    
    private void persistedDialogShellBoundsChanged(Event event) {
        switch (event.type) {
            case SWT.Resize:
            case SWT.Move:
                if (!this.dialogShell.isVisible()) {
                    return;
                }
                Config config = this.getController().getConfig();
                Rectangle shellBounds = this.dialogShell.getBounds();
                config.setValue(Config.DIALOG_BOUNDS_PREFIX + this.getClass().getSimpleName() + Config.DIALOG_WIDTH_SUFFIX, shellBounds.width);
                config.setValue(Config.DIALOG_BOUNDS_PREFIX + this.getClass().getSimpleName() + Config.DIALOG_HEIGHT_SUFFIX, shellBounds.height);
                try {
                    config.save();
                } catch (GeneralSecurityException | IOException e) {
                    // no-op
                    e.printStackTrace();
                }
                break;
        }
    }
}

/**
* This class filters the Saleforce fields list
*/

class SforceFieldsFilter extends ViewerFilter {
   private Text searchText;
   public SforceFieldsFilter(Text search) {
       super();
       this.searchText = search;
   }
   /**
    * Returns whether the specified element passes this filter
    *
    * @param arg0
    *            the viewer
    * @param arg1
    *            the parent element
    * @param arg2
    *            the element
    * @return boolean
    */
   @Override
   public boolean select(Viewer arg0, Object arg1, Object arg2) {

       Field selectedField = (Field)arg2;
       String fieldName = selectedField.getName();
       String fieldLabel = selectedField.getLabel();
       String searchText = this.searchText.getText();
       if (searchText != null && !searchText.isBlank()) {
           searchText = searchText.toLowerCase();
           if ((fieldName != null && fieldName.toLowerCase().contains(searchText)) 
              || (fieldLabel != null && fieldLabel.toLowerCase().contains(searchText))) {
               return true;
           } else {
               return false;
           }
       }
       return true;
   }
}
