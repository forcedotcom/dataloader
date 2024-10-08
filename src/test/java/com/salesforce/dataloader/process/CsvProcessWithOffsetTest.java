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
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.salesforce.dataloader.TestSetting;
import com.salesforce.dataloader.TestVariant;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.csv.CSVFileReader;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import com.salesforce.dataloader.model.Row;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test class for testing data loads with different configured row offsets.
 * 
 * @author Aleksandr Shulman, Colin Jarvis
 * @since 23.0
 */
@RunWith(Parameterized.class)
public class CsvProcessWithOffsetTest extends ProcessTestBase {

    private static final int NUM_DATA_ROWS = 10;
    private static final String FILE_NAME_BASE = "upsertAccountSmall";

    public CsvProcessWithOffsetTest(Map<String, String> config) {
        super(config);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> getTestParameters() {
        return Arrays.asList(
                TestVariant.defaultSettings(),
                TestVariant.forSettings(TestSetting.BULK_API_ENABLED),
                TestVariant.forSettings(TestSetting.BULK_API_ENABLED, TestSetting.BULK_API_CACHE_DAO_UPLOAD_ENABLED)
                );
    }

    /**
     * Verify that a row offset will produce the correct effects and success file for a small set of rows (<5).
     */
    @Test
    public void testSmallUpsertWithOffset() throws Exception {

        runOffsetValueTest(1, NUM_DATA_ROWS - 1);
    }

    /**
     * Verify that if the offset is the same size as the number of rows to process, that no DML occurs.
     * 
     * @expectedResults Assert that the number of successes and failures in returned spreadsheets matches with expected
     *                  results.
     */
    @Test
    public void testOffsetResultsInNoDML() throws Exception {

        runOffsetValueTest(NUM_DATA_ROWS, 0);
    }

    /**
     * Verify that when the offset is greater than the number of rows in the CSV, no DML occurs.
     * 
     * @expectedResults Assert no inserts or updates occur.
     */
    @Test
    public void testOffsetGreaterThanRowCount() throws Exception {

        runOffsetValueTest(NUM_DATA_ROWS + 1, 0);
    }

    @Test
    public void testNonNumericOffsetValueTreatedAsZero() throws Exception {
        runOffsetValueTest("abc", NUM_DATA_ROWS);
    }

    @Test
    public void testEmptyOffsetValueTreatedAsZero() throws Exception {

        runOffsetValueTest("", NUM_DATA_ROWS);
    }

    @Test
    public void testNegativeOffsetValueTreatedAsZero() throws Exception {
        runOffsetValueTest("-5", NUM_DATA_ROWS);
    }

    private void runOffsetValueTest(Object offset, int numberOfInserts) throws Exception {

        int iOffset;
        try {
            iOffset = Integer.valueOf(String.valueOf(offset));
        } catch (NumberFormatException e) {
            iOffset = 0;
        }

        if(iOffset < 0) {
            iOffset = 0;
        }

        int expectedUpdates = Math.max(0, numberOfInserts - iOffset);
        int expectedInserts = Math.max(0, NUM_DATA_ROWS - expectedUpdates - iOffset);
        // perform the upsert
        // verify no errors occurred
        final Controller ctl = runUpsertProcess(getRowOffsetTestConfig(offset, numberOfInserts), expectedInserts,
                expectedUpdates);

        // now check offset specs
        String rowOffset = ctl.getAppConfig().getString(AppConfig.LOAD_ROW_TO_START_AT);

        if (rowOffset != null) {
            verifyOffsetFromInputAndOutputFiles(iOffset, ctl.getAppConfig());
        }
    }

    private void verifyOffsetFromInputAndOutputFiles(int numberOfOffsetRows, AppConfig cfg) throws Exception {

        // Find out how many rows each file has
        int numberOfSuccessRows = 0;
        int numberOfErrorRows = 0;
        int numberOfInputRows = 0;

        // finding rows in input file and opening it

        numberOfInputRows = getNumCsvRows(cfg, AppConfig.DAO_NAME);

        // finding rows in success file and opening it
        CSVFileReader successFileReader = openConfiguredPath(cfg, AppConfig.OUTPUT_SUCCESS);
        numberOfSuccessRows = getNumCsvRows(cfg, AppConfig.OUTPUT_SUCCESS);

        // finding rows in error file and opening it
        CSVFileReader errorFileReader = openConfiguredPath(cfg, AppConfig.OUTPUT_ERROR);
        numberOfErrorRows = getNumCsvRows(cfg, AppConfig.OUTPUT_ERROR);

        if (numberOfOffsetRows <= numberOfInputRows) {
            assertEquals("Number of lines between input and output do not match", numberOfInputRows,
                    numberOfSuccessRows + numberOfErrorRows + numberOfOffsetRows);
        }

        // Initializations of row results
        Row firstInputOffsetAdjustedRow = new Row();
        Row lastInputRow = new Row();
        Row firstSuccessRow = new Row();
        Row lastSuccessRow = new Row();
        Row firstErrorRow = new Row();
        Row lastErrorRow = new Row();

        // The next few if statements deal with the edge statements on file size...(i.e. suppose that there are no
        // errors)
        if (numberOfSuccessRows > 0) {
            getFirstRow(firstSuccessRow, successFileReader, true, 0);
            getLastRow(lastSuccessRow, successFileReader, true);
        }

        if (numberOfErrorRows > 0) {
            getFirstRow(firstErrorRow, errorFileReader, false, 0);
            getLastRow(lastErrorRow, errorFileReader, false);
        }

        if (numberOfInputRows > 0) {
            final CSVFileReader inputFileReader = openConfiguredPath(cfg, AppConfig.DAO_NAME);

            getFirstRow(firstInputOffsetAdjustedRow, inputFileReader, false, numberOfOffsetRows);
            getLastRow(lastInputRow, inputFileReader, false);
        }

        // Requirement I: First offset-adjusted row of input matches to either the error or success file's first row
        if (numberOfSuccessRows > 0 || numberOfErrorRows > 0) {
            assertTrue(firstInputOffsetAdjustedRow.get("NAME").equals(firstSuccessRow.get("NAME"))
                    || firstInputOffsetAdjustedRow.get("NAME").equals(firstErrorRow.get("NAME")));

            // Requirement II: Last input row matches to either the error or success file's last row
            assertTrue(lastInputRow.get("NAME").equals(lastSuccessRow.get("NAME"))
                    || lastInputRow.get("NAME").equals(lastErrorRow.get("NAME")));
        } //otherwise vacuously true
    }

    private int getNumCsvRows(AppConfig cfg, String setting) throws DataAccessObjectException {
        final CSVFileReader rdr = openConfiguredPath(cfg, setting);
        try {
            return rdr.getTotalRows();
        } finally {
            rdr.close();
        }
    }

    private CSVFileReader openConfiguredPath(AppConfig cfg, String configSetting)
            throws DataAccessObjectInitializationException {
        final CSVFileReader rdr = new CSVFileReader(new File(cfg.getString(configSetting)), cfg, false, false);
        rdr.open();
        return rdr;
    }

    private void getFirstRow(Row rowResult, CSVFileReader reader, boolean isSuccessFile, int rowOffset)
            throws Exception {
        Row firstRow = reader.readRow();

        for (int i = 0; i < rowOffset; i++) {
            firstRow = reader.readRow(); // then, for each, move down one row
        }

        if (isSuccessFile) {
            // Also ask for ID
            rowResult.put("ID", firstRow.get("ID"));
        }
        if (firstRow != null && firstRow.get("NAME") != null) {
            rowResult.put("NAME", firstRow.get("NAME"));
        }
    }

    private void getLastRow(Row rowResult, CSVFileReader reader, boolean isSuccessFile)
            throws Exception {

        Row tempRow = new Row();
        Row lastRow = new Row();

        // get to the last row:
        while ((tempRow = reader.readRow()) != null) {
            lastRow = tempRow;
        }

        if (isSuccessFile) {
            // Also ask for ID
            rowResult.put("ID", lastRow.get("ID"));
        }

        rowResult.put("NAME", lastRow.get("NAME"));
    }

    private Map<String, String> getRowOffsetTestConfig(Object offset, int numInserts) throws DataAccessObjectException {
        final Map<String, String> argMap = getUpdateTestConfig(FILE_NAME_BASE, true, DEFAULT_ACCOUNT_EXT_ID_FIELD, numInserts);
        argMap.put(AppConfig.LOAD_ROW_TO_START_AT, offset.toString());
        return argMap;
    }
}
