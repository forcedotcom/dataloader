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

import java.text.*;
import java.util.*;

import junit.framework.TestSuite;

import com.salesforce.dataloader.ConfigGenerator;
import com.salesforce.dataloader.ConfigTestSuite;
import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.config.Config;
import com.sforce.soap.partner.QueryResult;

/**
 * Tests date values used in DataLoader processes
 * 
 * @author Colin Jarvis
 * @since 21.0
 */
public class DateProcessTest extends ProcessTestBase {

    public static TestSuite suite() {
        return ConfigTestSuite.createSuite(DateProcessTest.class);
    }

    public static ConfigGenerator getConfigGenerator() {
        final ConfigGenerator parent = ProcessTestBase.getConfigGenerator();
        final ConfigGenerator withBulkApi = new ConfigSettingGenerator(parent, Config.USE_BULK_API,
                Boolean.TRUE.toString());
        return new UnionConfigGenerator(parent, withBulkApi);
    }

    public DateProcessTest(String name) {
        super(name);
    }

    public DateProcessTest(String name, Map<String, String> config) {
        super(name, config);
    }

    @Override
    protected Map<String, String> getTestConfig() {
        Map<String, String> cfg = super.getTestConfig();
        cfg.put(Config.TIMEZONE, "PDT");
        cfg.put(Config.ENTITY, "Account");
        return cfg;
    }

    private static final TimeZone GMT_TIME_ZONE = TimeZone.getTimeZone("GMT");
    private static final DateFormat PARTNER_API_FMT;
    private static final DateFormat DATE_FMT_WITH_TZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz");
    static {
        PARTNER_API_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        PARTNER_API_FMT.setTimeZone(GMT_TIME_ZONE);
    }

    public void testDateEndingInZ() throws Exception {
        runProcess(getTestConfig(OperationInfo.insert, false), 1);

        QueryResult qr = getBinding().query("select CustomDateTime__c from Account where AccountNumber__c='ACCT_0'");
        assertEquals(qr.getSize(), 1);

        Date expectedDate = parseDateWithTimezone("2010-10-14T12:00:00.000GMT");
        assertEquals(expectedDate, parseDateFromPartnerApi((String)qr.getRecords()[0].getField("CustomDateTime__c")));
    }

    public void testDateUsingDefaultTimeZone() throws Exception {
        runProcess(getTestConfig(OperationInfo.insert, false), 1);
        QueryResult qr = getBinding().query("select CustomDateTime__c from Account where AccountNumber__c='ACCT_0'");
        assertEquals(qr.getSize(), 1);

        Date expectedDate = parseDateWithTimezone("2010-10-14T12:00:00.000GMT");
        assertEquals(expectedDate, parseDateFromPartnerApi((String)qr.getRecords()[0].getField("CustomDateTime__c")));
    }

    public void testDateWithTimeZone() throws Exception {
        runProcess(getTestConfig(OperationInfo.insert, false), 1);
        QueryResult qr = getBinding().query("select CustomDateTime__c from Account where AccountNumber__c='ACCT_0'");
        assertEquals(qr.getSize(), 1);

        Date expectedDate = parseDateWithTimezone("2010-10-14T12:00:00.000-0300");
        assertEquals(expectedDate, parseDateFromPartnerApi((String)qr.getRecords()[0].getField("CustomDateTime__c")));
    }

    private Date parseDateFromPartnerApi(String dateString) throws ParseException {
        return PARTNER_API_FMT.parse(dateString);
    }

    private Date parseDateWithTimezone(String dateString) throws ParseException {
        return DATE_FMT_WITH_TZ.parse(dateString);
    }

}
