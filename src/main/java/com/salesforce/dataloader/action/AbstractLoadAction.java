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

package com.salesforce.dataloader.action;

import java.util.List;
import java.util.Map;

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.action.visitor.DAOLoadVisitor;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.*;
import com.salesforce.dataloader.exception.*;
import com.salesforce.dataloader.mapping.LoadMapper;
import com.salesforce.dataloader.util.DAORowUtil;
import com.sforce.ws.ConnectionException;

/**
 * @author Lexi Viripaeff
 * @since 6.0
 */
abstract class AbstractLoadAction extends AbstractAction {

    protected AbstractLoadAction(Controller controller, ILoaderProgress monitor)
            throws DataAccessObjectInitializationException {
        super(controller, monitor);
    }

    @Override
    protected abstract DAOLoadVisitor createVisitor();

    @Override
    protected void checkDao(DataAccessObject dao) throws DataAccessObjectInitializationException {
        if (!(dao instanceof DataReader)) {
            final String errMsg = getMessage("errorWrongDao", getConfig().getString(Config.DAO_TYPE),
                    DataAccessObjectFactory.CSV_READ_TYPE + " or " + DataAccessObjectFactory.DATABASE_READ_TYPE,
                    getConfig().getString(Config.OPERATION));
            getLogger().fatal(errMsg);
            throw new DataAccessObjectInitializationException(errMsg);
        }
    }

    /**
     * @return true if there're still more rows, false if done
     */
    @Override
    protected boolean visit() throws DataAccessObjectException, ParameterLoadException, OperationException,
    ConnectionException {

        final int loadBatchSize = this.getConfig().getLoadBatchSize();
        final List<Map<String, Object>> daoRowList = getDao().readRowList(loadBatchSize);
        if (daoRowList == null || daoRowList.size() == 0) return false;
        for (final Map<String, Object> daoRow : daoRowList) {
            if (!DAORowUtil.isValidRow(daoRow)) return false;
            getVisitor().visit(daoRow);
        }
        return true;
    }

    @Override
    protected void flush() throws OperationException, DataAccessObjectException {
        getVisitor().flushRemaining();
    }

    @Override
    protected void initOperation() throws MappingInitializationException, DataAccessObjectException, OperationException {
        // ensure all field mappings are valid before data load
        ((LoadMapper)this.getController().getMapper()).resolveMappedFieldsForDataLoad();
        // start the Progress Monitor
        getMonitor().beginTask(getMessage("loading", getConfig().getString(Config.OPERATION)), getDao().getTotalRows());
        // set the starting row
        DAORowUtil.get().skipRowToStartOffset(getConfig(), getDao(), getMonitor(), !getConfig().isBulkAPIEnabled());
    }

    @Override
    protected List<String> getStatusColumns() {
        return getDao().getColumnNames();
    }

    @Override
    protected DataReader getDao() {
        return (DataReader)super.getDao();
    }

    @Override
    protected DAOLoadVisitor getVisitor() {
        return (DAOLoadVisitor)super.getVisitor();
    }

    @Override
    protected boolean writeStatus() {
        return true;
    }

}
