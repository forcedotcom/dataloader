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


public class IdLookupHandleForRelationship {
    private String parentFieldName = null;
    private ParentObjectHandleForRelationship parent = null;
    private static final Logger logger = LogManager.getLogger(IdLookupHandleForRelationship.class);

    private static final String OLD_FORMAT_PARENT_IDLOOKUP_FIELD_SEPARATOR_CHAR = ":"; //$NON-NLS-1$
    // old format - <relationship name attribute of relationship field>:<idLookup field of parent sObject>
    // Example - "Owner:username" where Account.Owner field is a lookup 
    // field to User object, username is an idLookup field in User
    //
    // new format to support polymorphic lookup relationship - 
    //      <parent object name>:<relationship name attribute of relationship field>.<idLookup field of parent sObject>
    // Example - "Account:Owner.username"
    public static final String NEW_FORMAT_PARENT_IDLOOKUP_FIELD_SEPARATOR_CHAR = "-";
    public static final String NEW_FORMAT_RELATIONSHIP_NAME_SEPARATOR_CHAR = ":";
    
    // fieldName param can be in one of the following formats:
    // format 1: alphanumeric string without any ':' or '-' in it. Represents name of child's non-polymorphic relationship field
    // format 1 => it is name of a non-polymorphic relationship field in child object.
    //
    // format 2: alphanumeric string with a ':' in it
    // format 2 has 2 interpretations:
    //   interpretation 1: <child relationship field name>:<parent sobject name>
    //      - this is the new format for keys of the hashmap referenceEntitiesDescribeMap
    //   interpretation 2 (legacy format): <child relationship field name>:<parent idlookup field name>
    //
    // format 3: alphanumeric string with a single ':' and a single '-' in it
    // format 3 => it is name of a field in child object with reference to an idlookup field in parent object
    //
    // Given 2 interpretations of format 2, an additional parameter, 'isFieldName', is required.
    // If 'hasParentIdLookupFieldName' == true, the code processes fieldName parameter according 
    // to the 2nd interpretation for format 2. It processes fieldName parameter according to 1st interpretation otherwise.

    public IdLookupHandleForRelationship(String fieldName, boolean hasParentIdLookupFieldName) {
        String[] fieldNameParts = fieldName.split(IdLookupHandleForRelationship.NEW_FORMAT_PARENT_IDLOOKUP_FIELD_SEPARATOR_CHAR);
        if (fieldNameParts.length == 2) {
            // parent name not specified
            parentFieldName = fieldNameParts[1];
            hasParentIdLookupFieldName = true; // '.' char shows up only in format 3
            fieldName = fieldNameParts[0];
        }
        fieldNameParts = fieldName.split(IdLookupHandleForRelationship.NEW_FORMAT_RELATIONSHIP_NAME_SEPARATOR_CHAR);
        String relationshipName = null;
        String parentObjectName = null;
        if (hasParentIdLookupFieldName) { // format 2, interpretation 2 or format 3
            if (fieldNameParts.length == 2) {
                if (parentFieldName == null) {// format 2, interpretation 2
                    relationshipName = fieldNameParts[0];
                    parentFieldName = fieldNameParts[1];
                } else { // format 3
                    relationshipName = fieldNameParts[0];
                    parentObjectName = fieldNameParts[1];
                }
            } else { // Should not happen - no ':' char in name, may have '-' char
                if (parentFieldName == null) { // no ':' and no '.' in name
                    logger.error("field name " + fieldName + " does not have ':' or '-' char" );
                } else {
                    // '-' char in name but no ':'
                    logger.error("field name " + fieldName + " has '-' but does not have ':' char" );
                }
            }
        } else { // format 1 or format 2, interpretation 1
            if (fieldNameParts.length == 2) { // format 2, interpretation 1
                relationshipName = fieldNameParts[0];
                parentObjectName = fieldNameParts[1];
            } else { // format 1
                relationshipName = fieldName;
            }
        }
        parent = new ParentObjectHandleForRelationship(parentObjectName, relationshipName, null);
    }

    public String getParentFieldName() {
        return parentFieldName;
    }
    
    public ParentObjectHandleForRelationship getParent() {
        return parent;
    }
    
    public void setParentFieldName(String parentIdLookupFieldName) {
        this.parentFieldName = parentIdLookupFieldName;
    }
    
    public String toFormattedRelationshipString() {
        if (parent.getParentObjectName() == null) {
            return parent.getRelationshipName();
        }
        return parent.getRelationshipName() 
                + IdLookupHandleForRelationship.NEW_FORMAT_RELATIONSHIP_NAME_SEPARATOR_CHAR
                + parent.getParentObjectName();
    }
    
    public boolean isRelationshipName(String nameStr) {
        if (parent.getRelationshipName() == null) {
            return false;
        }
        if (parent.getParentObjectName() == null) {
            return nameStr.toLowerCase().startsWith(parent.getRelationshipName().toLowerCase());
        } else {
            return nameStr.toLowerCase().equalsIgnoreCase(parent.getRelationshipName() + NEW_FORMAT_RELATIONSHIP_NAME_SEPARATOR_CHAR + parent.getParentObjectName());
        }
    }
    
    /**
     * @param objectName
     * @param fieldName
     * @return String formatted as objectName:fieldName
     */
    static public String formatAsString(String relationshipName, String parentIDLookupFieldName) {
        return relationshipName + IdLookupHandleForRelationship.OLD_FORMAT_PARENT_IDLOOKUP_FIELD_SEPARATOR_CHAR + parentIDLookupFieldName;
    }

    static public String formatAsString(String parentObjectName, String relationshipName, String parentIDLookupFieldName) {
        return relationshipName 
                + IdLookupHandleForRelationship.NEW_FORMAT_RELATIONSHIP_NAME_SEPARATOR_CHAR 
                + parentObjectName 
                + IdLookupHandleForRelationship.NEW_FORMAT_PARENT_IDLOOKUP_FIELD_SEPARATOR_CHAR 
                + parentIDLookupFieldName;
    }
    
    static public boolean isRelationshipFieldMapping(String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            return false;
        }
        if (fieldName.contains(NEW_FORMAT_PARENT_IDLOOKUP_FIELD_SEPARATOR_CHAR)) {
            if (fieldName.contains(NEW_FORMAT_RELATIONSHIP_NAME_SEPARATOR_CHAR)) {
                return true;
            }
        } else if (fieldName.contains(OLD_FORMAT_PARENT_IDLOOKUP_FIELD_SEPARATOR_CHAR)) {
            return true;
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (parent.getParentObjectName() == null) {
            return formatAsString(parent.getRelationshipName(), parentFieldName);
        } else {
            return formatAsString(parent.getParentObjectName(), parent.getRelationshipName(), parentFieldName);
        }
    }
}
