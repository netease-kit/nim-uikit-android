// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.fun.dialog;

import android.app.Activity;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamIdentifyChoiceDialogBinding;
import com.netease.yunxin.kit.teamkit.ui.dialog.BaseTeamIdentifyDialog;

public class FunTeamIdentifyDialog extends BaseTeamIdentifyDialog {

  public FunTeamIdentifyDialog(@NonNull Activity activity) {
    super(activity, R.style.FunBottomDialogTheme);
  }

  @Override
  protected View initViewAndGetRootView() {
    FunTeamIdentifyChoiceDialogBinding binding =
        FunTeamIdentifyChoiceDialogBinding.inflate(getLayoutInflater());
    tvTeamAllMember = binding.tvTeamAllMember;
    tvTeamOwner = binding.tvTeamOwner;
    tvCancel = binding.tvCancel;
    return binding.getRoot();
  }
}
