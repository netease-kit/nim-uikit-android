// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.popmenu;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatPopMenuLayoutBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatReactionEmojiItemLayoutBinding;
import com.netease.yunxin.kit.chatkit.ui.factory.ChatPopActionFactory;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.textSelectionHelper.SelectableTextHelper;
import com.netease.yunxin.kit.chatkit.ui.view.emoji.ReactionEmojiManager;
import com.netease.yunxin.kit.chatkit.ui.view.message.MessageReactionSupport;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.model.PluginAction;
import java.util.ArrayList;
import java.util.List;

/** message long click popup menu */
public class ChatPopMenu {

  private static final String TAG = "ChatPopMenu";
  private static final int DEFAULT_COLUMN_NUM = 5;

  // y offset for pop window
  private static final int Y_OFFSET = 8;

  private static final float CONTAINER_PADDING = 16f;

  private static final float MENU_BAR_HEIGHT = 50f;

  private static final float EMOJI_GRID_VIEW_HEIGHT = 200f;

  private static final int LONG_PRESS_QUICK_EMOJI_COUNT = 6;
  private static final float REACTION_GRID_EXTRA_HEIGHT = 6f;

  private final PopupWindow popupWindow;
  private final ChatPopMenuLayoutBinding layoutBinding;
  private final MenuAdapter adapter;
  private final List<PluginAction> chatPopMenuActionList = new ArrayList<>();
  private ChatMessageBean reactionMessage;
  private IChatPopMenuClickListener reactionListener;
  private boolean reactionExpanded;
  private boolean reactionOnly;
  private int popupX;
  private int popupY;
  private boolean showTop;

  public ChatPopMenu() {
    layoutBinding =
        ChatPopMenuLayoutBinding.inflate(LayoutInflater.from(IMKitClient.getApplicationContext()));
    layoutBinding.recyclerView.setLayoutManager(new FixedMenuLayoutManager());
    adapter = new MenuAdapter();
    layoutBinding.recyclerView.setAdapter(adapter);

    popupWindow =
        new PopupWindow(
            layoutBinding.getRoot(),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            false);
    popupWindow.setTouchable(true);
    popupWindow.setOutsideTouchable(true);
    // Consume the outside touch used to dismiss the popup so it cannot click the underlying reaction entry again.
    popupWindow.setFocusable(true);
  }

  /**
   * 设置pop window消失监听
   *
   * @param listener 消失监听
   */
  public void setDismissListener(PopupWindow.OnDismissListener listener) {
    if (popupWindow != null) {
      popupWindow.setOnDismissListener(listener);
    }
  }

  /**
   * 弹出pop Window，选中文本时
   *
   * @param anchorView 消息view
   * @param text 选中文本
   * @param isSelf 是否是自己
   * @param minY 最小Y值
   */
  public void show(Context context, View anchorView, String text, boolean isSelf, int minY) {
    ALog.d(LIB_TAG, TAG, "show text");
    initStringAction(context, text);
    if (chatPopMenuActionList.size() < 1) {
      return;
    }
    showWindow(anchorView, isSelf, minY);
  }

  /**
   * 弹出pop Window，真个消息选中时
   *
   * @param anchorView 消息view
   * @param message 消息
   * @param minY 最小Y值
   */
  public void show(Context context, View anchorView, ChatMessageBean message, int minY) {
    ALog.d(LIB_TAG, TAG, "show message");
    initDefaultAction(context, message);
    if (chatPopMenuActionList.size() < 1) {
      return;
    }
    if (IMKitConfigCenter.getEnableMessageReaction() && isReactionMessage(message)) {
      setupReaction(message, reactionListener);
    } else {
      clearReaction();
    }
    showWindow(anchorView, message.getMessageData().getMessage().isSelf(), minY);
  }

  public void setReactionListener(IChatPopMenuClickListener listener) {
    reactionListener = listener;
  }

  public void setReactionMessage(ChatMessageBean message) {
    reactionMessage = message;
  }

