package com.netease.nim.uikit.common.media.imagepicker.data;

import java.util.List;

/**
 */

public abstract class AbsDataSource {
    // 图片加载完成的回调接口
    private OnImagesLoadedListener loadedListener;

    void onImagesLoaded(List<ImageFolder> imageFolders) {
        if (loadedListener != null) {
            loadedListener.onImagesLoaded(imageFolders);
        }
    }

    public void setLoadedListener(OnImagesLoadedListener loadedListener) {
        this.loadedListener = loadedListener;
    }

    public abstract void reload();

    /** 所有图片加载完成的回调接口 */
    public interface OnImagesLoadedListener {
        void onImagesLoaded(List<ImageFolder> imageFolders);
    }
}
