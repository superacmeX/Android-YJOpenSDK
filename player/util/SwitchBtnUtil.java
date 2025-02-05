package com.acme.opensdk.demo.ui.player.util;

import android.view.*;
import android.widget.*;

public class SwitchBtnUtil {
    public interface OnStateChangeCallback {
        void onState(boolean state);
    }

    TextView view;
    boolean state;
    String enableText;
    String disableText;
    OnStateChangeCallback callback;

    public SwitchBtnUtil(TextView v, boolean initState, String enable, String disable, OnStateChangeCallback cb) {
        this.state = initState;
        this.view = v;
        this.enableText = enable;
        this.disableText = disable;
        this.callback = cb;

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                state = !state;
                callback.onState(state);
                updateUiText(state);
            }
        });

        updateUiText(state);
    }

    private void updateUiText(boolean enable) {
        if (enable) {
            view.setText(disableText);
        } else {
            view.setText(enableText);
        }
    }
}
