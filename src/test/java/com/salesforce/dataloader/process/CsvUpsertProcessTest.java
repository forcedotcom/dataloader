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

package com.salesforce.dataloader.process;

import com.salesforce.dataloader.TestSetting;
import com.salesforce.dataloader.TestVariant;
import com.salesforce.dataloader.config.AppConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Describe your class here.
 * 
 * @author Colin Jarvis, Aleksandr Shulman
 * @since 22.0
 */
@RunWith(Parameterized.class)
public class CsvUpsertProcessTest extends ProcessTestBase {

    public CsvUpsertProcessTest(Map<String, String> config) {
        super(config);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> getTestParameters() {
        return Arrays.asList(
                TestVariant.defaultSettings(),
                TestVariant.forSettings(TestSetting.BULK_API_ENABLED),
                TestVariant.forSettings(TestSetting.BULK_API_ENABLED, TestSetting.BULK_API_CACHE_DAO_UPLOAD_ENABLED),
                TestVariant.forSettings(TestSetting.BULK_API_ENABLED, TestSetting.BULK_V2_API_ENABLED)
                );
    }

    /**
     * Verify that a row offset will produce the correct effects and success file for a small set of rows (<5).
     */
    @Test
    public void testUpsertWithRowOffset() throws Exception {
        // define properties
        Map<String, String> argMap = getUpdateTestConfig(true, DEFAULT_ACCOUNT_EXT_ID_FIELD, 10);

        // start at 3, not 0!!
        argMap.put(AppConfig.LOAD_ROW_TO_START_AT, "3");

        // perform the upsert
        runUpsertProcess(argMap, 0, 7);
    }

}