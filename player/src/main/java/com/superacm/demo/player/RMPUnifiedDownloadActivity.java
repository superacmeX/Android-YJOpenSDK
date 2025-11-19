package com.superacm.demo.player;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.microbit.rmplayer.RMPDownloadConfig;
import com.microbit.rmplayer.RMPDownloadListener;
import com.microbit.rmplayer.RMPDownloadManager;
import com.microbit.rmplayer.RMPDownloadManager.RMPDownloadTaskInfo;
import com.microbit.rmplayer.RMPEngine;
import com.microbit.rmplayer.ap.RMPApLink;
import com.microbit.rmplayer.ap.RMPApLinkCallback;
import com.microbit.rmplayer.core.RMPLog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Unified download activity supporting multi-task downloads in AP mode
 */
public class RMPUnifiedDownloadActivity extends Activity implements RMPApLinkCallback {
    private static final String TAG = "RMPUnifiedDownload";

    // UI elements
    private Button addTaskBtn;
    private Button addBatchBtn;
    private Button startAllBtn;
    private Button pauseAllBtn;
    private Button cancelAllBtn;
    private TextView managerStatusText;
    private ListView taskListView;
    private TaskListAdapter taskAdapter;
    private EditText apRemoteFileEdit;

    // Core components
    private RMPEngine engine;
    private RMPApLink apLink;
    private RMPDownloadManager downloadManager;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private PlayerConfig config;

    // Download state
    private List<TaskInfo> taskList = new ArrayList<>();
    private Map<String, TaskInfo> taskMap = new HashMap<>();

    // Task tracking
    private static class TaskInfo {
        String taskId;
        String fileName;
        String filePath;
        @com.microbit.RMPDownloadState int state;
        int progress;
        long downloadedBytes;
        long totalBytes;
        double speed;
        String error;
        long lastUpdateTime;
        long lastDownloadedSize;

        TaskInfo(String taskId, String fileName, String filePath) {
            this.taskId = taskId;
            this.fileName = fileName;
            this.filePath = filePath;
            this.state = com.microbit.RMPDownloadState.PENDING;
            this.progress = 0;
            this.downloadedBytes = 0;
            this.totalBytes = 0;
            this.speed = 0;
            this.error = "";
            this.lastUpdateTime = System.currentTimeMillis();
            this.lastDownloadedSize = 0;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unified_download);

        // Get config from intent or use defaults
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

        engine = RMPEngine.getDefault(getApplicationContext());

        setupViews();
        initializeComponents();
    }

    private void setupViews() {
        addTaskBtn = findViewById(R.id.btn_add_task);
        addBatchBtn = findViewById(R.id.btn_add_batch);
        startAllBtn = findViewById(R.id.btn_start_all);
        pauseAllBtn = findViewById(R.id.btn_pause_all);
        cancelAllBtn = findViewById(R.id.btn_cancel_all);
        managerStatusText = findViewById(R.id.tv_manager_status);
        taskListView = findViewById(R.id.list_tasks);
        apRemoteFileEdit = findViewById(R.id.edit_ap_remote_file);

        // Setup adapter
        taskAdapter = new TaskListAdapter();
        taskListView.setAdapter(taskAdapter);

        // Setup button listeners
        addTaskBtn.setOnClickListener(v -> addNewTask());
        addBatchBtn.setOnClickListener(v -> addMultipleTasks(5));
        startAllBtn.setOnClickListener(v -> startAllTasks());
        pauseAllBtn.setOnClickListener(v -> pauseAllTasks());
        cancelAllBtn.setOnClickListener(v -> cancelAllTasks());

        // Set default file path
        apRemoteFileEdit.setText("100MEDIA/video.mp4");
    }

    private void initializeComponents() {
        initApLink();
        initDownloadManager();
    }

