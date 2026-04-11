# CommonUI ChangeLog

## v1.11.0( Apr 10, 2026)
* 跟随发版，依赖升级

## v1.10.0(Jan 2026)
* 'PickerView' 'CustomDatePicker' 支持设置默认选中颜色值

## v1.9.0(Oct 17, 2025)
* 跟随common发版

## v1.8.0(Sep 15, 2025)
* `BackTitleBar` 支持设置返回按钮定制能力UI

## v1.7.0(July 04, 2025)
* 跟随common发版

## v1.6.0(June 30, 2025)
### Bug Fixes
* 搜索匹配`EllipsizeUtils`中，搜索不到默认填充原内容

## v1.5.0(June 13, 2025)
### New Features
* 图片选择弹窗适配Android 13权限

## v1.4.0(Apr 27, 2025)
### New Features
* 编译java版本修改为1.8
* 新增`ScaleImageView` 支持图片放大缩小

## v1.3.5(July 16, 2024)
* 跟随发版

## v1.3.5(May 31, 2024)
### New Features
* `BackTitleBar`控件头像加载异常优化

## v1.3.4(Apr 19, 2024)
### New Features
* `BasePhotoChoiceDialog`中权限申请适配Android 13
* `FetchResult`新增构造函数
### Bug Fixes
* `ContactAvatarView`中`setData（）`增加非空判断，防止出现空指针

## v1.3.3(Jan 25, 2024)
### API Changes
* `BackTitleBar`中新增接口`getActionTextView（）`，用于获取右侧按钮

## v1.3.2(Dec 08, 2023)
* `BasePhotoChoiceDialog` 增加点击时的网络判断
* `FetchResult` 新增构造函数
* 修改断网场景下的网络提示语

## v1.3.1(Aug 11, 2023)
无

## v1.3.0(July 31, 2023)
### New Features
* Permission新增方法hasPermissions，用于判断是否有权限
* ToastUtils新增方法showShortToast，用于格式化字符串后再显示短时间的Toast
* ToastX新增方法showShortToast，用于格式化字符串后再显示短时间的Toast

### API Changes

## v1.2.0(Jul 6, 2023)
### New Features
* `BackTitleBar`新增`titleColor`属性支持
* `BackTitleBar`新增获取标题TextView方法`getCenterTitleTextView`
* `BaseFragment`支持修改状态栏颜色方法`changeStatusBarColor`
* `ContentListPopView`新增支持设置背景色方法`setBackground`
* 新增通用弹窗`BasePhotoChoiceDialog`
* `ViewHolderClickListener`中接口方法新增参数`View`
### Bug Fixes
* `ContactAvatarView`动态设置圆角不生效问题修复

## v1.1.19(May 12, 2023)
### New Features
* `CommonConfirmDialog`中增加可覆写方法`getLayout`，来修改默认布局

## v1.1.17(Apr 20, 2023)
### New Features
* `FetchResult`中增加扩展参数`extraInfo`

## v1.1.16(March 31, 2023)
## Added
* `LoadingDialog` 支持添加文本提示
* `IndexBar`添加拼音首字母快速选择组件

## v1.1.15(Feb 28, 2023)
### New Features
* `CleanableEditText`支持属性`maxLength`。
* `TransferHelperActivity`重复频繁启动过滤。
* `PhotoChoiceDialog`增加图片选择权限判断。
### Bug Fixes
* `BackTitleBar`机型适配（字体重叠）。
* `TransferHelperActivity`图片选择crash问题修复。

## v1.1.13(Dec 16, 2022)
无

## v1.1.12(Dec 07, 2022)
### Dependency Updates
*  `PhotoPicker`引用`CommonFileProvider`方法调整。

## v1.1.11(Dec 06, 2022)
无

## v1.1.10(Nov 23, 2022)
### Bug Fixes
* `PhotoPicker`拍照时本体图片从`CommonFileProvider`获取失败问题修复。

## v1.1.9(Oct 20, 2022)
### New Features
* 增加`PhotoChoiceDialog`和`PhtoPicker`提供图片选择器能力。
### API Changes
* `BackTitleBar`中提供更多获取View元素的方法，新增`getRightImageView()`、`getTitleTextView()`方法等

## v1.1.8(September 30, 2022)
### Bug Fixes
* 在`ToastUtils`方法中增加弹窗内容的非空判断。

## v1.1.7(September 13, 2022)
### New Features
* 增加`WaveView`。

## v1.1.6(Aug 31, 2022)
### New Features
* `CommonConfirmDialog`通用确认弹窗
### Behavior changes
* 资源拆分：Common 库中本身不需要的资源，的统一资源拆分到业务模块。

## v1.1.2(Jun 23, 2022)
### API Changes
* TitleBarView 中右侧第二个按钮的获取方法、设置图片方法、设置事件方法名称都将MiddleImageView
  变更为Right2ImageView。最右侧按钮的获取方法、设置图片、点击事件方法名称都将其中MoreImage改为RightImageView
* RoundFrameLayout 支持设置圆角大小，处理设置方法与XML配置之间的冲突
* ContactAvatarView 支持设置图片的圆角大小方法setCornerRadius()

## v1.1.1(June 07, 2022)
### API Changes
* MessageCommonBaseViewHolder移出到UI层
* ContactAvatarView头像加载setData中，增加图片加载失败，使用默认头像规则展示

## v1.0.7(May 19, 2022)
### API Changes
* 头部标题ICON替换，ic_yunxin.xml替换
* CommonUI初始化调整，新增CommonUIClient类，init实现中增加初始化判断。在依赖模块中进行初始化中的启动任务中初始化。

## v1.0.6(May 09, 2022)
### New Features
* 新增基础BaseActivity、BaseFragment基类
* 新增Web浏览Activity:BrowserActivity
* 新增通用弹窗