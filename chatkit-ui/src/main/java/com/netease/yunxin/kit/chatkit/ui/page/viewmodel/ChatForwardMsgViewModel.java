// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatObserverRepo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.EncryptUtils;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChatForwardMsgViewModel extends BaseViewModel {

  private final String TAG = "ChatForwardMsgViewModel";
  private String filePath = IMKitClient.getSDKStorageDirPath() + "/multiMsg/";

  private final MutableLiveData<FetchResult<List<ChatMessageBean>>> messageLiveData =
      new MutableLiveData<>();

  // 附件下载进度LiveData
  private final MutableLiveData<FetchResult<AttachmentProgress>> attachmentProgressMutableLiveData =
      new MutableLiveData<>();

  public MutableLiveData<FetchResult<List<ChatMessageBean>>> getMessageLiveData() {
    return messageLiveData;
  }

  public MutableLiveData<FetchResult<AttachmentProgress>> getAttachmentProgressMutableLiveData() {
    return attachmentProgressMutableLiveData;
  }

  private final Observer<AttachmentProgress> attachmentProgressObserver =
      attachmentProgress -> {
        ALog.d(
            LIB_TAG,
            TAG,
            "attachment progress update -->> "
                + attachmentProgress.getTransferred()
                + "/"
                + attachmentProgress.getTotal());
        FetchResult<AttachmentProgress> result = new FetchResult<>(LoadStatus.Finish);
        result.setData(attachmentProgress);
        result.setType(FetchResult.FetchType.Update);
        result.setTypeIndex(-1);
        attachmentProgressMutableLiveData.setValue(result);
      };

  public ChatForwardMsgViewModel() {
    ChatObserverRepo.registerAttachmentProgressObserve(attachmentProgressObserver);
  }

  public void downloadFile(IMMessageInfo messageInfo) {
    if (messageInfo != null
        && messageInfo.getMessage().getAttachment() instanceof MultiForwardAttachment) {
      MultiForwardAttachment attachment =
          (MultiForwardAttachment) messageInfo.getMessage().getAttachment();
      String fileName = messageInfo.getMessage().getUuid() + ".txt";
      if (!NetworkUtils.isConnected()) {
        File file = new File(filePath + fileName);
        if (!file.exists()) {
          ALog.d(LIB_TAG, TAG, "downloadFile network disconnect and file empty -->> ");
          FetchResult<List<ChatMessageBean>> result = new FetchResult<>(LoadStatus.Error, null);
          messageLiveData.setValue(result);
          return;
        }
      }
      ALog.d(LIB_TAG, TAG, "downloadFile  start downloadMultiMsg");
      ChatRepo.downloadMultiMsg(
          attachment.url,
          filePath + fileName,
          new FetchCallback<List<IMMessageInfo>>() {
            @Override
            public void onSuccess(@Nullable List<IMMessageInfo> param) {
              File file = new File(filePath + fileName);
              if (!TextUtils.equals(attachment.md5, EncryptUtils.md5(file))) {
                FetchResult<List<ChatMessageBean>> result =
                    new FetchResult<>(LoadStatus.Error, null);
                messageLiveData.setValue(result);
              } else {
                if (param != null && param.size() > 0) {
                  ChatUtils.sortMsgByTime(param);
                  List<ChatMessageBean> messageBeanList = new ArrayList<>();
                  for (IMMessageInfo info : param) {
                    messageBeanList.add(new ChatMessageBean(info));
                  }
                  FetchResult<List<ChatMessageBean>> result =
                      new FetchResult<>(LoadStatus.Success, messageBeanList);
                  messageLiveData.setValue(result);
                }
              }
            }

            @Override
            public void onFailed(int code) {
              FetchResult<List<ChatMessageBean>> result = new FetchResult<>(LoadStatus.Error, null);
              messageLiveData.setValue(result);
            }

            @Override
            public void onException(@Nullable Throwable exception) {
              FetchResult<List<ChatMessageBean>> result = new FetchResult<>(LoadStatus.Error, null);
              messageLiveData.setValue(result);
            }
          });
    }
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    ChatObserverRepo.unregisterAttachmentProgressObserve(attachmentProgressObserver);
  }
}
