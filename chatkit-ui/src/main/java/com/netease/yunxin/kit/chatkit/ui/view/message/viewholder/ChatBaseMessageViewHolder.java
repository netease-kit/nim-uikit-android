package com.netease.yunxin.kit.chatkit.ui.view.message.viewholder;

import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

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
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatMessageRevokedViewBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.page.ChatMessageAckActivity;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageReader;
import com.netease.yunxin.kit.chatkit.ui.view.message.MessageProperties;
import com.netease.yunxin.kit.chatkit.ui.view.message.adapter.ChatMessageAdapter;
import com.netease.yunxin.kit.common.ui.message.MessageCommonBaseViewHolder;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.utils.TimeFormatUtils;
import com.netease.yunxin.kit.common.utils.ScreenUtil;
import com.netease.yunxin.kit.corekit.im.XKitImClient;

import java.util.List;

/**
 * base message view holder for chat message item
 */
public abstract class ChatBaseMessageViewHolder extends MessageCommonBaseViewHolder<ChatMessageBean> {

    private static final String LOG_TAG = "ChatBaseMessageViewHolder";

    private static final int SHOW_TIME_INTERVAL = 5 * 60 * 1000;

    private static final int MAX_RECEIPT_NUM = 100;

    public IMessageItemClickListener itemClickListener;

    public IMessageReader messageReader;

    public int type;

    public long receiptTime;

    public Team teamInfo;

    public ChatMessageBean currentMessage;

    MessageProperties properties = new MessageProperties();

