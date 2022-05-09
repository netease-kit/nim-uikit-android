/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.utils;

import android.text.TextUtils;

public class ColorUtils {

    public static int avatarColor(String content){
        if (!TextUtils.isEmpty(content)){
            return content.charAt(content.length() - 1);
        }
        return 0;
    }

    public static int avatarColor(long value){
        return (int)value % Integer.MAX_VALUE;
    }
}
