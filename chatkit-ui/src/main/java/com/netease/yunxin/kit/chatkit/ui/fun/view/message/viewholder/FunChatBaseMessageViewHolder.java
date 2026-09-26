// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder;

import android.content.Context;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageRefer;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatMsgCache;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageReplayViewBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageRevokedViewBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatBaseMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options.ChatMessageViewHolderUIOptions;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options.CommonUIOption;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options.MessageStatusUIOption;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options.ReplayUIOption;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options.RevokeUIOption;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options.SignalUIOption;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

public class FunChatBaseMessageViewHolder extends ChatBaseMessageViewHolder {
  private static final String TAG = "FunChatBaseMessageViewHolder";

  protected FunChatMessageReplayViewBinding replayBinding;
  protected FunChatMessageRevokedViewBinding revokedViewBinding;

  public FunChatBaseMessageViewHolder(
      @NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  protected int getReactionHighlightColor(Context context) {
    return ContextCompat.getColor(context, R.color.color_007aff);
  }

  @Override
  protected int getMessageReactionBackgroundColor(Context context, boolean isSelfMessage) {
    if (isSelfMessage && properties.getSelfMessageReactionBgColor() == null) {
      Integer receiveReactionBgColor = properties.getReceiveMessageReactionBgColor();
      return receiveReactionBgColor != null
          ? receiveReactionBgColor
          : ContextCompat.getColor(context, R.color.fun_chat_page_bg_color);
    }
    if (!isSelfMessage && properties.getReceiveMessageReactionBgColor() == null) {
      return ContextCompat.getColor(context, R.color.fun_chat_page_bg_color);
    }
    return super.getMessageReactionBackgroundColor(context, isSelfMessage);
  }

  @Override
  protected void onLayoutConfig(ChatMessageBean messageBean) {
    super.onLayoutConfig(messageBean);
    // Fun 发送消息保持整体靠右，消息主体和 Reaction 在整体背景内从左侧对齐。
    if (!showReceiveUIStyle() && uiOptions.commonUIOption.messageContentLayoutGravity == null) {
      ConstraintLayout.LayoutParams messageContainerLayoutParams =
          (ConstraintLayout.LayoutParams) baseViewBinding.messageContainer.getLayoutParams();
      messageContainerLayoutParams.horizontalBias = CommonUIOption.MessageContentLayoutGravity.left;
      baseViewBinding.messageContainer.setLayoutParams(messageContainerLayoutParams);

      ConstraintLayout.LayoutParams messageBottomLayoutParams =
          (ConstraintLayout.LayoutParams) baseViewBinding.messageBottomGroup.getLayoutParams();
      messageBottomLayoutParams.horizontalBias = CommonUIOption.MessageContentLayoutGravity.left;
      baseViewBinding.messageBottomGroup.setLayoutParams(messageBottomLayoutParams);
    }
    if (messageBean.isRevoked()) {
      // 撤回消息控制居中
      ConstraintLayout.LayoutParams messageContentLayoutParams =
          (ConstraintLayout.LayoutParams) baseViewBinding.messageContentGroup.getLayoutParams();
      messageContentLayoutParams.horizontalBias = CommonUIOption.MessageContentLayoutGravity.center;
      baseViewBinding.messageContentGroup.setLayoutParams(messageContentLayoutParams);
    }
    // 设置标记提示 margin
    ConstraintLayout.LayoutParams signalLayoutParams =
        (ConstraintLayout.LayoutParams) baseViewBinding.llSignal.getLayoutParams();
    int paddingSize = SizeUtils.dp2px(12);
    signalLayoutParams.rightMargin = paddingSize;
    signalLayoutParams.leftMargin = paddingSize;
    baseViewBinding.llSignal.setLayoutParams(signalLayoutParams);
  }

  @Override
  protected void setUserInfo(ChatMessageBean message) {
    // 修改用户头像
    int avatarSize = SizeUtils.dp2px(42);
    int cornerRadius = SizeUtils.dp2px(4);
    ViewGroup.LayoutParams myAvatarLayoutParams = baseViewBinding.myAvatar.getLayoutParams();
    myAvatarLayoutParams.width = avatarSize;
    myAvatarLayoutParams.height = avatarSize;
    baseViewBinding.myAvatar.setCornerRadius(cornerRadius);

    ViewGroup.LayoutParams otherUserAvatarLayoutParams =
        baseViewBinding.otherUserAvatar.getLayoutParams();
    otherUserAvatarLayoutParams.width = avatarSize;
    otherUserAvatarLayoutParams.height = avatarSize;
    baseViewBinding.otherUserAvatar.setCornerRadius(cornerRadius);
    if (message.isRevoked()) {
      if (MessageHelper.isReceivedMessage(message) && revokedViewBinding != null) {
        Context context = revokedViewBinding.messageText.getContext();
        revokedViewBinding.messageText.setText(
            context.getString(
                R.string.fun_chat_message_revoked,
                MessageHelper.getChatMessageUserName(message.getMessageData().getMessage())));
      }
      return;
    }
    super.setUserInfo(message);
  }

  @Override
  protected void setSelectStatus(ChatMessageBean message) {
    // 当前账户发送消息的消息体右移
    ConstraintLayout.LayoutParams avatarLayoutParams =
        (ConstraintLayout.LayoutParams) baseViewBinding.myAvatar.getLayoutParams();
    // 接受消息内容右移
    ConstraintLayout.LayoutParams containerLayoutParams =
        (ConstraintLayout.LayoutParams) baseViewBinding.messageContentGroup.getLayoutParams();

    if (isMultiSelect && needShowMultiSelect() && !currentMessage.isRevoked()) {
      baseViewBinding.chatMsgSelectLayout.setVisibility(View.VISIBLE);
      baseViewBinding.chatSelectorCb.setChecked(
          ChatMsgCache.contains(message.getMessageData().getMessage().getMessageClientId()));
      avatarLayoutParams.setMarginEnd(SizeUtils.dp2px(mineAvatarMarginEndInMulti));
      containerLayoutParams.goneEndMargin = SizeUtils.dp2px(containerMarginEndInMulti);
    } else {
      baseViewBinding.chatMsgSelectLayout.setVisibility(View.GONE);
      avatarLayoutParams.setMarginEnd(SizeUtils.dp2px(mineAvatarMarginEnd));
      containerLayoutParams.goneEndMargin = SizeUtils.dp2px(containerMarginEnd);
    }
  }

  @Override
  protected void onCommonViewVisibleConfig(ChatMessageBean messageBean) {
    super.onCommonViewVisibleConfig(messageBean);
    if (messageBean.isRevoked()) {
      // 消息撤回，头像、名称不展示
      baseViewBinding.myAvatar.setVisibility(View.GONE);
      baseViewBinding.myName.setVisibility(View.GONE);
      baseViewBinding.otherUserAvatar.setVisibility(View.GONE);
      baseViewBinding.otherUsername.setVisibility(View.GONE);
    }
    baseViewBinding.chatSelectorCb.setBackgroundResource(R.drawable.fun_chat_radio_button_selector);
  }

  @Override
  protected ChatMessageViewHolderUIOptions provideUIOptions(ChatMessageBean messageBean) {
    SignalUIOption signalUIOption = new SignalUIOption();
    signalUIOption.signalBgRes = R.color.fun_chat_message_pin_bg_color;
    return ChatMessageViewHolderUIOptions.wrapExitsOptions(super.provideUIOptions(messageBean))
        .signalUIOption(signalUIOption)
        .build();
  }

  @Override
  protected void onMessageBackgroundConfig(ChatMessageBean messageBean) {
    super.onMessageBackgroundConfig(messageBean);
    if (messageBean.isRevoked()) {
      baseViewBinding.messageContentGroup.setBackgroundResource(R.color.title_transfer);
      return;
    }
    boolean isReceivedMsg = MessageHelper.isReceivedMessage(messageBean) || !isChatMsg();
    CommonUIOption commonUIOption = uiOptions.commonUIOption;
    boolean isCustomBgValid = true;
    baseViewBinding.messageContentGroup.setBackground(null);
    if (isReceivedMsg) {
      if (commonUIOption.otherUserMessageBg != null) {
        baseViewBinding.messageContentGroup.setBackground(commonUIOption.otherUserMessageBg);
      } else if (commonUIOption.otherUserMessageBgRes != null) {
        baseViewBinding.messageContentGroup.setBackgroundResource(
            commonUIOption.otherUserMessageBgRes);
      } else if (properties.getReceiveMessageBg() != null) {
        baseViewBinding.messageContentGroup.setBackground(properties.getReceiveMessageBg());
      } else if (properties.receiveMessageBgRes != null) {
        baseViewBinding.messageContentGroup.setBackgroundResource(properties.receiveMessageBgRes);
      } else {
        isCustomBgValid = false;
      }
    } else {
      if (commonUIOption.myMessageBg != null) {
        baseViewBinding.messageContentGroup.setBackground(commonUIOption.myMessageBg);
      } else if (commonUIOption.myMessageBgRes != null) {
        baseViewBinding.messageContentGroup.setBackgroundResource(commonUIOption.myMessageBgRes);
      } else if (properties.getSelfMessageBg() != null) {
        baseViewBinding.messageContentGroup.setBackground(properties.getSelfMessageBg());
      } else if (properties.selfMessageBgRes != null) {
        baseViewBinding.messageContentGroup.setBackgroundResource(properties.selfMessageBgRes);
      } else {
        isCustomBgValid = false;
      }
    }
    if (isCustomBgValid) {
      return;
    }
    if (baseViewBinding.messageContainer.getChildCount() <= 0) {
      return;
    }
    if (isReceivedMsg) {
      baseViewBinding.messageContentGroup.setBackgroundResource(R.drawable.fun_bg_message_receive);
    } else {
      if (useForwardMessageBackground(messageBean)) {
        baseViewBinding.messageContentGroup.setBackgroundResource(
            R.drawable.fun_forward_message_send_bg);
      } else {
        baseViewBinding.messageContentGroup.setBackgroundResource(R.drawable.fun_bg_message_send);
      }
    }
  }

  /** Allows message types with the forward layout style to reuse its default send background. */
  protected boolean useForwardMessageBackground(ChatMessageBean messageBean) {
    return messageBean.getMessageData().getAttachment() instanceof MultiForwardAttachment;
  }

  @Override
  protected void onMessageRevoked(ChatMessageBean messageBean) {
    RevokeUIOption revokeUIOption = uiOptions.revokeUIOption;
    if (revokeUIOption.enable != null && !revokeUIOption.enable) {
      return;
    }
    if (!messageBean.isRevoked()) {
      baseViewBinding.messageContainer.setEnabled(true);
      return;
    }
    baseViewBinding.messageContainer.setEnabled(false);
    baseViewBinding.messageNormalReplyContainer.removeAllViews();
    getMessageContentContainer().removeAllViews();
    baseViewBinding.messageReactionContainer.removeAllViews();
    baseViewBinding.messageFunReplyContainer.removeAllViews();
    addRevokeViewToMessageContainer();
    Context context = revokedViewBinding.messageText.getContext();
    if (MessageHelper.isReceivedMessage(messageBean)) {
      revokedViewBinding.messageText.setText(
          context.getString(
              R.string.fun_chat_message_revoked,
              MessageHelper.getChatMessageUserName(messageBean.getMessageData().getMessage())));
    } else {
      revokedViewBinding.messageText.setText(
          context.getString(
              R.string.fun_chat_message_revoked, context.getString(R.string.chat_you)));
    }
    // reedit
    revokedViewBinding.tvAction.setOnClickListener(
        v -> {
          if (itemClickListener != null && !isMultiSelect) {
            itemClickListener.onReeditRevokeMessage(v, position, messageBean);
          }
        });
    if (MessageHelper.revokeMsgIsEdit(messageBean)) {
      revokedViewBinding.tvAction.setVisibility(View.VISIBLE);
    } else {
      revokedViewBinding.tvAction.setVisibility(View.GONE);
    }
    if (revokeUIOption.revokedTipText != null) {
      revokedViewBinding.messageText.setText(revokeUIOption.revokedTipText);
    }
    if (revokeUIOption.actionBtnText != null) {
      revokedViewBinding.tvAction.setText(revokeUIOption.actionBtnText);
    }
    if (revokeUIOption.actionBtnVisible != null) {
      revokedViewBinding.tvAction.setVisibility(
          revokeUIOption.actionBtnVisible ? View.VISIBLE : View.GONE);
    }
  }

  @Override
  protected void setStatus(ChatMessageBean data) {
    baseViewBinding.readProcess.setColor(
        ContextCompat.getColor(parent.getContext(), R.color.fun_chat_message_read_process));
    super.setStatus(data);
    MessageStatusUIOption messageStatusUIOption = uiOptions.messageStatusUIOption;
    if (messageStatusUIOption.readProcessClickListener != null) {
      baseViewBinding.readProcess.setOnClickListener(
          v -> {
            if (!isMultiSelect) {
              messageStatusUIOption.readProcessClickListener.onClick(v);
            }
          });
    } else {
      baseViewBinding.readProcess.setOnClickListener(
          v -> {
            if (!isMultiSelect) {
              if (!NetworkUtils.isConnected()) {
                ToastX.showShortToast(R.string.chat_network_error_tip);
                return;
              }
              XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_READER_PAGE)
                  .withParam(RouterConstant.KEY_MESSAGE, data.getMessageData().getMessage())
                  .withContext(v.getContext())
                  .navigate();
            }
          });
    }
  }

