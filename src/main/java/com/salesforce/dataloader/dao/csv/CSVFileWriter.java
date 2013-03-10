/*
 * Copyright (c) 2012, salesforce.com, inc.
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

package com.salesforce.dataloader.dao.csv;

import java.io.*;
import java.util.*;

import com.salesforce.dataloader.model.Row;
import org.apache.log4j.Logger;

import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.dao.DataWriter;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;

/**
 * Writes csv files.
 *
 * @author Lexi Viripaeff
 * @since 6.0
 */
public class CSVFileWriter implements DataWriter {

    //logger
    private static Logger logger = Logger.getLogger(CSVFileWriter.class);

    private final String fileName;
    private BufferedWriter fileOut;
    private List<String> columnNames = new ArrayList<String>();
    private int currentRowNumber = 0;

    /**
     * <code>open</code> is true if the writer file is open, false otherwise.
     */
    private boolean open = false;

    /**
     * <code>encoding</code> contains a value for output character encoding, blank indicates "use default"
     */
    private final String encoding;

    /**
     * If <code>capitalizedHeadings</code> is true, output header row in caps
     */
    private final boolean capitalizedHeadings;

    public CSVFileWriter(String fileName, String encoding) {
        this.fileName = fileName;
        this.capitalizedHeadings = true;
        this.encoding = (encoding == null || encoding.isEmpty()) ? null : encoding;
    }

    /**
     * Check if writing can be performed successfully
     * @throws DataAccessObjectInitializationException
     */
    @Override
    public void checkConnection() throws DataAccessObjectInitializationException {
        open();
        close();
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.dao.DataAccessObject#open()
     */
    @Override
    public void open() throws DataAccessObjectInitializationException {
        try {
            if (this.encoding != null) {
                fileOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.fileName), this.encoding));
            } else {
                fileOut = new BufferedWriter(new FileWriter(this.fileName));
            }
            currentRowNumber = 0;
            setOpen(true);
        } catch (IOException e) {
            String errMsg = Messages.getFormattedString("CSVWriter.errorOpening", this.fileName);
            logger.error(errMsg, e);
            throw new DataAccessObjectInitializationException(errMsg, e);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.dao.csv.Writer#close()
     */
    @Override
    public void close() {

        if (fileOut != null) {
            try {
                fileOut.close();
            } catch (IOException e) {
                logger.error(Messages.getString("CSVWriter.errorClosing"), e); //$NON-NLS-1$
            }
            if (! columnNames.isEmpty()) {
                columnNames.clear();
            }
        }
    }

    private void writeHeaderRow() throws DataAccessObjectInitializationException {
        CSVColumnVisitor visitor = new CSVColumnVisitor(fileOut);
        try {
            visitHeaderColumns(this.columnNames, visitor);
            fileOut.newLine();
            visitor.newRow();
        } catch (IOException e) {
            String errMsg = Messages.getString("CSVWriter.errorWriting");
            logger.error(errMsg, e);
            throw new DataAccessObjectInitializationException(errMsg, e);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.dao.csv.Writer#writeRow(java.util.Map)
     */
    @Override
    public boolean writeRow(Row row) throws DataAccessObjectException {
        CSVColumnVisitor visitor = new CSVColumnVisitor(fileOut);
        try {
            visitColumns(columnNames, row, visitor);
            fileOut.newLine();
            visitor.newRow();
            currentRowNumber++;
            return true; // success unless there's an exception
        } catch (IOException e) {
            logger.error(Messages.getString("CSVWriter.errorWriting"), e); //$NON-NLS-1$
            throw new DataAccessObjectException(Messages.getString("CSVWriter.errorWriting"), e); //$NON-NLS-1$
        }
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.dao.csv.Writer#writeRowList(java.util.List)
     */
    @Override
    public boolean writeRowList(List<Row> rows) throws DataAccessObjectException {
        boolean success = true;
        // return the last result, should be same as others
        for (Row row : rows) {
            success = writeRow(row);
        }
        return success;
    }

    private void visitHeaderColumns(List<String> columnNames, CSVColumnVisitor visitor) throws IOException {
        for (String colName : columnNames) {
            String outColName;
            if(colName != null) {
                if(this.capitalizedHeadings) {
                    outColName = colName.toUpperCase();
                } else {
                    outColName = colName;
                }
            } else {
                outColName = "";
            }
            visitor.visit(outColName);
        }
    }

    static private void visitColumns(List<String> columnNames, Row row, CSVColumnVisitor visitor) throws IOException {
        for (String colName : columnNames) {
            Object colVal = row.get(colName);
            visitor.visit(colVal != null ? colVal.toString() : "");
        }
    }

    @Override
    public List<String> getColumnNames() {
        return columnNames;
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.dao.DataWriter#setColumnNames(java.util.List)
     */
    @Override
    public synchronized void setColumnNames(List<String> columnNames) throws DataAccessObjectInitializationException {
        if(columnNames == null || columnNames.isEmpty()){
            String errMsg = Messages.getString("CSVFileDAO.errorOpenNoHeaderRow");
            logger.error(errMsg);
            throw new DataAccessObjectInitializationException(errMsg);
        }
        // save column names
        this.columnNames = columnNames;

        writeHeaderRow();
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.dao.DataAccessObject#getCurrentRowNumber()
     */
    @Override
    public int getCurrentRowNumber() {
        return currentRowNumber;
    }

}
