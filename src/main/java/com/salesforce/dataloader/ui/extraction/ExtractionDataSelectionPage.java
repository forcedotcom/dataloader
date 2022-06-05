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

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataAccessObjectFactory;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import com.salesforce.dataloader.exception.MappingInitializationException;
import com.salesforce.dataloader.ui.EntitySelectionListViewerUtil;
import com.salesforce.dataloader.ui.Labels;
import com.salesforce.dataloader.ui.UIUtils;
import com.salesforce.dataloader.ui.entitySelection.*;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.ws.ConnectionException;

/**
 * Describe your class here.
 *
 * @author Lexi Viripaeff
 * @since 6.0
 */
public class ExtractionDataSelectionPage extends WizardPage {

    private final Controller controller;

    // These filter extensions are used to filter which files are displayed.
    private EntityFilter filter;
    private ListViewer lv;
    private Text fileText;
    public Composite comp;
    private boolean success;

    public ExtractionDataSelectionPage(Controller controller) {
        super(	Labels.getString("ExtractionDataSelectionPage.title"), 
                Labels.getString("ExtractionDataSelectionPage.titleMsg"), 
                UIUtils.getImageRegistry().getDescriptor("splashscreens")); //$NON-NLS-1$ //$NON-NLS-2$

        this.controller = controller;

        // Set the description
        setDescription(Labels.getString("ExtractionDataSelectionPage.description")
        + "\n\n"
        + Labels.getString("ExtractionInputDialog.querySize")
        + " "
        + controller.getConfig().getString(Config.EXTRACT_REQUEST_SIZE)); //$NON-NLS-1$

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

        comp = new Composite(parent, SWT.NONE);
        comp.setLayout(gridLayout);
        lv = EntitySelectionListViewerUtil.getEntitySelectionListViewer(comp);
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

        Label clearLabel = new Label(comp, SWT.NONE);
        GridData data = new GridData(GridData.VERTICAL_ALIGN_END);
        data.heightHint = 20;
        clearLabel.setLayoutData(data);

        //now select the file
        Composite compChooser = new Composite(comp, SWT.NONE);
        data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);

        compChooser.setLayoutData(data);

        GridLayout gLayout = new GridLayout(3, false);
        compChooser.setLayout(gLayout);

        //file Label
        Label fileLabel = new Label(compChooser, SWT.NONE);
        fileLabel.setText(Labels.getString("ExtractionDataSelectionPage.chooseTarget")); //$NON-NLS-1$

        //file text
        fileText = new Text(compChooser, SWT.BORDER);
        fileText.setText(Labels.getString("ExtractionDataSelectionPage.defaultFileName")); //$NON-NLS-1$
        data = new GridData(GridData.FILL_HORIZONTAL);
        fileText.setLayoutData(data);

        fileText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                checkPageComplete();
            }
        });

        Button fileButton = new Button(compChooser, SWT.PUSH | SWT.FLAT);
        fileButton.setText(Labels.getString("ExtractionDataSelectionPage.chooseFile")); //$NON-NLS-1$
        fileButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                FileDialog dlg = new FileDialog(getShell(), SWT.SAVE);
                String initialFile = fileText.getText();
                if(initialFile.length() == 0) {
                    initialFile = Labels.getString("ExtractionDataSelectionPage.defaultFileName"); //$NON-NLS-1$
                }
                dlg.setFileName(initialFile);
                String filename = dlg.open();
                if (filename != null && !"".equals(filename)) { //$NON-NLS-1$
                    //set the text, and see if the page is valid
                    fileText.setText(filename);
                    checkPageComplete();
                }
            }
        });

        setControl(comp);
    }

    /**
     * Function to dynamically set the entity list
     */
    public void setInput(Map<String, DescribeGlobalSObjectResult> entityDescribes) {
        Map<String, DescribeGlobalSObjectResult> inputDescribes = new HashMap<String, DescribeGlobalSObjectResult>();

        if (entityDescribes != null) {
            // for each object, check whether the object is valid for the Extract operation
            for (Entry<String, DescribeGlobalSObjectResult> entry : entityDescribes.entrySet()) {
                if (entry.getValue().isQueryable()) {
                    inputDescribes.put(entry.getKey(), entry.getValue());
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
        if (entity != null) { return true; }
        return false;

    }

    private void checkPageComplete() {

        if (!(fileText.getText().equals("")) && checkEntityStatus()) { //$NON-NLS-1$
            setPageComplete(true);
        } else {
            setPageComplete(false);
        }

    }

    /**
     * Need to subclass this function to prevent the getNextPage() function being called before the button is clicked.
     */
    @Override
    public boolean canFlipToNextPage() {
        return isPageComplete();
    }

    /**
     * Returns the next page, describes SObject and performs the total size calculation
     *
     * @return IWizardPage
     */

    @Override
    public IWizardPage getNextPage() {

        // if output file already exists, confirm that the user wants to replace it
        if(new File(fileText.getText()).exists()) {
            int button = UIUtils.warningConfMessageBox(getShell(), Labels.getString("UI.fileAlreadyExists"));
            if(button == SWT.NO) {
                return this;
            }
        }

        Config config = controller.getConfig();
        //get entity
        IStructuredSelection selection = (IStructuredSelection)lv.getSelection();
        DescribeGlobalSObjectResult entity = (DescribeGlobalSObjectResult)selection.getFirstElement();
        config.setValue(Config.ENTITY, entity.getName());
        // set DAO - CSV file name
        config.setValue(Config.DAO_NAME, fileText.getText());
        // set DAO type to CSV
        config.setValue(Config.DAO_TYPE, DataAccessObjectFactory.CSV_WRITE_TYPE);
        controller.saveConfig();

        try {
            // create data access object for the extraction output
            controller.createDao();
            // reinitialize the data mapping (UI extraction currently uses only implicit mapping)
            config.setValue(Config.MAPPING_FILE, "");
            controller.createMapper();
        } catch (DataAccessObjectInitializationException e) {
            MessageBox msgBox = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR);
            msgBox.setMessage(Labels.getString("ExtractionDataSelectionPage.extractOutputError")); //$NON-NLS-1$
            msgBox.open();
            return this;
        } catch (MappingInitializationException e) {
            UIUtils.errorMessageBox(getShell(), e);
            return this;
        }

        BusyIndicator.showWhile(Display.getDefault(), new Thread() {
            @Override
            public void run() {
                try {
                    controller.setFieldTypes();
                    controller.setReferenceDescribes();
                    success = true;
                } catch (ConnectionException e) {
                    success = false;
                }
            }
        });

        if (success) {
            //set the query
            ExtractionSOQLPage soql = (ExtractionSOQLPage)getWizard().getPage("SOQL"); //$NON-NLS-1$
            soql.initializeSOQLText();
            soql.setPageComplete(true);

            return super.getNextPage();
        } else {
            MessageBox msgBox = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR);
            msgBox.setMessage(Labels.getString("ExtractionDataSelectionPage.initError")); //$NON-NLS-1$
            msgBox.open();
            return this;
        }
    }
}
