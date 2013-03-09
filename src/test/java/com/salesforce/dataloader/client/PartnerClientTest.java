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
package com.salesforce.dataloader.client;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.dyna.ObjectField;
import com.salesforce.dataloader.dyna.SforceDynaBean;
import com.salesforce.dataloader.process.ProcessTestBase;
import com.sforce.soap.partner.DeleteResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.UpsertResult;
import com.sforce.soap.partner.fault.LoginFault;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for partner client operations provided with dataloader
 * 
 * @author Lexi Viripaeff
 * @author Alex Warshavsky
 * @since 8.0
 */
public class PartnerClientTest extends ProcessTestBase {

    @Test
    public void testPartnerClientConnect() {
        try {
            PartnerClient client = new PartnerClient(getController());
            assertFalse(getController().getConfig().getBoolean(Config.SFDC_INTERNAL_IS_SESSION_ID_LOGIN));
            boolean connect = client.connect();
            assertTrue(connect);
            assertNotNull(client.getClient());

            client.connect(client.getSession());
            assertTrue(client.getClient().getDisableFeedTrackingHeader().isDisableFeedTracking());
        } catch (ConnectionException e) {
            fail("Failed to connect to sfdc server", e);
        }
    }

    @Test
    public void testPartnerClientNoUserName() throws ConnectionException {
        Config config = getController().getConfig();
        String origUserName = config.getString(Config.USERNAME);
        try {
            config.setValue(Config.USERNAME, "");
            PartnerClient client = new PartnerClient(getController());
            boolean connect = client.connect();
            assertFalse("Should not connect with empty username", connect);
        } catch (RuntimeException e) {
            //make sure we get the right error message that mentions the username
            assertTrue(e.getMessage().contains(Config.USERNAME));
        } finally {
            config.setValue(Config.USERNAME, origUserName);
        }
    }

    @Test
    public void testPartnerClientSfdcInternalSessionIdConnect() throws Exception {
        Config config = getController().getConfig();

        final String origUsername = config.getString(Config.USERNAME);
        final String origPassword = config.getString(Config.PASSWORD);
        final String origEndpoint = config.getString(Config.ENDPOINT);

        //login normally just to get sessionId and endpoint
        PartnerClient setupOnlyClient = new PartnerClient(getController());
        setupOnlyClient.connect();
        final String sessionId = setupOnlyClient.getSessionId();
        final String endpoint = setupOnlyClient.getSession().getServer();
        setupOnlyClient.disconnect();

        try {
            config.setValue(Config.USERNAME, "");
            config.setValue(Config.PASSWORD, "");

            config.setValue(Config.SFDC_INTERNAL, true);
            config.setValue(Config.SFDC_INTERNAL_IS_SESSION_ID_LOGIN, true);
            config.setValue(Config.ENDPOINT, endpoint);
            config.setValue(Config.SFDC_INTERNAL_SESSION_ID, sessionId);

            PartnerClient client = new PartnerClient(getController());
            assertTrue(client.connect());
        } finally {
            config.setValue(Config.USERNAME, origUsername);
            config.setValue(Config.PASSWORD, origPassword);
            config.setValue(Config.ENDPOINT, origEndpoint);
            config.setValue(Config.SFDC_INTERNAL, false);
            config.setValue(Config.SFDC_INTERNAL_IS_SESSION_ID_LOGIN, false);
            config.setValue(Config.SFDC_INTERNAL_SESSION_ID, "");
        }
    }

    @Test
    public void testPartnerClientSfdcInternalSessionIdWithoutSfdcInternalConnect() throws Exception {
        Config config = getController().getConfig();

        final String origUsername = config.getString(Config.USERNAME);
        final String origPassword = config.getString(Config.PASSWORD);
        final String origEndpoint = config.getString(Config.ENDPOINT);

        //login normally just to get sessionId and endpoint
        PartnerClient setupOnlyClient = new PartnerClient(getController());
        setupOnlyClient.connect();
        final String sessionId = setupOnlyClient.getSessionId();
        final String endpoint = setupOnlyClient.getSession().getServer();
        setupOnlyClient.disconnect();

        try {
            config.setValue(Config.USERNAME, "");
            config.setValue(Config.PASSWORD, "");

            config.setValue(Config.SFDC_INTERNAL, false);
            config.setValue(Config.SFDC_INTERNAL_IS_SESSION_ID_LOGIN, true);
            config.setValue(Config.ENDPOINT, endpoint);
            config.setValue(Config.SFDC_INTERNAL_SESSION_ID, sessionId);

            PartnerClient client = new PartnerClient(getController());
            client.connect();
            Assert.fail("Should not be able to connect with sfdcInternal=false and no username.");
        } catch (IllegalStateException e) {
            assertEquals(
                    "Wrong error messsage",
                    "Empty salesforce.com username specified.  Please make sure that parameter sfdc.username is set to correct username.",
                    e.getMessage());
        } finally {
            config.setValue(Config.USERNAME, origUsername);
            config.setValue(Config.PASSWORD, origPassword);
            config.setValue(Config.ENDPOINT, origEndpoint);
            config.setValue(Config.SFDC_INTERNAL, false);
            config.setValue(Config.SFDC_INTERNAL_IS_SESSION_ID_LOGIN, false);
            config.setValue(Config.SFDC_INTERNAL_SESSION_ID, "");
        }
    }

