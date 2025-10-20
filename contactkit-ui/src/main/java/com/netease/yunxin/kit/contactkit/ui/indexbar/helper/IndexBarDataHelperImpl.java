// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.indexbar.helper;

import android.text.TextUtils;
import com.github.promeg.pinyinhelper.Pinyin;
import com.netease.yunxin.kit.contactkit.ui.indexbar.bean.IndexPinyinBean;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class IndexBarDataHelperImpl implements IIndexBarDataHelper {
  /** if need Chinese Character -> Pinyin */
  @Override
  public IIndexBarDataHelper convert(List<? extends IndexPinyinBean> data) {
    if (null == data || data.isEmpty()) {
      return this;
    }
    int size = data.size();
    for (int i = 0; i < size; i++) {
      IndexPinyinBean indexPinyinBean = data.get(i);
      StringBuilder pySb = new StringBuilder();
      if (indexPinyinBean.isNeedToPinyin()) {
        String target = indexPinyinBean.getTarget();
        for (int i1 = 0; i1 < target.length(); i1++) {
          pySb.append(Pinyin.toPinyin(target.charAt(i1)).toUpperCase());
        }
        indexPinyinBean.setIndexPinyin(pySb.toString()); //设置城市名全拼音
      }
    }
    return this;
  }

  /** 如果需要取出，则 取出首字母->tag,或者特殊字母 "#". 否则，用户已经实现设置好 */
  @Override
  public IIndexBarDataHelper fillIndexTag(List<? extends IndexPinyinBean> dataList) {
    if (null == dataList || dataList.isEmpty()) {
      return this;
    }
    int size = dataList.size();
    for (int i = 0; i < size; i++) {
      IndexPinyinBean indexPinyinBean = dataList.get(i);
      if (indexPinyinBean.isNeedToPinyin()) {
        //以下代码设置城市拼音首字母
        if (TextUtils.isEmpty(indexPinyinBean.getIndexPinyin())) {
          indexPinyinBean.setIndexTag("#");
          continue;
        }
        String tagString = indexPinyinBean.getIndexPinyin().substring(0, 1);
        if (tagString.matches("[A-Z]")) { //如果是A-Z字母开头
          indexPinyinBean.setIndexTag(tagString);
        } else { //特殊字母这里统一用#处理
          indexPinyinBean.setIndexTag("#");
        }
      }
    }
    return this;
  }

  @Override
  public IIndexBarDataHelper sortSourceData(List<? extends IndexPinyinBean> data) {
    if (null == data || data.isEmpty()) {
      return this;
    }
    convert(data);
    fillIndexTag(data);
    //对数据源进行排序
    Collections.sort(
        data,
        (Comparator<IndexPinyinBean>)
            (lhs, rhs) -> {
              if (!lhs.isNeedToPinyin()) {
                return 0;
              } else if (!rhs.isNeedToPinyin()) {
                return 0;
              } else if (lhs.getIndexTag().equals("#") && !rhs.getIndexTag().equals("#")) {
                return 1;
              } else if (!lhs.getIndexTag().equals("#") && rhs.getIndexTag().equals("#")) {
                return -1;
              } else {
                String lhsIndexTag = lhs.getIndexTag();
                String rhsIndexTag = rhs.getIndexTag();
                if (lhsIndexTag.equals("#") && rhsIndexTag.equals("#")) {
                  String lhsFirst = lhs.getIndexPinyin().substring(0, 1);
                  String rhsFirst = rhs.getIndexPinyin().substring(0, 1);
                  if (TextUtils.isDigitsOnly(lhsFirst) && !TextUtils.isDigitsOnly(rhsFirst)) {
                    return -1;
                  } else if (!TextUtils.isDigitsOnly(lhsFirst)
                      && TextUtils.isDigitsOnly(rhsFirst)) {
                    return 1;
                  } else {
                    return lhs.getIndexPinyin().compareTo(rhs.getIndexPinyin());
                  }
                } else {
                  return lhs.getIndexPinyin().compareTo(rhs.getIndexPinyin());
                }
              }
            });
    return this;
  }

  @Override
  public IIndexBarDataHelper getSortedIndexData(
      List<? extends IndexPinyinBean> sourceData, List<String> data) {
    if (null == sourceData || sourceData.isEmpty()) {
      return this;
    }
    int size = sourceData.size();
    String baseIndexTag;
    for (int i = 0; i < size; i++) {
      baseIndexTag = sourceData.get(i).getIndexTag();
      if (!data.contains(baseIndexTag)) {
        data.add(baseIndexTag);
      }
    }
    return this;
  }
}