  /** 展示消息下方入口的完整表情列表弹窗。 */
  public void showEmojiOnly(View anchorView, ChatMessageBean message, boolean isSelf, int minY) {
    if (!IMKitConfigCenter.getEnableMessageReaction()) {
      return;
    }
    reactionMessage = message;
    chatPopMenuActionList.clear();
    reactionOnly = true;
    reactionExpanded = true;
    setupQuickReactionBar(false);
    layoutBinding.reactionBar.setVisibility(View.VISIBLE);
    layoutBinding.reactionDivider.setVisibility(View.VISIBLE);
    layoutBinding.recyclerView.setVisibility(View.GONE);
    layoutBinding.reactionGrid.setVisibility(View.VISIBLE);
    configureEmojiGrid();
    showReactionWindow(anchorView, isSelf, minY);
  }

  /**
   * 弹出pop Window，使用自定义 action 列表（如译文长按菜单）
   *
   * @param anchorView 锚点 View
   * @param actions 自定义 action 列表
   * @param isSelf 是否是自己发送的消息（影响弹窗水平位置）
   * @param minY 最小 Y 值（防止弹窗超出顶部）
   */
  @SuppressLint("NotifyDataSetChanged")
  public void show(View anchorView, List<PluginAction> actions, boolean isSelf, int minY) {
    ALog.d(LIB_TAG, TAG, "show custom actions, size=" + actions.size());
    chatPopMenuActionList.clear();
    chatPopMenuActionList.addAll(actions);
    if (reactionMessage == null
        || !isReactionMessage(reactionMessage)
        || !IMKitConfigCenter.getEnableMessageReaction()) {
      clearReaction();
    } else {
      setupReaction(reactionMessage, reactionListener);
    }
    adapter.notifyDataSetChanged();
    if (chatPopMenuActionList.isEmpty()) {
      return;
    }
    showWindow(anchorView, isSelf, minY);
  }

  /**
   * 显示pop window
   *
   * @param anchorView 标的view
   * @param isSelf 是否是自己
   * @param minY 最小Y值
   */
  private void showWindow(View anchorView, boolean isSelf, int minY) {
    float anchorWidth = anchorView.getWidth();
    float anchorHeight = anchorView.getHeight();
    int[] location = new int[2];
    anchorView.getLocationOnScreen(location);

    int rowCount = (int) Math.ceil(chatPopMenuActionList.size() * 1.0f / DEFAULT_COLUMN_NUM);
    if (popupWindow != null) {

      int itemHeight =
          layoutBinding
              .getRoot()
              .getResources()
              .getDimensionPixelSize(R.dimen.chat_pop_menu_item_height);

      int paddingTopBottom = SizeUtils.dp2px(CONTAINER_PADDING);
      FixedMenuLayoutManager menuLayoutManager =
          (FixedMenuLayoutManager) layoutBinding.recyclerView.getLayoutManager();
      if (menuLayoutManager != null) {
        menuLayoutManager.setReactionVisible(reactionMessage != null);
      }
      int menuHeight = itemHeight * rowCount + paddingTopBottom;
      layoutBinding
          .getRoot()
          .measure(
              View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
              View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
      int popWidth = layoutBinding.getRoot().getMeasuredWidth();
      int popHeight;
      if (reactionMessage != null) {
        popHeight =
            reactionExpanded
                ? SizeUtils.dp2px(MENU_BAR_HEIGHT + EMOJI_GRID_VIEW_HEIGHT)
                : menuHeight + SizeUtils.dp2px(MENU_BAR_HEIGHT);
      } else {
        popHeight = menuHeight;
      }

      int x = location[0];
      int y = location[1] - popHeight - Y_OFFSET;
      // if this is a send message,show on right
      if (isSelf) {
        x = (int) (location[0] + anchorWidth - popWidth);
      }
      // if is top show pop below anchorView,else show above
      boolean isTop = y <= minY;
      if (isTop) {
        y = (int) (location[1] + anchorHeight) + Y_OFFSET;
      }
      if (reactionMessage != null) {
        popupX = x;
        popupY = y + popHeight;
        showTop = !isTop;
      }
      if (isShowing()) {
        popupWindow.update(x, y, popWidth, popHeight);
      } else {
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, x, y);
      }
    }
  }

  private void setupReaction(ChatMessageBean message, IChatPopMenuClickListener listener) {
    List<Long> quickIndexes = ReactionEmojiManager.getQuickIndexes();
    int count = Math.min(quickIndexes.size(), LONG_PRESS_QUICK_EMOJI_COUNT);
    setupReaction(message, listener, quickIndexes.subList(0, count));
  }

