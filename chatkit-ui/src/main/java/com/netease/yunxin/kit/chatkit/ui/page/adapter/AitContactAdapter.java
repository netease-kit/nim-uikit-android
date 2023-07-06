// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatMessageAitContactViewHolderBinding;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import java.util.List;

/** Team member @ adapter */
public class AitContactAdapter extends RecyclerView.Adapter<AitContactAdapter.AitContactHolder> {

  private List<UserInfoWithTeam> members;
  private OnItemSelectListener listener;

  private AitContactConfig contactConfig;

  public void setMembers(List<UserInfoWithTeam> userInfoWithTeams) {
    this.members = userInfoWithTeams;
  }

  public void setOnItemSelectListener(OnItemSelectListener listener) {
    this.listener = listener;
  }

  public void setAitContactConfig(AitContactConfig config) {
    contactConfig = config;
  }

  @NonNull
  @Override
  public AitContactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new AitContactHolder(
        ChatMessageAitContactViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull AitContactHolder holder, int position) {

    if (contactConfig != null) {
      if (contactConfig.avatarCorner >= 0) {
        holder.binding.contactHeader.setCornerRadius(contactConfig.avatarCorner);
      }

      if (contactConfig.nameColor != 0) {
        holder.binding.contactName.setTextColor(contactConfig.nameColor);
      }
    }

    if (position == 0) {
      holder.binding.contactName.setText(R.string.chat_team_ait_all);
      holder.binding.contactHeader.setCertainAvatar(contactConfig.defaultAvatarRes);
      holder
          .binding
          .getRoot()
          .setOnClickListener(
              v -> {
                if (listener != null) {
                  listener.onSelect(null);
                }
              });
    } else {
      UserInfoWithTeam member = members.get(position - 1);
      if (member == null) {
        return;
      }
      String showName = ChatUserCache.getName(member);
      holder.binding.contactName.setText(showName);
      NimUserInfo userInfo = ChatUserCache.getUserInfo(member);
      if (userInfo != null) {
        holder.binding.contactHeader.setData(
            userInfo.getAvatar(), showName, AvatarColor.avatarColor(userInfo.getAccount()));
      }
      holder
          .binding
          .getRoot()
          .setOnClickListener(
              v -> {
                if (listener != null) {
                  listener.onSelect(member);
                }
              });
    }
  }

  @Override
  public int getItemCount() {
    // add ait all
    return (members == null ? 0 : members.size()) + 1;
  }

  public static class AitContactHolder extends RecyclerView.ViewHolder {
    ChatMessageAitContactViewHolderBinding binding;

    public AitContactHolder(@NonNull ChatMessageAitContactViewHolderBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }

  public interface OnItemSelectListener {
    /** @param item null: @All */
    void onSelect(UserInfoWithTeam item);
  }

  public static class AitContactConfig {
    float avatarCorner;
    int nameColor;

    int defaultAvatarRes;

    public AitContactConfig(float corner, int color, int avatarRes) {
      avatarCorner = corner;
      nameColor = color;
      defaultAvatarRes = avatarRes;
    }
  }
}
