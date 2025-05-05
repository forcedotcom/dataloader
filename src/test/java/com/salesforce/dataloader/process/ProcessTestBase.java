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

import java.io.*;
import java.util.*;

import org.apache.logging.log4j.Logger;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import com.salesforce.dataloader.util.DLLogManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.salesforce.dataloader.*;
import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.client.LoginClient;
import com.salesforce.dataloader.client.PartnerClient;
import com.salesforce.dataloader.client.transport.HttpTransportImpl;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataAccessObjectFactory;
import com.salesforce.dataloader.dao.csv.CSVFileReader;
import com.salesforce.dataloader.dao.csv.CSVFileWriter;
import com.salesforce.dataloader.dyna.SforceDynaBean;
import com.salesforce.dataloader.exception.*;
import com.salesforce.dataloader.exception.UnsupportedOperationException;
import com.salesforce.dataloader.model.RowInterface;
import com.salesforce.dataloader.model.TableRow;
import com.salesforce.dataloader.util.AppUtil;
import com.salesforce.dataloader.util.Base64;
import com.salesforce.dataloader.action.progress.NihilistProgressAdapter;
import com.sforce.soap.partner.*;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.util.FileUtil;

/**
 * Base class for batch process tests
 *
 * @author Alex Warshavsky
 * @since 8.0
 */
public abstract class ProcessTestBase extends ConfigTestBase {

    private static Logger logger = DLLogManager.getLogger(ProcessTestBase.class);
    private int serverApiInvocationThreshold = 150;
    private long usedMemoryBefore = 0;
    Runtime runtime = Runtime.getRuntime();
    private static final long MEMORY_INCREASE_THRESHOLD_IN_MB_FOR_LOGGING = 2;
    private static final long DEFAULT_MEMORY_INCREASE_THRESHOLD_IN_MB_FOR_FAILING = 20;
    private long memoryIncreaseThresholdInMbForFailing = DEFAULT_MEMORY_INCREASE_THRESHOLD_IN_MB_FOR_FAILING;
    private long memoryIncreaseThresholdInMbForLogging = MEMORY_INCREASE_THRESHOLD_IN_MB_FOR_LOGGING;

    @Before
    public void setUpMemoryProfiling() throws Exception {
        AppUtil.enableUsedHeapCapture(true);
        System.gc();
        usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
    }

    @After
    public void tearDownMemoryProfiling() throws Exception {
        System.gc();
        long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncreaseAfterUseInMb = (usedMemoryAfter - usedMemoryBefore)/1024/1024;
        if (memoryIncreaseAfterUseInMb > memoryIncreaseThresholdInMbForLogging) {
            logger.warn("memory increase after use: " + memoryIncreaseAfterUseInMb + "Mb");
        }
        assertTrue("potential memory leak", memoryIncreaseAfterUseInMb < memoryIncreaseThresholdInMbForFailing);
        AppUtil.enableUsedHeapCapture(false);
    }
    
    protected void setMemoryIncreaseThresholds(int expectedIncreaseInMb) {
        // warn if increase more than 5%
        memoryIncreaseThresholdInMbForLogging = (long) (expectedIncreaseInMb * 1.05);
        // fail if increase more than 20%
        memoryIncreaseThresholdInMbForFailing = (long) (expectedIncreaseInMb * 1.2);
    }
    protected ProcessTestBase() {
        super(Collections.<String, String>emptyMap());
        HttpTransportImpl.resetServerInvocationCount();
    }

    protected ProcessTestBase(Map<String, String> config) {
        super(config);
        HttpTransportImpl.resetServerInvocationCount();
    }

    protected void verifyErrors(Controller controller, String expectedErrorMessage) throws DataAccessObjectException {
        String fileName = controller.getAppConfig().getString(AppConfig.PROP_OUTPUT_ERROR);
        final CSVFileReader errReader = new CSVFileReader(new File(fileName), getController().getAppConfig(), true, false);
        try {
            errReader.open();
            for (TableRow errorRow : errReader.readTableRowList(errReader.getTotalRows())) {
                String actualMessage = (String) errorRow.get("ERROR");
                if (actualMessage == null || !actualMessage.startsWith(expectedErrorMessage))
                    Assert.fail("Error row does not have the expected error message: " + expectedErrorMessage
                            + "\n  Actual row: " + errorRow);
            }
        } finally {
            errReader.close();
        }
    }

    protected void verifySuccessIds(Controller theController, String[] ids) throws DataAccessObjectException {
        verifySuccessIds(theController, new HashSet<String>(Arrays.asList(ids)));
    }

    protected void verifySuccessIds(Controller ctl, Set<String> ids) throws DataAccessObjectException {
        String fileName = ctl.getAppConfig().getString(AppConfig.PROP_OUTPUT_SUCCESS);
        final CSVFileReader successRdr = new CSVFileReader(new File(fileName), ctl.getAppConfig(), true, false);
        final Set<String> remaining = new HashSet<String>(ids);
        final Set<String> unexpected = new HashSet<String>();
        try {
            for (TableRow row : successRdr.readTableRowList(Integer.MAX_VALUE)) {
                final String rowid = (String) row.get("ID");
                if (rowid != null && rowid.length() > 0 && !remaining.remove(rowid)) unexpected.add(rowid);
            }
        } finally {
            successRdr.close();
        }

        if (!remaining.isEmpty()) Assert.fail("Ids not found: " + remaining);
        if (!unexpected.isEmpty()) Assert.fail("Unexpected ids found: " + unexpected);
    }

