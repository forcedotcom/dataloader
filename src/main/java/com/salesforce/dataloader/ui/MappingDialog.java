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
import java.util.*;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.mapping.LoadMapper;
import com.salesforce.dataloader.ui.mapping.*;
import com.sforce.soap.partner.Field;

/**
 * This class creates the mapping dialog
 */
public class MappingDialog extends Dialog {
    private String input;

    private Controller controller;
    private final Logger logger = Logger.getLogger(MappingDialog.class);

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
    private Field[] fields;

    //all the fields
    private Field[] allFields;

    //restore for the old values on cancel
    private Properties restore;

    private LoadMapper mapper;
    private Field[] sforceFieldInfo;
    private MappingPage page;
    private HashSet<String> mappedFields;

    public void setSforceFieldInfo(Field[] sforceFieldInfo) {
        this.sforceFieldInfo = sforceFieldInfo;
    }

    public void setFields(Field[] newFields) {
        this.fields = newFields;
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
        this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        this.controller = controller;
        this.page = page;

    }

    /**
     * InputDialog constructor
     *
     * @param parent
     *            the parent
     * @param style
     *            the style
     */
    public MappingDialog(Shell parent, int style) {
        // Let users override the default styles
        super(parent, style);

        setText(Labels.getString("MappingDialog.title")); //$NON-NLS-1$
    }

    /**
     * Gets the input
     *
     * @return String
     */
    public String getInput() {
        return input;
    }

    /**
     * Sets the input
     *
     * @param input
     *            the new input
     */
    public void setInput(String input) {
        this.input = input;
    }

    /**
     * Opens the dialog and returns the input
     *
     * @return String
     */
    public String open() {
        // Create the dialog window
        Shell shell = new Shell(getParent(), getStyle() | SWT.RESIZE);
        shell.setText(getText());
        shell.setSize(600, 600);
        createContents(shell);
        shell.pack();
        shell.open();
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        // Return the entered value, or null
        return input;
    }

