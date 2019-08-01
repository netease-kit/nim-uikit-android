package com.netease.nim.uikit.common.media.model;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.io.Serializable;

public class GLImage implements GLMedia, Serializable, Comparable<GLImage> {
    private String name;       //图片的名字
    private String path;       //图片的路径
    private long size;         //图片的大小
    private int width;         //图片的宽度
    private int height;       //图片的高度
    private String mimeType;   //图片的类型
    private long addTime;      //图片的创建时间

    private long id;
    private long duration;

    public GLImage(String path) {
        this.path = path;
    }

    private GLImage(long id, String name, String path, long size, int width, int height, String mimeType,
                    long addTime) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.width = width;
        this.height = height;
        this.mimeType = mimeType;
        this.addTime = addTime;
    }

    public long getId() {
        return id;
    }

    private void setId(long id) {
        this.id = id;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public String getMimeType() {
        return mimeType != null ? mimeType : "";
    }

    public long getAddTime() {
        return addTime;
    }

    public boolean isVideo() {
        if (getMimeType() == null) {
            return false;
        }
        return getMimeType().contains("video");
    }

    @Override
    public int compareTo(@NonNull GLImage o) {
        if (addTime != o.addTime) {
            return addTime > o.addTime ? -1 : 1;
        }

        if (isVideo() && o.isVideo()) {
            return 0;
        }

        if (!isVideo() && !o.isVideo()) {
            return 0;
        }

        return isVideo() ? 1 : -1;

    }

    public static final class Builder {
        private long id;
        private long duration;

        private String name;       //图片的名字
        private String path;       //图片的路径
        private long size;         //图片的大小
        private int width;         //图片的宽度
        private int height;        //图片的高度
        private String mimeType;   //图片的类型
        private long addTime;      //图片的创建时间

        private Builder() {
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public static Builder newBuilder(GLImage GLImage) {
            return new Builder().setId(GLImage.getId()).setName(GLImage.getName()).setPath(GLImage.getPath())
                                .setSize(GLImage.getSize()).setWidth(GLImage.getWidth()).setHeight(
                            GLImage.getHeight()).setMimeType(GLImage.getMimeType()).setAddTime(
                            GLImage.getAddTime());
        }

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setDuration(long duration) {
            this.duration = duration;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public Builder setSize(long size) {
            this.size = size;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setMimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder setAddTime(long addTime) {
            this.addTime = addTime;
            return this;
        }

        public GLImage build() {
            GLImage GLImage = new GLImage(id, name, path, size, width, height, mimeType, addTime);
            GLImage.setId(id);
            GLImage.setDuration(duration);
            return GLImage;
        }
    }


    /**
     * 图片的路径相同就认为是同一张图片，拍照回来时的照片是当前时间，和插入数据库后的时间有一点偏差
     * 但是要求自动选中，所以这两张图需要被视为相等
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GLImage)) {
            return false;
        }

        GLImage other = (GLImage) o;
        return this.path.equalsIgnoreCase(other.path);
    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }

    public static final String KEY_NAME = "name";
    public static final String KEY_PATH = "path";
    public static final String KEY_SIZE = "size";
    public static final String KEY_WIDTH = "width";
    public static final String KEY_HEIGHT = "height";
    public static final String KEY_MIMETYPE = "mimeType";
    public static final String KEY_ADDTIME = "addTime";

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("path", path);
        bundle.putLong("size", size);
        bundle.putInt("width", width);
        bundle.putInt("height", height);
        bundle.putString("mimeType", mimeType);
        bundle.putLong("addTime", addTime);
        return bundle;
    }
}
