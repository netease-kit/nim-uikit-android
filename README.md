云信 IM UIKit 是基于网易云信 IM SDK 开发的一款即时通讯 UI 组件库，包括聊天、会话、圈组、搜索、群管理等组件。通过 IM UIKit，可快速集成包含 UI 界面的即时通讯应用。

IM UIKit 简化了基于 NIM SDK 的应用开发过程。它不仅能助您快速实现 UI 功能，也支持调用 NIM SDK 相应的接口实现即时通讯业务逻辑和数据处理。因此，您在使用 IM UIKit
时仅需关注自身业务或个性化扩展。

IM UIKit 界面效果如下图所示：

<img src="https://yx-web-nosdn.netease.im/common/7ffe6a8afe28b48405b41fb3313d1fa2/uniapp.png" width="800" height="550" />
<br>
<img src="https://yx-web-nosdn.netease.im/common/895963a051a2ae1fae685cfd1682a6bf/%E9%80%9A%E8%AE%AF%E6%A8%A1%E5%9D%97%E4%B8%BB%E8%A6%81%E7%95%8C%E9%9D%A2.png" width="800" height="500" />

## 功能优势

优势 | 说明
---- | --------------
UI 组件解耦 | IM UIKit 不同组件可相互独立运行使用。您可按需选择组件，将其快速集成到您的应用，实现相应的 UI 功能，减少无用依赖。
UI 能力简洁易用 |IM UIKit 的业务逻辑层与 UI 层相互独立。在 UI 层，您仅需关注视图展示和事件处理。IM UIKit 清晰的数据流转处理，让 UI 层代码更简洁易懂。
强大的自定义能力 | IM UIKit 支持在各 UI 组件的初始化过程中配置自定义 UI。同时提供 Fragment 和 View 的能力封装，助您快速将 UI 功能添加到您的应用中。
完善的业务逻辑处理 | IM UIKit 业务逻辑层提供完善的业务逻辑处理能力。您无需关心 SDK 层不同接口间的复杂处理逻辑，业务逻辑层一个接口帮您搞定所有。

## 技术原理

### 工作原理

IM UIKit 采用 （Model–View–ViewModel）MVVM 架构模型，实现 UI 展示与业务逻辑开发的相互独立。

![IMuikitDataFlow_Android.png](https://yx-web-nosdn.netease.im/common/f1663a580335822a9770e486c3ea3e12/IMuikitDataFlow_Android.png)

流程 | 说明
---- | --------------
1 | IM UIKit 展示层的 Activity/Fragment/View 向响应层的 ViewModel 发送请求。
2 | ViewModel 将请求经由业务逻辑层转发至 NIM SDK（网易云信 IM SDK）。
3 | NIM SDK 接收请求后触发回调，回调数据经由业务逻辑层和响应层发送至 Activity/Fragment/View。
4 | Activity/Fragment/View 将回调数据发送至 RecyclerViewAdapter。后者根据界面需要展示的不同实体的 type，判定具体的 UI 样式。例如，SDK 返回的回调数据为消息数据时，RecyclerViewAdapter 可判定消息数据中包含的消息类型（即 type），将消息在 UI 上展示为对应类型的样式。

### 产品架构

![IMuikitArch.png](https://yx-web-nosdn.netease.im/common/4e67f1f8f355db7b8ea86ef8f9332011/IMuikitArch.png)

上图中：

- UIKit UI 层的 `ContactKit-ui`、`ChatKit-ui`、`ConversationKit-ui` 和 `QChatKit-ui`，对应上述工作原理图中的
  Activity/Fragment/View。
- UIKit UI 层的 `ContactKit`、`ChatKit` `ConversationKit` 和 `QChatKit`，对应上述工作原理图中的 Repository。
- UIKitCore 层对应上述工作原理图中的 Provider。

详见[IM UIKit介绍](https://doc.yunxin.163.com/messaging-uikit/concept/TI3NTgyNDA?platform=client)。

## IM UIKit 集成

具体的集成流程，请参见<a href="https://doc.yunxin.163.com/messaging-uikit/guide/DU4NzAzNzQ?platform=android" target="_blank">快速集成 IM UIKi</a>。