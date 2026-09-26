// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.model.MessageReactionState;

/** Displays one message Reaction item. It does not own Reaction business state. */
public class ReactionItemView extends LinearLayout {

  public ReactionItemView(@NonNull Context context) {
    super(context);
    setOrientation(HORIZONTAL);
    setGravity(Gravity.START | Gravity.BOTTOM);
  }

  public void bind(
      @NonNull MessageReactionState.ReactionSummary summary,
      @NonNull Drawable drawable,
      float visualScale,
      @NonNull ReactionGroupView.ReactionStyle style,
      @Nullable ReactionGroupView.OnReactionClickListener listener) {
    removeAllViews();
    setPadding(
        style.itemHorizontalPadding,
        style.itemVerticalPadding,
        style.itemHorizontalPadding,
        style.itemVerticalPadding);

    setBackground(style.createItemBackground(getContext(), summary.hasSelf()));

    ImageView imageView = new ImageView(getContext());
    imageView.setImageDrawable(drawable);
    imageView.setScaleX(visualScale);
    imageView.setScaleY(visualScale);
    addView(imageView, new LinearLayout.LayoutParams(style.emojiSize, style.emojiSize));

    TextView countView = new TextView(getContext());
    countView.setText(String.valueOf(summary.getCount()));
    countView.setTextSize(style.countTextSizeSp);
    countView.setTextColor(
        summary.hasSelf()
            ? style.highlightTextColor
            : ContextCompat.getColor(getContext(), R.color.color_666666));
    countView.setGravity(Gravity.BOTTOM);
    countView.setIncludeFontPadding(false);
    LinearLayout.LayoutParams countParams =
        new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    countParams.setMarginStart(style.countMarginStart);
    addView(countView, countParams);

    if (listener != null) {
      setOnClickListener(v -> listener.onReactionClick(v, summary.getIndex(), summary.hasSelf()));
    } else {
      setOnClickListener(null);
    }
  }
}
