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

package com.salesforce.dataloader.config;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A simple replacement for properties that will allow us to maintain order on a proprties object
 */
public class LinkedProperties extends Properties {
    private final LinkedHashMap<Object, Object> sorted =new LinkedHashMap<>();

    @Override
    public String getProperty(String key) {
        Object oval = sorted.get(key);
        String sval = (oval instanceof String) ? (String)oval : null;
        return ((sval == null) && (defaults != null)) ? defaults.getProperty(key) : sval;
    }

    @Override
    public synchronized int size() {
        return sorted.size();
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        return new Enumeration<Object>(){
            Iterator<Object> iterator = sorted.keySet().iterator();

            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public Object nextElement() {
                return iterator.next();
            }
        };
    }

    @Override
    public synchronized Enumeration<Object> elements() {
        return new Enumeration<Object>(){
            Iterator<Object> iterator = sorted.values().iterator();

            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public Object nextElement() {
                return iterator.next();
            }
        };
    }

    @Override
    public synchronized boolean isEmpty() {
        return sorted.isEmpty();
    }

    @Override
    public synchronized boolean contains(Object value) {
        return sorted.containsValue(value);
    }

    @Override
    public boolean containsValue(Object value) {
        return contains(value);
    }

    @Override
    public synchronized boolean containsKey(Object key) {
        return sorted.containsKey(key);
    }

    @Override
    public synchronized Object get(Object key) {
        return sorted.get(key);
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        return sorted.put(key, value);
    }

    @Override
    public synchronized Object remove(Object key) {
        return sorted.remove(key);
    }

    @Override
    public synchronized void putAll(Map<?, ?> t) {
        for (Map.Entry<?, ?> e : t.entrySet())
            sorted.put(e.getKey(), e.getValue());
    }

    @Override
    public synchronized void clear() {
        sorted.clear();
    }

    @Override
    public synchronized Object clone() {
        LinkedProperties properties = new LinkedProperties();
        properties.putAll(sorted);

        return properties;
    }

    @Override
    public Set<Object> keySet() {
        return sorted.keySet();
    }

    @Override
    public Set<Map.Entry<Object,Object>> entrySet() {
        return sorted.entrySet();
    }

    @Override
    public Collection<Object> values() {
        return sorted.values();
    }

    @Override
    public synchronized Object getOrDefault(Object key, Object defaultValue) {
        return sorted.getOrDefault(key, defaultValue);
    }
    @Override
    public synchronized void forEach(BiConsumer<? super Object, ? super Object> action) {
        sorted.forEach(action);
    }

    @Override
    public synchronized void replaceAll(BiFunction<? super Object, ? super Object, ?> function) {
        sorted.replaceAll(function);
    }

    @Override
    public synchronized Object putIfAbsent(Object key, Object value) {
        return sorted.putIfAbsent(key, value);
    }

    @Override
    public synchronized boolean remove(Object key, Object value) {
        return sorted.remove(key, value);
    }

    @Override
    public synchronized boolean replace(Object key, Object oldValue, Object newValue) {
        return sorted.replace(key, oldValue, newValue);
    }

    @Override
    public synchronized Object replace(Object key, Object value) {
        return sorted.replace(key, value);
    }

    @Override
    public synchronized Object computeIfAbsent(Object key, Function<? super Object, ?> mappingFunction) {
        return sorted.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public synchronized Object computeIfPresent(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return sorted.computeIfPresent(key, remappingFunction);
    }

    @Override
    public synchronized Object compute(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return sorted.compute(key, remappingFunction);
    }

    @Override
    public synchronized Object merge(Object key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return sorted.merge(key, value, remappingFunction);
    }
}
