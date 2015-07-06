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
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.salesforce.dataloader.TestSetting;
import com.salesforce.dataloader.TestVariant;
import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.config.Config;
import com.sforce.async.CSVReader;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;

import static org.junit.Assert.assertEquals;

/**
 * Test to validate we handle encodings correctly.
 *
 * @author Colin Jarvis
 * @author Federico Recio
 * @since 24
 */
@RunWith(Parameterized.class)
public class CsvEncodingProcessTest extends ProcessTestBase {

    private static final String UTF8 = "UTF-8";
    private static final String JAPANESE = "Shift_JIS";
    private static final String FILE_ENCODING_SYSTEM_PROPERTY = "file.encoding";
    private final Map<String, String> config;
    private final String fileEncoding;
    private String originalFileEncoding;

    public CsvEncodingProcessTest(String fileEncoding, Map<String, String> config) {
        super(config);
        this.config = config;
        this.fileEncoding = fileEncoding;
    }

    @Parameterized.Parameters(name = "{0}, {1}")
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(
                TestVariant.builder().withSettings(TestSetting.BULK_API_ENABLED, TestSetting.WRITE_UTF8_ENABLED, TestSetting.READ_UTF8_ENABLED).withFileEncoding(UTF8).get(),
                TestVariant.builder().withSettings(TestSetting.BULK_API_ENABLED, TestSetting.WRITE_UTF8_DISABLED, TestSetting.READ_UTF8_DISABLED).withFileEncoding(UTF8).get(),
                TestVariant.builder().withSettings(TestSetting.BULK_API_ENABLED, TestSetting.WRITE_UTF8_ENABLED, TestSetting.READ_UTF8_ENABLED).withFileEncoding(JAPANESE).get(),
                TestVariant.builder().withSettings(TestSetting.BULK_API_ENABLED, TestSetting.WRITE_UTF8_DISABLED, TestSetting.READ_UTF8_DISABLED).withFileEncoding(JAPANESE).get());
    }

    @Before
    public void overrideSystemFileEncoding() throws Exception {
        originalFileEncoding = System.getProperty(FILE_ENCODING_SYSTEM_PROPERTY);
        setSystemFileEncoding(fileEncoding);
    }

    @After
    public void restoreOriginalSystemFileEncoding() throws Exception {
        setSystemFileEncoding(originalFileEncoding);
    }

    private void setSystemFileEncoding(String encoding) throws NoSuchFieldException, IllegalAccessException {
        System.setProperty(FILE_ENCODING_SYSTEM_PROPERTY, encoding);
        // following is necessary for new value to be properly read because default charset is cached
        Field charset = Charset.class.getDeclaredField("defaultCharset");
        charset.setAccessible(true);
        charset.set(null, null);
    }

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
        argMap.putAll(config);
        argMap.remove(Config.MAPPING_FILE);
        return argMap;
    }

    private void validateExtraction(final String name, final Map<String, String> testConfig) throws IOException {
        FileInputStream fis = new FileInputStream(new File(testConfig.get(Config.DAO_NAME)));
        try {
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
        return getBinding().create(new SObject[]{account})[0].getId();
    }
}