package com.bitgo.crypto.encryption;

import android.util.Base64;

import com.bitgo.crypto.encoding.HexBinary;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by arik on 12/14/15.
 */
public class BitGoAES extends ReactContextBaseJavaModule {

    private static final String ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    public BitGoAES(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @ReactMethod
    public static String generateInitializationVector() { // 128 bits or 32 bytes

        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null; // should never happen due to fixed algorithm
        }
        keyGen.init(128);
        SecretKey secretKey = keyGen.generateKey();

        byte[] key = secretKey.getEncoded();
        return HexBinary.binaryToHex(key);

    }

    @ReactMethod
    public static String generateKey() { // 256 bits or 64 bytes

        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null; // should never happen due to fixed algorithm
        }
        keyGen.init(256);
        SecretKey secretKey = keyGen.generateKey();

        byte[] key = secretKey.getEncoded();
        return HexBinary.binaryToHex(key);

    }

    @ReactMethod
    public static String encrypt(String data, String key, String initializationVector) throws InvalidKeyException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException { // returns a base64-encoded string

        TyrannyOverride.overrideTyranny();

        SecretKeySpec aesKeySpec = new SecretKeySpec(HexBinary.hexToBinary(key), ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(HexBinary.hexToBinary(initializationVector));

        // Encrypt cipher
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            return null; // should never happen due to fixed cipher
        }
        cipher.init(Cipher.ENCRYPT_MODE, aesKeySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(data.getBytes());

        return Base64.encodeToString(encrypted, Base64.DEFAULT);

    }

    @ReactMethod
    public static String decrypt(String base64Data, String key, String initializationVector) throws InvalidAlgorithmParameterException, InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException { // accepts a base64-encoded string

        TyrannyOverride.overrideTyranny();

        SecretKeySpec aesKeySpec = new SecretKeySpec(HexBinary.hexToBinary(key), ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(HexBinary.hexToBinary(initializationVector));

        // Decrypt cipher
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            return null; // should never happen due to fixed cipher
        }
        cipher.init(Cipher.DECRYPT_MODE, aesKeySpec, ivSpec);
        byte[] original = cipher.doFinal(Base64.decode(base64Data, Base64.DEFAULT));

        return new String(original, Charset.forName("UTF-8"));

    }

    @Override
    public String getName() {
        return "BitGoAES";
    }
}
