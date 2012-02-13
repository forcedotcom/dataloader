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

import com.salesforce.dataloader.client.PartnerClient;
import com.salesforce.dataloader.exception.MappingInitializationException;
import com.sforce.soap.partner.Field;

/**
 * Mapper which maps field names for loading operations. Field names are mapped from dao (local) name to sfdc name.
 * 
 * @author Colin Jarvis
 * @since 21.0
 */
public class LoadMapper extends Mapper {

    public LoadMapper(PartnerClient client, Collection<String> columnNames, String mappingFileName)
            throws MappingInitializationException {
        super(client, columnNames, mappingFileName);
    }

    @Override
    protected void putPropertyEntry(Entry<Object, Object> entry) {
        String dao = (String)entry.getKey();
        String sfdc = (String)entry.getValue();
        if (isConstant(dao))
            putConstant(sfdc, dao);
        else if (!hasDaoColumns() || hasDaoColumn(dao)) putMapping(dao, sfdc);
    }

    public Map<String, String> getMappingWithUnmappedColumns(boolean includeUnmapped) {
        final HashMap<String, String> result = new HashMap<String, String>(getMap());
        if (includeUnmapped) {
            for (String daoColumn : getDaoColumns()) {
                if (getMapping(daoColumn) == null) result.put(daoColumn, null);
            }
        }
        return result;
    }

    public Map<String, Object> mapData(Map<String, Object> localRow) {
        Map<String, Object> mappedData = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : localRow.entrySet()) {
            String sfdcName = getMapping(entry.getKey());
            // TODO: maybe we should warn about unmapped input columns?
            if (sfdcName != null) mappedData.put(sfdcName, entry.getValue());
        }
        mapConstants(mappedData);
        return mappedData;
    }

    public Map<String, Field> resolveMappedFieldsForDataLoad() throws MappingInitializationException {
        final Map<String, Field> fields = new HashMap<String, Field>();
        for (Map.Entry<String, String> entry : getMappingWithUnmappedColumns(false).entrySet()) {
            String sfdcName = entry.getValue();
            final Field f = getClient().getField(sfdcName);
            if (f == null)
                throw new MappingInitializationException("Field mapping is invalid: " + entry.getKey() + " => "
                        + sfdcName);
            fields.put(entry.getKey(), f);
        }
        return fields;
    }

}
