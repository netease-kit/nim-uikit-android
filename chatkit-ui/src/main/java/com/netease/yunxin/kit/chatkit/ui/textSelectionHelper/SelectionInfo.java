// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.textSelectionHelper;

import android.widget.TextView;

/** 选择文本信息 */
public class SelectionInfo {
  // 选择文本的开始位置
  private int mStart;
  // 选择文本的结束位置
  private int mEnd;
  // 选择文本的内容
  public String mSelectionContent;

  /**
   * 获取开始位置
   *
   * @param textView TextView
   * @return 开始位置
   */
  public int getStart(TextView textView) {
    if (textView == null) {
      return 0;
    }
    if (mStart > textView.length()) {
      return textView.length();
    }
    if (mStart < 0) {
      return 0;
    }
    return mStart;
  }

  /**
   * 获取开始位置
   *
   * @param charSequence CharSequence
   * @return 开始位置
   */
  public int getStart(CharSequence charSequence) {
    if (charSequence == null) {
      return 0;
    }
    if (mStart > charSequence.length()) {
      return charSequence.length();
    }
    if (mStart < 0) {
      return 0;
    }
    return mStart;
  }

  /**
   * 设置开始位置
   *
   * @param start 开始位置
   */
  public void setStart(int start) {
    this.mStart = start;
  }

  /**
   * 获取结束位置
   *
   * @param textView TextView
   * @return 结束位置
   */
  public int getEnd(TextView textView) {
    if (textView == null) {
      return 0;
    }
    if (mEnd > textView.length()) {
      return textView.length();
    }
    if (mEnd < 0) {
      return 0;
    }
    return mEnd;
  }

  /**
   * 获取结束位置
   *
   * @param charSequence CharSequence
   * @return 结束位置
   */
  public int getEnd(CharSequence charSequence) {
    if (charSequence == null) {
      return 0;
    }
    if (mEnd > charSequence.length()) {
      return charSequence.length();
    }
    if (mEnd < 0) {
      return 0;
    }
    return mEnd;
  }

  /**
   * 设置结束位置
   *
   * @param end 结束位置
   */
  public void setEnd(int end) {
    this.mEnd = end;
  }
}
