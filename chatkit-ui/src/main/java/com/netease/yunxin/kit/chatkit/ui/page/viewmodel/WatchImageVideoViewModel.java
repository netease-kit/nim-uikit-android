// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatMessageRepo;
import com.netease.yunxin.kit.chatkit.repo.ChatServiceObserverRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im.model.EventObserver;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;

/**
 * Watch image or video info view model update image/video file downloading progress for watch
 * image/video page
 */
public class WatchImageVideoViewModel extends BaseViewModel {
  private static final String TAG = "WatchImageVideo";

  private final MutableLiveData<FetchResult<IMMessage>> statusMessageLiveData =
      new MutableLiveData<>();
  private final FetchResult<IMMessage> statusMessageResult = new FetchResult<>(LoadStatus.Finish);

  private final EventObserver<IMMessageInfo> msgStatusObserver =
      new EventObserver<IMMessageInfo>() {
        @Override
        public void onEvent(@Nullable IMMessageInfo msg) {
          if (msg == null) {
            return;
          }

          if (isFileHasDownloaded(msg.getMessage())) {
            onDownloadSuccess(msg.getMessage());
          } else if (msg.getMessage().getAttachStatus() == AttachStatusEnum.fail) {
            onDownloadFail(msg.getMessage());
          }
        }
      };

  public WatchImageVideoViewModel() {
    registerObservers(true);
  }

  /** image/video message download status live data */
  public MutableLiveData<FetchResult<IMMessage>> getStatusMessageLiveData() {
    return statusMessageLiveData;
  }

  private void registerObservers(boolean register) {
    ChatServiceObserverRepo.observeMsgStatus(msgStatusObserver, register);
  }

  private boolean isFileHasDownloaded(final IMMessage message) {
    return message.getAttachStatus() == AttachStatusEnum.transferred
        && !TextUtils.isEmpty(((FileAttachment) message.getAttachment()).getPath());
  }

  public void requestFile(IMMessage message) {
    ALog.i(TAG, "request file");
    if (isFileHasDownloaded(message)) {
      ALog.i(TAG, "request file has downloaded.");
      //onDownloadSuccess(message);
      return;
    }

    onDownloadStart(message);
    downloadAttachment(message);
  }

  private void onDownloadStart(IMMessage message) {
    ALog.i(
        TAG,
        "on download start -->> " + (((FileAttachment) message.getAttachment()).getPathForSave()));
    if (((FileAttachment) message.getAttachment()).getPath() == null) {
      statusMessageResult.setLoadStatus(LoadStatus.Loading);
    } else {
      statusMessageResult.setLoadStatus(LoadStatus.Finish);
    }
    statusMessageResult.setData(message);
    statusMessageResult.setType(FetchResult.FetchType.Update);
    statusMessageResult.setTypeIndex(-1);
    statusMessageLiveData.postValue(statusMessageResult);
  }

  private void onDownloadSuccess(IMMessage message) {
    ALog.i(
        TAG, "on download success -->> " + (((FileAttachment) message.getAttachment()).getPath()));
    statusMessageResult.setLoadStatus(LoadStatus.Success);
    statusMessageResult.setData(message);
    statusMessageResult.setType(FetchResult.FetchType.Update);
    statusMessageResult.setTypeIndex(-1);
    statusMessageLiveData.postValue(statusMessageResult);
  }

  private void onDownloadFail(IMMessage message) {
    ALog.i(TAG, "on download fail -->> " + (((FileAttachment) message.getAttachment()).getPath()));
    statusMessageResult.setLoadStatus(LoadStatus.Error);
    statusMessageResult.setData(message);
    statusMessageResult.setType(FetchResult.FetchType.Update);
    statusMessageResult.setTypeIndex(-1);
    statusMessageLiveData.postValue(statusMessageResult);
  }

  public void downloadAttachment(IMMessage message) {
    ChatMessageRepo.downloadAttachment(
        message,
        false,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(TAG, "download success");
            onDownloadSuccess(message);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(TAG, "download failed code:" + code);
            onDownloadFail(message);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(TAG, "download exception");
            onDownloadFail(message);
          }
        });
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    registerObservers(false);
  }
}
