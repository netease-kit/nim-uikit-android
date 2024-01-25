// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.page;

import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatReaderActivityBinding;
import com.netease.yunxin.kit.chatkit.ui.fun.page.fragment.FunChatReaderFragment;
import com.netease.yunxin.kit.chatkit.ui.page.ChatReaderBaseActivity;
import com.netease.yunxin.kit.chatkit.ui.page.fragment.ChatReaderBaseFragment;

/** Fun皮肤消息已读未读页面，包含已读和未读两个tab Fun皮肤差异化的部分，通用能力集中到ChatReaderBaseActivity中 */
public class FunChatReaderActivity extends ChatReaderBaseActivity {

  FunChatReaderActivityBinding binding;

  @Override
  public ChatReaderBaseFragment getReadFragment() {
    return new FunChatReaderFragment();
  }

  @Override
  public ChatReaderBaseFragment getUnreadFragment() {
    return new FunChatReaderFragment();
  }

  @Override
  public void initViewAndSetContentView(@Nullable Bundle savedInstanceState) {
    changeStatusBarColor(R.color.fun_chat_secondary_page_bg_color);
    binding = FunChatReaderActivityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    titleBarView = binding.titleBar;
    tabLayout = binding.tabLayout;
    fragmentViewPager = binding.viewPager;

    binding.titleBar.getTitleTextView().setTypeface(Typeface.DEFAULT_BOLD);
  }
}
