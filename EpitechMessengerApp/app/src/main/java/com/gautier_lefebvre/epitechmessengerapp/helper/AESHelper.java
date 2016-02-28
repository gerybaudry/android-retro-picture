package com.gautier_lefebvre.epitechmessengerapp.helper;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class AESHelper {
    private static final String algorithm = "AES/ECB/NoPadding";

    public static SecretKey generateKey() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(128);
        return kg.generateKey();
    }

    public static byte[] encrypt(byte[] message, SecretKey sKey) throws Exception {
        Cipher cipher = Cipher.getInstance(AESHelper.algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, sKey);
        return cipher.doFinal(message);
    }

    public static String decrypt(byte[] encryptedMessage, SecretKey sKey) throws Exception {
        Cipher cipher = Cipher.getInstance(AESHelper.algorithm);
        cipher.init(Cipher.DECRYPT_MODE, sKey);
        return new String(cipher.doFinal(encryptedMessage));
    }
}
