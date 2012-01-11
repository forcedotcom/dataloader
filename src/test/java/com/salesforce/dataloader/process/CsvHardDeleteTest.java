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

import java.util.Map;

import junit.framework.TestSuite;

import com.salesforce.dataloader.ConfigGenerator;
import com.salesforce.dataloader.ConfigTestSuite;
import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.client.PartnerClient;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.*;
import com.sforce.soap.partner.QueryResult;
import com.sforce.ws.ConnectionException;

/**
 * Test for dataloader hard delete feature
 *
 * @author Vidya Narayanan
 * @since 19.0
 * @hierarchy API.dataloader Csv Process Tests
 * @userstory Commenting existing data loader tests and uploading into QA force
 */
public class CsvHardDeleteTest extends ProcessTestBase {

    public CsvHardDeleteTest(String name, Map<String, String> config) {
        super(name, config);
    }

    public CsvHardDeleteTest(String name) {
        super(name);
    }

    public static TestSuite suite() {
        return ConfigTestSuite.createSuite(CsvHardDeleteTest.class);
    }

    public static ConfigGenerator getConfigGenerator() {
        final ConfigGenerator withBulkApi = new ConfigSettingGenerator(ProcessTestBase.getConfigGenerator(),
                Config.USE_BULK_API, Boolean.TRUE.toString());
        final ConfigGenerator bulkApiZipContent = new ConfigSettingGenerator(withBulkApi, Config.BULK_API_ZIP_CONTENT,
                Boolean.TRUE.toString());
        final ConfigGenerator bulkApiSerialMode = new ConfigSettingGenerator(withBulkApi, Config.BULK_API_SERIAL_MODE,
                Boolean.TRUE.toString());
        return new UnionConfigGenerator(withBulkApi, bulkApiSerialMode, bulkApiZipContent);
    }
    /**
     * Hard Delete the records based on a CSV file. Verifies that there were no errors during this operation and success
     * was returned. This operation permanently deletes records from the org.
     */
    public void testHardDeleteAccountCsv() throws ProcessInitializationException, DataAccessObjectException {

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
    public void testHardDeleteUserPermOff() throws ProcessInitializationException, DataAccessObjectException, ConnectionException {
        // attempt to hard delete 100 accounts as a user without the "Bulk API Hard Delete" user perm enabled
        final Map<String, String> argMap = getHardDeleteTestConfig(new AccountIdTemplateListener(100));
        
        String soql = "select username from user where profileId in (select id from profile where name like 'Standard User') limit 1";
        QueryResult qr = new PartnerClient(getController()).query(soql);
        String standardUserName = (String) qr.getRecords()[0].getField("Username");
        String originalUserName = getController().getConfig().getString(Config.USERNAME);
        
        try {
            // change the configured user to be the standard user (ie without the perm)
            argMap.put(Config.USERNAME, standardUserName);
            getController().getConfig().loadParameterOverrides(argMap);
            runProcessNegative(argMap, "You need the Bulk API Hard Delete user permission to permanently delete records.");
        } finally {
            argMap.put(Config.USERNAME, originalUserName);
            getController().getConfig().loadParameterOverrides(argMap);
        }
    }

    /**
     * Hard Delete - positive boundary test. Hard delete 1 record based on input from csv file.Verifies that there were
     * no errors during this operation and success was returned. This operation permanently deletes records from the
     * org.
     */
    public void testHardDelete1AccountCsv() throws ProcessInitializationException, DataAccessObjectException {
        // do an insert of 1 account record to ensure there is some data to
        // hard delete
        // set batch process parameters
        Map<String, String> argMap = getHardDeleteTestConfig(new AccountIdTemplateListener(1));
        runProcess(argMap, 1);
    }

    /**
     * Hard Delete - Negative test. An empty input csv file is used to verify that error message is thrown to user.
     */
    public void testHardDeleteEmptyCsvFile() throws ProcessInitializationException, DataAccessObjectException {
        // set batch process parameters
        Map<String, String> argMap = getHardDeleteTestConfig(new AccountIdTemplateListener(0));

        try {
            runProcess(argMap, 0);
            // fail("Did not expect empty CSV file to succeed");
        } catch (RuntimeException e) {
            if (e.getCause() instanceof DataAccessObjectInitializationException) {
                DataAccessObjectInitializationException ex = (DataAccessObjectInitializationException)e.getCause();
                String actualMessage = ex.getMessage();
                assertTrue("Wrong exception message: " + actualMessage,
                        actualMessage != null && actualMessage.contains("some error string"));
            } else {
                throw new RuntimeException("Wrong exception was thrown while processing empty CSV", e);
            }
        }
    }

    /**
     * Hard Delete - Negative test. Uncheck Bull Api setting in data loader to verify that Hard Delete operation cannot
     * be done.
     */
    public void testHardDeleteBulkApiSetToFalse() throws ProcessInitializationException, DataAccessObjectException {
        // do an insert of some account records to ensure there is some data
        // to hard delete

        // set batch process parameters
        Map<String, String> argMap = getHardDeleteTestConfig(new AccountIdTemplateListener(1));
        argMap.remove(Config.USE_BULK_API);
        try {
            runProcess(argMap, 889);
            fail("hard delete should not succeed if bulk api is turned off");
        } catch (Exception e) {
            final String msg = e.getMessage();
            final String expected = "java.lang.UnsupportedOperationException: Error instantiating operation hard_delete: could not instantiate class: null.";
            assertEquals("Wrong exception thrown when attempting to do hard delete with bulk api off : ", expected, msg);
        }

    }

    private class InvalidIdTemplateListener extends AccountIdTemplateListener {

        public InvalidIdTemplateListener(int numValidAccounts) {
            super(numValidAccounts);
        }

        @Override
        public void updateRow(int idx, Map<String, Object> row) {
            if (idx == 0)
                row.put("ID", "abcde0123456789XYZ");
            else
                super.updateRow(idx - 1, row);
        }
    }

    /**
     * Hard Delete - Negative test. Input a csv file with invalid id to verify that the test fails.
     */
    public void testHardDeleteInvalidInput() throws ProcessInitializationException, DataAccessObjectException {
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

    public void testHardDeleteInvalidIDFailsOtherValidIDPasses() throws ProcessInitializationException,
    DataAccessObjectException {
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
    public void testHardDeleteIDFromOtherObjectFails() throws ProcessInitializationException, DataAccessObjectException {
        // set batch process parameters
        Map<String, String> argMap = getHardDeleteTestConfig(new AccountIdTemplateListener(1));
        argMap.put(Config.ENTITY, "Contact");
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
        public void updateRow(int idx, Map<String, Object> row) {
            if (idx < this.contactIds.length)
                row.put("ID", this.contactIds[idx]);
            else
                super.updateRow(idx - contactIds.length, row);
        }

    }

    /**
     * Hard Delete - Negative test. Hard delete succeeds for same object ID and fails for other object ID in same csv.
     */
    public void testHardDeleteSameObjectIDSucceedsOtherObjectIDFails() throws ProcessInitializationException,
    DataAccessObjectException {
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
        final Map<String, String> configMap = getTestConfig(OperationInfo.hard_delete, deleteFn, false);
        return configMap;
    }

}
