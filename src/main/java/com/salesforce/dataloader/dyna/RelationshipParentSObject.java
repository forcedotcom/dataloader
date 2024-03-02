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
public class RelationshipParentSObject {
    private String relationshipName;
    private String parentObjectName = null;
    private int numParentTypes = 1;
    private static final Logger logger = LogManager.getLogger(RelationshipParentSObject.class);

    public static final String NEW_FORMAT_PARENT_IDLOOKUP_FIELD_SEPARATOR_CHAR = "-";
    public static final String NEW_FORMAT_RELATIONSHIP_NAME_SEPARATOR_CHAR = ":";
  
    public RelationshipParentSObject(String parentObjectName, String relationshipName, int numParentTypes) {
        this.parentObjectName = parentObjectName;
        this.relationshipName = relationshipName;
        this.numParentTypes = numParentTypes;
    }
    
    public int getNumParentTypes() {
        return numParentTypes;
    }

    public String getRelationshipName() {
        return relationshipName;
    }

    public String getParentObjectName() {
        return parentObjectName;
    }
    
    public String toFormattedRelationshipString() {
        if (parentObjectName == null) {
            return relationshipName;
        }
        return relationshipName 
                + RelationshipParentSObject.NEW_FORMAT_RELATIONSHIP_NAME_SEPARATOR_CHAR
                + parentObjectName;
    }
}
