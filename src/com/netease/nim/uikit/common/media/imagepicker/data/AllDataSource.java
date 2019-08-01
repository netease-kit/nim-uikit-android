package com.netease.nim.uikit.common.media.imagepicker.data;

import android.support.v4.app.FragmentActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**

 */

public class AllDataSource extends AbsDataSource {

    private CursorDataSource image;
    private CursorDataSource video;
    private final Result result;

    /**
     * @param activity       用于初始化LoaderManager，需要兼容到2.3
     * @param path           指定扫描的文件夹目录，可以为 null，表示扫描所有图片
     */
    AllDataSource(FragmentActivity activity, String path) {
        image = new ImageDataSource(activity, path);
        video = new VideoDataSource(activity, path);

        result = new Result();
        image.setLoadedListener(result.listener1);
        video.setLoadedListener(result.listener2);
    }

    @Override
    public void reload() {
        result.reset();

        image.reload();
        video.reload();
    }

    private class Result {
        private OnImagesLoadedListener listener1;
        private OnImagesLoadedListener listener2;
        private List<ImageFolder> result1;
        private List<ImageFolder> result2;

        Result() {
            listener1 = new OnImagesLoadedListener() {
                @Override
                public void onImagesLoaded(List<ImageFolder> imageFolders) {
                    result1 = imageFolders == null ? Collections.<ImageFolder>emptyList() : imageFolders;
                    check();
                }
            };

            listener2 = new OnImagesLoadedListener() {
                @Override
                public void onImagesLoaded(List<ImageFolder> imageFolders) {
                    result2 = imageFolders == null ? Collections.<ImageFolder>emptyList() : imageFolders;
                    check();
                }
            };
        }

        private void check() {
            if (result1 != null && result2 != null) {
                List<ImageFolder> result = new ArrayList<>();

                Map<ImageFolder, ImageFolder> merged = new LinkedHashMap<>();

                for (ImageFolder folder : result1) {
                    ImageFolder that = merged.get(folder);
                    if (that == null) {
                        that = folder.copyPath();
                        merged.put(folder, that);
                    }
                    that.merge(folder);
                }

                for (ImageFolder folder : result2) {
                    ImageFolder that = merged.get(folder);
                    if (that == null) {
                        that = folder.copyPath();
                        merged.put(folder, that);
                    }
                    that.merge(folder);
                }

                result.addAll(merged.values());
                for (ImageFolder folder : result) {
                    Collections.sort(folder.images);
                }

                AllDataSource.this.onImagesLoaded(result);
            }
        }

        private void reset() {
            result1 = null;
            result2 = null;
        }
    }
}
