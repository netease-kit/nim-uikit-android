package com.netease.nim.uikit.common.media.imagepicker;

import android.content.Context;
import android.graphics.Bitmap;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.media.imagepicker.data.ImageFolder;
import com.netease.nim.uikit.common.media.imagepicker.loader.ImageLoader;
import com.netease.nim.uikit.common.media.imagepicker.option.DefaultImagePickerOption;
import com.netease.nim.uikit.common.media.imagepicker.option.ImagePickerOption;
import com.netease.nim.uikit.common.media.imagepicker.view.CropImageView;
import com.netease.nim.uikit.common.media.model.GLImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ImagePicker {

    public static final String TAG = ImagePicker.class.getSimpleName();

    private File takeImageFile;

    public Bitmap cropBitmap;

    private ArrayList<GLImage> mSelectedImages = new ArrayList<>();   //选中的图片集合

    private List<ImageFolder> mImageFolders;      //所有的图片文件夹

    private int mCurrentImageFolderPosition = 0;  //当前选中的文件夹位置 0表示所有图片

    private ImageFolder mCurrentImageFolder = null;

    private List<OnImageSelectedListener> mImageSelectedListeners;          // 图片选中的监听回调

    private static ImagePicker mInstance;

    private ImagePicker() {
    }

    public static ImagePicker getInstance() {
        if (mInstance == null) {
            synchronized (ImagePicker.class) {
                if (mInstance == null) {
                    mInstance = new ImagePicker();
                }
            }
        }
        return mInstance;
    }


    public File getTakeImageFile() {
        return takeImageFile;
    }

    public void setTakeImageFile(File takeImageFile) {
        this.takeImageFile = takeImageFile;
    }

    public List<ImageFolder> getImageFolders() {
        return mImageFolders;
    }

    public void setImageFolders(List<ImageFolder> imageFolders) {
        mImageFolders = imageFolders;
        resetSelectImageFolder(imageFolders);
    }

    private void resetSelectImageFolder(List<ImageFolder> newImageFolders) {
        int selectedFolderIndex = getCurrentImageFolderPosition();
        if (selectedFolderIndex != 0) {
            ImageFolder oldSelectedFolder = getCurrentImageFolder();
            ImageFolder imageFolder = newImageFolders.get(selectedFolderIndex);
            if ((oldSelectedFolder == imageFolder) || (oldSelectedFolder != null && oldSelectedFolder.equals(
                    imageFolder))) {
                setCurrentImageFolderPosition(0);
            }
        }
    }

    public int getCurrentImageFolderPosition() {
        return mCurrentImageFolderPosition;
    }

    public ImageFolder getCurrentImageFolder() {
        return mCurrentImageFolder;
    }

    public void setCurrentImageFolderPosition(int mCurrentSelectedImageSetPosition) {
        mCurrentImageFolderPosition = mCurrentSelectedImageSetPosition;
        if (mImageFolders != null && mImageFolders.size() > mCurrentImageFolderPosition) {
            mCurrentImageFolder = mImageFolders.get(mCurrentSelectedImageSetPosition);
        } else {
            mCurrentImageFolder = null;
        }
    }

    public ArrayList<GLImage> getCurrentImageFolderItems() {
        return mImageFolders.get(mCurrentImageFolderPosition).images;
    }

    public boolean isSelect(GLImage item) {
        return mSelectedImages.contains(item);
    }

    public int selectOrder(GLImage item) {
        int i = 1;
        for (GLImage GLImage : mSelectedImages) {
            if (GLImage.equals(item)) {
                break;
            } else {
                i++;
            }
        }
        if (i > mSelectedImages.size()) {
            i = 0;
        }
        return i;
    }

    public boolean isSelectAll(List<GLImage> items) {
        return mSelectedImages.containsAll(items);
    }

    public long getSelectImageSize() {
        long size = 0;
        for (GLImage GLImage : mSelectedImages) {
            size += GLImage.getSize();
        }
        return size;
    }

    public int getSelectImageCount() {
        return mSelectedImages.size();
    }

    public boolean isMaxLimitOk() {
        return getSelectImageLeftCount() > 0;
    }

    public boolean isMinLimitOk() {
        return getSelectImageCount() > getSelectMin();
    }

    public int getSelectImageLeftCount() {
        return getSelectMax() - mSelectedImages.size();
    }

    public void clearSelectedImages() {
        if (mSelectedImages != null) {
            mSelectedImages.clear();
        }
    }

    public void clear() {
        if (mImageSelectedListeners != null) {
            mImageSelectedListeners.clear();
            mImageSelectedListeners = null;
        }
        if (mImageFolders != null) {
            mImageFolders.clear();
            mImageFolders = null;
        }
        if (mSelectedImages != null) {
            mSelectedImages.clear();
        }
        mCurrentImageFolderPosition = 0;
    }

    private ImagePickerOption option = DefaultImagePickerOption.getInstance();

    public ImagePickerOption getOption() {
        return option;
    }

    public void setOption(ImagePickerOption option) {
        option.checkParams();
        this.option = option;
    }

    public boolean videoEnable() {
        return !this.option.imageOnly();
    }

    public ArrayList<GLImage> getSelectedImages() {
        return mSelectedImages;
    }

    public String isSelectEnable(Context context, GLImage image) {
        if (!isMaxLimitOk()) {
            return context.getString(R.string.choose_max_num, getSelectMax());
        }
        if (option.isMixMode()) {
            if (image.isVideo() && image.getDuration() > getOption().getMaxVideoDuration() * 1000) {
                return context.getString(R.string.choose_video_duration_max_tip, getOption().getMaxVideoDuration());
            }
            return null;
        }
        boolean hasVideo = false;
        for (GLImage s : mSelectedImages) {
            if (s.isVideo()) {
                hasVideo = true;
                break;
            }
        }
        boolean hasImage = !hasVideo && mSelectedImages.size() > 0;
        if (hasVideo && image.isVideo()) {
            return context.getString(R.string.choose_max_num_video, 1);
        }
        if (hasVideo && !image.isVideo()) {
            return context.getString(R.string.choose_video_photo);
        }
        if (hasImage && image.isVideo()) {
            return context.getString(R.string.choose_video_photo);
        }
        if (image.isVideo() && image.getDuration() < getOption().getMinVideoDuration() * 1000) {
            return context.getString(R.string.choose_video_duration_min_tip);
        }
        if (image.isVideo() && image.getDuration() > getOption().getMaxVideoDuration() * 1000) {
            return context.getString(R.string.choose_video_duration_max_tip, getOption().getMaxVideoDuration());
        }
        return null;
    }

    public boolean isShowSection() {
        return option.isShowSection();
    }

    /**
     * 图片选中的监听
     */
    public interface OnImageSelectedListener {

        void onImageSelected(GLImage item, boolean isAdd);
    }

    public void addOnImageSelectedListener(OnImageSelectedListener l) {
        if (mImageSelectedListeners == null) {
            mImageSelectedListeners = new ArrayList<>();
        }
        mImageSelectedListeners.add(l);
    }

    public void removeOnImageSelectedListener(OnImageSelectedListener l) {
        if (mImageSelectedListeners == null) {
            return;
        }
        mImageSelectedListeners.remove(l);
    }

    public void addSelectedImageItem(GLImage item, boolean isAdd) {
        if (isAdd) {
            if (!mSelectedImages.contains(item)) {
                mSelectedImages.add(item);
            }
        } else {
            mSelectedImages.remove(item);
        }
        notifyImageSelectedChanged(item, isAdd);
    }

    private void notifyImageSelectedChanged(GLImage item, boolean isAdd) {
        if (mImageSelectedListeners == null) {
            return;
        }
        for (OnImageSelectedListener l : mImageSelectedListeners) {
            l.onImageSelected(item, isAdd);
        }
    }


    public boolean isMultiMode() {
        return option.isMultiMode();
    }

    public int getSelectMax() {
        return option.getSelectMax();
    }

    public int getSelectMin() {
        return option.getSelectMin();
    }

    public boolean isShowCamera() {
        return option.isShowCamera();
    }

    public boolean videoOnly() {
        return option.videoOnly();
    }

    public boolean imageOnly() {
        return option.imageOnly();
    }

    public boolean isSaveRectangle() {
        return option.isSaveRectangle();
    }

    public int getOutPutX() {
        return option.getOutPutX();
    }

    public int getOutPutY() {
        return option.getOutPutY();
    }

    public int getFocusWidth() {
        return option.getFocusWidth();
    }

    public int getFocusHeight() {
        return option.getFocusHeight();
    }

    public ImageLoader getImageLoader() {
        return option.getImageLoader();
    }

    public CropImageView.Style getStyle() {
        return option.getStyle();
    }

    public File getCropCacheFolder(Context context) {
        return option.getCropCacheFolder(context);
    }

    public boolean isCrop() {
        return option.isCrop();
    }

    public String getTitle() {
        return option.getTitle();
    }

    public ImagePickerOption.PickType getPickType() {
        return option.getPickType();
    }

    public boolean needCheckNetwork() {
        return option.needCheckNetwork();
    }
}