package com.netease.nim.uikit.common.media.imagepicker.adapter.vh;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.adapter.AdvancedAdapter;
import com.netease.nim.uikit.common.adapter.BaseViewHolder;
import com.netease.nim.uikit.common.media.imagepicker.ImagePicker;


/**
 */

public abstract class ItemViewHolder extends BaseViewHolder<SectionModel> {

    final ImagePicker imagePicker;
    private final AdvancedAdapter adapter;
    protected ImageView ivThumb;
    protected View mask;
    private TextView cbCheck;
    protected TextView timeMask;
    private SectionModel model;
    protected ImageView videoIcon;

    private SectionModel.Listener listener = new SectionModel.Listener() {
        @Override
        public void onChanged() {
            int position = getAdapterPosition();
            if (position == -1) {
                return;
            }

            adapter.notifyItemChanged(position);
        }
    };

    ItemViewHolder(ViewGroup parent, ImagePicker imagePicker, AdvancedAdapter adapter) {
        super(parent, R.layout.nim_adapter_image_list_item);
        this.imagePicker = imagePicker;
        this.adapter = adapter;
    }

    @Override
    @CallSuper
    public void findViews() {
        ivThumb = itemView.findViewById(R.id.iv_thumb);
        mask = itemView.findViewById(R.id.mask);
        cbCheck = itemView.findViewById(R.id.cb_check);
        timeMask = itemView.findViewById(R.id.time_mask);
        videoIcon = itemView.findViewById(R.id.video_icon);
        cbCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (model == null) {
                    return;
                }
                cbCheck.setSelected(!cbCheck.isSelected());
                if (cbCheck.isSelected()) {
                    Context context = cbCheck.getContext();
                    String errorTip = imagePicker.isSelectEnable(context, model.getImage());
                    if (!TextUtils.isEmpty(errorTip)) {
                        Toast.makeText(context.getApplicationContext(), errorTip, Toast.LENGTH_SHORT).show();
                        cbCheck.setSelected(false);
                        return;
                    }
                }
                imagePicker.addSelectedImageItem(model.getImage(), cbCheck.isSelected());
            }
        });

        ivThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (model == null) {
                    return;
                }

                model.notifyClicked(v);
            }
        });
    }

    @Override
    @CallSuper
    protected void onBindViewHolder(SectionModel data) {
        mask.setVisibility(View.GONE);
        videoIcon.setVisibility(View.GONE);
        this.model = data;
        this.model.setListener(listener);

        // 根据是否多选，显示或隐藏checkbox
        if (imagePicker.isMultiMode()) {
            cbCheck.setVisibility(View.VISIBLE);
            int order = imagePicker.selectOrder(data.getImage());
            cbCheck.setSelected(order > 0);
            cbCheck.setText(order > 0 ? order + "" : "");
        } else {
            cbCheck.setVisibility(View.GONE);
        }
    }

    public ImagePicker getImagePicker() {
        return imagePicker;
    }
}
