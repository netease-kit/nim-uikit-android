package com.netease.nim.uikit.common.media.imagepicker.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.media.imagepicker.ImagePicker;
import com.netease.nim.uikit.common.media.imagepicker.Utils;
import com.netease.nim.uikit.common.media.imagepicker.data.ImageFolder;

import java.util.ArrayList;
import java.util.List;

public class ImageFolderAdapter extends BaseAdapter {

    private ImagePicker imagePicker;
    private Activity mActivity;
    private LayoutInflater mInflater;
    private int mImageSize;
    private List<ImageFolder> imageFolders;
    private int lastSelected = 0;

    public ImageFolderAdapter(Activity activity, List<ImageFolder> folders) {
        mActivity = activity;
        if (folders != null && folders.size() > 0) {
            imageFolders = folders;
        } else {
            imageFolders = new ArrayList<>();
        }

        imagePicker = ImagePicker.getInstance();
        mImageSize = Utils.getImageItemWidth(mActivity);
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void refreshData(List<ImageFolder> folders) {
        if (folders != null && folders.size() > 0) {
            imageFolders = folders;
        } else {
            imageFolders.clear();
        }
        setSelectIndex(imagePicker.getCurrentImageFolderPosition());
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return imageFolders.size();
    }

    @Override
    public ImageFolder getItem(int position) {
        return imageFolders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.nim_adapter_folder_list_item, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ImageFolder folder = getItem(position);
        holder.folderName.setText(folder.name);
        holder.imageCount.setText(mActivity.getString(R.string.folder_image_count, folder.images.size()));
        imagePicker.getImageLoader().displayImage(mActivity, folder.cover.getPath(), holder.cover, mImageSize,
                mImageSize);

        if (lastSelected == position) {
            convertView.setBackgroundResource(R.color.color_f1f1f2);
        } else {
            convertView.setBackgroundResource(R.color.white);
        }

        return convertView;
    }

    public void setSelectIndex(int i) {
        if (lastSelected == i) {
            return;
        }
        lastSelected = i;
        notifyDataSetChanged();
    }

    public int getSelectIndex() {
        return lastSelected;
    }

    private static class ViewHolder {
        ImageView cover;
        TextView folderName;
        TextView imageCount;

        public ViewHolder(View view) {
            cover = view.findViewById(R.id.iv_cover);
            folderName = view.findViewById(R.id.tv_folder_name);
            imageCount = view.findViewById(R.id.tv_image_count);
            view.setTag(this);
        }
    }
}
