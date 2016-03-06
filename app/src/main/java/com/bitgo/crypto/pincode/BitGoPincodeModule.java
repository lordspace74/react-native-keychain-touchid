package com.bitgo.crypto.pincode;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.bitgo.crypto.encoding.HexBinary;
import com.facebook.common.util.Hex;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.github.orangegangsters.lollipin.lib.managers.AppLock;
import com.github.orangegangsters.lollipin.lib.managers.LockManager;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class BitGoPincodeModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    private static final int REQUEST_CODE_ENABLE = 11;
    private static final int REQUEST_CODE_UNLOCK = 22;

    private static final String ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    private static final String TOKEN_PREFERENCE_KEY = "BITGO_TOKEN_PREFERENCE_KEY";

    private byte[] mInitializationVector = null;

    private static LockManager mLockManager;
    private static BitGoAppLockImpl mAppLock;

    private Callback mSuccessCallback;
    private Callback mErrorCallback;
    private String mToken;
    private Intent mIntent;

    public BitGoPincodeModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(this);
        mLockManager = LockManager.getInstance();
        mAppLock = BitGoAppLockImpl.getInstance(reactContext, BitGoPinActivity.class);
        mIntent = new Intent(reactContext, BitGoPinActivity.class);
        mLockManager.setAppLock(mAppLock);
        mSuccessCallback = null;
        mErrorCallback = null;
        mToken = null;
        mInitializationVector = null;
    }

    @ReactMethod
    public void hasCredentials(Callback success, Callback error) {
        if (mAppLock.isPasscodeSet()) {
            success.invoke(true);
        } else {
            success.invoke(false);
        }
    }

    @ReactMethod
    public void retrieveCredentials(Callback success, Callback error) {
        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            error.invoke("Activity doesn't exist");
            return;
        }
        if (mAppLock.isPasscodeSet()) {
            mIntent.putExtra(AppLock.EXTRA_TYPE, AppLock.UNLOCK_PIN);

            mSuccessCallback = success;
            mErrorCallback = error;

            currentActivity.startActivityForResult(mIntent, REQUEST_CODE_UNLOCK);
        } else {
            error.invoke("Pin Code is Disabled");
        }
    }

    @ReactMethod
    public void storeCredentials(String token) {
        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            Log.e("BitGoPincodeModule", "Activity doesn't exist");
            return;
        }
        if (mLockManager.isAppLockEnabled()) {
            LockManager.getInstance().getAppLock().disableAndRemoveConfiguration();
        }
        mIntent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);

        mToken = token;

        currentActivity.startActivityForResult(mIntent, REQUEST_CODE_ENABLE);
    }

    @ReactMethod
    public void deleteCredentials() {
        if (mLockManager.isAppLockEnabled()) {
            LockManager.getInstance().getAppLock().disableAndRemoveConfiguration();
        }
    }

    @Override
    public String getName() {
        return "BitGoPincodeModule";
    }

    /*
     * override onActivityResult callback to process pincode results
     */
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (resultCode == BitGoPinActivity.RESULT_FAILURE || intent == null) {
            mErrorCallback.invoke("Pincode Request Failed");
            clearCache();
            return;
        }
        String pin = intent.getStringExtra(BitGoPinActivity.AppLockPinCodeResult);
        String key = getSHA256(pin);
        if (key == null) {
            mErrorCallback.invoke("Unable to generate key hash");
            clearCache();
            return;
        }
        if (requestCode == REQUEST_CODE_ENABLE) {
            mInitializationVector = generateKey(128);
            String encrypted = encrypt(mToken, key);
            if (encrypted != null) {
                mAppLock.setInitializationVector(HexBinary.binaryToHex(mInitializationVector));
                mAppLock.setToken(encrypted);
            }
        } else if (requestCode == REQUEST_CODE_UNLOCK) {
            if (mAppLock == null) {
                mErrorCallback.invoke("App Lock is null");
            } else {
                mInitializationVector = HexBinary.hexToBinary(mAppLock.getInitializationVector());
                String decrypted = decrypt(mAppLock.getToken(), key);
                if (decrypted == null) {
                    mErrorCallback.invoke("Decryption failed");
                } else {
                    mSuccessCallback.invoke(decrypted);
                }
            }
        }
        clearCache();
        return;
    }

    /*
     * generate sha-256 hash from text
     */
    private static String getSHA256(String text) {
        String sha1 = null;
        if (TextUtils.isEmpty(text)) {
            return sha1;
        }
        MessageDigest sha1Digest = null;
        try {
            sha1Digest = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            e.printStackTrace();
            return sha1;
        }
        byte[] textBytes = text.getBytes();
        sha1Digest.update(textBytes, 0, text.length());
        byte[] sha1hash = sha1Digest.digest();
        return HexBinary.binaryToHex(sha1hash);
    }

    /*
     * generate key of specified length
     */
    private static byte[] generateKey(int length) {

        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        keyGen.init(length);
        SecretKey secretKey = keyGen.generateKey();

        byte[] key = secretKey.getEncoded();
        return key;
    }

    /*
     * encrypt data with key
     */
    private String encrypt(String data, String key) {

        try {
            com.bitgo.crypto.encryption.TyrannyOverride.overrideTyranny();

            SecretKeySpec aesKeySpec = new SecretKeySpec(HexBinary.hexToBinary(key), ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(mInitializationVector);

            // Encrypt cipher
            Cipher cipher = null;
            try {
                cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                e.printStackTrace();
                return null;
            }
            cipher.init(Cipher.ENCRYPT_MODE, aesKeySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(data.getBytes("UTF-8"));
            return HexBinary.binaryToHex(encrypted);
        } catch (InvalidKeyException |
                InvalidAlgorithmParameterException |
                BadPaddingException |
                UnsupportedEncodingException |
                IllegalBlockSizeException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * decrypt data with key
     */
    private String decrypt(String data, String key) {

        try {
            com.bitgo.crypto.encryption.TyrannyOverride.overrideTyranny();

            SecretKeySpec aesKeySpec = new SecretKeySpec(HexBinary.hexToBinary(key), ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(mInitializationVector);

            // Decrypt cipher
            Cipher cipher = null;
            try {
                cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                e.printStackTrace();
                return null;
            }
            cipher.init(Cipher.DECRYPT_MODE, aesKeySpec, ivSpec);

            byte[] original = cipher.doFinal(HexBinary.hexToBinary(data));
            return new String(original, "UTF-8");

        } catch (InvalidKeyException|
                InvalidAlgorithmParameterException|
                BadPaddingException|
                UnsupportedEncodingException |
                IllegalBlockSizeException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * clear sensitive information
     */
    private void clearCache() {
        mErrorCallback = null;
        mSuccessCallback = null;
        mToken = null;
    }
}
