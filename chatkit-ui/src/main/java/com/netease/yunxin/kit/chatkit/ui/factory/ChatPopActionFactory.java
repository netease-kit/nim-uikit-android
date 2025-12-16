// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.factory;

import static com.netease.yunxin.kit.corekit.plugin.PluginConstantsKt.CHAT_POP_MENU_ACTION;

import android.content.Context;
import android.text.TextUtils;
import com.netease.nimlib.coexist.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.coexist.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.coexist.sdk.v2.message.enums.V2NIMMessageSendingState;
import com.netease.nimlib.coexist.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.ChatMessageType;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.custom.RichTextAttachment;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.IChatPopMenu;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.IChatPopMenuClickListener;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.model.PluginAction;
import com.netease.yunxin.kit.corekit.plugin.PluginService;
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
   * 获取文本长按弹窗中的内容
   *
   * @param text 长按的文本
   * @return 弹窗中的内容列表
   */
  public List<PluginAction> getTextActions(Context context, String text) {
    List<PluginAction> actions = new ArrayList<>();
    if (customPopMenu == null
        || customPopMenu.get() == null
        || customPopMenu.get().showDefaultPopMenu()) {
      actions.add(getCopyAction(context, text));
      actions.addAll(PluginService.getMenuActions(CHAT_POP_MENU_ACTION, text));
    }
    if (customPopMenu != null && customPopMenu.get() != null) {
      return customPopMenu.get().customizePopMenu(actions, null);
    }
    return actions;
  }

  /**
   * 获取长按弹窗中的内容
   *
   * @param message 长按的消息
   * @return 弹窗中的内容
   */
  public List<PluginAction> getMessageActions(Context context, ChatMessageBean message) {
    List<PluginAction> actions = new ArrayList<>();
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
        addCopyActionIfNeed(context, actions, message);
        actions.add(getDeleteAction(context, message));
        actions.add(getMultiSelectAction(context, message));
        addPluginTextActionIfNeed(actions, message);
        return actions;
      }
      if (message.getMessageData().getMessage().getMessageType()
          == V2NIMMessageType.V2NIM_MESSAGE_TYPE_AUDIO) {
        actions.add(getVoicePlayAction(context, message));
      }
      if (message.getViewType() == MsgTypeEnum.nrtc_netcall.getValue()) {
        // call
        actions.add(getDeleteAction(context, message));
        actions.add(getMultiSelectAction(context, message));
        return actions;
      }
      // 基础消息类型都在MsgTypeEnum中定义,自定义消息类型都是MsgTypeEnum.custom，
      // 自定义消息，根据自定义消息的Type区分IMUIKIt内置从101开始，客户定义从1000开始
      addCopyActionIfNeed(context, actions, message);
      actions.add(getReplyAction(context, message));
      if (message.getViewType() != MsgTypeEnum.audio.getValue()) {
        actions.add(getTransmitAction(context, message));
      }
      if (IMKitConfigCenter.getEnablePinMessage()) {
        actions.add(getPinAction(context, message));
      }
      actions.add(getDeleteAction(context, message));
      if (!MessageHelper.isReceivedMessage(message)) {
        actions.add(getRecallAction(context, message));
      }
      actions.add(getMultiSelectAction(context, message));
      if (IMKitConfigCenter.getEnableCollectionMessage()) {
        actions.add(getCollectionAction(context, message));
      }
      if (IMKitConfigCenter.getEnableTopMessage()
          && message.getMessageData().getMessage().getConversationType()
              == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM) {
        actions.add(getTopStickyAction(context, message));
      }
      if (message.getMessageData().getMessage().getMessageType()
              == V2NIMMessageType.V2NIM_MESSAGE_TYPE_AUDIO
          && IMKitConfigCenter.getEnableVoiceToText()
          && TextUtils.isEmpty(message.getVoiceToText())) {
        actions.add(getVoiceToTextAction(context, message));
      }
      addPluginTextActionIfNeed(actions, message);
    }
    if (customPopMenu != null && customPopMenu.get() != null) {
      return customPopMenu.get().customizePopMenu(actions, message);
    }
    return actions;
  }

  /**
   * 添加复制操作
   *
   * @param actions 弹窗操作列表
   * @param message 消息
   */
  private void addCopyActionIfNeed(
      Context context, List<PluginAction> actions, ChatMessageBean message) {
    if (message.getViewType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT.getValue()
        && !TextUtils.isEmpty(message.getMessageData().getMessage().getText())) {
      actions.add(getCopyAction(context, message.getMessageData().getMessage().getText()));
    }
    if (message.getViewType() == ChatMessageType.RICH_TEXT_ATTACHMENT) {
      RichTextAttachment attachment = (RichTextAttachment) message.getMessageData().getAttachment();
      if (attachment != null && !TextUtils.isEmpty(attachment.body)) {
        actions.add(getCopyAction(context, attachment.body));
      }
    }
  }

  /**
   * 添加插件文本操作
   *
   * @param actions 弹窗操作列表
   * @param message 消息
   */
  private void addPluginTextActionIfNeed(List<PluginAction> actions, ChatMessageBean message) {
    if (message.getViewType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT.getValue()
        && !TextUtils.isEmpty(message.getMessageData().getMessage().getText())) {
      actions.addAll(
          PluginService.getMenuActions(
              CHAT_POP_MENU_ACTION, message.getMessageData().getMessage().getText()));
    } else if (message.getViewType() == ChatMessageType.RICH_TEXT_ATTACHMENT) {
      RichTextAttachment attachment = (RichTextAttachment) message.getMessageData().getAttachment();
      if (attachment != null && !TextUtils.isEmpty(attachment.body)) {
        actions.addAll(PluginService.getMenuActions(CHAT_POP_MENU_ACTION, attachment.body));
      }
    }
  }

  // 构建回复按钮
  private PluginAction<ChatMessageBean> getReplyAction(Context context, ChatMessageBean message) {
    return new PluginAction<>(
        ActionConstants.POP_ACTION_REPLY,
        context.getString(R.string.chat_message_action_reply),
        R.drawable.ic_message_reply,
        (view, messageInfo) -> {
          if (actionListener != null) {
            actionListener.get().onReply(messageInfo);
          }
        },
        message);
  }
  // 构建复制按钮
  private PluginAction<String> getCopyAction(Context context, String text) {
    return new PluginAction<>(
        ActionConstants.POP_ACTION_COPY,
        context.getString(R.string.chat_message_action_copy),
        R.drawable.ic_message_copy,
        (view, messageInfo) -> {
          if (actionListener != null) {
            actionListener.get().onCopy(messageInfo);
          }
        },
        text);
  }
  // 构建撤回按钮
  private PluginAction<ChatMessageBean> getRecallAction(Context context, ChatMessageBean message) {
    return new PluginAction<>(
        ActionConstants.POP_ACTION_RECALL,
        context.getString(R.string.chat_message_action_recall),
        R.drawable.ic_message_recall,
        (view, messageInfo) -> {
          if (!NetworkUtils.isConnected()) {
            ToastX.showShortToast(R.string.chat_network_error_tip);
            return;
          }
          if (actionListener != null) {
            actionListener.get().onRecall(messageInfo);
          }
        },
        message);
  }
  // 构建语音转文字按钮
  private PluginAction<ChatMessageBean> getVoiceToTextAction(
      Context context, ChatMessageBean message) {
    return new PluginAction<>(
        ActionConstants.POP_ACTION_VOICE_TO_TEXT,
        context.getString(R.string.chat_voice_to_text),
        R.drawable.ic_voice_to_text,
        (view, messageInfo) -> {
          if (!NetworkUtils.isConnected()) {
            ToastX.showShortToast(R.string.chat_network_error_tip);
            return;
          }
          if (actionListener != null) {
            actionListener.get().onTransferToText(messageInfo);
          }
        },
        message);
  }
  // 构建标记按钮
  private PluginAction<ChatMessageBean> getPinAction(Context context, ChatMessageBean message) {
    return new PluginAction<>(
        ActionConstants.POP_ACTION_PIN,
        !TextUtils.isEmpty(message.getPinAccid())
            ? context.getString(R.string.chat_message_action_pin_cancel)
            : context.getString(R.string.chat_message_action_pin),
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
        },
        message);
  }
  // 构建多选按钮
  private PluginAction<ChatMessageBean> getMultiSelectAction(
      Context context, ChatMessageBean message) {
    return new PluginAction<>(
        ActionConstants.POP_ACTION_MULTI_SELECT,
        context.getString(R.string.chat_message_action_multi_select),
        R.drawable.ic_message_multi_select,
        (view, messageInfo) -> {
          if (actionListener != null) {
            actionListener.get().onMultiSelected(messageInfo);
          }
        },
        message);
  }

  // 构建置顶按钮
  private PluginAction<ChatMessageBean> getTopStickyAction(
      Context context, ChatMessageBean message) {
    boolean isAdd;
    if (ChatUserCache.getInstance().getTopMessage() != null) {
      isAdd = !ChatUserCache.getInstance().getTopMessage().equals(message.getMessageData());
    } else {
      isAdd = true;
    }
    return new PluginAction<>(
        ActionConstants.POP_ACTION_TOP_STICK,
        isAdd
            ? context.getString(R.string.chat_message_action_top)
            : context.getString(R.string.chat_message_action_cancel_top),
        isAdd ? R.drawable.ic_pop_top_sticky : R.drawable.ic_pop_untop_sticky,
        (view, messageInfo) -> {
          if (!NetworkUtils.isConnected()) {
            ToastX.showShortToast(R.string.chat_network_error_tip);
            return;
          }
          if (actionListener != null) {
            actionListener.get().onTopSticky(messageInfo, isAdd);
          }
        },
        message);
  }

  // 构建收藏按钮
  private PluginAction<ChatMessageBean> getCollectionAction(
      Context context, ChatMessageBean message) {
    return new PluginAction<>(
        ActionConstants.POP_ACTION_COLLECTION,
        context.getString(R.string.chat_message_action_collection),
        R.drawable.ic_message_collection,
        (view, messageInfo) -> {
          if (!NetworkUtils.isConnected()) {
            ToastX.showShortToast(R.string.chat_network_error_tip);
            return;
          }
          if (actionListener != null) {
            actionListener.get().onCollection(messageInfo);
          }
        },
        message);
  }

  // 构建删除按钮
  private PluginAction<ChatMessageBean> getDeleteAction(Context context, ChatMessageBean message) {
    return new PluginAction<>(
        ActionConstants.POP_ACTION_DELETE,
        context.getString(R.string.chat_message_action_delete),
        R.drawable.ic_message_delete,
        (view, messageInfo) -> {
          if (!NetworkUtils.isConnected()) {
            ToastX.showShortToast(R.string.chat_network_error_tip);
            return;
          }
          if (actionListener != null) {
            actionListener.get().onDelete(message);
          }
        },
        message);
  }

  // 构建转发按钮
  private PluginAction<ChatMessageBean> getTransmitAction(
      Context context, ChatMessageBean message) {
    return new PluginAction<>(
        ActionConstants.POP_ACTION_TRANSMIT,
        context.getString(R.string.chat_message_action_transmit),
        R.drawable.ic_message_transmit,
        (view, messageInfo) -> {
          if (!NetworkUtils.isConnected()) {
            ToastX.showShortToast(R.string.chat_network_error_tip);
            return;
          }
          if (actionListener != null) {
            actionListener.get().onForward(messageInfo);
          }
        },
        message);
  }

  // 构建语音播放按钮
  private PluginAction<ChatMessageBean> getVoicePlayAction(
      Context context, ChatMessageBean message) {
    return new PluginAction<>(
        ActionConstants.POP_ACTION_VOICE_PLAY,
        ChatKitClient.isEarphoneMode()
            ? context.getString(R.string.chat_message_action_voice_play_off)
            : context.getString(R.string.chat_message_action_voice_play_on),
        ChatKitClient.isEarphoneMode()
            ? R.drawable.ic_chat_audio_speaker
            : R.drawable.ic_chat_audio_earphone,
        (view, messageInfo) -> {
          if (!NetworkUtils.isConnected()) {
            ToastX.showShortToast(R.string.chat_network_error_tip);
            return;
          }
          if (actionListener != null) {
            actionListener.get().onVoicePlayChange(messageInfo);
          }
        },
        message);
  }
}
