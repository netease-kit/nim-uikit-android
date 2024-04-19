// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.blacklist;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.blacklist.BaseBlackListActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.selector.FunContactSelectorActivity;

/**
 * 娱乐版黑名单页面
 *
 * <p>
 */
public class FunBlackListActivity extends BaseBlackListActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_ededed);
  }

  // 配置差异化UI

  @Override
  protected void initView() {
    super.initView();
    binding
        .title
        .setTitle(R.string.black_list)
        .setActionImg(R.mipmap.ic_title_bar_more)
        .setActionListener(
            v -> {
              Intent intent = new Intent(this, FunContactSelectorActivity.class);
              blackListLauncher.launch(intent);
            });
    binding.title.getTitleTextView().setTextSize(17);
    binding.title.getTitleTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
    binding.title.setBackgroundResource(R.color.color_ededed);
  }
}
