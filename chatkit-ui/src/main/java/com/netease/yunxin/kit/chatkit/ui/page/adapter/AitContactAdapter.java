// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatMessageAitContactViewHolderBinding;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import java.util.ArrayList;
import java.util.List;

/** Team member @ adapter */
public class AitContactAdapter extends RecyclerView.Adapter<AitContactAdapter.AitContactHolder> {

  private List<TeamMemberWithUserInfo> members = new ArrayList<>();
  private OnItemListener listener;

  private AitContactConfig contactConfig;

  private boolean showAll = true;

  public void setMembers(List<TeamMemberWithUserInfo> userInfoWithTeams) {
    this.members.clear();
    this.members.addAll(userInfoWithTeams);
  }

  public void addMembers(List<TeamMemberWithUserInfo> userInfoWithTeams) {
    this.members.addAll(userInfoWithTeams);
  }

  public void setShowAll(boolean showAll) {
    this.showAll = showAll;
  }

  public void setOnItemListener(OnItemListener listener) {
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

    int dataPosition = position;
    if (showAll) {
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
        return;
      }
      dataPosition = position - 1;
    }

    TeamMemberWithUserInfo member = members.get(dataPosition);
    if (member == null) {
      return;
    }
    String showName = ChatUserCache.getInstance().getName(member);
    holder.binding.contactName.setText(showName);
    holder.binding.contactHeader.setData(
        member.getAvatar(), showName, AvatarColor.avatarColor(member.getAccountId()));
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

  @Override
  public int getItemCount() {
    // add ait all
    if (members == null || members.isEmpty()) {
      return 0;
    }

    return showAll ? members.size() + 1 : members.size();
  }

  public static class AitContactHolder extends RecyclerView.ViewHolder {
    ChatMessageAitContactViewHolderBinding binding;

    public AitContactHolder(@NonNull ChatMessageAitContactViewHolderBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }

  public interface OnItemListener {
    /** @param item null: @All */
    void onSelect(TeamMemberWithUserInfo item);
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
