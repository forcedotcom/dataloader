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
package com.salesforce.dataloader.process;

import java.io.File;
import java.text.ParseException;
import java.util.*;

import junit.framework.TestSuite;

import org.apache.log4j.Logger;

import com.salesforce.dataloader.ConfigGenerator;
import com.salesforce.dataloader.ConfigTestSuite;
import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.LastRun;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.database.DatabaseReader;
import com.salesforce.dataloader.dao.database.DatabaseTestUtil;
import com.salesforce.dataloader.exception.*;

/**
 * Automated tests for dataloader database batch interface
 *
 * @author Alex Warshavsky
 * @since 8.0
 */
public class DatabaseProcessTest extends ProcessTestBase {

    public static TestSuite suite() {
        return ConfigTestSuite.createSuite(DatabaseProcessTest.class);
    }

    public static ConfigGenerator getConfigGenerator() {
        final ConfigGenerator parent = ProcessTestBase.getConfigGenerator();
        final ConfigGenerator withBulkApi = new ConfigSettingGenerator(parent, Config.USE_BULK_API, Boolean.TRUE
                .toString());
        final ConfigGenerator bulkApiZipContent = new ConfigSettingGenerator(withBulkApi, Config.BULK_API_ZIP_CONTENT,
                Boolean.TRUE.toString());
        final ConfigGenerator bulkApiSerialMode = new ConfigSettingGenerator(withBulkApi, Config.BULK_API_SERIAL_MODE,
                Boolean.TRUE.toString());
        return new UnionConfigGenerator(parent, withBulkApi, bulkApiSerialMode, bulkApiZipContent);
    }

    // logger
    private static Logger logger = Logger.getLogger(DatabaseReader.class);
    private static final int NUM_ROWS = 1000;
    private static final int BATCH_SIZE = 100;

