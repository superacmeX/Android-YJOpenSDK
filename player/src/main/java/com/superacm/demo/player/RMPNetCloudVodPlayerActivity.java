package com.superacm.demo.player;

import android.*;
import android.app.*;
import android.content.pm.*;
import android.content.res.*;
import android.os.*;
import android.text.method.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.core.content.ContextCompat;

import com.microbit.*;
import com.microbit.rmplayer.*;
import com.microbit.rmplayer.core.*;
import com.microbit.rmplayer.net.*;
import com.superacm.demo.player.util.*;

import org.json.JSONObject;

import org.json.*;

import java.io.*;
import java.util.*;

public class RMPNetCloudVodPlayerActivity extends Activity implements RMPNetCloudVodDownloadListener, RMPlayerListener {
    private final static String TAG = "RMPNetCloudVodPlayer";

    private static final int PERMISSION_REQUEST = 1;
    private static final String[] MANDATORY_PERMISSIONS = {
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private RMPVideoView renderView;
    private IRMPVodPlayer vodPlayer;
    private RMPNetCloudVodDownloader downloader;
    private PlayerParam playParam;

    private ViewLog viewLog;
    private TextView loadingView;
    private Button stopBtn;
    private SeekBar seekBar;
    private int orientation;
    private boolean isRecording = false;

    // === 测试用静态回调注入 ===
    public static volatile RMPlayerListener testListener = null;
    public static volatile RMPNetCloudVodDownloadListener testDownloadListener = null;

    public static class VodRecord {
        String url;
        String meta;

        VodRecord(String url, String meta) {
            this.url = url;
            this.meta = meta;
        }
    }

    public static class VodParam {
        List<VodRecord> recordList = new ArrayList<>();
        String url = "none";
        String meta = "none";
        String token;

        boolean isPlaylistMode() {
            return recordList.size() > 1;
        }

        boolean parse(String param) {
            String[] parts = param.split("\\n+");
            if (parts.length == 0) {
                return false;
            }

            for(String line : parts) {
                VodRecord record = tryParseRecord(line);
                if (record != null) {
                    recordList.add(record);
                } else {
                    token = line;
                    break;
                }
            }

            if (recordList.size() == 0) {
                return false;
            }

            url = recordList.get(0).url;
            meta = recordList.get(0).meta;
            RMPLog.d(TAG, "vod param parsed, '%s' '%s' '%s', record count: %d", url, meta, token, recordList.size());
            return true;
        }

        VodRecord tryParseRecord(String recLine) {
            String[] parts = recLine.split("\\s+");
            if (parts.length >= 2) {
                return new VodRecord(parts[0], parts[1]);
            }
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rmp_net_cloud_vod_player);
        playParam = getIntent().getParcelableExtra(PlayerParam.PLAY_PARAMS);

        RMPLog.setLogCallback(new RMPLog.LogCallback() {
            @Override
            public void logMsg(int level, String tag, String msg) {
                if (tag == Post2UserPlayListener.TAG) {
                    viewLog.printLine(msg);
                }
                Log.println(level, "_RMP_" + tag, msg);
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
                String perms = missingPermissions[0];
                for (int i = 1; i < missingPermissions.length; i++) {
                    perms += ", " + missingPermissions[i];
                }
                RMPLog.e(TAG, "missing permissions: ", perms);
            }
        }
    }

    private void updateOrientation() {
        orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            portraitView();
        } else {
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

    private void initRTC() {
        renderView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        renderView.setEnableHardwareScaler(false);
        renderView.post(() -> {
            doCall();
        });
    }

    private void doCall() {
        RMPEngine engine = RMPEngine.getDefault(this);

        RMPNetConfig config = new RMPNetConfig(this, engine);
        config.deviceName = playParam.deviceName;
        config.productKey = playParam.productKey;

        VodParam vodParam = new VodParam();
        if (playParam.cvodUrl != null && !playParam.cvodUrl.isEmpty()) {
            // 使用传入的云存储URL和元数据
            vodParam.url = playParam.cvodUrl;
            vodParam.meta = playParam.ext != null ? playParam.ext : "{}";
        } else {
            // 解析播放参数中的云存储信息
            if (!vodParam.parse(playParam.cvodUrl)) {
                printLine("vod param parse failed: '%s'", playParam.cvodUrl);
                finish();
                return;
            }
        }

        if (vodParam.token != null) {
            engine.updateToken(vodParam.token);
        }

        RMPNetPlayerBuilder builder = new RMPNetPlayerBuilder(getApplicationContext(), engine);
        builder.setDeviceInfo(playParam.deviceName, playParam.productKey);

        // 优先使用测试注入的下载监听器
        RMPNetCloudVodDownloadListener downloadListener = testDownloadListener != null ? testDownloadListener : this;
        downloader = RMPNetCloudVodDownloader.create(config, downloadListener);
        downloader.setCloudSource(vodParam.url, vodParam.meta);

        int duration = 0;
        try {
            JSONObject obj = new JSONObject(vodParam.meta);
            duration = obj.getInt("success_duration");
        } catch (Exception e) {
            printLine("parse duration failed: %s", e.getMessage());
        }

        seekBar.setMax(duration * 1000);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private boolean isInTouch = false;
            private int lastUserValue = -1;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (vodPlayer != null && fromUser) {
                    lastUserValue = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isInTouch = true;
                lastUserValue = -1;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isInTouch = false;

                if (lastUserValue > 0) {
                    printLine("user seek to sec: %d", lastUserValue / 1000);
                    vodPlayer.seek(lastUserValue / 1000);
                }
            }
        });

        vodPlayer = builder.createCloudVodPlayer();
        // 优先使用测试注入的回调
        if (testListener != null) {
            vodPlayer.setPlayerListener(testListener);
        } else {
            vodPlayer.setPlayerListener(this);
        }
        vodPlayer.setVideoView(renderView);
        setSourceAndPlay();
    }

    private void setSourceAndPlay() {
        VodParam vodParam = new VodParam();
        if (playParam.cvodUrl != null && !playParam.cvodUrl.isEmpty()) {
            vodParam.url = playParam.cvodUrl;
            vodParam.meta = playParam.ext != null ? playParam.ext : "{}";
        } else {
            if (!vodParam.parse(playParam.cvodUrl)) {
                printLine("vod param parse failed: '%s'", playParam.cvodUrl);
                finish();
                return;
            }
        }

        if (vodParam.isPlaylistMode()) {
            for (VodRecord rec : vodParam.recordList) {
                vodPlayer.appendCouldPlaylist(rec.url, rec.meta);
            }
        } else {
            vodPlayer.setCloudSource(vodParam.url, vodParam.meta, IRMPVodPlayer.VOD_MODE_All);
        }

        RMPLog.i(TAG, "pause first and start");
        vodPlayer.start();

        printLine(TAG + " startPlay");
    }

    private void setupViews() {
        renderView = findViewById(R.id.video_view);

        TextView logView = findViewById(R.id.log_view);
        logView.setMovementMethod(new ScrollingMovementMethod());
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

        Button muteBtn = findViewById(R.id.mute_btn);
        new SwitchBtnUtil(muteBtn, false, "mute", "unmute", (state) -> {
            if (vodPlayer != null) {
                vodPlayer.muteRemoteAudio(state);
            }
        });

        findViewById(R.id.download_btn).setOnClickListener(v -> {
            String file = getRecordMp4File();
            int ret = downloader.startDownload(file);
            printLine("start download ret: %d, file: %s", ret, file);
        });

        findViewById(R.id.restart_btn).setOnClickListener(v -> {
            NonUiThread.post(() -> {
                IRMPVodPlayer player = vodPlayer;
                if (player == null) {
                    return;
                }

                player.stop();
                setSourceAndPlay();
            });
        });

        findViewById(R.id.record_btn).setOnClickListener(v -> {
            isRecording = !isRecording;
            if (isRecording) {
                String recordFile = getRecordingFile();
                int ret = vodPlayer.startFileRecording(recordFile);
                if (ret == 0) {
                    printLine("start file recording success, file: %s", recordFile);
                } else {
                    printLine("start file recording failed, file: %s", recordFile);
                }
            } else {
                vodPlayer.stopFileRecording();
            }

            updateRecordBtnUi();
        });

        seekBar = findViewById(R.id.seek_progress);
        updateOrientation();
        initLoadingView();
    }

    private void initLoadingView() {
        loadingView = findViewById(R.id.loading_cover);
        loadingView.setTextSize(20);
        loadingView.setTextColor(0xff00ff00);
    }

    private void updateRecordBtnUi() {
        Button recordBtn = findViewById(R.id.record_btn);
        if (isRecording) {
            recordBtn.setText("stopRec");
        } else {
            recordBtn.setText("startRec");
        }
    }

    private String getRecordMp4File() {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), 
                "cloud-vod-" + System.currentTimeMillis() + ".mp4");
        return f.getAbsolutePath();
    }

    private String getRecordingFile() {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), 
                "cloud-rec-" + System.currentTimeMillis() + ".mp4");
        return f.getAbsolutePath();
    }

    private void releasePlayerRes() {
        if (vodPlayer != null) {
            vodPlayer.stop();
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

        if (downloader != null) {
            downloader.release();
            downloader = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayerRes();
        RMPLog.setLogCallback(null);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateOrientation();
        RMPLog.i(TAG, "onConfigurationChanged orientation: %d", newConfig.orientation);
    }

    private void printLine(String line, Object... args) {
        viewLog.printLine(line, args);
    }

    // RMPNetCloudVodDownloadListener 实现
    @Override
    public void OnError(int error, String desc) {
        printLine("downloader error: %d, desc: %s", error, desc);
    }

    @Override
    public void OnFinish(boolean has_preroll, String preroll_file, String[] normal_files) {
        printLine("OnFinish: %b, preroll file: %s, normal file: %s",
                has_preroll, preroll_file, String.join(",", normal_files));
    }

    @Override
    public void OnDownloadProgress(int percent) {
        printLine("OnDownloadProgress: %d", percent);
    }

    // RMPlayerListener 实现
    @Override
    public void OnError(int type, int code, String desc) {
        RMPLog.e(TAG, "Player error: type=%d, code=%d, desc=%s", type, code, desc);
        printLine("Player error: type=%d, code=%d, desc=%s", type, code, desc);
    }

    @Override
    public void OnPlayerStateChange(int state, int extra) {
        RMPLog.i(TAG, "Player state changed: state=%d, extra=%d", state, extra);
        if (state == RMPlayerState.PLAYER_STARTED) {
            viewLog.printLine("total duration=" + vodPlayer.getTotalDuration());
        }

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
    public void OnVideoSizeChanged(int chl, int width, int height) {
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
        if (seekBar != null) {
            seekBar.post(() -> {
                seekBar.setProgress((int) millis);
            });
        }
    }

    @Override
    public void OnVodPlayComplete() {
        RMPLog.i(TAG, "VOD play complete");
    }
} 