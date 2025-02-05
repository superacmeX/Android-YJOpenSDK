package com.superacm.demo.player;

import android.app.*;
import android.content.pm.*;
import android.content.res.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.appcompat.app.*;

import com.microbit.*;
import com.microbit.rmplayer.*;
import com.microbit.rmplayer.core.*;
import com.microbit.rmplayer.net.*;
import com.superacm.demo.player.util.*;

import java.io.*;

public class RMPNetVodPlayerActivity extends Activity implements RMPlayerListener {
    private final static String TAG = "RMPNetVodPlayer";

    private static final int PERMISSION_REQUEST = 1;
    private static final String[] MANDATORY_PERMISSIONS = {
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO", "android.permission.INTERNET"};

    private Toast logToast;
    private RMPVideoView renderView;

    private IRMPVodPlayer vodPlayer;
    private PlayerParam playParam;

    private Button recordBtn;
    private Button stopBtn;
    private TextView    loadingView;
    private ViewLog viewLog;
    private TextView logView;
    private int orientation;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rmp_net_vod_player);

        playParam = getIntent().getParcelableExtra(PlayerParam.PLAY_PARAMS);

        RMPLog.setLogCallback(new RMPLog.LogCallback() {
            @Override
            public void logMsg(int level, String tag, String msg) {
                if (tag == Post2UserPlayListener.TAG) {
                    viewLog.printLine(msg);
                }
                Log.println(level, tag, msg) ;
            }
        });

