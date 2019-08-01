package com.netease.nim.uikit.common.media.imagepicker.data;


import com.netease.nim.uikit.common.media.model.GLImage;

import java.io.Serializable;
import java.util.ArrayList;


public class ImageFolder implements Serializable {

    public String name;  //当前文件夹的名字
    public String path;  //当前文件夹的路径
    public GLImage cover;   //当前文件夹需要要显示的缩略图，默认为最近的一次图片
    public ArrayList<GLImage> images = new ArrayList<>();  //当前文件夹下所有图片的集合

    /**
     * 只要文件夹的路径和名字相同，就认为是相同的文件夹
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ImageFolder)) {
            return false;
        }

        ImageFolder other = (ImageFolder) o;
        return this.path.equalsIgnoreCase(other.path) && this.name.equalsIgnoreCase(other.name);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }

    public void merge(ImageFolder other) {
        if (other != null && other.images != null) {
            images.addAll(other.images);
        }
    }

    public ImageFolder copyPath() {
        ImageFolder folder = new ImageFolder();
        folder.name = name;
        folder.path = path;
        folder.cover = cover;
        return folder;
    }
}
