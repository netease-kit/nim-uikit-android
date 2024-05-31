// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.normal.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.databinding.ConversationActivityBinding;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;

/** 普通版会话页面Activity，可以直接使用 跳转链接：{@link RouterConstant#PATH_CONVERSATION_PAGE} */
public class ConversationActivity extends BaseActivity {

  private ConversationActivityBinding viewBinding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    viewBinding = ConversationActivityBinding.inflate(LayoutInflater.from(this));
    setContentView(viewBinding.getRoot());
    FragmentManager fragmentManager = getSupportFragmentManager();
    ConversationFragment fragment = new ConversationFragment();
    fragmentManager
        .beginTransaction()
        .add(R.id.conversation_container, fragment)
        .commitAllowingStateLoss();
  }
}
