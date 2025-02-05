package com.superacm.demo.player.util;

import android.os.*;

public class NonUiThread {
    private final static Handler handler;

    static {
        HandlerThread ht = new HandlerThread("NonUiThreadUtil");
        ht.start();
        handler = new Handler(ht.getLooper());
    }

    static public void post(Runnable run) {
        handler.post(run);
    }

    static public void postDelayed(Runnable run, long millis) {
        handler.postDelayed(run, millis);
    }

}
