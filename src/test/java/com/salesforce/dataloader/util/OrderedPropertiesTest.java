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
package com.salesforce.dataloader.util;

import org.junit.Before; 
import org.junit.Test;
import java.util.Map; 
import java.util.Set;
import static org.junit.Assert.*;

public class OrderedPropertiesTest {
    private OrderedProperties orderedProperties;
    
    @Before
    public void setUp() {
        orderedProperties = new OrderedProperties();
    }
    
    @Test
    public void testPutAndGet() {
        OrderedProperties orderedProperties = new OrderedProperties();
        orderedProperties.put("key1", "value1");
        assertEquals("value1", orderedProperties.get("key1"));
    }
    
    @Test
    public void testPutDuplicateKey() {
        OrderedProperties orderedProperties = new OrderedProperties();
        orderedProperties.put("key1", "value1");
        orderedProperties.put("key1", "value2");
        assertEquals("value2", orderedProperties.get("key1"));
    }
    
    @Test
    public void testRemove() {
        OrderedProperties orderedProperties = new OrderedProperties();
        orderedProperties.put("key1", "value1");
        orderedProperties.remove("key1");
        assertNull(orderedProperties.get("key1"));
    }
    
    @Test
    public void testEntrySet() {
        OrderedProperties orderedProperties = new OrderedProperties();
        orderedProperties.put("key1", "value1");
        orderedProperties.put("key2", "value2");
        Set<Map.Entry<Object, Object>> entrySet = orderedProperties.entrySet();
        assertEquals(2, entrySet.size());
        for (Map.Entry<Object, Object> entry : entrySet) {
            assertTrue(entry.getKey().equals("key1") || entry.getKey().equals("key2"));
            assertTrue(entry.getValue().equals("value1") || entry.getValue().equals("value2"));
        }
    }
    
    @Test
    public void testPutNullKey() {
        OrderedProperties orderedProperties = new OrderedProperties();
        try {
            orderedProperties.put(null, "value");
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected exception
        }
    }
    
    @Test
    public void testPutNullValue() {
        OrderedProperties orderedProperties = new OrderedProperties();
        orderedProperties.put("key", null);
        assertEquals(orderedProperties.get("key"), "");
    }
    
    @Test
    public void testRemoveNonExistentKey() {
        OrderedProperties orderedProperties = new OrderedProperties();
        assertNull(orderedProperties.remove("nonExistentKey"));
    }
    
    @Test
    public void testPutAndRemove() {
        OrderedProperties orderedProperties = new OrderedProperties();
        orderedProperties.put("key1", "value1");
        orderedProperties.put("key2", "value2");
        orderedProperties.remove("key1");
        assertNull(orderedProperties.get("key1"));
        assertEquals("value2", orderedProperties.get("key2"));
    }
    
    @Test
    public void testEntrySetOrder() {
        OrderedProperties orderedProperties = new OrderedProperties();
        orderedProperties.put("key1", "value1");
        orderedProperties.put("key2", "value2");
        orderedProperties.put("key3", "value3");
        Set<Map.Entry<Object, Object>> entrySet = orderedProperties.entrySet();
        Object[] keys = entrySet.stream().map(Map.Entry::getKey).toArray();
        assertArrayEquals(new Object[]{"key1", "key2", "key3"}, keys);
    }
}