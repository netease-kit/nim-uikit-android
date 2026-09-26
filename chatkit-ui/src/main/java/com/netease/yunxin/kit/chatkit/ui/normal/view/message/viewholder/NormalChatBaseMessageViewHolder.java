// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.view.message.viewholder;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageRefer;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.NormalChatMessageReplayNormalViewBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.NormalChatMessageRevokedNormalViewBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatBaseMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options.CommonUIOption;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options.MessageStatusUIOption;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options.ReplayUIOption;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options.RevokeUIOption;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

public class NormalChatBaseMessageViewHolder extends ChatBaseMessageViewHolder {
  private static final String TAG = "NormalChatBaseMessageViewHolder";

  protected NormalChatMessageReplayNormalViewBinding replayBinding;
  // 撤销 ui 的控件集合
  protected NormalChatMessageRevokedNormalViewBinding revokedViewBinding;

  protected boolean hasLoadReply = false;

  public NormalChatBaseMessageViewHolder(
      @NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  protected void onMessageBackgroundConfig(ChatMessageBean messageBean) {
    super.onMessageBackgroundConfig(messageBean);
    baseViewBinding.messageContentGroup.setBackground(null);
    boolean isReceivedMsg = MessageHelper.isReceivedMessage(messageBean) || !isChatMsg();
    CommonUIOption commonUIOption = uiOptions.commonUIOption;
    boolean isCustomBgValid = true;
    if (isReceivedMsg) {
      if (commonUIOption.otherUserMessageBg != null) {
        setMessageBackground(commonUIOption.otherUserMessageBg);
      } else if (commonUIOption.otherUserMessageBgRes != null) {
        setMessageBackgroundResource(commonUIOption.otherUserMessageBgRes);
      } else if (properties.getReceiveMessageBg() != null) {
        setMessageBackground(properties.getReceiveMessageBg());
      } else if (properties.receiveMessageBgRes != null) {
        setMessageBackgroundResource(properties.receiveMessageBgRes);
      } else {
        isCustomBgValid = false;
      }
    } else {
      if (commonUIOption.myMessageBg != null) {
        setMessageBackground(commonUIOption.myMessageBg);
      } else if (commonUIOption.myMessageBgRes != null) {
        setMessageBackgroundResource(commonUIOption.myMessageBgRes);
      } else if (properties.getSelfMessageBg() != null) {
        setMessageBackground(properties.getSelfMessageBg());
      } else if (properties.selfMessageBgRes != null) {
        setMessageBackgroundResource(properties.selfMessageBgRes);
      } else {
        isCustomBgValid = false;
      }
    }
    if (isCustomBgValid) {
      return;
    }
    if (isReceivedMsg) {
      setMessageBackgroundResource(R.drawable.chat_message_other_bg);
    } else {
      setMessageBackgroundResource(R.drawable.chat_message_self_bg);
    }
  }

  private void setMessageBackground(Drawable background) {
    baseViewBinding.messageContentGroup.setBackground(background);
  }

  private void setMessageBackgroundResource(int backgroundRes) {
    baseViewBinding.messageContentGroup.setBackgroundResource(backgroundRes);
  }

  @Override
  protected void onLayoutConfig(ChatMessageBean messageBean) {
    super.onLayoutConfig(messageBean);
    if (!showReceiveUIStyle() && uiOptions.commonUIOption.messageContentLayoutGravity == null) {
      setMessageContentChildrenLeftAligned();
    }
    ConstraintLayout.LayoutParams messageContainerLayoutParams =
        (ConstraintLayout.LayoutParams) baseViewBinding.messageContentGroup.getLayoutParams();
    ConstraintLayout.LayoutParams signalLayoutParams =
        (ConstraintLayout.LayoutParams) baseViewBinding.llSignal.getLayoutParams();
    ConstraintLayout.LayoutParams statusLayoutParams =
        (ConstraintLayout.LayoutParams) baseViewBinding.messageStatus.getLayoutParams();
    int size = SizeUtils.dp2px(10);
    // 设置标记
    signalLayoutParams.rightMargin = size;
    signalLayoutParams.leftMargin = size;
    statusLayoutParams.rightMargin = size;
    // 设置消息体
    messageContainerLayoutParams.rightMargin = size;
    messageContainerLayoutParams.leftMargin = size;
  }

  private void setMessageContentChildrenLeftAligned() {
    ConstraintLayout.LayoutParams messageContainerParams =
        (ConstraintLayout.LayoutParams) baseViewBinding.messageContainer.getLayoutParams();
    messageContainerParams.horizontalBias = CommonUIOption.MessageContentLayoutGravity.left;
    baseViewBinding.messageContainer.setLayoutParams(messageContainerParams);

    ConstraintLayout.LayoutParams messageTopParams =
        (ConstraintLayout.LayoutParams) baseViewBinding.messageTopGroup.getLayoutParams();
    messageTopParams.horizontalBias = CommonUIOption.MessageContentLayoutGravity.left;
    baseViewBinding.messageTopGroup.setLayoutParams(messageTopParams);

    ConstraintLayout.LayoutParams messageBottomParams =
        (ConstraintLayout.LayoutParams) baseViewBinding.messageBottomGroup.getLayoutParams();
    messageBottomParams.horizontalBias = CommonUIOption.MessageContentLayoutGravity.left;
    baseViewBinding.messageBottomGroup.setLayoutParams(messageBottomParams);
  }

  @Override
  protected void onMessageRevoked(ChatMessageBean data) {
    RevokeUIOption revokeUIOption = uiOptions.revokeUIOption;
    if (revokeUIOption.enable != null && !revokeUIOption.enable) {
      return;
    }
    if (!data.isRevoked()) {
      baseViewBinding.messageContainer.setEnabled(true);
      return;
    }
    baseViewBinding.messageContainer.setEnabled(false);
    baseViewBinding.messageNormalReplyContainer.removeAllViews();
    getMessageContentContainer().removeAllViews();
    baseViewBinding.messageReactionContainer.removeAllViews();
    //reedit
    addRevokeViewToMessageContainer();
    revokedViewBinding.tvAction.setOnClickListener(
        v -> {
          if (itemClickListener != null && !isMultiSelect) {
            itemClickListener.onReeditRevokeMessage(v, position, data);
          }
        });
    if (MessageHelper.revokeMsgIsEdit(data)) {
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
  protected void setReplyInfo(ChatMessageBean messageBean) {
    replyMessage = null;
    hasLoadReply = true;
    ReplayUIOption replayUIOption = uiOptions.replayUIOption;
    if (replayUIOption.enable != null && !replayUIOption.enable) {
      return;
    }
    if (messageBean == null || messageBean.getMessageData() == null) {
      return;
    }
    baseViewBinding.messageNormalReplyContainer.removeAllViews();
    ALog.w(
        TAG,
        TAG,
        "setReplyInfo, uuid=" + messageBean.getMessageData().getMessage().getMessageClientId());
    if (MessageHelper.isThreadReplayInfo(messageBean)) {
      //thread 回复
      setThreadReplyInfo(messageBean);
    } else if (messageBean.hasReply()) {
      //自定义回复实现
      addReplayViewToTopGroup();
      final NormalChatMessageReplayNormalViewBinding replyBinding = replayBinding;
      final String messageClientId = messageBean.getMsgClientId();
      V2NIMMessageRefer refer = messageBean.getReplyMessageRefer();
      if (refer != null) {
        MessageHelper.getReplyMessageInfo(
            refer,
            messageBean.getReplyMessage(),
            new FetchCallback<IMMessageInfo>() {
              @Override
              public void onError(int errorCode, @Nullable String errorMsg) {
                if (isReplyBindingValid(messageClientId, replyBinding)) {
                  baseViewBinding.messageNormalReplyContainer.removeAllViews();
                }
              }

              @Override
              public void onSuccess(@Nullable IMMessageInfo param) {
                if (!isReplyBindingValid(messageClientId, replyBinding)) {
                  return;
                }
                replyMessage = param;
                String content = "| " + MessageHelper.getReplyContent(replyMessage);
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
    }
  }

  private boolean isReplyBindingValid(
      String messageClientId, NormalChatMessageReplayNormalViewBinding replyBinding) {
    return currentMessage != null
        && TextUtils.equals(currentMessage.getMsgClientId(), messageClientId)
        && replayBinding == replyBinding;
  }

  @Override
  protected void setStatus(ChatMessageBean data) {
    super.setStatus(data);
    MessageStatusUIOption messageStatusUIOption = uiOptions.messageStatusUIOption;
    if (messageStatusUIOption.readProcessClickListener != null) {
      baseViewBinding.readProcess.setOnClickListener(
          messageStatusUIOption.readProcessClickListener);
    } else {
      baseViewBinding.readProcess.setOnClickListener(
          v -> {
            if (!NetworkUtils.isConnected()) {
              ToastX.showShortToast(R.string.chat_network_error_tip);
              return;
            }
            XKitRouter.withKey(RouterConstant.PATH_CHAT_ACK_PAGE)
                .withParam(RouterConstant.KEY_MESSAGE, data.getMessageData().getMessage())
                .withContext(v.getContext())
                .navigate();
          });
    }
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
      baseViewBinding.messageNormalReplyContainer.removeAllViews();
      return;
    }
    addReplayViewToTopGroup();
    final NormalChatMessageReplayNormalViewBinding replyBinding = replayBinding;
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
            String content = "| " + MessageHelper.getReplyContent(replyMessage);
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

  // 添加 normal 下的回复布局
  private void addReplayViewToTopGroup() {
    replayBinding =
        NormalChatMessageReplayNormalViewBinding.inflate(
            LayoutInflater.from(parent.getContext()), getNormalReplyContainer(), true);
  }

  private void addRevokeViewToMessageContainer() {
    revokedViewBinding =
        NormalChatMessageRevokedNormalViewBinding.inflate(
            LayoutInflater.from(parent.getContext()), getMessageContentContainer(), true);
  }
}
