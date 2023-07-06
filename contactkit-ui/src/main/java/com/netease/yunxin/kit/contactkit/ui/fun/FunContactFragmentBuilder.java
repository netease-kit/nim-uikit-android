// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun;

import android.view.View;
import com.netease.yunxin.kit.contactkit.ui.ContactUIConfig;
import com.netease.yunxin.kit.contactkit.ui.FragmentBuilder;
import com.netease.yunxin.kit.contactkit.ui.fun.contact.FunContactFragment;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactClickListener;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactSelectorListener;
import com.netease.yunxin.kit.contactkit.ui.model.ContactEntranceBean;
import com.netease.yunxin.kit.contactkit.ui.view.ContactListViewAttrs;
import java.util.List;

public class FunContactFragmentBuilder extends FragmentBuilder {

  ContactUIConfig contactConfig;

  public FunContactFragmentBuilder() {}

  @Override
  public FunContactFragment build() {
    FunContactFragment fragment = new FunContactFragment();
    if (contactConfig != null) {
      fragment.setContactConfig(contactConfig);
    }
    return fragment;
  }

  public void setContactConfig(ContactUIConfig contactConfig) {
    this.contactConfig = contactConfig;
  }

  public FunContactFragmentBuilder setShowTitleBar(boolean show) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.showTitleBar = show;
    return this;
  }

  public FunContactFragmentBuilder setTitle(String title) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.title = title;
    return this;
  }

  public FunContactFragmentBuilder setTitleColor(int color) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.titleColor = color;
    return this;
  }

  public FunContactFragmentBuilder setShowTitleBarRight2Icon(boolean show) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.showTitleBarRight2Icon = show;
    return this;
  }

  public FunContactFragmentBuilder setSearchIcon(int res) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.titleBarRight2Res = res;
    return this;
  }

  public FunContactFragmentBuilder setSearchClickListener(View.OnClickListener listener) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.titleBarRight2Click = listener;
    return this;
  }

  public FunContactFragmentBuilder setShowMoreIcon(boolean show) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.showTitleBarRightIcon = show;
    return this;
  }

  public FunContactFragmentBuilder setMoreIcon(int res) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.titleBarRightRes = res;
    return this;
  }

  public FunContactFragmentBuilder setMoreClickListener(View.OnClickListener listener) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.titleBarRightClick = listener;
    return this;
  }

  public FunContactFragmentBuilder showHeader(boolean show) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.showHeader = show;
    return this;
  }

  public FunContactFragmentBuilder setContactListViewAttar(ContactListViewAttrs attrs) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.contactAttrs = attrs;
    return this;
  }

  public FunContactFragmentBuilder setHeaderData(List<ContactEntranceBean> data) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.headerData = data;
    return this;
  }

  public FunContactFragmentBuilder setContactClickListener(
      int type, IContactClickListener contactListener) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.itemClickListeners.put(type, contactListener);
    return this;
  }

  public FunContactFragmentBuilder setContactSelection(
      int type, IContactSelectorListener contactListener) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.itemSelectorListeners.put(type, contactListener);
    return this;
  }
}
