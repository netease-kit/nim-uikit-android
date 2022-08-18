// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im.model.EventObserver;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.qchatkit.repo.QChatMessageRepo;
import com.netease.yunxin.kit.qchatkit.repo.QChatServiceObserverRepo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatMessageInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatSendMessageInfo;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import java.util.List;

/** message view model fetch and send message to channel */
public class MessageViewModel extends BaseViewModel {

  private static final String TAG = "MessageViewModel";
  private final MutableLiveData<FetchResult<List<QChatMessageInfo>>> messageLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<QChatMessageInfo>> messageFetchResult =
      new FetchResult<>(LoadStatus.Finish);

  private final MutableLiveData<FetchResult<QChatMessageInfo>> sendMessageLiveData =
      new MutableLiveData<>();
  private final FetchResult<QChatMessageInfo> sendMessageFetchResult =
      new FetchResult<>(LoadStatus.Finish);

  private QChatMessageInfo forwardMessage;
  private boolean hasForward = true;
  private long mServerId;
  private long mChannelId;

  private int messagePageSize = 100;

  //fetch message live data
  public MutableLiveData<FetchResult<List<QChatMessageInfo>>> getQueryMessageLiveData() {
    return messageLiveData;
  }

  //send message live data
  public MutableLiveData<FetchResult<QChatMessageInfo>> getSendMessageLiveData() {
    return sendMessageLiveData;
  }

  public void init(long serverId, long channelId) {
    mServerId = serverId;
    mChannelId = channelId;
    ALog.d(TAG, "init", "info:" + mServerId + "," + mChannelId);
    registerMessageObserver();
  }

  public void registerMessageObserver() {
    QChatServiceObserverRepo.observeReceiveMessage(
        mServerId, mChannelId, receiveMessageObserver, true);
  }

  public void fetchMessageList() {
    queryMessage(0, 0, false);
  }

  public void fetchForwardMessage(QChatMessageInfo messageInfo) {
    forwardMessage = messageInfo;
    queryMessage(0, messageInfo.getTime(), false);
  }

  public void fetchBackwardMessage(QChatMessageInfo messageInfo) {
    queryMessage(messageInfo.getTime(), 0, true);
  }

  private void queryMessage(long fromTime, long toTime, boolean reverse) {
    ALog.d(TAG, "queryMessage", "info:" + fromTime + "," + toTime);
    QChatMessageRepo.fetchMessageHistory(
        mServerId,
        mChannelId,
        fromTime,
        toTime,
        messagePageSize,
        reverse,
        new FetchCallback<List<QChatMessageInfo>>() {
          @Override
          public void onSuccess(@Nullable List<QChatMessageInfo> param) {
            ALog.d(TAG, "queryMessage", "onSuccess");
            if (fromTime == 0 && toTime == 0) {
              messageFetchResult.setLoadStatus(LoadStatus.Success);
            } else if (fromTime > 0 && toTime == 0) {
              messageFetchResult.setFetchType(FetchResult.FetchType.Add);
              messageFetchResult.setTypeIndex(-1);
            } else if (fromTime == 0 && toTime > 0) {
              messageFetchResult.setFetchType(FetchResult.FetchType.Add);
              messageFetchResult.setTypeIndex(0);
              if (param == null || param.size() < 100) {
                hasForward = false;
              }
              if (param != null
                  && param.size() > 0
                  && TextUtils.equals(
                      param.get(param.size() - 1).getUuid(), forwardMessage.getUuid())) {
                param.remove(param.size() - 1);
              }
            }
            messageFetchResult.setData(param);
            messageLiveData.postValue(messageFetchResult);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(TAG, "queryMessage", "onFailed:" + code);
            messageFetchResult.setError(code, R.string.qchat_channel_message_fetch_error);
            messageLiveData.postValue(messageFetchResult);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            String errorMsg = exception != null ? exception.getMessage() : "";
            ALog.d(TAG, "fetchMessageHistory", "onException:" + errorMsg);
            messageFetchResult.setError(
                QChatConstant.ERROR_CODE_MESSAGE_FETCH, R.string.qchat_channel_message_fetch_error);
            messageLiveData.postValue(messageFetchResult);
          }
        });
  }

  public boolean isHasForward() {
    return hasForward;
  }

  public QChatMessageInfo sendTextMessage(String content) {
    QChatSendMessageInfo sendMessageInfo =
        new QChatSendMessageInfo(mServerId, mChannelId, MsgTypeEnum.text, content);
    return sendMessage(sendMessageInfo);
  }

  public QChatMessageInfo sendImageMessage(ImageAttachment attachment) {
    QChatSendMessageInfo sendMessageInfo =
        new QChatSendMessageInfo(
            mServerId, mChannelId, MsgTypeEnum.image, null, null, attachment.toJson(false));
    return sendMessage(sendMessageInfo);
  }

  private QChatMessageInfo sendMessage(QChatSendMessageInfo sendMessageInfo) {
    return QChatMessageRepo.sendMessage(
        sendMessageInfo,
        new FetchCallback<QChatMessageInfo>() {
          @Override
          public void onSuccess(@Nullable QChatMessageInfo param) {
            ALog.d(TAG, "sendMessage", "onSuccess");
            sendMessageFetchResult.setLoadStatus(LoadStatus.Success);
            sendMessageFetchResult.setData(param);
            sendMessageLiveData.postValue(sendMessageFetchResult);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(TAG, "sendMessage", "onFailed:" + code);
            sendMessageFetchResult.setError(code, R.string.qchat_channel_message_send_error);
            if (sendMessageInfo.getMessageInfo() != null) {
              sendMessageInfo.getMessageInfo().setSendMsgStatus(MsgStatusEnum.fail);
              sendMessageFetchResult.setData(sendMessageInfo.getMessageInfo());
            }
            sendMessageLiveData.postValue(sendMessageFetchResult);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            String errorMsg = exception != null ? exception.getMessage() : "";
            ALog.d(TAG, "sendMessage", "onException:" + errorMsg);
            sendMessageFetchResult.setError(
                QChatConstant.ERROR_CODE_SEND_MESSAGE, R.string.qchat_channel_message_send_error);
            if (sendMessageInfo.getMessageInfo() != null) {
              sendMessageFetchResult.setData(sendMessageInfo.getMessageInfo());
            }
            sendMessageLiveData.postValue(sendMessageFetchResult);
          }
        });
  }

  public void makeMessageRead(QChatMessageInfo messageInfo) {
    QChatMessageRepo.markMessageRead(
        messageInfo.getQChatServerId(),
        messageInfo.getQChatChannelId(),
        messageInfo.getTime(),
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(TAG, "makeMessageRead", "onSuccess");
          }

          @Override
          public void onFailed(int code) {
            ALog.d(TAG, "makeMessageRead", "onFailed:" + code);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            String errorMsg = exception != null ? exception.getMessage() : "";
            ALog.d(TAG, "makeMessageRead", "onException:" + errorMsg);
          }
        });
  }

  private final EventObserver<List<QChatMessageInfo>> receiveMessageObserver =
      new EventObserver<List<QChatMessageInfo>>() {
        @Override
        public void onEvent(@Nullable List<QChatMessageInfo> event) {
          messageFetchResult.setLoadStatus(LoadStatus.Finish);
          messageFetchResult.setData(event);
          messageFetchResult.setType(FetchResult.FetchType.Add);
          messageFetchResult.setTypeIndex(-1);
          messageLiveData.postValue(messageFetchResult);
        }
      };
}
