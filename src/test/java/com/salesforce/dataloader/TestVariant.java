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
package com.salesforce.dataloader;

import org.junit.runners.Parameterized;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A test variant consists of one or more {@link TestSetting}s and it's meant to be used as
 * a test parameter in a JUnit 4 {@link Parameterized.Parameters} annotated method.
 *
 * @author Federico Recio
 */
public class TestVariant {

    private static final Object[] EMPTY_SETTINGS = {Collections.emptyMap()};

    private TestVariant() {
    }

    public static Object[] forSettings(TestSetting... settings) {
        Map<String, String> config = new LinkedHashMap<String, String>();
        for (TestSetting setting : settings) {
            String parameter = setting.getParameter();
            if (config.containsKey(parameter)) {
                throw new IllegalArgumentException("Duplicate parameter: " + parameter);
            }
            config.put(parameter, setting.getValue().toString());
        }
        return new Object[]{Collections.unmodifiableMap(config)};
    }

    public static Object[] defaultSettings() {
        return EMPTY_SETTINGS;
    }
}
