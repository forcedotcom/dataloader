/*
 * Copyright (c) 2012, salesforce.com, inc.
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

import java.util.*;
import java.util.Map.Entry;

import com.salesforce.dataloader.model.Row;
import org.apache.log4j.Logger;

import com.salesforce.dataloader.client.PartnerClient;
import com.salesforce.dataloader.exception.MappingInitializationException;
import com.salesforce.dataloader.mapping.SOQLInfo.SOQLFieldInfo;
import com.salesforce.dataloader.mapping.SOQLInfo.SOQLParserException;
import com.sforce.soap.partner.*;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;

/**
 * Mapping from sfdcName -> daoName
 * 
 * @author Colin Jarvis
 * @since 21.0
 */
public class SOQLMapper extends Mapper {

    private static final Logger logger = Logger.getLogger(SOQLMapper.class);

    private SOQLInfo soqlInfo;

    public SOQLMapper(PartnerClient client, Collection<String> columnNames, String mappingFileName)
            throws MappingInitializationException {
        super(client, columnNames, mappingFileName);
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
        Row map = new Row();
        mapPartnerSObject(map, "", sobj);
        mapConstants(map);
        return map;
    }

    private void mapPartnerSObject(Row map, String prefix, XmlObject sobj) {
        Iterator<XmlObject> fields = sobj.getChildren();
        if (fields == null) return;
        while (fields.hasNext()) {
            XmlObject field = fields.next();

            final String fieldName = prefix + field.getName().getLocalPart();
            String localName = getMapping(fieldName);
            if (localName != null) {
                map.put(localName, field.getValue());
            }
            mapPartnerSObject(map, fieldName + ".", field);
        }
    }

    @Override
    protected void putPropertyEntry(Entry<Object, Object> entry) {
        String dao = (String)entry.getValue();
        String sfdc = (String)entry.getKey();
        if (isConstant(sfdc))
            putConstant(dao, sfdc);
        else if (!hasDaoColumns() || hasDaoColumn(dao)) try {
            addSoqlFieldMapping((String)entry.getValue(), new SOQLFieldInfo(sfdc));
        } catch (SOQLParserException e) {
            throw new InvalidMappingException(e.getMessage(), e);
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
                logger.warn("sfdc returned row that cannot be mapped: " + sfdcName);
            } else {
                resultRow.put(localName, val);
            }
        }
        mapConstants(resultRow);
        return resultRow;
    }

    public void initSoqlMapping(String soql) {
        if (this.soqlInfo == null) try {
            this.soqlInfo = new SOQLInfo(soql);
        } catch (SOQLParserException e) {
            throw new InvalidMappingException(e.getMessage(), e);
        }
        if (hasMappings()) return;
        // if we didn't map any fields from the properties file, then we do the default soql mapping
        for (SOQLFieldInfo fieldInfo : soqlInfo.getSelectedFields()) {
            addSoqlFieldMapping(fieldInfo.toString(), fieldInfo);
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
            if (!daoColumns.isEmpty())
                throw new InvalidMappingException("The following dao columns could not be mapped: " + daoColumns);
            if (!entries.isEmpty()) {
                logger.warn("The following select fields were not mapped: " + entries);
            }
        }
    }

    private void addSoqlFieldMapping(String daoName, SOQLFieldInfo fieldInfo) {
        putMapping(normalizeSoql(fieldInfo), daoName);
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
