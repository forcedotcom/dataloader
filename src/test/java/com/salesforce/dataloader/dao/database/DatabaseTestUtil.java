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

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;

/**
 * Utilities for database connectivity testing
 *
 * @author Alex Warshavsky
 * @since 8.0
 */
public class DatabaseTestUtil {
    // logger
    private static Logger logger = Logger.getLogger(DatabaseTestUtil.class);

    public enum DateType {CALENDAR, DATE, STRING, VALIDATION, NULL};

    public static void insertOrUpdateAccountsDb(Controller theController, boolean isInsert, int numAccounts,
            boolean insertNulls) {
        insertOrUpdateAccountsDb(theController, isInsert, numAccounts, DateType.CALENDAR, insertNulls,
                "java.sql.Timestamp");
    }

    /**
     * @param theController
     * @param isInsert
     * @param numAccounts
     */
    public static void insertOrUpdateAccountsDb(Controller theController, boolean isInsert, int numAccounts,
            DateType dateType, boolean insertNulls, String sqlDateClass) {
        DatabaseWriter writer = null;
        String dbConfigName = isInsert ? "insertAccount" : "updateAccount";
        logger.info("Preparing to write " + numAccounts + " accounts to the database using db config: " + dbConfigName);
        try {
            DatabaseConfig dbConfig = getDatabaseConfig(theController, dbConfigName);
            BasicDataSource dataSource = dbConfig.getDataSource();
            SqlConfig sqlConfig = dbConfig.getSqlConfig();
            // override the configured sqltype for the last_update column
            sqlConfig.getSqlParams().put(LAST_UPDATED_COL, sqlDateClass);
            writer = new DatabaseWriter(theController.getConfig(), dbConfigName, dataSource, sqlConfig);
            writer.open();
            List<Map<String, Object>> accountRowList = new ArrayList<Map<String, Object>>();
            int rowsProcessed = 0;
            for(int i=0; i < numAccounts; i++) {
                Map<String, Object> accountRow = getInsertOrUpdateAccountRow(isInsert, i, dateType, insertNulls);
                accountRowList.add(accountRow);
                if(accountRowList.size() >= 1000 || i == (numAccounts-1)) {
                    rowsProcessed += accountRowList.size();
                    writer.writeRowList(accountRowList);
                    logger.info("Written " + rowsProcessed + " of " + numAccounts + " total accounts using database config: " + dbConfigName);
                    accountRowList = new ArrayList<Map<String,Object>>();
                }
            }
        } catch (DataAccessObjectInitializationException e) {
            TestCase.fail("Error initializing database writer for db config: " + dbConfigName + ", error: " + e.toString());
        } catch (DataAccessObjectException e) {
            String dbOperName = isInsert ? "inserting" : "updating";
            TestCase.fail("error " + dbOperName + " accounts to the database using db config: " + dbConfigName + ", error: " + e.toString());
        } finally {
            if(writer != null) writer.close();
        }
    }

    public static Map<String, Object> getInsertOrUpdateAccountRow(boolean isInsert, int seqNum, DateType dateType) {
        return getInsertOrUpdateAccountRow(isInsert, seqNum, dateType, false);
    }
    
    public static DatabaseConfig getDatabaseConfig(Controller controller, String dbConfigName) {
        String dbConfigFilename = controller.getConfig().constructConfigFilePath(
                DatabaseContext.DEFAULT_CONFIG_FILENAME);
        return DatabaseConfig.getInstance(dbConfigFilename, dbConfigName);
    }

