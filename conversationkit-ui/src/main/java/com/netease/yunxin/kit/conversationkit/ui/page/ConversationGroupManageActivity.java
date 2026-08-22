// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.page;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMConversationGroup;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationGroupErrorHelper;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationGroupSystemBarHelper;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationGroupBean;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationGroupType;
import com.netease.yunxin.kit.conversationkit.ui.page.viewmodel.ConversationGroupViewModel;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;
import java.util.List;

public class ConversationGroupManageActivity extends BaseLocalActivity {

  private static final int GROUP_ROW_HEIGHT_DP = 52;

  private ConversationGroupViewModel viewModel;
  private RecyclerView visibleContainer;
  private RecyclerView hiddenContainer;
  private TextView hiddenEmptyView;
  private final List<ConversationGroupBean> groups = new ArrayList<>();
  private GroupAdapter visibleAdapter;
  private GroupAdapter hiddenAdapter;
  private ItemTouchHelper visibleDragHelper;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    viewModel = new ViewModelProvider(this).get(ConversationGroupViewModel.class);
    setContentView(createContentView());
    setupSystemBars();
    viewModel.getGroupLiveData().observe(this, this::renderGroups);
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (viewModel != null) {
      viewModel.loadGroups();
    }
  }

  @Override
  protected void onDestroy() {
    if (visibleDragHelper != null) {
      visibleDragHelper.attachToRecyclerView(null);
      visibleDragHelper = null;
    }
    super.onDestroy();
  }

  private void setupSystemBars() {
    ConversationGroupSystemBarHelper.apply(
        this,
        ContextCompat.getColor(
            this,
            isFunStyle()
                ? R.color.fun_conversation_secondary_page_bg_color
                : R.color.color_conversation_divider));
  }

  private View createContentView() {
    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setBackgroundColor(
        ContextCompat.getColor(
            this,
            isFunStyle()
                ? R.color.fun_conversation_secondary_page_bg_color
                : R.color.color_conversation_divider));

    BackTitleBar titleBar = new BackTitleBar(this);
    titleBar.setBackgroundColor(
        ContextCompat.getColor(
            this,
            isFunStyle()
                ? R.color.fun_conversation_secondary_page_bg_color
                : R.color.color_conversation_divider));
    titleBar
        .setTitle(R.string.conversation_group_manage_title)
        .setOnBackIconClickListener(v -> finish());
    root.addView(
        titleBar,
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

    ScrollView scrollView = new ScrollView(this);
    LinearLayout content = new LinearLayout(this);
    content.setOrientation(LinearLayout.VERTICAL);
    scrollView.addView(
        content,
        new ScrollView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    root.addView(
        scrollView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

    content.addView(createSectionTitle(R.string.conversation_group_visible_title));
    visibleContainer = createSectionContainer();
    content.addView(visibleContainer);

    content.addView(createSectionTitle(R.string.conversation_group_hidden_title));
    hiddenContainer = createSectionContainer();
    content.addView(hiddenContainer);
    hiddenEmptyView = createEmptyView();
    content.addView(hiddenEmptyView);

    TextView createView = createCreateGroupView();
    root.addView(createView);
    return root;
  }

  private TextView createSectionTitle(int stringRes) {
    TextView title = new TextView(this);
    title.setText(stringRes);
    title.setTextSize(13);
    title.setGravity(Gravity.CENTER_VERTICAL);
    title.setTextColor(ContextCompat.getColor(this, R.color.color_conversation_secondary_text));
    title.setPadding(
        SizeUtils.dp2px(16), SizeUtils.dp2px(18), SizeUtils.dp2px(16), SizeUtils.dp2px(8));
    return title;
  }

  private RecyclerView createSectionContainer() {
    RecyclerView container = new RecyclerView(this);
    container.setLayoutManager(new LinearLayoutManager(this));
    container.setBackgroundResource(R.drawable.conversation_group_section_bg);
    container.setNestedScrollingEnabled(false);
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    if (!isFunStyle()) {
      params.leftMargin = SizeUtils.dp2px(16);
      params.rightMargin = SizeUtils.dp2px(16);
    }
    container.setLayoutParams(params);
    return container;
  }

  private TextView createEmptyView() {
    TextView view = new TextView(this);
    view.setText(R.string.conversation_group_hidden_empty);
    view.setTextSize(13);
    view.setGravity(Gravity.CENTER_VERTICAL);
    view.setTextColor(ContextCompat.getColor(this, R.color.color_conversation_secondary_text));
    view.setPadding(SizeUtils.dp2px(16), 0, SizeUtils.dp2px(16), 0);
    view.setVisibility(View.GONE);
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(40));
    view.setLayoutParams(params);
    return view;
  }

  private TextView createCreateGroupView() {
    TextView view = new TextView(this);
    view.setText(R.string.conversation_group_create);
    view.setTextSize(16);
    view.setGravity(Gravity.CENTER);
    view.setTextColor(ContextCompat.getColor(this, R.color.color_white));
    view.setBackgroundResource(
        isFunStyle()
            ? R.drawable.fun_conversation_group_create_button_bg
            : R.drawable.conversation_group_create_button_bg);
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(SizeUtils.dp2px(315), SizeUtils.dp2px(50));
    params.gravity = Gravity.CENTER_HORIZONTAL;
    params.topMargin = SizeUtils.dp2px(12);
    params.bottomMargin = SizeUtils.dp2px(60);
    view.setLayoutParams(params);
    view.setOnClickListener(v -> showCreateGroupDialog());
    return view;
  }

  private void renderGroups(List<ConversationGroupBean> data) {
    groups.clear();
    if (data != null) {
      groups.addAll(data);
    }
    List<ConversationGroupBean> visibleGroups = new ArrayList<>();
    List<ConversationGroupBean> hiddenGroups = new ArrayList<>();
    for (ConversationGroupBean group : groups) {
      if (group.isVisible()) {
        visibleGroups.add(group);
      } else {
        hiddenGroups.add(group);
      }
    }
    visibleAdapter = new GroupAdapter(visibleGroups, true);
    hiddenAdapter = new GroupAdapter(hiddenGroups, false);
    visibleContainer.setAdapter(visibleAdapter);
    hiddenContainer.setAdapter(hiddenAdapter);
    updateSectionHeight(visibleContainer, visibleGroups.size());
    updateSectionHeight(hiddenContainer, hiddenGroups.size());
    attachDrag(visibleContainer, visibleAdapter);
    hiddenContainer.setVisibility(hiddenGroups.isEmpty() ? View.GONE : View.VISIBLE);
    hiddenEmptyView.setVisibility(View.GONE);
  }

  private void updateSectionHeight(RecyclerView container, int itemCount) {
    ViewGroup.LayoutParams params = container.getLayoutParams();
    params.height = SizeUtils.dp2px(GROUP_ROW_HEIGHT_DP * Math.max(itemCount, 0));
    container.setLayoutParams(params);
  }

  private View createGroupRow(
      ConversationGroupBean group, boolean visible, RecyclerView.ViewHolder holder) {
    LinearLayout row = new LinearLayout(this);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setPadding(SizeUtils.dp2px(16), 0, SizeUtils.dp2px(16), 0);
    row.setLayoutParams(
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(GROUP_ROW_HEIGHT_DP)));

    ImageView action = new ImageView(this);
    if (group.getType() == ConversationGroupType.ALL) {
      action.setImageResource(R.drawable.ic_conversation_group_disable);
      action.setEnabled(false);
    } else if (visible) {
      action.setImageResource(R.drawable.ic_conversation_group_remove);
      action.setOnClickListener(v -> toggleGroupVisible(group));
    } else {
      action.setImageResource(R.drawable.ic_conversation_group_add);
      action.setOnClickListener(v -> toggleGroupVisible(group));
    }
    LinearLayout.LayoutParams actionParams =
        new LinearLayout.LayoutParams(SizeUtils.dp2px(16), SizeUtils.dp2px(16));
    actionParams.rightMargin = SizeUtils.dp2px(16);
    row.addView(action, actionParams);

    TextView name = new TextView(this);
    name.setText(group.getName());
    name.setTextSize(16);
    name.setSingleLine(true);
    name.setEllipsize(TextUtils.TruncateAt.END);
    name.setGravity(Gravity.CENTER_VERTICAL);
    name.setTextColor(ContextCompat.getColor(this, R.color.color_conversation_primary_text));
    row.addView(name, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

    if (group.canOpenSetting()) {
      ImageView setting = new ImageView(this);
      setting.setImageResource(R.drawable.ic_conversation_group_setting);
      setting.setOnClickListener(
          v ->
              XKitRouter.withKey(getSettingPagePath())
                  .withContext(this)
                  .withParam(RouterConstant.KEY_CONVERSATION_GROUP_ID, group.getId())
                  .withParam(RouterConstant.KEY_CONVERSATION_GROUP_NAME, group.getName())
                  .navigate());
      LinearLayout.LayoutParams settingParams =
          new LinearLayout.LayoutParams(SizeUtils.dp2px(16), SizeUtils.dp2px(16));
      settingParams.leftMargin = SizeUtils.dp2px(12);
      row.addView(setting, settingParams);
    }

    if (visible && group.getType() != ConversationGroupType.ALL) {
      ImageView drag = new ImageView(this);
      drag.setImageResource(R.drawable.ic_conversation_group_right_draw);
      drag.setOnTouchListener(
          (v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN
                && visibleDragHelper != null
                && group.getType() != ConversationGroupType.ALL) {
              visibleDragHelper.startDrag(holder);
              return true;
            }
            return false;
          });
      LinearLayout.LayoutParams dragParams =
          new LinearLayout.LayoutParams(SizeUtils.dp2px(16), SizeUtils.dp2px(16));
      dragParams.leftMargin = SizeUtils.dp2px(16);
      row.addView(drag, dragParams);
    }
    return row;
  }

  private void toggleGroupVisible(ConversationGroupBean group) {
    if (group.getType() == ConversationGroupType.ALL) {
      return;
    }
    boolean visible = !group.isVisible();
    viewModel.setGroupVisible(group, visible);
  }

  private void attachDrag(RecyclerView recyclerView, GroupAdapter adapter) {
    if (adapter.visibleList && visibleDragHelper != null) {
      visibleDragHelper.attachToRecyclerView(null);
      visibleDragHelper = null;
    }
    ItemTouchHelper helper =
        new ItemTouchHelper(
            new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
              @Override
              public boolean isLongPressDragEnabled() {
                return false;
              }

              @Override
              public int getMovementFlags(
                  RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                  return 0;
                }
                ConversationGroupBean group = adapter.getItem(position);
                if (group == null || group.getType() == ConversationGroupType.ALL) {
                  return 0;
                }
                return super.getMovementFlags(recyclerView, viewHolder);
              }

              @Override
              public boolean onMove(
                  RecyclerView recyclerView,
                  RecyclerView.ViewHolder viewHolder,
                  RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getBindingAdapterPosition();
                int toPosition = target.getBindingAdapterPosition();
                if (fromPosition == RecyclerView.NO_POSITION
                    || toPosition == RecyclerView.NO_POSITION) {
                  return false;
                }
                return adapter.move(fromPosition, toPosition);
              }

              @Override
              public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {}

              @Override
              public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                saveCurrentOrder();
              }
            });
    helper.attachToRecyclerView(recyclerView);
    if (adapter.visibleList) {
      visibleDragHelper = helper;
    }
  }

  private void saveCurrentOrder() {
    if (visibleAdapter == null || hiddenAdapter == null) {
      return;
    }
    viewModel.saveGroupOrder(visibleAdapter.getData(), hiddenAdapter.getData());
  }

  protected void showCreateGroupDialog() {
    if (isFunStyle()) {
      startActivity(new Intent(this, ConversationGroupNameActivity.class));
      return;
    }
    ConversationGroupNameDialog.show(
        this,
        R.string.conversation_group_create,
        null,
        ContextCompat.getColor(this, getPrimaryColorRes()),
        this::createCustomGroup);
  }

  private void createCustomGroup(String name, Dialog dialog) {
    String trimName = name == null ? "" : name.trim();
    if (TextUtils.isEmpty(trimName)) {
      ToastX.showShortToast(R.string.conversation_group_name_empty_tip);
      return;
    }
    if (viewModel.getCustomGroupCount() >= ConversationGroupViewModel.CUSTOM_GROUP_MAX_COUNT) {
      ToastX.showShortToast(R.string.conversation_group_create_limit_tip);
      return;
    }
    if (!NetworkUtils.isConnected()) {
      ToastX.showShortToast(R.string.conversation_network_error_tip);
      return;
    }
    viewModel.createCustomGroup(
        trimName,
        new FetchCallback<V2NIMConversationGroup>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ConversationGroupErrorHelper.showErrorToast(
                ConversationGroupManageActivity.this, errorCode);
          }

          @Override
          public void onSuccess(@Nullable V2NIMConversationGroup data) {
            if (data == null) {
              ToastX.showShortToast(R.string.conversation_group_create_failed);
              return;
            }
            dialog.dismiss();
          }
        });
  }

  protected boolean isFunStyle() {
    return false;
  }

  protected int getPrimaryColorRes() {
    return isFunStyle()
        ? R.color.fun_conversation_group_primary
        : R.color.color_conversation_group_primary;
  }

  protected String getSettingPagePath() {
    return isFunStyle()
        ? RouterConstant.PATH_FUN_CONVERSATION_GROUP_SETTING_PAGE
        : RouterConstant.PATH_CONVERSATION_GROUP_SETTING_PAGE;
  }

  private class GroupAdapter extends RecyclerView.Adapter<GroupViewHolder> {
    private final List<ConversationGroupBean> data;
    private final boolean visibleList;

    GroupAdapter(List<ConversationGroupBean> data, boolean visibleList) {
      this.data = data == null ? new ArrayList<>() : data;
      this.visibleList = visibleList;
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      FrameLayout container = new FrameLayout(parent.getContext());
      container.setLayoutParams(
          new RecyclerView.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(GROUP_ROW_HEIGHT_DP)));
      return new GroupViewHolder(container);
    }

    @Override
    public void onBindViewHolder(GroupViewHolder holder, int position) {
      holder.container.removeAllViews();
      holder.container.addView(
          createGroupRow(data.get(position), visibleList, holder),
          new FrameLayout.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
      if (isFunStyle() && position < data.size() - 1) {
        View divider = new View(holder.container.getContext());
        divider.setBackgroundColor(
            ContextCompat.getColor(
                holder.container.getContext(), R.color.fun_conversation_group_divider));
        FrameLayout.LayoutParams dividerParams =
            new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(0.5f), Gravity.BOTTOM);
        dividerParams.leftMargin = SizeUtils.dp2px(54);
        holder.container.addView(divider, dividerParams);
      }
    }

    @Override
    public int getItemCount() {
      return data.size();
    }

    ConversationGroupBean getItem(int position) {
      if (position < 0 || position >= data.size()) {
        return null;
      }
      return data.get(position);
    }

    List<ConversationGroupBean> getData() {
      return new ArrayList<>(data);
    }

    boolean move(int fromPosition, int toPosition) {
      if (fromPosition < 0
          || toPosition < 0
          || fromPosition >= data.size()
          || toPosition >= data.size()) {
        return false;
      }
      if (data.get(fromPosition).getType() == ConversationGroupType.ALL) {
        return false;
      }
      int targetPosition = Math.max(toPosition, 1);
      if (fromPosition == targetPosition) {
        return true;
      }
      ConversationGroupBean item = data.remove(fromPosition);
      data.add(targetPosition, item);
      notifyItemMoved(fromPosition, targetPosition);
      return true;
    }
  }

  private static class GroupViewHolder extends RecyclerView.ViewHolder {
    private final FrameLayout container;

    GroupViewHolder(FrameLayout container) {
      super(container);
      this.container = container;
    }
  }
}
