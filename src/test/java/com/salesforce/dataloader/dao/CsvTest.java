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
package com.salesforce.dataloader.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.salesforce.dataloader.ConfigTestBase;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.dao.csv.CSVFileReader;
import com.salesforce.dataloader.dao.csv.CSVFileWriter;
import com.salesforce.dataloader.model.Row;

import org.junit.Assert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CsvTest extends ConfigTestBase {

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
    @Test
    public void testCSVReadBasic() throws Exception {
        testCSVReadBasic("csvtext.csv");
    }
    
    @Test
    public void testCSVReadUTF8BOMBasic() throws Exception{
        testCSVReadBasic("csvtext_BOM_UTF8.csv");
    }
    
    @Test
    public void testCSVReadUTF16BEBOMBasic() throws Exception{
        getController().getConfig().setValue(Config.READ_CHARSET, "UTF-16BE");
        testCSVReadBasic("csvtext_BOM_UTF16BE.csv");
        getController().getConfig().setValue(Config.READ_CHARSET, "");
    }
    
    @Test
    public void testCSVReadUTF16LEBOMBasic() throws Exception{
        getController().getConfig().setValue(Config.READ_CHARSET, "UTF-16LE");
        testCSVReadBasic("csvtext_BOM_UTF16LE.csv");
        getController().getConfig().setValue(Config.READ_CHARSET, "");
    }

    private void testCSVReadBasic(String csvFile) throws Exception {
        File f = new File(getTestDataDir(), csvFile);
        assertTrue(f.exists());
        assertTrue(f.canRead());

        CSVFileReader csv = new CSVFileReader(f, getController().getConfig(), false, false);
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

    @Test
    public void testCSVWriteBasic() throws Exception {
        doTestCSVWriteBasic(",");
    }
    
    @Test
    public void testCSVWriteBasicWithDashDelimiter() throws Exception {
        doTestCSVWriteBasic("-");
    }
    
    @Test
    public void testCSVWriteBasicWithColonDelimiter() throws Exception {
        doTestCSVWriteBasic(":");
    }
    
    @Test
    public void testCSVWriteBasicWithTabDelimiter() throws Exception {
        doTestCSVWriteBasic("   ");
    }
    
    private void doTestCSVWriteBasic(String delimiter) throws Exception {
        File f = new File(getTestDataDir(), "csvtestTemp.csv");
        String path = f.getAbsolutePath();
        CSVFileWriter writer = new CSVFileWriter(path, getController().getConfig(), delimiter);
        List<Row> rowList = new ArrayList<Row>();

        rowList.add(row1);
        rowList.add(row2);

        writer.open();
        writer.setColumnNames(writeHeader);

        writer.writeRowList(rowList);
        writer.close();

        compareWriterFile(path, delimiter, false, false); // 3rd param false and 4th param false => CSV for a upload
        compareWriterFile(path, delimiter, false, true);  // 3rd param false and 4th param true => query result CSV
        compareWriterFile(path, delimiter, true, true);   // 3rd param is set to true => upload result CSV
        compareWriterFile(path, delimiter, true, false);  // 3rd param is set to true => upload result CSV
        f.delete();
    }
    
    @Test
    public void testReadingSeparatedValues () throws Exception {
        File f = new File(getTestDataDir(), "csvSeparator.csv");
        assertTrue(f.exists());
        assertTrue(f.canRead());
        getController().getConfig().setValue("loader.csvOther", true);
        getController().getConfig().setValue("loader.csvOtherValue", "!");

        CSVFileReader csv = new CSVFileReader(f, getController().getConfig(), false, false);
        csv.open();
        Row firstRow = csv.readRow();
        assertEquals("somev1", firstRow.get("some"));
        assertEquals(4, firstRow.size());
        Row secondRow = csv.readRow();
        assertEquals("somev2", secondRow.get("some"));
        csv.close();

        getController().getConfig().setValue("loader.csvOther", false);
        csv = new CSVFileReader(f, getController().getConfig(), false, false);
        csv.open();
        firstRow = csv.readRow();
        assertEquals("col12!somev1", firstRow.get("column2!some"));
        assertEquals(3, firstRow.size());
        csv.close();
    }

    @Test
    public void testReadingEscapedValues() throws Exception {
        File f = new File(getTestDataDir(), "csvEscapedQuotes.csv");
        assertTrue(f.exists());
        assertTrue(f.canRead());

        CSVFileReader csv = new CSVFileReader(f, getController().getConfig(), false, false);
        csv.open();

        Row firstRow = csv.readRow();
        assertEquals("\"The Best\" Account", firstRow.get(COLUMN_1_NAME));

        Row secondRow = csv.readRow();
        assertEquals("The \"Best\" Account", secondRow.get(COLUMN_1_NAME));

        csv.close();
    }

    @Test
    public void testCsvWithManyRowsCanBeParsed() throws Exception {
        CSVFileReader csvFileReader = new CSVFileReader(new File(getTestDataDir(), "20kRows.csv"), getController().getConfig(), false, false);
        csvFileReader.open();
        assertEquals(20000, csvFileReader.getTotalRows());
        int count = 0;
        for(Row row = csvFileReader.readRow(); row != null; row = csvFileReader.readRow(), count++);
        assertEquals(20000, count);
    }

    /**
     * Helper to compare the static variables to the csv we wrote
     *
     * @param filePath
     */

    private void compareWriterFile(String filePath, String delimiterStr, boolean ignoreDelimiterConfig, boolean isQueryResultsCSV) throws Exception {
        Config config = getController().getConfig();
        String storedDelimiter;
        boolean storedCsvDelimiterComma = false, storedCsvDelimiterTab = false, storedCsvDelimiterOther = false;
        if (isQueryResultsCSV) {
            storedDelimiter = config.getString(Config.CSV_DELIMITER_FOR_QUERY_RESULTS);
            config.setValue(Config.CSV_DELIMITER_FOR_QUERY_RESULTS, delimiterStr);
        } else {
            storedDelimiter = config.getString(Config.CSV_DELIMETER_OTHER_VALUE);
            storedCsvDelimiterComma = config.getBoolean(Config.CSV_DELIMETER_COMMA);
            storedCsvDelimiterTab = config.getBoolean(Config.CSV_DELIMETER_TAB);
            storedCsvDelimiterOther = config.getBoolean(Config.CSV_DELIMETER_OTHER);
            config.setValue(Config.CSV_DELIMETER_COMMA, false);
            config.setValue(Config.CSV_DELIMETER_TAB, false);
            config.setValue(Config.CSV_DELIMETER_OTHER, false);
            config.setValue(Config.CSV_DELIMETER_OTHER_VALUE, delimiterStr);
            if (",".equals(delimiterStr)) {
                config.setValue(Config.CSV_DELIMETER_COMMA, true);
            } else if ("    ".equals(delimiterStr)) {
                config.setValue(Config.CSV_DELIMETER_TAB, true);
            } else {
                config.setValue(Config.CSV_DELIMETER_OTHER, true);
                storedDelimiter = config.getString(Config.CSV_DELIMETER_OTHER_VALUE);
                config.setValue(Config.CSV_DELIMETER_OTHER_VALUE, delimiterStr);
            }
        }
        config.setValue(Config.CSV_DELIMITER_FOR_QUERY_RESULTS, delimiterStr);
        CSVFileReader csv = new CSVFileReader(new File(filePath), config, ignoreDelimiterConfig, isQueryResultsCSV);
        try {
            csv.open();
        } catch (Exception e) {
            assertTrue("Exception reading header row: " + e.getMessage(), ignoreDelimiterConfig && !delimiterStr.equals(","));
            csv.close();
            return;
        }
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
        if (isQueryResultsCSV) {
            config.setValue(Config.CSV_DELIMITER_FOR_QUERY_RESULTS, storedDelimiter);
        } else {
            config.setValue(Config.CSV_DELIMETER_OTHER_VALUE, storedDelimiter);
            config.setValue(Config.CSV_DELIMETER_COMMA, storedCsvDelimiterComma);
            config.setValue(Config.CSV_DELIMETER_TAB, storedCsvDelimiterTab);
            config.setValue(Config.CSV_DELIMETER_OTHER, storedCsvDelimiterOther);
        }
    }
}
