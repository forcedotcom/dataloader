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
import com.salesforce.dataloader.dao.csv.CSVFileReader;
import com.salesforce.dataloader.dyna.DateConverter;
import com.salesforce.dataloader.model.Row;
import com.sforce.soap.partner.sobject.SObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for dataloader batch interface, also known as "integration framework"
 *
 * @author Alex Warshavsky
 * @since 8.0
 * @hierarchy API.dataloader Csv Process Tests
 * @userstory Commenting existing data loader tests and uploading into QA force
 */
@RunWith(Parameterized.class)
public class CsvProcessTest extends ProcessTestBase {

    public CsvProcessTest(Map<String, String> config) {
        super(config);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(
                TestVariant.defaultSettings(),
                TestVariant.forSettings(TestSetting.BULK_API_ENABLED),
                TestVariant.forSettings(TestSetting.BULK_API_ENABLED, TestSetting.BULK_API_SERIAL_MODE_ENABLED),
                TestVariant.forSettings(TestSetting.BULK_API_ENABLED, TestSetting.BULK_API_ZIP_CONTENT_ENABLED));
    }

    /**
     * Tests the insert operation on Account - Positive test.
     */
    @Test
    public void testInsertAccountCsv() throws Exception {
        runProcess(getTestConfig(OperationInfo.insert, false), 100);
    }

    /**
     * Tests update operation with input coming from a CSV file. Relies on the id's in the CSV on being in the database
     */
    @Test
    public void testUpdateAccountCsv() throws Exception {
        runProcess(getUpdateTestConfig(false, null, 100), 100);
    }

    /**
     * Upsert the records from CSV file
     */
    @Test
    public void testUpsertAccountCsv() throws Exception {
        // manually inserts 50 accounts, then upserts 100 accounts (50 inserts and 50 updates)
        runUpsertProcess(getUpdateTestConfig(true, DEFAULT_ACCOUNT_EXT_ID_FIELD, 50), 50, 50);
    }

    /**
     * Verify that when the constants are placed into the .sdl mapping file,
     * they are treated correctly. It tests that constants mapped on 1 field and
     * a constant mapped on 2 fields both get inserted correctly. This is more
     * of a basic test on the functionality and not an examination of corner
     * cases.
     *
     * @expectedResults Assert that the custom field values come back correctly
     *                  when queried.
     * @throws Exception
     */
    @Test
    public void testConstantMappingInCsv() throws Exception {

        // The use case is as follows:
        // This company in this scenario only does business in the state of CA, therefore billing and shipping
        // addresses are hard coded to that.
        // Also, all of its accounts are in the Aerospace Industry.

        final String stateValue = "California";
        final String industryValue = "Aerospace";

        // insert the values
        Map<String, String> argumentMap = getTestConfig(OperationInfo.insert, false);
        for (SObject acct : retrieveAccounts(runProcess(argumentMap, 2), "Industry", "BillingState", "ShippingState")) {

            assertEquals("Incorrect value for industry returned",
                    industryValue, acct.getField("Industry"));
            assertEquals("Incorrect value for billing state returned",
                    stateValue, acct.getField("BillingState"));
            assertEquals("Incorrect value for shipping state returned",
                    stateValue, acct.getField("ShippingState"));
        }
    }

    /**
     * Verify that the description field, which is of type 'LongTextArea', can be correctly placed as a constant.
     *
     * @expectedResults Assert that the custom field values come back correctly
     *                  when queried.
     *
     * @throws Exception
     */
    @Test
    public void testDescriptionAsConstantMappingInCsv() throws Exception {
        // The use case is as follows:
        // This company in this scenario only does business in the state of CA, therefore billing and shipping
        // addresses are hard coded to that.
        // Also, all of its descriptions are constant.
        final String stateValue = "California";
        final String descriptionValue = "Some Description";

        // insert the values
        Map<String, String> argumentMap = getTestConfig(OperationInfo.insert, getTestDataDir()
                + "/constantMappingInCsv.csv", false);
        Controller controller = runProcess(argumentMap, 2);
        for (SObject acct : retrieveAccounts(controller, "Description", "BillingState", "ShippingState")) {
            assertEquals("Incorrect value for billing state returned", stateValue, acct.getField("BillingState"));
            assertEquals("Incorrect value for shipping state returned", stateValue, acct.getField("ShippingState"));
            assertEquals("Incorrect value for description returned",
                    descriptionValue, acct.getField("Description"));
        }
    }

