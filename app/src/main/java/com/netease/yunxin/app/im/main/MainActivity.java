// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.netease.lava.nertc.sdk.NERtcOption;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.v2.V2NIMError;
import com.netease.nimlib.sdk.v2.ai.model.V2NIMAIUser;
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginListener;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginClientChange;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginStatus;
import com.netease.nimlib.sdk.v2.auth.model.V2NIMKickedOfflineDetail;
import com.netease.nimlib.sdk.v2.auth.model.V2NIMLoginClient;
import com.netease.nimlib.sdk.v2.user.V2NIMUser;
import com.netease.yunxin.app.im.AppSkinConfig;
import com.netease.yunxin.app.im.BuildConfig;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityMainBinding;
import com.netease.yunxin.app.im.main.mine.MineFragment;
import com.netease.yunxin.app.im.utils.Constant;
import com.netease.yunxin.app.im.utils.DataUtils;
import com.netease.yunxin.app.im.utils.MultiLanguageUtils;
import com.netease.yunxin.app.im.welcome.WelcomeActivity;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.call.p2p.NECallEngine;
import com.netease.yunxin.kit.call.p2p.model.NECallInitRtcMode;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.manager.AIUserAgentProvider;
import com.netease.yunxin.kit.chatkit.manager.AIUserManager;
import com.netease.yunxin.kit.chatkit.repo.SettingRepo;
import com.netease.yunxin.kit.chatkit.ui.custom.ChatConfigManager;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.utils.AppLanguageConfig;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.contactkit.ui.contact.BaseContactFragment;
import com.netease.yunxin.kit.contactkit.ui.fun.contact.FunContactFragment;
import com.netease.yunxin.kit.contactkit.ui.normal.contact.ContactFragment;
import com.netease.yunxin.kit.conversationkit.local.ui.ILocalConversationViewLayout;
import com.netease.yunxin.kit.conversationkit.local.ui.LocalConversationKitClient;
import com.netease.yunxin.kit.conversationkit.local.ui.LocalConversationUIConfig;
import com.netease.yunxin.kit.conversationkit.local.ui.fun.page.FunLocalConversationFragment;
import com.netease.yunxin.kit.conversationkit.local.ui.normal.page.LocalConversationFragment;
import com.netease.yunxin.kit.conversationkit.local.ui.page.LocalConversationBaseFragment;
import com.netease.yunxin.kit.conversationkit.ui.ConversationKitClient;
import com.netease.yunxin.kit.conversationkit.ui.ConversationUIConfig;
import com.netease.yunxin.kit.conversationkit.ui.IConversationViewLayout;
import com.netease.yunxin.kit.conversationkit.ui.fun.page.FunConversationFragment;
import com.netease.yunxin.kit.conversationkit.ui.normal.page.ConversationFragment;
import com.netease.yunxin.kit.conversationkit.ui.page.ConversationBaseFragment;
import com.netease.yunxin.kit.corekit.event.BaseEvent;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.event.EventNotify;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.nertc.ui.CallKitNotificationConfig;
import com.netease.yunxin.nertc.ui.CallKitUI;
import com.netease.yunxin.nertc.ui.CallKitUIOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Demo 主页面 */
public class MainActivity extends BaseLocalActivity {

  private ActivityMainBinding activityMainBinding;
  // 当前页面index 的key
  private static final String CURRENT_INDEX = "currentIndex";
  // 会话列表小红点 的key
  private static final String CONVERSATION_POT = "conversationPOT";
  // 联系人列表小红点 的key
  private static final String CONTACT_POT = "contactPOT";
  // 底部导航栏的三个tab
  private static final int START_INDEX = 0;
  private static final int CONTACT_INDEX = 1;
  private static final int MINE_INDEX = 2;
  //当前tab的 Index
  private int currentIndex = START_INDEX;
  //有未读消息的会话列表
  private boolean haveUnreadConversation = false;
  //有未读消息的联系人列表
  private boolean haveUnreadContact = false;
  private View mCurrentTab;
  private BaseContactFragment mContactFragment;
  private LocalConversationBaseFragment mLocalConversationFragment;
  private ConversationBaseFragment mConversationFragment;

  // AI搜索数字人账号
  private static final String AI_SEARCH_USER_ACCOUNT = "search";

