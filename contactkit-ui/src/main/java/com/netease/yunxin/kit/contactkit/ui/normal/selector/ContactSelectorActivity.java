// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.selector;

import android.os.Bundle;
import android.view.View;
import androidx.viewbinding.ViewBinding;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.ContactSelectorActivityLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.normal.contact.ContactDefaultFactory;
import com.netease.yunxin.kit.contactkit.ui.selector.BaseContactSelectorActivity;
import com.netease.yunxin.kit.contactkit.ui.selector.BaseSelectedListAdapter;

public class ContactSelectorActivity extends BaseContactSelectorActivity {

  protected ContactSelectorActivityLayoutBinding binding;

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    binding = ContactSelectorActivityLayoutBinding.inflate(getLayoutInflater());
    contactListView = binding.contactListView;
    contactListView
        .getDecoration()
        .setTitleAlignBottom(false)
        .setIndexDecorationBg(getResources().getColor(R.color.color_eff1f4))
        .setColorTitleBottomLine(getResources().getColor(R.color.color_dbe0e8));
    contactListView.setViewHolderFactory(new ContactDefaultFactory());
    emptyGroup = binding.emptyLayout;
    rvSelected = binding.rvSelected;
    titleBar = binding.title;
    return binding.getRoot();
  }

  protected BaseSelectedListAdapter<? extends ViewBinding> getSelectedListAdapter() {
    return new SelectedListAdapter();
  }
}