    /**
     * Verify that when a constant value is given in the CSV file for a given field, and that field
     * is also specified in the CSV, that the values from the CSV file take precedence.
     *
     * @expectedResults Assert that the values retrieved for that field match those in the CSV file
     */
    @Test
    public void testFieldAndConstantFieldClash()  throws Exception {

        // The use case is as follows:
        // This company in this scenario only does business in the state of CA, therefore billing and shipping
        // addresses are hard coded to that.
        // **In this case, not all accounts are in the Aerospace industry.

        final String stateValue = "California";
        final String industryValue = "Aerospace";

        // insert the values
        Map<String, String> argumentMap = getTestConfig(OperationInfo.insert,
                getTestDataDir() + "/constantMappingInCsvClashing.csv", false);
        for (SObject acct : retrieveAccounts(runProcess(argumentMap, 2), "Industry", "BillingState", "ShippingState")) {

            assertEquals("Incorrect value for industry returned",
                    industryValue, acct.getField("Industry"));
            assertEquals("Incorrect value for billing state returned",
                    stateValue, acct.getField("BillingState"));
            assertEquals("Incorrect value for shipping state returned",
                    stateValue, acct.getField("ShippingState"));
        }
    }

    /**
     * Verify that when the left side of a mapping is empty, "", or " ", the field returned is null
     *
     * @expectedResults Assert that the values retrieved for that field match those in the CSV file
     */
    @Test
    public void testNullConstantAssignment()  throws Exception {

        /* Field assignments in .sdl are as follows:
            ""=Industry
            " " = Description
            =AnnualRevenue
         */
        final String stateValue = "California";

        // insert the values
        Map<String, String> argumentMap = getTestConfig(OperationInfo.insert,
                getTestDataDir() + "/constantMappingInCsv.csv", false);

        for (SObject acct : retrieveAccounts(runProcess(argumentMap, 2), "Industry", "Description", "AnnualRevenue",
                "ShippingState")) {

            assertNull("Non-null value for industry returned",
                    acct.getField("Industry"));
            assertNull("Non-null value for billing state returned",
                    acct.getField("Description"));
            assertNull("Non-null value for shipping state returned",
                    acct.getField("AnnualRevenue"));

            //to verify a non-null placement as well
            assertEquals("Incorrect value for shipping state returned",
                    stateValue, acct.getField("ShippingState"));
        }
    }

    //Factor out the verification query
    private SObject[] retrieveAccounts(Controller resultController, String... accountFieldsToReturn) throws Exception {

        List<String> ids = new ArrayList<String>();
        final CSVFileReader successRdr = new CSVFileReader(resultController.getConfig().getString(Config.OUTPUT_SUCCESS));
        try {
            // TODO: revise the use of Integer.MAX_VALUE
            for (Row row : successRdr.readRowList(Integer.MAX_VALUE)) {
                final String rowId = (String) row.get("ID");
                if (rowId != null) {
                    ids.add(rowId);
                }
            }
        } finally {
            successRdr.close();
        }


        // query them and verify that they have the values
        StringBuilder fields = new StringBuilder("id");
        for(String field : accountFieldsToReturn) {
            fields.append(",").append(field);
        }

        SObject[] sobjects = getBinding().retrieve(fields.toString(), "Account", ids.toArray(new String[ids.size()]));
        assertEquals("Wrong number of accounts created", ids.size(), sobjects.length);
        return sobjects;
    }

    /**
     * Tests Upsert on foreign key for the records based on the CSV file
     */
    @Test
    public void testUpsertFkAccountCsv() throws Exception {
        // manually inserts 100 accounts, then upserts specifying account parent for 50 accounts
        runUpsertProcess(getUpdateTestConfig(true, DEFAULT_ACCOUNT_EXT_ID_FIELD, 100), 0, 50);
    }

    /**
     * Tests that Deleting the records based on a CSV file works
     */
    @Test
    public void testDeleteAccountCsv() throws Exception {
        AccountIdTemplateListener listener = new AccountIdTemplateListener(100);
        String deleteFileName = convertTemplateToInput(baseName + "Template.csv", baseName + ".csv", listener);
        Map<String, String> argMap = getTestConfig(OperationInfo.delete, deleteFileName, false);
        Controller theController = runProcess(argMap, 100);
        verifySuccessIds(theController, listener.getAccountIds());
    }

    private class AttachmentTemplateListener extends AccountIdTemplateListener {
        public AttachmentTemplateListener() {
            super(1);
        }

        @Override
        public void updateRow(int idx, Row row) {
            // set parent account id
            row.put("ParentId", getAccountIds()[0]);
            // make body pathname absolute
            String filePath = (String)row.get("Body");
            row.put("Body", getTestDataDir() + File.separator + filePath);
        }
    }

    @Test
    public void testCreateAttachment() throws Exception {
        // convert the template using the parent account id
        final String fileName = convertTemplateToInput(this.baseName + "Template.csv", this.baseName + ".csv",
                new AttachmentTemplateListener());

        final Map<String, String> argMap = getTestConfig(OperationInfo.insert, fileName, false);
        argMap.put(Config.ENTITY, "Attachment");

        // this feature does not work when bulk api is enabled but the zip content type is not
        final boolean bulkApi = isBulkAPIEnabled(argMap);
        final boolean zipContent = isSettingEnabled(argMap, Config.BULK_API_ZIP_CONTENT);
        if (bulkApi && !zipContent) {
            final String failureMessage = "Data Loader cannot map \"Body\" field using Bulk API and CSV content type.  Please enable the ZIP_CSV content type for Bulk API.";
            runProcessNegative(argMap, failureMessage);
        } else {
            runProcess(argMap, 1);
        }
    }

