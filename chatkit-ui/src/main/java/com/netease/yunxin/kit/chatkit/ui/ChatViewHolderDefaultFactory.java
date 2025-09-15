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

/** 默认的ViewHolder工厂类 该类实现了IChatDefaultFactory接口，用于创建自定义消息视图Holder */
public final class ChatViewHolderDefaultFactory implements IChatDefaultFactory {

  // 存储自定义消息视图Holder类的映射，键为消息类型，值为对应的ViewHolder类
  private final Map<Integer, Class<? extends ChatBaseMessageViewHolder>> viewHolderMap =
      new HashMap<>();
  // 存储通用自定义消息视图Holder类及其布局资源ID的映射，键为消息类型，值为包含ViewHolder类和布局资源ID的Pair
  private final Map<Integer, Pair<Class<? extends CommonBaseMessageViewHolder>, Integer>>
      commonBaseViewHolderMap = new HashMap<>();

  // 自定义的ViewHolder工厂接口实例
  private IChatDefaultFactory factory;

  // 私有构造函数，防止外部实例化
  private ChatViewHolderDefaultFactory() {}

  // 静态内部类，用于实现单例模式的实例持有
  private static class Holder {
    // 单例实例
    private static final ChatViewHolderDefaultFactory INSTANCE = new ChatViewHolderDefaultFactory();
  }

  // 获取单例实例的静态方法
  public static ChatViewHolderDefaultFactory getInstance() {
    return Holder.INSTANCE;
  }

  // 配置自定义的ViewHolder工厂，并将已存储的自定义ViewHolder添加到工厂中
  public void config(IChatDefaultFactory factory) {
    this.factory = factory;
    // 获取普通自定义ViewHolder映射的所有键
    Set<Integer> keys = viewHolderMap.keySet();
    // 如果键集合不为空，则将所有自定义ViewHolder添加到工厂中
    if (!keys.isEmpty()) {
      for (Integer type : keys) {
        factory.addCustomViewHolder(type, viewHolderMap.get(type));
      }
    }
    // 获取通用自定义ViewHolder映射的所有键
    Set<Integer> keysForCommon = commonBaseViewHolderMap.keySet();
    // 如果键集合不为空，则将所有通用自定义ViewHolder添加到工厂中
    if (!keysForCommon.isEmpty()) {
      // 此处原代码有误，应遍历 keysForCommon，已修正
      for (Integer type : keysForCommon) {
        Pair<Class<? extends CommonBaseMessageViewHolder>, Integer> value =
            commonBaseViewHolderMap.get(type);
        if (value == null) {
          continue;
        }
        factory.addCommonCustomViewHolder(type, value.first, value.second);
      }
    }
  }

  // 添加自定义消息视图Holder
  @Override
  public <T extends ChatBaseMessageViewHolder> void addCustomViewHolder(
      int type, Class<T> viewHolder) {
    // 如果工厂未配置，则将自定义ViewHolder添加到本地映射中
    if (factory == null) {
      viewHolderMap.put(type, viewHolder);
      return;
    }
    // 如果工厂已配置，则通过工厂添加自定义ViewHolder
    factory.addCustomViewHolder(type, viewHolder);
  }

  // 移除自定义消息视图Holder
  @Override
  public void removeCustomViewHolder(int type) {
    // 如果工厂未配置，则从本地映射中移除自定义ViewHolder
    if (factory == null) {
      viewHolderMap.remove(type);
      return;
    }
    // 如果工厂已配置，则通过工厂移除自定义ViewHolder
    factory.removeCustomViewHolder(type);
  }

  // 添加通用自定义消息视图Holder
  @Override
  public <T extends CommonBaseMessageViewHolder> void addCommonCustomViewHolder(
      int type, Class<T> viewHolderClass, int layoutRes) {
    // 如果工厂未配置，则将通用自定义ViewHolder添加到本地映射中
    if (factory == null) {
      commonBaseViewHolderMap.put(type, new Pair<>(viewHolderClass, layoutRes));
      return;
    }
    // 如果工厂已配置，则通过工厂添加通用自定义ViewHolder
    factory.addCommonCustomViewHolder(type, viewHolderClass, layoutRes);
  }

  // 移除通用自定义消息视图Holder
  @Override
  public void removeCommonCustomViewHolder(int type) {
    // 如果工厂未配置，则从本地映射中移除通用自定义ViewHolder
    if (factory == null) {
      commonBaseViewHolderMap.remove(type);
      return;
    }
    // 如果工厂已配置，则通过工厂移除通用自定义ViewHolder
    factory.removeCommonCustomViewHolder(type);
  }

  // 获取自定义消息视图类型
  @Override
  public int getCustomViewType(ChatMessageBean messageBean) {
    // 如果工厂未配置，则返回 -1 表示无自定义视图类型
    if (factory == null) {
      return -1;
    }
    // 如果工厂已配置，则通过工厂获取自定义视图类型
    return factory.getCustomViewType(messageBean);
  }

  // 创建自定义消息视图Holder
  @Nullable
  @Override
  public CommonBaseMessageViewHolder createViewHolderCustom(
      @NonNull ViewGroup parent, int viewType) {
    // 如果工厂未配置，则返回 null
    if (factory == null) {
      return null;
    }
    // 如果工厂已配置，则通过工厂创建自定义消息视图Holder
    return factory.createViewHolderCustom(parent, viewType);
  }

  // 获取消息项的视图类型
  @Override
  public int getItemViewType(ChatMessageBean messageBean) {
    // 如果工厂未配置，则返回 -1 表示无有效视图类型
    if (factory == null) {
      return -1;
    }
    // 如果工厂已配置，则通过工厂获取消息项的视图类型
    return factory.getItemViewType(messageBean);
  }

  // 创建消息视图Holder
  @Override
  public CommonBaseMessageViewHolder createViewHolder(@NonNull ViewGroup parent, int viewType) {
    // 如果工厂未配置，则返回 null
    if (factory == null) {
      return null;
    }
    // 如果工厂已配置，则通过工厂创建消息视图Holder
    return factory.createViewHolder(parent, viewType);
  }
}
