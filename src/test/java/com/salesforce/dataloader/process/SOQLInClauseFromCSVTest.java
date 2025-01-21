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

package com.salesforce.dataloader.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.salesforce.dataloader.TestBase;
import com.salesforce.dataloader.TestSetting;
import com.salesforce.dataloader.TestVariant;
import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.action.visitor.AbstractQueryVisitor;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.dao.csv.CSVFileReader;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import com.salesforce.dataloader.exception.ProcessInitializationException;
import com.salesforce.dataloader.model.TableRow;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;

@RunWith(Parameterized.class)
@SuppressWarnings("unused")
public class SOQLInClauseFromCSVTest extends ProcessTestBase {
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(
                // partner API
                TestVariant.forSettings(TestSetting.BULK_API_DISABLED, TestSetting.BULK_V2_API_DISABLED),
                // Bulk API
                TestVariant.forSettings(TestSetting.BULK_API_ENABLED, TestSetting.BULK_V2_API_DISABLED),
                // Bulk V2 Query API
                TestVariant.forSettings(TestSetting.BULK_V2_API_ENABLED));
    }

    private Map<String,String> testConfig;

    @Before
    public void setupTestConfig() {
        testConfig = getTestConfig(OperationInfo.extract, true);
        testConfig.put(AppConfig.PROP_ENTITY, "Account");
        testConfig.put(AppConfig.PROP_ENABLE_EXTRACT_STATUS_OUTPUT, AppConfig.TRUE);
        testConfig.remove(AppConfig.PROP_MAPPING_FILE);
    }

    public SOQLInClauseFromCSVTest(Map<String, String> config) {
        super(config);
    }

    private static final String COL_IN_CSV = "Oracle_Id__c";
    private static final String ORACLE_ID_VAL = "oracleIdXyz";
    private static final String NAME_VAL = "acctNameXyz";

    @Test
    public void testSoqlInClauseUsingCSV() throws Exception {
        String accountId1 = insertAccount(NAME_VAL+"1", ORACLE_ID_VAL+"1", TestBase.ACCOUNT_NUMBER_PREFIX+"1");
        String accountId2 = insertAccount(NAME_VAL+"2", ORACLE_ID_VAL+"2", TestBase.ACCOUNT_NUMBER_PREFIX+"2");
        String extractionFileName = getTestDataDir() + File.separator + "SoqlInClauseFromCSV.csv";
        runExtraction("select name from Account where "
                + COL_IN_CSV
                + " in ({" 
                + extractionFileName 
                + "}, {"
                + COL_IN_CSV
                + "}) OR Name = '" + NAME_VAL + "2' ORDER BY Name", 2);
        validateAccountNamesInOutputFile(NAME_VAL, 2);
        runExtraction("select name from Account where Name = '" + NAME_VAL + "1' AND "
                + COL_IN_CSV
                + " in ({" 
                + extractionFileName 
                + "}, {"
                + COL_IN_CSV
                + "})", 1);
        validateAccountNamesInOutputFile(NAME_VAL, 1);
    }
    
    @Test
    public void testSoqlInClauseUsingCSVMultiBatch() throws Exception {
        testConfig.put(AppConfig.PROP_SOQL_MAX_LENGTH, "500");
        for (int i = 0; i < 20; i++) {
            insertAccount(NAME_VAL + i, ORACLE_ID_VAL + i, TestBase.ACCOUNT_NUMBER_PREFIX + i);
        }
        String extractionFileName = getTestDataDir() + File.separator + "SoqlInClauseFromCSV.csv";
        runExtraction("select name from Account where " + COL_IN_CSV + " in ({" + extractionFileName + "}, {"
                + COL_IN_CSV + "})", 20);
        validateAccountNamesInOutputFile(NAME_VAL, 20);
        testConfig.put(AppConfig.PROP_SOQL_MAX_LENGTH, Integer.toString(AppConfig.DEFAULT_MAX_SOQL_CHAR_LENGTH));
    }

    private void runExtraction(String extractionQuery, int numSuccesses) throws ProcessInitializationException, DataAccessObjectException {
        testConfig.put(AppConfig.PROP_EXTRACT_SOQL, extractionQuery);
        testConfig.put(AppConfig.PROP_LIMIT_OUTPUT_TO_QUERY_FIELDS, AppConfig.TRUE);
        runProcess(testConfig, numSuccesses, true);
    }
    
    private String insertAccount(String name, String oracleId, String acctId) throws ConnectionException {
        final SObject account = new SObject();
        account.setType("Account");
        account.setField("Name", name);
        account.setField("Oracle_Id__c", oracleId);
        account.setField("AccountNumber__c", acctId); // to make sure that records are deleted before/after test execution
        String id = getBinding().create(new SObject[]{account})[0].getId();
        assertNotNull(id);
        return id;
    }
    
    private void validateAccountNamesInOutputFile(
            final String accountNamePrefix, final int count) throws IOException {

        FileInputStream fis = new FileInputStream(new File(testConfig.get(AppConfig.PROP_DAO_NAME)));
        try {
            CSVFileReader rdr = new CSVFileReader(new File(testConfig.get(AppConfig.PROP_DAO_NAME)),
                    this.getController().getAppConfig(), false, true);
            rdr.open();
            TableRow row = rdr.readTableRow();
            for (int i = 0; i < count; i++) {
                String extractedNameVal = (String) row.get("Name");
                assertTrue(extractedNameVal.startsWith(accountNamePrefix));
                row = rdr.readTableRow();
            }
        } catch (DataAccessObjectInitializationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DataAccessObjectException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }
}