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
package com.salesforce.dataloader.mapping;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.util.LinkedCaseInsensitiveMap;

public class CaseInsensitiveSet {
    private final Map<String, String> originalMap = new LinkedCaseInsensitiveMap<String>();

    public CaseInsensitiveSet(){
    }

    public CaseInsensitiveSet(Set<String> values){
        this();
        for(String value: values){
            add(value);
        }
    }

    public void add(String value) {
        originalMap.put(value, value);
    }

    public boolean containsKey(String key) {
        return key != null ? originalMap.containsKey(key): false;
    }

    public String getOriginal(String key){
        return containsKey(key) ? (String) originalMap.get(key): key;
    }

    public boolean isEmpty(){
        return originalMap.isEmpty();
    }

    public Set<String> getOriginalValues() {
        return new LinkedHashSet<String>(originalMap.values());
    }
}