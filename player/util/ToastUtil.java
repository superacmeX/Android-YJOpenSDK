package com.acme.opensdk.demo.ui.player.util;

import android.content.*;
import android.util.*;
import android.widget.*;

public class ToastUtil {
    private static Toast logToast;
    public static void logAndToast(Context context, String TAG, String msg) {
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT);
        logToast.show();
    }
}
