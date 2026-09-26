// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.search.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.netease.nimlib.sdk.search.model.RecordHitInfo;
import com.netease.yunxin.kit.chatkit.model.TeamSearchInfo;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.common.ui.widgets.HitHighlightTextView;
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
      setHitText(viewBinding.tvNickName, searchInfo.getTeam().getName(), searchInfo.getHitInfo());
      viewBinding.tvName.clearHitText("");
      viewBinding.tvNickName.setVisibility(View.VISIBLE);
      viewBinding.tvName.setVisibility(View.GONE);
      viewBinding.getRoot().setOnClickListener(v -> itemListener.onClick(v, data, position));
    }
  }

  private void setHitText(HitHighlightTextView textView, String text, RecordHitInfo hitInfo) {
    if (hitInfo == null) {
      textView.clearHitText(text);
      return;
    }
    textView.setHitText(
        text,
        hitInfo.start,
        hitInfo.end,
        ContextCompat.getColor(
            viewBinding.getRoot().getContext(), R.color.color_contact_blue_primary));
  }
}
