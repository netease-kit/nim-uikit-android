// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.indexbar.bean;

/** index pinyin bean */
public abstract class IndexPinyinBean extends IndexBean {

  private String indexPinyin;

  public String getIndexPinyin() {
    return indexPinyin;
  }

  public IndexPinyinBean setIndexPinyin(String indexPinyin) {
    this.indexPinyin = indexPinyin;
    return this;
  }

  //is need to translate as pinyin
  public boolean isNeedToPinyin() {
    return false;
  }

  //get target String
  public abstract String getTarget();

  @Override
  public boolean isSuspension() {
    return false;
  }
}
