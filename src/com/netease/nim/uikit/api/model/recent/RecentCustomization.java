package com.netease.nim.uikit.api.model.recent;

import com.netease.nimlib.sdk.msg.model.RecentContact;

import java.io.Serializable;

/**
 * 会话界面定制
 * 1. 会话项默认文案定制 
 * Created by huangjun on 2017/9/29.
 */

public class RecentCustomization implements Serializable {
    public String getDefaultDigest(RecentContact recent) {
        return null;
    }
}
