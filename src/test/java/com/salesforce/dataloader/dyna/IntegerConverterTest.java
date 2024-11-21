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

import org.junit.Ignore;
import org.junit.Test;

import com.salesforce.dataloader.ConfigTestBase;
import java.util.Locale;
import static org.junit.Assert.assertEquals;

public class IntegerConverterTest extends ConfigTestBase {

    @Test
    public void testIntegerConversionInUS() throws Exception {
        Locale prevSysLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);
        IntegerConverter converter = new IntegerConverter();
        assertEquals(10000, converter.convert(null, "10,000"));
        assertEquals(10000, converter.convert(null, "10000"));
        Locale.setDefault(prevSysLocale);
    }
    
    @Test
    public void testIntegerConversionInFrench() throws Exception {
        Locale prevSysLocale = Locale.getDefault();
        Locale.setDefault(Locale.FRANCE);
        IntegerConverter converter = new IntegerConverter();
        // as specified at https://stackoverflow.com/questions/9621322/numberformat-localization-issues
        String numStr = "10 000";
        numStr = numStr.replace(' ', '\u00a0');
       // assertEquals(10000, converter.convert(null, numStr));
        assertEquals(10, converter.convert(null, "10,000"));
        assertEquals(10000, converter.convert(null, "10000"));
        Locale.setDefault(prevSysLocale);
    }
    
    @Test
    public void testIntegerConversionInItalian() throws Exception {
        Locale prevSysLocale = Locale.getDefault();
        Locale.setDefault(Locale.ITALY);
        IntegerConverter converter = new IntegerConverter();
        assertEquals(10000, converter.convert(null, "10.000"));
        assertEquals(10000, converter.convert(null, "10000"));
        Locale.setDefault(prevSysLocale);
    }}