// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.viewholder;

public abstract class BaseBean {

  // 会话展示的View类型
  public int viewType;

  // 会话跳转的路由地址
  public String router;

  // 跳转需要传递的参数KEY
  public String paramKey;
  // 跳转需要传递的参数Value
  public Object param;
}
