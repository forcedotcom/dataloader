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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;

import org.apache.logging.log4j.Logger;
import org.springframework.util.LinkedCaseInsensitiveMap;
import com.salesforce.dataloader.util.DLLogManager;

import com.salesforce.dataloader.client.DescribeRefObject;
import com.salesforce.dataloader.client.PartnerClient;
import com.salesforce.dataloader.client.ReferenceEntitiesDescribeMap;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.dyna.ParentIdLookupFieldFormatter;
import com.salesforce.dataloader.dyna.ParentSObjectFormatter;
import com.salesforce.dataloader.exception.MappingInitializationException;
import com.salesforce.dataloader.model.Row;
import com.salesforce.dataloader.model.TableRow;
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

    private static final Logger logger = DLLogManager.getLogger(Mapper.class);

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

    protected final Map<String, String> daoColumnNames = new LinkedCaseInsensitiveMap<String>();
    
    private LinkedHashMap<String, Integer> compositeColSizeMap = new LinkedHashMap<String, Integer>();
    private LinkedHashMap<String, Integer> daoColPositionInCompositeColMap = new LinkedHashMap<String, Integer>();
    private LinkedHashMap<String, String> daoColToCompositeColMap = new LinkedHashMap<String, String>();
    private LinkedHashMap<String, Boolean> fieldTypeIsStringMap = new LinkedHashMap<String, Boolean>();
    private final Map<String, String> constants =  new LinkedCaseInsensitiveMap<String>();

    protected final Map<String, String> map = new LinkedCaseInsensitiveMap<String>();
    private final PartnerClient client;
    private final Map<String, String> fields = new LinkedCaseInsensitiveMap<String>();
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
                daoColumnNames.put(colName, colName);
                daoColPositionInCompositeColMap.put(colName, 0);
                daoColToCompositeColMap.put(colName, colName);
                compositeColSizeMap.put(colName, 1);
            }
        }
        if (fields != null) {
            for (Field field : fields) {
                boolean isStringType = true;
                this.fields.put(field.getName(), field.getName());
                FieldType fieldType = field.getType();
                if (fieldType == FieldType.string
                        || fieldType == FieldType.textarea) {
                    isStringType = true;
                } else {
                    isStringType = false;
                }
                this.fieldTypeIsStringMap.put(field.getName().toLowerCase(), isStringType);
            }
        }
        this.mappingFileName = mappingFileName;
        putPropertyFileMappings(mappingFileName);
    }

    public final void putMapping(String src, String destList) {
        processCompositeDaoColName(src, destList);

        // destination can be multiple field names for upload operations
        StringTokenizer st = new StringTokenizer(destList, AppUtil.COMMA);
        String originalDestList = null;
        while(st.hasMoreElements()) {
            String v = st.nextToken();
            v = v.trim();
            String originalVal = fields.get(v);
            if (originalVal == null) {
                originalVal = v;
                // check if it is a lookup relationship field
                if (v.contains(ParentIdLookupFieldFormatter.NEW_FORMAT_PARENT_IDLOOKUP_FIELD_SEPARATOR_CHAR)
                        || v.contains(ParentSObjectFormatter.NEW_FORMAT_RELATIONSHIP_NAME_SEPARATOR_CHAR)) {
                    try {
                        Field lookupFieldInParent = client.getField(v);
                        ParentIdLookupFieldFormatter specifiedFieldFormatter = new ParentIdLookupFieldFormatter(v);
                        Field relationshipField = client.getFieldFromRelationshipName(specifiedFieldFormatter.getParent().getRelationshipName());
                        String parentSObjectName = specifiedFieldFormatter.getParent().getParentObjectName();
                        ReferenceEntitiesDescribeMap refEntitiesMap = client.getReferenceDescribes();
                        DescribeRefObject refObject = refEntitiesMap.getParentSObject(specifiedFieldFormatter.getParent().toString());
                        if (refObject == null) { // legacy format: <relationship name in a relationship field>:<idLookup field in parent sobject>
                            if (relationshipField != null) {
                                parentSObjectName = relationshipField.getReferenceTo()[0];
                            }
                        } else { // new format: <relationship name in a relationship field>:<parent sobject name>-<idLookup field in parent sobject>
                            parentSObjectName = client.describeSObject(refObject.getParentObjectName()).getName();
                        }
                        if (relationshipField != null) {
                            ParentIdLookupFieldFormatter sfFieldFormatter = new ParentIdLookupFieldFormatter(parentSObjectName,
                                                                    relationshipField.getRelationshipName(),
                                                                    lookupFieldInParent.getName());
                            originalVal = sfFieldFormatter.toString();
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            if (originalDestList == null) {
                originalDestList = originalVal;
            } else {
                originalDestList = originalDestList + ", " + originalVal;
            }
        }
        this.map.put(src, originalDestList);
    }
    
    private void processCompositeDaoColName(String mappingSrcStr, String destFieldList) {
        boolean isDestinationListStringOnly = true;
        StringTokenizer st = new StringTokenizer(destFieldList, AppUtil.COMMA);
        while(st.hasMoreElements()) {
            String destFieldName = st.nextToken();
            destFieldName = destFieldName.trim();
            Boolean destFieldTypeIsString = this.fieldTypeIsStringMap.get(destFieldName.toLowerCase());
            if (destFieldTypeIsString == null || !destFieldTypeIsString) {
                isDestinationListStringOnly = false;
                logger.debug(destFieldName + " is not string or text area.");
                break;
            }
        }

        int daoColCount = 0;
        st = new StringTokenizer(mappingSrcStr, AppUtil.COMMA);
        while(st.hasMoreElements()) {
            String mappingSrcCol = st.nextToken();
            mappingSrcCol = mappingSrcCol.trim();
            String daoCol = daoColumnNames.get(mappingSrcCol);
            if (daoCol == null) {
                daoCol = mappingSrcCol;
            }
            daoColPositionInCompositeColMap.put(daoCol, daoColCount);
            if (daoColCount == 0 && st.countTokens() == 0 && daoCol.equalsIgnoreCase(mappingSrcCol)) {
                // keep dao column's label if possible
                daoColToCompositeColMap.put(daoCol, daoCol);
            } else {
                daoColToCompositeColMap.put(daoCol, mappingSrcStr);
            }
            daoColCount++;
            if (!isDestinationListStringOnly) {
                // set up only the first daoCol for mapping if the destination
                // field list contains a field whose type is not string
                logger.debug("Using only the first CSV column '" + daoCol + "' for mapping because one of the sobject fields it is mapped to is not a string or a text area");
                break; 
            }
        }
        if (daoColCount == 0) {
            String daoCol = daoColumnNames.get(mappingSrcStr);
            if (daoCol == null) {
                daoCol = mappingSrcStr;
            }
            getDaoColToCompositeColMap().put(mappingSrcStr, mappingSrcStr);
            daoColCount = 1;
        }
        compositeColSizeMap.put(mappingSrcStr, daoColCount);
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

    protected void mapConstants(TableRow row) {
        for (String constKey : constants.keySet()) {
            row.put(constKey, constants.get(constKey));
        }
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
        clearMappings();
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

    /*
    public String getMapping(String srcName, boolean isSrcNameComposite) {
        return getMapping(srcName, false, isSrcNameComposite);
    }
    */

    public String getMapping(String srcName, boolean strictMatching, boolean isSrcNameComposite) {
        String compositeColName = null;
        if (isSrcNameComposite) {
            compositeColName = srcName;
        } else {
            compositeColName = this.getDaoColToCompositeColMap().get(srcName);
        }
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
    public void clearMappings() {
        this.map.clear();
    }

    public void save(String filename) throws IOException {
        if (filename == null) throw new IOException(Messages.getMessage(getClass(), "errorFileName"));
        Properties props = new Properties();
        for (String key : map.keySet()) {
            String value = map.get(key);
            if (value == null) {
                map.put(key, "");
            }
        }
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

    protected Collection<String> getDaoColumns() {
        return this.daoColumnNames.values();
    }

    public PartnerClient getClient() {
        return client;
    }

    protected Set<String> getCompositeDAOColumns() {
        LinkedHashSet<String> compositeDAOCols = new LinkedHashSet<String>();
        compositeDAOCols.addAll(this.daoColToCompositeColMap.values());
        return compositeDAOCols;
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