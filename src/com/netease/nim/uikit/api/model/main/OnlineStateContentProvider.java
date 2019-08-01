package com.netease.nim.uikit.api.model.main;

/**
 * Created by hzchenkang on 2017/3/31.
 */

public interface OnlineStateContentProvider {

    // 用于展示最近联系人界面的在线状态
    String getSimpleDisplay(String account);

    // 用于展示聊天界面的在线状态
    String getDetailDisplay(String account);
}
