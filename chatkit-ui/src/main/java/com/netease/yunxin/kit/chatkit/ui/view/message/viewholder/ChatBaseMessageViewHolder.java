/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.view.message.viewholder;

import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.MsgThreadOption;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.ChatMessageType;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.common.MessageUtil;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatMessageRevokedViewBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.page.ChatMessageAckActivity;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageReader;
import com.netease.yunxin.kit.chatkit.ui.view.message.MessageProperties;
import com.netease.yunxin.kit.chatkit.ui.view.message.adapter.ChatMessageAdapter;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.utils.TimeFormatUtils;
import com.netease.yunxin.kit.common.utils.ScreenUtil;
import com.netease.yunxin.kit.corekit.im.IMKitClient;

import java.util.List;

/**
 * base message view holder for chat message item
 */
public abstract class ChatBaseMessageViewHolder extends RecyclerView.ViewHolder {

    private static final String LOG_TAG = "ChatBaseMessageViewHolder";

    private static final int SHOW_TIME_INTERVAL = 5 * 60 * 1000;

    private static final int MAX_RECEIPT_NUM = 100;

    public IMessageItemClickListener itemClickListener;

    public IMessageReader messageReader;

    public int type;

    public int position;

    public long receiptTime;

    public Team teamInfo;

    public ChatMessageBean currentMessage;

    public boolean showReadStatus;

    MessageProperties properties = new MessageProperties();
    
    public ChatBaseMessageViewHolderBinding baseViewBinding;

    public ViewGroup parent;

    public ChatBaseMessageViewHolder(@NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
        this(parent.baseRoot);
        this.parent = parent.getRoot();
        this.type = viewType;
        baseViewBinding = parent;
    }

    public ChatBaseMessageViewHolder(View view){
        super(view);
    }

    public void setItemClickListener(IMessageItemClickListener callBack) {
        itemClickListener = callBack;
    }

    public void setMessageReader(IMessageReader messageReader) {
        this.messageReader = messageReader;
    }

    public void setProperties(MessageProperties properties) {
        if (properties != null) {
            this.properties = properties;
        }
    }

    public void setShowReadStatus(boolean show){
        this.showReadStatus = show;
    }

    public void bindData(ChatMessageBean data,int position, @NonNull List<?> payload) {
        if (!payload.isEmpty()) {
            for (int i = 0; i < payload.size(); ++i) {
                String payloadItem = payload.get(i).toString();
                if (TextUtils.equals(payloadItem, ChatMessageAdapter.STATUS_PAYLOAD)) {
                    setStatus(data);
                    currentMessage = data;
                    onMessageStatus(data);
                } else if (TextUtils.equals(payloadItem, ChatMessageAdapter.REVOKE_PAYLOAD)) {
                    onMessageRevoked(data);
                } else if (TextUtils.equals(payloadItem, ChatMessageAdapter.SIGNAL_PAYLOAD)) {
                    onMessageSignal(data);
                } else if (TextUtils.equals(payloadItem, ChatMessageAdapter.PROGRESS_PAYLOAD)) {
                    onProgressUpdate(data);
                }else if (TextUtils.equals(payloadItem, ChatMessageAdapter.PROGRESS_PAYLOAD)){
                    setUserInfo(data);
                }
            }
        }
        this.position = position;
    }

    protected void onMessageStatus(ChatMessageBean data) {

    }

    protected void onProgressUpdate(ChatMessageBean data) {

    }

