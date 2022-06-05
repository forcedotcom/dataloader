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

import java.util.*;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.ui.Labels;
import com.salesforce.dataloader.ui.UIUtils;
import com.sforce.soap.partner.*;

/**
 * Creates the soql
 * 
 * @author Lexi Viripaeff
 * @since 6.0
 */
public class ExtractionSOQLPage extends ExtractionPage {

    private final Controller controller;
    private final Logger logger = LogManager.getLogger(ExtractionSOQLPage.class);
    private Text soqlText;
    private Field[] fields;
    private CheckboxTableViewer fieldViewer;
    private final String[] operationsDisplayNormal = { "equals", "not equals", "less than", "greater than", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "less than or equals", "greater than or equals" }; //$NON-NLS-1$ //$NON-NLS-2$
    private final String[] operationsDisplayString = {
            "equals", "not equals", "like", "starts with", "ends with", "contains", "less than", "greater than", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            "less than or equals", "greater than or equals" }; //$NON-NLS-1$ //$NON-NLS-2$
    private final String[] operationsDisplayMulti = { "equals", "not equals", "includes", "excludes" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    private HashMap<String, String> operationMap;
    private CCombo fieldCombo;
    private Composite whereComp;
    private Composite builderComp;
    private boolean isPickListField;
    private boolean isFocusDialogWanted = true;
    private int lastFieldType;
    private Text valueText;
    private PicklistEntry[] picklistValues;
    private static final String SPACE = " "; //$NON-NLS-1$
    private static final String BEGIN_SINGLEQUOTE = " '"; //$NON-NLS-1$
    private static final String END_SINGLEQUOTE = "' "; //$NON-NLS-1$
    private static final String WILD_CARD = "%"; //$NON-NLS-1$
    private static final String OPEN_BRACKET = "(";//$NON-NLS-1$
    private static final String CLOSE_BRACKET = ")";//$NON-NLS-1$

    // fieldType constants
    private static final int FIELD_STRING = 0;
    private static final int FIELD_MULTI = 1;
    private static final int FIELD_NORMAL = 2;

    // SOQL building variables
    private StringBuffer fromPart;
    private final String SELECT = "Select "; //$NON-NLS-1$
    private StringBuffer fieldPart = new StringBuffer();
    private StringBuffer wherePart = new StringBuffer();
    private Combo operCombo;

    public ExtractionSOQLPage(Controller controller) {
        super(
                Labels.getString("ExtractionSOQLPage.title"), Labels.getString("ExtractionSOQLPage.titleMessage"), UIUtils.getImageRegistry().getDescriptor("splashscreens")); //$NON-NLS-1$ //$NON-NLS-2$

        this.controller = controller;

        // Set the description
        setDescription(Labels.getString("ExtractionSOQLPage.description")); //$NON-NLS-1$
        initOperMap();

        setPageComplete(false);
        lastFieldType = FIELD_NORMAL;
        isPickListField = false;

    }

    private void initOperMap() {
        operationMap = new HashMap<String, String>();
        operationMap.put("equals", "=");
        operationMap.put("not equals", "!=");
        operationMap.put("like", "like");
        operationMap.put("less than", "<");
        operationMap.put("greater than", ">");
        operationMap.put("less than or equals", "<=");
        operationMap.put("greater than or equals", ">=");
        operationMap.put("includes", "includes");
        operationMap.put("excludes", "excludes");
        operationMap.put("starts with", "like");
        operationMap.put("ends with", "like");
        operationMap.put("contains", "like");

    }

    private void setLastFieldType(int type) {
        lastFieldType = type;
    }

    @Override
    public void createControl(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        GridData data;

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.horizontalSpacing = 10;
        gridLayout.marginHeight = 20;
        comp.setLayout(gridLayout);

        builderComp = new Composite(comp, SWT.NONE);
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        builderComp.setLayoutData(data);
        gridLayout = new GridLayout(2, false);
        gridLayout.horizontalSpacing = 25;
        builderComp.setLayout(gridLayout);

        Label fieldLable = new Label(builderComp, SWT.LEFT);
        fieldLable.setText(Labels.getString("ExtractionSOQLPage.chooseFields")); //$NON-NLS-1$

        Label fieldWhere = new Label(builderComp, SWT.LEFT);
        fieldWhere.setText(Labels.getString("ExtractionSOQLPage.createClauses")); //$NON-NLS-1$

        Composite fieldComp = new Composite(builderComp, SWT.NONE);
        gridLayout = new GridLayout(1, false);
        gridLayout.horizontalSpacing = 25;
        fieldComp.setLayout(gridLayout);

        Text search = new Text(fieldComp, SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH);
        data = new GridData(GridData.FILL_HORIZONTAL);
        search.setLayoutData(data);
        fieldComp.setLayoutData(data);

        fieldViewer = CheckboxTableViewer.newCheckList(fieldComp, SWT.BORDER);
        ColumnViewerToolTipSupport.enableFor(fieldViewer);
        fieldViewer.setLabelProvider(new ExtrFieldLabelProvider());
        fieldViewer.setContentProvider(new ExtrFieldContentProvider());
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.heightHint = 100;
        fieldViewer.getTable().setLayoutData(data);

        FieldFilter filter = new FieldFilter(search);
        fieldViewer.addFilter(filter);
        fieldViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                generateFieldPart();
                generateSOQLText();
            }
        });
        
        search.addSelectionListener(new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                fieldViewer.refresh();
            }
        });
        
        search.addListener(SWT.KeyUp, new Listener() {
            public void handleEvent(Event e) {
                fieldViewer.refresh();
            }
        });

        whereComp = new Composite(builderComp, SWT.NONE);
        data = new GridData(GridData.FILL_BOTH);
        whereComp.setLayoutData(data);
        gridLayout = new GridLayout(3, false);
        whereComp.setLayout(gridLayout);

        Label fLabel = new Label(whereComp, SWT.LEFT);
        fLabel.setText(Labels.getString("ExtractionSOQLPage.fields")); //$NON-NLS-1$

        Label opLabel = new Label(whereComp, SWT.CENTER);
        opLabel.setText(Labels.getString("ExtractionSOQLPage.operation")); //$NON-NLS-1$

        Label valLabel = new Label(whereComp, SWT.CENTER);
        valLabel.setText(Labels.getString("ExtractionSOQLPage.value")); //$NON-NLS-1$

        fieldCombo = new CCombo(whereComp, SWT.DROP_DOWN | SWT.V_SCROLL);
        operCombo = new Combo(whereComp, SWT.DROP_DOWN);
        operCombo.setItems(operationsDisplayNormal);
        fieldCombo.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {}

            @Override
            public void widgetSelected(SelectionEvent event) {
                // get the selected string
                String name = fieldCombo.getText();

                // get the corresponding field
                if (name != null && name.length() > 0) {
                    for (int i = 0; i < fields.length; i++) {
                        Field field = fields[i];
                        if (name.equals(field.getName())) {

                            // picklist values
                            if (field.getType() == FieldType.picklist || field.getType() == FieldType.multipicklist) {
                                picklistValues = field.getPicklistValues();
                                isPickListField = true;
                            } else {
                                isPickListField = false;
                            }

                            // operations values
                            if (field.getType() == FieldType.string && lastFieldType != FIELD_STRING) {
                                operCombo.setItems(operationsDisplayString);
                                setLastFieldType(FIELD_STRING);
                            } else if (field.getType() == FieldType.multipicklist && lastFieldType != FIELD_MULTI) {
                                operCombo.setItems(operationsDisplayMulti);
                                setLastFieldType(FIELD_MULTI);
                            } else if (lastFieldType != FIELD_NORMAL && field.getType() != FieldType.multipicklist
                                    && !field.getType().toString().equals("string")) {
                                operCombo.setItems(operationsDisplayNormal);
                                setLastFieldType(FIELD_NORMAL);
                            }
                            break;
                        }
                    }
                }
            }
        });
        
        fieldCombo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                    String text = fieldCombo.getText();
                    // updateFieldComboList(text);
            }
        });
        
        fieldCombo.addMouseListener(new MouseListener() {

            @Override
            public void mouseDoubleClick(MouseEvent arg0) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mouseDown(MouseEvent arg0) {
                String text = fieldCombo.getText();
                updateFieldComboList(text); 
                fieldCombo.setText(text);
            }

            @Override
            public void mouseUp(MouseEvent arg0) {
                // TODO Auto-generated method stub
                
            }
        });

        valueText = new Text(whereComp, SWT.BORDER);
        data = new GridData();
        data.widthHint = 85;
        valueText.setLayoutData(data);
        valueText.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {

                // if a picklist, pop a list
                if (isPickListField && isFocusDialogWanted) {
                    ExtrPopupList popUp = new ExtrPopupList(valueText.getShell());

                    String[] values = new String[picklistValues.length];
                    for (int x = 0, end = picklistValues.length; x < end; x++) {
                        values[x] = picklistValues[x].getValue();
                    }
                    popUp.setItems(values);

                    Rectangle rect = valueText.getBounds();
                    Composite sizer = valueText.getParent();
                    Rectangle sizerRect;
                    // need to determine the absolute position
                    while (sizer != null) {
                        sizerRect = sizer.getBounds();
                        rect.x = rect.x + sizerRect.x;
                        rect.y = rect.y + sizerRect.y;
                        if (sizer instanceof Shell) break;
                        sizer = sizer.getParent();
                    }
                    // if we return to the text after a selection, we don't want to pop up again
                    isFocusDialogWanted = false;
                    String selection = popUp.open(rect);
                    if (selection != null) {
                        valueText.setText(selection);
                    } else {
                        // this ordering is wacky because of when the next event gets thrown in Windows
                        isFocusDialogWanted = true;
                    }

                } else {
                    isFocusDialogWanted = true;
                }

            }

            @Override
            public void focusLost(FocusEvent e) {}
        });

        Button addWhere = new Button(whereComp, SWT.PUSH | SWT.FLAT);
        addWhere.setText(Labels.getString("ExtractionSOQLPage.addCondition")); //$NON-NLS-1$
        addWhere.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String whereField = fieldCombo.getText();
                String whereOper = operCombo.getText();
                String whereValue = valueText.getText();

                if (validateStr(whereField) && validateStr(whereOper)) {
                    if (wherePart.length() == 0) {
                        wherePart.append("WHERE "); //$NON-NLS-1$
                    } else {
                        wherePart.append("AND "); //$NON-NLS-1$
                    }

                    boolean isSingleQuoteValue = isSingleQuoteValue(whereField);

                    wherePart.append(whereField);
                    wherePart.append(SPACE);
                    wherePart.append(getOperValue(whereOper));

                    boolean isMultiPickList = isMultiPicklistOper(whereOper);

                    if (isMultiPickList) {
                        wherePart.append(SPACE);
                        wherePart.append(OPEN_BRACKET);
                    }
                    if (isSingleQuoteValue) {
                        wherePart.append(BEGIN_SINGLEQUOTE);
                    } else {
                        wherePart.append(SPACE);
                    }
                    if (whereOper.equals("ends with") || whereOper.equals("contains")) {
                        wherePart.append(WILD_CARD);
                    }
                    wherePart.append(whereValue);
                    if (whereOper.equals("starts with") || whereOper.equals("contains")) {
                        wherePart.append(WILD_CARD);
                    }

                    if (isSingleQuoteValue) {
                        wherePart.append(END_SINGLEQUOTE);
                    } else {
                        wherePart.append(SPACE);
                    }
                    if (isMultiPickList) {
                        wherePart.append(CLOSE_BRACKET);
                    }

                }
                generateSOQLText();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });

        Button clearWhere = new Button(whereComp, SWT.PUSH | SWT.FLAT);
        clearWhere.setText(Labels.getString("ExtractionSOQLPage.clearAllConditions")); //$NON-NLS-1$
        data = new GridData();
        data.horizontalSpan = 2;
        clearWhere.setLayoutData(data);
        clearWhere.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                wherePart = new StringBuffer();
                generateSOQLText();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });

        // button Comp for fields
        Composite fieldButtonComp = new Composite(builderComp, SWT.NONE);
        gridLayout = new GridLayout(2, false);
        fieldButtonComp.setLayout(gridLayout);

        Button selectAll = new Button(fieldButtonComp, SWT.PUSH | SWT.FLAT);
        selectAll.setText(Labels.getString("ExtractionSOQLPage.selectAllFields")); //$NON-NLS-1$
        selectAll.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fieldViewer.setAllChecked(true);
                generateFieldPart();
                generateSOQLText();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });

        Button clearAll = new Button(fieldButtonComp, SWT.PUSH | SWT.FLAT);
        clearAll.setText(Labels.getString("ExtractionSOQLPage.clearAllFields")); //$NON-NLS-1$
        clearAll.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fieldViewer.setAllChecked(false);
                generateFieldPart();
                generateSOQLText();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });

        new Label(builderComp, SWT.NONE);

        // the bottow separator
        Label labelSeparatorBottom = new Label(comp, SWT.SEPARATOR | SWT.HORIZONTAL);
        data = new GridData(GridData.FILL_HORIZONTAL);
        labelSeparatorBottom.setLayoutData(data);

        Label messageLabel = new Label(comp, SWT.NONE);
        messageLabel.setText(Labels.getString("ExtractionSOQLPage.queryBelowMsg")); //$NON-NLS-1$

        soqlText = new Text(comp, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
        data = new GridData(GridData.FILL_BOTH);
        data.heightHint = 80;
        soqlText.setLayoutData(data);

        setControl(comp);
    }

    private String getOperValue(String operation) {
        return operationMap.get(operation);
    }

    private boolean isSingleQuoteValue(String fieldName) {
        Field field;
        for (int i = 0; i < fields.length; i++) {
            field = fields[i];
            if (field.getName().equals(fieldName)) {
                switch (field.getType()) {
                case _boolean:
                case _double:
                case _int:
                case currency:
                case date:
                case datetime:
                case percent:
                    // don't quote above types
                    return false;
                default:
                    // quote the rest
                    // string:
                    // base64:
                    // combobox:
                    // email:
                    // id:
                    // multipicklist:
                    // phone:
                    // picklist:
                    // reference:
                    // textarea:
                    // url:
                    return true;
                }
            }
        }
        return true;
    }

    private boolean isMultiPicklistOper(String value) {
        return (value.equals("includes") || value.equals("excludes"));
    }

    private void generateFieldPart() {
        Object[] fields = fieldViewer.getCheckedElements();
        fieldPart = new StringBuffer();
        Field field;
        for (int i = 0; i < fields.length; i++) {
            field = (Field)fields[i];
            fieldPart.append(field.getName());
            if ((i + 1) < fields.length) {
                fieldPart.append(", "); //$NON-NLS-1$

            }
        }
        fieldPart.append(" "); //$NON-NLS-1$

    }

    private boolean validateStr(String str) {
        if (str != null && str.length() > 0) { return true; }
        return false;

    }

    public void initializeSOQLText() {
        logger.debug(Labels.getString("ExtractionSOQLPage.initializeMsg")); //$NON-NLS-1$
        Config config = controller.getConfig();

        DescribeSObjectResult result = controller.getFieldTypes();
        fields = result.getFields();
        if (config.getBoolean(Config.SORT_EXTRACT_FIELDS)) {
            Arrays.sort(fields, new Comparator<Field>(){
                @Override
                public int compare(Field f1, Field f2)
                {
                    return f1.getName().compareTo(f2.getName());
                }
            });
        }
        fieldViewer.setInput(fields);
        updateFieldComboList(null);
        builderComp.layout();
        whereComp.layout();

        fromPart = new StringBuffer("FROM ").append(config.getString(Config.ENTITY)).append(" "); //$NON-NLS-1$ //$NON-NLS-2$

    }
    
    private void updateFieldComboList(String filterStr) {
        List<String> fieldNames = new ArrayList<String>();
        for (int i = 0; i < fields.length; i++) {
            // include all fields except encrypted string ones
            String name = fields[i].getName().toLowerCase();
            if(FieldType.encryptedstring != fields[i].getType()) {
                if (filterStr == null 
                        || filterStr.isEmpty() 
                        || name.contains(filterStr.toLowerCase())) {
                    fieldNames.add(fields[i].getName());
                }
            }
        }
        String[] fieldNamesArray = fieldNames.toArray(new String[fieldNames.size()]);
        Arrays.sort(fieldNamesArray);
        fieldCombo.setItems(fieldNamesArray);
    }

    private void generateSOQLText() {
        StringBuffer soql = new StringBuffer(SELECT);
        soql.append(fieldPart);
        soql.append(fromPart);
        soql.append(wherePart);
        soqlText.setText(soql.toString());

    }

    public String getSOQL() {
        return soqlText.getText();
    }

    @Override
    public IWizardPage getNextPage() {
        finishPage();

        // get the next wizard page
        ExtractionFinishPage finishPage = (ExtractionFinishPage)getWizard().getPage(
                Labels.getString("FinishPage.title")); //$NON-NLS-1$
        if (finishPage != null) {
            getWizard().getPage(Labels.getString("FinishPage.title"));
            finishPage.setPageComplete(true);
            return finishPage;
        } else {
            return super.getNextPage();
        }
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.ui.extraction.ExtractionPage#finishPage()
     */
    @Override
    public boolean finishPage() {
        controller.getConfig().setValue(Config.EXTRACT_SOQL, getSOQL());
        if (!controller.saveConfig()) { return false; }
        return true;
    }
}