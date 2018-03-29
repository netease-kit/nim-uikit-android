package com.netease.nim.uikit.impl;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.UIKitInitStateListener;
import com.netease.nim.uikit.api.UIKitOptions;
import com.netease.nim.uikit.api.model.chatroom.ChatRoomMemberChangedObservable;
import com.netease.nim.uikit.api.model.chatroom.ChatRoomProvider;
import com.netease.nim.uikit.api.model.chatroom.ChatRoomSessionCustomization;
import com.netease.nim.uikit.api.model.contact.ContactChangedObservable;
import com.netease.nim.uikit.api.model.contact.ContactEventListener;
import com.netease.nim.uikit.api.model.contact.ContactProvider;
import com.netease.nim.uikit.api.model.location.LocationProvider;
import com.netease.nim.uikit.api.model.main.CustomPushContentProvider;
import com.netease.nim.uikit.api.model.main.LoginSyncDataStatusObserver;
import com.netease.nim.uikit.api.model.main.OnlineStateChangeObservable;
import com.netease.nim.uikit.api.model.main.OnlineStateContentProvider;
import com.netease.nim.uikit.api.model.recent.RecentCustomization;
import com.netease.nim.uikit.api.model.robot.RobotInfoProvider;
import com.netease.nim.uikit.api.model.session.SessionCustomization;
import com.netease.nim.uikit.api.model.session.SessionEventListener;
import com.netease.nim.uikit.api.model.team.TeamChangedObservable;
import com.netease.nim.uikit.api.model.team.TeamProvider;
import com.netease.nim.uikit.api.model.user.IUserInfoProvider;
import com.netease.nim.uikit.api.model.user.UserInfoObservable;
import com.netease.nim.uikit.business.chatroom.fragment.ChatRoomMessageFragment;
import com.netease.nim.uikit.business.chatroom.viewholder.ChatRoomMsgViewHolderBase;
import com.netease.nim.uikit.business.chatroom.viewholder.ChatRoomMsgViewHolderFactory;
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.business.preference.UserPreferences;
import com.netease.nim.uikit.business.session.activity.P2PMessageActivity;
import com.netease.nim.uikit.business.session.activity.TeamMessageActivity;
import com.netease.nim.uikit.business.session.audio.MessageAudioControl;
import com.netease.nim.uikit.business.session.emoji.StickerManager;
import com.netease.nim.uikit.business.session.module.MsgForwardFilter;
import com.netease.nim.uikit.business.session.module.MsgRevokeFilter;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderFactory;
import com.netease.nim.uikit.business.team.activity.AdvancedTeamInfoActivity;
import com.netease.nim.uikit.business.team.activity.NormalTeamInfoActivity;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.impl.cache.ChatRoomCacheManager;
import com.netease.nim.uikit.impl.cache.DataCacheManager;
import com.netease.nim.uikit.impl.customization.DefaultContactEventListener;
import com.netease.nim.uikit.impl.customization.DefaultP2PSessionCustomization;
import com.netease.nim.uikit.impl.customization.DefaultRecentCustomization;
import com.netease.nim.uikit.impl.customization.DefaultTeamSessionCustomization;
import com.netease.nim.uikit.impl.provider.DefaultChatRoomProvider;
import com.netease.nim.uikit.impl.provider.DefaultContactProvider;
import com.netease.nim.uikit.impl.provider.DefaultRobotProvider;
import com.netease.nim.uikit.impl.provider.DefaultTeamProvider;
import com.netease.nim.uikit.impl.provider.DefaultUserInfoProvider;
import com.netease.nim.uikit.support.glide.ImageLoaderKit;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomResultData;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;

/**
 * UIKit能力实现类。
 */
public final class NimUIKitImpl {

    // context
    private static Context context;

    // 自己的用户帐号
    private static String account;

    private static UIKitOptions options;

    // 用户信息提供者
    private static IUserInfoProvider userInfoProvider;

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
    private static OnlineStateChangeObservable onlineStateChangeObservable;

