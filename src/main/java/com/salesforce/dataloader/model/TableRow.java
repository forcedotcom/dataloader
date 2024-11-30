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
package com.salesforce.dataloader.model;

import java.util.ArrayList;
import java.util.Arrays;

public class TableRow {
    private TableHeader header;
    private Object[] cellValues;

    public TableRow(TableHeader header) {
        this.header = header;
        cellValues = new Object[header.getColumns().size()];
    }

    public TableRow(TableRow rowToCopy) {
        this.header = rowToCopy.getHeader();
        cellValues = Arrays.copyOf(rowToCopy.cellValues, rowToCopy.cellValues.length);
    }
    
    public Object get(String key) {
        Integer colPos = this.header.getColumnPosition(key);
        if (colPos == null) {
            return null;
        }
        return cellValues[colPos];
    }

    public Object put(String key, Object value) {
        Integer colPos = this.header.getColumnPosition(key);
        if (colPos == null) {
            return null;
        }
        return this.cellValues[colPos] = value;
    }
    
    public Row convertToRow() {
        Row row = new Row();
        for (String colName : this.header.getColumns()) {
            row.put(colName, cellValues[this.header.getColumnPosition(colName)]);
        }
        return row;
    }
    
    public TableHeader getHeader() {
        return this.header;
    }
    
    public static TableRow emptyRow() {
        return new TableRow(new TableHeader(new ArrayList<String>()));
    }
    
    public static TableRow singleEntryImmutableRow(String key, Object value) {
        ArrayList<String> headers = new ArrayList<String>();
        headers.add(key);
        TableHeader tableHeader = new TableHeader(headers);
        TableRow row = new TableRow(tableHeader);
        row.put(key, value);
        return row;
    }
    
    public int getNonEmptyCellsCount() {
        int numNonEmptyCells = 0;
        for (int i = 0; i < this.cellValues.length; i++) {
            if (cellValues[i] != null) {
                numNonEmptyCells++;
            }
        }
        return numNonEmptyCells;
    }
}