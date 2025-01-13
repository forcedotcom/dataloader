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
import com.salesforce.dataloader.config.AppConfig; import com.salesforce.dataloader.model.TableHeader; import com.salesforce.dataloader.model.TableRow; import com.salesforce.dataloader.util.AppUtil; import org.junit.Before; import org.junit.Test;
import java.io.File; import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static org.junit.Assert.*;

public class CSVFileTest {
    private CSVFileWriter csvFileWriter;
    private CSVFileReader csvFileReader;
    private List<String> header;
    private TableRow row1;
    private TableRow row2;
    private String writeCSVFilename;
    
    @Before
    public void setUp() throws Exception {
        writeCSVFilename = "testCSVFile.csv";
        AppConfig appConfig = AppConfig.getInstance(new HashMap<String, String>());
        csvFileWriter = new CSVFileWriter(writeCSVFilename, appConfig, AppUtil.COMMA);
    
        header = new ArrayList<>();
        header.add("column1");
        header.add("column2");
        header.add("column3");
    
        TableHeader tableHeader = new TableHeader(header);
        row1 = new TableRow(tableHeader);
        row1.put("column1", "value1");
        row1.put("column2", "value2");
        row1.put("column3", "value3");
    
        row2 = new TableRow(tableHeader);
        row2.put("column1", "value4");
        row2.put("column2", "value5");
        row2.put("column3", "value6");
    }
    
    @Test
    public void testWriteAndReadRowList() throws Exception {
        List<TableRow> rows = new ArrayList<>();
        rows.add(row1);
        rows.add(row2);
    
        csvFileWriter.open();
        csvFileWriter.setColumnNames(header);
        csvFileWriter.writeRowList(rows);
        csvFileWriter.close();
    
        csvFileReader = new CSVFileReader(new File(writeCSVFilename), AppConfig.getInstance(new HashMap<String, String>()), false, false);
        csvFileReader.open();
        List<String> readHeader = csvFileReader.getColumnNames();
        assertEquals(header, readHeader);
    
        TableRow readRow1 = csvFileReader.readTableRow();
        for (String column : header) {
            assertEquals(row1.get(column), readRow1.get(column));
        }
    
        TableRow readRow2 = csvFileReader.readTableRow();
        for (String column : header) {
            assertEquals(row2.get(column), readRow2.get(column));
        }
    
        csvFileReader.close();
        new File(writeCSVFilename).delete();
    }
    
    @Test
    public void testWriteEmptyRowList() throws Exception {
        List<TableRow> rows = new ArrayList<>();
    
        csvFileWriter.open();
        csvFileWriter.setColumnNames(header);
        csvFileWriter.writeRowList(rows);
        csvFileWriter.close();
    
        csvFileReader = new CSVFileReader(new File(writeCSVFilename), AppConfig.getInstance(new HashMap<String, String>()), false, false);
        csvFileReader.open();
        List<String> readHeader = csvFileReader.getColumnNames();
        assertEquals(header, readHeader);
    
        TableRow readRow = csvFileReader.readTableRow();
        assertNull(readRow);
    
        csvFileReader.close();
        new File(writeCSVFilename).delete();
    }
    
    @Test
    public void testWriteRowWithNullValues() throws Exception {
        TableRow rowWithNullValues = new TableRow(new TableHeader(header));
        rowWithNullValues.put("column1", null);
        rowWithNullValues.put("column2", "value");
        rowWithNullValues.put("column3", null);
    
        List<TableRow> rows = new ArrayList<>();
        rows.add(rowWithNullValues);
    
        csvFileWriter.open();
        csvFileWriter.setColumnNames(header);
        csvFileWriter.writeRowList(rows);
        csvFileWriter.close();
    
        csvFileReader = new CSVFileReader(new File(writeCSVFilename), AppConfig.getInstance(new HashMap<String, String>()), false, false);
        csvFileReader.open();
        List<String> readHeader = csvFileReader.getColumnNames();
        assertEquals(header, readHeader);
    
        TableRow readRow = csvFileReader.readTableRow();
        assertEquals("", readRow.get("column1"));
        assertEquals("", readRow.get("column3"));

        csvFileReader.close();
        new File(writeCSVFilename).delete();
    }
    
    @Test
    public void testWriteRowWithSpecialCharacters() throws Exception {
        TableRow rowWithSpecialChars = new TableRow(new TableHeader(header));
        rowWithSpecialChars.put("column1", "value,with,commas");
        rowWithSpecialChars.put("column2", "value\nwith\nnewlines");
        rowWithSpecialChars.put("column3", "value\"with\"quotes");
    
        List<TableRow> rows = new ArrayList<>();
        rows.add(rowWithSpecialChars);
    
        csvFileWriter.open();
        csvFileWriter.setColumnNames(header);
        csvFileWriter.writeRowList(rows);
        csvFileWriter.close();
    
        csvFileReader = new CSVFileReader(new File(writeCSVFilename), AppConfig.getInstance(new HashMap<String, String>()), false, false);
        csvFileReader.open();
        List<String> readHeader = csvFileReader.getColumnNames();
        assertEquals(header, readHeader);
    
        TableRow readRow = csvFileReader.readTableRow();
        for (String column : header) {
            assertEquals(rowWithSpecialChars.get(column), readRow.get(column));
        }
    
        csvFileReader.close();
        new File(writeCSVFilename).delete();
    }
    
    @Test
    public void testWriteLargeNumberOfRows() throws Exception {
        List<TableRow> rows = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            TableRow row = new TableRow(new TableHeader(header));
            row.put("column1", "value" + i);
            row.put("column2", "value" + i);
            row.put("column3", "value" + i);
            rows.add(row);
        }
    
        csvFileWriter.open();
        csvFileWriter.setColumnNames(header);
        csvFileWriter.writeRowList(rows);
        csvFileWriter.close();
    
        csvFileReader = new CSVFileReader(new File(writeCSVFilename), AppConfig.getInstance(new HashMap<String, String>()), false, false);
        csvFileReader.open();
        List<String> readHeader = csvFileReader.getColumnNames();
        assertEquals(header, readHeader);
    
        for (int i = 0; i < 10000; i++) {
            TableRow readRow = csvFileReader.readTableRow();
            assertEquals("value" + i, readRow.get("column1"));
            assertEquals("value" + i, readRow.get("column2"));
            assertEquals("value" + i, readRow.get("column3"));
        }
    
        csvFileReader.close();
        new File(writeCSVFilename).delete();
    }
}