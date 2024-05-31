// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.view.media.SimpleVideoPlayer;
import com.netease.yunxin.kit.common.ui.utils.Permission;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.storage.ExternalStorage;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/** 视频消息查看器 不支持左右滑动，只支持查看当前视频消息 */
public class WatchVideoActivity extends WatchBaseActivity {
  private static final String TAG = "WatchVideoActivity";

  public static final String EXT_MESSAGE_VIDEO_KEY = "EXT_MESSAGE_VIDEO_KEY";

  private SimpleVideoPlayer simpleVideoPlayer;
  // 视频消息，由消息页面跳转传入
  private V2NIMMessage message;
  // 保存视频权限，根据系统版本判断，如果是Android13则采用Manifest.permission.READ_MEDIA_VIDEO
  protected String[] permissionForAlbum;

  public static void launch(Context context, V2NIMMessage message) {
    Intent intent = new Intent(context, WatchVideoActivity.class);
    intent.putExtra(EXT_MESSAGE_VIDEO_KEY, message);
    context.startActivity(intent);
  }

  @Override
  public void initData(Intent intent) {
    super.initData(intent);
    if (intent != null) {
      message = (V2NIMMessage) intent.getSerializableExtra(EXT_MESSAGE_VIDEO_KEY);
    }
  }

  @Override
  public View initMediaView() {
    simpleVideoPlayer = new SimpleVideoPlayer(this);
    return simpleVideoPlayer;
  }

  @Override
  public void saveMedia() {
    String path = MessageHelper.getMessageAttachPath(message);
    if (TextUtils.isEmpty(path)) {
      ALog.e(TAG, "save video -->> path is null");
      return;
    }
    ALog.d(TAG, "save path:" + path);
    permissionForAlbum = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    // 根据系统版本判断，如果是Android13则采用Manifest.permission.READ_MEDIA_VIDEO
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      permissionForAlbum = new String[] {Manifest.permission.READ_MEDIA_VIDEO};
    }
    Permission.requirePermissions(this, permissionForAlbum)
        .request(
            new Permission.PermissionCallback() {
              @Override
              public void onGranted(List<String> permissionsGranted) {
                if (new HashSet<>(permissionsGranted)
                    .containsAll(Arrays.asList(permissionForAlbum))) {
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
