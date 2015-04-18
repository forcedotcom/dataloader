package com.salesforce.dataloader.dao.database;

import java.io.IOException;
import org.apache.log4j.Logger;

import com.salesforce.dataloader.security.EncryptionUtil;
import com.salesforce.dataloader.config.Messages;
import java.security.GeneralSecurityException;

import org.apache.commons.dbcp.BasicDataSource;

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
 * <bean id="qa" class="com.salesforce.dataloader.dao.database.EncryptedDataSource" destroy-method="close">
 *     <property name="driverClassName" value="oracle.jdbc.OracleDriver"/>
 *     <property name="url" value="your db url"/>
 *     <property name="username" value="username"/>
 *     <property name="password" value="fa7f28fd6b39f34660f359f4e67fcdbbf80a8187cf4eec85 "/>
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
    static private String DecryptString(EncryptionUtil encrypter, String encryptedString) {

        if (encryptedString != null && encryptedString.length() > 0) {
            try {
                return encrypter.decryptString(encryptedString);
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
    
    private final EncryptionUtil encrypter = new EncryptionUtil();
    
    private static Logger logger = Logger.getLogger(EncryptedDataSource.class);
}