    /**
     * Upsert numRecords and return an array of id's
     *
     * @param numRecords
     * @return String[] upserted id's
     */
    protected String[] upsertSfdcRecords(String entityName, int numRecords) {
        if (entityName.equalsIgnoreCase("Account")) {
            return upsertSfdcAccounts(numRecords);
        } else if (entityName.equalsIgnoreCase("Contact")) {
            return upsertSfdcContacts(numRecords);
        } else {
            throw new IllegalArgumentException("Unexpected entity name: "
                    + entityName);
        }
    }

    /**
     * Upsert numAccounts accounts and return an array of account id's
     *
     * @param numAccounts
     * @return String[] upserted id's
     */
    protected String[] upsertSfdcAccounts(int numRecords) {
        return upsertSfdcAccounts(numRecords, 0);
    }

    /**
     * Upsert numRecords contacts and return an array of contact id's
     *
     * @param numRecords
     * @return String[] upserted id's
     */
    protected String[] upsertSfdcContacts(int numRecords) {
        return saveSfdcRecords(numRecords, 0, false/* not insert */, true/*
         * ignore
         * output
         */,
                false/* not negative test */, new ContactGenerator());
    }

    /**
     * Upsert numAccounts accounts and return an array of account id's
     *
     * @param numRecords
     * @param startingSeq
     * @return String[] upserted id's
     */
    protected String[] upsertSfdcAccounts(int numRecords, int startingSeq) {
        return saveSfdcRecords(numRecords, startingSeq, false/* not insert */,
                true/* ignore output */, false/* not negative test */,
                new AccountGenerator());
    }

    /**
     * Upsert numAccounts BAD accounts -- missing required data -- and return an
     * array of account id's
     *
     * @param numRecords
     * @param startingSeq
     * @return String[] upserted id's
     */
    protected String[] upsertBadSfdcAccounts(int numRecords, int startingSeq) {
        return saveSfdcRecords(numRecords, startingSeq, false/* not insert */,
                true/* ignore output */, true/* negative test */,
                new AccountGenerator());
    }

    /**
     * Insert numAccounts accounts and return an array of account id's
     *
     * @param numAccounts
     * @param ignoreOutput
     * @return String[] inserted id's
     */
    protected String[] insertSfdcAccounts(int numAccounts, boolean ignoreOutput) {
        return insertSfdcRecords(numAccounts, ignoreOutput, new AccountGenerator());
    }

    /**
     * Insert numContacts contacts and return an array of contact id's
     *
     * @param numAccounts
     * @param ignoreOutput
     * @return String[] inserted id's
     *
     */
    protected String[] insertSfdcContacts(int numContacts, boolean ignoreOutput) {
        return insertSfdcRecords(numContacts, ignoreOutput, new ContactGenerator());
    }

    protected String[] insertSfdcRecords(int numObjects, boolean ignoreOutput, SObjectGenerator sObjectGen) {
        return saveSfdcRecords(numObjects, 0, true, ignoreOutput, false, sObjectGen);
    }

    /**
     * Insert numAccounts accounts and return an array of account id's
     *
     * @param numAccounts
     * @param startingSeq
     * @param ignoreOutput
     * @return String[] inserted id's
     */
    protected String[] insertSfdcAccounts(int numAccounts, int startingSeq,
                                          boolean ignoreOutput) {
        return saveSfdcRecords(numAccounts, startingSeq, true, ignoreOutput,
                false, new AccountGenerator());
    }

