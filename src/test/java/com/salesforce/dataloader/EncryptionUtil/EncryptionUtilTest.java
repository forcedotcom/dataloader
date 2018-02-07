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

import com.salesforce.dataloader.security.EncryptionAesUtil;

import org.apache.log4j.Logger;
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

    private static final Logger logger = Logger.getLogger(EncryptionUtilTest.class);

    @Test /* test for single line encryption */
    public void testEncryption() throws Exception {
        EncryptionAesUtil encryptionAesUtil = new EncryptionAesUtil();
        String passwordText = "somePassword6c3708b3";
        byte[] secureKey = encryptionAesUtil.generateEncryptionKey();
        byte[] encryptedMsg = encryptionAesUtil.encryptMsg(passwordText, secureKey);
        String decryptedMsg = encryptionAesUtil.decryptMsg(encryptedMsg, secureKey);
        logger.info("\nEncrypted text:" + encryptedMsg.toString() +"\nText to be encrypted:" + passwordText +  "\nDecrypted       text:" + decryptedMsg);
        Assert.assertEquals("Text recovered from encrypted message is as the expected: ", passwordText, decryptedMsg);
    }

    @Test
    public void testEncryptionToAKeyFile() throws IOException, GeneralSecurityException {
        EncryptionAesUtil encryptionAesUtil = new EncryptionAesUtil();

        File temp = File.createTempFile("temp-file-name", ".tmp");
        String filePath = temp.getAbsolutePath();

        encryptionAesUtil.createKeyFile(filePath);

        String passwordText = "somePassword6c3708b3";
        String key = encryptionAesUtil.setCipherKeyFromFilePath(filePath);
        logger.info("\nEncrypted key:" + key);

        String encryptMsg = encryptionAesUtil.encryptMsg(passwordText);
        String decryptedMsg = encryptionAesUtil.decryptMsg(encryptMsg);
        logger.info("\nEncrypted key:" + encryptMsg +"\nText to be encrypted:" + passwordText +  "\nDecrypted       text:" + decryptedMsg);

        Files.delete(Paths.get(filePath));
        Assert.assertEquals("Recovered text are the same", passwordText, decryptedMsg);
    }

    @Test
    public void testGetFilePath() throws IOException {
        EncryptionAesUtil encryptionAesUtil = new EncryptionAesUtil();
        String filePath = encryptionAesUtil.createUserProfileKeyName();
        Assert.assertTrue(filePath.contains(EncryptionAesUtil.DEFAULT_KEYFILE_NAME));
        filePath = encryptionAesUtil.createKeyFile(filePath + ".store");
        Assert.assertTrue(Files.exists(Paths.get(filePath)));
        Files.delete(Paths.get(filePath));
    }
}
