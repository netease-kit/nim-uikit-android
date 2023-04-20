// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message.adapter;

import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.PAYLOAD_PROGRESS;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.PAYLOAD_REPLY;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.PAYLOAD_REVOKE;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.PAYLOAD_SIGNAL;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.PAYLOAD_STATUS;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.PAYLOAD_USERINFO;

import android.text.TextUtils;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.nimlib.sdk.msg.model.MsgPinOption;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.ChatDefaultFactory;
import com.netease.yunxin.kit.chatkit.ui.IChatFactory;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageReader;
import com.netease.yunxin.kit.chatkit.ui.view.message.MessageProperties;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatBaseMessageViewHolder;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** chat message adapter for message list */
public class ChatMessageAdapter extends RecyclerView.Adapter<ChatBaseMessageViewHolder> {

  IChatFactory viewHolderFactory;

  private IMessageItemClickListener itemClickListener;

  private long receiptTime;

  private Team teamInfo;

  private IMessageReader messageReader;

  private MessageProperties messageProperties;

  public ChatMessageAdapter() {
    viewHolderFactory = ChatDefaultFactory.getInstance();
  }

  private final List<ChatMessageBean> messageList = new ArrayList<>();

  public void setItemClickListener(IMessageItemClickListener itemClickListener) {
    this.itemClickListener = itemClickListener;
  }

  @NonNull
  @Override
  public ChatBaseMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return viewHolderFactory.createViewHolder(parent, viewType);
  }

  @Override
  public void onBindViewHolder(
      @NonNull ChatBaseMessageViewHolder holder, int position, @NonNull List<Object> payloads) {
    if (payloads.isEmpty()) {
      super.onBindViewHolder(holder, position, payloads);
    } else {
      ChatMessageBean data = messageList.get(position);
      holder.setReceiptTime(receiptTime);
      holder.setTeamInfo(teamInfo);
      holder.bindData(data, position, payloads);
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

  public void setViewHolderFactory(IChatFactory factory) {
    if (factory != null) {
      this.viewHolderFactory = factory;
    }
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
    removeSameMessage(message);
    int pos = messageList.size();
    messageList.addAll(message);
    notifyItemRangeInserted(pos, message.size());
    if (pos + message.size() < messageList.size()) {
      //刷新消息之间的时间是否需要展示
      notifyItemChanged(pos + message.size());
    }
  }

  private void removeSameMessage(List<ChatMessageBean> message) {
    if (message == null || message.size() < 1) {
      return;
    }
    for (ChatMessageBean bean : message) {
      int index = -1;
      for (int j = 0; j < messageList.size(); j++) {
        if (bean.isSameMessage(messageList.get(j))) {
          index = j;
          break;
        }
      }
      if (index > -1) {
        messageList.remove(index);
        notifyItemRemoved(index);
      }
    }
  }

  public void appendMessage(ChatMessageBean message) {
    int pos = messageList.size();
    int deletePos = getMessageIndex(message);
    messageList.add(message);
    if (deletePos >= 0) {
      messageList.remove(deletePos);
      notifyItemRangeChanged(deletePos, pos - deletePos);
    } else {
      notifyItemInserted(pos);
    }
  }

  public void clearMessageList() {
    int size = messageList.size();
    messageList.clear();
    notifyItemRangeRemoved(0, size);
  }

  public void updateMessageProgress(AttachmentProgress progress) {
    ChatMessageBean messageBean = searchMessage(progress.getUuid());
    if (messageBean != null) {
      float pg = progress.getTransferred() * 100f / progress.getTotal();
      if (progress.getTransferred() == progress.getTotal()) {
        pg = 100;
      }
      messageBean.progress = progress.getTransferred();
      messageBean.setLoadProgress(pg);
      updateMessage(messageBean, PAYLOAD_PROGRESS);
    }
  }

  public void updateMessageStatus(
      ChatMessageBean message, AdapterProcessCallback<Boolean> callback) {
    boolean process;
    int index = getMessageIndex(message);
    if (index >= 0 && index < messageList.size()) {
      ChatMessageBean origin = messageList.get(index);
      origin
          .getMessageData()
          .getMessage()
          .setStatus(message.getMessageData().getMessage().getStatus());
      updateMessage(origin, PAYLOAD_STATUS);
      process = false;
    } else {
      //音视频消息，服务端消息抄送下发到发送方，
      // 通过msgStatusObserver所以在状态变更也需要把不存在的消息添加到列表中
      appendMessage(message);
      process = true;
    }
    if (callback != null) {
      callback.onProcess(process);
    }
  }

  public void revokeMessage(ChatMessageBean messageBean) {
    messageBean.setRevoked(true);
    updateMessage(messageBean, PAYLOAD_REVOKE);
    clearReply(messageBean);
  }

  public void clearReply(ChatMessageBean messageBean) {
    String uuid = messageBean.getMessageData().getMessage().getUuid();
    if (TextUtils.isEmpty(uuid)) {
      return;
    }
    for (int i = 0; i < messageList.size(); i++) {
      ChatMessageBean messageInfo = messageList.get(i);
      if (messageInfo != null && TextUtils.equals(uuid, messageInfo.getReplyUUid())) {
        notifyItemChanged(i, PAYLOAD_REPLY);
      }
    }
  }

  public void updateUserInfo(List<UserInfo> userInfoList) {
    if (userInfoList == null || userInfoList.size() < 1) {
      return;
    }
    Map<String, UserInfo> accountSet = new HashMap<>();
    for (int index = 0; index < userInfoList.size(); index++) {
      accountSet.put(userInfoList.get(index).getAccount(), userInfoList.get(index));
    }
    for (int i = 0; i < messageList.size(); i++) {
      IMMessageInfo messageInfo = messageList.get(i).getMessageData();
      if (messageInfo != null
          && accountSet.containsKey(messageInfo.getMessage().getFromAccount())) {
        notifyItemChanged(i, PAYLOAD_USERINFO);
      }
    }
  }

  public void notifyUserInfoChange(List<String> userInfoList) {
    if (userInfoList == null || userInfoList.size() < 1) {
      return;
    }
    Set<String> accountSet = new HashSet<>(userInfoList);

    for (int i = 0; i < messageList.size(); i++) {
      IMMessageInfo messageInfo = messageList.get(i).getMessageData();
      if (messageInfo != null && accountSet.contains(messageInfo.getMessage().getFromAccount())) {
        notifyItemChanged(i, PAYLOAD_USERINFO);
      }
    }
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
      updateMessage(messageList.get(index), PAYLOAD_SIGNAL);
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
      updateMessage(messageList.get(index), PAYLOAD_SIGNAL);
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

    notifyItemRangeChanged(start, messageList.size() - start, PAYLOAD_STATUS);
  }

  private int getMessageIndex(ChatMessageBean message) {
    if (message == null) {
      return -1;
    }
    for (int index = 0; index < messageList.size(); index++) {
      if (message.isSameMessage(messageList.get(index))) {
        return index;
      }
    }
    return -1;
  }

  public void forwardMessages(List<ChatMessageBean> message) {
    removeSameMessage(message);
    messageList.addAll(0, message);
    notifyItemRangeInserted(0, message.size());
    if (messageList.size() > message.size()) {
      notifyItemChanged(message.size());
    }
  }

  public ChatMessageBean getFirstMessage() {
    if (messageList.isEmpty()) {
      return null;
    }
    return messageList.get(0);
  }

  public ChatMessageBean getLastMessage() {
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
    clearReply(message);
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
    for (int i = 0; i < messageList.size(); i++) {
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

  public interface AdapterProcessCallback<T> {
    void onProcess(T data);
  }
}