    @Test
    public void testIsSessionValidAlwaysTrueForSessionIdLogin() throws Exception {
        Config config = getController().getConfig();

        try {
            config.setValue(Config.SFDC_INTERNAL, true);
            config.setValue(Config.SFDC_INTERNAL_IS_SESSION_ID_LOGIN, true);

            PartnerClient client = new PartnerClient(getController());
            assertTrue(client.isSessionValid());
        } finally {
            config.setValue(Config.SFDC_INTERNAL, false);
            config.setValue(Config.SFDC_INTERNAL_IS_SESSION_ID_LOGIN, false);
        }
    }

    @Test
    public void testDisconnect() throws Exception {
        PartnerClient client = new PartnerClient(getController());

        client.connect();
        assertTrue(client.isLoggedIn());

        client.disconnect();
        assertFalse(client.isLoggedIn());
    }

    @Test
    public void testSetEntityDescribe() {
        PartnerClient client = new PartnerClient(getController());
        try {
            assertTrue(client.setEntityDescribes());
        } catch (ConnectionException e) {
            fail(e);
        }
        assertNotNull(client.getDescribeGlobalResults());
        assertEquals(client.getEntityTypes().getSobjects().length, client
                .getDescribeGlobalResults().size());
    }

    @Test
    public void testDescribeSObjects() {
        PartnerClient client = new PartnerClient(getController());
        assertTrue(client.getEntityDescribeMap().isEmpty());
        try {
            client.setEntityDescribes();
        } catch (ConnectionException e) {
            fail(e);
        }
        int numDescribes = 0;
        for (String objectType : client.getDescribeGlobalResults().keySet()){
            try {
                DescribeSObjectResult describeResult = client.describeSObject(objectType);
                numDescribes++;
                assertNotNull(describeResult);
                assertEquals(objectType, describeResult.getName());
            } catch (ConnectionException e) {
                fail(e);
            }
            assertEquals(numDescribes, client.getEntityDescribeMap().size());
        }
    }

    @Test
    public void testSetFieldTypes() {
        try {
            PartnerClient client = new PartnerClient(getController());
            client.setFieldTypes();
            assertNotNull(client.getFieldTypes());
        } catch (ConnectionException e) {
            fail(e);
        }
    }

    @Test
    public void testGetSforceField() {
        // test for account name as a default test case
        try {
            PartnerClient client = new PartnerClient(getController());
            DescribeSObjectResult forceFields = client.describeSObject("account");
            Field[] fields = forceFields.getFields();
            assertNotNull(fields);
            Field f;
            boolean hasName = false;
            for (int i = 0; i < fields.length; i++) {
                f = fields[i];
                if (f.getName().equals("Name")) {
                    hasName = true;
                }
            }
            assertTrue("Account Name not found ", hasName);
        } catch (ConnectionException e) {
            fail(e);
        }

    }

    @Test
    public void testInsertBasic() {
        // setup our dynabeans
        BasicDynaClass dynaClass = null;
        try {
            dynaClass = setupDynaClass("Account");
        } catch (ConnectionException e) {
            fail(e);
        }

        HashMap<String, Object> sforceMapping = new HashMap<String, Object>();
        sforceMapping.put("Name", "name" + System.currentTimeMillis());
        sforceMapping.put("Description", "the description");
        // Account number is set for easier test data cleanup
        sforceMapping.put("AccountNumber", ACCOUNT_NUMBER_PREFIX + System.currentTimeMillis());

        // now convert to a dynabean array for the client
        DynaBean sforceObj = null;
        try {

            sforceObj = dynaClass.newInstance();

            // This does an automatic conversion of types.
            BeanUtils.copyProperties(sforceObj, sforceMapping);
        } catch (Exception e) {
            fail(e);
        }

        List<DynaBean> beanList = new ArrayList<DynaBean>();
        beanList.add(sforceObj);

        // get the client and make the insert call
        try {
            PartnerClient client = new PartnerClient(getController());
            SaveResult[] results = client.loadInserts(beanList);
            for (int i = 0; i < results.length; i++) {
                SaveResult result = results[i];
                if (!result.getSuccess()) {
                    Assert.fail("Insert returned an error: " + result.getErrors()[0].getMessage());
                }
            }
        } catch (ConnectionException e) {
            fail(e);
        }

    }

