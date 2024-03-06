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

import com.salesforce.dataloader.exception.RelationshipFormatException;


public class ParentIdLookupFieldFormatter {
    private String parentFieldName = null;
    private ParentSObjectFormatter parentSObjectFormatter = null;
    private static final Logger logger = LogManager.getLogger(ParentIdLookupFieldFormatter.class);

    private static final String OLD_FORMAT_PARENT_IDLOOKUP_FIELD_SEPARATOR_CHAR = ":"; //$NON-NLS-1$
    // old format - <relationship name attribute of relationship field>:<idLookup field of parent sObject>
    // Example - "Owner:username" where Account.Owner field is a lookup 
    // field to User object, username is an idLookup field in User
    //
    // new format to support polymorphic lookup relationship - 
    //      <parent object name>:<relationship name attribute of relationship field>.<idLookup field of parent sObject>
    // Example - "Account:Owner.username"
    public static final String NEW_FORMAT_PARENT_IDLOOKUP_FIELD_SEPARATOR_CHAR = "-";
    
    // fieldName param can be in one of the following formats:
    // format 1: alphanumeric string without any ':' or '-' in it. Represents name of child's non-polymorphic relationship field
    // format 1 => it is name of a non-polymorphic relationship field in child object.
    //
    // format 2: alphanumeric string with a ':' in it
    // format 2 has 2 (legacy format): 
    //    <child relationship field name>:<parent idlookup field name>
    //
    // format 3: alphanumeric string with a single ':' and a single '-' in it
    // format 3 => it is name of a field in child object with reference to an idlookup field in parent object
    //
    // Given 2 interpretations of format 2, an additional parameter, 'isFieldName', is required.
    // If 'hasParentIdLookupFieldName' == true, the code processes fieldName parameter according 
    // to the 2nd interpretation for format 2. It processes fieldName parameter according to 1st interpretation otherwise.

    public ParentIdLookupFieldFormatter(String formattedFieldName) throws RelationshipFormatException {
        if (formattedFieldName == null || formattedFieldName.isBlank()) {
            throw new RelationshipFormatException("parent idLookup field name not specified");
        }
        String[] fieldNameParts = formattedFieldName.split(ParentIdLookupFieldFormatter.NEW_FORMAT_PARENT_IDLOOKUP_FIELD_SEPARATOR_CHAR);
        boolean hasParentIdLookupFieldName = false;
        if (fieldNameParts.length == 2) {
            // parent name not specified
            parentFieldName = fieldNameParts[1];
            hasParentIdLookupFieldName = true; // '.' char shows up only in format 3
            formattedFieldName = fieldNameParts[0];
        }
        fieldNameParts = formattedFieldName.split(ParentSObjectFormatter.NEW_FORMAT_RELATIONSHIP_NAME_SEPARATOR_CHAR);
        String relationshipName = null;
        String parentObjectName = null;
        if (hasParentIdLookupFieldName) { // format 3
            if (fieldNameParts.length == 2) {
                relationshipName = fieldNameParts[0];
                parentObjectName = fieldNameParts[1];
            } else { // Should not happen - no ':' char in name, may have '-' char
                if (parentFieldName == null) { // no ':' and no '.' in name
                    String errorStr = "field name " + formattedFieldName + " does not have ':' or '-' char";
                    logger.error(errorStr);
                    throw new RelationshipFormatException(errorStr);
                } else {
                    // '-' char in name but no ':'
                    String errorStr = "field name " + formattedFieldName + " has '-' but does not have ':' char";
                    logger.error(errorStr);
                    throw new RelationshipFormatException(errorStr);
                }
            }
        } else { // format 1 or format 2
            if (fieldNameParts.length == 2) { // format 2
                relationshipName = fieldNameParts[0];
                parentFieldName = fieldNameParts[1];
            } else { // format 1
                relationshipName = formattedFieldName;
            }
        }
        parentSObjectFormatter = new ParentSObjectFormatter(parentObjectName, relationshipName);
    }
    
    public ParentIdLookupFieldFormatter(String parentObjectName, String relationshipName, String parentIdLookupFieldName) throws RelationshipFormatException {
        parentSObjectFormatter = new ParentSObjectFormatter(parentObjectName, relationshipName);
        this.parentFieldName = parentIdLookupFieldName;
    }

    public String getParentFieldName() {
        return parentFieldName;
    }
    
    public ParentSObjectFormatter getParent() {
        return parentSObjectFormatter;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String separator = ParentIdLookupFieldFormatter.NEW_FORMAT_PARENT_IDLOOKUP_FIELD_SEPARATOR_CHAR;
        if (parentSObjectFormatter.getParentObjectName() == null) {
            separator = ParentIdLookupFieldFormatter.OLD_FORMAT_PARENT_IDLOOKUP_FIELD_SEPARATOR_CHAR;
        }
        return parentSObjectFormatter.toString() + separator + parentFieldName;
    }
}
