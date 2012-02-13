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

import java.io.File;
import java.util.Map;

import junit.framework.TestSuite;

import com.salesforce.dataloader.ConfigTestSuite;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.ProcessInitializationException;

/**
 * Test suite for the extracting to csv.
 * 
 * @author Colin Jarvis, Aleksandr Shulman
 * @since 21.0
 */
public class CsvExtractProcessTest extends ProcessExtractTestBase {

    public static TestSuite suite() {
        return ConfigTestSuite.createSuite(CsvExtractProcessTest.class);
    }

    public CsvExtractProcessTest(String name, Map<String, String> config) {
        super(name, config);
    }

    public CsvExtractProcessTest(String name) {
        super(name);
    }

    @Override
    protected boolean isExtractAll() {
        return false;
    }

    /**
     * Verify that queries that involve the parent work, but with the caveat that the query is written in the below
     * manner. The reason is that this is a known DL bug.
     * 
     * @expectedResults Assert the correct number of results through runProcess().
     * @throws Exception
     */
    @Override
    public void testSoqlWithTableNameInSelect() throws Exception {
        runTestSoqlWithTableNameInSelect();
    }

    /**
     * Tests the extract operation on Account. Verifies that an extract operation with a soql query is performed
     * correctly.
     */
    @Override
    public void testExtractAccountCsv() throws Exception {
        runTestExtractAccountCsv();
    }

    @Override
    public void testExtractAccountCsvAggregate() throws Exception {
        runTestExtractAccountCsvAggregate();
    }

    /**
     * Tests that SOQL queries with relationships work as expected
     */
    @Override
    public void testSoqlWithRelationships() throws Exception {
        runTestSoqlWithRelationships();
    }

    /**
     * Test output of last run files. 1. Output is enabled, directory is not set (use default) 2. Output is enabled,
     * directory is set 3. Output is disabled
     * 
     * @hierarchy API.dataloader Csv Process Tests
     * @userstory Commenting existing data loader tests and uploading into QA force
     */
    public void testLastRunOutput() throws Exception {
        // 1. Output is enabled (use default), directory is not set (use
        // default)
        String baseName = this.baseName;
        upsertSfdcAccounts(1);
        testLastRunOutput(true, baseName + "_default", true, null);

        // 2. Output is enabled, directory is set
        testLastRunOutput(false, baseName + "_dirSet", true, System.getProperty("java.io.tmpdir"));

        // 3. Output is disabled
        testLastRunOutput(false, baseName + "_disabled", false, null);
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
     * @param enableLastRunOutput
     */
    private void testLastRunOutput(boolean useDefault, String baseProcessName, boolean enableOutput, String outputDir)
            throws DataAccessObjectException, ProcessInitializationException {
        final String soql = "Select ID FROM ACCOUNT WHERE " + ACCOUNT_WHERE_CLAUSE + " limit 1";
        Map<String, String> argMap = getTestConfig(soql, "Account", false);
        argMap.remove(Config.LAST_RUN_OUTPUT_DIR);

        // set last run output paramerers
        if (!useDefault) {
            argMap.put(Config.ENABLE_LAST_RUN_OUTPUT, String.valueOf(enableOutput));
            argMap.put(Config.LAST_RUN_OUTPUT_DIR, outputDir);
        }

        this.baseName = baseProcessName;
        Controller theController = runProcess(argMap, 1);

        Config config = theController.getConfig();
        String lastRunFilePath = config.getLastRunFilename();
        File lastRunFile = new File(lastRunFilePath);
        try {
            String defaultFileName = baseProcessName + "_lastRun.properties";
            File expectedFile = useDefault ? new File(config.constructConfigFilePath(defaultFileName)) : new File(
                    outputDir, defaultFileName);
            if (enableOutput) {
                assertTrue("Could not find last run file: " + lastRunFilePath, lastRunFile.exists());
                assertEquals("Did not get expected last run file.", expectedFile, lastRunFile);
            } else {
                assertFalse("Last run file should not exist: " + lastRunFilePath, lastRunFile.exists());
            }

        } finally {
            if (lastRunFile.exists()) lastRunFile.delete();
        }

    }

    /**
     * Verify that a correct error is given when a user attempts to perform a nested query using query().
     * 
     * @expectedResults Assert that the internal error message is correct.
     */
    @Override
    public void testNestedQueryErrorsCorrectly() throws Exception {
        runTestNestedQueryErrorsCorrectly();
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

}
