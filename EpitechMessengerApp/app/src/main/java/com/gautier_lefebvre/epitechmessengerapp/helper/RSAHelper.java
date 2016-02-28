package com.gautier_lefebvre.epitechmessengerapp.helper;

import android.util.Base64;

import com.gautier_lefebvre.epitechmessengerapp.exception.RSAException;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class RSAHelper {
    private static final String algorithm = "RSA";

    public static byte[] encrypt(byte[] message, PublicKey rsaPublicKey) throws RSAException {
        try {
            Cipher cipher = Cipher.getInstance(RSAHelper.algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
            return cipher.doFinal(message);
        } catch (Exception e) {
            throw new RSAException();
        }
    }

    public static PublicKey getKeyFromString(String keyStr) throws RSAException {
        try {
            byte[] keyBytes = Base64.decode(keyStr.getBytes("utf-8"), Base64.DEFAULT);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(RSAHelper.algorithm);
            return keyFactory.generatePublic(spec);
        } catch (UnsupportedEncodingException|NoSuchAlgorithmException|InvalidKeySpecException e) {
            throw new RSAException();
        }
    }
}
