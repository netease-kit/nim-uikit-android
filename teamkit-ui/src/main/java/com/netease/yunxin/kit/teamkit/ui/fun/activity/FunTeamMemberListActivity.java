// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.fun.activity;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamMemberListActivity;
import com.netease.yunxin.kit.teamkit.ui.adapter.BaseTeamMemberListAdapter;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamMemberListActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamMemberListItemBinding;
import com.netease.yunxin.kit.teamkit.ui.fun.adapter.FunTeamMemberListAdapter;

/**
 * 娱乐版群成员列表页面，差异化UI展示
 *
 * <p>
 */
public class FunTeamMemberListActivity extends BaseTeamMemberListActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_ededed);
  }

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    FunTeamMemberListActivityBinding binding =
        FunTeamMemberListActivityBinding.inflate(getLayoutInflater());
    ivBack = binding.viewTitle.getBackImageView();
    ivClear = binding.ivClear;
    groupEmpty = binding.groupEmtpy;
    rvMemberList = binding.rvMemberList;
    tvTitle = binding.viewTitle.getTitleTextView();
    rvMemberList.addItemDecoration(
        new RecyclerView.ItemDecoration() {
          @Override
          public void getItemOffsets(
              @NonNull Rect outRect,
              @NonNull View view,
              @NonNull RecyclerView parent,
              @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
          }

          @Override
          public void onDraw(
              @NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.onDraw(c, parent, state);
          }
        });
    etSearch = binding.etSearch;
    return binding.getRoot();
  }

  @Override
  protected BaseTeamMemberListAdapter<? extends ViewBinding> getMemberListAdapter(
      V2NIMTeamType typeEnum) {
    return new FunTeamMemberListAdapter(this, typeEnum, FunTeamMemberListItemBinding.class);
  }
}
