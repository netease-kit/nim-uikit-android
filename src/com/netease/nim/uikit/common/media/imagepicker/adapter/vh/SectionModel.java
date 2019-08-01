package com.netease.nim.uikit.common.media.imagepicker.adapter.vh;

import android.view.View;

import com.netease.nim.uikit.common.media.imagepicker.adapter.ImageSectionAdapter;
import com.netease.nim.uikit.common.media.model.GLImage;

import java.util.LinkedList;
import java.util.List;


/**

 */

public class SectionModel {
    private final int index;
    private final Shared shared;
    private Listener listener;

    private SectionModel(int index, Shared shared) {
        this.index = index;
        this.shared = shared;
        shared.add(this);
    }

    Shared getShared() {
        return shared;
    }

    public static SectionModel wrap(int index, SectionModel begin) {
        return new SectionModel(index, begin.getShared());
    }

    public static SectionModel begin(String key, List<GLImage> images, int offset, ImageSectionAdapter.OnImageClickListener listener) {
        return new SectionModel(-1, new Shared(key, images, offset, listener));
    }

    public List<GLImage> getImages() {
        return shared.images;
    }

    public GLImage getImage() {
        return shared.images.get(index);
    }

    public String getKey() {
        return shared.key;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void notifyChanged() {
        shared.changed();
    }

    public void notifyClicked(View view) {
        if (shared.clickListener != null) {
            shared.clickListener.onImageItemClick(view, getImage(), getImagePosition());
        }
    }

    private int getImagePosition() {
        return shared.offset + index;
    }

    private void onChanged() {
        if (listener != null) {
            listener.onChanged();
        }
    }

    public static class Shared {
        private final List<GLImage> images;
        private final String key;
        private final List<SectionModel> group = new LinkedList<>();
        private final int offset;
        private final ImageSectionAdapter.OnImageClickListener clickListener;

        public Shared(String key, List<GLImage> images, int offset, ImageSectionAdapter.OnImageClickListener clickListener) {
            this.images = images;
            this.key = key;
            this.offset = offset;
            this.clickListener = clickListener;
        }

        void add(SectionModel model) {
            group.add(model);
        }

        public void changed() {
            for (SectionModel model : group) {
                model.onChanged();
            }
        }
    }

    public interface Listener {
        void onChanged();
    }

}
