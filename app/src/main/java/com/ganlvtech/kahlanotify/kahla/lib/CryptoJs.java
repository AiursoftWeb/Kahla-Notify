package com.ganlvtech.kahlanotify.kahla.lib;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @link https://stackoverflow.com/questions/27220297/what-are-the-aes-parameters-used-and-steps-performed-internally-by-crypto-js-whi/27250883#27250883
 */
public class CryptoJs {
    public static String aesEncrypt(byte[] data, String passwordHex) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, UnsupportedEncodingException {
        // Salted__
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[8];
        random.nextBytes(salt);
        byte[] password = passwordHex.getBytes();
        int keySize = 8; // 8 words = 256-bit
        int ivSize = 4; // 4 words = 128-bit
        byte[] javaKey = new byte[keySize * 4];
        byte[] javaIv = new byte[ivSize * 4];
        evpKDF(password, keySize, ivSize, salt, javaKey, javaIv);
        Cipher aesCipherForEncryption = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKey = new SecretKeySpec(javaKey, "AES");
        IvParameterSpec ivspec = new IvParameterSpec(javaIv);
        aesCipherForEncryption.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
        byte[] cipherText = aesCipherForEncryption.doFinal(data);
        ByteBuffer buf = ByteBuffer.wrap(new byte[cipherText.length + 16]);
        buf.put("Salted__".getBytes());
        buf.put(salt);
        buf.put(cipherText);
        return Base64.encodeToString(buf.array(), Base64.NO_WRAP);
    }

    public static byte[] aesDecrypt(String cipherTextBase64, String passwordHex) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, UnsupportedEncodingException {
        byte[] cipherText = Base64.decode(cipherTextBase64, Base64.DEFAULT);
        // Salted__
        if (!(cipherText[0] == 0x53 && cipherText[1] == 0x61 && cipherText[2] == 0x6c && cipherText[3] == 0x74
                && cipherText[4] == 0x65 && cipherText[5] == 0x64 && cipherText[6] == 0x5f && cipherText[7] == 0x5f)) {
            return new byte[0];
        }
        byte[] salt = Arrays.copyOfRange(cipherText, 8, 16);
        cipherText = Arrays.copyOfRange(cipherText, 16, cipherText.length);
        byte[] password = passwordHex.getBytes();
        int keySize = 8; // 8 words = 256-bit
        int ivSize = 4; // 4 words = 128-bit
        byte[] javaKey = new byte[keySize * 4];
        byte[] javaIv = new byte[ivSize * 4];
        evpKDF(password, keySize, ivSize, salt, javaKey, javaIv);
        Cipher aesCipherForEncryption = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKey = new SecretKeySpec(javaKey, "AES");
        IvParameterSpec ivspec = new IvParameterSpec(javaIv);
        aesCipherForEncryption.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
        return aesCipherForEncryption.doFinal(cipherText);
    }

    public static byte[] evpKDF(byte[] password, int keySize, int ivSize, byte[] salt, byte[] resultKey, byte[] resultIv) throws NoSuchAlgorithmException {
        return evpKDF(password, keySize, ivSize, salt, 1, "MD5", resultKey, resultIv);
    }

    public static byte[] evpKDF(byte[] password, int keySize, int ivSize, byte[] salt, int iterations, String hashAlgorithm, byte[] resultKey, byte[] resultIv) throws NoSuchAlgorithmException {
        int targetKeySize = keySize + ivSize;
        byte[] derivedBytes = new byte[targetKeySize * 4];
        int numberOfDerivedWords = 0;
        byte[] block = null;
        MessageDigest hasher = MessageDigest.getInstance(hashAlgorithm);
        while (numberOfDerivedWords < targetKeySize) {
            if (block != null) {
                hasher.update(block);
            }
            hasher.update(password);
            block = hasher.digest(salt);
            hasher.reset();

            // Iterations
            for (int i = 1; i < iterations; i++) {
                block = hasher.digest(block);
                hasher.reset();
            }

            System.arraycopy(block, 0, derivedBytes, numberOfDerivedWords * 4,
                    Math.min(block.length, (targetKeySize - numberOfDerivedWords) * 4));

            numberOfDerivedWords += block.length / 4;
        }

        System.arraycopy(derivedBytes, 0, resultKey, 0, keySize * 4);
        System.arraycopy(derivedBytes, keySize * 4, resultIv, 0, ivSize * 4);

        return derivedBytes; // key + iv
    }

    /**
     * Copied from http://stackoverflow.com/a/140861
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
