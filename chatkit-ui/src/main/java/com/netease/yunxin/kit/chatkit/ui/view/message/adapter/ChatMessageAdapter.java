// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message.adapter;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.PAYLOAD_PROGRESS;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.PAYLOAD_REPLY;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.PAYLOAD_SIGNAL;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.PAYLOAD_STATUS;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.PAYLOAD_USERINFO;

import android.text.TextUtils;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.nimlib.sdk.v2.message.V2NIMMessagePin;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageRefer;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.model.MessagePinInfo;
import com.netease.yunxin.kit.chatkit.ui.ChatMessageType;
import com.netease.yunxin.kit.chatkit.ui.ChatViewHolderDefaultFactory;
import com.netease.yunxin.kit.chatkit.ui.IChatFactory;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageReader;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.textSelectionHelper.SelectableTextHelper;
import com.netease.yunxin.kit.chatkit.ui.view.message.MessageProperties;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.CommonBaseMessageViewHolder;
import com.netease.yunxin.kit.corekit.im2.model.IMMessageProgress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** chat message adapter for message list */
public class ChatMessageAdapter extends RecyclerView.Adapter<CommonBaseMessageViewHolder> {

  private final String TAG = "ChatMessageAdapter";
  IChatFactory viewHolderFactory;

  private IMessageItemClickListener itemClickListener;

  private long receiptTime;

  private V2NIMTeam teamInfo;

  private IMessageReader messageReader;

  private MessageProperties messageProperties;

  private boolean multiSelect;

  private int msgModel;

  public ChatMessageAdapter() {
    viewHolderFactory = ChatViewHolderDefaultFactory.getInstance();
  }

  private final List<ChatMessageBean> messageList = new ArrayList<>();
  private final Map<String, ChatMessageBean> messagesMap = new HashMap<>();

  public void setItemClickListener(IMessageItemClickListener itemClickListener) {
    this.itemClickListener = itemClickListener;
  }

  public void setMultiSelect(boolean select) {
    if (multiSelect == select) {
      return;
    }
    multiSelect = select;
    notifyDataSetChanged();
  }

  public void setMessageMode(int mode) {
    this.msgModel = mode;
  }

