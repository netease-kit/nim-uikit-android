// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.ai;

import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.ai.BaseAIUserListActivity;

/**
 * 协同版AI数字人页面
 *
 * <p>
 */
public class AIUserListActivity extends BaseAIUserListActivity {

  // 配置差异化UI
  @Override
  protected void initView() {
    super.initView();
    binding.title.setTitle(R.string.contact_ai_user_title);
    binding.emptyTv.setText(R.string.contact_ai_user_empty_tips);
    binding.emptyIv.setImageResource(R.drawable.ic_contact_empty);
  }
}
