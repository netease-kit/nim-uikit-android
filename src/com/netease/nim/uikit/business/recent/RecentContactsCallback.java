package com.netease.nim.uikit.business.recent;

import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.model.RecentContact;

/**
 * 最近联系人列表自定义事件回调函数.
 */
public interface RecentContactsCallback {

    /**
     * 最近联系人列表数据加载完成的回调函数
     */
    void onRecentContactsLoaded();

    /**
     * 有未读数更新时的回调函数，供更新除最近联系人列表外的其他界面和未读指示
     *
     * @param unreadCount 当前总的未读数
     */
    void onUnreadCountChange(int unreadCount);

    /**
     * 最近联系人点击响应回调函数，以供打开会话窗口时传入定制化参数，或者做其他动作
     *
     * @param recent 最近联系人
     */
    void onItemClick(RecentContact recent);

    /**
     * 设置自定义消息的摘要信息，展示在最近联系人列表的消息缩略栏上.
     * 当然，你也可以自定义一些内建消息的缩略语，例如图片，语音，音视频会话等，自定义的缩略语会被优先使用。
     *
     * @param attachment 消息附件对象
     * @return 消息摘要
     */
    String getDigestOfAttachment(RecentContact recent, MsgAttachment attachment);

    /**
     * 设置Tip消息的摘要信息，展示在最近联系人列表的消息缩略栏上
     *
     * @param recent 最近联系人
     * @return Tip消息摘要
     */
    String getDigestOfTipMsg(RecentContact recent);
}
