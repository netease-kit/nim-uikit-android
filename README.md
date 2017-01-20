# 云信 UI 组件使用说明

云信 UI 组件（`UIKit`）以Android library 工程的形式呈现，提供了构建 IM 功能所需的主要功能模块：聊天窗口、最近联系人列表、通讯录列表、联系人选择器、群名片，其他功能有：照片选择、查看大图、视频采集与播放。

`UIKit` 具有强大的 IM 界面组件能力，提供了简洁的接口使得开发者以最短的时间和成本即可完成丰富的 IM 功能。在此之上，`UIKit` 提供灵活自定义接口以满足开发者特定的需求。相比直接使用云信 SDK，开发者基于 `UIKit` 可以快速的实现聊天界面、最近联系人、通讯录等功能，并实现一些定制化开发。

云信的 UI 组件完全开源，如果开发者希望修改界面，只需要通过替换界面资源，修改 layout 等方式即可实现。如果开发者希望更深层次的自定义，也可自行修改代码。

## <span id="初始化 UI 组件"> 初始化 UI 组件 </span>

导入UIKit

- [Eclipse导入UIKit](http://note.youdao.com/share/?id=a8e904df99e1a114c5b565568a19906d&type=note  "target=_blank")
- [Android Studio导入UIKit](http://note.youdao.com/share/?id=66d12a2aa10b37928b869feaef54ec3e&type=note  "target=_blank")

并在 Application 中，在初始化云信 SDK 之后，初始化`UIKit`：

```java
NimUIKit.init(context);
```

> 请务必放在主进程中初始化，否则可能出现一些异常。

> `NimUIKit` 为 `UIKit` 能力输出类，即开发者只需与 `NimUIKit` 类交互即可完成所有的调用以及定制。

初始化示例：

```java
public class NimApplication extends Application {

    public void onCreate() {
		 // 初始化云信SDK
        NIMClient.init(this, loginInfo(), options());
        
        if (inMainProcess()) {
	         // 在主进程中初始化UI组件，判断所属进程方法请参见demo源码。
            initUiKit();
	     }
	 }

    private void initUiKit() {
    
    	 // 初始化
        NimUIKit.init(this);
        
        // 可选定制项
        // 注册定位信息提供者类（可选）,如果需要发送地理位置消息，必须提供。
        // demo中使用高德地图实现了该提供者，开发者可以根据自身需求，选用高德，百度，google等任意第三方地图和定位SDK。
        NimUIKit.setLocationProvider(new NimDemoLocationProvider());

        // 会话窗口的定制: 示例代码可详见demo源码中的SessionHelper类。
        // 1.注册自定义消息附件解析器（可选）
        // 2.注册各种扩展消息类型的显示ViewHolder（可选）
        // 3.设置会话中点击事件响应处理（一般需要）
        SessionHelper.init();

        // 通讯录列表定制：示例代码可详见demo源码中的ContactHelper类。
        // 1.定制通讯录列表中点击事响应处理（一般需要，UIKit 提供默认实现为点击进入聊天界面)
        ContactHelper.init();
    }
}
```

> 除了 `NimUIKit.init(this)` 是必须的以外，其他均为可选配置项。

`UIKit` 中用到的 `Activity` 已经在 `UIKit` 工程的 `AndroidManifest.xml` 文件中注册好，上层 APP 无需再去添加注册。除观看视频的 `WatchVideoActivity` 需要用到黑色主题，因此单独定义 `style` 外，其他 `Activity` 均使用项目默认主题。

同只使用 nimlib SDK 一样，需要参考接入云信 SDK 指南文档，在 `AndroidManifest.xml` 文件中声明云信 SDK 所用到的 `Service` 和 `BroadcastReceiver` 组件。

## <span id="手动登陆"> 手动登陆 </span>

开发者初始化 `UIKit` 之后，就可以在适当的时机调用登陆方法连接云信服务器，云信建议开发者首选自动登录，即在 SDK 初始化的时候传入登陆信息。

但需要注意的是，对于非多端在线系统，用户第一次登陆或者用户登录状态被其他端踢掉之后，必须进行手动登陆才能成功。下面是 `UIKit` 封装的手动登陆接口，开发者可以在 `callback` 处理登陆成功（失败）的逻辑，如保存登陆信息、跳转至会话列表界面等。

```java
NimUIKit.doLogin(loginInfo, callback);
```

手动登陆示例：

```java
loginRequest = NimUIKit.doLogin(new LoginInfo(account, token), new RequestCallback<LoginInfo>() {
    @Override
    public void onSuccess(LoginInfo param) {
        LogUtil.i(TAG, "login success");

        onLoginDone();

        DemoCache.setAccount(account);
        saveLoginInfo(account, token);

        // 初始化消息提醒配置
        initNotificationConfig();

        // 进入主界面
        MainActivity.start(LoginActivity.this, null);
        finish();
    }

    @Override
    public void onFailed(int code) {
        onLoginDone();
        if (code == 302 || code == 404) {
            Toast.makeText(LoginActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(LoginActivity.this, "登录失败: " + code, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onException(Throwable exception) {
        Toast.makeText(LoginActivity.this, R.string.login_exception, Toast.LENGTH_LONG).show();
        onLoginDone();
    }
});

```
## <span id="最近联系人列表"> 最近联系人列表 </span>

`UIKit` 中 `RecentContactsFragment` 以 `fragment` 方式实现了最近联系人列表的功能，开发者只需要将该 `fragment` 集成到自身的 `fragment` 或者 `Activity` 中即可。

`RecentContactsFragment` 实现了默认的列表点击事件处理，点击列表项将会直接跳转至默认的单聊或者群聊界面，若默认实现无法满足需求，开发者可以参照下文[定制最近联系人列表](#定制最近联系人列表)、[定制聊天窗口](#定制聊天窗口)来实现，详细可以参照云信 Demo。

### <span id="集成最近联系人列表"> 集成最近联系人列表 </span>

下面介绍静态集成和动态集成 `RecentContactsFragment` 两种方式。

- 使用 `xml` 布局的方式集成到 `Activity` 中，在界面的 layout 布局文件中添加

```xml
<fragment
    android:id="@+id/recent_contacts_fragment"
    android:name="com.netease.nim.uikit.recent.RecentContactsFragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
</fragment>
```

- 使用动态添加方式

```java
public class SessionListFragment extends MainTabFragment {

    private RecentContactsFragment contactsFragment;

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        addRecentContactsFragment();
    }

 	// 将最近联系人列表fragment动态集成进来。
 	private void addRecentContactsFragment() {
        contactsFragment = new RecentContactsFragment();

        // 设置要集成联系人列表fragment的布局文件
        contactsFragment.setContainerId(R.id.messages_fragment);

        TActionBarActivity activity = (TActionBarActivity) getActivity();

        // 如果是activity从堆栈恢复，FM中已经存在恢复而来的fragment，此时会使用恢复来的，而new出来这个会被丢弃掉
        contactsFragment = (RecentContactsFragment) activity.addFragment(contactsFragment);
    }
}
```

### <span id="定制最近联系人列表"> 定制最近联系人列表</span>

`UIKit` 实现了最近联系人列表界面的事件响应，如果无法满足需求，开发者也可以通过设置自定义事件回调函数 `RecentContactsCallback` 来定制，目前支持：

- 最近联系人列表数据加载完成的回调函数（默认不作处理）。
- 有未读数更新时的回调函数，供更新除最近联系人列表外的其他界面和未读指示（默认不作处理）。
- 最近联系人点击响应回调函数，以供打开会话窗口时传入定制化参数，或者做其他动作（默认跳转至聊天窗口）。
- 设置自定义消息的摘要消息，展示在最近联系人列表的消息缩略栏上。当然，你也可以自定义一些内建消息的缩略语，例如图片，语音，音视频会话等，自定义的缩略语会被优先使用。

如下是云信 Demo对最近联系人列表的定制

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

## <span id="聊天窗口"> 聊天窗口 </span>

`UIKit` 以 `Activity` 的形式提供点对点聊天、群聊窗口，开发者只需要一行代码即可启动默认的聊天窗口。

默认聊天窗口中，附件类型只有图片、视频、地理位置，界面顶部菜单为空，消息不支持转发、过滤等。若不能满足需求，开发者可以在参照下文[定制聊天窗口](#定制聊天窗口)。

### <span id="启动聊天窗口"> 启动聊天窗口 </span>

开发者调用如下两个方法即可直接启动单聊、群聊界面，传入 `Activity` 类型的`Context` 以及聊天对象的 `Account`。

```java
//启动单聊界面
NimUIKit.startP2PSession(context, account);

//启动群聊界面
NimUIKit.startTeamSession(context, account);
``` 

如果需要进入聊天窗口时中显示特定的消息位置，可以调用以下两个方法，新增消息对象参数作为列表显示的位置。

```java
//启动单聊界面
NimUIKit.startP2PSession(context, account, message);

//启动群聊界面
NimUIKit.startTeamSession(context, account, message);
```
### <span id="定制聊天窗口"> 定制聊天窗口 </span>

默认的聊天窗口已经能满足大部分的需求，考虑到开发者可能有更特殊的要求，`UIKit` 提供非常灵活的个性定制，包括聊天窗口界面样式、会话中点击事件、自定义扩展消息类型显示、消息撤回和转发过滤器等等。

#### 聊天窗口样式定制

聊天窗口界面样式定制依靠 `SessionCustomization` 实现，支持如下定制：

- 聊天窗口背景。优先使用 uri，如果没有提供 uri，使用 color。如果没有 color，使用默认。uri 暂时支持以下格式：
  drawable: android.resource://包名/drawable/资源名
  assets: file:///android_asset/{asset文件路径}
  file: file:///文件绝对路径
- 加号展开后的按钮和动作，默认已包含图片，视频和地理位置。
- ActionBar 右侧可定制按钮 `OptionsButton`，默认为空。
- 如果 `OptionsButton` 的点击响应中需要 startActivityForResult，可以处理 onActivityResult。需要注意的是，由于加号中的 Action 的限制，RequestCode 只能使用int的最低8位。
- UIKit 内建了对贴图消息的输入和管理展示，并和 emoji 表情整合在了一起，但贴图消息的附件定义，开发者可以按需扩展。
示例如下：

```java
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

    // 定制加号点开后可以包含的操作，默认已经有图片，视频等消息了，如果要去掉默认的操作，请修改MessageFragment的getActionList函数
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

接着，使用`UIKit` 提供的设置 `SessionCustomization` 的接口：

```java
// 设置单聊界面定制
NimUIKit.setCommonP2PSessionCustomization(commonP2PSessionCustomization);

// 设置群聊界面定制
NimUIKit.setCommonTeamSessionCustomization(commonTeamSessionCustomization);
```
设置之后将对全局生效，调用[启动聊天窗口](#启动聊天窗口)方法都会选择该定制项打开聊天窗口，若开发者需要对特殊的账号定制聊天窗口，可以使用如下的方法：

```java
// 启动单聊
NimUIKit.startChatting(context, account, SessionTypeEnum.P2P, sessionCustomization);

// 启动群聊
NimUIKit.startChatting(context, teamId, SessionTypeEnum.Team, sessionCustomization);
```

#### 注册自定义消息附件解析器

```java
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

当用户自定义消息时，可以根据消息附件类型注册对应的消息项展示 ViewHolder

```java
// 在Application初始化中注册
NimUIKit.registerMsgItemViewHolder(GuessAttachment.class, MsgViewHolderGuess.class);
NimUIKit.registerMsgItemViewHolder(FileAttachment.class, MsgViewHolderFile.class);
```

当用户使用 Tip 消息时，需要注册 Tip 消息项展示 ViewHolder

```java
// 在Application初始化中注册
NimUIKit.registerTipMsgViewHolder(MsgViewHolderTip.class);
```

#### 设置会话中点击事件响应处理

会话窗口消息列表提供一些点击事件的响应处理函数，见 `SessionEventListener`：

- 头像点击事件处理，一般用于打开用户资料页面
- 头像长按事件处理，一般用于群组@功能，或者弹出菜单，做拉黑，加好友等功能

```java
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

## <span id="通讯录列表"> 通讯录列表</span>

`UIKit` 提供的通讯录列表默认显示所有好友，提供字母导航，支持帐号、昵称搜索等。

通讯录列表默认不支持功能项（例如，折叠群、黑名单、消息验证、我的电脑等），开发者需要自己定制。默认点击响应为启动聊天窗口，若不能满足需求，开发者可以通过[定制通讯录列表](#定制通讯录列表)设置通讯录列表点击事件响应处理。

### <span id="集成通讯录列表"> 集成通讯录列表</span>

通讯录列表默认显示所有好友，与最近联系人列表类似，通讯录也以 `fragment` 的方式提供，下面演示集成通讯录 `ContactsFragment` :

- 静态集成

在 layout 布局文件中添加 `ContactsFragment` ：

```xml
<fragment
    android:id="@+id/contact_list_fragment"
    android:name="com.netease.nim.uikit.contact.ContactsFragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
</fragment>
```

- 动态集成

```java
public class ContactListFragment extends MainTabFragment {

    private ContactsFragment fragment;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 集成通讯录页面
        addContactFragment();
    }

    // 将通讯录列表fragment动态集成进来。 开发者也可以使用在xml中配置的方式静态集成。
    private void addContactFragment() {
        fragment = new ContactsFragment();
        fragment.setContainerId(R.id.contact_fragment);

        TActionBarActivity activity = (TActionBarActivity) getActivity();

        // 如果是activity从堆栈恢复，FM中已经存在恢复而来的fragment，此时会使用恢复来的，而new出来这个会被丢弃掉
        fragment = (ContactsFragment) activity.addFragment(fragment);
    }
```

### <span id="定制通讯录列表"> 定制通讯录列表</span>

#### 设置通讯录列表点击事件响应处理

通讯录列表提供点击事件的响应处理函数，见 `ContactEventListener` ：

- 通讯录联系人项点击事件处理
- 通讯录联系人项长按事件处理，一般弹出菜单：移除好友、添加到星标好友等
- 联系人头像点击相应

```java
// 在Application初始化中设置
NimUIKit.setContactEventListener(new ContactEventListener() {
    @Override
    public void onItemClick(Context context, String account) {
    	 // 进入个人资料页，开发者自行实现
        UserProfileActivity.start(context, account);
    }

    @Override
    public void onItemLongClick(Context context, String account) {

    }

    @Override
    public void onAvatarClick(Context context, String account) {
    	 // 进入个人资料页，开发者自行实现
        UserProfileActivity.start(context, account);
    }
});
```

#### 定制通讯录列表功能项

定制通讯录列表功能项依靠 `ContactsCustomization` 实现，例如折叠群、黑名单、消息验证、我的电脑等。

首先定义功能项 `FuncItem`，示例：

```java
public final static class FuncItem extends AbsContactItem {
    static final FuncItem NORMAL_TEAM = new FuncItem();
    static final FuncItem BLACK_LIST = new FuncItem();

    @Override
    public int getItemType() {
        return ItemTypes.FUNC;
    }

    @Override
    public String belongsGroup() {
        return null;
    }

    public static final class FuncViewHolder extends AbsContactViewHolder<FuncItem> {
        private ImageView image;
        private TextView funcName;
        private TextView unreadNum;

        @Override
        public View inflate(LayoutInflater inflater) {
            View view = inflater.inflate(R.layout.func_contacts_item, null);
            this.image = (ImageView) view.findViewById(R.id.img_head);
            this.funcName = (TextView) view.findViewById(R.id.tv_func_name);
            this.unreadNum = (TextView) view.findViewById(R.id.tab_new_msg_label);

            return view;
        }

        @Override
        public void refresh(ContactDataAdapter contactAdapter, int position, FuncItem item) {
            if (item == NORMAL_TEAM) {
                funcName.setText("讨论组");
                image.setImageResource(R.drawable.ic_secretary);
            } else if (item == BLACK_LIST) {
                funcName.setText("黑名单");
                image.setImageResource(R.drawable.ic_black_list);
            }
        }
    }

    static List<AbsContactItem> provide() {
        List<AbsContactItem> items = new ArrayList<AbsContactItem>();
        items.add(NORMAL_TEAM);
        items.add(BLACK_LIST);

        return items;
    }

    static void handle(Context context, AbsContactItem item) {
        if (item == NORMAL_TEAM) {
            TeamListActivity.start(context, ItemTypes.TEAMS.NORMAL_TEAM);
        } else if (item == BLACK_LIST) {
            BlackListActivity.start(context);
        }
    }
}
```
然后添加功能项到 `ContactsCustomization`：

```java
// 功能项定制
fragment.setContactsCustomization(new ContactsCustomization() {
    @Override
    public Class<? extends AbsContactViewHolder<? extends AbsContactItem>> onGetFuncViewHolderClass() {
        return FuncItem.FuncViewHolder.class;
    }

    @Override
    public List<AbsContactItem> onGetFuncItems() {
        return FuncItem.provide();
    }

    @Override
    public void onFuncItemClick(AbsContactItem item) {
        FuncItem.handle(getActivity(), item);
    }
});

```

## <span id="联系人选择器"> 联系人选择器</span>

### <span id="打开联系人选择器"> 打开联系人选择器</span>

在创建群、邀请群成员、消息转发等场景经常需要使用到联系人选择器，联系人选择器中的默认的联系人是你的好友。启动联系人选择器时可以传入可选参数 `ContactSelectActivity.Option` 来做联系人过滤、默认选中、多选等操作。例如：

```java
ContactSelectActivity.Option option = new ContactSelectActivity.Option();

// 设置联系人选择器标题
option.title = "邀请群成员";

// 设置可见但不可操作的联系人
ArrayList<String> disableAccounts = new ArrayList<>();
disableAccounts.addAll(memberAccounts);
option.itemDisableFilter = new ContactIdFilter(disableAccounts);

// 限制最大可选人数及超限提示
int capacity = teamCapacity - memberAccounts.size();
option.maxSelectNum = capacity;
option.maxSelectedTip = getString(R.string.reach_team_member_capacity, teamCapacity);

// 打开联系人选择器
NimUIKit.startContactSelect(NormalTeamInfoActivity.this, option, REQUEST_CODE_CONTACT_SELECT);
```

### <span id="定制联系人选择器"> 定制联系人选择器</span>

可以通过 `ContactSelectActivity.Option` 来定制，目前支持：

- 设置联系人选择器中数据源类型：好友（默认）、群、群成员（需要设置teamId）
- 设置联系人选择器标题
- 联系人单选、多选切换
- 限制至少选择人数、最多选择人数，设置超限文本提示
- 已选头像区域显示或隐藏
- 默认勾选指定的联系人
- 不显示指定的联系人
- 指定联系人可见但不可操作
- 帐号、昵称搜索
- 允许不选任何人点击确定

## <span id="群名片"> 群名片</span>

### <span id="打开讨论组或高级群资料页"> 打开讨论组或高级群资料页</span>

创建讨论组或高级群后，可以查看群资料，进行群消息提醒设置，群名称设置等操作。只需要传入参数：上下文和群id就可以打开相关的群资料页面。具体代码示例如下：

```java
// 例如在定制 actionbar 右上角按钮时，点击打开群资料页面。
SessionCustomization.OptionsButton infoButton = new SessionCustomization.OptionsButton() {
    @Override
    public void onClick(Context context, View view, String sessionId) {
        NimUIKit.startTeamInfo(context, sessionId);
    }
};
```
