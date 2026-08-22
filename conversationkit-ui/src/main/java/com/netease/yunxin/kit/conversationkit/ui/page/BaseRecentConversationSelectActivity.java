// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.page;

import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.common.ui.widgets.ContactAvatarView;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationGroupSystemBarHelper;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public abstract class BaseRecentConversationSelectActivity extends BaseLocalActivity {

  protected BackTitleBar titleBar;
  protected EditText searchEditText;
  protected RecyclerView recyclerView;
  protected View emptyView;
  protected RecentConversationAdapter adapter;
  protected String keyword = "";

  protected View createSelectContentView() {
    setupSystemBars();
    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setBackgroundColor(getPageBackgroundColor());

    titleBar = new BackTitleBar(this);
    titleBar.setBackgroundColor(getNavigationBackgroundColor());
    titleBar
        .setTitle(getTitleText())
        .setLeftText(R.string.cancel_title)
        .setActionText(R.string.sure_title)
        .setActionTextColor(getPrimaryColor())
        .setActionEnable(false)
        .setOnBackIconClickListener(v -> finish())
        .setActionListener(v -> submitSelectedConversations());
    root.addView(
        titleBar,
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

    searchEditText = new EditText(this);
    searchEditText.setSingleLine(true);
    searchEditText.setHint(R.string.conversation_group_search_hint);
    searchEditText.setTextSize(14);
    searchEditText.setTextColor(
        ContextCompat.getColor(this, R.color.color_conversation_primary_text));
    searchEditText.setHintTextColor(
        ContextCompat.getColor(this, R.color.color_conversation_secondary_text));
    searchEditText.setBackgroundResource(R.drawable.conversation_group_search_white_bg);
    searchEditText.setCompoundDrawablesWithIntrinsicBounds(
        R.drawable.conversation_group_ic_search, 0, 0, 0);
    searchEditText.setCompoundDrawablePadding(SizeUtils.dp2px(5));
    searchEditText.setPadding(SizeUtils.dp2px(15), 0, SizeUtils.dp2px(40), 0);
    LinearLayout searchContainer = new LinearLayout(this);
    searchContainer.setBackgroundColor(getNavigationBackgroundColor());
    searchContainer.setOrientation(LinearLayout.VERTICAL);
    root.addView(
        searchContainer,
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(56)));
    LinearLayout.LayoutParams searchParams =
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(32));
    searchParams.setMargins(
        SizeUtils.dp2px(20), SizeUtils.dp2px(16), SizeUtils.dp2px(20), SizeUtils.dp2px(8));
    FrameLayout searchRow = new FrameLayout(this);
    searchContainer.addView(searchRow, searchParams);
    searchRow.addView(
        searchEditText,
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    ImageView searchClearView = new ImageView(this);
    searchClearView.setImageResource(R.drawable.conversation_group_name_clear);
    searchClearView.setScaleType(ImageView.ScaleType.CENTER);
    searchClearView.setContentDescription(
        getString(R.string.conversation_group_clear_content_description));
    searchClearView.setVisibility(View.GONE);
    searchClearView.setOnClickListener(v -> searchEditText.setText(""));
    FrameLayout.LayoutParams clearParams =
        new FrameLayout.LayoutParams(SizeUtils.dp2px(32), SizeUtils.dp2px(32));
    clearParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
    clearParams.rightMargin = SizeUtils.dp2px(4);
    searchRow.addView(searchClearView, clearParams);

    FrameLayout listContainer = new FrameLayout(this);
    listContainer.setBackgroundColor(getListBackgroundColor());
    root.addView(
        listContainer, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

    recyclerView = new RecyclerView(this);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    adapter = new RecentConversationAdapter();
    recyclerView.setAdapter(adapter);
    recyclerView.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
          @Override
          public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE
                && TextUtils.isEmpty(keyword)
                && hasMoreConversations()) {
              LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
              if (manager != null
                  && manager.findLastVisibleItemPosition() >= adapter.getItemCount() - 5) {
                loadMoreConversations();
              }
            }
          }
        });
    listContainer.addView(
        recyclerView,
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    LinearLayout emptyLayout = new LinearLayout(this);
    emptyLayout.setOrientation(LinearLayout.VERTICAL);
    emptyLayout.setGravity(Gravity.CENTER);
    ImageView emptyImage = new ImageView(this);
    emptyImage.setImageResource(getEmptyImageRes());
    emptyLayout.addView(
        emptyImage, new LinearLayout.LayoutParams(SizeUtils.dp2px(118), SizeUtils.dp2px(96)));
    TextView emptyText = new TextView(this);
    emptyText.setText(R.string.conversation_group_search_empty);
    emptyText.setGravity(Gravity.CENTER);
    emptyText.setTextSize(15);
    emptyText.setTextColor(ContextCompat.getColor(this, R.color.color_conversation_secondary_text));
    LinearLayout.LayoutParams emptyTextParams =
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    emptyTextParams.topMargin = SizeUtils.dp2px(8);
    emptyLayout.addView(emptyText, emptyTextParams);
    emptyView = emptyLayout;
    emptyView.setVisibility(View.GONE);
    listContainer.addView(
        emptyView,
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    searchEditText.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            keyword = s == null ? "" : s.toString();
            searchClearView.setVisibility(TextUtils.isEmpty(keyword) ? View.GONE : View.VISIBLE);
            renderConversations(getFilteredConversations(keyword));
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });
    return root;
  }

  private void setupSystemBars() {
    ConversationGroupSystemBarHelper.apply(this, getPageBackgroundColor());
  }

  protected void renderConversations(List<ConversationBean> data) {
    adapter.setData(data);
    boolean empty = data == null || data.isEmpty();
    recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
  }

  protected void updateSelectedConversations(Set<String> selectedIds) {
    titleBar.setTitle(getTitleText());
    titleBar.setActionEnable(selectedIds != null && !selectedIds.isEmpty());
    adapter.setSelectedIds(selectedIds);
  }

  protected abstract String getTitleText();

  protected abstract List<ConversationBean> getFilteredConversations(String keyword);

  protected abstract Set<String> getSelectedConversationIds();

  protected abstract boolean hasMoreConversations();

  protected abstract void loadMoreConversations();

  protected abstract void toggleConversation(ConversationBean bean);

  protected abstract void submitSelectedConversations();

  protected boolean isFunStyle() {
    return false;
  }

  protected int getPrimaryColor() {
    return ContextCompat.getColor(
        this,
        isFunStyle()
            ? R.color.fun_conversation_group_primary
            : R.color.color_conversation_group_primary);
  }

  protected int getPageBackgroundColor() {
    return ContextCompat.getColor(
        this,
        isFunStyle() ? R.color.fun_conversation_secondary_page_bg_color : R.color.color_white);
  }

  protected int getNavigationBackgroundColor() {
    return ContextCompat.getColor(
        this,
        isFunStyle() ? R.color.fun_conversation_secondary_page_bg_color : R.color.color_white);
  }

  protected int getListBackgroundColor() {
    return ContextCompat.getColor(
        this, isFunStyle() ? R.color.fun_conversation_item_bg_color : R.color.color_white);
  }

  protected int getEmptyImageRes() {
    return isFunStyle() ? R.drawable.fun_ic_conversation_empty : R.drawable.ic_conversation_empty;
  }

  protected int getItemDividerColor() {
    return ContextCompat.getColor(
        this,
        isFunStyle()
            ? R.color.fun_conversation_item_divide_line_color
            : R.color.color_conversation_group_divider);
  }

  protected class RecentConversationAdapter
      extends RecyclerView.Adapter<RecentConversationViewHolder> {
    private final List<ConversationBean> data = new ArrayList<>();
    private Set<String> selectedIds;

    void setData(List<ConversationBean> conversations) {
      data.clear();
      if (conversations != null) {
        data.addAll(conversations);
      }
      notifyDataSetChanged();
    }

    void setSelectedIds(Set<String> selectedIds) {
      this.selectedIds = selectedIds;
      notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecentConversationViewHolder onCreateViewHolder(
        @NonNull ViewGroup parent, int viewType) {
      LinearLayout row = new LinearLayout(parent.getContext());
      row.setOrientation(LinearLayout.HORIZONTAL);
      row.setGravity(Gravity.CENTER_VERTICAL);
      row.setPadding(
          SizeUtils.dp2px(isFunStyle() ? 16 : 20), 0, SizeUtils.dp2px(isFunStyle() ? 16 : 20), 0);
      row.setBackgroundResource(
          isFunStyle()
              ? R.drawable.fun_conversation_view_holder_selector
              : R.drawable.conversation_common_view_holder_selector);
      row.setLayoutParams(
          new RecyclerView.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(isFunStyle() ? 72 : 64)));
      return new RecentConversationViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentConversationViewHolder holder, int position) {
      ConversationBean bean = data.get(position);
      holder.bind(bean, selectedIds != null && selectedIds.contains(bean.getConversationId()));
    }

    @Override
    public int getItemCount() {
      return data.size();
    }
  }

  protected class RecentConversationViewHolder extends RecyclerView.ViewHolder {
    private final AppCompatRadioButton radioButton;
    private final ContactAvatarView avatarView;
    private final TextView nameView;
    private final View divider;

    RecentConversationViewHolder(@NonNull LinearLayout row) {
      super(row);
      radioButton = new AppCompatRadioButton(row.getContext());
      radioButton.setButtonDrawable(
          isFunStyle()
              ? R.drawable.fun_conversation_radio_button_selector
              : R.drawable.conversation_radio_button_selector);
      radioButton.setClickable(false);
      row.addView(
          radioButton, new LinearLayout.LayoutParams(SizeUtils.dp2px(18), SizeUtils.dp2px(18)));

      avatarView = new ContactAvatarView(row.getContext());
      if (isFunStyle()) {
        avatarView.setCornerRadius(SizeUtils.dp2px(4));
      }
      LinearLayout.LayoutParams avatarParams =
          new LinearLayout.LayoutParams(
              SizeUtils.dp2px(isFunStyle() ? 48 : 42), SizeUtils.dp2px(isFunStyle() ? 48 : 42));
      avatarParams.leftMargin = SizeUtils.dp2px(12);
      row.addView(avatarView, avatarParams);

      FrameLayout contentFrame = new FrameLayout(row.getContext());
      LinearLayout textContainer = new LinearLayout(row.getContext());
      textContainer.setOrientation(LinearLayout.VERTICAL);
      textContainer.setGravity(Gravity.CENTER_VERTICAL);
      textContainer.setPadding(0, 0, 0, isFunStyle() ? SizeUtils.dp2px(1) : 0);
      nameView = new TextView(row.getContext());
      nameView.setSingleLine(true);
      nameView.setEllipsize(TextUtils.TruncateAt.END);
      nameView.setTextSize(isFunStyle() ? 17 : 16);
      nameView.setTextColor(
          ContextCompat.getColor(
              row.getContext(),
              isFunStyle()
                  ? R.color.fun_conversation_item_title_text_color
                  : R.color.color_conversation_primary_text));
      textContainer.addView(nameView);
      contentFrame.addView(
          textContainer,
          new FrameLayout.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
      divider = new View(row.getContext());
      divider.setBackgroundColor(getItemDividerColor());
      divider.setVisibility(isFunStyle() ? View.VISIBLE : View.GONE);
      FrameLayout.LayoutParams dividerParams =
          new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(1));
      dividerParams.gravity = Gravity.BOTTOM;
      contentFrame.addView(divider, dividerParams);
      LinearLayout.LayoutParams textParams =
          new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
      textParams.leftMargin = SizeUtils.dp2px(12);
      row.addView(contentFrame, textParams);
    }

    void bind(ConversationBean bean, boolean selected) {
      avatarView.setData(
          bean.getConversationAvatar(),
          bean.getAvatarName(),
          AvatarColor.avatarColor(bean.getTargetId()));
      nameView.setText(buildHighlightText(bean.getConversationName()));
      radioButton.setChecked(selected);
      itemView.setOnClickListener(v -> toggleConversation(bean));
    }

    private CharSequence buildHighlightText(String text) {
      if (TextUtils.isEmpty(text) || TextUtils.isEmpty(keyword)) {
        return text;
      }
      String lowerText = text.toLowerCase(Locale.ROOT);
      String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
      int start = lowerText.indexOf(lowerKeyword);
      if (start < 0) {
        return text;
      }
      SpannableString spannable = new SpannableString(text);
      spannable.setSpan(
          new ForegroundColorSpan(
              ContextCompat.getColor(
                  itemView.getContext(), R.color.color_conversation_group_primary)),
          start,
          start + keyword.length(),
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      return spannable;
    }
  }
}
