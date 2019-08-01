package com.netease.nim.uikit.common.media.imagepicker.adapter;

import android.view.View;
import android.view.ViewGroup;

import com.netease.nim.uikit.common.adapter.AdvancedAdapter;
import com.netease.nim.uikit.common.adapter.AdvancedDelegate;
import com.netease.nim.uikit.common.adapter.BaseViewHolder;
import com.netease.nim.uikit.common.media.imagepicker.Constants;
import com.netease.nim.uikit.common.media.imagepicker.ImagePicker;
import com.netease.nim.uikit.common.media.imagepicker.adapter.vh.CameraViewHolder;
import com.netease.nim.uikit.common.media.imagepicker.adapter.vh.ImageItemViewHolder;
import com.netease.nim.uikit.common.media.imagepicker.adapter.vh.SectionModel;
import com.netease.nim.uikit.common.media.imagepicker.adapter.vh.SectionViewHolder;
import com.netease.nim.uikit.common.media.imagepicker.adapter.vh.VideoItemViewHolder;
import com.netease.nim.uikit.common.media.imagepicker.ui.ImageBaseActivity;
import com.netease.nim.uikit.common.media.model.GLImage;
import com.netease.nim.uikit.common.util.media.MediaUtil;

import java.util.List;
import java.util.Map;

/**
 */

public class ImageSectionAdapter extends AdvancedAdapter {

    private ImageBaseActivity activity;
    private ImagePicker imagePicker;
    private static final int sTypeItemImage = 0;
    private static final int sTypeItemVideo = 1;
    private static final int sTypeItemTitle = 2;
    private static final int sTypeItemCamera = 3;

    public ImageSectionAdapter(ImageBaseActivity activity) {
        this.activity = activity;
        imagePicker = ImagePicker.getInstance();

        setDelegate(new AdvancedDelegate() {
            @Override
            public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                switch (viewType) {
                    case sTypeItemImage:
                        return new ImageItemViewHolder(parent, imagePicker, ImageSectionAdapter.this);
                    case sTypeItemVideo:
                        return new VideoItemViewHolder(parent, imagePicker, ImageSectionAdapter.this);
                    case sTypeItemTitle:
                        return new SectionViewHolder(parent, imagePicker, ImageSectionAdapter.this);
                    case sTypeItemCamera:
                        return new CameraViewHolder(parent, ImageSectionAdapter.this.activity, imagePicker);
                }
                return null;
            }
        });
    }

    public void refreshData(List<GLImage> images) {
        clear();

        if (images != null && images.size() > 0) {
            Map<String, List<GLImage>> groupedImages = MediaUtil.divideMedias(images);
            int offset = 0;
            for (int i = 0; i < groupedImages.keySet().size(); i++) {
                String key = (String) groupedImages.keySet().toArray()[i];
                List<GLImage> items = groupedImages.get(key);
                SectionModel begin = SectionModel.begin(key, items, offset, listener);

                // as section?
                if (imagePicker.isShowSection()) {
                    add(sTypeItemTitle, begin);
                }

                int index = 0;
                for (GLImage item : items) {
                    SectionModel model = SectionModel.wrap(index, begin);
                    if (item.isVideo()) {
                        add(sTypeItemVideo, model);
                    } else {
                        add(sTypeItemImage, model);
                    }
                    index++;
                }
                offset += items.size();
            }

        }

        if (imagePicker.isShowCamera()) {
            add(sTypeItemCamera, null, 0);
        }

        notifyDataSetChanged();
    }

    private OnImageClickListener listener;   //图片被点击的监听

    public void setOnImageItemClickListener(OnImageClickListener listener) {
        this.listener = listener;
    }

    public interface OnImageClickListener {
        void onImageItemClick(View view, GLImage GLImage, int position);
    }

    public int getSpanSize(int position) {
        int type = getItemViewType(position);
        switch (type) {
            case sTypeItemTitle:
                return Constants.GRIDVIEW_COLUMN;
            default:
                return 1;
        }
    }
}
