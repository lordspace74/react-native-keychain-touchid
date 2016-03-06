package com.bitgo.crypto.pincode;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.widget.Toast;

import com.github.orangegangsters.lollipin.lib.enums.KeyboardButtonEnum;
import com.github.orangegangsters.lollipin.lib.managers.AppLockActivity;
import com.github.orangegangsters.lollipin.lib.managers.LockManager;

/**
 * Created by oliviergoutay on 1/14/15.
 */
public class BitGoPinActivity extends AppLockActivity {

    public final static int RESULT_SUCCESS = 10001;
    public final static int RESULT_FAILURE = 20002;

    public final static String AppLockPinCodeResult = "com.bitgo.applock.pincode";

    public String getForgotText() { return "Forgot Pincode, please log me out"; }

    @Override
    public void showForgotDialog() {
        Log.e("showForgotDialog", "no pincode found");
        if (LockManager.getInstance() != null && LockManager.getInstance().getAppLock() != null) {
            LockManager.getInstance().getAppLock().disableAndRemoveConfiguration();
        }
        finish();
    }

    @Override
    public void onPinFailure(int attempts) {
        if (attempts == 3) {
            if (LockManager.getInstance() != null && LockManager.getInstance().getAppLock() != null) {
                LockManager.getInstance().getAppLock().disableAndRemoveConfiguration();
            }
            finish();
        }
    }

    @Override
    public void onPinSuccess(int attempts) {
        Intent data = new Intent();
        data.putExtra(AppLockPinCodeResult, super.mPinCode);
        setResult(RESULT_SUCCESS, data);
    }

    @Override
    public int getPinLength() {
        return 6;
    }
}