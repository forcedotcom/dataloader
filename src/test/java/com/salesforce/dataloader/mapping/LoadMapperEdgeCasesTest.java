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
package com.salesforce.dataloader.mapping;
import com.salesforce.dataloader.exception.MappingInitializationException;
import com.salesforce.dataloader.model.TableHeader;
import com.salesforce.dataloader.model.TableRow; import com.sforce.soap.partner.Field; import com.sforce.soap.partner.FieldType; import org.junit.Before; import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class LoadMapperEdgeCasesTest {
    private LoadMapper loadMapper;
    private TableRow sourceRow;
    private static final String[] SOURCE_NAMES = { "sourceOne", "sourceTwo", "sourceThree" };
    private static final String[] SOURCE_VALUES = { "valueOne", "valueTwo", "valueThree" };
    private static final String[] DEST_NAMES = { "destinationOne", "destinationTwo", "destinationThree" };
    
    @Before
    public void setUp() throws Exception {
        List<String> headerList = Arrays.asList(SOURCE_NAMES);
        sourceRow = new TableRow(new TableHeader(headerList));
        for (int i = 0; i < SOURCE_NAMES.length; i++) {
            sourceRow.put(SOURCE_NAMES[i], SOURCE_VALUES[i]);
        }
        Field[] fields = new Field[3];
        for (int i = 0; i < 3; i++) {
            fields[i] = new Field();
            fields[i].setName(DEST_NAMES[i]);
            fields[i].setType(FieldType.string);
        }
        loadMapper = new LoadMapper(null, Arrays.asList(SOURCE_NAMES), fields, null);
    }
    
    @Test
    public void testMapDataWithEmptySourceRow() throws MappingInitializationException {
        TableRow emptyRow = new TableRow(new TableHeader(Arrays.asList(SOURCE_NAMES)));
        TableRow result = loadMapper.mapData(emptyRow, true);
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testMapDataWithNullSourceRow() throws MappingInitializationException {
        TableRow nullRow = new TableRow(new TableHeader(Arrays.asList(SOURCE_NAMES)));
        nullRow.put(SOURCE_NAMES[0], null);
        TableRow result = loadMapper.mapData(nullRow, true);
        assertNull(result.get(DEST_NAMES[0]));
    }
    
    @Test
    public void testMapDataWithDuplicateSourceColumns() throws MappingInitializationException {
        loadMapper.putMapping(SOURCE_NAMES[0], DEST_NAMES[0]);
        loadMapper.putMapping(SOURCE_NAMES[0], DEST_NAMES[1]);
        TableRow result = loadMapper.mapData(sourceRow, true);
        assertNull(result.get(DEST_NAMES[0]));
        assertEquals(SOURCE_VALUES[0], result.get(DEST_NAMES[1]));
    }
    
    @Test
    public void testMapDataWithEmptyDestinationColumn() throws MappingInitializationException {
        loadMapper.putMapping(SOURCE_NAMES[0], "");
        TableRow result = loadMapper.mapData(sourceRow, true);
        assertNull(result.get(""));
    }
    
    @Test
    public void testMapDataWithWhitespaceInSourceColumn() throws MappingInitializationException {
        loadMapper.putMapping(" " + SOURCE_NAMES[0] + " ", DEST_NAMES[0]);
        TableRow result = loadMapper.mapData(sourceRow, true);
        assertNull(result.get(DEST_NAMES[0]));
    }
    
    @Test
    public void testMapDataWithWhitespaceInDestinationColumn() throws MappingInitializationException {
        loadMapper.putMapping(SOURCE_NAMES[0], " " + DEST_NAMES[0] + " ");
        TableRow result = loadMapper.mapData(sourceRow, true);
        assertEquals(SOURCE_VALUES[0], result.get(DEST_NAMES[0]));
    }
    
    @Test
    public void testMapDataWithSpecialCharactersInSourceColumn() throws MappingInitializationException {
        loadMapper.putMapping(SOURCE_NAMES[0] + "!@#$%^&*()", DEST_NAMES[0]);
        TableRow result = loadMapper.mapData(sourceRow, true);
        assertNull(result.get(DEST_NAMES[0]));
    }
    
    @Test
    public void testMapDataWithSpecialCharactersInDestinationColumn() throws MappingInitializationException {
        loadMapper.putMapping(SOURCE_NAMES[0], DEST_NAMES[0] + "!@#$%^&*()");
        TableRow result = loadMapper.mapData(sourceRow, true);
        assertEquals(SOURCE_VALUES[0], result.get(DEST_NAMES[0] + "!@#$%^&*()"));
    }
    
    @Test
    public void testMapDataWithLongSourceColumnName() throws MappingInitializationException {
        String longSourceName = "sourceColumnWithAVeryLongNameThatExceedsNormalLength";
        sourceRow.addHeaderColumn(longSourceName);
        loadMapper.putMapping(longSourceName, DEST_NAMES[0]);
        sourceRow.put(longSourceName, "longValue");
        TableRow result = loadMapper.mapData(sourceRow, true);
        assertEquals("longValue", result.get(DEST_NAMES[0]));
    }
    
    @Test
    public void testMapDataWithLongDestinationColumnName() throws MappingInitializationException {
        String longDestName = "destinationColumnWithAVeryLongNameThatExceedsNormalLength";
        loadMapper.putMapping(SOURCE_NAMES[0], longDestName);
        TableRow result = loadMapper.mapData(sourceRow, true);
        assertEquals(SOURCE_VALUES[0], result.get(longDestName));
    }
}