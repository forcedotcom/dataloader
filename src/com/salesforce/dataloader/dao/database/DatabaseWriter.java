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
package com.salesforce.dataloader.dao.database;

import java.io.File;
import java.sql.*;
import java.util.*;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.dao.DataWriter;
import com.salesforce.dataloader.exception.*;

/**
 * Describe your class here.
 * 
 * @author Alex Warshavsky
 * @since 8.0
 */
public class DatabaseWriter implements DataWriter {

    // logger
    private static Logger logger = Logger.getLogger(DatabaseReader.class);

    private final BasicDataSource dataSource;
    private final Config config;
    private int currentRowNumber = 0;
    private final SqlConfig sqlConfig;
    private final DatabaseContext dbContext;

    public DatabaseWriter(Config config) throws DataAccessObjectInitializationException {
        this(config, config.getString(Config.DAO_NAME));
    }

    /**
     * Create database writer based on configuration
     * 
     * @param config
     * @param dbConfigName
     * @throws DataAccessObjectInitializationException
     */
    DatabaseWriter(Config config, String dbConfigName) throws DataAccessObjectInitializationException {
        this.config = config;
        String dbConfigFilename = config.constructConfigFilePath(DatabaseContext.DEFAULT_CONFIG_FILENAME);
        if (!(new File(dbConfigFilename).exists())) { throw new DataAccessObjectInitializationException(
                Messages.getFormattedString("DatabaseDAO.errorConfigFileExists", dbConfigFilename)); //$NON-NLS-1$
        }
        DatabaseConfig dbConfig = DatabaseConfig.getInstance(dbConfigFilename, dbConfigName);
        dataSource = dbConfig.getDataSource();
        sqlConfig = dbConfig.getSqlConfig();
        dbContext = new DatabaseContext(dbConfigName);
    }

