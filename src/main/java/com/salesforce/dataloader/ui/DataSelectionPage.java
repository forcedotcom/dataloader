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

import org.apache.logging.log4j.Logger;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataAccessObjectFactory;
import com.salesforce.dataloader.ui.entitySelection.*;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;

/**
 * Describe your class here.
 *
 * @author Lexi Viripaeff
 * @since 6.0
 */
public class DataSelectionPage extends LoadPage {

    private final Logger logger = org.apache.logging.log4j.LogManager.getLogger(DataSelectionPage.class);

    private final Controller controller;

    // These filter extensions are used to filter which files are displayed.
    private static final String[] FILTER_EXTS = { "*.csv" }; //$NON-NLS-1$
    private final EntityFilter filter = new EntityFilter();
    private ListViewer lv;

    private FileFieldEditor csvChooser;

    public DataSelectionPage(Controller controller) {
        super(Labels.getString("DataSelectionPage.data"), Labels.getString("DataSelectionPage.dataMsg"), UIUtils.getImageRegistry().getDescriptor("splashscreens")); //$NON-NLS-1$ //$NON-NLS-2$

        this.controller = controller;

        // Set the description
        setDescription(Labels.getString("DataSelectionPage.message")); //$NON-NLS-1$

        setPageComplete(false);
    }

