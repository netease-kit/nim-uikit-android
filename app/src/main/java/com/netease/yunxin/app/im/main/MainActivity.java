// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.netease.lava.nertc.sdk.NERtcOption;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.yunxin.app.im.AppSkinConfig;
import com.netease.yunxin.app.im.CustomConfig;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityMainBinding;
import com.netease.yunxin.app.im.main.mine.MineFragment;
import com.netease.yunxin.app.im.utils.Constant;
import com.netease.yunxin.app.im.utils.DataUtils;
import com.netease.yunxin.app.im.welcome.WelcomeActivity;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.call.p2p.NECallEngine;
import com.netease.yunxin.kit.call.p2p.model.NECallInitRtcMode;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.chatkit.ui.custom.ChatConfigManager;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.contactkit.ui.contact.BaseContactFragment;
import com.netease.yunxin.kit.contactkit.ui.fun.contact.FunContactFragment;
import com.netease.yunxin.kit.contactkit.ui.normal.contact.ContactFragment;
import com.netease.yunxin.kit.conversationkit.ui.fun.page.FunConversationFragment;
import com.netease.yunxin.kit.conversationkit.ui.normal.page.ConversationFragment;
import com.netease.yunxin.kit.conversationkit.ui.page.ConversationBaseFragment;
import com.netease.yunxin.kit.corekit.event.BaseEvent;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.event.EventNotify;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.repo.SettingRepo;
import com.netease.yunxin.nertc.ui.CallKitNotificationConfig;
import com.netease.yunxin.nertc.ui.CallKitUI;
import com.netease.yunxin.nertc.ui.CallKitUIOptions;
import java.util.ArrayList;
import java.util.List;

/** IM Main Page include four tab , message/contact/live/profile */
public class MainActivity extends BaseActivity {

  private ActivityMainBinding activityMainBinding;
  private static final int START_INDEX = 0;
  private View mCurrentTab;
  private BaseContactFragment mContactFragment;
  private ConversationBaseFragment mConversationFragment;