    private void onMessageSignal(ChatMessageBean data) {
        if (!TextUtils.isEmpty(data.getPinAccid())) {
            baseViewBinding.tvSignal.setVisibility(View.VISIBLE);
            if (data.getMessageData().getMessage().getSessionType() == SessionTypeEnum.P2P) {
                baseViewBinding.tvSignal.setText(String.format(IMKitClient.getApplicationContext().getString(R.string.chat_message_signal_tip),
                        MessageHelper.getUserNickByAccId(data.getPinAccid(), true)));
            } else if (data.getMessageData().getMessage().getSessionType() == SessionTypeEnum.Team) {
                baseViewBinding.tvSignal.setText(String.format(IMKitClient.getApplicationContext().getString(R.string.chat_message_signal_tip_for_team),
                        MessageHelper.getUserNickByAccId(data.getPinAccid(), true)));
            }
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) baseViewBinding.tvSignal.getLayoutParams();
            if (isReceivedMessage(data)) {
                layoutParams.horizontalBias = 0f;
            } else {
                layoutParams.horizontalBias = 1f;
            }

            if (properties.getSignalBgColor() != MessageProperties.INT_NULL){
                baseViewBinding.baseRoot.setBackgroundColor(properties.getSignalBgColor());

            }else {
                baseViewBinding.baseRoot.setBackgroundColor(parent.getContext().getResources().getColor(R.color.color_fffbea));
            }

        } else {
            baseViewBinding.tvSignal.setVisibility(View.GONE);
            baseViewBinding.baseRoot.setBackgroundColor(parent.getContext().getResources().getColor(R.color.title_transfer));
        }
    }

    private void onMessageRevoked(ChatMessageBean data) {
        if (!data.isRevoked()) {
            return;
        }
        baseViewBinding.tvReply.setVisibility(View.GONE);
        baseViewBinding.messageContainer.removeAllViews();
        ChatMessageRevokedViewBinding revokedViewBinding = ChatMessageRevokedViewBinding.inflate(LayoutInflater.from(parent.getContext()),
                getContainer(), true);

        if (!isReceivedMessage(data) && data.getMessageData().getMessage().getMsgType() == MsgTypeEnum.text) {
            revokedViewBinding.tvAction.setVisibility(View.VISIBLE);
            //reedit
            revokedViewBinding.tvAction.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onReEditRevokeMessage(v,position, data);
                }
            });
        } else {
            revokedViewBinding.tvAction.setVisibility(View.GONE);
        }
    }

    public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
        currentMessage = message;
        int padding = ScreenUtil.dip2px(8);
        baseViewBinding.baseRoot.setPadding(padding, padding, padding, padding);
        baseViewBinding.messageContainer.removeAllViews();
        addContainer();
        if (type == ChatMessageType.NOTICE_MESSAGE_VIEW_TYPE || type == ChatMessageType.TIP_MESSAGE_VIEW_TYPE) {
            baseViewBinding.fromAvatar.setVisibility(View.GONE);
            baseViewBinding.avatarMine.setVisibility(View.GONE);
            baseViewBinding.messageStatus.setVisibility(View.GONE);
            baseViewBinding.tvTime.setVisibility(View.GONE);
            return;
        }
        if (message.getMessageData().getMessage().getThreadOption() != null) {
            setReplyInfo(message);
        } else {
            baseViewBinding.tvReply.setVisibility(View.GONE);
        }
        setUserInfo(message);
        setTime(message, lastMessage);
        onMessageSignal(message);
        onMessageRevoked(message);
        setStatusCallback();
    }

    private void setUserInfo(ChatMessageBean message){
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) baseViewBinding.messageBody.getLayoutParams();
        if (isReceivedMessage(message)) {
            //get nick name
            String name = MessageHelper.getChatMessageUserName(message.getMessageData().getMessage());
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
            String avatar = message.getMessageData().getFromUser() == null ? "" : message.getMessageData().getFromUser().getAvatar();
            baseViewBinding.fromAvatar.setVisibility(View.VISIBLE);
            baseViewBinding.fromAvatar.setData(avatar, name, AvatarColor.avatarColor(message.getMessageData().getMessage().getFromAccount()));
            if (properties.getAvatarCornerRadius() >= 0){
                baseViewBinding.fromAvatar.setCornerRadius(properties.getAvatarCornerRadius());
            }
            baseViewBinding.avatarMine.setVisibility(View.GONE);
            if (properties.getReceiveMessageBg() != null) {
                baseViewBinding.llyMessage.setBackground(properties.getReceiveMessageBg());
            } else {
                baseViewBinding.llyMessage.setBackgroundResource(R.drawable.chat_message_other_bg);
            }
            baseViewBinding.messageStatus.setVisibility(View.GONE);
            layoutParams.horizontalBias = 0f;
        } else {
            baseViewBinding.avatarMine.setVisibility(View.VISIBLE);
            if (properties.getAvatarCornerRadius() >= 0){
                baseViewBinding.avatarMine.setCornerRadius(properties.getAvatarCornerRadius());
            }
            baseViewBinding.tvName.setVisibility(View.GONE);
            NimUserInfo userInfo = IMKitClient.getUserInfo();
            if (userInfo != null) {
                String nickname = userInfo.getName() == null ? "" : userInfo.getName();
                baseViewBinding.avatarMine.setData(userInfo.getAvatar(),
                        nickname,
                        AvatarColor.avatarColor(userInfo.getAccount()));
            }
            baseViewBinding.fromAvatar.setVisibility(View.GONE);
            layoutParams.horizontalBias = 1f;
            if (properties.getSelfMessageBg() != null) {
                baseViewBinding.llyMessage.setBackground(properties.getSelfMessageBg());
            } else {
                baseViewBinding.llyMessage.setBackgroundResource(R.drawable.chat_message_self_bg);
            }

            baseViewBinding.messageStatus.setVisibility(View.VISIBLE);
            setStatus(message);
        }
    }

    private void setReplyInfo(ChatMessageBean messageBean) {
        MsgThreadOption threadOption = messageBean.getMessageData().getMessage().getThreadOption();
        String replyFrom = threadOption.getReplyMsgFromAccount();
        if (TextUtils.isEmpty(replyFrom)) {
            ALog.w(LOG_TAG, "no reply message found, uuid=" + messageBean.getMessageData().getMessage().getUuid());
            baseViewBinding.tvReply.setVisibility(View.GONE);
            return;
        }
        baseViewBinding.tvReply.setVisibility(View.VISIBLE);

        String replyUuid = threadOption.getReplyMsgIdClient();
        String content = "| " + MessageHelper.getReplyMessageInfo(replyUuid);
        MessageUtil.identifyFaceExpression(baseViewBinding.tvReply.getContext(), baseViewBinding.tvReply,content, ImageSpan.ALIGN_BOTTOM);
        if (itemClickListener != null) {
            baseViewBinding.tvReply.setOnClickListener(v -> itemClickListener.onReplyMessageClick(v,position, replyUuid));
        }
    }

    private void setTime(ChatMessageBean message, ChatMessageBean lastMessage) {
        long createTime = message.getMessageData().getMessage().getTime() == 0 ? System.currentTimeMillis() : message.getMessageData().getMessage().getTime();
        if (lastMessage != null
                && createTime - lastMessage.getMessageData().getMessage().getTime() < SHOW_TIME_INTERVAL) {
            baseViewBinding.tvTime.setVisibility(View.GONE);
        } else {
            baseViewBinding.tvTime.setVisibility(View.VISIBLE);
            if (properties.getTimeTextColor() != MessageProperties.INT_NULL){
                baseViewBinding.tvTime.setTextColor(properties.getTimeTextColor());
            }
            if (properties.getTimeTextSize() != MessageProperties.INT_NULL){
                baseViewBinding.tvTime.setTextSize(properties.getTimeTextSize());
            }
            baseViewBinding.tvTime.setText(TimeFormatUtils.formatMillisecond(itemView.getContext(), createTime));
        }
    }

    protected boolean isReceivedMessage(ChatMessageBean message) {
        return message.getMessageData().getMessage().getDirect() == MsgDirectionEnum.In;
    }

    public void setReceiptTime(long receiptTime) {
        this.receiptTime = receiptTime;
    }


    public void setTeamInfo(Team teamInfo) {
        this.teamInfo = teamInfo;
    }

    protected void setStatus(ChatMessageBean data) {
        if (data.getMessageData().getMessage().getStatus() == MsgStatusEnum.sending) {
            baseViewBinding.messageSending.setVisibility(View.VISIBLE);
            baseViewBinding.ivStatus.setVisibility(View.GONE);
            baseViewBinding.readProcess.setVisibility(View.GONE);
        } else if ((data.getMessageData().getMessage().getStatus() == MsgStatusEnum.fail)) {
            baseViewBinding.ivStatus.setVisibility(View.VISIBLE);
            baseViewBinding.ivStatus.setImageResource(R.drawable.ic_icon_error);
            baseViewBinding.messageSending.setVisibility(View.GONE);
        } else if (data.getMessageData().getMessage().getSessionType() == SessionTypeEnum.P2P) {
            baseViewBinding.messageSending.setVisibility(View.GONE);
            baseViewBinding.readProcess.setVisibility(View.GONE);
            if (!data.getMessageData().getMessage().needMsgAck() || !properties.getShowP2pMessageStatus()
            || !showReadStatus) {
                baseViewBinding.ivStatus.setVisibility(View.GONE);
            } else {
                baseViewBinding.ivStatus.setVisibility(View.VISIBLE);
                if (data.getMessageData().getMessage().getTime() <= receiptTime ||
                        data.getMessageData().getMessage().isRemoteRead()) {
                    baseViewBinding.ivStatus.setImageResource(R.drawable.ic_message_read);
                    data.setHaveRead(true);
                } else {
                    baseViewBinding.ivStatus.setImageResource(R.drawable.ic_message_unread);
                }
            }
        } else if (data.getMessageData().getMessage().getSessionType() == SessionTypeEnum.Team) {
            baseViewBinding.messageSending.setVisibility(View.GONE);
            baseViewBinding.ivStatus.setVisibility(View.GONE);
            if ((teamInfo != null && teamInfo.getMemberCount() >= MAX_RECEIPT_NUM) ||
                    !data.getMessageData().getMessage().needMsgAck()) {
                baseViewBinding.readProcess.setVisibility(View.GONE);
                return;
            }
            if (!properties.getShowTeamMessageStatus()|| !showReadStatus){
                baseViewBinding.readProcess.setVisibility(View.GONE);
                return;
            }
            baseViewBinding.readProcess.setVisibility(View.VISIBLE);
            int ackCount = data.getMessageData().getMessage().getTeamMsgAckCount();
            int unAckCount = data.getMessageData().getMessage().getTeamMsgUnAckCount();
            float all = ackCount + unAckCount;
            if (all > 0) {
                float process = ackCount / all;
                if (process < 1) {
                    baseViewBinding.readProcess.setProcess(process);
                } else {
                    baseViewBinding.ivStatus.setVisibility(View.VISIBLE);
                    baseViewBinding.ivStatus.setImageResource(R.drawable.ic_message_read);
                    baseViewBinding.readProcess.setVisibility(View.GONE);
                }
            }
            baseViewBinding.readProcess.setOnClickListener(v -> {
                //goto ChatMessageReadStateActivity
                ChatMessageAckActivity.startMessageAckActivity(v.getContext(), data.getMessageData().getMessage());
            });

        }
    }

    public void onAttachedToWindow() {
        if (currentMessage.getMessageData().getMessage().getSessionType() == SessionTypeEnum.Team) {
            if (messageReader != null && currentMessage.getMessageData().getMessage().needMsgAck()
                    && !currentMessage.getMessageData().getMessage().hasSendAck()) {
                messageReader.messageRead(currentMessage.getMessageData());
            }
        }
    }

    /**
     * set click listener for ivStatus
     */
    private void setStatusCallback() {
        if (itemClickListener != null) {
            baseViewBinding.ivStatus.setOnClickListener(v -> {
                if (currentMessage.getMessageData().getMessage().getStatus() == MsgStatusEnum.fail) {
                    itemClickListener.onSendFailBtnClick(v,position, currentMessage);
                }
            });

            baseViewBinding.fromAvatar.setOnClickListener(v -> itemClickListener.onUserIconClick(v,position, currentMessage));

            baseViewBinding.avatarMine.setOnClickListener(v -> itemClickListener.onSelfIconClick(v,position,currentMessage));

            baseViewBinding.messageContainer.setOnLongClickListener(v -> itemClickListener.onMessageLongClick(v,position, currentMessage));

            baseViewBinding.messageContainer.setOnClickListener(v -> itemClickListener.onMessageClick(v, position, currentMessage));

        }
    }

    public void addContainer(){}

    public void onDetachedFromWindow() {}

    public ViewGroup getContainer(){
        return baseViewBinding.messageContainer;
    }
}