  // AI翻译数字人账号
  private static final String AI_TRANSLATION_USER_ACCOUNT = "translation";

  //皮肤变更事件，切换皮肤后重新加载页面
  EventNotify<SkinEvent> skinNotify =
      new EventNotify<SkinEvent>() {
        @Override
        public void onNotify(@NonNull SkinEvent message) {
          Intent intent = getIntent();
          finish();
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
          startActivity(intent);
        }

        @NonNull
        @Override
        public String getEventType() {
          return "skinEvent";
        }
      };

  //语音变更事件，切换语言后重新加载页面
  EventNotify<MultiLanguageUtils.LangEvent> langeNotify =
      new EventNotify<MultiLanguageUtils.LangEvent>() {
        @Override
        public void onNotify(@NonNull MultiLanguageUtils.LangEvent message) {
          recreate();
        }

        @NonNull
        @Override
        public String getEventType() {
          return "langEvent";
        }
      };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ALog.d(Constant.PROJECT_TAG, "MainActivity:onCreate");
    if (TextUtils.isEmpty(IMKitClient.account())) {
      Intent intent = new Intent(this, WelcomeActivity.class);
      startActivity(intent);
      finish();
      return;
    }
    if (savedInstanceState != null) {
      currentIndex = savedInstanceState.getInt(CURRENT_INDEX, START_INDEX);
      haveUnreadConversation = savedInstanceState.getBoolean(CONVERSATION_POT, false);
      haveUnreadContact = savedInstanceState.getBoolean(CONTACT_POT, false);
    }
    initLanguage();
    activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(activityMainBinding.getRoot());
    initView();
    initData();
    EventCenter.registerEventNotify(skinNotify);
    EventCenter.registerEventNotify(langeNotify);
    initContactFragment(mContactFragment);
    initConversationFragment();
  }

  private void initLanguage() {
    String language = AppLanguageConfig.getInstance().getAppLanguage(this);
    //设置应用语言
    Locale locale = new Locale(language);
    MultiLanguageUtils.setApplicationLocal(locale);
  }

  // 初始化数据
  private void initData() {
    // 是否展示已读未读状态开关
    SettingRepo.getShowReadStatus(
        new FetchCallback<Boolean>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {}

          @Override
          public void onSuccess(@Nullable Boolean param) {
            // 设置是否展示已读未读状态
            ChatConfigManager.showReadStatus = param;
          }
        });

    //设置功能数字人信息
    AIUserManager.setProvider(
        new AIUserAgentProvider() {

          @NonNull
          @Override
          public List<String> getAiTranslateLanguages(@NonNull List<? extends V2NIMAIUser> users) {
            return new ArrayList<>();
          }

          @Override
          public V2NIMAIUser getAiTranslateUser(@NonNull List<? extends V2NIMAIUser> users) {
            for (V2NIMAIUser user : users) {
              if (AI_TRANSLATION_USER_ACCOUNT.equals(user.getAccountId())) {
                return user;
              }
            }
            return null;
          }

          @Override
          public V2NIMAIUser getAiSearchUser(@NonNull List<? extends V2NIMAIUser> users) {
            for (V2NIMAIUser user : users) {
              if (AI_SEARCH_USER_ACCOUNT.equals(user.getAccountId())) {
                return user;
              }
            }
            return null;
          }
        });
  }

  @Override
  protected void onPostResume() {
    super.onPostResume();
    // 部分Android机型在页面进入onResume前启动其他页面会取消当前页面流程，避免组件初始化后立即展示来电页面将初始化的逻辑滞后
    if (!CallKitUI.INSTANCE.getInit()) {
      configCallKit();
    }
  }

  private void initView() {
    // 判断是否是通用皮肤
    boolean isCommonSkin =
        AppSkinConfig.getInstance().getAppSkinStyle() == AppSkinConfig.AppSkin.commonSkin;
    ALog.d(Constant.PROJECT_TAG, "MainActivity:initView currentIndex = " + currentIndex);
    List<Fragment> fragments = new ArrayList<>();
    boolean localConversation = DataUtils.getLocalConversationConfigSwitch(this);
    // 根据皮肤类型加载不同的Fragment
    if (isCommonSkin) {
      changeStatusBarColor(R.color.fun_page_bg_color);
      // 通用皮肤，使用FunConversationFragment和FunContactFragment
      if (localConversation) {
        mLocalConversationFragment = new FunLocalConversationFragment();
      } else {
        mConversationFragment = new FunConversationFragment();
      }
      mContactFragment = new FunContactFragment();

    } else {
      // 协同皮肤使用ConversationFragment和ContactFragment
      changeStatusBarColor(R.color.normal_page_bg_color);
      if (localConversation) {
        mLocalConversationFragment = new LocalConversationFragment();
      } else {
        mConversationFragment = new ConversationFragment();
      }
      mContactFragment = new ContactFragment();
    }
    loadCustomConfig();
    if (mConversationFragment != null) {
      fragments.add(mConversationFragment);
    } else {
      fragments.add(mLocalConversationFragment);
    }

    fragments.add(mContactFragment);

    fragments.add(new MineFragment());

    FragmentAdapter fragmentAdapter = new FragmentAdapter(this);
    fragmentAdapter.setFragmentList(fragments);
    activityMainBinding.viewPager.setUserInputEnabled(false);
    activityMainBinding.viewPager.setAdapter(fragmentAdapter);
    activityMainBinding.viewPager.setCurrentItem(currentIndex, false);
    activityMainBinding.viewPager.setOffscreenPageLimit(fragments.size());
    if (currentIndex == START_INDEX) {
      mCurrentTab = activityMainBinding.conversationBtnGroup;
    } else if (currentIndex == MINE_INDEX) {
      mCurrentTab = activityMainBinding.myselfBtnGroup;
    } else if (currentIndex == CONTACT_INDEX) {
      mCurrentTab = activityMainBinding.contactBtnGroup;
    }
    changeStatusBarColor(R.color.color_white);
    resetTabStyle();
    resetTabSkin(isCommonSkin);
    if (haveUnreadConversation) {
      activityMainBinding.conversationDot.setVisibility(View.VISIBLE);
    }
    if (haveUnreadContact) {
      activityMainBinding.contactDot.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    ALog.d(Constant.PROJECT_TAG, "MainActivity:onSaveInstanceState currentIndex = " + currentIndex);
    outState.putInt(CURRENT_INDEX, currentIndex);
    outState.putBoolean(CONVERSATION_POT, haveUnreadConversation);
    outState.putBoolean(CONTACT_POT, haveUnreadContact);
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (BuildConfig.DEBUG) {
      loadSettingConfig();
    }
  }

  @Override
  protected void onDestroy() {
    ALog.d(Constant.PROJECT_TAG, "MainActivity:onDestroy");
    EventCenter.unregisterEventNotify(skinNotify);
    EventCenter.unregisterEventNotify(langeNotify);
    super.onDestroy();
  }

  @SuppressLint("UseCompatLoadingForDrawables")
  public void tabClick(View view) {

    // 防止重复点击
    if (mCurrentTab != null && mCurrentTab == view) {
      return;
    }
    resetTabStyle();
    mCurrentTab = view;
    resetTabSkin(AppSkinConfig.getInstance().getAppSkinStyle() == AppSkinConfig.AppSkin.commonSkin);
  }

  @SuppressLint("UseCompatLoadingForDrawables")
  private void resetTabSkin(boolean isCommonSkin) {
    // 重置tab样式，设置选中的tab样式以及皮肤对应的图标
    if (mCurrentTab == activityMainBinding.contactBtnGroup) {
      currentIndex = CONTACT_INDEX;
      activityMainBinding.viewPager.setCurrentItem(1, false);
      if (isCommonSkin) {
        activityMainBinding.contact.setTextColor(
            getResources().getColor(R.color.fun_tab_checked_color));
        activityMainBinding.contact.setCompoundDrawablesWithIntrinsicBounds(
            null, getResources().getDrawable(R.mipmap.ic_contact_tab_checked_fun), null, null);
        changeStatusBarColor(R.color.color_ededed);
      } else {
        activityMainBinding.contact.setTextColor(
            getResources().getColor(R.color.tab_checked_color));
        activityMainBinding.contact.setCompoundDrawablesWithIntrinsicBounds(
            null, getResources().getDrawable(R.mipmap.ic_contact_tab_checked), null, null);
        changeStatusBarColor(R.color.color_white);
      }
    } else if (mCurrentTab == activityMainBinding.myselfBtnGroup) {
      currentIndex = MINE_INDEX;
      activityMainBinding.viewPager.setCurrentItem(2, false);
      if (isCommonSkin) {
        activityMainBinding.mine.setTextColor(
            getResources().getColor(R.color.fun_tab_checked_color));
        activityMainBinding.mine.setCompoundDrawablesWithIntrinsicBounds(
            null, getResources().getDrawable(R.mipmap.ic_mine_tab_checked_fun), null, null);
      } else {
        activityMainBinding.mine.setTextColor(getResources().getColor(R.color.tab_checked_color));
        activityMainBinding.mine.setCompoundDrawablesWithIntrinsicBounds(
            null, getResources().getDrawable(R.mipmap.ic_mine_tab_checked), null, null);
      }
      changeStatusBarColor(R.color.color_white);
    } else if (mCurrentTab == activityMainBinding.conversationBtnGroup) {
      currentIndex = START_INDEX;
      activityMainBinding.viewPager.setCurrentItem(0, false);
      if (isCommonSkin) {
        activityMainBinding.conversation.setTextColor(
            getResources().getColor(R.color.fun_tab_checked_color));
        activityMainBinding.conversation.setCompoundDrawablesWithIntrinsicBounds(
            null, getResources().getDrawable(R.mipmap.ic_conversation_tab_checked_fun), null, null);
        changeStatusBarColor(R.color.color_ededed);
      } else {
        activityMainBinding.conversation.setTextColor(
            getResources().getColor(R.color.tab_checked_color));
        activityMainBinding.conversation.setCompoundDrawablesWithIntrinsicBounds(
            null, getResources().getDrawable(R.mipmap.ic_conversation_tab_checked), null, null);
        changeStatusBarColor(R.color.color_white);
      }
    }
  }

  // 初始化会话Fragment，设置未读消息的回调接口，当未读数变更，就会在该接口回调
  private void initConversationFragment() {
    if (mConversationFragment != null) {
      mConversationFragment.setConversationCallback(
          count -> {
            ALog.d("mainActivity,initConversationFragment", "unread count:" + count);
            if (count > 0) {
              activityMainBinding.conversationDot.setVisibility(View.VISIBLE);
              haveUnreadConversation = true;
            } else {
              activityMainBinding.conversationDot.setVisibility(View.INVISIBLE);
              haveUnreadConversation = false;
            }
          });
    } else {
      mLocalConversationFragment.setConversationCallback(
          count -> {
            ALog.d("mainActivity,initConversationFragment", "unread count:" + count);
            if (count > 0) {
              activityMainBinding.conversationDot.setVisibility(View.VISIBLE);
              haveUnreadConversation = true;
            } else {
              activityMainBinding.conversationDot.setVisibility(View.INVISIBLE);
              haveUnreadConversation = false;
            }
          });
    }
  }

  // 初始化联系人Fragment，设置系统通知的回调接口，当未读数变更，就会在该接口回调
  private void initContactFragment(BaseContactFragment contactFragment) {
    if (contactFragment != null) {
      contactFragment.setContactCallback(
          count -> {
            if (count > 0) {
              activityMainBinding.contactDot.setVisibility(View.VISIBLE);
              haveUnreadContact = true;
            } else {
              activityMainBinding.contactDot.setVisibility(View.INVISIBLE);
              haveUnreadContact = false;
            }
          });
    }
  }

  @SuppressLint("UseCompatLoadingForDrawables")
  private void resetTabStyle() {

    activityMainBinding.conversation.setTextColor(
        getResources().getColor(R.color.tab_unchecked_color));
    activityMainBinding.conversation.setCompoundDrawablesWithIntrinsicBounds(
        null, getResources().getDrawable(R.mipmap.ic_conversation_tab_unchecked), null, null);

    activityMainBinding.contact.setTextColor(getResources().getColor(R.color.tab_unchecked_color));
    activityMainBinding.contact.setCompoundDrawablesWithIntrinsicBounds(
        null, getResources().getDrawable(R.mipmap.ic_contact_tab_unchecked), null, null);

    activityMainBinding.mine.setTextColor(getResources().getColor(R.color.tab_unchecked_color));
    activityMainBinding.mine.setCompoundDrawablesWithIntrinsicBounds(
        null, getResources().getDrawable(R.mipmap.ic_mine_tab_unchecked), null, null);
  }

  // 初始化音视频通话组件
  private void configCallKit() {

    CallKitUIOptions options =
        new CallKitUIOptions.Builder()
            // 必要：音视频通话 sdk appKey，用于通话中使用
            .rtcAppKey(DataUtils.readAppKey(this))
            // 通话接听成功的超时时间单位 毫秒，默认30s
            .timeOutMillisecond(30 * 1000L)
            // 此处为 收到来电时展示的 notification 相关配置，如图标，提示语等。
            .notificationConfigFetcher(
                invitedInfo -> {
                  V2NIMUser currentUser = IMKitClient.currentUser();
                  String content =
                      (currentUser != null ? currentUser.getName() : invitedInfo.callerAccId)
                          + (invitedInfo.callType == ChannelType.AUDIO.getValue()
                              ? getString(R.string.incoming_call_notify_audio)
                              : getString(R.string.incoming_call_notify_video));
                  ALog.d("=======" + content);
                  return new CallKitNotificationConfig(R.mipmap.ic_logo, null, null, content);
                })
            // 收到被叫时若 app 在后台，在恢复到前台时是否自动唤起被叫页面，默认为 true
            .resumeBGInvitation(true)
            // 请求 rtc token 服务，若非安全模式则不需设置(V1.8.0版本之前需要配置，V1.8.0及之后版本无需配置)
            //.rtcTokenService((uid, callback) -> requestRtcToken(appKey, uid, callback)) // 自己实现的 token 请求方法
            // 设置初始化 rtc sdk 相关配置，按照所需进行配置
            .rtcSdkOption(new NERtcOption())
            // 呼叫组件初始化 rtc 范围，NECallInitRtcMode.GLOBAL-全局初始化，
            // NECallInitRtcMode.IN_NEED-每次通话进行初始化以及销毁，全局初始化有助于更快进入首帧页面，
            // 当结合其他组件使用时存在rtc初始化冲突可设置NECallInitRtcMode.IN_NEED
            // 或当结合其他组件使用时存在rtc初始化冲突可设置NECallInitRtcMode.IN_NEED_DELAY_TO_ACCEPT
            .initRtcMode(NECallInitRtcMode.GLOBAL)
            .build();
    // 设置自定义话单消息发送
    NECallEngine.sharedInstance().setCallRecordProvider(new CustomCallOrderProvider());
    // 若重复初始化会销毁之前的初始化实例，重新初始化
    CallKitUI.init(getApplicationContext(), options);
    IMKitClient.addLoginListener(new V2NIMLoginListener() {
      @Override
      public void onLoginStatus(V2NIMLoginStatus status) {
        if (status == V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGOUT) {
          CallKitUI.destroy();
        }
      }

      @Override
      public void onLoginFailed(V2NIMError error) {

      }

      @Override
      public void onKickedOffline(V2NIMKickedOfflineDetail detail) {

      }

      @Override
      public void onLoginClientChanged(
              V2NIMLoginClientChange change,
              List<V2NIMLoginClient> clients
      ) {

      }
    });


  }

  // 加载配置，示例代码，CustomConfig展示如何通过接口来设置界面UI
  private void loadCustomConfig() {
    if (mLocalConversationFragment != null) {
      LocalConversationUIConfig conversationUIConfig = new LocalConversationUIConfig();
      conversationUIConfig.customLayout =
          new ILocalConversationViewLayout() {
            @Override
            public void customizeConversationLayout(LocalConversationBaseFragment fragment) {

              if (fragment instanceof LocalConversationFragment) {
                LocalConversationFragment conversationFragment =
                    (LocalConversationFragment) fragment;
                TextView textView = new TextView(conversationFragment.getContext());
                textView.setText(R.string.yunxin_tips);
                ViewGroup.LayoutParams layoutParams =
                    new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(50));
                conversationFragment.getBodyTopLayout().setBackgroundResource(R.color.color_FFF5E1);
                textView.setTextColor(
                    conversationFragment.getResources().getColor(R.color.color_EB9718));
                textView.setMaxLines(2);
                textView.setTextSize(13);
                textView.setLineSpacing(1, 1.2f);
                textView.setEllipsize(TextUtils.TruncateAt.END);
                textView.setPadding(
                    SizeUtils.dp2px(16),
                    SizeUtils.dp2px(6),
                    SizeUtils.dp2px(16),
                    SizeUtils.dp2px(4));
                conversationFragment.getBodyTopLayout().addView(textView, layoutParams);
              } else if (fragment instanceof FunLocalConversationFragment) {
                FunLocalConversationFragment conversationFragment =
                    (FunLocalConversationFragment) fragment;
                TextView textView = new TextView(conversationFragment.getContext());
                textView.setText(R.string.yunxin_tips);
                ViewGroup.LayoutParams layoutParams =
                    new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(50));
                conversationFragment.getBodyTopLayout().setBackgroundResource(R.color.color_FFF5E1);
                textView.setTextColor(
                    conversationFragment.getResources().getColor(R.color.color_EB9718));
                textView.setMaxLines(2);
                textView.setEllipsize(TextUtils.TruncateAt.END);
                textView.setTextSize(13);
                textView.setLineSpacing(1, 1.2f);
                textView.setPadding(
                    SizeUtils.dp2px(16),
                    SizeUtils.dp2px(6),
                    SizeUtils.dp2px(16),
                    SizeUtils.dp2px(4));
                conversationFragment.getBodyTopLayout().addView(textView, layoutParams);
              }
            }
          };
      LocalConversationKitClient.setConversationUIConfig(conversationUIConfig);
    } else {
      ConversationUIConfig conversationUIConfig = new ConversationUIConfig();
      conversationUIConfig.customLayout =
          new IConversationViewLayout() {
            @Override
            public void customizeConversationLayout(ConversationBaseFragment fragment) {

              if (fragment instanceof ConversationFragment) {
                ConversationFragment conversationFragment = (ConversationFragment) fragment;
                TextView textView = new TextView(conversationFragment.getContext());
                textView.setText(R.string.yunxin_tips);
                ViewGroup.LayoutParams layoutParams =
                    new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(50));
                conversationFragment.getBodyTopLayout().setBackgroundResource(R.color.color_FFF5E1);
                textView.setTextColor(
                    conversationFragment.getResources().getColor(R.color.color_EB9718));
                textView.setMaxLines(2);
                textView.setTextSize(13);
                textView.setLineSpacing(1, 1.2f);
                textView.setEllipsize(TextUtils.TruncateAt.END);
                textView.setPadding(
                    SizeUtils.dp2px(16),
                    SizeUtils.dp2px(6),
                    SizeUtils.dp2px(16),
                    SizeUtils.dp2px(4));
                conversationFragment.getBodyTopLayout().addView(textView, layoutParams);
              } else if (fragment instanceof FunConversationFragment) {
                FunConversationFragment conversationFragment = (FunConversationFragment) fragment;
                TextView textView = new TextView(conversationFragment.getContext());
                textView.setText(R.string.yunxin_tips);
                ViewGroup.LayoutParams layoutParams =
                    new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(50));
                conversationFragment.getBodyTopLayout().setBackgroundResource(R.color.color_FFF5E1);
                textView.setTextColor(
                    conversationFragment.getResources().getColor(R.color.color_EB9718));
                textView.setMaxLines(2);
                textView.setEllipsize(TextUtils.TruncateAt.END);
                textView.setTextSize(13);
                textView.setLineSpacing(1, 1.2f);
                textView.setPadding(
                    SizeUtils.dp2px(16),
                    SizeUtils.dp2px(6),
                    SizeUtils.dp2px(16),
                    SizeUtils.dp2px(4));
                conversationFragment.getBodyTopLayout().addView(textView, layoutParams);
              }
            }
          };
      ConversationKitClient.setConversationUIConfig(conversationUIConfig);
    }
  }

  private void loadSettingConfig() {
    SettingKitConfig config = DataUtils.getSettingKitConfig();
    IMKitConfigCenter.setEnableCollectionMessage(config.hasCollection);
    IMKitConfigCenter.setEnableTeam(config.hasTeam);
    IMKitConfigCenter.setEnableTopMessage(config.hasStickTopMsg);
    IMKitConfigCenter.setEnableOnlyFriendCall(config.hasStrangeCallLimit);
    IMKitConfigCenter.setEnablePinMessage(config.hasPin);
    IMKitConfigCenter.setEnableTypingStatus(config.hasOnlineStatus);
  }

  //皮肤变更事件
  public static class SkinEvent extends BaseEvent {
    @NonNull
    @Override
    public String getType() {
      return "skinEvent";
    }
  }
}
