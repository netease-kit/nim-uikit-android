// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageDeletedNotification;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.impl.MessageListenerImpl;
import com.netease.yunxin.kit.chatkit.listener.ChatListener;
import com.netease.yunxin.kit.chatkit.listener.MessageRevokeNotification;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im2.model.IMMessageProgress;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ChatSearchBaseViewModel extends BaseViewModel {

  private final String TAG = "ChatSearchBaseViewModel";

  protected String mConversationId;

  // 消息附件下载进度LiveData
  private final MutableLiveData<FetchResult<IMMessageProgress>> attachmentProgressMutableLiveData =
      new MutableLiveData<>();

  private final MutableLiveData<FetchResult<List<String>>> messageDeletedMutableLiveData =
      new MutableLiveData<>();
  /**
   * 获取消息附件下载进度的LiveData
   *
   * @return 消息附件下载进度的LiveData
   */
  public MutableLiveData<FetchResult<IMMessageProgress>> getAttachmentProgressLiveData() {
    return attachmentProgressMutableLiveData;
  }

  /**
   * 获取消息删除或撤回的LiveData
   *
   * @return 消息删除或撤回的LiveData
   */
  public MutableLiveData<FetchResult<List<String>>> getMessageDeletedOrRevokedLiveData() {
    return messageDeletedMutableLiveData;
  }

  private final ChatListener messageListener =
      new MessageListenerImpl() {
        @Override
        public void onMessageAttachmentDownloadProgress(
            @NonNull V2NIMMessage message, int progress) {
          if (message.getConversationId() != null
              && message.getConversationId().equals(mConversationId)) {
            ALog.d(LIB_TAG, TAG, "onMessageAttachmentDownloadProgress -->> " + progress);
            FetchResult<IMMessageProgress> result = new FetchResult<>(LoadStatus.Success);
            result.setData(new IMMessageProgress(message.getMessageClientId(), progress));
            result.setType(FetchResult.FetchType.Update);
            result.setTypeIndex(-1);
            attachmentProgressMutableLiveData.setValue(result);
          }
        }

        @Override
        public void onMessageRevokeNotifications(
            @NotNull List<@NotNull MessageRevokeNotification> revokeNotifications) {
          super.onMessageRevokeNotifications(revokeNotifications);
          FetchResult<List<String>> result = new FetchResult<>(LoadStatus.Success);
          List<String> deletedMessageClientIds = new ArrayList<>();
          for (MessageRevokeNotification notification : revokeNotifications) {
            if (notification.getNimNotification().getMessageRefer() == null) {
              continue;
            }
            String msgClientId =
                notification.getNimNotification().getMessageRefer().getMessageClientId();
            if (msgClientId != null) {
              deletedMessageClientIds.add(msgClientId);
            }
          }
          result.setData(deletedMessageClientIds);
          result.setType(FetchResult.FetchType.Remove);
          messageDeletedMutableLiveData.setValue(result);
        }

        @Override
        public void onMessageDeletedNotifications(
            @NotNull List<? extends @NotNull V2NIMMessageDeletedNotification> messages) {
          super.onMessageDeletedNotifications(messages);
          FetchResult<List<String>> result = new FetchResult<>(LoadStatus.Success);
          List<String> deletedMessageClientIds = new ArrayList<>();
          for (V2NIMMessageDeletedNotification notification : messages) {
            String msgClientId = notification.getMessageRefer().getMessageClientId();
            if (msgClientId != null) {
              deletedMessageClientIds.add(msgClientId);
            }
          }
          result.setData(deletedMessageClientIds);
          result.setType(FetchResult.FetchType.Remove);
          messageDeletedMutableLiveData.setValue(result);
        }
      };

  public void init(@NonNull String conversationId) {
    mConversationId = conversationId;
    ChatRepo.addMessageListener(messageListener);
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    ChatRepo.removeMessageListener(messageListener);
  }
}
