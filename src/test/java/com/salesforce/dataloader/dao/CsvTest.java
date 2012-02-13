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

import java.io.File;
import java.util.*;

import com.salesforce.dataloader.TestBase;
import com.salesforce.dataloader.dao.csv.CSVFileReader;
import com.salesforce.dataloader.dao.csv.CSVFileWriter;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;

/**
 * 
 */
public class CsvTest extends TestBase {

    public CsvTest(String name) {
        super(name);
    }

    /**
     * Basic Test for CSV Reading
     *
     */

    public void testCSVReadBasic() {
        File f = new File(getTestDataDir(), "csvtext.csv");
        assertTrue(f.exists());
        assertTrue(f.canRead());

        CSVFileReader csv = new CSVFileReader(f);
        try {
            csv.open();
        } catch (DataAccessObjectInitializationException e) {
            fail("Exception has been caught, error: " + e.getMessage());
        }

        // check that the header row is correctly returned.
        List<String> headerRow = csv.getColumnNames();
        assertEquals("column1", headerRow.get(0));
        assertEquals("column2", headerRow.get(1));
        assertEquals("column3", headerRow.get(2));

        // validate the first row
        try {
            Map<String, Object> firstRow = csv.readRow();
            assertEquals("row1-1", (String) firstRow.get("column1"));
            assertEquals("row1-2", (String) firstRow.get("column2"));
            assertEquals("row1-3", (String) firstRow.get("column3"));
        } catch (DataAccessObjectException e) {
            fail("Exception has been caught, error: " + e.getMessage());
        }

        // validate the second row
        try {
            Map<String, Object> firstRow = csv.readRow();
            assertEquals("row2-1", (String) firstRow.get("column1"));
            assertEquals("row2-2", (String) firstRow.get("column2"));
            assertEquals("row2-3", (String) firstRow.get("column3"));
        } catch (DataAccessObjectException e) {
            fail("Exception has been caught, error: " + e.getMessage());
        }

        csv.close();

    }

    /**
     * Basic test for CSV Writing
     *
     */

    public synchronized void testCSVWriteBasic() {
        File f = new File(getTestDataDir(), "csvtestTemp.csv");
        String path = f.getAbsolutePath();
        CSVFileWriter writer = new CSVFileWriter(path);
        List<Map<String,Object>> rowList = new ArrayList<Map<String,Object>>();

        rowList.add(row1);
        rowList.add(row2);

        try {
            writer.open();
            writer.setColumnNames(writeHeader);
        } catch (DataAccessObjectInitializationException e) {
            fail("Exception has been caught, error: " + e.getMessage());
        }

        try {
            writer.writeRowList(rowList);
        } catch (DataAccessObjectException e) {
            fail("Exception has been caught, error: " + e.getMessage());
        }
        writer.close();

        compareWriterFile(path);

        f.delete();
    }


    /**
     * Helper to compare the static variables to the csv we wrote
     * @param filePath
     */

    private void compareWriterFile(String filePath) {
        CSVFileReader csv = new CSVFileReader(filePath);
        try {
            csv.open();
        } catch (DataAccessObjectInitializationException e) {
            fail("Exception has been caught, error: " + e.getMessage());
        }

        //check that the header is the same as what we wanted to write
        List<String> headerRow = csv.getColumnNames();
        for (int i = 0; i < writeHeader.size(); i ++) {
            assertEquals(headerRow.get(i), writeHeader.get(i));
        }

        //check that row 1 is valid
        try {
            Map<String, Object> nextRow = csv.readRow();
            for (String headerColumn : writeHeader) {
                assertEquals(row1.get(headerColumn), nextRow.get(headerColumn));
            }
        } catch (DataAccessObjectException e) {
            fail("Exception has been caught, error: " + e.getMessage());
        }

        //check that row 2 is valid
        try {
            Map<String, Object> nextRow = csv.readRow();
            for (String headerColumn : writeHeader) {
                assertEquals(row2.get(headerColumn), nextRow.get(headerColumn));
            }
        } catch (DataAccessObjectException e) {
            fail("Exception has been caught, error: " + e.getMessage());
        }

        csv.close();
    }


    private static List<String> writeHeader;
    private static Map<String,Object> row1;
    private static Map<String,Object> row2;

    static {
        writeHeader = new ArrayList<String>();
        writeHeader.add("COL1");
        writeHeader.add("COL2");
        writeHeader.add("COL3");

        row1 = new HashMap<String,Object>();
        row1.put("COL1", "row1col1");
        row1.put("COL2", "row1col2");
        row1.put("COL3", "row1col3");

        row2 = new HashMap<String,Object>();
        row2.put("COL1", "row2col1");
        row2.put("COL2", "row2col2");
        row2.put("COL3", "row2col3");
    }
}




