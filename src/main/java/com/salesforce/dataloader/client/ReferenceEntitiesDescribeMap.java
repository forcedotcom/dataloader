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

package com.salesforce.dataloader.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.salesforce.dataloader.dyna.RelationshipField;
import com.sforce.soap.partner.Field;

/**
 * 
 */
public class ReferenceEntitiesDescribeMap {

    private Map<String, DescribeRefObject> referenceEntitiesDescribeMap = new HashMap<String, DescribeRefObject>();
    /**
     * 
     */
    public ReferenceEntitiesDescribeMap() {
        
    }
    
    public void put(String relationshipFieldName, DescribeRefObject parent) {
        RelationshipField objField = new RelationshipField(parent.getParentObjectName(), relationshipFieldName);
        referenceEntitiesDescribeMap.put(objField.toFormattedRelationshipString(), parent);
    }
    
    // fieldName could be in the old format that assumes single parent: 
    // <relationship name attr of the field on child sobject>:<idlookup field name on parent sobject>
    //
    // fieldName could also be in the new format
    // <name of parent object>:<rel name on child object>.<idlookup field name on parent>

    public DescribeRefObject getParentSObject(String lookupFieldName) {
        return getParentSObject(new RelationshipField(lookupFieldName, false));
    }
    
    public void clear() {
        this.referenceEntitiesDescribeMap.clear();
    }
    
    public int size() {
        return this.referenceEntitiesDescribeMap.size();
    }
    
    public Set<String> keySet() {
        return this.referenceEntitiesDescribeMap.keySet();
    }
    
    // fieldName could be in the old format that assumes single parent: 
    // <relationship name attr of the field on child sobject>:<idlookup field name on parent sobject>
    //
    // fieldName could also be in the new format
    // <name of parent object>:<rel name on child object>.<idlookup field name on parent>
    public Field getParentField(String fieldName) {
        RelationshipField fieldName4LR = new RelationshipField(fieldName, true);
        if (fieldName4LR == null 
                || fieldName4LR.getParentFieldName() == null 
                || fieldName4LR.getRelationshipName() == null) {
            return null;
        } else {
            DescribeRefObject parent = getParentSObject(fieldName4LR);
            if (parent == null) {
                return null;
            }
            for (Map.Entry<String, Field> refEntry : parent.getParentObjectFieldMap().entrySet()) {
                if (fieldName4LR.getParentFieldName().equalsIgnoreCase(refEntry.getKey())) {
                    return refEntry.getValue();
                }
            }
            return null;
        }
    }
    
    private DescribeRefObject getParentSObject(RelationshipField fieldName4LR) {
        if (fieldName4LR == null || fieldName4LR.getRelationshipName() == null) {
            return null;
        }
        for (Map.Entry<String, DescribeRefObject> ent : referenceEntitiesDescribeMap.entrySet()) {
            String relNameInEntry = ent.getKey().toLowerCase();
            if (fieldName4LR.isRelationshipName(relNameInEntry)) {
                return ent.getValue();
            }
        }
        return null;
    }
}