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

import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.security.EncryptionAesUtil;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/*
 * This class can be substituted for org.apache.commons.dbcp.BasicDataSource in
 * a user's database-conf.xml file.  It's purpose is to allow the database
 * password to be enrypted.  
 *
 * Use dataloader's "ecnrypt -e" command (no key file) to encrypt the 
 * database password, and paste the resulting string into the password
 * entry as in:
 *
 * DrozBook:dao tgagne$ encrypt -e mysecretpassword
 * 2015-04-18 06:46:05,267 INFO  [main] security.EncryptionUtil main (EncryptionUtil.java:365) - fa7f28fd6b39f34660f359f4e67fcdbbf80a8187cf4eec85
 *
 * then 
 *
 * <bean id="qa" class="com.salesforce.dataloader.dao.EncryptedDataSource" destroy-method="close">
 *     <property name="driverClassName" value="oracle.jdbc.OracleDriver"/>
 *     <property name="url" value="your db url"/>
 *     <property name="username" value="username"/>
 *     <property name="password" value="fa7f28fd6b39f34660f359f4e67fcdbbf80a8187cf4eec85"/>
 * </bean>
 *
 */

public class EncryptedDataSource extends org.apache.commons.dbcp.BasicDataSource {

    public EncryptedDataSource() {
        super();
    }

    public synchronized void setPassword(String encodedPassword) {
    	this.password = decode(encodedPassword);
    }
    
    private String decode(String password) {
        return new String(DecryptString(encrypter, password));
    }
    
    /* Decrypt property with propName using the encrypter. If decryption succeeds, return the decrypted value
     *
     * @param propMap
     * @param propName
     * @return decrypted property value
     * @throws ParameterLoadException
     */
    static private String DecryptString(EncryptionAesUtil encrypter, String encryptedString) {

        if (encryptedString != null && encryptedString.length() > 0) {
            try {
                return encrypter.decryptMsg(encryptedString);
            } 
            
            catch (Exception e) {
            	String errMsg = Messages.getFormattedString("Config.errorParameterLoad", 
                	new String[] { "db password", String.class.getName() });                
                logger.error(errMsg, e);
                e.printStackTrace();
            }
        }
        return null;
    }
    
    private final EncryptionAesUtil encrypter = new EncryptionAesUtil();
    
    private static Logger logger = LogManager.getLogger(EncryptedDataSource.class);
}
