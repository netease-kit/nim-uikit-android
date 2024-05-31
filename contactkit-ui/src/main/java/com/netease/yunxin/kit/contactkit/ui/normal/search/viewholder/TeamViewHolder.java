// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.search.viewholder;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.search.model.RecordHitInfo;
import com.netease.yunxin.kit.chatkit.model.TeamSearchInfo;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.SearchUserItemLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.model.SearchTeamBean;

public class TeamViewHolder extends BaseViewHolder<SearchTeamBean> {

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
  public void onBindData(SearchTeamBean data, int position) {
    if (data != null) {
      searchInfo = data.teamSearchInfo;
      viewBinding.cavUserIcon.setData(
          searchInfo.getTeam().getAvatar(),
          searchInfo.getTeam().getName(),
          AvatarColor.avatarColor(searchInfo.getTeam().getTeamId()));
      viewBinding.tvNickName.setText(
          getSelectSpanText(searchInfo.getTeam().getName(), searchInfo.getHitInfo()));
      viewBinding.tvNickName.setVisibility(View.VISIBLE);
      viewBinding.tvName.setVisibility(View.GONE);
      viewBinding.getRoot().setOnClickListener(v -> itemListener.onClick(v, data, position));
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
