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

package com.salesforce.dataloader.action.visitor;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import com.salesforce.dataloader.model.Row;
import org.apache.commons.beanutils.*;

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataReader;
import com.salesforce.dataloader.dao.DataWriter;
import com.salesforce.dataloader.dyna.SforceDynaBean;
import com.salesforce.dataloader.exception.*;
import com.salesforce.dataloader.mapping.LoadMapper;
import com.sforce.async.AsyncApiException;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
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
    private HashMap<Integer, Boolean> rowConversionFailureMap;

    protected final BasicDynaClass dynaClass;
    protected final DynaProperty[] dynaProps;

    private final int batchSize;
    protected List<Row> daoRowList = new ArrayList<Row>();
    protected ArrayList<Integer> batchRowToDAORowList = new ArrayList<Integer>();
    private int processedDAORowCounter = 0;

    protected DAOLoadVisitor(Controller controller, ILoaderProgress monitor, DataWriter successWriter,
            DataWriter errorWriter) {
        super(controller, monitor, successWriter, errorWriter);

        this.columnNames = ((DataReader)controller.getDao()).getColumnNames();

        dynaArray = new LinkedList<DynaBean>();

        SforceDynaBean.registerConverters(getConfig());

        dynaProps = SforceDynaBean.createDynaProps(controller.getFieldTypes(), controller);
        dynaClass = SforceDynaBean.getDynaBeanInstance(dynaProps);

        this.batchSize = getConfig().getLoadBatchSize();
        rowConversionFailureMap = new HashMap<Integer, Boolean>();
    }
    
    public void setRowConversionStatus(int dataSourceRow, boolean conversionSuccess) {
        if (!conversionSuccess) {
            this.rowConversionFailureMap.put(dataSourceRow, true);
        }
    }
    
    protected boolean isRowConversionSuccessful(int dataSourceRow) {
        Boolean conversionFailure = this.rowConversionFailureMap.get(dataSourceRow);
        if (conversionFailure != null && conversionFailure.booleanValue()) {
            return false;
        }
        return true;   // no entry in the list of failed conversions means successful conversion
    }

    @Override
    public boolean visit(Row row) throws OperationException, DataAccessObjectException,
    ConnectionException {
        initLoadRateCalculator();
        if (controller.getConfig().getBoolean(Config.PROCESS_BULK_CACHE_DATA_FROM_DAO)
            || !controller.getConfig().getBoolean(Config.BULK_API_ENABLED)) {
            // either batch mode or cache bulk data uploaded from DAO
            this.daoRowList.add(row);
        }
        // the result are sforce fields mapped to data
        Row sforceDataRow = getMapper().mapData(row);
        try {
            convertBulkAPINulls(sforceDataRow);
            DynaBean dynaBean = SforceDynaBean.convertToDynaBean(dynaClass, sforceDataRow);
            Map<String, String> fieldMap = BeanUtils.describe(dynaBean);
            for (String fName : fieldMap.keySet()) {
                if (fieldMap.get(fName) != null) {
                    // see if any entity foreign key references are embedded here
                    Object value = this.getFieldValue(fName, dynaBean.get(fName));
                    dynaBean.set(fName, value);
                }
            }
            dynaArray.add(dynaBean);
            this.batchRowToDAORowList.add(this.processedDAORowCounter);
        } catch (ConversionException | IllegalAccessException conve) {
            String errMsg = Messages.getMessage("Visitor", "conversionErrorMsg", conve.getMessage());
            getLogger().error(errMsg, conve);

            conversionFailed(row, errMsg);
            // this row cannot be added since conversion has failed
            return false;
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            this.processedDAORowCounter++;
        }

        // load the batch
        if (dynaArray.size() >= this.batchSize || maxBatchBytesReached(dynaArray)) {
            loadBatch();
        }
        return true;
    }
    
    protected boolean maxBatchBytesReached(List<DynaBean> dynaArray) {
        return false;
    }

    /**
     * @param row
     * @param errMsg
     * @throws DataAccessObjectException
     * @throws OperationException
     */
    protected void conversionFailed(Row row, String errMsg) throws DataAccessObjectException,
            OperationException {
        writeError(row, errMsg);
    }

    protected void convertBulkAPINulls(Row row) {}

    public void flushRemaining() throws OperationException, DataAccessObjectException {
        // check if there are any entities left
        if (dynaArray.size() > 0) {
            loadBatch();
        }
    }

    protected abstract void loadBatch() throws DataAccessObjectException, OperationException;

    public void clearArrays() {
        // clear the arrays
        if (!controller.getConfig().getBoolean(Config.BULK_API_ENABLED)) {
            daoRowList.clear();
        }
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
    
    private static final int NONBREAKING_SPACE_ASCII_VAL = 0xA0;
    private static Controller currentController = null;
    private static ArrayList<String> htmlFormattedFieldList = null;

    private synchronized List<String> getHtmlFormattedFieldList() {
        if (getController() == currentController && htmlFormattedFieldList != null) {
            return htmlFormattedFieldList;
        }
        if (getController() == null) {
            return null;
        }
        if (!getController().isLoggedIn()) {
            // clear cached values if not logged in
            currentController = null;
            return null;
        }
        currentController = getController();
        htmlFormattedFieldList = new ArrayList<String>();
        DescribeSObjectResult result = getController().getFieldTypes();
        Field[] fields = result.getFields();
        for (Field field : fields) {
            if (field.getHtmlFormatted()) {
                htmlFormattedFieldList.add(field.getName());
            }
        }
        return htmlFormattedFieldList;
    }
    
    public Object getFieldValue(String fieldName, Object fieldValue) {
        List<String> htmlFormattedFieldList = getHtmlFormattedFieldList();
        if (htmlFormattedFieldList == null || !htmlFormattedFieldList.contains(fieldName)) {
            return fieldValue;
        }
        
        // The field is a HTML Formatted text field such as RichText
        String fvalue = (String)fieldValue;
        StringBuffer htmlFormattedFieldVal = new StringBuffer("");
        for (int i = 0, len = fvalue.length(); i < len; i++) {
            char c = fvalue.charAt(i);
            int cval = c;
            if (getController().getConfig().getBoolean(Config.LOAD_PRESERVE_WHITESPACE_IN_RICH_TEXT) 
                && (Character.isWhitespace(c) || cval == NONBREAKING_SPACE_ASCII_VAL)) {
                htmlFormattedFieldVal.append("&nbsp;");
            } else {
                htmlFormattedFieldVal.append(c);
            }
        }
        return htmlFormattedFieldVal.toString();
    }

}
