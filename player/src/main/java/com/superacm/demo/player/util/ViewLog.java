package com.superacm.demo.player.util;

import android.os.*;
import android.text.method.*;
import android.widget.*;

import com.microbit.rmplayer.core.*;

import java.text.*;
import java.util.*;

public class ViewLog {
    private TextView logView;
    private String logTag;

    private final static int maxTextSize = 4096;

    static public ViewLog createViewLog(TextView view, String tag) {
        return new ViewLog(view, tag);
    }

    private ViewLog(TextView view, String tag) {
        this.logView = view;
        this.logTag = tag;
        logView.setMovementMethod(new ScrollingMovementMethod());
    }

    public void post(Runnable r) {
        logView.post(r);
    }

    public void printLine(String line, Object ...args) {
        //make sure in ui thread
        if (Looper.myLooper() != Looper.getMainLooper()) {
            RMPCore.post2Ui(() -> {
                printLine(line, args);
            });
            return;
        }

        String fline = line;
        if (args.length > 0) {
            fline = String.format(line, args);
        }

        SimpleDateFormat format = new SimpleDateFormat("MMdd HH:mm:ss"); //"yyyy/MM/dd HH:mm:ss"

        Date date = new Date();
        String dateStr = format.format(date);

        String oldLines = logView.getText().toString();
        String logLine = dateStr + ": " + fline + "\n";
        oldLines =  logLine + oldLines;

        if (oldLines.length() > maxTextSize) {
            oldLines = oldLines.substring(0, maxTextSize);
        }

        logView.setText(oldLines);
        RMPLog.i(logTag, logLine);
    }

    public void printLine(String tag, String line, Object ...args) {
        //make sure in ui thread
        if (Looper.myLooper() != Looper.getMainLooper()) {
            RMPCore.post2Ui(() -> {
                printLine(tag, line, args);
            });
            return;
        }

        String fline = line;
        if (args.length > 0) {
            fline = String.format(line, args);
        }

        SimpleDateFormat format = new SimpleDateFormat("MMdd HH:mm:ss"); //"yyyy/MM/dd HH:mm:ss"

        Date date = new Date();
        String dateStr = format.format(date);

        String oldLines = logView.getText().toString();
        String logLine = dateStr + ": " + fline + "\n";
        oldLines =  logLine + oldLines;

        if (oldLines.length() > maxTextSize) {
            oldLines = oldLines.substring(0, maxTextSize);
        }

        logView.setText(oldLines);
    }
}
