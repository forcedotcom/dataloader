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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.salesforce.dataloader.client.PartnerClient;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.exception.MappingInitializationException;
import com.salesforce.dataloader.model.Row;
import com.salesforce.dataloader.util.AppUtil;
import com.salesforce.dataloader.util.OrderedProperties;

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
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public InvalidMappingException(String msg, Throwable e) {
            super(msg, e);
        }

        public InvalidMappingException(String msg) {
            super(msg);
        }
    }

    private final CaseInsensitiveSet daoColumnNames = new CaseInsensitiveSet();
    
    private CaseInsensitiveSet compositeDAOColumns = new CaseInsensitiveSet();
    private HashMap<String, Integer> compositeColSizeMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> daoColPositionInCompositeColMap = new HashMap<String, Integer>();
    private HashMap<String, String> daoColToCompositeColMap = new HashMap<String, String>();
    private HashMap<String, Boolean> fieldTypeIsStringMap = new HashMap<String, Boolean>();
    private final CaseInsensitiveMap constants = new CaseInsensitiveMap();

    protected final CaseInsensitiveMap map = new CaseInsensitiveMap();
    private final PartnerClient client;
    private final CaseInsensitiveSet fields = new CaseInsensitiveSet();
    protected final String mappingFileName;

    protected Mapper(PartnerClient client, Collection<String> columnNames, Field[] fields, String mappingFileName)
            throws MappingInitializationException {
        this.client = client;
        if (columnNames != null) {
            int i = 0;
            for (String colName : columnNames) {
                i++;
                if (colName == null) {
                    String errorMsg = "Missing column name in the CSV file at column " + i;
                    logger.error(errorMsg);
                    throw new MappingInitializationException(errorMsg);
                }
                daoColumnNames.add(colName);
            }
        }
        if (fields != null) {
            for (Field field : fields) {
                boolean isStringType = true;
                this.fields.add(field.getName());
                FieldType fieldType = field.getType();
                if (fieldType == FieldType.string
                        || fieldType == FieldType.textarea) {
                    isStringType = true;
                } else {
                    isStringType = false;
                }
                this.fieldTypeIsStringMap.put(field.getName(), isStringType);
            }
        }
        this.mappingFileName = mappingFileName;
        putPropertyFileMappings(mappingFileName);
    }

    public final void putMapping(String src, String destList) {
        String compositeDaoColName = getCompositeDaoColName(src, destList);

        // destination can be multiple field names for upload operations
        StringTokenizer st = new StringTokenizer(destList, AppUtil.COMMA);
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
        this.map.put(compositeDaoColName, originalDestList);
    }
    
    private String getCompositeDaoColName(String mappingSrcStr, String destFieldList) {
        boolean isDestinationListStringOnly = true;
        StringTokenizer st = new StringTokenizer(destFieldList, AppUtil.COMMA);
        while(st.hasMoreElements()) {
            String destFieldName = st.nextToken();
            destFieldName = destFieldName.trim();
            Boolean destFieldTypeIsString = this.fieldTypeIsStringMap.get(destFieldName);
            if (destFieldTypeIsString == null || !destFieldTypeIsString) {
                isDestinationListStringOnly = false;
                break;
            }
        }

        String compositeCol = null;
        int daoColCount = 0;
        st = new StringTokenizer(mappingSrcStr, AppUtil.COMMA);
        while(st.hasMoreElements()) {
            String mappingSrcCol = st.nextToken();
            mappingSrcCol = mappingSrcCol.trim();
            String daoCol = daoColumnNames.getOriginal(mappingSrcCol);
            if (compositeCol == null) {
                compositeCol = daoCol;
            } else {
                compositeCol += ", " + daoCol;
            }
            getDaoColPositionInCompositeColMap().put(daoCol, daoColCount);
            daoColCount++;
            if (!isDestinationListStringOnly) {
                // set up only the first daoCol for mapping if the destination
                // field list contains a field whose type is not string
                break; 
            }
        }
        if (compositeCol == null) {
            compositeCol = mappingSrcStr;
            daoColCount = 1;
        }
        getCompositeColSizeMap().put(compositeCol, daoColCount);
        getCompositeDAOColumns().add(compositeCol);
        
        // need to configure mapping from DAO column to composite column
        st = new StringTokenizer(mappingSrcStr, AppUtil.COMMA);
        boolean isDaoColToCompositeColMappingSet = false;
        while(st.hasMoreTokens()) {
            String mappingSrcCol = st.nextToken();
            mappingSrcCol = mappingSrcCol.trim();
            String daoCol = daoColumnNames.getOriginal(mappingSrcCol);
            if (daoCol != null) {
                getDaoColToCompositeColMap().put(daoCol, compositeCol);
                isDaoColToCompositeColMappingSet = true;
            }
        }
        if (!isDaoColToCompositeColMappingSet) {
            getDaoColToCompositeColMap().put(mappingSrcStr, compositeCol);
            isDaoColToCompositeColMappingSet = true;
        }
        return compositeCol;
    }

    protected void putConstant(String name, String value) {
        handleMultipleValuesFromConstant(name, extractConstant(value));
    }

    private void handleMultipleValuesFromConstant(String name, String value) {
        StringTokenizer st = new StringTokenizer(name, AppUtil.COMMA);
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
        rowMap.putAll(constants);
    }

    private Properties loadProperties(String fileName) throws MappingInitializationException {
        OrderedProperties props = new OrderedProperties();
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
        return !this.daoColumnNames.isEmpty();
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
        String compositeColName = this.getDaoColToCompositeColMap().get(srcName);
        if (compositeColName == null) {
            // DAO column is not mapped, ignore
            return null;
        }
        if (map.containsKey(compositeColName)) {
            return map.get(compositeColName);
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
        this.compositeDAOColumns = new CaseInsensitiveSet();
        this.getCompositeColSizeMap().clear();
        this.getDaoColPositionInCompositeColMap().clear();
        this.getDaoColToCompositeColMap().clear();
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
        return this.daoColumnNames.containsKey(localName);
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
        return this.daoColumnNames.getOriginalValues();
    }

    public PartnerClient getClient() {
        return client;
    }

    protected CaseInsensitiveSet getCompositeDAOColumns() {
        return compositeDAOColumns;
    }

    protected HashMap<String, Integer> getCompositeColSizeMap() {
        return compositeColSizeMap;
    }

    protected HashMap<String, Integer> getDaoColPositionInCompositeColMap() {
        return daoColPositionInCompositeColMap;
    }

    protected HashMap<String, String> getDaoColToCompositeColMap() {
        return daoColToCompositeColMap;
    }
}