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

import java.util.*;

import org.apache.commons.beanutils.*;

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataReader;
import com.salesforce.dataloader.dao.DataWriter;
import com.salesforce.dataloader.dyna.SforceDynaBean;
import com.salesforce.dataloader.exception.*;
import com.salesforce.dataloader.mapping.LoadMapper;
import com.sforce.async.AsyncApiException;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.ws.ConnectionException;

/**
 * Visitor to convert rows into Dynamic objects
 *
 * @author Lexi Viripaeff
 * @since 6.0
 */
public abstract class DAOLoadVisitor extends AbstractVisitor implements DAORowVisitor {

    protected final List<String> columnNames;

    // this stores the dynabeans, which convert types correctly
    protected final List<DynaBean> dynaArray;
    protected final List<Map<String, Object>> dataArray;

    protected final BasicDynaClass dynaClass;
    protected final DynaProperty[] dynaProps;

    private final int batchSize;

    protected DAOLoadVisitor(Controller controller, ILoaderProgress monitor, DataWriter successWriter,
            DataWriter errorWriter) {
        super(controller, monitor, successWriter, errorWriter);

        this.columnNames = ((DataReader)controller.getDao()).getColumnNames();

        dynaArray = new LinkedList<DynaBean>();
        dataArray = new LinkedList<Map<String, Object>>();

        SforceDynaBean.registerConverters(getConfig());

        dynaProps = SforceDynaBean.createDynaProps(controller.getFieldTypes(), controller);
        dynaClass = SforceDynaBean.getDynaBeanInstance(dynaProps);

        this.batchSize = getConfig().getLoadBatchSize();
    }

    @Override
    public final void visit(Map<String, Object> row) throws OperationException, DataAccessObjectException,
    ConnectionException {
        initLoadRateCalculator();
        // the result are sforce fields mapped to data
        Map<String, Object> sforceDataRow = getMapper().mapData(row);
        try {
            convertBulkAPINulls(sforceDataRow);
            dynaArray.add(SforceDynaBean.convertToDynaBean(dynaClass, sforceDataRow));
        } catch (ConversionException conve) {
            String errMsg = Messages.getMessage("Visitor", "conversionErrorMsg", conve.getMessage());
            getLogger().error(errMsg, conve);

            conversionFailed(row, errMsg);
            // this row cannot be added since conversion has failed
            return;
        }

        // add the data for writing to the result files
        // must do this after conversion.
        dataArray.add(row);
        // load the batch
        if (dynaArray.size() >= this.batchSize) {
            loadBatch();
        }
    }

    protected void conversionFailed(Map<String, Object> row, String errMsg) throws DataAccessObjectException,
    LoadException, OperationException {
        writeError(row, errMsg);
    }

    protected void convertBulkAPINulls(Map<String, Object> row) {}

    public void flushRemaining() throws OperationException, DataAccessObjectException {
        // check if there are any entities left
        if (dynaArray.size() > 0) {
            loadBatch();
        }
    }

    protected abstract void loadBatch() throws DataAccessObjectException, OperationException;

    public void clearArrays() {
        // clear the arrays
        dataArray.clear();
        dynaArray.clear();
    }

    protected void handleException(String msgOverride, Throwable t) throws LoadException {
        String msg = msgOverride;
        if (msg == null) {
            msg = t.getMessage();
            if (t instanceof AsyncApiException) {
                msg = ((AsyncApiException)t).getExceptionMessage();
            } else if (t instanceof ApiFault) {
                msg = ((ApiFault)t).getExceptionMessage();
            }
        }
        throw new LoadException(msg, t);
    }

    protected void handleException(Throwable t) throws LoadException {
        handleException(null, t);
    }

    @Override
    protected boolean writeStatus() {
        return true;
    }

    private void initLoadRateCalculator() throws DataAccessObjectException {
        getRateCalculator().setNumRecords(((DataReader)getController().getDao()).getTotalRows());
        getRateCalculator().start();
    }

    @Override
    protected LoadMapper getMapper() {
        return (LoadMapper)super.getMapper();
    }

}
