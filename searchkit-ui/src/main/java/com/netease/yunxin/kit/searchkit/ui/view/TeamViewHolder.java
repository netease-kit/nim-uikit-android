// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.searchkit.ui.view;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.search.model.RecordHitInfo;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.searchkit.model.TeamSearchInfo;
import com.netease.yunxin.kit.searchkit.ui.R;
import com.netease.yunxin.kit.searchkit.ui.databinding.SearchUserItemLayoutBinding;
import com.netease.yunxin.kit.searchkit.ui.model.TeamBean;

public class TeamViewHolder extends BaseViewHolder<TeamBean> {

  private SearchUserItemLayoutBinding viewBinding;
  private TeamSearchInfo searchInfo;

  public TeamViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  public TeamViewHolder(@NonNull SearchUserItemLayoutBinding viewBinding) {
    this(viewBinding.getRoot());
    this.viewBinding = viewBinding;
  }

  @Override
  public void onBindData(TeamBean data, int position) {
    if (data != null) {
      searchInfo = data.teamSearchInfo;
      viewBinding.cavUserIcon.setData(
          searchInfo.getTeam().getIcon(),
          searchInfo.getTeam().getName(),
          AvatarColor.avatarColor(searchInfo.getTeam().getId()));
      viewBinding.tvNickName.setText(
          getSelectSpanText(searchInfo.getTeam().getName(), searchInfo.getHitInfo()));
      viewBinding.tvNickName.setVisibility(View.VISIBLE);
      viewBinding.tvName.setVisibility(View.GONE);
      viewBinding.getRoot().setOnClickListener(v -> itemListener.onClick(data, position));
    }
  }

  private SpannableString getSelectSpanText(String text, RecordHitInfo hitInfo) {
    SpannableString spannable = new SpannableString(text);
    if (hitInfo != null) {
      spannable.setSpan(
          new ForegroundColorSpan(
              viewBinding.getRoot().getContext().getResources().getColor(R.color.color_337eff)),
          hitInfo.start,
          hitInfo.end,
          Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }
    return spannable;
  }
}
