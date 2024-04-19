// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.normal.dialog;

import android.app.Activity;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamIdentifyChoiceDialogBinding;
import com.netease.yunxin.kit.teamkit.ui.dialog.BaseTeamIdentifyDialog;

/** 设置权限时，群身份选择对话框 */
public class TeamIdentifyDialog extends BaseTeamIdentifyDialog {

  public TeamIdentifyDialog(@NonNull Activity activity) {
    super(activity, R.style.BottomDialogTheme);
  }

  @Override
  protected View initViewAndGetRootView() {
    TeamIdentifyChoiceDialogBinding binding =
        TeamIdentifyChoiceDialogBinding.inflate(getLayoutInflater());
    tvTeamAllMember = binding.tvTeamAllMember;
    tvTeamOwner = binding.tvTeamOwner;
    tvCancel = binding.tvCancel;
    return binding.getRoot();
  }
}
