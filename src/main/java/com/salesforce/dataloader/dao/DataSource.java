package com.salesforce.dataloader.dao.database;

import java.io.IOException;
import sun.misc.BASE64Decoder;
import org.apache.log4j.Logger;

import org.apache.commons.dbcp.BasicDataSource;

public class DataSource extends org.apache.commons.dbcp.BasicDataSource {

    public DataSource() {
        super();
    }

    public synchronized void setPassword(String encodedPassword) {
    	logger.warn("encoded password is " + encodedPassword);
    	this.password = decode(encodedPassword);
    }
    
    private String decode(String password) {
        BASE64Decoder decoder = new BASE64Decoder();
        String decodedPassword = null;
        try {
        	decodedPassword = new String(decoder.decodeBuffer(password));
        } catch (IOException e) {
        	e.printStackTrace();
        }
        
        return decodedPassword;
    }
    
    private static Logger logger = Logger.getLogger(DataSource.class);
}