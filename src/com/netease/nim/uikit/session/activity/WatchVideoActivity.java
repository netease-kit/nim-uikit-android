package com.netease.nim.uikit.session.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.activity.TActionBarActivity;
import com.netease.nim.uikit.common.util.file.FileUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.attachment.VideoAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.nimlib.sdk.msg.model.IMMessage;

/**
 * 视频播放界面
 * <p/>
 * Created by huangjun on 2015/4/11.
 */
public class WatchVideoActivity extends TActionBarActivity implements Callback {
    public static final String INTENT_EXTRA_DATA = "EXTRA_DATA";

    // player

    private MediaPlayer mediaPlayer;

    // context

    private Handler handlerTimes = new Handler();

    private ActionBar actionBar;

    private IMMessage message;

    // view

    private SurfaceView surfaceView;

    private SurfaceHolder surfaceHolder;

    private View videoIcon;

    private View downloadLayout;

    private View downloadProgressBackground;

    private View downloadProgressForeground;

    private TextView downloadProgressText;

    protected TextView fileInfoTextView;

    private TextView playTimeTextView;

    // state

    private boolean isSurfaceCreated = false;

    protected String videoFilePath;

    protected long videoLength = 0;

    private float lastPercent;

    private int playState = PLAY_STATE_STOP;

    private final static int PLAY_STATE_PLAYING = 1;

    private final static int PLAY_STATE_STOP = 2;

    private final static int PLAY_STATE_PAUSE = 3;

    // download control
    private boolean downloading;
    private ImageView downloadBtn;
    private AbortableFuture downloadFuture;

    public static void start(Context context, IMMessage message) {
        Intent intent = new Intent();
        intent.putExtra(WatchVideoActivity.INTENT_EXTRA_DATA, message);
        intent.setClass(context, WatchVideoActivity.class);
        context.startActivity(intent);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.nim_watch_video_activity);

        parseIntent();
        findViews();

        showVideoInfo();

