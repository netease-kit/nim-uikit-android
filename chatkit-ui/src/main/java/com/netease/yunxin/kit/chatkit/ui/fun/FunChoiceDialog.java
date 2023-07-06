// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChoiceDialogLayoutBinding;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import com.netease.yunxin.kit.common.ui.dialog.BaseBottomChoiceDialog;
import java.util.ArrayList;

public class FunChoiceDialog extends BaseBottomChoiceDialog {

  public FunChoiceDialog(@NonNull Context context, @NonNull ArrayList<ActionItem> list) {
    super(context, list);
  }

  @Nullable
  @Override
  protected View initViewAndGetRootView() {
    FunChoiceDialogLayoutBinding viewBinding =
        FunChoiceDialogLayoutBinding.inflate(LayoutInflater.from(getContext()), null, false);
    setCancelTv(viewBinding.tvCancel);
    setContainerView(viewBinding.actionContainer);
    return viewBinding.getRoot();
  }
}