    @Test
    public void testUpdateBasic() {
        String id = getRandomAccountId();

        // setup our dynabeans
        BasicDynaClass dynaClass = null;
        try {
            dynaClass = setupDynaClass("Account");
        } catch (ConnectionException e) {
            fail(e);
        }

        HashMap<String, Object> sforceMapping = new HashMap<String, Object>();
        sforceMapping.put("Id", id);
        sforceMapping.put("Name", "newname" + System.currentTimeMillis());
        sforceMapping.put("Description", "the new description");
        // Account number is set for easier test data cleanup
        sforceMapping.put("AccountNumber", ACCOUNT_NUMBER_PREFIX + System.currentTimeMillis());

        // now convert to a dynabean array for the client
        DynaBean sforceObj = null;
        try {

            sforceObj = dynaClass.newInstance();

            // This does an automatic conversion of types.
            BeanUtils.copyProperties(sforceObj, sforceMapping);
        } catch (Exception e) {
            fail(e);
        }

        List<DynaBean> beanList = new ArrayList<DynaBean>();
        beanList.add(sforceObj);

        // get the client and make the insert call
        try {
            PartnerClient client = new PartnerClient(getController());
            SaveResult[] results = client.loadUpdates(beanList);
            for (int i = 0; i < results.length; i++) {
                SaveResult result = results[i];
                if (!result.getSuccess()) {
                    Assert.fail("Update returned an error" + result.getErrors()[0].getMessage());
                }
            }
        } catch (ConnectionException e) {
            fail(e);
        }

    }

    /**
     * Basic failing - forgetting the id
     */
    @Test
    public void testUpdateFailBasic() {

        // setup our dynabeans
        BasicDynaClass dynaClass = null;
        try {
            dynaClass = setupDynaClass("Account");
        } catch (ConnectionException e) {
            fail(e);
        }

        HashMap<String, Object> sforceMapping = new HashMap<String, Object>();
        sforceMapping.put("Name", "newname" + System.currentTimeMillis());
        sforceMapping.put("Description", "the new description");
        // Account number is set for easier test data cleanup
        sforceMapping.put("AccountNumber", ACCOUNT_NUMBER_PREFIX + System.currentTimeMillis());

        // now convert to a dynabean array for the client
        DynaBean sforceObj = null;
        try {

            sforceObj = dynaClass.newInstance();

            // This does an automatic conversion of types.
            BeanUtils.copyProperties(sforceObj, sforceMapping);
        } catch (Exception e) {
            fail(e);
        }

        List<DynaBean> beanList = new ArrayList<DynaBean>();
        beanList.add(sforceObj);

        // get the client and make the insert call
        try {
            PartnerClient client = new PartnerClient(getController());
            SaveResult[] results = client.loadUpdates(beanList);
            for (int i = 0; i < results.length; i++) {
                SaveResult result = results[i];
                if (result.getSuccess()) {
                    Assert.fail("Update should not have been a success.");
                }
            }
        } catch (ConnectionException e) {
            fail(e);
        }
    }

    /**
     * Test basic upsert operation
     */
    @Test
    public void testUpsertAccountBasic() {
        doUpsertAccount(false);
    }

    /**
     * Test basic upsert operation
     */
    @Test
    public void testUpsertContactBasic() {
        doUpsertContact(false);
    }

    /**
     * Test basic upsert on foreign key
     */
    @Test
    public void testUpsertAccountFkBasic() {
        doUpsertAccount(true);
    }

    /**
     * Test basic upsert on foreign key
     */
    @Test
    public void testUpsertContactFkBasic() {
        doUpsertContact(true);
    }

    /**
     * Test basic failure to upsert - no external id specified
     */
    @Test
    public void testUpsertFailBasic() {
        doUpsertFailBasic(false);
    }