    // userInfo 变更监听
    private static UserInfoObservable userInfoObservable;

    // contact 变化监听
    private static ContactChangedObservable contactChangedObservable;

    //群、群成员信息提供者
    private static TeamProvider teamProvider;

    //群、群成员变化监听
    private static TeamChangedObservable teamChangedObservable;

    //智能机器人提供者
    private static RobotInfoProvider robotInfoProvider;

    // 聊天室提供者
    private static ChatRoomProvider chatRoomProvider;

    // 聊天室成员变更通知
    private static ChatRoomMemberChangedObservable chatRoomMemberChangedObservable;

    // 缓存构建成功
    private static boolean buildCacheComplete = false;

    //初始化状态监听
    private static UIKitInitStateListener initStateListener;

    /*
     * ****************************** 初始化 ******************************
     */
    public static void init(Context context) {
        init(context, new UIKitOptions(), null, null);
    }

    public static void init(Context context, UIKitOptions options) {
        init(context, options, null, null);
    }

    public static void init(Context context, IUserInfoProvider userInfoProvider, ContactProvider contactProvider) {
        init(context, new UIKitOptions(), userInfoProvider, contactProvider);
    }

    public static void init(Context context, UIKitOptions options, IUserInfoProvider userInfoProvider, ContactProvider contactProvider) {
        NimUIKitImpl.context = context.getApplicationContext();
        NimUIKitImpl.options = options;
        // init tools
        StorageUtil.init(context, options.appCacheDir);
        ScreenUtil.init(context);

        if (options.loadSticker) {
            StickerManager.getInstance().init();
        }

        // init log
        String path = StorageUtil.getDirectoryByDirType(StorageType.TYPE_LOG);
        LogUtil.init(path, Log.DEBUG);

        NimUIKitImpl.imageLoaderKit = new ImageLoaderKit(context);

        if (!options.independentChatRoom) {
            initUserInfoProvider(userInfoProvider);
            initContactProvider(contactProvider);
            initDefaultSessionCustomization();
            initDefaultContactEventListener();
            // init data cache
            LoginSyncDataStatusObserver.getInstance().registerLoginSyncDataStatus(true);  // 监听登录同步数据完成通知
            DataCacheManager.observeSDKDataChanged(true);
        }

        ChatRoomCacheManager.initCache();
        if (!TextUtils.isEmpty(getAccount())) {
            if (options.initAsync) {
                DataCacheManager.buildDataCacheAsync(); // build data cache on auto login
            } else {
                DataCacheManager.buildDataCache(); // build data cache on auto login
                buildCacheComplete = true;
            }
            getImageLoaderKit().buildImageCache(); // build image cache on auto login
        }
    }

    public static boolean isInitComplete() {
        return !options.initAsync || TextUtils.isEmpty(account) || buildCacheComplete;
    }

    public static void setInitStateListener(UIKitInitStateListener listener) {
        initStateListener = listener;
    }