    private String[] saveSfdcRecords(int numRecords, int startingSeq, boolean insert, boolean ignoreOutput,
            boolean negativeTest, SObjectGenerator sObjectGen) {
        // there're only SAVE_RECORD_LIMIT records allowed for this operation,
        // need to upsert records in batches and save
        // all results for the caller as an array of id's
        if (numRecords < SAVE_RECORD_LIMIT) {
            SObject[] records = getSObjects(numRecords, startingSeq, negativeTest, sObjectGen);
            if (insert) {
                logger.info("Inserting " + numRecords + " total " + sObjectGen.getEntityName() + "s");
                return insertSfdcRecords(records, ignoreOutput, 0);
            } else {
                logger.info("Upserting " + numRecords + " total " + sObjectGen.getEntityName() + "s");
                return upsertSfdcRecords(records, ignoreOutput, 0);
            }
        }

        String[] ids;
        if (ignoreOutput) {
            ids = new String[1];
        } else {
            ids = new String[numRecords];
        }

        if (insert) {
            logger.info("Inserting " + numRecords + " total "
                    + sObjectGen.getEntityName() + "s");
        } else {
            logger.info("Upserting " + numRecords + " total "
                    + sObjectGen.getEntityName() + "s");
        }
        List<SObject> recordsToSave = new ArrayList<SObject>();
        for (int i = 0; i < numRecords; i++) {

            // fill the array to use for operation
            recordsToSave.add(sObjectGen
                    .getObject(i + startingSeq, negativeTest));

            // when SAVE_RECORD_LIMIT records in a current batch or total number
            // of records are reached
            // do the upsert and optionally save the record ids
            if (i > 0 && (i + 1) % SAVE_RECORD_LIMIT == 0
                    || i == numRecords - 1) {
                String[] savedIds;
                if (insert) {
                    savedIds = insertSfdcRecords(recordsToSave
                            .toArray(new SObject[] {}), ignoreOutput, 0);
                    logger.info("Inserted " + (i + 1) + " of " + numRecords
                            + " total " + sObjectGen.getEntityName()
                            + "s into SFDC");
                } else {
                    savedIds = upsertSfdcRecords(recordsToSave
                            .toArray(new SObject[] {}), ignoreOutput, 0);
                    logger.info("Upserted " + (i + 1) + " of " + numRecords
                            + " total " + sObjectGen.getEntityName()
                            + "s into SFDC");
                }
                if (!ignoreOutput) {
                    for (int j = 0; j < savedIds.length; j++) {
                        ids[i] = savedIds[j];
                    }
                }
                // get new array of records
                recordsToSave.clear();
            }
        }
        return ids;
    }

    private String[] insertSfdcRecords(SObject[] records, boolean ignoreOutput,
            int retries) {
        // get the client and make the insert call
        try {
            SaveResult[] results = getBinding().create(records);
            String[] ids = new String[results.length];
            for (int i = 0; i < results.length; i++) {
                SaveResult result = results[i];
                if (!result.getSuccess()) {
                    Assert.fail("Insert returned an error: "
                            + result.getErrors()[0].getMessage());
                } else {
                    ids[i] = result.getId();
                }
            }
            if (ignoreOutput) {
                return new String[1];
            } else {
                return ids;
            }
        } catch (ApiFault e) {
            if (checkBinding(++retries, e) != null) {
                insertSfdcRecords(records, ignoreOutput, retries);
            }
            Assert.fail("Error inserting records: " + e.getExceptionMessage());
        } catch (ConnectionException e) {
            Assert.fail("Error inserting records: " + e.getMessage());
        }
        return null; // make eclipse happy, shouldn't reach this point after
        // fail()
    }

    private String[] upsertSfdcRecords(SObject[] records, boolean ignoreOutput,
            int retries) {
        // get the client and make the insert call
        try {
            UpsertResult[] results = getBinding().upsert(
                    getController().getAppConfig().getString(
                            AppConfig.PROP_IDLOOKUP_FIELD), records);
            String[] ids = new String[results.length];
            for (int i = 0; i < results.length; i++) {
                UpsertResult result = results[i];
                if (!result.getSuccess()) {
                    Assert.fail("Upsert returned an error: "
                            + result.getErrors()[0].getMessage());
                } else {
                    ids[i] = result.getId();
                }
            }
            if (ignoreOutput) {
                return new String[1];
            } else {
                return ids;
            }
        } catch (ApiFault e) {
            if (checkBinding(++retries, e) != null) {
                upsertSfdcRecords(records, ignoreOutput, retries);
            }
            Assert.fail("Error upserting records: " + e.getExceptionMessage());
        } catch (ConnectionException e) {
            Assert.fail("Error upserting records: " + e.getMessage());
        }
        return null; // make eclipse happy, shouldn't reach this point after
        // fail()
    }

    /**
     * @param numRecords
     * @return Array of SObjects
     */
    private static SObject[] getSObjects(int numRecords, int startingSeq,
            boolean negativeTest, SObjectGenerator sObjectGen) {
        SObject[] sobjects = new SObject[numRecords];
        for (int i = 0; i < numRecords; i++) {
            SObject sobj = sObjectGen.getObject(i + startingSeq, negativeTest);
            sobjects[i] = sobj;
        }
        return sobjects;
    }

    protected static interface SObjectGenerator {
        SObject getObject(int i, boolean negativeTest);

        String getEntityName();

        String getSOQL(String selectExpr);
    }

    protected static abstract class AbstractSObjectGenerator implements SObjectGenerator {

        protected SObject createSObject() {
            SObject obj = new SObject();
            obj.setType(getEntityName());
            return obj;
        }

        protected final String generateSOQL(String selectExpression, String... filters) {
            String soql = "SELECT " + selectExpression + " FROM " + getEntityName();
            String delim = " WHERE ";
            if (filters != null) {
                for (String filter : filters) {
                    soql = soql + delim + filter;
                    delim = " AND ";
                }
            }
            return soql;
        }
    }

