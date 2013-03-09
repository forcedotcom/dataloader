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

package com.salesforce.dataloader.mapping;

import com.salesforce.dataloader.ConfigTestBase;
import com.salesforce.dataloader.client.PartnerClient;
import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.exception.MappingInitializationException;
import com.sforce.ws.ConnectionException;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Federico Recio
 * @since 27.0
 */
public class SOQLMapperTest extends ConfigTestBase {

    private SOQLMapper soqlMapper;

    @Before
    public void createSoqlMapper() throws Exception {
        PartnerClient partnerClient = new PartnerClient(getController());
        soqlMapper = new SOQLMapper(partnerClient, Collections.<String>emptyList(), "");
    }

    @Test
    public void testRelationshipQuery() throws Exception {
        getController().getConfig().setValue(Config.ENTITY, "User");
        soqlMapper.initSoqlMapping("select Id, Contact.Accountid from User");

        List<String> daoColumnsForSoql = soqlMapper.getDaoColumnsForSoql();
        assertEquals(2, daoColumnsForSoql.size());
        assertTrue(daoColumnsForSoql.contains("Id"));
        assertTrue(daoColumnsForSoql.contains("Contact.Accountid"));
    }

    /**
     * Verify that when the query does not match up to the column, an exception is thrown.
     *
     * @expectedResults Assert that an exception is thrown with the correct error message.
     *
     * @throws Exception
     */
    @Test
    public void testMapAutoMatchFail() throws Exception {
        try {
            doAutoMatchTest("Select id, account.parent.id, A.numberofemployees from account a");
        } catch (Mapper.InvalidMappingException e) {
            assertEquals(
                    "The following dao columns could not be mapped: [NAME]",
                    e.getMessage());
        }
    }

    /**
     * Verify that the correct columns are mapped from a query.
     *
     * @expectedResults Assert that the returned map of columns is the same as what the SOQLMapper returns.
     *
     * @throws Exception
     */
    @Test
    public void testMapAutoMatch() throws Exception {
        doAutoMatchTest("Select AccOUNT.NamE, id, account.parent.id, A.numberofemployees from account a");
    }

    private void doAutoMatchTest(String soql) throws ConnectionException,
            MappingInitializationException {
        getController().login();
        List<String> daoCols = Arrays.asList("NAME", "ID", "Parent.Id",
                "NumberOfEMPLOYEES");
        SOQLMapper mapper = new SOQLMapper(getController().getPartnerClient(),
                daoCols, null);
        mapper.initSoqlMapping(soql);
        List<String> actual = mapper.getDaoColumnsForSoql();
        assertEquals(daoCols, actual);
    }
}
