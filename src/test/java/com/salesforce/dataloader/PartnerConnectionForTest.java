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
package com.salesforce.dataloader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;

import com.salesforce.dataloader.controller.Controller;
import com.sforce.soap.partner.DeleteResult;
import com.sforce.soap.partner.GetUserInfoResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.UpsertResult;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;

/**
 * This class represents the base class for all data loader JUnit tests. TODO: ProcessScheduler test? TODO: Encryption
 * test
 * 
 * @author Lexi Viripaeff
 * @author Alex Warshavsky
 * @since 8.0
 */
public class PartnerConnectionForTest extends TestBase {
    private static Logger logger = LogManager.getLogger(PartnerConnectionForTest.class);
    private PartnerConnection binding;
    private static HashSet<String> sObjectTypesCreatedOrUpserted = new HashSet<String>();
    private static final Calendar testStartTime = Calendar.getInstance();
    private static boolean cleanedOnInitialize = false;
    
    public PartnerConnectionForTest(PartnerConnection binding) {
        this.binding = binding;
        if (!cleanedOnInitialize) {
            deleteSfdcRecords("Account", ACCOUNT_WHERE_CLAUSE, 0);
            deleteSfdcRecords("Contact", CONTACT_WHERE_CLAUSE, 0);
            deleteSfdcRecords("TestField__c", TESTFIELD_WHERE_CLAUSE, 0);
            cleanedOnInitialize = true;
        }
    }
    
    public SaveResult[] create(SObject[] sobjectArray) throws ConnectionException {
        if (sobjectArray == null) {
            return null;
        }
        synchronized(sObjectTypesCreatedOrUpserted) {     
            for (SObject sobject : sobjectArray) {
                sObjectTypesCreatedOrUpserted.add(sobject.getType());
            }
        }
        return this.binding.create(sobjectArray);
    }
    
    public UpsertResult[] upsert(String externalIdFieldName, SObject[] sobjectArray) throws ConnectionException {
        if (sobjectArray == null) {
            return null;
        }
        for (SObject sobject : sobjectArray) {
            sObjectTypesCreatedOrUpserted.add(sobject.getType());
        }
        return this.binding.upsert(externalIdFieldName, sobjectArray);
    }    
    public DeleteResult[] delete(String[] idArray) throws ConnectionException {
        return this.binding.delete(idArray);
    }
    
    public GetUserInfoResult getUserInfo() throws ConnectionException {
        return this.binding.getUserInfo();
    }
    
    public QueryResult query(String queryStr) throws ConnectionException {
        return this.binding.query(queryStr);
    }
    
    public QueryResult queryMore(String queryLocatorStr) throws ConnectionException {
        return this.binding.queryMore(queryLocatorStr);
    }
    
    public SObject[] retrieve(String fieldList, String sObjectType, String[] idArray) throws ConnectionException {
        return this.binding.retrieve(fieldList, sObjectType, idArray);
    }
    
    public void cleanup() {
        if (this.binding == null) {
            return;
        }
        synchronized(sObjectTypesCreatedOrUpserted) {
            for (String type : sObjectTypesCreatedOrUpserted) {
                deleteSfdcRecordsCreatedSinceTestStart(type);
            }
        }
    }
    
    public void deleteSfdcRecordsCreatedSinceTestStart(String entityName) {
        if (this.binding == null) {
            return;
        }
        deleteSfdcRecordsCreatedSince(entityName, testStartTime);
    }

    private void deleteSfdcRecordsCreatedSince(String entityName, Calendar calendar) {
        if (this.binding == null) {
            return;
        }
        String createdByClause = "";
        if (calendar != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            String testStartTimeFormattedString = formatter.format(calendar.getTime());
            createdByClause = "CreatedDate > " + testStartTimeFormattedString;
        }
        deleteSfdcRecords(entityName, createdByClause, 0);
    }
    /**
     * @param entityName
     * @param whereClause
     * @param retries
     */
    public void deleteSfdcRecords(String entityName, String whereClause,
            int retries) {
        if (this.binding == null) {
            return;
        }
        try {
            // query for records
            String soql = "select Id from " + entityName + " where " + whereClause;
            logger.debug("Querying " + entityName + "s to delete with soql: " + soql);
            int deletedCount = 0;
            // now delete them 200 at a time.... we should use bulk api here
            for (QueryResult qr = this.binding.query(soql); qr != null && qr.getRecords().length > 0; qr = qr.isDone() ? null
                    : this.binding.queryMore(qr.getQueryLocator())) {
                deleteSfdcRecordsFromQueryResults(qr, 0);
                deletedCount += qr.getRecords().length;
                logger.debug("Deleted " + deletedCount + " out of " + qr.getSize() + " total deleted records");
            }
            logger.info("Deleted " + deletedCount + " total objects of type " + entityName);
        } catch (ApiFault e) {
            if (checkBinding(++retries, e) != null) {
                deleteSfdcRecords(entityName, whereClause, retries);
            }
            Assert.fail("Failed to query " + entityName + "s to delete ("
                    + whereClause + "), error: " + e.getExceptionMessage());
        } catch (ConnectionException e) {
            Assert.fail("Failed to query " + entityName + "s to delete ("
                    + whereClause + "), error: " + e.getMessage());
        }
    }
    
    protected static final int SAVE_RECORD_LIMIT = 200;

    /**
     * @param qryResult
     */
    private void deleteSfdcRecordsFromQueryResults(QueryResult qryResult, int retries) {
        if (this.binding == null) {
            return;
        }
        List<String> toDeleteIds = new ArrayList<String>();
        for (int i = 0; i < qryResult.getRecords().length; i++) {
            SObject record = qryResult.getRecords()[i];
            toDeleteIds.add(record.getId());
        }
        deleteSfdcRecords(toDeleteIds.toArray(new String[] {}), 0);
    }
    
    private void deleteSfdcRecords(String[] fullArrayOfIdsToDelete, int retries) {
        if (this.binding == null) {
            return;
        }
        try {
            List<String> toDeleteIdList = new ArrayList<String>();
            for (int i = 0; i < fullArrayOfIdsToDelete.length; i++) {
                toDeleteIdList.add(fullArrayOfIdsToDelete[i]);
                // when SAVE_RECORD_LIMIT records are reached or
                // if we're on the last query result record, do the delete
                if (i > 0 && (i + 1) % SAVE_RECORD_LIMIT == 0
                        || i == fullArrayOfIdsToDelete.length - 1) {
                    DeleteResult[] delResults = this.binding.delete(
                            toDeleteIdList.toArray(new String[] {}));
                    for (int j = 0; j < delResults.length; j++) {
                        DeleteResult delResult = delResults[j];
                        if (!delResult.getSuccess()) {
                            logger.warn("Delete returned an error: " + delResult.getErrors()[0].getMessage(),
                                    new RuntimeException());
                        }
                    }
                    toDeleteIdList.clear();
                }
            }
        } catch (ApiFault e) {
            if (checkBinding(++retries, e) != null) {
                deleteSfdcRecords(fullArrayOfIdsToDelete, retries);
            }
            Assert.fail("Failed to delete records, error: " + e.getExceptionMessage());
        } catch (ConnectionException e) {
            Assert.fail("Failed to delete records, error: " + e.getMessage());
        }
    }
}
