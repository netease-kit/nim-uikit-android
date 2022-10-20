// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui;

import android.view.View;
import com.netease.yunxin.kit.contactkit.ui.contact.ContactFragment;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactClickListener;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactSelectorListener;
import com.netease.yunxin.kit.contactkit.ui.model.ContactEntranceBean;
import com.netease.yunxin.kit.contactkit.ui.model.MenuBean;
import com.netease.yunxin.kit.contactkit.ui.view.ContactListViewAttrs;
import java.util.List;

public class ContactFragmentBuilder extends FragmentBuilder {

  ContactUIConfig contactConfig;

  public ContactFragmentBuilder() {}

  @Override
  public ContactFragment build() {
    ContactFragment fragment = new ContactFragment();
    if (contactConfig != null) {
      fragment.setContactConfig(contactConfig);
    }
    return fragment;
  }

  public void setContactConfig(ContactUIConfig contactConfig) {
    this.contactConfig = contactConfig;
  }

  public ContactFragmentBuilder setShowTitleBar(boolean show) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.showTitleBar = show;
    return this;
  }

  public ContactFragmentBuilder setTitle(String title) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.title = title;
    return this;
  }

  public ContactFragmentBuilder setTitleColor(int color) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.titleColor = color;
    return this;
  }

  public ContactFragmentBuilder setShowTitleBarRight2Icon(boolean show) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.showTitleBarRight2Icon = show;
    return this;
  }

  public ContactFragmentBuilder setSearchIcon(int res) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.titleBarRight2Res = res;
    return this;
  }

  public ContactFragmentBuilder setSearchClickListener(View.OnClickListener listener) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.titleBarRight2Click = listener;
    return this;
  }

  public ContactFragmentBuilder setShowMoreIcon(boolean show) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.showTitleBarRightIcon = show;
    return this;
  }

  public ContactFragmentBuilder setMoreIcon(int res) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.titleBarRightRes = res;
    return this;
  }

  public ContactFragmentBuilder setMoreClickListener(View.OnClickListener listener) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.titleBarRightClick = listener;
    return this;
  }

  public ContactFragmentBuilder setMoreMenu(List<MenuBean> menuList) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.titleBarRightMenu = menuList;
    return this;
  }

  public ContactFragmentBuilder showHeader(boolean show) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.showHeader = show;
    return this;
  }

  public ContactFragmentBuilder setContactListViewAttar(ContactListViewAttrs attrs) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.contactAttrs = attrs;
    return this;
  }

  public ContactFragmentBuilder setHeaderData(List<ContactEntranceBean> data) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.headerData = data;
    return this;
  }

  public ContactFragmentBuilder setContactClickListener(
      int type, IContactClickListener contactListener) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.itemClickListeners.put(type, contactListener);
    return this;
  }

  public ContactFragmentBuilder setContactSelection(
      int type, IContactSelectorListener contactListener) {
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    contactConfig.itemSelectorListeners.put(type, contactListener);
    return this;
  }
}
