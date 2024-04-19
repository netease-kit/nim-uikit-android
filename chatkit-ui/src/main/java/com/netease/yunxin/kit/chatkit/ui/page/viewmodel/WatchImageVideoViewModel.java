// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageFileAttachment;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageAttachmentUploadState;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im2.extend.ProgressFetchCallback;

/**
 * Watch image or video info view model update image/video file downloading progress for watch
 * image/video page
 */
public class WatchImageVideoViewModel extends BaseViewModel {
  private static final String TAG = "WatchImageVideo";

  private final MutableLiveData<FetchResult<V2NIMMessage>> statusMessageLiveData =
      new MutableLiveData<>();
  private final FetchResult<V2NIMMessage> statusMessageResult =
      new FetchResult<>(LoadStatus.Finish);

  public WatchImageVideoViewModel() {
    registerObservers(true);
  }

  /** image/video message download status live data */
  public MutableLiveData<FetchResult<V2NIMMessage>> getStatusMessageLiveData() {
    return statusMessageLiveData;
  }

  private void registerObservers(boolean register) {}

  private boolean isFileHasDownloaded(final V2NIMMessage message) {
    return message.getAttachmentUploadState()
            == V2NIMMessageAttachmentUploadState.V2NIM_MESSAGE_ATTACHMENT_UPLOAD_STATE_SUCCEEDED
        && !TextUtils.isEmpty(((V2NIMMessageFileAttachment) message.getAttachment()).getPath());
  }

  public void requestFile(V2NIMMessage message) {
    ALog.d(
        LIB_TAG, TAG, "requestFile:" + (message == null ? "null" : message.getMessageClientId()));
    if (isFileHasDownloaded(message)) {
      ALog.d(LIB_TAG, TAG, "request file has downloaded.");
      //onDownloadSuccess(message);
      return;
    }

    onDownloadStart(message);
    downloadAttachment(message);
  }

  private void onDownloadStart(V2NIMMessage message) {
    ALog.d(
        LIB_TAG,
        TAG,
        "onDownloadStart :" + (message == null ? "null" : message.getMessageClientId()));
    if (((V2NIMMessageFileAttachment) message.getAttachment()).getPath() == null) {
      statusMessageResult.setLoadStatus(LoadStatus.Loading);
    } else {
      statusMessageResult.setLoadStatus(LoadStatus.Finish);
    }
    statusMessageResult.setData(message);
    statusMessageResult.setType(FetchResult.FetchType.Update);
    statusMessageResult.setTypeIndex(-1);
    statusMessageLiveData.postValue(statusMessageResult);
  }

  private void onDownloadSuccess(V2NIMMessage message) {
    ALog.d(TAG, "on download success -->> ");
    statusMessageResult.setLoadStatus(LoadStatus.Success);
    statusMessageResult.setData(message);
    statusMessageResult.setType(FetchResult.FetchType.Update);
    statusMessageResult.setTypeIndex(-1);
    statusMessageLiveData.postValue(statusMessageResult);
  }

  private void onDownloadFail(V2NIMMessage message) {
    ALog.d(
        LIB_TAG,
        TAG,
        "on download fail -->> " + (((FileAttachment) message.getAttachment()).getPath()));
    statusMessageResult.setLoadStatus(LoadStatus.Error);
    statusMessageResult.setData(message);
    statusMessageResult.setType(FetchResult.FetchType.Update);
    statusMessageResult.setTypeIndex(-1);
    statusMessageLiveData.postValue(statusMessageResult);
  }

  public void downloadAttachment(V2NIMMessage message) {
    String path = MessageHelper.getMessageAttachPath(message);
    if (path == null) {
      return;
    }
    //downloadAttachment
    ChatRepo.downloadAttachment(
        message,
        path,
        new ProgressFetchCallback<String>() {
          @Override
          public void onProgress(int progress) {}

          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(TAG, "download error");
            onDownloadFail(message);
          }

          @Override
          public void onSuccess(@Nullable String param) {
            ALog.d(TAG, "download success");
            onDownloadSuccess(message);
          }
        });
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    registerObservers(false);
  }
}
