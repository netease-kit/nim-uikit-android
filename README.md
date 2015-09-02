# 云信UI组件使用说明

云信UI组件提供了构建IM功能所需的两个基本功能模块：聊天窗口和最近联系人列表。其他功能有：照片选择、查看大图、视频采集与播放。
UI组件工程提供了较为简洁的接口，开发者可基于组件快速的实现聊天界面和最近联系人功能，并实现基础的一些定制化开发。云信的UI组件完全开源，如果开发者希望修改界面，只需要通过替换界面资源，修改layout等方式即可实现。如果开发者希望更深层次的自定义，也可自行修改代码。

## 初始化UI组件

首先将官网的UIKit解压后作为库工程导入项目中，
在Android Studio将UIKit作为Module导入到Project中，并修改build.gradle文件中的buildToolsVer为自己的版本号，如"21.1.1"，并在你App的build
.gradle的dependencies下加入：
```
dependencies {
    ...
    compile project(path: ':uikit')
}
```
并在Application中初始化：

```
public class NimApplication extends Application {

	public void onCreate() {
		// ... your codes
		// 初始化云信SDK
		NIMClient.init(this, loginInfo(), options());
		// ... your codes

        if (inMainProcess()) {
            // 在主进程中初始化UI组件，判断所属进程方法请参见demo源码。
            initUiKit();
            // ... your codes
        }
	}

    private void initUiKit() {
    	// 设置用户资料提供者（必须）
        NimUIKit.init(this, new new UserInfoProvider(){ ... });

        // 注册定位信息提供者类（可选）,如果需要发送地理位置消息，必须提供。
        // demo中使用高德地图实现了该提供者，开发者可以根据自身需求，选用高德，百度，google等任意第三方地图和定位SDK。
        NimUIKit.setLocationProvider(new NimDemoLocationProvider());

        // 聊天初始化
        initSession();
    }

    // 示例代码可详见demo源码中的SessionHelper类。
     private void initSession() {
        // 注册自定义消息附件解析器（可选）

    	// 注册各种扩展消息类型的显示ViewHolder（可选）

  		// 设置会话中点击事件响应处理（一般需要）
    }
```

需要在AndroidManifest.xml中导入下面Activity声明，这些Activity使用的theme可在demo源码包中values/styles.xml中找到。

```
    <!-- 聊天窗口 -->
    <!-- UI组件中包含了语音选文字功能，该界面是全屏显示，为了视觉上的美观，该界面主题ActionBar使用Overlay模式。
         如果开发者不需要该功能或效果，使用普通主题即可。 同时，还需要将message_activity.xml这个layout中的根节点的paddingTop去掉。 -->
    <activity
        android:name="com.netease.nim.uikit.session.activity.P2PMessageActivity"
        android:configChanges="keyboardHidden|orientation"
        android:screenOrientation="portrait"
        android:theme="@style/OverlayBaseActionBarTheme"
        android:windowSoftInputMode="stateHidden|adjustResize"/>
    <activity
        android:name="com.netease.nim.uikit.session.activity.TeamMessageActivity"
        android:configChanges="keyboardHidden|orientation"
        android:screenOrientation="portrait"
        android:theme="@style/OverlayBaseActionBarTheme"
        android:windowSoftInputMode="stateHidden|adjustResize"/>

    <!-- 照片选择 -->
    <activity
        android:name="com.netease.nim.uikit.session.activity.PickImageActivity"
        android:screenOrientation="portrait"/>
    <activity
        android:name="com.netease.nim.uikit.common.media.picker.activity.PickerAlbumActivity"
        android:screenOrientation="portrait"/>
    <activity
        android:name="com.netease.nim.uikit.common.media.picker.activity.PickerAlbumPreviewActivity"
        android:screenOrientation="portrait"/>
    <activity
        android:name="com.netease.nim.uikit.common.media.picker.activity.PreviewImageFromLocalActivity"
        android:configChanges="keyboardHidden|orientation"
        android:screenOrientation="portrait"
        android:windowSoftInputMode="stateHidden|adjustResize"/>
    <activity
        android:name="com.netease.nim.uikit.common.media.picker.activity.PreviewImageFromCameraActivity"
        android:configChanges="keyboardHidden|orientation"
        android:label="@string/input_panel_take"
        android:screenOrientation="portrait"
        android:windowSoftInputMode="stateHidden|adjustResize"/>

    <!-- 视频 -->
    <activity
        android:name="com.netease.nim.uikit.session.activity.CaptureVideoActivity"
        android:configChanges="keyboardHidden|orientation"
        android:screenOrientation="portrait"
        android:windowSoftInputMode="stateHidden|adjustResize"/>

    <activity
        android:name="com.netease.nim.uikit.session.activity.WatchVideoActivity"
        android:configChanges="keyboardHidden|orientation|screenSize"
        android:theme="@style/DarkOverlayActionBarTheme"
        android:label="@string/video_play"/>

    <!-- 查看大图 -->
    <activity
        android:name="com.netease.nim.uikit.session.activity.WatchMessagePictureActivity"
        android:configChanges="keyboardHidden"/>
```

