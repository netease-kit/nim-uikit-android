// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.interfaces;

import androidx.annotation.ColorInt;
import com.netease.yunxin.kit.contactkit.ui.view.ContactListView;

/** attrs for {@link ContactListView} */
public interface IContactViewAttrs {

  /** set textColor for Name */
  void setTitleColor(@ColorInt int color);

  /**
   * show indexBar or not
   *
   * @param show show if true
   */
  void showIndexBar(boolean show);

  /**
   * show selector
   *
   * @param show show selector if true
   */
  void showSelector(boolean show);
}
