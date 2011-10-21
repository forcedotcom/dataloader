/*
 * Copyright (c) 2011, salesforce.com, inc.
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

import java.io.IOException;
import java.util.*;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.csv.CSVFileReader;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import com.salesforce.dataloader.ui.csvviewer.CSVContentProvider;
import com.salesforce.dataloader.ui.csvviewer.CSVLabelProvider;
import com.salesforce.dataloader.util.DAORowUtil;
import com.salesforce.dataloader.util.StreamGobbler;

/**
 * This class creates the mapping dialog
 */
public class CSVViewerDialog extends Dialog {
    private String input;

    private Logger logger = Logger.getLogger(CSVViewerDialog.class);
    private String filename;
    private int numberOfRows;

    //the two tableViewers
    private TableViewer csvTblViewer;

    public void setFileName(String filename) {
        this.filename = filename;
    }

    public void setNumberOfRows(int rows) {
        numberOfRows = rows;
    }

    /**
     * @param parent
     */
    public CSVViewerDialog(Shell parent, Controller controller) {
        // Pass the default styles here
        this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.MAX | SWT.MIN);

    }

    /**
     * @param parent
     *            the parent
     * @param style
     *            the style
     */
    public CSVViewerDialog(Shell parent, int style) {
        // Let users override the default styles
        super(parent, style);

        setText(Labels.getString("CSVViewer.title")); //$NON-NLS-1$
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
     * @throws DataAccessObjectInitializationException
     */
    public String open() throws DataAccessObjectInitializationException {
        logger.info(Labels.getString("CSVViewer.message")); //$NON-NLS-1$

        // Create the dialog window
        Shell shell = new Shell(getParent(), getStyle() | SWT.RESIZE);
        shell.setText(getText());
        shell.setImage(UIUtils.getImageRegistry().get("sfdc_icon")); //$NON-NLS-1$
        shell.setSize(600, 600);
        createContents(shell);
        //shell.pack();
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
     * @throws DataAccessObjectInitializationException
     */
    private void createContents(final Shell shell) throws DataAccessObjectInitializationException {
        shell.setLayout(new GridLayout(1, false));
        GridData data;

        ///////////////////////////////////////////////
        //InitializeSforceViewer
        ///////////////////////////////////////////////
        initializeCSVViewer(shell);

        Label sep3 = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
        sep3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        if (isWindows) {
            Label externalLabel = new Label(shell, SWT.NONE);
            externalLabel.setText(Labels.getString("CSVViewerDialog.externalMsg")); //$NON-NLS-1$
            externalLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        }

        //  buttons
        Composite comp2 = new Composite(shell, SWT.NONE);
        comp2.setLayout(new GridLayout(2, false));
        comp2.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        if (isWindows) {
            Button external = new Button(comp2, SWT.PUSH);
            external.setText(Labels.getString("CSVViewerDialog.externalButton")); //$NON-NLS-1$
            external.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            external.addSelectionListener(new SelectionListener() {
                public void widgetSelected(SelectionEvent e) {
                    Thread runner = new Thread() {
                        @Override
                        public void run() {
                            int exitVal = 0;
                            try {
                                String[] cmd = { "cmd.exe", "/c", "\"" + filename + "\"" }; //$NON-NLS-1$ //$NON-NLS-2$
                                Process proc = Runtime.getRuntime().exec(cmd);
                                StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR"); //$NON-NLS-1$
                                StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT"); //$NON-NLS-1$
                                errorGobbler.start();
                                outputGobbler.start();
                                exitVal = proc.waitFor();
                            } catch (IOException iox) {
                                logger.error(Labels.getString("CSVViewerDialog.errorExternal"), iox); //$NON-NLS-1$
                            } catch (InterruptedException ie) {
                                logger.error(Labels.getString("CSVViewerDialog.errorExternal"), ie); //$NON-NLS-1$
                            }

                            if (exitVal != 0) {
                                logger.error(Labels.getString("CSVViewerDialog.errorProcessExit") + exitVal); //$NON-NLS-1$
                            }

                        }
                    };

                    runner.setPriority(Thread.MAX_PRIORITY);
                    runner.start();

                }

                public void widgetDefaultSelected(SelectionEvent e) {}
            });
        }

        Button ok = new Button(comp2, SWT.PUSH);
        ok.setText(Labels.getString("UI.close")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        data.widthHint = 75;
        ok.setLayoutData(data);
        ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                shell.close();
            }
        });

        // Set the OK button as the default, so
        // user can type input and press Enter
        // to dismiss
        shell.setDefaultButton(ok);
    }

    private void initializeCSVViewer(Shell shell) throws DataAccessObjectInitializationException {
        GridData data;

        CSVFileReader csvReader = new CSVFileReader(filename);

        try {
            csvReader.open();

            List header = csvReader.getColumnNames();

            //sforce field table viewer
            csvTblViewer = new TableViewer(shell, SWT.FULL_SELECTION);
            csvTblViewer.setContentProvider(new CSVContentProvider());
            csvTblViewer.setLabelProvider(new CSVLabelProvider());

            // Set up the sforce table
            Table csvTable = csvTblViewer.getTable();
            data = new GridData(GridData.FILL_BOTH);
            data.heightHint = 300;
            csvTable.setLayoutData(data);

            Iterator iter = header.iterator();

            new TableColumn(csvTable, SWT.LEFT).setText(Labels.getString("CSVViewerDialog.rowNumber")); //$NON-NLS-1$

            while (iter.hasNext()) {
                TableColumn tc = new TableColumn(csvTable, SWT.LEFT);
                tc.setText(iter.next().toString());
            }

            //update the model
            updateCSVTable(csvReader);

            //pack the columns
            packSforceColumns();

            // Turn on the header and the lines
            csvTable.setHeaderVisible(true);
            csvTable.setLinesVisible(true);

            //start scrolled up, dumb swt
            if (csvTable.getItemCount() > 0) {
                csvTable.showItem(csvTable.getItem(0));
            }
        } finally {
            csvReader.close();
        }
    }

    /**
     * Responsible for updating the sforce model
     */
    private void updateCSVTable(CSVFileReader csvReader) {

        List<List<Object>> rowList = new LinkedList<List<Object>>();
        for (int i = 0; i < numberOfRows; i++) {
            Map<String, Object> rowMap;
            try {
                rowMap = csvReader.readRow();
            } catch (DataAccessObjectException e) {
                break;
            }
            if (!DAORowUtil.isValidRow(rowMap)) {
                break;
            }

            List<String> columns = csvReader.getColumnNames();
            List<Object> row = new ArrayList<Object>();
            row.add(0, String.valueOf(i + 1));
            for(String column : columns) {
                row.add(rowMap.get(column));
            }
            rowList.add(row);
        }
        csvReader.close();
        csvTblViewer.setInput(rowList);
    }

    private void packSforceColumns() {
        Table sforceTable = csvTblViewer.getTable();
        //  Pack the columns
        for (int i = 0, n = sforceTable.getColumnCount(); i < n; i++) {
            sforceTable.getColumn(i).pack();
        }
    }

}
