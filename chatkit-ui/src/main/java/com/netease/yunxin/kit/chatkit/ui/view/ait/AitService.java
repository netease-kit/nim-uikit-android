// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.ait;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.util.NIMUtil;
import com.netease.nimlib.sdk.v2.V2NIMError;
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginDetailListener;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMConnectStatus;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncState;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncType;
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMConversation;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageRefer;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.impl.ConversationListenerImpl;
import com.netease.yunxin.kit.chatkit.impl.LocalConversationListenerImpl;
import com.netease.yunxin.kit.chatkit.impl.MessageListenerImpl;
import com.netease.yunxin.kit.chatkit.listener.ChatListener;
import com.netease.yunxin.kit.chatkit.listener.MessageRevokeNotification;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.chatkit.repo.LocalConversationRepo;
import com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant;
import com.netease.yunxin.kit.chatkit.ui.common.AitDBHelper;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AtContactsModel;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.custom.AitEvent;
import com.netease.yunxin.kit.corekit.im2.custom.AitInfo;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.utils.CoroutineUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/** 用于@功能服务类，用于管理@信息，包括接收到的@信息，本地保存的@信息，以及发送@信息事件 */
public class AitService {

  private static final String TAG = "AitService";
  private static final long READ_TIME_QUERY_DELAY_MS = 50L;
  private static final int MAX_READ_TIME_QUERY_SIZE = 100;
  private static AitService instance;
  private final Map<String, AitInfo> aitInfoMapCache = new HashMap<>();
  private final Map<String, Long> readTimeCache = new HashMap<>();
  private final Map<String, List<AitMessage>> pendingAitMessages = new HashMap<>();
  private final Set<String> readTimeQueryingConversationIds = new HashSet<>();
  private final Set<String> pendingReadTimeQueryConversationIds = new LinkedHashSet<>();
  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  private final CopyOnWriteArrayList<AitInfo> updateList = new CopyOnWriteArrayList<>();
  private final CopyOnWriteArrayList<AitInfo> insertList = new CopyOnWriteArrayList<>();
  private final CopyOnWriteArrayList<AitInfo> deleteList = new CopyOnWriteArrayList<>();
  private Context mContext;
  private boolean hasRegister;
  private boolean readTimeQueryScheduled;
  private long readTimeQueryVersion;

  private ChatListener messageObserver;
  private LocalConversationListenerImpl localConversationListener;
  private ConversationListenerImpl conversationListener;

  private AitService() {}

  public static AitService getInstance() {
    if (instance == null) {
      instance = new AitService();
    }
    return instance;
  }

  // 初始化
  public void init(Context context) {
    if (NIMUtil.isMainProcess(context) && IMKitConfigCenter.getEnableAtMessage()) {
      ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "init");
      mContext = context;
      // 监听登录状态
      IMKitClient.addLoginDetailListener(
          new V2NIMLoginDetailListener() {
            @Override
            public void onConnectStatus(V2NIMConnectStatus status) {
              ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "onConnectStatus:" + status.name());
              if (!IMKitConfigCenter.getEnableAtMessage()) {
                return;
              }
              if (status == V2NIMConnectStatus.V2NIM_CONNECT_STATUS_CONNECTED) {
                resetReadTimeQueryState();
                if (!hasRegister) {
                  registerObserver();
                  hasRegister = true;
                }
                ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "AuthServiceObserver:LOGINED");
                CoroutineUtils.run(
                    new CoroutineUtils.CoroutineCallback<List<AitInfo>>() {
                      @Override
                      public void runMain(List<AitInfo> params) {
                        if (params != null) {
                          List<AitInfo> aitInfoList = new ArrayList<>();
                          for (AitInfo aitInfo : params) {
                            if (TextUtils.equals(aitInfo.getAccountId(), IMKitClient.account())
                                || TextUtils.equals(
                                    AtContactsModel.ACCOUNT_ALL, aitInfo.getAccountId())) {
                              if (!aitInfoMapCache.containsKey(aitInfo.getConversationId())) {
                                aitInfoMapCache.put(aitInfo.getConversationId(), aitInfo);
                                ALog.d(TAG, "init,load,add cache:" + aitInfo.getConversationId());
                              }
                              aitInfoList.add(aitInfo);
                              ALog.d(TAG, "init,load:" + aitInfo.getConversationId());
                            }
                          }
                          if (aitInfoList.size() > 0) {
                            sendAitEvent(aitInfoList, AitEvent.AitEventType.Load);
                          }
                        }
                      }

                      @Override
                      public List<AitInfo> runIO() {
                        AitDBHelper.getInstance(context).openWrite();
                        List<AitInfo> result = AitDBHelper.getInstance(context).queryAll();
                        return result;
                      }
                    });
              } else if (status == V2NIMConnectStatus.V2NIM_CONNECT_STATUS_DISCONNECTED) {
                if (hasRegister) {
                  unRegisterObserver();
                  hasRegister = false;
                }
              }
            }

            @Override
            public void onDisconnected(V2NIMError error) {
              ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "onDisconnected:" + error.getDesc());
            }