    private void initApLink() {
        apLink = RMPApLink.create(this, engine);
        if (apLink != null) {
            apLink.init(config.getLocalIp(), config.getClientId());
            apLink.connect(config.getApIp(), Integer.parseInt(config.getApPort()));
            RMPLog.i(TAG, "AP Link created and connecting to " + config.getApIp() + ":" + config.getApPort());
        } else {
            RMPLog.e(TAG, "Failed to create AP Link");
            showToast("Failed to initialize AP Link");
        }
    }

    private void initDownloadManager() {
        if (downloadManager != null) {
            downloadManager.release();
            downloadManager = null;
        }

        taskList.clear();
        taskMap.clear();
        if (taskAdapter != null) {
            taskAdapter.notifyDataSetChanged();
        }

        RMPDownloadConfig downloadConfig = new RMPDownloadConfig();
        downloadConfig.cacheFilePath = getCacheDir().getAbsolutePath() + "/unified_download_tasks.json";
        downloadConfig.persistTasks = true;
        downloadConfig.autoResumeOnStart = false;
        downloadConfig.maxConcurrentDownloads = 1;  // AP mode: sequential downloads
        downloadConfig.enableCrcValidation = false;

        if (apLink == null) {
            RMPLog.e(TAG, "Failed to create download manager: AP Link is null");
            showToast("Waiting for AP Link connection...");
            return;
        }

        downloadManager = RMPDownloadManager.create(apLink, downloadConfig);
        RMPLog.i(TAG, "Download manager initialized with AP Link");

        if (downloadManager != null) {
            downloadManager.initialize();

            // Load persisted tasks from cache
            loadPersistedTasks();

            downloadManager.setListener(new RMPDownloadListener() {
                @Override
                public void onTaskStateChanged(String taskId, @com.microbit.RMPDownloadState int state) {
                    mainHandler.post(() -> updateTaskState(taskId, state));
                }

                @Override
                public void onTaskProgress(String taskId, long downloadedBytes, long totalBytes, int progress) {
                    mainHandler.post(() -> updateTaskProgress(taskId, downloadedBytes, totalBytes, progress));
                }

                @Override
                public void onTaskCompleted(String taskId, String filePath) {
                    RMPLog.i(TAG, "Task completed: taskId=%s, filePath=%s", taskId, filePath);
                    mainHandler.post(() -> {
                        updateManagerStatus();
                        showToast("Download completed: " + new File(filePath).getName());
                    });
                }

                @Override
                public void onTaskFailed(String taskId, int errorCode, String error) {
                    RMPLog.e(TAG, "Task failed: taskId=%s, error=%s", taskId, error);
                    mainHandler.post(() -> {
                        updateTaskError(taskId, error);
                        updateManagerStatus();
                    });
                }
            });
        }

        updateManagerStatus();
    }

    private void loadPersistedTasks() {
        if (downloadManager == null) return;

        List<RMPDownloadTaskInfo> persistedTasks = downloadManager.getAllTasks();
        RMPLog.i(TAG, "Loading persisted tasks: %d tasks found", persistedTasks.size());

        for (RMPDownloadTaskInfo taskInfo : persistedTasks) {
            String fileName = taskInfo.remoteFile;
            if (fileName.contains("/")) {
                fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
            }

            TaskInfo task = new TaskInfo(taskInfo.taskId, fileName, taskInfo.localFile);
            task.state = taskInfo.state;
            task.downloadedBytes = taskInfo.downloadedBytes;
            task.totalBytes = taskInfo.totalBytes;
            task.progress = (task.totalBytes > 0) ? (int) (task.downloadedBytes * 100 / task.totalBytes) : 0;

            taskList.add(task);
            taskMap.put(task.taskId, task);

            RMPLog.i(TAG, "Loaded task: taskId=%s, fileName=%s, state=%d, progress=%d%%",
                    task.taskId, fileName, task.state, task.progress);
        }

        if (taskAdapter != null) {
            taskAdapter.notifyDataSetChanged();
        }
    }

