package com.netease.nim.uikit.common.media.imagepicker.video;

import android.content.Context;
import android.os.Handler;
import android.view.Surface;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.util.log.LogUtil;

import static com.netease.nim.uikit.common.media.imagepicker.video.GLMediaPlayer.PlayerState.PLAYER_ERROR;
import static com.netease.nim.uikit.common.media.imagepicker.video.GLMediaPlayer.PlayerState.PLAYER_IDLE;
import static com.netease.nim.uikit.common.media.imagepicker.video.GLMediaPlayer.PlayerState.PLAYER_INIT;
import static com.netease.nim.uikit.common.media.imagepicker.video.GLMediaPlayer.PlayerState.PLAYER_INITIALIZED;
import static com.netease.nim.uikit.common.media.imagepicker.video.GLMediaPlayer.PlayerState.PLAYER_PAUSED;
import static com.netease.nim.uikit.common.media.imagepicker.video.GLMediaPlayer.PlayerState.PLAYER_PENDING_DESTROY;
import static com.netease.nim.uikit.common.media.imagepicker.video.GLMediaPlayer.PlayerState.PLAYER_PLAYBACK_COMPLETE;
import static com.netease.nim.uikit.common.media.imagepicker.video.GLMediaPlayer.PlayerState.PLAYER_PREPARED;
import static com.netease.nim.uikit.common.media.imagepicker.video.GLMediaPlayer.PlayerState.PLAYER_PREPARING;
import static com.netease.nim.uikit.common.media.imagepicker.video.GLMediaPlayer.PlayerState.PLAYER_SEEKING;
import static com.netease.nim.uikit.common.media.imagepicker.video.GLMediaPlayer.PlayerState.PLAYER_STARTED;
import static com.netease.nim.uikit.common.media.imagepicker.video.GLMediaPlayer.PlayerState.PLAYER_STOPPED;

/**
 * 负责接收外部变化=>改变model状态=>根据model处理player状态
 */

public class GLMediaPlayerManager implements GLVideoModel.Callback, GLMediaPlayer.PlayerObserver {

    private static final String TAG = "YXMediaPlayerManager";

    private GLMediaPlayer mediaPlayer;

    private GLVideoModel attachedVideoModel;

    private Context context;

    private Handler mainHandler;

    public GLMediaPlayerManager() {
        mainHandler = new Handler();
    }

    public void attach(Context context, final GLVideoModel model) {
        this.context = context;
        ensurePlayer();

        if (attachedVideoModel == model) {
            attachedVideoModel.userPlay();
            return;
        }

        detach();

        attachedVideoModel = model;
        attachedVideoModel.reset();
        model.setCallback(this);
        model.fireAttachSurface();
        model.userPlay();
    }

    public void detach() {
        if (attachedVideoModel != null) {
            attachedVideoModel.userStop();
            attachedVideoModel.setCallback(null);
        }
        attachedVideoModel = null;
    }

    private void ensurePlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new GLMediaPlayer(mainHandler);
            mediaPlayer.create();
            mediaPlayer.addObserver(this);
        }
    }

    private void play(int position) {
        if (attachedVideoModel == null) {
            return;
        }

        ensurePlayer();
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.setDataSource(context, attachedVideoModel.getUri());
        mediaPlayer.pendingSeek(position);
        mediaPlayer.prepare();
    }


    private void start() {
        if (attachedVideoModel == null) {
            return;
        }

        ensurePlayer();
        mediaPlayer.start();
    }

    private void pause() {
        if (attachedVideoModel == null) {
            return;
        }

        ensurePlayer();
        mediaPlayer.pause();
    }

    private void stop() {
        if (attachedVideoModel == null) {
            return;
        }

        ensurePlayer();
        mediaPlayer.pause();
        mediaPlayer.stop();
    }

    @Override
    public void onModelTextureAvailable(GLVideoModel model, Surface surface) {
        ensurePlayer();
        mediaPlayer.setSurface(surface);

        if (attachedVideoModel != null) {
            attachedVideoModel.physicalPlay();
        }
    }

    @Override
    public void onModelTextureDestroy(GLVideoModel model) {
        ensurePlayer();
        mediaPlayer.setSurface(null);

        if (attachedVideoModel != null) {
            attachedVideoModel.physicalPause();
        }
    }

    @Override
    public void onNeedPlay(int position) {
        play(position);
    }

    @Override
    public void onNeedStop() {
        stop();
    }

    @Override
    public void onNeedPause() {
        pause();
    }

    @Override
    public void onNeedStart() {
        start();
    }

    @Override
    public void onPlaySizeChanged(int width, int height) {
        if (attachedVideoModel != null) {
            attachedVideoModel.setVideoSize(width, height);
        }
    }

    @Override
    public void onPlayStateChanged(int state) {
        LogUtil.i(TAG, "YXMediaPlayerManager onStateChanged:" + state);
        switch (state) {
            case PLAYER_INIT:
            case PLAYER_IDLE:
            case PLAYER_INITIALIZED:
                break;
            case PLAYER_PREPARED:
                break;

            case PLAYER_PREPARING:
            case PLAYER_SEEKING:
                if (attachedVideoModel != null) {
                    attachedVideoModel.setPlayerLoading();
                }
                break;

            case PLAYER_STARTED:
                if (attachedVideoModel != null) {
                    attachedVideoModel.setPlayerPlay();
                }
                break;

            case PLAYER_PAUSED:
                if (attachedVideoModel != null) {
                    attachedVideoModel.setPlayerPaused();
                }
                break;

            case PLAYER_PENDING_DESTROY:
                mediaPlayer.removeObservers();
                mediaPlayer.destroy();
                mediaPlayer = null;
                if (attachedVideoModel != null) {
                    attachedVideoModel.setPlayCurrent(0);
                    attachedVideoModel.setPlayerStop();
                }
                break;

            case PLAYER_STOPPED:
                if (attachedVideoModel != null) {
                    attachedVideoModel.setPlayCurrent(0);
                    attachedVideoModel.setPlayerStop();
                }
                break;

            case PLAYER_PLAYBACK_COMPLETE:
                if (attachedVideoModel != null) {
                    attachedVideoModel.setPlayCurrent(0);
                    attachedVideoModel.setPlayerComplete();
                }
                break;

            case PLAYER_ERROR:
                if (attachedVideoModel != null) {
                    if (attachedVideoModel.isPlayerPlay() || attachedVideoModel.isPlayerLoading()) {
                        ToastHelper.showToast(context, R.string.video_network_not_good);
                    }
                    mediaPlayer.reset();
                    attachedVideoModel.setPlayerError();
                }
                break;
        }
    }

    @Override
    public void onPlayProgressChanged(int current, int duration) {
        if (attachedVideoModel != null) {
            attachedVideoModel.setPlayCurrent(current);
            attachedVideoModel.setPlayDuration(duration);
            attachedVideoModel.notifyModelChanged();
        }
    }
}
