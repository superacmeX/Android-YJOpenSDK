package com.superacm.demo.player;

import android.content.pm.*;
import android.content.res.*;
import android.os.*;
import android.text.method.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.appcompat.app.*;

import com.microbit.rmplayer.*;
import com.microbit.rmplayer.ap.*;
import com.microbit.rmplayer.core.*;

import com.microbit.*;
import com.superacm.demo.player.util.*;

import java.io.*;
import java.text.*;
import java.util.*;

public class RMPApLivePlayerActivity extends AppCompatActivity implements RMPApLinkCallback, RMPlayerListener {
    private final static String TAG = "RMPApLivePlayer";

    private static final int PERMISSION_REQUEST = 1;
    private static final String[] MANDATORY_PERMISSIONS = {
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO", "android.permission.INTERNET"};

    private Toast logToast;
    private RMPVideoView renderView;

    private RMPApLink   link;
    private RMPApPlayerFactory factory;
    private IRMPLivePlayer livePlayer;
    //private EglBase eglBase;

    private TextView    logView;
    private PlayerConfig config;
    private Button      stopBtn;
    private Button      talkBtn;
    private Button      recordBtn;
    private Button      muteBtn;
    private TextView    loadingView;

    private String recordFile;
    private boolean isTalking = false;
    private boolean isRecording = false;
    private boolean isMuted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rmp_ap_live_player);

        String apIp = getIntent().getStringExtra("ap_ip");
        String apPort = getIntent().getStringExtra("ap_port");
        String localIp = getIntent().getStringExtra("local_ip");
        String clientId = getIntent().getStringExtra("client_id");

        config = PlayerConfig.createApLiveConfig(
            this,
            apIp != null ? apIp : "192.168.43.1",
            apPort != null ? apPort : "1664",
            localIp != null ? localIp : "0.0.0.0",
            clientId != null ? clientId : "demo_client"
        );

        setupViews();
        checkPermission();
        initPlayer();

        RMPLog.setLogCallback(new RMPLog.LogCallback() {
            @Override
            public void logMsg(int level, String tag, String msg) {
                if (tag == Post2UserPlayListener.TAG) {
                    printLine(msg);
                }
                Log.println(level, tag, msg) ;
            }
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void initPlayer() {
        RMPEngine engine = RMPEngine.getDefault(getApplicationContext());
        link = RMPApLink.create(this, engine);

        link.init(config.getLocalIp(), config.getClientId());
        link.connect(config.getApIp(), Integer.parseInt(config.getApPort()));

        RMPApConfig apConfig = new RMPApConfig(link);
        factory = RMPApPlayerFactory.create(this, apConfig);
        factory.setDecoderStrategy(config.getVdecodeStrategy());

        renderView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        renderView.setEnableHardwareScaler(false);

        renderView.init();
    }

    private String getSnapshotFile() {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "snapshot-" + System.currentTimeMillis() + ".jpeg");
        return f.getAbsolutePath();
    }

    private String getRecordingFile() {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "recording-" + System.currentTimeMillis() + ".mp4");
        return f.getAbsolutePath();
    }

    private void doCall() {
        if (livePlayer != null) {
            RMPLog.w(TAG, "do Call when livePlayer != null");
            return;
        }

        if (factory == null) {
            RMPLog.w(TAG, "do Call when factory != null");
            return;
        }

        livePlayer = factory.createLivePlayer();
        livePlayer.setVideoSink(new VideoSink() {
            @Override
            public void onFrame(VideoFrame frame) {
                if (renderView != null) {
                    renderView.onFrame(frame);
                }
            }
        });
        livePlayer.setPlayerListener(this);

        livePlayer.setSeiDataCallback((channelId, pts, data) -> {
            String seiHex = formatSeiData(data);
            String logMsg = String.format(Locale.US, "SEI: pts=%d, chl=%d, size=%d, data=%s",
                    pts, channelId, data.length, seiHex);
            RMPLog.i(TAG, logMsg);
            runOnUiThread(() -> printLine(logMsg));
        });

        loadingView.setTextSize(20);
        loadingView.setTextColor(0xff00ff00);

        livePlayer.start();
        printLine("startPlay");
    }

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

    private void setupViews(){
        renderView = findViewById(R.id.video_view);
        logView = findViewById(R.id.log_view);
        logView.setMovementMethod(new ScrollingMovementMethod());

        TextView idView = findViewById(R.id.client_id);
        idView.setText(config.getClientId());

        stopBtn = findViewById(R.id.stop_call);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releasePlayerRes();
                finish();
            }
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

        muteBtn = findViewById(R.id.mute_btn);
        muteBtn.setOnClickListener(v -> {
            isMuted = !isMuted;
            livePlayer.muteRemoteAudio(isMuted);
        });

        recordBtn = findViewById(R.id.record_btn);
        recordBtn.setOnClickListener(v -> {
            isRecording = !isRecording;
            if (isRecording) {
                recordFile = getRecordingFile();
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

        loadingView = findViewById(R.id.loading_cover);

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

        findViewById(R.id.debug_btn).setOnClickListener( v -> {
            link.disconnect();
        });

        findViewById(R.id.dumpfd_btn).setOnClickListener(v -> {
            try {
                String output = ShellUtil.ls_fds();
                printLine("dumpfd result ====================, total lines: %d", output.split("\n").length);
                Log.i(TAG, output);
            } catch (Exception e) {
                printLine("dumpfd exception: " + e);
            }
        });

        updateTalkBtnUi();
        updateRecordBtnUi();
        updateMuteBtnUi();
    }

    private void updateTalkBtnUi() {
        if (isTalking) {
            talkBtn.setText("stopTalk");
        }else {
            talkBtn.setText("startTalk");
        }
    }

    private void updateMuteBtnUi() {
        if (isMuted) {
            muteBtn.setText("unmute");
        }else {
            muteBtn.setText("mute");
        }
    }

    private void updateRecordBtnUi() {
        if (isRecording) {
            recordBtn.setText("stopRec");
        }else {
            recordBtn.setText("startRec");
        }
    }

    private void releasePlayerRes() {
        if (livePlayer != null) {
            livePlayer.release();
            livePlayer = null;
        }

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
        // Check for mandatory permissions.
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

    private void printLine(String line, Object ...args) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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

                if (oldLines.length() > 4096) {
                    oldLines = oldLines.substring(oldLines.length() - 4096);
                }

                logView.setText(oldLines);
                RMPLog.i(TAG, logLine);
            }
        });
    }

    @Override
    public void onConnectedResult(int code) {
        printLine("on link connect result: %d", code);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logView.post(() -> {
            if (code == 0) {
                doCall();
            }
        });
    }

    @Override
    public void onDisconnected() {
        RMPLog.i(TAG, "on link disconnected");
    }

    @Override
    public void OnLinkStatusChanged(int status) {

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

    private static String formatSeiData(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        if (data.length <= 4) {
            StringBuilder sb = new StringBuilder();
            for (byte b : data) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        return String.format("%02x%02x...%02x%02x",
                data[0], data[1],
                data[data.length - 2], data[data.length - 1]);
    }
}