// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.emoji;

public class StickerItem {
  private String category;
  private String name;

  public StickerItem(String category, String name) {
    this.category = category;
    this.name = name;
  }

  public String getIdentifier() {
    return category + "/" + name;
  }

  public String getCategory() {
    return category;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof StickerItem) {
      StickerItem item = (StickerItem) o;
      return item.getCategory().equals(category) && item.getName().equals(name);
    }

    return false;
  }
}
