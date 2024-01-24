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


/**
 * Container for an object field of format
 *     objectName:fieldName
 * 
 * @author Alex Warshavsky
 * @since 8.0
 */
public class ObjectField {
    private String relationshipName;
    private String parentFieldName;
    private String parentObjectName = null;
    public static final String VALUE_SEPARATOR_CHAR = ":"; //$NON-NLS-1$
    // old format - <relationship name attribute of relationship field>:<idLookup field of parent sObject>
    // Example - "Owner:username" where Account.Owner field is a lookup 
    // field to User object, username is an idLookup field in User
    //
    // new format to support polymorphic lookup relationship - 
    //      <parent object name>:<relationship name attribute of relationship field>.<idLookup field of parent sObject>
    // Example - "Account:Owner.username"
    public static final String NEW_FORMAT_VALUE_SEPARATOR_CHAR = ".";
    public static final String NEW_FORMAT_OBJECT_NAME_SEPARATOR_CHAR = ":";

    /**
     * @param objectField
     */
    public ObjectField(String objectField) {
        String[] refFieldNameInfo = objectField.split(ObjectField.NEW_FORMAT_VALUE_SEPARATOR_CHAR);
        if (refFieldNameInfo.length < 2) {
            refFieldNameInfo = objectField.split(ObjectField.VALUE_SEPARATOR_CHAR);
            relationshipName = refFieldNameInfo[0];
            parentFieldName = refFieldNameInfo[1];  
        } else { // new format
            parentFieldName = refFieldNameInfo[1];
            refFieldNameInfo = objectField.split(ObjectField.NEW_FORMAT_OBJECT_NAME_SEPARATOR_CHAR);
            relationshipName = refFieldNameInfo[1];
            parentObjectName = refFieldNameInfo[0];
        }
    }

    public String getParentFieldName() {
        return parentFieldName;
    }

    public String getRelationshipName() {
        return relationshipName;
    }

    public String getParentObjectName() {
        return parentObjectName;
    }
    
    /**
     * @param objectName
     * @param fieldName
     * @return String formatted as objectName:fieldName
     */
    static public String formatAsString(String objectName, String fieldName) {
        return objectName + ObjectField.VALUE_SEPARATOR_CHAR + fieldName;
    }

    static public String formatAsString(String parentObjectName, String objectName, String fieldName) {
        return parentObjectName 
                + ObjectField.NEW_FORMAT_OBJECT_NAME_SEPARATOR_CHAR 
                + objectName 
                + ObjectField.NEW_FORMAT_VALUE_SEPARATOR_CHAR 
                + fieldName;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (parentObjectName == null) {
            return formatAsString(relationshipName, parentFieldName);
        } else {
            return formatAsString(parentObjectName, relationshipName, parentFieldName);
        }
    }
}
