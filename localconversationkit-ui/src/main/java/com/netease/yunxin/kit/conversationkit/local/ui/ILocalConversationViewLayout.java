// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.local.ui;

import com.netease.yunxin.kit.conversationkit.local.ui.page.LocalConversationBaseFragment;

/** 会话界面布局定制接口，页面加载触发 */
public interface ILocalConversationViewLayout {
  //页面加载时，回调该方法，重写该方法可以拿到加载的Fragment，进行自定义布局
  void customizeConversationLayout(final LocalConversationBaseFragment fragment);
}
