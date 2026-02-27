package com.superacm.demo.player;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.res.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;

import com.microbit.*;
import com.microbit.rmplayer.*;
import com.microbit.rmplayer.core.*;
import com.microbit.rmplayer.net.*;
import com.superacm.demo.player.util.*;

import java.io.*;
import java.util.Objects;

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
                if (Objects.equals(tag, Post2UserPlayListener.TAG)) {
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

    @SuppressLint("SetTextI18n")
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

        RMPNetPlayerBuilder builder = new RMPNetPlayerBuilder(getApplicationContext(), RMPEngine.getDefault(getApplicationContext()));
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
            vodPlayer.setDeviceSource(playParam.startSec, playParam.endSec);
        } else {
            long nowSec = System.currentTimeMillis()/1000;
            int rangeMin = 30;
            long startSec = nowSec - 60*rangeMin;
            vodPlayer.setDeviceSource(startSec, nowSec);
        }

        vodPlayer.setVideoView(renderView);
        vodPlayer.setPlayerListener(this);
        int ret = vodPlayer.start();
        viewLog.printLine("startPlay ret: " + ret);
    }

    @Override
    public void OnError(int type, int code, String desc) {
        RMPLog.e(TAG, "Player error: type=%d, code=%d, desc=%s", type, code, desc);
        viewLog.printLine("Player error: type=%d, code=%d, desc=%s", type, code, desc);
    }

    @Override
    public void OnPlayerStateChange(int state, int extra) {
        RMPLog.i(TAG, "Player state changed: state=%d, extra=%d", state, extra);
        if (state == RMPlayerState.PLAYER_STARTED) {
            loadingView.post(() -> {
                loadingView.setVisibility(View.GONE);
            });
        } else {
            loadingView.post(() -> {
                loadingView.setVisibility(View.VISIBLE);
            });
        }
    }

    @Override
    public void OnTalkStateChange(int state) {
        RMPLog.i(TAG, "Talk state changed: state=%d", state);
    }

    @Override
    public void OnPlaybackSpeedUpdate(int speed) {
        RMPLog.i(TAG, "Playback speed updated: speed=%d", speed);
        viewLog.printLine("Playback speed updated: %dx", speed);
    }

    @Override
    public void OnSeekComplete(boolean success) {
        RMPLog.i(TAG, "Seek complete: success=%b", success);
        viewLog.printLine("Seek complete: success=%b", success);
    }

    @Override
    public void OnBufferStateUpdate(int state, long buffer_duration) {
        RMPLog.i(TAG, "Buffer state update: state=%d, duration=%d", state, buffer_duration);
    }

    @Override
    public void OnFirstFrameRendered(long elapse_ms) {
        RMPLog.i(TAG, "First frame rendered: elapse=%dms", elapse_ms);
    }

    @Override
    public void OnVideoSizeChanged(int channel, int width, int height) {
        RMPLog.i(TAG, "Video size changed: %dx%d", width, height);
    }

    @Override
    public void OnSnapshotResult(String file, int result, String desc) {
        RMPLog.i(TAG, "Snapshot result: file=%s, result=%d, desc=%s", file, result, desc);
    }

    @Override
    public void OnFileRecordingStart(String file) {
        RMPLog.i(TAG, "File recording started: %s", file);
        viewLog.printLine("File recording started: %s", file);
    }

    @Override
    public void OnFileRecordingError(String file, int code, String desc) {
        RMPLog.e(TAG, "File recording error: file=%s, code=%d, desc=%s", file, code, desc);
        viewLog.printLine("File recording error: file=%s, code=%d, desc=%s", file, code, desc);
    }

    @Override
    public void OnFileRecordingFinish(String file) {
        RMPLog.i(TAG, "File recording finished: %s", file);
        viewLog.printLine("File recording finished: %s", file);
    }

    @Override
    public void OnVodPlayProgress(long millis) {
        RMPLog.i(TAG, "VOD play progress: %dms", millis);
        viewLog.printLine("VOD play progress: %dms", millis);
    }

    @Override
    public void OnVodPlayComplete() {
        RMPLog.i(TAG, "VOD play complete");
        viewLog.printLine("VOD play complete");
    }
}