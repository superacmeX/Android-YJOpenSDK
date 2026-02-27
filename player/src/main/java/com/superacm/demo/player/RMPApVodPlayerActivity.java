package com.superacm.demo.player;

import android.content.pm.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import androidx.appcompat.app.*;

import com.microbit.rmplayer.*;
import com.microbit.rmplayer.ap.*;
import com.microbit.rmplayer.core.*;
import com.superacm.demo.player.util.*;
import com.superacm.demo.player.PlayerConfig;

import com.microbit.*;

import java.io.File;

public class RMPApVodPlayerActivity extends AppCompatActivity implements RMPApLinkCallback, RMPlayerListener, RMPMonoDownloadListener {
    private final static String TAG = "RMPApVodPlayer";

    private static final String[] MANDATORY_PERMISSIONS = {
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO", "android.permission.INTERNET"};

    private Toast logToast;
    private RMPVideoView renderView;

    private RMPApLink   link;
    private RMPApPlayerFactory factory;
    private IRMPVodPlayer vodPlayer;

    private Button  stopBtn;
    private Button  playBtn;
    private Button  downloadBtn;
    private Button  recordBtn;
    private TextView    loadingView;
    private PlayerConfig config;
    private ViewLog viewLog;

    private ProgressBar progressBar;
    private TextView progressText;
    private long currentProgress = 0;
    private long maxProgress = 0;
    private long totalDuration = 0;

    private EditText fileNameInput;
    private EditText fileSeekInput;
    private CheckBox useFileModeCheckbox;

    // Download related
    private RMPMonoDownloader downloader;
    private boolean isDownloading = false;
    private boolean isPlaying = false;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rmp_ap_vod_player);

        // Create default config, can be customized via intent extras
        String apIp = getIntent().getStringExtra("ap_ip");
        String apPort = getIntent().getStringExtra("ap_port");
        String localIp = getIntent().getStringExtra("local_ip");
        String clientId = getIntent().getStringExtra("client_id");

