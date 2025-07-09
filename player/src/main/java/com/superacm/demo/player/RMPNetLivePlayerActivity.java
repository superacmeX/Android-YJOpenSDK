package com.superacm.demo.player;

import android.*;
import android.app.*;
import android.content.pm.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;


import androidx.annotation.*;
import androidx.core.content.*;

import com.microbit.*;
import com.microbit.rmplayer.*;
import com.microbit.rmplayer.core.*;
import com.microbit.rmplayer.net.*;
import com.superacm.demo.player.util.*;

import java.io.*;
import java.nio.*;
import java.util.*;

public class RMPNetLivePlayerActivity extends Activity implements RMPlayerListener {
    private final static String TAG = "RMPNetLivePlayerActivity";

    private static final int PERMISSION_REQUEST = 1;

    private static final String[] MANDATORY_PERMISSIONS = {
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };

    private RMPVideoView renderView;
    private IRMPLivePlayer livePlayer;
    private PlayerParam playParam;

    private ViewLog viewLog;
    private Button stopBtn;
    private Button recordBtn;
    private Button talkBtn;
    private TextView loadingView;
    private boolean isTalking = false;
    private boolean isRecording = false;

    private Runnable getDurationTask = new Runnable() {
        @Override
        public void run() {
            if (livePlayer == null) {
                return;
            }

            long duration = livePlayer.getFileRecordingDuration();
            printLine("recording duration: %d", duration);

            if (isRecording) {
                recordBtn.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rmp_net_live_player);
        playParam = getIntent().getParcelableExtra(PlayerParam.PLAY_PARAMS);

        RMPLog.setLogCallback(new RMPLog.LogCallback() {
            @Override
            public void logMsg(int level, String tag, String msg) {
                if (tag == Post2UserPlayListener.TAG ) {
                    printLine(msg);
                }
                Log.println(level, tag, msg) ;
            }
        });

        setupViews();
        requestPermissions();
        initRTC();
    }

    private boolean isMandantoryPermission(String s) {
        boolean need = false;

        for(String p : MANDATORY_PERMISSIONS) {
            if (p.equals(s)) {
                need = true;
                break;
            }
        }

        return need;
    }

    private String[] getMissingPermissions(boolean request_action) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return new String[0];
        }

