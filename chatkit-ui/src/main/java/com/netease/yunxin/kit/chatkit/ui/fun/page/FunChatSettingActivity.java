// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.page;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.CHAT_P2P_INVITER_USER_LIMIT;
import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_REQUEST_SELECTOR_NAME;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.StickTopSessionInfo;
import com.netease.nimlib.sdk.team.model.CreateTeamResult;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatCallback;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatSettingActivityBinding;
import com.netease.yunxin.kit.chatkit.ui.model.CloseChatPageEvent;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatSettingViewModel;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.event.EventNotify;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;

/** Setting page for P2P chat and Team chat page */
public class FunChatSettingActivity extends BaseActivity {
  private static final String TAG = "ChatSettingActivity";

  FunChatSettingActivityBinding binding;

  ChatSettingViewModel viewModel;

  UserInfo userInfo;
  String accId;

  protected final EventNotify<CloseChatPageEvent> closeEventNotify =
      new EventNotify<CloseChatPageEvent>() {
        @Override
        public void onNotify(@NonNull CloseChatPageEvent message) {
          finish();
        }

        @NonNull
        @Override
        public String getEventType() {
          return CloseChatPageEvent.EVENT_TYPE;
        }
      };

  private ActivityResultLauncher<Intent> launcher;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EventCenter.registerEventNotify(closeEventNotify);
    changeStatusBarColor(R.color.color_white);
    binding = FunChatSettingActivityBinding.inflate(getLayoutInflater());
    viewModel = new ViewModelProvider(this).get(ChatSettingViewModel.class);
    setContentView(binding.getRoot());
    binding
        .titleBarView
        .setOnBackIconClickListener(v -> onBackPressed())
        .setTitle(R.string.chat_setting);
    initView();
    initData();
  }

  private void initView() {
    userInfo = (UserInfo) getIntent().getSerializableExtra(RouterConstant.CHAT_KRY);
    accId = (String) getIntent().getSerializableExtra(RouterConstant.CHAT_ID_KRY);
    if (userInfo == null && TextUtils.isEmpty(accId)) {
      finish();
      return;
    }
    if (TextUtils.isEmpty(accId)) {
      accId = userInfo.getAccount();
    }
    refreshView();
    binding.addIv.setOnClickListener(v -> selectUsersCreateGroup());
    String finalAccId = accId;
    launcher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() != RESULT_OK) {
                return;
              }
              ALog.d(LIB_TAG, TAG, "contact selector result");
              Intent data = result.getData();
              if (data != null) {
                ArrayList<String> friends =
                    data.getStringArrayListExtra(REQUEST_CONTACT_SELECTOR_KEY);
                if (friends != null && !friends.isEmpty()) {
                  friends.add(finalAccId);
                  XKitRouter.withKey(RouterConstant.PATH_FUN_CREATE_NORMAL_TEAM_ACTION)
                      .withParam(REQUEST_CONTACT_SELECTOR_KEY, friends)
                      .withParam(
                          KEY_REQUEST_SELECTOR_NAME,
                          data.getStringArrayListExtra(KEY_REQUEST_SELECTOR_NAME))
                      .navigate(
                          res -> {
                            if (res.getSuccess() && res.getValue() instanceof CreateTeamResult) {
                              Team teamInfo = ((CreateTeamResult) res.getValue()).getTeam();
                              XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_TEAM_PAGE)
                                  .withParam(RouterConstant.CHAT_KRY, teamInfo)
                                  .withContext(FunChatSettingActivity.this)
                                  .navigate();
                              finish();
                            }
                          });
                }
              }
            });
  }

  private void refreshView() {
    if (userInfo == null) {
      binding.avatarView.setData(null, accId, AvatarColor.avatarColor(accId));
      binding.nameTv.setText(accId);
    } else {
      String name =
          TextUtils.isEmpty(userInfo.getComment()) ? userInfo.getName() : userInfo.getComment();
      if (name == null) {
        name = userInfo.getAccount();
      }
      ALog.d(LIB_TAG, TAG, "initView name -->> " + name);
      binding.avatarView.setData(
          userInfo.getAvatar(), name, AvatarColor.avatarColor(userInfo.getAccount()));
      binding.nameTv.setText(name);
    }
  }

  private void initData() {
    if (accId == null) return;
    viewModel
        .getUserInfoLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                userInfo = result.getData();
                refreshView();
              }
            });
    viewModel.getUserInfo(accId);
    binding.stickTopSC.setChecked(ConversationRepo.isStickTop(accId, SessionTypeEnum.P2P));
    binding.stickTopLayout.setOnClickListener(
        v -> {
          if (!binding.stickTopSC.isChecked()) {
            ConversationRepo.addStickTop(
                accId,
                SessionTypeEnum.P2P,
                "",
                new ChatCallback<StickTopSessionInfo>() {
                  @Override
                  public void onSuccess(@Nullable StickTopSessionInfo param) {
                    binding.stickTopSC.setChecked(true);
                    ConversationRepo.notifyStickTop(accId, SessionTypeEnum.P2P);
                  }
                });
          } else {
            ConversationRepo.removeStickTop(
                accId,
                SessionTypeEnum.P2P,
                "",
                new ChatCallback<Void>() {
                  @Override
                  public void onSuccess(@Nullable Void param) {
                    binding.stickTopSC.setChecked(false);
                    ConversationRepo.notifyStickTop(accId, SessionTypeEnum.P2P);
                  }
                });
          }
        });

    binding.pinLayout.setOnClickListener(
        v ->
            XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_PIN_PAGE)
                .withParam(RouterConstant.KEY_SESSION_TYPE, SessionTypeEnum.P2P.getValue())
                .withParam(RouterConstant.KEY_SESSION_ID, accId)
                .withContext(FunChatSettingActivity.this)
                .navigate());

    binding.notifySC.setChecked(ConversationRepo.isNotify(accId, SessionTypeEnum.P2P));
    binding.notifyLayout.setOnClickListener(
        v ->
            ConversationRepo.setNotify(
                accId,
                SessionTypeEnum.P2P,
                !binding.notifySC.isChecked(),
                new ChatCallback<Void>() {
                  @Override
                  public void onSuccess(@Nullable Void param) {
                    binding.notifySC.setChecked(!binding.notifySC.isChecked());
                  }
                }));
  }

  private void selectUsersCreateGroup() {
    ArrayList<String> filterList = new ArrayList<>();
    filterList.add(accId);
    XKitRouter.withKey(RouterConstant.PATH_FUN_CONTACT_SELECTOR_PAGE)
        .withParam(RouterConstant.KEY_CONTACT_SELECTOR_MAX_COUNT, CHAT_P2P_INVITER_USER_LIMIT)
        .withParam(RouterConstant.KEY_REQUEST_SELECTOR_NAME_ENABLE, true)
        .withContext(this)
        .withParam(RouterConstant.SELECTOR_CONTACT_FILTER_KEY, filterList)
        .navigate(launcher);
  }
}
