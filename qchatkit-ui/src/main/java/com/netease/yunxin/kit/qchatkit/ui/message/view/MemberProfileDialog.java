// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.view;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.common.ui.dialog.BaseBottomDialog;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.qchat.QChatKitClient;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerMemberInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerRoleInfo;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatMemberProfileLayoutBinding;
import java.util.List;

/** Dialog to show member profile in channel message */
public class MemberProfileDialog extends BaseBottomDialog {

  private QChatMemberProfileLayoutBinding viewBinding;
  private QChatServerMemberInfo memberInfo;
  private List<QChatServerRoleInfo> roleInfoList;

  @Nullable
  @Override
  protected View getRootView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    viewBinding = QChatMemberProfileLayoutBinding.inflate(inflater, container, false);
    return viewBinding.getRoot();
  }

  public void setData(QChatServerMemberInfo member, List<QChatServerRoleInfo> roleInfoList) {
    memberInfo = member;
    this.roleInfoList = roleInfoList;
  }

  @Override
  protected void initData() {
    super.initData();
    loadData();
  }

  public void updateData(List<QChatServerRoleInfo> roleInfoList) {
    this.roleInfoList = roleInfoList;
    loadData();
  }

  private void loadData() {
    if (!TextUtils.isEmpty(memberInfo.getNick())) {
      viewBinding.qChatMemberProfileName.setText(memberInfo.getNick());
      viewBinding.qChatMemberProfileNick.setVisibility(View.VISIBLE);
      viewBinding.qChatMemberProfileNick.setText(memberInfo.getNicknameOfIM());
    } else {
      viewBinding.qChatMemberProfileName.setText(memberInfo.getNicknameOfIM());
      viewBinding.qChatMemberProfileNick.setVisibility(View.GONE);
    }

    viewBinding.qChatMemberProfileAvatar.setData(
        memberInfo.getAvatarUrl(),
        memberInfo.getNickName(),
        AvatarColor.avatarColor(memberInfo.getAccId()));
    if (roleInfoList == null || roleInfoList.size() < 1) {
      viewBinding.qChatMemberProfileNull.setVisibility(View.VISIBLE);
    } else {
      viewBinding.qChatMemberProfileNull.setVisibility(View.GONE);
      viewBinding.qChatMemberProfileFlGroup.setData(roleInfoList);
    }
    viewBinding.qChatMemberProfileAvatar.setOnClickListener(
        v -> {
          if (TextUtils.equals(memberInfo.getAccId(), QChatKitClient.account())) {
            XKitRouter.withKey(RouterConstant.PATH_MINE_INFO_PAGE)
                .withContext(v.getContext())
                .navigate();
          } else {
            XKitRouter.withKey(RouterConstant.PATH_USER_INFO_PAGE)
                .withContext(v.getContext())
                .withParam(RouterConstant.KEY_ACCOUNT_ID_KEY, memberInfo.getAccId())
                .navigate();
          }
        });
  }
}
