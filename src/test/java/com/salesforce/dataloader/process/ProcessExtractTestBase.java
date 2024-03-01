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

import static org.junit.Assert.*;

import java.io.File;
import java.util.*;

import org.junit.Assert;
import org.junit.runners.Parameterized;

import com.salesforce.dataloader.TestSetting;
import com.salesforce.dataloader.TestVariant;
import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataReader;
import com.salesforce.dataloader.dao.csv.CSVFileReader;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.ProcessInitializationException;
import com.salesforce.dataloader.model.Row;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;

/**
 * Base class for extraction process tests
 *
 * @author Aleksandr Shulman, Colin Jarvis
 * @since 21.0
 */
public abstract class ProcessExtractTestBase extends ProcessTestBase {

    public ProcessExtractTestBase(Map<String, String> config) {
        super(config);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(
                // partner API
                TestVariant.forSettings(TestSetting.BULK_API_DISABLED, TestSetting.BULK_V2_API_DISABLED)
                // Bulk API
                , TestVariant.forSettings(TestSetting.BULK_API_ENABLED, TestSetting.BULK_V2_API_DISABLED)
                // Bulk V2 Query API
                , TestVariant.forSettings(TestSetting.BULK_API_ENABLED, TestSetting.BULK_V2_API_ENABLED)
                );
    }

    protected class ExtractContactGenerator extends ContactGenerator {
        private final String uniqueLastName;

        public ExtractContactGenerator() {
            this.uniqueLastName = String.valueOf(System.currentTimeMillis());
        }

        @Override
        public SObject getObject(int i, boolean negativeTest) {
            final SObject contact = super.getObject(i, negativeTest);
            contact.setField("LastName", this.uniqueLastName);
            return contact;
        }

        @Override
        public String getSOQL(String selectExpression) {
            final String isDeletedFilter = "IsDeleted=" + String.valueOf(isExtractAll());
            return generateSOQL(selectExpression, CONTACT_WHERE_CLAUSE, "LastName='" + this.uniqueLastName + "'",
                    isDeletedFilter);
        }
    }

    protected class ExtractAccountGenerator extends AccountGenerator {
        private final String uniqueName;

        public ExtractAccountGenerator() {
            this.uniqueName = String.valueOf(System.currentTimeMillis());
        }

        @Override
        public SObject getObject(int i, boolean negativeTest) {
            final SObject account = super.getObject(i, negativeTest);
            account.setField("Name", this.uniqueName);
            return account;
        }

        @Override
        public String getSOQL(String selectExpression) {
            final String isDeletedFilter = "IsDeleted=" + String.valueOf(isExtractAll());
            return generateSOQL(selectExpression, ACCOUNT_WHERE_CLAUSE, "Name='" + this.uniqueName + "'",
                    isDeletedFilter);
        }
    }

    protected abstract boolean isExtractAll();

    protected Map<String, String> getExtractionTestConfig(String soql, String entity, boolean useMappingFile) {

        final Map<String, String> argMap = getTestConfig(isExtractAll() ? OperationInfo.extract_all
                : OperationInfo.extract, true);
        argMap.put(Config.ENTITY, entity);
        argMap.put(Config.EXTRACT_SOQL, soql);
        argMap.put(Config.ENABLE_EXTRACT_STATUS_OUTPUT, Config.TRUE);
        argMap.put(Config.LIMIT_OUTPUT_TO_QUERY_FIELDS, Config.TRUE);
        argMap.put(Config.EXTRACT_REQUEST_SIZE, "2000");
        if (!useMappingFile) {
            argMap.remove(Config.MAPPING_FILE);
        }
        return argMap;
    }
    
    Map<String, String> getDoNotLimitOutputToQueryFieldsTestConfig(String soql, String entity, boolean useMappingFile) {

        final Map<String, String> argMap = getExtractionTestConfig(soql, entity, useMappingFile);
        argMap.put(Config.LIMIT_OUTPUT_TO_QUERY_FIELDS, Config.FALSE);
        return argMap;
    }
    
    // Utility functions
    protected void verifyIdsInCSV(Controller control, String[] ids) throws DataAccessObjectException {
        verifyIdsInCSV(control, ids, false);
    }

