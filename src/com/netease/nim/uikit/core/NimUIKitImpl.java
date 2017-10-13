package com.netease.nim.uikit.core;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.netease.nim.uikit.cache.DataCacheManager;
import com.netease.nim.uikit.cache.TeamDataCache;
import com.netease.nim.uikit.chatroom.ChatRoomSessionCustomization;
import com.netease.nim.uikit.chatroom.fragment.ChatRoomMessageFragment;
import com.netease.nim.uikit.chatroom.viewholder.ChatRoomMsgViewHolderBase;
import com.netease.nim.uikit.chatroom.viewholder.ChatRoomMsgViewHolderFactory;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.contact.ContactEventListener;
import com.netease.nim.uikit.contact.ContactProvider;
import com.netease.nim.uikit.contact_selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.custom.DefaultContactEventListener;
import com.netease.nim.uikit.custom.DefaultContactProvider;
import com.netease.nim.uikit.custom.DefaultP2PSessionCustomization;
import com.netease.nim.uikit.custom.DefaultRecentCustomization;
import com.netease.nim.uikit.custom.DefaultTeamSessionCustomization;
import com.netease.nim.uikit.custom.DefaultUserInfoProvider;
import com.netease.nim.uikit.glide.ImageLoaderKit;
import com.netease.nim.uikit.plugin.CustomPushContentProvider;
import com.netease.nim.uikit.plugin.LocationProvider;
import com.netease.nim.uikit.plugin.LoginSyncDataStatusObserver;
import com.netease.nim.uikit.plugin.OnlineStateChangeListener;
import com.netease.nim.uikit.plugin.OnlineStateContentProvider;
import com.netease.nim.uikit.session.RecentCustomization;
import com.netease.nim.uikit.session.SessionCustomization;
import com.netease.nim.uikit.session.SessionEventListener;
import com.netease.nim.uikit.session.activity.P2PMessageActivity;
import com.netease.nim.uikit.session.activity.TeamMessageActivity;
import com.netease.nim.uikit.session.emoji.StickerManager;
import com.netease.nim.uikit.session.module.MsgForwardFilter;
import com.netease.nim.uikit.session.module.MsgRevokeFilter;
import com.netease.nim.uikit.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.session.viewholder.MsgViewHolderFactory;
import com.netease.nim.uikit.team.activity.AdvancedTeamInfoActivity;
import com.netease.nim.uikit.team.activity.NormalTeamInfoActivity;
import com.netease.nim.uikit.uinfo.UserInfoHelper;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * UIKit能力实现类。
 */
public final class NimUIKitImpl {

    // context
    private static Context context;

    // 自己的用户帐号
    private static String account;

    // 用户信息提供者
    private static UserInfoProvider userInfoProvider;

    // 通讯录信息提供者
    private static ContactProvider contactProvider;

    // 地理位置信息提供者
    private static LocationProvider locationProvider;

    // 图片加载、缓存与管理组件
    private static ImageLoaderKit imageLoaderKit;

    // 会话窗口消息列表一些点击事件的响应处理函数
    private static SessionEventListener sessionListener;

    // 通讯录列表一些点击事件的响应处理函数
    private static ContactEventListener contactEventListener;

    // 转发消息过滤器
    private static MsgForwardFilter msgForwardFilter;

    // 撤回消息过滤器
    private static MsgRevokeFilter msgRevokeFilter;

    // 自定义推送配置
    private static CustomPushContentProvider customPushContentProvider;

    // 单聊界面定制
    private static SessionCustomization commonP2PSessionCustomization;

    // 群聊界面定制
    private static SessionCustomization commonTeamSessionCustomization;

    // 最近联系人界面定制
    private static RecentCustomization recentCustomization;

    // 在线状态展示内容
    private static OnlineStateContentProvider onlineStateContentProvider;

    // 在线状态变化监听
    private static List<OnlineStateChangeListener> onlineStateChangeListeners;

    /*
     * ****************************** 初始化 ******************************
     */
    public static void init(Context context) {
        init(context, null, null);
    }

    public static void init(Context context, UserInfoProvider userInfoProvider, ContactProvider contactProvider) {
        NimUIKitImpl.context = context.getApplicationContext();

        // init tools
        StorageUtil.init(context, null);
        ScreenUtil.init(context);
        StickerManager.getInstance().init();

        // init log
        String path = StorageUtil.getDirectoryByDirType(StorageType.TYPE_LOG);
        LogUtil.init(path, Log.DEBUG);

        initUserInfoProvider(userInfoProvider);
        initContactProvider(contactProvider);
        initDefaultSessionCustomization();
        initDefaultContactEventListener();

        NimUIKitImpl.imageLoaderKit = new ImageLoaderKit(context);

        // init data cache
        LoginSyncDataStatusObserver.getInstance().registerLoginSyncDataStatus(true);  // 监听登录同步数据完成通知
        DataCacheManager.observeSDKDataChanged(true);
        if (!TextUtils.isEmpty(getAccount())) {
            DataCacheManager.buildDataCache(); // build data cache on auto login
            getImageLoaderKit().buildImageCache(); // build image cache on auto login
        }
    }

