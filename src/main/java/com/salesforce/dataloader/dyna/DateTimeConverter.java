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

import java.text.*;
import java.util.*;

import com.salesforce.dataloader.model.NACalendarValue;
import com.salesforce.dataloader.model.NATextValue;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class DateTimeConverter implements Converter {

    static final TimeZone GMT_TZ = TimeZone.getTimeZone("GMT");
    static final List<String> supportedEuropeanPatterns = getSupportedPatterns(true);
    static final List<String> supportedRegularPatterns = getSupportedPatterns(false);

    static Logger logger = LogManager.getLogger(DateTimeConverter.class);
    /**
     * The default value specified to our Constructor, if any.
     */
    final Object defaultValue;

    /**
     * Should we return the default value on conversion errors?
     */
    final boolean useDefault;
    final boolean useEuroDates;
    final TimeZone timeZone;



    public DateTimeConverter(TimeZone tz) {
        this(tz, null, false, false);

    }

    public DateTimeConverter(TimeZone tz, boolean useEuroDateFormat) {
        this(tz, null, useEuroDateFormat, false);
    }

    public DateTimeConverter(TimeZone tz, Object defaultValue, boolean useEuroDateFormat) {
        this(tz, defaultValue, useEuroDateFormat, true);
    }

    private DateTimeConverter(TimeZone tz, Object defaultValue, boolean useEuroDateFormat, boolean useDefault) {
        this.timeZone = tz;
        this.defaultValue = defaultValue;
        this.useDefault = useDefault;
        this.useEuroDates = useEuroDateFormat;
    }

    public DateTimeConverter(TimeZone tz, Object defaultValue) {
        this(tz, defaultValue, false, true);
    }

    Calendar parseDate(TimeZone tz, String dateString, String pattern) {
        final DateFormat df = new SimpleDateFormat(pattern);
        df.setTimeZone(tz);
        return parseDate(dateString, df);
    }

    private Calendar parseDate(String dateString, DateFormat fmt) {
        final ParsePosition pos = new ParsePosition(0);
        fmt.setLenient(false);
        final Date date = fmt.parse(dateString, pos);
        // we only want to use the date if parsing succeeded and used the entire string
        if (date != null && pos.getIndex() == dateString.length()) {
            Calendar cal = getCalendar(fmt.getTimeZone());
            cal.setTimeInMillis(date.getTime());
            return cal;
        }
        return null;
    }

    /**
     * Attempts to parse a date string using the given formatting patterns
     * 
     * @param dateString
     *            The date string to parse
     * @param patterns
     *            Patterns to try. These will be used in the constructor for SimpleDateFormat
     * @return A Calendar object representing the given date string
     */
    private Calendar tryParse(TimeZone tz, String dateString, String... patterns) {
        if (patterns == null) return null;
        for (String pattern : patterns) {
            Calendar cal = parseDate(tz, dateString, pattern);
            if (cal != null) return cal;
        }
        return null;
    }


    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object convert(Class type, Object value) {
        if (value == null) {
            return null;
        }

        if(value instanceof NATextValue) {
            return getNAValueCalendar();
        }
        
        Calendar cal = getCalendar(this.timeZone);

        if (value instanceof Date) {
            cal.setTimeInMillis(((Date)value).getTime());
            return cal;
        }

        if (value instanceof Calendar) { return value; }

        String dateString = value.toString().trim();
        int len = dateString.length();

        if (len == 0) return null;

        String gmtDateString = null;
        if ("z".equalsIgnoreCase(dateString.substring(len - 1)))
            gmtDateString = dateString.substring(0, len - 1);

        for (String basePattern : useEuroDates ? supportedEuropeanPatterns : supportedRegularPatterns) {
            if (gmtDateString != null)
                cal = tryParse(GMT_TZ, gmtDateString, basePattern);
            else
                cal = tryParse(this.timeZone, dateString, basePattern, basePattern + "'Z'Z", basePattern + "'z'Z",
                        basePattern + "z");
            if (cal != null) return cal;
        }

        // FIXME -- BUG: this format is picked up as a mistake instead of MM-dd-yyyy or dd-MM-yyyy
        cal = parseDate(this.timeZone, dateString, "yyyy-MM-dd");
        if (cal != null) return cal;

        if (useEuroDates) {
            cal = tryParse(this.timeZone, dateString, "dd/MM/yyyy HH:mm:ss", "dd/MM/yyyy");

            // FIXME -- Warning: this never gets picked up because of yyyy-MM-dd
            /*
             * Calendar cal = parseDate("dd-MM-yyyy", dateString); if (cal != null) return cal;
             */
        } else {
            cal = tryParse(this.timeZone, dateString, "MM/dd/yyyy HH:mm:ss", "MM/dd/yyyy");

            //FIXME -- Warning: this never gets picked up because of yyyy-MM-dd
            /*
             * Calendar cal = parseDate("MM-dd-yyyy", dateString); if (cal != null) return cal;
             */
        }

        if (cal != null) return cal;

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT);
        df.setTimeZone(this.timeZone);
        cal = parseDate(dateString, df);
        if (cal != null) return cal;

        df = DateFormat.getDateInstance(DateFormat.SHORT);
        df.setTimeZone(this.timeZone);
        cal = parseDate(dateString, df);
        if (cal != null) return cal;

        if (useDefault) {
            return defaultValue;
        } else {
            throw new ConversionException("Failed to parse date: " + value);
        }
    }
    
    Calendar getCalendar(TimeZone timezone) {
        return Calendar.getInstance(timezone);
    }
    
    Calendar getNAValueCalendar() {
        return NACalendarValue.getInstance();
    }

    /* Helper function to produce all the patterns that DL supports */
    static List<String> getSupportedPatterns(boolean europeanDates) {

        List<String> basePatterns = new ArrayList<String>();

        // Extended patterns means using the - delimiter in the date

        List<String> extendedPatterns = new ArrayList<String>();
        extendedPatterns.add("yyyy-MM-dd'T'HH:mm:ss.SSS");
        extendedPatterns.add("yyyy-MM-dd'T'HH:mm:ss");
        extendedPatterns.add("yyyy-MM-dd'T'HH:mm");
        extendedPatterns.add("yyyy-MM-dd'T'HH");
        extendedPatterns.add("yyyy-MM-dd'T'"); //?

        //As per ISO 8601 5.2.1.1, when only the days are omitted, a - is necessary between year and month
        List<String> extendedPatternsDateOnly = new ArrayList<String>();
        extendedPatternsDateOnly.add("yyyy-MM");
        extendedPatternsDateOnly.add("yyyyMMdd");
        extendedPatternsDateOnly.add("yyyy");

        // Using a space instead of 'T' to separate date and time
        List<String> extendedPatternsWithoutT = new ArrayList<String>();
        extendedPatternsWithoutT.add("yyyy-MM-dd HH:mm:ss.SSS");
        extendedPatternsWithoutT.add("yyyy-MM-dd HH:mm:ss");
        extendedPatternsWithoutT.add("yyyy-MM-dd HH:mm");
        extendedPatternsWithoutT.add("yyyy-MM-dd HH");

        // Not using anything to deliminate the date elements from each
        // other. Matched through known lengths of components.
        List<String> basicPatterns = new ArrayList<String>();
        basicPatterns.add("yyyyMMdd'T'HH:mm:ss.SSS");
        basicPatterns.add("yyyyMMdd'T'HH:mm:ss");
        basicPatterns.add("yyyyMMdd'T'HH:mm");
        basicPatterns.add("yyyyMMdd'T'HH");
        basicPatterns.add("yyyyMMdd'T'"); //?

        // Using a space instead of 'T' to separate date and time
        List<String> basicPatternsWithoutT = new ArrayList<String>();
        basicPatternsWithoutT.add("yyyyMMdd HH:mm:ss.SSS");
        basicPatternsWithoutT.add("yyyyMMdd HH:mm:ss");
        basicPatternsWithoutT.add("yyyyMMdd HH:mm");
        basicPatternsWithoutT.add("yyyyMMdd HH");

        //as per the iso 8601 spec
        List<String> fullBasicFormats = new ArrayList<String>();
        fullBasicFormats.add("yyyyMMdd'T'HHmmss");
        fullBasicFormats.add("yyyyMMdd'T'HHmm");
        fullBasicFormats.add("yyyyMMdd'T'HH");


        List<String> fullBasicFormatsWithoutT = new ArrayList<String>();
        fullBasicFormatsWithoutT.add("yyyyMMdd HHmmss");
        fullBasicFormatsWithoutT.add("yyyyMMdd HHmm");
        fullBasicFormatsWithoutT.add("yyyyMMdd HH");


        String baseDate = europeanDates ? "dd/MM/yyyy" : "MM/dd/yyyy";

        // Using a space instead of 'T' to separate date and time
        List<String> slashPatternsWithoutT = new ArrayList<String>();
        extendedPatternsWithoutT.add(baseDate +" HH:mm:ss.SSS");
        extendedPatternsWithoutT.add(baseDate +" HH:mm:ss");
        extendedPatternsWithoutT.add(baseDate +" HH:mm");
        extendedPatternsWithoutT.add(baseDate +" HH");
        extendedPatternsWithoutT.add(baseDate +" HHZ");

        List<String> slashPatternsWithT = new ArrayList<String>();
        extendedPatternsWithoutT.add(baseDate +  "'T'HH:mm:ss.SSS");
        extendedPatternsWithoutT.add(baseDate +  "'T'HH:mm:ss");
        extendedPatternsWithoutT.add(baseDate +  "'T'HH:mm");
        extendedPatternsWithoutT.add(baseDate +  "'T'HH");

        //order is important here because if it matches against the wrong format first, it will
        //misinterpret the time

        basePatterns.addAll(fullBasicFormatsWithoutT);
        basePatterns.addAll(fullBasicFormats);
        basePatterns.addAll(basicPatterns);
        basePatterns.addAll(basicPatternsWithoutT);
        basePatterns.addAll(extendedPatternsDateOnly);
        basePatterns.addAll(extendedPatterns);
        basePatterns.addAll(extendedPatternsWithoutT);
        basePatterns.addAll(slashPatternsWithoutT);
        basePatterns.addAll(slashPatternsWithT);

        List<String> timeZones = new ArrayList<>();
        basePatterns.forEach(p -> timeZones.add(p + "Z"));
        basePatterns.addAll(timeZones);

        return basePatterns;
    }
}