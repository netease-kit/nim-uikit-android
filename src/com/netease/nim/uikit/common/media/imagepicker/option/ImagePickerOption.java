package com.netease.nim.uikit.common.media.imagepicker.option;

import android.content.Context;
import android.support.annotation.NonNull;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.media.imagepicker.Constants;
import com.netease.nim.uikit.common.media.imagepicker.loader.GlideImageLoader;
import com.netease.nim.uikit.common.media.imagepicker.loader.ImageLoader;
import com.netease.nim.uikit.common.media.imagepicker.view.CropImageView;

import java.io.File;
import java.io.Serializable;

/**
 */

public class ImagePickerOption implements Serializable {
    public enum PickType {
        Image(R.string.pick_image), Video(R.string.pick_video), All(R.string.pick_album);

        private final int title;

        PickType(int title) {
            this.title = title;
        }

        public int getTitle() {
            return title;
        }

    }

    private PickType pickType;

    private boolean multiMode = false;    //图片选择模式

    private int selectMax = Constants.MAX_SELECT_NUM;         //最大选择图片数量
    private int selectMin = 0;
    private boolean crop = false;         //裁剪
    private boolean showCamera = false;   //显示相机
    private boolean isSaveRectangle = false;  //裁剪后的图片是否是矩形，否者跟随裁剪框的形状
    private int outPutX = 800;           //裁剪保存宽度
    private int outPutY = 800;           //裁剪保存高度
    private boolean section = false;
    private int focusWidth = Constants.PORTRAIT_IMAGE_WIDTH;         //焦点框的宽度
    private int focusHeight = Constants.PORTRAIT_IMAGE_WIDTH;        //焦点框的高度
    private ImageLoader imageLoader = new GlideImageLoader();     //图片加载器
    private CropImageView.Style style = CropImageView.Style.RECTANGLE; //裁剪框的形状
    private boolean checkNetwork = false;
    private boolean mixMode = false;// 是否能够混选
    private static final int FEED_VIDEO_DURATION_MAX = 15; //单位秒
    private static final int FEED_VIDEO_DURATION_MIN = 3;

    private int maxVideoDuration = FEED_VIDEO_DURATION_MAX;
    private int minVideoDuration = FEED_VIDEO_DURATION_MIN;
    private File cropCacheFolder;

    private String mTitle;

    public boolean isMultiMode() {
        return multiMode;
    }

    public ImagePickerOption setMultiMode(boolean multiMode) {
        this.multiMode = multiMode;
        return this;
    }

    public boolean isMixMode() {
        return mixMode;
    }

    public ImagePickerOption setMixMode(boolean mixMode) {
        this.mixMode = mixMode;
        return this;
    }

    public int getSelectMax() {
        return selectMax;
    }

    public int getSelectMin() {
        return selectMin;
    }

    public ImagePickerOption setSelectMax(int selectMax) {
        this.selectMax = selectMax;
        return this;
    }

    public ImagePickerOption setSelectMin(int selectMin) {
        this.selectMin = selectMin;
        return this;
    }

    public int getMaxVideoDuration() {
        return maxVideoDuration;
    }

    public ImagePickerOption setMaxVideoDuration(int maxVideoDuration) {
        this.maxVideoDuration = maxVideoDuration;
        return this;
    }

    public int getMinVideoDuration() {
        return minVideoDuration;
    }

    public void setMinVideoDuration(int minVideoDuration) {
        this.minVideoDuration = minVideoDuration;
    }

    public boolean needCheckNetwork() {
        return checkNetwork;
    }

    public ImagePickerOption setCheckNetwork(boolean checkNetwork) {
        this.checkNetwork = checkNetwork;
        return this;
    }

    public boolean isCrop() {
        return crop;
    }

    public ImagePickerOption setCrop(boolean crop) {
        this.crop = crop;
        return this;
    }

    public ImagePickerOption asSection() {
        section = true;
        return this;
    }

    public boolean isShowCamera() {
        return showCamera;
    }

    public boolean isShowSection() {
        return section;
    }

    public ImagePickerOption setShowCamera(boolean showCamera) {
        this.showCamera = showCamera;
        return this;
    }

    public boolean videoOnly() {
        return pickType == PickType.Video;
    }

    public boolean imageOnly() {
        return pickType == PickType.Image;
    }

    public boolean isSaveRectangle() {
        return isSaveRectangle;
    }

    public ImagePickerOption setSaveRectangle(boolean isSaveRectangle) {
        this.isSaveRectangle = isSaveRectangle;
        return this;
    }

    public int getOutPutX() {
        return outPutX;
    }

    public ImagePickerOption setOutPutX(int outPutX) {
        this.outPutX = outPutX;
        return this;
    }

    public int getOutPutY() {
        return outPutY;
    }

    public ImagePickerOption setOutPutY(int outPutY) {
        this.outPutY = outPutY;
        return this;
    }

    public int getFocusWidth() {
        return focusWidth;
    }

    public ImagePickerOption setFocusWidth(int focusWidth) {
        this.focusWidth = focusWidth;
        return this;
    }

    public int getFocusHeight() {
        return focusHeight;
    }

    public ImagePickerOption setFocusHeight(int focusHeight) {
        this.focusHeight = focusHeight;
        return this;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public ImagePickerOption setImageLoader(@NonNull ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
        return this;
    }

    public CropImageView.Style getStyle() {
        return style;
    }

    public ImagePickerOption setStyle(CropImageView.Style style) {
        this.style = style;
        return this;
    }

    public File getCropCacheFolder(Context context) {
        if (cropCacheFolder == null) {
            cropCacheFolder = new File(context.getCacheDir() + "/ImagePicker/cropTemp/");
        }
        return cropCacheFolder;
    }

    public ImagePickerOption setCropCacheFolder(File cropCacheFolder) {
        this.cropCacheFolder = cropCacheFolder;
        return this;
    }

    public ImagePickerOption setTitle(String title) {
        mTitle = title;
        return this;
    }

    public String getTitle() {
        return mTitle;
    }

    public PickType getPickType() {
        return pickType;
    }

    public ImagePickerOption setPickType(PickType pickType) {
        this.pickType = pickType;
        return this;
    }

    public void checkParams() {
        if (videoOnly() && isMultiMode()) {
            throw new IllegalArgumentException("can't set Video with MultiMode");
        }
    }
}