    /*
    * ****************************** 登录登出 ******************************
    */
    public static AbortableFuture<LoginInfo> login(LoginInfo loginInfo, final RequestCallback<LoginInfo> callback) {

        AbortableFuture<LoginInfo> loginRequest = NIMClient.getService(AuthService.class).login(loginInfo);
        loginRequest.setCallback(new RequestCallback<LoginInfo>() {
            @Override
            public void onSuccess(LoginInfo loginInfo) {
                NimUIKitImpl.setAccount(loginInfo.getAccount());
                DataCacheManager.buildDataCacheAsync();
                getImageLoaderKit().buildImageCache();
                callback.onSuccess(loginInfo);
            }

            @Override
            public void onFailed(int code) {
                callback.onFailed(code);
            }

            @Override
            public void onException(Throwable exception) {
                callback.onException(exception);
            }
        });
        return loginRequest;
    }

    public static void logout() {
        DataCacheManager.clearDataCache();
        getImageLoaderKit().clear();
    }


    // 初始化用户信息提供者
    private static void initUserInfoProvider(UserInfoProvider userInfoProvider) {

        if (userInfoProvider == null) {
            userInfoProvider = new DefaultUserInfoProvider(context);
        }

        NimUIKitImpl.userInfoProvider = userInfoProvider;
    }

    // 初始化联系人信息提供者
    private static void initContactProvider(ContactProvider contactProvider) {
        if (contactProvider == null) {
            contactProvider = new DefaultContactProvider();
        }

        NimUIKitImpl.contactProvider = contactProvider;
    }

    // 初始化会话定制，P2P、Team、ChatRoom
    private static void initDefaultSessionCustomization() {
        if (commonP2PSessionCustomization == null) {
            commonP2PSessionCustomization = new DefaultP2PSessionCustomization();
        }

        if (commonTeamSessionCustomization == null) {
            commonTeamSessionCustomization = new DefaultTeamSessionCustomization();
        }

        if (recentCustomization == null) {
            recentCustomization = new DefaultRecentCustomization();
        }
    }

    // 初始化联系人点击事件
    private static void initDefaultContactEventListener() {
        if (contactEventListener == null) {
            contactEventListener = new DefaultContactEventListener();
        }
    }

    public static void startP2PSession(Context context, String account) {
        startP2PSession(context, account, null);
    }

    public static void startP2PSession(Context context, String account, IMMessage anchor) {
        NimUIKitImpl.startChatting(context, account, SessionTypeEnum.P2P, commonP2PSessionCustomization, anchor);
    }

    public static void startTeamSession(Context context, String tid) {
        startTeamSession(context, tid, null);
    }

    public static void startTeamSession(Context context, String tid, IMMessage anchor) {
        NimUIKitImpl.startChatting(context, tid, SessionTypeEnum.Team, commonTeamSessionCustomization, anchor);
    }

    public static void startChatting(Context context, String id, SessionTypeEnum sessionType, SessionCustomization
            customization, IMMessage anchor) {
        if (sessionType == SessionTypeEnum.P2P) {
            P2PMessageActivity.start(context, id, customization, anchor);
        } else if (sessionType == SessionTypeEnum.Team) {
            TeamMessageActivity.start(context, id, customization, null, anchor);
        }
    }

    public static void startChatting(Context context, String id, SessionTypeEnum sessionType, SessionCustomization customization,
                                     Class<? extends Activity> backToClass, IMMessage anchor) {
        if (sessionType == SessionTypeEnum.Team) {
            TeamMessageActivity.start(context, id, customization, backToClass, anchor);
        }
    }

    public static void startContactSelector(Context context, ContactSelectActivity.Option option, int requestCode) {
        ContactSelectActivity.startActivityForResult(context, option, requestCode);
    }

    public static void startTeamInfo(Context context, String teamId) {
        Team team = TeamDataCache.getInstance().getTeamById(teamId);
        if (team == null) {
            return;
        }
        if (team.getType() == TeamTypeEnum.Advanced) {
            AdvancedTeamInfoActivity.start(context, teamId); // 启动固定群资料页
        } else if (team.getType() == TeamTypeEnum.Normal) {
            NormalTeamInfoActivity.start(context, teamId); // 启动讨论组资料页
        }

    }

    public static UserInfoProvider getUserInfoProvider() {
        return userInfoProvider;
    }

    public static ContactProvider getContactProvider() {
        return contactProvider;
    }

