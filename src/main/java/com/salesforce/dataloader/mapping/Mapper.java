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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import com.sforce.soap.partner.Field;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.salesforce.dataloader.client.PartnerClient;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.exception.MappingInitializationException;
import com.salesforce.dataloader.model.Row;

/**
 * Base class for field name mappers. Used by data loader operations to map between local field names and sfdc field
 * names.
 * 
 * @author Colin Jarvis
 * @since 21.0
 */
public abstract class Mapper {

    private static final Logger logger = LogManager.getLogger(Mapper.class);

    public static class InvalidMappingException extends RuntimeException {
        public InvalidMappingException(String msg, Throwable e) {
            super(msg, e);
        }

        public InvalidMappingException(String msg) {
            super(msg);
        }
    }

    private final CaseInsensitiveSet daoColumns;
    private final Map<String, String> constants = caseInsensitiveMap();

    private final Map<String, String> map = caseInsensitiveMap();
    private final PartnerClient client;
    private final CaseInsensitiveSet fields;

    private <V> Map<String, V> caseInsensitiveMap() {
        return new TreeMap<String, V>(String.CASE_INSENSITIVE_ORDER);
    }

    protected Mapper(PartnerClient client, Collection<String> columnNames, Field[] fields, String mappingFileName)
            throws MappingInitializationException {
        this.client = client;
        this.fields = new CaseInsensitiveSet();
        Set<String> daoColumns = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        if (columnNames != null) daoColumns.addAll(columnNames);
        this.daoColumns = new CaseInsensitiveSet(Collections.unmodifiableSet(daoColumns));
        if (fields != null) {
            for (Field field : fields) {
                this.fields.add(field.getName());
            }
        }
        putPropertyFileMappings(mappingFileName);
    }

    public final void putMapping(String src, String dest) {
        // destination can be multiple field names for upload operations
        StringTokenizer st = new StringTokenizer(dest, ",");
        String originalDestList = null;
        while(st.hasMoreElements()) {
            String v = st.nextToken();
            v = v.trim();
            String originalVal = fields.getOriginal(v);
            if (originalDestList == null) {
                originalDestList = originalVal;
            } else {
                originalDestList = originalDestList + ", " + originalVal;
            }
        }
        this.map.put(daoColumns.getOriginal(src), originalDestList);
    }

    protected void putConstant(String name, String value) {
        handleMultipleValuesFromConstant(name, extractConstant(value));
    }

    private void handleMultipleValuesFromConstant(String name, String value) {
        StringTokenizer st = new StringTokenizer(name, ",");
        while(st.hasMoreElements()) {
            String v = st.nextToken();
            v = v.trim();
            this.constants.put(v, value);
        }
    }

    private static String extractConstant(String constantVal) {
        return constantVal.substring(1, constantVal.length() - 1);
    }

    protected void mapConstants(Row rowMap) {
        rowMap.putAll(this.constants);
    }

    private Properties loadProperties(String fileName) throws MappingInitializationException {
        Properties props = new Properties();
        if (fileName != null && fileName.length() > 0) {
            try {
                FileInputStream in = new FileInputStream(fileName);
                try {
                    props.load(in);
                } finally {
                    in.close();
                }
            } catch (IOException e) {
                String errMsg = Messages.getMessage(getClass(), "errorLoad", e.getMessage());
                logger.error(errMsg, e);
                throw new MappingInitializationException(errMsg, e);
            }
        }
        return props;
    }

    public void putPropertyFileMappings(String fileName) throws MappingInitializationException {
        putPropertyFileMappings(loadProperties(fileName));
    }

    public void putPropertyFileMappings(Properties props) {
        for (Entry<Object, Object> entry : props.entrySet()) {
            putPropertyEntry(entry);
        }
    }

    /**
     * add mapping if: 1. if source columns were originally provided & the key exists 2. source columns were not
     * provided 3. source is a constant
     */
    protected abstract void putPropertyEntry(Entry<Object, Object> entry);

    protected boolean hasDaoColumns() {
        return !this.daoColumns.isEmpty();
    }

    protected static boolean isConstant(String name) {
        if (name == null) return false;
        int len = name.length();
        if (len < 2) return false;
        return name.charAt(0) == '"' && name.charAt(len - 1) == '"';
    }

    protected boolean hasMappings() {
        return !this.map.isEmpty();
    }

    public Collection<String> getDestColumns() {
        return this.map.values();
    }

    public String getMapping(String srcName) {
        return getMapping(srcName, false);
    }

    public String getMapping(String srcName, boolean strictMatching) {
        if (map.containsKey(srcName)) {
            return map.get(srcName);
        }
        // handle aggregate queries
        if (!strictMatching) {
            for (Entry<String, String> entry : map.entrySet()) {
                if (entry.getKey().endsWith("." + srcName)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }
    public void clearMap() {
        this.map.clear();
    }

    public void save(String filename) throws IOException {
        if (filename == null) throw new IOException(Messages.getMessage(getClass(), "errorFileName"));
        Properties props = new Properties();
        props.putAll(this.map);
        FileOutputStream out = new FileOutputStream(filename);
        try {
            props.store(out, "Mapping values");
        } finally {
            out.close();
        }
    }

    public boolean hasDaoColumn(String localName) {
        return this.daoColumns.contains(localName);
    }

    public void removeMapping(String srcName) {
        this.map.remove(srcName);
    }

    protected Map<String, String> getMap() {
        return Collections.unmodifiableMap(this.map);
    }

    public Map<String, String> getConstantsMap() {
        return Collections.unmodifiableMap(this.constants);
    }

    protected Set<String> getDaoColumns() {
        return this.daoColumns.getOriginalValues();
    }

    public PartnerClient getClient() {
        return client;
    }
}
