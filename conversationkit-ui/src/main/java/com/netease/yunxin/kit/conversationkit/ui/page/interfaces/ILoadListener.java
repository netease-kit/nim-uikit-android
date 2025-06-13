// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.page.interfaces;

/** 加载更多接口 */
public interface ILoadListener {

  boolean hasMore();

  void loadMore(Object last);

  void onScrollStateIdle(int first, int end);
}
