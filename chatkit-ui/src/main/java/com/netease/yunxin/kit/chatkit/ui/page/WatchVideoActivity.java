// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import com.netease.nimlib.coexist.sdk.v2.message.V2NIMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.view.media.SimpleVideoPlayer;

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
    viewModel.saveMedia(this, message);
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
