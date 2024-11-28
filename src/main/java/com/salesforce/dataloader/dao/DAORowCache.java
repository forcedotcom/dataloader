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
package com.salesforce.dataloader.dao;

import java.util.ArrayList;

import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.model.TableRow;

public class DAORowCache {
    private ArrayList<TableRow> rowList = new ArrayList<TableRow>();
    private int currentRowIndex = 0;
    private int cachedRows = 0;

    public DAORowCache() {
    }
    
    public void resetCurrentRowIndex() {
        currentRowIndex = 0;
    }
    
    public TableRow getCurrentRow() {
        AppConfig appConfig = AppConfig.getCurrentConfig();
        if (currentRowIndex >= cachedRows
            || !appConfig.getBoolean(AppConfig.PROP_PROCESS_BULK_CACHE_DATA_FROM_DAO)) {
            return null;
        }
        return rowList.get(currentRowIndex++);
    }
    
    public void addRow(TableRow row) {
        // add a row to the cache only if it is not cached already
        if (currentRowIndex >= cachedRows) {
            rowList.add(row);
            cachedRows++;
        }
        currentRowIndex++;
    }
    
    public int getCachedRows() {
        return cachedRows;
    }
}