// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.emoji;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.netease.yunxin.kit.chatkit.emoji.ChatEmojiManager;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatEmojiItemLayoutBinding;

public class EmojiAdapter extends BaseAdapter {

  private final Context context;

  private final int startIndex;

  public EmojiAdapter(Context mContext, int startIndex) {
    this.context = mContext;
    this.startIndex = startIndex;
  }

  public int getCount() {
    int count = ChatEmojiManager.INSTANCE.getDisplayCount() - startIndex + 1;
    count = Math.min(count, EmojiView.EMOJI_PER_PAGE + 1);
    return count;
  }

  @Override
  public Object getItem(int position) {
    return null;
  }

  @Override
  public long getItemId(int position) {
    return startIndex + position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    if (convertView == null) {
      ChatEmojiItemLayoutBinding binding =
          ChatEmojiItemLayoutBinding.inflate(LayoutInflater.from(context), parent, false);
      holder = new ViewHolder(binding);
      convertView = binding.getRoot();
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    int count = ChatEmojiManager.INSTANCE.getDisplayCount();
    int index = startIndex + position;
    if (position == EmojiView.EMOJI_PER_PAGE || index == count) {
      holder.binding.ivEmoji.setBackgroundResource(R.drawable.ic_chat_emoji_del);
    } else if (index < count) {
      holder.binding.ivEmoji.setBackground(ChatEmojiManager.INSTANCE.getDisplayDrawable(index));
    } else {
      holder.binding.ivEmoji.setBackground(null);
    }

    return convertView;
  }

  private static final class ViewHolder {
    private final ChatEmojiItemLayoutBinding binding;

    private ViewHolder(ChatEmojiItemLayoutBinding binding) {
      this.binding = binding;
    }
  }
}
