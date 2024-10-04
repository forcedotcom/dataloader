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

import com.salesforce.dataloader.action.AbstractExtractAction;
import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.client.HttpClientTransport;
import com.salesforce.dataloader.client.HttpTransportInterface;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.dao.DataWriter;
import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import com.salesforce.dataloader.exception.ExtractException;
import com.salesforce.dataloader.exception.OperationException;
import com.salesforce.dataloader.exception.ParameterLoadException;
import com.salesforce.dataloader.mapping.SOQLMapper;
import com.salesforce.dataloader.model.Row;
import com.sforce.async.AsyncApiException;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.ws.ConnectionException;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Superclass for all query visitors
 * 
 * @author Colin Jarvis
 * @since 21.0
 */
public abstract class AbstractQueryVisitor extends AbstractVisitor implements IQueryVisitor {

    private final DataWriter queryWriter;
    private final String soql;
    private final List<Row> batchRows;
    private final List<String> batchIds;
    private final int batchSize;
    protected final AbstractExtractAction action;
    private static final Logger logger = LogManager.getLogger(AbstractQueryVisitor.class);

    public AbstractQueryVisitor(AbstractExtractAction action, Controller controller, ILoaderProgress monitor, DataWriter queryWriter,
            DataWriter successWriter, DataWriter errorWriter) {
        super(controller, monitor, successWriter, errorWriter);
        this.queryWriter = queryWriter;
        this.soql = getConfig().getString(Config.EXTRACT_SOQL);
        this.batchRows = new ArrayList<Row>();
        this.batchIds = new ArrayList<String>();
        this.batchSize = getWriteBatchSize();
        this.action = action;
    }

    @Override
    public final void visit() throws DataAccessObjectException, OperationException {
        try {
            if (getProgressMonitor().isCanceled()) return;
            final int size = executeQuery(getSoql());
            if (size == 0) {
                getLogger().info(Messages.getMessage(getClass(), "noneReturned"));
            } else {
                if (getProgressMonitor().isCanceled()) return;
                startWriteExtraction(size);
                writeExtraction();
                flushResults();
            }
        } catch (final ApiFault e) {
            throw new ExtractException(e.getExceptionMessage(), e);
        } catch (final ConnectionException e) {
            throw new ExtractException(e.getMessage(), e);
        } catch (final AsyncApiException e) {
            throw new ExtractException(e.getExceptionMessage(), e);
        }
    }

    protected abstract void writeExtraction() throws AsyncApiException, ExtractException, DataAccessObjectException,
    ConnectionException;

    protected abstract int executeQuery(String soql) throws ConnectionException, AsyncApiException, OperationException;

    @Override
    protected boolean writeStatus() {
        return getConfig().getBoolean(Config.ENABLE_EXTRACT_STATUS_OUTPUT);
    }

    private String getSoql() {
        return this.soql;
    }

    private DataWriter getQueryWriter() {
        return this.queryWriter;
    }

    protected void addResultRow(Row row, String id) throws DataAccessObjectException {
        if (controller.getConfig().getBoolean(Config.INCLUDE_RICH_TEXT_FIELD_DATA_IN_QUERY_RESULTS)) {
            getRTFDataForRow(row);
        }
        this.batchRows.add(row);
        this.batchIds.add(id);
        if (this.batchSize == this.batchRows.size()) {
            writeBatch();
        }
    }
    

    private static final String IMG_TAG_SRC_ATTR_PATTERN = "<img\\s+(?:[^>]*?\\s+)?src=\"([^\"]*)\"(?:\\s+[^>]*?)?>";
    private void getRTFDataForRow(Row row) {
        for (String colName : row.keySet()) {
            Object colVal = row.get(colName);
            boolean isColValModified = false;
            if (colVal == null) {
                continue;
            }
            String strValOfCol = colVal.toString();
            String[] outsideIMGTagSrcAttrParts = strValOfCol.split(IMG_TAG_SRC_ATTR_PATTERN);
            Pattern htmlTagInRichTextPattern = Pattern.compile(IMG_TAG_SRC_ATTR_PATTERN);
            Matcher matcher = htmlTagInRichTextPattern.matcher(strValOfCol);
            int idx = 0;
            String newValOfCol = "";
            while (matcher.find()) {
                String imageTagSrcAttrValue = matcher.group();
                String[] imageTagParts = imageTagSrcAttrValue.split("src\\s*=\\s*\"([^\"]+)\"");
                if (imageTagParts.length == 2 && imageTagSrcAttrValue.contains(".file.force.com/servlet/rtaImage?")) {
                    String srcAttrWithBinaryContent = imageTagSrcAttrValue.substring(imageTagParts[0].length(), imageTagSrcAttrValue.length() - imageTagParts[1].length());
                    String[] srcAttrNameValue = srcAttrWithBinaryContent.split("=", 2);
                    String binaryContent = getBinaryContentForURL(srcAttrNameValue[1].replace("\"",""), colName);
                    imageTagSrcAttrValue = imageTagParts[0] + " src=\"data:image/png;base64," + binaryContent + "\"" + imageTagParts[1];
                    isColValModified = true;
                }
                if (idx >= outsideIMGTagSrcAttrParts.length) {
                    newValOfCol += imageTagSrcAttrValue;
                } else {
                    newValOfCol += outsideIMGTagSrcAttrParts[idx] + imageTagSrcAttrValue;
                }
                idx++;
            }
            if (outsideIMGTagSrcAttrParts.length > idx) {
                newValOfCol += outsideIMGTagSrcAttrParts[idx];
            }
            if (isColValModified) {
                row.put(colName, newValOfCol);
            }
        }
    }
    
