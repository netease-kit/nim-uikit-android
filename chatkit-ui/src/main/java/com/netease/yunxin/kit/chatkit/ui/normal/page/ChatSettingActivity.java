// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.page;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.CHAT_P2P_INVITER_USER_LIMIT;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.NormalChatSettingActivityBinding;
import com.netease.yunxin.kit.chatkit.ui.model.CloseChatPageEvent;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatSettingViewModel;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.event.EventNotify;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;

/** 标准皮肤，单聊会话设置页面。 */
public class ChatSettingActivity extends BaseActivity {
  private static final String TAG = "ChatSettingActivity";

  NormalChatSettingActivityBinding binding;

  ChatSettingViewModel viewModel;

  UserWithFriend friendInfo;
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

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EventCenter.registerEventNotify(closeEventNotify);
    changeStatusBarColor(R.color.color_eef1f4);
    binding = NormalChatSettingActivityBinding.inflate(getLayoutInflater());
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
    accId = (String) getIntent().getSerializableExtra(RouterConstant.CHAT_ID_KRY);
    if (TextUtils.isEmpty(accId)) {
      finish();
      return;
    }
    refreshView();
    if (IMKitConfigCenter.getEnableTeam()) {
      binding.addIv.setVisibility(View.VISIBLE);
      binding.noTeamNameTv.setVisibility(View.GONE);
      binding.nameTv.setVisibility(View.VISIBLE);
      binding.addIv.setOnClickListener(v -> selectUsersCreateGroup());
    } else {
      binding.addIv.setVisibility(View.GONE);
      binding.nameTv.setVisibility(View.GONE);
      binding.noTeamNameTv.setVisibility(View.VISIBLE);
    }

    binding.stickTopLayout.setOnClickListener(
        v -> {
          if (!NetworkUtils.isConnected()) {
            Toast.makeText(this, R.string.chat_network_error_tip, Toast.LENGTH_SHORT).show();
            return;
          }
          viewModel.stickTop(accId, !binding.stickTopSC.isChecked());
        });

    if (IMKitConfigCenter.getEnablePinMessage()) {
      binding.pinLayout.setVisibility(View.VISIBLE);
      binding.pinLayout.setOnClickListener(
          v ->
              XKitRouter.withKey(RouterConstant.PATH_CHAT_PIN_PAGE)
                  .withParam(
                      RouterConstant.KEY_SESSION_TYPE,
                      V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P.getValue())
                  .withParam(RouterConstant.KEY_SESSION_ID, accId)
                  .withParam(RouterConstant.KEY_SESSION_NAME, getName())
                  .withContext(ChatSettingActivity.this)
                  .navigate());
    } else {
      binding.pinLayout.setVisibility(View.GONE);
    }

    binding.notifyLayout.setOnClickListener(
        v -> {
          if (!NetworkUtils.isConnected()) {
            Toast.makeText(this, R.string.chat_network_error_tip, Toast.LENGTH_SHORT).show();
            return;
          }
          viewModel.setMute(accId, binding.notifySC.isChecked());
        });
  }

  private void refreshView() {
    if (friendInfo != null) {
      binding.avatarView.setData(
          friendInfo.getAvatar(),
          friendInfo.getAvatarName(),
          AvatarColor.avatarColor(friendInfo.getAccount()));
      binding.nameTv.setText(friendInfo.getName());
      binding.noTeamNameTv.setText(friendInfo.getName());
    } else {
      binding.avatarView.setData(null, accId, AvatarColor.avatarColor(accId));
      binding.nameTv.setText(accId);
      binding.noTeamNameTv.setText(accId);
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
                friendInfo = result.getData();
                refreshView();
              }
            });

    viewModel
        .getStickTopLiveData()
        .observe(
            this,
            result -> {
              if (result.getData() != null) {
                if (result.getLoadStatus() == LoadStatus.Success) {
                  if (result.getData() != binding.stickTopSC.isChecked()) {
                    binding.stickTopSC.setChecked(result.getData());
                  }

                } else {
                  binding.stickTopSC.setChecked(result.getData());
                }
              }
            });

    viewModel
        .getMuteLiveData()
        .observe(
            this,
            result -> {
              if (result.getData() != null) {
                if (result.getLoadStatus() == LoadStatus.Success) {
                  if (result.getData() == binding.notifySC.isChecked()) {
                    binding.notifySC.setChecked(!result.getData());
                  }
                } else {
                  binding.notifySC.setChecked(result.getData());
                }
              }
            });
    if (IMKitConfigCenter.getEnableAIUser()) {
      viewModel
          .getAIUserPinLiveData()
          .observe(
              this,
              result -> {
                if (result.getData() != null) {
                  if (result.getLoadStatus() == LoadStatus.Success) {
                    if (result.getData() != binding.aiPinSC.isChecked()) {
                      binding.aiPinSC.setChecked(result.getData());
                    }
                  } else {
                    binding.aiPinSC.setChecked(!binding.aiPinSC.isChecked());
                  }
                }
              });
      viewModel
          .getIsAIPinLiveData()
          .observe(
              this,
              result -> {
                if (result.getLoadStatus() == LoadStatus.Success) {
                  if (IMKitConfigCenter.getEnableAIUser() && result.getData()) {
                    binding.aiPinLayout.setVisibility(View.VISIBLE);
                    binding.aiPinDivider.setVisibility(View.VISIBLE);
                    binding.aiPinLayout.setOnClickListener(
                        new View.OnClickListener() {
                          @Override
                          public void onClick(View v) {
                            viewModel.switchPinAIUser(!binding.aiPinSC.isChecked(), accId);
                          }
                        });
                  } else {
                    binding.aiPinLayout.setVisibility(View.GONE);
                    binding.aiPinDivider.setVisibility(View.GONE);
                  }
                }
              });
    }
    viewModel.requestData(accId);
  }

  private void selectUsersCreateGroup() {
    ArrayList<String> filterList = new ArrayList<>();
    filterList.add(accId);
    XKitRouter.withKey(RouterConstant.PATH_SELECT_CREATE_TEAM_PAGE)
        .withParam(RouterConstant.KEY_CONTACT_SELECTOR_MAX_COUNT, CHAT_P2P_INVITER_USER_LIMIT - 1)
        .withParam(RouterConstant.KEY_REQUEST_SELECTOR_NAME_ENABLE, true)
        .withContext(this)
        .withParam(RouterConstant.SELECTOR_CONTACT_FILTER_KEY, filterList)
        .withParam(RouterConstant.REQUEST_CONTACT_SELECTOR_KEY, filterList)
        .navigate();
  }

  private String getName() {
    if (friendInfo != null) {
      return friendInfo.getName();
    }
    return accId;
  }
}
