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
package com.salesforce.dataloader.dyna;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Container for an object field of format
 *     objectName:fieldName
 * 
 * @author Alex Warshavsky
 * @since 8.0
 */
public class ParentSObjectString {
    private String relationshipName;
    private String parentObjectName = null;
    private Integer numParentTypes = null;
    private static final Logger logger = LogManager.getLogger(ParentSObjectString.class);

    public static final String NEW_FORMAT_RELATIONSHIP_NAME_SEPARATOR_CHAR = ":";
  
    public ParentSObjectString(String parentObjectName, String relationshipName, Integer numParentTypes) {
        initialize(parentObjectName, relationshipName, numParentTypes);
    }
    
    // parentAndRelationshipName param can be in one of the following formats:
    // format 1: alphanumeric string without any ':' or '-' in it. Represents name of child's non-polymorphic relationship field
    // format 1 => it is name of a non-polymorphic relationship field in child object.
    //
    // format 2: alphanumeric string with a ':' in it
    // format 2 has 1 interpretations:
    //   interpretation 1: <child relationship field name>:<parent sobject name>
    //      - this is the new format for keys of the hashmap referenceEntitiesDescribeMap

    public ParentSObjectString(String parentAndRelationshipName, Integer numParentTypes) {
        String relationshipName = null;
        String parentObjectName = null;
        String[] fieldNameParts = parentAndRelationshipName.split(ParentIdLookupFieldString.NEW_FORMAT_PARENT_IDLOOKUP_FIELD_SEPARATOR_CHAR);
        if (fieldNameParts.length == 2) { // discard the part containing parent's idLookup field name
            parentAndRelationshipName = fieldNameParts[0];
        }
        fieldNameParts = parentAndRelationshipName.split(ParentIdLookupFieldString.NEW_FORMAT_RELATIONSHIP_NAME_SEPARATOR_CHAR);
        if (fieldNameParts.length == 2) { // format 2, interpretation 1
            relationshipName = fieldNameParts[0];
            parentObjectName = fieldNameParts[1];
        } else { // format 1
            relationshipName = parentAndRelationshipName;
        }
        initialize(parentObjectName, relationshipName, null);
    }

    private void initialize(String parentObjectName, String relationshipName, Integer numParentTypes) {
        this.parentObjectName = parentObjectName;
        this.relationshipName = relationshipName;
        this.numParentTypes = numParentTypes;
    }
    public Integer getNumParentTypes() {
        return numParentTypes;
    }

    public String getRelationshipName() {
        return relationshipName;
    }

    public String getParentObjectName() {
        return parentObjectName;
    }
    
    public String toString() {
        if (parentObjectName == null) {
            return relationshipName;
        }
        return relationshipName 
                + ParentSObjectString.NEW_FORMAT_RELATIONSHIP_NAME_SEPARATOR_CHAR
                + parentObjectName;
    }
}
