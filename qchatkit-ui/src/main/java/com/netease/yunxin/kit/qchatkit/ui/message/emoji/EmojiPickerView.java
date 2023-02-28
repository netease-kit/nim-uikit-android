// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.emoji;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.utils.ImageUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatEmojiLayoutBinding;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/** emoji picker view */
public class EmojiPickerView extends LinearLayout implements IEmojiCategoryChanged {

  private Context context;
  private QChatEmojiLayoutBinding viewBinding;

  private IEmojiSelectedListener listener;

  private boolean loaded = false;

  private boolean withSticker;

  private EmojiView gifView;

  private int categoryIndex;

  private Handler uiHandler;

  public EmojiPickerView(Context context) {
    super(context);
    init(context);
  }

  public EmojiPickerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  public EmojiPickerView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context);
  }

  private void init(Context context) {
    this.context = context;
    this.uiHandler = new Handler(context.getMainLooper());
    viewBinding = QChatEmojiLayoutBinding.inflate(LayoutInflater.from(context), this, true);
    LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.q_chat_emoji_layout, this);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    setupEmojiView();
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
  }

  public void show(IEmojiSelectedListener listener) {
    setListener(listener);
    if (loaded) return;
    loadStickers();
    loaded = true;
    show();
  }

  public void setListener(IEmojiSelectedListener listener) {
    if (listener != null) {
      this.listener = listener;
    } else {
      ALog.i("sticker", "listener is null");
    }
  }

  protected void setupEmojiView() {
    viewBinding.topDividerLine.setVisibility(View.VISIBLE);
    viewBinding.emojiSendTv.setOnClickListener(
        view -> {
          if (listener != null) {
            listener.onEmojiSendClick();
          }
        });
  }

  // add tab button
  OnClickListener tabCheckListener = v -> onEmoticonBtnChecked(v.getId());

  private void loadStickers() {

    final StickerManager manager = StickerManager.getInstance();

    viewBinding.emojiTabView.removeAllViews();

    int index = 0;

    // emoji
    CheckedImageButton btn = addEmojiIconTabBtn(index++, tabCheckListener);
    btn.setNormalImageId(R.drawable.ic_qchat_emoji_inactive);
    btn.setCheckedImageId(R.drawable.ic_qchat_emoji);

    //sticker
    if (withSticker) {
      List<StickerCategory> categories = manager.getCategories();
      for (StickerCategory category : categories) {
        btn = addEmojiIconTabBtn(index++, tabCheckListener);
        setCheckedButtonImage(btn, category);
      }
    }
  }

  private CheckedImageButton addEmojiIconTabBtn(int index, OnClickListener listener) {
    CheckedImageButton emojiBtn = new CheckedImageButton(context);
    emojiBtn.setNormalBkResId(R.drawable.bg_sticker_button_normal_layer);
    emojiBtn.setCheckedBkResId(R.drawable.bg_sticker_button_pressed_layer);
    emojiBtn.setId(index);
    emojiBtn.setOnClickListener(listener);
    emojiBtn.setScaleType(ImageView.ScaleType.FIT_CENTER);
    emojiBtn.setPaddingValue(SizeUtils.dp2px(7));

    final int emojiBtnWidth = SizeUtils.dp2px(50);
    final int emojiBtnHeight = SizeUtils.dp2px(42);

    viewBinding.emojiTabView.addView(emojiBtn);

    ViewGroup.LayoutParams emojiBtnLayoutParams = emojiBtn.getLayoutParams();
    emojiBtnLayoutParams.width = emojiBtnWidth;
    emojiBtnLayoutParams.height = emojiBtnHeight;
    emojiBtn.setLayoutParams(emojiBtnLayoutParams);

    return emojiBtn;
  }

  private void setCheckedButtonImage(CheckedImageButton btn, StickerCategory category) {
    try {
      InputStream is = category.getCoverNormalInputStream(context);
      if (is != null) {
        Bitmap bmp = ImageUtils.getBitmap(is);
        btn.setNormalImage(bmp);
        is.close();
      }
      is = category.getCoverPressedInputStream(context);
      if (is != null) {
        Bitmap bmp = ImageUtils.getBitmap(is);
        btn.setCheckedImage(bmp);
        is.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void onEmoticonBtnChecked(int index) {
    updateTabButton(index);
    showEmojiPager(index);
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

  private void showEmojiPager(int index) {
    if (gifView == null) {
      gifView =
          new EmojiView(context, listener, viewBinding.scrPlugin, viewBinding.layoutScrBottom);
      gifView.setCategoryChangCheckedCallback(this);
    }

    gifView.showStickers(index);
  }

  private void showEmojiView() {
    if (gifView == null) {
      gifView =
          new EmojiView(context, listener, viewBinding.scrPlugin, viewBinding.layoutScrBottom);
    }
    gifView.showEmojis();
  }

  private void show() {
    if (listener == null) {
      ALog.i("sticker", "show picker view when listener is null");
    }
    //        if (!withSticker) {
    //            showEmojiView();
    //        } else {
    onEmoticonBtnChecked(0);
    setSelectedVisible(0);
    //        }
  }

  private void setSelectedVisible(final int index) {
    final Runnable runnable =
        new Runnable() {
          @Override
          public void run() {
            if (viewBinding.emojTabViewContainer.getChildAt(0).getWidth() == 0) {
              uiHandler.postDelayed(this, 100);
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
    uiHandler.postDelayed(runnable, 100);
  }

  @Override
  public void onCategoryChanged(int index) {
    if (categoryIndex == index) {
      return;
    }

    categoryIndex = index;
    updateTabButton(index);
  }

  public void setWithSticker(boolean withSticker) {
    this.withSticker = withSticker;
  }
}
