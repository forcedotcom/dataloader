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

package com.salesforce.dataloader.EncryptionUtil;

import com.salesforce.dataloader.dao.EncryptedDataSource;
import com.salesforce.dataloader.security.EncryptionAesUtil;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

/**
 * com.salesforce.dataloader
 *
 * @author xbian
 */
public class EncryptionUtilTest {

    private static final Logger logger = LogManager.getLogger(EncryptionUtilTest.class);

    @Test /* test for single line encryption */
    public void testEncryption() throws Exception {
        EncryptionAesUtil encryptionAesUtil = new EncryptionAesUtil();
        String passwordText = "somePassword6c3708b3";
        byte[] secureKey = encryptionAesUtil.generateEncryptionKey();
        logger.info("\nEncryption key:" + secureKey.toString());
        byte[] encryptedMsg = encryptionAesUtil.encryptMsg(passwordText, secureKey);
        String decryptedMsg = encryptionAesUtil.decryptMsg(encryptedMsg, secureKey);
        logger.info("\nEncrypted message:" + encryptedMsg.toString() + "\nText to be encrypted:" + passwordText + "\nDecrypted       text:" + decryptedMsg);
        Assert.assertNotEquals("Encrpted message should be not be equal to original message", passwordText, new String(encryptedMsg));
        Assert.assertEquals("Text recovered from encrypted message is as the expected: ", passwordText, decryptedMsg);
    }

    @Test(expected = GeneralSecurityException.class)
    public void testEncryptionExceptionThrownWhenKeyNotSet() throws Exception {
        EncryptionAesUtil encryptionAesUtil = new EncryptionAesUtil();
        String passwordText = "somePassword6c3708b3";
        encryptionAesUtil.encryptMsg(passwordText, null);
    }

    @Test
    public void testEncryptionToAKeyFile() throws IOException, GeneralSecurityException {
        EncryptionAesUtil encryptionAesUtil = new EncryptionAesUtil();

        File temp = File.createTempFile("temp-file-name", ".tmp");
        String filePath = temp.getAbsolutePath();
        //delete so that file not actually created
        Files.delete(Paths.get(filePath));

        encryptionAesUtil.createKeyFileIfNotExisting(filePath);

        String passwordText = "somePassword6c3708b3";
        encryptionAesUtil.setCipherKeyFromFilePath(filePath);

        String encryptMsg = encryptionAesUtil.encryptMsg(passwordText);
        String decryptedMsg = encryptionAesUtil.decryptMsg(encryptMsg);
        logger.info("\nEncrypted key:" + encryptMsg + "\nText to be encrypted:" + passwordText + "\nDecrypted       text:" + decryptedMsg);

        Files.delete(Paths.get(filePath));
        Assert.assertEquals("Recovered text are the same", passwordText, decryptedMsg);
    }

    @Test
    public void testGetFilePath() throws IOException, GeneralSecurityException {
        EncryptionAesUtil encryptionAesUtil = new EncryptionAesUtil();
        String filePath = encryptionAesUtil.createUserProfileKeyName();
        Assert.assertTrue(filePath.contains(EncryptionAesUtil.DEFAULT_KEYFILE_NAME));
        filePath = encryptionAesUtil.createKeyFileIfNotExisting(filePath + ".store");
        Assert.assertTrue(Files.exists(Paths.get(filePath)));
        Files.delete(Paths.get(filePath));
    }
    
    @Test
    public void testAutoSetKeyFile() throws IOException, GeneralSecurityException {
        boolean cleanup = false;
        String filePath = null;
        String filePathBak = null;
        try {
            EncryptionAesUtil encryptionAesUtil = new EncryptionAesUtil();
            String passwordText = "somePassword6c3708b3";
            filePath = encryptionAesUtil.createUserProfileKeyName();
            filePathBak = filePath + ".bak";
            if (Files.exists(Paths.get(filePath))) {
                cleanup = true;
                Files.move(Paths.get(filePath), Paths.get(filePathBak));
            }
            // without setting the default key
            String encryptedMsg = encryptionAesUtil.encryptMsg(passwordText);
            String decryptedMsg = encryptionAesUtil.decryptMsg(encryptedMsg);

            Assert.assertTrue(Files.exists(Paths.get(filePath)));
            Files.delete(Paths.get(filePath));

            logger.info("\nEncrypted message:" + encryptedMsg.toString() + "\nText to be encrypted:" + passwordText + "\nDecrypted       text:" + decryptedMsg);
            Assert.assertNotEquals("Encrypted message should be not be equal to original message", passwordText, encryptedMsg);
            Assert.assertEquals("Text recovered from encrypted message is as the expected: ", passwordText, decryptedMsg);
        } finally {
             if (cleanup) {
                Files.move(Paths.get(filePathBak), Paths.get(filePath));
            }
        }
    }
    
    @Test /* test for single line encryption */
    public void testEncryptedDataSource() throws Exception {
        String passwordText = "somePassword6c3708b3";
        EncryptionAesUtil encryptionAesUtil = new EncryptionAesUtil();
        String encryptedPassword = encryptionAesUtil.encryptMsg(passwordText);
        EncryptedDataSource dataSource = new EncryptedDataSource();
        dataSource.setPassword(encryptedPassword);
        String savedPassword = dataSource.getPassword();
        dataSource.close();
        Assert.assertNotEquals("Encrypted password should be not be equal to original password", passwordText, encryptedPassword);                
        Assert.assertEquals("Password recovered from EncryptedDataSource instance is as the expected: ", passwordText, savedPassword);
    }
}
