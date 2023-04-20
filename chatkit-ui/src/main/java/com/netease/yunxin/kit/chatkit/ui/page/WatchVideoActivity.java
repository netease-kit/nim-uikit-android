// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import com.netease.nimlib.sdk.msg.attachment.VideoAttachment;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.view.media.SimpleVideoPlayer;
import com.netease.yunxin.kit.common.ui.utils.Permission;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.storage.ExternalStorage;
import java.io.File;
import java.util.List;

/** Watch video page */
public class WatchVideoActivity extends WatchBaseActivity {
  private static final String TAG = "WatchVideoActivity";

  public static final String EXT_MESSAGE_VIDEO_KEY = "EXT_MESSAGE_VIDEO_KEY";

  private SimpleVideoPlayer simpleVideoPlayer;
  private IMMessage message;

  public static void launch(Context context, IMMessage message) {
    Intent intent = new Intent(context, WatchVideoActivity.class);
    intent.putExtra(EXT_MESSAGE_VIDEO_KEY, message);
    context.startActivity(intent);
  }

  @Override
  public void initData(Intent intent) {
    super.initData(intent);
    if (intent != null) {
      message = (IMMessage) intent.getSerializableExtra(EXT_MESSAGE_VIDEO_KEY);
    }
  }

  @Override
  public View initMediaView() {
    simpleVideoPlayer = new SimpleVideoPlayer(this);
    return simpleVideoPlayer;
  }

  @Override
  public void saveMedia() {
    VideoAttachment attachment = (VideoAttachment) message.getAttachment();
    String path = attachment.getPath();
    if (TextUtils.isEmpty(path)) {
      ALog.e(TAG, "save video -->> path is null");
      return;
    }
    ALog.d(TAG, "save path:" + path);

    Permission.requirePermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        .request(
            new Permission.PermissionCallback() {
              @Override
              public void onGranted(List<String> permissionsGranted) {
                if (permissionsGranted.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                  if (ExternalStorage.saveVideoFile(new File(path))) {
                    ToastX.showShortToast(R.string.chat_message_video_save);
                  } else {
                    ToastX.showShortToast(R.string.chat_message_video_save_fail);
                  }
                } else {
                  Toast.makeText(
                          WatchVideoActivity.this,
                          WatchVideoActivity.this
                              .getResources()
                              .getString(R.string.permission_default),
                          Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onDenial(
                  List<String> permissionsDenial, List<String> permissionDenialForever) {
                Toast.makeText(
                        WatchVideoActivity.this,
                        WatchVideoActivity.this
                            .getResources()
                            .getString(R.string.permission_default),
                        Toast.LENGTH_SHORT)
                    .show();
              }

              @Override
              public void onException(Exception exception) {
                Toast.makeText(
                        WatchVideoActivity.this,
                        WatchVideoActivity.this
                            .getResources()
                            .getString(R.string.permission_default),
                        Toast.LENGTH_SHORT)
                    .show();
              }
            });
  }

  @Override
  public void initView() {
    super.initView();
    simpleVideoPlayer.handlePlay(message);
  }

  @Override
  protected void onResume() {
    super.onResume();
    ALog.e(TAG, "onResume");
    simpleVideoPlayer.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    ALog.e(TAG, "onPause");
    simpleVideoPlayer.onPause();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    ALog.e(TAG, "onDestroy");
    simpleVideoPlayer.onDestroy();
  }
}
