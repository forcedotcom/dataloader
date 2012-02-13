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

package com.salesforce.dataloader.process;

import java.util.Map;

import junit.framework.TestSuite;

import com.salesforce.dataloader.ConfigTestSuite;

/**
 * Tests extract all dataloader process
 * 
 * @author Aleksandr Shulman, Colin Jarvis
 * @since 21.0
 */
public class CsvExtractAllProcessTest extends ProcessExtractTestBase {

    public static TestSuite suite() {
        return ConfigTestSuite.createSuite(CsvExtractAllProcessTest.class);
    }

    public CsvExtractAllProcessTest(String name, Map<String, String> config) throws Exception {
        super(name, config);

    }

    public CsvExtractAllProcessTest(String name) {
        super(name);
    }

    @Override
    protected boolean isExtractAll() {
        return true;
    }

    /**
     * Verify that a correct error is given when a user attempts to perform a nested query. This verifies bug W-870843.
     * 
     * @expectedResults Assert that the internal error message is correct.
     */
    @Override
    public void testNestedQueryErrorsCorrectly() throws Exception {
        runTestNestedQueryErrorsCorrectly();
    }

    /**
     * Verify that queries that involve the parent work.
     * 
     * @expectedResults Assert the correct number of results through runProcess().
     * @throws Exception
     */
    @Override
    public void testSoqlWithRelationships() throws Exception {
        runTestSoqlWithRelationships();
    }

    /**
     * Verify that queries that involve the parent work, but with the caveat that the query is written in the below
     * manner. The reason is that this is a known DL bug. This test serves to verify W-809130.
     * 
     * @expectedResults Assert the correct number of results through runProcess().
     * @throws Exception
     */
    @Override
    public void testSoqlWithTableNameInSelect() throws Exception {
        runTestSoqlWithTableNameInSelect();
    }

    /**
     * Verify that a given SObject that does not support query produces the correct DL error. This just a quick check
     * that DL passes the API's error responses back correctly.
     * 
     * @expectedResults Assert that the error message is correct and contains the SObject type
     */
    @Override
    public void testForNonQueryableSObjects() throws Exception {
        runTestForNonQueryableSObjects();
    }

    /**
     * Verify that the Query parser is able to handle bad input with the correct error messages. These tests are run
     * against account, since the query will not get parsed and thus no results will be returned.
     * 
     * @expectedResults Assert that the correct error messages are given for a given malformed query.
     * @throws Exception
     */
    @Override
    public void testMalformedQueries() throws Exception {
        runMalformedQueriesTest();
    }

    @Override
    public void testExtractAccountCsv() throws Exception {
        runTestExtractAccountCsv();
    }

    @Override
    public void testExtractAccountCsvAggregate() throws Exception {
        runTestExtractAccountCsvAggregate();
    }
}