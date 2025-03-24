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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.salesforce.dataloader.util.AppUtil;

public class EncryptionUtil {

    /**
     * Convert text to bytes
     *
     * @return bytes for the input text
     */
    public static byte[] textToBytes(String text) {
        int length = text.length() / 2;
        byte[] byteArray = new byte[length];
        for (int i = 0; i < length; i++) {
            int value = Integer.decode("0x" + text.substring(i * 2, (i * 2) + 2));
            if (value > 127) {
                value = (value - 127) * -1;
            }
            byteArray[i] = (byte) value;
        }
        return byteArray;
    }

    /**
     * Convert bytes to text
     *
     * @return text for the input bytes
     */
    public static String bytesToText(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            int num = b < 0 ? 127 + (b * -1) : b;
            String hex = Integer.toHexString(num);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    private static void printUsage() {
        String usage = "\nUtility to encrypt a string based on a static or a provided key\n"
                + "Options (mutually exclusive - use one at a time): \n"
                + "\t-e <plain text> [keyfile]                Encrypt a plain text value using optional keyfile\n"
                + "\t-d <encryptText> [keyfile]               Decrypt an encrypted text back to plain text value using optional keyfile\n"
                + "\t-k [keyfile]                             Generate keyfile with optional path to keyfile\n"
                + "\n keyfile defaults to $HOME(%userprofile% on Windows)/.dataloader/dataLoader.key if not specified";
        System.out.println(usage);
    }

    public static void main(String[] args) {
        System.exit(execute(args));
    }

    public static int execute(String[] args) {
        try {
            if (args.length < 1) {
                printUsage();
                return AppUtil.EXIT_CODE_CLIENT_ERROR;
            }

            args = removeCommandLineOptions(args);

            String operation = "";
            for (String arg : args) {
                if (arg.startsWith("-") && (arg.equals("-e") || arg.equals("-d") || arg.equals("-k"))) {
                    operation = arg;
                    break;
                }
            }

            if (operation.isEmpty()) {
                throw new IllegalArgumentException("Unsupported operation");
            }

            int operationArgIndex = Arrays.asList(args).indexOf(operation);
            if (operationArgIndex == -1 || (operationArgIndex + 1 >= args.length && !operation.equals("-k"))) {
                System.out.println("Option '" + operation + "' requires at least one parameter. Please check usage.\n");
                throw new IllegalArgumentException();
            }

            String param = args.length > operationArgIndex + 1 ? args[operationArgIndex + 1] : null;
            switch (operation) {
                case "-e":
                    handleEncryption(param, args, operationArgIndex);
                    break;
                case "-d":
                    handleDecryption(param, args, operationArgIndex);
                    break;
                case "-k":
                    handleKeyFileCreation(args, operationArgIndex);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported operation");
            }
            return AppUtil.EXIT_CODE_NO_ERRORS;
        } catch (IllegalArgumentException e) {
            printUsage();
            return AppUtil.EXIT_CODE_CLIENT_ERROR;
        }
    }

    private static void handleEncryption(String param, String[] args, int operationArgIndex) {
        if (param == null) {
            throw new IllegalArgumentException("Encryption requires a plain text value.");
        }
        try {
            EncryptionAesUtil enc = new EncryptionAesUtil();
            if (args.length > operationArgIndex + 2) {
                enc.setCipherKeyFromFilePath(args[operationArgIndex + 2]);
            }
            String encrypted = enc.encryptMsg(param);
            System.out.println("The output string of encryption is: \n" + encrypted);
        } catch (Exception e) {
            System.out.println("Error during encryption: " + e.getMessage());
            throw new IllegalArgumentException();
        }
    }

    private static void handleDecryption(String param, String[] args, int operationArgIndex) {
        if (param == null) {
            throw new IllegalArgumentException("Decryption requires an encrypted text value.");
        }
        try {
            EncryptionAesUtil enc = new EncryptionAesUtil();
            if (args.length > operationArgIndex + 2) {
                enc.setCipherKeyFromFilePath(args[operationArgIndex + 2]);
            }
            String plainText = enc.decryptMsg(param);
            System.out.println("The output string of decryption is: \n" + plainText);
        } catch (Exception e) {
            System.out.println("Error during decryption: " + e.getMessage());
            throw new IllegalArgumentException();
        }
    }

    private static void handleKeyFileCreation(String[] args, int operationArgIndex) {
        try {
            EncryptionAesUtil enc = new EncryptionAesUtil();
            String filePath = enc.createKeyFileIfNotExisting(args.length > operationArgIndex + 1 ? args[operationArgIndex + 1] : null);
            System.out.println("Keyfile \"" + filePath + "\" was created!");
        } catch (Exception e) {
            System.out.println("Error during key file creation: " + e.getMessage());
            throw new IllegalArgumentException();
        }
    }

    public static boolean arrayTooSmall(String[] array, int index) {
        return index >= array.length - 1;
    }

    public static String[] removeCommandLineOptions(String[] args) {
        List<String> remainingArgs = new ArrayList<>();
        for (String arg : args) {
            if (!arg.contains("=")) {
                remainingArgs.add(arg);
            }
        }
        return remainingArgs.toArray(new String[0]);
    }
}