  @Override
  protected void setReplyInfo(ChatMessageBean messageBean) {
    replyMessage = null;
    ReplayUIOption replayUIOption = uiOptions.replayUIOption;
    if (replayUIOption.enable != null && !replayUIOption.enable) {
      return;
    }
    if (messageBean == null || messageBean.getMessageData() == null) {
      return;
    }
    baseViewBinding.messageReactionContainer.removeAllViews();
    baseViewBinding.messageFunReplyContainer.removeAllViews();
    ALog.w(
        TAG,
        TAG,
        "setReplyInfo, uuid=" + messageBean.getMessageData().getMessage().getMessageClientId());
    if (MessageHelper.isThreadReplayInfo(messageBean)) {
      // thread 回复
      setThreadReplyInfo(messageBean);
    } else if (messageBean.hasReply()) {
      // 自定义回复实现
      addReplayViewToReplyGroup();
      final FunChatMessageReplayViewBinding replyBinding = replayBinding;
      final String messageClientId = messageBean.getMsgClientId();
      V2NIMMessageRefer replyMsg = messageBean.getReplyMessageRefer();
      if (replyMsg != null) {
        MessageHelper.getReplyMessageInfo(
            replyMsg,
            messageBean.getReplyMessage(),
            new FetchCallback<IMMessageInfo>() {
              @Override
              public void onError(int errorCode, @Nullable String errorMsg) {
                if (isReplyBindingValid(messageClientId, replyBinding)) {
                  baseViewBinding.messageFunReplyContainer.removeAllViews();
                }
              }

              @Override
              public void onSuccess(@Nullable IMMessageInfo param) {
                if (!isReplyBindingValid(messageClientId, replyBinding)) {
                  return;
                }
                replyMessage = param;
                String content = MessageHelper.getReplyContent(replyMessage);
                MessageHelper.identifyFaceExpression(
                    replyBinding.tvReply.getContext(),
                    replyBinding.tvReply,
                    content,
                    ImageSpan.ALIGN_BOTTOM);
              }
            });
      }

      if (itemClickListener != null) {
        replyBinding.tvReply.setOnClickListener(
            v -> {
              if (!isMultiSelect) {
                itemClickListener.onReplyMessageClick(v, position, replyMessage);
              }
            });
      }
    } else {
      baseViewBinding.messageFunReplyContainer.removeAllViews();
    }
  }

