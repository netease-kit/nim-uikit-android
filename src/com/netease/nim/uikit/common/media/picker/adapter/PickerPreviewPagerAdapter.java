package com.netease.nim.uikit.common.media.picker.adapter;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.media.picker.activity.PickerAlbumPreviewActivity;
import com.netease.nim.uikit.common.media.picker.model.PhotoInfo;
import com.netease.nim.uikit.common.ui.imageview.BaseZoomableImageView;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;

import java.util.List;

public class PickerPreviewPagerAdapter extends PagerAdapter {

    private List<PhotoInfo> mList;
    private LayoutInflater mInflater;
    private UI mActivity;

    public PickerPreviewPagerAdapter(Context cx, List<PhotoInfo> list, LayoutInflater inflater, int width, int height, UI activity) {
        mList = list;
        mInflater = inflater;
        mActivity = activity;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View layout = (View) object;
        BaseZoomableImageView iv = layout.findViewById(R.id.imageView);
        iv.clear();
        container.removeView(layout);
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return (arg0 == arg1);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View layout = mInflater.inflate(R.layout.nim_preview_image_layout_multi_touch, null);
        container.addView(layout);
        layout.setTag(position);

        return layout;
    }

    private void finishUpdate() {

    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void setPrimaryItem(View container, int position, Object object) {
        ((PickerAlbumPreviewActivity) mActivity).updateCurrentImageView(position);
    }
}
