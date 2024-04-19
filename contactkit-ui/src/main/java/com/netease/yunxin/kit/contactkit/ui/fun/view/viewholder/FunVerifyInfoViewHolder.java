// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.view.viewholder;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunVerifyListViewHolderBinding;
import com.netease.yunxin.kit.contactkit.ui.model.BaseContactBean;
import com.netease.yunxin.kit.contactkit.ui.model.ContactVerifyInfoBean;
import com.netease.yunxin.kit.contactkit.ui.view.ContactListViewAttrs;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.model.FriendAddApplicationInfo;

public class FunVerifyInfoViewHolder extends BaseContactViewHolder {
  private final int cornerSize = SizeUtils.dp2px(4);

  private VerifyListener verifyListener;

  private FunVerifyListViewHolderBinding binding;

  public FunVerifyInfoViewHolder(@NonNull ViewGroup itemView) {
    super(itemView);
  }

  @Override
  public void initViewBinding(LayoutInflater layoutInflater, ViewGroup container) {
    binding = FunVerifyListViewHolderBinding.inflate(layoutInflater, container, true);
  }

  @Override
  public void onBind(BaseContactBean bean, int position, ContactListViewAttrs attrs) {
    ContactVerifyInfoBean infoBean = ((ContactVerifyInfoBean) bean);
    FriendAddApplicationInfo info = infoBean.data;
    String name = info.getApplicantUserInfo().getUserInfoName();
    String avatar = info.getApplicantUserInfo().getAvatar();
    int unreadCount = infoBean.getUnreadCount();
    if (unreadCount > 1) {
      binding.unreadTv.setVisibility(View.VISIBLE);
      if (unreadCount > 99) {
        binding.unreadTv.setText(context.getString(R.string.verify_max_count_text));
      } else {
        binding.unreadTv.setText(String.valueOf(unreadCount));
      }
    } else {
      binding.unreadTv.setVisibility(View.GONE);
    }

    if (infoBean.data.getUnread()) {
      binding.rootView.setBackgroundColor(context.getResources().getColor(R.color.color_ededef));
    } else {
      binding.rootView.setBackgroundColor(context.getResources().getColor(R.color.color_white));
    }

    switch (info.getStatus()) {
      case V2NIM_FRIEND_ADD_APPLICATION_STATUS_INIT:
        binding.llyVerifyResult.setVisibility(View.GONE);
        binding.llyVerify.setVisibility(View.VISIBLE);
        binding.tvAction.setText(R.string.friend_apply);
        binding.tvAccept.setOnClickListener(
            v -> {
              if (verifyListener != null) {
                verifyListener.onAccept((ContactVerifyInfoBean) bean);
              }
            });
        binding.tvReject.setOnClickListener(
            v -> {
              if (verifyListener != null) {
                verifyListener.onReject((ContactVerifyInfoBean) bean);
              }
            });
        break;
      case V2NIM_FRIEND_ADD_APPLICATION_STATUS_AGREED:
        if (TextUtils.equals(
            ((ContactVerifyInfoBean) bean).data.getApplicantAccountId(), IMKitClient.account())) {
          // 对方同意了我的申请
          name = info.getOperatorUserInfo().getUserInfoName();
          avatar = info.getOperatorUserInfo().getAvatar();
          binding.tvAction.setText(R.string.accept_your_friend_apply);
          showResult(null, true);
        } else if (TextUtils.equals(
            ((ContactVerifyInfoBean) bean).data.getOperatorAccountId(), IMKitClient.account())) {
          binding.tvAction.setText(R.string.friend_apply);
          showResult(context.getString(R.string.contact_verify_agreed), true);
        }
        break;
      case V2NIM_FRIEND_ADD_APPLICATION_STATUS_REJECTED:
        if (TextUtils.equals(
            ((ContactVerifyInfoBean) bean).data.getApplicantAccountId(), IMKitClient.account())) {
          name = info.getOperatorUserInfo().getUserInfoName();
          avatar = info.getOperatorUserInfo().getAvatar();
          binding.tvAction.setText(R.string.reject_your_friend_apply);
          showResult(null, false);
        } else if (TextUtils.equals(
            ((ContactVerifyInfoBean) bean).data.getOperatorAccountId(), IMKitClient.account())) {
          binding.tvAction.setText(R.string.friend_apply);
          showResult(context.getString(R.string.contact_verify_rejected), false);
        }
        break;
      case V2NIM_FRIEND_ADD_APPLICATION_STATUS_EXPIRED:
        binding.tvAction.setText(R.string.friend_apply);
        showResult(context.getString(R.string.contact_verify_expired), false);
        break;
      default:
        break;
    }

    binding.tvName.setText(name);
    binding.avatarView.setCornerRadius(cornerSize);
    binding.avatarView.setData(avatar, name, AvatarColor.avatarColor(info.getOperatorAccountId()));

    loadConfig(attrs);
  }

  private void loadConfig(ContactListViewAttrs attrs) {
    if (attrs == null) {
      return;
    }
    if (attrs.getNameTextColor() != ContactListViewAttrs.INT_NULL) {
      binding.tvName.setTextColor(attrs.getNameTextColor());
    }
    if (attrs.getNameTextSize() != ContactListViewAttrs.INT_NULL) {
      binding.tvName.setTextSize(attrs.getNameTextSize());
    }

    if (attrs.getAvatarCornerRadius() != ContactListViewAttrs.INT_NULL) {
      binding.avatarView.setCornerRadius(attrs.getAvatarCornerRadius());
    }
  }

  private void showResult(String content, boolean agreeIcon) {
    if (!TextUtils.isEmpty(content)) {
      binding.tvVerifyResult.setText(content);
      binding.ivVerifyResult.setImageResource(
          agreeIcon ? R.mipmap.ic_agree_status : R.mipmap.ic_reject_status);
      binding.llyVerifyResult.setVisibility(View.VISIBLE);
    } else {
      binding.llyVerifyResult.setVisibility(View.GONE);
    }
    binding.llyVerify.setVisibility(View.GONE);
  }

  public void setVerifyListener(VerifyListener verifyListener) {
    this.verifyListener = verifyListener;
  }

  public interface VerifyListener {
    void onAccept(ContactVerifyInfoBean bean);

    void onReject(ContactVerifyInfoBean bean);
  }
}
