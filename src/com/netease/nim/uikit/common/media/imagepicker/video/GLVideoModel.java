package com.netease.nim.uikit.common.media.imagepicker.video;

import android.net.Uri;
import android.view.Surface;

import java.util.LinkedList;
import java.util.List;

/**

 */

public class GLVideoModel implements GLVideoView.Callback {
    private int userStatus = sUserStop;
    private int physicalStatus = sPhysicalDestroy;

    private static final int sUserPaused = 1;
    private static final int sUserPlaying = 2;
    private static final int sUserStop = 3;

    private static final int sPhysicalDestroy = 1;
    private static final int sPhysicalAvailable = 2;

    // 播放状态
    private int playerStatus = sPlayerStop;

    private static final int sPlayerStop = 1;
    private static final int sPlayerPlay = 2;
    private static final int sPlayerPaused = 3;
    private static final int sPlayerError = 4;
    private static final int sPlayerLoading = 5; // seek or prepare
    private static final int sPlayerComplete = 6;

    private GLVideoModel.Callback callback;

    private List<ModelObserver> observers = new LinkedList<>();

    private final Uri uri;

    private GLVideoView attachedVideoView;

    private int current;

    private int duration;
    private long feedDuration;
    private int videoWidth = -1;
    private int viewHeight = -1;

    public GLVideoModel(Uri uri, long duration) {
        this.uri = uri;
        this.feedDuration = duration;
    }

    public void attachVideoView(GLVideoView videoView){
        if (videoView == attachedVideoView){
            return;
        }

        if (attachedVideoView != null){
            attachedVideoView.setCallback(null);
            attachedVideoView = null;
        }

        attachedVideoView = videoView;
    }

    public void fireAttachSurface(){
        if (attachedVideoView != null){
            attachedVideoView.setCallback(this);
        }
    }

    public boolean isSurfaceAvailable(){
        return sPhysicalAvailable == physicalStatus;
    }

    public boolean isPlayerPlay(){
        return playerStatus == sPlayerPlay;
    }

    public boolean isPlayerPaused(){
        return playerStatus == sPlayerPaused;
    }

    public boolean isPlayerStopped(){
        return playerStatus == sPlayerStop;
    }

    public boolean isPlayerError(){
        return playerStatus == sPlayerError;
    }

    public boolean isPlayerLoading() {
        return playerStatus == sPlayerLoading;
    }

    public boolean isPlayerComplete() {
        return playerStatus == sPlayerComplete;
    }

    private boolean setPlayerStatus(int status){
        if (playerStatus != status){
            playerStatus = status;
            notifyModelChanged();
            return true;
        }
        return false;
    }

    public void setPlayerStop(){
        setPlayerStatus(sPlayerStop);
    }

    public void setPlayerPlay(){
        setPlayerStatus(sPlayerPlay);
    }

    public void setPlayerLoading(){
        setPlayerStatus(sPlayerLoading);
    }

    public void setPlayerComplete(){
        boolean success = setPlayerStatus(sPlayerComplete);

        if (success){
            userPause();
        }
    }

    public void setPlayerError(){
        boolean success = setPlayerStatus(sPlayerError);

        if (success){
            userStop();
        }
    }

    public void setPlayerPaused(){
        setPlayerStatus(sPlayerPaused);
    }

    public void userPlay(){
        int current = userStatus;
        if (current != sUserPlaying){
            userStatus = sUserPlaying;
        }
        userChanged();
    }

    public void userPause(){
        int current = userStatus;
        if (current != sUserPaused){
            userStatus = sUserPaused;
            userChanged();
        }
    }

    public void userStop(){
        int current = userStatus;
        if (current != sUserStop){
            userStatus = sUserStop;
        }
        userChanged();
    }

    public void physicalPlay(){
        int current = physicalStatus;
        if (current == sPhysicalDestroy){
            physicalStatus = sPhysicalAvailable;
            physicalChanged();
        }
    }

    public void physicalPause(){
        int current = physicalStatus;
        if (current == sPhysicalAvailable){
            physicalStatus = sPhysicalDestroy;
            physicalChanged();
        }
    }

    private void physicalChanged(){
        mergePlayerStatus();
    }

    private void userChanged(){
        mergePlayerStatus();
    }

    private void mergePlayerStatus(){
        if (playerStatus == sPlayerError || playerStatus == sPlayerLoading){
            if (userStatus == sUserStop || physicalStatus == sPhysicalDestroy){
                if (callback != null){
                    callback.onNeedStop();
                }
                return;
            }
        }

        if (playerStatus == sPlayerPaused){
            if (userStatus == sUserStop){
                if (callback != null){
                    callback.onNeedStop();
                }
                return;
            }
        }

        if (playerStatus == sPlayerPlay){
            if (userStatus == sUserPaused || physicalStatus == sPhysicalDestroy){
                if (callback != null){
                    callback.onNeedPause();
                }
                return;
            }

            if (userStatus == sUserStop){
                if (callback != null){
                    callback.onNeedPause();
                    callback.onNeedStop();
                }
                return;
            }
        }

        if (playerStatus == sPlayerPaused || playerStatus == sPlayerComplete){
            if (userStatus == sUserPlaying && physicalStatus == sPhysicalAvailable){
                if (callback != null){
                    callback.onNeedStart();
                    return;
                }
            }
        }

        if (playerStatus == sPlayerError || playerStatus == sPlayerStop){
            if (userStatus == sUserPlaying && physicalStatus == sPhysicalAvailable){
                if (callback != null){
                    callback.onNeedPlay(current);
                    return;
                }
            }
        }
    }

    public void notifyModelChanged(){
        for (ModelObserver observer : observers){
            observer.onModelChanged(this);
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void addObserver(ModelObserver observer){
        observers.add(observer);
    }

    public void removeObserver(ModelObserver observer){
        observers.remove(observer);
    }

    public void removeObservers(){
        observers.clear();
    }

    @Override
    public void onSurfaceAvailable(Surface surface) {
        if (callback != null){
            callback.onModelTextureAvailable(this, surface);
        }

        physicalPlay();
    }

    @Override
    public void onSurfaceDestroyed() {
        if (callback != null){
            callback.onModelTextureDestroy(this);
        }

        physicalPause();
        userPause();
    }

    public Uri getUri() {
        return uri;
    }

    public void setPlayCurrent(int current) {
        this.current = current;
    }

    public void setPlayDuration(int duration){
        this.duration = duration;
    }

    // ms
    public int getCurrent() {
        return current;
    }

    // ms
    public long getDuration() {
        if (feedDuration != 0){
            return feedDuration;
        }

        return duration;
    }

    public void reset() {
        userStatus = sUserStop;
        playerStatus = sPlayerStop;
    }

    public void setVideoSize(int width, int height) {
        if (width != this.videoWidth || height != this.viewHeight){
            this.videoWidth = width;
            this.viewHeight = height;
            notifyModelChanged();
        }
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public int getViewHeight() {
        return viewHeight;
    }

    public interface Callback {
        void onModelTextureAvailable(GLVideoModel model, Surface surface);
        void onModelTextureDestroy(GLVideoModel model);
        void onNeedPlay(int position);
        void onNeedStop();
        void onNeedPause();
        void onNeedStart();
    }

    public interface ModelObserver {
        void onModelChanged(GLVideoModel model);
    }

}
