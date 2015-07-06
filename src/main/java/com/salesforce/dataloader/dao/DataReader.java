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

import java.util.List;
import java.util.Map;

import com.salesforce.dataloader.exception.DataAccessObjectException;
import com.salesforce.dataloader.model.Row;

/**
 * Interface to be implemented for data readers -- data access objects that are used for reading rows of data.
 *
 * @author Alex Warshavsky
 * @since 8.0
 */
public interface DataReader extends DataAccessObject {

    /**
     * Get a row of data from a data source
     *
     * @return a {@link Row} containing all the keys and values of a row
     * @throws DataAccessObjectException
     */
    Row readRow() throws DataAccessObjectException;

    /**
     * Get a list of rows of data from a data source
     *
     * @param maxRows Maximum number of rows to read in one call
     * @return a list of up to maxRows {@link Row} objects, each of them containing all the keys and values of a row
     * @throws DataAccessObjectException
     */
    List<Row> readRowList(int maxRows) throws DataAccessObjectException;

    /**
     * @return Total number of rows that will be read by the current Data Access Object
     * @throws DataAccessObjectException
     */
    int getTotalRows() throws DataAccessObjectException;
}
