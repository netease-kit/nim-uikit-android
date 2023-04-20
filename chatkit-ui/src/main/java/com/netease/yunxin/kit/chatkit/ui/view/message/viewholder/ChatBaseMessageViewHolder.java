// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message.viewholder;

import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.MsgThreadOption;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant;
import com.netease.yunxin.kit.chatkit.ui.ChatMessageType;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatMessageRevokedViewBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.page.ChatMessageAckActivity;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageReader;
import com.netease.yunxin.kit.chatkit.ui.view.message.MessageProperties;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.utils.TimeFormatUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import java.util.List;

/** base message view holder for chat message item */
public abstract class ChatBaseMessageViewHolder extends RecyclerView.ViewHolder {

  private static final String TAG = "ChatBaseMessageViewHolder";

  private static final int SHOW_TIME_INTERVAL = 5 * 60 * 1000;

  private static final int MAX_RECEIPT_NUM = 100;

  public IMessageItemClickListener itemClickListener;

  public IMessageReader messageReader;

  public int type;

  public int position;

  public long receiptTime;

  public Team teamInfo;

  public ChatMessageBean currentMessage;

  public IMMessageInfo replyMessage;

  MessageProperties properties = new MessageProperties();

  public ChatBaseMessageViewHolderBinding baseViewBinding;

  public ChatMessageRevokedViewBinding revokedViewBinding;

  public ViewGroup parent;

  public ChatBaseMessageViewHolder(@NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    this(parent.baseRoot);
    this.parent = parent.getRoot();
    this.type = viewType;
    baseViewBinding = parent;
  }

  public ChatBaseMessageViewHolder(View view) {
    super(view);
  }

