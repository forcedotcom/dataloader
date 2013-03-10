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

package com.salesforce.dataloader.action.visitor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataWriter;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.model.Row;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;

/**
 * Visitor to convert rows into Dynamic objects
 * 
 * @author Lexi Viripaeff
 * @author Alex Warshavsky
 * @since 6.0
 */
public class PartnerQueryVisitor extends AbstractQueryVisitor {

    private QueryResult qr;

    public PartnerQueryVisitor(Controller controller, ILoaderProgress monitor, DataWriter queryWriter,
            DataWriter successWriter, DataWriter errorWriter) {
        super(controller, monitor, queryWriter, successWriter, errorWriter);
    }

    @Override
    protected int executeQuery(String soql) throws ConnectionException {
        this.qr = getQueryResult(soql);
        return this.qr.getSize();
    }

    protected QueryResult getQueryResult(String soql) throws ConnectionException {
        return getController().getPartnerClient().query(soql);
    }

    @Override
    protected void writeExtraction() throws DataAccessObjectException, ConnectionException {
        while (this.qr.getRecords() != null) {
            // form a map, because we aren't guaranteed to get back all the fields
            final SObject[] sfdcResults = this.qr.getRecords();
            if (sfdcResults == null) {
                getLogger().error(Messages.getMessage(getClass(), "errorNoResults"));
                return;
            }
            for (int i = 0; i < sfdcResults.length; i++) {
                // add row to batch
                addResultRow(getDaoRow(sfdcResults[i]), sfdcResults[i].getId());
            }
            if (this.qr.getDone()) {
                break;
            }
            if (getProgressMonitor().isCanceled()) return;
            this.qr = getController().getPartnerClient().queryMore(this.qr.getQueryLocator());
        }
    }

    private Row getDaoRow(SObject sob) {
        Row row = getMapper().mapPartnerSObjectSfdcToLocal(sob);
        for (Map.Entry<String, Object> ent : row.entrySet()) {
            Object newVal = convertFieldValue(ent.getValue());
            if (newVal != ent.getValue()) row.put(ent.getKey(), newVal);
        }
        return row;
    }

    private static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private Object convertFieldValue(Object fieldVal) {
        if (fieldVal instanceof Calendar) {
            DF.setCalendar((Calendar)fieldVal);
            return DF.format(((Calendar)fieldVal).getTime());
        }

        if (fieldVal instanceof Date) {
            final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            return df.format((Date)fieldVal);
        }

        return fieldVal;
    }

}
