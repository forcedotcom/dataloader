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
package com.salesforce.dataloader.security;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * com.salesforce.dataloader.security
 *
 * @author xbian
 */
public class EncryptionAesUtil {

    private static final Logger LOGGER = Logger.getLogger(EncryptionAesUtil.class);

    // Support single text encryption and decryption

    static public Cipher cipher;
    static public byte[] cipherKey;


    public static final int ENCRYPTION_KEY_LENGTH_IN_BYTES = 16;
    public static final String DEFAULT_KEYFILE_NAME = "dataLoader.key";

    static {
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        } catch (Exception e) {
            LOGGER.error("Fail to initialize encryption: " + e.getMessage());
            throw new  RuntimeException("Fail to initialize encryption:  ", e);
        }
    }


    private byte[] extractIvBytes(byte[] cipheredText) {
        byte[] iv = new byte[ENCRYPTION_KEY_LENGTH_IN_BYTES];
        System.arraycopy(cipheredText, 0, iv, 0, ENCRYPTION_KEY_LENGTH_IN_BYTES);
        return iv;
    }

    private byte[] extractCipherContent(byte[] cipheredText) {
        byte[] cipheredPassword = new byte[cipheredText.length - ENCRYPTION_KEY_LENGTH_IN_BYTES];
        System.arraycopy(cipheredText, ENCRYPTION_KEY_LENGTH_IN_BYTES, cipheredPassword, 0, cipheredText.length - ENCRYPTION_KEY_LENGTH_IN_BYTES);
        return cipheredPassword;

    }

    private byte[] concatenateByteArray(byte[] a, byte[] b) {

        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public byte[] generateEncryptionKey() {
        // The secure random uses threadId and tick to make sure it is not repeatable
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[ENCRYPTION_KEY_LENGTH_IN_BYTES];
        random.nextBytes(bytes);
        return bytes;
    }

    public String createUserProfileKeyName() {
        String path = Paths.get(System.getProperty("user.home"), "dataloader").toString();
        File customDir = new File(path);

        if (customDir.exists()) {
            LOGGER.info(customDir + " exists");
        } else if (customDir.mkdirs()) {
            LOGGER.info(customDir + " was created");
        } else {
            LOGGER.info(customDir + " was not created");
            throw new RuntimeException("Cannot create directory:" + path);
        }
        return Paths.get(path, DEFAULT_KEYFILE_NAME).toString();
    }

    public String createKeyFile(String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            filePath = createUserProfileKeyName();
        }
        byte[] key = generateEncryptionKey();
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(key);
        }
        return filePath;
    }

    public void resetCipherKey(){
        cipherKey = null;
    }

    public String setCipherKeyFromFilePath(String filePath) throws IOException, GeneralSecurityException {
        byte[] data = new byte[1024];
        try (FileInputStream fis = new FileInputStream(filePath)) {
            int size = fis.read(data);
            if (size < ENCRYPTION_KEY_LENGTH_IN_BYTES)
                throw new GeneralSecurityException("Keyfile content is too short:" + filePath);
            else
            {
                cipherKey = new byte[ENCRYPTION_KEY_LENGTH_IN_BYTES];
                System.arraycopy(data, 0, cipherKey, 0, ENCRYPTION_KEY_LENGTH_IN_BYTES);
            }
            return new String(cipherKey, "UTF-8");
        }
    }

    public String encryptMsg(String msg) throws GeneralSecurityException {
        try {
            return EncryptionUtil.bytesToText(encryptMsg(msg, cipherKey));
        } catch (Exception e) {
            LOGGER.error("Fail to encrypt message: " + e.getMessage());
            throw new  GeneralSecurityException("Error to encrypt message: ", e);
        }
    }

    public String decryptMsg(String cipherMsgString) throws GeneralSecurityException {
        try {
            byte[] cipherMsg = EncryptionUtil.textToBytes(cipherMsgString);
            return decryptMsg(cipherMsg, cipherKey);
        } catch (Exception e) {
            LOGGER.error("Fail to decrypt message: " + e.getMessage());
            throw new GeneralSecurityException("Error to encrypt message: ", e);
        }
    }

    synchronized public byte[] encryptMsg(String msg, byte[] encryptionKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, InvalidParameterSpecException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {

        if(encryptionKey == null)
            return msg.getBytes("UTF-8");
        SecretKeySpec key = new SecretKeySpec(encryptionKey, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] cipherText = cipher.doFinal(msg.getBytes("UTF-8"));
        byte[] iv = cipher.getIV();
        return concatenateByteArray(iv, cipherText);
    }

    synchronized public String decryptMsg(byte[] cipherMsg, byte[] encryptionKey) throws InvalidKeySpecException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException, UnsupportedEncodingException {
        if (encryptionKey == null)
            return new String(cipherMsg,"UTF-8");
        SecretKeySpec key = new SecretKeySpec(encryptionKey, "AES");
        byte[] iv = extractIvBytes(cipherMsg);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] cipherContent = extractCipherContent(cipherMsg);
        String plaintext = new String(cipher.doFinal(cipherContent), "UTF-8");
        return plaintext;
    }
}