  private void setupReaction(
      ChatMessageBean message, IChatPopMenuClickListener listener, List<Long> quickIndexes) {
    reactionMessage = message;
    reactionExpanded = false;
    reactionOnly = false;
    layoutBinding.reactionBar.setVisibility(View.VISIBLE);
    layoutBinding.reactionDivider.setVisibility(View.VISIBLE);
    layoutBinding.reactionGrid.setVisibility(View.GONE);
    layoutBinding.recyclerView.setVisibility(View.VISIBLE);
    setupQuickReactionBar(true, quickIndexes);
  }

  private void setupQuickReactionBar(boolean includeExpandControl) {
    setupQuickReactionBar(includeExpandControl, ReactionEmojiManager.getQuickIndexes());
  }

  private void setupQuickReactionBar(boolean includeExpandControl, List<Long> quickIndexes) {
    layoutBinding.reactionExpand.setVisibility(includeExpandControl ? View.VISIBLE : View.GONE);
    ViewGroup.LayoutParams quickContainerParams =
        layoutBinding.reactionQuickContainer.getLayoutParams();
    quickContainerParams.width = 0;
    if (quickContainerParams instanceof LinearLayout.LayoutParams) {
      ((LinearLayout.LayoutParams) quickContainerParams).weight = 1f;
    }
    layoutBinding.reactionQuickContainer.setLayoutParams(quickContainerParams);
    layoutBinding.reactionQuickContainer.removeAllViews();
    for (Long reactionIndex : quickIndexes) {
      if (!ReactionEmojiManager.isValidIndex(reactionIndex)) {
        continue;
      }
      ImageView image = new ImageView(layoutBinding.getRoot().getContext());
      image.setImageDrawable(ReactionEmojiManager.getDrawable(reactionIndex));
      float visualScale = ReactionEmojiManager.getVisualScale(reactionIndex);
      image.setScaleX(visualScale);
      image.setScaleY(visualScale);
      image.setTag(reactionIndex);
      image.setOnClickListener(
          v -> {
            if (reactionListener != null && reactionMessage != null) {
              reactionListener.onEmojiReaction(reactionMessage, (Long) v.getTag());
            }
            hide();
          });
      LinearLayout.LayoutParams params =
          new LinearLayout.LayoutParams(
              layoutBinding
                  .getRoot()
                  .getResources()
                  .getDimensionPixelSize(R.dimen.chat_reaction_popup_slot_size),
              layoutBinding
                  .getRoot()
                  .getResources()
                  .getDimensionPixelSize(R.dimen.chat_reaction_popup_emoji_size));
      params.gravity = Gravity.CENTER_VERTICAL;
      layoutBinding.reactionQuickContainer.addView(image, params);
    }
    if (includeExpandControl) {
      layoutBinding.reactionExpand.setImageResource(R.drawable.ic_chat_emoji_arrow_down);
      layoutBinding.reactionExpand.setOnClickListener(
          v -> {
            reactionExpanded = !reactionExpanded;
            layoutBinding.reactionGrid.setVisibility(reactionExpanded ? View.VISIBLE : View.GONE);
            layoutBinding.recyclerView.setVisibility(reactionExpanded ? View.GONE : View.VISIBLE);
            layoutBinding.reactionExpand.setImageResource(
                reactionExpanded
                    ? R.drawable.ic_chat_emoji_arrow_up
                    : R.drawable.ic_chat_emoji_arrow_down);
            updateReactionWindow(reactionExpanded);
            if (reactionExpanded) {
              configureEmojiGrid();
            }
          });
    } else {
      layoutBinding.reactionExpand.setOnClickListener(null);
    }
  }

  private void configureEmojiGrid() {
    layoutBinding.reactionGrid.setAdapter(new AllEmojiAdapter());
    layoutBinding.reactionGrid.setOnItemClickListener(
        (parent, view, position, id) -> {
          if (reactionListener != null && reactionMessage != null) {
            reactionListener.onEmojiReaction(
                reactionMessage, ReactionEmojiManager.getAllIndexes().get(position));
          }
          hide();
        });
  }