    public static void notifyCacheBuildComplete() {
        buildCacheComplete = true;
        if (initStateListener != null) {
            initStateListener.onFinish();
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
                loginSuccess(loginInfo.getAccount());
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

    public static void loginSuccess(String account) {
        setAccount(account);
        DataCacheManager.buildDataCache();
        buildCacheComplete = true;
        getImageLoaderKit().buildImageCache();
    }

    public static void logout() {
        DataCacheManager.clearDataCache();
        ChatRoomCacheManager.clearCache();
        getImageLoaderKit().clear();
        LoginSyncDataStatusObserver.getInstance().reset();
    }

    public static void enterChatRoomSuccess(EnterChatRoomResultData data, boolean independent) {
        ChatRoomInfo roomInfo = data.getRoomInfo();

        if (independent) {
            setAccount(data.getAccount());
            DataCacheManager.buildRobotCacheIndependent(roomInfo.getRoomId());
        }

        //存储 member
        ChatRoomMember member = data.getMember();
        member.setRoomId(roomInfo.getRoomId());
        ChatRoomCacheManager.saveMyMember(member);
    }

    public static void exitedChatRoom(String roomId) {
        ChatRoomCacheManager.clearRoomCache(roomId);
    }

    public static UIKitOptions getOptions() {
        return options;
    }

    // 初始化用户信息提供者
    private static void initUserInfoProvider(IUserInfoProvider userInfoProvider) {

        if (userInfoProvider == null) {
            userInfoProvider = new DefaultUserInfoProvider();
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

    public static void startTeamSession(Context context, String tid, SessionCustomization sessionCustomization, IMMessage anchor) {
        NimUIKitImpl.startChatting(context, tid, SessionTypeEnum.Team, sessionCustomization, anchor);
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
        Team team = NimUIKit.getTeamProvider().getTeamById(teamId);
        if (team == null) {
            return;
        }
        if (team.getType() == TeamTypeEnum.Advanced) {
            AdvancedTeamInfoActivity.start(context, teamId); // 启动固定群资料页
        } else if (team.getType() == TeamTypeEnum.Normal) {
            NormalTeamInfoActivity.start(context, teamId); // 启动讨论组资料页
        }
    }

    public static IUserInfoProvider getUserInfoProvider() {
        return userInfoProvider;
    }

    public static UserInfoObservable getUserInfoObservable() {
        if (userInfoObservable == null) {
            userInfoObservable = new UserInfoObservable(context);
        }
        return userInfoObservable;
    }

    public static ContactProvider getContactProvider() {
        return contactProvider;
    }


    public static void setTeamProvider(TeamProvider provider) {
        teamProvider = provider;
    }

    public static ContactChangedObservable getContactChangedObservable() {
        if (contactChangedObservable == null) {
            contactChangedObservable = new ContactChangedObservable(context);
        }
        return contactChangedObservable;
    }

    public static TeamProvider getTeamProvider() {
        if (teamProvider == null) {
            teamProvider = new DefaultTeamProvider();
        }
        return teamProvider;
    }

    public static TeamChangedObservable getTeamChangedObservable() {
        if (teamChangedObservable == null) {
            teamChangedObservable = new TeamChangedObservable(context);
        }
        return teamChangedObservable;
    }

    public static void setRobotInfoProvider(RobotInfoProvider provider) {
        robotInfoProvider = provider;
    }

    public static RobotInfoProvider getRobotInfoProvider() {
        if (robotInfoProvider == null) {
            robotInfoProvider = new DefaultRobotProvider();
        }
        return robotInfoProvider;
    }

    public static void setChatRoomProvider(ChatRoomProvider provider) {
        chatRoomProvider = provider;
    }

    public static ChatRoomProvider getChatRoomProvider() {
        if (chatRoomProvider == null) {
            chatRoomProvider = new DefaultChatRoomProvider();
        }
        return chatRoomProvider;
    }

    public static ChatRoomMemberChangedObservable getChatRoomMemberChangedObservable() {
        if (chatRoomMemberChangedObservable == null) {
            chatRoomMemberChangedObservable = new ChatRoomMemberChangedObservable(context);
        }
        return chatRoomMemberChangedObservable;
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

    public static OnlineStateChangeObservable getOnlineStateChangeObservable() {
        if (onlineStateChangeObservable == null) {
            onlineStateChangeObservable = new OnlineStateChangeObservable(context);
        }
        return onlineStateChangeObservable;
    }

    public static boolean enableOnlineState() {
        return onlineStateContentProvider != null;
    }


    public static void setEarPhoneModeEnable(boolean enable) {
        MessageAudioControl.getInstance(context).setEarPhoneModeEnable(enable);
        UserPreferences.setEarPhoneModeEnable(enable);
    }

    public static boolean getEarPhoneModeEnable() {
        return UserPreferences.isEarPhoneModeEnable();
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
