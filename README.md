# 网易云信 UI 组件 · Android

## <span id="简介">简介</span>

云信 UI 组件（`UIKit`）以 Android library 工程的形式呈现，提供了构建 IM 功能所需的主要功能模块：聊天窗口、最近联系人列表、通讯录列表、联系人选择器、群名片，其他功能有：照片选择、查看大图、视频采集与播放。

`UIKit` 具有强大的 IM 界面组件能力，提供了简洁的接口使得开发者以最短的时间和成本即可完成丰富的 IM 功能。在此之上，`UIKit` 提供灵活自定义接口以满足开发者特定的需求。相比直接使用云信 SDK，开发者基于 `UIKit` 可以快速的实现聊天界面、最近联系人、通讯录等功能，并实现一些定制化开发。

云信的 UI 组件完全开源，如果开发者希望修改界面，只需要通过替换界面资源，修改 layout 等方式即可实现。如果开发者希望更深层次的自定义，也可自行修改代码。

## <span id="架构解析">架构解析</span>

[架构解析](./documents/架构解析.md)

## <span id="集成说明">集成说明</span>

### 导入UIKit

- [Eclipse导入UIKit](http://note.youdao.com/share/?id=a8e904df99e1a114c5b565568a19906d&type=note  "target=_blank")
- [Android Studio导入UIKit](http://note.youdao.com/share/?id=66d12a2aa10b37928b869feaef54ec3e&type=note  "target=_blank")

### 初始化

在 Application 中，在初始化云信 SDK 之后，初始化`UIKit`：

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

		// 在线状态定制初始化。
        NimUIKit.setOnlineStateContentProvider(new DemoOnlineStateContentProvider());
    }
}
```

> 除了 `NimUIKit.init(this)` 是必须的以外，其他均为可选配置项。可参考[Uikit全局配置项介绍](./documents/Uikit全局配置项介绍.md)

`UIKit` 中用到的 `Activity` 已经在 `UIKit` 工程的 `AndroidManifest.xml` 文件中注册好，上层 APP 无需再去添加注册。除观看视频的 `WatchVideoActivity` 需要用到黑色主题，因此单独定义 `style` 外，其他 `Activity` 均使用项目默认主题。

同只使用 nimlib SDK 一样，需要参考接入云信 SDK 指南文档，在 `AndroidManifest.xml` 文件中声明云信 SDK 所用到的 `Service` 和 `BroadcastReceiver` 组件。

## <span id="快速使用">快速使用</span>

### <span id="手动登陆"> 手动登陆 </span>

开发者初始化 `UIKit` 之后，就可以在适当的时机调用登陆方法连接云信服务器，云信建议开发者首选自动登录，即在 SDK 初始化的时候传入登陆信息。

但需要注意的是，对于非多端在线系统，用户第一次登陆或者用户登录状态被其他端踢掉之后，必须进行手动登陆才能成功。下面是 `UIKit` 封装的手动登陆接口，开发者可以在 `callback` 处理登陆成功（失败）的逻辑，如保存登陆信息、跳转至会话列表界面等。

- API 原型

```java
/**
 * 手动登陆，由于手动登陆完成之后，UIKit 需要设置账号、构建缓存等，使用此方法登陆 UIKit 会将这部分逻辑处理好，开发者只需要处理自己的逻辑即可
 *
 * @param loginInfo 登陆账号信息
 * @param callback  登陆结果回调
 */
