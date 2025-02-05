package com.acme.opensdk.demo.ui.player;

import android.app.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import com.acme.opensdk.demo.ui.player.util.*;
import com.microbit.*;
import com.microbit.rmplayer.*;
import com.microbit.rmplayer.core.*;
import com.microbit.rmplayer.net.*;
import com.acme.opensdk.demo.R;

import java.io.*;
import java.nio.*;

public class RMPNetLivePlayerActivity extends Activity implements RMPlayerListener {
    public static final String PLAY_PARAMS = "play_params";

    private final static String TAG = "RMPNetLivePlayerActivity";

    private RMPVideoView renderView;

    //private RMPNetPlayerFactory factory;
    private IRMPLivePlayer livePlayer;
    //private EglBase eglBase;

    private PlayerParam playParam;

    private ViewLog viewLog;
    private Button stopBtn;
    private Button recordBtn;
    private Button talkBtn;
    private TextView loadingView;
    private boolean isTalking = false;
    private boolean isRecording = false;
    //private String signalServer = "106.15.92.157:9090";

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
        setContentView(com.acme.opensdk.demo.R.layout.activity_rmp_net_live_player);

        playParam = getIntent().getParcelableExtra(PLAY_PARAMS);

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
        if (!PermissionChecker.getInstance().checkMandatoryPermission(this)) {
            return;
        }
        initRTC();
    }

    private void initRTC(){
        renderView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        renderView.setEnableHardwareScaler(false);
        doCall();
    }

    private void doCall() {
        RMPNetPlayerBuilder builder = new RMPNetPlayerBuilder(getApplicationContext());
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
                //player.release();
                //
                //RMPNetPlayerBuilder builder = new RMPNetPlayerBuilder(getApplicationContext());
                //builder.setDeviceInfo(prefMgr.getDeviceID(), prefMgr.getProdKey())
                //        .setAppToken(prefMgr.getAppToken())
                //        .setDecoderStrategy(prefMgr.getVdecodeStrategy());
                //
                //livePlayer = builder.createLivePlayer();
                //player = livePlayer;
                //livePlayer.setPlayerListener(this);
                player.setVideoView(renderView);

                int ret = player.start();
                printLine("restart player ret: %d", ret);
            });
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
    }

    private void initLoadingView() {
        loadingView = findViewById(R.id.loading_cover);
        loadingView.setTextSize(20);
        loadingView.setTextColor(0xff00ff00);
    }

    private String getSnapshotFile() {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "snapshot-" + System.currentTimeMillis() + ".jpeg");
        //if (!f.exists()) {
        //    try {
        //        f.createNewFile();
        //    } catch (IOException e) {
        //        throw new RuntimeException(e);
        //    }
        //}
        return f.getAbsolutePath();
    }

    private String getRecordingFile() {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "net-rec-live-" + System.currentTimeMillis() + ".mp4");
        //if (!f.exists()) {
        //    try {
        //        f.createNewFile();
        //    } catch (IOException e) {
        //        throw new RuntimeException(e);
        //    }
        //}
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

        livePlayer.setSeiDataCallback(new IRMPlayer.SEIDataCallback() {
            @Override
            public void OnSeiData(long pts, byte[] sei_data) {
                //RMPLog.d(TAG, "recv sei data pts=%d, size=%d", pts, sei_data.length);
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

    }

    @Override
    public void OnPlayerStateChange(int state, int extra) {
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