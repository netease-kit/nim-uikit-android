// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.emoji;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.ChatUIConfig;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.StickerConfig;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatEmojiLayoutBinding;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import java.util.ArrayList;
import java.util.List;

/** emoji picker view */
public class EmojiPickerView extends LinearLayout implements IEmojiCategoryChanged {
  private Context context;
  private ChatEmojiLayoutBinding viewBinding;

  private IEmojiSelectedListener listener;
  private IStickerSelectedListener stickerListener;
  private StickerAssetManager stickerAssetManager;

  private boolean loaded = false;

  private EmojiView gifView;

  private int categoryIndex;

  private Handler uiHandler;
  private Runnable selectedVisibleRunnable;

  private int tabDividerColorResId = R.color.color_d9d9d9;
  private int tabSelectedBackgroundColor;
  private int pageIndicatorSelectorResId = R.drawable.chat_emoji_page_indicator_selector;
  private final List<View> tabDividers = new ArrayList<>();

  public EmojiPickerView(Context context) {
    super(context);
    init(context);
  }

  public EmojiPickerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public EmojiPickerView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context);
  }

  private void init(Context context) {
    this.context = context;
    tabSelectedBackgroundColor =
        ContextCompat.getColor(context, R.color.chat_emoji_tab_selected_bg);
    this.uiHandler = new Handler(context.getMainLooper());
    viewBinding = ChatEmojiLayoutBinding.inflate(LayoutInflater.from(context), this, true);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    setupEmojiView();
  }

  @Override
  protected void onDetachedFromWindow() {
    if (selectedVisibleRunnable != null) {
      uiHandler.removeCallbacks(selectedVisibleRunnable);
      selectedVisibleRunnable = null;
    }
    if (gifView != null) {
      gifView.release();
    }
    super.onDetachedFromWindow();
    if (stickerAssetManager != null) {
      stickerAssetManager.clear();
      stickerAssetManager = null;
    }
    gifView = null;
    loaded = false;
  }

  public void show(IEmojiSelectedListener listener) {
    show(listener, null);
  }

  public void show(IEmojiSelectedListener listener, IStickerSelectedListener stickerListener) {
    setListener(listener);
    this.stickerListener = stickerListener;
    if (!loaded) {
      loadStickers();
      loaded = true;
    }
    show();
  }

  public void setListener(IEmojiSelectedListener listener) {
    if (listener != null) {
      this.listener = listener;
    } else {
      ALog.d(LIB_TAG, "sticker", "listener is null");
    }
  }

  @Nullable
  public StickerAssetManager getStickerAssetManager() {
    return stickerAssetManager;
  }

  protected void setupEmojiView() {
    setClickable(true);
    setOnClickListener(v -> {});
    viewBinding.topDividerLine.setVisibility(View.VISIBLE);
    viewBinding.scrPlugin.setOnClickListener(v -> {});
    viewBinding.layoutScrBottom.setOnClickListener(v -> {});
    viewBinding.emojiSendTv.setOnClickListener(
        view -> {
          if (listener != null) {
            listener.onEmojiSendClick();
          }
        });
  }

  public void setTabDividerColorResource(int colorResId) {
    tabDividerColorResId = colorResId;
    for (View divider : tabDividers) {
      divider.setBackgroundResource(colorResId);
    }
  }

  public void setPageIndicatorSelectorResource(int selectorResId) {
    pageIndicatorSelectorResId = selectorResId;
    if (gifView != null) {
      gifView.setPageIndicatorSelectorResource(selectorResId);
    }
  }

  public void setSendButtonBackgroundResource(int backgroundResId) {
    viewBinding.emojiSendTv.setBackgroundResource(backgroundResId);
  }

  public void setTabSelectedBackgroundColorResource(int colorResId) {
    tabSelectedBackgroundColor = ContextCompat.getColor(context, colorResId);
    for (int i = 0; i < viewBinding.emojiTabView.getChildCount(); i++) {
      View child = viewBinding.emojiTabView.getChildAt(i);
      if (child instanceof FrameLayout && ((FrameLayout) child).getChildCount() > 0) {
        View tabButton = ((FrameLayout) child).getChildAt(0);
        if (tabButton instanceof CheckedImageButton) {
          ((CheckedImageButton) tabButton).setCheckedBackgroundColor(tabSelectedBackgroundColor);
        }
      }
    }
  }

  // add tab button
  OnClickListener tabCheckListener = v -> onEmoticonBtnChecked(v.getId());

  private void loadStickers() {
    viewBinding.emojiTabView.removeAllViews();
    tabDividers.clear();
    int index = 0;

    // emoji
    CheckedImageButton btn = addEmojiIconTabBtn(index++, tabCheckListener);
    btn.setNormalImageId(R.drawable.ic_chat_emoji_inactive);
    btn.setCheckedImageId(R.drawable.ic_chat_emoji);
    ChatUIConfig config = ChatKitClient.getChatUIConfig();
    StickerConfig stickerConfig = config == null ? null : config.stickerConfig;
    if (stickerConfig != null && stickerConfig.enabled) {
      stickerAssetManager = new StickerAssetManager(context, stickerConfig.provider);
      for (StickerPack pack : stickerAssetManager.getPacks()) {
        CheckedImageButton stickerButton = addEmojiIconTabBtn(index++, tabCheckListener);
        loadTabIcon(stickerButton, pack.packId, pack.iconNormalKey, false);
        loadTabIcon(stickerButton, pack.packId, pack.iconSelectedKey, true);
      }
    }
  }

  private void loadTabIcon(
      CheckedImageButton button, String packId, String iconKey, boolean checked) {
    if (iconKey == null || iconKey.length() == 0) return;
    Glide.with(button)
        .asBitmap()
        .load(stickerAssetManager.getStickerIconPreviewPath(packId, iconKey))
        .into(
            new CustomTarget<Bitmap>() {
              @Override
              public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                if (checked) {
                  button.setCheckedImage(resource);
                } else {
                  button.setNormalImage(resource);
                }
              }

              @Override
              public void onLoadCleared(@Nullable android.graphics.drawable.Drawable placeholder) {}
            });
  }

  private CheckedImageButton addEmojiIconTabBtn(int index, OnClickListener listener) {
    CheckedImageButton emojiBtn = new CheckedImageButton(context);
    emojiBtn.setNormalBkResId(R.drawable.bg_sticker_button_normal_layer);
    emojiBtn.setCheckedBkResId(R.drawable.bg_sticker_button_pressed_layer);
    emojiBtn.setCheckedBackgroundColor(tabSelectedBackgroundColor);
    emojiBtn.setId(index);
    emojiBtn.setOnClickListener(listener);
    emojiBtn.setScaleType(ImageView.ScaleType.FIT_CENTER);
    emojiBtn.setPaddingValue(SizeUtils.dp2px(7));

    final int emojiBtnWidth = SizeUtils.dp2px(50);
    final int emojiBtnHeight = SizeUtils.dp2px(42);

    FrameLayout tabContainer = new FrameLayout(context);
    tabContainer.setLayoutParams(new LinearLayout.LayoutParams(emojiBtnWidth, emojiBtnHeight));
    tabContainer.addView(emojiBtn, new FrameLayout.LayoutParams(emojiBtnWidth, emojiBtnHeight));
    if (index > 0) {
      View divider = new View(context);
      divider.setBackgroundResource(tabDividerColorResId);
      FrameLayout.LayoutParams dividerParams =
          new FrameLayout.LayoutParams(
              getResources().getDimensionPixelSize(R.dimen.chat_emoji_tab_divider_width),
              ViewGroup.LayoutParams.MATCH_PARENT,
              android.view.Gravity.START);
      tabContainer.addView(divider, dividerParams);
      divider.setTranslationX(
          getResources().getDimension(R.dimen.chat_emoji_tab_divider_width) / 2f);
      tabDividers.add(divider);
    }
    viewBinding.emojiTabView.addView(tabContainer);

    return emojiBtn;
  }

  private void onEmoticonBtnChecked(int index) {
    updateTabButton(index);
    updateSendButtonVisibility(index);
    if (index == 0) {
      showEmojiView();
    } else {
      showEmojiPager(index - 1);
    }
  }

  private void updateTabButton(int index) {
    for (int i = 0; i < viewBinding.emojiTabView.getChildCount(); ++i) {
      View child = viewBinding.emojiTabView.getChildAt(i);
      if (child instanceof FrameLayout) {
        child = ((FrameLayout) child).getChildAt(0);
      }

      if (child != null && child instanceof CheckedImageButton) {
        CheckedImageButton tabButton = (CheckedImageButton) child;
        if (tabButton.isChecked() && i != index) {
          tabButton.setChecked(false);
        } else if (!tabButton.isChecked() && i == index) {
          tabButton.setChecked(true);
        }
      }
    }
  }

  private void updateSendButtonVisibility(int index) {
    viewBinding.emojiSendTv.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
  }

  private void showEmojiPager(int index) {
    if (gifView == null) {
      gifView =
          new EmojiView(
              context,
              listener,
              stickerListener,
              stickerAssetManager,
              this,
              viewBinding.scrPlugin,
              viewBinding.layoutScrBottom);
      gifView.setPageIndicatorSelectorResource(pageIndicatorSelectorResId);
    }
    gifView.showStickers(index);
  }

  private void showEmojiView() {
    if (gifView == null) {
      gifView =
          new EmojiView(
              context,
              listener,
              stickerListener,
              stickerAssetManager,
              this,
              viewBinding.scrPlugin,
              viewBinding.layoutScrBottom);
      gifView.setPageIndicatorSelectorResource(pageIndicatorSelectorResId);
    }
    gifView.showEmojis();
  }

  private void show() {
    if (listener == null) {
      ALog.d(LIB_TAG, "sticker", "show picker view when listener is null");
    }
    showEmojiView();
    resetToFirstTab();
    setSelectedVisible(0);
  }

  private void resetToFirstTab() {
    onEmoticonBtnChecked(0);
  }

  private void setSelectedVisible(final int index) {
    if (selectedVisibleRunnable != null) {
      uiHandler.removeCallbacks(selectedVisibleRunnable);
    }
    final Runnable runnable =
        new Runnable() {
          @Override
          public void run() {
            View firstTab = viewBinding.emojTabViewContainer.getChildAt(0);
            if (firstTab == null || firstTab.getWidth() == 0) {
              uiHandler.postDelayed(this, 100);
              return;
            }
            int x = -1;
            View child = viewBinding.emojiTabView.getChildAt(index);
            if (child != null) {
              if (child.getRight() > viewBinding.emojTabViewContainer.getWidth()) {
                x = child.getRight() - viewBinding.emojTabViewContainer.getWidth();
              }
            }
            if (x != -1) {
              viewBinding.emojTabViewContainer.smoothScrollTo(x, 0);
            }
          }
        };
    selectedVisibleRunnable = runnable;
    uiHandler.postDelayed(runnable, 100);
  }

  @Override
  public void onCategoryChanged(int index) {
    updateSendButtonVisibility(index);
    if (categoryIndex == index) {
      return;
    }
    categoryIndex = index;
    updateTabButton(index);
  }
}
