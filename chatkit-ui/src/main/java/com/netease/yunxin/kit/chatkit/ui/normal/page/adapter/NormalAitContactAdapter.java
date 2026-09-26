// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.page.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.search.model.RecordHitInfo;
import com.netease.yunxin.kit.chatkit.model.HitType;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.NormalChatMessageAitContactViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AitUserInfo;
import com.netease.yunxin.kit.chatkit.utils.SearchEngine;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.widgets.HitHighlightTextView;
import java.util.ArrayList;
import java.util.List;

/** Team member @ adapter */
public class NormalAitContactAdapter
    extends RecyclerView.Adapter<NormalAitContactAdapter.AitContactHolder> {

  private List<AitUserInfo> members = new ArrayList<>();
  private List<AitUserInfo> allMembers = new ArrayList<>();
  //@所有人的特殊类型
  private static final int SHOW_ALL_TYPE = 101;

  private OnItemListener listener;

  private AitContactConfig contactConfig;

  private boolean showAll = true;
  private boolean searching;
  private boolean allowAISearch;
  private String query = "";
  private final SearchEngine searchEngine = new SearchEngine();

  public void setMembers(List<AitUserInfo> userInfoWithTeams) {
    allMembers = userInfoWithTeams == null ? new ArrayList<>() : new ArrayList<>(userInfoWithTeams);
    for (AitUserInfo member : allMembers) {
      member.setSearchHitType(HitType.None);
      member.setSearchHitInfo(null);
    }
    this.members.clear();
    this.members.addAll(allMembers);
    if (searching) {
      filter(query);
    }
  }

  public void filter(String query) {
    clearSearchHitInfo();
    this.query = searchEngine.normalizeQuery(query);
    searching = !TextUtils.isEmpty(this.query);
    members.clear();
    if (!searching) {
      members.addAll(allMembers);
    } else {
      for (AitUserInfo member : allMembers) {
        if (member.isAI()) {
          if (!allowAISearch
              || !member.isAISearchable()
              || (!match(member, this.query, member.getShowName(), HitType.UserName)
                  && !match(member, this.query, member.getAccount(), HitType.Account))) {
            continue;
          }
          members.add(member);
          continue;
        }
        if (match(member, this.query, member.getFriendAlias(), HitType.Alias)
            || match(member, this.query, member.getTeamNick(), HitType.TeamName)
            || match(member, this.query, member.getUserName(), HitType.UserName)
            || match(member, this.query, member.getAccount(), HitType.Account)) {
          members.add(member);
        }
      }
    }
    notifyDataSetChanged();
  }

  private void clearSearchHitInfo() {
    for (AitUserInfo member : allMembers) {
      member.setSearchHitType(HitType.None);
      member.setSearchHitInfo(null);
    }
  }

  private boolean match(AitUserInfo member, String query, String value, HitType hitType) {
    if (TextUtils.isEmpty(value)) {
      return false;
    }
    RecordHitInfo hit = searchEngine.searchTextIgnoreCase(value, query);
    if (hit == null) {
      return false;
    }
    member.setSearchHitType(hitType);
    member.setSearchHitInfo(hit);
    return true;
  }

  public void addMembers(List<AitUserInfo> userInfoWithTeams) {
    if (userInfoWithTeams == null || userInfoWithTeams.isEmpty()) {
      return;
    }
    for (AitUserInfo member : userInfoWithTeams) {
      if (member == null || containsAccount(member.getAccount())) {
        continue;
      }
      member.setSearchHitType(HitType.None);
      member.setSearchHitInfo(null);
      this.allMembers.add(member);
    }
    if (searching) {
      filter(query);
    } else {
      this.members.clear();
      this.members.addAll(this.allMembers);
      notifyDataSetChanged();
    }
  }

  private boolean containsAccount(String account) {
    for (AitUserInfo member : allMembers) {
      if (member != null && TextUtils.equals(member.getAccount(), account)) {
        return true;
      }
    }
    return false;
  }

  public void setShowAll(boolean showAll) {
    this.showAll = showAll;
  }

  public void setAllowAISearch(boolean allowAISearch) {
    this.allowAISearch = allowAISearch;
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
        NormalChatMessageAitContactViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public int getItemViewType(int position) {
    if (showAll && !searching && position == 0) {
      return SHOW_ALL_TYPE;
    }
    return super.getItemViewType(position);
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
    if (showAll && !searching) {
      if (position == 0) {
        holder.binding.contactName.clearHitText(
            holder.binding.getRoot().getContext().getString(R.string.chat_team_ait_all));
        holder.binding.contactSubName.clearHitText("");
        holder.binding.contactSubName.setVisibility(android.view.View.GONE);
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

    AitUserInfo member = members.get(dataPosition);
    if (member == null) {
      return;
    }
    String showName = member.getShowName();
    bindMemberName(holder, member, showName);
    holder.binding.contactHeader.setData(
        member.getAvatar(), member.getAvatarName(), AvatarColor.avatarColor(member.getAccount()));
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

  private void bindMemberName(AitContactHolder holder, AitUserInfo member, String showName) {
    HitType hitType = member.getSearchHitType();
    RecordHitInfo hit = member.getSearchHitInfo();
    if (hitType == HitType.None) {
      holder.binding.contactName.clearHitText(showName);
      holder.binding.contactSubName.clearHitText("");
      holder.binding.contactSubName.setVisibility(android.view.View.GONE);
      return;
    }
    String primary =
        firstNonEmpty(
            member.getFriendAlias(),
            member.getTeamNick(),
            member.getUserName(),
            member.getAccount());
    String secondary = null;
    String highlighted = null;
    if (hitType == HitType.Alias) {
      highlighted = member.getFriendAlias();
    } else if (hitType == HitType.TeamName) {
      highlighted = member.getTeamNick();
      if (!TextUtils.isEmpty(member.getFriendAlias())) {
        primary = member.getFriendAlias();
        secondary = highlighted;
      }
    } else if (hitType == HitType.UserName) {
      highlighted = member.getUserName();
      if (!TextUtils.isEmpty(member.getFriendAlias())) {
        primary = member.getFriendAlias();
        secondary = highlighted;
      } else if (!TextUtils.isEmpty(member.getTeamNick())) {
        primary = member.getTeamNick();
        secondary = highlighted;
      }
    } else {
      highlighted = member.getAccount();
      if (!TextUtils.equals(primary, highlighted)) {
        secondary = highlighted;
      }
    }
    setText(holder.binding.contactName, primary, hit, TextUtils.equals(primary, highlighted));
    if (!TextUtils.isEmpty(secondary)) {
      holder.binding.contactSubName.setVisibility(android.view.View.VISIBLE);
      setText(
          holder.binding.contactSubName, secondary, hit, TextUtils.equals(secondary, highlighted));
    } else {
      holder.binding.contactSubName.clearHitText("");
      holder.binding.contactSubName.setVisibility(android.view.View.GONE);
    }
  }

  private String firstNonEmpty(String... values) {
    for (String value : values) {
      if (!TextUtils.isEmpty(value)) {
        return value;
      }
    }
    return "";
  }

  private void setText(
      HitHighlightTextView textView, String text, RecordHitInfo hit, boolean useHit) {
    if (!useHit || hit == null) {
      textView.clearHitText(text);
      return;
    }
    textView.setHitText(
        text,
        hit.start,
        hit.end,
        ContextCompat.getColor(textView.getContext(), R.color.color_337eff));
  }

  @Override
  public int getItemCount() {
    // add ait all
    if (members == null || members.isEmpty()) {
      return 0;
    }

    return showAll && !searching ? members.size() + 1 : members.size();
  }

  public static class AitContactHolder extends RecyclerView.ViewHolder {
    NormalChatMessageAitContactViewHolderBinding binding;

    public AitContactHolder(@NonNull NormalChatMessageAitContactViewHolderBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }

  public interface OnItemListener {
    /** @param item null: @All */
    void onSelect(AitUserInfo item);
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