  //皮肤变更事件
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
    activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(activityMainBinding.getRoot());
    initView();
    initData();
    EventCenter.registerEventNotify(skinNotify);
  }

  private void initData() {
    SettingRepo.getShowReadStatus(
        new FetchCallback<Boolean>() {
          @Override
          public void onSuccess(@Nullable Boolean param) {
            ChatConfigManager.showReadStatus = param;
          }

          @Override
          public void onFailed(int code) {}

          @Override
          public void onException(@Nullable Throwable exception) {}
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
    boolean isCommonSkin =
        AppSkinConfig.getInstance().getAppSkinStyle() == AppSkinConfig.AppSkin.commonSkin;
    ALog.d(Constant.PROJECT_TAG, "MainActivity:initView");
    //    loadConfig();
    List<Fragment> fragments = new ArrayList<>();

    if (isCommonSkin) {
      changeStatusBarColor(R.color.fun_page_bg_color);
      mConversationFragment = new FunConversationFragment();
      mContactFragment = new FunContactFragment();

    } else {
      changeStatusBarColor(R.color.normal_page_bg_color);
      mConversationFragment = new ConversationFragment();
      mContactFragment = new ContactFragment();
    }

    fragments.add(mConversationFragment);
    fragments.add(mContactFragment);

    fragments.add(new MineFragment());

    FragmentAdapter fragmentAdapter = new FragmentAdapter(this);
    fragmentAdapter.setFragmentList(fragments);
    activityMainBinding.viewPager.setUserInputEnabled(false);
    activityMainBinding.viewPager.setAdapter(fragmentAdapter);
    activityMainBinding.viewPager.setCurrentItem(START_INDEX, false);
    activityMainBinding.viewPager.setOffscreenPageLimit(fragments.size());
    mCurrentTab = activityMainBinding.conversationBtnGroup;
    changeStatusBarColor(R.color.color_white);
    resetTabSkin(isCommonSkin);
  }

  @Override
  protected void onResume() {
    super.onResume();
    initContactFragment(mContactFragment);
    initConversationFragment(mConversationFragment);
  }

  @Override
  protected void onDestroy() {
    ALog.d(Constant.PROJECT_TAG, "MainActivity:onDestroy");
    EventCenter.unregisterEventNotify(skinNotify);
    super.onDestroy();
  }

  @SuppressLint("UseCompatLoadingForDrawables")
  public void tabClick(View view) {

    if (mCurrentTab != null && mCurrentTab == view) {
      return;
    }
    resetTabStyle();
    mCurrentTab = view;
    resetTabSkin(AppSkinConfig.getInstance().getAppSkinStyle() == AppSkinConfig.AppSkin.commonSkin);
  }

  @SuppressLint("UseCompatLoadingForDrawables")
  private void resetTabSkin(boolean isCommonSkin) {
    if (mCurrentTab == activityMainBinding.contactBtnGroup) {
      activityMainBinding.viewPager.setCurrentItem(1, false);
      if (isCommonSkin) {
        activityMainBinding.contact.setTextColor(
            getResources().getColor(R.color.fun_tab_checked_color));
        activityMainBinding.contact.setCompoundDrawablesWithIntrinsicBounds(
            null, getResources().getDrawable(R.mipmap.ic_contact_tab_checked_fun), null, null);
        changeStatusBarColor(R.color.fun_page_bg_color);
      } else {
        activityMainBinding.contact.setTextColor(
            getResources().getColor(R.color.tab_checked_color));
        activityMainBinding.contact.setCompoundDrawablesWithIntrinsicBounds(
            null, getResources().getDrawable(R.mipmap.ic_contact_tab_checked), null, null);
        changeStatusBarColor(R.color.color_white);
      }
    } else if (mCurrentTab == activityMainBinding.myselfBtnGroup) {
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
      activityMainBinding.viewPager.setCurrentItem(0, false);
      if (isCommonSkin) {
        activityMainBinding.conversation.setTextColor(
            getResources().getColor(R.color.fun_tab_checked_color));
        activityMainBinding.conversation.setCompoundDrawablesWithIntrinsicBounds(
            null, getResources().getDrawable(R.mipmap.ic_conversation_tab_checked_fun), null, null);
        changeStatusBarColor(R.color.fun_page_bg_color);
      } else {
        activityMainBinding.conversation.setTextColor(
            getResources().getColor(R.color.tab_checked_color));
        activityMainBinding.conversation.setCompoundDrawablesWithIntrinsicBounds(
            null, getResources().getDrawable(R.mipmap.ic_conversation_tab_checked), null, null);
        changeStatusBarColor(R.color.color_white);
      }
    }
  }

  private void initConversationFragment(ConversationBaseFragment conversationFragment) {
    if (conversationFragment != null) {
      conversationFragment.setConversationCallback(
          count -> {
            if (count > 0) {
              activityMainBinding.conversationDot.setVisibility(View.VISIBLE);
            } else {
              activityMainBinding.conversationDot.setVisibility(View.GONE);
            }
          });
    }
  }

  private void initContactFragment(BaseContactFragment contactFragment) {
    if (contactFragment != null) {
      contactFragment.setContactCallback(
          count -> {
            if (count > 0) {
              activityMainBinding.contactDot.setVisibility(View.VISIBLE);
            } else {
              activityMainBinding.contactDot.setVisibility(View.GONE);
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

  private void configCallKit() {

    CallKitUIOptions options =
            new CallKitUIOptions.Builder()
                    // 必要：音视频通话 sdk appKey，用于通话中使用
                    .rtcAppKey(DataUtils.readAppKey(this))
                    // 必要：当前用户 AccId
                    .currentUserAccId(IMKitClient.account())
                    // 通话接听成功的超时时间单位 毫秒，默认30s
                    .timeOutMillisecond(30 * 1000L)
                    // 此处为 收到来电时展示的 notification 相关配置，如图标，提示语等。
                    .notificationConfigFetcher(
                            invitedInfo -> {
                              UserInfo info = ContactRepo.getUserInfoFromLocal(invitedInfo.callerAccId);
                              String content =
                                      (info != null ? info.getUserInfoName() : invitedInfo.callerAccId)
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
    IMKitClient.getAuthServiceObserver().observeOnlineStatus(
            (Observer<StatusCode>) statusCode -> {
              if (statusCode == StatusCode.LOGOUT) {
                CallKitUI.destroy();
              }
            },
            true);
  }

  private void loadConfig() {
    CustomConfig.configContactKit(this);
    CustomConfig.configConversation(this);
    CustomConfig.configChatKit(this);
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
