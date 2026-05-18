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
package com.salesforce.dataloader.client;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Unit tests for {@link ClientBase#getClientName}.
 * Verifies that External Client App configurations produce neutral client names
 * (without UI/Batch suffix) while legacy configurations retain the original format.
 */
public class ClientBaseGetClientNameTest {

    private static final String VERSION = "65.0.0";

    // --- Legacy (no ECA) ---

    @Test
    public void legacy_partnerUI() {
        assertEquals("DataLoaderPartnerUI/65.0.0",
                ClientBase.getClientName(false, false, false, false, VERSION));
    }

    @Test
    public void legacy_partnerBatch() {
        assertEquals("DataLoaderPartnerBatch/65.0.0",
                ClientBase.getClientName(false, false, true, false, VERSION));
    }

    @Test
    public void legacy_bulkUI() {
        assertEquals("DataLoaderBulkUI/65.0.0",
                ClientBase.getClientName(true, false, false, false, VERSION));
    }

    @Test
    public void legacy_bulkv2Batch() {
        assertEquals("DataLoaderBulkv2Batch/65.0.0",
                ClientBase.getClientName(false, true, true, false, VERSION));
    }

    // --- ECA configured ---

    @Test
    public void eca_partner() {
        String name = ClientBase.getClientName(false, false, false, true, VERSION);
        assertEquals("DataLoaderPartner/65.0.0", name);
        assertFalse(name.contains("UI"));
        assertFalse(name.contains("Batch"));
    }

    @Test
    public void eca_bulk() {
        String name = ClientBase.getClientName(true, false, false, true, VERSION);
        assertEquals("DataLoaderBulk/65.0.0", name);
        assertFalse(name.contains("UI"));
        assertFalse(name.contains("Batch"));
    }

    @Test
    public void eca_bulkv2() {
        String name = ClientBase.getClientName(false, true, false, true, VERSION);
        assertEquals("DataLoaderBulkv2/65.0.0", name);
        assertFalse(name.contains("UI"));
        assertFalse(name.contains("Batch"));
    }

    @Test
    public void eca_ignoresBatchMode() {
        String name = ClientBase.getClientName(false, false, true, true, VERSION);
        assertEquals("DataLoaderPartner/65.0.0", name);
        assertFalse("ECA path should not include Batch suffix", name.contains("Batch"));
    }
}
