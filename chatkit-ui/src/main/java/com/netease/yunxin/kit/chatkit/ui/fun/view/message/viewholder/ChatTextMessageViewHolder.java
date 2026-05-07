// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder;

import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageRefer;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageAIStreamStatus;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.manager.UserAIBotManager;
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

  /** 文本消息背景只设置给原文气泡（flMessageBody）， 译文气泡（llTranslation）使用独立背景 fun_bg_message_translate。 */
  @Override
  protected View getMessageBackgroundView() {
    if (textBinding != null) {
      return textBinding.flMessageBody;
    }
    return super.getMessageBackgroundView();
  }

  @Override
  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    super.bindData(message, lastMessage);
    baseViewBinding.messageUpdateRefresh.setImageResource(R.drawable.fun_ic_chat_ai_refresh);
    baseViewBinding.messageUpdateStop.setImageResource(R.drawable.fun_ic_chat_ai_stop);
    setMessageText(message);
    syncRootGravity();
    bindTranslation(message);
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
  protected void onTranslationUpdate(ChatMessageBean message) {
    bindTranslation(message);
  }

  /** 同步根容器的 gravity，保证发送方消息的原文气泡和译文气泡都右对齐， 避免译文出现时内容宽度撑大后与右侧头像间距异常。 */
  private void syncRootGravity() {
    if (textBinding == null) return;
    android.widget.LinearLayout root = (android.widget.LinearLayout) textBinding.getRoot();
    if (showReceiveUIStyle()) {
      root.setGravity(android.view.Gravity.START);
    } else {
      root.setGravity(android.view.Gravity.END);
    }
  }

  /** 绑定译文展示区域（Fun 皮肤：独立译文气泡） */
  private void bindTranslation(ChatMessageBean message) {
    com.netease.yunxin.kit.chatkit.model.TranslationInfo info = message.getTranslationInfo();
    boolean hasTranslation =
        info != null
            && !android.text.TextUtils.isEmpty(info.getTranslatedText())
            && message.isTranslationVisible();
    if (hasTranslation) {
      textBinding.llTranslation.setVisibility(View.VISIBLE);
      textBinding.translationText.setText(info.getTranslatedText());
      // 发送方：译文气泡右侧紧贴气泡边缘；接收方：左侧
      android.view.ViewGroup.MarginLayoutParams lp =
          (android.view.ViewGroup.MarginLayoutParams) textBinding.llTranslation.getLayoutParams();
      int margin = com.netease.yunxin.kit.common.utils.SizeUtils.dp2px(5);
      if (showReceiveUIStyle()) {
        lp.setMarginStart(margin);
        lp.setMarginEnd(0);
      } else {
        lp.setMarginStart(0);
        lp.setMarginEnd(margin);
      }
      textBinding.llTranslation.setLayoutParams(lp);
      // 译文气泡长按：弹出复制/转发/隐藏菜单
      textBinding.llTranslation.setOnLongClickListener(
          v -> {
            if (itemClickListener != null) {
              itemClickListener.onTranslationLongClick(v, position, message);
            }
            return true;
          });
      // 译文 TextView 使用 wrap_content，可以撑宽译文气泡（llTranslation）。
      // 待布局完成后，将 maxWidth 设置为 messageContainer 的最大允许宽度，
      // 防止超长译文在单行无限延伸超出屏幕边界。
      baseViewBinding.messageContainer.post(
          () -> {
            int containerMaxWidth = baseViewBinding.messageContainer.getWidth();
            if (containerMaxWidth > 0) {
              textBinding.translationText.setMaxWidth(containerMaxWidth);
            }
          });
    } else {
      textBinding.translationText.setMaxWidth(Integer.MAX_VALUE);
      textBinding.llTranslation.setVisibility(View.GONE);
      textBinding.llTranslation.setOnLongClickListener(null);
    }
  }

  private void setMessageText(ChatMessageBean message) {
    // 设置消息文本
    if (showReceiveUIStyle()) {
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
      String text = message.getMessageData().getText();
      if (message.isAIResponseMsg()) {
        updateOperateView();
        V2NIMMessageAIStreamStatus aiStreamStatus = message.getAIConfig().getAIStreamStatus();
        if (aiStreamStatus
            != V2NIMMessageAIStreamStatus.V2NIM_MESSAGE_AI_STREAM_STATUS_PLACEHOLDER) {
          if (!TextUtils.isEmpty(currentMessage.getKeyword())) {
            MessageHelper.identifyFaceExpressionAndHighlight(
                textBinding.getRoot().getContext(),
                textBinding.messageText,
                message.getMessageData().getText(),
                currentMessage.getKeyword(),
                textBinding
                    .getRoot()
                    .getContext()
                    .getResources()
                    .getColor(R.color.fun_chat_message_highlight_color));
          } else if (TextUtils.isEmpty(text)
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
      } else if (UserAIBotManager.isUserAIBot(message.getSenderId())) {
        MarkDownViwUtils.makeMarkDown(
            textBinding.getRoot().getContext(), textBinding.messageText, text);
      } else {
        if (!TextUtils.isEmpty(currentMessage.getKeyword())) {
          MessageHelper.identifyFaceExpressionAndHighlight(
              textBinding.getRoot().getContext(),
              textBinding.messageText,
              message.getMessageData().getText(),
              currentMessage.getKeyword(),
              textBinding
                  .getRoot()
                  .getContext()
                  .getResources()
                  .getColor(R.color.fun_chat_message_highlight_color));
        } else if (!isChatMsg() || !IMKitConfigCenter.getEnableAtMessage()) {
          MessageHelper.identifyFaceExpression(
              textBinding.getRoot().getContext(),
              textBinding.messageText,
              message.getMessageData().getText(),
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
    if (TextUtils.isEmpty(currentMessage.keyword)) {
      TextLinkifyUtils.addLinks(
          textBinding.messageText, itemClickListener, position, currentMessage);
    }
  }

  private void updateOperateView() {
    if (currentMessage.isAIResponseMsg() && isChatMsg()) {
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
      if (itemClickListener != null) {
        textBinding.messageText.setOnClickListener(
            v -> {
              SelectableTextHelper.getInstance().dismiss();
              itemClickListener.onMessageClick(textBinding.messageText, position, currentMessage);
            });
      }
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
