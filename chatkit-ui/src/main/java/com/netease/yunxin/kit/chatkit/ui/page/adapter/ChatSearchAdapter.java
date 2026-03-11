// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.adapter;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.PAYLOAD_PROGRESS;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.PAYLOAD_SIGNAL;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.PAYLOAD_USERINFO;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.ChatMessageType;
import com.netease.yunxin.kit.chatkit.ui.ChatViewHolderDefaultFactory;
import com.netease.yunxin.kit.chatkit.ui.IChatFactory;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
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
public class ChatSearchAdapter extends RecyclerView.Adapter<CommonBaseMessageViewHolder> {

  private final String TAG = "ChatMessageAdapter";
  IChatFactory viewHolderFactory;

  private IMessageItemClickListener itemClickListener;

  private MessageProperties messageProperties;

  public ChatSearchAdapter() {
    viewHolderFactory = ChatViewHolderDefaultFactory.getInstance();
  }

  private final List<ChatMessageBean> messageList = new ArrayList<>();
  private final Map<String, ChatMessageBean> messagesMap = new HashMap<>();
  private boolean showFooter = false;
  private boolean footerLoading = false;
  private static final int VIEW_TYPE_FOOTER = -1000;

  public void setItemClickListener(IMessageItemClickListener itemClickListener) {
    this.itemClickListener = itemClickListener;
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
      if (TextUtils.equals(clientId, messageList.get(i).getMessageData().getMessageClientId())) {
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

  @NonNull
  @Override
  public CommonBaseMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (viewType == VIEW_TYPE_FOOTER) {
      TextView tv = new TextView(parent.getContext());
      tv.setLayoutParams(
          new RecyclerView.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
      int padH = (int) (parent.getContext().getResources().getDisplayMetrics().density * 12);
      int padV = (int) (parent.getContext().getResources().getDisplayMetrics().density * 8);
      tv.setPadding(padH, padV, padH, padV);
      tv.setTextColor(parent.getContext().getResources().getColor(R.color.color_999999));
      tv.setGravity(Gravity.CENTER);
      return new FooterViewHolder(tv);
    }
    return viewHolderFactory.createViewHolder(parent, viewType);
  }

  @Override
  public void onBindViewHolder(
      @NonNull CommonBaseMessageViewHolder holder, int position, @NonNull List<Object> payloads) {
    if (payloads.isEmpty()) {
      super.onBindViewHolder(holder, position, payloads);
    } else {
      ChatMessageBean data = messageList.get(position);
      holder.setMode(ChatMessageType.ONLY_MESSAGE_MODE);
      holder.setItemClickListener(itemClickListener);
      holder.setProperties(messageProperties);
      holder.bindData(data, position, payloads);
    }
  }

  @Override
  public void onBindViewHolder(@NonNull CommonBaseMessageViewHolder holder, int position) {
    if (showFooter && position == messageList.size()) {
      // 动态更新 footer 文案
      if (holder instanceof FooterViewHolder) {
        ((FooterViewHolder) holder)
            .textView.setText(
                footerLoading
                    ? R.string.chat_list_loading_more_tips
                    : R.string.chat_list_no_more_tips);
      }
      return;
    }
    ChatMessageBean data = messageList.get(position);
    holder.setMode(ChatMessageType.ONLY_MESSAGE_MODE);
    holder.setItemClickListener(itemClickListener);
    holder.setProperties(messageProperties);
    holder.bindData(data, null);
  }

  @Override
  public int getItemCount() {
    return messageList.size() + (showFooter ? 1 : 0);
  }

  public void setViewHolderFactory(IChatFactory factory) {
    if (factory != null) {
      this.viewHolderFactory = factory;
    }
  }

  @Override
  public int getItemViewType(int position) {
    if (showFooter && position == messageList.size()) {
      return VIEW_TYPE_FOOTER;
    }
    return this.viewHolderFactory.getItemViewType(messageList.get(position));
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

  public void setMessagesData(List<ChatMessageBean> messages) {
    clearMessage();
    if (messages != null && messages.size() > 0) {
      messageList.addAll(messages);
      //刷新消息之间的时间是否需要展示
      ChatUtils.updateShowTimeText(messageList, 0);
      for (ChatMessageBean messageBean : messages) {
        messagesMap.put(messageBean.getMsgClientId(), messageBean);
      }
      notifyItemRangeInserted(0, messages.size());
    }
  }

  public void appendMessages(List<ChatMessageBean> messages) {
    if (messages == null || messages.size() < 1) {
      return;
    }
    int pos = messageList.size();
    messageList.addAll(messages);
    //刷新消息之间的时间是否需要展示
    ChatUtils.updateShowTimeText(messageList, pos);
    for (ChatMessageBean messageBean : messages) {
      messagesMap.put(messageBean.getMsgClientId(), messageBean);
    }
    notifyItemRangeInserted(pos, messages.size());
    if (pos + messages.size() < messageList.size()) {
      //刷新消息之间的时间是否需要展示
      notifyItemChanged(pos + messages.size());
    }
    if (showFooter) {
      notifyItemChanged(getItemCount() - 1);
    }
  }

  public void appendMessage(ChatMessageBean message) {
    int pos = messageList.size();
    int deletePos = getMessageIndex(message);
    messageList.add(message);
    //刷新消息之间的时间是否需要展示
    ChatUtils.updateShowTimeText(messageList, pos);
    messagesMap.put(message.getMsgClientId(), message);
    if (deletePos >= 0) {
      messageList.remove(deletePos);
      notifyItemRangeChanged(deletePos, pos - deletePos);
    } else {
      notifyItemInserted(pos);
    }
  }

  public void clearMessage() {
    int size = messageList.size();
    boolean hadFooter = showFooter;
    showFooter = false;
    footerLoading = false;
    messageList.clear();
    messagesMap.clear();
    notifyItemRangeRemoved(0, size + (hadFooter ? 1 : 0));
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

  public void updateMessage(ChatMessageBean message, Object payload) {
    int pos = getMessageIndex(message);
    if (pos >= 0) {
      messageList.set(pos, message);
      messagesMap.put(message.getMsgClientId(), message);
      notifyItemChanged(pos, payload);
    }
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

  public ChatMessageBean searchMessage(String messageId) {
    for (int i = messageList.size() - 1; i >= 0; i--) {
      if (TextUtils.equals(messageId, messageList.get(i).getMessageData().getMessageClientId())) {
        return messageList.get(i);
      }
    }
    return null;
  }

  public List<ChatMessageBean> getMessageList() {
    return messageList;
  }

  public void setShowFooter(boolean show) {
    if (this.showFooter != show) {
      this.showFooter = show;
      notifyDataSetChanged();
    }
  }

  public void setFooterLoading(boolean loading) {
    if (this.footerLoading != loading) {
      this.footerLoading = loading;
    }
    if (!this.showFooter) {
      this.showFooter = true;
    }
    notifyDataSetChanged();
  }

  static class FooterViewHolder extends CommonBaseMessageViewHolder {
    final TextView textView;

    public FooterViewHolder(@NonNull View itemView) {
      super(itemView);
      this.textView = (TextView) itemView;
    }
  }
}
