package com.netease.nim.uikit.common.media.imagepicker.adapter.vh;

import android.view.View;
import android.view.ViewGroup;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.adapter.AdvancedAdapter;
import com.netease.nim.uikit.common.media.imagepicker.ImagePicker;
import com.netease.nim.uikit.common.media.imagepicker.loader.GlideImageLoader;
import com.netease.nim.uikit.common.media.model.GLImage;
import com.netease.nim.uikit.common.util.sys.TimeUtil;


/**
 */

public class VideoItemViewHolder extends ItemViewHolder {

    public VideoItemViewHolder(ViewGroup vp, ImagePicker imagePicker, AdvancedAdapter adapter) {
        super(vp, imagePicker, adapter);
    }

    public void clearRequest() {
        ImagePicker.getInstance().getImageLoader().clearRequest(ivThumb);
    }

    @Override
    protected void onBindViewHolder(final SectionModel model) {
        super.onBindViewHolder(model);
        mask.setVisibility(View.VISIBLE);
        videoIcon.setVisibility(View.VISIBLE);
        // 本地视频的缩略图通过MediaStore以bitmap的形式拿到
        // Glide不能加载bitmap，需要自己处理
        // 不要把ivThumb的tag改掉，glide会使用这个字段
        final GLImage GLImage = model.getImage();
        timeMask.setVisibility(View.VISIBLE);
        timeMask.setText(TimeUtil.secToTime((int) (GLImage.getDuration() / 1000f)));
        GlideImageLoader.displayAlbumThumb(ivThumb, GLImage.getPath(), R.drawable.nim_image_default);
    }
}
