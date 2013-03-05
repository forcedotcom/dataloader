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
package com.salesforce.dataloader.mapping;

import com.salesforce.dataloader.ConfigTestBase;
import com.salesforce.dataloader.exception.MappingInitializationException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Set of unit tests for the data mapper
 * 
 * @author Alex Warshavsky
 * @author Federico Recio
 * @since 8.0
 */
public class LoadMapperTest extends ConfigTestBase {

    private static final String[] SOURCE_NAMES = { "sourceOne", "sourceTwo", "sourceThree" };
    private static final String[] SOURCE_VALUES = { "valueOne", "valueTwo", "valueThree" };
    private static final String[] DEST_NAMES = { "destinationOne", "destinationTwo", "destinationThree" };
    private static final String DEST_CONSTANT_NAME = "destinationConstant";
    private static final String CONSTANT_VALUE = "constantValue123";
    private Map<String, Object> sourceValueMap;

    public LoadMapperTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        sourceValueMap = new HashMap<String, Object>();
        // populate all the available values
        for (int i = 0; i < SOURCE_NAMES.length; i++) {
            sourceValueMap.put(SOURCE_NAMES[i], SOURCE_VALUES[i]);
        }
    }

    /**
     * Verify that the map file is created correctly.
     * 
     * @expectedResults  Assert that the source values (value) are mapped to the correct destination names (key). Also verify that the constant (key)
     *                      is mapped correctly to the destinationConstant (value).
     * 
     * @throws MappingInitializationException
     */
    public void testMapFile() throws MappingInitializationException {
        LoadMapper mapper = new LoadMapper(null, Arrays.asList(SOURCE_NAMES), new File(getTestDataDir(), "basicMap.sdl").getAbsolutePath());

        verifyMapping(mapper, DEST_NAMES);
    }

    /**
     * Verify that adding no columns in addition to what is the in .sdl file will map correctly.
     * 
     * @expectedResults Assert that the source values (value) are mapped to the correct destination names (key). Also verify that the constant (key)
     *                      is mapped correctly to the destinationConstant (value).
     * 
     * @throws MappingInitializationException
     */
    public void testMapFileNoSourceColumns()
            throws MappingInitializationException {
        // null column list
        LoadMapper mapper = new LoadMapper(null, null, new File(
                getTestDataDir(), "basicMap.sdl").getAbsolutePath());
        verifyMapping(mapper, DEST_NAMES);

        // empty column list
        mapper = new LoadMapper(null, new ArrayList<String>(), new File(
                getTestDataDir(), "basicMap.sdl").getAbsolutePath());
        verifyMapping(mapper, DEST_NAMES);
    }

    /**
     * Verify that the map can be populated from a properties file.
     * 
     * @expectedResults Assert that the source values (value) are mapped to the correct destination names (key). Also verify that the constant (key)
     *                      is mapped correctly to the destinationConstant (value).
     * 
     * @throws MappingInitializationException
     */
    public void testMapProperties() throws MappingInitializationException {
        // prepopulate properties map
        Properties mappings = new Properties();
        for (int i = 0; i < SOURCE_NAMES.length; i++) {
            mappings.setProperty(SOURCE_NAMES[i], DEST_NAMES[i]);
        }
        mappings.setProperty("\"" + CONSTANT_VALUE + "\"", DEST_CONSTANT_NAME);

        LoadMapper mapper = new LoadMapper(null, Arrays.asList(SOURCE_NAMES),
                null);
        mapper.putPropertyFileMappings(mappings);
        verifyMapping(mapper, DEST_NAMES);
    }

    /**
     * Verify that multiple fields can have the same value for a constant when listed as a comma-separated list.
     * It also tests that white space does not affect the interpretation of these fields.
     * 
     * @expectedResults Assert that the mappings from various keys (fields) can have the same value.
     * @throws MappingInitializationException
     */
    public void testDuplicateConstants() throws MappingInitializationException {
        Properties mappings = new Properties();

        String constantValue = "5";
        String wrappedConstantValue = "\"" + constantValue + "\"";

        final String value = wrappedConstantValue;
        mappings.setProperty(value, "Name, field1__c,field2__c,   field3__c,\n\tfield4__c");
        mappings.setProperty("", "Value6");
        LoadMapper mapper = new LoadMapper(null, null, null);
        mapper.putPropertyFileMappings(mappings);
        Map<String, Object> result = mapper.mapData(Collections.<String, Object> emptyMap());
        assertEquals(constantValue, result.get("Name"));
        assertEquals(constantValue, result.get("field1__c"));
        assertEquals(constantValue, result.get("field2__c"));
        assertEquals(constantValue, result.get("field3__c"));
        assertEquals(constantValue, result.get("field4__c"));
    }

    /**
     *  Verify that dao mappings placed into the mapping do not override the initial field mapping in the sdl file.
     * 
     *  This warrants a careful explanation:
     * 
     *  When placing column assignments into .map, it is (src column, sfdc field).
     *  When placing a constant into the mapper, it's (constant val, sfdc field).
     *  Basically, this is just as it is in the .sdl file.
     * 
     *  When extracting the values using mapData(...), it will examine if the key of the
     *  input map exists as a key in the .map map.
     * 
     *  The result it returns has the key being the sfdc field and the value being either
     *  the column name (if from csv) or the constant value (from sdl file).
     * 
     * @expectedResults Assert that the original (field) value is maintained.
     * 
     * @throws MappingInitializationException
     */
    public void testColumnValueDoesNotOverrideConstant() throws MappingInitializationException {

        Properties mappings = new Properties();
        final String sfdcField = "Name";
        final String constantValue = "abc";
        final String wrappedConstantValue = "\"" + constantValue + "\"";
        final String csvFieldName = "NotAbc";

        //place a constant mapping into the LoadMapper.
        mappings.setProperty(wrappedConstantValue, sfdcField);
        LoadMapper mapper = new LoadMapper(null, null, null);
        mapper.putPropertyFileMappings(mappings);

        //place a dao column -> sfdc field mapping
        Map<String, Object> input = new HashMap<String, Object>(1);
        input.put(csvFieldName, sfdcField);

        //(src, dest).
        mapper.putMapping(csvFieldName, sfdcField);
        Map<String, Object> result = mapper.mapData(input);

        //verify that the old value holds
        assertEquals(constantValue, result.get(sfdcField));
    }

    /**
     * Verify that if a constant value is assigned to a given field,
     * and that field appears as an entry mapped from a column, that
     * the constant value will still take precedence.
     * 
     * @expectedResults Assert that the new value is maintained.
     */
    public void testConstValueOverridesColumnValue() throws Exception {

        Properties mappings = new Properties();
        final String sfdcField = "Name";
        final String csvFieldName = "abc"; //this is the column in the csv file to which the sfdc field will be bound

        final String constantValue = "NotAbc";
        final String wrappedConstantValue = "\"" + constantValue + "\"";

        LoadMapper mapper = new LoadMapper(null, null, null);

        //place a dao column -> sfdc field mapping
        //(src, dest).
        mapper.putMapping(csvFieldName, sfdcField);

        //place a constant mapping into the LoadMapper.
        mappings.setProperty(wrappedConstantValue, sfdcField);
        mapper.putPropertyFileMappings(mappings);

        Map<String, Object> input = new HashMap<String, Object>(1);
        input.put(constantValue, sfdcField);

        Map<String, Object> result = mapper.mapData(input);

        //verify that the old value holds
        assertEquals(constantValue, result.get(sfdcField));
    }

    public void testMapDataEmptyEntriesIgnored() throws Exception {
        LoadMapper loadMapper = new LoadMapper(null, null, null);
        loadMapper.putMapping("SOURCE_COL", "");

        Map<String, Object> inputData = new HashMap<String, Object>(1);
        inputData.put("SOURCE_COL", "123");

        Map<String, Object> result = loadMapper.mapData(inputData);

        assertTrue("Empty destination column should have not been mapped", result.isEmpty());
    }

    public void testVerifyMappingsAreValidEmptyEntries() throws Exception {
        LoadMapper loadMapper = new LoadMapper(null, null, null);
        loadMapper.putMapping("SOURCE_COL", "");
        loadMapper.verifyMappingsAreValid();
    }

    /**
     * Helper method to verify that the LoadMapper has mapped the specified columns to their correct respective field, along with any constants in the mapping file.
     */
    private void verifyMapping(LoadMapper mapper, String... destNames) {
        Map<String, Object> destValueMap = mapper.mapData(this.sourceValueMap);
        for (int i = 0; i < destNames.length; i++) {
            assertNotNull("Destination# " + i + "(" + destNames[i]
                    + ") should have a mapped value",
                    destValueMap.get(destNames[i]));
            assertEquals("Destination# " + i + "(" + destNames[i]
                    + ") should contain the expected value", SOURCE_VALUES[i],
                    destValueMap.get(destNames[i]));
        }
        // verify constant mapped correctly
        assertEquals("Destination[" + DEST_CONSTANT_NAME
                + "] should contain constant", CONSTANT_VALUE,
                destValueMap.get(DEST_CONSTANT_NAME));
    }
}