    /**
     * Test basic failure to upsert on foreign key - no foreign key external id specified (blank value)
     */
    @Test
    public void testUpsertFkFailBasic() {
        doUpsertFailBasic(true);
    }

    private void doUpsertAccount(boolean upsertFk) {
        String origExtIdField = getController().getConfig().getString(Config.EXTERNAL_ID_FIELD);

        try {
            // make sure the external id is set
            String extIdField = setExtIdField(DEFAULT_ACCOUNT_EXT_ID_FIELD);
            Object extIdValue = getRandomExtId("Account", ACCOUNT_WHERE_CLAUSE, null);

            HashMap<String, Object> sforceMapping = new HashMap<String, Object>();
            sforceMapping.put(extIdField, extIdValue);
            sforceMapping.put("Name", "newname" + System.currentTimeMillis());
            sforceMapping.put("Description", "the new description");
            // Account number is set for easier test data cleanup
            sforceMapping.put("AccountNumber", ACCOUNT_NUMBER_PREFIX + System.currentTimeMillis());

            // Add upsert on FK
            if (upsertFk) {
                Object parentExtIdValue = getRandomExtId("Account", ACCOUNT_WHERE_CLAUSE, extIdValue);
                // if there's only one external id on account, do another upsert and get the second external id thus
                // created
                if (parentExtIdValue == null) {
                    doUpsertAccount(false);
                    parentExtIdValue = getRandomExtId("Account", ACCOUNT_WHERE_CLAUSE, extIdValue);
                }
                sforceMapping.put(ObjectField.formatAsString("Parent", extIdField), parentExtIdValue);
            }

            doUpsert("Account", sforceMapping);
        } finally {
            setExtIdField(origExtIdField);
        }
    }

    private void doUpsertContact(boolean upsertFk) {
        String origExtIdField = getController().getConfig().getString(Config.EXTERNAL_ID_FIELD);

        try {
            // make sure the external id is set
            String extIdField = setExtIdField(DEFAULT_CONTACT_EXT_ID_FIELD);
            Object extIdValue = getRandomExtId("Contact", CONTACT_WHERE_CLAUSE, null);

            HashMap<String, Object> sforceMapping = new HashMap<String, Object>();
            sforceMapping.put(extIdField, extIdValue);
            sforceMapping.put("FirstName", "newFirstName" + System.currentTimeMillis());
            sforceMapping.put("LastName", "newLastName" + System.currentTimeMillis());
            // Title is set for easier test data cleanup
            sforceMapping.put("Title", CONTACT_TITLE_PREFIX + System.currentTimeMillis());

            // Add upsert on FK -- reference to an account
            if (upsertFk) {
                // remember original ext id field
                String oldExtIdField = getController().getConfig().getString(Config.EXTERNAL_ID_FIELD);

                String acctExtIdField = setExtIdField(DEFAULT_ACCOUNT_EXT_ID_FIELD);
                Object accountExtIdValue = getRandomExtId("Account", ACCOUNT_WHERE_CLAUSE, null);
                // if there's only one external id on account, do another upsert and get the second external id thus
                // created
                if (accountExtIdValue == null) {
                    doUpsertAccount(false);
                    accountExtIdValue = getRandomExtId("Account", ACCOUNT_WHERE_CLAUSE, accountExtIdValue);
                }
                sforceMapping.put(ObjectField.formatAsString("Account", acctExtIdField), accountExtIdValue);

                // restore ext id field
                setExtIdField(oldExtIdField);
            }

            doUpsert("Contact", sforceMapping);

        } finally {
            setExtIdField(origExtIdField);
        }
    }

    /**
     * @param sforceMapping
     */
    private void doUpsert(String entity, HashMap<String, Object> sforceMapping) {
        DynaBean sforceObj = null;
        try {
            // now convert to a dynabean array for the client
            // setup our dynabeans
            BasicDynaClass dynaClass = null;
            try {
                dynaClass = setupDynaClass(entity);
            } catch (ConnectionException e) {
                fail(e);
            }

            sforceObj = dynaClass.newInstance();

            // This does an automatic conversion of types.
            BeanUtils.copyProperties(sforceObj, sforceMapping);
        } catch (Exception e) {
            fail(e);
        }

        List<DynaBean> beanList = new ArrayList<DynaBean>();
        beanList.add(sforceObj);

        // get the client and make the insert call
        try {
            PartnerClient client = new PartnerClient(getController());
            UpsertResult[] results = client.loadUpserts(beanList);
            for (UpsertResult result : results) {
                if (!result.getSuccess()) {
                    Assert.fail("Upsert returned an error: " + result.getErrors()[0].getMessage());
                }
            }
        } catch (ConnectionException e) {
            fail(e);
        }
    }

