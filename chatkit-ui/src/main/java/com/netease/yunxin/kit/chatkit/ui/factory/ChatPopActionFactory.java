// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.factory;

import android.text.TextUtils;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageSendingState;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.ui.ChatMessageType;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.ChatPopMenuAction;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.IChatPopMenu;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.IChatPopMenuClickListener;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/** 聊天界面长按弹窗工厂类，根据长按的消息返回对应的弹窗中的内容 */
public class ChatPopActionFactory {

  private static volatile ChatPopActionFactory instance;

  private WeakReference<IChatPopMenuClickListener> actionListener;

  private WeakReference<IChatPopMenu> customPopMenu;

  private ChatPopActionFactory() {}

  public static ChatPopActionFactory getInstance() {
    if (instance == null) {
      synchronized (ChatPopActionFactory.class) {
        if (instance == null) {
          instance = new ChatPopActionFactory();
        }
      }
    }
    return instance;
  }

  public void setActionListener(IChatPopMenuClickListener actionListener) {
    this.actionListener = new WeakReference<>(actionListener);
  }

  public void setChatPopMenu(IChatPopMenu popMenu) {
    this.customPopMenu = new WeakReference<>(popMenu);
  }

  /**
   * 获取长按弹窗中的内容
   *
   * @param message
   * @return
   */
  public List<ChatPopMenuAction> getMessageActions(ChatMessageBean message) {
    List<ChatPopMenuAction> actions = new ArrayList<>();
    if (message.getMessageData() == null) {
      return actions;
    }
    if (customPopMenu == null
        || customPopMenu.get() == null
        || customPopMenu.get().showDefaultPopMenu()) {
      if (message.getMessageData().getMessage().getSendingState()
              == V2NIMMessageSendingState.V2NIM_MESSAGE_SENDING_STATE_FAILED
          || message.getMessageData().getMessage().getSendingState()
              == V2NIMMessageSendingState.V2NIM_MESSAGE_SENDING_STATE_SENDING
          || message.getViewType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_INVALID.getValue()) {
        if (message.getViewType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT.getValue()) {
          actions.add(getCopyAction(message));
        }
        actions.add(getDeleteAction(message));
        actions.add(getMultiSelectAction(message));
        return actions;
      }

      if (message.getViewType() == MsgTypeEnum.nrtc_netcall.getValue()) {
        // call
        actions.add(getDeleteAction(message));
        actions.add(getMultiSelectAction(message));
        return actions;
      }
      // 基础消息类型都在MsgTypeEnum中定义,自定义消息类型都是MsgTypeEnum.custom，
      // 自定义消息，根据自定义消息的Type区分IMUIKIt内置从101开始，客户定义从1000开始
      if (message.getViewType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT.getValue()
          || message.getViewType() == ChatMessageType.RICH_TEXT_ATTACHMENT) {
        actions.add(getCopyAction(message));
      }
      actions.add(getReplyAction(message));
      if (message.getViewType() != MsgTypeEnum.audio.getValue()) {
        actions.add(getTransmitAction(message));
      }
      actions.add(getPinAction(message));
      actions.add(getDeleteAction(message));
      actions.add(getMultiSelectAction(message));
      if (message.getMessageData().getMessage().isSelf()) {
        actions.add(getRecallAction(message));
      }
      //      actions.add(getCollectionAction(message));
      if (IMKitConfigCenter.getTopMessageEnable()) {
        actions.add(getTopStickyAction(message));
      }
    }
    if (customPopMenu != null && customPopMenu.get() != null) {
      return customPopMenu.get().customizePopMenu(actions, message);
    }
    return actions;
  }
  // 构建回复按钮
  private ChatPopMenuAction getReplyAction(ChatMessageBean message) {
    return new ChatPopMenuAction(
        ActionConstants.POP_ACTION_REPLY,
        R.string.chat_message_action_reply,
        R.drawable.ic_message_reply,
        (view, messageInfo) -> {
          if (actionListener != null) {
            actionListener.get().onReply(messageInfo);
          }
        });
  }
  // 构建复制按钮
  private ChatPopMenuAction getCopyAction(ChatMessageBean message) {
    return new ChatPopMenuAction(
        ActionConstants.POP_ACTION_COPY,
        R.string.chat_message_action_copy,
        R.drawable.ic_message_copy,
        (view, messageInfo) -> {
          if (actionListener != null) {
            actionListener.get().onCopy(messageInfo);
          }
        });
  }
  // 构建撤回按钮
  private ChatPopMenuAction getRecallAction(ChatMessageBean message) {
    return new ChatPopMenuAction(
        ActionConstants.POP_ACTION_RECALL,
        R.string.chat_message_action_recall,
        R.drawable.ic_message_recall,
        (view, messageInfo) -> {
          if (!NetworkUtils.isConnected()) {
            ToastX.showShortToast(R.string.chat_network_error_tip);
            return;
          }
          if (actionListener != null) {
            actionListener.get().onRecall(messageInfo);
          }
        });
  }
  // 构建标记按钮
  private ChatPopMenuAction getPinAction(ChatMessageBean message) {
    return new ChatPopMenuAction(
        ActionConstants.POP_ACTION_PIN,
        !TextUtils.isEmpty(message.getPinAccid())
            ? R.string.chat_message_action_pin_cancel
            : R.string.chat_message_action_pin,
        R.drawable.ic_message_sign,
        (view, messageInfo) -> {
          if (!NetworkUtils.isConnected()) {
            ToastX.showShortToast(R.string.chat_network_error_tip);
            return;
          }
          if (actionListener != null) {
            actionListener
                .get()
                .onSignal(messageInfo, !TextUtils.isEmpty(messageInfo.getPinAccid()));
          }
        });
  }
  // 构建多选按钮
  private ChatPopMenuAction getMultiSelectAction(ChatMessageBean message) {
    return new ChatPopMenuAction(
        ActionConstants.POP_ACTION_MULTI_SELECT,
        R.string.chat_message_action_multi_select,
        R.drawable.ic_message_multi_select,
        (view, messageInfo) -> {
          if (actionListener != null) {
            actionListener.get().onMultiSelected(messageInfo);
          }
        });
  }

