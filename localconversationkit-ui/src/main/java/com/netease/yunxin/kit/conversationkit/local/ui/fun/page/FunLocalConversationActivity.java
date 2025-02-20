// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.local.ui.fun.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.conversationkit.local.ui.R;
import com.netease.yunxin.kit.conversationkit.local.ui.databinding.FunLocalConversationActivityBinding;

/** 娱乐版会话列表Activity, 用于展示娱乐版会话列表 可以作为独立页面使用 */
public class FunLocalConversationActivity extends BaseLocalActivity {

  private FunLocalConversationActivityBinding viewBinding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.fun_conversation_page_bg_color);
    viewBinding = FunLocalConversationActivityBinding.inflate(LayoutInflater.from(this));
    setContentView(viewBinding.getRoot());
    FragmentManager fragmentManager = getSupportFragmentManager();
    FunLocalConversationFragment fragment = new FunLocalConversationFragment();
    fragmentManager
        .beginTransaction()
        .add(R.id.conversation_container, fragment)
        .commitAllowingStateLoss();
  }
}
