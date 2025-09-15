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

  /** 视图绑定对象，用于访问布局中的UI元素 */
  private FunLocalConversationActivityBinding viewBinding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // 设置状态栏颜色为会话页面背景色
    changeStatusBarColor(R.color.fun_conversation_page_bg_color);
    // 使用视图绑定初始化布局
    viewBinding = FunLocalConversationActivityBinding.inflate(LayoutInflater.from(this));
    // 将绑定布局的根视图设置为Activity的内容视图
    setContentView(viewBinding.getRoot());

    // 获取Fragment管理器
    FragmentManager fragmentManager = getSupportFragmentManager();
    // 创建娱乐版会话列表Fragment实例
    FunLocalConversationFragment fragment = new FunLocalConversationFragment();
    // 开启Fragment事务，将会话列表Fragment添加到容器中
    fragmentManager
        .beginTransaction()
        .add(R.id.conversation_container, fragment)
        // 提交事务，允许状态丢失（适用于Activity可能被销毁的场景）
        .commitAllowingStateLoss();
  }
}
