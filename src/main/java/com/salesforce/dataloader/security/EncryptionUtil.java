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
package com.salesforce.dataloader.security;

import org.apache.log4j.Logger;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;

public class EncryptionUtil {

    private static final Logger LOGGER = Logger.getLogger(EncryptionUtil.class);
    private Key gKey = null;
    private String gCipherSeed = "namastearrigato";
    private String gCipherKey = "51dda30be226233d";

    public EncryptionUtil() {
    }

    /**
     *
     */
    synchronized public void resetCryptoKey() {
        gKey = null;
        gCipherKey = null;
    }

    /**
     * @param sKey
     */
    synchronized public void setCipherKey(String sKey) {
        gCipherKey = sKey;
    }

    /**
     * Convert text to bytes
     *
     * @param text
     * @return bytes for the input text
     */
    private static byte[] textToBytes(String text) {
        byte[] baBytes = new byte[text.length() / 2];
        for (int j = 0; j < text.length() / 2; j++) {
            Integer tmpInteger = Integer.decode(new String("0x" + text.substring(j * 2, (j * 2) + 2)));
            int tmpValue = tmpInteger.intValue();
            if (tmpValue > 127) // fix negatives
            {
                tmpValue = (tmpValue - 127) * -1;
            }
            tmpInteger = new Integer(tmpValue);
            baBytes[j] = tmpInteger.byteValue();
        }

        return baBytes;
    }

    /**
     * Convert bytes to text
     *
     * @param bytes
     * @return text for the input bytes
     */
    private static String bytesToText(byte[] bytes) {
        StringBuffer sb = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            int num = bytes[i];
            if (num < 0) num = 127 + (num * -1); // fix negative back to positive
            String hex = Integer.toHexString(num);
            if (hex.length() == 1) {
                hex = "0" + hex; // ensure 2 digits
            }
            sb.append(hex);
        }

