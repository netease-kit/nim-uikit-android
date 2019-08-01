# 全局配置项 UIKitOptions

`UIKit` 组件提供了全局配置类 `UIKitOptions` ，初始化 `UIKit`时传入 `UIKitOptions` 对象，如果没有配置需求，则直接使用默认。

`UIKitOptions` 属性介绍：

|类型|UIKitOptions 属性|说明|默认
|:---|:---|:---|:---|
|String|appCacheDir|保存图片/语音/文件/log等数据缓存的目录|/sdcard/{packageName}/
|boolean|aitEnable|是否开启@功能|true
|boolean|aitTeamMember|是否支持@群成员|true
|boolean|aitIMRobot|是否在 IM 聊天中@机器人|true
|boolean|aitChatRoomRobot|是否在聊天室中@机器人|true
|boolean|initAsync|是否使用异步方式初始化UIKit|false
|boolean|buildNimUserCache|是否使用云信托管账号体系，构建缓存|true
|boolean|buildTeamCache|是否构建群缓存|true
|boolean|buildFriendCache|构建群好友关系缓存|true
|boolean|buildRobotInfoCache|构建智能机器人缓存|true
|boolean|buildChatRoomMemberCache|构建聊天室成员缓存|true
|long|displayMsgTimeWithInterval|消息列表每隔多久显示一条消息时间信息|5分钟
|int|messageCountLoadOnce|单次抓取消息条数配置|20
|int|messageLeftBackground|IM 接收到的消息时，内容区域背景的drawable id|R.drawable.nim_message_item_left_selector
|int|messageRightBackground|IM 发送出去消息时，内容区域背景的drawable id|R.drawable.nim_message_item_right_selector
|int|chatRoomMsgLeftBackground|聊天室接收到的消息时，内容区域背景的drawable id|0
|int|chatRoomMsgRightBackground|聊天室发送消息时，内容区域背景的drawableid|0
|boolean|shouldHandleReceipt|全局是否使用消息已读|true
|int|maxInputTextLength|消息文本输入框最大输入字符数目|5000
|RecordType|audioRecordType|录音类型|RecordType.AAC
|int|audioRecordMaxTime|录音时长限制，单位秒|120s
|boolean|disableAudioPlayedStatusIcon|不显示语音消息未读红点|false
|boolean|disableAutoPlayNextAudio|禁止音频轮播|false