    protected static class AccountGenerator extends AbstractSObjectGenerator {
        /**
         * @param i
         * @return SObject account
         */
        @Override
        public SObject getObject(int i, boolean negativeTest) {
            String seqStr = String.format("%06d", i);
            SObject account = createSObject();
            account.setField("Name", "account insert#" + seqStr);
            String accountNumberValue = ACCOUNT_NUMBER_PREFIX + seqStr;
            if (negativeTest) {
                // dataloader test database doesn't access long account numbers
                // (longer than 20 chars)
                accountNumberValue = accountNumberValue
                        + "extraextraextraextraextraLongAccountNumber";
            }
            account.setField("AccountNumber__c", accountNumberValue);
            account.setField("AnnualRevenue", (double) 1000 * i);
            int remainder = i % 5;
            switch (remainder) {
                case 0:
                    account.setField("Phone", "+1415555" + seqStr);
                    break;
                case 1:
                    account.setField("Phone", "415555" + seqStr);
                    break;
                case 2:
                    account.setField("Phone", "1415555" + seqStr);
                    break;
                case 3:  // length less than 10
                    account.setField("Phone", "14155" + seqStr);
                    break;
                default:
                    account.setField("Phone", "141555567" + seqStr);
                    break;
            }
            account.setField("WebSite", "http://www.accountInsert" + seqStr
                    + ".com");
            account.setField(DEFAULT_ACCOUNT_EXT_ID_FIELD, "1-" + seqStr);
            account.setField("NumberOfEmployees", i);
            return account;
        }

        /*
         * (non-Javadoc)
         *
         * @seecom.salesforce.dataloader.process.ProcessTestBase.SObjectGetter#
         * getEntityName()
         */
        @Override
        public String getEntityName() {
            return "Account";
        }

        @Override
        public String getSOQL(String selectExpr) {
            return generateSOQL(selectExpr, ACCOUNT_WHERE_CLAUSE);
        }
    }

    protected static class ContactGenerator extends AbstractSObjectGenerator {
        /**
         * @param i
         * @return SObject contact
         */
        @Override
        public SObject getObject(int i, boolean negativeTest) {
            String seqStr = String.format("%06d", i);
            SObject contact = createSObject();
            contact.setField("FirstName", "First " + seqStr);
            contact.setField("LastName", "Last " + seqStr);
            String titleValue = CONTACT_TITLE_PREFIX + seqStr;
            if (negativeTest) {
                titleValue = titleValue
                        + "extraextraextraextraextraextraLoongTitleextraextraextraextraextraextraLoongTitleextraextraextraextraextraextraLoongTitle";
            }
            contact.setField("Title", titleValue);
            contact.setField("Phone", "415-555-" + seqStr);
            contact.setField(DEFAULT_CONTACT_EXT_ID_FIELD, (double) i);
            contact.setField("Email", "contact"+i+"@testcustomer.com");
            return contact;
        }

        /*
         * (non-Javadoc)
         *
         * @seecom.salesforce.dataloader.process.ProcessTestBase.SObjectGetter#
         * getEntityName()
         */
        @Override
        public String getEntityName() {
            return "Contact";
        }

        @Override
        public String getSOQL(String selectFields) {
            return generateSOQL(selectFields, CONTACT_WHERE_CLAUSE);
        }
    }

    protected static class TestFieldGenerator extends AbstractSObjectGenerator {
        /**
         * @param i
         * @return SObject contact
         */
        @Override
        public SObject getObject(int i, boolean negativeTest) {
            String seqStr = String.format("%06d", i);
            SObject testField = createSObject();
            testField.setField("Name", TESTFIELD_FIELD_PREFIX + seqStr);
            testField.setField("TestField__c", TESTFIELD_FIELD_PREFIX + seqStr);
            return testField;
        }

        /*
         * (non-Javadoc)
         *
         * @seecom.salesforce.dataloader.process.ProcessTestBase.SObjectGetter#
         * getEntityName()
         */
        @Override
        public String getEntityName() {
            return "TestField__c";
        }

        @Override
        public String getSOQL(String selectFields) {
            return generateSOQL(selectFields, TESTFIELD_WHERE_CLAUSE);
        }
    }

    protected static interface TemplateListener {
        void updateRow(int idx, TableRow row);
    }

    /**
     * Listener which fills in pre-created account ids for template rows. This is used for testing delete, hard delete,
     * update, and upsert.
     */
    protected class AccountIdTemplateListener implements TemplateListener {
        private final String[] accountIds;

        public AccountIdTemplateListener(int numAccounts) {
            this.accountIds = insertSfdcAccounts(numAccounts, false);
        }

        @Override
        public void updateRow(int idx, TableRow row) {
            row.put("ID", idx < this.accountIds.length ? this.accountIds[idx] : "");
            for (String key : row.getHeader().getColumns()) {
                if ("standard@org.com".equals(row.get(key))) {
                    row.put(key, getProperty("test.user.restricted"));
                }
            }
        }

        public String[] getAccountIds() {
            return this.accountIds;
        }
    }

