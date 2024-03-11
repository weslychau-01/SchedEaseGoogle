/**
 * Encryption Utility class
 * Implements AES Encryption with MongoDB
 * 
 * @author Wesly Chau Li Zhan wesly.chau.li.zhan.01@gmail.com
 * @version 1.0
 * 
 */

package com.Encryption;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;


import java.util.*;

public class EncryptionUtil {

    private static final String ALGORITHM = "AES";

    public static SecretKey generateSecretKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(256);

        return keyGenerator.generateKey();
    }

    public static String serialiseSecretString(SecretKey k) throws Exception {
        return Base64.getEncoder().encodeToString(k.getEncoded());
    }

    public static SecretKey getSecretKeyFromSecretString(String secretString) {
        byte[] dKey = Base64.getDecoder().decode(secretString);
        return new SecretKeySpec(dKey, 0, dKey.length, ALGORITHM);
    }

    public static String encrypt(String p, SecretKey k) throws Exception {

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, k);
        byte[] eData = cipher.doFinal(p.getBytes());
        return Base64.getEncoder().encodeToString(eData);
    }

    public static String decrypt(String c, SecretKey k) throws Exception {

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, k);
        byte[] dData = cipher.doFinal(Base64.getDecoder().decode(c));
        return new String(dData);
    }
    
}
