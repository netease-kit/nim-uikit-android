# NimUIKit 接口

`NimUIKit` 是 `UIKit` 组件的能力输出类，以静态方法的形式提供，下面分别介绍各个方法的作用以及调用时机。

## 初始化

`UIKit` 在 SDK 完成初始化之后调用，注意判断一下进程，在主进程中调用初始化。一般地，直接调用 `NimUIKit` 下面的方法即可初始化 `UIKit`。

```java
public static void init(Context context);
```

此外，有如下重载方法，可以传递全局配置项 `UIKitOptions`，注意这个参数不要传null。

```java
public static void init(Context context, UIKitOptions option);
```

或者如下方法，可以设置用户信息提供者和联系人信息提供者，以及全局配置项 `UIKitOptions`。如果设置了 `userInfoProvider` 和 `contactProvider`，那么 `UIKit` 将会替代对应的默认数据提供者。

```java
public static void init(Context context, IUserInfoProvider userInfoProvider, ContactProvider contactProvider);

public static void init(Context context, UIKitOptions option, IUserInfoProvider userInfoProvider, ContactProvider contactProvider);
```

> 全局配置项 UIKitOptions 的参考文档：[Uikit全局配置项介绍](Uikit全局配置项介绍.md)

## 登陆IM、聊天室

- 登陆

`UIKit` 封装了云信 SDK 的登录接口，原因是 `UIKit` 在登陆成功后模块本身要做一些处理，例如构建数据缓存等等。开发者可以调用如下方法发起登陆即可。

```java
public static AbortableFuture<LoginInfo> login(LoginInfo loginInfo, final RequestCallback<LoginInfo> callback);
```

若 `APP` 直接调用 SDK 登陆方法，需在登陆成功之后调用 `NimUIKit` 下面方法通知 `UIKit` 。
```java
public static void loginSuccess(String account);
```

- 登出

`APP`调用 SDK 登出方法之后，需调用 `NimUIKit` 如下方法通知 `UIKit`：
```java
public static void logout(String account);
```

- 进入聊天室

进入聊天室成功之后，需调用 `NimUIKit` 如下方法通知 `UIKit`：
```java
public static void enterChatRoomSuccess(EnterChatRoomResultData data, boolean independent);
```

- 退出聊天室

退出聊天室之后，需调用 `NimUIKit` 如下方法通知 `UIKit`：
```java
public static void exitedChatRoom(String roomId);
```

## 数据请求接口 Provider

`UIKit` 提供了相对独立的功能模块，其与 `APP` 之间必然会有相同的请求业务数据需求，例如在 `UIKit` 与 `APP` 之间都会请求用户信息 `UserInfo`。`UIKit` 定义了一套数据接口，以 xxxProvider 的方式命名，这样双方通过同一套接口来访问，接口的实现细节只需要做一次。

`UIKit` 内部已经默认实现了部分接口，并且为了提升体验，构建了对应的数据缓存，例如用户资料，对于大部分开发者，使用了云信托管用户资料，则直接通过这套约定接口获取数据，避免了繁杂的 SDK 接口调用，降低了开发者接入的难度。此外，`UIKit` 也支持开发者自行管理数据，例如部分开发者用户数据或者联系人数据并没有使用云信托管，这就要求开发者必须实现对应的数据接口，并设置到 `UIKit` ，使 `UIKit` 内部也能够通过接口访问到数据。

- 各个数据接口的名称以及作用

|名称|作用| UIKit 默认实现|
|:---|:---|:---|
|IUserProvider|用户信息|DefaultUserInfoProvider|
|ContactProvider|联系人信息（好友关系）|DefaultContactProvider|
|TeamProvider|群、群成员信息|DefaultTeamProvider|
|RobotProvider|智能机器人信息|DefaultRobotProvider|
|ChatRoomProvider|聊天室成员信息|DefaultChatRoomProvider|
|LocationProvider|地理位置信息|无，由`App` 实现|
|OnlineStateContentProvider|在线状态事件|无，由`App` 实现|

- 数据接口调用：

开发者 `APP` 中统一通过 NimUIKit#getXXXProvider 获取 provider，再调用 provider 接口获取数据。
例如，获取用户信息 `UserInfo` API ：

```java
 public static IUserInfoProvider getUserInfoProvider();
```

示例：

```java
UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(account);
```

- 实现数据接口并设置到 `UIkit`

