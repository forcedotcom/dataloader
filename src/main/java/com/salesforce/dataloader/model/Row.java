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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Basically a Row is a set of column names and column values which can come from a CSV file, a database
 * or any other data source.
 *
 * A row is an abstraction that replaces the use of Maps in favor of a more object oriented approach.
 * For now it implements Map to make initial refactoring easier but should move towards more specific
 * methods and probably stop implementing Map interface. All Row behavior should be moved into this
 * class and not be spread in multiple class.
 */
public class Row implements Map<String, Object>, RowInterface {
    private final Map<String, Object> internalMap;
    private final Map<String, String> keyMap = new HashMap<String, String>();

    public Row() {
        this.internalMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    @Override
    public int size() {
        return internalMap.size();
    }

    @Override
    public boolean isEmpty() {
        return internalMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        String realKey = this.keyMap.get(((String)key).toLowerCase());
        if (realKey == null) {
            return false;
        }
        return internalMap.containsKey(realKey);
    }

    @Override
    public boolean containsValue(Object value) {
        return internalMap.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        String realKey = this.keyMap.get(((String)key).toLowerCase());
        if (realKey == null) {
            return null;
        }
        return internalMap.get(realKey);
    }

    @Override
    public Object put(String key, Object value) {
        this.keyMap.put(key.toLowerCase(), key);
        return internalMap.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        this.keyMap.remove(((String)key).toLowerCase());
        return internalMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        for (String key : m.keySet()) {
            this.keyMap.put(key.toLowerCase(), key);
        }
        internalMap.putAll(m);
    }

    @Override
    public void clear() {
        this.keyMap.clear();
        internalMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return internalMap.keySet();
    }

    @Override
    public Collection<Object> values() {
        return internalMap.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return internalMap.entrySet();
    }

    @Override
    public String toString() {
        return "Row{" +
                " size=" + internalMap.size() +
                " columns=" + internalMap +
                '}';
    }
    
    public TableRow convertToTableRow(TableHeader header) {
        TableRow trow = new TableRow(header);
        for (String headerColName : header.getColumns()) {
            trow.put(headerColName, this.get(headerColName));
        }
        return trow;
    }

    @Override
    public Object get(String key) {
        return get((Object)key);
    }

    @Override
    public List<String> getColumnNames() {
        Set<String> fieldNameSet = this.keySet();
        return new ArrayList<String>(fieldNameSet);
    }
}
