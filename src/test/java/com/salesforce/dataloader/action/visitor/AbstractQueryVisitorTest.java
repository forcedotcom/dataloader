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

package com.salesforce.dataloader.action.visitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;


public class AbstractQueryVisitorTest {
    @Test
    public void testParseInClauseForFileAndColumnName_IN_ValidInput() {
        String input = "SELECT name FROM Account WHERE id IN ({c:\\users\\me\\dataloader\\accounts.csv}, {acctid})";
        List<String> result = AbstractQueryVisitor.parseInClauseForFileAndColumnName(input);
        assertEquals(2, result.size());
        assertEquals("c:\\users\\me\\dataloader\\accounts.csv", result.get(0));
        assertEquals("acctid", result.get(1));
    }

    @Test
    public void testParseInClauseForFileAndColumnName_in_ValidInput() {
        String input = "SELECT name FROM Account WHERE id in ({c:\\users\\me\\dataloader\\accounts.csv}, {acctid})";
        List<String> result = AbstractQueryVisitor.parseInClauseForFileAndColumnName(input);
        assertEquals(2, result.size());
        assertEquals("c:\\users\\me\\dataloader\\accounts.csv", result.get(0));
        assertEquals("acctid", result.get(1));
    }

    @Test
    public void testParseInClauseForFileAndColumnName_iN_ValidInput() {
        String input = "SELECT name FROM Account WHERE id iN ({c:\\users\\me\\dataloader\\accounts.csv}, {acctid})";
        List<String> result = AbstractQueryVisitor.parseInClauseForFileAndColumnName(input);
        assertEquals(2, result.size());
        assertEquals("c:\\users\\me\\dataloader\\accounts.csv", result.get(0));
        assertEquals("acctid", result.get(1));
    }
    

    @Test
    public void testParseInClauseForFileAndColumnNameWithAndValidInput() {
        String input = "SELECT name FROM Account WHERE id iN ({c:\\users\\me\\dataloader\\accounts.csv}, {acctid}) AND name='bar'";
        List<String> result = AbstractQueryVisitor.parseInClauseForFileAndColumnName(input);
        assertEquals(2, result.size());
        assertEquals("c:\\users\\me\\dataloader\\accounts.csv", result.get(0));
        assertEquals("acctid", result.get(1));
    }
    @Test
    public void testParseInClauseForFileAndColumnName_InvalidInput() {
        String input = "SELECT name FROM Account WHERE id IN ()";
        List<String> result = AbstractQueryVisitor.parseInClauseForFileAndColumnName(input);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testParseInClauseForFileAndColumnName_EmptyInput() {
        String input = "";
        List<String> result = AbstractQueryVisitor.parseInClauseForFileAndColumnName(input);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testParseInClauseForFileAndColumnName_MissingBraces() {
        String input = "SELECT name FROM Account WHERE id IN ('c:\\users\\me\\dataloader\\accounts.csv', 'acctid')";
        List<String> result = AbstractQueryVisitor.parseInClauseForFileAndColumnName(input);
        assertTrue(result.isEmpty());
    }
}