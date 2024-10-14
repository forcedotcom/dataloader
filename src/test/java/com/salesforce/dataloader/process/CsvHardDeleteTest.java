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
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import com.salesforce.dataloader.model.Row;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for dataloader hard delete feature
 *
 * @author Vidya Narayanan
 * @since 19.0
 * @hierarchy API.dataloader Csv Process Tests
 * @userstory Commenting existing data loader tests and uploading into QA force
 */
@RunWith(Parameterized.class)
@SuppressWarnings("unused")
public class CsvHardDeleteTest extends ProcessTestBase {

    public CsvHardDeleteTest(Map<String, String> config) {
        super(config);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> getTestParameters() {
        return Arrays.asList(
                TestVariant.forSettings(TestSetting.BULK_API_ENABLED),
                TestVariant.forSettings(TestSetting.BULK_API_ENABLED, TestSetting.BULK_API_CACHE_DAO_UPLOAD_ENABLED),
                TestVariant.forSettings(TestSetting.BULK_API_ENABLED, TestSetting.BULK_API_ZIP_CONTENT_ENABLED),
                TestVariant.forSettings(TestSetting.BULK_API_ENABLED, TestSetting.BULK_API_SERIAL_MODE_ENABLED),
                TestVariant.forSettings(TestSetting.BULK_V2_API_ENABLED)
            );
    }

    /**
     * Hard Delete the records based on a CSV file. Verifies that there were no errors during this operation and success
     * was returned. This operation permanently deletes records from the org.
     */
    @Test
    public void testHardDeleteAccountCsv() throws Exception {

        // do an insert of some account records to ensure there is some data to
        // hard delete
        AccountIdTemplateListener listener = new AccountIdTemplateListener(100);

        // set batch process parameters
        Map<String, String> argMap = getHardDeleteTestConfig(listener);
        Controller theController = runProcess(argMap, 100);
        verifySuccessIds(theController, listener.getAccountIds());
    }

    /**
     * Hard Delete - negative test. Login with user who has bulk api hard delete user permission disabled and
     * verify that hard delete operation cannot be performed
     */
    @Test
    public void testHardDeleteUserPermOff() throws Exception {
        // attempt to hard delete 100 accounts as a user without the "Bulk API Hard Delete" user perm enabled
        final Map<String, String> argMap = getHardDeleteTestConfig(new AccountIdTemplateListener(100));
        // change the configured user to be the standard user (ie without the perm)
        argMap.put(AppConfig.PROP_USERNAME, getProperty("test.user.restricted"));

        runProcessNegative(argMap, "You need the Bulk API Hard Delete user permission to permanently delete records.");
    }

    /**
     * Hard Delete - positive boundary test. Hard delete 1 record based on input from csv file.Verifies that there were
     * no errors during this operation and success was returned. This operation permanently deletes records from the
     * org.
     */
    @Test
    public void testHardDelete1AccountCsv() throws Exception {
        // do an insert of 1 account record to ensure there is some data to
        // hard delete
        // set batch process parameters
        Map<String, String> argMap = getHardDeleteTestConfig(new AccountIdTemplateListener(1));
        runProcess(argMap, 1);
    }

    /**
     * Hard Delete - Negative test. An empty input csv file is used to verify that error message is thrown to user.
     */
    @Test
    public void testHardDeleteEmptyCsvFile() throws Exception {
        // set batch process parameters
        Map<String, String> argMap = getHardDeleteTestConfig(new AccountIdTemplateListener(0));

        try {
            runProcess(argMap, 0);
            // fail("Did not expect empty CSV file to succeed");
        } catch (RuntimeException e) {
            if (e.getCause() instanceof DataAccessObjectInitializationException) {
                DataAccessObjectInitializationException ex = (DataAccessObjectInitializationException) e.getCause();
                String actualMessage = ex.getMessage();
                assertTrue("Wrong exception message: " + actualMessage,
                        actualMessage != null && actualMessage.contains("some error string"));
            } else {
                throw new RuntimeException("Wrong exception was thrown while processing empty CSV", e);
            }
        }
    }

    /**
     * Hard Delete - Negative test. Uncheck Bulk Api setting in data loader to verify that Hard Delete operation cannot
     * be done.
     */
    @Test
    public void testHardDeleteBulkApiSetToFalse() throws DataAccessObjectException {
        // do an insert of some account records to ensure there is some data
        // to hard delete

        // set batch process parameters
        Map<String, String> argMap = getHardDeleteTestConfig(new AccountIdTemplateListener(1));
        argMap.remove(AppConfig.PROP_BULK_API_ENABLED);
        argMap.remove(AppConfig.PROP_BULKV2_API_ENABLED);
        try {
            runProcess(argMap, 889);
            Assert.fail("hard delete should not succeed if bulk api is turned off");
        } catch (Exception e) {
            final String msg = e.getMessage();
            final String expected = "Error instantiating operation hard_delete: could not instantiate class: null.";
            assertEquals("Wrong exception thrown when attempting to do hard delete with bulk api off : ", expected, msg);
        }

    }

    private class InvalidIdTemplateListener extends AccountIdTemplateListener {

        public InvalidIdTemplateListener(int numValidAccounts) {
            super(numValidAccounts);
        }

        @Override
        public void updateRow(int idx, Row row) {
            if (idx == 0)
                row.put("ID", "abcde0123456789XYZ");
            else
                super.updateRow(idx - 1, row);
        }
    }

    /**
     * Hard Delete - Negative test. Input a csv file with invalid id to verify that the test fails.
     */
    @Test
    public void testHardDeleteInvalidInput() throws Exception {
        // do an insert of some account records to ensure there is some data
        // to hard delete
        Map<String, String> argMap = getHardDeleteTestConfig(new InvalidIdTemplateListener(0));
        Controller theController = runProcessWithErrors(argMap, 0, 1);

        // verify there were errors during operation
        verifyErrors(theController, "MALFORMED_ID:malformed id abcde0123456789XYZ");
    }

    /**
     * Hard Delete - Negative test. Input a csv file with 1 invalid id and 2 other valid ids to verify that the test
     * fails for the invalid id and passes for the valid id.
     */
    @Test
    public void testHardDeleteInvalidIDFailsOtherValidIDPasses() throws Exception {
        // do an insert of some account records to ensure there is some data to
        // hard delete
        InvalidIdTemplateListener listener = new InvalidIdTemplateListener(2);
        Map<String, String> argMap = getHardDeleteTestConfig(listener);
        Controller theController = runProcessWithErrors(argMap, 2, 1);

        // verify there were errors during operation
        verifyErrors(theController, "MALFORMED_ID:malformed id abcde0123456789XYZ");

        // verify that the value of ids in success file matches the actual input
        // id value
        verifySuccessIds(theController, listener.getAccountIds());
    }

    /**
     * Hard Delete - Negative test. Hard delete should fail when other object's ID is used.
     */
    @Test
    public void testHardDeleteIDFromOtherObjectFails() throws Exception {
        // set batch process parameters
        Map<String, String> argMap = getHardDeleteTestConfig(new AccountIdTemplateListener(1));
        argMap.put(AppConfig.PROP_ENTITY, "Contact");
        Controller theController = runProcessWithErrors(argMap, 0, 1);

        // verify there were errors during operation
        verifyErrors(theController, "INVALID_ID_FIELD:Invalid Id for entity type 'Contact'");
    }

    private class HeterogeneousIdTemplateListener extends AccountIdTemplateListener {
        private final String[] contactIds;

        public HeterogeneousIdTemplateListener(int numAccounts, int numContacts) {
            super(numAccounts);
            this.contactIds = insertSfdcContacts(numContacts, false);
        }

        @Override
        public void updateRow(int idx, Row row) {
            if (idx < this.contactIds.length)
                row.put("ID", this.contactIds[idx]);
            else
                super.updateRow(idx - contactIds.length, row);
        }

    }

    /**
     * Hard Delete - Negative test. Hard delete succeeds for same object ID and fails for other object ID in same csv.
     */
    @Test
    public void testHardDeleteSameObjectIDSucceedsOtherObjectIDFails() throws Exception {
        // do an insert of some account records to ensure there is some data to
        // hard delete
        HeterogeneousIdTemplateListener listener = new HeterogeneousIdTemplateListener(1, 1);
        Map<String, String> argMap = getHardDeleteTestConfig(listener);
        Controller theController = runProcessWithErrors(argMap, 1, 1);

        // verify there were errors during operation
        verifyErrors(theController, "INVALID_ID_FIELD:Invalid Id for entity type 'Account'");

        // verify the id value matches the input id value for the 1 successful
        // records
        verifySuccessIds(theController, listener.getAccountIds());
    }

    private Map<String, String> getHardDeleteTestConfig(TemplateListener listener) throws DataAccessObjectException {
        final String deleteFn = convertTemplateToInput(this.baseName + "Template.csv", this.baseName + ".csv", listener);
        return getTestConfig(OperationInfo.hard_delete, deleteFn, false);
    }

}
