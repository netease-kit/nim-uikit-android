// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.textSelectionHelper;

import android.content.Context;
import android.graphics.Rect;
import android.text.Layout;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/** 文本布局工具类 */
public class TextLayoutUtils {
  public static int getScreenWidth(Context context) {
    return context.getResources().getDisplayMetrics().widthPixels;
  }

  public static int getPreciseOffset(TextView textView, int x, int y) {
    Layout layout = textView.getLayout();
    if (layout != null) {
      int topVisibleLine = layout.getLineForVertical(y);
      int offset = layout.getOffsetForHorizontal(topVisibleLine, x);

      int offsetX = (int) layout.getPrimaryHorizontal(offset);

      if (offsetX > x) {
        return layout.getOffsetToLeftOf(offset);
      } else {
        return offset;
      }
    } else {
      return -1;
    }
  }

  public static int getHysteresisOffset(TextView textView, int x, int y, int previousOffset) {
    final Layout layout = textView.getLayout();
    if (layout == null) return -1;

    int line = layout.getLineForVertical(y);

    // The "HACK BLOCK"S in this function is required because of how Android Layout for
    // TextView works - if 'offset' equals to the last character of a line, then
    //
    // * getLineForOffset(offset) will result the NEXT line
    // * getPrimaryHorizontal(offset) will return 0 because the next insertion point is on the next line
    // * getOffsetForHorizontal(line, x) will not return the last offset of a line no matter where x is
    // These are highly undesired and is worked around with the HACK BLOCK
    //
    // @see Moon+ Reader/Color Note - see how it can't select the last character of a line unless you move
    // the cursor to the beginning of the next line.
    //
    ////////////////////HACK BLOCK////////////////////////////////////////////////////

    if (isEndOfLineOffset(layout, previousOffset)) {
      // we have to minus one from the offset so that the code below to find
      // the previous line can work correctly.
      int left = (int) layout.getPrimaryHorizontal(previousOffset - 1);
      int right = (int) layout.getLineRight(line);
      int threshold = (right - left) / 2; // half the width of the last character
      if (x > right - threshold) {
        previousOffset -= 1;
      }
    }
    ///////////////////////////////////////////////////////////////////////////////////

    final int previousLine = layout.getLineForOffset(previousOffset);
    final int previousLineTop = layout.getLineTop(previousLine);
    final int previousLineBottom = layout.getLineBottom(previousLine);
    final int hysteresisThreshold = (previousLineBottom - previousLineTop) / 2;

    // If new line is just before or after previous line and y position is less than
    // hysteresisThreshold away from previous line, keep cursor on previous line.
    if (((line == previousLine + 1) && ((y - previousLineBottom) < hysteresisThreshold))
        || ((line == previousLine - 1) && ((previousLineTop - y) < hysteresisThreshold))) {
      line = previousLine;
    }

    int offset = layout.getOffsetForHorizontal(line, x);

    // This allow the user to select the last character of a line without moving the
    // cursor to the next line. (As Layout.getOffsetForHorizontal does not return the
    // offset of the last character of the specified line)
    //
    // But this function will probably get called again immediately, must decrement the offset
    // by 1 to compensate for the change made below. (see previous HACK BLOCK)
    /////////////////////HACK BLOCK///////////////////////////////////////////////////
    if (offset < textView.getText().length() - 1) {
      if (isEndOfLineOffset(layout, offset + 1)) {
        int left = (int) layout.getPrimaryHorizontal(offset);
        int right = (int) layout.getLineRight(line);
        int threshold = (right - left) / 2; // half the width of the last character
        if (x > right - threshold) {
          offset += 1;
        }
      }
    }
    //////////////////////////////////////////////////////////////////////////////////

    if (offset > textView.getText().length()) {
      offset = textView.getText().length();
    }

    return offset;
  }

  private static boolean isEndOfLineOffset(Layout layout, int offset) {
    return offset > 0 && layout.getLineForOffset(offset) == layout.getLineForOffset(offset - 1) + 1;
  }

  /** 判断触摸的点是否在View范围内 */
  public static boolean isInView(View view, MotionEvent event) {
    int[] location = {0, 0};
    view.getLocationInWindow(location);
    int left = location[0],
        top = location[1],
        bottom = top + view.getHeight(),
        right = left + view.getWidth();
    float eventX = event.getX();
    float eventY = event.getY();
    Rect rect = new Rect(left, top, right, bottom);
    return rect.contains((int) eventX, (int) eventY);
  }
}