  // 构建置顶按钮
  private ChatPopMenuAction getTopStickyAction(ChatMessageBean message) {
    boolean isAdd;
    if (ChatUserCache.getInstance().getTopMessage() != null) {
      isAdd = !ChatUserCache.getInstance().getTopMessage().equals(message.getMessageData());
    } else {
      isAdd = true;
    }
    return new ChatPopMenuAction(
        ActionConstants.POP_ACTION_TOP_STICK,
        isAdd ? R.string.chat_message_action_top : R.string.chat_message_action_cancel_top,
        R.drawable.ic_pop_top_sticky,
        (view, messageInfo) -> {
          if (!NetworkUtils.isConnected()) {
            ToastX.showShortToast(R.string.chat_network_error_tip);
            return;
          }
          if (actionListener != null) {
            actionListener.get().onTopSticky(messageInfo, isAdd);
          }
        });
  }

  // 构建收藏按钮
  private ChatPopMenuAction getCollectionAction(ChatMessageBean message) {
    return new ChatPopMenuAction(
        ActionConstants.POP_ACTION_COLLECTION,
        R.string.chat_message_action_collection,
        R.drawable.ic_message_collection,
        (view, messageInfo) -> {
          if (actionListener != null) {
            actionListener.get().onCollection(messageInfo);
          }
        });
  }

  // 构建删除按钮
  private ChatPopMenuAction getDeleteAction(ChatMessageBean message) {
    return new ChatPopMenuAction(
        ActionConstants.POP_ACTION_DELETE,
        R.string.chat_message_action_delete,
        R.drawable.ic_message_delete,
        (view, messageInfo) -> {
          if (!NetworkUtils.isConnected()) {
            ToastX.showShortToast(R.string.chat_network_error_tip);
            return;
          }
          if (actionListener != null) {
            actionListener.get().onDelete(message);
          }
        });
  }

  // 构建转发按钮
  private ChatPopMenuAction getTransmitAction(ChatMessageBean message) {
    return new ChatPopMenuAction(
        ActionConstants.POP_ACTION_TRANSMIT,
        R.string.chat_message_action_transmit,
        R.drawable.ic_message_transmit,
        (view, messageInfo) -> {
          if (!NetworkUtils.isConnected()) {
            ToastX.showShortToast(R.string.chat_network_error_tip);
            return;
          }
          if (actionListener != null) {
            actionListener.get().onForward(messageInfo);
          }
        });
  }
}
