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

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import com.salesforce.dataloader.model.Row;
import com.salesforce.dataloader.util.DateOnlyCalendar;

import org.apache.commons.beanutils.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.salesforce.dataloader.action.visitor.DAOLoadVisitor;
import com.salesforce.dataloader.client.DescribeRefObject;
import com.salesforce.dataloader.client.SObject4JSON;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.LoadException;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.exception.RelationshipFormatException;
import com.sforce.soap.partner.*;
import com.sforce.soap.partner.sobject.SObject;

/**
 * Salesforce DynaBean utilities
 *
 * @author Alex Warshavsky
 * @since 8.0
 */
public class SforceDynaBean {

    //logger
    public static Logger logger = LogManager.getLogger(DAOLoadVisitor.class);

    /**
     *
     */
    public SforceDynaBean() {
        super();
    }

    /**
     * Helper function for building type mapping for an SObject
     *
     * @param describer
     * @param controller
     * @return DynaProperty array - used to create the DynaBean
     */
    static public DynaProperty[] createDynaProps(DescribeSObjectResult describer, Controller controller) {

        Field[] fields = describer.getFields();
        ArrayList<DynaProperty> dynaProps = new ArrayList<DynaProperty>();

        for (Field field : fields) {
            String fieldName = field.getName();
            //see which class type equals the field type
            Class<?> classType = getConverterClass(field);
            dynaProps.add(new DynaProperty(fieldName, classType));

            // if field is a reference to another object, remember the reference
            // NOTE: currently only fields with one reference are supported on the server
            FieldType fieldType = field.getType();
            String relationshipName = field.getRelationshipName();
            if (fieldType == FieldType.reference &&
                    relationshipName != null && relationshipName.length() > 0) {
                for (String parentName : field.getReferenceTo()) {
                    ParentSObjectFormatter parentHandleForRelationship;
                    try {
                        parentHandleForRelationship = new ParentSObjectFormatter(parentName, relationshipName);
                    } catch (RelationshipFormatException e) {
                        logger.error(e.getMessage());
                        continue;
                    }
                    DescribeRefObject parent = controller.getReferenceDescribes().getParentSObject(parentHandleForRelationship.toString());
                    if(parent != null) {
                        for(String refFieldName : parent.getParentObjectFieldMap().keySet()) {
                            // property name contains information for mapping
                            // add old format to dyna props
                            try {
                                dynaProps.add(new DynaProperty(
                                                new ParentIdLookupFieldFormatter(null, relationshipName, refFieldName).toString(),
                                                SObjectReference.class));
                                dynaProps.add(new DynaProperty(
                                        new ParentIdLookupFieldFormatter(parent.getParentObjectName(), relationshipName, refFieldName).toString(),
                                        SObjectReference.class));
                             } catch (RelationshipFormatException e) {
                                // TODO Auto-generated catch block
                               logger.error(e.getMessage());
                            }
                            // add new format to dyna props
                       }
                    }
                }
            }
        }

        return dynaProps.toArray(new DynaProperty[dynaProps.size()]);
    }

    /**
     * @param field
     * @return class to perform conversion
     */
    public static Class<?> getTypeClass(Field field) {
        Class<?> classType;
        SoapType soapType = field.getSoapType();

        switch(soapType) {
        case dateTime:
            classType = Calendar.class;
            break;
        case ID:
        case string:
            classType = String.class;
            break;
        case _double:
            if(field.isExternalId()){
                classType = Double.class;
            }
            else{
                classType = String.class;
            }
            break;
        case _int:
            classType = Integer.class;
            break;
        case _boolean:
            classType = Boolean.class;
            break;
        case date:
            classType = Date.class;
            break;
        case base64Binary:
            classType = byte[].class;
            break;
        case anyType:
        default:
            classType = String.class;
        }
        return classType;
    }

    /**
     * @param field
     * @return class to perform conversion
     */
    public static Class<?> getConverterClass(Field field) {
        Class<?> classType;
        SoapType soapType = field.getSoapType();

        switch(soapType) {
        case dateTime:
            classType = Calendar.class;
            break;
        case ID:
        case string:
            classType = String.class;
            break;
        case _double:
            if(field.isExternalId()){
                classType = Double.class;
            }
            else{
                classType = String.class;
            }
            break;
        case _int:
            classType = Integer.class;
            break;
        case _boolean:
            classType = Boolean.class;
            break;
        case date:
            classType = DateOnlyCalendar.class;
            break;
        case base64Binary:
            classType = byte[].class;
            break;
        case anyType:
        default:
            classType = String.class;
        }
        return classType;
    }

