package com.netease.nim.uikit.common.media.imagepicker.adapter;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import com.netease.nim.uikit.common.media.imagepicker.ImagePicker;
import com.netease.nim.uikit.common.media.imagepicker.Utils;
import com.netease.nim.uikit.common.media.model.GLImage;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;

public abstract class ImagePageAdapter extends PagerAdapter {

    private int screenWidth;
    private int screenHeight;
    protected ArrayList<GLImage> images;
    protected Activity mActivity;
    public PhotoViewClickListener listener;

    public ImagePageAdapter(Activity activity, ArrayList<GLImage> images) {
        this.mActivity = activity;
        this.images = images;
        DisplayMetrics dm = Utils.getScreenPix(activity);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
    }

    protected int getScreenWidth() {
        return screenWidth;
    }

    protected int getScreenHeight() {
        return screenHeight;
    }

    public void setData(ArrayList<GLImage> images) {
        this.images = images;
    }

    public void setPhotoViewClickListener(PhotoViewClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object instanceof View) {
            //FIXME YXFImageLoader
            ImagePicker.getInstance().getImageLoader().clearRequest((View) object);
            container.removeView((View) object);
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public interface PhotoViewClickListener {
        void OnPhotoTapListener(View view, float v, float v1);

        void onPhotoLongListener(PhotoView view, String url);
    }
}
