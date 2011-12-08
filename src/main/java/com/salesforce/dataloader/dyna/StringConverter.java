/*
 * Copyright (c) 2011, salesforce.com, inc.
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


import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.sql.Clob;
import java.sql.SQLException;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;

/**
 * 
 * Converts rogue Strings to XML-Friendly Strings.
 *
 * @author Lexi Viripaeff
 * @since 6.0
 */

public final class StringConverter implements Converter {


    // ----------------------------------------------------------- Constructors

    public StringConverter() {

        this.defaultValue = null;
        this.useDefault = false;

    }

    public StringConverter(Object defaultValue) {

        this.defaultValue = defaultValue;
        this.useDefault = true;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The default value specified to our Constructor, if any.
     */
    private Object defaultValue = null;


    /**
     * Should we return the default value on conversion errors?
     */
    private boolean useDefault = true;


    // --------------------------------------------------------- Public Methods


    /**
     * Convert the specified input object into an output object of the
     * specified type.
     *
     * @param type Data type to which this value should be converted
     * @param value The input value to be converted
     *
     * @exception ConversionException if conversion cannot be performed
     *  successfully
     */
    @Override
    public Object convert(Class type, Object value) {

        if (value == null || String.valueOf(value).length()==0) {
            return null;
        }
        if (value instanceof Clob) {
            final StringBuilder sb = new StringBuilder(1024);
            final CharBuffer cbuf = CharBuffer.allocate(1024);
            try {
                final Reader rdr = ((Clob) value).getCharacterStream();
                while (rdr.read(cbuf) >= 0) {
                    cbuf.rewind();
                    sb.append(cbuf);
                    cbuf.rewind();
                }
            } catch (IOException e) {
                throw new ConversionException("Error reading from clob", e);
            } catch (SQLException e) {
                throw new ConversionException("Error reading from clob", e);
            }
            value = sb.toString();
        }
        try {
            return cleanseString(value.toString());
        } catch (ClassCastException e) {
            if (useDefault) {
                return (defaultValue);
            } else {
                throw new ConversionException(e);
            }
        }

    }
    /**
     * This strips out invalid characters from being sent in XML. This will only allow characters in the range:
     * <code>((c >= 0x20) && (c <= 0xD7FF)) || ((c >= 0xE000) && (c <= 0xFFFD))</code>
     * 
     * @param value
     *            the string to cleanse
     * @return the cleansed string.
     */
    private String cleanseString(String value) {

        if (value == null) return value;



        StringBuffer buff = new StringBuffer();

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            // parallel code of XmlWriter.write()
            switch (c) {
            case '\n': // Line Feed is OK
            case '\r': // Carriage Return is OK
            case '\t': // Tab is OK
                // These characters are specifically OK, as exceptions to the general rule below:
                buff.append(c);
                break;
            default:
                if (((c >= 0x20) && (c <= 0xD7FF)) || ((c >= 0xE000) && (c <= 0xFFFD))) {
                    buff.append(c);
                }
                // For chars outside these ranges (such as control chars),
                // do nothing; it's not legal XML to print these chars,
                // even escaped
            }
        }
        return buff.toString();
    }


}
