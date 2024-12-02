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

package com.salesforce.dataloader.dao;

import java.util.ArrayList;
import java.util.List;

import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import com.salesforce.dataloader.model.TableHeader;
import com.salesforce.dataloader.model.TableRow;
import com.salesforce.dataloader.util.DAORowUtil;

public abstract class AbstractDataReaderImpl implements DataReader {
    private AppConfig appConfig;
    private DAORowCache rowCache = new DAORowCache();
    private int currentRowNumber;
    private int totalRows = 0;
    private TableHeader tableHeader = null;
    private List<String> daoColsList = new ArrayList<String>();

    public AbstractDataReaderImpl(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public List<TableRow> readTableRowList(int maxRows) throws DataAccessObjectException {
        List<TableRow> rowList = null;
        if (this.rowCache.size() > this.currentRowNumber + maxRows) {
            rowList = this.rowCache.getRows(currentRowNumber, maxRows);
            currentRowNumber = currentRowNumber + rowList.size();
            return rowList;
        } else {
            return readTableRowListFromDAO(maxRows);
        }
    }
    
    public void open() throws DataAccessObjectInitializationException{
        if (isOpenFlag()) {
            close();
        }
        if (!appConfig.getBoolean(AppConfig.PROP_PROCESS_BULK_CACHE_DATA_FROM_DAO)
                || rowCache.size() == 0) {
            openDAO();
            this.daoColsList = initializeDaoColumnsList();
            initializeTableHeader();
        }
        currentRowNumber = 0;
        rowCache.resetCurrentRowIndex();
        setOpenFlag(true);
    }
    
    private void initializeTableHeader() {
        if (this.daoColsList == null) {
            this.daoColsList = new ArrayList<String>();
        }
        ArrayList<String> tableHeaderCols = new ArrayList<>(this.daoColsList);
        if (tableHeaderCols.get(0) == null
            || !tableHeaderCols.get(0).equalsIgnoreCase(AppConfig.ID_COLUMN_NAME)) {
            tableHeaderCols.add(0, AppConfig.ID_COLUMN_NAME);
        }
        if (!tableHeaderCols.contains(AppConfig.STATUS_COLUMN_NAME)) {
            tableHeaderCols.add(AppConfig.STATUS_COLUMN_NAME);
        }
        if (!tableHeaderCols.contains(AppConfig.ERROR_COLUMN_NAME)) {
            tableHeaderCols.add(AppConfig.ERROR_COLUMN_NAME);
        }
        this.tableHeader = new TableHeader(tableHeaderCols);
    }
    
    public TableRow readTableRow() throws DataAccessObjectException {
        if (!isOpenFlag()) {
            open();
        }
        
        // look in the cache first
        TableRow trow = rowCache.getCurrentRow();
        if (trow != null) {
            currentRowNumber++;
            return trow;
        }
        // not found in cache. Try from DAO.
        trow = readTableRowFromDAO();
        if (trow == null) {
            this.totalRows = currentRowNumber;
            return null;
        }
        currentRowNumber++;
        if (appConfig.getBoolean(AppConfig.PROP_PROCESS_BULK_CACHE_DATA_FROM_DAO)) {
            rowCache.addRow(trow);
        }
        return trow;
    }
    
    public int getCurrentRowNumber() {
        return this.currentRowNumber;
    }
    
    public int getTotalRows() throws DataAccessObjectException {
        if (totalRows == 0) {
            if (!isOpenFlag()) {
                open();
            }
            totalRows = DAORowUtil.calculateTotalRows(this);
        }
        return totalRows;
    }
    
    protected AppConfig getAppConfig() {
        return this.appConfig;
    }
    
    protected TableHeader getTableHeader() {
        return this.tableHeader;
    }
    
    @Override
    public List<String> getColumnNames() {
        return new ArrayList<>(this.daoColsList);
    }
    
    abstract protected void setOpenFlag(boolean open);
    abstract protected boolean isOpenFlag();
    abstract protected void openDAO() throws DataAccessObjectInitializationException;
    abstract protected List<TableRow> readTableRowListFromDAO(int maxRows) throws DataAccessObjectException;
    abstract protected TableRow readTableRowFromDAO() throws DataAccessObjectException;
    abstract protected List<String> initializeDaoColumnsList() throws DataAccessObjectInitializationException;
}