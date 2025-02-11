// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.fun.dialog;

import android.app.Activity;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunDialogPhotoChoiceBinding;
import com.netease.yunxin.kit.teamkit.ui.dialog.BasePhotoChoiceDialog;

/** This dialog supports to take photos or get a image from album and upload to nos service. */
public class FunPhotoChoiceDialog extends BasePhotoChoiceDialog {
  public FunPhotoChoiceDialog(@NonNull Activity activity) {
    super(activity, R.style.FunBottomDialogTheme);
  }

  @Override
  protected View initViewAndGetRootView() {
    FunDialogPhotoChoiceBinding binding = FunDialogPhotoChoiceBinding.inflate(getLayoutInflater());
    takePhotoView = binding.tvTakePhoto;
    getFromAlbumView = binding.tvGetFromAlbum;
    cancelView = binding.tvCancel;
    return binding.getRoot();
  }
}
