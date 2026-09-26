package com.netease.yunxin.kit.chatkit.ui.view.emoji;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.bumptech.glide.Glide;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatStickerItemLayoutBinding;
import java.util.List;

final class StickerAdapter extends BaseAdapter {
  private final StickerAssetManager assetManager;
  private final List<StickerItem> items;

  StickerAdapter(StickerAssetManager assetManager, List<StickerItem> items) {
    this.assetManager = assetManager;
    this.items = items;
  }

  @Override
  public int getCount() {
    return items.size();
  }

  @Override
  public Object getItem(int position) {
    return items.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    boolean recycled = convertView != null;
    if (!recycled) {
      ChatStickerItemLayoutBinding binding =
          ChatStickerItemLayoutBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false);
      holder = new ViewHolder(binding);
      convertView = binding.getRoot();
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    if (recycled) {
      Glide.with(holder.binding.ivEmoji).clear(holder.binding.ivEmoji);
      holder.binding.ivEmoji.setImageDrawable(null);
    }

    StickerItem item = items.get(position);
    Glide.with(holder.binding.ivEmoji)
        .load(assetManager.getStickerPreviewPath(item))
        .into(holder.binding.ivEmoji);
    return convertView;
  }

  private static final class ViewHolder {
    private final ChatStickerItemLayoutBinding binding;

    private ViewHolder(ChatStickerItemLayoutBinding binding) {
      this.binding = binding;
    }
  }
}