    /**
     * Inserts the records specified in the template file and writes the
     * inserted ids into the input csv file. Constructs the input file from the
     * template file.
     *
     * @param templateFileName
     * @param inputFileName
     * @param updateColName
     * @param setIds
     * @return String path to the input file path
     */
    protected String convertTemplateToInput(String templateFileName, String inputFileName,
            TemplateListener... listeners) throws DataAccessObjectException {

        String fileName = new File(getTestDataDir(), templateFileName).getAbsolutePath();
        final CSVFileReader templateReader = new CSVFileReader(new File(fileName), getController().getAppConfig(), true, false);
        try {
            templateReader.open();

            int numRows = templateReader.getTotalRows();
            final List<TableRow> templateRows = templateReader.readTableRowList(numRows);
            assertNotNull("CVSReader returned a null list of rows, but expected a list with size " + numRows,
                    templateRows);
            final List<RowInterface> inputRows = new ArrayList<RowInterface>(templateRows.size());

            // verify that the template file is useable
            assertEquals("Wrong number of rows were read using readRowList while attempting to convert template file: "
                    + templateFileName, numRows, templateRows.size());

            // insert accounts for the whole template or part of it if
            // maxInserts is smaller then template size
            int idx = 0;
            for (TableRow templateRow : templateRows) {
                final TableRow row = new TableRow(templateRow);
                if (listeners != null) {
                    for (TemplateListener l : listeners) {
                        l.updateRow(idx, row);
                    }
                }
                inputRows.add(row);
                idx++;
            }
            final String inputPath = new File(getTestDataDir(), inputFileName).getAbsolutePath();
            final CSVFileWriter inputWriter = new CSVFileWriter(inputPath, getController().getAppConfig(), AppUtil.COMMA);
            try {
                inputWriter.open();
                inputWriter.setColumnNames(templateReader.getColumnNames());
                inputWriter.writeRowList(inputRows);
                return inputPath;
            } finally {
                inputWriter.close();
            }
        } finally {
            templateReader.close();
        }
    }

    protected static final boolean DEBUG_MESSAGES = false;

    protected final Map<String, String> getTestConfig(OperationInfo op, String daoName, boolean isExtraction) {
        return getTestConfig(op, daoName, new File(getTestDataDir(), this.baseName + "Map.sdl").getAbsolutePath(),
                isExtraction);
    }

    protected final Map<String, String> getTestConfig(OperationInfo op, String daoName, String mappingFile,
            boolean isExtraction) {
        Map<String, String> res = super.getTestConfig();
        res.put(AppConfig.PROP_MAPPING_FILE, mappingFile);
        res.put(AppConfig.PROP_OPERATION, op.name());
        res.put(AppConfig.PROP_DAO_NAME, daoName);
        res.put(AppConfig.PROP_DAO_TYPE, isExtraction ? DataAccessObjectFactory.CSV_WRITE_TYPE
                : DataAccessObjectFactory.CSV_READ_TYPE);
        res.put(AppConfig.PROP_OUTPUT_STATUS_DIR, getTestStatusDir());
        String apiType = "Soap";
        if (isBulkAPIEnabled(res)) {
            apiType = "Bulk";
        } else if (isBulkV2APIEnabled(res)) {
            apiType = "BulkV2";
        }
        res.put(AppConfig.PROP_OUTPUT_SUCCESS, getSuccessFilePath(apiType));
        res.put(AppConfig.PROP_OUTPUT_ERROR, getErrorFilePath(apiType));

        // Don't debug by default, as it slows down the processing
        if (ProcessTestBase.DEBUG_MESSAGES) {
            res.put(AppConfig.PROP_DEBUG_MESSAGES, "true");
            res.put(AppConfig.PROP_DEBUG_MESSAGES_FILE,
                    new File(getTestStatusDir(), this.baseName + apiType + "DebugTrace.log").getAbsolutePath());
        }

        return res;
    }

    protected final Map<String, String> getTestConfig(OperationInfo op, boolean isExtraction) {
        return getTestConfig(op, new File(getTestDataDir(), this.baseName + ".csv").getAbsolutePath(), isExtraction);
    }

    protected Controller runProcess(Map<String, String> argMap, int numRows) throws ProcessInitializationException,
    DataAccessObjectException {
        return runProcess(argMap, numRows, false);
    }

    protected Controller runProcess(Map<String, String> argMap, int numRows, boolean emptyId)
            throws ProcessInitializationException,
            DataAccessObjectException {
        return runProcessWithErrors(argMap, numRows, 0, emptyId);
    }

    protected Controller runProcessWithErrors(Map<String, String> argMap, int numSuccesses, int numFailures)
            throws ProcessInitializationException, DataAccessObjectException {
        return runProcessWithErrors(argMap, numSuccesses, numFailures, false);
    }

    private Controller runProcessWithErrors(Map<String, String> argMap, int numSuccesses, int numFailures,
            boolean emptyId) throws ProcessInitializationException, DataAccessObjectException {
        int numInserts = 0;
        int numUpdates = 0;

        OperationInfo op = OperationInfo.valueOf(argMap.get(AppConfig.PROP_OPERATION));
        if (op == OperationInfo.insert)
            numInserts = numSuccesses;
        else if (op != null && op != OperationInfo.upsert)
            numUpdates = numSuccesses;
        else
            throw new UnsupportedOperationException(op + " not supported");
        return runProcess(argMap, true, null, numInserts, numUpdates, numFailures, emptyId);
    }