    protected void verifyIdsInCSV(Controller control, String[] ids, boolean checkPhoneFormat) throws DataAccessObjectException {

        // assert that it's a CSV...if not fail
        final Set<String> unexpectedIds = new HashSet<String>();
        final Set<String> expectedIds = new HashSet<String>(Arrays.asList(ids));
        String fileName = control.getConfig().getString(Config.OUTPUT_SUCCESS);
        final DataReader resultReader = new CSVFileReader(new File(fileName), getController().getConfig(), true, false);
        try {
            resultReader.open();

            // go through item by item and assert that it's there
            Row row;
            int currentRow = 0;
            while ((row = resultReader.readRow()) != null) {
                final String resultId = (String)row.get(Config.ID_COLUMN_NAME);
                assertValidId(resultId);
                String resultPhone = (String)row.get("Phone");
                if (checkPhoneFormat && resultPhone != null) {
                    resultPhone = resultPhone.substring(0, 8);
                    int remainder = currentRow++ % 4;
                    switch (remainder) {
                        case 0 :
                            assertEquals("Incorrect phone number conversion", resultPhone, "+1415555");
                            break;
                        case 1 :
                            assertEquals("Incorrect phone number conversion", resultPhone, "(415) 55");
                            break;
                        case 2 :
                            assertEquals("Incorrect phone number conversion", resultPhone, "(415) 55");
                            break;
                        case 3 :
                            resultPhone = resultPhone.substring(0,5);
                            assertEquals("Incorrect phone number conversion", resultPhone, "14155");
                            break;
                        default :
                            assertEquals("Incorrect phone number conversion", resultPhone, "1415555");
                            break;
                    }
                }
                if (!expectedIds.remove(resultId)) {
                    unexpectedIds.add(resultId);
                }
            }
        } finally {
            resultReader.close();
        }

        if (!expectedIds.isEmpty()) {
            Assert.fail("These ids were not found in the result file: " + expectedIds);
        }

        if (!unexpectedIds.isEmpty()) {
            Assert.fail("These unexpected ids were found in the result file");
        }
    }

    public abstract void testNestedQueryErrorsCorrectly() throws Exception;

    /**
     * Verify that a correct error is given when a user attempts to perform a nested query using queryAll(). This
     * verifies bug W-870843.
     *
     * @throws com.salesforce.dataloader.exception.DataAccessObjectException
     * @throws com.salesforce.dataloader.exception.ProcessInitializationException
     * @expectedResults Assert that the internal error message is correct.
     */
    protected void runTestNestedQueryErrorsCorrectly() throws ProcessInitializationException, DataAccessObjectException {
        String soql = null;
        Map<String, String> argmap = null;
        soql = "Select Account.Name, (Select Contact.LastName FROM Account.Contacts) FROM Account";
        argmap = getExtractionTestConfig(soql, "Account", false);
        // this error message to change
        runProcessNegative(argmap, "Invalid soql: Nested queries are not supported in SOQL SELECT clause");
    }

    public abstract void testSoqlWithRelationships() throws Exception;

    /**
     * Tests that SOQL queries with relationships work as expected. This is a utility function originally obtained from
     * CsvExtractProcessTest and then retrofitted with an argument as to whether it's extract or extractAll.
     */
    protected void runTestSoqlWithRelationships() throws ProcessInitializationException,
    DataAccessObjectException {

        final String accountId = insertSfdcAccounts(1, false)[0];
        final ContactGenerator contactGen = new ContactGenerator() {
            @Override
            public SObject getObject(int i, boolean negativeTest) {
                final SObject contact = super.getObject(i, negativeTest);
                contact.setField("AccountId", accountId);
                return contact;
            }
        };
        final String contactId = insertSfdcRecords(1, false, contactGen)[0];

        // TEST
        // set batch process parameters
        runSoqlRelationshipTest(contactId, accountId,
                "Select Id, Name, Account.Name, Account.Id From Contact Where Id = '" + contactId + "'");

        runSoqlRelationshipTest(contactId, accountId,
                "Select c.Id, C.Name, CONTACT.account.NAME, c.account.Id From Contact c Where Id = '" + contactId + "'");

        runSoqlRelationshipTest(contactId, accountId,
                "Select c.Id, C.Name, TestField__r.TestField__c, CONTACT.account.NAME, c.account.Id From Contact c Where Id = '" + contactId + "'");
    }
    
