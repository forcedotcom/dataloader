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
package com.salesforce.dataloader.dao.database;

import java.io.File;
import java.sql.*;
import java.util.*;

import com.salesforce.dataloader.model.TableRow;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.Logger;
import com.salesforce.dataloader.util.DLLogManager;

import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.dao.AbstractDataReaderImpl;
import com.salesforce.dataloader.exception.*;

/**
 * Data Access Object (DAO) that connects to database to update and retrieve the data This is a generic data access
 * class with all of the queries being passed in from the application layer.
 * <p>
 * Most of the SQL calls are derived from the information found in the configuration file.
 *
 * @author Alex Warshavsky
 */
public class DatabaseReader extends AbstractDataReaderImpl {

    // logger
    private static Logger logger = DLLogManager.getLogger(DatabaseReader.class);

    private final BasicDataSource dataSource;
    private final SqlConfig sqlConfig;
    private final DatabaseContext dbContext;
    private boolean endOfTableReached = false;

    /**
     * Get an instance of database reader for the data access object name from configuration
     * @param appConfig
     * @throws DataAccessObjectInitializationException
     */
    public DatabaseReader(AppConfig appConfig) throws DataAccessObjectInitializationException {
        this(appConfig, appConfig.getString(AppConfig.PROP_DAO_NAME));
    }

    /**
     * Get an instance of database reader for the given database configuration name
     * @param dbConfigName
     * @throws DataAccessObjectInitializationException
     */
    public DatabaseReader(AppConfig appConfig, String dbConfigName) throws DataAccessObjectInitializationException {
        super(appConfig);
        String dbConfigFilename = appConfig.constructConfigFilePath(DatabaseContext.DEFAULT_CONFIG_FILENAME);
        if(! (new File(dbConfigFilename).exists())) {
            throw new DataAccessObjectInitializationException(Messages.getFormattedString("DatabaseDAO.errorConfigFileExists", dbConfigFilename)); //$NON-NLS-1$
        }
        DatabaseConfig dbConfig = DatabaseConfig.getInstance(dbConfigFilename, dbConfigName);
        this.dataSource = dbConfig.getDataSource();
        this.sqlConfig = dbConfig.getSqlConfig();
        this.dbContext = new DatabaseContext(dbConfigName);
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.dao.DataAccessObject#open()
     */
    @Override
    protected void openDAO() throws DataAccessObjectInitializationException {
        open(null);
    }

    /**
     * Open the reader using additional runtime parameters. This is convenient for explicit database access in the code
     * 
     * @param params
     * @throws DataAccessObjectInitializationException
     */
    private void open(Map<String,Object> params) throws DataAccessObjectInitializationException {
        try {
            setupQuery(params);
        } catch (DataAccessObjectInitializationException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessObjectInitializationException(e.getMessage(), e);
        }
    }
    
    protected void setOpenFlag(boolean open) {
        dbContext.setOpen(open);
    }
    
    protected boolean isOpenFlag() {
        return dbContext.isOpen();
    }

    private void setupQuery(Map<String,Object> params) throws DataAccessObjectInitializationException, ParameterLoadException, IllegalArgumentException {
        try {
            dbContext.initConnection(dataSource);
            dbContext.replaceSqlParams(sqlConfig.getSqlString());
            PreparedStatement statement = dbContext.prepareStatement();
            // right now, query doesn't support data input -- all the parameters are static vs. update which takes data
            // for every put call
            dbContext.setSqlParamValues(sqlConfig, 
                    this.getAppConfig(), params);

            // set the query fetch size
            int fetchSize;
            try {
                fetchSize = this.getAppConfig().getInt(AppConfig.PROP_DAO_READ_BATCH_SIZE);
                if(fetchSize > AppConfig.MAX_DAO_READ_BATCH_SIZE) {
                    fetchSize = AppConfig.MAX_DAO_READ_BATCH_SIZE;
                }
            } catch (ParameterLoadException e) {
                // warn about getting batch size parameter, otherwise continue w/ default
                logger.warn(Messages.getFormattedString("DatabaseDAO.errorGettingBatchSize", new String[] {
                        String.valueOf(AppConfig.DEFAULT_DAO_READ_BATCH_SIZE), e.getMessage() }));
                fetchSize = AppConfig.DEFAULT_DAO_READ_BATCH_SIZE;
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
     * @see com.salesforce.dataloader.dao.DataReader#readTableRowList(int)
     */
    @Override
    protected List<TableRow> readTableRowListFromDAO(int maxRows) throws DataAccessObjectException {
        List<TableRow> outputRows = new ArrayList<TableRow>();
        for(int i=0; i < maxRows; i++) {
            TableRow outputRow = readTableRow();
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
    protected TableRow readTableRowFromDAO() throws DataAccessObjectException {
        if (endOfTableReached) {
            return null;
        }
        String currentColumnName = "";
        try {
            TableRow trow = null;
            ResultSet rs = dbContext.getDataResultSet();
            if (rs != null && rs.next()) {
                trow = new TableRow(getTableHeader());

                for (String columnName : getColumnNames()) {
                    currentColumnName = columnName;
                    Object value = rs.getObject(columnName);
                    trow.put(columnName, value);
                }
            }
            if (trow == null) {
                endOfTableReached = true;
                return null;
            }
            return trow;
        } catch (SQLException sqe) {
            String errMsg = Messages.getFormattedString("DatabaseDAO.sqlExceptionReadRow", new String[] {
                    currentColumnName, String.valueOf(getCurrentRowNumber() + 1), dbContext.getDbConfigName(), sqe.getMessage() });
            logger.error(errMsg, sqe);
            close();
            throw new DataAccessObjectException(errMsg, sqe);
        } catch (Exception e) {
            String errMsg = Messages.getFormattedString("DatabaseDAO.exceptionReadRow", new String[] {
                    currentColumnName, String.valueOf(getCurrentRowNumber() + 1), dbContext.getDbConfigName(), e.getMessage() });
            logger.error(errMsg, e);
            close();
            throw new DataAccessObjectException(errMsg, e);
        }
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
    
    public int getTotalRows() throws DataAccessObjectException {
        if (this.getAppConfig().getBoolean(AppConfig.PROP_DAO_SKIP_TOTAL_COUNT)) {
            return 0;
        }
        return super.getTotalRows();
    }

    @Override
    protected List<String> initializeDaoColumnsList() {
        List<String> daoColsList = sqlConfig.getColumnNames();
        if(daoColsList == null) {
            daoColsList = new ArrayList<String>();
        }
       return daoColsList;
    }
}
