// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.qchat.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import androidx.fragment.app.Fragment;
import com.netease.lava.nertc.sdk.NERtcOption;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.yunxin.app.qchat.CustomConfig;
import com.netease.yunxin.app.qchat.R;
import com.netease.yunxin.app.qchat.databinding.ActivityMainBinding;
import com.netease.yunxin.app.qchat.main.mine.MineFragment;
import com.netease.yunxin.app.qchat.utils.Constant;
import com.netease.yunxin.app.qchat.utils.DataUtils;
import com.netease.yunxin.app.qchat.welcome.WelcomeActivity;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.alog.ParameterMap;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.contactkit.ui.contact.ContactFragment;
import com.netease.yunxin.kit.conversationkit.ui.page.ConversationFragment;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.qchat.QChatKitClient;
import com.netease.yunxin.kit.qchatkit.ui.server.QChatServerFragment;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCVideoCall;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.DefaultCallOrderImpl;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.state.CallState;
import com.netease.yunxin.nertc.nertcvideocall.utils.CallOrderHelper;
import com.netease.yunxin.nertc.nertcvideocall.utils.NrtcCallStatus;
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
  private ContactFragment mContactFragment;
  private ConversationFragment mConversationFragment;

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
    ALog.d(Constant.PROJECT_TAG, "MainActivity:initView");
    //    loadConfig();
    List<Fragment> fragments = new ArrayList<>();
    mConversationFragment = new ConversationFragment();
    fragments.add(mConversationFragment);
    QChatServerFragment qChatServerFragment = new QChatServerFragment();
    fragments.add(qChatServerFragment);
    //Contact
    mContactFragment = new ContactFragment();
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
  }

  @Override
  protected void onResume() {
    super.onResume();
    initContactFragment(mContactFragment);
    initConversationFragment(mConversationFragment);
  }

  @SuppressLint("UseCompatLoadingForDrawables")
  public void tabClick(View view) {

    if (mCurrentTab != null && mCurrentTab == view) {
      return;
    }
    resetTabStyle();
    mCurrentTab = view;
    if (mCurrentTab == activityMainBinding.contactBtnGroup) {
      activityMainBinding.viewPager.setCurrentItem(2, false);
      activityMainBinding.contact.setTextColor(getResources().getColor(R.color.tab_checked_color));
      activityMainBinding.contact.setCompoundDrawablesWithIntrinsicBounds(
          null, getResources().getDrawable(R.mipmap.ic_contact_tab_checked), null, null);
      changeStatusBarColor(R.color.color_white);
    } else if (mCurrentTab == activityMainBinding.myselfBtnGroup) {
      activityMainBinding.viewPager.setCurrentItem(3, false);
      activityMainBinding.mine.setTextColor(getResources().getColor(R.color.tab_checked_color));
      activityMainBinding.mine.setCompoundDrawablesWithIntrinsicBounds(
          null, getResources().getDrawable(R.mipmap.ic_mine_tab_checked), null, null);
      changeStatusBarColor(R.color.color_white);
    } else if (mCurrentTab == activityMainBinding.qchatBtnGroup) {
      activityMainBinding.viewPager.setCurrentItem(1, false);
      activityMainBinding.qchat.setTextColor(getResources().getColor(R.color.tab_checked_color));
      activityMainBinding.qchat.setCompoundDrawablesWithIntrinsicBounds(
          null, getResources().getDrawable(R.drawable.ic_qchat_checked), null, null);
      changeStatusBarColor(R.color.color_e9eff5);
    } else if (mCurrentTab == activityMainBinding.conversationBtnGroup) {
      activityMainBinding.viewPager.setCurrentItem(0, false);
      activityMainBinding.conversation.setTextColor(
          getResources().getColor(R.color.tab_checked_color));
      activityMainBinding.conversation.setCompoundDrawablesWithIntrinsicBounds(
          null, getResources().getDrawable(R.mipmap.ic_conversation_tab_checked), null, null);
      changeStatusBarColor(R.color.color_white);
    }
  }

  private void initConversationFragment(ConversationFragment conversationFragment) {
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

  private void initContactFragment(ContactFragment contactFragment) {
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

    activityMainBinding.qchat.setTextColor(getResources().getColor(R.color.tab_unchecked_color));
    activityMainBinding.qchat.setCompoundDrawablesWithIntrinsicBounds(
        null, getResources().getDrawable(R.drawable.ic_qchat_unchecked), null, null);
  }

  private void configCallKit() {
    CallKitUIOptions options =
        new CallKitUIOptions.Builder()
            // 必要：音视频通话 sdk appKey，用于通话中使用
            .rtcAppKey(DataUtils.readAppKey(this))
            // 必要：当前用户 AccId
            .currentUserAccId(QChatKitClient.account())
            // 通话接听成功的超时时间单位 毫秒，默认30s
            .timeOutMillisecond(30 * 1000L)
            // 此处为 收到来电时展示的 notification 相关配置，如图标，提示语等。
            .notificationConfigFetcher(
                invitedInfo -> {
                  UserInfo info = ChatRepo.getUserInfo(invitedInfo.invitor);
                  String content =
                      (info != null ? info.getUserInfoName() : invitedInfo.invitor)
                          + (invitedInfo.channelType == ChannelType.AUDIO.getValue()
                              ? getString(R.string.incoming_call_notify_audio)
                              : getString(R.string.incoming_call_notify_video));
                  ALog.e("=======" + content);
                  return new CallKitNotificationConfig(R.mipmap.ic_logo, null, null, content);
                })
            // 收到被叫时若 app 在后台，在恢复到前台时是否自动唤起被叫页面，默认为 true
            .resumeBGInvitation(true)
            // 请求 rtc token 服务，若非安全模式则不需设置(V1.8.0版本之前需要配置，V1.8.0及之后版本无需配置)
            //.rtcTokenService((uid, callback) -> requestRtcToken(appKey, uid, callback)) // 自己实现的 token 请求方法
            // 设置初始化 rtc sdk 相关配置，按照所需进行配置
            .rtcSdkOption(new NERtcOption())
            // 呼叫组件初始化 rtc 范围，true-全局初始化，false-每次通话进行初始化以及销毁
            // 全局初始化有助于更快进入首帧页面，当结合其他组件使用时存在rtc初始化冲突可设置false
            .rtcInitScope(false)
            .build();
    NERTCVideoCall.sharedInstance()
        .setCallOrderListener(
            new DefaultCallOrderImpl() {
              @Override
              public void onTimeout(ChannelType channelType, String accountId, int callType) {
                ALog.dApi(
                    "CallOrderImpl",
                    new ParameterMap("onTimeout")
                        .append("channelType", channelType)
                        .append("callType", callType)
                        .append("accountId", accountId)
                        .append("enableOrder", isEnable())
                        .toValue());
                if (!isEnable()) {
                  return;
                }
                if (NERTCVideoCall.sharedInstance().getCurrentState() == CallState.STATE_INVITED) {
                  return;
                }
                if (NetworkUtils.isConnected()) {
                  CallOrderHelper.sendOrder(
                      channelType, accountId, NrtcCallStatus.NrtcCallStatusTimeout, callType);
                } else {
                  CallOrderHelper.sendOrder(
                      channelType, accountId, NrtcCallStatus.NrtcCallStatusCanceled, callType);
                }
              }
            });
    // 若重复初始化会销毁之前的初始化实例，重新初始化
    CallKitUI.init(getApplicationContext(), options);
    NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(new Observer<StatusCode>() {
      @Override
      public void onEvent(StatusCode statusCode) {
        if (statusCode == StatusCode.LOGOUT){
          CallKitUI.destroy();
        }
      }
    },true);
  }

  private void loadConfig() {
    CustomConfig.configContactKit(this);
    CustomConfig.configConversation(this);
    CustomConfig.configChatKit(this);
  }
}
