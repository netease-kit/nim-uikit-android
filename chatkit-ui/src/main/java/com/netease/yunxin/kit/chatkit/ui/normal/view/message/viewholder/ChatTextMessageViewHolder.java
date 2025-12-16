// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.view.message.viewholder;

import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.nimlib.coexist.sdk.v2.message.V2NIMMessageRefer;
import com.netease.nimlib.coexist.sdk.v2.message.enums.V2NIMMessageAIStreamStatus;
import com.netease.nimlib.coexist.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.common.TextLinkifyUtils;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.NormalChatMessageTextViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.textSelectionHelper.SelectableTextHelper;
import com.netease.yunxin.kit.chatkit.ui.view.MarkDownViwUtils;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options.CommonUIOption;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;

/** view holder for Text message */
public class ChatTextMessageViewHolder extends NormalChatBaseMessageViewHolder {

  NormalChatMessageTextViewHolderBinding textBinding;

  public ChatTextMessageViewHolder(@NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addViewToMessageContainer() {
    textBinding =
        NormalChatMessageTextViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getMessageContainer(), true);
  }

  @Override
  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    super.bindData(message, lastMessage);
    setMessageText(message);
    initEvent();
  }

  @Override
  protected void onMessageUpdate(ChatMessageBean data) {
    super.onMessageUpdate(data);
    setMessageText(data);
  }

  @Override
  protected void setSelectStatus(ChatMessageBean message) {
    super.setSelectStatus(message);
    updateOperateView();
  }

  private void setMessageText(ChatMessageBean message) {
    CommonUIOption commonUIOption = uiOptions.commonUIOption;
    if (MessageHelper.isReceivedMessage(message)) {
      if (commonUIOption.messageTextColor != null) {
        textBinding.messageText.setTextColor(commonUIOption.messageTextColor);
      } else if (properties.getReceiveMessageTextColor() != null) {
        textBinding.messageText.setTextColor(properties.getReceiveMessageTextColor());
      }
      if (commonUIOption.messageTextSize != null) {
        textBinding.messageText.setTextSize(commonUIOption.messageTextSize);
      } else if (properties.getReceiveMessageTextSize() != null) {
        textBinding.messageText.setTextSize(properties.getReceiveMessageTextSize());
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
        //转发消息不需要展示@的高亮
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
        baseViewBinding.messageUpdateOperate.setVisibility(View.GONE);
      }
    } else {
      //文件消息暂不支持所以展示提示信息
      textBinding.messageText.setText(
          parent.getContext().getResources().getString(R.string.chat_message_not_support_tips));
    }
    // 也可单独指定模式（例如只识别电话和邮箱）
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
  protected boolean needShowMultiSelect() {
    return super.needShowMultiSelect() && !this.currentMessage.AIMessageStreaming();
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
