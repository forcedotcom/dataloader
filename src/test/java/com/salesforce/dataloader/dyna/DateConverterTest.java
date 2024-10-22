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
package com.salesforce.dataloader.dyna;

import org.apache.commons.beanutils.ConversionException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.salesforce.dataloader.ConfigTestBase;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.util.AppUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DateConverterTest extends ConfigTestBase {

    private static final TimeZone TZ = TimeZone.getTimeZone("GMT");

    /**
     * Verify that equivalent instances in time (string form) but written in different time zones
     * are understood to be the same instant by the calendars and calendar comparison functions.
     *
     * @expectedResults Assert that the String given in Australian time is the same as the String given
     * in GMT.
     *
     * @throws Exception
     */
    @Test
    public void testDateConverterAcknowledgesTimezoneInDate() throws Exception {

        //create a date, October 15th, 7 PM, +0 GMT.
        int actualMonth = 10;
        int actualDay = 15;
        int actualHour = 19;
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        Calendar calDateGMT = Calendar.getInstance(gmt);
        calDateGMT.set(2011, actualMonth-1, actualDay, actualHour, 0,0); //7 PM.
        calDateGMT.set(Calendar.MILLISECOND, 0);

        //create a date, October 11th, 3 AM, +8 GMT.
        TimeZone wst = TimeZone.getTimeZone("Australia/Perth");
        Calendar calDateWST = Calendar.getInstance(wst);
        calDateWST.setTimeZone(wst);
        calDateWST.set(2011, 9, 16, 3, 0,0); // 3 AM.
        calDateWST.set(Calendar.MILLISECOND, 0);

        assertEquals("Incorrect time zone offset in GMT", 0 , calDateGMT.getTimeZone().getOffset(calDateGMT.getTimeInMillis()));
        assertEquals("Incorrect time zone offset in WST Aust Cal", 8*1000*60*60, calDateWST.getTimeZone().getOffset(calDateWST.getTimeInMillis()));

        //assert that the above calendars map to both of these string formats:
        String happyHourNoTimeZoneGMT ="2011-10-15T19:00:00z"; // 7 PM GMT

        //assert that both calendars are on the same time
        assertValidDate(happyHourNoTimeZoneGMT, calDateGMT, false);
        assertValidDate(happyHourNoTimeZoneGMT, calDateWST, false);

        String happyHourGMT = "2011-10-15T19:00:00+0000"; // 8 PM GMT
        String happyHourWST = "2011-10-16T03:00:00+0800"; // 3 AM Australia time

        assertValidDate(happyHourGMT, calDateWST, false);
        assertValidDate(happyHourWST, calDateWST, false);
        assertValidDate(happyHourWST, calDateGMT, false);
    }

    /**
     * Verify that when seconds are not specified in the time, that this still registers
     * as a valid calendar.
     * This is to comply with ISO8601 5.2.1.2
     *
     * This is by that iso8601 standard.
     *
     * @expectedResults Assert that the date string is a valid calendar of that same instant.
     */
    @Test
    public void testTimeConversationOfSecondSpecificityWithRespectToTimeZones() throws Exception {

        TimeZone gmt = TimeZone.getTimeZone("GMT");
        Calendar calDateGMT = Calendar.getInstance(gmt);
        calDateGMT.set(2011, 9, 15, 19, 8,0); //7 PM.
        calDateGMT.set(Calendar.MILLISECOND, 0);

        String dateString = "2011-10-15T19:08z";
        String dateStringWithoutExplicitTimeZone = "2011-10-15T19:08";
        String dateStringWithExplicitTimeZone = "2011-10-15T16:08-0300";
        String dateStringWithoutT = "2011-10-15 19:08z";

        assertValidDate("String to calendar conversation fails when seconds not specified", dateString, calDateGMT, false);
        assertValidDate("String to calendar conversation fails when seconds and time zone are not specified", dateStringWithoutExplicitTimeZone, calDateGMT, false);
        assertValidDate("String to calendar conversation fails when a non-GMT timezone is specified", dateStringWithExplicitTimeZone, calDateGMT, false);
        assertValidDate("String to calendar conversation fails when seconds not specified with sans-T format", dateStringWithoutT, calDateGMT, false);
    }

    /**
     * Verify that minutes do not necessarily need to be specified when giving a date or time.
     * This is to comply with ISO8601 5.2.1.2
     *
     * @expectedResults Assert that the string is converted to a calendar correctly.
     *
     * @throws Exception
     */
    @Test
    public void testTimeConversationOfMinuteSpecificityWithRespectToTimeZones() throws Exception {

        TimeZone gmt = TimeZone.getTimeZone("GMT");
        Calendar calDateGMT = Calendar.getInstance(gmt);
        calDateGMT.set(2011, 9, 15, 19, 0,0); //7 PM.
        calDateGMT.set(Calendar.MILLISECOND, 0);

        //create the inputs
        String dateString = "2011-10-15T19z";
        String dateStringWithoutExplicitTimeZone = "2011-10-15T19";
        String dateStringWithExplicitTimeZone = "2011-10-15T16-0300";
        String dateStringWithoutT = "2011-10-15 19z";

        //test the inputs
        assertValidDate("String to calendar conversation fails when minutes not specified", dateString, calDateGMT, false);
        assertValidDate("String to calendar conversation fails when minutes and time zone are not specified", dateStringWithoutExplicitTimeZone, calDateGMT, false);
        assertValidDate("String to calendar conversation fails when minutes not specified with non-GMT timezone", dateStringWithExplicitTimeZone, calDateGMT, false);
        assertValidDate("String to calendar conversation fails when minutes not specified with sans-T format", dateStringWithoutT, calDateGMT, false);
    }

    /**
     * Verify that the full basic format, as defined in ISO-8601 (Section 5.4.1), is supported.
     * Basic format means that all delimiting characters are removed.
     * For example, "2011-10-15T14:33:22.343" in extended is -> "20111015T143322343"
     *
     * This is testing with and without time zone consideration.
     * It is also tested against the 'T' delimiter.
     *
     * @expectedResults Assert that the strings maps to the same instant as the calendar.
     *
     */
    @Test
    public void testFullBasicFormat() {

        TimeZone gmt = TimeZone.getTimeZone("GMT");
        Calendar calDateGMT = Calendar.getInstance(gmt);
        calDateGMT.setLenient(false); //should hopefully prevent overflow
        calDateGMT.set(2011, 9, 15, 19, 0,0); //7 PM.
        calDateGMT.set(Calendar.MILLISECOND, 0);

        String dateString = "20111015T19z";
        String dateStringWithoutExplicitTimeZone = "20111015T19";
        String dateStringWithFullBasicTime = "20111015T190000";
        String dateStringWithoutT = "20111015 1900z";

        assertValidDate("String to calendar conversation fails when minutes not specified", dateString, calDateGMT, false);
        assertValidDate("String to calendar conversation fails when minutes and time zone are not specified", dateStringWithoutExplicitTimeZone, calDateGMT, false);
        assertValidDate("String to calendar conversation fails when minutes not specified with non-GMT timezone", dateStringWithFullBasicTime, calDateGMT, false);
        assertValidDate("String to calendar conversation fails when minutes not specified with sans-T format", dateStringWithoutT, calDateGMT, false);

        dateStringWithoutT = "20111015"; //yyyyMMdd
        calDateGMT.set(2011, 9, 15, 0, 0, 0);
        assertValidDate("String to calendar conversation fails when minutes not specified with sans-T format", dateStringWithoutT, calDateGMT, false);
    }

    /**
     * Verify that converting a null input or empty string will result in a null
     * calendar output.
     *
     * @expectedResults Assert that the Calendar object created is null.
     */
    @Test
    public void testDateConverterWithNull() {

        Calendar calDate;
        DateTimeConverter converter = new DateTimeConverter(TZ, false);

        // test null and empty string
        calDate = (Calendar) converter.convert(null, null);
        assertNull(calDate);

        calDate = (Calendar) converter.convert(null, "");
        assertNull(calDate);
    }

    /**
     * Verify that if a calendar or a date is passed to the converter, the converter returns
     * back the a calendar or date that is equivalent.
     *
     * @expectedResults Assert that both objects are equal.
     */
    @Test
    public void testDateClosureUnderCalendarConversion() {

        Calendar calDate;
        DateTimeConverter converter = new DateTimeConverter(TZ, false);

        // if we pass in a calendar, should get the same Calendar back
        Calendar testCalDate = Calendar.getInstance();
        calDate = (Calendar)converter.convert(null, testCalDate);
        assertEquals(testCalDate, calDate);

        // if we pass in a date, we should get a Date back
        Date testDate = new Date();
        calDate = (Calendar)converter.convert(null, testDate);
        assertEquals(testDate, calDate.getTime());
    }

    /**
     * Verify that an input with a iso8601-compliant date string that has a time
     * zone can be correctly interpreted to equal a GMT time.
     *
     * @expectedResults Assert that the GMT and timezone-specified times equate
     *                  to the same instant.
     */
    @Test
    public void testTimeZoneIsRecognized() {

        Calendar expCalDate = Calendar.getInstance(TZ);

        // test the valid date format
        expCalDate.clear();
        expCalDate.set(2001, 11 - 1, 11, 10, 11, 40);
        assertValidDate("2001-11-11T10:11:40.000Z", expCalDate, false);

        // same date but with time zone
        assertValidDate("2001-11-11T02:11:40.000Z-0800", expCalDate, false);
    }

    /**
     * Verify that the iso8601 pattern of having a space instead of 'T' to delimit date and time.
     *
     * @expectedResults Assert that the calendar and the string will evaluate to the same instant.
     */
    @Test
    public void testNotInDelimiterPattern() {

        // use this as the expected calendar instance
        Calendar expCalDate = Calendar.getInstance(TZ);

        expCalDate.clear();
        expCalDate.set(2004, 3 - 1, 29, 11, 30, 23);
        assertValidDate("2004-03-29 11:30:23", expCalDate, false);
    }


    /**
     * Verify that specifying various degrees of specificity in the time are all processed correctly wrt the time that they represent.
     * Note that there is no such thing as European format with respect to ISO 8601. Namely, the specific order
     * of the month and date is there to remove any ambiguity.
     *
     * @expectedResults Assert that all strings correct map to their corresponding calendars.
     */
    @Test
    public void testDegreesOfPrecisionInTimeString() {

        TimeZone gmt = TimeZone.getTimeZone("GMT");
        Calendar millisecondPrecisionCalendar = Calendar.getInstance(gmt);
        Calendar secondPrecisionCalendar = Calendar.getInstance(gmt);
        Calendar minutePrecisionCalendar = Calendar.getInstance(gmt);
        Calendar hourPrecisionCalendar = Calendar.getInstance(gmt);

        Integer year = 2011;
        Integer month = 10;
        Integer day = 15;
        Integer hours = 19;
        Integer minutes = 18;
        Integer seconds = 57;
        Integer milliseconds = 329;

        millisecondPrecisionCalendar.set(year, month - 1, day, hours, minutes, seconds);
        millisecondPrecisionCalendar.set(Calendar.MILLISECOND, milliseconds);

        secondPrecisionCalendar.set(year, month - 1, day, hours, minutes, seconds);
        secondPrecisionCalendar.set(Calendar.MILLISECOND, 0);

        minutePrecisionCalendar.set(year, month - 1, day, hours, minutes, 0);
        minutePrecisionCalendar.set(Calendar.MILLISECOND, 0);

        hourPrecisionCalendar.set(year, month - 1, day, hours, 0, 0);
        hourPrecisionCalendar.set(Calendar.MILLISECOND, 0);

        for (String dayDateDelimiter : new String[] { "T", " " }) {
            for (String dateDel : new String[] { "-", "" }) {

                String hourString = year.toString() + dateDel + month.toString() + dateDel + day.toString()
                        + dayDateDelimiter + hours.toString();
                String minuteString = hourString + ":" + minutes;
                String secondString = minuteString + ":" + seconds.toString();
                String millisecondString = secondString + "." + milliseconds.toString();

                assertValidDate(millisecondString, millisecondPrecisionCalendar, false);
                assertValidDate(secondString, secondPrecisionCalendar, false);
                assertValidDate(minuteString, minutePrecisionCalendar, false);
                assertValidDate(hourString, hourPrecisionCalendar, false);
            }
        }
    }


    /**
     * Verify that if a string just gives the date, that there is no ambiguity.
     *
     * @expectedResults Assert that date-only is treated as a case of dateTime
     *                  where it is 00:00:00 on that day.
     */
    @Test
    public void testDateOnly() {
        testDateOnly(true);
        testDateOnly(false);
    }
    
    private void testDateOnly(boolean useEuropeanDateFormat) {
        Calendar expCalDate = Calendar.getInstance(TZ);

        expCalDate.clear();
        // yyyy, mm, dd
        expCalDate.set(2020, 10, 05);

        assertValidDate("2020-11-05 00:00:00z", expCalDate, useEuropeanDateFormat);
        assertValidDate("2020-11-05 00:00:00Z", expCalDate, useEuropeanDateFormat);
        assertValidDate("2020-11-05 00:00:00", expCalDate, useEuropeanDateFormat);
        assertValidDate("2020-11-05 00:00", expCalDate, useEuropeanDateFormat);
        assertValidDate("2020-11-05 00", expCalDate, useEuropeanDateFormat);
        assertValidDate("2020-11-05", expCalDate, useEuropeanDateFormat);
        assertValidDate("2020-11-05 ", expCalDate, useEuropeanDateFormat);
        assertValidDate("2020-11-05T", expCalDate, useEuropeanDateFormat);
        assertValidDate("2020-11-05Tz", expCalDate, useEuropeanDateFormat);
        assertValidDate("2020-11-05TZ", expCalDate, useEuropeanDateFormat);
        assertValidDate("20201105", expCalDate, useEuropeanDateFormat);
        assertValidDate("20201105 ", expCalDate, useEuropeanDateFormat);
        if (useEuropeanDateFormat) {
            assertValidDate("05/11/2020", expCalDate, useEuropeanDateFormat);
        } else {
            assertValidDate("11/05/2020", expCalDate, useEuropeanDateFormat);
        }

        //should fail
        assertStringAndCalendarDoNotMatch("2020-11-05 00:00:01", expCalDate, useEuropeanDateFormat);
    }


    /**
     * Verify that a string with date information can also contain timeZone information
     *
     * @expectedResults
     */
    @Test
    public void testDateWithTimeZone() {

        Calendar expCalDate = Calendar.getInstance(TZ);

        TimeZone wst = TimeZone.getTimeZone("Australia/Perth");
        Calendar calDateWST = Calendar.getInstance(wst);
        Calendar calDateWST2 = Calendar.getInstance(wst);
        Calendar calDateWST3 = Calendar.getInstance(wst);

        //set up GMT calendar
        expCalDate.clear();
        expCalDate.set(2004, 03, 29);
        expCalDate.set(Calendar.MILLISECOND, 0);

        //set up Australia calendar I
        calDateWST.clear();
        calDateWST.set(2004, 03, 29, 8,0,0);
        calDateWST.set(Calendar.MILLISECOND, 0);

        //set up Australia calendar II
        calDateWST2.clear();
        calDateWST2.set(2004, 03, 28, 0,0,0);
        calDateWST2.set(Calendar.MILLISECOND, 0);

        //set up Australia calendar III
        calDateWST3.clear();
        calDateWST3.set(2004, 03, 29, 6,0,0);
        calDateWST3.set(Calendar.MILLISECOND, 0);

        //test that having just the 'z' indicates default time zone and doesn't cause parse failure
        assertValidDate("2004-04-29Tz", expCalDate, false);

        //test that +0000 parses the same way as z
        assertValidDate("2004-04-29T-0000", calDateWST, false);

        //test varying levels of precision with time and timeZone
        assertValidDate("2004-04-29T00:00:00+0200", calDateWST3, false);
        assertValidDate("2004-04-29T00:00+0200", calDateWST3, false);
        assertValidDate("2004-04-29T00+0200", calDateWST3, false);
    }
    /**
     *
     * Verify that omitted date information will be replaced with expected default values.
     *
     * @expectedResults Assert that the interpreted string will be treated
     *
     */
    @Test
    public void testDatePrecision() {

        TimeZone gmt = TimeZone.getTimeZone("GMT");
        Calendar dateCalendar = Calendar.getInstance(gmt);
        Calendar monthCalendar = Calendar.getInstance(gmt);
        Calendar yearCalendar = Calendar.getInstance(gmt);

        Integer year = 2011;
        Integer month = 10;
        Integer day = 15;
        Integer milliseconds = 0;

        dateCalendar.clear();
        dateCalendar.set(year, month - 1, day);
        dateCalendar.set(Calendar.MILLISECOND, milliseconds);

        monthCalendar.clear();
        monthCalendar.set(year, month - 1, 1);
        monthCalendar.set(Calendar.MILLISECOND, 0);

        yearCalendar.clear();
        yearCalendar.set(year, 0, 1);
        yearCalendar.set(Calendar.MILLISECOND, 0);


        //basic (5.2.1.1 of ISO 8601 standard)
        assertValidDate(year.toString() + month.toString() + day.toString(), dateCalendar, false);
        assertValidDate(year.toString(), yearCalendar, false);

        //extended
        assertValidDate(year.toString() + "-" + month.toString() + "-" + day.toString(), dateCalendar, false);

        //this one is tricky (see 5.2.1.1 of ISO 8601)
        assertValidDate(year.toString() + "-" + month.toString(), monthCalendar, false);
    }


    /**
     * Verify that precision to the level of milliseconds is supported.
     *
     * @expectedResults Assert that the calendar to which the string is converted represents
     * the same instant as the original calendar.
     *
     */
    @Test
    public void testMillisecondPrecisionSupported() {

        Calendar expCalDate = Calendar.getInstance(TZ);

        expCalDate.clear();
        expCalDate.set(1999, 12 - 1, 24, 11, 11, 11);
        expCalDate.set(Calendar.MILLISECOND, 111);
        assertValidDate("1999-12-24T11:11:11.111z", expCalDate, false);
    }


    /**
     * Verify that an input string without dashes can be correctly interpreted as a date.
     *
     * @expectedResults Assert that the calendar to which the string is converted represents
     * the same instant as the original calendar.
     */
    @Test
    public void testInputStringWithoutDashes() {

        // use this as the expected calendar instance
        Calendar expCalDate = Calendar.getInstance(TZ);

        expCalDate.clear();
        expCalDate.set(1977, 12 - 1, 24, 07, 36, 44);
        assertValidDate("19771224T07:36:44", expCalDate, false);
    }


    /**
     * Verify that a string without timezone information still parses to a calendar
     *
     * @expectedResults Assert that the string and converted calendar equate to the same instance.
     *
     */
    @Test
    public void testInputStringWithoutTimeZoneInformation() {

        Calendar expCalDate = Calendar.getInstance(TZ);

        expCalDate.clear();
        expCalDate.set(1984, 04 - 1, 12, 6, 34, 22);
        assertValidDate("1984-04-12T06:34:22", expCalDate, false);
        assertValidDate("1984-04-12T08:34:22+02:00", expCalDate, false);
    }


    /**
     * Verify that a calendar is able to handle a string that only has date information.
     *
     * @expectedResults Assert that the String converted to a calendar evaluates to the same date
     * as the calendar.
     *
     */
    @Test
    public void testInputDateString() {

        // use this as the expected calendar instance
        Calendar expCalDate = Calendar.getInstance(TZ);

        expCalDate.clear();
        expCalDate.set(1999, 9 - 1, 11);
        assertValidDate("1999-09-11", expCalDate, false);
    }


    /**
     * Verify that using '/' to delimit date elements, along with a
     * space to delimit the date from the time is supported by the converter.
     *
     * @expectedResults Assert that the date and time in the string map to the date
     * and time in the calendar.
     *
     */
    @Test
    public void testSlashAndNoTDelimiterFormat() {

        Calendar expCalDate = Calendar.getInstance(TZ);

        expCalDate.clear();
        expCalDate.set(2009, 7 - 1, 16, 12, 14, 45);
        assertValidDate("07/16/2009 12:14:45", expCalDate, false);

        expCalDate.clear();
        expCalDate.set(2007, 8 - 1, 23);
        assertValidDate("08/23/2007", expCalDate, false);

        expCalDate.clear();
        expCalDate.set(2002, 2 - 1, 16);
        assertValidDate("2/16/2002", expCalDate, false);
    }

    /**
     * Verify that using '/' with timezone in the regular format to delimit date elements, along with a space to
     * delimit the date from the time, is supported by the converter.
     *
     * @expectedResults Assert that the date and time in the string map to the date
     * and time in the calendar.
     *
     */
    @Test
    public void testSlashWithTimeZoneDelimiterFormat() {

        TimeZone wst = TimeZone.getTimeZone("Australia/Perth");
        Calendar calDateWST = Calendar.getInstance(wst);

        calDateWST.clear();
        calDateWST.set(2009, 7 - 1, 16, 12, 14, 45);

        for (String delimiter : new String[] { " ", "T" }) {

            //vanilla cases that don't cross into different days
            assertValidDate("07/16/2009" + delimiter + "12:14:45+0800", calDateWST,    false);
            assertValidDate("07/16/2009" + delimiter + "02:14:45-0200", calDateWST, false); // offset case
            assertValidDate("07/16/2009" + delimiter + "16:14:45+1200", calDateWST, false); // offset case

            //cross-day cases
            assertValidDate("07/16/2009" + delimiter + "03:14:45-0100", calDateWST,    false);
            assertValidDate("07/16/2009" + delimiter + "12:14:45+0800", calDateWST, false); // offset case
        }
    }

    /**
     * Verify that using '/' with timezone in the european format to delimit date elements, along with a space to
     * delimit the date from the time, is supported by the converter.
     *
     * @expectedResults Assert that the date and time in the string map to the
     *                  date and time in the calendar.
     *
     */
    @Test
    public void testSlashWithTimeZoneDelimiterFormatEuropeanFormat() {

        TimeZone wst = TimeZone.getTimeZone("Australia/Perth");
        Calendar calDateWST = Calendar.getInstance(wst);

        calDateWST.clear();
        calDateWST.set(2009, 7 - 1, 16, 12, 14, 45);

        for (String delimiter : new String[] { " ", "T" }) {

            //vanilla cases that don't cross into different days
            assertValidDate("16/07/2009" + delimiter + "12:14:45+0800", calDateWST,    true);
            assertValidDate("16/07/2009" + delimiter + "02:14:45-0200", calDateWST, true); // offset case
            assertValidDate("16/07/2009" + delimiter + "16:14:45+1200", calDateWST, true); // offset case

            //cross-day cases
            assertValidDate("16/07/2009" + delimiter + "03:14:45-0100", calDateWST,    true);
            assertValidDate("16/07/2009" + delimiter + "12:14:45+0800", calDateWST, true); // offset case
        }
    }

    /**
     * Verify that dates given in European format correspond properly to the
     * calendar date.
     *
     * @expectedResults Assert that the string converts to the same instant as the calendar.
     */
    @Test
    public void testEuropeanDatesWithSlashes() {

        Calendar expCalDate = Calendar.getInstance(TZ);

        expCalDate.clear();
        expCalDate.set(2009, 7 - 1, 16, 12, 14, 45);
        assertValidDate("16/7/2009 12:14:45", expCalDate, true);

        expCalDate.clear();
        expCalDate.set(2007, 8 - 1, 23);
        assertValidDate("23/08/2007", expCalDate, true);

        expCalDate.clear();
        expCalDate.set(2002, 2 - 1, 16);
        assertValidDate("16/2/2002", expCalDate, true);
    }

    /**
     * Verify that the conversion will fail for invalid dates
     *
     * @expectedResults Assert that an exception is thrown.
     */
    @Test
    public void testDateConverterNegative() {
        assertInvalidDate("fofofod", null, false);
        assertInvalidDate("20A4-11-08", null, false);
    }

    // test user-specified timezones and date formats specified at
    // https://developer.salesforce.com/docs/atlas.en-us.dataLoader.meta/dataLoader/supported_data_types.htm
    @Test
    public void testUserSpecifiedTimeZoneIsUsed() throws Exception {
        DateTimeConverter AsianTZDateConverter = new DateTimeConverter(TimeZone.getTimeZone("Asia/Tokyo"), false);
        DateTimeConverter USTZDateConverter = new DateTimeConverter(TimeZone.getTimeZone("America/Los_Angeles"), false);
        DateTimeConverter GMTTZDateConverter = new DateTimeConverter(TimeZone.getTimeZone("GMT"), false);
        DateOnlyConverter AsianTZDateOnlyConverter = new DateOnlyConverter(TimeZone.getTimeZone("Asia/Tokyo"), false);
        DateOnlyConverter USTZDateOnlyConverter = new DateOnlyConverter(TimeZone.getTimeZone("America/Los_Angeles"), false);
        DateOnlyConverter GMTTZDateOnlyConverter = new DateOnlyConverter(TimeZone.getTimeZone("GMT"), false);

        // DateConverter should always return the Calendar in GMT.
        Calendar result = (Calendar) AsianTZDateConverter.convert(null, "6/7/2012");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("Asia/Tokyo"), result.getTimeZone());

        result = (Calendar) USTZDateConverter.convert(null, "6/7/2012");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("America/Los_Angeles"), result.getTimeZone());

        result = (Calendar) GMTTZDateConverter.convert(null, "6/7/2012");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("GMT"), result.getTimeZone());

        // DateConverter should always return the Calendar in GMT.
        result = (Calendar) AsianTZDateOnlyConverter.convert(null, "6/22/2012");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(22, result.get(Calendar.DAY_OF_MONTH));

        this.getController().getAppConfig().setValue(AppConfig.PROP_GMT_FOR_DATE_FIELD_VALUE, true);
        AsianTZDateOnlyConverter = new DateOnlyConverter(TimeZone.getTimeZone("Asia/Tokyo"), false);
        USTZDateOnlyConverter = new DateOnlyConverter(TimeZone.getTimeZone("America/Los_Angeles"), false);
        result = (Calendar) USTZDateOnlyConverter.convert(null, "6/22/2012");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(22, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("GMT"), result.getTimeZone());

        AsianTZDateOnlyConverter = new DateOnlyConverter(TimeZone.getTimeZone("Asia/Tokyo"), false);
        USTZDateOnlyConverter = new DateOnlyConverter(TimeZone.getTimeZone("America/Los_Angeles"), false);
        result = (Calendar) AsianTZDateOnlyConverter.convert(null, "6/7/2012 0:00");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH) + 1);
        assertEquals(TimeZone.getTimeZone("GMT"), result.getTimeZone());

        AsianTZDateOnlyConverter = new DateOnlyConverter(TimeZone.getTimeZone("Asia/Tokyo"), false);
        USTZDateOnlyConverter = new DateOnlyConverter(TimeZone.getTimeZone("America/Los_Angeles"), false);
        result = (Calendar) AsianTZDateOnlyConverter.convert(null, "6/7/2012 02:00");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH) + 1);
        assertEquals(TimeZone.getTimeZone("GMT"), result.getTimeZone());

        // JST is 9 hours ahead of GMT
        // Any time after 9am in Japan is the same day in GMT
        AsianTZDateOnlyConverter = new DateOnlyConverter(TimeZone.getTimeZone("Asia/Tokyo"), false);
        USTZDateOnlyConverter = new DateOnlyConverter(TimeZone.getTimeZone("America/Los_Angeles"), false);
        result = (Calendar) AsianTZDateOnlyConverter.convert(null, "6/7/2012 11:00");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("GMT"), result.getTimeZone());

        AsianTZDateOnlyConverter = new DateOnlyConverter(TimeZone.getTimeZone("Asia/Tokyo"), false);
        USTZDateOnlyConverter = new DateOnlyConverter(TimeZone.getTimeZone("America/Los_Angeles"), false);
        result = (Calendar) AsianTZDateOnlyConverter.convert(null, "6/7/2012 23:00");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("GMT"), result.getTimeZone());

        this.getController().getAppConfig().setValue(AppConfig.PROP_GMT_FOR_DATE_FIELD_VALUE, false);
        AsianTZDateOnlyConverter = new DateOnlyConverter(TimeZone.getTimeZone("Asia/Tokyo"), false);
        USTZDateOnlyConverter = new DateOnlyConverter(TimeZone.getTimeZone("America/Los_Angeles"), false);

        result = (Calendar) GMTTZDateOnlyConverter.convert(null, "6/22/2012");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(22, result.get(Calendar.DAY_OF_MONTH));

        result = (Calendar) AsianTZDateConverter.convert(null, "6/7/2012 0:00");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("Asia/Tokyo"), result.getTimeZone());

        result = (Calendar) AsianTZDateOnlyConverter.convert(null, "6/7/2012 04:00");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("Asia/Tokyo"), result.getTimeZone());

        result = (Calendar) AsianTZDateOnlyConverter.convert(null, "6/7/2012 11:00");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("Asia/Tokyo"), result.getTimeZone());

        result = (Calendar) AsianTZDateOnlyConverter.convert(null, "6/7/2012 17:00");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("Asia/Tokyo"), result.getTimeZone());

        result = (Calendar) USTZDateConverter.convert(null, "6/7/2012 0:00");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("America/Los_Angeles"), result.getTimeZone());

        result = (Calendar) USTZDateOnlyConverter.convert(null, "6/7/2012 11:00");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("America/Los_Angeles"), result.getTimeZone());

        result = (Calendar) USTZDateOnlyConverter.convert(null, "6/7/2012 23:00");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("America/Los_Angeles"), result.getTimeZone());

        result = (Calendar) AsianTZDateConverter.convert(null, "2012-06-07 00:00:00JST");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("Asia/Tokyo"), result.getTimeZone());

        result = (Calendar) AsianTZDateOnlyConverter.convert(null, "2012-06-07 10:00:00JST");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("Asia/Tokyo"), result.getTimeZone());

        result = (Calendar) AsianTZDateOnlyConverter.convert(null, "2012-06-07 22:00:00JST");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("Asia/Tokyo"), result.getTimeZone());

        result = (Calendar) USTZDateConverter.convert(null, "2012-06-07 00:00:00PST");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("America/Los_Angeles"), result.getTimeZone());

        result = (Calendar) USTZDateConverter.convert(null, "2012-06-07 00:00:00 PST");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("America/Los_Angeles"), result.getTimeZone());

        result = (Calendar) USTZDateConverter.convert(null, "2012-06-07 00:00:00Pacific Standard Time");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("America/Los_Angeles"), result.getTimeZone());

        result = (Calendar) USTZDateConverter.convert(null, "2012-06-07 00:00:00 Pacific Standard Time");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("America/Los_Angeles"), result.getTimeZone());

        result = (Calendar) USTZDateConverter.convert(null, "2012-06-07 00:00:00GMT-08:00");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("America/Los_Angeles"), result.getTimeZone());
 
        result = (Calendar) USTZDateConverter.convert(null, "2012-06-07 00:00:00 GMT-08:00");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("America/Los_Angeles"), result.getTimeZone());
        
        result = (Calendar) USTZDateConverter.convert(null, "2012-06-07 00:00:00-08:00");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("America/Los_Angeles"), result.getTimeZone());
        
        result = (Calendar) USTZDateConverter.convert(null, "2012-06-07 00:00:00 -08:00");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("America/Los_Angeles"), result.getTimeZone());
        
        result = (Calendar) USTZDateConverter.convert(null, "2012-06-07 00:00:00-0800");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("America/Los_Angeles"), result.getTimeZone());
        
        result = (Calendar) USTZDateConverter.convert(null, "2012-06-07 00:00:00 -0800");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("America/Los_Angeles"), result.getTimeZone());
        
        result = (Calendar) AsianTZDateConverter.convert(null, "2012-06-07 00:00:00JST");
        assertEquals(6, result.get(Calendar.MONTH) + 1);
        assertEquals(7, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(TimeZone.getTimeZone("Asia/Tokyo"), result.getTimeZone());

        // Make sure the date is not changed in Japan DST
        result = (Calendar) AsianTZDateOnlyConverter.convert(null, "1948-05-01"); // JST(UTC+9)
        assertEquals(5, result.get(Calendar.MONTH) + 1);
        assertEquals(1, result.get(Calendar.DAY_OF_MONTH));

        result = (Calendar) AsianTZDateOnlyConverter.convert(null, "1948-05-02"); // DST(UTC+10)
        assertEquals(5, result.get(Calendar.MONTH) + 1);
        assertEquals(2, result.get(Calendar.DAY_OF_MONTH));
     }

    private void assertValidDate(String msg, String strDate, Calendar expCalDate, boolean useEuropean) {

        DateTimeConverter converter = new DateTimeConverter(TZ, useEuropean);
        Calendar calFromString = (Calendar)converter.convert(null, strDate);  //converter is set to be
        assertNotNull(calFromString);  //here, caldate is set to be in pacific time
        calFromString.setLenient(false);
        Date timeFromExpectedCal = expCalDate.getTime();
        Date timeFromStringCal = calFromString.getTime();
        assertEquals(msg, timeFromExpectedCal, timeFromStringCal);
    }

    private void assertValidDate(String strDate, Calendar expCalDate, boolean useEuropean) {

        assertValidDate(null, strDate, expCalDate, useEuropean);
    }

    private void assertStringAndCalendarDoNotMatch(String strDate, Calendar expCalDate, boolean useEuropean) {

        DateTimeConverter converter = new DateTimeConverter(TZ, useEuropean);
        Calendar calFromString = (Calendar)converter.convert(null, strDate);  //converter is set to be
        assertNotNull(calFromString);  //here, caldate is set to be in pacific time
        calFromString.setLenient(false);
        Date timeFromExpectedCal = expCalDate.getTime();
        Date timeFromStringCal = calFromString.getTime();
        assertFalse("String does match the calendar, though they should represent different instants", timeFromExpectedCal.equals(timeFromStringCal));
    }


    private void assertInvalidDate(String strDate, Calendar expCalDate, boolean useEuropean) {

        DateTimeConverter converter = new DateTimeConverter(TZ, useEuropean);
        try {
            converter.convert(null, strDate); // converter is set to be
            Assert.fail("The conversion of an invalid string into a valid date occurred");
        } catch (ConversionException c) {
            assertEquals("Incorrect error message on date conversion failure", "Failed to parse date: " + strDate,
                    c.getLocalizedMessage());
        }
    }
}