        return sb.toString();
    }

    /**
     * Create a key for encryption and decryption
     *
     * @return Key
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws InvalidKeyException
     */
    synchronized private Key getCryptoKey() throws NoSuchAlgorithmException, InvalidKeySpecException,
    InvalidKeyException {
        if (gKey != null) return gKey;

        String sKeyText = gCipherKey != null ? gCipherKey : generateKey(gCipherSeed);
        byte[] baKeyBytes = textToBytes(sKeyText);

        try {
            SecretKeyFactory desFactory = SecretKeyFactory.getInstance("DES");
            gKey = desFactory.generateSecret(new DESKeySpec(baKeyBytes));
        } catch (InvalidKeyException e) {
            throw e;
        } catch (InvalidKeySpecException e) {
            throw e;
        }

        return gKey;
    }

    /**
     * Create the cipher object for encryption or decryption. We create a DES cipher with PKCS#5 padding
     *
     * @return Cipher
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
    private static Cipher createCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
        try {
            return Cipher.getInstance("DES/ECB/PKCS5Padding");
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (NoSuchPaddingException e) {
            throw e;
        }
    }

    /**
     * Encrypt a string using whatever is the default encryption technique for the system.
     *
     * @param clearText
     * @return The encrypted string
     * @throws GeneralSecurityException
     */
    public String encryptString(String clearText) throws GeneralSecurityException {
        if (clearText == null) return clearText;
        Key oKey = getCryptoKey();
        Cipher oCipher = createCipher();
        byte[] encryptedBytes;

        if (oCipher == null) { return clearText; }

        byte[] inputBytes = clearText.getBytes();

        try {
            oCipher.init(Cipher.ENCRYPT_MODE, oKey);
        } catch (InvalidKeyException e) {
            throw e;
        }

        try {
            encryptedBytes = oCipher.doFinal(inputBytes);
        } catch (IllegalBlockSizeException e) {
            throw e;
        } catch (BadPaddingException e) {
            throw e;
        }

        return bytesToText(encryptedBytes);
    }

    /**
     * Decrypt a string using whatever is the default encryption technique for the system.
     *
     * @param cipherText
     * @return The decrypted string
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     */
    public String decryptString(String cipherText) throws GeneralSecurityException {
        if (cipherText == null) return cipherText;
        Key key = getCryptoKey();
        Cipher cipher = createCipher();

        if (cipher == null) { return cipherText; }

        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            throw e;
        }

        byte[] baCipherBytes = textToBytes(cipherText);

        try {
            return new String(cipher.doFinal(baCipherBytes));
        } catch (BadPaddingException e) {
            throw e;
        } catch (IllegalBlockSizeException e) {
            throw e;
        }
    }

    /**
     * Create a DES key and return a string version of the raw bits.
     *
     * @param seed
     * @return A key for encryption
     * @throws NoSuchAlgorithmException
     */
    public static String generateKey(String seed) throws NoSuchAlgorithmException {

        byte[] seedBytes = seed.getBytes();
        SecureRandom random = new SecureRandom(seedBytes);
        KeyGenerator keygen = null;
        try {
            keygen = KeyGenerator.getInstance("DES");
        } catch (NoSuchAlgorithmException e) {
            throw e;
        }

        keygen.init(random);
        Key key = keygen.generateKey();

        byte[] keyBytes = key.getEncoded();

        return bytesToText(keyBytes);
    }

    /**
     * @param keyFilename
     * @throws IOException
     */
    public void setCipherKeyFromFilePath(String keyFilename) throws IOException {
        try {
            File keyFile = new File(keyFilename);
            if (keyFile.exists() && keyFile.canRead()) {
                FileReader fr = new FileReader(keyFile);
                BufferedReader br = new BufferedReader(fr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    break;
                }
                if (line != null) {
                    resetCryptoKey(); // Reset Key
                    setCipherKey(line);
                }
            } else {
                throw new IOException("Cannot Access Key File: " + keyFilename);
            }
        } catch (IOException e) {
            throw e;
        }
    }

    private static void printUsage() {
        LOGGER.info("\nUtility to encrypt a string based on a static or a provided key");
        LOGGER.info("Options (mutually exclusive - use one at a time): \n"
                + "\t-g <seed text>                                 Generate key based on seed\n"
                + "\t-v <encrypted> <decrypted value> [Path to Key] Validate whether decryption of encrypted value matches the decrypted value, optionally provide key file\n"
                + "\t-e <plain text> [Path to Key]                  Encrypt a plain text value, optionally provide key file (generate key using option -g)");
    }

    public static void main(String[] args) {
        // args[0] = input data, required
        // args[1] = key (optional)
        if (args.length < 1) {
            printUsage();
            System.exit(-1);
        }

        int i = 0;
        String option = args[i];
        if (option.length() < 2 || option.charAt(0) != '-') {
            LOGGER.info("Invalid option format: " + args[i]);
            System.exit(-1);
        }
        // make sure enough arguments are provided
        if (arrayTooSmall(args, i)) {
            LOGGER.info("Option '" + option + "' requires at least one parameter.  Please check usage.\n");
            printUsage();
            System.exit(-1);
        }
        // advance index to param and save the param value
        String param = args[++i];
        switch (option.charAt(1)) {
            case 'g':
                try {
                    String key = generateKey(param);
                    LOGGER.info(key);
                } catch (NoSuchAlgorithmException e) {
                    LOGGER.error("Error generating key: " + e.getMessage());
                    System.exit(-1);
                }
                break;
            case 'v':
                // verify if encrypted value matches the encrypted the encrypted
                // cleartext
                // if optional key file is provided, use it
                if (arrayTooSmall(args, i)) {
                    LOGGER.info("Please provide decrypted value to validate against");
                    printUsage();
                    System.exit(-1);
                }
                String decryptExpected = args[++i];
                EncryptionUtil dec = new EncryptionUtil();
                if (!arrayTooSmall(args, i)) {
                    try {
                        dec.setCipherKeyFromFilePath(args[++i]);
                    } catch (IOException e) {
                        LOGGER.error("Error setting the key from file: "
                                + args[i] + ", error: " + e.getMessage());
                        System.exit(-1);
                    }
                }
                try {
                    String decrypted = dec.decryptString(param);
                    LOGGER.info("Decryption of encrypted value "
                            + (decryptExpected.equals(decrypted) ? "MATCHES"
                            : "DOES NOT MATCH") + " the expected value");
                } catch (GeneralSecurityException e) {
                    LOGGER.error("Error decrypting string: " + param
                            + ", error: " + e.getMessage());
                    System.exit(-1);
                }
                break;
            case 'e':
                // if optional key file is provided, use it
                EncryptionUtil enc = new EncryptionUtil();
                if (!arrayTooSmall(args, i)) {
                    String keyFilename = args[++i];
                    File keyFile = new File(keyFilename);
                    if (!keyFile.exists() && !keyFile.canRead()) {
                        LOGGER.warn("Please ensure that the key file '"
                                + keyFilename + "' exists and is readable");
                        printUsage();
                        System.exit(-1);
                    }
                    try {
                        enc.setCipherKeyFromFilePath(keyFilename);
                    } catch (IOException e) {
                        LOGGER.error("Error setting the key from file: "
                                + keyFilename + ", error: " + e.getMessage());
                        System.exit(-1);
                    }
                }
                try {
                    // encrypt the given string and output to STDOUT
                    String encrypted;
                    encrypted = enc.encryptString(param);
                    LOGGER.info(encrypted);
                } catch (GeneralSecurityException e) {
                    LOGGER.error("Error encrypting string: " + param
                            + ", error: " + e.getMessage());
                    System.exit(-1);
                }
                break;
            default:
                LOGGER.error("Unsupported option: " + option);
                printUsage();
                System.exit(-1);
        }
    }

    /**
     * @param array
     * @param index
     * @return true if array is too small to increment the index
     */
    private static boolean arrayTooSmall(String[] array, int index) {
        return (index + 1) > (array.length - 1);
    }
}
