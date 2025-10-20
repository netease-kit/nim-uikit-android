// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageRefer;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageAIStreamStatus;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.common.TextLinkifyUtils;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageTextViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.textSelectionHelper.SelectableTextHelper;
import com.netease.yunxin.kit.chatkit.ui.view.MarkDownViwUtils;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;

/** view holder for Text message */
public class ChatTextMessageViewHolder extends FunChatBaseMessageViewHolder {

  FunChatMessageTextViewHolderBinding textBinding;

  public ChatTextMessageViewHolder(@NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addViewToMessageContainer() {
    textBinding =
        FunChatMessageTextViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getMessageContainer(), true);
  }

  @Override
  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    super.bindData(message, lastMessage);
    baseViewBinding.messageUpdateRefresh.setImageResource(R.drawable.fun_ic_chat_ai_refresh);
    baseViewBinding.messageUpdateStop.setImageResource(R.drawable.fun_ic_chat_ai_stop);
    setMessageText(message);
    initEvent();
  }

  @Override
  protected boolean needShowMultiSelect() {
    return super.needShowMultiSelect() && !this.currentMessage.AIMessageStreaming();
  }

  @Override
  protected void setSelectStatus(ChatMessageBean message) {
    super.setSelectStatus(message);
    updateOperateView();
  }

  @Override
  protected void onMessageUpdate(ChatMessageBean data) {
    super.onMessageUpdate(data);
    ALog.d(
        LIB_TAG,
        "ChatTextMessageViewHolder",
        "onMessageUpdate:" + data.getMessageData().getMessage().getText());
    setMessageText(data);
  }

  private void setMessageText(ChatMessageBean message) {
    // 设置消息文本
    if (MessageHelper.isReceivedMessage(message)) {
      if (properties.getReceiveMessageTextSize() != null) {
        textBinding.messageText.setTextSize(properties.getReceiveMessageTextSize());
      }
      if (properties.getReceiveMessageTextColor() != null) {
        textBinding.messageText.setTextColor(properties.getReceiveMessageTextColor());
      }
    } else {
      if (properties.getSelfMessageTextSize() != null) {
        textBinding.messageText.setTextSize(properties.getSelfMessageTextSize());
      }
      if (properties.getSelfMessageTextColor() != null) {
        textBinding.messageText.setTextColor(properties.getSelfMessageTextColor());
      }
    }

    if (message.getMessageData().getMessage().getMessageType()
        == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT) {
      if (message.isAIResponseMsg()) {
        updateOperateView();
        V2NIMMessageAIStreamStatus aiStreamStatus = message.getAIConfig().getAIStreamStatus();
        if (aiStreamStatus
            != V2NIMMessageAIStreamStatus.V2NIM_MESSAGE_AI_STREAM_STATUS_PLACEHOLDER) {
          String text = message.getMessageData().getMessage().getText();
          if (TextUtils.isEmpty(text)
              && aiStreamStatus
                  == V2NIMMessageAIStreamStatus.V2NIM_MESSAGE_AI_STREAM_STATUS_ABORTED) {
            text = textBinding.getRoot().getContext().getString(R.string.chat_ai_error);
            textBinding.messageText.setText(text);
          } else {
            MarkDownViwUtils.makeMarkDown(
                textBinding.getRoot().getContext(), textBinding.messageText, text);
          }
        }
        if (aiStreamStatus != V2NIMMessageAIStreamStatus.V2NIM_MESSAGE_AI_STREAM_STATUS_PLACEHOLDER
            && aiStreamStatus
                != V2NIMMessageAIStreamStatus.V2NIM_MESSAGE_AI_STREAM_STATUS_STREAMING) {
          setSelectStatus(message);
        }
      } else {
        if (isForwardMsg() || !IMKitConfigCenter.getEnableAtMessage()) {
          MessageHelper.identifyFaceExpression(
              textBinding.getRoot().getContext(),
              textBinding.messageText,
              message.getMessageData().getMessage().getText(),
              ImageSpan.ALIGN_BOTTOM);
        } else {
          MessageHelper.identifyExpression(
              textBinding.getRoot().getContext(),
              textBinding.messageText,
              message.getMessageData().getMessage());
        }
      }
    } else {
      //暂不支持消息展示提示信息
      textBinding.messageText.setText(
          parent.getContext().getResources().getString(R.string.chat_message_not_support_tips));
    }
    // 指定模式（例如只识别电话和邮箱）
    TextLinkifyUtils.addLinks(textBinding.messageText, itemClickListener, position, currentMessage);
  }

  private void updateOperateView() {
    if (currentMessage.isAIResponseMsg()) {
      V2NIMMessageAIStreamStatus aiStreamStatus = currentMessage.getAIConfig().getAIStreamStatus();
      V2NIMMessageRefer threadInfo = currentMessage.getReplyMessageRefer();
      if (aiStreamStatus == V2NIMMessageAIStreamStatus.V2NIM_MESSAGE_AI_STREAM_STATUS_PLACEHOLDER) {
        if (TextUtils.equals(threadInfo.getSenderId(), IMKitClient.account()) && !isMultiSelect) {
          baseViewBinding.messageUpdateRefresh.setVisibility(View.GONE);
          baseViewBinding.messageUpdateStop.setVisibility(View.VISIBLE);
          baseViewBinding.messageUpdateOperate.setVisibility(View.VISIBLE);
        } else {
          baseViewBinding.messageUpdateRefresh.setVisibility(View.GONE);
          baseViewBinding.messageUpdateStop.setVisibility(View.GONE);
          baseViewBinding.messageUpdateOperate.setVisibility(View.GONE);
        }
        textBinding.messageLoading.setVisibility(View.VISIBLE);
        textBinding.messageText.setVisibility(View.VISIBLE);
      } else {
        if (TextUtils.equals(threadInfo.getSenderId(), IMKitClient.account()) && !isMultiSelect) {
          if (aiStreamStatus
              == V2NIMMessageAIStreamStatus.V2NIM_MESSAGE_AI_STREAM_STATUS_STREAMING) {
            baseViewBinding.messageUpdateRefresh.setVisibility(View.GONE);
            baseViewBinding.messageUpdateStop.setVisibility(View.VISIBLE);
          } else {
            baseViewBinding.messageUpdateRefresh.setVisibility(View.VISIBLE);
            baseViewBinding.messageUpdateStop.setVisibility(View.GONE);
          }
          baseViewBinding.messageUpdateOperate.setVisibility(View.VISIBLE);
        } else {
          baseViewBinding.messageUpdateRefresh.setVisibility(View.GONE);
          baseViewBinding.messageUpdateStop.setVisibility(View.GONE);
          baseViewBinding.messageUpdateOperate.setVisibility(View.GONE);
        }
        textBinding.messageLoading.setVisibility(View.GONE);
        textBinding.messageText.setVisibility(View.VISIBLE);
      }
    } else {
      baseViewBinding.messageUpdateRefresh.setVisibility(View.GONE);
      baseViewBinding.messageUpdateStop.setVisibility(View.GONE);
      baseViewBinding.messageUpdateOperate.setVisibility(View.GONE);
      textBinding.messageLoading.setVisibility(View.GONE);
      textBinding.messageText.setVisibility(View.VISIBLE);
    }
  }

  private void initEvent() {
    //    设置选中文本监听回调
    SelectableTextHelper.getInstance()
        .setSelectableOnChangeListener(
            (view, pos, msg, text, isSelectAll) -> {
              if (itemClickListener != null) {
                itemClickListener.onTextSelected(view, pos, msg, text.toString(), isSelectAll);
              }
            });
    //    设置长按事件
    if (!isMultiSelect) {
      textBinding.messageText.setOnLongClickListener(
          v -> {
            if (isMultiSelect) {
              return true;
            }
            SelectableTextHelper.getInstance()
                .showSelectView(
                    textBinding.messageText,
                    textBinding.messageText.getLayout(),
                    position,
                    currentMessage);
            return true;
          });
    } else {
      textBinding.messageText.setOnLongClickListener(null);
    }

    //    设置点击事件
    if (!isMultiSelect) {
      textBinding.messageText.setOnClickListener(v -> SelectableTextHelper.getInstance().dismiss());
    } else {
      textBinding.messageText.setOnClickListener(this::clickSelect);
    }

    baseViewBinding.messageUpdateStop.setOnClickListener(
        v -> {
          itemClickListener.onAIMessageStreamStop(v, position, currentMessage.getMessageData());
        });
    baseViewBinding.messageUpdateRefresh.setOnClickListener(
        v -> {
          itemClickListener.onAIMessageRefresh(v, position, currentMessage.getMessageData());
        });
  }

  @Override
  public void onMessageRevokeStatus(ChatMessageBean data) {
    super.onMessageRevokeStatus(data);
    if (revokedViewBinding != null) {
      if (!MessageHelper.revokeMsgIsEdit(data)) {
        revokedViewBinding.tvAction.setVisibility(View.GONE);
      }
    }
  }
}