`NimUIKit`是UIKit的接口，提供了UI组件的所有能力，上述初始化代码中的注册、设置等函数中均在`NimUIKit`中，下文将逐一介绍。

### 设置用户资料提供者

网易云信不托管用户资料数据，用户资料由第三方APP服务器自行管理，当UI组件显示需要用到用户资料（`UserInfo`）时，会通过`UserInfoProvider`来获取，开发者在初始化UIKit时必须设置用户资料提供者，需要实现：

- 根据用户账号返回用户资料，一般App的设计会先从本地缓存中获取，当本地缓存中没有时，则需要异步请求远程服务器，
当数据返回时需要通知UIKit刷新界面（重新载入用户资料数据），接口为NimUIKit.notifyUserInfoChanged(accounts);
- 返回用户默认头像资源ID，当本地缓存不存在时，先显示默认头像。
- 根据群ID返回群头像位图。

注意：第三方APP的用户资料类需要实现`UserInfo`接口，接口中需要提供用户账号，用户名(用户昵称)，用户头像位图，默认头像资源ID。

在demo中，头像采用的圆形剪切绘制。开发者可调用`HeadImageView#setMask`修改头像形状，demo中提供了`nim_portrait_mask_round`和`nim_portrait_mask_square`两种。

示例如下：

```
private UserInfoProvider infoProvider = new UserInfoProvider() {
    @Override
    public UserInfo getUserInfo(String account) {
        // 先从本地缓存中获取
        UserInfo user = ContactDataCache.getInstance().getUser(account);
        if (user == null) {
            // 若缓存中没有，则发起异步请求数据，回调返回时调用NimUIKit.notifyUserInfoChanged(accounts);来通知UI更新
            ContactDataCache.getInstance().getUserFromRemote(account);
        }

        return user;
    }

    @Override
    public int getDefaultIconResId() {
        return R.drawable.avatar_def;
    }

    @Override
    public Bitmap getTeamIcon(String tid) {
        Drawable drawable = getResources().getDrawable(R.drawable.avatar_group);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        return null;
    }
};
```

## 最近联系人列表

### 集成最近联系人列表

下面演示集成最近联系人列表`RecentContactsFragment`