    DatabaseWriter(Config config, String dbConfigName, BasicDataSource dataSource, SqlConfig sqlConfig) {
        this.config = config;
        this.dataSource = dataSource;
        this.sqlConfig = sqlConfig;
        this.dbContext = new DatabaseContext(dbConfigName);
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.dao.DataAccessObject#checkConnection()
     */
    public void checkConnection() throws DataAccessObjectInitializationException {
        dbContext.checkConnection(dataSource);
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.dao.DataWriter#open(java.util.List)
     */
    public void open() throws DataAccessObjectInitializationException {
        setupUpdate(); // setup for writing
        dbContext.setOpen(true);
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.dao.DataAccessObject#close()
     */
    public void close() {
        dbContext.close();
    }

    private void setupUpdate() throws DataAccessObjectInitializationException {
        dbContext.initConnection(dataSource);
        dbContext.replaceSqlParams(sqlConfig.getSqlString());
        dbContext.prepareStatement();
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.dao.DataWriter#writeRowList(java.util.List)
     */
    public boolean writeRowList(List<Map<String, Object>> inputRowList) throws DataAccessObjectException {

        // make sure that the update is setup and ready to go, otherwise stop
        if (!dbContext.isOpen()) { throw new DataAccessObjectInitializationException(Messages
                .getString("DatabaseDAO.errorUpdateNotOpen")); }

        boolean success = true;
        int startingRowNumber = currentRowNumber;

        try {
            //for batchsize = 1, don't do batching, this provides much better error output
            if(inputRowList.size() == 1) {
                dbContext.setSqlParamValues(sqlConfig, config, inputRowList.get(0));
                currentRowNumber++;
            } else {
                // for each row set the Sql params in the prepared statement
                dbContext.getDataStatement().clearBatch();
                for (Map<String, Object> inputRow : inputRowList) {
                    dbContext.setSqlParamValues(sqlConfig, config, inputRow);
                    dbContext.getDataStatement().addBatch();
                    currentRowNumber++;
                }
            }
        } catch (ParameterLoadException e) {
            throw new DataAccessObjectException(e.getMessage(), e);
        } catch (SQLException sqe) {
            String errMsg = Messages.getFormattedString("DatabaseDAO.sqlExceptionPrepareRow", new String[] {
                    String.valueOf(currentRowNumber + 1), String.valueOf(startingRowNumber + 1),
                    String.valueOf(startingRowNumber + inputRowList.size() + 1), dbContext.getDbConfigName(),
                    sqe.getMessage() });
            logger.error(errMsg, sqe);
            // batch failed: set current row number to the end of the batch
            currentRowNumber = startingRowNumber + inputRowList.size();
            throw new DataAccessObjectException(errMsg, sqe);
        } catch (Exception e) {
            String errMsg = Messages.getFormattedString("DatabaseDAO.exceptionPrepareRow", new String[] {
                    String.valueOf(currentRowNumber + 1), String.valueOf(startingRowNumber + 1),
                    String.valueOf(startingRowNumber + inputRowList.size() + 1), dbContext.getDbConfigName(),
                    e.getMessage() });
            logger.error(errMsg, e);
            // batch failed: set current row number to the end of the batch
            currentRowNumber = startingRowNumber + inputRowList.size();
            throw new DataAccessObjectException(errMsg, e);
        }

        try {
            //for batchsize = 1, don't do batching, this provides much better error output
            int totalSuccessRows = 0;
            if(inputRowList.size() == 1) {
                // non-batch update returns exception, so it's always success unless exception is returned
                dbContext.getDataStatement().executeUpdate();
                success = true;
                totalSuccessRows = 1;
            } else {
                // execute the update SQL in batch
                int[] rowsUpdatedArray = dbContext.getDataStatement().executeBatch();
                for (int rowsUpdated : rowsUpdatedArray) {
                    if (rowsUpdated == PreparedStatement.SUCCESS_NO_INFO) {
                        totalSuccessRows = rowsUpdatedArray.length;
                        success = true;
                        break;
                    }
                }
            }
            logger.debug(Messages.getFormattedString("DatabaseDAO.updatedStatus", new String[] {
                    String.valueOf(totalSuccessRows), String.valueOf(currentRowNumber) }));

            // commit the change
            dbContext.getDataConnection().commit();

        } catch (SQLException sqe) {
            if (sqe instanceof BatchUpdateException) {
                int[] updateCountArray = ((BatchUpdateException)sqe).getUpdateCounts();
                for (int i = 0; i < updateCountArray.length; i++) {
                    if (updateCountArray[i] == PreparedStatement.EXECUTE_FAILED) {
                        // FIXME all results are the same, return
                        success = false;
                        break;
                    }
                }
            }
            String errMsg = Messages.getFormattedString("DatabaseDAO.sqlExceptionWriteRow", new String[] {
                    String.valueOf(currentRowNumber + 1 - inputRowList.size()), String.valueOf(currentRowNumber + 1),
                    dbContext.getDbConfigName(), sqe.getMessage() });
            logger.error(errMsg, sqe);

            endException(inputRowList.size());
            throw new DataAccessObjectException(errMsg, sqe);
        } catch (Exception e) {
            String errMsg = Messages.getFormattedString("DatabaseDAO.exceptionWriteRow", new String[] {
                    String.valueOf(currentRowNumber + 1 - inputRowList.size()), String.valueOf(currentRowNumber + 1),
                    dbContext.getDbConfigName(), e.getMessage() });
            logger.error(errMsg, e);

            endException(inputRowList.size());
            throw new DataAccessObjectException(errMsg, e);
        }

        return success;
    }

    /**
     * @param inputRow
     * @return Any output values resulting from the write
     * @throws DataAccessObjectException
     */
    public boolean writeRow(Map<String, Object> inputRow) throws DataAccessObjectException {
        // FIXME: Think about refactoring this for the caller to writeRow() and here take care of batching internally
        List<Map<String, Object>> inputRowList = new ArrayList<Map<String, Object>>();
        inputRowList.add(inputRow);
        return writeRowList(inputRowList);
    }

    /**
     * @param sqe
     */
    private void endException(int batchSize) {
        // Rollback if dbContext.getAutoCommit() is false
        try {
            dbContext.getDataConnection().rollback();
        } catch (SQLException sqe) {
            logger.error(Messages.getFormattedString("DatabaseDAO.sqlExceptionRollback", new String[] {
                    String.valueOf(currentRowNumber + 1 - batchSize),
                    String.valueOf(currentRowNumber + 1), dbContext.getDbConfigName(), sqe.getMessage() }), sqe);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.dao.DataAccessObject#getColumnNames()
     */
    public List<String> getColumnNames() {
        // since columnnames are not known, return blank list
        return new ArrayList<String>();
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.dao.DataAccessObject#getCurrentRowNumber()
     */
    public int getCurrentRowNumber() {
        return currentRowNumber;
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.dao.DataWriter#setColumnNames(java.util.List)
     */
    public void setColumnNames(List<String> columnNames) {
        // TODO: Ordered column names can possibly used for ordered output from the write. Currently, this is not used
        // since writeRow will contain column information anyway and order doesn't matter in database
    }
}
