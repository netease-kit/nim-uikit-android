// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.view.viewholder;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamJoinActionStatus;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.VerifyListViewHolderBinding;
import com.netease.yunxin.kit.contactkit.ui.model.BaseContactBean;
import com.netease.yunxin.kit.contactkit.ui.model.TeamVerifyInfoBean;
import com.netease.yunxin.kit.contactkit.ui.view.ContactListViewAttrs;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;

public class TeamVerifyInfoViewHolder extends BaseContactViewHolder {

  private VerifyListener verifyListener;

  private VerifyListViewHolderBinding binding;

  public TeamVerifyInfoViewHolder(@NonNull ViewGroup itemView) {
    super(itemView);
  }

  @Override
  public void initViewBinding(LayoutInflater layoutInflater, ViewGroup container) {
    binding = VerifyListViewHolderBinding.inflate(layoutInflater, container, true);
  }

  @Override
  public void onBind(BaseContactBean bean, int position, ContactListViewAttrs attrs) {
    TeamVerifyInfoBean infoBean = ((TeamVerifyInfoBean) bean);
    String name = infoBean.getOperatorUserName();
    String avatar = infoBean.getOperatorAvatar();
    String teamName = infoBean.getTeamName();
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

    if (infoBean.getUnreadCount() > 0) {
      binding.rootView.setBackgroundColor(context.getResources().getColor(R.color.color_ededef));
    } else {
      binding.rootView.setBackgroundColor(context.getResources().getColor(R.color.color_white));
    }

    switch (infoBean.getActionType()) {
      case V2NIM_TEAM_JOIN_ACTION_TYPE_APPLICATION:
        String applyText =
            String.format(context.getString(R.string.team_verify_apply_text), teamName);
        binding.tvAction.setText(applyText);
        if (infoBean.getActionStatus()
            == V2NIMTeamJoinActionStatus.V2NIM_TEAM_JOIN_ACTION_STATUS_INIT) {
          binding.llyVerifyResult.setVisibility(View.GONE);
          binding.llyVerify.setVisibility(View.VISIBLE);
          binding.tvAccept.setOnClickListener(
              v -> {
                if (verifyListener != null) {
                  verifyListener.onAccept((TeamVerifyInfoBean) bean);
                }
              });
          binding.tvReject.setOnClickListener(
              v -> {
                if (verifyListener != null) {
                  verifyListener.onReject((TeamVerifyInfoBean) bean);
                }
              });
        } else if (infoBean.getActionStatus()
            == V2NIMTeamJoinActionStatus.V2NIM_TEAM_JOIN_ACTION_STATUS_AGREED) {
          binding.llyVerifyResult.setVisibility(View.VISIBLE);
          binding.llyVerify.setVisibility(View.GONE);
          showResult(context.getString(R.string.contact_verify_agreed), true);
        } else if (infoBean.getActionStatus()
            == V2NIMTeamJoinActionStatus.V2NIM_TEAM_JOIN_ACTION_STATUS_REJECTED) {
          binding.llyVerifyResult.setVisibility(View.VISIBLE);
          binding.llyVerify.setVisibility(View.GONE);
          showResult(context.getString(R.string.contact_verify_rejected), false);
        }
        break;
      case V2NIM_TEAM_JOIN_ACTION_TYPE_REJECT_APPLICATION:
        String rejectApplication =
            String.format(context.getString(R.string.team_verify_reject_text), teamName);
        binding.tvAction.setText(rejectApplication);
        showResult(null, false);
        break;
      case V2NIM_TEAM_JOIN_ACTION_TYPE_INVITATION:
        String invitationText =
            String.format(context.getString(R.string.team_verify_invitation_text), teamName);
        binding.tvAction.setText(invitationText);
        if (infoBean.getActionStatus()
            == V2NIMTeamJoinActionStatus.V2NIM_TEAM_JOIN_ACTION_STATUS_INIT) {
          binding.llyVerifyResult.setVisibility(View.GONE);
          binding.llyVerify.setVisibility(View.VISIBLE);
          binding.tvAccept.setOnClickListener(
              v -> {
                if (verifyListener != null) {
                  verifyListener.onAccept((TeamVerifyInfoBean) bean);
                }
              });
          binding.tvReject.setOnClickListener(
              v -> {
                if (verifyListener != null) {
                  verifyListener.onReject((TeamVerifyInfoBean) bean);
                }
              });
        } else if (infoBean.getActionStatus()
            == V2NIMTeamJoinActionStatus.V2NIM_TEAM_JOIN_ACTION_STATUS_AGREED) {
          binding.llyVerifyResult.setVisibility(View.VISIBLE);
          binding.llyVerify.setVisibility(View.GONE);
          showResult(context.getString(R.string.contact_verify_agreed), true);
        } else if (infoBean.getActionStatus()
            == V2NIMTeamJoinActionStatus.V2NIM_TEAM_JOIN_ACTION_STATUS_REJECTED) {
          binding.llyVerifyResult.setVisibility(View.VISIBLE);
          binding.llyVerify.setVisibility(View.GONE);
          showResult(context.getString(R.string.contact_verify_rejected), false);
        }
        break;
      case V2NIM_TEAM_JOIN_ACTION_TYPE_REJECT_INVITATION:
        String rejectInvitation =
            String.format(context.getString(R.string.team_verify_reject_invitation_text), teamName);
        binding.tvAction.setText(rejectInvitation);
        showResult(null, false);
      default:
        break;
    }

    if (infoBean.getActionStatus()
        == V2NIMTeamJoinActionStatus.V2NIM_TEAM_JOIN_ACTION_STATUS_EXPIRED) {
      showResult(context.getString(R.string.contact_verify_expired), false);
    }

    binding.tvName.setText(name);
    binding.avatarView.setData(
        avatar, name, AvatarColor.avatarColor(infoBean.getOperateAccountId()));
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
    void onAccept(TeamVerifyInfoBean bean);

    void onReject(TeamVerifyInfoBean bean);
  }
}
