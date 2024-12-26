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

import java.util.ArrayList;
import java.util.List;

import com.salesforce.dataloader.action.progress.ILoaderProgress;
import com.salesforce.dataloader.action.visitor.DAOLoadVisitor;
import com.salesforce.dataloader.action.visitor.bulk.BulkLoadVisitor;
import com.salesforce.dataloader.action.visitor.bulk.BulkV2LoadVisitor;
import com.salesforce.dataloader.controller.Controller;
import com.salesforce.dataloader.exception.DataAccessObjectInitializationException;
import com.salesforce.dataloader.mapping.LoadMapper;

/**
 * @author Jesper Joergensen, Colin Jarvis
 * @since 17.0
 */
class BulkLoadAction extends AbstractLoadAction {

    public BulkLoadAction(Controller controller, ILoaderProgress monitor)
            throws DataAccessObjectInitializationException {
        super(controller, monitor);
    }

    @Override
    protected DAOLoadVisitor createVisitor() {
        if (this.getConfig().isBulkV2APIEnabled()) {
            return new BulkV2LoadVisitor(getController(), getMonitor(), getSuccessWriter(), getErrorWriter());
        }
        return new BulkLoadVisitor(getController(), getMonitor(), getSuccessWriter(), getErrorWriter());
    }
    
    @Override
    protected List<String> getStatusColumns() {
        if (this.getConfig().isBulkV2APIEnabled()) {
            // Success and error results in Bulk v2 job are downloaded separately.
            //
            // Success and error results in Bulk V2 upload operations contain all of the mapped columns
            // in the order in which they were uploaded.
            // Header of the error results file contains mapped DAO columns, not all DAO columns.
            // Header of the success results file is saved as-is from the downloaded success file. It
            // contains server-side field names along with a column answering "yes" or "no" to the header
            // field "created?" and another column labeled "sf__id" listing id of the sobject.
            LoadMapper mapper = (LoadMapper)this.getController().getMapper();
            return mapper.getMappedDaoColumns();
        } else {
            // A single file that matches rows in uploaded batch contains success and error
            // results in Bulk v1.
            // Each row has 2 columns: "ID" and "STATUS".
            // Header row of the success and error files can be generated using DAO's header row.
            return super.getStatusColumns();
        }
    }

}