    protected Controller runUpsertProcess(Map<String, String> args, int numInserts, int numUpdates)
            throws ProcessInitializationException, DataAccessObjectException {
        return runProcess(args, true, null, numInserts, numUpdates, 0, false);
    }

    protected Controller runProcessNegative(Map<String, String> args, String failureMessage)
            throws ProcessInitializationException, DataAccessObjectException {
        Controller controller = null;
        try {
            controller = runProcess(args, false, failureMessage, 0, 0, 0, false);
        } catch (RuntimeException ex) {
            // ignore
        }
        return controller;
    }
    
    protected IProcess runBatchProcess(Map<String, String> argMap) {
        if (argMap == null) argMap = getTestConfig();
        argMap.put(AppConfig.PROP_PROCESS_THREAD_NAME, this.baseName);
        argMap.put(AppConfig.PROP_READ_ONLY_CONFIG_PROPERTIES, Boolean.TRUE.toString());
        argMap.put(AppConfig.CLI_OPTION_RUN_MODE, AppConfig.RUN_MODE_BATCH_VAL);

        // emulate invocation through process.bat script
        String[] args = new String[argMap.size()+1];
        args[0] = getTestConfDir();
        int i = 1;
        if (argMap.containsKey(AppConfig.PROP_PROCESS_NAME)) {
            args[i++] = argMap.get(AppConfig.PROP_PROCESS_NAME);
            argMap.remove(AppConfig.PROP_PROCESS_NAME);
        }
        for (Map.Entry<String, String> entry: argMap.entrySet())
        {
            args[i++] = entry.getKey() + "=" + entry.getValue();
        }
        this.getBinding(); // establish the test connection if not done so already
        final NihilistProgressAdapter monitor = new NihilistProgressAdapter();
        return DataLoaderRunner.runApp(args, monitor);
    }

    protected Controller runProcess(Map<String, String> argMap, boolean expectProcessSuccess, String failMessage,
            int numInserts, int numUpdates, int numFailures, boolean emptyId) throws ProcessInitializationException,
            DataAccessObjectException {
        IProcess runner = runBatchProcess(argMap);
        ILoaderProgress monitor = runner.getMonitor();
        Controller controller = runner.getController();

        // verify process completed as expected
        String actualMessage = monitor.getMessage();
        if (expectProcessSuccess) {

            assertTrue("Process failed: " + actualMessage, monitor.isSuccess());
            verifyFailureFile(controller, numFailures);        //A.S.: To be removed and replaced
            verifySuccessFile(controller, numInserts, numUpdates, emptyId);
            long serverAPIInvocations = HttpTransportImpl.getServerInvocationCount();
            assertTrue("Number of server invocations (" + serverAPIInvocations + ") have exceeded the threshold of " + serverApiInvocationThreshold, serverAPIInvocations <= serverApiInvocationThreshold);
        } else {
            assertFalse("Expected process to fail but got success: " + actualMessage, monitor.isSuccess());
        }
        // TODO: validate all messages, including nulls if those exist
        if (failMessage != null) {
            Assert.assertTrue("Error message should contain '" + failMessage + "' but the actual message was '" + actualMessage + "'",
                    actualMessage.contains(failMessage));
        }

        // return the controller used by the process so that the tests can validate success/error output files, etc
        return controller;
    }
    
    protected void setServerApiInvocationThreshold(int threshold) {
        serverApiInvocationThreshold = threshold;
    }

    private static final String INSERT_MSG = "Item Created";
    private static final Map<OperationInfo, String> UPDATE_MSGS;

    static {
        UPDATE_MSGS = new EnumMap<OperationInfo, String>(OperationInfo.class);
        UPDATE_MSGS.put(OperationInfo.delete, "Item Deleted");
        UPDATE_MSGS.put(OperationInfo.undelete, "Item Undeleted");
        UPDATE_MSGS.put(OperationInfo.hard_delete, "Item Hard Deleted");
        UPDATE_MSGS.put(OperationInfo.upsert, "Item Updated");
        UPDATE_MSGS.put(OperationInfo.update, "Item Updated");
        UPDATE_MSGS.put(OperationInfo.extract, "Item queried and written successfully");
        UPDATE_MSGS.put(OperationInfo.extract_all, "Item queried and written successfully");
    }