        PackageInfo info;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Failed to retrieve permissions.");
            return new String[0];
        }

        if (info.requestedPermissions == null) {
            Log.w(TAG, "No requested permissions.");
            return new String[0];
        }

        ArrayList<String> missingPermissions = new ArrayList<>();
        for (int i = 0; i < info.requestedPermissions.length; i++) {

            if ((info.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) == 0) {
                if (request_action || isMandantoryPermission(info.requestedPermissions[i])) {
                    missingPermissions.add(info.requestedPermissions[i]);
                }
            }
        }
        RMPLog.d(TAG, "Missing permissions: " + missingPermissions + ", request_action: " + request_action);

        return missingPermissions.toArray(new String[missingPermissions.size()]);
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Dynamic permissions are not required before Android M.
            return;
        }

        String[] missingPermissions = getMissingPermissions(true);
        if (missingPermissions.length != 0) {
            requestPermissions(missingPermissions, PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int i = 0; i < permissions.length; i++) {
            RMPLog.i(TAG, "onRequestPermissionsResult permissions[%d]: %s, result: %d, check result: %d",
                    i, permissions[i], grantResults[i], ContextCompat.checkSelfPermission(this, permissions[i]));
        }

        if (requestCode == PERMISSION_REQUEST) {
            String[] missingPermissions = getMissingPermissions(false);
            if (missingPermissions.length != 0) {
                // User didn't grant all the permissions. Warn that the application might not work
                // correctly.

                String perms = missingPermissions[0];
                for (int i = 1; i < missingPermissions.length; i++) {
                    perms += ", " + missingPermissions[i];
                }

                RMPLog.e(TAG, "missing permissions: ", perms);
            }
        }
    }

    private void initRTC(){
        renderView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        renderView.setEnableHardwareScaler(false);
        doCall();
    }

    private void doCall() {
        RMPNetPlayerBuilder builder = new RMPNetPlayerBuilder(getApplicationContext(), RMPEngine.getDefault(getApplicationContext()));
        builder.setDeviceInfo(playParam.deviceName, playParam.productKey);

        livePlayer = builder.createLivePlayer();
        livePlayer.setPlayerListener(this);
        startPlayer();
    }

    void updatePlayerStats() {
        if (livePlayer ==null) {
            return;
        }
        RMPlayerStatistics stat = new RMPlayerStatistics();
        boolean ok = livePlayer.getStats(stat);
        TextView view = findViewById(R.id.player_stats);

        String desc;

        desc = "session: " + livePlayer.getPlaySession() + "\n";
        if (ok) {
            desc += String.format("fps: %d, recv bw: %.2f KB/s, send bw: %.2f KB/s",
                    stat.fps,
                    (float)stat.recv_bytes_per_second / 1024,
                    (float)stat.send_bytes_per_second / 1024);
        } else {
            desc += "fps: --, recv bw: -- KB/s, send bw: -- KB/s";
        }

        view.setText(desc);

        view.postDelayed(() -> updatePlayerStats(), 1000);
    }

    private void setupViews(){
        renderView = findViewById(R.id.video_view);

        TextView logView = findViewById(R.id.log_view);
        viewLog = ViewLog.createViewLog(logView, TAG);

        stopBtn = findViewById(R.id.stop_call);
        stopBtn.setOnClickListener(v -> {
            releasePlayerRes();
            finish();
        });

        recordBtn = findViewById(R.id.record_btn);
        recordBtn.setOnClickListener(v -> {
            isRecording = !isRecording;
            if (isRecording) {
                String recordFile = getRecordingFile();
                int ret = livePlayer.startFileRecording(recordFile);
                if (ret == 0) {
                    printLine("start file recording success, file: %s", recordFile);

                    recordBtn.postDelayed(getDurationTask, 1000);
                } else {
                    printLine("start file recording failed, file: %s", recordFile);
                }
            } else {
                livePlayer.stopFileRecording();
            }

            updateRecordBtnUi();
        });

        talkBtn = findViewById(R.id.talk_btn);
        talkBtn.setOnClickListener(v -> {
            isTalking = !isTalking;
            if (isTalking) {
                livePlayer.startTalk();
            }else {
                livePlayer.stopTalk();
            }

            updateTalkBtnUi();
        });

        initLoadingView();
        Button muteBtn = findViewById(R.id.mute_btn);
        new SwitchBtnUtil(muteBtn, false, "mute", "unmute", (state) -> {
            if (livePlayer != null) {
                livePlayer.muteRemoteAudio(state);
            }
        });

        findViewById(R.id.snapshot_btn).setOnClickListener(v -> {
            NonUiThread.post(() -> {
                String file = getSnapshotFile();
                int ret = livePlayer.snapshot(file);
                RMPCore.post2Ui(() -> {
                    if (ret == 0) {
                        printLine("snapshot success to file: %s", file);
                    } else {
                        printLine("snapshot failed to file: %s", file);
                    }
                });
            });
        });

        findViewById(R.id.restart_btn).setOnClickListener(v -> {
            NonUiThread.post(() -> {
                IRMPLivePlayer player = livePlayer;
                if (player == null) {
                    return;
                }

                player.stop();

                player.setVideoView(renderView);
                int ret = player.start();
                printLine("restart player ret: %d", ret);
            });
        });
    }

    private void initLoadingView() {
        loadingView = findViewById(R.id.loading_cover);
        loadingView.setTextSize(20);
        loadingView.setTextColor(0xff00ff00);
    }

    private String getSnapshotFile() {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "snapshot-" + System.currentTimeMillis() + ".jpeg");
        return f.getAbsolutePath();
    }

    private String getRecordingFile() {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "net-rec-live-" + System.currentTimeMillis() + ".mp4");
        return f.getAbsolutePath();
    }

    private void updateRecordBtnUi() {
        if (isRecording) {
            recordBtn.setText("stopRec");
        } else {
            recordBtn.setText("startRec");
        }
    }

    private void updateTalkBtnUi() {
        if (isTalking) {
            talkBtn.setText("stopTalk");
        } else {
            talkBtn.setText("startTalk");
        }
    }

    private void releasePlayerRes() {
        if (livePlayer != null) {
            livePlayer.release();
            livePlayer = null;
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

    private void printLine(String line, Object...args) {
        viewLog.printLine(line, args);
    }

    private void startPlayer() {
        livePlayer.setVideoView(renderView);
        livePlayer.start();

        livePlayer.setSeiDataCallback(new RMPSEIDataCallback() {
            @Override
            public void OnSeiData(int chl, long pts, byte[] sei_data) {
                RMPLog.d(TAG, "recv sei data pts=%d, size=%d", pts, sei_data.length);
            }
        });
        livePlayer.setVideoSink(new VideoSink() {
            @Override
            public void onFrame(VideoFrame frame) {
                //RMPLog.d(TAG, "recv video frame pts=%d", frame.ptsMs());
                VideoFrame.I420Buffer i420Buffer = frame.getBuffer().toI420();
                int height = i420Buffer.getHeight();
                int width = i420Buffer.getWidth();

                ByteBuffer yPlane = i420Buffer.getDataY();
                int iStride = i420Buffer.getStrideY();

                ByteBuffer uPlane = i420Buffer.getDataU();
                ByteBuffer vPlane = i420Buffer.getDataV();

                //remember call release
                i420Buffer.release();
            }
        });

        printLine("startPlay");
        updatePlayerStats();
    }

    @Override
    public void OnError(int type, int code, String desc) {
        RMPLog.e(TAG, "Player error: type=%d, code=%d, desc=%s", type, code, desc);
        printLine("Player error: type=%d, code=%d, desc=%s", type, code, desc);
    }

    @Override
    public void OnPlayerStateChange(int state, int extra) {
        RMPLog.i(TAG, "Player state changed: state=%d, extra=%d", state, extra);
        if (state == RMPlayerState.PLAYER_STARTED) {
            loadingView.post(() -> {
                loadingView.setVisibility(View.GONE);
            });
        }else {
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
    }

    @Override
    public void OnSeekComplete(boolean success) {
        RMPLog.i(TAG, "Seek complete: success=%b", success);
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
        RMPLog.i(TAG, "channel id:%d, Video size changed: %dx%d", width, height);
    }

    @Override
    public void OnSnapshotResult(String file, int result, String desc) {
        RMPLog.i(TAG, "Snapshot result: file=%s, result=%d, desc=%s", file, result, desc);
    }

    @Override
    public void OnFileRecordingStart(String file) {
        RMPLog.i(TAG, "File recording started: %s", file);
        printLine("File recording started: %s", file);
    }

    @Override
    public void OnFileRecordingError(String file, int code, String desc) {
        RMPLog.e(TAG, "File recording error: file=%s, code=%d, desc=%s", file, code, desc);
        printLine("File recording error: file=%s, code=%d, desc=%s", file, code, desc);
    }

    @Override
    public void OnFileRecordingFinish(String file) {
        RMPLog.i(TAG, "File recording finished: %s", file);
        printLine("File recording finished: %s", file);
    }

    @Override
    public void OnVodPlayProgress(long millis) {
        RMPLog.i(TAG, "VOD play progress: %dms", millis);
    }

    @Override
    public void OnVodPlayComplete() {
        RMPLog.i(TAG, "VOD play complete");
    }

}