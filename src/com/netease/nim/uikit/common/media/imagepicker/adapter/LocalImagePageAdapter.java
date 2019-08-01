package com.netease.nim.uikit.common.media.imagepicker.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.media.imagepicker.ImagePicker;
import com.netease.nim.uikit.common.media.imagepicker.loader.GlideImageLoader;
import com.netease.nim.uikit.common.media.model.GLImage;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 */

public class LocalImagePageAdapter extends ImagePageAdapter {

    public LocalImagePageAdapter(Activity activity, ArrayList<GLImage> images) {
        super(activity, images);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.nim_adapter_preview_item, container,
                                                                        false);
        PhotoView photoView = view.findViewById(R.id.pv_preview);
        final View loadingView = view.findViewById(R.id.pb_loading);
        GLImage GLImage = images.get(position);

        ImagePicker.getInstance().getImageLoader().displayImage(mActivity, GLImage.getPath(), photoView,
                                                                getScreenWidth(), getScreenHeight(), new GlideImageLoader.LoadListener() {
                    @Override
                    public void onLoadSuccess() {
                        loadingView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadFailed() {

                    }
                });

        photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                if (listener != null) {
                    listener.OnPhotoTapListener(view, x, y);
                }
            }
        });

        container.addView(view);
        return view;
    }
}
