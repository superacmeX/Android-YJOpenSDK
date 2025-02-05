package com.superacm.demo.player.util;

import android.app.*;
import android.content.pm.*;

public class PermissionChecker {
    private final static String TAG = "PermissionChecker";

    private static final String[] MANDATORY_PERMISSIONS = {
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO", "android.permission.INTERNET"};

    private static PermissionChecker instance;

    public static PermissionChecker getInstance() {
        synchronized (PermissionChecker.class) {
            if (instance == null) {
                instance = new PermissionChecker();
            }

            return instance;
        }
    }

    private PermissionChecker(){

    }

    public boolean checkMandatoryPermission(Activity activity) {
        // Check for mandatory permissions.
        for (String permission : MANDATORY_PERMISSIONS) {
            if (activity.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                ToastUtil.logAndToast(activity, TAG, "Permission " + permission + " is not granted");
                activity.setResult(Activity.RESULT_CANCELED);
                activity.finish();
                return false;
            }
        }
        return true;
    }
}