    private void addNewTask() {
        if (downloadManager == null) {
            showToast("Download manager not initialized. Please wait...");
            return;
        }

        String inputFile = apRemoteFileEdit.getText().toString().trim();
        String remoteFileName;

        if (inputFile.isEmpty()) {
            remoteFileName = REMOTE_FILES[0];
            apRemoteFileEdit.setText(remoteFileName);
            showToast("Using sample file: " + remoteFileName);
        } else if (inputFile.matches("\\d+")) {
            try {
                int index = Integer.parseInt(inputFile);
                if (index >= 0 && index < REMOTE_FILES.length) {
                    remoteFileName = REMOTE_FILES[index];
                    showToast("Selected file #" + index + ": " + remoteFileName);
                } else {
                    remoteFileName = inputFile;
                }
            } catch (NumberFormatException e) {
                remoteFileName = inputFile;
            }
        } else if (!inputFile.contains("/")) {
            remoteFileName = "100MEDIA/" + inputFile;
        } else {
            remoteFileName = inputFile;
        }

        String localFileName = remoteFileName.contains("/")
            ? remoteFileName.substring(remoteFileName.lastIndexOf('/') + 1)
            : String.format("download_%s.mp4",
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()));

        File downloadDir = new File(getExternalFilesDir(null), "Downloads");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        String outputPath = new File(downloadDir, localFileName).getAbsolutePath();

        String taskId = downloadManager.addTask(remoteFileName, outputPath, false);

