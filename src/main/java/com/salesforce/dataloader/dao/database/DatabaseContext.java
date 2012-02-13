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

import java.sql.*;
import java.util.*;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.dyna.DateConverter;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import com.salesforce.dataloader.exception.ParameterLoadException;

/**
 * Describe your class here.
 * 
 * @author Alex Warshavsky
 * @since 8.0
 */
public class DatabaseContext {

    private final String dbConfigName;
    private ResultSet dataResultSet;
    private Connection dataConnection;
    private PreparedStatement dataStatement;
    private List<String> currentSqlParamNames;
    private String currentSqlString;
    private boolean open = false;
    public static final String DEFAULT_CONFIG_FILENAME = "database-conf.xml";

    // logger
    private static Logger logger = Logger.getLogger(DatabaseContext.class);

    public DatabaseContext(String dbConfigName) {
        this.dbConfigName = dbConfigName;
    }

    /**
     * Check whether connection to database is usable
     * 
     * @throws DataAccessObjectInitializationException
     */
    public void checkConnection(DataSource dataSource) throws DataAccessObjectInitializationException {
        initConnection(dataSource);
        closeConnection();
    }

    public void initConnection(DataSource dataSource) throws DataAccessObjectInitializationException {
        try {
            // open connection only if necessary
            if (dataConnection == null || dataConnection.isClosed()) {
                dataConnection = dataSource.getConnection();
                // control the transactions instead of using autoCommit
                dataConnection.setAutoCommit(false);
            }
        } catch (SQLException sqe) {
            String errMsg = Messages.getFormattedString("DatabaseDAO.sqlExceptionConnect", new String[] { dbConfigName,
                    sqe.getMessage() });
            logger.error(errMsg, sqe);
            throw new DataAccessObjectInitializationException(errMsg, sqe);
        }
    }

    public void closeConnection() throws DataAccessObjectInitializationException {
        try {
            // close connection only if necessary
            if (dataConnection != null && !dataConnection.isClosed()) {
                dataConnection.close();
            }
        } catch (SQLException sqe) {
            String errMsg = Messages.getFormattedString("DatabaseDAO.sqlExceptionConnClose", new String[] {
                    dbConfigName, sqe.getMessage() });
            logger.info(errMsg, sqe);
        }
    }

    /**
     * This method replaces substitution params in given Sql string with '?' for the preparestatement and save a list of
     * those params for later retrieval during the value binding
     * 
     * @param sqlString
     *            Sql string
     */
    public void replaceSqlParams(String sqlString) {
        List<String> paramNames = new ArrayList<String>();

        boolean moreParams = false;
        int start = sqlString.indexOf("@");
        if (start > 0) moreParams = true;

        // Find all the params and add them to the List to be replaced. All params have format "@paramname@"
        while (moreParams) {
            int end = sqlString.indexOf("@", start + 1);
            if (end < 0) break;

            String paramName = sqlString.substring(start + 1, end);
            paramNames.add(paramName);

            start = sqlString.indexOf("@", end + 1);
            if (start < 0) moreParams = false;
        }

        // Replace set the params of form "@param@" with the '?' char and save for prepare statement
        String sqlStringReplace = new String(sqlString);
        for (String param : paramNames) {
            sqlStringReplace = sqlStringReplace.replaceFirst("@" + param + "@", "?");
        }

        // save the output
        currentSqlString = sqlStringReplace;
        currentSqlParamNames = paramNames;
    }

