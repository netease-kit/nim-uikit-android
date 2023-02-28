// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.emoji;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatEmojiItemLayoutBinding;

public class EmojiAdapter extends BaseAdapter {

  private final Context context;

  private final int startIndex;

  public EmojiAdapter(Context mContext, int startIndex) {
    this.context = mContext;
    this.startIndex = startIndex;
  }

  public int getCount() {
    int count = EmojiManager.getDisplayCount() - startIndex + 1;
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

  @SuppressLint({"ViewHolder", "InflateParams"})
  public View getView(int position, View convertView, ViewGroup parent) {
    QChatEmojiItemLayoutBinding viewBinding =
        QChatEmojiItemLayoutBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    convertView = viewBinding.getRoot();
    int count = EmojiManager.getDisplayCount();
    int index = startIndex + position;
    if (position == EmojiView.EMOJI_PER_PAGE || index == count) {
      viewBinding.ivEmoji.setBackgroundResource(R.drawable.ic_emoji_del);
    } else if (index < count) {
      viewBinding.ivEmoji.setBackground(EmojiManager.getDisplayDrawable(context, index));
    }

    return convertView;
  }
}
