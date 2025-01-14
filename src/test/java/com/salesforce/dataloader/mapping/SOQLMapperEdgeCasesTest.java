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
import com.salesforce.dataloader.model.Row;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType; 
import com.sforce.soap.partner.sobject.SObject; 
import org.junit.Before; import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
public class SOQLMapperEdgeCasesTest {
    private SOQLMapper soqlMapper;
    
    @Before
    public void setUp() throws Exception {
        Field[] fields = new Field[3];
        for (int i = 0; i < 3; i++) {
            fields[i] = new Field();
            fields[i].setName("Field" + i);
            fields[i].setType(FieldType.string);
        }
        soqlMapper = new SOQLMapper(null, Arrays.asList("Field0", "Field1", "Field2"), fields, null);
    }
    
    @Test
    public void testInitSoqlMappingWithEmptyQuery() {
        try {
            soqlMapper.initSoqlMapping("");
            fail("Expected MappingInitializationException");
        } catch (Exception e) {
            assertEquals("Cannot parse empty string", e.getMessage());
        }
    }
    
    @Test
    public void testInitSoqlMappingWithInvalidQuery() {
        try {
            soqlMapper.initSoqlMapping("SELECT FROM Account");
            fail("Expected MappingInitializationException");
        } catch (Exception e) {
            assertEquals("Invalid soql: No sobject specified after 'FROM' keyword", e.getMessage());
        }
    }
    
    @Test
    public void testMapPartnerSObjectSfdcToLocalWithNullSObject() {
        Row result = soqlMapper.mapPartnerSObjectSfdcToLocal(null);
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testMapPartnerSObjectSfdcToLocalWithEmptySObject() {
        SObject sObject = new SObject();
        Row result = soqlMapper.mapPartnerSObjectSfdcToLocal(sObject);
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testMapCsvRowSfdcToLocalWithEmptyHeadersAndValues() {
        List<String> headers = new ArrayList<>();
        List<String> values = new ArrayList<>();
        StringBuilder id = new StringBuilder();
        Row result = soqlMapper.mapCsvRowSfdcToLocal(headers, values, id);
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testMapCsvRowSfdcToLocalWithMismatchedHeadersAndValues() {
        List<String> headers = Arrays.asList("Field0", "Field1");
        List<String> values = Arrays.asList("Value0");
        StringBuilder id = new StringBuilder();
        Row result = soqlMapper.mapCsvRowSfdcToLocal(headers, values, id);
        assertEquals("Value0", result.get("Field0"));
        assertNull(result.get("Field1"));
    }
    
    @Test
    public void testGetExtractionMappingWithNonExistentField() {
        String result = soqlMapper.getExtractionMapping("NonExistentField", false);
        assertNull(result);
    }
    
    @Test
    public void testGetExtractionMappingWithStrictMatching() {
        soqlMapper.initSoqlMapping("SELECT Field0 FROM Account");
        String result = soqlMapper.getExtractionMapping("Field0", true);
        assertEquals("Field0", result);
    }
    
    @Test
    public void testGetExtractionMappingWithNonStrictMatching() {
        soqlMapper.initSoqlMapping("SELECT Field0 FROM Account");
        String result = soqlMapper.getExtractionMapping("field0", false);
        assertEquals("Field0", result);
    }
}