  public void setItemClickListener(IMessageItemClickListener callBack) {
    itemClickListener = callBack;
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "setItemClickListener" + (callBack == null));
  }

  public void setMessageReader(IMessageReader messageReader) {
    this.messageReader = messageReader;
  }

  public void setProperties(MessageProperties properties) {
    if (properties != null) {
      this.properties = properties;
    }
  }

  public void bindData(ChatMessageBean data, int position, @NonNull List<?> payload) {
    if (!payload.isEmpty()) {
      for (int i = 0; i < payload.size(); ++i) {
        String payloadItem = payload.get(i).toString();
        ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "bindData,payload" + payloadItem);
        if (TextUtils.equals(payloadItem, ActionConstants.PAYLOAD_STATUS)) {
          setStatus(data);
          currentMessage = data;
          onMessageStatus(data);
        } else if (TextUtils.equals(payloadItem, ActionConstants.PAYLOAD_REVOKE)) {
          onMessageRevoked(data);
          onMessageSignal(data);
        } else if (TextUtils.equals(payloadItem, ActionConstants.PAYLOAD_SIGNAL)) {
          onMessageSignal(data);
        } else if (TextUtils.equals(payloadItem, ActionConstants.PAYLOAD_PROGRESS)) {
          onProgressUpdate(data);
        } else if (TextUtils.equals(payloadItem, ActionConstants.PAYLOAD_USERINFO)) {
          setUserInfo(data);
        } else if (TextUtils.equals(payloadItem, ActionConstants.PAYLOAD_REVOKE_STATUS)) {
          onMessageRevokeStatus(data);
          onMessageSignal(data);
        } else if (TextUtils.equals(payloadItem, ActionConstants.PAYLOAD_REPLY)) {
          setReplyInfo(data);
        }
      }
    }
    this.position = position;
  }

  protected void onMessageStatus(ChatMessageBean data) {}

  protected void onProgressUpdate(ChatMessageBean data) {}

  public void onMessageRevokeStatus(ChatMessageBean data) {}

  private void onMessageSignal(ChatMessageBean data) {
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "onMessageSignal" + data.getPinAccid());
    if (!TextUtils.isEmpty(data.getPinAccid()) && !data.isRevoked()) {
      baseViewBinding.tvSignal.setVisibility(View.VISIBLE);
      String tid = null;
      if (data.getMessageData().getMessage().getSessionType() == SessionTypeEnum.Team) {
        tid = data.getMessageData().getMessage().getSessionId();
      }
      String nick = MessageHelper.getChatDisplayNameYou(tid, data.getPinAccid());
      if (data.getMessageData().getMessage().getSessionType() == SessionTypeEnum.P2P) {
        baseViewBinding.tvSignal.setText(
            String.format(
                IMKitClient.getApplicationContext().getString(R.string.chat_message_signal_tip),
                nick));
      } else if (data.getMessageData().getMessage().getSessionType() == SessionTypeEnum.Team) {
        baseViewBinding.tvSignal.setText(
            String.format(
                IMKitClient.getApplicationContext()
                    .getString(R.string.chat_message_signal_tip_for_team),
                nick));
      }

      ConstraintLayout.LayoutParams layoutParams =
          (ConstraintLayout.LayoutParams) baseViewBinding.tvSignal.getLayoutParams();
      if (MessageHelper.isReceivedMessage(data)) {
        layoutParams.leftToLeft = R.id.marginStart;
        layoutParams.rightToRight = -1;
      } else {
        layoutParams.leftToLeft = -1;
        layoutParams.rightToRight = R.id.marginEnd;
      }
      baseViewBinding.tvSignal.setLayoutParams(layoutParams);

      if (properties.getSignalBgColor() != MessageProperties.INT_NULL) {
        baseViewBinding.baseRoot.setBackgroundColor(properties.getSignalBgColor());

      } else {
        baseViewBinding.baseRoot.setBackgroundColor(
            parent.getContext().getResources().getColor(R.color.color_fffbea));
      }

    } else {
      baseViewBinding.tvSignal.setVisibility(View.GONE);
      baseViewBinding.baseRoot.setBackgroundColor(
          parent.getContext().getResources().getColor(R.color.title_transfer));
    }
  }

  public void onMessageRevoked(ChatMessageBean data) {
    if (!data.isRevoked()) {
      return;
    }
    baseViewBinding.tvReply.setVisibility(View.GONE);
    baseViewBinding.messageContainer.removeAllViews();
    revokedViewBinding =
        ChatMessageRevokedViewBinding.inflate(
            LayoutInflater.from(parent.getContext()), baseViewBinding.messageRevoke, true);
    if (MessageHelper.revokeMsgIsEdit(data)) {
      revokedViewBinding.tvAction.setVisibility(View.VISIBLE);
      //reedit
      revokedViewBinding.tvAction.setOnClickListener(
          v -> {
            if (itemClickListener != null) {
              itemClickListener.onReEditRevokeMessage(v, position, data);
            }
          });
    } else {
      revokedViewBinding.tvAction.setVisibility(View.GONE);
    }
  }

  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    currentMessage = message;
    int padding = SizeUtils.dp2px(8);
    baseViewBinding.baseRoot.setPadding(padding, padding, padding, padding);
    baseViewBinding.messageContainer.removeAllViews();
    baseViewBinding.messageRevoke.removeAllViews();
    addContainer();
    if (type == ChatMessageType.NOTICE_MESSAGE_VIEW_TYPE
        || type == ChatMessageType.TIP_MESSAGE_VIEW_TYPE) {
      baseViewBinding.fromAvatar.setVisibility(View.GONE);
      baseViewBinding.avatarMine.setVisibility(View.GONE);
      baseViewBinding.messageStatus.setVisibility(View.GONE);
      baseViewBinding.tvTime.setVisibility(View.GONE);
      baseViewBinding.messageBody.setGravity(Gravity.CENTER);
      return;
    }

    setReplyInfo(message);
    setUserInfo(message);
    setTime(message, lastMessage);
    onMessageSignal(message);
    onMessageRevoked(message);
    setStatusCallback();
  }

  public void setUserInfo(ChatMessageBean message) {
    if (type == ChatMessageType.NOTICE_MESSAGE_VIEW_TYPE
        || type == ChatMessageType.TIP_MESSAGE_VIEW_TYPE) {
      return;
    }
    ConstraintLayout.LayoutParams layoutParams =
        (ConstraintLayout.LayoutParams) baseViewBinding.messageBody.getLayoutParams();
    if (MessageHelper.isReceivedMessage(message)) {
      baseViewBinding.messageBody.setGravity(Gravity.START);
      //防止UserInfo数据不存在
      if (message.getMessageData().getFromUser() == null) {
        ChatRepo.fetchUserInfo(
            message.getMessageData().getMessage().getFromAccount(),
            new FetchCallback<UserInfo>() {
              @Override
              public void onSuccess(@Nullable UserInfo param) {
                message.getMessageData().setFromUser(param);
                loadNickAndAvatar(message);
              }

              @Override
              public void onFailed(int code) {
                loadNickAndAvatar(message);
              }

              @Override
              public void onException(@Nullable Throwable exception) {
                loadNickAndAvatar(message);
              }
            });
      } else {
        loadNickAndAvatar(message);
      }

    } else {
      baseViewBinding.messageBody.setGravity(Gravity.END);
      baseViewBinding.avatarMine.setVisibility(View.VISIBLE);
      if (properties.getAvatarCornerRadius() >= 0) {
        baseViewBinding.avatarMine.setCornerRadius(properties.getAvatarCornerRadius());
      }
      baseViewBinding.tvName.setVisibility(View.GONE);
      String messageUser = message.getMessageData().getMessage().getFromAccount();
      UserInfo userInfo = MessageHelper.getChatMessageUserInfo(messageUser);
      if (userInfo == null) {
        userInfo = message.getMessageData().getFromUser();
      }
      if (userInfo != null) {
        String nickname = userInfo.getName();
        baseViewBinding.avatarMine.setData(
            userInfo.getAvatar(), nickname, AvatarColor.avatarColor(userInfo.getAccount()));
      }
      baseViewBinding.fromAvatar.setVisibility(View.GONE);
      layoutParams.horizontalBias = 1f;
      if (properties.selfMessageRes != MessageProperties.INT_NULL) {
        baseViewBinding.llyMessage.setBackgroundResource(properties.selfMessageRes);
      } else if (properties.getSelfMessageBg() != null) {
        baseViewBinding.llyMessage.setBackground(properties.getSelfMessageBg());
      } else if (currentMessage.getMessageData().getMessage().getMsgType()
          == MsgTypeEnum.location) {
        baseViewBinding.llyMessage.setBackgroundResource(R.drawable.chat_message_stoke_self_bg);
      } else {
        baseViewBinding.llyMessage.setBackgroundResource(R.drawable.chat_message_self_bg);
      }

      baseViewBinding.messageStatus.setVisibility(View.VISIBLE);
      setStatus(message);
    }
  }

  public void loadNickAndAvatar(ChatMessageBean message) {
    ConstraintLayout.LayoutParams layoutParams =
        (ConstraintLayout.LayoutParams) baseViewBinding.messageBody.getLayoutParams();
    //get nick name
    String name = MessageHelper.getChatMessageUserName(message.getMessageData());
    if (message.getMessageData().getMessage().getSessionType() == SessionTypeEnum.Team) {
      baseViewBinding.tvName.setVisibility(View.VISIBLE);
      baseViewBinding.tvName.setText(name);
      if (properties.getUserNickColor() != MessageProperties.INT_NULL) {
        baseViewBinding.tvName.setTextColor(properties.getUserNickColor());
      }
      if (properties.getUserNickTextSize() != MessageProperties.INT_NULL) {
        baseViewBinding.tvName.setTextSize(properties.getUserNickTextSize());
      }
      layoutParams.topToBottom = R.id.tv_name;
    }
    String avatar =
        MessageHelper.getChatCacheAvatar(message.getMessageData().getMessage().getFromAccount());
    if (TextUtils.isEmpty(avatar)) {
      avatar =
          message.getMessageData().getFromUser() == null
              ? ""
              : message.getMessageData().getFromUser().getAvatar();
    }
    String avatarName =
        message.getMessageData().getFromUser() == null
            ? message.getMessageData().getMessage().getFromAccount()
            : message.getMessageData().getFromUser().getUserInfoName();
    baseViewBinding.fromAvatar.setVisibility(View.VISIBLE);
    if (properties.getAvatarCornerRadius() >= 0) {
      baseViewBinding.fromAvatar.setCornerRadius(properties.getAvatarCornerRadius());
    }
    baseViewBinding.fromAvatar.setData(
        avatar,
        avatarName,
        AvatarColor.avatarColor(message.getMessageData().getMessage().getFromAccount()));
    baseViewBinding.avatarMine.setVisibility(View.GONE);
    if (properties.receiveMessageRes != MessageProperties.INT_NULL) {
      baseViewBinding.llyMessage.setBackgroundResource(properties.receiveMessageRes);
    } else if (properties.getReceiveMessageBg() != null) {
      baseViewBinding.llyMessage.setBackground(properties.getReceiveMessageBg());
    } else if (currentMessage.getMessageData().getMessage().getMsgType() == MsgTypeEnum.location) {
      baseViewBinding.llyMessage.setBackgroundResource(R.drawable.chat_message_stroke_other_bg);
    } else {
      baseViewBinding.llyMessage.setBackgroundResource(R.drawable.chat_message_other_bg);
    }
    baseViewBinding.messageStatus.setVisibility(View.GONE);
    layoutParams.horizontalBias = 0f;
  }

  public void setThreadReplyInfo(ChatMessageBean messageBean) {
    MsgThreadOption threadOption = messageBean.getMessageData().getMessage().getThreadOption();
    String replyFrom = threadOption.getReplyMsgFromAccount();
    if (TextUtils.isEmpty(replyFrom)) {
      ALog.w(
          TAG,
          "no reply message found, uuid=" + messageBean.getMessageData().getMessage().getUuid());
      baseViewBinding.tvReply.setVisibility(View.GONE);
      return;
    }
    baseViewBinding.tvReply.setVisibility(View.VISIBLE);

    String replyUuid = threadOption.getReplyMsgIdClient();
    MessageHelper.getReplyMessageInfo(
        replyUuid,
        new FetchCallback<List<IMMessageInfo>>() {
          @Override
          public void onSuccess(@Nullable List<IMMessageInfo> param) {
            if (param != null && param.size() > 0) {
              replyMessage = param.get(0);

              String content = "| " + MessageHelper.getReplyContent(replyMessage);
              MessageHelper.identifyFaceExpression(
                  baseViewBinding.tvReply.getContext(),
                  baseViewBinding.tvReply,
                  content,
                  ImageSpan.ALIGN_BOTTOM);
            }
          }

          @Override
          public void onFailed(int code) {
            baseViewBinding.tvReply.setVisibility(View.GONE);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            baseViewBinding.tvReply.setVisibility(View.GONE);
          }
        });

    if (itemClickListener != null) {
      baseViewBinding.tvReply.setOnClickListener(
          v -> itemClickListener.onReplyMessageClick(v, position, replyMessage));
    }
  }

  public void setReplyInfo(ChatMessageBean messageBean) {
    replyMessage = null;
    if (messageBean == null || messageBean.getMessageData() == null) {
      return;
    }
    ALog.w(TAG, TAG, "setReplyInfo, uuid=" + messageBean.getMessageData().getMessage().getUuid());
    if (messageBean.hasReply()) {
      //自定义回复实现
      baseViewBinding.tvReply.setVisibility(View.VISIBLE);
      String replyUuid = messageBean.getReplyUUid();
      if (!TextUtils.isEmpty(replyUuid)) {
        MessageHelper.getReplyMessageInfo(
            replyUuid,
            new FetchCallback<List<IMMessageInfo>>() {
              @Override
              public void onSuccess(@Nullable List<IMMessageInfo> param) {
                replyMessage = null;
                if (param != null && param.size() > 0) {
                  replyMessage = param.get(0);
                }
                String content = "| " + MessageHelper.getReplyContent(replyMessage);
                MessageHelper.identifyFaceExpression(
                    baseViewBinding.tvReply.getContext(),
                    baseViewBinding.tvReply,
                    content,
                    ImageSpan.ALIGN_BOTTOM);
              }

              @Override
              public void onFailed(int code) {
                baseViewBinding.tvReply.setVisibility(View.GONE);
              }

              @Override
              public void onException(@Nullable Throwable exception) {
                baseViewBinding.tvReply.setVisibility(View.GONE);
              }
            });
      }

      if (itemClickListener != null) {
        baseViewBinding.tvReply.setOnClickListener(
            v -> itemClickListener.onReplyMessageClick(v, position, replyMessage));
      }
    } else if (messageBean.getMessageData().getMessage().getThreadOption() != null
        && !TextUtils.isEmpty(
            messageBean.getMessageData().getMessage().getThreadOption().getReplyMsgIdClient())) {
      //thread 回复
      setThreadReplyInfo(messageBean);
    } else {
      baseViewBinding.tvReply.setVisibility(View.GONE);
    }
  }

  public void setTime(ChatMessageBean message, ChatMessageBean lastMessage) {
    long createTime =
        message.getMessageData().getMessage().getTime() == 0
            ? System.currentTimeMillis()
            : message.getMessageData().getMessage().getTime();
    if (lastMessage != null
        && createTime - lastMessage.getMessageData().getMessage().getTime() < SHOW_TIME_INTERVAL) {
      baseViewBinding.tvTime.setVisibility(View.GONE);
    } else {
      baseViewBinding.tvTime.setVisibility(View.VISIBLE);
      if (properties.getTimeTextColor() != MessageProperties.INT_NULL) {
        baseViewBinding.tvTime.setTextColor(properties.getTimeTextColor());
      }
      if (properties.getTimeTextSize() != MessageProperties.INT_NULL) {
        baseViewBinding.tvTime.setTextSize(properties.getTimeTextSize());
      }
      baseViewBinding.tvTime.setText(
          TimeFormatUtils.formatMillisecond(itemView.getContext(), createTime));
    }
  }

  public void setReceiptTime(long receiptTime) {
    this.receiptTime = receiptTime;
  }

  public void setTeamInfo(Team teamInfo) {
    this.teamInfo = teamInfo;
  }

  protected void setStatus(ChatMessageBean data) {
    if (data.getMessageData().getMessage().getStatus() == MsgStatusEnum.sending) {
      if (showSending()) {
        baseViewBinding.messageSending.setVisibility(View.VISIBLE);
      } else {
        baseViewBinding.messageSending.setVisibility(View.GONE);
      }
      baseViewBinding.ivStatus.setVisibility(View.GONE);
      baseViewBinding.readProcess.setVisibility(View.GONE);
    } else if (needMsgSendingStatus()
        && ((data.getMessageData().getMessage().getStatus() == MsgStatusEnum.fail)
            || data.getMessageData().getMessage().isInBlackList())) {
      baseViewBinding.ivStatus.setVisibility(View.VISIBLE);
      baseViewBinding.ivStatus.setImageResource(R.drawable.ic_error);
      baseViewBinding.readProcess.setVisibility(View.GONE);
      baseViewBinding.messageSending.setVisibility(View.GONE);
    } else if (data.getMessageData().getMessage().getSessionType() == SessionTypeEnum.P2P) {
      baseViewBinding.messageSending.setVisibility(View.GONE);
      baseViewBinding.readProcess.setVisibility(View.GONE);
      if (!properties.getShowP2pMessageStatus()
          || !data.getMessageData().getMessage().needMsgAck()
          || !needReadStatus()) {
        baseViewBinding.ivStatus.setVisibility(View.GONE);
      } else {
        baseViewBinding.ivStatus.setVisibility(View.VISIBLE);
        if (data.getMessageData().getMessage().isRemoteRead()) {
          baseViewBinding.ivStatus.setImageResource(R.drawable.ic_message_read);
          data.setHaveRead(true);
        } else {
          baseViewBinding.ivStatus.setImageResource(R.drawable.ic_message_unread);
        }
      }
    } else if (data.getMessageData().getMessage().getSessionType() == SessionTypeEnum.Team) {
      baseViewBinding.messageSending.setVisibility(View.GONE);
      baseViewBinding.ivStatus.setVisibility(View.GONE);
      if (!properties.getShowTeamMessageStatus()
          || !data.getMessageData().getMessage().needMsgAck()
          || !needReadStatus()) {
        baseViewBinding.readProcess.setVisibility(View.GONE);
        return;
      }
      if ((teamInfo != null && teamInfo.getMemberCount() >= MAX_RECEIPT_NUM)
          || !data.getMessageData().getMessage().needMsgAck()) {
        baseViewBinding.readProcess.setVisibility(View.GONE);
        return;
      }
      int ackCount = data.getMessageData().getMessage().getTeamMsgAckCount();
      int unAckCount = data.getMessageData().getMessage().getTeamMsgUnAckCount();
      float all = ackCount + unAckCount;
      if (all > 0) {
        float process = ackCount / all;
        if (process < 1) {
          baseViewBinding.readProcess.setProcess(process);
          baseViewBinding.readProcess.setVisibility(View.VISIBLE);
        } else {
          baseViewBinding.ivStatus.setVisibility(View.VISIBLE);
          baseViewBinding.ivStatus.setImageResource(R.drawable.ic_message_read);
          baseViewBinding.readProcess.setVisibility(View.GONE);
        }
      }
      baseViewBinding.readProcess.setOnClickListener(
          v -> {
            ChatMessageAckActivity.startMessageAckActivity(
                v.getContext(), data.getMessageData().getMessage());
          });
    }
  }

  public void onAttachedToWindow() {
    if (currentMessage.getMessageData().getMessage().getSessionType() == SessionTypeEnum.Team) {
      if (messageReader != null
          && currentMessage.getMessageData().getMessage().needMsgAck()
          && !currentMessage.getMessageData().getMessage().hasSendAck()) {
        messageReader.messageRead(currentMessage.getMessageData());
      }
    }
  }

  /** set click listener for ivStatus */
  private void setStatusCallback() {
    if (itemClickListener != null) {
      baseViewBinding.ivStatus.setOnClickListener(
          v -> {
            if (currentMessage.getMessageData().getMessage().getStatus() == MsgStatusEnum.fail) {
              itemClickListener.onSendFailBtnClick(v, position, currentMessage);
            }
          });

      baseViewBinding.fromAvatar.setOnClickListener(
          v -> itemClickListener.onUserIconClick(v, position, currentMessage));

      baseViewBinding.fromAvatar.setOnLongClickListener(
          v -> itemClickListener.onUserIconLongClick(v, position, currentMessage));

      baseViewBinding.avatarMine.setOnClickListener(
          v -> itemClickListener.onSelfIconClick(v, position, currentMessage));

      baseViewBinding.avatarMine.setOnLongClickListener(
          v -> itemClickListener.onSelfIconLongClick(v, position, currentMessage));

      baseViewBinding.messageContainer.setOnLongClickListener(
          v -> itemClickListener.onMessageLongClick(v, position, currentMessage));

      baseViewBinding.messageContainer.setOnClickListener(
          v -> itemClickListener.onMessageClick(v, position, currentMessage));
    }
  }

  public void addContainer() {}

  public void onDetachedFromWindow() {}

  public ViewGroup getContainer() {
    return baseViewBinding.messageContainer;
  }

  public boolean showSending() {
    return true;
  }

  public boolean needReadStatus() {
    return true;
  }

  public boolean needMsgSendingStatus() {
    return true;
  }
}
