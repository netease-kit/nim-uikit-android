// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.factory;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.IChatDefaultFactory;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.normal.view.message.ChatMessageViewHolderFactory;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatBaseMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.CommonBaseMessageViewHolder;
import com.netease.yunxin.kit.corekit.im.custom.CustomAttachment;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChatVieHolderFactory extends ChatMessageViewHolderFactory
    implements IChatDefaultFactory {
  private static final String TAG = "ChatDefaultFactory";

  private final Map<Integer, Class<? extends ChatBaseMessageViewHolder>> viewHolderMap =
      new HashMap<>();
  private final Map<Integer, Pair<Class<? extends CommonBaseMessageViewHolder>, Integer>>
      commonBaseViewHolderMap = new HashMap<>();

  private ChatVieHolderFactory() {}

  public static ChatVieHolderFactory getInstance() {
    return ChatDefaultFactoryHolder.instance;
  }

  public <T extends ChatBaseMessageViewHolder> void addCustomViewHolder(
      int type, Class<T> viewHolder) {
    viewHolderMap.put(type, viewHolder);
  }

  public void removeCustomViewHolder(int type) {
    viewHolderMap.remove(type);
  }

  public <T extends CommonBaseMessageViewHolder> void addCommonCustomViewHolder(
      int type, Class<T> viewHolderClass, @LayoutRes int layoutRes) {
    commonBaseViewHolderMap.put(type, new Pair<>(viewHolderClass, layoutRes));
  }

  public void removeCommonCustomViewHolder(int type) {
    commonBaseViewHolderMap.remove(type);
  }

  //获取自定义消息的消息类型，一般采用CustomAttachment中的Type区分，Type是由用户定义值（大于1000），不与当前重复即可
  @Override
  public int getCustomViewType(ChatMessageBean messageBean) {
    if (messageBean != null) {
      if (messageBean.getMessageData().getMessage().getMsgType() == MsgTypeEnum.custom) {
        CustomAttachment attachment =
            (CustomAttachment) messageBean.getMessageData().getMessage().getAttachment();
        if (attachment != null) {
          return attachment.getType();
        }
      }
    }
    return -1;
  }

  @Nullable
  @Override
  public CommonBaseMessageViewHolder createViewHolderCustom(
      @NonNull ViewGroup parent, int viewType) {
    CommonBaseMessageViewHolder viewHolder = null;
    if (viewHolderMap.containsKey(viewType) && viewHolderMap.get(viewType) != null) {
      ChatBaseMessageViewHolderBinding viewHolderBinding =
          ChatBaseMessageViewHolderBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false);
      try {
        viewHolder =
            Objects.requireNonNull(viewHolderMap.get(viewType))
                .getConstructor(ChatBaseMessageViewHolderBinding.class, int.class)
                .newInstance(viewHolderBinding, viewType);
      } catch (Exception exception) {
        ALog.e(TAG, "createViewHolderCustom-Chatbase", exception);
      }
    } else if (commonBaseViewHolderMap.containsKey(viewType)
        && commonBaseViewHolderMap.get(viewType) != null) {
      Pair<Class<? extends CommonBaseMessageViewHolder>, Integer> pair =
          commonBaseViewHolderMap.get(viewType);
      try {
        int layoutRes = Objects.requireNonNull(pair).second;
        View itemView = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        viewHolder = pair.first.getConstructor(View.class).newInstance(itemView);
      } catch (Exception exception) {
        ALog.e(TAG, "createViewHolderCustom-CommonBase", exception);
      }
    }
    return viewHolder;
  }

  private static class ChatDefaultFactoryHolder {
    private static final ChatVieHolderFactory instance = new ChatVieHolderFactory();
  }
}
