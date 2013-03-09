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

import com.salesforce.dataloader.ConfigGenerator;
import com.salesforce.dataloader.action.OperationInfo;
import com.salesforce.dataloader.config.Config;
import com.sforce.soap.partner.QueryResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

/**
 * Tests date values used in DataLoader processes
 *
 * @author Colin Jarvis
 * @since 21.0
 */
@RunWith(Parameterized.class)
public class DateProcessTest extends ProcessTestBase {

    public DateProcessTest(Map<String, String> config) {
        super(config);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> getTestParameters() {
        final ConfigGenerator parent = ProcessTestBase.getConfigGenerator();
        final ConfigGenerator withBulkApi = new ConfigSettingGenerator(parent, Config.BULK_API_ENABLED,
                Boolean.TRUE.toString());
        return Arrays.asList(new Object[] {parent.getConfigurations().get(0)}, new Object[] {withBulkApi.getConfigurations().get(0)});
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

    @Test
    public void testDateEndingInZ() throws Exception {
        runProcess(getTestConfig(OperationInfo.insert, false), 1);

        QueryResult qr = getBinding().query("select CustomDateTime__c from Account where AccountNumber__c='ACCT_0'");
        assertEquals(qr.getSize(), 1);

        Date expectedDate = parseDateWithTimezone("2010-10-14T12:00:00.000GMT");
        assertEquals(expectedDate, parseDateFromPartnerApi((String)qr.getRecords()[0].getField("CustomDateTime__c")));
    }

    @Test
    public void testDateUsingDefaultTimeZone() throws Exception {
        runProcess(getTestConfig(OperationInfo.insert, false), 1);
        QueryResult qr = getBinding().query("select CustomDateTime__c from Account where AccountNumber__c='ACCT_0'");
        assertEquals(qr.getSize(), 1);

        Date expectedDate = parseDateWithTimezone("2010-10-14T12:00:00.000PDT");
        assertEquals(expectedDate, parseDateFromPartnerApi((String)qr.getRecords()[0].getField("CustomDateTime__c")));
    }

    @Test
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