  @NonNull
  @Override
  public CommonBaseMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return viewHolderFactory.createViewHolder(parent, viewType);
  }

  @Override
  public void onBindViewHolder(
      @NonNull CommonBaseMessageViewHolder holder, int position, @NonNull List<Object> payloads) {
    if (payloads.isEmpty()) {
      super.onBindViewHolder(holder, position, payloads);
    } else {
      ChatMessageBean data = messageList.get(position);
      holder.setMode(msgModel);
      if (msgModel != ChatMessageType.FORWARD_MESSAGE_MODE) {
        holder.setMode(ChatMessageType.CHAT_MESSAGE_MODE);
        holder.setReceiptTime(receiptTime);
        holder.setTeamInfo(teamInfo);
        holder.setMultiSelect(multiSelect);
      }
      holder.bindData(data, position, payloads);
    }
  }

  @Override
  public void onBindViewHolder(@NonNull CommonBaseMessageViewHolder holder, int position) {
    ChatMessageBean data = messageList.get(position);
    ChatMessageBean lastMessage = null;
    if (position - 1 >= 0) {
      lastMessage = messageList.get(position - 1);
    }
    holder.setMode(msgModel);
    if (msgModel != ChatMessageType.FORWARD_MESSAGE_MODE) {
      holder.setTeamInfo(teamInfo);
      holder.setMessageReader(messageReader);
      holder.setMultiSelect(multiSelect);
    }
    holder.setItemClickListener(itemClickListener);
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

  public void setTeamInfo(V2NIMTeam teamInfo) {
    this.teamInfo = teamInfo;
  }

  public void setMessageProperties(MessageProperties messageProperties) {
    this.messageProperties = messageProperties;
  }

  @Override
  public void onViewAttachedToWindow(@NonNull CommonBaseMessageViewHolder holder) {
    holder.onAttachedToWindow();
    super.onViewAttachedToWindow(holder);
  }

  @Override
  public void onViewDetachedFromWindow(@NonNull CommonBaseMessageViewHolder holder) {
    holder.onDetachedFromWindow();
    super.onViewDetachedFromWindow(holder);
  }

  public void appendMessages(List<ChatMessageBean> messages) {
    if (messages == null || messages.size() < 1) {
      return;
    }
    removeSameMessage(messages);
    int pos = messageList.size();
    messageList.addAll(messages);
    for (ChatMessageBean messageBean : messages) {
      messagesMap.put(messageBean.getMsgClientId(), messageBean);
    }
    loadReplyInfo(messages);
    notifyItemRangeInserted(pos, messages.size());
    if (pos + messages.size() < messageList.size()) {
      //刷新消息之间的时间是否需要展示
      notifyItemChanged(pos + messages.size());
    }
  }

  private void loadReplyInfo(List<ChatMessageBean> messages) {
    for (ChatMessageBean messageBean : messages) {
      if (messageBean.hasReply()
          && messageBean.getReplyMessageRefer() != null
          && messageBean.getReplyMessage() == null
          && messagesMap.containsKey(messageBean.getReplyMessageRefer().getMessageClientId())) {
        messageBean.setReplyMessage(
            messagesMap
                .get(messageBean.getReplyMessageRefer().getMessageClientId())
                .getMessageData());
      }
    }
  }

  private void loadReplyInfo(ChatMessageBean message) {
    if (message.hasReply()
        && message.getReplyMessageRefer() != null
        && message.getReplyMessage() == null
        && messagesMap.containsKey(message.getReplyMessageRefer().getMessageClientId())) {
      message.setReplyMessage(
          messagesMap.get(message.getReplyMessageRefer().getMessageClientId()).getMessageData());
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
    messagesMap.put(message.getMsgClientId(), message);
    loadReplyInfo(message);
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
    messagesMap.clear();
    notifyItemRangeRemoved(0, size);
  }

  public void updateMessageProgress(IMMessageProgress progress) {
    ChatMessageBean messageBean = searchMessage(progress.getMessageCid());
    if (messageBean != null) {
      float pg = progress.getProgress();
      messageBean.progress = progress.getProgress();
      messageBean.setLoadProgress(pg);
      updateMessage(messageBean, PAYLOAD_PROGRESS);
    }
  }

  public int updateMessageStatus(ChatMessageBean message) {
    int index = getMessageIndex(message);
    ALog.d(LIB_TAG, TAG, "updateMessageStatus,index:" + String.valueOf(index));
    if (index >= 0 && index < messageList.size()) {
      ChatMessageBean origin = messageList.get(index);
      origin.setMessageData(message.getMessageData());
      updateMessage(origin, PAYLOAD_STATUS);
    } else {
      insertMessageSortByTime(message);
    }
    return index;
  }

  public void reloadMessages(List<ChatMessageBean> messageList, Object payload) {
    ALog.d(LIB_TAG, TAG, "reloadMessages");
    for (ChatMessageBean message : messageList) {
      int index = getMessageIndex(message);
      if (index >= 0 && index < this.messageList.size()) {
        notifyItemChanged(index, payload);
      }
    }
  }

  public void revokeMessage(V2NIMMessageRefer message) {
    ALog.d(LIB_TAG, TAG, "revokeMessage");
    removeMessageByClientId(message.getMessageClientId());
    clearReply(message.getMessageClientId());
    //撤回消息后，清除选中的消息
    ChatMessageBean selectedMessage = SelectableTextHelper.getInstance().getMessage();
    if (selectedMessage != null) {
      if (TextUtils.equals(selectedMessage.getMsgClientId(), message.getMessageClientId())) {
        SelectableTextHelper.getInstance().dismiss();
      }
    }
  }

  public void clearReply(String clientId) {
    if (TextUtils.isEmpty(clientId)) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "clearReply");

    for (int i = 0; i < messageList.size(); i++) {
      ChatMessageBean messageInfo = messageList.get(i);
      if (messageInfo != null
          && messageInfo.getReplyMessageRefer() != null
          && TextUtils.equals(clientId, messageInfo.getReplyMessageRefer().getMessageClientId())) {
        messageInfo.setReplyMessage(null);
        loadReplyInfo(messageInfo);
        notifyItemChanged(i, PAYLOAD_REPLY);
      }
    }
  }

  public void updateUserInfo(List<UserInfo> userInfoList) {
    if (userInfoList == null || userInfoList.size() < 1) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "updateUserInfo");

    Map<String, UserInfo> accountSet = new HashMap<>();
    for (int index = 0; index < userInfoList.size(); index++) {
      accountSet.put(userInfoList.get(index).getAccount(), userInfoList.get(index));
    }
    for (int i = 0; i < messageList.size(); i++) {
      IMMessageInfo messageInfo = messageList.get(i).getMessageData();
      if (messageInfo != null && accountSet.containsKey(messageInfo.getMessage().getSenderId())) {
        notifyItemChanged(i, PAYLOAD_USERINFO);
      }
    }
  }

  public void notifyUserInfoChange(List<String> userInfoList) {
    if (userInfoList == null || userInfoList.size() < 1) {
      return;
    }
    Set<String> accountSet = new HashSet<>(userInfoList);
    ALog.d(LIB_TAG, TAG, "notifyUserInfoChange");

    for (int i = 0; i < messageList.size(); i++) {
      IMMessageInfo messageInfo = messageList.get(i).getMessageData();
      if (messageInfo != null) {
        if (accountSet.contains(MessageHelper.getRealMessageSenderId(messageInfo.getMessage()))) {
          notifyItemChanged(i, PAYLOAD_USERINFO);
        }
        if (messageInfo.getPinOption() != null
            && accountSet.contains(messageInfo.getPinOption().getOperatorId())) {
          notifyItemChanged(i, PAYLOAD_SIGNAL);
        }
        //通知消息可能涉及用户名称，需要更新内容
        if (messageInfo.getMessage().getMessageType()
            == V2NIMMessageType.V2NIM_MESSAGE_TYPE_NOTIFICATION) {
          notifyItemChanged(i);
        }
      }
    }
  }

  public void pinMsg(String uuid, V2NIMMessagePin pinOption) {
    int index = -1;
    for (int i = 0; i < messageList.size(); i++) {
      if (TextUtils.equals(
          messageList.get(i).getMessageData().getMessage().getMessageClientId(), uuid)) {
        index = i;
        break;
      }
    }
    if (index != -1) {
      messageList.get(index).setPinAccid(pinOption);
      updateMessage(messageList.get(index), PAYLOAD_SIGNAL);
    }
  }

  public void updateMessagePin(Map<String, V2NIMMessagePin> pinOptionMap) {
    ALog.d(LIB_TAG, TAG, "updateMessagePin");
    for (int i = 0; i < messageList.size(); i++) {
      IMMessageInfo messageInfo = messageList.get(i).getMessageData();
      String uuId = messageInfo.getMessage().getMessageClientId();
      if (pinOptionMap != null && pinOptionMap.containsKey(uuId)) {
        V2NIMMessagePin pin = pinOptionMap.get(uuId);
        if (pin != null) {
          messageInfo.setPinOption(new MessagePinInfo(pin));
        } else {
          messageInfo.setPinOption(null);
        }
        notifyItemChanged(i, PAYLOAD_SIGNAL);
      } else if (messageInfo.getPinOption() != null
          && (pinOptionMap == null || !pinOptionMap.containsKey(uuId))) {
        messageInfo.setPinOption(null);
        notifyItemChanged(i, PAYLOAD_SIGNAL);
      }
    }
  }

  public void removeMessagePin(String uuid) {
    int index = -1;
    for (int i = 0; i < messageList.size(); i++) {
      if (TextUtils.equals(
          messageList.get(i).getMessageData().getMessage().getMessageClientId(), uuid)) {
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
      messagesMap.put(message.getMsgClientId(), message);
      notifyItemChanged(pos, payload);
    }
  }

  /**
   * 根据时间排序插入消息
   *
   * @param message 消息体
   * @return 插入的位置
   */
  public int insertMessageSortByTime(ChatMessageBean message) {
    int index = -1;
    if (message == null) {
      return index;
    }

    for (int i = 0; i < messageList.size(); i++) {
      if (message.getMessageData().getMessage().getCreateTime()
          < messageList.get(i).getMessageData().getMessage().getCreateTime()) {
        index = i;
        break;
      }
    }
    messagesMap.put(message.getMsgClientId(), message);
    loadReplyInfo(message);
    if (index >= 0) {
      messageList.add(index, message);
      notifyItemInserted(index);
    } else {
      messageList.add(message);
      notifyItemInserted(messageList.size() - 1);
    }
    return index;
  }

  /**
   * 通过client id删除消息
   *
   * @param clientId client id
   */
  public void removeMessageByClientId(String clientId) {
    if (clientId == null) {
      return;
    }
    int index = -1;
    for (int i = 0; i < messageList.size(); i++) {
      if (TextUtils.equals(
          clientId, messageList.get(i).getMessageData().getMessage().getMessageClientId())) {
        index = i;
        break;
      }
    }
    if (index >= 0) {
      messageList.remove(index);
      messagesMap.remove(clientId);
      notifyItemRemoved(index);
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
    for (ChatMessageBean messageBean : message) {
      messagesMap.put(messageBean.getMsgClientId(), messageBean);
    }
    loadReplyInfo(message);
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
      messagesMap.remove(message.getMsgClientId());
      notifyItemRemoved(pos);
    }
    clearReply(message.getMsgClientId());
  }

  public ChatMessageBean searchMessage(String messageId) {
    for (int i = messageList.size() - 1; i >= 0; i--) {
      if (TextUtils.equals(
          messageId, messageList.get(i).getMessageData().getMessage().getMessageClientId())) {
        return messageList.get(i);
      }
    }
    return null;
  }

  public int searchMessagePosition(String messageId) {
    for (int i = 0; i < messageList.size(); i++) {
      if (TextUtils.equals(
          messageId, messageList.get(i).getMessageData().getMessage().getMessageClientId())) {
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