    /**
     * Basic failing - forgetting the external id or foreign key external id
     */
    private void doUpsertFailBasic(boolean upsertFk) {

        // setup our dynabeans
        BasicDynaClass dynaClass = null;
        try {
            dynaClass = setupDynaClass("Account");
        } catch (ConnectionException e) {
            fail(e);
        }

        HashMap<String, Object> sforceMapping = new HashMap<String, Object>();
        sforceMapping.put("Name", "newname" + System.currentTimeMillis());
        sforceMapping.put("Description", "the new description");
        // Account number is set for easier test data cleanup
        sforceMapping.put("AccountNumber", ACCOUNT_NUMBER_PREFIX + System.currentTimeMillis());

        // Add FAILURE for upsert on FK.
        String extIdField = setExtIdField(DEFAULT_ACCOUNT_EXT_ID_FIELD);
        Object extIdValue = getRandomExtId("Account", ACCOUNT_WHERE_CLAUSE, null);
        if (upsertFk) {
            sforceMapping.put(extIdField, extIdValue);
            // forget to set the foreign key external id value
            sforceMapping.put(ObjectField.formatAsString("Parent", extIdField), "bogus");
        }

        // now convert to a dynabean array for the client
        DynaBean sforceObj = null;
        try {

            sforceObj = dynaClass.newInstance();

            // This does an automatic conversion of types.
            BeanUtils.copyProperties(sforceObj, sforceMapping);
        } catch (Exception e) {
            fail(e);
        }

        List<DynaBean> beanList = new ArrayList<DynaBean>();
        beanList.add(sforceObj);

        try {
            PartnerClient client = new PartnerClient(getController());
            UpsertResult[] results = client.loadUpserts(beanList);
            for (UpsertResult result : results) {
                if (result.getSuccess()) {
                    Assert.fail("Upsert should not have been a success.");
                }
            }
        } catch (ConnectionException e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteBasic() {
        String id = getRandomAccountId();

        // setup our dynabeans
        BasicDynaClass dynaClass = null;
        try {
            dynaClass = setupDynaClass("Account");
        } catch (ConnectionException e) {
            fail(e);
        }

        HashMap<String, Object> sforceMapping = new HashMap<String, Object>();
        sforceMapping.put("Id", id);
        sforceMapping.put("Name", "name" + System.currentTimeMillis());
        sforceMapping.put("Description", "the description");
        // Account number is set for easier test data cleanup
        sforceMapping.put("AccountNumber", ACCOUNT_NUMBER_PREFIX + System.currentTimeMillis());

        // now convert to a dynabean array for the client
        DynaBean sforceObj = null;
        try {

            sforceObj = dynaClass.newInstance();

            // This does an automatic conversion of types.
            BeanUtils.copyProperties(sforceObj, sforceMapping);
        } catch (Exception e) {
            fail(e);
        }

        List<DynaBean> beanList = new ArrayList<DynaBean>();
        beanList.add(sforceObj);

        // get the client and make the insert call
        try {
            PartnerClient client = new PartnerClient(getController());
            DeleteResult[] results = client.loadDeletes(beanList);
            for (int i = 0; i < results.length; i++) {
                DeleteResult result = results[i];
                if (!result.getSuccess()) {
                    Assert.fail("Delete returned an error: " + result.getErrors()[0].getMessage());
                }
            }
        } catch (ConnectionException e) {
            fail(e);
        }
    }

    /**
     * Test a delete missing the id
     */
    @Test
    public void testDeleteFailBasic() {

        // setup our dynabeans
        BasicDynaClass dynaClass = null;
        try {
            dynaClass = setupDynaClass("Account");
        } catch (ConnectionException e) {
            fail(e);
        }

        HashMap<String, Object> sforceMapping = new HashMap<String, Object>();
        sforceMapping.put("name", "name" + System.currentTimeMillis());
        sforceMapping.put("description", "the description");
        // Account number is set for easier test data cleanup
        sforceMapping.put("AccountNumber", ACCOUNT_NUMBER_PREFIX + System.currentTimeMillis());

        // now convert to a dynabean array for the client
        DynaBean sforceObj = null;
        try {

            sforceObj = dynaClass.newInstance();

            // This does an automatic conversion of types.
            BeanUtils.copyProperties(sforceObj, sforceMapping);
        } catch (Exception e) {
            fail(e);
        }

        List<DynaBean> beanList = new ArrayList<DynaBean>();
        beanList.add(sforceObj);

        // get the client and make the insert call
        try {
            PartnerClient client = new PartnerClient(getController());
            DeleteResult[] results = client.loadDeletes(beanList);
            for (int i = 0; i < results.length; i++) {
                DeleteResult result = results[i];
                if (result.getSuccess()) {
                    Assert.fail("Delete should have returned an error");
                }
            }
        } catch (ConnectionException e) {
            fail(e);
        }
    }

    @Test
    public void testQueryBasic() {
        // make sure there're some records to test with
        upsertSfdcAccounts(10);

        // get the client and make the query call
        try {
            PartnerClient client = new PartnerClient(getController());
            QueryResult result = client.query("select id from account where " + ACCOUNT_WHERE_CLAUSE);
            SObject[] records = result.getRecords();
            assertNotNull(records);
            assertTrue(records.length > 0);

            // test query more if we have more records
            if (!result.getDone()) {
                QueryResult result2 = client.queryMore(result.getQueryLocator());

                // if we are not done, we should get some records back
                assertNotNull(result2.getRecords());
                assertTrue(records.length > 0);
            }

        } catch (ConnectionException e) {
            fail(e);
        }

    }

    /**
     * Get a random acount id for delete and update testing
     * 
     * @return String account id
     */
    private String getRandomAccountId() {
        // make sure there're some records to get
        upsertSfdcAccounts(10);

        String id = "";

        // get the client and make the query call
        try {
            PartnerClient client = new PartnerClient(getController());
            QueryResult result = client.query("select id from account where " + ACCOUNT_WHERE_CLAUSE);
            SObject[] records = result.getRecords();
            assertNotNull(records);
            assertTrue(records.length > 0);

            id = records[0].getId().toString();
        } catch (ConnectionException e) {
            fail(e);
        }

        return id;
    }

    /**
     * Make sure to set external id field
     */
    private String setExtIdField(String extIdField) {
        getController().getConfig().setValue(Config.EXTERNAL_ID_FIELD, extIdField);
        return extIdField;
    }

    /**
     * Get a random account external id for upsert testing
     * 
     * @param entity
     *            TODO
     * @param whereClause
     *            TODO
     * @param prevValue
     *            Indicate that the value should be different from the specified
     *            value or null if uniqueness not required
     * @return String Account external id value
     */
    private Object getRandomExtId(String entity, String whereClause, Object prevValue) {
        Object extIdValue;

        // insert couple of accounts so there're at least two records to work with
        upsertSfdcRecords(entity, 2);

        // get the client and make the query call
        try {
            String extIdField = getController().getConfig().getString(Config.EXTERNAL_ID_FIELD);
            PartnerClient client = new PartnerClient(getController());
            // only get the records that have external id set, avoid nulls
            String soql = "select " + extIdField + " from " + entity + " where " + whereClause + " and " + extIdField
                    + " != null";
            if (prevValue != null) {
                soql += " and "
                        + extIdField
                        + "!= "
                        + (prevValue.getClass().equals(String.class) ? ("'" + prevValue + "'") : String
                                .valueOf(prevValue));
            }
            QueryResult result = client.query(soql);
            SObject[] records = result.getRecords();
            assertNotNull("Operation should return non-null values", records);
            assertTrue("Operation should return 1 or more records", records.length > 0);
            assertNotNull("Records should have non-null field: " + extIdField + " values", records[0]
                    .getField(extIdField));

            extIdValue = records[0].getField(extIdField);

            return extIdValue;
        } catch (ConnectionException e) {
            fail(e);
        }
        return null;
    }

    private BasicDynaClass setupDynaClass(String entity) throws ConnectionException {
        getController().getConfig().setValue(Config.ENTITY, entity);
        PartnerClient client = getController().getPartnerClient();
        if (!client.isLoggedIn()) {
            try {
                client.connect();
            } catch (LoginFault e) {
                fail(e);
            } catch (ConnectionException e) {
                fail(e);
            }
        }

        getController().setFieldTypes();
        getController().setReferenceDescribes();
        DynaProperty[] dynaProps;
        dynaProps = SforceDynaBean.createDynaProps(getController().getPartnerClient().getFieldTypes(), getController());
        BasicDynaClass dynaClass = SforceDynaBean.getDynaBeanInstance(dynaProps);
        SforceDynaBean.registerConverters(getController().getConfig());
        return dynaClass;
    }

}