public static AbortableFuture<LoginInfo> login(LoginInfo loginInfo, final RequestCallback<LoginInfo> callback)；
```

手动登陆示例：

```java
loginRequest = NimUIKit.login(new LoginInfo(account, token), new RequestCallback<LoginInfo>() {
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

### <span id="聊天界面">聊天界面</span>

`UIKit` 以 `Activity` 的形式提供点对点聊天、群聊窗口，开发者只需要一行代码即可启动默认的聊天窗口。

#### <span id="单聊界面">单聊界面</span>

开发者调用如下两个方法即可直接启动单聊界面，传入 `Activity` 类型的`Context` 以及聊天对象的 `Account`。

```
// 打开单聊界面
NimUIKit.startP2PSession(context, account);
// 打开单聊界面，跳转到指定消息位置
NimUIKit.startP2PSession(context, account, anchor);
```

#### <span id="群聊界面">群聊界面</span>

开发者调用如下两个方法即可直接启动群聊界面，传入 `Activity` 类型的`Context` 以及聊天对象的 `teamId`。

```
// 打开群聊界面
NimUIKit.startTeamSession(context, teamId);

// 打开群里界面，跳转到指定消息位置
NimUIKit.startTeamSession(context, teamId, anchor);
```

### <span id="会话列表">会话列表</span>

UIKit 中 RecentContactsFragment 以 fragment 方式实现了最近联系人列表的功能，开发者只需要将该 fragment 集成到自身的 fragment 或者 Activity 中即可。

`RecentContactsFragment` 实现了默认的列表点击事件处理，点击列表项将会直接跳转至默认的单聊或者群聊界面。


#### <span id="静态集成">静态集成</span>

使用 `xml` 布局的方式集成到 `Activity` 中，在界面的 layout 布局文件中添加

```xml
<fragment
    android:id="@+id/recent_contacts_fragment"
    android:name="com.netease.nim.uikit.recent.RecentContactsFragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
</fragment>
```

#### <span id="动态集成">动态集成</span>

```java
public class SessionListFragment extends MainTabFragment {

    private RecentContactsFragment fragment;

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        addRecentContactsFragment();
    }

 	// 将最近联系人列表fragment动态集成进来。
 	private void addRecentContactsFragment() {
        fragment = new RecentContactsFragment();
        // 设置要集成联系人列表fragment的布局文件
        fragment.setContainerId(R.id.messages_fragment);

        final UI activity = (UI) getActivity();

        // 如果是activity从堆栈恢复，FM中已经存在恢复而来的fragment，此时会使用恢复来的，而new出来这个会被丢弃掉
        fragment = (RecentContactsFragment) activity.addFragment(fragment);
    }
}
```

### <span id="通讯录列表">通讯录列表</span>

`UIKit` 提供的通讯录列表默认显示所有好友，提供字母导航，支持帐号、昵称搜索等。列表默认点击响应为启动聊天窗口。

#### <span id="静态集成">静态集成</span>

在 layout 布局文件中添加 `ContactsFragment` ：

```xml
<fragment
    android:id="@+id/contact_list_fragment"
    android:name="com.netease.nim.uikit.contact.ContactsFragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
</fragment>
```

#### <span id="动态集成">动态集成</span>

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

        UI activity = (UI) getActivity();

        // 如果是activity从堆栈恢复，FM中已经存在恢复而来的fragment，此时会使用恢复来的，而new出来这个会被丢弃掉
        fragment = (ContactsFragment) activity.addFragment(fragment);
    }
```

## <span id="集成效果">集成效果</span>

| 最近会话进入会话                                 | 群组会话                                     | 发送多张图片                                   |
| ---------------------------------------- | ---------------------------------------- | ---------------------------------------- |
| ![image](https://github.com/netease-im/NIM_Resources/blob/master/Android/Images/recent_session.gif) | ![image](https://github.com/netease-im/NIM_Resources/blob/master/Android/Images/team.gif) | ![image](https://github.com/netease-im/NIM_Resources/blob/master/Android/Images/multiple_pic.gif) |

| 发送语音                                     | 发送地理位置                                   | 发送中与发送失败，点击叹号可重发                         |
| ---------------------------------------- | ---------------------------------------- | ---------------------------------------- |
| ![image](https://github.com/netease-im/NIM_Resources/blob/master/Android/Images/voice.gif) | ![image](https://github.com/netease-im/NIM_Resources/blob/master/Android/Images/location.gif) | ![image](https://github.com/netease-im/NIM_Resources/blob/master/Android/Images/sending_failed.gif) |

| 自定义消息-阅后即焚示例                             | 最近联系人选择器                                 | 最近会话删除与未读删除                              |
| ---------------------------------------- | ---------------------------------------- | ---------------------------------------- |
| ![image](https://github.com/netease-im/NIM_Resources/blob/master/Android/Images/snapchat.gif) | ![image](https://github.com/netease-im/NIM_Resources/blob/master/Android/Images/selector.gif) | ![image](https://github.com/netease-im/NIM_Resources/blob/master/Android/Images/recent_delete.gif) |


## <span id="深度定制">深度定制</span>

对于一般场景，`UIKit` 已经完全够用，使用当 `UIKit` 默认实现不足以满足需求，那么该如何进行定制化和扩展，这就需要开发者对其整体架构和构建思路有所了解。按照以下步骤，逐步掌握深度定制：

1\. [架构解析](./documents/架构解析.md)

2\. [NimUikit定制化接口介绍](./documents/NimUikit定制化接口介绍.md)

3\. [Uikit全局配置项介绍](./documents/Uikit全局配置项介绍.md)

4\. [定制聊天窗口](./documents/定制聊天窗口.md)

5\. [定制最近联系人列表](./documents/定制最近联系人列表.md)

6\. [定制联系人选择器](./documents/定制联系人选择器.md)

7\. [定制通讯录](./documents/定制通讯录.md)

8\. [自定义消息](./documents/自定义消息.md)

9\. [机器人消息](./documents/机器人消息排版.md)

## <span id="版本变更说明">版本变更说明</span>
详见 [升级指南](./documents/升级指南.md)