// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.photo;

import android.app.Activity;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.R;
import com.netease.yunxin.kit.common.ui.databinding.CommonDialogPhotoChoiceBinding;

/** This dialog supports to take photos or get a image from album and upload to nos service. */
public class PhotoChoiceDialog extends BasePhotoChoiceDialog {
  public PhotoChoiceDialog(@NonNull Activity activity) {
    super(activity, R.style.BottomDialogTheme);
  }

  @Override
  protected View initViewAndGetRootView() {
    CommonDialogPhotoChoiceBinding binding =
        CommonDialogPhotoChoiceBinding.inflate(getLayoutInflater());
    takePhotoView = binding.tvTakePhoto;
    getFromAlbumView = binding.tvGetFromAlbum;
    cancelView = binding.tvCancel;
    return binding.getRoot();
  }
}