```
public class SessionListFragment extends MainTabFragment {

    private RecentContactsFragment contactsFragment;

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addRecentContactsFragment();
    }

 	// 将最近联系人列表fragment动态集成进来。开发者也可以使用在xml中配置的方式静态集成。
    private void addRecentContactsFragment() {
        contactsFragment = new RecentContactsFragment();

        // 设置要集成联系人列表fragment的布局文件
        contactsFragment.setContainerId(R.id.messages_fragment);

        TActionBarActivity activity = (TActionBarActivity) getActivity();

        // 如果是activity从堆栈恢复，FM中已经存在恢复而来的fragment，此时会使用恢复来的，而new出来这个会被丢弃掉
        contactsFragment = (RecentContactsFragment) activity.addFragment(contactsFragment);

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
        });
    }

	// 用户资料变更监听器
    ContactDataCache.UserDataChangedObserver userDataChangedObserver = new ContactDataCache.UserDataChangedObserver() {
        @Override
        public void onUpdateUsers(List<User> users) {
            if (contactsFragment != null) {
                contactsFragment.notifyDataSetChanged(); // 刷新列表
            }
        }
    };

```

### 定制联系人列表

通过设置自定义事件回调函数`RecentContactsCallback`来定制，目前支持：
- 最近联系人列表数据加载完成的回调函数。
- 有未读数更新时的回调函数，供更新除最近联系人列表外的其他界面和未读指示。
- 最近联系人点击响应回调函数，以供打开会话窗口时传入定制化参数，或者做其他动作。
- 设置自定义消息的摘要消息，展示在最近联系人列表的消息缩略栏上。当然，你也可以自定义一些内建消息的缩略语，例如图片，语音，音视频会话等，自定义的缩略语会被优先使用。

## 聊天窗口

UI组件目前提供点对点聊天、群聊窗口。

### 集成聊天窗口

启动聊天窗口，需要指定回话类型（P2P/TEAM），传入账号（单聊个人账号/群号），如果需要定制，传入`SessionCustomization`，使用默认界面传null，例如：

```
NimUIKit.startChatting(context, account, SessionTypeEnum.P2P, null);

NimUIKit.startChatting(context, teamId, SessionTypeEnum.Team, getTeamCustomization());
```

### 定制聊天窗口

#### 定制聊天窗口

支持如下定制：

- 聊天窗口背景。优先使用uri，如果没有提供uri，使用color。如果没有color，使用默认。uri暂时支持以下格式：
  drawable: android.resource://包名/drawable/资源名
  assets: file:///android_asset/{asset文件路径}
  file: file:///文件绝对路径
- 加号展开后的按钮和动作，默认已包含图片，视频和地理位置。
- ActionBar右侧可定制按钮`OptionsButton`，默认为空。
- 如果`OptionsButton`的点击响应中需要startActivityForResult，可以处理onActivityResult，需要注意的是，由于加号中的Action的限制，RequestCode只能使用int的最低8位。
- UIKit内建了对贴图消息的输入和管理展示，并和emoji表情整合在了一起，但贴图消息的附件定义，开发者可以按需扩展。
示例如下：