    /**
     * Private helper method to replace the params in the query with the values passed in from the Map. It sets the
     * parameters with the param values.
     * 
     * @param paramValues
     *            Values for the parameter replacement
     * @throws ParameterLoadException
     */
    public void setSqlParamValues(SqlConfig sqlConfig, Config config, Map<String, Object> paramValues)
            throws ParameterLoadException {
        // detect if there're no parameters to set
        if (sqlConfig.getSqlParams() == null) { return; }

        if (paramValues == null) {
            paramValues = new HashMap<String, Object>();
        }

        for (String paramName : sqlConfig.getSqlParams().keySet()) {
            String type = sqlConfig.getSqlParams().get(paramName);
            if (paramValues.containsKey(paramName)) {
                Object sqlValue = mapParamToDbType(config, paramValues.get(paramName), type);
                paramValues.put(paramName, sqlValue);
            } else {
                // look in the config if the parameter value is not passed in
                if (config.contains(paramName)) {
                    Object configValue = getConfigValue(config, paramName, type);
                    Object sqlValue = mapParamToDbType(config, configValue, type);
                    logger.info(Messages.getFormattedString("DatabaseDAO.sqlParamInfo", new String[] { paramName,
                            sqlValue.toString() }));
                    paramValues.put(paramName, sqlValue);
                } else {
                    String errMsg = Messages.getFormattedString("DatabaseDAO.errorParamMissing", new String[] {
                            paramName, dbConfigName });
                    logger.fatal(errMsg);
                    throw new ParameterLoadException(errMsg);
                }
            }
        }

        // Set the parameters for all the replaced params
        // Set the params in the Map
        int index = 1;
        for (String paramName : currentSqlParamNames) {
            Object sqlValue = paramValues.get(paramName);
            try {
                if (sqlValue != null)
                    dataStatement.setObject(index, sqlValue);
                else {
                    dataStatement.setNull(index, getSqlType(sqlConfig.getSqlParams().get(paramName)));
                }
            } catch (SQLException sqe) {
                String valueString = String.valueOf(sqlValue);
                String valueClass = String.valueOf(sqlValue == null ? null : sqlValue.getClass());
                String errMsg = Messages.getFormattedString("DatabaseDAO.sqlExceptionParamReplace", new String[] {
                        this.dbConfigName, paramName, valueString, valueClass, sqe.getMessage() });
                logger.error(errMsg, sqe);
                throw new ParameterLoadException(errMsg, sqe);
            }
            index++;
        }

    }

    private int getSqlType(String type) {
        try {
            final Class<?> cls = Class.forName(type);
            if (java.sql.Timestamp.class.isAssignableFrom(cls)) {
                return Types.TIMESTAMP;
            } else if (java.sql.Time.class.isAssignableFrom(cls)) {
                return Types.TIME;
            } else if (java.sql.Date.class.isAssignableFrom(cls)) {
                return Types.DATE;
            } else if (int.class.equals(cls) || long.class.equals(cls) || float.class.equals(cls)
                    || double.class.equals(cls) || Number.class.isAssignableFrom(cls)) {
                return Types.NUMERIC;
            } else if (String.class.isAssignableFrom(cls)) { return Types.VARCHAR; }
        } catch (ClassNotFoundException e) {}
        throw new UnsupportedOperationException("Type not supported: " + type);
    }

    private Object getConfigValue(Config config, String paramName, String type) throws ParameterLoadException {
        Object value;
        try {
            if (type.equals(java.sql.Date.class.getName()) || type.equals(Timestamp.class.getName())
                    || type.equals(Time.class.getName())) {
                value = config.getDate(paramName);
            } else if (type.equals(boolean.class.getName())) {
                value = config.getBoolean(paramName);
            } else if (type.equals(int.class.getName())) {
                value = config.getInt(paramName);
            } else if (type.equals(long.class.getName())) {
                value = config.getLong(paramName);
            } else if (type.equals(float.class.getName())) {
                value = config.getFloat(paramName);
            } else if (type.equals(double.class.getName())) {
                value = config.getDouble(paramName);
            } else {
                value = config.getString(paramName);
            }
        } catch (ParameterLoadException e) {
            String errMsg = Messages.getFormattedString("DatabaseDAO.errorSqlParamReplace", new String[] { paramName,
                    type, dbConfigName });
            logger.error(errMsg, e);
            throw new ParameterLoadException(errMsg, e);
        }
        return value;
    }

