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

import com.salesforce.dataloader.client.PartnerClient;
import com.salesforce.dataloader.exception.MappingInitializationException;
import com.salesforce.dataloader.mapping.SOQLInfo.SOQLFieldInfo;
import com.salesforce.dataloader.mapping.SOQLInfo.SOQLParserException;
import com.salesforce.dataloader.model.Row;
import com.salesforce.dataloader.util.AppUtil;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.xml.namespace.QName;

/**
 * Mapping from sfdcName -> daoName
 * 
 * @author Colin Jarvis
 * @since 21.0
 */

/*
 * Mapping for extraction operations has the following parts:
 * 1. A mapping file.
 * 2. A mapping array provided in the constructor.
 * 3. SoQL - fields specified in the query. This is skipped if skipSoQLMapping == true in the constructor.
 * 4. query results - fields returned in the results. This is skipped if skipSoQLMapping == false.
 * 
 * The implementation needs to do the following:
 * If skipSoQLMapping == false (default case, legacy behavior)
 *     start with 1, overlay 2, produce an output file containing fields from the union of 1, 2, and 3.
 * If skipSoQLMapping == true (no client-side SoQL checks, no client-side parsing of SoQL query)
 *     produce an output file containing all fields in the result. Use mapped field name if the field is mapped.
 */
public class SOQLMapper extends Mapper {

    private static final Logger logger = LogManager.getLogger(SOQLMapper.class);

    private SOQLInfo soqlInfo;
    private boolean skipSoQLMapping = false;
    private CaseInsensitiveMap extractionMap = new CaseInsensitiveMap();

    public SOQLMapper(PartnerClient client, Collection<String> columnNames, Field[] fields, String mappingFileName)
            throws MappingInitializationException {
        this(client, columnNames, fields, mappingFileName, false);
    }
    
    public SOQLMapper(PartnerClient client, Collection<String> columnNames, Field[] fields, String mappingFileName, boolean skipSoQLMapping)
            throws MappingInitializationException {
        super(client, columnNames, fields, mappingFileName);
        this.skipSoQLMapping = skipSoQLMapping;
    }

    public List<String> getDaoColumnsForSoql() {
        List<String> daoColumns = new ArrayList<String>();
        for (SOQLFieldInfo fieldInfo : soqlInfo.getSelectedFields()) {
            String daoColumn = getMapping(normalizeSoql(fieldInfo));
            if (daoColumn != null) daoColumns.add(daoColumn);
        }
        return daoColumns;
    }

    public Row mapPartnerSObjectSfdcToLocal(SObject sobj) {
        Row row = new Row();
        mapPartnerSObject(row, "", sobj);
        mapConstants(row);
        return row;
    }
    
    public void setSkipSoQLMapping(boolean skipSoQLMapping) {
        this.skipSoQLMapping = skipSoQLMapping;
    }
    
