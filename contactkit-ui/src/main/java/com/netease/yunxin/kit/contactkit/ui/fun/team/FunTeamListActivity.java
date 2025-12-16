// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.team;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.BaseListActivityLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.fun.view.FunContactViewHolderFactory;
import com.netease.yunxin.kit.contactkit.ui.fun.view.viewholder.FunTeamListViewHolder;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.contactkit.ui.team.BaseTeamListActivity;
import com.netease.yunxin.kit.contactkit.ui.team.TeamListViewModel;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

public class FunTeamListActivity extends BaseTeamListActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_ededed);
  }

  protected void configTitle(BaseListActivityLayoutBinding binding) {
    super.configTitle(binding);
    binding.title.getTitleTextView().setTextSize(17);
    binding.title.getTitleTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
    binding.title.setBackgroundResource(R.color.color_ededed);
  }

  protected void configRoutePath(TeamListViewModel viewModel) {
    viewModel.configRoutePath(RouterConstant.PATH_FUN_CHAT_TEAM_PAGE);
  }

  @Override
  protected void configViewHolderFactory() {
    binding.contactListView.setViewHolderFactory(
        new FunContactViewHolderFactory() {
          @Override
          protected BaseContactViewHolder getCustomViewHolder(ViewGroup view, int viewType) {
            if (viewType == IViewTypeConstant.CONTACT_TEAM_LIST) {
              FunTeamListViewHolder viewHolder = new FunTeamListViewHolder(view);
              viewHolder.setItemClickListener(
                  data -> {
                    if (isSelector) {
                      Intent result = new Intent();
                      result.putExtra(RouterConstant.KEY_TEAM_ID, data.data.getTeamId());
                      setResult(RESULT_OK, result);
                      finish();
                    } else {
                      XKitRouter.withKey(data.router)
                          .withParam(RouterConstant.CHAT_KRY, data.data)
                          .withContext(FunTeamListActivity.this)
                          .navigate();
                    }
                  });
              return viewHolder;
            }
            return null;
          }
        });
  }
}