    /**
     * Creates the dialog's contents
     *
     * @param shell
     *            the dialog window
     */
    private void createContents(final Shell shell) {
        shell.setImage(UIUtils.getImageRegistry().get("sfdc_icon")); //$NON-NLS-1$
        shell.setLayout(new GridLayout(1, false));
        GridData data;

        //top label
        Label label = new Label(shell, SWT.NONE);
        label.setText(Labels.getString("MappingDialog.matchlabel")); //$NON-NLS-1$

        //buttons
        Composite comp = new Composite(shell, SWT.NONE);
        comp.setLayout(new GridLayout(2, false));

        Button buttonClear = new Button(comp, SWT.PUSH);
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

        Button buttonMatch = new Button(comp, SWT.PUSH);
        buttonMatch.setText(Labels.getString("MappingDialog.autoMatch")); //$NON-NLS-1$
        buttonMatch.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                autoMatchFields();
            }
        });

        ///////////////////////////////////////////////
        //InitializeSforceViewer
        ///////////////////////////////////////////////
        initializeSforceViewer(shell);

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
        Button ok = new Button(comp2, SWT.PUSH);
        ok.setText(Labels.getString("UI.ok")); //$NON-NLS-1$
        data = new GridData();
        data.widthHint = 75;
        ok.setLayoutData(data);
        ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                //refresh the mapping page view
                page.updateMapping();
                page.packMappingColumns();

                shell.close();
            }
        });

        Button buttonSave = new Button(comp2, SWT.PUSH);
        buttonSave.setText(Labels.getString("MappingDialog.saveMapping")); //$NON-NLS-1$
        data = new GridData();
        data.widthHint = Math.max(75, buttonSave.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
        buttonSave.setLayoutData(data);
        buttonSave.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                FileDialog dlg = new FileDialog(shell, SWT.SAVE);
                Config config = controller.getConfig();
                dlg.setFileName(config.getString(Config.MAPPING_FILE));
                dlg.setFilterPath(config.getString(Config.MAPPING_FILE));
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
                    config.setValue(Config.MAPPING_FILE, filename);
                    try {
                        mapper.save(filename);
                    } catch (IOException e1) {
                        logger.error(Labels.getString("MappingDialog.errorSave"), e1); //$NON-NLS-1$
                    }
                }

                //refresh the mapping page view
                page.updateMapping();
                page.packMappingColumns();
            }
        });

        // Create the cancel button and add a handler
        // so that pressing it will set input to null
        Button cancel = new Button(comp2, SWT.PUSH);
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
        mappingTblViewer.setSorter(new MappingViewerSorter());

        //add drop support
        int ops = DND.DROP_MOVE;
        Transfer[] transfers = new Transfer[] { TextTransfer.getInstance() };
        mappingTblViewer.addDropSupport(ops, transfers, new MappingDropAdapter(mappingTblViewer, this));

        //add drag support
        mappingTblViewer.addDragSupport(ops, transfers, new SforceDragListener(mappingTblViewer, this));

        //  Set up the sforce table
        Table mappingTable = mappingTblViewer.getTable();
        data = new GridData(GridData.FILL_BOTH);
        data.heightHint = 200;
        mappingTable.setLayoutData(data);

        //add key listener to process deletes
        mappingTable.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent event) {
                //\u007f is delete
                if (event.character == '\u007f') {
                    IStructuredSelection selection = (IStructuredSelection)mappingTblViewer.getSelection();
                    for (Iterator it = selection.iterator(); it.hasNext();) {
                        @SuppressWarnings("unchecked")
                        Map.Entry<String, String> elem = (Entry<String, String>)it.next();
                        String oldSforce = elem.getValue();
                        if (oldSforce != null && oldSforce.length() > 0) {
                            //clear the sforce
                            replenishField(oldSforce);

                            elem.setValue("");
                            mapper.removeMapping(elem.getKey());

                            packMappingColumns();
                            mappingTblViewer.refresh();
                        }
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent event) {}
        });

        // Add the first column - name
        TableColumn tc = new TableColumn(mappingTable, SWT.LEFT);
        tc.setText(Labels.getString("MappingDialog.fileColumn")); //$NON-NLS-1$
        tc.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                ((MappingViewerSorter)mappingTblViewer.getSorter()).doSort(MAPPING_DAO);
                mappingTblViewer.refresh();
            }
        });

        //Add the second column - label
        tc = new TableColumn(mappingTable, SWT.LEFT);
        tc.setText(Labels.getString("MappingDialog.fileName")); //$NON-NLS-1$
        tc.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                ((MappingViewerSorter)mappingTblViewer.getSorter()).doSort(MAPPING_SFORCE);
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

    private void initializeSforceViewer(Shell shell) {
        GridData data;

        //sforce field table viewer
        sforceTblViewer = new TableViewer(shell, SWT.FULL_SELECTION);
        sforceTblViewer.setContentProvider(new SforceContentProvider());
        sforceTblViewer.setLabelProvider(new SforceLabelProvider());
        sforceTblViewer.setSorter(new SforceViewerSorter());

        //add drag support
        int ops = DND.DROP_MOVE;
        Transfer[] transfers = new Transfer[] { TextTransfer.getInstance() };
        sforceTblViewer.addDragSupport(ops, transfers, new MappingDragListener(sforceTblViewer, this));

        //add drop support
        sforceTblViewer.addDropSupport(ops, transfers, new SforceDropAdapter(sforceTblViewer, this));

        // Set up the sforce table
        Table sforceTable = sforceTblViewer.getTable();
        data = new GridData(GridData.FILL_BOTH);
        data.heightHint = 150;
        sforceTable.setLayoutData(data);

        // Add the first column - name
        TableColumn tc = new TableColumn(sforceTable, SWT.LEFT);
        tc.setText(Labels.getString("MappingDialog.sforceName")); //$NON-NLS-1$
        tc.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                ((SforceViewerSorter)sforceTblViewer.getSorter()).doSort(FIELD_NAME);
                sforceTblViewer.refresh();
            }
        });

        //Add the second column - label
        tc = new TableColumn(sforceTable, SWT.LEFT);
        tc.setText(Labels.getString("MappingDialog.sforceLabel")); //$NON-NLS-1$
        tc.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                ((SforceViewerSorter)sforceTblViewer.getSorter()).doSort(FIELD_LABEL);
                sforceTblViewer.refresh();
            }
        });

        //  Add the third column - type
        tc = new TableColumn(sforceTable, SWT.RIGHT);
        tc.setText(Labels.getString("MappingDialog.sforceType")); //$NON-NLS-1$
        tc.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                ((SforceViewerSorter)sforceTblViewer.getSorter()).doSort(FIELD_TYPE);
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

    private void autoMatchFields() {

        LinkedList<Field> fieldList = new LinkedList<Field>(Arrays.asList(fields));
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

        fields = fieldList.toArray(new Field[fieldList.size()]);

        sforceTblViewer.setInput(fields);
        sforceTblViewer.refresh();
        mappingTblViewer.refresh();

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
    }

    private void packSforceColumns() {
        Table sforceTable = sforceTblViewer.getTable();
        //  Pack the columns
        for (int i = 0, n = sforceTable.getColumnCount(); i < n; i++) {
            sforceTable.getColumn(i).pack();
        }
    }

    public void replenishField(String fieldName) {
        //find the Field object to add to the current list.
        Field field;
        for (int i = 0; i < allFields.length; i++) {
            field = allFields[i];
            if (field.getName().equals(fieldName)) {
                ArrayList<Field> fieldArray = new ArrayList<Field>(Arrays.asList(fields));

                //else add the field
                fieldArray.add(field);
                fields = fieldArray.toArray(new Field[fieldArray.size()]);

                //then refresh
                sforceTblViewer.setInput(fields);
                sforceTblViewer.refresh();

                packSforceColumns();

                return;
            }
        }
    }

    /**
     * Responsible for updating the sforce model
     */
    private void updateSforce() {

        ArrayList<Field> mappableFieldList = new ArrayList<Field>();
        ArrayList<Field> allFieldList = new ArrayList<Field>();
        Field field;
        OperationInfo operation = controller.getConfig().getOperationInfo();
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
            case hard_delete:
                if (field.getType().toString().toLowerCase().equals("id")) {
                    isMappable = true;
                }
                break;
            case upsert:
                if (field.isUpdateable() || field.isCreateable()
                        || field.getType().toString().toLowerCase().equals("id")) {
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

        fields = mappableFieldList.toArray(new Field[mappableFieldList.size()]);
        allFields = allFieldList.toArray(new Field[allFieldList.size()]);

        // Set the table viewer's input
        sforceTblViewer.setInput(fields);
    }

    /**
     * Clears the mapping
     */
    private void clearMapping() {
        mapper.clearMap();
        // restore the fields that were mapped before
        for(String fieldName : mappedFields) {
            replenishField(fieldName);
        }
        mappedFields.clear();
        mappingTblViewer.refresh();
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

}