    /**
     * Verify that if not all columns are matched, that the DL operation cannot go forward.
     *
     * @expectedResults Assert that all the records were inserted and that the constant value was mapped as well.
     *
     */
    @Test
    public void testNonMappedFieldsPermittedInDLTransaction() throws Exception {

        final int numberOfRows = 4;
        final String hardCodedShippingState = "California";

        // insert the values
        Map<String, String> argumentMap = getTestConfig(OperationInfo.insert,
                getTestDataDir() + "/accountsForInsert.csv", false);

        SObject[] returnedAccounts = retrieveAccounts(runProcess(argumentMap, numberOfRows), "ShippingState",
                "Industry");

        //quick sanity check on the inserted record.
        for (SObject acct : returnedAccounts) {

            assertEquals("Incorrect value for Shipping state returned",
                    hardCodedShippingState, acct.getField("ShippingState"));

            assertNull("Industry field was populated though it was not mapped", acct.getField("Industry"));
        }
    }

    /**
     *
     * Verify that Date/Time with time zone, when truncated to just date, gets transferred and interpreted correctly.
     *
     * @expectedResults Assert that the dates in Salesforce match up.
     *
     */
    @Test
    public void testTimezoneNotTruncated() throws Exception {

        final int numberOfRows = 12;
        final int targetDate = 14;
        final String dateField = "CustomDateField__c";

        TimeZone TZ = TimeZone.getTimeZone("GMT");

        DateConverter converter = new DateConverter(TZ, false);
        //find the csv file
        Map<String, String> argumentMap = getTestConfig(OperationInfo.insert,
                getTestDataDir() + "/timeZoneFormatTesting.csv", false);

        //insert into the account on the custom fields specified
        SObject[] returnedAccounts = retrieveAccounts(runProcess(argumentMap, numberOfRows), dateField);


        for (SObject acct : returnedAccounts) {

            String dateString = (String)acct.getField(dateField);
            Calendar calFromString = (Calendar) converter.convert(null, dateString);

            assertEquals("Day field does not match", targetDate, calFromString.get(Calendar.DAY_OF_MONTH));
        }
    }

    /**
     * Verify that the controller reports that errors occurred on invalid dates.
     *
     * @expectedResults Assert that the correct number of errors and successes are returned.
     *
     * @throws Exception
     */
    @Test
    public void testErrorsGeneratedOnInvalidDateMatching() throws Exception {

    	runTestErrorsGeneratedOnInvalidDateMatchWithOffset(0, 3,3);
    }

    /**
     * Verify that the controller reports that errors occurred on invalid dates and works with row offsets.
     *
     * @expectedResults Assert that the correct number of errors and successes are returned.
     *
     * @throws Exception
     */
    @Test
    public void testErrorsGeneratedOnInvalidDateMatchingWithOffset() throws Exception {
    	runTestErrorsGeneratedOnInvalidDateMatchWithOffset(2, 2, 2);
    }

    private void runTestErrorsGeneratedOnInvalidDateMatchWithOffset(Integer rowOffset, final int numSuccesses, final int numFailures) throws Exception {

    	//examine the error file and verify that the row in question failed
    	final int numberOfRows = 6;
        final int targetDate = 14;
        final String dateField = "CustomDateField__c";

        assertEquals("Invalid testing configuration", numberOfRows, rowOffset + numFailures + numSuccesses);

        TimeZone TZ = TimeZone.getTimeZone("GMT");

        DateConverter converter = new DateConverter(TZ, false);
        //find the csv file
        Map<String, String> argumentMap = getTestConfig(OperationInfo.insert, getTestDataDir()
                + "/timeZoneFormatTestingWithErrors.csv", false);
        argumentMap.put(Config.LOAD_ROW_TO_START_AT, rowOffset.toString());

        // insert into the account on the custom fields specified
        Controller controller = runProcessWithErrors(argumentMap, numSuccesses, numFailures);

        verifyErrors(controller, "Error converting value to correct data type: Failed to parse date: ");

        SObject[] returnedAccounts = retrieveAccounts(controller, dateField);

        // TODO this verification shouldn't be here necessarily. move it to DateProcessTest
        for (SObject acct : returnedAccounts) {

            String dateString = (String)acct.getField(dateField);

            Calendar.getInstance(TZ);
            Calendar calFromString = (Calendar)converter.convert(null, dateString);

            assertEquals("Day field does not match", targetDate, calFromString.get(Calendar.DAY_OF_MONTH));
        }
    }
}

