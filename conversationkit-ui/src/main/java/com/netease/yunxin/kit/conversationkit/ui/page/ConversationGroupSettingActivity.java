// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.page;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMConversationGroup;
import com.netease.yunxin.kit.chatkit.repo.ConversationGroupRepo;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.common.ui.widgets.ContactAvatarView;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationGroupErrorHelper;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationGroupSystemBarHelper;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationGroupBean;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationGroupType;
import com.netease.yunxin.kit.conversationkit.ui.page.viewmodel.ConversationGroupViewModel;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;
import java.util.List;

public class ConversationGroupSettingActivity extends BaseLocalActivity {

  private static final int MEMBER_ROW_HEIGHT_DP = 64;
  private static final int MEMBER_ROW_HORIZONTAL_PADDING_DP = 24;
  private static final int REMOVE_BUTTON_MAX_WIDTH_DP = 72;
  private static final int REMOVE_BUTTON_HORIZONTAL_PADDING_DP = 8;

  private ConversationGroupViewModel viewModel;
  private String groupId;
  private String groupName;
  private TextView nameValueView;
  private TextView memberTitleView;
  private RecyclerView memberRecyclerView;
  private View emptyView;
  private MemberAdapter memberAdapter;
  private boolean currentGroupConfirmed;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    viewModel = new ViewModelProvider(this).get(ConversationGroupViewModel.class);
    groupId = getIntent().getStringExtra(RouterConstant.KEY_CONVERSATION_GROUP_ID);
    groupName = getIntent().getStringExtra(RouterConstant.KEY_CONVERSATION_GROUP_NAME);
    if (TextUtils.isEmpty(groupId)) {
      finish();
      return;
    }
    setupSystemBars();
    setContentView(createContentView());
    bindViewModel();
    checkCurrentGroupExistence();
    viewModel.loadGroups();
    viewModel.loadGroupConversationsForSetting(groupId);
  }

  private void setupSystemBars() {
    ConversationGroupSystemBarHelper.apply(this, getPageBackgroundColor());
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (viewModel != null && !TextUtils.isEmpty(groupId)) {
      checkCurrentGroupExistence();
      viewModel.loadGroupConversationsForSetting(groupId);
    }
  }

  private View createContentView() {
    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setBackgroundColor(getPageBackgroundColor());

    BackTitleBar titleBar = new BackTitleBar(this);
    titleBar.setBackgroundColor(getNavigationBackgroundColor());
    titleBar
        .setTitle(R.string.conversation_group_setting_title)
        .setActionText(R.string.conversation_group_delete_action)
        .setActionTextColor(getTitleActionColor())
        .setOnBackIconClickListener(v -> finish())
        .setActionListener(
            v -> {
              if (showNetworkTipIfDisconnected()) {
                return;
              }
              showDeleteConfirmDialog();
            });
    root.addView(
        titleBar,
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

    root.addView(createNameRow());
    View divider = new View(this);
    divider.setBackgroundColor(
        ContextCompat.getColor(this, R.color.color_conversation_group_divider));
    root.addView(
        divider,
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(6)));
    root.addView(createSectionTitle());
    root.addView(createAddConversationRow());
    root.addView(
        createMemberListView(),
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    return root;
  }

  private void bindViewModel() {
    viewModel.getSettingGroupConversationLiveData().observe(this, this::renderMembers);
    viewModel
        .getGroupLiveData()
        .observe(
            this,
            groups -> {
              if (groups == null) {
                return;
              }
              boolean containsCurrentGroup = containsCurrentGroup(groups);
              if (containsCurrentGroup) {
                currentGroupConfirmed = true;
                return;
              }
              if ((currentGroupConfirmed || hasCustomGroupData(groups))) {
                finish();
              }
            });
  }

  private boolean hasCustomGroupData(List<ConversationGroupBean> groups) {
    for (ConversationGroupBean group : groups) {
      if (group.getType() == ConversationGroupType.CUSTOM) {
        return true;
      }
    }
    return false;
  }

  private boolean containsCurrentGroup(List<ConversationGroupBean> groups) {
    for (ConversationGroupBean group : groups) {
      if (TextUtils.equals(group.getId(), groupId)) {
        return true;
      }
    }
    return false;
  }

  private void checkCurrentGroupExistence() {
    ConversationGroupRepo.getConversationGroup(
        groupId,
        new FetchCallback<V2NIMConversationGroup>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            if (errorCode == ConversationGroupErrorHelper.ERROR_CONVERSATION_GROUP_NOT_EXIST) {
              finish();
            }
          }

          @Override
          public void onSuccess(@Nullable V2NIMConversationGroup data) {
            if (data == null) {
              finish();
            } else {
              currentGroupConfirmed = true;
            }
          }
        });
  }

  private View createNameRow() {
    LinearLayout row = createRow();
    TextView label = createLabel(R.string.conversation_group_name_label);
    row.addView(label, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
    nameValueView = createValue(groupName);
    row.addView(
        nameValueView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
    ImageView arrow = new ImageView(this);
    arrow.setImageResource(R.drawable.conversation_group_setting_arrow);
    LinearLayout.LayoutParams arrowParams =
        new LinearLayout.LayoutParams(SizeUtils.dp2px(16), SizeUtils.dp2px(16));
    arrowParams.leftMargin = SizeUtils.dp2px(8);
    row.addView(arrow, arrowParams);
    row.setOnClickListener(
        v -> {
          if (showNetworkTipIfDisconnected()) {
            return;
          }
          showRenameDialog();
        });
    return row;
  }

  private View createSectionTitle() {
    LinearLayout container = new LinearLayout(this);
    container.setOrientation(LinearLayout.VERTICAL);
    container.setBackgroundColor(ContextCompat.getColor(this, R.color.color_white));
    container.setLayoutParams(
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(48)));

    memberTitleView = new TextView(this);
    memberTitleView.setText(getString(R.string.conversation_group_member_title, 0));
    memberTitleView.setTextSize(15);
    memberTitleView.setGravity(Gravity.CENTER_VERTICAL);
    memberTitleView.setTextColor(
        ContextCompat.getColor(this, R.color.color_conversation_secondary_text));
    memberTitleView.setPadding(
        SizeUtils.dp2px(MEMBER_ROW_HORIZONTAL_PADDING_DP),
        SizeUtils.dp2px(22),
        SizeUtils.dp2px(MEMBER_ROW_HORIZONTAL_PADDING_DP),
        0);
    container.addView(
        memberTitleView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    return container;
  }

  private View createAddConversationRow() {
    LinearLayout row = createRow();
    ImageView addIcon = new ImageView(this);
    addIcon.setImageResource(getAddIconRes());
    row.addView(addIcon, new LinearLayout.LayoutParams(SizeUtils.dp2px(36), SizeUtils.dp2px(36)));
    TextView label = createLabel(R.string.conversation_group_add_conversation);
    LinearLayout.LayoutParams labelParams =
        new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
    labelParams.leftMargin = SizeUtils.dp2px(24);
    row.addView(label, labelParams);
    row.setOnClickListener(
        v -> {
          if (showNetworkTipIfDisconnected()) {
            return;
          }
          XKitRouter.withKey(getAddConversationPagePath())
              .withContext(this)
              .withParam(RouterConstant.KEY_CONVERSATION_GROUP_ID, groupId)
              .withParam(RouterConstant.KEY_CONVERSATION_GROUP_NAME, groupName)
              .navigate();
        });
    return row;
  }

  private LinearLayout createRow() {
    LinearLayout row = new LinearLayout(this);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setPadding(
        SizeUtils.dp2px(MEMBER_ROW_HORIZONTAL_PADDING_DP),
        0,
        SizeUtils.dp2px(MEMBER_ROW_HORIZONTAL_PADDING_DP),
        0);
    row.setBackgroundColor(ContextCompat.getColor(this, R.color.color_white));
    row.setLayoutParams(
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(52)));
    return row;
  }

  private TextView createLabel(int resId) {
    TextView view = new TextView(this);
    view.setText(resId);
    view.setTextSize(16);
    view.setGravity(Gravity.CENTER_VERTICAL);
    view.setTextColor(ContextCompat.getColor(this, R.color.color_conversation_primary_text));
    return view;
  }

  private TextView createValue(String value) {
    TextView view = new TextView(this);
    view.setText(value);
    view.setTextSize(15);
    view.setSingleLine(true);
    view.setEllipsize(TruncateAt.END);
    view.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
    view.setTextColor(ContextCompat.getColor(this, R.color.color_conversation_secondary_text));
    return view;
  }

  private View createMemberListView() {
    FrameLayout container = new FrameLayout(this);
    memberRecyclerView = new RecyclerView(this);
    memberRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    memberAdapter = new MemberAdapter();
    memberRecyclerView.setAdapter(memberAdapter);
    container.addView(
        memberRecyclerView,
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    LinearLayout emptyLayout = new LinearLayout(this);
    emptyLayout.setOrientation(LinearLayout.VERTICAL);
    emptyLayout.setGravity(Gravity.CENTER);
    emptyLayout.setVisibility(View.GONE);
    ImageView emptyImage = new ImageView(this);
    emptyImage.setImageResource(getEmptyImageRes());
    emptyLayout.addView(
        emptyImage, new LinearLayout.LayoutParams(SizeUtils.dp2px(118), SizeUtils.dp2px(96)));
    TextView emptyText = new TextView(this);
    emptyText.setText(R.string.conversation_group_member_empty);
    emptyText.setTextSize(15);
    emptyText.setGravity(Gravity.CENTER);
    emptyText.setTextColor(ContextCompat.getColor(this, R.color.color_conversation_secondary_text));
    LinearLayout.LayoutParams emptyTextParams =
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    emptyTextParams.topMargin = SizeUtils.dp2px(8);
    emptyLayout.addView(emptyText, emptyTextParams);
    container.addView(emptyLayout, createEmptyLayoutParams());
    emptyView = emptyLayout;
    return container;
  }

  private FrameLayout.LayoutParams createEmptyLayoutParams() {
    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
    params.topMargin = SizeUtils.dp2px(50);
    return params;
  }

  private void renderMembers(List<ConversationBean> data) {
    memberAdapter.setData(data);
    boolean empty = data == null || data.isEmpty();
    memberTitleView.setText(
        getString(R.string.conversation_group_member_title, empty ? 0 : data.size()));
    memberRecyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
  }

  private void showRenameDialog() {
    ConversationGroupNameDialog.show(
        this,
        R.string.conversation_group_rename_title,
        groupName,
        getPrimaryColor(),
        this::updateGroupName);
  }

  private void updateGroupName(String name, Dialog dialog) {
    String trimName = name == null ? "" : name.trim();
    if (TextUtils.isEmpty(trimName)) {
      ToastX.showShortToast(R.string.conversation_group_name_empty_tip);
      return;
    }
    if (showNetworkTipIfDisconnected()) {
      return;
    }
    viewModel.updateCustomGroupName(
        groupId,
        trimName,
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ConversationGroupErrorHelper.showErrorToast(
                ConversationGroupSettingActivity.this, errorCode);
          }

          @Override
          public void onSuccess(@Nullable Void data) {
            groupName = trimName;
            nameValueView.setText(groupName);
            dialog.dismiss();
          }
        });
  }

  private void showDeleteConfirmDialog() {
    CommonChoiceDialog dialog = new CommonChoiceDialog();
    if (isFunStyle()) {
      dialog.setPositiveTextColor(
          ContextCompat.getColor(this, R.color.fun_conversation_group_primary));
    }
    dialog
        .setTitleStr(getString(R.string.conversation_group_delete_title))
        .setContentStr(getString(R.string.conversation_group_delete_message))
        .setNegativeStr(getString(R.string.cancel_title))
        .setPositiveStr(getString(R.string.sure_title))
        .setConfirmListener(
            new ChoiceListener() {
              @Override
              public void onPositive() {
                deleteGroup();
              }

              @Override
              public void onNegative() {}
            })
        .show(getSupportFragmentManager());
  }

  protected boolean isFunStyle() {
    return false;
  }

  private int getPageBackgroundColor() {
    return ContextCompat.getColor(
        this,
        isFunStyle() ? R.color.fun_conversation_secondary_page_bg_color : R.color.color_white);
  }

  private int getNavigationBackgroundColor() {
    return ContextCompat.getColor(
        this,
        isFunStyle() ? R.color.fun_conversation_secondary_page_bg_color : R.color.color_white);
  }

  private int getTitleActionColor() {
    return ContextCompat.getColor(
        this,
        isFunStyle()
            ? R.color.fun_conversation_group_primary
            : R.color.color_conversation_group_primary);
  }

  private int getPrimaryColor() {
    return ContextCompat.getColor(
        this,
        isFunStyle()
            ? R.color.fun_conversation_group_primary
            : R.color.color_conversation_group_primary);
  }

  private int getDividerColor() {
    return ContextCompat.getColor(
        this,
        isFunStyle()
            ? R.color.fun_conversation_group_divider
            : R.color.color_conversation_group_divider);
  }

  private int getEmptyImageRes() {
    return isFunStyle() ? R.drawable.fun_ic_conversation_empty : R.drawable.ic_conversation_empty;
  }

  private int getAddIconRes() {
    return isFunStyle()
        ? R.drawable.fun_conversation_group_add_member
        : R.drawable.conversation_group_add_member;
  }

  private String getAddConversationPagePath() {
    return isFunStyle()
        ? RouterConstant.PATH_FUN_CONVERSATION_GROUP_ADD_CONVERSATION_PAGE
        : RouterConstant.PATH_CONVERSATION_GROUP_ADD_CONVERSATION_PAGE;
  }

  private void deleteGroup() {
    if (showNetworkTipIfDisconnected()) {
      return;
    }
    viewModel.deleteCustomGroup(
        groupId,
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ConversationGroupErrorHelper.showErrorToast(
                ConversationGroupSettingActivity.this, errorCode);
          }

          @Override
          public void onSuccess(@Nullable Void data) {
            finish();
          }
        });
  }

  private class MemberAdapter extends RecyclerView.Adapter<MemberViewHolder> {
    private final List<ConversationBean> data = new ArrayList<>();

    void setData(List<ConversationBean> conversations) {
      data.clear();
      if (conversations != null) {
        data.addAll(conversations);
      }
      notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      LinearLayout container = new LinearLayout(parent.getContext());
      container.setOrientation(LinearLayout.VERTICAL);
      container.setBackgroundColor(
          ContextCompat.getColor(parent.getContext(), R.color.color_white));
      container.setLayoutParams(
          new RecyclerView.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(MEMBER_ROW_HEIGHT_DP)));

      LinearLayout row = new LinearLayout(parent.getContext());
      row.setOrientation(LinearLayout.HORIZONTAL);
      row.setGravity(Gravity.CENTER_VERTICAL);
      row.setPadding(
          SizeUtils.dp2px(MEMBER_ROW_HORIZONTAL_PADDING_DP),
          0,
          SizeUtils.dp2px(MEMBER_ROW_HORIZONTAL_PADDING_DP),
          0);
      container.addView(
          row, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

      View divider = new View(parent.getContext());
      divider.setBackgroundColor(getDividerColor());
      LinearLayout.LayoutParams dividerParams =
          new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(1));
      dividerParams.leftMargin =
          isFunStyle() ? SizeUtils.dp2px(64) : SizeUtils.dp2px(MEMBER_ROW_HORIZONTAL_PADDING_DP);
      dividerParams.rightMargin =
          isFunStyle() ? 0 : SizeUtils.dp2px(MEMBER_ROW_HORIZONTAL_PADDING_DP);
      container.addView(divider, dividerParams);
      return new MemberViewHolder(container, row, divider);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
      holder.bind(data.get(position), position == data.size() - 1);
    }

    @Override
    public int getItemCount() {
      return data.size();
    }
  }

  private class MemberViewHolder extends RecyclerView.ViewHolder {
    private final ContactAvatarView avatarView;
    private final TextView nameView;
    private final TextView removeView;
    private final View divider;

    MemberViewHolder(
        @NonNull LinearLayout container, @NonNull LinearLayout row, @NonNull View divider) {
      super(container);
      this.divider = divider;
      avatarView = new ContactAvatarView(row.getContext());
      if (isFunStyle()) {
        avatarView.setCornerRadius(SizeUtils.dp2px(4));
      }
      row.addView(
          avatarView, new LinearLayout.LayoutParams(SizeUtils.dp2px(36), SizeUtils.dp2px(36)));

      LinearLayout textContainer = new LinearLayout(row.getContext());
      textContainer.setOrientation(LinearLayout.VERTICAL);
      textContainer.setGravity(Gravity.CENTER_VERTICAL);
      nameView = new TextView(row.getContext());
      nameView.setSingleLine(true);
      nameView.setEllipsize(TruncateAt.END);
      nameView.setTextSize(16);
      nameView.setTextColor(
          ContextCompat.getColor(row.getContext(), R.color.color_conversation_primary_text));
      textContainer.addView(nameView);
      LinearLayout.LayoutParams textParams =
          new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
      textParams.leftMargin = SizeUtils.dp2px(12);
      textParams.rightMargin = SizeUtils.dp2px(30);
      row.addView(textContainer, textParams);

      removeView = new TextView(row.getContext());
      removeView.setText(R.string.conversation_group_remove_conversation);
      removeView.setTextSize(14);
      removeView.setGravity(Gravity.CENTER);
      removeView.setSingleLine(true);
      removeView.setMaxWidth(SizeUtils.dp2px(REMOVE_BUTTON_MAX_WIDTH_DP));
      removeView.setPadding(
          SizeUtils.dp2px(REMOVE_BUTTON_HORIZONTAL_PADDING_DP),
          0,
          SizeUtils.dp2px(REMOVE_BUTTON_HORIZONTAL_PADDING_DP),
          0);
      removeView.setBackgroundResource(
          isFunStyle()
              ? R.drawable.fun_conversation_group_member_delete_bg
              : R.drawable.conversation_group_member_delete_bg);
      removeView.setTextColor(getPrimaryColor());
      row.addView(
          removeView,
          new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, SizeUtils.dp2px(28)));
    }

    void bind(ConversationBean bean, boolean lastItem) {
      divider.setVisibility(lastItem ? View.GONE : View.VISIBLE);
      avatarView.setData(
          bean.getConversationAvatar(),
          bean.getAvatarName(),
          AvatarColor.avatarColor(bean.getTargetId()));
      nameView.setText(bean.getConversationName());
      removeView.setOnClickListener(
          v -> {
            if (showNetworkTipIfDisconnected()) {
              return;
            }
            viewModel.removeConversationFromGroup(
                groupId,
                bean.getConversationId(),
                new FetchCallback<Void>() {
                  @Override
                  public void onError(int errorCode, @Nullable String errorMsg) {
                    ConversationGroupErrorHelper.showErrorToast(
                        ConversationGroupSettingActivity.this, errorCode);
                  }

                  @Override
                  public void onSuccess(@Nullable Void data) {
                    viewModel.loadGroupConversationsForSetting(groupId);
                  }
                });
          });
    }
  }

  private boolean showNetworkTipIfDisconnected() {
    if (NetworkUtils.isConnected()) {
      return false;
    }
    ToastX.showShortToast(R.string.conversation_network_error_tip);
    return true;
  }
}
