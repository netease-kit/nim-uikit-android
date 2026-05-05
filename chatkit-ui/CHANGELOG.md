# ChatKitUI ChangeLog
## v10.9.25-beta01( May 5, 2026)
### New Features
* 文本消息翻译功能：支持手动翻译、自动翻译、译文展示与隐藏
* 翻译 @mention 保留：切分文本按段翻译，保留 @ 原文不翻译
* 译文长按菜单：支持复制译文内容、转发译文、隐藏译文

### Bug Fixes
* 翻译自测问题修复：译文显示、长按菜单交互问题修复

## v10.9.21( Apr 10, 2026)
### New Features
* 增加机器人缓存管理
* markdown 渲染新增图片插件支持（markwon.image）
* demo 增加扫码和机器人功能

### Bug Fixes
* 测试问题修复

## v10.9.10(Jan 2026)
### New Features
* 聊天记录搜索功能：支持文本、图片/视频、文件、按群成员、按日期多维度搜索
* 搜索结果文本高亮显示
* 图片/文件搜索分页加载增加 loading 效果
* 按群成员搜索增加"没有更多"提示
* 单聊增加搜索入口
* 娱乐版本新消息提醒功能
* 已读未读功能设置开关

### Bug Fixes
* 修复历史消息加载更多问题
* 修复本地搜索未展示加载更多及翻译资源缺失
* 修复收藏消息列表点击区域过小
* 修复文件搜索消息头像/昵称展示问题
* 修复换行消息展示及搜索跳转后返回展示异常
* 修复文件搜索页面加载更多
* 修复日历选择页面默认滚动位置
* 修复图片搜索排序、加载下一页不滚动
* 修复文本搜索高亮匹配问题
* 修复聊天页面多选时群信息按钮异常展示
* 修复搜索跳转聊天页面 PIN 信息未展示
* 修复合并转发使用好友备注名
* 修复新消息提醒在特定场景下重复触发或未消失
* 修复图片搜索 crash
* 修复搜索日期从 1970.01.01 查询失败
* 修复群成员搜索结果包含撤回消息
* 修复云端查询照片/视频不展示"没有更多"
* 修复微信版按群成员搜索历史记录

## v10.9.1(Nov 21, 2025)
### New Features
* 增加反垃圾命中，本地插入提示消息

## v10.8.7(Oct 17, 2025)
### New Features
* 音频消息播放控制开关
* 图片选择器插件

### Dependency Updates
* IM SDK 升级到10.9.52

## v10.8.5(Sep 15, 2025)
### New Features
* 消息支持链、电话、邮箱高亮
* 消息支持定制接受消息背景和发送消息背景

### Dependency Updates
* IM SDK 升级到10.9.45

## v10.8.2(June 30, 2025)
### New Features
* 新增入群申请和群邀请功能

## v10.8.1(June 13, 2025)
### New Features
* 新增用户在线状态订阅
* 新增单聊AI助聊

## v10.8.0(Apr 27, 2025)
### New Features
* 编译java版本修改为1.8
* 新增AI流式输出功能

## v10.6.2( Mar 26, 2025)
### New Features
* IMKitConfigCenter中enableLocalConversation参数删除，采用SDK参数

## v10.6.1( Feb 17, 2025)
### Bug Fixes
* 修复偶现地理位置消息不展示图片，高德地图API拼接优化
* 支持本地会话，需要通过配置中心配置本地会话。其他组件需要感知本地会话和云端回话，调用不同的接口

## v10.5.2( Jan 1, 2025)
### Bug Fixes
* 群成员拉取被移除的好友时，出现死循环

## v10.5.1( Dec 16, 2024)
### Bug Fixes
* 适配Android14 发送图片
* 修复发送视频不展示封面图片

## v10.5.0( Dec 9, 2024)
### New Features
* 语音转文字功能
* 动态语言切换功能
### Bug Fixes
* 消息撤回编辑时间问题修复
* 遗留问题修复
### Dependency Updates
* IM SDK 升级到10.6.0
* 呼叫组件升级到 3.0.0

## v10.3.2(Aug 30, 2024)
### Bug Fixes
* 修复消息回复闪烁问题
* 修复crash平台检测到附件下载失败的crash
* 修复通用版自定义消息注册接口失效问题

## v10.3.0(July 16, 2024)
### New Features
* AI数字人功能，AI聊、AI划词和AI翻译功能
### Bug Fixes
* 修复遗留问题
### Dependency Updates
* IM SDK 升级到10.3.0-beta

## v10.2.0(June 19, 2024)
### New Features
* 新增群聊会话中消息置顶功能
* 新增收藏功能
### Bug Fixes
* 修复遗留问题

