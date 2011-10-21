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


import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import com.salesforce.dataloader.*;
import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataAccessObjectFactory;
import com.salesforce.dataloader.dao.csv.CSVFileReader;
import com.salesforce.dataloader.dao.csv.CSVFileWriter;
import com.salesforce.dataloader.exception.*;
import com.salesforce.dataloader.exception.UnsupportedOperationException;
import com.salesforce.dataloader.process.CsvProcessTest.AttachmentTemplateListener;
import com.sforce.soap.partner.*;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.util.FileUtil;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;



//import common.api.*;

/**
 * Base class for batch process tests
 *
 * @author Alex Warshavsky
 * @since 8.0
 */
abstract public class ProcessTestBase extends ConfigTestBase {

    public static ConfigGenerator getConfigGenerator() {
        return DEFAULT_CONFIG_GEN;
    }

    protected ProcessTestBase(String name, Map<String, String> config) {
        super(name, config);
    }

    protected ProcessTestBase(String name) {
        super(name);
    }

    // logger
    private static Logger logger = Logger.getLogger(TestBase.class);

    @Override
    public void setUp() {
        super.setUp();
        cleanRecords();
    }

    @Override
    public void tearDown() throws Exception {
        try {
            cleanRecords();
        } finally {
            super.tearDown();
        }
    }

    private void cleanRecords() {
        // cleanup the records that might've been created on previous tests
        deleteSfdcRecords("Account", ACCOUNT_WHERE_CLAUSE, 0);
        deleteSfdcRecords("Contact", CONTACT_WHERE_CLAUSE, 0);
    }

