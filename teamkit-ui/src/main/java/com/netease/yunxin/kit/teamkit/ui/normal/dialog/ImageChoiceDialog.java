// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.normal.dialog;

import android.app.Activity;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.listener.MediaChooseConfig;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.common.ui.photo.PhotoChoiceDialog;
import com.netease.yunxin.kit.common.utils.model.LocalFileInfo;
import com.netease.yunxin.kit.corekit.coexist.im2.extend.FetchCallback;
import java.io.File;
import java.util.ArrayList;
import org.jetbrains.annotations.Nullable;

public class ImageChoiceDialog extends PhotoChoiceDialog {
  private Activity mActivity;

  public ImageChoiceDialog(@NonNull Activity activity) {
    super(activity);
    mActivity = activity;
  }

  @Override
  public void onGetFromAlbumClick(View view) {
    if (ChatKitClient.getPictureChooseEngine() != null) {

      MediaChooseConfig config = new MediaChooseConfig();
      config.setMaxSelectCount(1);
      config.setOnlyImage(true);
      config.setOriginalImage(false);
      ChatKitClient.getPictureChooseEngine()
          .onStartPictureChoose(
              mActivity,
              null,
              config,
              new FetchCallback<ArrayList<LocalFileInfo>>() {
                @Override
                public void onError(int errorCode, @Nullable String errorMsg) {
                  if (callback != null) {
                    ImageChoiceDialog.this.onFailed(errorCode);
                  }
                }

                @Override
                public void onSuccess(ArrayList<LocalFileInfo> localFileInfos) {
                  if (localFileInfos != null && localFileInfos.size() > 0) {
                    File file = new File(localFileInfos.get(0).getPath());
                    if (file.exists()) {
                      ImageChoiceDialog.this.onSuccess(file);
                    } else {
                      ImageChoiceDialog.this.onFailed(-1);
                    }
                  }
                }
              });
      //      IMKitCustomFactory.getMessageChooseListener()
      //          .onImageChoose(
      //              mActivity,
      //              config,
      //              new FetchCallback<ArrayList<LocalFileInfo>>() {
      //                @Override
      //                public void onError(int errorCode, @Nullable String errorMsg) {
      //                  if (callback != null) {
      //                    ImageChoiceDialog.this.onFailed(errorCode);
      //                  }
      //                }
      //
      //                @Override
      //                public void onSuccess(ArrayList<LocalFileInfo> localFileInfos) {
      //                  if (localFileInfos != null && localFileInfos.size() > 0) {
      //                    File file = new File(localFileInfos.get(0).getPath());
      //                    if (file.exists()) {
      //                      ImageChoiceDialog.this.onSuccess(file);
      //                    } else {
      //                      ImageChoiceDialog.this.onFailed(-1);
      //                    }
      //                  }
      //                }
      //              });
    } else {
      super.onGetFromAlbumClick(view);
    }
  }
}
