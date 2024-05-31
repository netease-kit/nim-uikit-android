// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.selector.forward;

import com.netease.yunxin.kit.contactkit.ui.model.SelectableBean;

/** 选择器监听 */
public interface SelectableListener<T> {

  /**
   * 选择状态变化
   *
   * @param selectableBean 选择的数据
   * @param selected 是否选中
   */
  void onSelected(SelectableBean<T> selectableBean, boolean selected);
}
