// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.indexbar.helper;

import com.netease.yunxin.kit.contactkit.ui.indexbar.bean.IndexPinyinBean;
import java.util.List;

/**
 * helper for indexBar and data 1 translate Chinese characters to pinyin 2 fill indexTag 3 sort
 * source data 4 source data after sort->indexBar source data
 */
public interface IIndexBarDataHelper {
  //translate Chinese characters to pinyin
  IIndexBarDataHelper convert(List<? extends IndexPinyinBean> data);

  //pinyin->tag
  IIndexBarDataHelper fillIndexTag(List<? extends IndexPinyinBean> data);

  //sort source data
  IIndexBarDataHelper sortSourceData(List<? extends IndexPinyinBean> data);

  //sort the data for indexBar,call after sortSourceData
  IIndexBarDataHelper getSortedIndexData(
      List<? extends IndexPinyinBean> sourceData, List<String> data);
}
