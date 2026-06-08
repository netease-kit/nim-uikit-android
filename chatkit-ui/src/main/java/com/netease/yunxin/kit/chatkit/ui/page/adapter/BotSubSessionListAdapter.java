// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.v2.topic.V2NIMTopic;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.BotSubSessionUtils;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBotSubSessionItemBinding;
import com.netease.yunxin.kit.chatkit.ui.model.BotSubSessionItem;
import java.util.ArrayList;
import java.util.List;

public class BotSubSessionListAdapter
    extends RecyclerView.Adapter<BotSubSessionListAdapter.TopicViewHolder> {

  public interface OnTopicActionListener {
    void onTopicClick(V2NIMTopic topic);

    void onTopicLongClick(V2NIMTopic topic);
  }

  private final List<BotSubSessionItem> dataList = new ArrayList<>();
  private OnTopicActionListener listener;
  private int itemBackgroundColorRes = R.color.color_white;

  public void setOnTopicActionListener(OnTopicActionListener listener) {
    this.listener = listener;
  }

  public void setItemBackgroundColorRes(int colorRes) {
    itemBackgroundColorRes = colorRes;
    notifyDataSetChanged();
  }

  public void submitList(List<BotSubSessionItem> data) {
    dataList.clear();
    if (data != null) {
      dataList.addAll(data);
    }
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ChatBotSubSessionItemBinding binding =
        ChatBotSubSessionItemBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    return new TopicViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
    holder.bind(dataList.get(position));
  }

  @Override
  public int getItemCount() {
    return dataList.size();
  }

  class TopicViewHolder extends RecyclerView.ViewHolder {

    private final ChatBotSubSessionItemBinding binding;

    TopicViewHolder(@NonNull ChatBotSubSessionItemBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }

    void bind(BotSubSessionItem item) {
      Context context = binding.getRoot().getContext();
      V2NIMTopic topic = item.getTopic();
      binding.getRoot().setBackgroundColor(ContextCompat.getColor(context, itemBackgroundColorRes));
      binding.topicTitle.setText(BotSubSessionUtils.getTopicTitle(context, topic));
      binding.topicSummary.setText(
          item.getSummary() == null
              ? context.getString(R.string.chat_bot_sub_session_no_message)
              : item.getSummary());
      binding.topicTime.setText(BotSubSessionUtils.formatTime(context, item.getTime()));
      binding.topicUnreadDot.setVisibility(item.hasUnread() ? View.VISIBLE : View.GONE);
      binding
          .getRoot()
          .setOnClickListener(
              v -> {
                if (listener != null) {
                  listener.onTopicClick(topic);
                }
              });
      binding
          .getRoot()
          .setOnLongClickListener(
              v -> {
                if (listener != null) {
                  listener.onTopicLongClick(topic);
                }
                return true;
              });
    }
  }
}
