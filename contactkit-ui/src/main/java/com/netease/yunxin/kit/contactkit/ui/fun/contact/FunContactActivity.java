// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.contact;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import com.netease.yunxin.kit.contactkit.ui.contact.BaseContactActivity;
import com.netease.yunxin.kit.contactkit.ui.contact.BaseContactFragment;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunContactActivityBinding;

public class FunContactActivity extends BaseContactActivity {

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    FunContactActivityBinding viewBinding =
        FunContactActivityBinding.inflate(LayoutInflater.from(this));
    return viewBinding.getRoot();
  }

  protected BaseContactFragment getContactFragment() {
    return new FunContactFragment();
  }
}
