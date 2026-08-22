// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.KEY_LOCAL_TOPIC_TIP;
import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.ai.enums.V2NIMAIModelRoleType;
import com.netease.nimlib.sdk.v2.ai.model.V2NIMAIUser;
import com.netease.nimlib.sdk.v2.ai.params.V2NIMAIModelCallMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageCreator;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageDeletedNotification;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageRefer;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageQueryDirection;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageSendingState;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMQueryDirection;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMSortOrder;
import com.netease.nimlib.sdk.v2.message.option.V2NIMMessageListOption;
import com.netease.nimlib.sdk.v2.message.params.V2NIMSendMessageParams;
import com.netease.nimlib.sdk.v2.message.result.V2NIMSendMessageResult;
import com.netease.nimlib.sdk.v2.topic.V2NIMTopic;
import com.netease.nimlib.sdk.v2.topic.V2NIMTopicListener;
import com.netease.nimlib.sdk.v2.topic.V2NIMTopicRefer;
import com.netease.nimlib.sdk.v2.topic.option.V2NIMTopicMessageListOption;
import com.netease.nimlib.sdk.v2.topic.params.V2NIMRemoveTopicsParams;
import com.netease.nimlib.sdk.v2.topic.params.V2NIMSendTopicMessageParams;
import com.netease.nimlib.sdk.v2.topic.params.V2NIMUpdateTopicParams;
import com.netease.nimlib.sdk.v2.topic.result.V2NIMTopicMessageListResult;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.impl.MessageListenerImpl;
import com.netease.yunxin.kit.chatkit.listener.MessageUpdateType;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.chatkit.repo.LocalConversationRepo;
import com.netease.yunxin.kit.chatkit.repo.TopicRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.BotSubSessionUtils;
import com.netease.yunxin.kit.chatkit.ui.common.MessageCreator;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.common.MessageParamBuildUtils;
import com.netease.yunxin.kit.chatkit.ui.model.AnchorScrollInfo;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public class ChatBotSubSessionViewModel extends ChatP2PViewModel {

  private static final String TAG = "ChatBotSubSessionChatViewModel";
  private static final int TOPIC_MESSAGE_PAGE_SIZE = 30;

  private V2NIMTopic topic;
  private V2NIMMessage olderAnchorMessage;
  private V2NIMMessage newerAnchorMessage;
  private boolean hasMoreOlderTopicMessages;
  private boolean hasMoreNewerTopicMessages;
  private boolean loadingOlderTopicMessages;
  private boolean loadingNewerTopicMessages;
  private String topicConversationId;
  private boolean topicMessageListenerAdded;
  private V2NIMMessage pendingAutoNameMessage;
  private V2NIMTopicRefer pendingAutoNameTopicRefer;
  private boolean autoNamingTopic;
  private final MutableLiveData<V2NIMTopic> topicLiveData = new MutableLiveData<>();
  private final MutableLiveData<Boolean> topicRemovedLiveData = new MutableLiveData<>();

  private final MessageListenerImpl topicMessageListener =
      new MessageListenerImpl() {
        @Override
        public void onReceiveMessages(@NonNull List<IMMessageInfo> messages) {
          List<IMMessageInfo> filteredMessages = filterTopicMessageInfos(messages);
          if (filteredMessages.isEmpty()) {
            return;
          }
          ALog.i(LIB_TAG, TAG, "receive topic msg -->> " + filteredMessages.size());
          // 机器人回复在聊天页实时到达时同步推进父会话已读时间，返回子会话列表不显示红点。
          clearConversationUnread();
          FetchResult<List<ChatMessageBean>> result = new FetchResult<>(LoadStatus.Success);
          result.setData(convertToTopicChatBeans(filteredMessages));
          result.setType(FetchResult.FetchType.Add);
          result.setTypeIndex(-1);
          getRecMessageLiveData().setValue(result);
        }

        @Override
        public void onSendMessage(@NonNull V2NIMMessage message) {
          if (!shouldHandleTopicMessage(message)) {
            return;
          }
          ALog.i(LIB_TAG, TAG, "onSendTopicMessage -->> " + message.getMessageClientId());
          IMMessageInfo info = new IMMessageInfo(message);
          boolean isSending =
              message.getSendingState()
                  == V2NIMMessageSendingState.V2NIM_MESSAGE_SENDING_STATE_SENDING;
          boolean isPendingNewTopicMessage = isPendingNewTopicMessageWithoutRefer(message);
          FetchResult<ChatMessageBean> result = new FetchResult<>(LoadStatus.Success);
          result.setType(
              isSending || isPendingNewTopicMessage
                  ? FetchResult.FetchType.Add
                  : FetchResult.FetchType.Update);
          result.setData(createTopicChatBean(info));
          getSendMessageLiveData().setValue(result);
        }

        @Override
        public void onMessagesUpdate(
            @NonNull List<IMMessageInfo> messages, @NonNull MessageUpdateType type) {
          List<IMMessageInfo> filteredMessages = filterTopicMessageInfos(messages);
          if (filteredMessages.isEmpty()) {
            return;
          }
          FetchResult<Pair<MessageUpdateType, List<ChatMessageBean>>> result =
              new FetchResult<>(LoadStatus.Success);
          result.setData(new Pair<>(type, convertToTopicChatBeans(filteredMessages)));
          result.setType(FetchResult.FetchType.Update);
          result.setTypeIndex(-1);
          getUpdateMessageLiveData().setValue(result);
        }

        @Override
        public void onReceiveMessagesModified(@Nullable List<V2NIMMessage> messages) {
          List<IMMessageInfo> messageInfos = new ArrayList<>();
          if (messages != null) {
            for (V2NIMMessage message : messages) {
              if (shouldHandleTopicMessage(message)) {
                messageInfos.add(new IMMessageInfo(message));
              }
            }
          }
          postTopicMessageUpdates(messageInfos, MessageUpdateType.UpdateMessage);
        }

        @Override
        public void onMessageDeletedNotifications(
            @NonNull List<? extends V2NIMMessageDeletedNotification> notifications) {
          List<V2NIMMessageRefer> deletedMessages = new ArrayList<>();
          for (V2NIMMessageDeletedNotification notification : notifications) {
            V2NIMMessageRefer refer = notification == null ? null : notification.getMessageRefer();
            if (refer != null && TextUtils.equals(refer.getConversationId(), mConversationId)) {
              deletedMessages.add(refer);
            }
          }
          if (!deletedMessages.isEmpty()) {
            FetchResult<List<V2NIMMessageRefer>> result = new FetchResult<>(LoadStatus.Success);
            result.setType(FetchResult.FetchType.Remove);
            result.setTypeIndex(-1);
            result.setData(deletedMessages);
            getDeleteMessageLiveData().setValue(result);
          }
        }
      };

  private final V2NIMTopicListener topicListener =
      new V2NIMTopicListener() {
        @Override
        public void onTopicAdded(V2NIMTopic topic) {
          if (isCurrentTopic(topic)) {
            updateTopic(topic);
          }
        }

        @Override
        public void onTopicsRemoved(List<V2NIMTopicRefer> topics) {
          if (topics == null || topic == null) {
            return;
          }
          for (V2NIMTopicRefer refer : topics) {
            if (sameTopic(topic, refer)) {
              topicRemovedLiveData.postValue(true);
              return;
            }
          }
        }

        @Override
        public void onTopicUpdated(V2NIMTopic topic) {
          if (isCurrentTopic(topic)) {
            updateTopic(topic);
          }
        }
      };

  public void initTopic(@Nullable V2NIMTopic topic, @Nullable String conversationId) {
    this.topic = topic;
    if (!TextUtils.isEmpty(conversationId)) {
      topicConversationId = conversationId;
      mConversationId = conversationId;
    } else {
      topicConversationId = getConversationId();
    }
    TopicRepo.addTopicListener(topicListener);
  }

  public V2NIMTopic getTopic() {
    return topic;
  }

  public MutableLiveData<V2NIMTopic> getTopicLiveData() {
    return topicLiveData;
  }

  public MutableLiveData<Boolean> getTopicRemovedLiveData() {
    return topicRemovedLiveData;
  }

  public boolean hasMoreTopicMessages() {
    return hasMoreOlderTopicMessages;
  }

  public void clearConversationUnread() {
    if (TextUtils.isEmpty(getConversationId())) {
      return;
    }
    if (IMKitClient.enableV2CloudConversation()) {
      ConversationRepo.clearUnreadCountByIds(Collections.singletonList(getConversationId()), null);
    } else {
      LocalConversationRepo.clearUnreadCountByIds(
          Collections.singletonList(getConversationId()), null);
    }
  }

  public void renameTopic(String newName) {
    renameTopic(newName, null);
  }

  public void renameTopic(String newName, @Nullable FetchCallback<V2NIMTopic> callback) {
    if (topic == null || TextUtils.isEmpty(newName)) {
      return;
    }
    V2NIMUpdateTopicParams params =
        new V2NIMUpdateTopicParams(topic, newName, topic.getServerExtension());
    TopicRepo.updateTopic(
        params,
        new FetchCallback<V2NIMTopic>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.e(LIB_TAG, TAG, "renameTopic error:" + errorCode + " msg:" + errorMsg);
            if (callback != null) {
              callback.onError(errorCode, errorMsg);
            }
          }

          @Override
          public void onSuccess(@Nullable V2NIMTopic data) {
            updateTopic(data);
            if (callback != null) {
              callback.onSuccess(data);
            }
          }
        });
  }

  public void deleteTopic() {
    deleteTopic(null);
  }

  public void deleteTopic(@Nullable FetchCallback<Void> callback) {
    if (topic == null) {
      return;
    }
    V2NIMRemoveTopicsParams params = new V2NIMRemoveTopicsParams(Collections.singletonList(topic));
    TopicRepo.removeTopics(
        params,
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.e(LIB_TAG, TAG, "deleteTopic error:" + errorCode + " msg:" + errorMsg);
            if (callback != null) {
              callback.onError(errorCode, errorMsg);
            }
          }

          @Override
          public void onSuccess(@Nullable Void data) {
            topicRemovedLiveData.postValue(true);
            if (callback != null) {
              callback.onSuccess(data);
            }
          }
        });
  }

  @Override
  public void sendTextMessage(
      String content,
      List<String> pushList,
      Map<String, Object> remoteExtension,
      V2NIMAIUser aiUser,
      List<V2NIMAIModelCallMessage> aiMessages) {
    V2NIMMessage textMessage = V2NIMMessageCreator.createTextMessage(content);
    sendTopicMessage(textMessage, pushList, remoteExtension, aiUser, aiMessages);
  }

  @Override
  public void replyTextMessage(
      String content,
      V2NIMMessage message,
      List<String> pushList,
      Map<String, Object> remoteExtension,
      V2NIMAIUser aiUser) {
    V2NIMMessage textMessage = V2NIMMessageCreator.createTextMessage(content);
    replyTopicMessage(textMessage, message, pushList, remoteExtension, aiUser);
  }

  @Override
  public void sendMessage(
      V2NIMMessage message,
      List<String> pushList,
      Map<String, Object> remoteExtension,
      V2NIMAIUser aiUser,
      List<V2NIMAIModelCallMessage> aiMessages) {
    sendTopicMessage(message, pushList, remoteExtension, aiUser, aiMessages);
  }

  @Override
  public void replyMessage(
      V2NIMMessage message,
      V2NIMMessage replyMsg,
      List<String> pushList,
      Map<String, Object> remoteExtension,
      V2NIMAIUser aiUser) {
    replyTopicMessage(message, replyMsg, pushList, remoteExtension, aiUser);
  }

  @Override
  public void resendMessage(V2NIMMessage message, V2NIMMessage replyMessage) {
    if (message == null) {
      return;
    }
    // Robot sub-session messages must use the same topic send path as the initial send.
    // The UI replyMessage is only needed by the generic reply-resend flow; passing it to
    // replyTopicMessage here would create a new Thread reply relation during retry.
    sendTopicMessage(message, null, null, null, null);
  }

  public void getTopicData(@Nullable V2NIMMessage anchor) {
    getFriendInfo(mChatAccountId);
    if (topic == null) {
      ALog.i(LIB_TAG, TAG, "getTopicData topic is null conversationId:" + topicConversationId);
      postTopicMessages(Collections.emptyList(), FetchResult.FetchType.Init, 0, false, null);
      return;
    }
    if (anchor == null) {
      olderAnchorMessage = null;
      newerAnchorMessage = null;
      hasMoreOlderTopicMessages = false;
      hasMoreNewerTopicMessages = false;
      getTopicMessageList(null, V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_DESC, false);
      return;
    }
    olderAnchorMessage = anchor;
    newerAnchorMessage = anchor;
    fetchTopicMessageListBothDirect(anchor);
  }

  public void getMoreTopicMessages() {
    ALog.i(
        LIB_TAG,
        TAG,
        "getMoreTopicMessages topicId:"
            + (topic == null ? "null" : topic.getTopicId())
            + " hasMore:"
            + hasMoreOlderTopicMessages
            + " loading:"
            + loadingOlderTopicMessages);
    if (topic == null || !hasMoreOlderTopicMessages || loadingOlderTopicMessages) {
      return;
    }
    getTopicMessageList(olderAnchorMessage, V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_DESC, false);
  }

  public void getNewerTopicMessages() {
    ALog.i(
        LIB_TAG,
        TAG,
        "getNewerTopicMessages topicId:"
            + (topic == null ? "null" : topic.getTopicId())
            + " hasMore:"
            + hasMoreNewerTopicMessages
            + " loading:"
            + loadingNewerTopicMessages);
    if (topic == null || !hasMoreNewerTopicMessages || loadingNewerTopicMessages) {
      return;
    }
    getTopicMessageList(newerAnchorMessage, V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_ASC, false);
  }

  private void fetchTopicMessageListBothDirect(@NonNull V2NIMMessage anchor) {
    new Handler(Looper.getMainLooper())
        .post(
            () ->
                getTopicMessageList(anchor, V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_DESC, true));
    getTopicMessageList(anchor, V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_ASC, true);
  }

  private void getTopicMessageList(
      @Nullable V2NIMMessage anchor, @NonNull V2NIMQueryDirection direction, boolean anchorMode) {
    if (topic == null || isTopicMessageLoading(direction)) {
      return;
    }
    setTopicMessageLoading(direction, true);
    addListener();
    ALog.i(
        LIB_TAG,
        TAG,
        "getTopicMessageList request topicId:"
            + topic.getTopicId()
            + " conversationId:"
            + topicConversationId
            + " limit:"
            + TOPIC_MESSAGE_PAGE_SIZE
            + " direction:"
            + direction.name()
            + " anchorMode:"
            + anchorMode);
    V2NIMTopicMessageListOption option =
        new V2NIMTopicMessageListOption(
            topic,
            0,
            0,
            anchor,
            TOPIC_MESSAGE_PAGE_SIZE,
            direction,
            V2NIMSortOrder.V2NIM_SORT_ORDER_ASC);
    TopicRepo.getTopicMessageList(
        option,
        new FetchCallback<V2NIMTopicMessageListResult>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            setTopicMessageLoading(direction, false);
            ALog.e(
                LIB_TAG,
                TAG,
                "getTopicMessageList error:"
                    + errorCode
                    + " msg:"
                    + errorMsg
                    + " direction:"
                    + direction.name()
                    + " topicId:"
                    + topic.getTopicId());
            FetchResult<List<ChatMessageBean>> result = new FetchResult<>(LoadStatus.Error);
            result.setError(errorCode, R.string.chat_message_fetch_error);
            result.setData(Collections.emptyList());
            getQueryMessageLiveData().setValue(result);
          }

          @Override
          public void onSuccess(@Nullable V2NIMTopicMessageListResult data) {
            setTopicMessageLoading(direction, false);
            List<V2NIMMessage> messages = data == null ? null : data.getReplyList();
            updateTopicMessageAnchor(direction, data, messages);
            updateTopicMessageHasMore(direction, data);
            ALog.i(
                LIB_TAG,
                TAG,
                "getTopicMessageList success topicId:"
                    + topic.getTopicId()
                    + " conversationId:"
                    + topicConversationId
                    + " direction:"
                    + direction.name()
                    + " resultSize:"
                    + (messages == null ? "null" : messages.size())
                    + " hasMore:"
                    + hasMoreTopicMessages(direction)
                    + " anchorMode:"
                    + anchorMode);
            FetchResult.FetchType fetchType =
                anchor == null && direction == V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_DESC
                    ? FetchResult.FetchType.Init
                    : FetchResult.FetchType.Add;
            if (fetchType == FetchResult.FetchType.Init) {
              loadLocalTopicTipsAndPost(messages, fetchType, anchorMode ? anchor : null);
            } else {
              postTopicMessages(
                  messages,
                  fetchType,
                  direction == V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_DESC ? 0 : -1,
                  false,
                  anchorMode ? anchor : null);
            }
          }
        });
  }

  private void loadLocalTopicTipsAndPost(
      @Nullable List<V2NIMMessage> topicMessages,
      @NonNull FetchResult.FetchType fetchType,
      @Nullable V2NIMMessage anchor) {
    V2NIMMessageListOption option =
        MessageParamBuildUtils.buildMessageOptions(
            null,
            0,
            mConversationId,
            TOPIC_MESSAGE_PAGE_SIZE * 3,
            V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_DESC);
    ChatRepo.getMessageList(
        option,
        new FetchCallback<List<IMMessageInfo>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            postTopicMessages(topicMessages, fetchType, 0, false, anchor);
          }

          @Override
          public void onSuccess(@Nullable List<IMMessageInfo> data) {
            List<V2NIMMessage> merged = new ArrayList<>();
            if (topicMessages != null) {
              merged.addAll(topicMessages);
            }
            if (data != null) {
              for (IMMessageInfo info : data) {
                if (isCurrentLocalTopicTip(info == null ? null : info.getMessage())) {
                  merged.add(info.getMessage());
                }
              }
            }
            Collections.sort(
                merged, (left, right) -> Long.compare(left.getCreateTime(), right.getCreateTime()));
            postTopicMessages(merged, fetchType, 0, false, anchor);
          }
        });
  }

  private boolean isTopicMessageLoading(@NonNull V2NIMQueryDirection direction) {
    return direction == V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_DESC
        ? loadingOlderTopicMessages
        : loadingNewerTopicMessages;
  }

  private void setTopicMessageLoading(@NonNull V2NIMQueryDirection direction, boolean loading) {
    if (direction == V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_DESC) {
      loadingOlderTopicMessages = loading;
    } else {
      loadingNewerTopicMessages = loading;
    }
  }

  private void updateTopicMessageAnchor(
      @NonNull V2NIMQueryDirection direction,
      @Nullable V2NIMTopicMessageListResult data,
      @Nullable List<V2NIMMessage> messages) {
    V2NIMMessage nextAnchor = data == null ? null : data.getAnchorMessage();
    if (nextAnchor == null) {
      nextAnchor =
          direction == V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_DESC
              ? firstMessage(messages)
              : lastMessage(messages);
    }
    if (direction == V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_DESC) {
      olderAnchorMessage = nextAnchor;
    } else {
      newerAnchorMessage = nextAnchor;
    }
  }

  private void updateTopicMessageHasMore(
      @NonNull V2NIMQueryDirection direction, @Nullable V2NIMTopicMessageListResult result) {
    boolean hasMore = result != null && result.hasMore();
    if (direction == V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_DESC) {
      hasMoreOlderTopicMessages = hasMore;
    } else {
      hasMoreNewerTopicMessages = hasMore;
    }
  }

  private boolean hasMoreTopicMessages(@NonNull V2NIMQueryDirection direction) {
    return direction == V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_DESC
        ? hasMoreOlderTopicMessages
        : hasMoreNewerTopicMessages;
  }

  @Nullable
  private V2NIMMessage getTopicMessageAnchor(@NonNull V2NIMQueryDirection direction) {
    return direction == V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_DESC
        ? olderAnchorMessage
        : newerAnchorMessage;
  }

  private void sendTopicMessage(
      V2NIMMessage message,
      List<String> pushList,
      Map<String, Object> remoteExtension,
      V2NIMAIUser aiUser,
      List<V2NIMAIModelCallMessage> aiMessages) {
    if (message == null) {
      return;
    }
    addListener();
    String remoteStr = MessageParamBuildUtils.toJson(remoteExtension);
    V2NIMSendMessageParams params =
        MessageCreator.createSendMessageParam(
            message, getConversationId(), pushList, remoteStr, aiUser, aiMessages, showRead);
    V2NIMSendTopicMessageParams topicParams = new V2NIMSendTopicMessageParams(params, null);
    boolean shouldAutoName = topic == null;
    if (shouldAutoName) {
      pendingAutoNameMessage = message;
      pendingAutoNameTopicRefer = null;
    }
    TopicRepo.sendTopicMessage(
        message,
        getConversationId(),
        topic,
        topicParams,
        new FetchCallback<V2NIMSendMessageResult>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.e(LIB_TAG, TAG, "sendTopicMessage error:" + errorCode + " msg:" + errorMsg);
            clearPendingAutoNameIfSame(message);
          }

          @Override
          public void onSuccess(@Nullable V2NIMSendMessageResult data) {
            if (data != null && data.getMessage() != null) {
              V2NIMMessage sentMessage = data.getMessage();
              postSentMessage(sentMessage);
              if (shouldAutoName) {
                pendingAutoNameMessage = sentMessage;
                pendingAutoNameTopicRefer = sentMessage.getTopicRefer();
              }
              saveAntispamTipIfNeeded(data);
              if (topic == null && data.getMessage().getTopicRefer() != null) {
                TopicRepo.getTopicByRefer(
                    sentMessage.getTopicRefer(),
                    new FetchCallback<V2NIMTopic>() {
                      @Override
                      public void onError(int errorCode, @Nullable String errorMsg) {
                        ALog.e(LIB_TAG, TAG, "getTopicByRefer error:" + errorCode);
                      }

                      @Override
                      public void onSuccess(@Nullable V2NIMTopic data) {
                        updateTopic(data);
                      }
                    });
              }
              tryAutoNamePendingTopic();
            }
          }
        });
  }

  private void tryAutoNamePendingTopic() {
    if (topic == null || pendingAutoNameMessage == null || autoNamingTopic) {
      return;
    }
    if (pendingAutoNameTopicRefer != null && !sameTopic(topic, pendingAutoNameTopicRefer)) {
      return;
    }
    if (!TextUtils.isEmpty(topic.getTopicName())) {
      clearPendingAutoName();
      return;
    }
    String topicName =
        BotSubSessionUtils.buildAutoTopicName(
            IMKitClient.getApplicationContext(), pendingAutoNameMessage);
    V2NIMUpdateTopicParams params =
        new V2NIMUpdateTopicParams(topic, topicName, topic.getServerExtension());
    autoNamingTopic = true;
    TopicRepo.updateTopic(
        params,
        new FetchCallback<V2NIMTopic>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            autoNamingTopic = false;
            ALog.e(LIB_TAG, TAG, "autoNameTopic error:" + errorCode + " msg:" + errorMsg);
          }

          @Override
          public void onSuccess(@Nullable V2NIMTopic data) {
            autoNamingTopic = false;
            clearPendingAutoName();
            updateTopic(data);
          }
        });
  }

  private void clearPendingAutoNameIfSame(@Nullable V2NIMMessage message) {
    if (pendingAutoNameMessage == null || message == null) {
      return;
    }
    if (TextUtils.equals(
        pendingAutoNameMessage.getMessageClientId(), message.getMessageClientId())) {
      clearPendingAutoName();
    }
  }

  private void clearPendingAutoName() {
    pendingAutoNameMessage = null;
    pendingAutoNameTopicRefer = null;
    autoNamingTopic = false;
  }

  private void replyTopicMessage(
      V2NIMMessage message,
      V2NIMMessage replyMessage,
      List<String> pushList,
      Map<String, Object> remoteExtension,
      V2NIMAIUser aiUser) {
    if (topic == null || replyMessage == null) {
      sendTopicMessage(message, pushList, remoteExtension, aiUser, null);
      return;
    }
    V2NIMAIModelCallMessage aiMessage = null;
    if (aiUser != null && !TextUtils.isEmpty(MessageHelper.getAIContentMsg(replyMessage))) {
      aiMessage =
          new V2NIMAIModelCallMessage(
              V2NIMAIModelRoleType.V2NIM_AI_MODEL_ROLE_TYPE_USER,
              MessageHelper.getAIContentMsg(replyMessage),
              0);
    }
    List<V2NIMAIModelCallMessage> aiMessageList =
        aiMessage == null ? null : Collections.singletonList(aiMessage);
    String remoteStr =
        MessageParamBuildUtils.toJson(
            MessageHelper.createReplyExtension(remoteExtension, replyMessage));
    V2NIMSendMessageParams params =
        MessageCreator.createSendMessageParam(
            message, getConversationId(), pushList, remoteStr, aiUser, aiMessageList, showRead);
    TopicRepo.replyTopicMessage(
        message,
        replyMessage,
        topic,
        params,
        new FetchCallback<V2NIMSendMessageResult>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.e(LIB_TAG, TAG, "replyTopicMessage error:" + errorCode + " msg:" + errorMsg);
          }

          @Override
          public void onSuccess(@Nullable V2NIMSendMessageResult data) {
            if (data != null && data.getMessage() != null) {
              postSentMessage(data.getMessage());
              saveAntispamTipIfNeeded(data);
            }
          }
        });
  }

  private void postSentMessage(V2NIMMessage message) {
    IMMessageInfo info = new IMMessageInfo(message);
    info.setSendingState(V2NIMMessageSendingState.V2NIM_MESSAGE_SENDING_STATE_SUCCEEDED);
    FetchResult<ChatMessageBean> result = new FetchResult<>(LoadStatus.Success);
    result.setType(FetchResult.FetchType.Update);
    result.setData(createTopicChatBean(info));
    getSendMessageLiveData().setValue(result);
  }

  private void saveAntispamTipIfNeeded(@NonNull V2NIMSendMessageResult data) {
    if (!IMKitConfigCenter.getEnableAntiSpamTipMessage()
        || data.getMessage() == null
        || TextUtils.isEmpty(data.getAntispamResult())) {
      return;
    }
    V2NIMTopicRefer topicRefer = data.getMessage().getTopicRefer();
    if (topicRefer == null) {
      return;
    }
    String tips =
        MessageHelper.getAntispamTips(
            IMKitClient.getApplicationContext(), data.getAntispamResult());
    V2NIMMessage tipMessage = V2NIMMessageCreator.createTipsMessage(tips);
    tipMessage.setLocalExtension(
        "{\""
            + KEY_LOCAL_TOPIC_TIP
            + "\":true,\"topicId\":"
            + topicRefer.getTopicId()
            + ",\"topicCreateTime\":"
            + topicRefer.getCreateTime()
            + "}");
    ChatRepo.insertMessageToLocal(
        tipMessage,
        mConversationId,
        data.getMessage().getSenderId(),
        data.getMessage().getCreateTime() + 5,
        null);
  }

  private boolean isCurrentLocalTopicTip(@Nullable V2NIMMessage message) {
    if (message == null
        || message.getMessageType() != V2NIMMessageType.V2NIM_MESSAGE_TYPE_TIPS
        || TextUtils.isEmpty(message.getLocalExtension())) {
      return false;
    }
    try {
      JSONObject extension = new JSONObject(message.getLocalExtension());
      if (!extension.optBoolean(KEY_LOCAL_TOPIC_TIP)) {
        return false;
      }
      if (topic != null) {
        return extension.optLong("topicId") == topic.getTopicId()
            && extension.optLong("topicCreateTime") == topic.getCreateTime();
      }
      return pendingAutoNameTopicRefer != null
          && extension.optLong("topicId") == pendingAutoNameTopicRefer.getTopicId()
          && extension.optLong("topicCreateTime") == pendingAutoNameTopicRefer.getCreateTime();
    } catch (Exception ignored) {
      return false;
    }
  }

  private void postTopicMessages(
      @Nullable List<V2NIMMessage> messages,
      FetchResult.FetchType type,
      int typeIndex,
      boolean scrollEnd,
      @Nullable V2NIMMessage anchorMessage) {
    List<ChatMessageBean> beans = new ArrayList<>();
    if (messages != null) {
      for (V2NIMMessage message : messages) {
        if (message != null) {
          beans.add(createTopicChatBean(new IMMessageInfo(message)));
        }
      }
    }
    FetchResult<List<ChatMessageBean>> result =
        new FetchResult<>(
            messages == null || messages.isEmpty() ? LoadStatus.Finish : LoadStatus.Success);
    result.setType(type);
    result.setTypeIndex(typeIndex);
    if (anchorMessage != null && !scrollEnd) {
      result.setExtraInfo(new AnchorScrollInfo(anchorMessage));
    }
    result.setData(beans);
    getQueryMessageLiveData().setValue(result);
  }

  private void updateTopic(@Nullable V2NIMTopic topic) {
    if (topic == null) {
      return;
    }
    boolean topicWasPending = this.topic == null;
    this.topic = topic;
    topicLiveData.postValue(topic);
    tryAutoNamePendingTopic();
    if (topicWasPending) {
      // A reply can arrive before the Topic object lookup completes. Reload the
      // confirmed Topic so the history query restores any early filtered reply.
      getTopicData(null);
    }
  }

  private ChatMessageBean createTopicChatBean(@NonNull IMMessageInfo messageInfo) {
    return new ChatMessageBean(messageInfo, ChatMessageBean.ReplyParseMode.BOT_TOPIC);
  }

  private List<ChatMessageBean> convertToTopicChatBeans(@Nullable List<IMMessageInfo> messageList) {
    if (messageList == null) {
      return null;
    }
    List<ChatMessageBean> result = new ArrayList<>(messageList.size());
    for (IMMessageInfo messageInfo : messageList) {
      if (messageInfo != null) {
        result.add(createTopicChatBean(messageInfo));
      }
    }
    return result;
  }

  private void postTopicMessageUpdates(
      @Nullable List<IMMessageInfo> messages, @NonNull MessageUpdateType type) {
    if (messages == null || messages.isEmpty()) {
      return;
    }
    FetchResult<Pair<MessageUpdateType, List<ChatMessageBean>>> result =
        new FetchResult<>(LoadStatus.Success);
    result.setData(new Pair<>(type, convertToTopicChatBeans(messages)));
    result.setType(FetchResult.FetchType.Update);
    result.setTypeIndex(-1);
    getUpdateMessageLiveData().setValue(result);
  }

  private boolean isCurrentTopic(@Nullable V2NIMTopicRefer refer) {
    if (topic != null) {
      return sameTopic(topic, refer);
    }
    return pendingAutoNameTopicRefer != null && sameTopic(pendingAutoNameTopicRefer, refer);
  }

  private boolean sameTopic(V2NIMTopicRefer left, V2NIMTopicRefer right) {
    return left != null
        && right != null
        && TextUtils.equals(left.getConversationId(), right.getConversationId())
        && left.getTopicId() == right.getTopicId()
        && left.getCreateTime() == right.getCreateTime();
  }

  private V2NIMMessage firstMessage(@Nullable List<V2NIMMessage> messages) {
    return messages == null || messages.isEmpty() ? null : messages.get(0);
  }

  private V2NIMMessage lastMessage(@Nullable List<V2NIMMessage> messages) {
    return messages == null || messages.isEmpty() ? null : messages.get(messages.size() - 1);
  }

  private List<IMMessageInfo> filterTopicMessageInfos(@Nullable List<IMMessageInfo> messages) {
    List<IMMessageInfo> result = new ArrayList<>();
    if (messages == null || messages.isEmpty()) {
      return result;
    }
    for (IMMessageInfo messageInfo : messages) {
      if (messageInfo != null && shouldHandleTopicMessage(messageInfo.getMessage())) {
        result.add(messageInfo);
      }
    }
    return result;
  }

  private boolean shouldHandleTopicMessage(@Nullable V2NIMMessage message) {
    if (message == null || !TextUtils.equals(message.getConversationId(), mConversationId)) {
      return false;
    }
    if (message.getTopicRefer() == null) {
      // Local tip messages do not carry a TopicRefer. They are created for the
      // current Topic conversation and must remain visible in this Topic page.
      if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TIPS) {
        return isCurrentLocalTopicTip(message);
      }
      return isPendingNewTopicMessageWithoutRefer(message);
    }
    if (topic == null) {
      return pendingAutoNameTopicRefer != null
          && sameTopic(pendingAutoNameTopicRefer, message.getTopicRefer());
    }
    return sameTopic(topic, message.getTopicRefer());
  }

  private boolean isPendingNewTopicMessageWithoutRefer(@Nullable V2NIMMessage message) {
    return topic == null
        && pendingAutoNameMessage != null
        && message != null
        && message.getTopicRefer() == null
        && TextUtils.equals(message.getConversationId(), topicConversationId)
        && TextUtils.equals(
            message.getMessageClientId(), pendingAutoNameMessage.getMessageClientId());
  }

  @Override
  public void addListener() {
    if (!topicMessageListenerAdded) {
      ChatRepo.addMessageListener(topicMessageListener);
      topicMessageListenerAdded = true;
    }
  }

  @Override
  public void removeListener() {
    if (topicMessageListenerAdded) {
      ChatRepo.removeMessageListener(topicMessageListener);
      topicMessageListenerAdded = false;
    }
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    TopicRepo.removeTopicListener(topicListener);
  }
}
