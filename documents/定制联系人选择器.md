# 定制联系人选择器

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
NimUIKit.startContactSelector(NormalTeamInfoActivity.this, option, REQUEST_CODE_CONTACT_SELECT);
```

可以通过 `ContactSelectActivity.Option` 来定制，目前支持：

|参数|说明|
|:---|:---|
|type|联系人选择器中数据源类型：好友（默认）、群、群成员（需要设置teamId）。参考 ContactSelectType|
|teamId|联系人选择器数据源类型为群成员时，需要设置群号|
|title|联系人选择器标题|
|multi|联系人单选/多选（默认）|
|minSelectNum|至少选择人数|
|minSelectedTip|低于最少选择人数的提示|
|maxSelectNum|最大可选人数|
|maxSelectedTip|超过最大可选人数的提示|
|showContactSelectArea|是否显示已选头像区域|
|alreadySelectedAccounts|默认勾选（且可操作）的联系人项|
|itemFilter|需要过滤（不显示）的联系人项|
|itemDisableFilter|需要disable(可见但不可操作）的联系人项|
|searchVisible|是否支持搜索|
|allowSelectEmpty|允许不选任何人点击确定|
|maxSelectNumVisible|是否显示最大数目，结合maxSelectNum,与搜索位置相同|
