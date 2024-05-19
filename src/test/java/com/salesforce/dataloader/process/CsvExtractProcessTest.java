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

package com.salesforce.dataloader.process;

import com.salesforce.dataloader.TestSetting;
import com.salesforce.dataloader.TestVariant;
import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.csv.CSVFileReader;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.ProcessInitializationException;
import com.salesforce.dataloader.model.Row;
import com.sforce.soap.partner.GetUserInfoResult;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test suite for the extracting to csv.
 * 
 * @author Colin Jarvis, Aleksandr Shulman
 * @since 21.0
 */
@RunWith(Parameterized.class)
@SuppressWarnings("unused")
public class CsvExtractProcessTest extends ProcessExtractTestBase {
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(
                // partner API
                TestVariant.forSettings(TestSetting.BULK_API_DISABLED, TestSetting.BULK_V2_API_DISABLED),
                // Bulk API
                TestVariant.forSettings(TestSetting.BULK_API_ENABLED, TestSetting.BULK_V2_API_DISABLED),
                // Bulk V2 Query API
                TestVariant.forSettings(TestSetting.BULK_V2_API_ENABLED));
    }
    
    public CsvExtractProcessTest(Map<String, String> config) {
        super(config);
    }

    @Override
    protected boolean isExtractAll() {
        return false;
    }

    /**
     * Verify that queries that involve the parent work, but with the caveat that the query is written in the below
     * manner. The reason is that this is a known DL bug.
     * 
     * @expectedResults Assert the correct number of results through runProcess().
     * @throws Exception
     */
    @Override
    @Test
    public void testSoqlWithTableNameInSelect() throws Exception {
        runTestSoqlWithTableNameInSelect();
    }

    /**
     * Tests the extract operation on Account. Verifies that an extract operation with a soql query is performed
     * correctly.
     */
    @Override
    @Test
    public void testExtractAccountCsv() throws Exception {
        runTestExtractAccountCsv();
    }
    
    @Test
    public void testSelectFieldsSoql() throws Exception {
        runTestSelectFieldsSoql();
    }

    @Override
    @Test
    public void testExtractAccountCsvAggregate() throws Exception {
        runTestExtractAccountCsvAggregate();
    }

    /**
     * Tests that SOQL queries with relationships work as expected
     */
    @Override
    @Test
    public void testSoqlWithRelationships() throws Exception {
        runTestSoqlWithRelationships();
    }

    /**
     * Test output of last run files. 1. Output is enabled, folder is not set (use default) 2. Output is enabled,
     * folder is set 3. Output is disabled
     * 
     * @hierarchy API.dataloader Csv Process Tests
     * @userstory Commenting existing data loader tests and uploading into QA force
     */
    @Test
    public void testLastRunOutput() throws Exception {
        // 1. Output is enabled (use default), folder is not set (use
        // default)
        String baseName = this.baseName;
        upsertSfdcAccounts(1);
        testLastRunOutput(true, baseName + "_default", true, null);

        // 2. Output is enabled, folder is set
        testLastRunOutput(false, baseName + "_dirSet", true, System.getProperty("java.io.tmpdir"));

        // 3. Output is disabled
        testLastRunOutput(false, baseName + "_disabled", false, null);
    }

    /**
     * Verify that a given SObject that does not support query produces the correct DL error. This just a quick check
     * that DL passes the API's error responses back correctly.
     * 
     * @expectedResults Assert that the error message is correct and contains the SObject type
     */
    @Override
    @Test
    public void testForNonQueryableSObjects() throws Exception {
        runTestForNonQueryableSObjects();
    }
    
    @Test
    public void testPolymorphicRelationshipExtract() throws Exception {
        // create a test lead
        final String uid = getBinding().getUserInfo().getUserId();
        final String[] leadidArr = createLead(uid);
        try {
            final String soql = "SELECT Id, Owner.Name, Lead.Owner.Id, x.owner.lastname, OwnerId FROM Lead x where id='"
                    + leadidArr[0] + "'";
            final Map<String, String> argmap = getExtractionTestConfig(soql, "Lead", true);
                // run the extract
                runProcess(argmap, 1);
                GetUserInfoResult userInfo = getBinding().getUserInfo();
                
                // open the results of the extraction
                final CSVFileReader rdr = new CSVFileReader(new File(argmap.get(Config.DAO_NAME)), getController().getConfig(), true, false);
                rdr.open();
                Row row = rdr.readRow();
                assertNotNull(row);
                assertEquals(5,row.size());
                // validate the extract results are correct.
                assertEquals(leadidArr[0], row.get("LID"));
                assertTrue(userInfo.getUserFullName().contains(row.get("LNAME").toString()));
                assertEquals(userInfo.getUserFullName(), row.get("NAME__RESULT"));
                assertEquals(uid, row.get("OID"));
                assertEquals(uid,row.get("OWNID"));
                // validate that we have read the only result. there should be only one.
                assertNull(rdr.readRow());
        } finally {
            // cleanup here since the parent doesn't clean up leads
            getBinding().delete(leadidArr);
        }

    }

    /** creates a lead owned by the provided user */
    private String[] createLead(final String uid) throws ConnectionException {
        final SObject lead = new SObject();
        // Create a lead sobject
        lead.setType("Lead");
        lead.setField("LastName", "test lead");
        lead.setField("Company", "salesforce");
        lead.setField("OwnerId", uid);

        // insert the lead
        final SaveResult[] result = getBinding().create(new SObject[] { lead });

        // validate save result
        assertNotNull(result);
        assertEquals(1, result.length);
        assertTrue(Arrays.toString(result[0].getErrors()), result[0].isSuccess());

        // get new lead id
        final String[] leadidArr = new String[] { result[0].getId() };
        return leadidArr;
    }
    
    /**
     * Tests the extract operation on Account. Verifies that an extract operation with a soql query is performed
     * correctly.
     */
    @Test
    public void testExtractSObjectWithJSONFieldType() throws Exception {
        try {
            // Test for regression in the fix for bug id: W-8551311
            // describeSObject for ApiEvent sObject fails if JSON FieldType enum 
            // does not exist in WSC because 'Records' field is of type JSON.
            
            final String soql = "SELECT Id FROM ApiEvent";
            final Map<String, String> argmap = getExtractionTestConfig(soql, "ApiEvent", false);
                // run the extract
                runProcess(argmap, 0);
        } finally {
            // noop
        }
    }
    
    /**
     * @param enableLastRunOutput
     */
    private void testLastRunOutput(boolean useDefault, String baseProcessName, boolean enableOutput, String outputDir)
            throws DataAccessObjectException, ProcessInitializationException {
        final String soql = "Select ID FROM ACCOUNT WHERE " + ACCOUNT_WHERE_CLAUSE + " limit 1";
        Map<String, String> argMap = getExtractionTestConfig(soql, "Account", false);
        argMap.remove(Config.LAST_RUN_OUTPUT_DIR);

        // set last run output paramerers
        if (!useDefault) {
            argMap.put(Config.ENABLE_LAST_RUN_OUTPUT, String.valueOf(enableOutput));
            argMap.put(Config.LAST_RUN_OUTPUT_DIR, outputDir);
        }

        this.baseName = baseProcessName;
        Controller theController = runProcess(argMap, 1);

        Config config = theController.getConfig();
        String lastRunFilePath = config.getLastRunFilename();
        File lastRunFile = new File(lastRunFilePath);
        try {
            String lastrunFileNamePrefix = config.getString(Config.PROCESS_NAME);
            if (lastrunFileNamePrefix == null || lastrunFileNamePrefix.isBlank()) {
                lastrunFileNamePrefix = config.getString(Config.ENTITY) + config.getString(Config.OPERATION);
            }
            String defaultFileName =  lastrunFileNamePrefix + "_lastRun.properties";
            File expectedFile = useDefault ? new File(config.constructConfigFilePath(defaultFileName)) : new File(
                    outputDir, defaultFileName);
            if (enableOutput) {
                assertTrue("Could not find last run file: " + lastRunFilePath, lastRunFile.exists());
                assertEquals("Did not get expected last run file.", expectedFile, lastRunFile);
            } else {
                assertFalse("Last run file should not exist: " + lastRunFilePath, lastRunFile.exists());
            }

        } finally {
            if (lastRunFile.exists()) lastRunFile.delete();
        }

    }

    /**
     * Verify that a correct error is given when a user attempts to perform a nested query using query().
     * 
     * @expectedResults Assert that the internal error message is correct.
     */
    @Override
    @Test
    public void testNestedQueryErrorsCorrectly() throws Exception {
        runTestNestedQueryErrorsCorrectly();
    }

    /**
     * Verify that the Query parser is able to handle bad input with the correct error messages. These tests are run
     * against account, since the query will not get parsed and thus no results will be returned.
     * 
     * @expectedResults Assert that the correct error messages are given for a given malformed query.
     * @throws Exception
     */
    @Override
    @Test
    public void testMalformedQueries() throws Exception {
        runMalformedQueriesTest();
    }

    @Test
    public void testBinaryDataInRTFQueryResult() throws Exception {
        testBinaryDataInRTFQueryResult("true");
        testBinaryDataInRTFQueryResult("false");
    }
    
    private void testBinaryDataInRTFQueryResult(String inclueRTFBinaryData) throws Exception {
        // insert an account with binary data
        Map<String, String> insertArgMap = getTestConfig(OperationInfo.insert,
                getTestDataDir() + "/acctsWithBinaryDataInRTF.csv", false);
        Controller controller = runProcess(insertArgMap, 1);
        List<String> ids = new ArrayList<String>();
        String fileName = controller.getConfig().getString(Config.OUTPUT_SUCCESS);
        final CSVFileReader successRdr = new CSVFileReader(new File(fileName), getController().getConfig(), true, false);
        String idFieldName = this.isBulkV2APIEnabled(insertArgMap)?"sf__Id":"ID";
        try {
            for (Row row : successRdr.readRowList(Integer.MAX_VALUE)) {
                final String rowId = (String) row.get(idFieldName);
                if (rowId != null) {
                    ids.add(rowId);
                }
            }
        } finally {
            successRdr.close();
        }

        // set config property loader.query.includeBinaryData to true
        String soql = "Select ID,RICHTEXT__C FROM Account where id='" + ids.get(0) + "'";
        Map<String, String> queryArgMap = getExtractionTestConfig(soql, "Account", false);
        queryArgMap.put(Config.INCLUDE_RICH_TEXT_FIELD_DATA_IN_QUERY_RESULTS, inclueRTFBinaryData);

        // query the account and verify results
        controller = runProcess(queryArgMap, 1);
        CSVFileReader queryResultsReader = new CSVFileReader(new File(queryArgMap.get(Config.DAO_NAME)), getController().getConfig(), true, false);
        queryResultsReader.open();
        Row queryResultsRow = queryResultsReader.readRow();
        String queryResultsRTVal = (String)queryResultsRow.get("RICHTEXT__c");

        if ("true".equalsIgnoreCase(inclueRTFBinaryData)) {
            CSVFileReader uploadedCSVReader = new CSVFileReader(new File(getTestDataDir() + "/acctsWithBinaryDataInRTF.csv"), getController().getConfig(), true, false);
            uploadedCSVReader.open();
            Row uploadedRow = uploadedCSVReader.readRow();
            String uploadedRTVal = (String)queryResultsRow.get("RICHTEXT__c");
            assertEquals("Binary data in query result file does not match uploaded data: " 
            + queryArgMap.get(Config.DAO_NAME), queryResultsRTVal, uploadedRTVal);
        } else {
            assertTrue(queryResultsRTVal.contains(".file.force.com/servlet/rtaImage?"));
        }
    }
}
