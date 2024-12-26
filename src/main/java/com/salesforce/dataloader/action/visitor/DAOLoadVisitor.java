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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import com.salesforce.dataloader.model.TableRow;
import com.salesforce.dataloader.util.DAORowUtil;

import org.apache.commons.beanutils.*;
import org.apache.commons.text.StringEscapeUtils;
import com.salesforce.dataloader.util.DLLogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.client.PartnerClient;
import com.salesforce.dataloader.client.SessionInfo;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.config.LastRunProperties;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataReader;
import com.salesforce.dataloader.dao.DataWriter;
import com.salesforce.dataloader.dyna.SforceDynaBean;
import com.salesforce.dataloader.exception.*;
import com.salesforce.dataloader.mapping.LoadMapper;
import com.sforce.async.AsyncApiException;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;
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
    protected int dynaArraySize = 0;
    private HashMap<Integer, Boolean> rowConversionFailureMap;

    protected BasicDynaClass dynaClass = null;
    protected DynaProperty[] dynaProps = null;

    private final int MAX_ROWS_IN_BATCH;
    protected List<TableRow> daoRowList = new ArrayList<TableRow>();
    protected ArrayList<Integer> batchRowToDAORowList = new ArrayList<Integer>();
    private int processedDAORowCounter = 0;
    private static final Logger logger = DLLogManager.getLogger(DAOLoadVisitor.class);
    // following regex pattern is based on info from:
    // - https://www.regular-expressions.info/lookaround.html
    // - https://www.geeksforgeeks.org/how-to-validate-html-tag-using-regular-expression/#
    private String richTextRegex = AppConfig.DEFAULT_RICHTEXT_REGEX;
    private Field[] cachedFieldAttributesForOperation = null;
    
    protected DAOLoadVisitor(Controller controller, ILoaderProgress monitor, DataWriter successWriter,
            DataWriter errorWriter) {
        super(controller, monitor, successWriter, errorWriter);

        this.columnNames = ((DataReader)controller.getDao()).getColumnNames();

        List<DynaBean> dynaList = null;
        try {
            dynaList = new ArrayList<DynaBean>(((DataReader)controller.getDao()).getTotalRows());
        } catch (DataAccessObjectException e) {
            dynaList = new ArrayList<DynaBean>();
        }
        dynaArray = dynaList;
        SforceDynaBean.registerConverters(getConfig());

        this.MAX_ROWS_IN_BATCH = getConfig().getMaxRowsInImportBatch();
        rowConversionFailureMap = new HashMap<Integer, Boolean>();
        String newRichTextRegex = getConfig().getString(AppConfig.PROP_RICH_TEXT_FIELD_REGEX);
        if (newRichTextRegex != null 
                && !newRichTextRegex.isBlank() 
                && !newRichTextRegex.equals(richTextRegex)) {
            this.richTextRegex = newRichTextRegex;
        }
        this.initLoadRateCalculator();
    }
    
    public void setRowConversionStatus(int dataSourceRow, boolean conversionSuccess) {
        if (!conversionSuccess) {
            this.rowConversionFailureMap.put(dataSourceRow, true);
        }
    }
    
    private int rowConversionCheckCounter = 0;
    private boolean gotSkippedRowsCount = false;
    private int skippedRowsCount = 0;
    protected boolean isRowConversionSuccessful() {
        if (!gotSkippedRowsCount) {
            try {
                skippedRowsCount = controller.getAppConfig().getInt(AppConfig.PROP_LOAD_ROW_TO_START_AT);
                gotSkippedRowsCount = true;
            } catch (ParameterLoadException e) {
                // @ignored
            }
        }
        int rowToCheck = skippedRowsCount + rowConversionCheckCounter++;
        Boolean conversionFailure = this.rowConversionFailureMap.get(rowToCheck);
        if (conversionFailure != null && conversionFailure.booleanValue()) {
            return false;
        }
        return true;   // no entry in the list of failed conversions means successful conversion
    }
    
    private int bytesInBatch = 0;

    @Override
    public boolean visit(TableRow row) throws OperationException, DataAccessObjectException,
    ConnectionException, BatchSizeLimitException {
        AppConfig appConfig = controller.getAppConfig();
        // the result are sforce fields mapped to data
        TableRow sforceDataRow = getMapper().mapData(row, processedDAORowCounter == 0);
        if (this.getConfig().getBoolean(AppConfig.PROP_TRUNCATE_FIELDS)
            && this.getConfig().isRESTAPIEnabled()
            && "update".equalsIgnoreCase(this.getConfig().getString(AppConfig.PROP_OPERATION))) {
            PartnerClient partnerClient = this.getController().getPartnerClient();
            if (cachedFieldAttributesForOperation == null) {
                cachedFieldAttributesForOperation = partnerClient.getSObjectFieldAttributesForRow(
                                this.getConfig().getString(AppConfig.PROP_ENTITY), sforceDataRow);
            }
            for (String fieldName : sforceDataRow.getHeader().getColumns()) {
                for (Field fieldDescribe : cachedFieldAttributesForOperation) {
                    // Field truncation is applicable to certain field types only.
                    // See https://developer.salesforce.com/docs/atlas.en-us.api_tooling.meta/api_tooling/sforce_api_header_allowfieldtruncation.htm
                    // for the list of field types that field truncation is applicable to.
                    FieldType type = fieldDescribe.getType();
                    if (fieldDescribe.getName().equalsIgnoreCase(fieldName)
                            && (type == FieldType.email
                               || type == FieldType.string
                               || type == FieldType.picklist
                               || type == FieldType.phone
                               || type == FieldType.textarea
                               || type == FieldType.multipicklist)
                        ) {
                        int fieldLength = fieldDescribe.getLength();
                        if (row.get(fieldName).toString().length() > fieldLength) {
                            if (type == FieldType.email) {
                                String[] emailParts = row.get(fieldName).toString().split("@");
                                if (emailParts.length == 2) {
                                    String firstPart = emailParts[0].substring(0,
                                            fieldLength - emailParts[1].length() - 1);
                                    row.put(fieldName, firstPart + "@" + emailParts[1]);
                                    continue;
                                }
                            }
                            row.put(fieldName, row.get(fieldName).toString().substring(0, fieldLength));
                        }
                    }
                }
            }
        }
        convertBulkAPINulls(sforceDataRow);
                
        // Make sure to initialize dynaClass only after mapping a row.
        // This is to make sure that all polymorphic field mappings specified
        // in the mapping file are mapped to parent object.
        if (dynaProps == null) {
            dynaProps = SforceDynaBean.createDynaProps(controller.getFieldTypes(), controller);
        }
        if (dynaClass == null) {
            dynaClass = SforceDynaBean.getDynaBeanInstance(dynaProps);
        }
        try {
            DynaBean dynaBean = SforceDynaBean.convertToDynaBean(dynaClass, sforceDataRow);
            Map<String, String> fieldMap = BeanUtils.describe(dynaBean);
            for (String fName : fieldMap.keySet()) {
                if (fieldMap.get(fName) != null) {
                    // see if any entity foreign key references are embedded here
                    Object value = this.getFieldValue(fName, dynaBean.get(fName));
                    dynaBean.set(fName, value);
                }
            }

            int bytesInBean = getBytesInBean(dynaBean);
            if (this.bytesInBatch + bytesInBean > getMaxBytesInBatch()) {
                loadBatch();
                this.bytesInBatch = 0;
                this.processedDAORowCounter--; // roll back the counter by 1
                throw new BatchSizeLimitException("batch max bytes size reached");
            }
            if (appConfig.getBoolean(AppConfig.PROP_PROCESS_BULK_CACHE_DATA_FROM_DAO)
                    || (!appConfig.isBulkAPIEnabled() && !appConfig.isBulkV2APIEnabled())) {
                // either bulk mode or cache bulk data uploaded from DAO
                this.daoRowList.add(row);
            }
            dynaArray.add(dynaBean);
            this.bytesInBatch += bytesInBean;
            this.batchRowToDAORowList.add(this.processedDAORowCounter);
        } catch (ConversionException | IllegalAccessException conve) {
            String errMsg = Messages.getMessage("Visitor", "conversionErrorMsg", conve.getMessage());
            getLogger().error(errMsg, conve);

            conversionFailed(row, errMsg);
            if (!appConfig.isBulkAPIEnabled() && !appConfig.isBulkV2APIEnabled()) {
                // SOAP or REST API use daoRowList to process results of an upload request
                this.daoRowList.add(row);
            }
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
        if (dynaArray.size() >= this.MAX_ROWS_IN_BATCH) {
            loadBatch();
        }
        return true;
    }
        
    protected abstract int getBytesInBean(DynaBean dynaBean);
    protected abstract int getMaxBytesInBatch();

    /**
     * @param row
     * @param errMsg
     * @throws DataAccessObjectException
     * @throws OperationException
     */
    protected void conversionFailed(TableRow row, String errMsg) throws DataAccessObjectException,
            OperationException {
        writeError(row, errMsg);
    }

    protected void convertBulkAPINulls(TableRow row) {}

    public void flushRemaining() throws OperationException, DataAccessObjectException, BatchSizeLimitException {
        // check if there are any entities left
        if (dynaArray.size() > 0) {
            loadBatch();
        }
        // clear the caches
        cachedFieldAttributesForOperation = null;
        closeJob();
    }
    
    protected void closeJob() throws OperationException, DataAccessObjectException, BatchSizeLimitException {
        // do nothing. Subclasses should override if they have a job to close
    }
    
    protected abstract void loadBatch() throws DataAccessObjectException, OperationException, BatchSizeLimitException;

    public void clearArrays() {
        // clear the arrays
        daoRowList.clear();
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
        throw new LoadExceptionOnServer(msg, t);
    }

    protected void handleException(Throwable t) throws LoadException {
        handleException(null, t);
    }

    @Override
    protected boolean writeStatus() {
        return true;
    }

    private void initLoadRateCalculator() {
        try {
            DataReader dao = (DataReader)getController().getDao();
            getRateCalculator().start(dao.getTotalRows());
            getProgressMonitor().setSubTask(getRateCalculator().calculateSubTask(getNumberOfRows(), getNumberErrors()));
        } catch (Exception e) {
            logger.error("Unable to get total rows to upload from CSV or database");
            getRateCalculator().start(0);
        }
    }

    @Override
    protected LoadMapper getMapper() {
        return (LoadMapper)super.getMapper();
    }
    
    private static final int NONBREAKING_SPACE_ASCII_VAL = 0xA0;
    private static Controller currentController = null;
    private ArrayList<String> htmlFormattedSforceFieldList = null;
    private ArrayList<String> phoneSforceFieldList = null;

    private synchronized void getHtmlFormattedAndPhoneSforceFieldList() {
        if (htmlFormattedSforceFieldList != null && phoneSforceFieldList != null) {
            return; // already created
        }
        if (getController() == currentController && htmlFormattedSforceFieldList != null) {
            return;
        }
        if (getController() == null) {
            return;
        }
        if (!getController().isLoggedIn()) {
            // clear cached values if not logged in
            currentController = null;
            return;
        }
        currentController = getController();
        htmlFormattedSforceFieldList = new ArrayList<String>();
        phoneSforceFieldList = new ArrayList<String>();
        DescribeSObjectResult result = getController().getFieldTypes();
        Field[] fields = result.getFields();
        for (Field field : fields) {
            if (field.getHtmlFormatted()) {
                htmlFormattedSforceFieldList.add(field.getName());
            }
            if (field.getType() == FieldType.phone) {
                phoneSforceFieldList.add(field.getName());
            }
        }
    }
    
    public Object getFieldValue(String fieldName, Object fieldValue) {
        fieldValue = getHtmlFormattedFieldValue(fieldName, fieldValue);
        fieldValue = getPhoneFieldValue(fieldName, fieldValue);
        return fieldValue;
    }
    
    private Object getHtmlFormattedFieldValue(String fieldName, Object fieldValue) {
        getHtmlFormattedAndPhoneSforceFieldList();
        if (htmlFormattedSforceFieldList == null 
            || !htmlFormattedSforceFieldList.contains(fieldName)
            || !getController().getAppConfig().getBoolean(AppConfig.PROP_LOAD_PRESERVE_WHITESPACE_IN_RICH_TEXT)) {
            return fieldValue;
        }
        return convertToHTMLFormatting((String)fieldValue, this.richTextRegex);
    }

    public static String convertToHTMLFormatting(String fvalue, String regex) {
        fvalue = fvalue.replaceAll("\r\n", "<br/>");
        fvalue = fvalue.replaceAll("\n", "<br/>");
        fvalue = fvalue.replaceAll("\r", "<br/>");
        String[] outsideHTMLTags = fvalue.split(regex);
        Pattern htmlTagInRichTextPattern = Pattern.compile(regex);
        Matcher matcher = htmlTagInRichTextPattern.matcher(fvalue);
        String htmlEscapedValue = "";
        int idx = 0;
        while (matcher.find()) {
            if (idx >= outsideHTMLTags.length) {
                htmlEscapedValue += matcher.group();
            } else {
                htmlEscapedValue += escapeHTMLChars(outsideHTMLTags[idx]) + matcher.group();
            }
            idx++;
        }
        if (outsideHTMLTags.length > idx) {
            htmlEscapedValue += escapeHTMLChars(outsideHTMLTags[idx]);
        }
        return htmlEscapedValue;
    }
    
    private static String escapeHTMLChars(String input) {
        if (input == null) {
            return null;
        }
        String unescapedInput = StringEscapeUtils.unescapeHtml4(input);
        StringBuffer htmlFormattedStr = new StringBuffer("");
        for (int i = 0, len = unescapedInput.length(); i < len; i++) {
            char c = unescapedInput.charAt(i);
            int cval = c;
            char nextChar = 0;
            if (i+1 < unescapedInput.length()) {
                nextChar = unescapedInput.charAt(i+1);
            }
            char prevChar = 0;
            if (i > 0) {
                prevChar = unescapedInput.charAt(i-1);
            }

            boolean isCharWhitespace = Character.isWhitespace(c) || cval == NONBREAKING_SPACE_ASCII_VAL;
            boolean isNextCharWhitespace = Character.isWhitespace(nextChar) || nextChar == NONBREAKING_SPACE_ASCII_VAL;
            boolean isPrevCharWhitespace = Character.isWhitespace(prevChar) || prevChar == NONBREAKING_SPACE_ASCII_VAL;
            //only occurrences of multiple w
            if (isCharWhitespace) {
                if (isNextCharWhitespace || isPrevCharWhitespace) {
                    htmlFormattedStr.append("&nbsp;");
                } else {
                    htmlFormattedStr.append(c);
                }
            } else {
                htmlFormattedStr.append(StringEscapeUtils.escapeHtml4(Character.toString(c)));
            }
        }
        return htmlFormattedStr.toString();
    }

    private Object getPhoneFieldValue(String fieldName, Object fieldValue) {
        getHtmlFormattedAndPhoneSforceFieldList();
        if (this.phoneSforceFieldList == null
                || !this.phoneSforceFieldList.contains(fieldName)
                || !this.getConfig().getBoolean(AppConfig.PROP_FORMAT_PHONE_FIELDS)) {
            return fieldValue;
        }
        String localeStr = Locale.getDefault().toString();
        SessionInfo sessionInfo = this.controller.getPartnerClient().getSession();
        if (sessionInfo != null) {
            localeStr = sessionInfo.getUserInfoResult().getUserLocale();
        }
        return DAORowUtil.getPhoneFieldValue((String)fieldValue, localeStr);
    }
    

    protected void processResult(TableRow dataRow, boolean isSuccess, String id, Error[] errors)
            throws DataAccessObjectException {
        // process success vs. error
        // extract error message from error result
        if (isSuccess) {
            writeSuccess(dataRow, id, null);
        } else {
            writeError(dataRow,
                    errors == null ? Messages.getString("Visitor.noErrorReceivedMsg") : errors[0].getMessage());
        }
    }
    
    protected void setLastRunProperties(Object[] results) throws LoadException {
        // set the last processed row number in the config (*_lastRun.properties) file
        int currentProcessed;
        try {
            currentProcessed = getConfig().getInt(LastRunProperties.LAST_LOAD_BATCH_ROW);
        } catch (ParameterLoadException e) {
            // if there's a problem getting last batch row, start at the beginning
            currentProcessed = 0;
        }
        currentProcessed += results.length;
        getConfig().setValue(LastRunProperties.LAST_LOAD_BATCH_ROW, currentProcessed);
        try {
            getConfig().saveLastRun();
        } catch (IOException e) {
            String errMsg = Messages.getString("LoadAction.errorLastRun");
            getLogger().error(errMsg, e);
            handleException(errMsg, e);
        }
    }
}
