// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationGroupBean;
import java.util.ArrayList;
import java.util.List;

public class ConversationGroupBar extends LinearLayout {

  public interface OnGroupClickListener {
    void onGroupClick(ConversationGroupBean group);

    void onSettingClick();
  }

  private final LinearLayout container;
  private final HorizontalScrollView scrollView;
  private final ImageView settingView;
  private int backgroundColorRes = R.color.color_conversation_divider;
  private boolean funStyle;
  private OnGroupClickListener listener;
  private String selectedId = ConversationGroupBean.ID_ALL;
  private List<ConversationGroupBean> groups = new ArrayList<>();

  public ConversationGroupBar(Context context) {
    super(context);
    setOrientation(HORIZONTAL);
    setGravity(Gravity.CENTER_VERTICAL);
    scrollView = new HorizontalScrollView(context);
    scrollView.setHorizontalScrollBarEnabled(false);
    scrollView.setFillViewport(false);
    container = new LinearLayout(context);
    container.setOrientation(LinearLayout.HORIZONTAL);
    container.setGravity(Gravity.CENTER_VERTICAL);
    container.setPadding(
        SizeUtils.dp2px(12), SizeUtils.dp2px(8), SizeUtils.dp2px(8), SizeUtils.dp2px(8));
    scrollView.addView(
        container,
        new HorizontalScrollView.LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    LayoutParams scrollParams = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
    scrollParams.rightMargin = SizeUtils.dp2px(16);
    addView(scrollView, scrollParams);
    settingView = createSettingView();
    addView(
        settingView, new LayoutParams(SizeUtils.dp2px(44), ViewGroup.LayoutParams.MATCH_PARENT));
    applyBackgroundColor();
  }

  public void setOnGroupClickListener(OnGroupClickListener listener) {
    this.listener = listener;
  }

  public void setBackgroundColorRes(int backgroundColorRes) {
    this.backgroundColorRes = backgroundColorRes;
    applyBackgroundColor();
  }

  public void setFunStyle(boolean funStyle) {
    if (this.funStyle == funStyle) {
      return;
    }
    this.funStyle = funStyle;
    render();
  }

  private void applyBackgroundColor() {
    setBackgroundColor(ContextCompat.getColor(getContext(), backgroundColorRes));
    if (scrollView != null) {
      scrollView.setBackgroundColor(ContextCompat.getColor(getContext(), backgroundColorRes));
    }
    if (container != null) {
      container.setBackgroundColor(ContextCompat.getColor(getContext(), backgroundColorRes));
    }
    if (settingView != null) {
      settingView.setBackgroundColor(ContextCompat.getColor(getContext(), backgroundColorRes));
    }
  }

  public void setSelectedId(String selectedId) {
    if (TextUtils.equals(this.selectedId, selectedId)) {
      return;
    }
    this.selectedId = selectedId;
    render();
  }

  public void setGroups(List<ConversationGroupBean> groups) {
    List<ConversationGroupBean> safeGroups = groups == null ? new ArrayList<>() : groups;
    if (isSameGroups(this.groups, safeGroups)) {
      return;
    }
    this.groups = new ArrayList<>(safeGroups);
    render();
  }

  public void setGroupsAndSelected(List<ConversationGroupBean> groups, String selectedId) {
    List<ConversationGroupBean> safeGroups = groups == null ? new ArrayList<>() : groups;
    boolean groupsChanged = !isSameGroups(this.groups, safeGroups);
    boolean selectedChanged = !TextUtils.equals(this.selectedId, selectedId);
    if (!groupsChanged && !selectedChanged) {
      return;
    }
    this.groups = new ArrayList<>(safeGroups);
    this.selectedId = selectedId;
    render();
  }

  private void render() {
    container.removeAllViews();
    for (ConversationGroupBean group : groups) {
      if (!group.isVisible()) {
        continue;
      }
      TextView groupView = createGroupView(group);
      container.addView(groupView);
    }
  }

  private TextView createGroupView(ConversationGroupBean group) {
    TextView view = new TextView(getContext());
    view.setGravity(Gravity.CENTER);
    view.setSingleLine(true);
    view.setText(buildTitle(group));
    view.setTextSize(14);
    boolean selected = group.getId().equals(selectedId);
    view.setTypeface(Typeface.DEFAULT, selected ? Typeface.BOLD : Typeface.NORMAL);
    view.setTextColor(
        ContextCompat.getColor(
            getContext(),
            selected ? getSelectedTextColorRes() : R.color.color_conversation_primary_text));
    view.setBackgroundResource(
        selected ? R.drawable.conversation_group_tab_selected_bg : android.R.color.transparent);
    int horizontal = SizeUtils.dp2px(8);
    view.setPadding(horizontal, SizeUtils.dp2px(6), horizontal, SizeUtils.dp2px(6));
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, SizeUtils.dp2px(34));
    params.rightMargin = SizeUtils.dp2px(2);
    view.setLayoutParams(params);
    view.setOnClickListener(
        v -> {
          selectedId = group.getId();
          if (listener != null) {
            listener.onGroupClick(group);
          }
          render();
        });
    return view;
  }

  private int getSelectedTextColorRes() {
    return funStyle
        ? R.color.fun_conversation_group_primary
        : R.color.color_conversation_group_primary;
  }

  private boolean isSameGroups(
      List<ConversationGroupBean> oldGroups, List<ConversationGroupBean> newGroups) {
    if (oldGroups == newGroups) {
      return true;
    }
    if (oldGroups == null || newGroups == null || oldGroups.size() != newGroups.size()) {
      return false;
    }
    for (int index = 0; index < oldGroups.size(); index++) {
      ConversationGroupBean oldGroup = oldGroups.get(index);
      ConversationGroupBean newGroup = newGroups.get(index);
      if (oldGroup == null || newGroup == null) {
        if (oldGroup != newGroup) {
          return false;
        }
        continue;
      }
      if (!TextUtils.equals(oldGroup.getId(), newGroup.getId())
          || oldGroup.getType() != newGroup.getType()
          || !TextUtils.equals(oldGroup.getName(), newGroup.getName())
          || oldGroup.isVisible() != newGroup.isVisible()
          || oldGroup.getSortOrder() != newGroup.getSortOrder()
          || oldGroup.getUnreadOrCount() != newGroup.getUnreadOrCount()) {
        return false;
      }
    }
    return true;
  }

  private ImageView createSettingView() {
    ImageView view = new ImageView(getContext());
    view.setScaleType(ImageView.ScaleType.CENTER);
    view.setImageResource(R.drawable.ic_conversation_group_manager);
    view.setPadding(0, 0, SizeUtils.dp2px(20), 0);
    view.setOnClickListener(
        v -> {
          if (listener != null) {
            listener.onSettingClick();
          }
        });
    return view;
  }

  private String buildTitle(ConversationGroupBean group) {
    if (group.getUnreadOrCount() > 0) {
      String count =
          group.getUnreadOrCount() > 99 ? "99+" : String.valueOf(group.getUnreadOrCount());
      return group.getName() + "(" + count + ")";
    }
    return group.getName();
  }
}