    /**
     * @param dynaProps
     * @return an instance of dynabean created using dynaProps
     */
    static public BasicDynaClass getDynaBeanInstance(DynaProperty[] dynaProps) {
        return new BasicDynaClass("sobject", null, dynaProps);
    }

    /**
     * @param dynaClass
     * @param sforceDataRow
     * @return Instance of dynabean for the given sforceData instance
     * @throws ConversionException
     * @throws LoadException
     */
    static public DynaBean convertToDynaBean(BasicDynaClass dynaClass, Row sforceDataRow)
            throws ConversionException, LoadException {
        //now convert the data types, through our strongly typed bean
        DynaBean sforceObj = null;
        try {
            sforceObj = dynaClass.newInstance();
            //This does an automatic conversion of types.
            BeanUtils.copyProperties(sforceObj, sforceDataRow);
            for (String sforceField : sforceDataRow.keySet()) {
                Object val = sforceDataRow.get(sforceField);
                if (val != null
                        && val instanceof String
                        && !((String)val).isBlank()
                        && sforceObj.get(sforceField) == null) {
                    String errStr = "unable to convert a non-null " + sforceField + "value " + (String)val + " to a field on entity " + AppConfig.getCurrentConfig().getString(AppConfig.ENTITY);
                    logger.error(errStr); //$NON-NLS-1$
                    throw new LoadException(errStr);
                }
            }
            return sforceObj;
        } catch (IllegalAccessException e1) {

            logger.error(Messages.getString("Visitor.dynaBeanError"), e1); //$NON-NLS-1$
            throw new LoadException(e1);
        } catch (InstantiationException e1) {
            logger.fatal(Messages.getString("Visitor.dynaBeanError"), e1); //$NON-NLS-1$
            throw new LoadException(e1);
        } catch (InvocationTargetException e) {
            logger.error(Messages.getString("Visitor.invocationError"), e); //$NON-NLS-1$
            throw new LoadException(e);
        }
    }

    /**
     * Set all the fields specified in the dynaBean to null on the sObj
     * 
     * @param sObj
     * @param dynaBean
     */
    static public void insertNullArrayForSOAP(Controller controller, SObject sObj, DynaBean dynaBean) {
        final List<String> fieldsToNull = getFieldsToNull(controller, dynaBean);
        if (fieldsToNull.size() > 0) sObj.setFieldsToNull(fieldsToNull.toArray(new String[fieldsToNull.size()]));
    }
    
    static public void insertNullArrayForREST(Controller controller, Map<String, Object> sObj, DynaBean dynaBean) {
        final List<String> fieldsToNull = getFieldsToNull(controller, dynaBean);
        for (String field : fieldsToNull) {
            sObj.put(field, null);
        }
    }
    
    static public List<String> getFieldsToNull(Controller controller, DynaBean dynaBean) {
        final List<String> fieldsToNull = new ArrayList<String>();
        for (String sfdcField : controller.getMapper().getDestColumns()) {
            handleNull(sfdcField, dynaBean, fieldsToNull, controller);
        }
        for (Map.Entry<String, String> constantEntry : controller.getMapper().getConstantsMap().entrySet()) {
            handleNull(constantEntry.getKey(), dynaBean, fieldsToNull, controller);
        }
        return fieldsToNull;
    }
    
    private static void handleNull(final String fieldNameList, final DynaBean dynaBean, final List<String> fieldsToNull,
            final Controller controller) {
        // fieldNameList can be a list of comma separated fields
        String[]fieldNameArray = fieldNameList.split(",");

        for (String fieldName : fieldNameArray) {
            final Object o = dynaBean.get(fieldName.strip());
            if (o != null && o instanceof SObjectReference && ((SObjectReference)o).isNull())
                fieldsToNull.add(SObjectReference.getRelationshipField(controller, fieldName));
            else if (o == null || String.valueOf(o).length() == 0) fieldsToNull.add(fieldName);
        }
    }

    /**
     * @param dynaBeans
     * @return SObject array with data from dynaBeans
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws ParameterLoadException
     */
    static public SObject[] getSObjectArray(Controller controller, List<DynaBean> dynaBeans, String entityName, boolean insertNulls) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ParameterLoadException {
        SObject[] sObjects = new SObject[dynaBeans.size()];

        for (int j = 0; j < sObjects.length; j++) {
            DynaBean dynaBean = dynaBeans.get(j);

            SObject sObj = getSOAPSObject(controller, entityName, dynaBean);

            // if we are inserting nulls, build the null array
            if (insertNulls) {
                insertNullArrayForSOAP(controller, sObj, dynaBean);
            }

            sObjects[j] = sObj;
        }
        return sObjects;
    }
    
