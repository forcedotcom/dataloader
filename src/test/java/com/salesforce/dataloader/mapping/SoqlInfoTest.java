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

import java.util.List;

import com.salesforce.dataloader.TestBase;
import com.salesforce.dataloader.mapping.SOQLInfo.SOQLFieldInfo;
import com.salesforce.dataloader.mapping.SOQLInfo.SOQLParserException;

/**
 * Tests parsing SOQLInfo
 * 
 * @author Colin Jarvis
 * @since 21.0
 */
public class SoqlInfoTest extends TestBase {
    public SoqlInfoTest(String name) {
        super(name);
    }

    public void testParseSoql() throws SOQLParserException {
        SOQLInfo soqlInfo = new SOQLInfo("select account.id, blarney.name from account blarney");
        assertEquals("blarney", soqlInfo.getTableAlias());
        assertEquals("account", soqlInfo.getTableName());
        assertEquals(2, soqlInfo.getSelectedFields().size());
        doFieldAssertions(soqlInfo.getSelectedFields().get(0), "account.id");
        doFieldAssertions(soqlInfo.getSelectedFields().get(1), "blarney.name");
    }

    public void testParseSoql3() throws SOQLParserException {
        SOQLInfo soqlInfo = new SOQLInfo("Select Id, Name From Account Blarney");
        assertEquals("Blarney", soqlInfo.getTableAlias());
        assertEquals("Account", soqlInfo.getTableName());
        assertEquals(2, soqlInfo.getSelectedFields().size());
        doFieldAssertions(soqlInfo.getSelectedFields().get(0), "Id");
        doFieldAssertions(soqlInfo.getSelectedFields().get(1), "Name");
    }

    public void testParseSoql1() throws SOQLParserException {
        SOQLInfo soqlInfo = new SOQLInfo("select id, name from account blarney where id = ''");
        assertEquals("blarney", soqlInfo.getTableAlias());
        assertEquals("account", soqlInfo.getTableName());
        assertEquals(2, soqlInfo.getSelectedFields().size());
        doFieldAssertions(soqlInfo.getSelectedFields().get(0), "id");
        doFieldAssertions(soqlInfo.getSelectedFields().get(1), "name");
    }

    public void testParseSoql2() throws SOQLParserException {
        SOQLInfo soqlInfo = new SOQLInfo("select id, name from account  where id = ''");
        assertEquals("", soqlInfo.getTableAlias());
        assertEquals("account", soqlInfo.getTableName());
        assertEquals(2, soqlInfo.getSelectedFields().size());
        doFieldAssertions(soqlInfo.getSelectedFields().get(0), "id");
        doFieldAssertions(soqlInfo.getSelectedFields().get(1), "name");
    }

    private void doFieldAssertions(SOQLInfo.SOQLFieldInfo fieldInfo, String expectedFieldName) {
        assertNull(fieldInfo.getAggregateFunction());
        assertNull(fieldInfo.getAlias());
        assertEquals(expectedFieldName, fieldInfo.getFieldName());
    }

    public void testNoFields() throws SOQLParserException {
        try {
            new SOQLInfo("select  from account  where id = ''");
            fail("should not be able to parse query");
        } catch (IllegalArgumentException e) {
            assertEquals("Cannot parse empty string", e.getMessage());
        }
    }

    public void testParseAggregateFields() throws SOQLParserException {
        SOQLInfo info = new SOQLInfo("SELECT max(fld) xXx FROM account blarney");
        assertEquals("blarney", info.getTableAlias());
        assertEquals("account", info.getTableName());
        assertEquals(1, info.getSelectedFields().size());
        SOQLFieldInfo fieldInfo = info.getSelectedFields().get(0);
        assertEquals("max", fieldInfo.getAggregateFunction());
        assertEquals("xXx", fieldInfo.getAlias());
        assertEquals("fld", fieldInfo.getFieldName());
    }