## v10.1.0(May 31, 2024)
### New Features
* 新转发选择页面支持最近会话、最近转发、好友列表、群
### Bug Fixes
* 修复遗留问题
### Dependency Updates
* IM SDK 升级到10.2.6-beta

## v10.0.0-beta01(Apr 19, 2024)
### Dependency Updates
* IM SDK 升级到10.2.5-beta

## v9.7.0(Jan 08, 2024)
### New Features
* 新增合并转发功能
* 新增批量删除功能
* 新增批量删除功能
* 新增会话支持富文本消息发送和展示
* 新增PIN页面支持消息播放和查看

## v9.6.5(Dec 08, 2023)
### New Features
* 增加是否隐藏群的开关及群的相关入口处理。
### Bug Fixes
* 修复表情库和圈组冲突问题。
* 图片消息展示大小问题。

## v9.6.4(Nov 15, 2023)
### Bug Fixes
* `CoreKit-IM`修复`FriendProvider`中`getFriendList`方法中NEP问题。增加空判断。

## v9.6.3(Nov 02, 2023)
### Dependency Updates
* NIM SDK 升级到9.12.0
* 呼叫组件升级到2.1.2
## Bug Fixes
* 删除标记消息，其他接受端的标记状态也被取消
* 普通成员在讨论组设置页面，讨论组创建者退出讨论组后普通成员退出讨论组失败
* 单聊转发消息，选择好友列表无当前聊天对象
* 单聊设置页面创建讨论组不展示通知消息
* 修改群聊页面配置右侧按钮事件不生效
* 微信版删除被回复消息后页面消息体展示重叠
* 微信版转发弹窗人员头像是圆形
* 好友查询接口从本地查询，修改为先查询本地如果没有查询远端
* 置顶查询优化，一次性查询。（之前，单个查询）
* @消息增加空判断，防止不初始化aitService造成空指针异常

## v9.6.3-alpha01(July 31, 2023)
### Dependency Updates
* 适配common sdk 1.3.0版本新的网络回调接口
* ChatFragment相关的fragment提供getMessageBottomLayout()接口

## v9.6.0(Jul 6, 2023)
### New Features
* 新增微信版。
* Repo层接口整理，增加注释和废弃标记
* 新增拉后之后，消息发送失败提示
* 已读未读功能支持全局配置
## Bug Fixes
* @功能数据库删除线上Crash修复
* 文件大小计算调整统一
### Dependency Updates
* IM SDK 升级到9.11.0

## v9.5.3(Apr 24, 2023)
### Bug Fixes
* @功能，数据库查询@消息问题修复，查询条件采用字符串匹配。

## v9.5.0(Apr 20, 2023)
### New Features
* 支持@功能，相关内容高亮，回复消息填充@数据。
* 新增PIN消息列表，支持点击跳转到相关聊天消息。
* 新增自定义技术方案实现回复功能。
### Dependency Updates
* IM SDK 升级到9.10.0
### Bug Fixes
* 群通知文案统一。

## v9.4.1(Mar 17, 2023)
### Dependency Updates
* `CoreKit-IM`问题修复，升级到1.3.1版本，跟随发版

## v9.4.0(Feb 28, 2023)
### New Features
* 支持获取当前会话ID的接口。
* 增加音视频通话能力。
* 消息拉取采用 IM SDK9.8.0新的动态拉取接口，去除可信时间戳。
* 讨论组采用IM SDK高级群来实现。
### Dependency Updates
* IM SDK 升级到9.8.0
### Behavior changes
* 如果添加的好友在黑名单中，需要首先进行移除，然后添加好友。
### Bug Fixes
* 语音消息录制时间过短限制。
* 语音消息离开页面，停止播放。
* 定位未打开提示。
* 消息撤回编辑状态变更。

## v9.3.0(Dec 16, 2022)
### New Features
* 新增文件发送功能
* 新增位置发送功能

## v9.2.12(Dec 07, 2022)
### Bug Fixes
* 从聊天页面进入其他页面之后，消息接受异常修复

## v9.2.11(Nov 23,2022)
### Dependency Updates
* IM SDK 升级到9.6.4