    public DatabaseProcessTest(String name, Map<String, String> config) {
        super(name, config);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    public void setUp() {
        super.setUp();
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

    public void testExtractAccountDb() throws ProcessInitializationException, DataAccessObjectException {
        // upsert accounts into salesforce so there's something to query
        upsertSfdcAccounts(NUM_ROWS);

        String processName = baseName + "Process";
        // do insert
        doExtractAccountDb(processName, NUM_ROWS, 0, true);
        // do update
        doExtractAccountDb(processName, NUM_ROWS, 0, false);
    }

    public void testExtractAccountDbNegative() throws ProcessInitializationException, DataAccessObjectException {
        // upsert accounts into salesforce so there's something to query
        upsertSfdcAccounts(500);
        // upsert one bad record causing only one of the database write batches to fail
        upsertBadSfdcAccounts(1, 250);

        // do insert
        doExtractAccountDb("extractAccountDbProcess", 400, 100, true);
    }

    public void testExtractMultipleBadAccounts() throws ProcessInitializationException, DataAccessObjectException {
        // upsert many accounts which will fail to be written to the database on extract
        // the sql exceptions should be logged
        upsertBadSfdcAccounts(555, 777);

        // do insert
        doExtractAccountDb("extractAccountDbProcess", 0, 555, true);
    }

    public void testUpsertAccountDb() throws ParseException, ProcessInitializationException, DataAccessObjectException {
	// This test happens to configure its own encryption file and encrypted password
        // so we need to remove the default test password from the config
        final Map<String, String> argMap = getTestConfig();
        argMap.remove(Config.PASSWORD);
        // insert
        testUpsertAccountsDb(argMap, NUM_ROWS, true, false);
        // update
        testUpsertAccountsDb(argMap, NUM_ROWS, false, false);
    }

    public void testMaximumBatchRowsDb() throws ParseException, ProcessInitializationException,
    DataAccessObjectException {
        final int numRows = isBulkAPIEnabled(getTestConfig()) ? Config.MAX_BULK_API_BATCH_SIZE
                : Config.MAX_LOAD_BATCH_SIZE;
        // insert
        testUpsertAccountsDb(numRows, true);
        // update
        testUpsertAccountsDb(numRows, false);
    }

    private void testUpsertAccountsDb(int numRows, boolean isInsert) throws ParseException,
    ProcessInitializationException, DataAccessObjectException {
        testUpsertAccountsDb(null, numRows, isInsert, false);
    }

    private void testUpsertAccountsDb(Map<String, String> args, int numRows, boolean isInsert, boolean nullValues)
            throws ParseException, ProcessInitializationException, DataAccessObjectException {
        String processName = baseName + "Process";
        String startTime = "2006-01-01T00:00:00.000-0700";

        DatabaseTestUtil.insertOrUpdateAccountsDb(getController(), isInsert, numRows, nullValues);

        // specify the name of the configured process and select appropriate database access type
        if (args == null) args = getTestConfig();
        args.put(ProcessRunner.PROCESS_NAME, processName);
        Config.DATE_FORMATTER.parse(startTime);
        args.put(LastRun.LAST_RUN_DATE, startTime);
        args.put(Config.OPERATION, OperationInfo.upsert.name());

        runUpsertProcess(args, isInsert ? numRows : 0, isInsert ? 0 : numRows);
    }

    public void testInsertNullsDB() throws ParseException, ProcessInitializationException, DataAccessObjectException {
        Map<String, String> args = getTestConfig();
        if (isBulkAPIEnabled(args)) {
            logger.info("testInsertNulls is disabled for bulk api");
            return;
        }
        // TODO: we need to get the accounts from sfdc and check that field values were updated correctly
        // create some rows with non-null values in them
        args.put(Config.INSERT_NULLS, Boolean.toString(false));
        testUpsertAccountsDb(args, 10, true, false);
        // update the rows with some null values, but with insert nulls disabled
        testUpsertAccountsDb(args, 10, false, true);
        // update the rows with some null values, but with insert nulls enabled
        args.put(Config.INSERT_NULLS, Boolean.toString(true));
        testUpsertAccountsDb(args, 10, false, true);
    }

    protected void doExtractAccountDb(String processName, int expectedSuccesses, int expectedFailures, boolean isInsert)
            throws ProcessInitializationException, DataAccessObjectException {

        // specify the name of the configured process and select appropriate database access type
        OperationInfo op = isInsert ? OperationInfo.insert : OperationInfo.update;
        Map<String, String> argMap = getTestConfig();
        argMap.put(Config.OPERATION, OperationInfo.extract.name());
        argMap.put(ProcessRunner.PROCESS_NAME, processName);
        argMap.put(Config.DAO_NAME, op.name() + "Account");
        argMap.put(Config.OUTPUT_SUCCESS, new File(getTestStatusDir(), baseName + op.name() + "Success.csv")
        .getAbsolutePath());
        argMap.put(Config.OUTPUT_ERROR, new File(getTestStatusDir(), baseName + op.name() + "Error.csv")
        .getAbsolutePath());
        argMap.put(Config.ENABLE_EXTRACT_STATUS_OUTPUT, Config.TRUE);
        argMap.put(Config.DAO_WRITE_BATCH_SIZE, String.valueOf(BATCH_SIZE));

        Date startTime = new Date();

        Controller theController = runProcessWithErrors(argMap, expectedSuccesses, expectedFailures);

        // verify there were no errors during extract
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("compare_date", startTime);
        verifyDbSuccess(theController, "queryAccountSince", params, expectedSuccesses);
    }

    /**
     * @param theController
     * @param startTime
     */
    private void verifyDbSuccess(Controller theController, String dbConfigName, Map<String,Object> params, int expectedSuccesses) {
        DatabaseReader reader = null;
        logger.info("Verifying database success for database configuration: " + dbConfigName);
        try {
            reader = new DatabaseReader(theController.getConfig(), dbConfigName);
            reader.open(params);
            int readBatchSize = theController.getConfig().getInt(Config.DAO_READ_BATCH_SIZE);
            List<Map<String,Object>> successRows = reader.readRowList(readBatchSize);
            int rowsProcessed = 0;
            assertNotNull("Error reading " + readBatchSize + " rows", successRows);
            while(successRows.size() > 0) {
                rowsProcessed += successRows.size();
                logger.info("Verifying database success for next " + successRows.size() + " of total " + rowsProcessed + " rows");
                assertTrue("No updated rows have been found in the database.", successRows.size() > 0);
                successRows = reader.readRowList(readBatchSize);
            }
            assertEquals(expectedSuccesses, rowsProcessed);
        } catch (DataAccessObjectInitializationException e) {
            fail("Error initializing database operation success verification using dbConfig: " + dbConfigName +
                    ", error:" + e.getMessage());
        } catch (DataAccessObjectException e) {
            fail("Error reading rows during database operation success verification using dbConfig: " + dbConfigName +
                    ", error:" + e.getMessage());
        } catch (ParameterLoadException e) {
            fail("Error getting a config parameter: " + e.getMessage()
                    + "during database operation success verification using dbConfig: " + dbConfigName);
        } finally {
            if(reader != null) reader.close();
        }
    }
}
