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
package com.salesforce.dataloader.dao;

import org.apache.logging.log4j.Logger;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.dao.csv.CSVFileReader;
import com.salesforce.dataloader.dao.csv.CSVFileWriter;
import com.salesforce.dataloader.dao.database.DatabaseReader;
import com.salesforce.dataloader.dao.database.DatabaseWriter;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import com.salesforce.dataloader.exception.UnsupportedDataAccessObjectException;

public class DataAccessObjectFactory {
    private static Logger logger = org.apache.logging.log4j.LogManager.getLogger(DataAccessObjectFactory.class);

    static public final String CSV_READ_TYPE = "csvRead";
    static public final String CSV_WRITE_TYPE = "csvWrite";
    static public final String DATABASE_READ_TYPE = "databaseRead";
    static public final String DATABASE_WRITE_TYPE = "databaseWrite";

    public DataAccessObject getDaoInstance(String daoType, Config config)
            throws DataAccessObjectInitializationException {
        DataAccessObject dao = null;

        logger.info(Messages.getFormattedString("DataAccessObjectFactory.creatingDao", new String[] {config.getString(Config.DAO_NAME), daoType}));

        if (CSV_READ_TYPE.equalsIgnoreCase(daoType)) {
            dao = new CSVFileReader(config);
        } else if (CSV_WRITE_TYPE.equalsIgnoreCase(daoType)) {
            dao = new CSVFileWriter(config.getString(Config.DAO_NAME), config);
        } else if (DATABASE_READ_TYPE.equalsIgnoreCase(daoType)) {
            dao = new DatabaseReader(config);
        } else if (DATABASE_WRITE_TYPE.equalsIgnoreCase(daoType)) {
            dao = new DatabaseWriter(config);
        } else {
            String errMsg = Messages.getFormattedString("DataAccessObjectFactory.daoTypeNotSupported", daoType);
            logger.error(errMsg);
            throw new UnsupportedDataAccessObjectException(errMsg);
        }
        return dao;
    }
}
