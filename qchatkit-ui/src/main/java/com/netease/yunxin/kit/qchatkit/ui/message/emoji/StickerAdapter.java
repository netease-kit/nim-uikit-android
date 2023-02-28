// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.emoji;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.netease.yunxin.kit.qchatkit.ui.R;

public class StickerAdapter extends BaseAdapter {

  private Context context;
  private StickerCategory category;
  private int startIndex;

  public StickerAdapter(Context mContext, StickerCategory category, int startIndex) {
    this.context = mContext;
    this.category = category;
    this.startIndex = startIndex;
  }

  public int getCount() {
    int count = category.getStickers().size() - startIndex;
    count = Math.min(count, EmojiView.STICKER_PER_PAGE);
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
      convertView = View.inflate(context, R.layout.q_chat_sticker_picker_view, null);
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

    Glide.with(context)
        .load(StickerManager.getInstance().getStickerUri(sticker.getCategory(), sticker.getName()))
        .apply(
            new RequestOptions()
                .error(R.drawable.ic_img_failed)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .dontAnimate())
        .into(viewHolder.imageView);

    viewHolder.descLabel.setVisibility(View.GONE);

    return convertView;
  }

  class StickerViewHolder {
    public ImageView imageView;
    public TextView descLabel;
  }
}
