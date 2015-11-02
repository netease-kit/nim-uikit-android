package com.netease.nim.uikit.session.emoji;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 每屏显示的贴图
 */
public class StickerAdapter extends BaseAdapter {

    private Context context;
    private StickerCategory category;
    private int startIndex;

    public StickerAdapter(Context mContext, StickerCategory category, int startIndex) {
        this.context = mContext;
        this.category = category;
        this.startIndex = startIndex;
    }

    public int getCount() {//获取每一页的数量
        int count = category.getStickers().size() - startIndex;
        count = Math.min(count, EmoticonView.STICKER_PER_PAGE);
        return count;
    }

    @Override
    public Object getItem(int position) {
        return category.getStickers().get(startIndex + position);
    }

    @Override
    public long getItemId(int position) {
        return startIndex + position;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        StickerViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.nim_sticker_picker_view, null);
            viewHolder = new StickerViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.sticker_thumb_image);
            viewHolder.descLabel = (TextView) convertView.findViewById(R.id.sticker_desc_label);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (StickerViewHolder) convertView.getTag();
        }

        int index = startIndex + position;
        if (index >= category.getStickers().size()) {
            return convertView;
        }

        StickerItem sticker = category.getStickers().get(index);
        if (sticker == null) {
            return convertView;
        }

        ImageLoader.getInstance().displayImage(StickerManager.getInstance().getStickerBitmapUri(sticker.getCategory()
                , sticker.getName()), viewHolder.imageView, StickerManager.getInstance().getStickerImageOptions(ScreenUtil.dip2px(64)));

        viewHolder.descLabel.setVisibility(View.GONE);

        return convertView;
    }

    class StickerViewHolder {
        public ImageView imageView;
        public TextView descLabel;
    }
}