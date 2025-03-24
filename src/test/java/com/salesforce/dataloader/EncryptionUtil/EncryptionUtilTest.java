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

import com.salesforce.dataloader.ConfigTestBase;
import com.salesforce.dataloader.dao.EncryptedDataSource;
import com.salesforce.dataloader.security.EncryptionAesUtil;
import com.salesforce.dataloader.security.EncryptionUtil;

import org.apache.logging.log4j.Logger;

import com.salesforce.dataloader.util.AppUtil;
import com.salesforce.dataloader.util.DLLogManager;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

/**
 * com.salesforce.dataloader
 *
 * @author xbian
 */
public class EncryptionUtilTest extends ConfigTestBase {

    private static final Logger logger = DLLogManager.getLogger(EncryptionUtilTest.class);

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
        @SuppressWarnings("deprecation")
        String savedPassword = dataSource.getPassword();
        dataSource.close();
        Assert.assertNotEquals("Encrypted password should be not be equal to original password", passwordText, encryptedPassword);                
        Assert.assertEquals("Password recovered from EncryptedDataSource instance is as the expected: ", passwordText, savedPassword);
    }
    
    @Test
    public void testTextToBytes() {
        String input = "48656c6c6f"; // Hexadecimal representation of "Hello"
        byte[] expected = {72, 101, 108, 108, 111}; // ASCII values of "Hello"
        byte[] result = EncryptionUtil.textToBytes(input);
        Assert.assertArrayEquals("The byte array should match the expected values.", expected, result);
    }

    @Test(expected = NumberFormatException.class)
    public void testTextToBytesInvalidInput() {
        String input = "InvalidHex"; // Invalid hexadecimal string
        EncryptionUtil.textToBytes(input);
    }

    @Test
    public void testBytesToText() {
        byte[] input = {72, 101, 108, 108, 111}; // ASCII values of "Hello"
        String expected = "48656c6c6f"; // Hexadecimal representation of "Hello"
        String result = EncryptionUtil.bytesToText(input);
        Assert.assertEquals("The hexadecimal string should match the expected value.", expected, result);
    }

    @Test
    public void testBytesToTextEmptyArray() {
        byte[] input = {};
        String result = EncryptionUtil.bytesToText(input);
        Assert.assertEquals("The result should be an empty string for an empty byte array.", "", result);
    }

    @Test
    public void testArrayTooSmall() {
        String[] array = {"-e", "Hello"};
        Assert.assertTrue("The array should be considered too small.", EncryptionUtil.arrayTooSmall(array, 1));
        Assert.assertFalse("The array should not be considered too small.", EncryptionUtil.arrayTooSmall(array, 0));
    }

    @Test
    public void testRemoveCommandLineOptions() {
        String[] args = {"-e", "Hello", "key=value"};
        String[] expected = {"-e", "Hello"};
        String[] result = EncryptionUtil.removeCommandLineOptions(args);
        Assert.assertArrayEquals("The command-line options should be removed.", expected, result);
    }

    @Test
    public void testExecuteInvalidArguments() {
        String[] args = {"-x", "InvalidOption"};
        // Redirect System.out for testing
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        int exitCode = EncryptionUtil.execute(args);
        Assert.assertTrue("Expected error code.", exitCode == AppUtil.EXIT_CODE_CLIENT_ERROR);

        String output = outputStream.toString().trim();
        Assert.assertTrue("The output should contain an error message.", output.startsWith("Utility to encrypt a string"));
    }
    
    @Test
    public void testExecuteEncryption() {
        String[] args = {"-e", "abc123"};
        // Redirect System.out for testing
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        int exitCode = EncryptionUtil.execute(args);
        Assert.assertTrue("Expected success.", exitCode == AppUtil.EXIT_CODE_NO_ERRORS);

        String output = outputStream.toString().trim();
        Assert.assertTrue("The output should contain the encrypted string.", 
                output.startsWith("The output string of encryption is:"));
        
        String[] outputParts = output.split("\n");
        Assert.assertTrue("The output should contain the encrypted string.", 
                outputParts.length == 2);
        String encryptedString = outputParts[1].trim();
        
        String[] args2 = {"-d", encryptedString};
        // Redirect System.out for testing
        ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream2));
        
        int exitCode2 = EncryptionUtil.execute(args2);
        Assert.assertTrue("Expected success.", exitCode2 == AppUtil.EXIT_CODE_NO_ERRORS);
        
        String output2 = outputStream2.toString().trim();
        Assert.assertTrue("The output should contain the decrypted string.", 
                output2.startsWith("The output string of decryption is:"));
        
        String[] outputParts2 = output2.split("\n");
        Assert.assertTrue("The output should contain the decrypted string.", 
                outputParts2.length == 2);
        String decryptedString = outputParts2[1].trim();
        
        Assert.assertEquals("The decrypted string should match the original string.", "abc123", decryptedString);
    }
    
    @Test
    public void testExecuteEncryptionWithKeyfile() {
        String keyfileName = getProperty("testfiles.dir") + File.separator + "testKeyfile";
        String[] keyfileArgs = {"-k", keyfileName};
        
        // Redirect System.out for testing
        ByteArrayOutputStream outputStreamKeyfile = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamKeyfile));
        
        int exitCodeKeyfile = EncryptionUtil.execute(keyfileArgs);
        Assert.assertTrue("Expected success.", exitCodeKeyfile == AppUtil.EXIT_CODE_NO_ERRORS);
        
        String[] args = {"-e", "abc123", keyfileName};
        // Redirect System.out for testing
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        int exitCode = EncryptionUtil.execute(args);
        Assert.assertTrue("Expected success.", exitCode == AppUtil.EXIT_CODE_NO_ERRORS);

        String output = outputStream.toString().trim();
        Assert.assertTrue("The output should contain the encrypted string.", 
                output.startsWith("The output string of encryption is:"));
        
        String[] outputParts = output.split("\n");
        Assert.assertTrue("The output should contain the encrypted string.", 
                outputParts.length == 2);
        String encryptedString = outputParts[1].trim();
        
        String[] args2 = {"-d", encryptedString, keyfileName};
        // Redirect System.out for testing
        ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream2));
        
        int exitCode2 = EncryptionUtil.execute(args2);
        Assert.assertTrue("Expected success.", exitCode2 == AppUtil.EXIT_CODE_NO_ERRORS);
        
        String output2 = outputStream2.toString().trim();
        Assert.assertTrue("The output should contain the decrypted string.", 
                output2.startsWith("The output string of decryption is:"));
        
        String[] outputParts2 = output2.split("\n");
        Assert.assertTrue("The output should contain the decrypted string.", 
                outputParts2.length == 2);
        String decryptedString = outputParts2[1].trim();
        
        Assert.assertEquals("The decrypted string should match the original string.", "abc123", decryptedString);
    }
}
