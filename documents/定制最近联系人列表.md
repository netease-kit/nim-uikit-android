# 定制最近联系人列表

RecentContactsFragment 实现了默认的列表点击事件处理，点击列表项将会直接跳转至默认的单聊或者群聊界面，若默认实现无法满足需求，开发者可以参照下文定制最近联系人列表来实现，详细可以参照云信 Demo。

## 界面布局

最近联系人的布局属性如下图所示：

![image](https://github.com/netease-im/NIM_Resources/blob/master/Android/Images/recent.png)

|属性|说明|
|:---|:---|
|network_status_bar|网络状态背景条|
|status_desc_label|网络状态文字说明|
|img_head|头像|
|tv_nickname|昵称|
|tv_online_state|在线状态|
|tv_message|消息内容|
|tv_date_time|时间戳|
|img_msg_status|消息发送状态|
|unread_number_tip|未读红点，占坑用|
|unread_number_explosion|未读红点，全屏动画时使用|

## 深度定制

开发者也可以通过设置自定义事件回调函数 RecentContactsCallback 来定制，目前支持：

1\. 最近联系人列表数据加载完成的回调函数（默认不作处理）。

2\. 有未读数更新时的回调函数，供更新除最近联系人列表外的其他界面和未读指示（默认不作处理）。

3\. 最近联系人点击响应回调函数，以供打开会话窗口时传入定制化参数，或者做其他动作（默认跳转至聊天窗口）。

4\. 设置自定义消息的摘要消息，展示在最近联系人列表的消息缩略栏上。当然，你也可以自定义一些内建消息的缩略语，例如图片，语音，音视频会话等，自定义的缩略语会被优先使用。

5\. 设置Tip消息的摘要信息，展示在最近联系人列表的消息缩略栏上。

如下是云信 Demo对最近联系人列表的定制:

```java
// 设置自定义事件回调函数
contactsFragment.setCallback(new RecentContactsCallback() {
    @Override
    public void onRecentContactsLoaded() {
        // 最近联系人列表加载完毕
    }

    @Override
    public void onUnreadCountChange(int unreadCount) {
        // 未读数发生变化
        ReminderManager.getInstance().updateSessionUnreadNum(unreadCount);
    }

    @Override
    public void onItemClick(RecentContact recent) {
        // 回调函数，以供打开会话窗口时传入定制化参数，或者做其他动作
        switch (recent.getSessionType()) {
            case P2P:
                SessionHelper.startP2PSession(getActivity(), recent.getContactId());
                break;
            case Team:
                SessionHelper.startTeamSession(getActivity(), recent.getContactId());
                break;
            default:
                break;
        }
    }

    @Override
    public String getDigestOfAttachment(MsgAttachment attachment) {
        // 设置自定义消息的摘要消息，展示在最近联系人列表的消息缩略栏上
        // 当然，你也可以自定义一些内建消息的缩略语，例如图片，语音，音视频会话等，自定义的缩略语会被优先使用。
        if (attachment instanceof GuessAttachment) {
            GuessAttachment guess = (GuessAttachment) attachment;
            return guess.getValue().getDesc();
        } else if (attachment instanceof RTSAttachment) {
            RTSAttachment rts = (RTSAttachment) attachment;
            return rts.getContent();
        } else if (attachment instanceof StickerAttachment) {
            return "[贴图]";
        } else if (attachment instanceof SnapChatAttachment) {
            return "[阅后即焚]";
        }
        return null;
    }

    @Override
    public String getDigestOfTipMsg(RecentContact recent) {
	    // 设置Tip消息的摘要信息，展示在最近联系人列表的消息缩略栏上
        String msgId = recent.getRecentMessageId();
        List<String> uuids = new ArrayList<>(1);
        uuids.add(msgId);
        List<IMMessage> msgs = NIMClient.getService(MsgService.class).queryMessageListByUuidBlock(uuids);
        if (msgs != null && !msgs.isEmpty()) {
            IMMessage msg = msgs.get(0);
            Map<String, Object> content = msg.getRemoteExtension();
            if (content != null && !content.isEmpty()) {
                return (String) content.get("content");
             }
        }
        return null;
    }
});
```