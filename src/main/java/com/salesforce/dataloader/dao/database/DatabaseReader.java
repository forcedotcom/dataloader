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
package com.salesforce.dataloader.dao.database;

import java.io.File;
import java.sql.*;
import java.util.*;

import com.salesforce.dataloader.model.Row;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.dao.DataReader;
import com.salesforce.dataloader.exception.*;
import com.salesforce.dataloader.util.DAORowUtil;

/**
 * Data Access Object (DAO) that connects to database to update and retrieve the data This is a generic data access
 * class with all of the queries being passed in from the application layer.
 * <p>
 * Most of the SQL calls are derived from the information found in the configuration file.
 *
 * @author Alex Warshavsky
 */
public class DatabaseReader implements DataReader {

    // logger
    private static Logger logger = Logger.getLogger(DatabaseReader.class);

    private final BasicDataSource dataSource;
    private final Config config;
    private List<String> columnNames = new ArrayList<String>();
    private int totalRows = 0;
    private int currentRowNumber = 0;
    private final SqlConfig sqlConfig;
    private final DatabaseContext dbContext;

    /**
     * Get an instance of database reader for the data access object name from configuration
     * @param config
     * @throws DataAccessObjectInitializationException
     */
    public DatabaseReader(Config config) throws DataAccessObjectInitializationException {
        this(config, config.getString(Config.DAO_NAME));
    }

    /**
     * Get an instance of database reader for the given database configuration name
     * @param dbConfigName
     * @throws DataAccessObjectInitializationException
     */
    public DatabaseReader(Config config, String dbConfigName) throws DataAccessObjectInitializationException {
        this.config = config;
        String dbConfigFilename = config.constructConfigFilePath(DatabaseContext.DEFAULT_CONFIG_FILENAME);
        if(! (new File(dbConfigFilename).exists())) {
            throw new DataAccessObjectInitializationException(Messages.getFormattedString("DatabaseDAO.errorConfigFileExists", dbConfigFilename)); //$NON-NLS-1$
        }
        DatabaseConfig dbConfig = DatabaseConfig.getInstance(dbConfigFilename, dbConfigName);
        this.dataSource = dbConfig.getDataSource();
        this.sqlConfig = dbConfig.getSqlConfig();
        this.dbContext = new DatabaseContext(dbConfigName);
        this.columnNames = sqlConfig.getColumnNames();
        if(columnNames == null) {
            columnNames = new ArrayList<String>();
        }
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.dao.DataAccessObject#open()
     */
    @Override
    public void open() throws DataAccessObjectInitializationException {
        open(null);
    }

    /**
     * Open the reader using additional runtime parameters. This is convenient for explicit database access in the code
     * 
     * @param params
     * @throws DataAccessObjectInitializationException
     */
    public void open(Map<String,Object> params) throws DataAccessObjectInitializationException {
        currentRowNumber = 0;
        try {
            setupQuery(params);
        } catch (DataAccessObjectInitializationException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessObjectInitializationException(e.getMessage(), e);
        }
        dbContext.setOpen(true);
    }

    private void setupQuery(Map<String,Object> params) throws DataAccessObjectInitializationException, ParameterLoadException, IllegalArgumentException {
        try {
            dbContext.initConnection(dataSource);
            dbContext.replaceSqlParams(sqlConfig.getSqlString());
            PreparedStatement statement = dbContext.prepareStatement();
            // right now, query doesn't support data input -- all the parameters are static vs. update which takes data
            // for every put call
            dbContext.setSqlParamValues(sqlConfig, config, params);

            // set the query fetch size
            int fetchSize;
            try {
                fetchSize = config.getInt(Config.DAO_READ_BATCH_SIZE);
                if(fetchSize > Config.MAX_DAO_READ_BATCH_SIZE) {
                    fetchSize = Config.MAX_DAO_READ_BATCH_SIZE;
                }
            } catch (ParameterLoadException e) {
                // warn about getting batch size parameter, otherwise continue w/ default
                logger.warn(Messages.getFormattedString("DatabaseDAO.errorGettingBatchSize", new String[] {
                        String.valueOf(Config.DEFAULT_DAO_READ_BATCH_SIZE), e.getMessage() }));
                fetchSize = Config.DEFAULT_DAO_READ_BATCH_SIZE;
            }
            statement.setFetchSize(fetchSize);

            // execute the query and save the result set
            dbContext.setDataResultSet(statement.executeQuery());
        } catch (SQLException sqe) {
            String errMsg = Messages.getFormattedString("DatabaseDAO.sqlExceptionSetup", new String[] {dbContext.getDbConfigName(), sqe.getMessage()});
            logger.error(errMsg, sqe);
            close();
            throw new DataAccessObjectInitializationException(errMsg, sqe);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.dao.DataReader#readRowList(int)
     */
    @Override
    public List<Row> readRowList(int maxRows) throws DataAccessObjectException {
        List<Row> outputRows = new ArrayList<Row>();
        for(int i=0; i < maxRows; i++) {
            Row outputRow = readRow();
            if(outputRow != null) {
                // if row has been returned, add it to the output
                outputRows.add(outputRow);
            } else {
                // if encountered null, the reading is over
                break;
            }
        }
        return outputRows;
    }

    @Override
    public Row readRow() throws DataAccessObjectException {
        Row row = null;

        if (!dbContext.isOpen()) {
            open();
        }

        String currentColumnName = "";
        try {
            ResultSet rs = dbContext.getDataResultSet();
            if (rs != null && rs.next()) {
                row = new Row(columnNames.size());

                for (String columnName : columnNames) {
                    currentColumnName = columnName;
                    Object value = rs.getObject(columnName);
                    row.put(columnName, value);
                }
                currentRowNumber++;
            }
            return row;
        } catch (SQLException sqe) {
            String errMsg = Messages.getFormattedString("DatabaseDAO.sqlExceptionReadRow", new String[] {
                    currentColumnName, String.valueOf(currentRowNumber + 1), dbContext.getDbConfigName(), sqe.getMessage() });
            logger.error(errMsg, sqe);
            close();
            throw new DataAccessObjectException(errMsg, sqe);
        } catch (Exception e) {
            String errMsg = Messages.getFormattedString("DatabaseDAO.exceptionReadRow", new String[] {
                    currentColumnName, String.valueOf(currentRowNumber + 1), dbContext.getDbConfigName(), e.getMessage() });
            logger.error(errMsg, e);
            close();
            throw new DataAccessObjectException(errMsg, e);
        }
    }

    @Override
    public int getTotalRows() throws DataAccessObjectException {
        return 0;
    }

    @Override
    public int getCurrentRowNumber() {
        return currentRowNumber;
    }

    @Override
    public List<String> getColumnNames() {
        return columnNames;
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.dao.DataAccessObject#checkConnection()
     */
    @Override
    public void checkConnection() throws DataAccessObjectInitializationException {
        dbContext.checkConnection(dataSource);
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.dao.DataReader#closeRead()
     */
    @Override
    public void close() {
        dbContext.close();
    }
}
