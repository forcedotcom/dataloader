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

import java.util.*;

import com.salesforce.dataloader.model.Row;
import org.apache.log4j.Logger;

import com.salesforce.dataloader.TestBase;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.database.DatabaseTestUtil.DateType;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import com.salesforce.dataloader.util.AccountRowComparator;

/**
 * Unit test for database operations
 *
 * @author Alex Warshavsky
 * @since 8.0
 */
public class DatabaseTest extends TestBase {

    public DatabaseTest(String name) {
        super(name);
    }

    // logger
    private static Logger logger = Logger.getLogger(DatabaseReader.class);
    
    private static final String[] VALIDATE_COLS = { DatabaseTestUtil.EXT_ID_COL, DatabaseTestUtil.SFDC_ID_COL,
        DatabaseTestUtil.NAME_COL, DatabaseTestUtil.PHONE_COL, DatabaseTestUtil.REVENUE_COL,
        DatabaseTestUtil.ACCOUNT_NUMBER_COL };
    private static final int NUM_ROWS = 10;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        DatabaseTestUtil.createTable(getController(), "dataloader");
        
        // delete accounts from database to start fresh
        DatabaseTestUtil.deleteAllAccountsDb(getController());
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        // delete accounts from database to finish with no leftovers
        DatabaseTestUtil.deleteAllAccountsDb(getController());
    }

    public void testDatabaseInsertQuery() {
        // insert some data
        DatabaseTestUtil.insertOrUpdateAccountsDb(getController(), true/* insert */, NUM_ROWS, false);

        // query and verify the results
        verifyDbInsertOrUpdate(getController(), true, true);
    }

    public void testDatabaseUpdateQuery() {
        // insert some data
        DatabaseTestUtil.insertOrUpdateAccountsDb(getController(), true/* insert */, NUM_ROWS, false);

        // update some data
        DatabaseTestUtil.insertOrUpdateAccountsDb(getController(), false/* update */, NUM_ROWS, false);

        // verify update
        verifyDbInsertOrUpdate(getController(), false, true);
    }

    public void testDatabaseDateMappingDate() {
        doTestDatabaseDateMapping(DatabaseTestUtil.DateType.DATE, true);
    }

    public void testDatabaseDateMappingCalendar() {
        doTestDatabaseDateMapping(DatabaseTestUtil.DateType.CALENDAR, true);
    }

    public void testDatabaseDateMappingString() {
        doTestDatabaseDateMapping(DatabaseTestUtil.DateType.STRING, true);
    }

    public void testDatabaseDateMappingNull() {
        // just make sure that this works.  Used to crash.
        doTestDatabaseDateMapping(DatabaseTestUtil.DateType.NULL, false);
    }

    public void doTestDatabaseDateMapping(DatabaseTestUtil.DateType dateType, boolean verifyDates) {
        for (String sqlType : new String[] { "java.sql.Date", "java.sql.Time", "java.sql.Timestamp" }) {
            try {
                // insert some data
                DatabaseTestUtil.insertOrUpdateAccountsDb(getController(), true/* insert */, 1, dateType, false,
                        sqlType);
                // query and verify the results
                verifyDbInsertOrUpdate(getController(), true, verifyDates);
            } finally {
                DatabaseTestUtil.deleteAllAccountsDb(getController());
            }
        }
    }

    /**
     * @param theController
     * @param validateDates TODO
     * 
     */
    private static void verifyDbInsertOrUpdate(Controller theController, boolean isInsert, boolean validateDates) {
        DatabaseReader reader = null;
        logger.info("Verifying database success for '" + (isInsert ? "insert" : "update") + "' operation");
        try {
            // sort order is reverse between insert and update
            reader = new DatabaseReader(theController.getConfig(), "queryAccountAll");
            reader.open();
            int readBatchSize = 1000;
            List<Row> readRowList = reader.readRowList(readBatchSize);
            int rowsProcessed = 0;
            assertNotNull("Error reading " + readBatchSize + " rows", readRowList);
            while(readRowList.size() > 0) {
                // sort the row list so that it comes out in the right order.
                // for the update, the order is reversed.
                Collections.sort(readRowList, new AccountRowComparator(!isInsert));
                logger.info("Verifying database success for next " + (rowsProcessed + readRowList.size()) + " rows");
                for (int i=0; i < readRowList.size(); i++) {
                    Row readRow = readRowList.get(i);
                    assertNotNull("Error reading data row #" + i + ": the row shouldn't be null", readRow);
                    assertTrue("Error reading data row #" + i + ": the row shouldn't be empty", readRow.size()>0);
                    Row expectedRow = DatabaseTestUtil.getInsertOrUpdateAccountRow(isInsert, rowsProcessed, DatabaseTestUtil.DateType.VALIDATION);
                    // verify all expected data
                    for(String colName : VALIDATE_COLS) {
                        if(validateDates && colName.equals(DateType.DATE)) {
                            verifyCol(DatabaseTestUtil.LAST_UPDATED_COL, readRow, expectedRow);
                        } else {
                            verifyCol(colName, readRow, expectedRow);
                        }
                    }

                    rowsProcessed++;
                }
                readRowList = reader.readRowList(readBatchSize);
                assertNotNull("Error reading " + readBatchSize + " rows", readRowList);
            }
        } catch (DataAccessObjectInitializationException e) {
            fail("Error initializing database reader for db config: " + "queryAccountAll");
        } catch (DataAccessObjectException e) {
            fail("Error getting database from the database using db config: "+ "queryAccountAll");
        } finally {
            if(reader != null) reader.close();
        }
    }

    /**
     * @param colName
     * @param col
     * @param colExpected
     */
    private static void verifyCol(String colName, Row row, Row expectedRow) {
        Object actualValue = row.get(colName);
        Object expectedValue = expectedRow.get(colName);
        assertNotNull("actual value is null", actualValue);
        assertNotNull("expected value is null", expectedValue);
        Class<?> expectedClass = expectedValue.getClass();
        Class<?> actualClass = actualValue.getClass();
        assertEquals("Data validation failed for column: " + colName + ", expected type: " + expectedClass
                + ", actual type: " + actualClass, expectedValue.toString(), actualValue.toString());
    }
}
