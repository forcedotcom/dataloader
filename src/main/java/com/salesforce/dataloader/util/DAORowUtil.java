/*
 * Copyright (c) 2011, salesforce.com, inc.
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

package com.salesforce.dataloader.util;

import java.io.IOException;
import java.util.*;

import org.apache.log4j.Logger;

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.action.visitor.DAOSizeVisitor;
import com.salesforce.dataloader.config.*;
import com.salesforce.dataloader.dao.DataAccessObject;
import com.salesforce.dataloader.dao.DataReader;
import com.salesforce.dataloader.exception.*;

/**
 * RowUtil for the Data Access Object
 *
 * @author Lexi Viripaeff
 * @since 6.0
 */
public class DAORowUtil {
    private static final DAORowUtil INSTANCE = new DAORowUtil();

    private DAORowUtil() {};

    public static DAORowUtil get() {
        return INSTANCE;
    }

    static Logger logger = Logger.getLogger(DAORowUtil.class);

    /**
     * Utility function for calculating the total number of rows available to current DAO instance
     * @throws DataAccessObjectException
     */
    public static int calculateTotalRows(DataReader dataReader) throws DataAccessObjectException {
        try {
            //visit the rows
            DAOSizeVisitor visitor = new DAOSizeVisitor();
            for (Map<String, Object> row = dataReader.readRow(); isValidRow(row); row = dataReader.readRow()) {
                visitor.visit(row);
            }

            return visitor.getNumberOfRows();
        } catch (DataAccessObjectException daoe) {
            logger.error(Messages.getString("RowUtil.error"), daoe); //$NON-NLS-1$
            throw daoe;
        } finally {
            // since we've read all the rows, reopen the reader to reset the input
            dataReader.close();
            dataReader.open();
        }

    }

    /**
     * @param row
     * @return true if row is valid
     */
    public static boolean isValidRow(Map<String, Object> row) {
        if (row == null) { return false; }
        return true;
    }

    /**
     * @param row
     * @return true if row is valid
     */
    public static boolean isValidRow(List<?> row) {
        if (row == null) { return false; }
        if (row.size() == 1 && ("".equals(row.get(0)))) { return false; }
        return true;
    }

    /**
     * Validate column names and return a string if there's a validation warning.
     * @param dao Data access object to validate columns for
     * @return A validation warning or null
     */
    public static String validateColumns(DataAccessObject dao) {
        HashSet<String> uniqueHeaders = new HashSet<String>();
        String warning = null;
        for (String header : dao.getColumnNames()) {
            if (header == null || header.length() == 0) {
                warning = Messages.getString("RowUtil.warningEmptyColumn"); //$NON-NLS-1$
                break;
            } else if (uniqueHeaders.contains(header)) {
                warning = Messages.getFormattedString("RowUtil.warningDuplicateColumn", header); //$NON-NLS-1$
                break;
            }
            uniqueHeaders.add(header);
        }
        if (warning != null) {
            logger.warn(warning);
        }
        return warning;
    }

    public void skipRowToStartOffset(Config cfg, DataReader rdr, ILoaderProgress mon, boolean updateProgress)
            throws LoadException {

        try {
            cfg.setValue(LastRun.LAST_LOAD_BATCH_ROW, 0);
            rowToStart(cfg, rdr);
            if (updateProgress) {
                // set the last processed value to the starting row
                int currentRow = rdr.getCurrentRowNumber();
                if (mon != null && currentRow > 0) mon.worked(currentRow);
                cfg.setValue(LastRun.LAST_LOAD_BATCH_ROW, currentRow);
                cfg.saveLastRun();
            }
        } catch (final DataAccessObjectException e) {
            handleError(e, "errorDaoStartRow");
        } catch (final IOException e) {
            handleError(e, "errorLastRun");
        }
    }

    private void handleError(final Exception e, String msgKey) throws LoadException {
        final String errMsg = Messages.getMessage(getClass(), msgKey);
        logger.error(errMsg, e);
        throw new LoadException(errMsg, e);
    }

    /**
     * Set the dataReader to point to the row where load has to be started
     */
    private void rowToStart(Config cfg, DataReader daoReader) throws LoadException, DataAccessObjectException,
    IOException {
        // start at the correct row
        final int rowToStart;
        try {
            rowToStart = cfg.getInt(Config.LOAD_ROW_TO_START_AT);
        } catch (final ParameterLoadException e) {
            return;
        }
        if (rowToStart > 0) {
            // keep skipping over rows until we run into an invalid row or we have gotten
            // to the starting row
            while (daoReader.getCurrentRowNumber() < rowToStart) {
                if (!DAORowUtil.isValidRow(daoReader.readRow())) break;
            }
        }
    }
}
