package com.salesforce.dataloader.dao.database;

import java.io.IOException;
import org.apache.log4j.Logger;

import com.salesforce.dataloader.security.EncryptionUtil;
import com.salesforce.dataloader.config.Messages;
import java.security.GeneralSecurityException;

import org.apache.commons.dbcp.BasicDataSource;

public class DataSource extends org.apache.commons.dbcp.BasicDataSource {

    public DataSource() {
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
    
    private static Logger logger = Logger.getLogger(DataSource.class);
}