```
private static SessionCustomization myP2pCustomization;
private static SessionCustomization getMyP2pCustomization() {
        if (myP2pCustomization == null) {
            myP2pCustomization = initBaseP2P();

            // 定制ActionBar右边的按钮，可以加多个
            ArrayList<SessionCustomization.OptionsButton> buttons = new ArrayList<>();
            SessionCustomization.OptionsButton cloudMsgButton = new SessionCustomization.OptionsButton() {
                @Override
                public void onClick(Context context, String sessionId) {
                    MessageHistoryActivity.start(context, sessionId, SessionTypeEnum.P2P); // 漫游消息查询
                }
            };
            cloudMsgButton.iconId = R.drawable.nim_ic_messge_history;

            buttons.add(cloudMsgButton);
            myP2pCustomization.buttons = buttons;
        }
        return myP2pCustomization;
    }

    private static SessionCustomization initBaseP2P() {
        SessionCustomization sessionCustomization = new SessionCustomization() {
            // 由于需要Activity Result， 所以重载该函数。
            @Override
            public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                if (requestCode == NormalTeamInfoActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                    String result = data.getStringExtra(NormalTeamInfoActivity.RESULT_EXTRA_REASON);
                    if (result == null) {
                        return;
                    }
                    if (result.equals(NormalTeamInfoActivity.RESULT_EXTRA_REASON_CREATE)) {
                        String tid = data.getStringExtra(NormalTeamInfoActivity.RESULT_EXTRA_DATA);
                        if (TextUtils.isEmpty(tid)) {
                            return;
                        }

                        startTeamSession(activity, tid);
                        activity.finish();
                    }
                }
            }

            @Override
            public MsgAttachment createStickerAttachment(String category, String item) {
                return new StickerAttachment(category, item);
            }
        };

        // 背景
        p2pCustomization.backgroundColor = Color.BLUE;
        p2pCustomization.backgroundUri = "file:///android_asset/xx/bk.jpg";
        p2pCustomization.backgroundUri = "file:///sdcard/Pictures/bk.png";
        p2pCustomization.backgroundUri = "android.resource://com.netease.nim.demo/drawable/bk"

        // 定制加号点开后可以包含的操作，默认已经有图片，视频等消息了
        ArrayList<BaseAction> actions = new ArrayList<>();
        actions.add(new AVChatAction(AVChatType.AUDIO));
        actions.add(new AVChatAction(AVChatType.VIDEO));
        actions.add(new SnapChatAction());
        actions.add(new GuessAction());
        actions.add(new FileAction());
        sessionCustomization.actions = actions;
        sessionCustomization.withSticker = true;

        return sessionCustomization;
    }
```

#### 注册自定义消息附件解析器

```
// 在Application初始化中注册自定义消息附件解析器
NIMClient.getService(MsgService.class).registerCustomAttachmentParser(new CustomAttachParser());

public class CustomAttachParser implements MsgAttachmentParser {

    private static final String KEY_TYPE = "type";
    private static final String KEY_DATA = "data";

    @Override
    public MsgAttachment parse(String json) {
        CustomAttachment attachment = null;
        try {
            JSONObject object = JSON.parseObject(json);
            int type = object.getInteger(KEY_TYPE);
            JSONObject data = object.getJSONObject(KEY_DATA);
            switch (type) {
                case CustomAttachmentType.Guess:
                    attachment = new GuessAttachment();
                    break;
                default:
                    attachment = new DefaultCustomAttachment();
                    break;
            }

            if (attachment != null) {
                attachment.fromJson(data);
            }
        } catch (Exception e) {
        }

        return attachment;
    }

    public static String packData(int type, JSONObject data) {
        JSONObject object = new JSONObject();
        object.put(KEY_TYPE, type);
        if (data != null) {
            object.put(KEY_DATA, data);
        }

        return object.toJSONString();
    }
}
```

#### 注册各种扩展消息类型的显示ViewHolder

当用户自定义消息时，可以根据消息附件类型注册对应的消息项展示ViewHolder

```
// 在Application初始化中注册
NimUIKit.registerMsgItemViewHolder(GuessAttachment.class, MsgViewHolderGuess.class);
NimUIKit.registerMsgItemViewHolder(FileAttachment.class, MsgViewHolderFile.class);
```

#### 设置会话中点击事件响应处理

会话窗口消息列表提供一些点击事件的响应处理函数，见`SessionEventListener`：

- 头像点击事件处理，一般用于打开用户资料页面
- 头像长按事件处理，一般用于群组@功能，或者弹出菜单，做拉黑，加好友等功能

```
SessionEventListener listener = new SessionEventListener() {
    @Override
    public void onPortraitClicked(Context context, IMMessage message) {
        // 一般用于打开用户资料页面
        UserProfileActivity.start(context, message.getFromAccount());
    }

    @Override
    public void onPortraitLongClicked(Context context, IMMessage message) {
        // 一般用于群组@功能，或者弹出菜单，做拉黑，加好友等功能
    }
};

// 在Application初始化中设置
NimUIKit.setSessionListener(listener);
```