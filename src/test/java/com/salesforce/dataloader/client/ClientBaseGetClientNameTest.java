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

import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.controller.Controller;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    // --- DAO type suffix (W-22717496) ---

    @Test
    public void batch_partner_databaseRead() {
        assertEquals("DataLoaderPartnerBatch/65.0.0/DBR",
                ClientBase.getClientName(false, false, true, false, VERSION, "databaseRead"));
    }

    @Test
    public void batch_partner_databaseWrite() {
        assertEquals("DataLoaderPartnerBatch/65.0.0/DBW",
                ClientBase.getClientName(false, false, true, false, VERSION, "databaseWrite"));
    }

    @Test
    public void batch_bulk_databaseRead() {
        assertEquals("DataLoaderBulkBatch/65.0.0/DBR",
                ClientBase.getClientName(true, false, true, false, VERSION, "databaseRead"));
    }

    @Test
    public void batch_bulkv2_databaseWrite() {
        assertEquals("DataLoaderBulkv2Batch/65.0.0/DBW",
                ClientBase.getClientName(false, true, true, false, VERSION, "databaseWrite"));
    }

    @Test
    public void batch_csvRead_unchanged() {
        assertEquals("DataLoaderPartnerBatch/65.0.0",
                ClientBase.getClientName(false, false, true, false, VERSION, "csvRead"));
    }

    @Test
    public void batch_csvWrite_unchanged() {
        assertEquals("DataLoaderPartnerBatch/65.0.0",
                ClientBase.getClientName(false, false, true, false, VERSION, "csvWrite"));
    }

    @Test
    public void batch_databaseRead_caseInsensitive() {
        assertEquals("DataLoaderPartnerBatch/65.0.0/DBR",
                ClientBase.getClientName(false, false, true, false, VERSION, "DATABASEREAD"));
    }

    @Test
    public void ui_databaseRead_noSuffix() {
        assertEquals("DataLoaderPartnerUI/65.0.0",
                ClientBase.getClientName(false, false, false, false, VERSION, "databaseRead"));
    }

    @Test
    public void eca_batch_databaseWrite() {
        assertEquals("DataLoaderBulk/65.0.0/DBW",
                ClientBase.getClientName(true, false, true, true, VERSION, "databaseWrite"));
    }

    // W-19625612 regression guard: ECA + UI + database* must NOT acquire a suffix.
    // PR 16 requires ECA paths to emit a neutral name that doesn't match any
    // blocked legacy Connected App; appending /DBR or /DBW to a UI-mode header
    // would break that contract if the suffix logic ever drifted away from
    // batchMode-gating.
    @Test
    public void eca_ui_databaseRead_noSuffix() {
        assertEquals("DataLoaderBulk/65.0.0",
                ClientBase.getClientName(true, false, false, true, VERSION, "databaseRead"));
    }

    @Test
    public void eca_ui_databaseWrite_noSuffix() {
        assertEquals("DataLoaderPartner/65.0.0",
                ClientBase.getClientName(false, false, false, true, VERSION, "databaseWrite"));
    }

    @Test
    public void dao_type_null_unchanged() {
        assertEquals("DataLoaderPartnerBatch/65.0.0",
                ClientBase.getClientName(false, false, true, false, VERSION, (String) null));
    }

    // --- AppConfig wiring (W-22717496): the single-arg overload must read PROP_DAO_TYPE ---

    private AppConfig mockCfg(boolean bulk, boolean bulkV2, boolean batch, boolean eca, String daoType) {
        AppConfig cfg = mock(AppConfig.class);
        when(cfg.isBulkAPIEnabled()).thenReturn(bulk);
        when(cfg.isBulkV2APIEnabled()).thenReturn(bulkV2);
        when(cfg.isBatchMode()).thenReturn(batch);
        when(cfg.isExternalClientAppConfigured()).thenReturn(eca);
        when(cfg.getString(AppConfig.PROP_DAO_TYPE)).thenReturn(daoType);
        return cfg;
    }

    @Test
    public void appConfig_batch_databaseRead_emitsDBR() {
        String name = ClientBase.getClientName(mockCfg(true, false, true, false, "databaseRead"));
        assertTrue("expected /DBR suffix, got: " + name, name.endsWith("/DBR"));
        assertTrue(name.startsWith("DataLoaderBulkBatch/"));
    }

    @Test
    public void appConfig_batch_databaseWrite_emitsDBW() {
        String name = ClientBase.getClientName(mockCfg(false, true, true, false, "databaseWrite"));
        assertTrue("expected /DBW suffix, got: " + name, name.endsWith("/DBW"));
        assertTrue(name.startsWith("DataLoaderBulkv2Batch/"));
    }

    @Test
    public void appConfig_batch_csvRead_noSuffix() {
        String name = ClientBase.getClientName(mockCfg(false, false, true, false, "csvRead"));
        assertFalse("CSV must not get DAO suffix, got: " + name, name.endsWith("/DBR"));
        assertFalse(name.endsWith("/DBW"));
        assertEquals("DataLoaderPartnerBatch/" + Controller.APP_VERSION, name);
    }

    @Test
    public void appConfig_ui_databaseRead_noSuffix() {
        String name = ClientBase.getClientName(mockCfg(false, false, false, false, "databaseRead"));
        assertFalse("UI mode must not get DAO suffix, got: " + name, name.endsWith("/DBR"));
        assertEquals("DataLoaderPartnerUI/" + Controller.APP_VERSION, name);
    }

    @Test
    public void appConfig_batch_nullDaoType_noSuffix() {
        String name = ClientBase.getClientName(mockCfg(false, false, true, false, null));
        assertFalse(name.endsWith("/DBR"));
        assertFalse(name.endsWith("/DBW"));
    }
}
