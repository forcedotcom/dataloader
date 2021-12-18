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

import com.salesforce.dataloader.TestSetting;
import com.salesforce.dataloader.TestVariant;
import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.config.Config;
import com.sforce.soap.partner.QueryResult;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

/**
 * Tests date-only values used in DataLoader processes
 *
 */
@RunWith(Parameterized.class)
public class DateOnlyProcessTest extends ProcessTestBase {

    private static final TimeZone GMT_TIME_ZONE = TimeZone.getTimeZone("GMT");
    private final DateFormat partnerApiDateFormat;

    public DateOnlyProcessTest(Map<String, String> config) {
        super(config);
        partnerApiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        partnerApiDateFormat.setTimeZone(GMT_TIME_ZONE);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> getTestParameters() {
        return Arrays.asList(
                TestVariant.defaultSettings(),
                TestVariant.forSettings(TestSetting.BULK_API_ENABLED));

    }

    @Override
    protected Map<String, String> getTestConfig() {
        Map<String, String> cfg = super.getTestConfig();
        cfg.put(Config.ENTITY, "Account");
        return cfg;
    }

    @Test
    public void testDateEndingInZ() throws Exception {
        Map<String, String> testConfig = getTestConfig(OperationInfo.insert, false);

        // need to do this before calling runProcess to avoid incorrect timezone setting for DateOnlyConverter
        testConfig.put(Config.TIMEZONE, "GMT"); 
        runProcess(testConfig, 1);

        QueryResult qr = getBinding().query("select CustomDateOnly__c from Account where AccountNumber__c='ACCT_0'");
        assertEquals(1, qr.getSize());

        assertEquals("2010-10-14", (String)qr.getRecords()[0].getField("CustomDateOnly__c"));
    }

    @Test
    public void testDateUsingDefaultTimeZone() throws Exception {
        Map<String, String> testConfig = getTestConfig(OperationInfo.insert, false);

        // need to do this before calling runProcess to avoid incorrect timezone setting for DateOnlyConverter
        testConfig.put(Config.TIMEZONE, "PDT");
        runProcess(testConfig, 1);
        QueryResult qr = getBinding().query("select CustomDateOnly__c from Account where AccountNumber__c='ACCT_0'");
        assertEquals(1, qr.getSize());

        assertEquals("2010-10-14", (String)qr.getRecords()[0].getField("CustomDateOnly__c"));
    }

    @Test
    public void testDateWithTimeZone() throws Exception {
        Map<String, String> testConfig = getTestConfig(OperationInfo.insert, false);
        
     // need to do this before calling runProcess to avoid incorrect timezone setting for DateOnlyConverter
        testConfig.put(Config.TIMEZONE, "IST");
        runProcess(testConfig, 2);
        QueryResult qr = getBinding().query("select CustomDateOnly__c from Account where AccountNumber__c='ACCT_0'");
        assertEquals(1, qr.getSize());

        // 1st entry specifies the date-only field in Zulu format
        assertEquals("2010-10-14", (String)qr.getRecords()[0].getField("CustomDateOnly__c"));

        qr = getBinding().query("select CustomDateOnly__c from Account where AccountNumber__c='ACCT_1'");
        assertEquals(1, qr.getSize());

        // 2nd entry specifies the date-only field without 'Z'
        assertEquals("2010-10-15", (String)qr.getRecords()[0].getField("CustomDateOnly__c"));

    }
}