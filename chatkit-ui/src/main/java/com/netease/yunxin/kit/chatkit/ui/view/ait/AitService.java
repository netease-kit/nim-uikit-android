// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.ait;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.msg.model.RevokeMsgNotification;
import com.netease.nimlib.sdk.util.NIMUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatObserverRepo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant;
import com.netease.yunxin.kit.chatkit.ui.common.AitDBHelper;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AitContactsModel;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.custom.AitEvent;
import com.netease.yunxin.kit.corekit.im.custom.AitInfo;
import com.netease.yunxin.kit.corekit.im.model.EventObserver;
import com.netease.yunxin.kit.corekit.im.utils.CoroutineUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class AitService {

  private static final String TAG = "AitService";
  private static AitService instance;
  private Map<String, AitInfo> aitInfoMapCache = new HashMap<>();
  private CopyOnWriteArrayList<AitInfo> updateList = new CopyOnWriteArrayList<>();
  private CopyOnWriteArrayList<AitInfo> insertList = new CopyOnWriteArrayList<>();
  private CopyOnWriteArrayList<AitInfo> deleteList = new CopyOnWriteArrayList<>();
  private Context mContext;
  private boolean hasRegister;

  private AitService() {}

  public static AitService getInstance() {
    if (instance == null) {
      instance = new AitService();
    }
    return instance;
  }

  public void init(Context context) {
    if (NIMUtil.isMainProcess(context)) {
      ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "init");
      mContext = context;
      NIMClient.getService(AuthServiceObserver.class)
          .observeOnlineStatus(
              (Observer<StatusCode>)
                  statusCode -> {
                    if (statusCode == StatusCode.LOGINED) {
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
                                  if (TextUtils.equals(
                                          aitInfo.getAccountId(), IMKitClient.account())
                                      || TextUtils.equals(
                                          AitContactsModel.ACCOUNT_ALL, aitInfo.getAccountId())) {
                                    if (!aitInfoMapCache.containsKey(aitInfo.getSessionId())) {
                                      aitInfoMapCache.put(aitInfo.getSessionId(), aitInfo);
                                      ALog.d(TAG, "init,load,add cache:" + aitInfo.getSessionId());
                                    }
                                    aitInfoList.add(aitInfo);
                                    ALog.d(TAG, "init,load:" + aitInfo.getSessionId());
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
                    }
                  },
              true);
    }
  }

  public void clearAitInfo(String sessionId) {
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "clearAitInfo:" + sessionId);
    AitInfo aitInfo = aitInfoMapCache.remove(sessionId);
    if (aitInfo == null) {
      aitInfo = new AitInfo();
      aitInfo.setSessionId(sessionId);
    }
    List<AitInfo> aitInfoList = new ArrayList<>();
    aitInfoList.add(aitInfo);
    sendAitEvent(aitInfoList, AitEvent.AitEventType.Clear);
    deleteList.add(aitInfo);
    CoroutineUtils.runIO(deleteCoroutine);
  }

  public void sendLocalAitEvent() {
    sendAitEvent(new ArrayList<>(aitInfoMapCache.values()), AitEvent.AitEventType.Load);
  }

  public void sendAitEvent(List<AitInfo> aitInfoList, AitEvent.AitEventType type) {
    if (aitInfoList == null) {
      return;
    }
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "sendAitEvent:" + type.name() + aitInfoList.size());
    AitEvent aitEvent = new AitEvent(type);
    aitEvent.setAitInfoList(aitInfoList);
    EventCenter.notifyEvent(aitEvent);
  }

  public void registerObserver() {
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "registerObserver");
    ChatObserverRepo.registerReceiveMessageObserve(
        new EventObserver<List<IMMessageInfo>>() {
          @Override
          public void onEvent(@Nullable List<IMMessageInfo> msgList) {
            if (msgList != null) {
              ALog.d(
                  ChatKitUIConstant.LIB_TAG, TAG, "ReceiveMessageObserve,onEvent" + msgList.size());
              Map<String, AitInfo> aitInfoMap = parseMessage(msgList);
              if (aitInfoMap.size() > 0) {
                sendAitEvent(new ArrayList<>(aitInfoMap.values()), AitEvent.AitEventType.Arrive);
                updateAitInfo(aitInfoMap);
              }
            }
          }
        });

    ChatObserverRepo.registerRevokeMessageObserve(
        (Observer<RevokeMsgNotification>)
            revokeMsgNotification -> {
              if (revokeMsgNotification.getMessage() != null) {
                List<IMMessageInfo> msgList = new ArrayList<>();
                msgList.add(new IMMessageInfo(revokeMsgNotification.getMessage()));
                removeAitInfo(parseMessage(msgList));
              }
            });
  }

  private Map<String, AitInfo> parseMessage(List<IMMessageInfo> msgList) {

    Map<String, AitInfo> aitInfoMap = new HashMap<>();
    for (IMMessageInfo messageInfo : msgList) {
      if (TextUtils.equals(messageInfo.getMessage().getSessionId(), ChatRepo.getChattingAccount())
          || TextUtils.equals(
              messageInfo.getMessage().getFromAccount(), ChatRepo.getChattingAccount())) {
        continue;
      }
      AitContactsModel aitModel = MessageHelper.getAitBlock(messageInfo.getMessage());
      if (aitModel != null) {
        List<String> aitAccount = aitModel.getAitTeamMember();
        for (String account : aitAccount) {
          if (TextUtils.equals(IMKitClient.account(), account)
              || TextUtils.equals(AitContactsModel.ACCOUNT_ALL, account)) {
            String uuid = messageInfo.getMessage().getUuid();
            String sessionId = messageInfo.getMessage().getSessionId();
            ALog.d(
                ChatKitUIConstant.LIB_TAG,
                TAG,
                "ReceiveMessageObserve,onEvent has ait:" + sessionId + "," + uuid);

            AitInfo aitInfo = aitInfoMap.get(sessionId);
            if (aitInfo == null) {
              aitInfo = new AitInfo();
              aitInfo.setSessionId(sessionId);
              aitInfo.setAccountId(IMKitClient.account());
            }
            aitInfo.getMsgUidList().add(uuid);
            aitInfoMap.put(sessionId, aitInfo);
          }
        }
      }
    }
    return aitInfoMap;
  }

  public void updateAitInfo(Map<String, AitInfo> aitInfoMap) {
    for (String sessionId : aitInfoMap.keySet()) {
      AitInfo newAitInfo = aitInfoMap.get(sessionId);
      if (newAitInfo == null) {
        continue;
      }
      if (aitInfoMapCache.containsKey(sessionId)) {
        AitInfo cacheAitInfo = aitInfoMapCache.get(sessionId);
        if (cacheAitInfo != null) {
          cacheAitInfo.addMsgUid(newAitInfo.getMsgUidList());
          updateList.add(cacheAitInfo);
          ALog.d(
              ChatKitUIConstant.LIB_TAG,
              TAG,
              "updateAitInfo,updateList" + cacheAitInfo.getSessionId());
        }
      } else {
        insertList.add(newAitInfo);
        aitInfoMapCache.put(sessionId, newAitInfo);
        ALog.d(
            ChatKitUIConstant.LIB_TAG, TAG, "updateAitInfo,insertList" + newAitInfo.getSessionId());
      }
    }
    CoroutineUtils.runIO(updateCoroutine);
  }

  public void removeAitInfo(Map<String, AitInfo> aitInfoMap) {
    List<AitInfo> notifyDelete = new ArrayList<>();
    for (String sessionId : aitInfoMap.keySet()) {
      AitInfo newAitInfo = aitInfoMap.get(sessionId);
      if (newAitInfo == null) {
        continue;
      }
      if (aitInfoMapCache.containsKey(sessionId)) {
        AitInfo cacheAitInfo = aitInfoMapCache.get(sessionId);
        if (cacheAitInfo != null) {
          cacheAitInfo.removeMsgUid(newAitInfo.getMsgUidList());
          ALog.d(
              ChatKitUIConstant.LIB_TAG,
              TAG,
              "removeAitInfo,updateList" + cacheAitInfo.getSessionId());
          if (!cacheAitInfo.hasMsgUid()) {
            deleteList.add(cacheAitInfo);
            notifyDelete.add(cacheAitInfo);
            aitInfoMapCache.remove(sessionId);
          } else {
            updateList.add(cacheAitInfo);
          }
        }
      } else {
        deleteList.add(newAitInfo);
        notifyDelete.add(newAitInfo);
      }
    }
    if (notifyDelete.size() > 0) {
      sendAitEvent(notifyDelete, AitEvent.AitEventType.Clear);
    }
    CoroutineUtils.runIO(updateCoroutine);
    CoroutineUtils.runIO(deleteCoroutine);
  }

  private void deleteAit() {
    AitDBHelper.getInstance(mContext).openWrite();
    List<String> sessionList = new ArrayList<>();
    for (AitInfo info : deleteList) {
      sessionList.add(info.getSessionId());
      ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "deleteAit:" + info.getSessionId());
    }
    AitDBHelper.getInstance(mContext).deleteWithSessionId(sessionList.toArray(new String[0]));
    deleteList.clear();
  }

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

  private CoroutineUtils.CoroutineCallback<Void> updateCoroutine =
      new CoroutineUtils.CoroutineCallback<Void>() {
        @Override
        public Void runIO() {
          updateAit();
          return null;
        }

        @Override
        public void runMain(Void param) {}
      };

  private CoroutineUtils.CoroutineCallback<Void> deleteCoroutine =
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
