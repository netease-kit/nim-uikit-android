// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.utils;

import android.text.TextUtils;
import kotlin.random.Random;

public class TeamIconUtils {

  public static final String[] DEFAULT_ICON_URL_ARRAY =
      new String[] {
        "https://s.netease.im/safe/ABg8YjWQWvcqO6sAAAAAAAAAAAA?_im_url=1",
        "https://s.netease.im/safe/ABg8YjmQWvcqO6sAAAAAAAABAAA?_im_url=1",
        "https://s.netease.im/safe/ABg8YjyQWvcqO6sAAAAAAAABAAA?_im_url=1",
        "https://s.netease.im/safe/ABg8YkCQWvcqO6sAAAAAAAABAAA?_im_url=1",
        "https://s.netease.im/safe/ABg8YkSQWvcqO6sAAAAAAAABAAA?_im_url=1"
      };

  public static String getDefaultIconUrl(int index) {
    if (index < 0 || index > 4) {
      return null;
    }
    return DEFAULT_ICON_URL_ARRAY[index];
  }

  public static int getDefaultIconUrlIndex(String url) {
    for (int index = 0; index < DEFAULT_ICON_URL_ARRAY.length; index++) {
      if (TextUtils.equals(url, DEFAULT_ICON_URL_ARRAY[index])) {
        return index;
      }
    }
    return -1;
  }

  public static String getDefaultRandomIconUrl() {
    return DEFAULT_ICON_URL_ARRAY[Random.Default.nextInt(0, 5)];
  }
}
