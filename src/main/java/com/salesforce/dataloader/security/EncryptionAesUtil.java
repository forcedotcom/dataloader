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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * com.salesforce.dataloader.security
 *
 * @author xbian
 */
public class EncryptionAesUtil {

    private static final Logger LOGGER = Logger.getLogger(EncryptionAesUtil.class);

    public enum OSType {
        Windows, MacOS, Linux, Other
    }

    ;
    protected static OSType detectedOS;

    /**
     * detect the operating system from the os.name System property and cache the result
     *
     * @returns - the operating system detected
     */
    public static OSType getOperatingSystemType() {
        if (detectedOS == null) {
            String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
            if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
                detectedOS = OSType.MacOS;
            } else if (OS.indexOf("win") >= 0) {
                detectedOS = OSType.Windows;
            } else if (OS.indexOf("nux") >= 0) {
                detectedOS = OSType.Linux;
            } else {
                detectedOS = OSType.Other;
            }
        }
        return detectedOS;
    }

    // Support single text encryption and decryption

    static public Cipher cipher;
    static public byte[] cipherKey;

    // 16 bytes was used for 128 bit AES encryption
    public static final int ENCRYPTION_KEY_LENGTH_IN_BYTES = 16;
    public static final String DEFAULT_KEYFILE_NAME = "dataLoader.key";

    static {
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            detectedOS = getOperatingSystemType();

        } catch (Exception e) {
            LOGGER.error("Fail to initialize encryption: " + e.getMessage());
            throw new RuntimeException("Fail to initialize encryption:  ", e);
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
        // If a call to setSeed had not occurred previously,
        // the first call to this method forces this SecureRandom object to seed itself.
        // This self-seeding will not occur if setSeed was previously called.
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[ENCRYPTION_KEY_LENGTH_IN_BYTES];
        random.nextBytes(bytes);
        return bytes;
    }

    public String createUserProfileKeyName() {
        String path = Paths.get(System.getProperty("user.home"), ".dataloader").toString();
        File customDir = new File(path);

        if (customDir.exists()) {
            LOGGER.debug(customDir + " exists");
        } else if (customDir.mkdirs()) {
            if (detectedOS == OSType.MacOS || detectedOS == OSType.Linux) {
                // set all reading to false
                customDir.setReadable(false, false);
                // only owner can read
                customDir.setReadable(true, true);
            }
            LOGGER.info(customDir + " was created");

        } else {
            LOGGER.info(customDir + " was not created");
            throw new RuntimeException("Cannot create directory:" + path);
        }
        return Paths.get(path, DEFAULT_KEYFILE_NAME).toString();
    }

    // If given path not existing, create one.
    public String createKeyFileIfNotExisting(String filePath) throws GeneralSecurityException {
        if (filePath == null || filePath.isEmpty() || !Files.exists(Paths.get(filePath))) {
            // no valid file path provided, check if default one exists
            if (filePath == null || filePath.isEmpty()) {
                filePath = createUserProfileKeyName();
            }
            if (!Files.exists(Paths.get(filePath))) {
                byte[] key = generateEncryptionKey();
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    fos.write(key);
                } catch (IOException io) {
                    throw new GeneralSecurityException("Failed to open file:" + filePath, io);
                }
                // Windows platform is already readable only to owner.
                if (detectedOS == OSType.MacOS || detectedOS == OSType.Linux) {
                    File file = new File(filePath);
                    // set all reading to false
                    file.setReadable(false, false);
                    // set Owner reading to true
                    file.setReadable(true, true);
                }
            }
        }
        setCipherKeyFromFilePath(filePath);
        return filePath;
    }

    public void resetCipherKey() {
        cipherKey = null;
    }

    public void setCipherKeyFromFilePath(String filePath) throws GeneralSecurityException {
        byte[] data = new byte[1024];
        try (FileInputStream fis = new FileInputStream(filePath)) {
            int size = fis.read(data);
            if (size < ENCRYPTION_KEY_LENGTH_IN_BYTES)
                throw new GeneralSecurityException("Keyfile content is too short:" + filePath);
            else {
                cipherKey = new byte[ENCRYPTION_KEY_LENGTH_IN_BYTES];
                System.arraycopy(data, 0, cipherKey, 0, ENCRYPTION_KEY_LENGTH_IN_BYTES);
            }
        } catch (IOException io) {
            throw new GeneralSecurityException("Failed to open file: " + filePath, io);
        }
    }

    private void ensureKeyIsSet() throws IOException, GeneralSecurityException {
        if (cipherKey != null) {
            return;
        }
        // if no key was set up already, create one
        createKeyFileIfNotExisting(null);

    }

    public String encryptMsg(String msg) throws GeneralSecurityException {
        try {
            ensureKeyIsSet();
            return EncryptionUtil.bytesToText(encryptMsg(msg, cipherKey));
        } catch (Exception e) {
            LOGGER.error("Fail to encrypt message: " + e.getMessage());
            throw new GeneralSecurityException("Error to encrypt message: ", e);
        }
    }

    public String decryptMsg(String cipherMsgString) throws GeneralSecurityException {
        try {
            ensureKeyIsSet();
            byte[] cipherMsg = EncryptionUtil.textToBytes(cipherMsgString);
            return decryptMsg(cipherMsg, cipherKey);
        } catch (Exception e) {
            LOGGER.error("Fail to decrypt message: " + e.getMessage());
            throw new GeneralSecurityException("Error to encrypt message: ", e);
        }
    }

    synchronized public byte[] encryptMsg(String msg, byte[] encryptionKey) throws GeneralSecurityException, UnsupportedEncodingException {

        if (encryptionKey == null || encryptionKey.length != ENCRYPTION_KEY_LENGTH_IN_BYTES)
            throw new GeneralSecurityException("Encryption key is null or has invalid length");
        SecretKeySpec key = new SecretKeySpec(encryptionKey, "AES");
        byte[] ivBytes = generateEncryptionKey();
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(msg.getBytes());
        return concatenateByteArray(ivBytes, cipherText);
    }

    synchronized public String decryptMsg(byte[] cipherMsg, byte[] encryptionKey) throws GeneralSecurityException, UnsupportedEncodingException {
        if (encryptionKey == null)
            throw new GeneralSecurityException("Encryption key is null or has invalid length");
        SecretKeySpec key = new SecretKeySpec(encryptionKey, "AES");
        byte[] iv = extractIvBytes(cipherMsg);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] cipherContent = extractCipherContent(cipherMsg);
        return new String(cipher.doFinal(cipherContent));
    }
}
