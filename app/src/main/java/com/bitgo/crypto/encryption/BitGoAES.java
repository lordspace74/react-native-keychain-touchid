package com.bitgo.crypto.encryption;

import android.telecom.Call;
import android.util.Base64;

import com.bitgo.crypto.encoding.HexBinary;
import com.facebook.react.bridge.Callback;
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
    public static void generateInitializationVector(Callback success, Callback error) { // 128 bits or 32 bytes

        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            error.invoke("" + e);
        }
        keyGen.init(128);
        SecretKey secretKey = keyGen.generateKey();

        byte[] key = secretKey.getEncoded();
        success.invoke(HexBinary.binaryToHex(key));

    }

    @ReactMethod
    public static void generateKey(Callback success, Callback error) { // 256 bits or 64 bytes

        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            error.invoke("" + e);
        }
        keyGen.init(256);
        SecretKey secretKey = keyGen.generateKey();

        byte[] key = secretKey.getEncoded();
        success.invoke(HexBinary.binaryToHex(key));

    }

    @ReactMethod
    public static void encrypt(String data, String key, String initializationVector, Callback success, Callback error) { // returns a base64-encoded string

        try {
            TyrannyOverride.overrideTyranny();

            SecretKeySpec aesKeySpec = new SecretKeySpec(HexBinary.hexToBinary(key), ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(HexBinary.hexToBinary(initializationVector));

            // Encrypt cipher
            Cipher cipher = null;
            try {
                cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                e.printStackTrace();
                error.invoke("" + e);
            }
            cipher.init(Cipher.ENCRYPT_MODE, aesKeySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(data.getBytes());

            success.invoke(Base64.encodeToString(encrypted, Base64.DEFAULT));
        } catch (InvalidKeyException|
                InvalidAlgorithmParameterException|
                BadPaddingException|
                IllegalBlockSizeException e) {
            e.printStackTrace();
            error.invoke("" + e);
        }

    }

    @ReactMethod
    public static void decrypt(String base64Data, String key, String initializationVector, Callback success, Callback error) {

        try {
            TyrannyOverride.overrideTyranny();

            SecretKeySpec aesKeySpec = new SecretKeySpec(HexBinary.hexToBinary(key), ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(HexBinary.hexToBinary(initializationVector));

            // Decrypt cipher
            Cipher cipher = null;
            try {
                cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                e.printStackTrace();
                error.invoke("" + e);
            }
            cipher.init(Cipher.DECRYPT_MODE, aesKeySpec, ivSpec);
            byte[] original = cipher.doFinal(Base64.decode(base64Data, Base64.DEFAULT));

            success.invoke(new String(original, Charset.forName("UTF-8")));
        } catch (InvalidKeyException|
                InvalidAlgorithmParameterException|
                BadPaddingException|
                IllegalBlockSizeException e) {
            e.printStackTrace();
            error.invoke("" + e);
        }
    }

    @Override
    public String getName() {
        return "BitGoAES";
    }
}