        config = PlayerConfig.createApVodConfig(
            this,
            apIp != null ? apIp : "192.168.43.1",
            apPort != null ? apPort : "6684",
            localIp != null ? localIp : "",
            clientId != null ? clientId : "demo_client"
        );

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
        checkPermission();
        initPlayer();
    }

    private void initPlayer() {
        RMPEngine engine = RMPEngine.getDefault(getApplicationContext());
        link = RMPApLink.create(this, engine);

        String localIp = config.getLocalIp() != null ? config.getLocalIp() : "";
        String clientId = config.getClientId() != null ? config.getClientId() : "demo_client";
        String apIp = config.getApIp() != null ? config.getApIp() : "192.168.43.1";
        String apPort = config.getApPort() != null ? config.getApPort() : "6684";

        link.init(localIp, clientId);
        link.connect(apIp, Integer.parseInt(apPort));

        RMPApConfig apConfig = new RMPApConfig(link);
        factory = RMPApPlayerFactory.create(this, apConfig);
        factory.setDecoderStrategy(config.getVdecodeStrategy());

        renderView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        renderView.init();
    }

    private void doCall() {
        if (vodPlayer != null) {
            RMPLog.w(TAG, "doCall when vodPlayer != null, restarting playback");
            viewLog.printLine("Player already exists, restarting playback...");
            startPlayback();
            return;
        }

        if (factory == null) {
            return;
        }

        if (isDownloading) {
            viewLog.printLine("Cannot play while downloading. Stop download first.");
            return;
        }

        vodPlayer = factory.createVodPlayer();

        if (useFileModeCheckbox.isChecked()) {
            String fileName = fileNameInput.getText().toString().trim();
            int seekSec = 0;
            try {
                seekSec = Integer.parseInt(fileSeekInput.getText().toString());
            } catch (NumberFormatException e) {
                seekSec = 0;
            }
            
            if (fileName.isEmpty()) {
                viewLog.printLine("File name is empty!");
                vodPlayer = null;
                return;
            }
            
            vodPlayer.setRecordFile(fileName, seekSec);
            viewLog.printLine("Using file mode: " + fileName + ", seek: " + seekSec);
        } else {
            long nowSec = System.currentTimeMillis()/1000;
                long startSec = nowSec - 3600*24 * 4;
            vodPlayer.setDeviceSource(startSec, nowSec);
            viewLog.printLine("Using time range mode");
        }
        vodPlayer.setVideoSink(new VideoSink() {
            @Override
            public void onFrame(VideoFrame frame) {
                if (renderView != null) {
                    renderView.onFrame(frame);
                }
            }
        });
        vodPlayer.setPlayerListener(this);


        loadingView.setTextSize(20);
        loadingView.setTextColor(0xff00ff00);
        loadingView.setText("Starting playback...");
        vodPlayer.start();
        isPlaying = true;
        playBtn.setText("Stop Play");
        viewLog.printLine("startPlay");
    }

    private void setupViews(){
        renderView = findViewById(R.id.video_view);

        TextView logView = findViewById(R.id.log_view);
        viewLog = ViewLog.createViewLog(logView, TAG);

        TextView idView = findViewById(R.id.client_id);
        String displayClientId = config.getClientId() != null ? config.getClientId() : "demo_client";
        idView.setText(displayClientId);
        
        playBtn = findViewById(R.id.play_btn);
        playBtn.setOnClickListener(view -> {
            if (!isPlaying) {
                doCall();
            } else {
                stopPlayback();
            }
        });

        findViewById(R.id.pause_btn).setOnClickListener(view -> {
            if (vodPlayer != null) {
                vodPlayer.pause();
            }
        });

        findViewById(R.id.resume_btn).setOnClickListener(view -> {
            if (vodPlayer != null) {
                vodPlayer.resume();
            }
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
            viewLog.printLine("seek to: %dx", secs);
        });

        stopBtn = findViewById(R.id.stop_call);
        stopBtn.setOnClickListener((View v) -> {
                stopPlayback();
        });

        findViewById(R.id.restart_btn).setOnClickListener((View v) -> {
            restartPlayback();
        });

        fileNameInput = findViewById(R.id.file_name_input);
        fileSeekInput = findViewById(R.id.file_seek_input);
        useFileModeCheckbox = findViewById(R.id.use_file_mode);
        
        downloadBtn = findViewById(R.id.download_btn);
        downloadBtn.setOnClickListener((View v) -> {
            if (!isDownloading) {
                startDownload();
            } else {
                stopDownload();
            }
        });

        loadingView = findViewById(R.id.loading_cover);

        recordBtn = findViewById(R.id.record_btn);
        recordBtn.setOnClickListener(v -> {
            if (!isRecording) {
                startRecording();
            } else {
                stopRecording();
            }
        });

        Button muteBtn = findViewById(R.id.mute_btn);
        new SwitchBtnUtil(muteBtn, false, "mute", "unmute", (state) -> {
            if (vodPlayer != null) {
                vodPlayer.muteRemoteAudio(state);
            }
        });

        Button snapshotBtn = findViewById(R.id.snapshot_btn);
        snapshotBtn.setOnClickListener(v -> {
            takeSnapshot();
        });

        progressBar = findViewById(R.id.progress_bar);
        progressText = findViewById(R.id.progress_text);
    }

    private String getRecordFile() {
        File dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File recordFile = new File(dcimDir, "ap-vod-record-" + System.currentTimeMillis() + ".mp4");
        return recordFile.getAbsolutePath();
    }

    private String getSnapshotFile() {
        File dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File snapshotFile = new File(dcimDir, "ap-vod-snapshot-" + System.currentTimeMillis() + ".jpg");
        return snapshotFile.getAbsolutePath();
    }

    private void takeSnapshot() {
        if (vodPlayer == null) {
            viewLog.printLine("ERROR: Player not initialized");
            return;
        }

        String snapshotPath = getSnapshotFile();
        int ret = vodPlayer.snapshot(snapshotPath);
        if (ret == 0) {
            viewLog.printLine("Snapshot taken: " + snapshotPath);
        } else {
            viewLog.printLine("ERROR: Failed to take snapshot, ret=" + ret);
        }
    }

    private void startRecording() {
        if (vodPlayer == null) {
            viewLog.printLine("ERROR: Player not initialized");
            return;
        }

        String recordPath = getRecordFile();
        int ret = vodPlayer.startFileRecording(recordPath);
        if (ret == 0) {
            isRecording = true;
            recordBtn.setText("Stop Rec");
            viewLog.printLine("Recording started: " + recordPath);
        } else {
            viewLog.printLine("ERROR: Failed to start recording, ret=" + ret);
        }
    }

    private void stopRecording() {
        if (vodPlayer == null) {
            return;
        }

        int ret = vodPlayer.stopFileRecording();
        isRecording = false;
        recordBtn.setText("record");
        if (ret == 0) {
            viewLog.printLine("Recording stopped");
        } else {
            viewLog.printLine("ERROR: Failed to stop recording, ret=" + ret);
        }
    }

    private void stopPlayback() {
        if (vodPlayer != null) {
            if (isRecording) {
                stopRecording();
            }
            vodPlayer.stop();
        }
        isPlaying = false;
        totalDuration = 0;

        playBtn.setText("Play");
        loadingView.setVisibility(View.VISIBLE);
        loadingView.setText("Stopped. Press Play to resume.");
        progressBar.setProgress(0);
        progressText.setText("00:00 / 00:00");
        viewLog.printLine("Playback stopped (player not released)");
    }

    private void startPlayback() {
        if (vodPlayer == null) {
            viewLog.printLine("ERROR: vodPlayer is null, cannot start playback");
            return;
        }

        // 重新设置数据源
        if (useFileModeCheckbox.isChecked()) {
            String fileName = fileNameInput.getText().toString().trim();
            int seekSec = 0;
            try {
                seekSec = Integer.parseInt(fileSeekInput.getText().toString());
            } catch (NumberFormatException e) {
                seekSec = 0;
            }

            if (fileName.isEmpty()) {
                viewLog.printLine("File name is empty!");
                return;
            }

            vodPlayer.setRecordFile(fileName, seekSec);
            viewLog.printLine("Using file mode: " + fileName + ", seek: " + seekSec);
        } else {
            long nowSec = System.currentTimeMillis()/1000;
            long startSec = nowSec - 3600*24 * 4;
            vodPlayer.setDeviceSource(startSec, nowSec);
            viewLog.printLine("Using time range mode");
        }

        loadingView.setTextSize(20);
        loadingView.setTextColor(0xff00ff00);
        loadingView.setText("Starting playback...");
        vodPlayer.start();
        isPlaying = true;
        playBtn.setText("Stop Play");
        viewLog.printLine("Playback started");
    }

    private void restartPlayback() {
        viewLog.printLine("Restarting playback...");

        if (vodPlayer != null) {
            if (isRecording) {
                stopRecording();
            }
            vodPlayer.stop();
            vodPlayer.release();
            vodPlayer = null;
        }
        isPlaying = false;

        new android.os.Handler().postDelayed(() -> {
            viewLog.printLine("Starting new playback session...");
            doCall();
        }, 500);
    }
    private String getDownloadFile() {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "ap-vod-" + System.currentTimeMillis() + ".mp4");
        return f.getAbsolutePath();
    }
    private void startDownload() {
        try {
            if (downloader != null) {
                // Already downloading, this click should stop it
                stopDownload();
                return;
            }
            
            // Check if playing
            if (isPlaying) {
                viewLog.printLine("Cannot download while playing. Stop playback first.");
                return;
            }
            
            viewLog.printLine("Starting AP card video download...");
            
            // Download last 1 hour of recording
            long nowSec = System.currentTimeMillis() / 1000;
            long startTimestamp = nowSec - 3600; // 1 hour ago
            long endTimestamp = nowSec;
            
            viewLog.printLine("Creating AP VOD source...");
            viewLog.printLine("- Time range: " + startTimestamp + " - " + endTimestamp);

            RMPApConfig config = new RMPApConfig(link);

            RMPApVodSource source = RMPMediaSource.createApVodSource(config);
            source.setRangeSec(startTimestamp, endTimestamp);
            
            // Create MonoDownloader with AP source
            downloader = RMPMonoDownloader.create(source);
            if (downloader == null) {
                viewLog.printLine("Failed to create MonoDownloader");
                source.release();
                return;
            }

            downloader.setOutputFile(getDownloadFile());
            downloader.setLisener(this);
            
            viewLog.printLine("Output file: " + getDownloadFile());
            viewLog.printLine("Starting download...");
            
            // Start download
            int ret = downloader.start();
            if (ret == 0) {
                isDownloading = true;
                downloadBtn.setText("Stop Download");
                viewLog.printLine("Download started successfully");
            } else {
                viewLog.printLine("Failed to start download, error: " + ret);
                downloader.release();
                downloader = null;
            }
            
        } catch (Exception e) {
            viewLog.printLine("Download start error: " + e.getMessage());
            if (downloader != null) {
                downloader.release();
                downloader = null;
            }
        }
    }
    
    private void stopDownload() {
        if (downloader != null) {
            viewLog.printLine("Stopping download...");
            downloader.stop();
            downloader.release();
            downloader = null;
        }
        isDownloading = false;
        downloadBtn.setText("Download");
        viewLog.printLine("Download stopped");
    }
    
    private String getDownloadFilePath() {
        File downloadDir = new File(getExternalFilesDir(null), "downloads");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        return new File(downloadDir, "ap_recording_" + timestamp + ".mp4").getAbsolutePath();
    }

    private void releasePlayerRes() {
        // Release downloader
        if (downloader != null) {
            downloader.release();
            downloader = null;
        }
        isDownloading = false;
        
        if (vodPlayer != null) {
            vodPlayer.release();
            vodPlayer = null;
        }
        isPlaying = false;

        if (factory != null) {
            factory.destroy();
            factory = null;
        }

        if (link != null) {
            link.disconnect();
            link.deinit();
            link.release();
            link = null;
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

    private void checkPermission() {
        for (String permission : MANDATORY_PERMISSIONS) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                logAndToast("Permission " + permission + " is not granted");
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
        }
    }

    private void logAndToast(String msg) {
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        logToast.show();
    }

    int count = 0;
    @Override
    public void onConnectedResult(int code) {
        viewLog.printLine("on link connect result: %d", code);

        RMPCore.post2Ui(() -> {
            if (count < 1) {
                count++;
                if (link != null) {
                    link.disconnect();
                    String reconnectApIp = config.getApIp() != null ? config.getApIp() : "192.168.43.1";
                    String reconnectApPort = config.getApPort() != null ? config.getApPort() : "6684";
                    link.connect(reconnectApIp, Integer.parseInt(reconnectApPort));
                }
                return;
            }

            if (code == 0) {
                viewLog.printLine("Connected successfully. Choose either Play or Download.");
                viewLog.printLine("Note: Simultaneous play and download is not supported.");
                link.queryMediaRecord(System.currentTimeMillis() / 1000 - 3600*10, System.currentTimeMillis() / 1000, 2, 1, 10);
                loadingView.post(() -> {
                    loadingView.setText("Connected. Press Play or Download.");
                    loadingView.setTextColor(0xff00ff00);
                });
            }
        });
    }

    @Override
    public void onDisconnected() {
        RMPLog.i(TAG, "on link disconnected");
    }

    @Override
    public void OnLinkStatusChanged(int status) {
        viewLog.printLine("on link status changed: %d", status);
    }

    @Override
    public void OnLinkEventPost(byte[] event, byte[] payload) {

    }

    @Override
    public void OnLinkPropertyPost(byte[] payload) {

    }

    @Override
    public void OnAlarmEvent(int type, int format, byte[] media, byte[] addition_string) {

    }

    @Override
    public void OnMediaRecordResult(int query_id, int status, String result) {
        viewLog.printLine("OnMediaRecordResult, query id: %d, status: %d, result: %s", query_id, status, result);
    }

    @Override
    public void OnLinkLogFile(int seq, int total_seq, byte[] payload) {

    }

    @Override
    public int OnLinkBindInterface(int sockFd) {
        RMPLog.i(TAG, "OnLinkBindInterface called, socket fd: " + sockFd);
        return 0;
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
            viewLog.printLine("Player started successfully");
        } else if (state == RMPlayerState.PLAYER_STOPED) {
            isPlaying = false;
            runOnUiThread(() -> {
                playBtn.setText("Play");
                loadingView.setVisibility(View.VISIBLE);
                loadingView.setText("Connected. Press Play or Download.");
            });
            viewLog.printLine("Player stopped");
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
    public void OnVideoSizeChanged(int chl, int width, int height) {

    }

    @Override
    public void OnSnapshotResult(String file, int result, String desc) {

        String resultStr = result == 1 ? "SUCCESS" : "FAILED";
        viewLog.printLine("Snapshot result: " + resultStr + ", file: " + file + ", desc: " + desc);
        RMPLog.i(TAG, "OnSnapshotResult: result=" + result + ", file=" + file + ", desc=" + desc);
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
        if (totalDuration == 0) {
            totalDuration = millis + 1000; // 估算总时长
        }
        runOnUiThread(() -> {
            updateProgress(millis, totalDuration);
        });
    }

    @Override
    public void OnVodPlayComplete() {
        viewLog.printLine("VOD playback completed!");
        runOnUiThread(() -> {
            if (totalDuration > 0) {
                updateProgress(totalDuration, totalDuration);
            }
            progressText.setText("Playback Complete");
        });
    }

    @Override
    public void OnError(int error, String desc) {
        viewLog.printLine("Download error: " + error + ", " + desc);
        isDownloading = false;
        if (downloader != null) {
            downloader.release();
            downloader = null;
        }
        runOnUiThread(() -> downloadBtn.setText("Download"));
    }
    
    @Override
    public void OnProgress(RMPMonoDownloadProgress progress) {
        viewLog.printLine("Download progress: " + progress.toString());
        runOnUiThread(() -> {
            if (progress.progress >= 0) {
                downloadBtn.setText("Downloading " + progress.progress + "%");
            }
        });
    }
    
    @Override
    public void OnFinish(String[] files) {
        StringBuilder filesStr = new StringBuilder();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (i > 0) filesStr.append(", ");
                filesStr.append(files[i]);
            }
        }
        viewLog.printLine("Download finished successfully!");
        viewLog.printLine("Output files: " + filesStr.toString());
        isDownloading = false;
        if (downloader != null) {
            downloader.release();
            downloader = null;
        }
        runOnUiThread(() -> downloadBtn.setText("Download"));
    }

    private void updateProgress(long currentMillis, long totalMillis) {
        String currentTime = formatTime(currentMillis);
        String totalTime = formatTime(totalMillis);
        progressText.setText(currentTime + " / " + totalTime);

        if (totalMillis > 0) {
            int progress = (int) ((currentMillis * 100) / totalMillis);
            progressBar.setProgress(Math.min(progress, 100));
        }
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds = seconds % 60;
        minutes = minutes % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}