            @Override
            public void onConnectFailed(V2NIMError error) {
              ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "onConnectFailed:" + error.getDesc());
            }

            @Override
            public void onDataSync(
                V2NIMDataSyncType type, V2NIMDataSyncState state, V2NIMError error) {
              ALog.d(
                  ChatKitUIConstant.LIB_TAG, TAG, "onDataSync:" + type.name() + "," + state.name());
            }
          });
    }
  }

  // 清除某个会话@记录
  public void clearAitInfo(String conversationId) {
    if (mContext == null) {
      return;
    }
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "clearAitInfo:" + conversationId);
    AitInfo aitInfo = aitInfoMapCache.remove(conversationId);
    if (aitInfo == null) {
      return;
    }
    List<AitInfo> aitInfoList = new ArrayList<>();
    aitInfoList.add(aitInfo);
    sendAitEvent(aitInfoList, AitEvent.AitEventType.Clear);
    deleteList.add(aitInfo);
    CoroutineUtils.runIO(deleteCoroutine);
  }

  // 发送加载@信息事件
  public void sendLocalAitEvent() {
    if (mContext == null || !IMKitConfigCenter.getEnableAtMessage()) {
      return;
    }
    sendAitEvent(new ArrayList<>(aitInfoMapCache.values()), AitEvent.AitEventType.Load);
  }

  // 发送@信息事件，如果收到@信息则发送事件，会话列表接收到事件之后，在相关的会话中增加@提示。事件包括加载、清理和新增
  public void sendAitEvent(List<AitInfo> aitInfoList, AitEvent.AitEventType type) {
    if (aitInfoList == null || mContext == null || !IMKitConfigCenter.getEnableAtMessage()) {
      return;
    }
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "sendAitEvent:" + type.name() + aitInfoList.size());
    AitEvent aitEvent = new AitEvent(type);
    aitEvent.setAitInfoList(aitInfoList);
    EventCenter.notifyEvent(aitEvent);
  }

  // 注册消息监听,监听接受到的消息和撤回消息
  public void registerObserver() {
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "registerObserver");
    messageObserver =
        new MessageListenerImpl() {
          @Override
          public void onReceiveMessages(@NonNull List<IMMessageInfo> messages) {
            super.onReceiveMessages(messages);
            if (mContext != null && IMKitConfigCenter.getEnableAtMessage()) {
              processAitMessages(parseMessage(messages));
            }
          }

          @Override
          public void onMessageRevokeNotifications(
              @Nullable List<MessageRevokeNotification> revokeNotifications) {
            super.onMessageRevokeNotifications(revokeNotifications);
            if (revokeNotifications == null || !IMKitConfigCenter.getEnableAtMessage()) {
              return;
            }
            if (mContext != null) {
              Map<String, AitInfo> clearInfo = new HashMap<>();
              for (MessageRevokeNotification notification : revokeNotifications) {
                V2NIMMessageRefer msgRefer = notification.getNimNotification().getMessageRefer();
                if (clearInfo.containsKey(msgRefer.getConversationId())) {
                  clearInfo
                      .get(msgRefer.getConversationId())
                      .addMsgUid(msgRefer.getMessageClientId());
                } else {
                  AitInfo aitInfo = new AitInfo();
                  aitInfo.addMsgUid(msgRefer.getMessageClientId());
                  clearInfo.put(msgRefer.getConversationId(), aitInfo);
                }
              }
              removeAitInfo(clearInfo);
            }
          }
        };
    ChatRepo.addMessageListener(messageObserver);

    if (IMKitClient.enableV2CloudConversation()) {
      conversationListener =
          new ConversationListenerImpl() {

            @Override
            public void onConversationReadTimeUpdated(
                @Nullable String conversationId, long readTime) {
              updateReadTime(conversationId, readTime);
              clearAitInfo(conversationId);
            }
          };
      ConversationRepo.addConversationListener(conversationListener);
    } else {
      localConversationListener =
          new LocalConversationListenerImpl() {

            @Override
            public void onConversationReadTimeUpdated(
                @Nullable String conversationId, long readTime) {
              updateReadTime(conversationId, readTime);
              clearAitInfo(conversationId);
            }
          };
      LocalConversationRepo.addConversationListener(localConversationListener);
    }
  }

  // 取消注册消息监听
  public void unRegisterObserver() {
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "unRegisterObserver");
    if (messageObserver != null) {
      ChatRepo.removeMessageListener(messageObserver);
    }
    if (conversationListener != null) {
      ConversationRepo.removeConversationListener(conversationListener);
    }
    if (localConversationListener != null) {
      LocalConversationRepo.removeConversationListener(localConversationListener);
    }
  }

  // 解析接受到的消息，获取@信息，如果有则本地保存
  private Map<String, List<AitMessage>> parseMessage(List<IMMessageInfo> msgList) {
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "parseMessage");
    Map<String, List<AitMessage>> aitInfoMap = new HashMap<>();
    for (IMMessageInfo messageInfo : msgList) {
      if (TextUtils.equals(
          messageInfo.getMessage().getConversationId(), ChatRepo.getConversationId())) {
        continue;
      }
      AtContactsModel aitModel = MessageHelper.getAitBlockFromMsg(messageInfo.getMessage());
      if (aitModel != null) {
        List<String> aitAccount = aitModel.getAtTeamMember();
        for (String account : aitAccount) {
          if (TextUtils.equals(IMKitClient.account(), account)
              || TextUtils.equals(AtContactsModel.ACCOUNT_ALL, account)) {
            String uuid = messageInfo.getMessage().getMessageClientId();
            String conversationId = messageInfo.getMessage().getConversationId();
            List<AitMessage> aitMessages = aitInfoMap.get(conversationId);
            if (aitMessages == null) {
              aitMessages = new ArrayList<>();
              aitInfoMap.put(conversationId, aitMessages);
            }
            aitMessages.add(new AitMessage(uuid, messageInfo.getMessage().getCreateTime()));
            break;
          }
        }
      }
    }
    return aitInfoMap;
  }

  private void processAitMessages(Map<String, List<AitMessage>> messagesByConversation) {
    if (messagesByConversation == null || messagesByConversation.isEmpty()) {
      return;
    }
    Map<String, Long> cachedReadTimes = new HashMap<>();
    synchronized (this) {
      for (Map.Entry<String, List<AitMessage>> entry : messagesByConversation.entrySet()) {
        String conversationId = entry.getKey();
        Long readTime = readTimeCache.get(conversationId);
        if (readTime == null) {
          List<AitMessage> pendingMessages = pendingAitMessages.get(conversationId);
          if (pendingMessages == null) {
            pendingMessages = new ArrayList<>();
            pendingAitMessages.put(conversationId, pendingMessages);
          }
          pendingMessages.addAll(entry.getValue());
          if (readTimeQueryingConversationIds.add(conversationId)) {
            pendingReadTimeQueryConversationIds.add(conversationId);
          }
        } else {
          cachedReadTimes.put(conversationId, readTime);
        }
      }
    }
    notifyAitInfo(buildUnreadAitInfo(messagesByConversation, cachedReadTimes));
    scheduleReadTimeQuery();
  }

  private void scheduleReadTimeQuery() {
    synchronized (this) {
      if (readTimeQueryScheduled || pendingReadTimeQueryConversationIds.isEmpty()) {
        return;
      }
      readTimeQueryScheduled = true;
    }
    mainHandler.postDelayed(this::queryPendingReadTimes, READ_TIME_QUERY_DELAY_MS);
  }

  private void queryPendingReadTimes() {
    List<String> conversationIds = new ArrayList<>();
    final long queryVersion;
    synchronized (this) {
      readTimeQueryScheduled = false;
      queryVersion = readTimeQueryVersion;
      Iterator<String> iterator = pendingReadTimeQueryConversationIds.iterator();
      while (iterator.hasNext() && conversationIds.size() < MAX_READ_TIME_QUERY_SIZE) {
        conversationIds.add(iterator.next());
        iterator.remove();
      }
    }
    scheduleReadTimeQuery();
    if (conversationIds.isEmpty()) {
      return;
    }
    if (IMKitClient.enableV2CloudConversation()) {
      ConversationRepo.getConversationListByIds(
          conversationIds,
          new FetchCallback<List<V2NIMConversation>>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              completeReadTimeQuery(queryVersion, conversationIds, new HashMap<>());
            }

            @Override
            public void onSuccess(@Nullable List<V2NIMConversation> conversations) {
              Map<String, Long> readTimes = new HashMap<>();
              if (conversations != null) {
                for (V2NIMConversation conversation : conversations) {
                  if (conversation != null) {
                    readTimes.put(conversation.getConversationId(), conversation.getLastReadTime());
                  }
                }
              }
              completeReadTimeQuery(queryVersion, conversationIds, readTimes);
            }
          });
    } else {
      for (String conversationId : conversationIds) {
        LocalConversationRepo.getConversationReadTime(
            conversationId,
            new FetchCallback<Long>() {
              @Override
              public void onError(int errorCode, @Nullable String errorMsg) {
                completeReadTimeQuery(
                    queryVersion, Collections.singletonList(conversationId), new HashMap<>());
              }

              @Override
              public void onSuccess(@Nullable Long readTime) {
                Map<String, Long> readTimes = new HashMap<>();
                if (readTime != null) {
                  readTimes.put(conversationId, readTime);
                }
                completeReadTimeQuery(
                    queryVersion, Collections.singletonList(conversationId), readTimes);
              }
            });
      }
    }
  }

  private void completeReadTimeQuery(
      long queryVersion, List<String> conversationIds, Map<String, Long> readTimes) {
    Map<String, List<AitMessage>> messagesByConversation = new HashMap<>();
    Map<String, Long> effectiveReadTimes = new HashMap<>();
    synchronized (this) {
      if (queryVersion != readTimeQueryVersion) {
        return;
      }
      for (String conversationId : conversationIds) {
        Long readTime = readTimes.get(conversationId);
        Long cachedReadTime = readTimeCache.get(conversationId);
        if (cachedReadTime != null && (readTime == null || cachedReadTime > readTime)) {
          readTime = cachedReadTime;
        }
        if (readTime != null) {
          readTimeCache.put(conversationId, readTime);
          effectiveReadTimes.put(conversationId, readTime);
        }
        readTimeQueryingConversationIds.remove(conversationId);
        List<AitMessage> pendingMessages = pendingAitMessages.remove(conversationId);
        if (pendingMessages != null) {
          messagesByConversation.put(conversationId, pendingMessages);
        }
      }
    }
    notifyAitInfo(buildUnreadAitInfo(messagesByConversation, effectiveReadTimes));
  }

  private Map<String, AitInfo> buildUnreadAitInfo(
      Map<String, List<AitMessage>> messagesByConversation, Map<String, Long> readTimes) {
    Map<String, AitInfo> result = new HashMap<>();
    for (Map.Entry<String, List<AitMessage>> entry : messagesByConversation.entrySet()) {
      Long readTime = readTimes.get(entry.getKey());
      if (readTime == null) {
        continue;
      }
      AitInfo aitInfo = new AitInfo();
      aitInfo.setConversationId(entry.getKey());
      aitInfo.setAccountId(IMKitClient.account());
      for (AitMessage message : entry.getValue()) {
        if (message.createTime > readTime) {
          aitInfo.addMsgUid(message.messageId);
        }
      }
      if (aitInfo.hasMsgUid()) {
        result.put(entry.getKey(), aitInfo);
      }
    }
    return result;
  }

  private void notifyAitInfo(Map<String, AitInfo> aitInfoMap) {
    if (aitInfoMap == null || aitInfoMap.isEmpty()) {
      return;
    }
    sendAitEvent(new ArrayList<>(aitInfoMap.values()), AitEvent.AitEventType.Arrive);
    updateAitInfo(aitInfoMap);
  }

  private synchronized void updateReadTime(@Nullable String conversationId, long readTime) {
    if (!TextUtils.isEmpty(conversationId)) {
      Long cachedReadTime = readTimeCache.get(conversationId);
      if (cachedReadTime == null || readTime > cachedReadTime) {
        readTimeCache.put(conversationId, readTime);
      }
    }
  }

  private void resetReadTimeQueryState() {
    synchronized (this) {
      readTimeCache.clear();
      pendingAitMessages.clear();
      readTimeQueryingConversationIds.clear();
      pendingReadTimeQueryConversationIds.clear();
      readTimeQueryScheduled = false;
      readTimeQueryVersion++;
    }
    mainHandler.removeCallbacksAndMessages(null);
  }

  private static class AitMessage {
    private final String messageId;
    private final long createTime;

    private AitMessage(String messageId, long createTime) {
      this.messageId = messageId;
      this.createTime = createTime;
    }
  }

  // 更新@信息
  public void updateAitInfo(Map<String, AitInfo> aitInfoMap) {
    if (mContext == null) {
      return;
    }

    for (String conversationId : aitInfoMap.keySet()) {
      AitInfo newAitInfo = aitInfoMap.get(conversationId);
      if (newAitInfo == null) {
        continue;
      }
      if (aitInfoMapCache.containsKey(conversationId)) {
        AitInfo cacheAitInfo = aitInfoMapCache.get(conversationId);
        if (cacheAitInfo != null) {
          cacheAitInfo.addMsgUid(newAitInfo.getMsgUidList());
          updateList.add(cacheAitInfo);
          ALog.d(
              ChatKitUIConstant.LIB_TAG,
              TAG,
              "updateAitInfo,updateList" + cacheAitInfo.getConversationId());
        }
      } else {
        insertList.add(newAitInfo);
        aitInfoMapCache.put(conversationId, newAitInfo);
        ALog.d(
            ChatKitUIConstant.LIB_TAG,
            TAG,
            "updateAitInfo,insertList" + newAitInfo.getConversationId());
      }
    }
    CoroutineUtils.runIO(updateCoroutine);
  }

  // 删除@信息
  public void removeAitInfo(Map<String, AitInfo> aitInfoMap) {
    if (mContext == null) {
      return;
    }

    List<AitInfo> notifyDelete = new ArrayList<>();
    for (String conversationId : aitInfoMap.keySet()) {
      AitInfo newAitInfo = aitInfoMap.get(conversationId);
      if (newAitInfo == null) {
        continue;
      }
      if (aitInfoMapCache.containsKey(conversationId)) {
        AitInfo cacheAitInfo = aitInfoMapCache.get(conversationId);
        if (cacheAitInfo != null) {
          boolean hasRemove = cacheAitInfo.removeMsgUid(newAitInfo.getMsgUidList());
          ALog.d(
              ChatKitUIConstant.LIB_TAG,
              TAG,
              "removeAitInfo,updateList" + cacheAitInfo.getConversationId());
          if (!cacheAitInfo.hasMsgUid()) {
            deleteList.add(cacheAitInfo);
            notifyDelete.add(cacheAitInfo);
            aitInfoMapCache.remove(conversationId);
          } else if (hasRemove) {
            updateList.add(cacheAitInfo);
          }
        }
      }
      //      else {
      //        deleteList.add(newAitInfo);
      //        notifyDelete.add(newAitInfo);
      //      }
    }
    if (notifyDelete.size() > 0) {
      sendAitEvent(notifyDelete, AitEvent.AitEventType.Clear);
    }
    if (updateList.size() > 0) {
      CoroutineUtils.runIO(updateCoroutine);
    }
    if (deleteList.size() > 0) {
      CoroutineUtils.runIO(deleteCoroutine);
    }
  }

  // 删除本地数据库中@信息
  private void deleteAit() {
    if (mContext == null || !IMKitConfigCenter.getEnableAtMessage()) {
      return;
    }

    AitDBHelper.getInstance(mContext).openWrite();
    List<String> conversationList = new ArrayList<>();
    for (AitInfo info : deleteList) {
      conversationList.add(info.getConversationId());
      ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "deleteAit:" + info.getConversationId());
    }
    AitDBHelper.getInstance(mContext)
        .deleteWithConversationId(conversationList.toArray(new String[0]));
    deleteList.clear();
  }

  // 更新本地数据库@信息
  public void updateAit() {
    AitDBHelper.getInstance(mContext).openWrite();
    for (AitInfo info : updateList) {
      long updateResult = AitDBHelper.getInstance(mContext).update(info);
      ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "updateAit,update:" + updateResult);
    }
    for (AitInfo insertInfo : insertList) {
      long result = AitDBHelper.getInstance(mContext).insert(insertInfo);
      ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "updateAit,insert:" + result);
    }
    insertList.clear();
    updateList.clear();
  }

  // 更新本地数据库@信息
  private final CoroutineUtils.CoroutineCallback<Void> updateCoroutine =
      new CoroutineUtils.CoroutineCallback<Void>() {
        @Override
        public Void runIO() {
          updateAit();
          return null;
        }

        @Override
        public void runMain(Void param) {}
      };

  // 删除本地数据库中@信息
  private final CoroutineUtils.CoroutineCallback<Void> deleteCoroutine =
      new CoroutineUtils.CoroutineCallback<Void>() {
        @Override
        public Void runIO() {
          deleteAit();
          return null;
        }

        @Override
        public void runMain(Void param) {}
      };
}
