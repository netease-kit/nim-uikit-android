// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.common;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.bumptech.glide.Glide;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatMessageInfo;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatWatchPictureActivityBinding;
import java.io.File;

public class WatchImageActivity extends BaseActivity {

  private static final String TAG = WatchImageActivity.class.getSimpleName();

  private QChatWatchPictureActivityBinding viewBiding;
  private static QChatMessageInfo sMessage;

  public static void start(Context context, QChatMessageInfo message) {
    Intent intent = new Intent();
    sMessage = message;
    intent.setClass(context, WatchImageActivity.class);
    context.startActivity(intent);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    viewBiding = QChatWatchPictureActivityBinding.inflate(getLayoutInflater());
    setContentView(viewBiding.getRoot());
    changeStatusBarColor(R.color.color_black);
    initViews();
    displaySimpleImage();
  }

  private void initViews() {
    viewBiding.getRoot().setOnClickListener(view -> finish());
  }

  private void displaySimpleImage() {
    if (sMessage == null) {
      finish();
      return;
    }
    String path = ((ImageAttachment) sMessage.getAttachment()).getPath();
    String url = ((ImageAttachment) sMessage.getAttachment()).getUrl();
    if (!TextUtils.isEmpty(path)) {
      Glide.with(this)
          .load(new File(path))
          .placeholder(R.drawable.bg_image_loading_qchat)
          .into(viewBiding.simpleImageView);
      return;
    }
    if (!TextUtils.isEmpty(url)) {
      Glide.with(this)
          .load(url)
          .placeholder(R.drawable.bg_image_loading_qchat)
          .into(viewBiding.simpleImageView);
    }
  }
}
