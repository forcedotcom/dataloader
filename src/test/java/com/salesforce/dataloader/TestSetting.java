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
package com.salesforce.dataloader;

import com.salesforce.dataloader.config.Config;

/**
 * A test setting is used to modify the behavior of dataloader during test execution.
 * Many test settings can be used together to configure a {@link TestVariant}.
 *
 * @author Federico Recio
 */
public enum TestSetting {

    BULK_API_ENABLED(Config.BULK_API_ENABLED, Boolean.TRUE),
    BULK_API_DISABLED(Config.BULK_API_ENABLED, Boolean.FALSE),
    BULK_API_ZIP_CONTENT_ENABLED(Config.BULK_API_ZIP_CONTENT, Boolean.TRUE),
    BULK_API_SERIAL_MODE_ENABLED(Config.BULK_API_SERIAL_MODE, Boolean.TRUE),
    BULK_API_CACHE_DAO_UPLOAD_ENABLED(Config.PROCESS_BULK_CACHE_DATA_FROM_DAO, Boolean.TRUE),
    BULK_V2_API_ENABLED(Config.BULKV2_API_ENABLED, Boolean.TRUE),
    BULK_V2_API_DISABLED(Config.BULKV2_API_ENABLED, Boolean.FALSE),
    COMPOSITE_REST_API_DISABLED(Config.REST_API_ENABLED, Boolean.FALSE),
    COMPOSITE_REST_API_ENABLED(Config.REST_API_ENABLED, Boolean.TRUE),
    WRITE_UTF8_ENABLED(Config.WRITE_UTF8, Boolean.TRUE),
    WRITE_UTF8_DISABLED(Config.WRITE_UTF8, Boolean.FALSE),
    READ_UTF8_ENABLED(Config.READ_UTF8, Boolean.TRUE),
    READ_UTF8_DISABLED(Config.READ_UTF8, Boolean.FALSE),
    COMPRESSION_ENABLED(Config.NO_COMPRESSION, Boolean.FALSE),
    COMPRESSION_DISABLED(Config.NO_COMPRESSION, Boolean.TRUE)
    ;

    private final String parameter;
    private final Object value;

    TestSetting(String parameter, Object value) {
        this.parameter = parameter;
        this.value = value;
    }

    public String getParameter() {
        return parameter;
    }

    public Object getValue() {
        return value;
    }
}