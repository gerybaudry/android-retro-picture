package com.gautier_lefebvre.epitechmessengerapp.business;

import android.util.Base64;

import com.gautier_lefebvre.epitechmessengerapp.exception.RSAException;
import com.gautier_lefebvre.epitechmessengerapp.helper.AESHelper;
import com.gautier_lefebvre.epitechmessengerapp.helper.RSAHelper;

import java.security.PublicKey;

import javax.crypto.SecretKey;

public class MessageService {
    public static String encryptRSA(String message, PublicKey key) throws RSAException {
        byte[] bytes = RSAHelper.encrypt(message.getBytes(), key);
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static String encryptRSA(byte[] message, PublicKey key) throws RSAException {
        byte[] bytes = RSAHelper.encrypt(message, key);
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static String encryptAES(String message, SecretKey key) throws Exception {
        return MessageService.encryptAES(message.getBytes(), key);
    }

    public static String encryptAES(byte[] message, SecretKey key) throws Exception {
        int diff = (16 - (message.length % 16));
        byte[] padded = new byte[message.length + diff];
        System.arraycopy(message, 0, padded, 0, message.length);
        for (int i = message.length; i < padded.length ; ++i) {
            padded[i] = 0;
        }
        return Base64.encodeToString(AESHelper.encrypt(padded, key), Base64.DEFAULT);
    }

    public static String decryptAES(String message, SecretKey key) throws Exception {
        byte[] msg = Base64.decode(message, Base64.DEFAULT);
        return AESHelper.decrypt(msg, key);
    }

}
