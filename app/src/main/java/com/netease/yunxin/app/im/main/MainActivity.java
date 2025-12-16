// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.netease.nimlib.coexist.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.coexist.sdk.msg.model.IMMessage;
import com.netease.nimlib.coexist.sdk.v2.V2NIMError;
import com.netease.nimlib.coexist.sdk.v2.ai.model.V2NIMAIUser;
import com.netease.nimlib.coexist.sdk.v2.auth.V2NIMLoginListener;
import com.netease.nimlib.coexist.sdk.v2.auth.enums.V2NIMLoginClientChange;
import com.netease.nimlib.coexist.sdk.v2.auth.enums.V2NIMLoginStatus;
import com.netease.nimlib.coexist.sdk.v2.auth.model.V2NIMKickedOfflineDetail;
import com.netease.nimlib.coexist.sdk.v2.auth.model.V2NIMLoginClient;
import com.netease.nimlib.coexist.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.coexist.sdk.v2.message.config.V2NIMMessagePushConfig;
import com.netease.nimlib.coexist.sdk.v2.message.params.V2NIMSendMessageParams;
import com.netease.nimlib.coexist.sdk.v2.user.V2NIMUser;
import com.netease.yunxin.app.im.AppConfig;
import com.netease.yunxin.app.im.AppSkinConfig;
import com.netease.yunxin.app.im.BuildConfig;
import com.netease.yunxin.app.im.MeetingConfig;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityMainBinding;
import com.netease.yunxin.app.im.main.mine.MineFragment;
import com.netease.yunxin.app.im.network.AIHelperAnswer;
import com.netease.yunxin.app.im.network.IMKitNetRequester;
import com.netease.yunxin.app.im.utils.Constant;
import com.netease.yunxin.app.im.utils.DataUtils;
import com.netease.yunxin.app.im.utils.MessageUtils;
import com.netease.yunxin.app.im.utils.MultiLanguageUtils;
import com.netease.yunxin.app.im.utils.ViewUtils;
import com.netease.yunxin.app.im.welcome.WelcomeActivity;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.IMKitCustomFactory;
import com.netease.yunxin.kit.chatkit.listener.MessageCallback;
import com.netease.yunxin.kit.chatkit.listener.MessageSendParams;
import com.netease.yunxin.kit.chatkit.manager.AIUserAgentProvider;
import com.netease.yunxin.kit.chatkit.manager.AIUserManager;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.repo.SettingRepo;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.ChatUIConfig;
import com.netease.yunxin.kit.chatkit.ui.IChatInputMenu;
import com.netease.yunxin.kit.chatkit.ui.builder.IChatViewCustom;
import com.netease.yunxin.kit.chatkit.ui.custom.ChatConfigManager;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IChatView;
import com.netease.yunxin.kit.chatkit.ui.normal.view.AIHelperView;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.chatkit.utils.ConversationIdUtils;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.utils.AppLanguageConfig;
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
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.coexist.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

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

  // 搜索数字人账号
  private static final String AI_SEARCH_USER_ACCOUNT = "search";
  private static final String NOTIFICATION_MESSAGE = "com.netease.nim.EXTRA.NOTIFY_CONTENT";
  private String aiHelperLastMsgClientId = null;
  private String aiHelperLastAccountId = null;

  // 翻译数字人账号
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
    loadSettingConfig();
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
    IMKitConfigCenter.setEnableAIStream(DataUtils.getAIStreamConfigSwitch(this));
    boolean pushConfigToggle = DataUtils.getPushConfigToggle(this);
    if (pushConfigToggle) {
      String serverConfig = DataUtils.getPushConfigContent(IMKitClient.getApplicationContext());
      V2NIMMessagePushConfig pushContent = MessageUtils.convertToPushConfig(serverConfig);
      IMKitCustomFactory.setPushConfig(pushContent);
    }

    IMKitCustomFactory.setMessageSendCallback(
        new MessageCallback() {
          @Override
          public MessageSendParams beforeSend(@NotNull MessageSendParams param) {
            V2NIMSendMessageParams sendMessageParams = param.getParams();
            //sendMessageParams如果为空，则创建一个新的V2NIMSendMessageParams对象。否则，使用sendMessageParams
            // 中的值重新创建一个新的V2NIMSendMessageParams对象，保留原值的同时修改pushConfig中的payload
            String conversationId = ChatRepo.getCurrentConversationId();
            if (!TextUtils.isEmpty(conversationId)) {

              V2NIMConversationType conversationType =
                  ConversationIdUtils.conversationType(conversationId);
              String type = "team";
              String accountId = ConversationIdUtils.conversationTargetId(conversationId);
              //根据V2NIMConversationType来判断，如果是P2P 则tepe值为"p2p"
              if (conversationType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
                type = "p2p";
                accountId = IMKitClient.account();
              }
              //将type值和conversationId的值放在Json中，然后转换为String复制给payload
              JSONObject jsonObject = new JSONObject();
              try {
                jsonObject.put("sessionType", type);
                jsonObject.put("sessionId", accountId);
              } catch (JSONException e) {
                e.printStackTrace();
              }
              V2NIMMessagePushConfig.V2NIMMessagePushConfigBuilder pushConfig =
                  V2NIMMessagePushConfig.V2NIMMessagePushConfigBuilder.builder();
              pushConfig.withPayload(jsonObject.toString());

              V2NIMSendMessageParams.V2NIMSendMessageParamsBuilder builder =
                  V2NIMSendMessageParams.V2NIMSendMessageParamsBuilder.builder();
              if (sendMessageParams != null) {
                builder.withAIConfig(sendMessageParams.getAIConfig());
                builder.withMessageConfig(sendMessageParams.getMessageConfig());
                builder.withAntispamConfig(sendMessageParams.getAntispamConfig());
                builder.withClientAntispamEnabled(sendMessageParams.isClientAntispamEnabled());
                builder.withRobotConfig(sendMessageParams.getRobotConfig());
                builder.withTargetConfig(sendMessageParams.getTargetConfig());
                builder.withClientAntispamReplace(sendMessageParams.getClientAntispamReplace());
                builder.withRouteConfig(sendMessageParams.getRouteConfig());
                if (sendMessageParams.getPushConfig() != null) {
                  pushConfig.withForcePush(sendMessageParams.getPushConfig().isForcePush());
                  pushConfig.withPushEnabled(sendMessageParams.getPushConfig().isPushEnabled());
                  pushConfig.withContent(sendMessageParams.getPushConfig().getPushContent());
                  pushConfig.withForcePushContent(
                      sendMessageParams.getPushConfig().getForcePushContent());
                  pushConfig.withForcePushAccountIds(
                      sendMessageParams.getPushConfig().getForcePushAccountIds());
                }
              }
              builder.withPushConfig(pushConfig.build());
              builder.withPushConfig(pushConfig.build());
              param.setParams(builder.build());
            }
            return param;
          }
        });
  }

  @Override
  protected void onPostResume() {
    super.onPostResume();
  }

  private void initView() {
    // 判断是否是通用皮肤
    boolean isCommonSkin =
        AppSkinConfig.getInstance().getAppSkinStyle() == AppSkinConfig.AppSkin.commonSkin;
    ALog.d(Constant.PROJECT_TAG, "MainActivity:initView currentIndex = " + currentIndex);
    List<Fragment> fragments = new ArrayList<>();
    boolean cloudConversation = DataUtils.getCloudConversationConfigSwitch(this);
    // 根据皮肤类型加载不同的Fragment
    if (isCommonSkin) {
      changeStatusBarColor(R.color.fun_page_bg_color);
      // 通用皮肤，使用FunConversationFragment和FunContactFragment
      if (cloudConversation) {
        mConversationFragment = new FunConversationFragment();
      } else {
        mLocalConversationFragment = new FunLocalConversationFragment();
      }
      mContactFragment = new FunContactFragment();

    } else {
      // 协同皮肤使用ConversationFragment和ContactFragment
      changeStatusBarColor(R.color.normal_page_bg_color);
      if (cloudConversation) {
        mConversationFragment = new ConversationFragment();
      } else {
        mLocalConversationFragment = new LocalConversationFragment();
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
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    // 在线通知点击逻辑处理
    parseAndGoChat(intent);
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


  private void backToLogin() {
    IMKitClient.logout(
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            MainActivity.this.startActivity(intent);
            MainActivity.this.finish();
          }

          @Override
          public void onSuccess(@Nullable Void data) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            MainActivity.this.startActivity(intent);
            MainActivity.this.finish();
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
              ViewUtils.addTipsView(fragment.getBodyTopLayout(), fragment.getContext(), false);
            }
          };
      LocalConversationKitClient.setConversationUIConfig(conversationUIConfig);
    } else {
      ConversationUIConfig conversationUIConfig = new ConversationUIConfig();
      conversationUIConfig.customLayout =
          new IConversationViewLayout() {
            @Override
            public void customizeConversationLayout(ConversationBaseFragment fragment) {
              ViewUtils.addTipsView(fragment.getBodyTopLayout(), fragment.getContext(), false);
            }
          };
      ConversationKitClient.setConversationUIConfig(conversationUIConfig);
    }

    IMKitNetRequester.getInstance()
        .setup(
            AppConfig.AIHelperHost_ONLINE,
            DataUtils.readAppKey(this),
            AppConfig.account,
            AppConfig.accessToken);

    ChatUIConfig uiConfig = new ChatUIConfig();
    uiConfig.chatViewCustom =
        new IChatViewCustom() {
          @Override
          public void customizeChatLayout(IChatView layout) {
            ViewUtils.addTipsView(
                layout.getChatBodyTopLayout(), layout.getRootView().getContext(), true);
          }
        };
    uiConfig.chatInputMenu =
        new IChatInputMenu() {
          @Override
          public boolean onAIHelperClick(
              Context context, View view, String action, List<IMMessageInfo> msgList) {
            if (TextUtils.equals(action, ActionConstants.ACTION_AI_HELPER_REFRESH)
                && view instanceof AIHelperView) {
              loadAIHelper((AIHelperView) view, msgList);
            } else if (TextUtils.equals(action, ActionConstants.ACTION_AI_HELPER_SHOW)
                && view instanceof AIHelperView) {
              String msgClient = "msgClient";
              AIHelperView helperView = ((AIHelperView) view);
              if (msgList != null && !msgList.isEmpty()) {
                msgClient = msgList.get(msgList.size() - 1).getMessage().getMessageClientId();
              }
              if (TextUtils.isEmpty(aiHelperLastMsgClientId)
                  || helperView.getHelperItemList().size() < 1
                  || !TextUtils.equals(msgClient, aiHelperLastMsgClientId)) {
                helperView.showLoading();
                loadAIHelper((AIHelperView) view, msgList);
              }
            }
            return true;
          }
        };
    ChatKitClient.setChatUIConfig(uiConfig);
  }

  private void loadAIHelper(AIHelperView helperView, List<IMMessageInfo> msgList) {
    if (helperView == null) {
      return;
    }
    JSONObject queryStr = MessageUtils.generateAIHelperInfo(msgList);
    IMKitNetRequester.getInstance()
        .requestAIChatHelper(
            queryStr,
            new FetchCallback<AIHelperAnswer>() {
              @Override
              public void onError(int errorCode, @Nullable String errorMsg) {
                helperView.showError("");
              }

              @Override
              public void onSuccess(@Nullable AIHelperAnswer data) {
                List<AIHelperView.AIHelperItem> itemList = MessageUtils.convertToAIHelperItem(data);
                if (itemList.isEmpty()) {
                  helperView.showError("");
                } else {
                  helperView.setHelperContent(itemList);
                }
                if (msgList != null && !msgList.isEmpty()) {
                  aiHelperLastMsgClientId =
                      msgList.get(msgList.size() - 1).getMessage().getMessageClientId();
                } else {
                  aiHelperLastMsgClientId = "msgClient";
                }
                aiHelperLastAccountId = ChatRepo.getConversationId();
              }
            });
  }

  private void loadSettingConfig() {
    SettingKitConfig config = DataUtils.getSettingKitConfig();
    IMKitConfigCenter.setEnableCollectionMessage(config.hasCollection);
    IMKitConfigCenter.setEnableTeam(config.hasTeam);
    IMKitConfigCenter.setEnableTopMessage(config.hasStickTopMsg);
    IMKitConfigCenter.setEnableOnlyFriendCall(config.hasStrangeCallLimit);
    IMKitConfigCenter.setEnablePinMessage(config.hasPin);
    IMKitConfigCenter.setEnableTypingStatus(config.hasOnlineStatus);
    IMKitConfigCenter.setEnableTeamJoinAgreeModelAuth(config.hasTeamApplyMode);
  }

  public void parseAndGoChat(Intent intent) {
    if (intent.getExtras() != null && intent.getExtras().containsKey(NOTIFICATION_MESSAGE)) {
      Object msgObject = intent.getExtras().get(NOTIFICATION_MESSAGE);
      if (msgObject instanceof List) {
        List<Object> msgList = (List<Object>) msgObject;
        if (msgList.size() > 0) {
          Object msg = msgList.get(0);
          if (msg instanceof IMMessage) {
            IMMessage message = (IMMessage) msg;
            String targetId = message.getSessionId();
            SessionTypeEnum conversationType = message.getSessionType();
            boolean isNormal =
                AppSkinConfig.getInstance().getAppSkinStyle() == AppSkinConfig.AppSkin.baseSkin;
            if (conversationType == SessionTypeEnum.P2P) {
              XKitRouter.withKey(
                      isNormal
                          ? RouterConstant.PATH_CHAT_P2P_PAGE
                          : RouterConstant.PATH_FUN_CHAT_P2P_PAGE)
                  .withParam(RouterConstant.CHAT_ID_KRY, targetId)
                  .withContext(this)
                  .navigate();
            } else if (conversationType == SessionTypeEnum.Team) {
              XKitRouter.withKey(
                      isNormal
                          ? RouterConstant.PATH_CHAT_TEAM_PAGE
                          : RouterConstant.PATH_FUN_CHAT_TEAM_PAGE)
                  .withParam(RouterConstant.CHAT_ID_KRY, targetId)
                  .withContext(this)
                  .navigate();
            }
            tabClick(activityMainBinding.conversationBtnGroup);
          }
        }
      }
    }
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
