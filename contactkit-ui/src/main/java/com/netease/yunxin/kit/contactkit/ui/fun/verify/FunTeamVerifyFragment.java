// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.verify;

import android.view.ViewGroup;
import com.netease.yunxin.kit.contactkit.ui.IContactFactory;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.fun.view.viewholder.FunTeamVerifyInfoViewHolder;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.contactkit.ui.model.TeamVerifyInfoBean;
import com.netease.yunxin.kit.contactkit.ui.normal.view.ContactViewHolderFactory;
import com.netease.yunxin.kit.contactkit.ui.utils.ContactUtils;
import com.netease.yunxin.kit.contactkit.ui.verify.TeamVerifyBaseFragment;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;

public class FunTeamVerifyFragment extends TeamVerifyBaseFragment {

  @Override
  protected IContactFactory configViewHolderFactory() {
    return new ContactViewHolderFactory() {
      @Override
      protected BaseContactViewHolder getCustomViewHolder(ViewGroup view, int viewType) {
        if (viewType == IViewTypeConstant.CONTACT_TEAM_VERIFY_INFO) {
          FunTeamVerifyInfoViewHolder viewHolder = new FunTeamVerifyInfoViewHolder(view);
          viewHolder.setVerifyListener(
              new FunTeamVerifyInfoViewHolder.VerifyListener() {
                @Override
                public void onAccept(TeamVerifyInfoBean bean) {
                  if (ContactUtils.checkNetworkAndToast(
                      FunTeamVerifyFragment.this.requireContext())) {
                    viewModel.acceptJoinApplication(bean);
                  }
                }

                @Override
                public void onReject(TeamVerifyInfoBean bean) {
                  if (ContactUtils.checkNetworkAndToast(
                      FunTeamVerifyFragment.this.requireContext())) {
                    viewModel.rejectJoinApplication(bean);
                  }
                }
              });
          return viewHolder;
        }
        return null;
      }
    };
  }

  protected int getEmptyStateViewRes() {
    return R.drawable.fun_ic_contact_empty;
  }
}
