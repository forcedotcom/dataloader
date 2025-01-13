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
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.exception.ConfigInitializationException;
import com.salesforce.dataloader.model.TableHeader; 
import com.salesforce.dataloader.model.TableRow; 
import org.junit.Before; import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;

import static org.junit.Assert.*;
public class DAORowCacheTest {
private DAORowCache daoRowCache;
private TableRow row1;
private TableRow row2;

@Before
public void setUp() {
    try {
        AppConfig.getInstance(new HashMap<String, String>());
    } catch (ConfigInitializationException | FactoryConfigurationError | IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    
    AppConfig.getCurrentConfig().setValue(AppConfig.PROP_PROCESS_BULK_CACHE_DATA_FROM_DAO, true);
    daoRowCache = new DAORowCache();

    List<String> headerList = new ArrayList<>();
    headerList.add("column1");
    headerList.add("column2");
    TableHeader header = new TableHeader(headerList);

    row1 = new TableRow(header);
    row1.put("column1", "value1");
    row1.put("column2", "value2");

    row2 = new TableRow(header);
    row2.put("column1", "value3");
    row2.put("column2", "value4");
}

@Test
public void testAddRow() {
    daoRowCache.addRow(row1);
    assertEquals(1, daoRowCache.size());
    daoRowCache.addRow(row2);
    assertEquals(2, daoRowCache.size());
}

@Test
public void testGetCurrentRow() {

    daoRowCache.addRow(row1);
    daoRowCache.addRow(row2);

    daoRowCache.resetCurrentRowIndex();
    TableRow currentRow = daoRowCache.getCurrentRow();
    assertNotNull(currentRow);
    assertEquals("value1", currentRow.get("column1"));

    currentRow = daoRowCache.getCurrentRow();
    assertNotNull(currentRow);
    assertEquals("value3", currentRow.get("column1"));

    currentRow = daoRowCache.getCurrentRow();
    assertNull(currentRow);
}

@Test
public void testResetCurrentRowIndex() {
    daoRowCache.addRow(row1);
    daoRowCache.addRow(row2);
    daoRowCache.getCurrentRow();
    daoRowCache.resetCurrentRowIndex();
    TableRow currentRow = daoRowCache.getCurrentRow();
    assertEquals("value1", currentRow.get("column1"));
}

@Test
public void testGetRows() {
    daoRowCache.addRow(row1);
    daoRowCache.addRow(row2);

    List<TableRow> rows = daoRowCache.getRows(0, 1);
    assertNotNull(rows);
    assertEquals(1, rows.size());
    assertEquals("value1", rows.get(0).get("column1"));

    rows = daoRowCache.getRows(1, 1);
    assertNotNull(rows);
    assertEquals(1, rows.size());
    assertEquals("value3", rows.get(0).get("column1"));

    rows = daoRowCache.getRows(0, 3);
    assertNotNull(rows);
    assertEquals(2, rows.size());
}

@Test
public void testGetRowsWithInvalidStartRow() {
    daoRowCache.addRow(row1);
    daoRowCache.addRow(row2);

    List<TableRow> rows = daoRowCache.getRows(3, 1);
    assertNull(rows);
}

@Test
public void testGetRowsWithExceedingNumRows() {
    daoRowCache.addRow(row1);
    daoRowCache.addRow(row2);

    List<TableRow> rows = daoRowCache.getRows(1, 5);
    assertNotNull(rows);
    assertEquals(1, rows.size());
    assertEquals("value3", rows.get(0).get("column1"));
}
}