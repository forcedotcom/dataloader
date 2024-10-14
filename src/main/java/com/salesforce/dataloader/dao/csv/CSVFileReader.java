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

package com.salesforce.dataloader.dao.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.dao.DAORowCache;
import com.salesforce.dataloader.dao.DataReader;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import com.salesforce.dataloader.exception.DataAccessRowException;
import com.salesforce.dataloader.model.Row;
import com.salesforce.dataloader.util.AppUtil;
import com.salesforce.dataloader.util.DAORowUtil;
import com.sforce.async.CSVReader;

/**
 * Wrapper around {@link CSVReader} that allows to read CSV files
 *
 * @author Federico Recio
 */
public class CSVFileReader implements DataReader {

    private static final Logger LOGGER = LogManager.getLogger(CSVFileReader.class);
    private final Object lock = new Object();
    private File file;
    private FileInputStream input;
    private int totalRows;
    private CSVReader csvReader;
    private int currentRowNumber;
    private List<String> headerRow;
    private boolean isOpen;
    private char[] csvDelimiters;
    private AppConfig appConfig;
    private DAORowCache rowCache = new DAORowCache();
    private boolean endOfFileReached = false;

    // Handles 3 types of CSV files:
    // 1. CSV files provided by the user for upload operations: ignoreDelimiterConfig = false, isQueryOperationResult = false
    // 2. CSV files that are results of query operations: ignoreDelimiterConfig = false, isQueryOperationResult = true
    // 3. CSV files that capture successes/failures when performing an upload operation: ignoreDelimiterConfig = true, isQueryOperationResult = <value ignored>
    //    isQueryOperationsResult value is ignored if ignoreDelimiterConfig is 'true'. 
    public CSVFileReader(File file, AppConfig appConfig, boolean ignoreDelimiterConfig, boolean isQueryOperationResult) {
        this.file = file;
        this.appConfig = appConfig;
        StringBuilder separator = new StringBuilder();
        if (ignoreDelimiterConfig) {
            separator.append(AppUtil.COMMA);
            LOGGER.debug(Messages.getString("CSVFileDAO.debugMessageCommaSeparator"));            
        } else {
            if (isQueryOperationResult) {
                separator.append(appConfig.getString(AppConfig.PROP_CSV_DELIMITER_FOR_QUERY_RESULTS));
            } else { // reading CSV for a load operation
                if (appConfig.getBoolean(AppConfig.PROP_CSV_DELIMITER_COMMA)) {
                    separator.append(AppUtil.COMMA);
                    LOGGER.debug(Messages.getString("CSVFileDAO.debugMessageCommaSeparator"));
                }
                if (appConfig.getBoolean(AppConfig.PROP_CSV_DELIMITER_TAB)) {
                    separator.append(AppUtil.TAB);
                    LOGGER.debug(Messages.getString("CSVFileDAO.debugMessageTabSeparator"));
                }
                if (appConfig.getBoolean(AppConfig.PROP_CSV_DELIMITER_OTHER)) {
                    separator.append(appConfig.getString(AppConfig.PROP_CSV_DELIMITER_OTHER_VALUE));
                    LOGGER.debug(Messages.getFormattedString("CSVFileDAO.debugMessageSeparatorChar", separator));
                }
            }
        }
        csvDelimiters = separator.toString().toCharArray();

        if (csvDelimiters.length == 0) {
            String errorMsg = "No csv separator present! You need at least one separator character!";
            LOGGER.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
    }

    @Override
    public void checkConnection() throws DataAccessObjectInitializationException {
        open();
        close();
    }

    @Override
    public void open() throws DataAccessObjectInitializationException {
        if (isOpen) {
            close();
        }
        currentRowNumber = 0;
        rowCache.resetCurrentRowIndex();

        initalizeInput(csvDelimiters);
        readHeaderRow();
        isOpen = true;
    }

    /**
     * Close the file stream when we are finishe with a load
     */
    @Override
    public void close() {
        try {
            IOUtils.closeQuietly(input);
        } finally {
            input = null;
            csvReader = null;
            isOpen = false;
        }
    }
    
    @Override
    public List<Row> readRowList(int maxRows) throws DataAccessObjectException {
        List<Row> outputRows = new ArrayList<Row>();
        for (int i = 0; i < maxRows; i++) {
            Row outputRow = readRow();
            if (outputRow != null) {
                // if row has been returned, add it to the output
                outputRows.add(outputRow);
            } else {
                // if encountered null, the reading is over
                break;
            }
        }
        return outputRows;
    }

    /**
     * Gets the next row from the current data access object data source. <i>Side effect:</i>
     * Updates the current record number
     */
    @Override
    public Row readRow() throws DataAccessObjectException {
        if (!isOpen) {
            open();
        }
        
        Row row = rowCache.getCurrentRow();
        if (row != null) {
            currentRowNumber++;
            return row;
        }
        
        if (appConfig.getBoolean(AppConfig.PROCESS_BULK_CACHE_DATA_FROM_DAO)
            && endOfFileReached) {
            return null;
        }
        
        List<String> record;
        synchronized (lock) {
            try {
                record = csvReader.nextRecord();
            } catch (IOException e) {
                throw new DataAccessObjectException(e);
            }
        }

        if (!DAORowUtil.isValidRow(record)) {
            endOfFileReached = true;
            return null;
        }

        if (record.size() > headerRow.size()) {
            String errMsg = Messages.getFormattedString("CSVFileDAO.errorRowTooLarge", new String[]{
                    String.valueOf(currentRowNumber), String.valueOf(record.size()), String.valueOf(headerRow.size())});
            throw new DataAccessRowException(errMsg);
        } else if (record.size() < headerRow.size()) {
            String errMsg = Messages.getFormattedString("CSVFileDAO.errorRowTooSmall", new String[]{
                    String.valueOf(currentRowNumber), String.valueOf(record.size()), String.valueOf(headerRow.size())});
            throw new DataAccessRowException(errMsg);
        }

        row = new Row(record.size());

        for (int i = 0; i < headerRow.size(); i++) {
            String value = record.get(i);
            if (value == null) {
                value = "";
            }
            row.put(headerRow.get(i), value);
        }
        currentRowNumber++;
        rowCache.addRow(row);
        return row;
    }

    /**
     * @return Names of output columns being read during each readRow call
     */
    @Override
    public List<String> getColumnNames() {
        return headerRow;
    }

    /*
     * Returns the number of rows in the file. <i>Side effect:</i> Moves the row pointer to the first row
     */
    @Override
    public int getTotalRows() throws DataAccessObjectException {
        if (totalRows == 0) {
            if (!isOpen) {
                open();
            }
            totalRows = DAORowUtil.calculateTotalRows(this);
        }
        return totalRows;
    }

    /**
     * @return Current record number that has been read
     */
    @Override
    public int getCurrentRowNumber() {
        return currentRowNumber;
    }

    private void readHeaderRow() throws DataAccessObjectInitializationException {
        try {
            synchronized (lock) {
                headerRow = csvReader.nextRecord();
            }
            if (headerRow == null) {
                LOGGER.error(Messages.getString("CSVFileDAO.errorHeaderRow"));
                throw new DataAccessObjectInitializationException(Messages.getString("CSVFileDAO.errorHeaderRow"));
            }
            LOGGER.debug(Messages.getFormattedString(
                    "CSVFileDAO.debugMessageHeaderRowSize", headerRow.size()));

            LOGGER.info("Columns in CSV header = " + headerRow.size());
        } catch (IOException e) {
            String errMsg = Messages.getString("CSVFileDAO.errorHeaderRow");
            LOGGER.error(errMsg, e);
            throw new DataAccessObjectInitializationException(errMsg, e);
        } finally {
            // if there's a problem getting header row, the stream needs to be closed
            if (headerRow == null) {
                IOUtils.closeQuietly(input);
            }
        }
    }

    private void initalizeInput(char[] csvDelimiters) throws DataAccessObjectInitializationException {

        try {
            input = new FileInputStream(file);
            String encoding = this.appConfig.getCsvEncoding(false);
            if (StandardCharsets.UTF_8.name().equals(encoding)
                || StandardCharsets.UTF_16BE.name().equals(encoding)
                || StandardCharsets.UTF_16LE.name().equals(encoding)
                || "UTF-32LE".equals(encoding)
                || "UTF-32BE".equals(encoding)) {
                BOMInputStream bomInputStream = 
                        BOMInputStream.builder()
                                        .setFile(file)
                                        .setByteOrderMarks(ByteOrderMark.UTF_8,
                                                            ByteOrderMark.UTF_16LE,
                                                            ByteOrderMark.UTF_16BE,
                                                            ByteOrderMark.UTF_32LE,
                                                            ByteOrderMark.UTF_32BE)
                                        .setInclude(false)
                                        .get();
                csvReader = new CSVReader(bomInputStream, encoding, csvDelimiters);
            } else {
                csvReader = new CSVReader(input, encoding, csvDelimiters);
                LOGGER.debug(this.getClass().getName(), "encoding used to read from CSV file is " + encoding);
            }
            csvReader.setMaxRowsInFile(Integer.MAX_VALUE);
            csvReader.setMaxCharsInFile(Integer.MAX_VALUE);
        } catch (FileNotFoundException e) {
            String errMsg = Messages.getFormattedString("CSVFileDAO.errorOpen", file.getAbsolutePath());
            LOGGER.error(errMsg, e);
            throw new DataAccessObjectInitializationException(errMsg, e);
        } catch (UnsupportedEncodingException e) {
            String errMsg = Messages.getString("CSVFileDAO.errorUnsupportedEncoding");
            LOGGER.error(errMsg, e);
            throw new DataAccessObjectInitializationException(errMsg, e);
        } catch (IOException e) {
            throw new DataAccessObjectInitializationException(e);
        } finally {
            if (csvReader == null) {
                IOUtils.closeQuietly(input);
            }
        }
    }
}
