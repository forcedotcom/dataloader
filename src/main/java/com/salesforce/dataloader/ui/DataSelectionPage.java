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


import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataAccessObjectFactory;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;

/**
 * Describe your class here.
 *
 * @author Lexi Viripaeff
 * @since 6.0
 */
public class DataSelectionPage extends LoadPage {

    // These filter extensions are used to filter which files are displayed.
    private static final String[] FILTER_EXTS = { "*.csv" }; //$NON-NLS-1$
    private ListViewer lv;

    private FileFieldEditor csvChooser;

    public DataSelectionPage(Controller controller) {
        super("DataSelectionPage", controller); //$NON-NLS-1$ //$NON-NLS-2$
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
        GridData data = new GridData(GridData.FILL_BOTH);
        comp.setLayoutData(data);
        lv = EntitySelectionListViewerUtil.getEntitySelectionListViewer(comp, this.controller.getConfig());
        lv.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                setPageComplete();
            }

        });

        setupPage();

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
                            setPageComplete();

                        } else {
                            setErrorMessage(null);
                            setPageComplete();
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
                if ((operation.isDelete() || operation.isUndelete()) && objectDesc.isDeletable()) {
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
        lv.getControl().getParent().pack();
        lv.refresh();
        Point shellSize = getCachedShellSize();
        shellSize.x += 1;
        shellSize.y += 1;
        this.getShell().setSize(shellSize);
    }

    private boolean checkEntityStatus() {
        IStructuredSelection selection = (IStructuredSelection)lv.getSelection();
        DescribeGlobalSObjectResult entity = (DescribeGlobalSObjectResult)selection.getFirstElement();
        if (entity != null) {
            return true;
        }
        return false;

    }

    public void setPageComplete() {
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
        //get entity
        IStructuredSelection selection = (IStructuredSelection)lv.getSelection();
        DescribeGlobalSObjectResult selectedEntity = (DescribeGlobalSObjectResult)selection.getFirstElement();
        DataSelectionDialog dlg = new DataSelectionDialog(getShell(), controller);
        if (dlg.open(DataAccessObjectFactory.CSV_READ_TYPE, 
                csvChooser.getStringValue(), selectedEntity.getName())) {
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
    public boolean setupPagePostLogin() {
        Map<String, DescribeGlobalSObjectResult> describes = controller.getEntityDescribes();
        if(describes == null) {
            return false;
        }
        setInput(describes);
        return true;
    }
}
