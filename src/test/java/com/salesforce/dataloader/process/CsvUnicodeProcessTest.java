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

import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.config.Config;
import com.sforce.async.CSVReader;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Test to validate we handle unicode correctly.
 * 
 * @author Colin Jarvis
 * @since 24
 */

public class CsvUnicodeProcessTest extends ProcessTestBase {

    @Test
    public void testUnicodeExtraction() throws Exception {
        final String name = System.nanoTime() + "☠";
        final String accountId = insertAccount(name);
        final String soql = "SELECT Id,Name FROM ACCOUNT WHERE Id ='" + accountId + "'";
        final Map<String, String> testConfig = getBulkUnicodeExtractConfig(soql);
        runProcess(testConfig, 1);
        validateExtraction(name, testConfig);
    }

    private Map<String, String> getBulkUnicodeExtractConfig(String soql) {
        final Map<String, String> argMap = getTestConfig(OperationInfo.extract, true);
        argMap.put(Config.ENTITY, "Account");
        argMap.put(Config.EXTRACT_SOQL, soql);
        argMap.put(Config.ENABLE_EXTRACT_STATUS_OUTPUT, Config.TRUE);
        argMap.put(Config.EXTRACT_REQUEST_SIZE, "2000");
        argMap.put(Config.WRITE_UTF8, Config.TRUE);
        argMap.put(Config.READ_UTF8, Config.TRUE);
        argMap.put(Config.BULK_API_ENABLED, Config.TRUE);
        argMap.remove(Config.MAPPING_FILE);
        return argMap;
    }

    private void validateExtraction(final String name, final Map<String, String> testConfig) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(testConfig.get(Config.DAO_NAME)));
            CSVReader rdr = new CSVReader(fis, "UTF-8");
            int nameidx = rdr.nextRecord().indexOf("NAME");
            assertEquals(name, rdr.nextRecord().get(nameidx));
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

    private String insertAccount(String name) throws ConnectionException {
        final SObject account = new SObject();
        account.setType("Account");
        account.setField("Name", name);
        return getBinding().create(new SObject[] { account })[0].getId();
    }
}