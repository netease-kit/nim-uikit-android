// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;

/** Keeps the scroll-to-bottom tip at a fixed position within the message area. */
public final class MessageTipsLayoutHelper {

  private MessageTipsLayoutHelper() {}

  public static void bindToMessageArea(
      @NonNull View messageTipsView, @NonNull View chatView, @NonNull View messageAreaView) {
    messageAreaView.addOnLayoutChangeListener(
        new View.OnLayoutChangeListener() {
          private int messageAreaBottomGap = -1;

          @Override
          public void onLayoutChange(
              View view,
              int left,
              int top,
              int right,
              int bottom,
              int oldLeft,
              int oldTop,
              int oldRight,
              int oldBottom) {
            if (chatView.getHeight() <= 0) {
              return;
            }
            updateBottomMargin(messageTipsView, chatView, view);
          }

          private void updateBottomMargin(View tipsView, View wholeChatView, View messageArea) {
            FrameLayout.LayoutParams layoutParams =
                (FrameLayout.LayoutParams) tipsView.getLayoutParams();
            if (messageAreaBottomGap < 0) {
              int tipsBottom = wholeChatView.getHeight() - layoutParams.bottomMargin;
              messageAreaBottomGap = messageArea.getBottom() - tipsBottom;
            }
            int bottomMargin =
                wholeChatView.getHeight() - messageArea.getBottom() + messageAreaBottomGap;
            if (layoutParams.bottomMargin != bottomMargin) {
              layoutParams.bottomMargin = bottomMargin;
              tipsView.setLayoutParams(layoutParams);
            }
          }
        });
  }
}
