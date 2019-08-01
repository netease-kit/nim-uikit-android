package com.netease.nim.uikit.common.media.imagepicker.adapter.vh;

import android.support.annotation.CallSuper;
import android.view.View;
import android.view.ViewGroup;

import com.netease.nim.uikit.common.adapter.AdvancedAdapter;
import com.netease.nim.uikit.common.media.imagepicker.ImagePicker;
import com.netease.nim.uikit.common.media.imagepicker.Utils;
import com.netease.nim.uikit.common.media.model.GLImage;


/**
 */

public class ImageItemViewHolder extends ItemViewHolder {
    private final int imageSize;

    public ImageItemViewHolder(ViewGroup vp, ImagePicker imagePicker, AdvancedAdapter adapter) {
        super(vp, imagePicker, adapter);
        this.imageSize = Utils.getImageItemWidth(vp.getContext());
    }

    public void clearRequest() {
        ImagePicker.getInstance().getImageLoader().clearRequest(ivThumb);
    }

    @Override
    @CallSuper
    protected void onBindViewHolder(SectionModel model) {
        super.onBindViewHolder(model);

        GLImage GLImage = model.getImage();
        timeMask.setVisibility(View.GONE);
        getImagePicker().getImageLoader().displayImage(ivThumb.getContext(), GLImage.getPath(), ivThumb, imageSize, imageSize); //显示图片
    }
}