    public static LocationProvider getLocationProvider() {
        return locationProvider;
    }

    public static ImageLoaderKit getImageLoaderKit() {
        return imageLoaderKit;
    }

    public static void setLocationProvider(LocationProvider locationProvider) {
        NimUIKitImpl.locationProvider = locationProvider;
    }

    public static void setCommonP2PSessionCustomization(SessionCustomization commonP2PSessionCustomization) {
        NimUIKitImpl.commonP2PSessionCustomization = commonP2PSessionCustomization;
    }

    public static void setCommonTeamSessionCustomization(SessionCustomization commonTeamSessionCustomization) {
        NimUIKitImpl.commonTeamSessionCustomization = commonTeamSessionCustomization;
    }

    public static void setRecentCustomization(RecentCustomization recentCustomization) {
        NimUIKitImpl.recentCustomization = recentCustomization;
    }

    public static void setCommonChatRoomSessionCustomization(ChatRoomSessionCustomization commonChatRoomSessionCustomization) {
        ChatRoomMessageFragment.setChatRoomSessionCustomization(commonChatRoomSessionCustomization);
    }

    public static RecentCustomization getRecentCustomization() {
        return recentCustomization;
    }

    public static void registerMsgItemViewHolder(Class<? extends MsgAttachment> attach, Class<? extends MsgViewHolderBase> viewHolder) {
        MsgViewHolderFactory.register(attach, viewHolder);
    }

    public static void registerChatRoomMsgItemViewHolder(Class<? extends MsgAttachment> attach, Class<? extends ChatRoomMsgViewHolderBase> viewHolder) {
        ChatRoomMsgViewHolderFactory.register(attach, viewHolder);
    }

    public static void registerTipMsgViewHolder(Class<? extends MsgViewHolderBase> viewHolder) {
        MsgViewHolderFactory.registerTipMsgViewHolder(viewHolder);
    }

    public static void setAccount(String account) {
        NimUIKitImpl.account = account;
    }

    public static SessionEventListener getSessionListener() {
        return sessionListener;
    }

    public static void setSessionListener(SessionEventListener sessionListener) {
        NimUIKitImpl.sessionListener = sessionListener;
    }

    public static ContactEventListener getContactEventListener() {
        return contactEventListener;
    }


    public static void setContactEventListener(ContactEventListener contactEventListener) {
        NimUIKitImpl.contactEventListener = contactEventListener;
    }

    public static void notifyUserInfoChanged(List<String> accounts) {
        UserInfoHelper.notifyChanged(accounts);
    }

    public static void setMsgForwardFilter(MsgForwardFilter msgForwardFilter) {
        NimUIKitImpl.msgForwardFilter = msgForwardFilter;
    }

    public static MsgForwardFilter getMsgForwardFilter() {
        return msgForwardFilter;
    }

    public static void setMsgRevokeFilter(MsgRevokeFilter msgRevokeFilter) {
        NimUIKitImpl.msgRevokeFilter = msgRevokeFilter;
    }

    public static MsgRevokeFilter getMsgRevokeFilter() {
        return msgRevokeFilter;
    }

    public static CustomPushContentProvider getCustomPushContentProvider() {
        return customPushContentProvider;
    }

    public static void setCustomPushContentProvider(CustomPushContentProvider mixPushCustomConfig) {
        NimUIKitImpl.customPushContentProvider = mixPushCustomConfig;
    }

    /*
    * ****************************** 在线状态 ******************************
    */

    public static void setOnlineStateContentProvider(OnlineStateContentProvider onlineStateContentProvider) {
        NimUIKitImpl.onlineStateContentProvider = onlineStateContentProvider;
    }

    public static OnlineStateContentProvider getOnlineStateContentProvider() {
        return onlineStateContentProvider;
    }

    public static void addOnlineStateChangeListeners(OnlineStateChangeListener onlineStateChangeListener) {
        if (onlineStateChangeListeners == null) {
            onlineStateChangeListeners = new LinkedList<>();
        }
        onlineStateChangeListeners.add(onlineStateChangeListener);
    }

    public static void removeOnlineStateChangeListeners(OnlineStateChangeListener onlineStateChangeListener) {
        if (onlineStateChangeListeners == null) {
            return;
        }
        onlineStateChangeListeners.remove(onlineStateChangeListener);
    }

    public static boolean enableOnlineState() {
        return onlineStateContentProvider != null;
    }

    public static void notifyOnlineStateChange(Set<String> accounts) {
        if (onlineStateChangeListeners != null) {
            for (OnlineStateChangeListener listener : onlineStateChangeListeners) {
                listener.onlineStateChange(accounts);
            }
        }
    }

    /*
    * ****************************** basic ******************************
    */
    public static Context getContext() {
        return context;
    }

    public static String getAccount() {
        return account;
    }
}
