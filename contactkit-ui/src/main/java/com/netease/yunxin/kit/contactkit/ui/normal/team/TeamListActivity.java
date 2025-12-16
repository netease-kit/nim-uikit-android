// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.team;

import android.content.Intent;
import android.view.ViewGroup;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.contactkit.ui.normal.view.ContactViewHolderFactory;
import com.netease.yunxin.kit.contactkit.ui.normal.view.viewholder.TeamListViewHolder;
import com.netease.yunxin.kit.contactkit.ui.team.BaseTeamListActivity;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

public class TeamListActivity extends BaseTeamListActivity {

  @Override
  protected void configViewHolderFactory() {
    binding.contactListView.setViewHolderFactory(
        new ContactViewHolderFactory() {
          @Override
          protected BaseContactViewHolder getCustomViewHolder(ViewGroup view, int viewType) {
            if (viewType == IViewTypeConstant.CONTACT_TEAM_LIST) {
              TeamListViewHolder viewHolder = new TeamListViewHolder(view);
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
                          .withContext(TeamListActivity.this)
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