        setupViews();
        initPlayer();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        updateOrientation();
        RMPLog.i(TAG, "onConfigurationChanged orientation: %d", newConfig.orientation);
    }

    private void updateRecordBtnUi() {
        if (isRecording) {
            recordBtn.setText("stopRec");
        }else {
            recordBtn.setText("startRec");
        }
    }

    private void updateOrientation() {
        orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // code for portrait mode
            portraitView();
        } else {
            // code for landscape mode
            landscapeView();
        }
    }

    private void portraitView() {
        findViewById(R.id.ctrl_panel).setVisibility(View.VISIBLE);
        findViewById(R.id.log_view).setVisibility(View.VISIBLE);
    }

    private void landscapeView() {
        findViewById(R.id.ctrl_panel).setVisibility(View.GONE);
        findViewById(R.id.log_view).setVisibility(View.GONE);
    }

    private void initPlayer() {
        renderView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

        RMPNetPlayerBuilder builder = new RMPNetPlayerBuilder(getApplicationContext());
        builder.setDeviceInfo(playParam.deviceName, playParam.productKey);

        vodPlayer = builder.createVodPlayer();
        startPlayer();
    }

    private void setupViews(){
        renderView = findViewById(R.id.video_view);

        logView = findViewById(R.id.log_view);
        viewLog = ViewLog.createViewLog(logView, TAG);

        findViewById(R.id.pause_btn).setOnClickListener(view -> {
            vodPlayer.pause();
        });

        findViewById(R.id.resume_btn).setOnClickListener(view -> {
            vodPlayer.resume();
        });

        findViewById(R.id.speed_btn).setOnClickListener(v -> {
            TextView view = findViewById(R.id.speed_scale);
            int speed = Integer.parseInt(view.getText().toString());
            vodPlayer.setPlaybackSpeed(speed);
            viewLog.printLine("set play speed to: %dx", speed);
        });

        findViewById(R.id.seek_btn).setOnClickListener(v -> {
            TextView view = findViewById(R.id.seek_secs);
            int secs = Integer.parseInt(view.getText().toString());
            vodPlayer.seek(secs);
            viewLog.printLine("seek to: %d", secs);
        });

        stopBtn = findViewById(R.id.stop_call);
        stopBtn.setOnClickListener((View v) -> {
                releasePlayerRes();
                finish();
        });

        loadingView = findViewById(R.id.loading_cover);

        Button muteBtn = findViewById(R.id.mute_btn);
        new SwitchBtnUtil(muteBtn, false, "mute", "unmute", (state) -> {
            if (vodPlayer != null) {
                vodPlayer.muteRemoteAudio(state);
            }
        });

        findViewById(R.id.restart_btn).setOnClickListener(v -> {
            NonUiThread.post(() -> {
                IRMPVodPlayer player = vodPlayer;
                if (player == null) {
                    return;
                }

                player.stop();
                startPlayer();
            });
        });

        recordBtn = findViewById(R.id.record_btn);
        recordBtn.setOnClickListener(v -> {
            isRecording = !isRecording;
            if (isRecording) {
                String recordFile = getRecordingFile();
                int ret = vodPlayer.startFileRecording(recordFile);
                if (ret == 0) {
                    viewLog.printLine("start file recording success, file: %s", recordFile);
                    recordBtn.postDelayed(getDurationTask, 1000);
                } else {
                    viewLog.printLine("start file recording failed, file: %s", recordFile);
                }
            } else {
                vodPlayer.stopFileRecording();
            }

            updateRecordBtnUi();
        });

        updateOrientation();
    }

    private Runnable getDurationTask = new Runnable() {
        @Override
        public void run() {
            if (vodPlayer == null) {
                return;
            }

            long duration = vodPlayer.getFileRecordingDuration();
            viewLog.printLine("recording duration: %d", duration);

            if (isRecording) {
                recordBtn.postDelayed(this, 1000);
            }
        }
    };

    private String getRecordingFile() {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "net-rec-vod-" + System.currentTimeMillis() + ".mp4");
        //if (!f.exists()) {
        //    try {
        //        f.createNewFile();
        //    } catch (IOException e) {
        //        throw new RuntimeException(e);
        //    }
        //}
        return f.getAbsolutePath();
    }

    private void releasePlayerRes() {
        if (vodPlayer != null) {
            vodPlayer.release();
            vodPlayer = null;
        }

        releasePeerConnection();
    }

    private void releasePeerConnection() {
        if (renderView != null) {
            renderView.release();
            renderView = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        releasePlayerRes();
        RMPLog.setLogCallback(null);
    }

    private void startPlayer() {
        if (playParam.startSec != 0) {
            vodPlayer.setDeviceSource(playParam.startSec, playParam.endSec, 0);
        } else {
            long nowSec = System.currentTimeMillis()/1000;
            int rangeMin = 30;
            long startSec = nowSec - 60*rangeMin;
            long seekSec = 0;

            vodPlayer.setDeviceSource(startSec, nowSec, seekSec);
        }

        vodPlayer.setVideoView(renderView);
        vodPlayer.setPlayerListener(this);
        int ret = vodPlayer.start();
        viewLog.printLine("startPlay ret: " + ret);
    }

    @Override
    public void OnError(int type, int code, String desc) {

    }

    @Override
    public void OnPlayerStateChange(int state, int extra) {
        if (state == RMPlayerState.PLAYER_STARTED) {
            loadingView.post(() -> {
                loadingView.setVisibility(View.GONE);
            });
        } else if (state == RMPlayerState.PLAYER_BUFFERING) {
            loadingView.post(() -> {
                loadingView.setVisibility(View.VISIBLE);
            });
        }
    }

    @Override
    public void OnTalkStateChange(int state) {

    }

    @Override
    public void OnPlaybackSpeedUpdate(int speed) {

    }

    @Override
    public void OnSeekComplete(boolean success) {

    }

    @Override
    public void OnBufferStateUpdate(int state, long buffer_duration) {

    }

    @Override
    public void OnFirstFrameRendered(long elapse_ms) {

    }

    @Override
    public void OnVideoSizeChanged(int width, int height) {

    }

    @Override
    public void OnSnapshotResult(String file, int result, String desc) {

    }

    @Override
    public void OnFileRecordingStart(String file) {

    }

    @Override
    public void OnFileRecordingError(String file, int code, String desc) {

    }

    @Override
    public void OnFileRecordingFinish(String file) {

    }

    @Override
    public void OnVodPlayProgress(long millis) {

    }

    @Override
    public void OnVodPlayComplete() {

    }
}