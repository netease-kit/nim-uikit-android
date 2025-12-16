// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.coexist.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.coexist.sdk.v2.message.attachment.V2NIMMessageFileAttachment;
import com.netease.nimlib.coexist.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.common.PermissionHelper;
import com.netease.yunxin.kit.common.ui.utils.Permission;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.ThreadUtils;
import com.netease.yunxin.kit.common.utils.storage.ExternalStorage;
import com.netease.yunxin.kit.corekit.coexist.im2.extend.ProgressFetchCallback;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

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
    return message.getAttachment() instanceof V2NIMMessageFileAttachment
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
    ALog.d(LIB_TAG, TAG, "on download fail ");
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

  public void saveMedia(Context context, V2NIMMessage currentMsg) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      saveToLocal(currentMsg);
    } else {
      // 根据系统版本判断，如果是Android13则采用Manifest.permission.READ_MEDIA_IMAGES
      String pressionStorage = PermissionHelper.STORAGE;
      Permission.requirePermissions(context, pressionStorage)
          .request(
              new Permission.PermissionCallback() {
                @Override
                public void onGranted(List<String> permissionsGranted) {
                  if (new HashSet<>(permissionsGranted)
                      .containsAll(Arrays.asList(pressionStorage))) {
                    saveToLocal(currentMsg);
                  } else {
                    ToastX.showShortToast(R.string.permission_default);
                  }
                }

                @Override
                public void onDenial(
                    List<String> permissionsDenial, List<String> permissionDenialForever) {
                  ToastX.showShortToast(R.string.permission_default);
                }

                @Override
                public void onException(Exception exception) {
                  ToastX.showShortToast(R.string.permission_default);
                }
              });
    }
  }

  public void saveToLocal(V2NIMMessage currentMsg) {
    if (currentMsg.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_IMAGE) {
      ThreadUtils.execute(
          new Runnable() {
            @Override
            public void run() {
              String path = MessageHelper.getMessageAttachPath(currentMsg);
              if (TextUtils.isEmpty(path)) {
                ALog.e(TAG, "save image -->> path is null");
                return;
              }
              ALog.d(TAG, "save path:" + path);
              boolean isSuccess = ExternalStorage.savePictureFile(new File(path));
              if (isSuccess) {
                ToastX.showShortToast(R.string.chat_message_image_save);
              } else {
                ToastX.showShortToast(R.string.chat_message_image_save_fail);
              }
            }
          });
    } else if (currentMsg.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_VIDEO) {
      ThreadUtils.execute(
          new Runnable() {
            @Override
            public void run() {
              String path = MessageHelper.getMessageAttachPath(currentMsg);
              if (TextUtils.isEmpty(path)) {
                ALog.e(TAG, "save video -->> path is null");
                return;
              }
              ALog.d(TAG, "save path:" + path);
              boolean isSuccess = ExternalStorage.saveVideoFile(new File(path));
              if (isSuccess) {
                ToastX.showShortToast(R.string.chat_message_video_save);
              } else {
                ToastX.showShortToast(R.string.chat_message_video_save_fail);
              }
            }
          });
    }
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    registerObservers(false);
  }
}
