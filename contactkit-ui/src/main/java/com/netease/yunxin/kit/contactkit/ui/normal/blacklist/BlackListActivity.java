// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.blacklist;

import android.content.Intent;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.blacklist.BaseBlackListActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.selector.ContactSelectorActivity;

/**
 * 普通版黑名单页面
 *
 * <p>
 */
public class BlackListActivity extends BaseBlackListActivity {

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
              Intent intent = new Intent(this, ContactSelectorActivity.class);
              blackListLauncher.launch(intent);
            });
  }
}
