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

package com.salesforce.dataloader.config;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * All non-UI messages go here. UI Labels go in Labels.
 * 
 * @author Lexi Viripaeff
 * @since 6.0
 */
public class Messages {
    private static final String BUNDLE_NAME = "messages";//$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private Messages() { }

    public static String getString(String key, Object... args) {
        return getString(key, false, args);
    }

    private static String getString(String key, boolean nullOk, Object... args) {
        assert key.contains(".");
        try {
            if (args == null) return RESOURCE_BUNDLE.getString(key);
            for (int i = 0; i < args.length; i++)
                if (args[i] == null) args[i] = "";
            return MessageFormat.format(RESOURCE_BUNDLE.getString(key), args);
        } catch (MissingResourceException e) {
            return nullOk ? null : '!' + key + '!';
        }
    }

    public static String getMessage(String section, String key, boolean nullOk, Object... args) {
        return getString(section + "." + key, nullOk, args);
    }

    public static String getMessage(String section, String key, Object... args) {
        return getMessage(section, key, false, args);
    }

    public static String getMessage(Class<?> cls, String key, Object... args) {
        return getMessage(cls, key, false, args);
    }

    public static String getMessage(Class<?> cls, String key, boolean nullOk, Object... args) {
        for (Class<?> currentClass = cls; currentClass != null; currentClass = currentClass.getSuperclass()) {
            final String msg = getMessage(currentClass.getSimpleName(), key, true, args);
            if (msg != null) return msg;
        }
        return getMessage(cls.getSimpleName(), key, nullOk, args);
    }

    public static String getFormattedString(String key, Object arg) {
        return getString(key, arg);
    }

    public static String getFormattedString(String key, Object[] args) {
        return getString(key, args);
    }

}