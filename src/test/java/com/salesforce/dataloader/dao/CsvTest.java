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
package com.salesforce.dataloader.dao;

import com.salesforce.dataloader.TestBase;
import com.salesforce.dataloader.dao.csv.CSVFileReader;
import com.salesforce.dataloader.dao.csv.CSVFileWriter;
import com.salesforce.dataloader.model.Row;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class CsvTest extends TestBase {

    private static final String COLUMN_1_NAME = "column1";
    private static final String COLUMN_2_NAME = "column2";
    private static final String COLUMN_3_NAME = "column3";
    private List<String> writeHeader;
    private Row row1;
    private Row row2;

    @Before
    public void createTestData() {
        writeHeader = new ArrayList<String>(3);
        writeHeader.add("COL1");
        writeHeader.add("COL2");
        writeHeader.add("COL3");

        row1 = new Row();
        row1.put("COL1", "row1col1");
        row1.put("COL2", "row1col2");
        row1.put("COL3", "row1col3");

        row2 = new Row();
        row2.put("COL1", "row2col1");
        row2.put("COL2", "row2col2");
        row2.put("COL3", "row2col3");
    }

    /**
     * Basic Test for CSV Reading
     */
    @Test
    public void testCSVReadBasic() throws Exception {
        File f = new File(getTestDataDir(), "csvtext.csv");
        assertTrue(f.exists());
        assertTrue(f.canRead());

        CSVFileReader csv = new CSVFileReader(f);
        csv.open();

        List<String> headerRow = csv.getColumnNames();
        assertEquals(COLUMN_1_NAME, headerRow.get(0));
        assertEquals(COLUMN_2_NAME, headerRow.get(1));
        assertEquals(COLUMN_3_NAME, headerRow.get(2));

        Row firstRow = csv.readRow();
        assertEquals("row1-1", firstRow.get(COLUMN_1_NAME));
        assertEquals("row1-2", firstRow.get(COLUMN_2_NAME));
        assertEquals("row1-3", firstRow.get(COLUMN_3_NAME));

        Row secondRow = csv.readRow();
        assertEquals("row2-1", secondRow.get(COLUMN_1_NAME));
        assertEquals("row2-2", secondRow.get(COLUMN_2_NAME));
        assertEquals("row2-3", secondRow.get(COLUMN_3_NAME));

        csv.close();
    }


    /**
     * Basic test for CSV Writing
     */
    @Test
    public void testCSVWriteBasic() throws Exception {
        File f = new File(getTestDataDir(), "csvtestTemp.csv");
        String path = f.getAbsolutePath();
        CSVFileWriter writer = new CSVFileWriter(path, DEFAULT_CHARSET);
        List<Row> rowList = new ArrayList<Row>();

        rowList.add(row1);
        rowList.add(row2);

        writer.open();
        writer.setColumnNames(writeHeader);

        writer.writeRowList(rowList);
        writer.close();

        compareWriterFile(path);

        f.delete();
    }


    /**
     * Helper to compare the static variables to the csv we wrote
     *
     * @param filePath
     */

    private void compareWriterFile(String filePath) throws Exception {
        CSVFileReader csv = new CSVFileReader(filePath);
        csv.open();

        //check that the header is the same as what we wanted to write
        List<String> headerRow = csv.getColumnNames();
        for (int i = 0; i < writeHeader.size(); i++) {
            assertEquals(headerRow.get(i), writeHeader.get(i));
        }

        //check that row 1 is valid
        Row firstRow = csv.readRow();
        for (String headerColumn : writeHeader) {
            assertEquals(row1.get(headerColumn), firstRow.get(headerColumn));
        }

        //check that row 2 is valid
        Row secondRow = csv.readRow();
        for (String headerColumn : writeHeader) {
            assertEquals(row2.get(headerColumn), secondRow.get(headerColumn));
        }
        csv.close();
    }
}




