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

import org.apache.commons.beanutils.ConvertUtils;

import com.salesforce.dataloader.client.DescribeRefObject;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.sobject.SObject;

/**
 * Class that converts string-based object references (e.g., used for upsert on relationships) to instances
 *
 * @author Alex Warshavsky
 * @since 8.0
 */
public class SObjectReference {

    private final Object referenceExtIdValue;

    /**
     * @param refValue
     */
    public SObjectReference(Object refValue) {
        if (refValue != null && String.valueOf(refValue).length() == 0) refValue = null;
        this.referenceExtIdValue = refValue;
    }

    /**
     * @param controller
     * @param sObj
     * @param refFieldName
     * @throws ParameterLoadException
     */
    public void addReferenceToSObject(Controller controller, SObject sObj, String refFieldName) throws ParameterLoadException {
        // break the name into relationship and field name components
        ObjectField refField = new ObjectField(refFieldName);
        String relationshipName = refField.getRelationshipName();
        String parentFieldName = refField.getParentFieldName();

        // get object info for the given reference (foreign key) relationship
        DescribeRefObject entityRefInfo = controller.getReferenceDescribes().get(relationshipName);

        // build the reference SObject
        SObject sObjRef = new SObject();
        // set entity type, has to be set before all others
        sObjRef.setType(entityRefInfo.getParentObjectName());
        // set external id, do type conversion as well
        Class<?> typeClass = SforceDynaBean.getConverterClass(entityRefInfo.getParentObjectFieldMap().get(parentFieldName));
        Object extIdValue = ConvertUtils.convert(this.referenceExtIdValue.toString(), typeClass);
        sObjRef.setField(parentFieldName, extIdValue);
        // Add the sObject reference as a child elemetn, name set to relationshipName
        sObj.addField(relationshipName, sObjRef);
    }

    @Override
    public String toString() {
        return referenceExtIdValue == null ? "" : referenceExtIdValue.toString();
    }

    @Override
    public int hashCode() {
        return referenceExtIdValue == null ? "".hashCode() : referenceExtIdValue.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SObjectReference
                && (this.referenceExtIdValue == o || (this.referenceExtIdValue != null && this.referenceExtIdValue
                .equals(((SObjectReference)o).referenceExtIdValue)));
    }

    public Object getReferenceExtIdValue() {
        return referenceExtIdValue;
    }

    public static String getRelationshipField(Controller controller, String refFieldName) {
        final String relName = new ObjectField(refFieldName).getRelationshipName();
        controller.getReferenceDescribes().get(relName).getParentObjectFieldMap();
        for (Field f : controller.getFieldTypes().getFields()) {
            if (f != null) {
                if (relName.equals(f.getRelationshipName())) { return f.getName(); }
            }
        }
        return null;
    }

    public boolean isNull() {
        return this.referenceExtIdValue == null;
    }
}