    protected void verifySuccessFile(Controller ctl, int numInserts, int numUpdates, boolean emptyId)
            throws ParameterLoadException,
            DataAccessObjectException {
        final String successFile = ctl.getAppConfig().getStringRequired(AppConfig.PROP_OUTPUT_SUCCESS);
        //final String suceessFule2 = ctl.getConfig().
        assertNumRowsInCSVFile(successFile, numInserts + numUpdates);
        boolean isBulkV2Operation = ctl.getAppConfig().isBulkV2APIEnabled();

        TableRow row = null;
        CSVFileReader rdr = new CSVFileReader(new File(successFile), getController().getAppConfig(), true, false);
        String expectedUpdateStatusVal = UPDATE_MSGS.get(ctl.getAppConfig().getOperationInfo());
        String expectedInsertStatusVal = INSERT_MSG;
        if (isBulkV2Operation && !ctl.getAppConfig().getOperationInfo().isExtraction()) {
            expectedInsertStatusVal = "true";
            expectedUpdateStatusVal = "false";
        }
        int insertsFound = 0;
        int updatesFound = 0;
        while ((row = rdr.readTableRow()) != null) {
            String id = (String)row.get(AppConfig.ID_COLUMN_NAME);
            if (id == null) {
                id = (String)row.get(AppConfig.ID_COLUMN_NAME_IN_BULKV2);
            }
            if (emptyId) assertEquals("Expected empty id", "", id);
            else
                assertValidId(id);
            String statusValForRow = (String)row.get(AppConfig.STATUS_COLUMN_NAME);
            
            // status column for Bulk v2 upload operation is different from that for all Bulk v1 operations
            // and Bulk v2 extract operation
            if (isBulkV2Operation && !ctl.getAppConfig().getOperationInfo().isExtraction()) {
                statusValForRow = (String)row.get(AppConfig.STATUS_COLUMN_NAME_IN_BULKV2);
            }
            if (expectedInsertStatusVal.equals(statusValForRow))
                insertsFound++;
            else if (expectedUpdateStatusVal.equals(statusValForRow))
                updatesFound++;
            else
                Assert.fail("unrecognized status: " + statusValForRow);
        }
        assertEquals("Wrong number of inserts in success file: " + successFile, numInserts, insertsFound);
        assertEquals("Wrong number of updates in success file: " + successFile, numUpdates, updatesFound);
    }

    protected void assertValidId(String id) {
        assertTrue("Invalid id: " + id, id != null && id.length() == 18);
    }

    /**
     * To create a mapping between the name of the objects being inserted and their base-64 encoded data.
     *
     * @param None
     * @return The mapping of String to String -- Map<String,String>
     */
    protected Map<String, String> createAttachmentFileMap(String... fileNames) throws IOException {

        final Map<String, String> resultMap = new HashMap<String, String>();

        for (String fn : fileNames) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            FileUtil.copy(new FileInputStream(getTestDataDir() + File.separator + fn), bytes);
            resultMap.put(fn, Base64.encodeBytes(bytes.toByteArray()));
        }

