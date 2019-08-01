package com.netease.nim.uikit.common.media.imagepicker.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

/**

 */

public class GLVideoView extends TextureView implements TextureView.SurfaceTextureListener {

    private Surface surface;

    private Callback callback;

    public GLVideoView(Context context) {
        super(context);
        init();
    }

    public GLVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GLVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GLVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;

        if (surface != null && callback != null){
            callback.onSurfaceAvailable(surface);
        }
    }

    public Callback getCallback() {
        return callback;
    }

    private void init() {
        setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        surface = new Surface(surfaceTexture);
        if (callback != null){
            callback.onSurfaceAvailable(surface);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        if (callback != null){
            callback.onSurfaceDestroyed();
        }

        if (surface != null){
            surface.release();
            surface = null;
        }

        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    public interface Callback {
        void onSurfaceAvailable(Surface surface);
        void onSurfaceDestroyed();
    }
}