    static public List<Map<String, Object>> getRESTSObjectArray(Controller controller, List<DynaBean> dynaBeans, String entityName, boolean insertNulls) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ParameterLoadException {
        List<Map<String, Object>> restSObjects = new ArrayList <Map<String, Object>>();

        for (int j = 0; j < dynaBeans.size(); j++) {
            DynaBean dynaBean = dynaBeans.get(j);

            Map<String, Object> sObj = getCompositeRESTSObject(controller, entityName, dynaBean);

            // if we are inserting nulls, build the null array
            if (insertNulls) {
                insertNullArrayForREST(controller, sObj, dynaBean);
            }

            restSObjects.add(sObj);
        }
        return restSObjects;
    }

    /**
     * @param entityName
     * @param dynaBean
     * @return SObject of type entityName with data coming from dynaBean
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws ParameterLoadException
     */
    public static SObject getSOAPSObject(Controller controller, String entityName, DynaBean dynaBean) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ParameterLoadException {
        SObject sObj = new SObject();
        sObj.setType(entityName);
        Map<String, String> fieldMap = BeanUtils.describe(dynaBean);
        for (String fName : fieldMap.keySet()) {
            if (fieldMap.get(fName) != null) {
                // see if any entity foreign key references are embedded here
                Object value = dynaBean.get(fName);
                if (value instanceof SObjectReference) {
                    SObjectReference sObjRef = (SObjectReference)value;
                    if (!sObjRef.isNull()) sObjRef.addReferenceToSObject(controller, sObj, null, fName);
                } else {
                    sObj.setField(fName, value);
                }
            }
        }
        return sObj;
    }
    
    public static Map<String, Object> getCompositeRESTSObject(Controller controller, String entityName, DynaBean dynaBean) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ParameterLoadException {
        SObject4JSON restSObj = new SObject4JSON(entityName);
        Map<String, String> fieldMap = BeanUtils.describe(dynaBean);
        for (String fName : fieldMap.keySet()) {
            if (fieldMap.get(fName) != null) {
                // see if any entity foreign key references are embedded here
                Object value = dynaBean.get(fName);
                if (value instanceof SObjectReference) {
                    try {
                        ParentIdLookupFieldFormatter idLookupFieldFormatter = new ParentIdLookupFieldFormatter(fName);
                        DescribeSObjectResult parentSObjectDescribe = controller.getPartnerClient().describeSObject(idLookupFieldFormatter.getParent().getParentObjectName());
                        DynaProperty[] parentDynaProps = createDynaProps(parentSObjectDescribe, controller);
                        BasicDynaClass parentDynaClass = getDynaBeanInstance(parentDynaProps);
                        Row parentDataRow = new Row();
                        parentDataRow.put(idLookupFieldFormatter.getParentFieldName(), value);
                        DynaBean parentDynaBean = convertToDynaBean(parentDynaClass, parentDataRow);
                        Map<String, Object> parentRESTSObject = getCompositeRESTSObject(controller, idLookupFieldFormatter.getParent().getParentObjectName(), parentDynaBean);
                        SObjectReference sObjRef = (SObjectReference)value;
                        if (!sObjRef.isNull()) {
                            restSObj.setField(idLookupFieldFormatter.getParent().getRelationshipName(),
                                    parentRESTSObject);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.error("Unable to convert " + fName + " to parent reference");
                    }
                } else {
                    restSObj.setField(fName, value);
                }
            }
        }
        return restSObj.getRepresentationForCompositeREST();
    }

    /**
     * Register dynabean data type converters for common java data types
     * @param useEuroDates if true, european date format will be used
     */
    synchronized static public void registerConverters(AppConfig cfg) {
        final boolean useEuroDates = cfg.getBoolean(AppConfig.EURO_DATES);
        final TimeZone tz = cfg.getTimeZone();
        // Register DynaBean type conversions
        ConvertUtils.register(new DateTimeConverter(tz, useEuroDates), Calendar.class);
        ConvertUtils.register(new DateOnlyConverter(tz, useEuroDates), DateOnlyCalendar.class);
        ConvertUtils.register(new DoubleConverter(), Double.class);
        ConvertUtils.register(new IntegerConverter(), Integer.class);
        ConvertUtils.register(new BooleanConverter(), Boolean.class);
        ConvertUtils.register(new StringConverter(), String.class);
        ConvertUtils.register(new FileByteArrayConverter(), byte[].class);
        ConvertUtils.register(new SObjectReferenceConverter(), SObjectReference.class);
    }

}
