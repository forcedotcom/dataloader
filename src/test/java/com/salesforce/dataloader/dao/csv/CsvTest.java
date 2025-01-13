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
package com.salesforce.dataloader.dao.csv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.salesforce.dataloader.ConfigTestBase;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.dao.csv.CSVFileReader;
import com.salesforce.dataloader.dao.csv.CSVFileWriter;
import com.salesforce.dataloader.model.RowInterface;
import com.salesforce.dataloader.model.TableHeader;
import com.salesforce.dataloader.model.TableRow;
import com.salesforce.dataloader.util.AppUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CsvTest extends ConfigTestBase {

    private static final String COLUMN_1_NAME = "column1";
    private static final String COLUMN_2_NAME = "column2";
    private static final String COLUMN_3_NAME = "column3";
    private List<String> writeHeader;
    private TableRow row1;
    private TableRow row2;

    @Before
    public void createTestData() {
        writeHeader = new ArrayList<String>(3);
        writeHeader.add("column1");
        writeHeader.add("column2");
        writeHeader.add("column3");

        TableHeader header = new TableHeader(writeHeader);
        row1 = new TableRow(header);
        row1.put(writeHeader.get(0), "row1-1");
        row1.put(writeHeader.get(1), "row1-2");
        row1.put(writeHeader.get(2), "row1-3");

        row2 = new TableRow(header);
        row2.put(writeHeader.get(0), "row2-1");
        row2.put(writeHeader.get(1), "row2-2");
        row2.put(writeHeader.get(2), "row2-3");
    }
    @Test
    public void testCSVReadBasic() throws Exception {
        testCSVReadBasic("csvtext.csv");
    }
    
    @Test
    public void testCSVReadUTF8BOMBasic() throws Exception{
        testCSVReadBasic("csvtext_BOM_UTF8.csv");
        assertTrue("did not find BOM in " + getTestDataDir() + "/csvtext_BOM_UTF8.csv",
                    hasBOM(getTestDataDir() + "/csvtext_BOM_UTF8.csv"));
    }
    
    @Test
    public void testCSVReadUTF16BEBOMBasic() throws Exception{
        getController().getAppConfig().setValue(AppConfig.PROP_READ_CHARSET, "UTF-16BE");
        testCSVReadBasic("csvtext_BOM_UTF16BE.csv");
        assertTrue("did not find BOM in " + getTestDataDir() + "/csvtext_BOM_UTF16BE.csv",
                    hasBOM(getTestDataDir() + "/csvtext_BOM_UTF16BE.csv"));
        getController().getAppConfig().setValue(AppConfig.PROP_READ_CHARSET, "");
    }
    
    @Test
    public void testCSVReadUTF16LEBOMBasic() throws Exception{
        getController().getAppConfig().setValue(AppConfig.PROP_READ_CHARSET, "UTF-16LE");
        testCSVReadBasic("csvtext_BOM_UTF16LE.csv");
        assertTrue("did not find BOM in " + getTestDataDir() + "/csvtext_BOM_UTF16LE.csv",
                hasBOM(getTestDataDir() + "/csvtext_BOM_UTF16LE.csv"));
       getController().getAppConfig().setValue(AppConfig.PROP_READ_CHARSET, "");
    }
    
    @Test
    public void testCSVReadUTF32LEBOMBasic() throws Exception{
        getController().getAppConfig().setValue(AppConfig.PROP_READ_CHARSET, "UTF-32LE");
        testCSVReadBasic("csvtext_BOM_UTF32LE.csv");
        assertTrue("did not find BOM in " + getTestDataDir() + "/csvtext_BOM_UTF32LE.csv",
                hasBOM(getTestDataDir() + "/csvtext_BOM_UTF32LE.csv"));
        getController().getAppConfig().setValue(AppConfig.PROP_READ_CHARSET, "");
    }
    
    @Test
    public void testCSVReadUTF32BEBOMBasic() throws Exception{
        getController().getAppConfig().setValue(AppConfig.PROP_READ_CHARSET, "UTF-32BE");
        testCSVReadBasic("csvtext_BOM_UTF32BE.csv");
        assertTrue("did not find BOM in " + getTestDataDir() + "/csvtext_BOM_UTF32BE.csv",
                hasBOM(getTestDataDir() + "/csvtext_BOM_UTF32BE.csv"));
        getController().getAppConfig().setValue(AppConfig.PROP_READ_CHARSET, "");
    }

    private void testCSVReadBasic(String csvFile) throws Exception {
        File f = new File(getTestDataDir(), csvFile);
        assertTrue(f.exists());
        assertTrue(f.canRead());

        CSVFileReader csv = new CSVFileReader(f, getController().getAppConfig(), false, false);
        csv.open();

        List<String> headerRow = csv.getColumnNames();
        assertEquals(COLUMN_1_NAME, headerRow.get(0));
        assertEquals(COLUMN_2_NAME, headerRow.get(1));
        assertEquals(COLUMN_3_NAME, headerRow.get(2));

        TableRow firstRow = csv.readTableRow();
        assertEquals("row1-1", firstRow.get(COLUMN_1_NAME));
        assertEquals("row1-2", firstRow.get(COLUMN_2_NAME));
        assertEquals("row1-3", firstRow.get(COLUMN_3_NAME));

        TableRow secondRow = csv.readTableRow();
        assertEquals("row2-1", secondRow.get(COLUMN_1_NAME));
        assertEquals("row2-2", secondRow.get(COLUMN_2_NAME));
        assertEquals("row2-3", secondRow.get(COLUMN_3_NAME));

        csv.close();
    }
    

    public static boolean hasBOM(String filePath) throws IOException {
        try (InputStream is = new FileInputStream(filePath)) {
            byte[] bom = new byte[3];
            if (is.read(bom) == 3) {
                boolean bomFound = false;
                // UTF-8 case
                bomFound = bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF;
                if (!bomFound) {
                    // UTF-16BE, UTF-32BE, UTF-32LE cases
                    bomFound = bom[0] == (byte)0xFE && bom[1] == (byte) 0xFF;
                }
                if (!bomFound) {
                    // UTF16-LE case
                    bomFound = bom[0] == (byte)0xFF && bom[1] == (byte) 0xFE;
                }
                return bomFound;
            }
        }
        return false;
    }

    @Test
    public void testCSVWriteBasic() throws Exception {
        doTestCSVWriteBasic(AppUtil.COMMA);
    }
    
    @Test
    public void testCSVWriteUTF8BOMBasic() throws Exception{
        getController().getAppConfig().setValue(AppConfig.PROP_READ_CHARSET, "UTF-8");
        getController().getAppConfig().setValue(AppConfig.PROP_WRITE_CHARSET, "UTF-8");
        doTestCSVWriteBasic(AppUtil.COMMA);
    }
    
    @Test
    public void testCSVWriteUTF16LEBOMBasic() throws Exception{
        getController().getAppConfig().setValue(AppConfig.PROP_READ_CHARSET, "UTF-16LE");
        getController().getAppConfig().setValue(AppConfig.PROP_WRITE_CHARSET, "UTF-16LE");
        doTestCSVWriteBasic(AppUtil.COMMA);
    }
    
    @Test
    public void testCSVWriteUTF16BEBOMBasic() throws Exception{
        getController().getAppConfig().setValue(AppConfig.PROP_READ_CHARSET, "UTF-16BE");
        getController().getAppConfig().setValue(AppConfig.PROP_WRITE_CHARSET, "UTF-16BE");
        doTestCSVWriteBasic(AppUtil.COMMA);
    }   
    
    @Test
    public void testCSVWriteUTF32LEBOMBasic() throws Exception{
        getController().getAppConfig().setValue(AppConfig.PROP_READ_CHARSET, "UTF-32LE");
        getController().getAppConfig().setValue(AppConfig.PROP_WRITE_CHARSET, "UTF-32LE");
        doTestCSVWriteBasic(AppUtil.COMMA);
    }
    
    @Test
    public void testCSVWriteUTF32BEBOMBasic() throws Exception{
        getController().getAppConfig().setValue(AppConfig.PROP_READ_CHARSET, "UTF-32BE");
        getController().getAppConfig().setValue(AppConfig.PROP_WRITE_CHARSET, "UTF-32BE");
        doTestCSVWriteBasic(AppUtil.COMMA);
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
        doTestCSVWriteBasic(AppUtil.TAB);
    }
    
    private String writeCSVFilename = getTestDataDir() + "/csvtestTemp.csv";
    private void doTestCSVWriteBasic(String delimiter) throws Exception {
        File f = new File(writeCSVFilename);
        String path = f.getAbsolutePath();
        CSVFileWriter writer = new CSVFileWriter(path, getController().getAppConfig(), delimiter);
        List<RowInterface> rowList = new ArrayList<RowInterface>();

        rowList.add(row1);
        rowList.add(row2);

        writer.open();
        writer.setColumnNames(writeHeader);

        writer.writeRowList(rowList);
        writer.close();
        assertTrue("did not find BOM in " + path, hasBOM(path));

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
        getController().getAppConfig().setValue("loader.csvOther", true);
        getController().getAppConfig().setValue("loader.csvOtherValue", "!");

        CSVFileReader csv = new CSVFileReader(f, getController().getAppConfig(), false, false);
        csv.open();
        TableRow firstRow = csv.readTableRow();
        assertEquals("somev1", firstRow.get("some"));
        assertEquals(4, firstRow.getNonEmptyCellsCount());
        TableRow secondRow = csv.readTableRow();
        assertEquals("somev2", secondRow.get("some"));
        csv.close();

        getController().getAppConfig().setValue("loader.csvOther", false);
        csv = new CSVFileReader(f, getController().getAppConfig(), false, false);
        csv.open();
        firstRow = csv.readTableRow();
        assertEquals("col12!somev1", firstRow.get("column2!some"));
        assertEquals(3, firstRow.getNonEmptyCellsCount());
        csv.close();
    }

    @Test
    public void testReadingEscapedValues() throws Exception {
        File f = new File(getTestDataDir(), "csvEscapedQuotes.csv");
        assertTrue(f.exists());
        assertTrue(f.canRead());

        CSVFileReader csv = new CSVFileReader(f, getController().getAppConfig(), false, false);
        csv.open();

        TableRow firstRow = csv.readTableRow();
        assertEquals("\"The Best\" Account", firstRow.get(COLUMN_1_NAME));

        TableRow secondRow = csv.readTableRow();
        assertEquals("The \"Best\" Account", secondRow.get(COLUMN_1_NAME));

        csv.close();
    }

    @Test
    public void testCsvWithManyRowsCanBeParsed() throws Exception {
        CSVFileReader csvFileReader = new CSVFileReader(new File(getTestDataDir(), "20kRows.csv"), getController().getAppConfig(), false, false);
        csvFileReader.open();
        assertEquals(20000, csvFileReader.getTotalRows());
        int count = 0;
        for(TableRow row = csvFileReader.readTableRow(); row != null; row = csvFileReader.readTableRow(), count++);
        assertEquals(20000, count);
    }

    /**
     * Helper to compare the static variables to the csv we wrote
     *
     * @param filePath
     */

    private void compareWriterFile(String filePath, String delimiterStr, boolean ignoreDelimiterConfig, boolean isQueryResultsCSV) throws Exception {
        AppConfig appConfig = getController().getAppConfig();
        String storedDelimiter;
        boolean storedCsvDelimiterComma = false, storedCsvDelimiterTab = false, storedCsvDelimiterOther = false;
        if (isQueryResultsCSV) {
            storedDelimiter = appConfig.getString(AppConfig.PROP_CSV_DELIMITER_FOR_QUERY_RESULTS);
            appConfig.setValue(AppConfig.PROP_CSV_DELIMITER_FOR_QUERY_RESULTS, delimiterStr);
        } else {
            storedDelimiter = appConfig.getString(AppConfig.PROP_CSV_DELIMITER_OTHER_VALUE);
            storedCsvDelimiterComma = appConfig.getBoolean(AppConfig.PROP_CSV_DELIMITER_COMMA);
            storedCsvDelimiterTab = appConfig.getBoolean(AppConfig.PROP_CSV_DELIMITER_TAB);
            storedCsvDelimiterOther = appConfig.getBoolean(AppConfig.PROP_CSV_DELIMITER_OTHER);
            appConfig.setValue(AppConfig.PROP_CSV_DELIMITER_COMMA, false);
            appConfig.setValue(AppConfig.PROP_CSV_DELIMITER_TAB, false);
            appConfig.setValue(AppConfig.PROP_CSV_DELIMITER_OTHER, false);
            appConfig.setValue(AppConfig.PROP_CSV_DELIMITER_OTHER_VALUE, delimiterStr);
            if (AppUtil.COMMA.equals(delimiterStr)) {
                appConfig.setValue(AppConfig.PROP_CSV_DELIMITER_COMMA, true);
            } else if ("    ".equals(delimiterStr)) {
                appConfig.setValue(AppConfig.PROP_CSV_DELIMITER_TAB, true);
            } else {
                appConfig.setValue(AppConfig.PROP_CSV_DELIMITER_OTHER, true);
                storedDelimiter = appConfig.getString(AppConfig.PROP_CSV_DELIMITER_OTHER_VALUE);
                appConfig.setValue(AppConfig.PROP_CSV_DELIMITER_OTHER_VALUE, delimiterStr);
            }
        }
        appConfig.setValue(AppConfig.PROP_CSV_DELIMITER_FOR_QUERY_RESULTS, delimiterStr);
        CSVFileReader csv = new CSVFileReader(new File(filePath), appConfig, ignoreDelimiterConfig, isQueryResultsCSV);
        try {
            csv.open();
        } catch (Exception e) {
            assertTrue("Exception reading header row: " + e.getMessage(), ignoreDelimiterConfig && !delimiterStr.equals(AppUtil.COMMA));
            csv.close();
            return;
        }
        //check that the header is the same as what we wanted to write
        List<String> headerRow = csv.getColumnNames();
        for (int i = 0; i < writeHeader.size(); i++) {
            assertEquals(headerRow.get(i), writeHeader.get(i));
        }

        //check that row 1 is valid
        TableRow firstRow = csv.readTableRow();
        for (String headerColumn : writeHeader) {
            assertEquals(row1.get(headerColumn), firstRow.get(headerColumn));
        }

        //check that row 2 is valid
        TableRow secondRow = csv.readTableRow();
        for (String headerColumn : writeHeader) {
            assertEquals(row2.get(headerColumn), secondRow.get(headerColumn));
        }
        csv.close();
        if (isQueryResultsCSV) {
            appConfig.setValue(AppConfig.PROP_CSV_DELIMITER_FOR_QUERY_RESULTS, storedDelimiter);
        } else {
            appConfig.setValue(AppConfig.PROP_CSV_DELIMITER_OTHER_VALUE, storedDelimiter);
            appConfig.setValue(AppConfig.PROP_CSV_DELIMITER_COMMA, storedCsvDelimiterComma);
            appConfig.setValue(AppConfig.PROP_CSV_DELIMITER_TAB, storedCsvDelimiterTab);
            appConfig.setValue(AppConfig.PROP_CSV_DELIMITER_OTHER, storedCsvDelimiterOther);
        }
    }
}