  private boolean isReplyBindingValid(
      String messageClientId, FunChatMessageReplayViewBinding replyBinding) {
    return currentMessage != null
        && TextUtils.equals(currentMessage.getMsgClientId(), messageClientId)
        && replayBinding == replyBinding;
  }

  /// 内部设置 thread 回复消息
  private void setThreadReplyInfo(ChatMessageBean messageBean) {
    V2NIMMessageRefer threadOption = messageBean.getMessageData().getMessage().getThreadReply();
    String replyFrom = threadOption.getSenderId();
    if (TextUtils.isEmpty(replyFrom)) {
      ALog.w(
          TAG,
          "no reply message found, uuid="
              + messageBean.getMessageData().getMessage().getMessageClientId());
      baseViewBinding.messageFunReplyContainer.removeAllViews();
      return;
    }
    addReplayViewToReplyGroup();
    final FunChatMessageReplayViewBinding replyBinding = replayBinding;
    final String messageClientId = messageBean.getMsgClientId();
    MessageHelper.getReplyMessageInfo(
        threadOption,
        messageBean.getReplyMessage(),
        new FetchCallback<IMMessageInfo>() {

          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            if (isReplyBindingValid(messageClientId, replyBinding)) {
              replyBinding.tvReply.setVisibility(View.GONE);
            }
          }

          @Override
          public void onSuccess(@Nullable IMMessageInfo param) {
            if (!isReplyBindingValid(messageClientId, replyBinding)) {
              return;
            }

            replyMessage = param;
            String content = MessageHelper.getReplyContent(replyMessage);
            MessageHelper.identifyFaceExpression(
                replyBinding.tvReply.getContext(),
                replyBinding.tvReply,
                content,
                ImageSpan.ALIGN_BOTTOM);
          }
        });

    if (itemClickListener != null) {
      replyBinding.tvReply.setOnClickListener(
          v -> {
            if (!isMultiSelect) {
              itemClickListener.onReplyMessageClick(v, position, replyMessage);
            }
          });
    }
  }

  // 添加 fun 下的独立回复布局
  private void addReplayViewToReplyGroup() {
    replayBinding =
        FunChatMessageReplayViewBinding.inflate(
            LayoutInflater.from(parent.getContext()), getFunReplyContainer(), true);
  }

  private void addRevokeViewToMessageContainer() {
    revokedViewBinding =
        FunChatMessageRevokedViewBinding.inflate(
            LayoutInflater.from(parent.getContext()), getMessageContentContainer(), true);
  }
}
