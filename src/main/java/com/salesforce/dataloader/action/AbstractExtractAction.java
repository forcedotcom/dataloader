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

package com.salesforce.dataloader.action;

import java.util.*;

import org.apache.logging.log4j.Logger;

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.action.visitor.IQueryVisitor;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.*;
import com.salesforce.dataloader.exception.*;
import com.salesforce.dataloader.mapping.SOQLMapper;
import com.salesforce.dataloader.util.AppUtil;

/**
 * Parent class for all extract dataloader actions.
 * 
 * @author Colin jarvis
 * @since 21.0
 */
abstract public class AbstractExtractAction extends AbstractAction {

    protected AbstractExtractAction(Controller controller, ILoaderProgress monitor)
            throws DataAccessObjectInitializationException {
        super(controller, monitor);
    }

    @Override
    protected boolean visit() throws OperationException, DataAccessObjectException, ParameterLoadException {
        getVisitor().visit();
        return false;
    }

    @Override
    protected boolean writeStatus() {
        return getConfig().getBoolean(AppConfig.LIMIT_OUTPUT_TO_QUERY_FIELDS) && getConfig().getBoolean(AppConfig.ENABLE_EXTRACT_STATUS_OUTPUT);
    }

    @Override
    protected void checkDao(DataAccessObject dao) throws DataAccessObjectInitializationException {
        if (!(dao instanceof DataWriter)) {
            final String errMsg = getMessage("errorWrongDao", getConfig().getString(AppConfig.DAO_TYPE),
                    DataAccessObjectFactory.CSV_WRITE_TYPE + " or " + DataAccessObjectFactory.DATABASE_WRITE_TYPE,
                    getConfig().getString(AppConfig.OPERATION));
            getLogger().fatal(errMsg);
            throw new DataAccessObjectInitializationException(errMsg);
        }
    }

    @Override
    protected DataWriter getDao() {
        return (DataWriter)super.getDao();
    }

    @Override
    public IQueryVisitor getVisitor() {
        return (IQueryVisitor)super.getVisitor();
    }

    private List<String> getDaoColumnsFromMapper() {
        ((SOQLMapper)getController().getMapper()).initSoqlMapping(getConfig().getString(AppConfig.EXTRACT_SOQL));
        return ((SOQLMapper)getController().getMapper()).getDaoColumnsForSoql();
    }

    /** static so that this can be easily called form ExtractTest */
    static List<String> getColumnsFromSoql(String soql, Logger logger) throws ExtractException {

        if (soql == null || soql.length() == 0) {
            final String errMsg = Messages.getMessage(AbstractExtractAction.class, "errorEmptyQuery");
            logger.error(errMsg);
            throw new ExtractException(errMsg);
        }

        // normalize the SOQL string and find the field list
        final String trimmedSoql = soql.trim().replaceAll("[\\s]*,[\\s]*", AppUtil.COMMA);
        final String upperSOQL = trimmedSoql.toUpperCase();
        final int selectPos = upperSOQL.indexOf("SELECT ");
        if (selectPos == -1) {
            final String errMsg = Messages.getMessage(AbstractExtractAction.class, "errorMissingSelect", soql);
            logger.error(errMsg);
            throw new ExtractException(errMsg);
        }
        final int fieldListStart = selectPos + "SELECT ".length(); //$NON-NLS-1$
        final int fieldListEnd = upperSOQL.indexOf(" FROM "); //$NON-NLS-1$

        try {
            final String fieldString = trimmedSoql.substring(fieldListStart, fieldListEnd).trim();
            final String[] fields = fieldString.split(AppUtil.COMMA); //$NON-NLS-1$
            return new ArrayList<String>(Arrays.asList(fields));
        } catch (final Exception e) {
            String errMsg;
            if (fieldListStart < "SELECT ".length()) {
                errMsg = Messages.getMessage(AbstractExtractAction.class, "errorMissingSelect", soql); //$NON-NLS-1$
            } else if (fieldListEnd < 0) {
                errMsg = Messages.getMessage(AbstractExtractAction.class, "errorMissingFrom", soql); //$NON-NLS-1$
            } else {
                errMsg = Messages.getMessage(AbstractExtractAction.class, "errorMalformedQuery", soql); //$NON-NLS-1$
            }
            logger.error(errMsg, e);
            throw new ExtractException(errMsg);
        }
    }

    @Override
    protected List<String> getStatusColumns() throws ExtractException {
        return getDaoColumnsFromMapper();
    }

    @Override
    protected void initOperation() throws DataAccessObjectInitializationException, OperationException {
        ((SOQLMapper)getController().getMapper()).clearMappings();
        if (getController().getAppConfig().getBoolean(AppConfig.LIMIT_OUTPUT_TO_QUERY_FIELDS)) {
            final List<String> daoColumns = getDaoColumnsFromMapper();
            getDao().setColumnNames(daoColumns);
        } else {
            // check for syntactical correctness and presence of nested soql.
            // nested soql is currently not supported.
            ((SOQLMapper)getController().getMapper()).parseSoql(getConfig().getString(AppConfig.EXTRACT_SOQL));
        }
    }

    @Override
    protected void flush() {}

}
