// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.local.ui.fun;

import static com.netease.yunxin.kit.conversationkit.local.ui.common.ConversationConstant.LIB_TAG;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.viewholder.ViewHolderClickListener;
import com.netease.yunxin.kit.conversationkit.local.ui.databinding.FunLocalConversationTopItemBinding;
import com.netease.yunxin.kit.conversationkit.local.ui.fun.viewholder.FunConversationTopViewHolder;
import com.netease.yunxin.kit.conversationkit.local.ui.model.AIUserBean;
import java.util.ArrayList;
import java.util.List;

/** PIN会话列表适配器 */
public class FunConversationTopAdapter extends RecyclerView.Adapter<FunConversationTopViewHolder> {

  private final String TAG = "FunConversationTopAdapter";
  // 会话列表数据
  private final List<AIUserBean> conversationList = new ArrayList<>();
  // 点击事件监听
  private ViewHolderClickListener clickListener;

  // 设置数据，将原有数据清空，添加新数据
  public void setData(List<AIUserBean> data) {
    conversationList.clear();
    if (data != null) {
      conversationList.addAll(data);
      notifyDataSetChanged();
    }
  }

  // 更新数据
  public void update(List<AIUserBean> data) {
    for (int i = 0; data != null && i < data.size(); i++) {
      update(data.get(i));
    }
  }

  // 更新数据，如果数据已存在，则更新，不存在则添加
  public void update(AIUserBean data) {
    ALog.d(LIB_TAG, TAG, "update" + data.getAccountId());
    int removeIndex = -1;
    for (int j = 0; j < conversationList.size(); j++) {
      if (data.equals(conversationList.get(j))) {
        removeIndex = j;
        break;
      }
    }
    if (removeIndex > -1) {
      conversationList.remove(removeIndex);
      conversationList.add(removeIndex, data);
      notifyItemChanged(removeIndex);
    } else {
      conversationList.add(data);
      notifyItemInserted(conversationList.size() - 1);
    }
  }

  public void setViewHolderClickListener(ViewHolderClickListener listener) {
    this.clickListener = listener;
  }

  @NonNull
  @Override
  public FunConversationTopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    FunLocalConversationTopItemBinding binding =
        FunLocalConversationTopItemBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    return new FunConversationTopViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull FunConversationTopViewHolder holder, int position) {
    holder.onBindData(conversationList.get(position), position);
    holder.setItemOnClickListener(clickListener);
  }

  @Override
  public int getItemViewType(int position) {
    return 0;
  }

  @Override
  public int getItemCount() {
    return conversationList.size();
  }

  public AIUserBean getData(int index) {
    if (index >= 0 && index < conversationList.size()) {
      return conversationList.get(index);
    }
    return null;
  }
}
