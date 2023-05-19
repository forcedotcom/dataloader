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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DateOnlyCalendar extends GregorianCalendar {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    static final TimeZone GMT_TZ = TimeZone.getTimeZone("GMT");
    static final Logger logger;
    
    static {
        logger = LogManager.getLogger(DateOnlyCalendar.class);
    }

    public DateOnlyCalendar() {
        super();
    }

    private DateOnlyCalendar(TimeZone tz) {
        // Use the timezone param to update the date by 1 in setDate()
        super(tz);
    }

    public void setTimeInMillis(long specifiedTimeInMilliSeconds) {
        TimeZone myTimeZone = super.getTimeZone();
        
        if (myTimeZone == null) {
            logger.info("timezone is null");
        } else {
            logger.info("Timezone is " + myTimeZone.getDisplayName());
        }
        Calendar cal = Calendar.getInstance(myTimeZone);
        cal.setTimeInMillis(specifiedTimeInMilliSeconds);

        if (!AppUtil.isUseGMTForDateFieldValue() && myTimeZone != null) {
            // Set hour, minute, second, and millisec to 0 (12:00AM) as it is date-only value
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.AM_PM, Calendar.AM);
            
            int timeZoneDifference = myTimeZone.getOffset(cal.getTimeInMillis());
            if (timeZoneDifference > 0) {
                // timezone is ahead of GMT, compensate for it as server-side thinks it is in GMT.
                cal.setTimeInMillis(cal.getTimeInMillis() + timeZoneDifference);
            }
        }
        super.setTimeInMillis(cal.getTimeInMillis());
    }

    public static DateOnlyCalendar getInstance(TimeZone timeZone) {
        if (AppUtil.isUseGMTForDateFieldValue()) {
            timeZone = GMT_TZ;
        } 
        return new DateOnlyCalendar(timeZone);
    }
}