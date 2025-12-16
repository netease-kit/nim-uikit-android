// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.fun.dialog;

import android.app.Activity;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.listener.MediaChooseConfig;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.common.ui.photo.BasePhotoChoiceDialog;
import com.netease.yunxin.kit.common.utils.model.LocalFileInfo;
import com.netease.yunxin.kit.corekit.coexist.im2.extend.FetchCallback;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunDialogPhotoChoiceBinding;
import java.io.File;
import java.util.ArrayList;
import org.jetbrains.annotations.Nullable;

/**
 * 娱乐版群头像选择弹窗，差异化UI展示
 *
 * <p>
 */
public class FunPhotoChoiceDialog extends BasePhotoChoiceDialog {
  private Activity mActivity;

  public FunPhotoChoiceDialog(@NonNull Activity activity) {
    super(activity, R.style.FunBottomDialogTheme);
    mActivity = activity;
  }

  @Override
  protected View initViewAndGetRootView() {
    FunDialogPhotoChoiceBinding binding = FunDialogPhotoChoiceBinding.inflate(getLayoutInflater());
    takePhotoView = binding.tvTakePhoto;
    getFromAlbumView = binding.tvGetFromAlbum;
    cancelView = binding.tvCancel;
    return binding.getRoot();
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
                public void onSuccess(@Nullable ArrayList<LocalFileInfo> localFileInfos) {
                  if (localFileInfos != null && localFileInfos.size() > 0) {
                    File file = new File(localFileInfos.get(0).getPath());
                    if (file.exists()) {
                      FunPhotoChoiceDialog.this.onSuccess(file);
                    } else {
                      FunPhotoChoiceDialog.this.onFailed(-1);
                    }
                  }
                }

                @Override
                public void onError(int errorCode, @Nullable String errorMsg) {
                  if (callback != null) {
                    FunPhotoChoiceDialog.this.onFailed(errorCode);
                  }
                }
              });
    } else {
      super.onGetFromAlbumClick(view);
    }
  }
}
