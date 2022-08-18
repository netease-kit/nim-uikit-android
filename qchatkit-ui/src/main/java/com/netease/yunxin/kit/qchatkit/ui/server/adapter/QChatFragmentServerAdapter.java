// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.server.adapter;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.qchatkit.observer.ObserverUnreadInfoResultHelper;
import com.netease.yunxin.kit.qchatkit.observer.QChatUnreadInfoSubscriberHelper;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerInfo;
import com.netease.yunxin.kit.qchatkit.ui.common.QChatCommonAdapter;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatFragmentServerListItemBinding;
import com.netease.yunxin.kit.qchatkit.ui.server.model.QChatFragmentServerInfo;
import java.util.List;

/**
 * Adapter for recyclerView in {@link
 * com.netease.yunxin.kit.qchatkit.ui.server.QChatServerFragment}.
 */
public class QChatFragmentServerAdapter
    extends QChatCommonAdapter<QChatFragmentServerInfo, QChatFragmentServerListItemBinding> {
  private static final String PAYLOADS_FOCUS = "focus";
  private static final String PAYLOADS_UNREAD = "unread";
  private int lastFocusPosition = 0;
  private OnClickListener<QChatFragmentServerInfo, QChatFragmentServerListItemBinding>
      clickListener;

  private OnUnreadInfoRefresh<QChatFragmentServerInfo> refreshListener;

  public QChatFragmentServerAdapter(Context context) {
    super(context, QChatFragmentServerListItemBinding.class);
  }

  public void setRefreshListener(OnUnreadInfoRefresh<QChatFragmentServerInfo> refreshListener) {
    this.refreshListener = refreshListener;
  }

  /** update server unread count info. */
  public void updateUnreadInfoList(List<Long> serverIdList) {
    for (Long serverId : serverIdList) {
      if (serverId == null) {
        continue;
      }
      QChatFragmentServerInfo serverInfo = getInfoByServerId(serverId);
      if (serverInfo != null) {
        serverInfo.unreadInfoItemMap = ObserverUnreadInfoResultHelper.getUnreadInfoMap(serverId);
      }
    }
    notifyItemRangeChanged(0, dataSource.size(), PAYLOADS_UNREAD);
  }

  public void setItemClickListener(
      OnClickListener<QChatFragmentServerInfo, QChatFragmentServerListItemBinding> listener) {
    this.clickListener = listener;
  }

  @Override
  public void addDataList(List<QChatFragmentServerInfo> data, boolean clearOld) {
    super.addDataList(data, clearOld);
    if (clearOld) {
      lastFocusPosition = 0;
    }
  }

  public void removeServerById(long serverId) {
    int index = dataSource.indexOf(QChatFragmentServerInfo.generateWithServerId(serverId));
    if (index >= 0) {
      dataSource.remove(index);
      notifyItemRemoved(index);
      if (lastFocusPosition >= getItemCount()) {
        lastFocusPosition--;
      }
      if (lastFocusPosition >= 0) {
        notifyItemChanged(lastFocusPosition);
      }
    }
  }

  public void updateData(QChatServerInfo info) {
    if (info == null) {
      return;
    }
    int index =
        dataSource.indexOf(QChatFragmentServerInfo.generateWithServerId(info.getServerId()));
    if (index >= 0) {
      QChatFragmentServerInfo data = dataSource.get(index);
      data.serverInfo = info;
      notifyItemChanged(index);
    }
  }

  @Override
  public void onBindViewHolder(
      @NonNull ItemViewHolder<QChatFragmentServerListItemBinding> holder,
      int position,
      QChatFragmentServerInfo data) {
    super.onBindViewHolder(holder, position, data);

    QChatFragmentServerListItemBinding binding = holder.binding;
    binding.cavIcon.setData(
        data.serverInfo.getIconUrl(),
        data.serverInfo.getName(),
        AvatarColor.avatarColor(data.serverInfo.getServerId()));
    binding.ivMsgTip.setVisibility(
        ObserverUnreadInfoResultHelper.hasUnreadMsg(data.serverInfo.getServerId())
            ? View.VISIBLE
            : View.GONE);

    updateFocus(holder, position, data);

    holder.itemView.setOnClickListener(
        v -> {
          QChatFragmentServerInfo lastInfo = getItemData(lastFocusPosition);
          if (lastInfo != null) {
            notifyItemChanged(lastFocusPosition, PAYLOADS_FOCUS);
          }
          lastFocusPosition = holder.getBindingAdapterPosition();
          notifyItemChanged(lastFocusPosition, PAYLOADS_FOCUS);
        });

    if (data.unreadInfoItemMap == null) {
      QChatUnreadInfoSubscriberHelper.fetchServerUnreadInfoCount(
          data.serverInfo,
          result -> {
            data.unreadInfoItemMap =
                ObserverUnreadInfoResultHelper.getMapFromResult(
                    data.serverInfo.getServerId(), result);
            notifyItemChanged(holder.getBindingAdapterPosition(), PAYLOADS_UNREAD);
          });
    }
  }

  @Override
  public void onBindViewHolder(
      @NonNull ItemViewHolder<QChatFragmentServerListItemBinding> holder,
      int position,
      @NonNull List<Object> payloads) {
    QChatFragmentServerInfo data = getItemData(position);
    if (payloads.contains(PAYLOADS_FOCUS)) {
      if (data != null) {
        updateFocus(holder, position, data);
      }
    } else if (payloads.contains(PAYLOADS_UNREAD)) {
      QChatFragmentServerListItemBinding binding = holder.binding;
      binding.ivMsgTip.setVisibility(
          ObserverUnreadInfoResultHelper.hasUnreadMsg(data.serverInfo.getServerId())
              ? View.VISIBLE
              : View.GONE);
      if (lastFocusPosition == position) {
        refreshListener.onCurrentRefresh(data);
      }
    } else {
      super.onBindViewHolder(holder, position, payloads);
    }
  }

  private void updateFocus(
      ItemViewHolder<QChatFragmentServerListItemBinding> holder,
      int position,
      QChatFragmentServerInfo data) {
    QChatFragmentServerListItemBinding binding = holder.binding;
    if (lastFocusPosition == position) {
      binding.ivFocus.setVisibility(View.VISIBLE);
      clickListener.onClick(data, holder);
    } else {
      binding.ivFocus.setVisibility(View.GONE);
    }
  }

  private QChatFragmentServerInfo getInfoByServerId(long serverId) {
    int index = dataSource.indexOf(new QChatFragmentServerInfo(new QChatServerInfo(serverId)));
    if (index < 0) {
      return null;
    }
    return dataSource.get(index);
  }

  public interface OnUnreadInfoRefresh<T> {
    void onCurrentRefresh(T data);
  }
}