    private String getBinaryContentForURL(String urlStr, String fieldName) {
        try {
            urlStr = java.net.URLDecoder.decode(urlStr, StandardCharsets.UTF_8.name());
            URI uri = new URI(urlStr);
            String queryStr = uri.getQuery();
            String[] queryParams = queryStr.split("&amp;");
            String sobjectId = "";
            String refId = "";
            for (String param : queryParams) {
                String[] nameValPair = param.split("=");
                if (nameValPair[0].equals("eid")) {
                    sobjectId = nameValPair[1];
                } else if (nameValPair[0].equals("refid")) {
                    refId = nameValPair[1];
                }
            }
            urlStr = "https://" 
                    + uri.getHost() 
                    + "/services/data/v"
                    + Controller.getAPIVersion()
                    + "/sobjects/"
                    + controller.getConfig().getString(Config.ENTITY)
                    + "/"
                    + sobjectId
                    + "/richTextImageFields/"
                    + fieldName
                    + "/"
                    + refId;
            
            HttpClientTransport transport = HttpClientTransport.getInstance();
            transport.setConfig(controller.getClient().getConnectorConfig());
            InputStream is = transport.httpGet(urlStr);
            byte[] binaryResponse = is.readAllBytes();
            return Base64.getEncoder().encodeToString(binaryResponse);
        } catch (Exception e) {
            logger.warn("Unable get image data : " + e.getMessage());
            return urlStr;
        }
    }

    private void flushResults() throws DataAccessObjectException {
        if (!this.batchRows.isEmpty()) {
            writeBatch();
        }
    }

    private void writeBatch() throws DataAccessObjectException {
        if (getProgressMonitor().isCanceled()) return;
        try {
            if (getQueryWriter().writeRowList(this.batchRows)) {
                writeSuccesses();
            } else {
                writeErrors(Messages.getMessage(getClass(), "statusErrorNotWritten",
                        getConfig().getString(Config.DAO_NAME)));
            }
            getProgressMonitor().worked(this.batchRows.size());
            getProgressMonitor().setSubTask(getRateCalculator().calculateSubTask(getNumberOfRows(), getNumberErrors()));
        } catch (final DataAccessObjectInitializationException ex) {
            throw ex;
        } catch (final DataAccessObjectException ex) {
            writeErrors(Messages.getMessage(getClass(), "statusErrorNotWrittenException",
                    getConfig().getString(Config.DAO_NAME), ex.getMessage()));
        } finally {
            this.batchRows.clear();
            this.batchIds.clear();
        }
    }

    private void writeSuccesses() throws DataAccessObjectException {
        final String msg = Messages.getMessage(getClass(), "statusItemQueried");
        final Iterator<String> ids = this.batchIds.iterator();
        for (final Row row : this.batchRows) {
            writeSuccess(row, ids.next(), msg);
        }
    }

    private void writeErrors(String errorMessage) throws DataAccessObjectException {
        for (final Row row : this.batchRows) {
            writeError(row, errorMessage);
        }
    }

    protected int getWriteBatchSize() {
        int daoBatchSize;
        try {
            daoBatchSize = getConfig().getInt(Config.DAO_WRITE_BATCH_SIZE);
            if (daoBatchSize > Config.MAX_DAO_WRITE_BATCH_SIZE) {
                daoBatchSize = Config.MAX_DAO_WRITE_BATCH_SIZE;
            }
        } catch (final ParameterLoadException e) {
            // warn about getting batch size parameter, otherwise continue w/ default
            getLogger().warn(
                    Messages.getMessage(getClass(), "errorGettingBatchSize",
                            String.valueOf(Config.DEFAULT_DAO_WRITE_BATCH_SIZE), e.getMessage()));
            daoBatchSize = Config.DEFAULT_DAO_WRITE_BATCH_SIZE;
        }
        return daoBatchSize;
    }

    protected void startWriteExtraction(int size) {
        getRateCalculator().start(size);
        // start the Progress Monitor
        getProgressMonitor().beginTask(Messages.getMessage(getClass(), "extracting"), size); //$NON-NLS-1$
        getProgressMonitor().setSubTask(getRateCalculator().calculateSubTask(getNumberOfRows(), getNumberErrors()));
    }

    @Override
    protected SOQLMapper getMapper() {
        return (SOQLMapper)super.getMapper();
    }

}
