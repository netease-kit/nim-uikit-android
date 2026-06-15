// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.robot;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunRobotBindItemLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.robot.RobotBindAdapter;

/** Fun style robot binding list adapter. */
public class FunRobotBindAdapter extends RobotBindAdapter {

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    FunRobotBindItemLayoutBinding binding =
        FunRobotBindItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    return new ViewHolder(binding.avatarView, binding.tvName, binding.tvSubtitle, binding.getRoot());
  }
}