        registerObservers(true);
        download();
    }

    public void onResume() {
        super.onResume();
        mediaPlayer = new MediaPlayer();

        if (isSurfaceCreated) {
            play();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        stopMediaPlayer();
    }

    @Override
    public void onBackPressed() {
        stopDownload();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerObservers(false);
    }

    private void parseIntent() {
        message = (IMMessage) getIntent().getSerializableExtra(INTENT_EXTRA_DATA);
    }

    private void findViews() {
        downloadLayout = findViewById(R.id.layoutDownload);
        downloadProgressBackground = findViewById(R.id.downloadProgressBackground);
        downloadProgressForeground = findViewById(R.id.downloadProgressForeground);
        downloadProgressText = (TextView) findViewById(R.id.downloadProgressText);
        videoIcon = findViewById(R.id.videoIcon);

        surfaceView = (SurfaceView) findViewById(R.id.videoView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(this);

        playTimeTextView = (TextView) findViewById(R.id.lblVideoTimes);
        playTimeTextView.setVisibility(View.INVISIBLE);
        fileInfoTextView = (TextView) findViewById(R.id.lblVideoFileInfo);
        playTimeTextView.setVisibility(View.INVISIBLE);

        downloadBtn = (ImageView) findViewById(R.id.control_download_btn);
        downloadBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (downloading) {
                    stopDownload();
                } else {
                    download();
                }
                downloadBtn.setImageResource(downloading ? R.drawable.nim_icon_download_pause : R.drawable.nim_icon_download_resume);
            }
        });

        actionBar = getSupportActionBar();
    }

    private void initVideoSize() {
        if (mediaPlayer == null) {
            return;
        }
        // 视频宽高
        int width = mediaPlayer.getVideoWidth();
        int height = mediaPlayer.getVideoHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        // 屏幕宽高
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;

        int videoRatio = width / height;
        int screenRatio = screenWidth / screenHeight;

        if (screenRatio > videoRatio) {
            int newHeight = screenHeight;
            int newWidth = screenHeight * width / height;
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    newWidth, newHeight);
            int margin = (screenWidth - newWidth) / 2;
            layoutParams.setMargins(margin, 0, margin, 0);
            surfaceView.setLayoutParams(layoutParams);
        } else {
            int newWidth = screenWidth;
            int newHeight = screenWidth * height / width;
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    newWidth, newHeight);
            int margin = (screenHeight - newHeight) / 2;
            layoutParams.setMargins(0, margin, 0, margin);
            surfaceView.setLayoutParams(layoutParams);
        }
    }

    /**
     * ****************************** MediaPlayer Start ********************************
     */
    private void stopMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            actionBar.show();

        }
    }

    /**
     * 处理视频播放时间
     */
    private Runnable timeRunnable = new Runnable() {
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                playState = PLAY_STATE_PLAYING;

                if (videoLength <= 0) {
                    playTimeTextView.setVisibility(View.INVISIBLE);
                } else {
                    // 由于mediaPlayer取到的时间不统一,采用消息体中的时间
                    int leftTimes = (int) (videoLength * 1000 - mediaPlayer.getCurrentPosition());
                    if (leftTimes < 0) {
                        leftTimes = 0;
                    }

                    playTimeTextView.setVisibility(View.VISIBLE);
                    long seconds = TimeUtil.getSecondsByMilliseconds(leftTimes);
                    playTimeTextView.setText(TimeUtil.secToTime((int) seconds));
                    handlerTimes.postDelayed(this, 1000);
                }

            }
        }
    };

    protected void pauseVideo() {
        videoIcon.setVisibility(View.VISIBLE);
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            handlerTimes.removeCallbacks(timeRunnable);
            playState = PLAY_STATE_PAUSE;
            actionBar.show();
        }
    }

    protected void resumeVideo() {
        videoIcon.setVisibility(View.GONE);
        if (mediaPlayer != null) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                playState = PLAY_STATE_PLAYING;
                handlerTimes.postDelayed(timeRunnable, 100);
                actionBar.hide();
            }
        }
    }

    protected void playVideo() {
        videoIcon.setVisibility(View.GONE);
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            } else {
                if (isSurfaceCreated) {
                    mediaPlayer.setDisplay(surfaceHolder);
                } else {
                    Toast.makeText(WatchVideoActivity.this, R.string.look_video_fail_try_again,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(videoFilePath);
            } catch (Exception e) {
                Toast.makeText(WatchVideoActivity.this, R.string.look_video_fail_try_again,
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return;
            }

            setMediaPlayerListener();
            mediaPlayer.prepareAsync();
            actionBar.hide();
        }
    }

    private void setMediaPlayerListener() {
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoIcon.setVisibility(View.VISIBLE);

                playState = PLAY_STATE_STOP;
                playTimeTextView.setText("00:00");
                handlerTimes.removeCallbacks(timeRunnable);
                actionBar.show();
            }
        });

        mediaPlayer.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    String type = "video/3gp";
                    Uri name = Uri.parse("file://" + videoFilePath);
                    intent.setDataAndType(name, type);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Toast.makeText(WatchVideoActivity.this, R.string.look_video_fail,
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp)// 缓冲完毕
            {
                mediaPlayer.start();// 播放视频
                initVideoSize();//根据视频宽高，调整视频显示
                // if (position > 0) {
                // mediaPlayer.seekTo(position);
                // mediaPlayer.start();
                // }
                handlerTimes.postDelayed(timeRunnable, 100);
            }
        });
    }

    /**
     * **************************** 下载视频 *********************************
     */
    private void showVideoInfo() {
        long duration = ((VideoAttachment) message.getAttachment()).getDuration();
        long fileSize = ((VideoAttachment) message.getAttachment()).getSize();

        if (duration <= 0) {
            fileInfoTextView.setText("大小: " + FileUtil.formatFileSize(fileSize));
        } else {
            long seconds = TimeUtil.getSecondsByMilliseconds(duration);
            fileInfoTextView.setText("大小: " + FileUtil.formatFileSize(fileSize) + ",时长: "
                    + String.valueOf(seconds) + " 秒");
            videoLength = seconds;
        }
    }

    private void registerObservers(boolean register) {
        NIMClient.getService(MsgServiceObserve.class).observeMsgStatus(statusObserver, register);
        NIMClient.getService(MsgServiceObserve.class).observeAttachmentProgress(attachmentProgressObserver, register);
    }

    private Observer<IMMessage> statusObserver = new Observer<IMMessage>() {
        @Override
        public void onEvent(IMMessage msg) {
            if (!msg.isTheSame(message) || isDestroyedCompatible()) {
                return;
            }

            if (msg.getAttachStatus() == AttachStatusEnum.transferred && isVideoHasDownloaded(msg)) {
                onDownloadSuccess(msg);
            } else if (msg.getAttachStatus() == AttachStatusEnum.fail) {
                onDownloadFailed();
            }
        }
    };

    private Observer<AttachmentProgress> attachmentProgressObserver = new Observer<AttachmentProgress>() {
        @Override
        public void onEvent(AttachmentProgress p) {
            long total = p.getTotal();
            long progress = p.getTransferred();
            float percent = (float) progress / (float) total;
            if (percent > 1.0) {
                // 消息中标识的文件大小有误，小于实际大小
                percent = (float) 1.0;
                progress = total;
            }
            if (percent - lastPercent >= 0.10) {
                lastPercent = percent;
                setDownloadProgress(getString(R.string.download_video), progress, total);
            } else {
                if (lastPercent == 0.0) {
                    lastPercent = percent;
                    setDownloadProgress(getString(R.string.download_video), progress, total);
                }
                if (percent == 1.0 && lastPercent != 1.0) {
                    lastPercent = percent;
                    setDownloadProgress(getString(R.string.download_video), progress, total);
                }
            }
        }
    };

    private void setDownloadProgress(final String label, final long progress, final long total) {
        final float percent = (float) ((double) progress / total);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutParams fgLayoutParams = downloadProgressForeground.getLayoutParams();
                fgLayoutParams.width = (int) (downloadProgressBackground.getWidth() * percent);
                downloadProgressForeground.setLayoutParams(fgLayoutParams);

                downloadProgressText.setText(String.format(getString(R.string.download_progress_description), label,
                        FileUtil.formatFileSize(progress), FileUtil.formatFileSize(total)));
            }
        });
    }

    private boolean isVideoHasDownloaded(final IMMessage message) {
        if (message.getAttachStatus() == AttachStatusEnum.transferred &&
                !TextUtils.isEmpty(((VideoAttachment) message.getAttachment()).getPath())) {
            return true;
        }

        return false;
    }

    private void download() {
        if (!isVideoHasDownloaded(message)) {
            // async download original image
            onDownloadStart(message);
            downloadFuture = NIMClient.getService(MsgService.class).downloadAttachment(message, false);
            downloading = true;
        }
    }

    private void play() {
        if (isVideoHasDownloaded(message)) {
            onDownloadSuccess(message);
        }
    }

    private void onDownloadSuccess(final IMMessage message) {
        downloadFuture = null;

        downloadLayout.setVisibility(View.GONE);

        videoFilePath = ((VideoAttachment) message.getAttachment()).getPath();

        surfaceView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (playState == PLAY_STATE_PAUSE) {
                    resumeVideo();
                } else if (playState == PLAY_STATE_PLAYING) {
                    pauseVideo();
                } else if (playState == PLAY_STATE_STOP) {
                    playVideo();
                }
            }
        });
        playVideo();
    }

    private void onDownloadFailed() {
        downloadFuture = null;

        downloadLayout.setVisibility(View.GONE);
        Toast.makeText(WatchVideoActivity.this, R.string.download_video_fail, Toast.LENGTH_SHORT).show();
    }

    private void onDownloadStart(IMMessage message) {
        setDownloadProgress(getString(R.string.download_video), 0, ((VideoAttachment) message.getAttachment()).getSize());
        downloadLayout.setVisibility(View.VISIBLE);
    }

    private void stopDownload() {
        if (downloadFuture != null) {
            downloadFuture.abort();
            downloadFuture = null;

            downloading = false;
        }
    }

    /**
     * ***************************** SurfaceHolder Callback **************************************
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!isSurfaceCreated) {
            isSurfaceCreated = true;
            play();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isSurfaceCreated = false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initVideoSize();// 屏幕旋转后，改变视频显示布局
    }
}
