package com.netease.nim.uikit.common.media.imagepicker.adapter.vh;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.adapter.AdvancedAdapter;
import com.netease.nim.uikit.common.adapter.BaseViewHolder;
import com.netease.nim.uikit.common.media.imagepicker.ImagePicker;
import com.netease.nim.uikit.common.media.model.GLImage;

import java.util.List;


/**
 */

public class SectionViewHolder extends BaseViewHolder<SectionModel> {

    private final ImagePicker imagePicker;
    private final AdvancedAdapter adapter;
    public TextView sectionTitle;
    public CheckBox chooseAll;
    private SectionModel model;

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

    public SectionViewHolder(ViewGroup parent, ImagePicker imagePicker, AdvancedAdapter adapter) {
        super(parent, R.layout.nim_adapter_image_list_section);
        this.imagePicker = imagePicker;
        this.adapter = adapter;
    }

    @Override
    public void findViews() {
        sectionTitle = itemView.findViewById(R.id.section_title);
        chooseAll = itemView.findViewById(R.id.choose_all);
        chooseAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (model == null) {
                    return;
                }

                final List<GLImage> images = model.getImages();
                if (chooseAll.isChecked()) {

                    if (!imagePicker.isMaxLimitOk()) {
                        Context context = itemView.getContext();
                        String string = context.getString(R.string.choose_max_num, imagePicker.getSelectMax());
                        Toast.makeText(context.getApplicationContext(), string, Toast.LENGTH_LONG).show();
                        chooseAll.setChecked(false);
                        return;
                    }

                    int sizeUnselected = countSizeOfUnselected(images, imagePicker);
                    int count = Math.min(imagePicker.getSelectImageLeftCount(), sizeUnselected);

                    int i = 0;
                    boolean selectedAll = true;
                    for (GLImage image : images) {
                        if (i < count) {
                            if (imagePicker.isSelect(image)) {
                                continue;
                            }

                            String error = imagePicker.isSelectEnable(context, image);
                            if (!TextUtils.isEmpty(error)) {
                                selectedAll = false;
                                continue;
                            }

                            imagePicker.addSelectedImageItem(image, true);
                            i++;
                        } else {
                            break;
                        }
                    }
                    if (!selectedAll) {
                        chooseAll.setChecked(false);
                    }
                } else {
                    for (int i = 0; i < images.size(); i++) {
                        GLImage GLImage = images.get(i);
                        imagePicker.addSelectedImageItem(GLImage, false);
                    }
                }

                // model.notifyChanged();
            }
        });
    }

    private int countSizeOfUnselected(List<GLImage> images, ImagePicker imagePicker) {
        int i = 0;
        for (GLImage s : images) {
            if (imagePicker.isSelect(s)) {
                i++;
            }
        }
        return images.size() - i;
    }

    @Override
    protected void onBindViewHolder(SectionModel data) {
        this.model = data;
        this.model.setListener(listener);

        sectionTitle.setText(data.getKey());

        if (imagePicker.isMultiMode()) {
            chooseAll.setVisibility(View.VISIBLE);
            final List<GLImage> images = data.getImages();
            chooseAll.setChecked(imagePicker.isSelectAll(images));
        } else {
            chooseAll.setVisibility(View.GONE);
        }
    }
}
