// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.fun.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.databinding.FunConversationActivityBinding;

public class FunConversationActivity extends BaseActivity {

  private FunConversationActivityBinding viewBinding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.fun_conversation_page_bg_color);
    viewBinding = FunConversationActivityBinding.inflate(LayoutInflater.from(this));
    setContentView(viewBinding.getRoot());
    FragmentManager fragmentManager = getSupportFragmentManager();
    FunConversationFragment fragment = new FunConversationFragment();
    fragmentManager
        .beginTransaction()
        .add(R.id.conversation_container, fragment)
        .commitAllowingStateLoss();
  }
}
