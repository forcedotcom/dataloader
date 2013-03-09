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

import com.salesforce.dataloader.ConfigGenerator;
import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.csv.CSVFileReader;
import com.salesforce.dataloader.dao.csv.CSVFileWriter;
import com.salesforce.dataloader.model.NATextValue;
import com.salesforce.dataloader.model.Row;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * This class test that #N/A can be used to set fields to null when Use Bulk Api is enabled.
 * It also validates that empty fields are not handled in the same way as #N/A
 *
 * @author Jeff Lai
 * @since 25.0
 */
@RunWith(Parameterized.class)
public class NAProcessTest extends ProcessTestBase {

    private static final String TASK_SUBJECT = "NATest";
    private static final String TARGET_DIR = getProperty("target.dir").trim();
    private static final String CSV_DIR_PATH = TARGET_DIR + File.separator + NAProcessTest.class.getSimpleName();
    private static final String CSV_FILE_PATH = CSV_DIR_PATH + File.separator + "na.csv";
    private String userId;

    public NAProcessTest(Map<String, String> config) {
        super(config);
    }

    @Before
    public void populateUserId() throws Exception {
        if (userId == null) {
            userId = getUserId();
        }
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> getConfigGeneratorParams() {
        final ConfigGenerator bulkApiTrue = new ConfigSettingGenerator(ProcessTestBase.getConfigGenerator(),
                Config.BULK_API_ENABLED, Boolean.TRUE.toString());
        final ConfigGenerator bulkApiFalse = new ConfigSettingGenerator(ProcessTestBase.getConfigGenerator(),
                Config.BULK_API_ENABLED, Boolean.FALSE.toString());
        return Arrays.asList(new Object[]{bulkApiTrue.getConfigurations().get(0)}, new Object[]{bulkApiFalse.getConfigurations().get(0)});
    }

    @Test
    public void testTextFieldInsert() throws Exception {
        runNAtest("Description", false, OperationInfo.insert);
    }

    @Test
    public void testTextFieldUpdate() throws Exception {
        runNAtest("Description", false, OperationInfo.update);
    }

    @Test
    public void testDateTimeFieldInsert() throws Exception {
        runNAtest("ReminderDateTime", true, OperationInfo.insert);
    }

    @Test
    public void testDateTimeFieldUpdate() throws Exception {
        runNAtest("ReminderDateTime", true, OperationInfo.update);
    }

    @Test
    public void testDateFieldInsert() throws Exception {
        runNAtest("ActivityDate", true, OperationInfo.insert);
    }

    @Test
    public void testDateFieldUpdate() throws Exception {
        runNAtest("ActivityDate", true, OperationInfo.update);
    }

    @Test
    public void testTextEmptyFieldIsNotHandledAsNAUpdate() throws Exception {
        runEmptyFieldUpdateTest("Description", false);
    }

    @Test
    public void testTextEmptyFieldIsNotHandledAsNAInsert() throws Exception {
        runEmptyFieldInsertTest("Description");
    }

    @Test
    public void testDateEmptyFieldIsNotHandledAsNAUpdate() throws Exception {
        runEmptyFieldUpdateTest("ActivityDate", true);
    }

    @Test
    public void testDateEmptyFieldIsNotHandledAsNAInsert() throws Exception {
        runEmptyFieldInsertTest("ActivityDate");
    }

    private void runNAtest(String nullFieldName, boolean isDateField, OperationInfo operation) throws Exception {
        String taskId = null;
        if (!operation.equals(OperationInfo.insert)) {
            taskId = createTask(nullFieldName, isDateField);
        }
        generateCsvWithNAField(nullFieldName, taskId);
        Map<String, String> argMap = getArgMap(operation);
        Controller controller;
        if (!getController().getConfig().getBoolean(Config.BULK_API_ENABLED) && isDateField) {
            controller = runProcess(argMap, true, null, 0, 0, 1, false);
            String errorFile = controller.getConfig().getStringRequired(Config.OUTPUT_ERROR);
            String errorMessage = getCsvFieldValue(errorFile, "ERROR");
            assertEquals("unexpected error message",
                    "Error converting value to correct data type: Failed to parse date: #N/A", errorMessage);
        } else {
            int numInsert = operation.equals(OperationInfo.insert) ? 1 : 0;
            int numUpdate = operation.equals(OperationInfo.update) ? 1 : 0;
            controller = runProcess(argMap, true, null, numInsert, numUpdate, 0, false);
            String actualNullFieldValue = getFieldValueAfterOperation(nullFieldName, controller);
            String expectedNullFieldValue = getController().getConfig().getBoolean(Config.BULK_API_ENABLED) ? null : NATextValue.getInstance().toString();
            assertEquals("unexpected field value", expectedNullFieldValue, actualNullFieldValue);
        }
    }

    private void runEmptyFieldInsertTest(String emtpyFieldName) throws Exception {
        generateCsvWithEmptyField(emtpyFieldName, null);
        Controller controller = runProcess(getArgMap(OperationInfo.insert), true, null, 1, 0, 0, false);
        String actualValue = getFieldValueAfterOperation(emtpyFieldName, controller);
        assertNull("Empty field values in CSV shouldn't have been inserted with values", actualValue);
    }

    private void runEmptyFieldUpdateTest(String nullFieldName, boolean isDateField) throws Exception {
        String taskId = createTask(nullFieldName, isDateField);
        generateCsvWithEmptyField(nullFieldName, taskId);
        Controller controller = runProcess(getArgMap(OperationInfo.update), true, null, 0, 1, 0, false);
        String actualValue = getFieldValueAfterOperation(nullFieldName, controller);
        assertNotNull("Empty field values in CSV should have been ignored", actualValue);
    }

    private String getFieldValueAfterOperation(String nullFieldName, Controller controller) throws Exception {
        String successFile = controller.getConfig().getStringRequired(Config.OUTPUT_SUCCESS);
        String taskId = getCsvFieldValue(successFile, "ID");
        QueryResult result = getController().getPartnerClient().query("select " + nullFieldName + " from Task where Id='" + taskId + "'");
        assertEquals(1, result.getSize());
        return (String)result.getRecords()[0].getField(nullFieldName);
    }

    private Map<String, String> getArgMap(OperationInfo operation) {
        Map<String, String> argMap = getTestConfig(operation, CSV_FILE_PATH, getTestDataDir() + File.separator + "NAProcessTest.sdl", false);
        argMap.put(Config.ENTITY, "Task");
        argMap.remove(Config.EXTERNAL_ID_FIELD);
        return argMap;
    }

    private String createTask(String fieldToNullName, boolean isDateField) throws Exception {
        Object fieldToNullValue = isDateField ? new Date() : "asdf";
        SObject task = new SObject();
        task.setType("Task");
        task.setField("OwnerId", userId);
        task.setField("Subject", TASK_SUBJECT);
        task.setField(fieldToNullName, fieldToNullValue);
        SaveResult[] result = getController().getPartnerClient().getClient().create(new SObject[] { task });
        assertEquals(1, result.length);
        if (!result[0].getSuccess())
            Assert.fail("creation of task failed with error " + result[0].getErrors()[0].getMessage());
        return result[0].getId();
    }

    private String getCsvFieldValue(String csvFile, String fieldName) throws Exception {
        CSVFileReader reader = new CSVFileReader(csvFile);
        reader.open();
        assertEquals(1, reader.getTotalRows());
        String fieldValue = (String)reader.readRow().get(fieldName);
        reader.close();
        return fieldValue;
    }

    private String getUserId() throws Exception {
        QueryResult result = getController().getPartnerClient().query(
                "select id from user where username='" + getController().getConfig().getString(Config.USERNAME) + "'");
        assertEquals(1, result.getSize());
        return result.getRecords()[0].getId();
    }

    private void generateCsvWithNAField(String nullFieldName, String id) throws Exception {
         generateCsv(nullFieldName, NATextValue.getInstance(), id);
    }

    private void generateCsvWithEmptyField(String emptyFieldName, String id) throws Exception {
        generateCsv(emptyFieldName, null, id);
    }

    /**
     * We have to generate the csv file because the user id will change.
     */
    private void generateCsv(String nullFieldName, Object nullFieldValue, String id) throws Exception {
        File csvDir = new File(CSV_DIR_PATH);
        if (!csvDir.exists()) {
            boolean deleteCsvDirOk = csvDir.mkdirs();
            assertTrue("Could not delete directory: " + CSV_DIR_PATH, deleteCsvDirOk);
        }
        File csvFile = new File(CSV_FILE_PATH);
        if (csvFile.exists()) {
            boolean deleteCsvFileOk = csvFile.delete();
            assertTrue("Could not delete existing CSV file: " + CSV_FILE_PATH, deleteCsvFileOk);
        }

        Row row = new Row();
        row.put("OwnerId", userId);
        row.put("Subject", TASK_SUBJECT);
        row.put(nullFieldName, nullFieldValue);
        if (id != null) row.put("Id", id);

        CSVFileWriter writer = null;
        try {
            writer = new CSVFileWriter(CSV_FILE_PATH, DEFAULT_CHARSET);
            writer.open();
            writer.setColumnNames(new ArrayList<String>(row.keySet()));
            writer.writeRow(row);
        } finally {
            if (writer != null) writer.close();
        }
    }

    @Override
    public void cleanRecords() {
        deleteSfdcRecords("Task", "Subject='" + TASK_SUBJECT + "'", 0);
    }

}
