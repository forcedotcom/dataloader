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
import org.apache.commons.beanutils.Converter;

/**
 * Converts Strings to Doubles
 * 
 * @author Lexi Viripaeff
 * @since 6.0
 */
public final class DoubleConverter implements Converter {

    // ----------------------------------------------------------- Constructors

    public DoubleConverter() {

        this.defaultValue = null;
        this.useDefault = false;

    }

    public DoubleConverter(Object defaultValue) {

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
     * Convert the specified input object into an output object of the specified type.
     * 
     * @param type
     *            Data type to which this value should be converted
     * @param value
     *            The input value to be converted
     * @exception ConversionException
     *                if conversion cannot be performed successfully
     */
    @Override
    public Object convert(Class type, Object value) {
        if (value == null || String.valueOf(value).length() == 0) {
            return null;
        }
        if (value instanceof Double) {
            return (value);
        } else if (value instanceof Number) { return new Double(((Number)value).doubleValue()); }

        try {
            return (new Double(value.toString()));
        } catch (Exception e) {
            if (useDefault) {
                return (defaultValue);
            } else {
                throw new ConversionException(e);
            }
        }

    }

}