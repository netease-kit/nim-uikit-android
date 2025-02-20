// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message;

import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.PAYLOAD_SELECT_STATUS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMMessagePin;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageRefer;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.IChatFactory;
import com.netease.yunxin.kit.chatkit.ui.factory.ChatPopActionFactory;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageData;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageLoadHandler;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageReader;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.adapter.ChatMessageAdapter;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.IChatPopMenu;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.IChatPopMenuClickListener;
import com.netease.yunxin.kit.common.utils.BarUtils;
import com.netease.yunxin.kit.corekit.im2.model.IMMessageProgress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** recycler view for show message */
public class ChatMessageListView extends RecyclerView implements IMessageData {

  private final String TAG = "ChatMessageListView";
  private IMessageItemClickListener itemClickListener;

  private ChatMessageAdapter messageAdapter;

  private IMessageLoadHandler loadHandler;

  private IMessageReader messageReader;

  private OnListViewEventListener onListViewEventListener;
  private GestureDetector gestureDetector;
  private boolean isScroll = false;

  private boolean hasMoreForwardMessages;

  private boolean hasMoreNewerMessages;

  public ChatMessageListView(@NonNull Context context) {
    super(context);
    initView(null);
  }

  public ChatMessageListView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(attrs);
  }

  public ChatMessageListView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initView(attrs);
  }

  private void initView(AttributeSet attrs) {
    setFocusableInTouchMode(false);
    setFocusable(true);
    setClickable(true);
    initRecyclerView();
    gestureDetector =
        new GestureDetector(
            getContext(),
            new GestureDetector.SimpleOnGestureListener() {
              @Override
              public boolean onSingleTapUp(MotionEvent e) {
                if (!isScroll) {
                  if (onListViewEventListener != null) {
                    onListViewEventListener.onListViewStartScroll();
                  }
                  isScroll = true;
                }
                return true;
              }

              @Override
              public boolean onScroll(
                  MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (!isScroll) {
                  if (onListViewEventListener != null) {
                    onListViewEventListener.onListViewStartScroll();
                  }
                  isScroll = true;
                }
                return true;
              }
            });
  }

  private void initRecyclerView() {
    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
    layoutManager.setStackFromEnd(true);
    setLayoutManager(layoutManager);
    setItemAnimator(null);
    messageAdapter = new ChatMessageAdapter();
    setAdapter(messageAdapter);
  }

  //设置列表RecyclerView的数据加载停靠位置
  public void setStackFromEnd(boolean stackFromEnd) {
    LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
    if (layoutManager != null) {
      layoutManager.setStackFromEnd(stackFromEnd);
    }
  }

  /**
   * should set before add data
   *
   * @param viewHolderFactory your factory
   */
  public void setViewHolderFactory(IChatFactory viewHolderFactory) {
    if (viewHolderFactory == null) {
      return;
    }
    if (messageAdapter != null) {
      messageAdapter.setViewHolderFactory(viewHolderFactory);
    }
  }

  public void setOnListViewEventListener(OnListViewEventListener onListViewEventListener) {
    this.onListViewEventListener = onListViewEventListener;
  }

  public IMessageItemClickListener getItemClickListener() {
    return itemClickListener;
  }

  public void setItemClickListener(IMessageItemClickListener itemClickListener) {
    this.itemClickListener = itemClickListener;
    if (messageAdapter != null) {
      messageAdapter.setItemClickListener(itemClickListener);
    }
  }

  public void setPopActionListener(IChatPopMenuClickListener listener) {
    ChatPopActionFactory.getInstance().setActionListener(listener);
  }

  public void setChatPopMenu(IChatPopMenu popAction) {
    ChatPopActionFactory.getInstance().setChatPopMenu(popAction);
  }

  public void setMessageReader(IMessageReader messageReader) {
    this.messageReader = messageReader;
    if (messageAdapter != null) {
      messageAdapter.setMessageReader(messageReader);
    }
  }

  public ChatMessageAdapter getMessageAdapter() {
    return messageAdapter;
  }

  @Override
  public void clearMessageList() {
    messageAdapter.clearMessageList();
  }

  @Override
  public void addMessageListForward(List<ChatMessageBean> messageList) {
    if (messageAdapter != null) {
      messageAdapter.forwardMessages(messageList);
    }
  }

  public void setMessageProperties(MessageProperties messageProperties) {
    if (messageAdapter != null) {
      messageAdapter.setMessageProperties(messageProperties);
    }
  }

  @Override
  public void appendMessageList(List<ChatMessageBean> messageList) {
    appendMessageList(messageList, true);
  }

  @Override
  public void appendMessageList(List<ChatMessageBean> messageList, boolean needToScrollEnd) {
    if (messageAdapter != null) {
      messageAdapter.appendMessages(messageList);
      if (needToScrollEnd) {
        scrollToEnd();
      }
    }
  }

  @Override
  public void appendMessage(ChatMessageBean message) {
    if (messageAdapter != null) {
      messageAdapter.appendMessage(message);
      scrollToEnd();
    }
  }

  @Override
  public void updateMessageStatus(ChatMessageBean message) {
    if (messageAdapter != null) {
      int index = messageAdapter.updateMessageStatus(message);
      if (index < 0) {
        scrollToEnd();
      }
    }
  }

  @Override
  public void updateMessage(ChatMessageBean message, Object payload) {
    if (messageAdapter != null) {
      messageAdapter.updateMessage(message, payload);
    }
  }

  @Override
  public void updateMessage(V2NIMMessage message, Object payload) {
    if (messageAdapter != null && message != null) {
      String uuid = message.getMessageClientId();
      ChatMessageBean messageBean = messageAdapter.searchMessage(uuid);
      messageBean.setMessageData(new IMMessageInfo(message));
      messageAdapter.updateMessage(messageBean, payload);
    }
  }

  public void updateUserInfo(List<UserInfo> userInfoList) {
    if (messageAdapter != null) {
      messageAdapter.updateUserInfo(userInfoList);
    }
  }

  public void notifyUserInfoChanged(List<String> userInfoList) {
    if (messageAdapter != null) {
      messageAdapter.notifyUserInfoChange(userInfoList);
    }
  }

  @Override
  public void revokeMessage(V2NIMMessageRefer message) {
    if (messageAdapter != null) {
      messageAdapter.revokeMessage(message);
    }
  }

  /**
   * 插入消息
   *
   * @param message 消息
   */
  public void insertMessage(ChatMessageBean message) {
    if (messageAdapter != null) {
      int messageIndex = messageAdapter.searchMessagePosition(message.getMsgClientId());
      // 失败重发，如果想要让重发消息展示在最下方，需要再这里remove，然后insertMessageSortByTime
      if (messageIndex >= 0) {
        messageAdapter.updateMessage(message, null);
        return;
      }
      int index = messageAdapter.insertMessageSortByTime(message);
      if (index == -1) {
        scrollToEnd();
      }
    }
  }

  /**
   * 根据ClientID移除消息
   *
   * @param clientId 本地ID
   */
  public void removeMessageByClientId(String clientId) {
    if (messageAdapter != null) {
      messageAdapter.removeMessageByClientId(clientId);
    }
  }

  @Override
  public void addPinMessage(String uuid, V2NIMMessagePin pinOption) {
    if (messageAdapter != null) {
      messageAdapter.pinMsg(uuid, pinOption);
    }
  }

  public void updateMessagePin(Map<String, V2NIMMessagePin> pinOptionMap) {
    if (messageAdapter != null) {
      messageAdapter.updateMessagePin(pinOptionMap);
    }
  }

  @Override
  public void removePinMessage(String uuid) {
    if (messageAdapter != null) {
      messageAdapter.removeMessagePin(uuid);
    }
  }

  @Override
  public void deleteMessage(ChatMessageBean message) {
    if (messageAdapter != null) {
      messageAdapter.removeMessage(message);
    }
  }

  @Override
  public void deleteMessage(List<ChatMessageBean> message) {
    if (messageAdapter != null && message != null && !message.isEmpty()) {
      for (ChatMessageBean msg : message) {
        messageAdapter.removeMessage(msg);
      }
    }
  }

  /**
   * 根据clientId删除消息
   *
   * @param clientIds 消息clientId
   */
  public void deleteMessages(List<String> clientIds) {
    if (messageAdapter != null && clientIds != null && !clientIds.isEmpty()) {
      for (String clientId : clientIds) {
        messageAdapter.removeMessageByClientId(clientId);
        messageAdapter.clearReply(clientId);
      }
    }
  }

  public void updateTeamInfo(V2NIMTeam teamInfo) {
    if (messageAdapter != null) {
      messageAdapter.setTeamInfo(teamInfo);
    }
  }

  public void setP2PReceipt(long receipt) {
    messageAdapter.setReceiptTime(receipt);
  }

  public void updateAttachmentProgress(IMMessageProgress progress) {
    messageAdapter.updateMessageProgress(progress);
  }

  public void setLoadHandler(IMessageLoadHandler loadHandler) {
    this.loadHandler = loadHandler;
  }

  public void scrollToEnd() {
    if (messageAdapter != null) {
      int itemCount = messageAdapter.getItemCount();
      if (itemCount > 0) {
        post(() -> scrollToPosition(itemCount - 1));
      }
    }
  }

  public ChatMessageBean searchMessage(String messageId) {
    if (messageAdapter != null) {
      return messageAdapter.searchMessage(messageId);
    }
    return null;
  }

  public int searchMessagePosition(String messageId) {
    if (messageAdapter != null) {
      return messageAdapter.searchMessagePosition(messageId);
    }
    return -1;
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(MotionEvent e) {
    gestureDetector.onTouchEvent(e);

    if (e.getAction() == MotionEvent.ACTION_CANCEL || e.getAction() == MotionEvent.ACTION_UP) {
      isScroll = false;
    }
    if (onListViewEventListener != null) {
      onListViewEventListener.onListViewTouched();
    }
    return super.onTouchEvent(e);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    if (Math.abs(oldh - h)
        > BarUtils.getStatusBarHeight(getContext()) + BarUtils.getNavBarHeight(getContext())) {
      if (hasMoreNewerMessages) {
        scrollBy(0, oldh - h);
      } else {
        scrollToEnd();
      }
    }
    super.onSizeChanged(w, h, oldw, oldh);
  }

  @Override
  public void onScrollStateChanged(int state) {
    super.onScrollStateChanged(state);
    if (state == RecyclerView.SCROLL_STATE_IDLE) {
      if (loadHandler != null && getLayoutManager() != null) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
        int firstPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        int lastPosition = layoutManager.findLastCompletelyVisibleItemPosition();
        if (firstPosition == 0 && hasMoreForwardMessages) {
          loadHandler.loadMoreForward(messageAdapter.getFirstMessage());
        } else if (isLastItemVisibleCompleted() && hasMoreNewerMessages) {
          loadHandler.loadMoreBackground(messageAdapter.getLastMessage());
        }
      }
      refreshTeamMessageReceipt();
      if (onListViewEventListener != null) {
        onListViewEventListener.onListViewScrollEnd();
      }
    } else {
      if (onListViewEventListener != null) {
        onListViewEventListener.onListViewScrolling();
      }
    }
  }

  @Override
  public void setHasMoreNewerMessages(boolean hasMoreNewerMessages) {
    this.hasMoreNewerMessages = hasMoreNewerMessages;
  }

  @Override
  public void setHasMoreForwardMessages(boolean hasMoreForwardMessages) {
    this.hasMoreForwardMessages = hasMoreForwardMessages;
  }

  @Override
  public void setMultiSelect(boolean multiSelect) {
    if (messageAdapter != null) {
      messageAdapter.setMultiSelect(multiSelect);
    }
  }

  @Override
  public void updateMultiSelectMessage(List<ChatMessageBean> messages) {
    if (messageAdapter != null) {
      messageAdapter.reloadMessages(messages, PAYLOAD_SELECT_STATUS);
    }
  }

  @Override
  public void setMessageMode(int mode) {
    if (messageAdapter != null) {
      messageAdapter.setMessageMode(mode);
    }
  }

  @Override
  public boolean hasMoreNewerMessages() {
    return hasMoreNewerMessages;
  }

  @Override
  public boolean hasMoreForwardMessages() {
    return hasMoreForwardMessages;
  }

  public void refreshTeamMessageReceipt() {
    LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
    if (layoutManager != null && loadHandler != null) {
      int firstVisible = layoutManager.findFirstVisibleItemPosition();
      int lastVisible = layoutManager.findLastVisibleItemPosition();
      if (messageAdapter.getMessageList() == null || messageAdapter.getMessageList().isEmpty()) {
        return;
      }
      if (firstVisible < 0
          || lastVisible + 1 > messageAdapter.getMessageList().size()
          || firstVisible > lastVisible + 1) {
        return;
      }
      loadHandler.onVisibleItemChange(
          messageAdapter.getMessageList().subList(firstVisible, lastVisible + 1));
    }
  }

  /**
   * 滚动到消息
   *
   * @param msgUuid 消息uuid
   * @return 是否滚动成功
   */
  public boolean scrollToMessage(String msgUuid) {
    if (messageAdapter != null) {
      int index = searchMessagePosition(msgUuid);
      if (index >= 0) {
        smoothScrollToPosition(index);
        return true;
      }
    }
    return false;
  }

  private boolean needScrollToBottom() {
    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) getLayoutManager();
    if (linearLayoutManager == null) {
      return false;
    }
    int firstPosition = linearLayoutManager.findFirstVisibleItemPosition();
    int count = linearLayoutManager.getChildCount();
    return firstPosition + count >= linearLayoutManager.getItemCount() - 1 && !hasMoreNewerMessages;
  }

  private boolean isLastItemVisibleCompleted() {
    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) getLayoutManager();
    if (linearLayoutManager == null) {
      return false;
    }
    int lastPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
    int childCount = linearLayoutManager.getChildCount();
    int firstPosition = linearLayoutManager.findFirstVisibleItemPosition();
    return lastPosition >= firstPosition + childCount - 1;
  }

  public ArrayList<ChatMessageBean> filterMessagesByType(int typeValue) {
    ArrayList<ChatMessageBean> mediaMsgList = new ArrayList<>();
    for (ChatMessageBean msg : messageAdapter.getMessageList()) {
      if (msg.getViewType() == typeValue) {
        mediaMsgList.add(msg);
      }
    }
    return mediaMsgList;
  }

  public interface OnListViewEventListener {
    void onListViewStartScroll();

    void onListViewTouched();

    void onListViewScrolling();

    void onListViewScrollEnd();
  }
}