  private void showReactionWindow(View anchorView, boolean isSelf, int minY) {
    float anchorWidth = anchorView.getWidth();
    float anchorHeight = anchorView.getHeight();
    int[] location = new int[2];
    anchorView.getLocationOnScreen(location);
    layoutBinding
        .getRoot()
        .measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
    int popWidth = layoutBinding.getRoot().getMeasuredWidth();
    int popHeight =
        SizeUtils.dp2px(
            reactionOnly
                ? MENU_BAR_HEIGHT + EMOJI_GRID_VIEW_HEIGHT + REACTION_GRID_EXTRA_HEIGHT
                : EMOJI_GRID_VIEW_HEIGHT);
    int x = location[0];
    int y = location[1] - popHeight - Y_OFFSET;
    if (isSelf) {
      x = (int) (location[0] + anchorWidth - popWidth);
    }
    boolean isTop = y <= minY;
    if (isTop) {
      y = (int) (location[1] + anchorHeight) + Y_OFFSET;
    }
    popupX = x;
    popupY = y + popHeight;
    showTop = !isTop;
    if (isShowing()) {
      popupWindow.update(x, y, popWidth, popHeight);
    } else {
      popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, x, y);
    }
  }

  private void updateReactionWindow(boolean expanded) {
    if (!showTop || !popupWindow.isShowing()) return;
    int rowCount = (int) Math.ceil(chatPopMenuActionList.size() * 1.0f / DEFAULT_COLUMN_NUM);
    int menuHeight =
        layoutBinding
                    .getRoot()
                    .getResources()
                    .getDimensionPixelSize(R.dimen.chat_pop_menu_item_height)
                * rowCount
            + SizeUtils.dp2px(CONTAINER_PADDING);
    int popHeight =
        expanded
            ? SizeUtils.dp2px(MENU_BAR_HEIGHT + EMOJI_GRID_VIEW_HEIGHT)
            : reactionOnly
                ? SizeUtils.dp2px(MENU_BAR_HEIGHT)
                : menuHeight + SizeUtils.dp2px(MENU_BAR_HEIGHT);
    popupWindow.update(popupX, popupY - popHeight, -1, -1);
  }

  private void clearReaction() {
    reactionMessage = null;
    reactionExpanded = false;
    reactionOnly = false;
    layoutBinding.reactionBar.setVisibility(View.GONE);
    layoutBinding.reactionDivider.setVisibility(View.GONE);
    layoutBinding.reactionGrid.setVisibility(View.GONE);
    layoutBinding.recyclerView.setVisibility(View.VISIBLE);
    layoutBinding.reactionExpand.setVisibility(View.VISIBLE);
  }

  private boolean isReactionMessage(ChatMessageBean message) {
    return MessageReactionSupport.canOperate(message);
  }

  public boolean isShowing() {
    return popupWindow != null && popupWindow.isShowing();
  }

  public void hide() {
    if (popupWindow != null && popupWindow.isShowing()) {
      popupWindow.dismiss();
    }
  }

  private final class AllEmojiAdapter extends BaseAdapter {
    @Override
    public int getCount() {
      return ReactionEmojiManager.getCount();
    }

    @Override
    public Object getItem(int position) {
      return ReactionEmojiManager.getAllIndexes().get(position);
    }

    @Override
    public long getItemId(int position) {
      return ReactionEmojiManager.getAllIndexes().get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      ChatReactionEmojiItemLayoutBinding binding =
          ChatReactionEmojiItemLayoutBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false);
      binding.ivReactionEmoji.setScaleType(ImageView.ScaleType.FIT_CENTER);
      binding.ivReactionEmoji.setImageDrawable(
          ReactionEmojiManager.getDrawable(ReactionEmojiManager.getAllIndexes().get(position)));
      return binding.getRoot();
    }
  }

  @SuppressLint("NotifyDataSetChanged")
  private void initDefaultAction(Context context, ChatMessageBean message) {
    chatPopMenuActionList.clear();
    chatPopMenuActionList.addAll(
        ChatPopActionFactory.getInstance().getMessageActions(context, message));
    adapter.notifyDataSetChanged();
  }

  /**
   * 初始化文本操作
   *
   * @param text 选中文本
   */
  private void initStringAction(Context context, String text) {
    chatPopMenuActionList.clear();
    chatPopMenuActionList.addAll(ChatPopActionFactory.getInstance().getTextActions(context, text));
    adapter.notifyDataSetChanged();
  }

  private PluginAction getChatPopMenuAction(int position) {
    return chatPopMenuActionList.get(position);
  }

  class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuItemViewHolder> {

    @NonNull
    @Override
    public MenuAdapter.MenuItemViewHolder onCreateViewHolder(
        @NonNull ViewGroup parent, int viewType) {
      View view =
          LayoutInflater.from(IMKitClient.getApplicationContext())
              .inflate(R.layout.chat_pop_menu_item_layout, parent, false);
      return new MenuItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuAdapter.MenuItemViewHolder holder, int position) {
      PluginAction chatPopMenuAction = getChatPopMenuAction(position);
      if (chatPopMenuAction.getTitleRes() != 0) {
        holder.title.setText(
            IMKitClient.getApplicationContext().getString(chatPopMenuAction.getTitleRes()));
      } else {
        holder.title.setText(chatPopMenuAction.getTitle());
      }
      Drawable drawable =
          ResourcesCompat.getDrawable(
              IMKitClient.getApplicationContext().getResources(),
              chatPopMenuAction.getIcon(),
              null);
      holder.icon.setImageDrawable(drawable);
      holder.itemView.setOnClickListener(
          v -> {
            if (chatPopMenuAction.getActionClickListener() != null) {
              // 文本选择器Dismiss
              SelectableTextHelper.getInstance().dismiss();
              chatPopMenuAction.getActionClickListener().onClick(v, chatPopMenuAction.getBean());
            }
            hide();
          });
    }

    @Override
    public int getItemCount() {
      return chatPopMenuActionList.size();
    }

    class MenuItemViewHolder extends RecyclerView.ViewHolder {
      public TextView title;
      public ImageView icon;

      public MenuItemViewHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.menu_title);
        icon = itemView.findViewById(R.id.menu_icon);
      }
    }
  }

  /** Keeps menu items at fixed size and spacing instead of redistributing grid spans. */
  private final class FixedMenuLayoutManager extends RecyclerView.LayoutManager {
    private boolean reactionVisible;

    void setReactionVisible(boolean visible) {
      reactionVisible = visible;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
      return new RecyclerView.LayoutParams(
          getDimension(R.dimen.chat_pop_menu_item_width),
          getDimension(R.dimen.chat_pop_menu_item_height));
    }

    @Override
    public boolean canScrollVertically() {
      return false;
    }

    @Override
    public void onMeasure(
        RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
      int itemCount = state.getItemCount();
      int columnCount = Math.min(itemCount, DEFAULT_COLUMN_NUM);
      int rowCount =
          columnCount == 0 ? 0 : (itemCount + DEFAULT_COLUMN_NUM - 1) / DEFAULT_COLUMN_NUM;
      int itemWidth = getDimension(R.dimen.chat_pop_menu_item_width);
      int itemHeight = getDimension(R.dimen.chat_pop_menu_item_height);
      int itemSpacing = getDimension(R.dimen.chat_pop_menu_item_horizontal_spacing);
      int desiredWidth =
          reactionVisible
              ? getDimension(R.dimen.chat_reaction_popup_width)
              : columnCount * itemWidth + Math.max(0, columnCount - 1) * itemSpacing;
      int desiredHeight =
          rowCount * itemHeight + getDimension(R.dimen.chat_pop_menu_item_vertical_padding) * 2;
      setMeasuredDimension(
          View.resolveSize(desiredWidth, widthSpec), View.resolveSize(desiredHeight, heightSpec));
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
      detachAndScrapAttachedViews(recycler);
      int itemWidth = getDimension(R.dimen.chat_pop_menu_item_width);
      int itemHeight = getDimension(R.dimen.chat_pop_menu_item_height);
      int itemSpacing = getDimension(R.dimen.chat_pop_menu_item_horizontal_spacing);
      for (int position = 0; position < state.getItemCount(); position++) {
        View child = recycler.getViewForPosition(position);
        addView(child);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
        params.width = itemWidth;
        params.height = itemHeight;
        child.setLayoutParams(params);
        measureChildWithMargins(child, 0, 0);
        int column = position % DEFAULT_COLUMN_NUM;
        int row = position / DEFAULT_COLUMN_NUM;
        int left = getPaddingLeft() + column * (itemWidth + itemSpacing);
        int top = getPaddingTop() + row * itemHeight;
        layoutDecorated(child, left, top, left + itemWidth, top + itemHeight);
      }
    }

    private int getDimension(int resourceId) {
      return layoutBinding.getRoot().getResources().getDimensionPixelSize(resourceId);
    }
  }
}
