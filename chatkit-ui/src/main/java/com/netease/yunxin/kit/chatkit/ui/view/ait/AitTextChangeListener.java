// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.ait;

public interface AitTextChangeListener {

  /** called when @ a team member */
  void onTextAdd(String content, int start, int length);

  /** called when delete a @ member */
  void onTextDelete(int start, int length);
}