    public ChatBaseMessageViewHolder(@NonNull ViewGroup parent, int viewType) {
        super(parent);
        this.type = viewType;
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

    @Override
    public void bindData(ChatMessageBean data, @NonNull List<?> payload) {
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
                }
            }
        }
    }

    protected void onMessageStatus(ChatMessageBean data) {

    }

    protected void onProgressUpdate(ChatMessageBean data) {

    }

    private void onMessageSignal(ChatMessageBean data) {
        if (!TextUtils.isEmpty(data.getPinAccid())) {
            getTvSignal().setVisibility(View.VISIBLE);
            if (data.getMessageData().getMessage().getSessionType() == SessionTypeEnum.P2P) {
                getTvSignal().setText(String.format(XKitImClient.getApplicationContext().getString(R.string.chat_message_signal_tip),
                        MessageHelper.getUserNickByAccId(data.getPinAccid(), true)));
            } else if (data.getMessageData().getMessage().getSessionType() == SessionTypeEnum.Team) {
                getTvSignal().setText(String.format(XKitImClient.getApplicationContext().getString(R.string.chat_message_signal_tip_for_team),
                        MessageHelper.getUserNickByAccId(data.getPinAccid(), true)));
            }
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) getTvSignal().getLayoutParams();
            if (isReceivedMessage(data)) {
                layoutParams.horizontalBias = 0f;
            } else {
                layoutParams.horizontalBias = 1f;
            }
            getBaseRoot().setBackgroundColor(getParent().getContext().getResources().getColor(R.color.color_fffbea));
        } else {
            getTvSignal().setVisibility(View.GONE);
            getBaseRoot().setBackgroundColor(getParent().getContext().getResources().getColor(R.color.title_transfer));
        }
    }

    private void onMessageRevoked(ChatMessageBean data) {
        if (!data.isRevoked()) {
            return;
        }
        getTvReply().setVisibility(View.GONE);
        getContainer().removeAllViews();
        ChatMessageRevokedViewBinding revokedViewBinding = ChatMessageRevokedViewBinding.inflate(LayoutInflater.from(getParent().getContext()),
                getContainer(), true);

        if (!isReceivedMessage(data) && data.getMessageData().getMessage().getMsgType() == MsgTypeEnum.text) {
            revokedViewBinding.tvAction.setVisibility(View.VISIBLE);
            //reedit
            revokedViewBinding.tvAction.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onReEditRevokeMessage(v, data);
                }
            });
        } else {
            revokedViewBinding.tvAction.setVisibility(View.GONE);
        }
    }

    @Override
    public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
        currentMessage = message;
        int padding = ScreenUtil.dip2px(8);
        getBaseRoot().setPadding(padding, padding, padding, padding);
        getContainer().removeAllViews();
        addContainer();
        if (type == ChatMessageType.NOTICE_MESSAGE_VIEW_TYPE || type == ChatMessageType.TIP_MESSAGE_VIEW_TYPE) {
            getFromAvatar().setVisibility(View.GONE);
            getMyAvatar().setVisibility(View.GONE);
            getMessageStatus().setVisibility(View.GONE);
            getTvTime().setVisibility(View.GONE);
            return;
        }
        if (message.getMessageData().getMessage().getThreadOption() != null) {
            setReplyInfo(message);
        } else {
            getTvReply().setVisibility(View.GONE);
        }
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) getMessageBody().getLayoutParams();
        if (isReceivedMessage(message)) {
            //get nick name
            String name = MessageHelper.getChatMessageUserName(message.getMessageData().getMessage());
            if (message.getMessageData().getMessage().getSessionType() == SessionTypeEnum.Team) {
                getTvName().setVisibility(View.VISIBLE);
                getTvName().setText(name);
                if (properties.getUserNickColor() != 0) {
                    getTvName().setTextColor(properties.getUserNickColor());
                }
                layoutParams.topToBottom = R.id.tv_name;
            }
            String avatar = message.getMessageData().getFromUser() == null ? "" : message.getMessageData().getFromUser().getAvatar();
            getFromAvatar().setVisibility(View.VISIBLE);
            getFromAvatar().setData(avatar, name, AvatarColor.avatarColor(message.getMessageData().getMessage().getFromAccount()));
            getMyAvatar().setVisibility(View.GONE);
            if (properties.getReceiveMessageBg() != null) {
                getLlyMessage().setBackground(properties.getReceiveMessageBg());
            } else {
                getLlyMessage().setBackgroundResource(R.drawable.chat_message_other_bg);
            }
            getMessageStatus().setVisibility(View.GONE);
            layoutParams.horizontalBias = 0f;
        } else {
            getMyAvatar().setVisibility(View.VISIBLE);
            getTvName().setVisibility(View.GONE);
            NimUserInfo userInfo = XKitImClient.getUserInfo();
            if (userInfo != null) {
                String nickname = userInfo.getName() == null ? "" : userInfo.getName();
                getMyAvatar().setData(userInfo.getAvatar(),
                        nickname,
                        AvatarColor.avatarColor(userInfo.getAccount()));
            }
            getFromAvatar().setVisibility(View.GONE);
            layoutParams.horizontalBias = 1f;
            if (properties.getSelfMessageBg() != null) {
                getLlyMessage().setBackground(properties.getSelfMessageBg());
            } else {
                getLlyMessage().setBackgroundResource(R.drawable.chat_message_self_bg);
            }

            getMessageStatus().setVisibility(View.VISIBLE);
            setStatus(message);
        }
        setTime(message, lastMessage);
        onMessageSignal(message);
        onMessageRevoked(message);
        setStatusCallback();
    }

    private void setReplyInfo(ChatMessageBean messageBean) {
        MsgThreadOption threadOption = messageBean.getMessageData().getMessage().getThreadOption();
        String replyFrom = threadOption.getReplyMsgFromAccount();
        if (TextUtils.isEmpty(replyFrom)) {
            ALog.w(LOG_TAG, "no reply message found, uuid=" + messageBean.getMessageData().getMessage().getUuid());
            getTvReply().setVisibility(View.GONE);
            return;
        }
        getTvReply().setVisibility(View.VISIBLE);

        String replyUuid = threadOption.getReplyMsgIdClient();
        String content = "| " + MessageHelper.getReplyMessageInfo(replyUuid);
        MessageUtil.identifyFaceExpression(getTvReply().getContext(),getTvReply(),content, ImageSpan.ALIGN_BOTTOM);
        if (itemClickListener != null) {
            getTvReply().setOnClickListener(v -> itemClickListener.onReplyMessageClick(v, replyUuid));
        }
    }

    private void setTime(ChatMessageBean message, ChatMessageBean lastMessage) {
        long createTime = message.getMessageData().getMessage().getTime() == 0 ? System.currentTimeMillis() : message.getMessageData().getMessage().getTime();
        if (lastMessage != null
                && createTime - lastMessage.getMessageData().getMessage().getTime() < SHOW_TIME_INTERVAL) {
            getTvTime().setVisibility(View.GONE);
        } else {
            getTvTime().setVisibility(View.VISIBLE);
            getTvTime().setText(TimeFormatUtils.formatMillisecond(itemView.getContext(), createTime));
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
            getMessageSending().setVisibility(View.VISIBLE);
            getIvStatus().setVisibility(View.GONE);
            getReadProcess().setVisibility(View.GONE);
        } else if ((data.getMessageData().getMessage().getStatus() == MsgStatusEnum.fail)) {
            getIvStatus().setVisibility(View.VISIBLE);
            getIvStatus().setImageResource(R.drawable.ic_icon_error);
            getMessageSending().setVisibility(View.GONE);
        } else if (data.getMessageData().getMessage().getSessionType() == SessionTypeEnum.P2P) {
            getMessageSending().setVisibility(View.GONE);
            getReadProcess().setVisibility(View.GONE);
            if (!data.getMessageData().getMessage().needMsgAck()) {
                getIvStatus().setVisibility(View.GONE);
            } else {
                getIvStatus().setVisibility(View.VISIBLE);
                if (data.getMessageData().getMessage().getTime() <= receiptTime ||
                        data.getMessageData().getMessage().isRemoteRead()) {
                    getIvStatus().setImageResource(R.drawable.ic_message_read);
                    data.setHaveRead(true);
                } else {
                    getIvStatus().setImageResource(R.drawable.ic_message_unread);
                }
            }
        } else if (data.getMessageData().getMessage().getSessionType() == SessionTypeEnum.Team) {
            getMessageSending().setVisibility(View.GONE);
            getIvStatus().setVisibility(View.GONE);
            if ((teamInfo != null && teamInfo.getMemberCount() >= MAX_RECEIPT_NUM) ||
                    !data.getMessageData().getMessage().needMsgAck()) {
                getReadProcess().setVisibility(View.GONE);
                return;
            }
            getReadProcess().setVisibility(View.VISIBLE);
            int ackCount = data.getMessageData().getMessage().getTeamMsgAckCount();
            int unAckCount = data.getMessageData().getMessage().getTeamMsgUnAckCount();
            float all = ackCount + unAckCount;
            if (all > 0) {
                float process = ackCount / all;
                if (process < 1) {
                    getReadProcess().setProcess(process);
                } else {
                    getIvStatus().setVisibility(View.VISIBLE);
                    getIvStatus().setImageResource(R.drawable.ic_message_read);
                    getReadProcess().setVisibility(View.GONE);
                }
            }
            getReadProcess().setOnClickListener(v -> {
                //goto ChatMessageReadStateActivity
                ChatMessageAckActivity.startMessageAckActivity(v.getContext(), data.getMessageData().getMessage());
            });

        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
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
            getIvStatus().setOnClickListener(v -> {
                if (currentMessage.getMessageData().getMessage().getStatus() == MsgStatusEnum.fail) {
                    itemClickListener.onSendFailBtnClick(v, currentMessage);
                }
            });

            getFromAvatar().setOnClickListener(v -> itemClickListener.onUserIconClick(v, currentMessage));

            getMyAvatar().setOnClickListener(v -> itemClickListener.onSelfIconClick(v));

            getContainer().setOnLongClickListener(v -> itemClickListener.onMessageLongClick(v, currentMessage));

            getContainer().setOnClickListener(v -> itemClickListener.onMessageClick(v, -1, currentMessage));

        }
    }
}
