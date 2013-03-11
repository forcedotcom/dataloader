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

package com.salesforce.dataloader.process;

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
import org.junit.Assert;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
                TestVariant.defaultSettings(),
                TestVariant.forSettings(TestSetting.BULK_API_ENABLED));
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

    protected Map<String, String> getTestConfig(String soql, String entity, boolean useMappingFile) {

        final Map<String, String> argMap = getTestConfig(isExtractAll() ? OperationInfo.extract_all
                : OperationInfo.extract, true);
        argMap.put(Config.ENTITY, entity);
        argMap.put(Config.EXTRACT_SOQL, soql);
        argMap.put(Config.ENABLE_EXTRACT_STATUS_OUTPUT, Config.TRUE);
        argMap.put(Config.EXTRACT_REQUEST_SIZE, "2000");
        if (!useMappingFile) {
            argMap.remove(Config.MAPPING_FILE);
        }
        return argMap;
    }

    // Utility functions

    protected void verifyIdsInCSV(Controller control, String[] ids) throws DataAccessObjectException {

        // assert that it's a CSV...if not fail
        final Set<String> unexpectedIds = new HashSet<String>();
        final Set<String> expectedIds = new HashSet<String>(Arrays.asList(ids));
        final DataReader resultReader = new CSVFileReader(control.getConfig().getString(Config.OUTPUT_SUCCESS));
        try {
            resultReader.open();

            // go through item by item and assert that it's there
            Row row;
            while ((row = resultReader.readRow()) != null) {
                final String resultId = (String)row.get(Config.ID_COLUMN_NAME);
                assertValidId(resultId);
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
        argmap = getTestConfig(soql, "Account", false);
        // this error message to change
        runProcessNegative(argmap, "Invalid soql: Nested queries are not supported");
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

    public abstract void testSoqlWithTableNameInSelect() throws Exception;

    protected void runTestSoqlWithTableNameInSelect() throws ProcessInitializationException, DataAccessObjectException,
    ConnectionException {
        final ExtractContactGenerator contactGenerator = new ExtractContactGenerator();
        final String soql = contactGenerator.getSOQL("Contact.Id, Contact.Account.id");
        final Map<String, String> argmap = getTestConfig(soql, "Contact", false);
        if (isBulkAPIEnabled(argmap)) {
            runProcessNegative(
                    argmap,
                    "Batch failed: InvalidBatch : Failed to process query: FUNCTIONALITY_NOT_ENABLED: Foreign Key Relationships not supported in Bulk Query");
        } else {
            final String[] contactIds = insertExtractTestRecords(10, contactGenerator);
            Controller control = runProcess(argmap, contactIds.length);
            verifyIdsInCSV(control, contactIds);
        }
    }

    // Tests common to both that need to be implemented
    public abstract void testForNonQueryableSObjects() throws Exception;

    protected void runTestForNonQueryableSObjects() throws ProcessInitializationException, DataAccessObjectException {
        final String nonQueryableType = "AggregateResult";

        final String soql = "select id from " + nonQueryableType;
        final Map<String, String> argmap = getTestConfig(soql, nonQueryableType, false);
        argmap.put(Config.OPERATION, OperationInfo.extract_all.name());

        runProcessNegative(argmap, "entity type " + nonQueryableType + " does not support query");
    }

    public abstract void testMalformedQueries() throws Exception;

    protected void runMalformedQueriesTest() throws ProcessInitializationException, DataAccessObjectException {
        // invalid select expression: missing comma
        runExtractNegativeTest("select id name from account where", "No such field id name on entity Account");
        // no select keyword
        runExtractNegativeTest("id from account where Name='sometext'", "Invalid soql: No 'SELECT' keyword");
        // incomplete where expression
        final boolean isBulkApi = isBulkAPIEnabled(getTestConfig());
        // bulk api prepends some additional stuff to the error message
        final String bulkPrefix = isBulkApi ? "Batch failed: InvalidBatch : Failed to process query: MALFORMED_QUERY: "
                : "";
        runExtractNegativeTest("select id from account where", bulkPrefix + "unexpected token: '<EOF>'");
        // from doesn't contain table name
        runExtractNegativeTest("select id from where Name='sometext'", "Invalid soql: Failed to parse table name");
        // bad field name in select expression
        runExtractNegativeTest("select id, not_existing_field from account where Name='sometext'",
                "No such field not_existing_field on entity Account");
    }

    private void runExtractNegativeTest(String soql, String expectedErrorMsg) throws ProcessInitializationException,
    DataAccessObjectException {
        runProcessNegative(getTestConfig(soql, "Account", false), expectedErrorMsg);
    }

    protected void runSoqlRelationshipTest(String contactId, String accountId, final String soql)
            throws ProcessInitializationException, DataAccessObjectException {

        final Map<String, String> argMap = getTestConfig(soql, "Contact", true);

        if (isBulkAPIEnabled(argMap)) {
            runProcessNegative(
                    argMap,
                    "Batch failed: InvalidBatch : Failed to process query: FUNCTIONALITY_NOT_ENABLED: Foreign Key Relationships not supported in Bulk Query");
        } else {
            runProcess(argMap, 1);
            final CSVFileReader resultReader = new CSVFileReader(argMap.get(Config.DAO_NAME));
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
    }

    public abstract void testExtractAccountCsv() throws Exception;

    protected void runTestExtractAccountCsv() throws ProcessInitializationException, DataAccessObjectException,
    ConnectionException {
        // insert accounts so there's something to query
        final ExtractAccountGenerator accountGen = new ExtractAccountGenerator();
        final int numRecords = 100;
        final String[] accountIds = insertExtractTestRecords(numRecords, accountGen);
        final String soql = accountGen
                .getSOQL("ID, NAME, TYPE, PHONE, ACCOUNTNUMBER__C, WEBSITE, ANNUALREVENUE, LASTMODIFIEDDATE, ORACLE_ID__C");
        Controller control = runProcess(getTestConfig(soql, "Account", true), numRecords);
        verifyIdsInCSV(control, accountIds);
    }

    public void testPolymorphicRelationshipExtract() throws Exception {
        // create a test lead
        final String uid = getBinding().getUserInfo().getUserId();
        final String[] leadidArr = createLead(uid);
        try {
            final String soql = "SELECT Id, Owner.Name, Lead.Owner.Id, x.owner.lastname, OwnerId FROM Lead x where id='"
                    + leadidArr[0] + "'";
            final Map<String, String> argmap = getTestConfig(soql, "Lead", true);
            if (isBulkAPIEnabled(argmap)) {
                // bulk api doesn't support foreign key relationships so it will always fail
                final String expectedError = "Batch failed: InvalidBatch : Failed to process query: FUNCTIONALITY_NOT_ENABLED: Foreign Key Relationships not supported in Bulk Query";
                runProcessNegative(
                        argmap,
                        expectedError);
            } else {
                // run the extract
                runProcess(argmap, 1);
                // open the results of the extraction
                final CSVFileReader rdr = new CSVFileReader(argmap.get(Config.DAO_NAME));
                rdr.open();
                Row row = rdr.readRow();
                assertNotNull(row);
                assertEquals(5,row.size());
                // validate the extract results are correct.
                assertEquals(leadidArr[0], row.get("LID"));
                assertEquals("loader", row.get("LNAME"));
                assertEquals("data loader", row.get("NAME__RESULT"));
                assertEquals(uid, row.get("OID"));
                assertEquals(uid,row.get("OWNID"));
                // validate that we have read the only result. there should be only one.
                assertNull(rdr.readRow());

            }
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

    public abstract void testExtractAccountCsvAggregate() throws Exception;

    protected void runTestExtractAccountCsvAggregate() throws ConnectionException, ProcessInitializationException,
    DataAccessObjectException {
        final int numRecords = 15;
        // insert accounts so there's something to query
        final ExtractAccountGenerator accountGen = new ExtractAccountGenerator();
        insertExtractTestRecords(numRecords, accountGen);

        final String soql = accountGen.getSOQL("max(numberofemployees) max_emps");
        final Map<String, String> argMap = getTestConfig(soql, "Account", false);
        if (isBulkAPIEnabled(argMap)) {
            runProcessNegative(
                    argMap,
                    "Batch failed: InvalidBatch : Failed to process query: FUNCTIONALITY_NOT_ENABLED: Aggregate Relationships not supported in Bulk Query");
        } else {
            runProcess(argMap, 1, true);
            final CSVFileReader resultReader = new CSVFileReader(argMap.get(Config.DAO_NAME));
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

    @Override
    protected boolean isBulkAPIEnabled(Map<String, String> argMap) {
        // bulk api is not used for query all
        return !isExtractAll() && super.isBulkAPIEnabled(argMap);
    }

}