        if (taskId != null && !taskId.isEmpty()) {
            TaskInfo taskInfo = new TaskInfo(taskId, remoteFileName, outputPath);

            RMPDownloadTaskInfo downloadTask = downloadManager.getTask(taskId);
            if (downloadTask != null) {
                taskInfo.state = downloadTask.state;
                taskInfo.downloadedBytes = downloadTask.downloadedBytes;
                taskInfo.totalBytes = downloadTask.totalBytes;
            }

            taskList.add(taskInfo);
            taskMap.put(taskId, taskInfo);
            taskAdapter.notifyDataSetChanged();

            downloadManager.startTask(taskId);

            RMPLog.i(TAG, "Added task #%d: remote=%s, local=%s, taskId=%s",
                     taskList.size(), remoteFileName, localFileName, taskId);
            showToast(String.format("Task #%d added: %s", taskList.size(), remoteFileName));
            updateManagerStatus();
        } else {
            showToast("Failed to add task");
        }
    }

    // Predefined remote files
    private static final String[] REMOTE_FILES = {
        "100MEDIA/video001.mp4",
        "100MEDIA/video002.mp4",
        "100MEDIA/video003.mp4",
        "100MEDIA/image001.jpg",
        "100MEDIA/image002.jpg",
    };

    private void addMultipleTasks(int count) {
        if (downloadManager == null) {
            showToast("Download manager not initialized");
            return;
        }

        mainHandler.post(() -> {
            for (int i = 0; i < count; i++) {
                final int index = i;
                final String remoteFile = REMOTE_FILES[i % REMOTE_FILES.length];

                mainHandler.postDelayed(() -> {
                    String fileName = remoteFile.substring(remoteFile.lastIndexOf('/') + 1);

                    String localFileName;
                    if (count > REMOTE_FILES.length && index >= REMOTE_FILES.length) {
                        int dotIndex = fileName.lastIndexOf('.');
                        if (dotIndex > 0) {
                            localFileName = fileName.substring(0, dotIndex) + "_" + (index + 1) + fileName.substring(dotIndex);
                        } else {
                            localFileName = fileName + "_" + (index + 1);
                        }
                    } else {
                        localFileName = fileName;
                    }

                    // Use app-specific directory to avoid scoped storage restrictions
                    File downloadDir = new File(getExternalFilesDir(null), "Downloads");
                    if (!downloadDir.exists()) {
                        downloadDir.mkdirs();
                    }
                    String outputPath = new File(downloadDir, localFileName).getAbsolutePath();

                    String taskId = downloadManager.addTask(remoteFile, outputPath, true);

                    if (taskId != null && !taskId.isEmpty()) {
                        TaskInfo taskInfo = new TaskInfo(taskId, fileName, outputPath);

                        RMPDownloadTaskInfo downloadTask = downloadManager.getTask(taskId);
                        if (downloadTask != null) {
                            taskInfo.state = downloadTask.state;
                            taskInfo.downloadedBytes = downloadTask.downloadedBytes;
                            taskInfo.totalBytes = downloadTask.totalBytes;
                        }

                        taskList.add(taskInfo);
                        taskMap.put(taskId, taskInfo);
                        taskAdapter.notifyDataSetChanged();

                        RMPLog.i(TAG, "Added batch task %d/%d: %s -> %s",
                                index + 1, count, remoteFile, localFileName);
                    }

                    updateManagerStatus();
                }, i * 50);
            }

            showToast(String.format("Adding %d tasks...", count));
        });
    }

    private void startAllTasks() {
        if (downloadManager != null) {
            downloadManager.startAll();
            updateManagerStatus();
        }
    }

    private void pauseAllTasks() {
        if (downloadManager != null) {
            downloadManager.pauseAll();
            updateManagerStatus();
        }
    }

    private void cancelAllTasks() {
        if (downloadManager != null) {
            downloadManager.cancelAll();
            taskList.clear();
            taskMap.clear();
            taskAdapter.notifyDataSetChanged();
            updateManagerStatus();
        }
    }

    private void updateTaskState(String taskId, @com.microbit.RMPDownloadState int state) {
        TaskInfo task = taskMap.get(taskId);
        if (task != null) {
            task.state = state;
            taskAdapter.notifyDataSetChanged();
            updateManagerStatus();
        }
    }

    private long lastNotifyTime = 0;

    private void updateTaskProgress(String taskId, long downloadedBytes, long totalBytes, int progress) {
        TaskInfo task = taskMap.get(taskId);
        if (task != null) {
            long currentTime = System.currentTimeMillis();
            long timeDiff = currentTime - task.lastUpdateTime;

            if (timeDiff > 0) {
                long sizeDiff = downloadedBytes - task.lastDownloadedSize;
                task.speed = (sizeDiff * 1000.0) / timeDiff;
            }

            task.lastUpdateTime = currentTime;
            task.lastDownloadedSize = downloadedBytes;
            task.downloadedBytes = downloadedBytes;
            task.totalBytes = totalBytes;
            task.progress = progress;

            if (currentTime - lastNotifyTime > 500) {
                lastNotifyTime = currentTime;
                taskAdapter.notifyDataSetChanged();
            }
        }
    }

    private void updateTaskError(String taskId, String error) {
        TaskInfo task = taskMap.get(taskId);
        if (task != null) {
            task.error = error;
            task.state = com.microbit.RMPDownloadState.FAILED;
            taskAdapter.notifyDataSetChanged();
            updateManagerStatus();
        }
    }

    private void updateManagerStatus() {
        if (downloadManager != null) {
            int totalTasks = taskList.size();
            int activeTasks = 0;
            int pendingTasks = 0;
            int pausedTasks = 0;
            int completedTasks = 0;
            int failedTasks = 0;

            for (TaskInfo task : taskList) {
                switch (task.state) {
                    case com.microbit.RMPDownloadState.DOWNLOADING:
                        activeTasks++;
                        break;
                    case com.microbit.RMPDownloadState.PENDING:
                        pendingTasks++;
                        break;
                    case com.microbit.RMPDownloadState.PAUSED:
                        pausedTasks++;
                        break;
                    case com.microbit.RMPDownloadState.COMPLETED:
                        completedTasks++;
                        break;
                    case com.microbit.RMPDownloadState.FAILED:
                        failedTasks++;
                        break;
                }
            }

            String status = String.format(Locale.US,
                "AP Mode (Sequential Downloads)\n" +
                "Total: %d | Downloading: %d | Queued: %d\n" +
                "Paused: %d | Completed: %d | Failed: %d",
                totalTasks, activeTasks, pendingTasks,
                pausedTasks, completedTasks, failedTasks);

            managerStatusText.setText(status);
            pauseAllBtn.setEnabled(activeTasks > 0);
        }
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(Locale.US, "%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024));
        return String.format(Locale.US, "%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private String formatSpeed(double bytesPerSecond) {
        if (bytesPerSecond < 1024) return String.format(Locale.US, "%.0f B/s", bytesPerSecond);
        if (bytesPerSecond < 1024 * 1024) return String.format(Locale.US, "%.1f KB/s", bytesPerSecond / 1024);
        return String.format(Locale.US, "%.1f MB/s", bytesPerSecond / (1024 * 1024));
    }

    private String getStateText(@com.microbit.RMPDownloadState int state) {
        switch (state) {
            case com.microbit.RMPDownloadState.PENDING: return "PENDING";
            case com.microbit.RMPDownloadState.DOWNLOADING: return "DOWNLOADING";
            case com.microbit.RMPDownloadState.PAUSED: return "PAUSED";
            case com.microbit.RMPDownloadState.COMPLETED: return "COMPLETED";
            case com.microbit.RMPDownloadState.FAILED: return "FAILED";
            default: return "UNKNOWN";
        }
    }

    private int getStateColor(@com.microbit.RMPDownloadState int state) {
        switch (state) {
            case com.microbit.RMPDownloadState.PENDING: return 0xFF888888;
            case com.microbit.RMPDownloadState.DOWNLOADING: return 0xFF007ACC;
            case com.microbit.RMPDownloadState.PAUSED: return 0xFFFF8C00;
            case com.microbit.RMPDownloadState.COMPLETED: return 0xFF4CAF50;
            case com.microbit.RMPDownloadState.FAILED: return 0xFFF44336;
            default: return 0xFF888888;
        }
    }

    private class TaskListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return taskList.size();
        }

        @Override
        public Object getItem(int position) {
            return taskList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            TextView fileNameText;
            TextView stateText;
            TextView progressText;
            TextView sizeText;
            TextView speedText;
            TextView errorText;
            ProgressBar progressBar;
            Button pauseBtn;
            Button resumeBtn;
            Button cancelBtn;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            View view = convertView;

            if (view == null) {
                view = LayoutInflater.from(RMPUnifiedDownloadActivity.this)
                    .inflate(R.layout.item_download_task, parent, false);

                holder = new ViewHolder();
                holder.fileNameText = view.findViewById(R.id.tv_file_name);
                holder.stateText = view.findViewById(R.id.tv_state);
                holder.progressText = view.findViewById(R.id.tv_progress_text);
                holder.sizeText = view.findViewById(R.id.tv_size);
                holder.speedText = view.findViewById(R.id.tv_speed);
                holder.errorText = view.findViewById(R.id.tv_error);
                holder.progressBar = view.findViewById(R.id.progress_bar);
                holder.pauseBtn = view.findViewById(R.id.btn_task_pause);
                holder.resumeBtn = view.findViewById(R.id.btn_task_resume);
                holder.cancelBtn = view.findViewById(R.id.btn_task_cancel);

                holder.pauseBtn.setOnClickListener(v -> {
                    String taskId = (String) v.getTag();
                    if (taskId != null && v.isEnabled() && downloadManager != null) {
                        downloadManager.pauseTask(taskId);
                    }
                });

                holder.resumeBtn.setOnClickListener(v -> {
                    String taskId = (String) v.getTag();
                    if (taskId != null && v.isEnabled() && downloadManager != null) {
                        downloadManager.resumeTask(taskId);
                    }
                });

                holder.cancelBtn.setOnClickListener(v -> {
                    String taskId = (String) v.getTag();
                    if (taskId != null && v.isEnabled() && downloadManager != null) {
                        downloadManager.cancelTask(taskId);
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            TaskInfo toRemove = null;
                            for (TaskInfo t : taskList) {
                                if (t.taskId.equals(taskId)) {
                                    toRemove = t;
                                    break;
                                }
                            }
                            if (toRemove != null) {
                                taskList.remove(toRemove);
                                notifyDataSetChanged();
                                updateManagerStatus();
                            }
                        }, 200);
                    }
                });

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            TaskInfo task = taskList.get(position);

            holder.fileNameText.setText(task.fileName);
            holder.stateText.setText(getStateText(task.state));
            holder.stateText.setTextColor(getStateColor(task.state));

            holder.progressBar.setProgress(task.progress);
            holder.progressText.setText(String.format(Locale.US, "%d%%", task.progress));

            if (task.totalBytes > 0) {
                holder.sizeText.setText(String.format(Locale.US, "%s / %s",
                    formatSize(task.downloadedBytes), formatSize(task.totalBytes)));
            } else {
                holder.sizeText.setText("Calculating...");
            }

            if (task.state == com.microbit.RMPDownloadState.DOWNLOADING && task.speed > 0) {
                holder.speedText.setText(formatSpeed(task.speed));
                holder.speedText.setVisibility(View.VISIBLE);
            } else {
                holder.speedText.setVisibility(View.GONE);
            }

            if (!task.error.isEmpty()) {
                holder.errorText.setText("Error: " + task.error);
                holder.errorText.setVisibility(View.VISIBLE);
            } else {
                holder.errorText.setVisibility(View.GONE);
            }

            boolean canPause = task.state == com.microbit.RMPDownloadState.DOWNLOADING;
            boolean canResume = task.state == com.microbit.RMPDownloadState.PAUSED ||
                                task.state == com.microbit.RMPDownloadState.FAILED;
            boolean canCancel = task.state != com.microbit.RMPDownloadState.COMPLETED;

            holder.pauseBtn.setEnabled(canPause);
            holder.pauseBtn.setTag(task.taskId);

            holder.resumeBtn.setEnabled(canResume);
            holder.resumeBtn.setTag(task.taskId);

            holder.cancelBtn.setEnabled(canCancel);
            holder.cancelBtn.setTag(task.taskId);

            return view;
        }
    }

    @Override
    public void onConnectedResult(int code) {
        if (code == 0) {
            RMPLog.i(TAG, "AP Link connected successfully");
            mainHandler.post(() -> {
                showToast("AP Link Connected");
                if (downloadManager == null) {
                    initDownloadManager();
                }
            });
        } else {
            RMPLog.e(TAG, "AP Link connection failed with code: " + code);
            mainHandler.post(() -> showToast("AP Link Connection Failed: " + code));
        }
    }

    @Override
    public void onDisconnected() {
        RMPLog.i(TAG, "AP Link disconnected");
        mainHandler.post(() -> showToast("AP Link Disconnected"));
    }

    @Override
    public void OnLinkStatusChanged(int status) {
        RMPLog.i(TAG, "AP Link status changed: " + status);
    }

    @Override
    public void OnLinkEventPost(byte[] event, byte[] payload) {}

    @Override
    public void OnLinkPropertyPost(byte[] payload) {}

    @Override
    public void OnAlarmEvent(int type, int format, byte[] media, byte[] addition_string) {}

    @Override
    public void OnMediaRecordResult(int query_id, int status, String result) {}

    @Override
    public int OnLinkBindInterface(int sockFd) {
        return 0;
    }

    @Override
    public void OnLinkLogFile(int seq, int total_seq, byte[] payload) {}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (downloadManager != null) {
            downloadManager.shutdown();
            downloadManager.release();
        }
        if (apLink != null) {
            apLink.disconnect();
            apLink.deinit();
            apLink.release();
            apLink = null;
        }
    }
}
