// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.popmenu;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatPopMenuLayoutBinding;
import com.netease.yunxin.kit.chatkit.ui.factory.ChatPopActionFactory;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.textSelectionHelper.SelectableTextHelper;
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

  private static final float ITEM_SIZE_WIDTH = 28f;

  private static final float ITEM_SIZE_HEIGHT = 42f;

  private static final float CONTAINER_PADDING = 16f;

  private final PopupWindow popupWindow;
  private final ChatPopMenuLayoutBinding layoutBinding;
  private final MenuAdapter adapter;
  private final List<PluginAction> chatPopMenuActionList = new ArrayList<>();

  public ChatPopMenu() {
    layoutBinding =
        ChatPopMenuLayoutBinding.inflate(LayoutInflater.from(IMKitClient.getApplicationContext()));
    GridLayoutManager gridLayoutManager =
        new GridLayoutManager(IMKitClient.getApplicationContext(), DEFAULT_COLUMN_NUM);
    layoutBinding.recyclerView.setLayoutManager(gridLayoutManager);
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
  public void show(View anchorView, String text, boolean isSelf, int minY) {
    ALog.d(LIB_TAG, TAG, "show text");
    initStringAction(text);
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
  public void show(View anchorView, ChatMessageBean message, int minY) {
    ALog.d(LIB_TAG, TAG, "show message");
    initDefaultAction(message);
    if (chatPopMenuActionList.size() < 1) {
      return;
    }
    showWindow(anchorView, message.getMessageData().getMessage().isSelf(), minY);
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

      int itemWidth = SizeUtils.dp2px(ITEM_SIZE_WIDTH);
      int itemHeight = SizeUtils.dp2px(ITEM_SIZE_HEIGHT);

      int paddingLeftRight = SizeUtils.dp2px(CONTAINER_PADDING);
      int paddingTopBottom = SizeUtils.dp2px(CONTAINER_PADDING);

      int columnNum = Math.min(chatPopMenuActionList.size(), DEFAULT_COLUMN_NUM);
      GridLayoutManager gridLayoutManager =
          new GridLayoutManager(IMKitClient.getApplicationContext(), columnNum);
      layoutBinding.recyclerView.setLayoutManager(gridLayoutManager);
      int popWidth = itemWidth * columnNum + paddingLeftRight * (columnNum * 2);
      int popHeight = itemHeight * rowCount + paddingTopBottom * (rowCount + 1);

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
      if (isShowing()) {
        popupWindow.update(x, y, popWidth, popHeight);
      } else {
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, x, y);
      }
    }
  }

  public boolean isShowing() {
    return popupWindow != null && popupWindow.isShowing();
  }

  public void hide() {
    if (popupWindow != null && popupWindow.isShowing()) {
      popupWindow.dismiss();
    }
  }

  @SuppressLint("NotifyDataSetChanged")
  private void initDefaultAction(ChatMessageBean message) {
    chatPopMenuActionList.clear();
    chatPopMenuActionList.addAll(ChatPopActionFactory.getInstance().getMessageActions(message));
    adapter.notifyDataSetChanged();
  }

  /**
   * 初始化文本操作
   *
   * @param text 选中文本
   */
  private void initStringAction(String text) {
    chatPopMenuActionList.clear();
    chatPopMenuActionList.addAll(ChatPopActionFactory.getInstance().getTextActions(text));
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
      holder.title.setText(chatPopMenuAction.getTitle());
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
}
