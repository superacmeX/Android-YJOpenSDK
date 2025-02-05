package com.acme.login.view;

import android.annotation.SuppressLint;
import android.util.Log;

import java.util.Calendar;
import java.util.Map;
import java.util.Random;

public class InnerLoginLogger {
    private final static String TAG = "Logger";
    //日志前缀  方便排查问题
    public final static String LOG_Prefix = "Acme_";

    public final static String tagDelimiter = "->";

    private final static int logMaxLength = 4 * 1024;

    public static boolean isOpenLog = false;
    public static String version;
    public static final String session = String.valueOf(Math.abs(new Random().nextInt()));

    public static void setIsOpenLog(boolean open) {
        Log.d(TAG, "open debug: " + open);
        isOpenLog = open;
    }

    public static void info(String info) {
        info(LOG_Prefix + TAG, info);
    }

    public static void info(String tag, String msg) {
        if (isOpenLog) {
            Log.i(LOG_Prefix + tag, msg);
        }
        addLog(tag, msg);

    }

    public static void log(int level, String tag, String msg) {

        if (isOpenLog) {
            Log.println(level, tag, msg);
        }
        addLog(tag, msg);
    }

    //TODO test longInfo Method
    public static void longInfo(String tag, String info) {
        int strLen = info.length();
        int start = 0;

        int unitMaxLength = logMaxLength - tag.length() - tagDelimiter.length();
        if (strLen > logMaxLength) {
            do {
                int tmpEnd = start + unitMaxLength;
                if (tmpEnd >= strLen) {
                    tmpEnd = strLen;
                }
                info(tag + tagDelimiter + info.substring(start, tmpEnd));
                start = tmpEnd;
            } while (start < strLen);
        } else {
            info(tag + tagDelimiter + info);
        }
    }


    public static void error(String info) {
        error(LOG_Prefix + TAG, info);
    }

    public static void warn(String info) {
        warn(LOG_Prefix + TAG, info);
    }

    public static void warn(String tag, String info) {
        if (isOpenLog) {
            Log.w(LOG_Prefix + tag, info);
        }
        addLog(LOG_Prefix + tag, info);
    }

    public static void error(String tag, String msg) {
        if (isOpenLog) {
            Log.e(TAG + tag, msg);
        }
        addLog(tag, msg);

    }

    public static void addLog(String tag, String content) {
        try {

        } catch (Exception exception) { //隐私权限未同意前，调用getInstance cause exception

        }
    }

    public static void addLog(Map<String, String> logs) {

    }

    public static void d(String tag, String msg) {
        d(tag, msg, true);
    }

    public static void d(String tag, String msg, boolean ssl) {
        if (isOpenLog) {
            Log.d(LOG_Prefix + tag, msg);
        }
        if (ssl) {
            addLog(tag, msg);
        }
    }


    @SuppressLint("DefaultLocale")
    private static String formatTimestamp(Calendar c) {
        long month = c.get(Calendar.MONTH) + 1;
        long day = c.get(Calendar.DAY_OF_MONTH);
        long hours = c.get(Calendar.HOUR_OF_DAY);
        long minutes = c.get(Calendar.MINUTE);
        long seconds = c.get(Calendar.SECOND);
        long millisec = c.get(Calendar.MILLISECOND);
        return String.format("%02d-%02d %02d:%02d:%02d %03d", month, day, hours, minutes, seconds, millisec);
    }
}
