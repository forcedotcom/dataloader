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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.ProcessInitializationException;
import com.sforce.async.CSVReader;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Set of tests to verify that aggregate queries can be used to extract data
 * and that results are mapped correctly in the output file.
 *
 * @author Federico Recio
 */
public class CsvExtractAggregateQueryProcessTest extends ProcessTestBase {

    private Map<String,String> testConfig;

    @Before
    public void setUpTestConfig() {
        testConfig = getTestConfig(OperationInfo.extract, true);
        testConfig.put(Config.ENTITY, "Contact");
        testConfig.put(Config.ENABLE_EXTRACT_STATUS_OUTPUT, Config.TRUE);
        testConfig.remove(Config.MAPPING_FILE);
    }

    @Test
    public void testAggregateQuery() throws Exception {
        String accountId = insertAccount("acctNameXyz");
        String contactId = insertContact(accountId);
        runExtraction("select Count(Id), Account.Name from Contact where Id='" + contactId + "' GROUP BY Account.Name");
        validateAccountNameInOutputFile("acctNameXyz");
    }

    private void runExtraction(String extractionQuery) throws ProcessInitializationException, DataAccessObjectException {
        testConfig.put(Config.EXTRACT_SOQL, extractionQuery);
        runProcess(testConfig, 1, true);
    }

    private void validateAccountNameInOutputFile(final String accountName) throws IOException {
        FileInputStream fis = new FileInputStream(new File(testConfig.get(Config.DAO_NAME)));
        try {
            CSVReader rdr = new CSVReader(fis, StandardCharsets.UTF_8.name());
            int acctNameIndex = rdr.nextRecord().indexOf("ACCOUNT.NAME");
            assertEquals(accountName, rdr.nextRecord().get(acctNameIndex));
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

    private String insertAccount(String name) throws ConnectionException {
        final SObject account = new SObject();
        account.setType("Account");
        account.setField("Name", name);
        String id = getBinding().create(new SObject[]{account})[0].getId();
        assertNotNull(id);
        return id;
    }

    private String insertContact(String accountId) throws ConnectionException {
        final SObject contact = new SObject();
        contact.setType("Contact");
        contact.setField("AccountId", accountId);
        contact.setField("LastName", "Abc");
        String id = getBinding().create(new SObject[]{contact})[0].getId();
        assertNotNull(id);
        return id;
    }
}
