package com.netease.yunxin.kit.chatkit.ui.view.message.adapter;

import android.text.TextUtils;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.nimlib.sdk.msg.model.MsgPinOption;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageReader;
import com.netease.yunxin.kit.chatkit.ui.view.message.ChatMessageViewHolderFactory;
import com.netease.yunxin.kit.chatkit.ui.view.message.MessageProperties;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatBaseMessageViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * chat message adapter for message list
 */
public class ChatMessageAdapter extends RecyclerView.Adapter<ChatBaseMessageViewHolder> {

    public static final String STATUS_PAYLOAD = "messageStatus";
    public static final String PROGRESS_PAYLOAD = "messageProgress";
    public static final String REVOKE_PAYLOAD = "messageRevoke";
    public static final String SIGNAL_PAYLOAD = "messageSignal";

    ChatMessageViewHolderFactory viewHolderFactory;

    private IMessageItemClickListener itemClickListener;

    private long receiptTime;

    private Team teamInfo;

    private IMessageReader messageReader;

    private MessageProperties messageProperties;

    public ChatMessageAdapter(ChatMessageViewHolderFactory viewHolderFactory) {
        this.viewHolderFactory = viewHolderFactory;
    }

    private final List<ChatMessageBean> messageList = new ArrayList<>();

    public void setItemClickListener(IMessageItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ChatBaseMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return viewHolderFactory.getViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatBaseMessageViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            ChatMessageBean data = messageList.get(position);
            holder.setReceiptTime(receiptTime);
            holder.setTeamInfo(teamInfo);
            holder.bindData(data, payloads);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChatBaseMessageViewHolder holder, int position) {
        ChatMessageBean data = messageList.get(position);
        ChatMessageBean lastMessage = null;
        if (position - 1 >= 0) {
            lastMessage = messageList.get(position - 1);
        }
        holder.setTeamInfo(teamInfo);
        holder.setItemClickListener(itemClickListener);
        holder.setMessageReader(messageReader);
        holder.setProperties(messageProperties);
        holder.bindData(data, lastMessage);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void setViewHolderFactory(ChatMessageViewHolderFactory viewHolderFactory) {
        this.viewHolderFactory = viewHolderFactory;
    }

    public void setMessageReader(IMessageReader messageReader) {
        this.messageReader = messageReader;
    }

    @Override
    public int getItemViewType(int position) {
        return this.viewHolderFactory.getItemViewType(messageList.get(position));
    }

    public void setTeamInfo(Team teamInfo) {
        this.teamInfo = teamInfo;
    }

    public void setMessageProperties(MessageProperties messageProperties) {
        this.messageProperties = messageProperties;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ChatBaseMessageViewHolder holder) {
        holder.onAttachedToWindow();
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ChatBaseMessageViewHolder holder) {
        holder.onDetachedFromWindow();
        super.onViewDetachedFromWindow(holder);
    }


    public void appendMessages(List<ChatMessageBean> message) {
        int pos = messageList.size();
        messageList.addAll(message);
        notifyItemRangeInserted(pos, message.size());
    }

    public void appendMessage(ChatMessageBean message) {
        int pos = messageList.size();
        messageList.add(message);
        notifyItemInserted(pos);
    }

    public void clearMessageList() {
        int size = messageList.size();
        messageList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void updateMessageProgress(AttachmentProgress progress) {
        ChatMessageBean messageBean = searchMessage(progress.getUuid());
        if (messageBean != null) {
            messageBean.setLoadProgress(progress.getTransferred() * 100f / progress.getTotal());
            updateMessage(messageBean, PROGRESS_PAYLOAD);
        }
    }

    public void updateMessageStatus(ChatMessageBean message) {
        updateMessage(message, STATUS_PAYLOAD);
    }

    public void revokeMessage(ChatMessageBean messageBean) {
        messageBean.setRevoked(true);
        updateMessage(messageBean, REVOKE_PAYLOAD);
    }

    public void pinMsg(String uuid, MsgPinOption pinOption) {
        int index = -1;
        for (int i = 0; i < messageList.size(); i++) {
            if (TextUtils.equals(messageList.get(i).getMessageData().getMessage().getUuid(), uuid)) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            messageList.get(index).setPinAccid(pinOption);
            updateMessage(messageList.get(index), SIGNAL_PAYLOAD);
        }
    }

    public void removeMessagePin(String uuid) {
        int index = -1;
        for (int i = 0; i < messageList.size(); i++) {
            if (TextUtils.equals(messageList.get(i).getMessageData().getMessage().getUuid(), uuid)) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            messageList.get(index).setPinAccid(null);
            updateMessage(messageList.get(index), SIGNAL_PAYLOAD);
        }
    }

    public void updateMessage(ChatMessageBean message, Object payload) {
        int pos = getMessageIndex(message);
        if (pos >= 0) {
            messageList.set(pos, message);
            notifyItemChanged(pos, payload);
        }
    }

    public void setReceiptTime(long receiptTime) {
        this.receiptTime = receiptTime;
        int start = messageList.size() - 1;
        while (start > 0 && !messageList.get(start).isHaveRead()) {
            start--;
        }

        notifyItemRangeChanged(start, messageList.size() - start, STATUS_PAYLOAD);
    }

    private int getMessageIndex(ChatMessageBean message) {
        return messageList.indexOf(message);
    }

    public void forwardMessages(List<ChatMessageBean> message) {
        messageList.addAll(0, message);
        notifyItemRangeInserted(0, message.size());
    }

    public ChatMessageBean getFirstMessage() {
        if (messageList.isEmpty()) {
            return null;
        }
        return messageList.get(0);
    }

    public ChatMessageBean getlastMessage() {
        if (messageList.isEmpty()) {
            return null;
        }
        return messageList.get(messageList.size() - 1);
    }

    public void removeMessage(ChatMessageBean message) {
        int pos = messageList.indexOf(message);
        if (pos >= 0) {
            messageList.remove(message);
            notifyItemRemoved(pos);
        }
    }

    public ChatMessageBean searchMessage(String messageId) {
        for (int i = messageList.size() - 1; i >= 0; i--) {
            if (TextUtils.equals(messageId, messageList.get(i).getMessageData().getMessage().getUuid())) {
                return messageList.get(i);
            }
        }
        return null;
    }

    public int searchMessagePosition(String messageId) {
        for (int i = 0; i <  messageList.size() ; i++) {
            if (TextUtils.equals(messageId, messageList.get(i).getMessageData().getMessage().getUuid())) {
                return i;
            }
        }
        return -1;
    }

    public List<ChatMessageBean> getMessageList() {
        return messageList;
    }

    public interface EndItemBindingListener {
        void onEndItemBinding();
    }
}