## v9.2.10(Oct 20,2022)
### New Features
* 新增`IChatInputMenu`支持对聊天页面输入框按钮进行个性化配置，可通过`ChatUIConfig`进行配置。
* 新增`IChatPopMenu`支持对聊天页面消息长按菜单进行个性化配置，可通过`ChatUIConfig`进行配置。
* `ChatKitClient`中新增方法`addCustomAttach`、`addCustomViewHolder`方法完成自定义消息添加。
* `MessageProperties`中新增对头部标题的个性化定制能力。
### Bug Fixes
* 长按loading和发送失败消息，展示回复转发等功能。
* 发送某些视频消息对端接受失败，提示：云信IM视频下载失败。
* 发送1s语音消息撤回，撤回通知消息换行展示。
* 发送视频过程中断网重连后点击红色叹号重发提示视频下载失败。
* UI和文案展示优化。

## v9.2.9(September 1,2022)
### Bug Fixes
* 颜色资源color_ededef、color_dbdde4缺失

## v9.2.8(September 1,2022)
### Behavior changes
* 网络库依赖修改，网络判断依赖`NetworkUtils`。
* 图片获取FileProvider，依赖`IMKitFileProvider`。
* 未实现功能去除，包括：
    - 去除文件发送入口
    - 去除多选选项
    - 去除标记列表
    - 去除“我的”界面的收藏选项和收藏的入口
* 资源拆分：将依赖资源迁移到 UIKit 库中，不再依赖底层 Common 库的统一资源。
### API Changes
* 新增`MessageProperties`类，可通过该类下的`selfMessageRes`设置当前用户发送消息的背景资源 ID。
* 新增`MessageProperties`类，可通过该类下的`receiveMessageRes`设置当前用户接收消息的背景资源 ID。
### Bug Fixes
* 多语言英文文案更新。
* 修复人员选择器选中的人员头像颜色不一致的问题。
* 修复通知消息界面头像颜色不一致的问题。
* 修复转发消息昵称过长且展示未对齐的问题。
### Compatibility
* Compatible with `NIM` version `9.2.5`.

## 9.2.7(August 18,2022）
### API Changes
* 新增`CommonRepo`类，该类中包含用户数据获取方法`getUserInfo`方法和用户信息更新方法`updateUserInfo`。
* 在`ChatKit-ui`模块中，支持传入会话 ID 进入会话界面（包括群聊界面和单聊界面），传入参数的 KEY 值为`RouterConstant.CHAT_ID_KRY`。
* 增加国际化英文文案，跟随系统切换。
### Bug Fixes
* 修复黑名单中昵称展示问题。
* 修复@功能名称展示问题。
* 修复发送视频失败的提示文案。
* 修复权限申请逻辑问题。

## 9.2.0(June 23,2022)
### New Features
* 增加ChatUIConfig实体类，支持修改聊天页面中UI的个性化定制。
* 增加ChatKitClient提供设置ChatUIConfig接口，设置UI属性，在单聊和群聊页面都生效。
* 个性化定制相关View和Adapter修改。
### API Changes
* ChatMessageRepo 接口名称调整保持两端一致。
* ChatMessageRepo 接口名称增加中文注释。
## Dependency Updates
* IMSDK升级9.2.5。

## 9.0.2(June 7,2022)
### New Features
* 在ChatMessageBean中增加isSameMessage(ChatMessageBean bean)方法，判断消息体是否相同。
* 在ChatMessageAdapter中增加私有方法 removeSameMessage(List<ChatMessageBean> message)，去除重复消息。
* 新增SendMediaHelper类，从Common库中迁移。
* manifest中增加FileProvider配置。
### Behavior changes
* ChatBaseMessageViewHolder继承关系修改，由继承Common库中的MessageCommonBaseViewHolder，改为继承RecyclerView.ViewHolder，同时将MessageCommonBaseViewHolder能力在该类中进行实现。
* ChatAudioMessageViewHolder、ChatImageMessageViewHolder、ChatTextMessageViewHolder、ChatNotificationMessageViewHolder、ChatTipMessageViewHolder、ChatVideoMessageViewHolder进行参数使用调整。
* ChatMessageViewHolderFactory中ViewHolder的创建进行修改，需要先创建ChatBaseMessageViewHolderBinding对象。
* 在ChatMessageAdapter中方法appendMessages和forwardMessages中增加对removeSameMessage调用，消息列表增加消息时候进行去重。
### Bug Fixes
* 创建讨论组和高级群之后，进入讨论组或高级群的聊天页面，部分机型出现重复展示创建成功的提示信息。

## 9.0.1(May 19,2022)
### Behavior changes
* 去除单聊和群聊中视频发送大小20M限制 
* 启动任务中增加CommonUI的初始化，即在ChatUIService:create(@NonNull Context context)中增加CommonUIClient.init(context)。

## 9.0.0(May 9,2022)
### New Features
* 聊天模块增加单聊，支持图片、视频、文字和表情消息体。
* 聊天模块增加群聊，支持图片、视频、文字和表情消息体。

