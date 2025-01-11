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
package com.salesforce.dataloader.util;
import static org.junit.Assert.*;
import java.util.Calendar; 
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class DateOnlyCalendarTest {
    private DateOnlyCalendar dateOnlyCalendar;
    
    @Before
    public void setUp() {
        dateOnlyCalendar = new DateOnlyCalendar();
    }
    
    @Test
    public void testSetTimeInMillisWithDefaultTimeZone() {
        long timeInMillis = 1633046400000L; // 2021-10-01 00:00:00 GMT
        dateOnlyCalendar.setTimeInMillis(timeInMillis);
        // 2021-09-30 17:00:00 PST is transformed to 2021-09-30 00:00:00 PST by DateOnlyCalendar
        // which is 1632985200000L
        assertEquals(1632985200000L, dateOnlyCalendar.getTimeInMillis());
    }
    
    @Test
    public void testSetTimeInMillisWithCustomTimeZone() {
        TimeZone timeZone = TimeZone.getTimeZone("PST");
        DateOnlyCalendar customCalendar = DateOnlyCalendar.getInstance(timeZone);
        long timeInMillis = 1633046400000L; // 2021-10-01 00:00:00 GMT, translates to 2021-09-30 17:00:00 PST
        customCalendar.setTimeInMillis(timeInMillis);
        // 2021-09-30 17:00:00 PST is transformed to 2021-09-30 00:00:00 PST by DateOnlyCalendar
        // which is 1632985200000L
        assertEquals(1632985200000L, customCalendar.getTimeInMillis());
    }
    
    @Test
    public void testSetTimeInMillisWithNullTimeZone() {
        dateOnlyCalendar.setTimeZone(null);
        long timeInMillis = 1633046400000L; // 2021-10-01 00:00:00 GMT
        dateOnlyCalendar.setTimeInMillis(timeInMillis);
        assertEquals(1633046400000L, dateOnlyCalendar.getTimeInMillis());
    }
    
    @Test
    public void testGetInstanceWithGMTTimeZone() {
        TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");
        DateOnlyCalendar gmtCalendar = DateOnlyCalendar.getInstance(gmtTimeZone);
        assertEquals(gmtTimeZone, gmtCalendar.getTimeZone());
    }
    
    @Test
    public void testGetInstanceWithCustomTimeZone() {
        TimeZone customTimeZone = TimeZone.getTimeZone("PST");
        DateOnlyCalendar customCalendar = DateOnlyCalendar.getInstance(customTimeZone);
        assertEquals(customTimeZone, customCalendar.getTimeZone());
    }
    
    @Test
    public void testSetTimeInMillisAdjustForTimeZone() {
        TimeZone timeZone = TimeZone.getTimeZone("PST");
        DateOnlyCalendar customCalendar = DateOnlyCalendar.getInstance(timeZone);
        long timeInMillis = 1633046400000L; // 2021-10-01 00:00:00 GMT
        customCalendar.setTimeInMillis(timeInMillis);
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTimeInMillis(timeInMillis);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.AM_PM, Calendar.AM);
        assertEquals(cal.getTimeInMillis(), customCalendar.getTimeInMillis());
    }
}