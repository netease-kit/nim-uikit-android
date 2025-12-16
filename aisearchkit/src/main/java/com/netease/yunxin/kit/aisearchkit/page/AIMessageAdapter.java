// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.aisearchkit.page;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.coexist.sdk.v2.ai.params.V2NIMAIModelCallContent;
import com.netease.yunxin.kit.aisearchkit.databinding.AiSearchMessageHolderBinding;
import java.util.ArrayList;
import java.util.List;

/** AI消息适配器 */
public class AIMessageAdapter extends RecyclerView.Adapter<AIMessageViewHolder> {

  // 消息列表
  private List<V2NIMAIModelCallContent> messages = new ArrayList<>();

  /**
   * 添加消息
   *
   * @param message 消息
   */
  public void addMessage(V2NIMAIModelCallContent message) {
    this.messages.add(0, message);
    notifyItemInserted(0);
  }

  @NonNull
  @Override
  public AIMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new AIMessageViewHolder(
        AiSearchMessageHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull AIMessageViewHolder holder, int position) {
    holder.onBindData(messages.get(position), position);
  }

  @Override
  public int getItemCount() {
    return messages.size();
  }
}
