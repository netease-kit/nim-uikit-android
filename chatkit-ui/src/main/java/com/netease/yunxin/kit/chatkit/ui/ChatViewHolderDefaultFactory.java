// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import android.util.Pair;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatBaseMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.CommonBaseMessageViewHolder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class ChatViewHolderDefaultFactory implements IChatDefaultFactory {
  private final Map<Integer, Class<? extends ChatBaseMessageViewHolder>> viewHolderMap =
      new HashMap<>();
  private final Map<Integer, Pair<Class<? extends CommonBaseMessageViewHolder>, Integer>>
      commonBaseViewHolderMap = new HashMap<>();

  private IChatDefaultFactory factory;

  private ChatViewHolderDefaultFactory() {}

  private static class Holder {
    private static final ChatViewHolderDefaultFactory INSTANCE = new ChatViewHolderDefaultFactory();
  }

  public static ChatViewHolderDefaultFactory getInstance() {
    return Holder.INSTANCE;
  }

  public void config(IChatDefaultFactory factory) {
    this.factory = factory;
    Set<Integer> keys = viewHolderMap.keySet();
    if (!keys.isEmpty()) {
      for (Integer type : keys) {
        factory.addCustomViewHolder(type, viewHolderMap.get(type));
      }
    }
    Set<Integer> keysForCommon = commonBaseViewHolderMap.keySet();
    if (!keysForCommon.isEmpty()) {
      for (Integer type : keys) {
        Pair<Class<? extends CommonBaseMessageViewHolder>, Integer> value =
            commonBaseViewHolderMap.get(type);
        if (value == null) {
          continue;
        }
        factory.addCommonCustomViewHolder(type, value.first, value.second);
      }
    }
  }

  @Override
  public <T extends ChatBaseMessageViewHolder> void addCustomViewHolder(
      int type, Class<T> viewHolder) {
    if (factory == null) {
      viewHolderMap.put(type, viewHolder);
      return;
    }
    factory.addCustomViewHolder(type, viewHolder);
  }

  @Override
  public void removeCustomViewHolder(int type) {
    if (factory == null) {
      viewHolderMap.remove(type);
      return;
    }
    factory.removeCustomViewHolder(type);
  }

  @Override
  public <T extends CommonBaseMessageViewHolder> void addCommonCustomViewHolder(
      int type, Class<T> viewHolderClass, int layoutRes) {
    if (factory == null) {
      commonBaseViewHolderMap.put(type, new Pair<>(viewHolderClass, layoutRes));
      return;
    }
    factory.addCommonCustomViewHolder(type, viewHolderClass, layoutRes);
  }

  @Override
  public void removeCommonCustomViewHolder(int type) {
    if (factory == null) {
      commonBaseViewHolderMap.remove(type);
      return;
    }
    factory.removeCommonCustomViewHolder(type);
  }

  @Override
  public int getCustomViewType(ChatMessageBean messageBean) {
    if (factory == null) {
      return -1;
    }
    return factory.getCustomViewType(messageBean);
  }

  @Nullable
  @Override
  public CommonBaseMessageViewHolder createViewHolderCustom(
      @NonNull ViewGroup parent, int viewType) {
    if (factory == null) {
      return null;
    }
    return factory.createViewHolderCustom(parent, viewType);
  }

  @Override
  public int getItemViewType(ChatMessageBean messageBean) {
    if (factory == null) {
      return -1;
    }
    return factory.getItemViewType(messageBean);
  }

  @Override
  public CommonBaseMessageViewHolder createViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (factory == null) {
      return null;
    }
    return factory.createViewHolder(parent, viewType);
  }
}