    @Override
    public void createControl(Composite parent) {
        getShell().setImage(UIUtils.getImageRegistry().get("sfdc_icon")); //$NON-NLS-1$

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.horizontalSpacing = 10;
        gridLayout.marginHeight = 15;
        gridLayout.verticalSpacing = 5;
        gridLayout.marginRight = 5;

        Composite comp = new Composite(parent, SWT.NONE);
        comp.setLayout(gridLayout);

        Label label = new Label(comp, SWT.RIGHT);
        label.setText(Labels.getString("DataSelectionPage.selectObject")); //$NON-NLS-1$
        GridData data = new GridData();
        label.setLayoutData(data);

        // Add a checkbox to toggle filter
        Button filterAll = new Button(comp, SWT.CHECK);
        filterAll.setText(Labels.getString("DataSelectionPage.showAll")); //$NON-NLS-1$
        data = new GridData();
        filterAll.setLayoutData(data);

        lv = new ListViewer(comp, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        lv.setContentProvider(new EntityContentProvider());
        lv.setLabelProvider(new EntityLabelProvider());
        lv.setInput(null);
        data = new GridData(GridData.FILL, GridData.FILL, true, true);
        data.heightHint = 140;
        data.widthHint = 140;
        lv.getControl().setLayoutData(data);
        lv.addFilter(filter);
        lv.setSorter(new EntityViewerSorter());

        lv.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                checkPageComplete();
            }

        });

        //if we're logged in, set the input
        if (controller.isLoggedIn()) {
            setInput(controller.getEntityDescribes());
        }

        filterAll.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (((Button)event.widget).getSelection())
                    lv.removeFilter(filter);
                else
                    lv.addFilter(filter);
            }
        });

        new Label(comp, SWT.NONE);

        final String infoMessage = this.controller.getConfig().getOperationInfo().getInfoMessageForDataSelectionPage();
        if (infoMessage != null) {
            Label l = new Label(comp, SWT.RIGHT);
            GridData gd = new GridData();
            gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_END;
            l.setLayoutData(gd);
            l.setText(infoMessage);
            l.setForeground(new Color(getShell().getDisplay(), 0xff, 0, 0));
        }

        new Label(comp, SWT.NONE);

        //now select the csv

        Composite compChooser = new Composite(comp, SWT.NONE);
        data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
        data.widthHint = 400;
        compChooser.setLayoutData(data);

        csvChooser = new FileFieldEditor(
                Labels.getString("DataSelectionPage.csv"), Labels.getString("DataSelectionPage.csvMessage"), compChooser); //$NON-NLS-1$ //$NON-NLS-2$
        csvChooser.setFileExtensions(FILTER_EXTS);
        csvChooser.setEmptyStringAllowed(false);
        csvChooser.setPropertyChangeListener(new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if ("field_editor_is_valid".equals(event.getProperty())) { //$NON-NLS-1$
                    try {

                        if (!((Boolean)event.getNewValue()).booleanValue()) {
                            setErrorMessage(Labels.getString("DataSelectionPage.selectValid")); //$NON-NLS-1$
                            checkPageComplete();

                        } else {
                            setErrorMessage(null);
                            checkPageComplete();
                        }
                    } catch (ClassCastException cle) {
                        logger.error(Labels.getString("DataSelectionPage.errorClassCast"), cle); //$NON-NLS-1$
                    }
                }
            }
        });

        setControl(comp);
    }

    /**
     * Function to dynamically set the entity list
     */
    private void setInput(Map<String, DescribeGlobalSObjectResult> entityDescribes) {
        OperationInfo operation = controller.getConfig().getOperationInfo();
        Map<String, DescribeGlobalSObjectResult> inputDescribes = new HashMap<String, DescribeGlobalSObjectResult>();

        // for each object, check whether the object is valid for the current
        // operation
        if (entityDescribes != null) {
            for (Entry<String, DescribeGlobalSObjectResult> entry : entityDescribes.entrySet()) {
                String objectName = entry.getKey();
                DescribeGlobalSObjectResult objectDesc = entry.getValue();
                if (operation.isDelete() && objectDesc.isDeletable()) {
                    inputDescribes.put(objectName, objectDesc);
                } else if (operation == OperationInfo.insert && objectDesc.isCreateable()) {
                    inputDescribes.put(objectName, objectDesc);
                } else if (operation == OperationInfo.update && objectDesc.isUpdateable()) {
                    inputDescribes.put(objectName, objectDesc);
                } else if (operation == OperationInfo.upsert && (objectDesc.isUpdateable() || objectDesc.isCreateable())) {
                    inputDescribes.put(objectName, objectDesc);
                }
            }
        }
        lv.setInput(inputDescribes);
        lv.refresh();
        lv.getControl().getParent().pack();

    }

    private boolean checkEntityStatus() {
        IStructuredSelection selection = (IStructuredSelection)lv.getSelection();
        DescribeGlobalSObjectResult entity = (DescribeGlobalSObjectResult)selection.getFirstElement();
        if (entity != null) {
            return true;
        }
        return false;

    }

    private void checkPageComplete() {

        if (csvChooser.isValid() && checkEntityStatus()) {
            setPageComplete(true);
        } else {
            setPageComplete(false);
        }

    }

    /**
     * Returns the next page, describes SObject and performs the total size calculation
     *
     * @return IWizardPage
     */

    @Override
    public LoadPage getNextPage() {
        //attempt to login
        Config config = controller.getConfig();
        //get entity
        IStructuredSelection selection = (IStructuredSelection)lv.getSelection();
        DescribeGlobalSObjectResult entity = (DescribeGlobalSObjectResult)selection.getFirstElement();

        config.setValue(Config.ENTITY, entity.getName());
        // set DAO - CSV file name
        config.setValue(Config.DAO_NAME, csvChooser.getStringValue());
        // set DAO type to CSV
        config.setValue(Config.DAO_TYPE, DataAccessObjectFactory.CSV_READ_TYPE);
        controller.saveConfig();

        DataSelectionDialog dlg = new DataSelectionDialog(getShell(), controller);
        if (dlg.open()) {
            return super.getNextPage();
        } else {
            return this;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.ui.LoadPage#setupPage()
     */
    @Override
    boolean setupPage() {
        Map<String, DescribeGlobalSObjectResult> describes = controller
                .getEntityDescribes();
        if(describes != null) {
            setInput(describes);
            return true;
        }
        return false;
    }
}
