// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.model;

import com.netease.yunxin.kit.contactkit.ui.indexbar.bean.IndexPinyinBean;

public abstract class BaseContactBean extends IndexPinyinBean {

  /** viewType {@link IViewTypeConstant} for data */
  public int viewType;

  /** router for item click */
  public String router;

  /** start as 0, weight bigger means item will show higher 0 for Friends Data */
  public int weight;

  public String getAccountId() {
    return "";
  };

  public interface ContactBeanWeight {
    /** base weight,for friend item */
    int BASE_WEIGHT = 0;

    /** weight for entrance */
    int ENTRANCE_WEIGHT = 50;
  }
}
