// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.local.ui.normal.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.conversationkit.local.ui.R;
import com.netease.yunxin.kit.conversationkit.local.ui.databinding.LocalConversationActivityBinding;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;

/** 普通版会话页面Activity，可以直接使用 跳转链接：{@link RouterConstant#PATH_CONVERSATION_PAGE} */
public class LocalConversationActivity extends BaseLocalActivity {

  private LocalConversationActivityBinding viewBinding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    viewBinding = LocalConversationActivityBinding.inflate(LayoutInflater.from(this));
    setContentView(viewBinding.getRoot());
    FragmentManager fragmentManager = getSupportFragmentManager();
    LocalConversationFragment fragment = new LocalConversationFragment();
    fragmentManager
        .beginTransaction()
        .add(R.id.conversation_container, fragment)
        .commitAllowingStateLoss();
  }
}