    protected void runTestSelectFieldsSoql() throws ProcessInitializationException,
    DataAccessObjectException {

        final TestFieldGenerator testFieldGen = new TestFieldGenerator();
        final String[] testFieldIds = insertSfdcRecords(1, false, testFieldGen);

        // TEST only if it is Partner or Bulk v2
        // set batch process parameters
        if (!isBulkAPIEnabled(this.getTestConfig()) || isBulkV2APIEnabled(this.getTestConfig())) {
            String soql = "SELECT fields(standard) FROM TestField__c WHERE id='" + testFieldIds[0] + "'"; // fields are not explicitly specified in SOQL
            Map<String, String> testConfig = getDoNotLimitOutputToQueryFieldsTestConfig(soql, "Account", true);
            Controller control = runProcess(testConfig, 1);
            // verify IDs and phone format 
            verifyIdsInCSV(control, testFieldIds, false);
            String fileName = control.getConfig().getString(Config.OUTPUT_SUCCESS);
            final DataReader resultReader = new CSVFileReader(new File(fileName), getController().getConfig(), true, false);
            try {
                resultReader.open();

                // go through item by item and assert that it's there
                Row row;
                int rowIdx = 0;
                while ((row = resultReader.readRow()) != null) {
                    final String resultId = (String)row.get(Config.ID_COLUMN_NAME);
                    assertValidId(resultId);
                    assertEquals(resultId, testFieldIds[rowIdx]);
                    
                    final String resultName = (String)row.get("name_to_test");
                    assertTrue("Name field not mapped to DAO name in success file", 
                            resultName != null && !resultName.isBlank() && resultName.startsWith("testfield__"));
                    rowIdx++;
                }
            } finally {
                resultReader.close();
            }

        }
        // cleanup
        try {
            getBinding().delete(testFieldIds);
        } catch (ConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
     }

    public abstract void testSoqlWithTableNameInSelect() throws Exception;

    protected void runTestSoqlWithTableNameInSelect() throws ProcessInitializationException, DataAccessObjectException,
    ConnectionException {
        final ExtractContactGenerator contactGenerator = new ExtractContactGenerator();
        final String soql = contactGenerator.getSOQL("Contact.Id, Contact.Account.id");
        final Map<String, String> argmap = getExtractionTestConfig(soql, "Contact", false);
        final String[] contactIds = insertExtractTestRecords(10, contactGenerator);
        Controller control = runProcess(argmap, contactIds.length);
        verifyIdsInCSV(control, contactIds);
    }

    // Tests common to both that need to be implemented
    public abstract void testForNonQueryableSObjects() throws Exception;

    protected void runTestForNonQueryableSObjects() throws ProcessInitializationException, DataAccessObjectException {
        final String nonQueryableType = "AggregateResult";

        final String soql = "select id from " + nonQueryableType;
        final Map<String, String> argmap = getExtractionTestConfig(soql, nonQueryableType, false);

        if (isBulkV2APIEnabled(argmap) || !isBulkAPIEnabled(argmap)) {
            // Partner or Bulk v2 query 
            runProcessNegative(argmap, "entity type " + nonQueryableType + " does not support query");
        } else {
            // Bulk v1 query
            runProcessNegative(argmap, "Entity '" + nonQueryableType + "' is not supported by the Bulk API.");
        }
    }

    public abstract void testMalformedQueries() throws Exception;

    protected void runMalformedQueriesTest() throws ProcessInitializationException, DataAccessObjectException {
        // invalid select expression: missing comma
        runExtractNegativeTest("select id name from account where", "No such field id name on entity Account");
        // no select keyword
        runExtractNegativeTest("id from account where Name='sometext'", "Invalid soql: No 'SELECT' keyword");
        // incomplete where expression
        runExtractNegativeTest("select id from account where", "unexpected token: '<EOF>'");
        // from doesn't contain table name
        runExtractNegativeTest("select id from where Name='sometext'", "Invalid soql: Failed to parse table name");
        // bad field name in select expression
        runExtractNegativeTest("select id, not_existing_field from account where Name='sometext'",
                "No such field not_existing_field on entity Account");
    }

    private void runExtractNegativeTest(String soql, String expectedErrorMsg) throws ProcessInitializationException,
    DataAccessObjectException {
        runProcessNegative(getExtractionTestConfig(soql, "Account", false), expectedErrorMsg);
    }

    protected void runSoqlRelationshipTest(String contactId, String accountId, final String soql)
            throws ProcessInitializationException, DataAccessObjectException {
        setServerApiInvocationThreshold(100);
        Map<String, String> argMap = getExtractionTestConfig(soql, "Contact", true);
        doRunSoqlRelationshipTest(contactId, accountId, soql, argMap);
        argMap = getDoNotLimitOutputToQueryFieldsTestConfig(soql, "Contact", true);
        doRunSoqlRelationshipTest(contactId, accountId, soql, argMap);
    }
    
    private void doRunSoqlRelationshipTest(String contactId, String accountId, final String soql, Map<String, String> argMap)
            throws ProcessInitializationException, DataAccessObjectException {

        runProcess(argMap, 1);
        final CSVFileReader resultReader = new CSVFileReader(new File(argMap.get(Config.DAO_NAME)), getController().getConfig(), true, false);
        try {
            final Row resultRow = resultReader.readRow();
            assertEquals("Query returned incorrect Contact ID", contactId, resultRow.get("CONTACT_ID"));
            assertEquals("Query returned incorrect Contact Name", "First 000000 Last 000000",
                    resultRow.get("CONTACT_NAME"));
            assertEquals("Query returned incorrect Account ID", accountId, resultRow.get("ACCOUNT_ID"));
            assertEquals("Query returned incorrect Account Name", "account insert#000000",
                    resultRow.get("ACCOUNT_NAME"));

        } finally {
            resultReader.close();
        }
    }

    public abstract void testExtractAccountCsv() throws Exception;

    protected void runTestExtractAccountCsv() throws ProcessInitializationException, DataAccessObjectException,
    ConnectionException {
        // insert accounts so there's something to query
        final ExtractAccountGenerator accountGen = new ExtractAccountGenerator();
        final int numRecords = 100;
        final String[] accountIds = insertExtractTestRecords(numRecords, accountGen);
        
        String soql;
        if (isBulkAPIEnabled(this.getTestConfig()) || isBulkV2APIEnabled(this.getTestConfig())) {
            soql= accountGen
                    .getSOQL("ID, NAME, TYPE, PHONE, ACCOUNTNUMBER__C, WEBSITE, ANNUALREVENUE, LASTMODIFIEDDATE, ORACLE_ID__C");
        } else {
            soql= accountGen
                    .getSOQL("ID, BILLINGADDRESS, NAME, TYPE, PHONE, ACCOUNTNUMBER__C, WEBSITE, ANNUALREVENUE, LASTMODIFIEDDATE, ORACLE_ID__C");
            
        }
        Map<String, String> testConfig = getExtractionTestConfig(soql, "Account", true);
        testConfig.put(Config.DAO_WRITE_BATCH_SIZE, "10"); // total 100 entries in the results file, write in chunks of 10
        Controller control = runProcess(testConfig, numRecords);
        // verify IDs and phone format 
        verifyIdsInCSV(control, accountIds, true);
        
        testConfig = getDoNotLimitOutputToQueryFieldsTestConfig(soql, "Account", true);
        control = runProcess(testConfig, numRecords);
        // verify IDs and phone format 
        verifyIdsInCSV(control, accountIds, true);
        
        if (!isBulkAPIEnabled(this.getTestConfig())
                && !isBulkV2APIEnabled(this.getTestConfig())) {
            // Bulk v1 does not support Select fields()
            // Bulk v2 supports Select fields but Account sobject's standard fields contain compound
            // fields which are not supported by Bulk v2.
            soql = accountGen
                    .getSOQL("fields(standard)"); // fields are not explicitly specified in SOQL
            testConfig = getDoNotLimitOutputToQueryFieldsTestConfig(soql, "Account", true);
            control = runProcess(testConfig, numRecords);
            // verify IDs and phone format 
            verifyIdsInCSV(control, accountIds, true);
        }
    }

    public abstract void testExtractAccountCsvAggregate() throws Exception;

    protected void runTestExtractAccountCsvAggregate() throws ConnectionException, ProcessInitializationException,
    DataAccessObjectException {
        final int numRecords = 15;
        // insert accounts so there's something to query
        final ExtractAccountGenerator accountGen = new ExtractAccountGenerator();
        insertExtractTestRecords(numRecords, accountGen);

        final String soql = accountGen.getSOQL("max(numberofemployees) max_emps");
        final Map<String, String> argMap = getExtractionTestConfig(soql, "Account", false);
        if (isBulkAPIEnabled(argMap) || isBulkV2APIEnabled(this.getTestConfig())) {
            runProcessNegative(
                    argMap,
                    "Aggregate Relationships not supported in Bulk Query");
        } else {
            runProcess(argMap, 1, true);
            final CSVFileReader resultReader = new CSVFileReader(new File(argMap.get(Config.DAO_NAME)), getController().getConfig(), true, false);
            try {
                assertEquals(String.valueOf(numRecords - 1), resultReader.readRow().get("MAX(NUMBEROFEMPLOYEES)"));
            } finally {
                resultReader.close();
            }
        }
    }

    /** inserts test records using the given generator, and deletes them if this is an extractAll test */
    protected String[] insertExtractTestRecords(int numRecords, SObjectGenerator sObjectGen) throws ConnectionException {
        final String[] ids = insertSfdcRecords(numRecords, false, sObjectGen);
        if (isExtractAll()) {
            getBinding().delete(ids);
        }
        return ids;
    }

    protected boolean isBulkV2APIEnabled(Map<String, String> argMap) {
        // bulk v2 api is not used for query all
        return !isExtractAll() && super.isBulkV2APIEnabled(argMap);
    }
}