一般情况下，如上表所示，对于 `UIKit` 已经实现的数据接口，开发者无须再关注。其他情况下，如果需要自行管理数据，则应该首先实现对应数据的 provider 接口，其次设置到 `UIKit` 使其生效，对于`IUserProvider` 和 `ContactProvider` 是在 `UIKit` 初始化时传递，其他通过 NimUIKit#setXXXProvider 设置，例如

```java
public static void setTeamProvider(TeamProvider teamProvider);
```

## 数据变化通知接口

在一些业务场景中，例如好友列表，不仅要展示当前的好友，而且如果期间变更，则需要及时自动更新页面，因此，当前界面需要监听好友关系变化。`UIKit` 组件定义了一组数据变化监听接口，以 xxxObserver 命名，如下所示：

|名称|作用|
|:---|:---|
|UserInfoObserver|用户资料变更监听接口|
|ContactChangedObserver|联系人信息变更监听接口|
|TeamDataChangedObserver|群信息变更监听接口|
|TeamMemberDataChangedObserver|群成员信息变更监听接口|
|RoomMemberChangedObserver|聊天室成员信息变更监听接口|
|OnlineStateChangeObserver|在线状态事件变更监听接口|

- 注册、取消注册监听

示例：

```java

/**
 * 注册聊天室成员信息变更监听接口
 */
NimUIKit.getChatRoomMemberChangedObservable().registerObserver(observer, true);

/**
 * 注册群成员信息变更监听接口
 */
NimUIKit.getTeamChangedObservable().registerTeamMemberDataChangedObserver(observer, true);

/**
 * 注册群信息信息变更监听接口
 */
NimUIKit.getTeamChangedObservable().registerTeamDataChangedObserver(observer, true);

/**
 * 注册聊天室成员信息变更监听接口
 */
NimUIKit.getChatRoomMemberChangedObservable().registerObserver(observer, true);

/**
 * 注册在线状态事件变更监听接口
 */
NimUIKit.getOnlineStateChangeObservable().registerOnlineStateChangeListeners(observer, true)

```

- 通知数据变更

在上一节数据请求接口 Provider 中，如果开发者没有自定义 provider，则可以跳过此节。相反，如果开发者自定义实现 provider 接口，当对应数据发生变更，则应该主动通知观察者。

例如，在线状态事件，`UIKit` 没有实现 `OnlineStateContentProvider`，因此，如果开发者使用了改功能，则应该自行管理对应的数据，并在监听的 SDK 的事件变更通知之后，通知 `UIKit`：

```java
NimUIKit.getOnlineStateChangeObservable().notifyOnlineStateChange(accounts)
```

或者，如果开发者不使用云信托管用户资料和好友关系，当用户信息发生变更之后，通知

```java
NimUIKit.getUserInfoObservable().notifyUserInfoChanged(accounts)
```

当好友关系变更之后，通知给 `ContactChangedObserver`，如

```java
NimUIKit.getContactChangedObservable().notifyAddedOrUpdated(friendAccounts)
```

其他类似。

## 定制化 Customization

UIKit 组件提供了丰富的定制化接口，以 xxxCustomization 结尾，主要与最近会话列表、会话、联系人这三大 IM 主题业务相关。开发者在 `UIKit` 初始化完成之后设置。如果默认界面满足需求，开发者可以跳过此节。

|名称|作用|
|:---|:---|
|SessionCustomization|会话界面定制接口|
|RecentCustomization|最近会话列表定制接口|
|ContactsCustomization|联系人定制接口|
|ChatRoomSessionCustomization|聊天室会话界面定制接口|

- SessionCustomization

支持 IM 聊天界面三项定制，1. 聊天背景 2. 输入框加号展开后的按钮和动作 3. Toolbar右侧更多按钮。定制生效又分为 P2P聊天和群聊、全局生效和单个生效。

例如，调用 `NimUIKit` 设置全局群聊会话定制界面：
```java
public static void setCommonTeamSessionCustomization( commonTeamSessionCustomization);
```

调用 `NimUIKit` 设置全局点对点会话定制界面:
```java
public static void setCommonP2PSessionCustomization(commonP2PSessionCustomization);
```

以及在启动会话界面时，调用 `NimUIKit` 指定当前界面使用特殊定制:
```java
public static void startChatting(context, id, sessionType,
            sessionCustomization, anchorMessage)
```

- RecentCustomization

支持定制会话项文案，即最近会话列表里面每一行的消息文案。

```java
public static void setRecentCustomization(recentCustomization)
```

- ChatRoomSessionCustomization

支持 Chat Room 聊天界面自定义“输入框加号展开后的按钮和动作”。

```java
public static void setCommonChatRoomSessionCustomization( commonChatRoomSessionCustomization)
```