    // overwrite parent's methods to use soqlMap instead of map
    public String getMapping(String srcName, boolean strictMatching) {
        if (extractionMap.containsKey(srcName)) {
            return extractionMap.get(srcName);
        }
        // handle aggregate queries
        if (!strictMatching) {
            for (Entry<String, String> entry : extractionMap.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(srcName)
                    || entry.getKey().toLowerCase().endsWith("." + srcName.toLowerCase())) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private void mapPartnerSObject(Row row, String prefix, XmlObject sobj) {
        Iterator<XmlObject> fields = sobj.getChildren();
        if (fields == null) return;
        while (fields.hasNext()) {
            XmlObject field = fields.next();
            final String fieldName = prefix + field.getName().getLocalPart();
            String localName = getMapping(fieldName);
            if (localName == null) {
                localName = fieldName;
            }
            if (localName != null) {
                Object value = field.getValue();
                QName xmlType = field.getXmlType();
                if (xmlType != null && xmlType.getLocalPart().equals("date") && value instanceof Date){
                    //WSC got confused and converted a date string to a date object.
                    //this causes weirdness in the output format and timezone correction that we don't want
                    //convert the type back to a string before a later handler mis-handles it
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
                    value = formatter.format(value);
                }
                row.put(localName, value);
            }
            mapPartnerSObject(row, fieldName + ".", field);
        }
    }

    @Override
    protected void putPropertyEntry(Entry<Object, Object> entry) {
        String daoColName = (String)entry.getValue();
        String sfdcColName = (String)entry.getKey();
        if (isConstant(sfdcColName)) {
            putConstant(daoColName, sfdcColName);
        } else if (!hasDaoColumns() || hasDaoColumn(daoColName)) {
            try {
                addExtractionMapping((String)entry.getValue(), new SOQLFieldInfo(sfdcColName));
            } catch (SOQLParserException e) {
                throw new InvalidMappingException(e.getMessage(), e);
            } catch (InvalidMappingException e) {
                logger.warn("Unable to find Salesforce object field " + sfdcColName + " specified in the mapping file " + this.mappingFileName);
                logger.warn(e.getMessage());
            }
        }
    }

    public Row mapCsvRowSfdcToLocal(List<String> headers, List<String> values, StringBuilder id) {
        Row resultRow = new Row();
        Iterator<String> headerIter = headers.listIterator();
        for (String val : values) {
            String sfdcName = headerIter.next();
            if ("Id".equalsIgnoreCase(sfdcName)) id.append(val);
            String localName = getMapping(sfdcName);
            if (localName == null) {
                if (this.skipSoQLMapping) {
                    this.map.put(sfdcName, sfdcName);
                    localName = sfdcName;
                    resultRow.put(localName, val);
                } else {
                    logger.warn("sfdc returned row that cannot be mapped: " + sfdcName);
                }
            } else {
                resultRow.put(localName, val);
            }
        }
        mapConstants(resultRow);
        return resultRow;
    }

    //
    public void initSoqlMapping(String soql) {
        try {
            this.soqlInfo = new SOQLInfo(soql);
        } catch (SOQLParserException e) {
            throw new InvalidMappingException(e.getMessage(), e);
        }
        initializeSoQLMap();
        if (skipSoQLMapping) return;

        // if we didn't map any fields from the properties file, then we do the default soql mapping
        for (SOQLFieldInfo fieldInfo : soqlInfo.getSelectedFields()) {
            addSoqlFieldMapping(fieldInfo, fieldInfo.toString());
        }
        
        if (hasDaoColumns()) {
            // if no mapping file was provided, but dao col names are known (eg database writer), then we should get
            // the dao names correct
            List<Map.Entry<String, String>> entries = new LinkedList<Map.Entry<String, String>>(getMap().entrySet());
            clearMap();

            // FIXME UGLY, NESTED LOOPS
            List<String> daoColumns = new LinkedList<String>(getDaoColumns());
            ListIterator<String> daoIter = daoColumns.listIterator();
            while (daoIter.hasNext()) {
                String daoColName = daoIter.next();
                ListIterator<Entry<String, String>> entryIter = entries.listIterator();
                while (entryIter.hasNext()) {
                    Entry<String, String> ent = entryIter.next();
                    String sfdcName = ent.getKey();
                    String autoDaoName = ent.getValue();
                    if (sfdcName.equalsIgnoreCase(daoColName) || autoDaoName.equalsIgnoreCase(daoColName)) {
                        putMapping(sfdcName, daoColName);
                        entryIter.remove();
                        daoIter.remove();
                        break;
                    }
                }
            }
            addDaoMappingToExtractionMapping();
            if (!daoColumns.isEmpty())
                throw new InvalidMappingException("The following dao columns could not be mapped: " + daoColumns);
            if (!entries.isEmpty()) {
                logger.warn("The following select fields were not mapped: " + entries);
            }
        }
    }
    
    public void addDaoMappingToExtractionMapping() {
        this.extractionMap.putAll(this.map);
    }
    
    public Collection<String> getDestColumns() {
        return this.extractionMap.values();
    }
    
    public void clearMap() {
        super.clearMap();
        this.extractionMap.clear();
    }
    
    public void removeMapping(String srcName) {
        super.removeMapping(srcName);
        this.extractionMap.remove(srcName);
    }

    protected Map<String, String> getMap() {
        return Collections.unmodifiableMap(this.extractionMap);
    }

    
    private void initializeSoQLMap() {
        this.extractionMap.clear();
        
        //add extraction mapping
        this.extractionMap.putAll(this.map);
    }

    private void addExtractionMapping(String daoName, SOQLFieldInfo fieldInfo) {
        putMapping(normalizeSoql(fieldInfo), daoName);
    }
    
    private void addSoqlFieldMapping(SOQLFieldInfo fieldInfo, String daoName) {
        String sfdcField = normalizeSoql(fieldInfo);
        if (!this.extractionMap.containsKey(sfdcField)) {
            this.extractionMap.put(sfdcField, daoName);
        }
    }

    private String normalizeSoql(SOQLFieldInfo fieldInfo) {
        String normalizedFieldName = evalSfdcField(fieldInfo.getFieldName());
        return fieldInfo.isAggregate() ? fieldInfo.getAlias() : normalizedFieldName;
    }

    private String evalSfdcField(String fieldExpr) {
        fieldExpr = fieldExpr.toLowerCase();
        if (this.soqlInfo != null) {
            String typePrefix = this.soqlInfo.getTableName().toLowerCase() + ".";
            String aliasPrefix = this.soqlInfo.getTableAlias().toLowerCase() + ".";
            if (fieldExpr.startsWith(typePrefix)) {
                fieldExpr = fieldExpr.substring(typePrefix.length());
            } else if (fieldExpr.startsWith(aliasPrefix)) {
                fieldExpr = fieldExpr.substring(aliasPrefix.length());
            }
        }
        DescribeSObjectResult describeResult = getClient().getFieldTypes();
        return evalSfdcField(describeResult, fieldExpr);
    }

    private String evalSfdcField(DescribeSObjectResult describeResult, String fieldExpr) {
        if (describeResult == null) {
            throw new InvalidMappingException("Failed to get entity fields from server");
        }
        final int splitIdx = fieldExpr.indexOf('.');
        if (splitIdx >= 0) {
          final   Field field = getReferenceField(describeResult, fieldExpr.substring(0, splitIdx));

            try {
                fieldExpr = fieldExpr.substring(splitIdx + 1);
            } catch (IndexOutOfBoundsException e) {
                throw new InvalidMappingException("Failed to parse field expression " + fieldExpr);
            }
            
            assert field.getReferenceTo() != null && field.getReferenceTo().length>0;  // this should never happen
            try {
                // if the reference is polymorphic we need to lookup the next field on the Name object
                final String relEntityName = field.isNamePointing() ? "Name" : field.getReferenceTo()[0];
                describeResult = getClient().describeSObject(relEntityName);
                return field.getRelationshipName() + "." + evalSfdcField(describeResult, fieldExpr);
            } catch (ConnectionException e) {
                throw new InvalidMappingException("Connection error while parsing field expression " + fieldExpr, e);
            }
        } else {
            return getSfdcField(describeResult, fieldExpr).getName();
        }

    }

    private Field getSfdcField(DescribeSObjectResult describeResult, String fieldName) {
        for (Field f : describeResult.getFields()) {
            if (f.getName().equalsIgnoreCase(fieldName)) return f;
        }
        throw new InvalidMappingException("No such field " + fieldName + " on entity " + describeResult.getName());
    }

    private Field getReferenceField(DescribeSObjectResult describeResult, String relName) {
        for (Field f : describeResult.getFields()) {
            if (FieldType.reference == f.getType() && relName.equalsIgnoreCase(f.getRelationshipName())) {
                return f;
            }
        }
        throw new InvalidMappingException("No reference field " + relName + " on entity " + describeResult.getName());
    }
}
