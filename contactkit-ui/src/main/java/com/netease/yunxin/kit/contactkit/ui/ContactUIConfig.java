// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui;

import android.util.SparseArray;
import android.view.View;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactClickListener;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactSelectorListener;
import com.netease.yunxin.kit.contactkit.ui.model.ContactEntranceBean;
import com.netease.yunxin.kit.contactkit.ui.view.ContactListViewAttrs;
import java.util.List;

public class ContactUIConfig {

  public static int INT_DEFAULT_NULL = -1;

  //转发默认最大选择数量
  public static final int DEFAULT_SESSION_MAX_SELECT_COUNT = 9;

  // 人员选择器默认最大选择数量（创建群）
  public static final int DEFAULT_SELECTOR_MAX_SELECT_COUNT = 199;

  //title config
  public String title;
  public Integer titleColor;
  public boolean showTitleBar = true;
  public boolean showTitleBarRight2Icon = true;
  public Integer titleBarRight2Res;
  public View.OnClickListener titleBarRight2Click;
  public boolean showTitleBarRightIcon = true;
  public Integer titleBarRightRes;
  public View.OnClickListener titleBarRightClick;
  //page
  public boolean showHeader = true;
  public List<ContactEntranceBean> headerData;
  public ContactListViewAttrs contactAttrs;

  public SparseArray<IContactClickListener> itemClickListeners = new SparseArray<>();
  public SparseArray<IContactSelectorListener> itemSelectorListeners = new SparseArray<>();

  public IContactFactory viewHolderFactory;

  public IContactViewLayout customLayout;
}