    /**
     * Map Sql replacement parameters from config file values to an object usable as a replacement in a Sql statement
     * 
     * @param config
     * @param paramName
     * @param type
     * @return An object of type usable as a replacement in a Sql statement
     * @throws ParameterLoadException
     */
    private Object mapParamToDbType(Config cfg, Object paramValue, String type) throws ParameterLoadException {
        Object sqlValue;
        if(paramValue == null) {
            return paramValue;
        }
        try {
            if (type.equals(java.sql.Date.class.getName())) {
                sqlValue = new java.sql.Date(getTimeInMillis(cfg.getTimeZone(), paramValue));
            } else if (type.equals(Timestamp.class.getName())) {
                sqlValue = new Timestamp(getTimeInMillis(cfg.getTimeZone(), paramValue));
            } else if (type.equals(Time.class.getName())) {
                sqlValue = new Time(getTimeInMillis(cfg.getTimeZone(), paramValue));
            } else {
                sqlValue = paramValue;
            }
            return sqlValue;
        } catch(Exception e) {
            String errMsg = Messages.getFormattedString("DatabaseDAO.errorParamMapping", new String[] {
                    paramValue.toString(), paramValue.getClass().getName(), type, e.getMessage() });
            logger.error(errMsg, e);
            throw new ParameterLoadException(errMsg, e);
        }
    }

    private long getTimeInMillis(TimeZone tz, Object paramValue) {
        if(paramValue instanceof Calendar) {
            return ((Calendar)paramValue).getTimeInMillis();
        }
        if(paramValue instanceof Date) {
            return ((Date)paramValue).getTime();
        }
        else if(paramValue instanceof String) {
            Calendar cal = (Calendar)new DateConverter(tz).convert(java.util.Calendar.class, paramValue);
            return cal.getTimeInMillis();
        } else {
            throw new IllegalArgumentException(Messages.getFormattedString("DatabaseDAO.errorParamMappingType", paramValue.getClass().getName()));
        }
    }

    /**
     * Create the PreparedStatement
     * 
     * @return prepared statement
     * @throws DataAccessObjectInitializationException
     */
    public PreparedStatement prepareStatement() throws DataAccessObjectInitializationException {
        try {
            dataStatement = dataConnection.prepareStatement(currentSqlString);
            return dataStatement;
        } catch (SQLException sqe) {
            String errMsg = Messages.getFormattedString("DatabaseDAO.sqlExceptionPrepareStatement", new String[] {
                    dbConfigName, sqe.getMessage() });
            logger.error(errMsg, sqe);
            throw new DataAccessObjectInitializationException(errMsg, sqe);
        }
    }

    public void close() {
        try {
            if (dataResultSet != null) {
                dataResultSet.close();
            }
            if (dataStatement != null) {
                dataStatement.close();
            }
            if (dataConnection != null && !dataConnection.isClosed()) {
                dataConnection.close();
            }
        } catch (SQLException sqe) {
            String errMsg = Messages.getFormattedString("DatabaseDAO.sqlExceptionConnClose", new String[] {
                    dbConfigName, sqe.getMessage() });
            logger.info(errMsg, sqe);
        } // Ignore
        setOpen(false);
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public ResultSet getDataResultSet() {
        return dataResultSet;
    }

    public void setDataResultSet(ResultSet dataResultSet) {
        this.dataResultSet = dataResultSet;
    }

    public PreparedStatement getDataStatement() {
        return dataStatement;
    }

    public void setDataStatement(PreparedStatement dataStatement) {
        this.dataStatement = dataStatement;
    }

    public List<String> getCurrentSqlParamNames() {
        return currentSqlParamNames;
    }

    public void setCurrentSqlParamNames(List<String> currentSqlParamNames) {
        this.currentSqlParamNames = currentSqlParamNames;
    }

    public String getCurrentSqlString() {
        return currentSqlString;
    }

    public void setCurrentSqlString(String currentSqlString) {
        this.currentSqlString = currentSqlString;
    }

    public Connection getDataConnection() {
        return dataConnection;
    }

    public String getDbConfigName() {
        return dbConfigName;
    }

}
