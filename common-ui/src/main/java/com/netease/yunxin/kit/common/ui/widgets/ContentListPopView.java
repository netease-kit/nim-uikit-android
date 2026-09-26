// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.widgets;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import com.netease.yunxin.kit.common.ui.databinding.ContentPopListViewBinding;
import com.netease.yunxin.kit.common.ui.databinding.ContentPopListViewWithShadowBinding;
import java.util.ArrayList;
import java.util.List;

public class ContentListPopView extends PopupWindow {
  private final LinearLayout llGroup;

  public ContentListPopView(Context context, List<Item> itemList, Config config) {
    super(context);
    View rootView;
    View backgroundView;
    if (config.enableShadow) {
      ContentPopListViewWithShadowBinding binding =
          ContentPopListViewWithShadowBinding.inflate(LayoutInflater.from(context));
      rootView = binding.getRoot();
      llGroup = binding.llGroup;
      backgroundView = binding.cardView;
    } else {
      ContentPopListViewBinding binding =
          ContentPopListViewBinding.inflate(LayoutInflater.from(context));
      rootView = binding.getRoot();
      llGroup = binding.llGroup;
      backgroundView = llGroup;
    }
    if (config.bgColor != null) {
      backgroundView.setBackgroundColor(config.bgColor);
    }
    if (config.bgRes != null) {
      backgroundView.setBackgroundResource(config.bgRes);
    }
    initUI(itemList);
    setContentView(rootView);
    setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
    setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
    setOutsideTouchable(true);
    setBackgroundDrawable(new ColorDrawable(0x00000000));
  }

  private void initUI(List<Item> itemList) {
    if (itemList == null || itemList.isEmpty()) {
      return;
    }
    for (Item item : itemList) {
      if (item == null) {
        continue;
      }
      item.itemView.setOnClickListener(
          v -> {
            if (item.clickListener != null) {
              item.clickListener.onClick(v);
            }
            if (item.autoDismissWhenClicked && isShowing()) {
              dismiss();
            }
          });
      llGroup.addView(item.itemView, item.params);
    }
  }

  public static class Builder {
    private final Context context;
    private final List<Item> itemList = new ArrayList<>();
    private final Config config = new Config();

    public Builder(Context context) {
      this.context = context;
    }

    public Builder addItem(Item item) {
      itemList.add(item);
      return this;
    }

    public Builder backgroundColor(int color) {
      config.bgColor = color;
      return this;
    }

    public Builder backgroundRes(int resId) {
      config.bgRes = resId;
      return this;
    }

    public Builder enableShadow(boolean enable) {
      config.enableShadow = enable;
      return this;
    }

    public ContentListPopView build() {
      return new ContentListPopView(context, itemList, config);
    }
  }

  public static class Config {
    public Integer bgColor;
    public Integer bgRes;
    public boolean enableShadow = true;
  }

  public static class Item {
    private final View itemView;
    private final View.OnClickListener clickListener;
    private final boolean autoDismissWhenClicked;
    private final LinearLayout.LayoutParams params;

    public Item(
        View itemView, LinearLayout.LayoutParams params, View.OnClickListener clickListener) {
      this(itemView, params, clickListener, true);
    }

    public Item(
        View itemView,
        LinearLayout.LayoutParams params,
        View.OnClickListener clickListener,
        boolean autoDismissWhenClicked) {
      this.itemView = itemView;
      this.params = params;
      this.clickListener = clickListener;
      this.autoDismissWhenClicked = autoDismissWhenClicked;
    }

    public static class Builder {
      private View itemView;
      private View.OnClickListener clickListener;
      private boolean autoDismissWhenClicked = true;
      private LinearLayout.LayoutParams params =
          new LinearLayout.LayoutParams(
              ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

      public Item.Builder configView(View view) {
        this.itemView = view;
        return this;
      }

      public Item.Builder configClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
        return this;
      }

      public Item.Builder configAutoDismissWhenClicked(boolean autoDismissWhenClicked) {
        this.autoDismissWhenClicked = autoDismissWhenClicked;
        return this;
      }

      public Item.Builder configParams(LinearLayout.LayoutParams params) {
        this.params = params;
        return this;
      }

      public Item build() {
        return new Item(itemView, params, clickListener, autoDismissWhenClicked);
      }
    }
  }
}
