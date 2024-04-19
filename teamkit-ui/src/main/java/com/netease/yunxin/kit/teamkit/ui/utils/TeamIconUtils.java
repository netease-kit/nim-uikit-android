// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.utils;

import android.text.TextUtils;
import kotlin.random.Random;

/** 群头像工具类 群头像默认内置头像地址 */
public class TeamIconUtils {

  public static final String[] DEFAULT_ICON_URL_ARRAY =
      new String[] {
        "https://s.netease.im/safe/ABg8YjWQWvcqO6sAAAAAAAAAAAA?_im_url=1",
        "https://s.netease.im/safe/ABg8YjmQWvcqO6sAAAAAAAABAAA?_im_url=1",
        "https://s.netease.im/safe/ABg8YjyQWvcqO6sAAAAAAAABAAA?_im_url=1",
        "https://s.netease.im/safe/ABg8YkCQWvcqO6sAAAAAAAABAAA?_im_url=1",
        "https://s.netease.im/safe/ABg8YkSQWvcqO6sAAAAAAAABAAA?_im_url=1"
      };
  public static final String[] DEFAULT_ICON_URL_ARRAY_SQUARE =
      new String[] {
        "https://nim-nosdn.netease.im/MjYxNDkzNzE=/bmltYV8xNDIxMTk0NzAzMzhfMTY4NDgyNzc0MTczNV8yY2FlMjczZS01MDk0LTQ5NWMtODMzMS1mYTBmMTE1NmEyNDQ=",
        "https://nim-nosdn.netease.im/MjYxNDkzNzE=/bmltYV8xNDIxMTk0NzAzMzhfMTY4NDgyNzc0MTczNV9jYWJmNjViNy1kMGM3LTRiNDEtYmVmMi1jYjhiNzRjY2EwY2M=",
        "https://nim-nosdn.netease.im/MjYxNDkzNzE=/bmltYV8xNDIxMTk0NzAzMzhfMTY4NDgyNzc0MTczNV8yMzY1YmY5YS0xNGE1LTQxYTctYTg2My1hMzMyZWE5YzhhOTQ=",
        "https://nim-nosdn.netease.im/MjYxNDkzNzE=/bmltYV8xNDIxMTk0NzAzMzhfMTY4NDgyNzc0MTczNV80NTQxMDhhNy1mNWMzLTQxMzMtOWU3NS1hNThiN2FiNjI5MWY=",
        "https://nim-nosdn.netease.im/MjYxNDkzNzE=/bmltYV8xNDIxMTk0NzAzMzhfMTY4NDgyNzc0MTczNV8wMGVlNWUyOS0wYzg3LTQxMzUtYmVjOS00YjI1MjcxMDhhNTM="
      };

  // 获取默认头像地址
  public static String getDefaultIconUrl(int index, boolean isCircle) {
    if (index < 0 || index > 4) {
      return null;
    }
    return getDefaultIconUrlArray(isCircle)[index];
  }

  // 获取默认头像地址索引
  public static int getDefaultIconUrlIndex(String url, boolean isCircle) {
    String[] array = getDefaultIconUrlArray(isCircle);
    for (int index = 0; index < array.length; index++) {
      if (TextUtils.equals(url, array[index])) {
        return index;
      }
    }
    return -1;
  }

  // 获取默认随机头像地址
  public static String getDefaultRandomIconUrl(boolean isCircle) {
    return getDefaultIconUrlArray(isCircle)[Random.Default.nextInt(0, 5)];
  }

  // 获取默认头像地址数组
  private static String[] getDefaultIconUrlArray(boolean isCircle) {
    return isCircle ? DEFAULT_ICON_URL_ARRAY : DEFAULT_ICON_URL_ARRAY_SQUARE;
  }
}
