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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.csv.CSVFileWriter;
import com.salesforce.dataloader.model.Row;
import com.salesforce.dataloader.util.AppUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BulkV1CsvProcessTest extends ProcessTestBase {

    private static final String TASK_SUBJECT = "BulkV1CsvProcessTest";
    private static final String TARGET_DIR = getProperty("target.dir").trim();
    private static final String CSV_DIR_PATH = TARGET_DIR + File.separator + "BatchTests";
    private static final String CSV_FILE_PATH = CSV_DIR_PATH + File.separator + "BatchTests.csv";
    private static Row validRow;
    private static Row invalidRow;
    private Map<String, String> argMap;

    @BeforeClass
    public static void setUpData() {
        validRow = new Row();
        validRow.put("Subject", TASK_SUBJECT);
        validRow.put("ReminderDateTime", "");

        invalidRow = new Row();
        invalidRow.put("Subject", TASK_SUBJECT);
        invalidRow.put("ReminderDateTime", "NULL"); // this makes date conversion fail
    }

    @Before
    public void createArgMap() {
        argMap = getTestConfig(OperationInfo.insert, CSV_FILE_PATH, getTestDataDir() + File.separator + "NAProcessTest.sdl", false);
        argMap.put(Config.ENTITY, "Task");
        argMap.remove(Config.EXTERNAL_ID_FIELD);
        argMap.put(Config.BULK_API_ENABLED, Boolean.TRUE.toString());
    }

    @Test
    public void testBatchSizes() throws Exception {
        writeCsv(validRow, validRow);
        argMap.put(Config.LOAD_BATCH_SIZE, "1");
        ILoaderProgress monitor = runProcess(argMap, 2, 0, 0, false);
        assertEquals("Inserting 2 rows with batch size of 1 should have produced 2 batches", 2, monitor.getNumberBatchesTotal());
    }

    @Test
    public void testBatchSizesNotAlteredByInvalidData() throws Exception {
        writeCsv(validRow, invalidRow, validRow);
        argMap.put(Config.LOAD_BATCH_SIZE, "2");
        ILoaderProgress monitor = runProcess(argMap, 2, 0, 1, false);
        assertEquals("Even though middle row contains invalid data only 1 batch should have been created", 1, monitor.getNumberBatchesTotal());
    }

    private ILoaderProgress runProcess(Map<String, String> argMap, int numInserts, int numUpdates, int numFailures, boolean emptyId) throws Exception {

        final IProcess runner = this.runBatchProcess(argMap);
        ILoaderProgress monitor = runner.getMonitor();
        Controller controller = runner.getController();

        assertTrue("Process failed", monitor.isSuccess());
        verifyFailureFile(controller, numFailures);
        verifySuccessFile(controller, numInserts, numUpdates, emptyId);
        return monitor;
    }

    private void writeCsv(Row... rows) throws Exception {
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

        CSVFileWriter writer = null;
        try {
            writer = new CSVFileWriter(CSV_FILE_PATH, getController().getConfig(), AppUtil.COMMA);
            writer.open();
            writer.setColumnNames(new ArrayList<String>(rows[0].keySet()));
            writer.writeRowList(Arrays.asList(rows));
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