    protected void verifyErrors(Controller theController, String expectedErrorMessage) throws DataAccessObjectException {
        final CSVFileReader errReader = new CSVFileReader(theController.getConfig().getString(Config.OUTPUT_ERROR));
        try {
            errReader.open();
            for (Map<String, Object> errorRow : errReader.readRowList(errReader.getTotalRows())) {
                String actualMessage = (String)errorRow.get("ERROR");
                if (actualMessage == null || !actualMessage.startsWith(expectedErrorMessage))
                    fail("Error row does not have the expected error message: " + expectedErrorMessage
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
        final CSVFileReader successRdr = new CSVFileReader(ctl.getConfig().getString(Config.OUTPUT_SUCCESS));
        final Set<String> remaining = new HashSet<String>(ids);
        final Set<String> unexpected = new HashSet<String>();
        try {
            for (Map<String, Object> row : successRdr.readRowList(Integer.MAX_VALUE)) {
                final String rowid = (String)row.get("ID");
                if (rowid != null && rowid.length() > 0 && !remaining.remove(rowid)) unexpected.add(rowid);
            }
        } finally {
            successRdr.close();
        }

        if (!remaining.isEmpty()) fail("Ids not found: " + remaining);
        if (!unexpected.isEmpty()) fail("Unexpected ids found: " + unexpected);
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
                    fail("Insert returned an error: "
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
            fail("Error inserting records: " + e.getExceptionMessage());
        } catch (ConnectionException e) {
            fail("Error inserting records: " + e.getMessage());
        }
        return null; // make eclipse happy, shouldn't reach this point after
        // fail()
    }

    private String[] upsertSfdcRecords(SObject[] records, boolean ignoreOutput,
            int retries) {
        // get the client and make the insert call
        try {
            UpsertResult[] results = getBinding().upsert(
                    getController().getConfig().getString(
                            Config.EXTERNAL_ID_FIELD), records);
            String[] ids = new String[results.length];
            for (int i = 0; i < results.length; i++) {
                UpsertResult result = results[i];
                if (!result.getSuccess()) {
                    fail("Upsert returned an error: "
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
            fail("Error upserting records: " + e.getExceptionMessage());
        } catch (ConnectionException e) {
            fail("Error upserting records: " + e.getMessage());
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
            account.setField("Phone", "415-555-" + seqStr);
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
        public String getEntityName() {
            return "Account";
        }

        public String getSOQL(String selectExpr) {
            return generateSOQL(selectExpr, ACCOUNT_WHERE_CLAUSE);
        }
    }

    protected static class ContactGenerator extends AbstractSObjectGenerator {
        /**
         * @param i
         * @return SObject contact
         */
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
            return contact;
        }

        /*
         * (non-Javadoc)
         *
         * @seecom.salesforce.dataloader.process.ProcessTestBase.SObjectGetter#
         * getEntityName()
         */
        public String getEntityName() {
            return "Contact";
        }

        public String getSOQL(String selectFields) {
            return generateSOQL(selectFields, CONTACT_WHERE_CLAUSE);
        }
    }

    /**
     * @param entityName
     * @param whereClause
     * @param retries
     */
    protected void deleteSfdcRecords(String entityName, String whereClause,
            int retries) {
        try {
            // query for records
            String soql = "select Id from " + entityName + " where " + whereClause;
            logger.debug("Querying " + entityName + "s to delete with soql: " + soql);
            int deletedCount = 0;
            PartnerConnection conn = getBinding();
            // now delete them 200 at a time.... we should use bulk api here
            for (QueryResult qr = conn.query(soql); qr != null && qr.getRecords().length > 0; qr = qr.isDone() ? null
                    : conn.queryMore(qr.getQueryLocator())) {
                deleteSfdcRecords(qr, 0);
                deletedCount += qr.getRecords().length;
                logger.debug("Deleted " + deletedCount + " out of " + qr.getSize() + " total deleted records");
            }
            logger.info("Deleted " + deletedCount + " total objects of type " + entityName);
        } catch (ApiFault e) {
            if (checkBinding(++retries, e) != null) {
                deleteSfdcRecords(entityName, whereClause, retries);
            }
            fail("Failed to query " + entityName + "s to delete ("
                    + whereClause + "), error: " + e.getExceptionMessage());
        } catch (ConnectionException e) {
            fail("Failed to query " + entityName + "s to delete ("
                    + whereClause + "), error: " + e.getMessage());
        }
    }

    /**
     * @param qryResult
     */
    protected void deleteSfdcRecords(QueryResult qryResult, int retries) {
        try {
            List<String> toDeleteIds = new ArrayList<String>();
            for (int i = 0; i < qryResult.getRecords().length; i++) {
                SObject record = qryResult.getRecords()[i];
                logger.debug("Deleting record id:" + record.getId());
                toDeleteIds.add(record.getId());
                // when SAVE_RECORD_LIMIT records are reached or
                // if we're on the last query result record, do the delete
                if (i > 0 && (i + 1) % SAVE_RECORD_LIMIT == 0
                        || i == qryResult.getRecords().length - 1) {
                    DeleteResult[] delResults = getBinding().delete(
                            toDeleteIds.toArray(new String[] {}));
                    for (int j = 0; j < delResults.length; j++) {
                        DeleteResult delResult = delResults[j];
                        if (!delResult.getSuccess()) {
                            logger.warn("Delete returned an error: " + delResult.getErrors()[0].getMessage(),
                                    new RuntimeException());
                        }
                    }
                    toDeleteIds.clear();
                }
            }
        } catch (ApiFault e) {
            if (checkBinding(++retries, e) != null) {
                deleteSfdcRecords(qryResult, retries);
            }
            fail("Failed to delete records, error: " + e.getExceptionMessage());
        } catch (ConnectionException e) {
            fail("Failed to delete records, error: " + e.getMessage());
        }
    }

    protected static interface TemplateListener {
        void updateRow(int idx, Map<String, Object> row);
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

        public void updateRow(int idx, Map<String, Object> row) {
            row.put("ID", idx < this.accountIds.length ? this.accountIds[idx] : "");
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

        final CSVFileReader templateReader = new CSVFileReader(new File(getTestDataDir(), templateFileName)
        .getAbsolutePath());
        try {
            templateReader.open();

            int numRows = templateReader.getTotalRows();
            final List<Map<String, Object>> templateRows = templateReader.readRowList(numRows);
            assertNotNull("CVSReader returned a null list of rows, but expected a list with size " + numRows,
                    templateRows);
            final List<Map<String, Object>> inputRows = new ArrayList<Map<String, Object>>(templateRows.size());

            // verify that the template file is useable
            assertEquals("Wrong number of rows were read using readRowList while attempting to convert template file: "
                    + templateFileName, numRows, templateRows.size());

            // insert accounts for the whole template or part of it if
            // maxInserts is smaller then template size
            int idx = 0;
            for (Map<String, Object> templateRow : templateRows) {
                final Map<String, Object> row = new HashMap<String, Object>(templateRow);
                if (listeners != null) {
                    for (TemplateListener l : listeners) {
                        l.updateRow(idx, row);
                    }
                }
                inputRows.add(row);
                idx++;
            }
            final String inputPath = new File(getTestDataDir(), inputFileName).getAbsolutePath();
            final CSVFileWriter inputWriter = new CSVFileWriter(inputPath);
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
        Map<String, String> res = super.getTestConfig();
        res.put(Config.MAPPING_FILE, new File(getTestDataDir(), this.baseName + "Map.sdl").getAbsolutePath());
        res.put(Config.OPERATION, op.name());
        res.put(Config.DAO_NAME, daoName);
        res.put(Config.DAO_TYPE, isExtraction ? DataAccessObjectFactory.CSV_WRITE_TYPE
                : DataAccessObjectFactory.CSV_READ_TYPE);
        res.put(Config.OUTPUT_STATUS_DIR, getTestStatusDir());
        String apiType = isBulkAPIEnabled(res) ? "Bulk" : "Soap";
        res.put(Config.OUTPUT_SUCCESS, getSuccessFilePath(apiType));
        res.put(Config.OUTPUT_ERROR, getErrorFilePath(apiType));

        // Don't debug by default, as it slows down the processing
        if (ProcessTestBase.DEBUG_MESSAGES) {
            res.put(Config.DEBUG_MESSAGES, "true");
            res.put(Config.DEBUG_MESSAGES_FILE,
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


    protected Controller runProcessWithAttachmentListener(Map<String, String> argMap, int numRows, AttachmentTemplateListener myAttachmentTemplateListener, String... files) throws ProcessInitializationException,
    DataAccessObjectException, ConnectionException {
        return runProcessWithErrorsWithAttachmentListener(argMap, numRows, 0, myAttachmentTemplateListener,files);
    }

    protected Controller runProcessWithErrors(Map<String, String> argMap, int numSuccesses, int numFailures)
            throws ProcessInitializationException, DataAccessObjectException {
        return runProcessWithErrors(argMap, numSuccesses, numFailures, false);
    }

    private Controller runProcessWithErrors(Map<String, String> argMap, int numSuccesses, int numFailures,
            boolean emptyId) throws ProcessInitializationException, DataAccessObjectException {
        int numInserts = 0;
        int numUpdates = 0;

        OperationInfo op = OperationInfo.valueOf(argMap.get(Config.OPERATION));
        if (op == OperationInfo.insert)
            numInserts = numSuccesses;
        else if (op != null && op != OperationInfo.upsert)
            numUpdates = numSuccesses;
        else
            throw new UnsupportedOperationException(op + " not supported");
        return runProcess(argMap, true, null, numInserts, numUpdates, numFailures, emptyId);
    }


    protected Controller runProcessWithErrorsWithAttachmentListener(Map<String, String> argMap, int numSuccesses, int numFailures, AttachmentTemplateListener myAttachmentTemplateListener, String... files)
            throws ProcessInitializationException, DataAccessObjectException, ConnectionException {
        int numInserts = 0;
        int numUpdates = 0;

        OperationInfo op = OperationInfo.valueOf(argMap.get(Config.OPERATION));
        if (op == OperationInfo.insert)
            numInserts = numSuccesses;
        else if (op != null && op != OperationInfo.upsert)
            numUpdates = numSuccesses;
        else
            throw new UnsupportedOperationException(op + " not supported");
        return runProcessWithAttachmentListener(argMap, true, null, numInserts, numUpdates, numFailures, myAttachmentTemplateListener, files);
    }

    protected Controller runUpsertProcess(Map<String, String> args, int numInserts, int numUpdates)
            throws ProcessInitializationException, DataAccessObjectException {
        return runProcess(args, true, null, numInserts, numUpdates, 0, false);
    }

    protected Controller runProcessNegative(Map<String, String> args, String failureMessage)
            throws ProcessInitializationException, DataAccessObjectException {
        return runProcess(args, false, failureMessage, 0, 0, 0, false);
    }

    private Controller runProcess(Map<String, String> argMap, boolean expectProcessSuccess, String failMessage,
            int numInserts, int numUpdates, int numFailures, boolean emptyId) throws ProcessInitializationException,
            DataAccessObjectException {

        if (argMap == null) argMap = getTestConfig();

        final ProcessRunner runner = ProcessRunner.getInstance(argMap);
        runner.setName(this.baseName);

        final TestProgressMontitor monitor = new TestProgressMontitor();
        runner.run(monitor);
        Controller controller = runner.getController();

        // verify process completed as expected
        String actualMessage = monitor.getMessage();
        if (expectProcessSuccess) {

            assertTrue("Process failed: " + actualMessage, monitor.isSuccess());
            verifyFailureFile(controller, numFailures);        //A.S.: To be removed and replaced
            verifySuccessFile(controller, numInserts, numUpdates, emptyId);

        } else {
            assertFalse("Expected process to fail but got success: " + actualMessage, monitor.isSuccess());
        }
        // TODO: validate all messages, including nulls if those exist
        if (failMessage != null) {
            if (!actualMessage.startsWith(failMessage))
                fail("Error message should start with '" + failMessage + "' but the actual message was '"
                        + actualMessage + "'");
        }

        // return the controller used by the process so that the tests can validate success/error output files, etc
        return controller;
    }

    private Controller runProcessWithAttachmentListener(Map<String, String> argMap, boolean expectProcessSuccess, String failMessage,
            int numInserts, int numUpdates, int numFailures, AttachmentTemplateListener myAttachmentTemplateListener, String... files) throws ProcessInitializationException,
            DataAccessObjectException, ConnectionException {

        if (argMap == null) argMap = getTestConfig();

        final ProcessRunner runner = ProcessRunner.getInstance(argMap);
        runner.setName(this.baseName);

        final TestProgressMontitor monitor = new TestProgressMontitor();
        runner.run(monitor);
        Controller controller = runner.getController();

        // verify process completed as expected
        if (expectProcessSuccess) {

            verifyInsertCorrectByContent(controller, createAttachmentFileMap(files), myAttachmentTemplateListener);
            //this should also still work
            assertTrue("Process failed: " + monitor.getMessage(), monitor.isSuccess());
            verifyFailureFile(controller, numFailures);        //A.S.: To be removed and replaced
            verifySuccessFile(controller, numInserts, numUpdates, false);

        } else {
            assertFalse("Expected process to fail but got success: " + monitor.getMessage(), monitor.isSuccess());
        }
        // TODO: validate all messages, including nulls if those exist
        if (failMessage != null) {
            assertEquals("wrong message: ", failMessage, monitor.getMessage());
        }

        // return the controller used by the process so that the tests can validate success/error output files, etc
        return controller;
    }


    private static final String INSERT_MSG = "Item Created";
    private static final Map<OperationInfo, String> UPDATE_MSGS;
    static {
        UPDATE_MSGS = new EnumMap<OperationInfo, String>(OperationInfo.class);
        UPDATE_MSGS.put(OperationInfo.delete, "Item Deleted");
        UPDATE_MSGS.put(OperationInfo.hard_delete, "Item Hard Deleted");
        UPDATE_MSGS.put(OperationInfo.upsert, "Item Updated");
        UPDATE_MSGS.put(OperationInfo.update, "Item Updated");
        UPDATE_MSGS.put(OperationInfo.extract, "Item queried and written successfully");
        UPDATE_MSGS.put(OperationInfo.extract_all, "Item queried and written successfully");
    }

    protected void verifySuccessFile(Controller ctl, int numInserts, int numUpdates, boolean emptyId)
            throws ParameterLoadException,
            DataAccessObjectException {
        final String successFile = ctl.getConfig().getStringRequired(Config.OUTPUT_SUCCESS);
        //final String suceessFule2 = ctl.getConfig().
        assertNumRowsInCSVFile(successFile, numInserts + numUpdates);

        Map<String, Object> row = null;
        CSVFileReader rdr = new CSVFileReader(successFile);
        String updateMsg = UPDATE_MSGS.get(ctl.getConfig().getOperationInfo());
        int insertsFound = 0;
        int updatesFound = 0;
        while ((row = rdr.readRow()) != null) {
            String id = (String)row.get("ID");
            if (emptyId) assertEquals("Expected empty id", "", id);
            else
                assertValidId(id);
            String status = (String)row.get("STATUS");
            if (INSERT_MSG.equals(status))
                insertsFound++;
            else if (updateMsg.equals(status))
                updatesFound++;
            else
                fail("unrecognized status: " + status);
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
     *
     */

    protected Map<String,String> createAttachmentFileMap(String... fileNames) {

        Map<String,String> resultMap = new HashMap<String,String>();

        for(String fn : fileNames) {

            String fileContents = Base64.encode(importFileToBinary(getTestDataDir() + File.separator + fn));
            resultMap.put(fn, fileContents);

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
     * To import a file and place it into program memory by inputing it as a byte array
     *
     * @param fileName String - The name of the file in the system
     * @return byte[] - The byte array of the file
     *
     */

    protected byte[] importFileToBinary(String fileName) {

        if(fileName==null || fileName.length()==0) {
            return null;
        }

        try {

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            FileUtil.copy(new FileInputStream(fileName), bytes);
            return bytes.toByteArray();


        } catch (IOException e) {

            System.out.println("IO Exception =: " + e);
            return null;
        }

    }

    /**
     * To verify that the insertion is done correctly
     *
     * @param The
     *            controller ctl - Controller
     * @param Mapping
     *            of filename to base-64 encodings Map<String,String>
     * @param The
     *            listener - myAttachmentTemplateListener - AttachmentTemplateListener
     */
    protected void verifyInsertCorrectByContent(Controller ctl, Map<String, String> expectedMapping,
            AttachmentTemplateListener myAttachmentTemplateListener) throws ConnectionException {
        // expectedMapping should have the expected base-64 encodings of the files
        //now get the actual encodings of the files and their filenames

        Map<String,String> dbaseFileCorrespondence = getActualAttachmentMap(myAttachmentTemplateListener, expectedMapping.keySet());

        if(dbaseFileCorrespondence==null) {
            fail("verifyInsertCorrectByContent: retrieved actual attachment information unsuccessfully, as it is null");
        } else {
            verifyAttachmentObjects(dbaseFileCorrespondence, expectedMapping);
        }
        return;
    }

    /**
     * Get the actual attachment names and their respective base-64 encodings.
     *
     * @param myAttachmentTemplateListener
     *            - AttachmentTemplateListener
     *
     * @param fileNames
     *             The set of filesnames that we expect to find
     * @return Map<String,String>
     */
    protected Map<String, String> getActualAttachmentMap(AttachmentTemplateListener myAttachmentTemplateListener, Set<String> fileNames)
            throws ConnectionException {

        HashMap<String, String> resultMap = new HashMap<String,String>();

        String soql = "select Name, Body from Attachment where ParentId=\'"+ myAttachmentTemplateListener.getAccountIds()[0] + "\'";

        for (QueryResult qr = getBinding().query(soql); qr!=null;qr=qr.isDone() ? null : getBinding().queryMore(qr.getQueryLocator())) {
            for(SObject myRecord : qr.getRecords()) {
                resultMap.put(myRecord.getField("Name").toString(), myRecord.getField("Body").toString());
            }
        }
        assertEquals("wrong number of results returned", fileNames.size(), resultMap.size());

        for (String fn: fileNames) {
            assertTrue("Missing file in results: " + fn, resultMap.containsKey(fn));
        }

        return resultMap;
    }

    /**
     *
     * Verifies that the list of files imported and their makeup is consistent
     * with what we expect to be in the org
     *
     * @param dbaseFileCorrespondence
     *            - Map<String,String>
     * @param expectedFileCorrespondence
     *            - Map<String,String>
     * @return void
     */
    protected void verifyAttachmentObjects(Map<String,String> dbaseFileCorrespondence, Map<String,String> expectedFileCorrespondence) {

        if (dbaseFileCorrespondence == null
                || expectedFileCorrespondence == null) {

            fail("verifyAttachmentObjects: null input map object(s)");

        }

        if (dbaseFileCorrespondence.size() != expectedFileCorrespondence.size()) {

            fail("verifyAttachmentObjects: number of attached files ("
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
        assertNumRowsInCSVFile(ctl.getConfig().getStringRequired(
                Config.OUTPUT_ERROR), numFailures);
    }

    private void assertNumRowsInCSVFile(String fName, int expectedRows) throws DataAccessObjectException {
        CSVFileReader rdr = new CSVFileReader(fName);
        rdr.open();
        int actualRows = rdr.getTotalRows();
        assertEquals("Wrong number of rows in file :" + fName, expectedRows, actualRows);
    }

    protected boolean isBulkAPIEnabled(Map<String, String> argMap) {
        return isSettingEnabled(argMap, Config.USE_BULK_API);
    }

    protected boolean isSettingEnabled(Map<String, String> argMap, String configKey) {
        return Config.TRUE.equalsIgnoreCase(argMap.get(configKey));
    }

    protected Map<String, String> getUpdateTestConfig(boolean isUpsert, String extIdField, int numAccountsToInsert) throws DataAccessObjectException {
        final boolean hasExtId = isUpsert && extIdField != null;
        TemplateListener[] listeners = null;
        if (hasExtId) {
            insertSfdcAccounts(numAccountsToInsert, true);
        } else {
            listeners = new TemplateListener[] { new AccountIdTemplateListener(numAccountsToInsert) };
        }
        final String updateFileName = convertTemplateToInput(this.baseName + "Template.csv", this.baseName + ".csv",
                listeners);
        final Map<String, String> argMap = getTestConfig(isUpsert ? OperationInfo.upsert : OperationInfo.update,
                updateFileName, false);
        if (hasExtId) argMap.put(Config.EXTERNAL_ID_FIELD, extIdField);
        return argMap;
    }

}
