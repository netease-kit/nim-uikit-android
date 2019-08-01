package com.netease.nim.uikit.common.media.imagepicker.data;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.media.model.GLImage;

import java.util.ArrayList;

public abstract class CursorDataSource extends AbsDataSource implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ALL = 0;         //加载所有图片
    private static final int LOADER_CATEGORY = 1;    //分类加载图片

    private FragmentActivity activity;
    private ArrayList<ImageFolder> imageFolders = new ArrayList<>();   //所有的图片文件夹
    private final Loader loader;

    /**
     * @param activity 用于初始化LoaderManager，需要兼容到2.3
     * @param path     指定扫描的文件夹目录，可以为 null，表示扫描所有图片
     */
    CursorDataSource(FragmentActivity activity, String path) {
        this.activity = activity;

        LoaderManager loaderManager = activity.getSupportLoaderManager();
        if (path == null) {
            // 加载所有的图片
            loader = loaderManager.initLoader(getId(LOADER_ALL), null, this);
        } else {
            // 加载指定目录的图片
            Bundle bundle = new Bundle();
            bundle.putString("path", path);
            loader = loaderManager.initLoader(getId(LOADER_CATEGORY), bundle, this);
        }
    }

    protected abstract int getBaseId();

    protected int getId(int category) {
        return getBaseId() + category;
    }

    @Override
    public void reload() {
        if (manuallyControlLoader()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loader.startLoading();
                    loader.forceLoad();
                }
            }, 1000);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = getMediaStoreUri();
        String sort = MediaStore.MediaColumns.DATE_ADDED + " DESC";
        return new CursorLoader(activity, uri, getProjection(), getSelection(), getSelectionArgs(), sort);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        imageFolders.clear();
        if (data != null) {
            parserData(data);
        }
        //回调接口，通知图片数据准备完成
        onImagesLoaded(imageFolders);
        if (manuallyControlLoader()) {
            loader.stopLoading();
        }
    }

    private void parserData(Cursor data) {
        ArrayList<GLImage> allImages = new ArrayList<>();   //所有图片的集合,不分文件夹
        parserRealData(data, allImages, imageFolders);
        //防止没有图片报异常
        if (data.getCount() > 0) {
            //构造所有图片的集合
            ImageFolder allImagesFolder = new ImageFolder();
            allImagesFolder.name = activity.getResources().getString(R.string.all_images);
            allImagesFolder.path = "/";
            allImagesFolder.cover = allImages.size() > 0 ? allImages.get(0) : GLImage.Builder.newBuilder().build();
            allImagesFolder.images = allImages;
            imageFolders.add(0, allImagesFolder);  //确保第一条是所有图片
        }
    }

    protected abstract void parserRealData(Cursor data, ArrayList<GLImage> allImages, ArrayList<ImageFolder> imageFolders);

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        System.out.println("--------");
    }

    //某些机型如果查询VIDEO_PROJECTION，onLoadFinished会被多次调用
    //导致视频缩略图不断被重新刷新，无法正常显示
    //
    //所以收到onLoadFinished后stopLoading，Activity resume的时候再startLoading
    //
    //这样做会影响实时性，比如停留在当前页面截图的图片不会被自动加载，但是Activity切出去拍摄的内容会在回来resume时候加载到
    //而且回来时会重新加载，导致gridview重新定位，不能停留在之前的状态
    public boolean manuallyControlLoader() {
        return false;
        // return ImagePicker.getInstance().videoEnable() && DeviceUtil.isMiui();
    }

    abstract protected Uri getMediaStoreUri();

    abstract protected String[] getProjection();

    abstract protected String[] getSelectionArgs();

    @NonNull
    abstract protected String getSelection();
}