    public void testParseAggregateFields1() throws SOQLParserException {
        SOQLInfo info = new SOQLInfo("select max(fld)  from account blarney");
        assertEquals("blarney", info.getTableAlias());
        assertEquals("account", info.getTableName());
        assertEquals(1, info.getSelectedFields().size());
        SOQLFieldInfo fieldInfo = info.getSelectedFields().get(0);
        assertEquals("max", fieldInfo.getAggregateFunction());
        assertEquals("expr0", fieldInfo.getAlias());
        assertEquals("fld", fieldInfo.getFieldName());
    }

    public void testAggregateExpressionAliases() throws SOQLParserException {
        SOQLInfo info = new SOQLInfo("select max(fld1), min(fld2) fld2_min, max(fld3)  from account blarney");
        doSoqlInfoAssertions(info, "account", "blarney", 3);
        List<SOQLFieldInfo> fields = info.getSelectedFields();
        doFieldInfoAssertions(fields.get(0), "fld1", "max", "expr0");
        doFieldInfoAssertions(fields.get(1), "fld2", "min", "fld2_min");
        doFieldInfoAssertions(fields.get(2), "fld3", "max", "expr1");
    }

    private void doSoqlInfoAssertions(SOQLInfo soqlInfo, String tableName, String tableAlias, int numFields) {
        assertEquals(tableName, soqlInfo.getTableName());
        assertEquals(tableAlias, soqlInfo.getTableAlias());
        assertEquals(numFields, soqlInfo.getSelectedFields().size());
    }

    private void doFieldInfoAssertions(SOQLFieldInfo fieldInfo, String expectedFieldName, String expectedAggFunc,
            String expectedAlias) {
        assertEquals(expectedFieldName, fieldInfo.getFieldName());
        assertEquals(expectedAggFunc, fieldInfo.getAggregateFunction());
        assertEquals(expectedAlias, fieldInfo.getAlias());
    }

    public void testParseAggregateMissingField() throws SOQLParserException {
        try {
            new SOQLInfo("select max() xxx from account blarney");
            fail("should not be able to parse query");
        } catch (IllegalArgumentException e) {
            assertEquals("Cannot parse empty string", e.getMessage());
        }
    }

    public void testNoSelect() {
        runInvalidQueryTest("id, name from account blarney", "No 'SELECT' keyword");
    }

    public void testNoFrom() {
        try {
            new SOQLInfo("select id, name account blarney");
            fail("should not be able to parse query");
        } catch (SOQLParserException e) {
            assertEquals("Invalid soql: No 'FROM' keyword", e.getMessage());
        }
    }

    public void testNestedQuery() {
        runInvalidQueryTest("select id, (select name from contacts) from account blarney",
                "Nested queries are not supported");
    }

    public void testMissingTableName() {
        runInvalidQueryTest("select id from where Name='sometext'", "Failed to parse table name");
        runInvalidQueryTest("select id from", "No sobject specified after 'FROM' keyword");
    }

    public void testGroupQuery() throws SOQLParserException {
        new SOQLInfo("select id from group");
    }

    public void testOrderQuery() throws SOQLParserException {
        new SOQLInfo("select id from order");
    }

    public void testWhereValidation() throws Exception {
        SOQLInfo info = runValidQueryTest("select id from account WHERE id != null");
        assertEquals("", info.getTableAlias());

        runInvalidQueryTest("select id FrOm wHeRe id != null", "Failed to parse table name");
    }

    public void testReservedKeywords() throws Exception {
        runValidQueryTest("select id from from__c");
        runValidQueryTest("select from__c from from__c");
        runValidQueryTest("select id from where__c");
        runValidQueryTest("select where__c from where__c");
        runValidQueryTest("select where__c from where__c where where__c='abc'");
        runValidQueryTest("select select__c from from__c where where__c='abc'");
    }

    private SOQLInfo runValidQueryTest(String query) throws SOQLParserException {
        return new SOQLInfo(query);
    }

    private void runInvalidQueryTest(String query, String expectedMessage) {
        try {
            new SOQLInfo(query);
            fail("should not be able to parse query: " + query);
        } catch (SOQLParserException e) {
            assertEquals("Invalid soql: " + expectedMessage, e.getMessage());
        }
    }
}