    /**
     * Generate data for one account row based on the seqNum passed in. If insert is desired, text data is based on
     * seqNum, if update, text data is based on 9999-seqNum
     * 
     * @param isInsert
     *            if true, account is for insert, otherwise - for update
     * @param seqNum
     *            Account sequence in set of generated accounts
     * @param dateType Type for the date field values
     * @return Map<String,Object> containing account data based on seqNum
     */
    public static Map<String, Object> getInsertOrUpdateAccountRow(boolean isInsert, int seqNum, DateType dateType,
            boolean insertNulls) {
        Map<String,Object> row = new HashMap<String,Object>();
        String operation;
        int seqInt;
        // external id is the key, use normal sequencing for update so the same set of records gets updated as inserted
        row.put(EXT_ID_COL, "1-" + String.format("%06d", seqNum));
        if(isInsert) {
            // for insert use "forward" sequence number for data
            seqInt = seqNum;
            operation = "insert";
        } else {
            // for update use "reverse" sequence number for data
            seqInt = 999999 - seqNum;
            operation = "update";
        }
        String seqStr = String.format("%06d", seqInt);
        row.put(NAME_COL, "account " + operation + "#" + seqStr); // this is important to get the correct sort order
        row.put(SFDC_ID_COL, "001account_" + seqStr);
        row.put(ACCOUNT_NUMBER_COL, "ACCT" + seqStr);
        if (insertNulls) {
            row.put(PHONE_COL, null);
            row.put(REVENUE_COL, null);
        } else {
            row.put(PHONE_COL, "415-555-" + seqStr);
            row.put(REVENUE_COL, BigDecimal.valueOf(seqInt * 1000));
        }
        Object dateValue;
        Calendar cal = Calendar.getInstance();
        switch(dateType) {
        case STRING:
            DateFormat formatter = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss'Z'Z");
            formatter.setCalendar(cal);
            dateValue = formatter.format(cal.getTime());
            break;
        case DATE:
            dateValue = cal.getTime();
            break;
        case NULL:
            dateValue = null;
            break;
        case VALIDATION:
            dateValue = new java.sql.Date(cal.getTimeInMillis());
            break;
        case CALENDAR:
        default:
            dateValue = cal;
            break;
        }
        row.put(LAST_UPDATED_COL, dateValue);
        return row;
    }

    /**
     * Delete all accounts from account table. Useful as a cleanup step
     */
    public static void deleteAllAccountsDb(Controller theController) {
        DatabaseWriter writer = null;
        try {
            writer = new DatabaseWriter(theController.getConfig(), "deleteAccountAll");
            writer.open();
            logger.info("Deleting all Accounts from database, using configuration: " + "deleteAccountAll");
            writer.writeRow(null);
        } catch (DataAccessObjectInitializationException e) {
            TestCase.fail("Error initializing database writer for db config: " + "deleteAccountAll");
        } catch (DataAccessObjectException e) {
            TestCase.fail("error deleting accounts from the database using db config: " + "deleteAccountAll");
        } finally {
            if(writer != null) writer.close();
        }
    }

    public static final String NAME_COL = "account_name";
    public static final String PHONE_COL = "business_phone";
    public static final String EXT_ID_COL = "account_ext_id";
    public static final String SFDC_ID_COL = "sfdc_account_id";
    public static final String REVENUE_COL = "annual_revenue";
    public static final String LAST_UPDATED_COL = "last_updated";
    public static final String ACCOUNT_NUMBER_COL = "account_number";
    
    public static final Map<String, String> ALL_COLS = new HashMap<String, String>() {{
        put(NAME_COL, "varchar(100)");
        put(PHONE_COL, "varchar(100)");
        put(EXT_ID_COL, "varchar(100)");
        put(SFDC_ID_COL, "varchar(100)");
        put(REVENUE_COL, "decimal");
        put(LAST_UPDATED_COL, "date");
        put(ACCOUNT_NUMBER_COL, "varchar(100)");
        put("system_modstamp", "date default sysdate not null");
    }};
    
    public static void createTable(Controller controller, String tableName) {
        DataSource dataSource = DatabaseTestUtil.getDatabaseConfig(controller, "insertAccount").getDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        SqlRowSet tables = jdbcTemplate.queryForRowSet("show tables");
        boolean tableExists = false;
        while (tables.next()) {
            if (tables.getString("TABLE_NAME").equals(tableName.toUpperCase())) {
                tableExists = true;
                break;
            }
        }
        
        // create table if it doesn't exist
        if (!tableExists) {
            String createTableSql = "create table "+ tableName + " (";
            List<String> keys = new ArrayList<String>(ALL_COLS.keySet());
            for (int i = 0; i < keys.size(); i++ ) {
                createTableSql += keys.get(i) + " " + ALL_COLS.get(keys.get(i));
                if (i == keys.size() - 1) {
                    createTableSql += ")";
                } else {
                    createTableSql += ", ";
                }
            }
            jdbcTemplate.execute(createTableSql);
        }
    }

}