        return resultMap;
    }

    // Utility function to get the path of the success .csv
    protected String getSuccessFilePath(String apiType) {
        return getStatusFile(apiType, "Success.csv").getAbsolutePath();
    }

    protected String getErrorFilePath(String apiType) {
        return getStatusFile(apiType, "Error.csv").getAbsolutePath();
    }

    protected File getStatusFile(String apiType, String fnEnd) {
        return new File(getTestStatusDir(), this.baseName + "-" + apiType + "-" + fnEnd);
    }

    /**
     * Verifies that the list of files imported and their makeup is consistent
     * with what we expect to be in the org
     *
     * @param dbaseFileCorrespondence    - Map<String,String>
     * @param expectedFileCorrespondence - Map<String,String>
     * @return void
     */
    protected void verifyAttachmentObjects(Map<String, String> dbaseFileCorrespondence, Map<String, String> expectedFileCorrespondence) {

        if (dbaseFileCorrespondence == null
                || expectedFileCorrespondence == null) {

            Assert.fail("verifyAttachmentObjects: null input map object(s)");

        }

        if (dbaseFileCorrespondence.size() != expectedFileCorrespondence.size()) {

            Assert.fail("verifyAttachmentObjects: number of attached files ("
                    + dbaseFileCorrespondence.size()
                    + ") differs from expected number of attached files ("
                    + expectedFileCorrespondence.size() + ")");

        }


        for (Map.Entry<String, String> ent : dbaseFileCorrespondence.entrySet()) {

            String currentFileName = ent.getKey().toString();
            String currentFileContents = ent.getValue().toString();

            String expFileContents = expectedFileCorrespondence.get(
                    currentFileName).toString();

            String modifiedExpFileContents = expFileContents.replace("\n", "");

            assertEquals(modifiedExpFileContents, currentFileContents);

        }

    }

    protected void verifyFailureFile(Controller ctl, int numFailures)
            throws ParameterLoadException, DataAccessObjectException {
        assertNumRowsInCSVFile(ctl.getAppConfig().getStringRequired(
                AppConfig.PROP_OUTPUT_ERROR), numFailures);
    }

    private void assertNumRowsInCSVFile(String fName, int expectedRows) throws DataAccessObjectException {
        CSVFileReader rdr = new CSVFileReader(new File(fName), getController().getAppConfig(), true, false);
        rdr.open();
        int actualRows = rdr.getTotalRows();
        assertEquals("Wrong number of rows in file :" + fName, expectedRows, actualRows);
    }

    protected boolean isBulkAPIEnabled(Map<String, String> argMap) {
        return isSettingEnabled(argMap, AppConfig.PROP_BULK_API_ENABLED)
                && !isSettingEnabled(argMap, AppConfig.PROP_BULKV2_API_ENABLED);
    }
    
    protected boolean isBulkV2APIEnabled(Map<String, String> argMap) {
        return isSettingEnabled(argMap, AppConfig.PROP_BULKV2_API_ENABLED);
    }
    protected boolean isSettingEnabled(Map<String, String> argMap, String configKey) {
        return AppConfig.TRUE.equalsIgnoreCase(argMap.get(configKey));
    }

    protected Map<String, String> getUpdateTestConfig(boolean isUpsert, String extIdField, int numAccountsToInsert)
            throws DataAccessObjectException {
        return getUpdateTestConfig(this.baseName, isUpsert, extIdField, numAccountsToInsert);
    }

    /**
     * Get a config map for use with update/upsert operations
     *
     * @param fileNameBase        This method will expect a file named <fileNameBase>Template.csv to exist.
     *                            The template file will be filled in using freshly inserted accounts (if numAccountsToInsert is
     *                            greater than zero). This will generate <fileNameBase>.csv for the DAO, and the mapping file will be
     *                            set to <fileNameBase>Map.sdl.
     * @param isUpsert            True for upsert process configuration false for update process configuration.
     * @param extIdField          The name of the external id field (for upsert)
     * @param numAccountsToInsert Number of accounts to create right now and use to fill in the template file.
     * @return Map of dataloader settings for running an update/upsert operation.
     */
    protected Map<String, String> getUpdateTestConfig(String fileNameBase, boolean isUpsert, String extIdField,
            int numAccountsToInsert) throws DataAccessObjectException {
        final boolean hasExtId = isUpsert && extIdField != null;
        TemplateListener[] listeners = null;
        if (hasExtId) {
            insertSfdcAccounts(numAccountsToInsert, true);
        } else {
            listeners = new TemplateListener[] { new AccountIdTemplateListener(numAccountsToInsert) };
        }
        final String updateFileName = convertTemplateToInput(fileNameBase + "Template.csv", fileNameBase + ".csv", listeners);
        final File mappingFile = new File(getTestDataDir(), fileNameBase + "Map.sdl");
        final Map<String, String> argMap = getTestConfig(isUpsert ? OperationInfo.upsert : OperationInfo.update,
                updateFileName, mappingFile.getAbsolutePath(), false);
        if (hasExtId) argMap.put(AppConfig.PROP_IDLOOKUP_FIELD, extIdField);
        return argMap;
    }

    protected Map<String, String> getTestConfig() {
        Map<String, String> configArgsMap = super.getTestConfig();
        // run process tests in batch mode
        configArgsMap.put(AppConfig.CLI_OPTION_RUN_MODE, AppConfig.RUN_MODE_BATCH_VAL);
        return configArgsMap;
    }
    

    @SuppressWarnings("unchecked")
    protected UpsertResult[] doUpsert(String entity, Map<String, Object> sforceMapping) throws Exception {
        // now convert to a dynabean array for the client
        // setup our dynabeans
        BasicDynaClass dynaClass = setupDynaClass(entity, (Collection<String>)(Collection<?>)(sforceMapping.values()));

        DynaBean sforceObj = dynaClass.newInstance();

        // This does an automatic conversion of types.
        BeanUtils.copyProperties(sforceObj, sforceMapping);

        List<DynaBean> beanList = new ArrayList<DynaBean>();
        beanList.add(sforceObj);

        // get the client and make the insert call
        PartnerClient client = PartnerClient.getInstance(getController());
        UpsertResult[] results = client.loadUpserts(beanList);
        for (UpsertResult result : results) {
            if (!result.getSuccess()) {
                Assert.fail("Upsert returned an error: " + result.getErrors()[0].getMessage());
            }
        }
        return results;
    }
    
    /**
     * Make sure to set external id field
     */
    protected String setExtIdField(String extIdField) {
        getController().getAppConfig().setValue(AppConfig.PROP_IDLOOKUP_FIELD, extIdField);
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
    protected Object getRandomExtId(String entity, String whereClause, Object prevValue) throws ConnectionException {

        // insert couple of accounts so there're at least two records to work with
        upsertSfdcRecords(entity, 2);

        // get the client and make the query call
        String extIdField = getController().getAppConfig().getString(AppConfig.PROP_IDLOOKUP_FIELD);
        PartnerClient client = PartnerClient.getInstance(getController());
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

        return records[0].getField(extIdField);
    }
    
    protected BasicDynaClass setupDynaClass(String entity, Collection<String> sfFields) throws ConnectionException {
        getController().getAppConfig().setValue(AppConfig.PROP_ENTITY, entity);
        LoginClient loginClient = getController().getLoginClient();
        if (!loginClient.isLoggedIn()) {
        	loginClient.connect();
        }

        getController().setFieldTypes();
        getController().setReferenceDescribes(sfFields);
        DynaProperty[] dynaProps = SforceDynaBean.createDynaProps(getController().getPartnerClient().getFieldTypes(), getController());
        BasicDynaClass dynaClass = SforceDynaBean.getDynaBeanInstance(dynaProps);
        SforceDynaBean.registerConverters(getController().getAppConfig());
        return dynaClass;
    }
}
