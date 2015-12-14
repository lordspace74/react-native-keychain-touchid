package com.bitgo.crypto.hashing;

import com.bitgo.crypto.encoding.HexBinary;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by arik on 12/14/15.
 */
public class BitGoHash extends ReactContextBaseJavaModule {

    public BitGoHash(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @ReactMethod
    public static String md5(String input) {

        byte[] hash = new byte[0];
        try {
            hash = digest(input, "MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return exportHash(hash);

    }

    @ReactMethod
    public static String sha256(String input) {

        byte[] hash = new byte[0];
        try {
            hash = digest(input, "SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return exportHash(hash);

    }

    @ReactMethod
    public static String sha512(String input) {

        byte[] hash = new byte[0];
        try {
            hash = digest(input, "SHA-512");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return exportHash(hash);

    }

    private static byte[] digest(String input, String digestionName) throws NoSuchAlgorithmException {

        byte[] bytesOfMessage = new byte[0];
        try {
            bytesOfMessage = input.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        MessageDigest digestion = MessageDigest.getInstance(digestionName);
        return digestion.digest(bytesOfMessage);

    }

    private static String exportHash(byte[] binary){

        return HexBinary.binaryToHex(binary).toLowerCase();

    }

    @Override
    public String getName() {
        return "BitGoHash";
    }
}
