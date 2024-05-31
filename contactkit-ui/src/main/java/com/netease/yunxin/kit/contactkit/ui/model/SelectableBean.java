// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.model;

import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.search.model.RecordHitInfo;
import com.netease.yunxin.kit.chatkit.model.HitType;

/**
 * 可选择的数据
 *
 * @param <T>
 */
public class SelectableBean<T> {

  public T data;
  // 是否选中
  public boolean isSelected;

  //搜索结果相关
  public HitType hitType = HitType.None;
  public RecordHitInfo recordHitInfo;

  //成员数，用于展示
  public int memberCount;

  public SelectableBean(T data) {
    this.data = data;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj instanceof SelectableBean) {
      return data.equals(((SelectableBean) obj).data);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return data.hashCode();
  }
}
