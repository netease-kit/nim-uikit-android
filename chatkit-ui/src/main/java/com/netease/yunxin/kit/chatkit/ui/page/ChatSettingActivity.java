/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.page;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_REQUEST_SELECTOR_NAME;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.netease.nimlib.sdk.msg.model.StickTopSessionInfo;
import com.netease.nimlib.sdk.team.model.CreateTeamResult;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.ChatMessageRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatCallback;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatSettingActivityBinding;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

import java.util.ArrayList;

/**
 * Setting page for P2P chat and Team chat page
 */
public class ChatSettingActivity extends BaseActivity {
    private static final String TAG = "ChatSettingActivity";

    ChatSettingActivityBinding binding;

    UserInfo userInfo;

    private ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ChatSettingActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.title.setOnBackIconClickListener(v -> onBackPressed())
                .setTitle(R.string.chat_setting);
        initView();
        initData();
    }

    private void initView() {
        userInfo = (UserInfo) getIntent().getSerializableExtra(RouterConstant.CHAT_KRY);
        String name = TextUtils.isEmpty(userInfo.getComment()) ? userInfo.getName() : userInfo.getComment();
        if (name == null) {
            name = userInfo.getAccount();
        }
        ALog.i(TAG, "initView name -->> " + name);
        binding.avatar.setData(userInfo.getAvatar(), name, AvatarColor.avatarColor(userInfo.getAccount()));
        binding.tvName.setText(name);
        binding.ivAdd.setOnClickListener(v -> selectUsersCreateGroup());
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != RESULT_OK) {
                return;
            }
            ALog.i(TAG, "contact selector result");
            Intent data = result.getData();
            if (data != null) {
                ArrayList<String> friends = data.getStringArrayListExtra(REQUEST_CONTACT_SELECTOR_KEY);
                if (friends != null && !friends.isEmpty()) {
                    friends.add(userInfo.getAccount());
                    XKitRouter.withKey(RouterConstant.PATH_CREATE_NORMAL_TEAM)
                            .withParam(REQUEST_CONTACT_SELECTOR_KEY, friends)
                            .withParam(KEY_REQUEST_SELECTOR_NAME, data.getStringArrayListExtra(KEY_REQUEST_SELECTOR_NAME))
                            .navigate(res -> {
                                if (res.getSuccess() && res.getValue() instanceof CreateTeamResult) {
                                    Team teamInfo = ((CreateTeamResult) res.getValue()).getTeam();
                                    Intent intent = new Intent(ChatSettingActivity.this, ChatTeamActivity.class);
                                    intent.putExtra(RouterConstant.CHAT_KRY, teamInfo);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                }
            }
        });
    }

    private void initData() {
        if (userInfo == null) return;
        binding.scSessionTop.setChecked(ChatMessageRepo.isStick(userInfo.getAccount()));
        binding.rlySessionTop.setOnClickListener(v -> {
            if (!binding.scSessionTop.isChecked()) {
                ChatMessageRepo.addStick(userInfo.getAccount(), new ChatCallback<StickTopSessionInfo>() {
                    @Override
                    public void onSuccess(@Nullable StickTopSessionInfo param) {
                        binding.scSessionTop.setChecked(true);
                        ChatMessageRepo.notifyP2PStickNotify(userInfo.getAccount());
                    }
                });
            } else {
                ChatMessageRepo.removeStick(userInfo.getAccount(), new ChatCallback<Void>() {
                    @Override
                    public void onSuccess(@Nullable Void param) {
                        binding.scSessionTop.setChecked(false);
                        ChatMessageRepo.notifyP2PStickNotify(userInfo.getAccount());
                    }
                });
            }
        });

        binding.scMessageNotice.setChecked(ChatMessageRepo.isNotify(userInfo.getAccount()));
        binding.rlyMessageNotice.setOnClickListener(v -> ChatMessageRepo.setNotify(userInfo.getAccount(),
                !binding.scMessageNotice.isChecked(), new ChatCallback<Void>() {
                    @Override
                    public void onSuccess(@Nullable Void param) {
                        binding.scMessageNotice.setChecked(!binding.scMessageNotice.isChecked());
                    }
                }));

    }

    private void selectUsersCreateGroup() {
        ArrayList<String> filterList = new ArrayList<>();
        filterList.add(userInfo.getAccount());
        XKitRouter.withKey(RouterConstant.PATH_SELECTOR_ACTIVITY)
                .withParam(RouterConstant.KEY_CONTACT_SELECTOR_MAX_COUNT, 199)
                .withParam(RouterConstant.KEY_REQUEST_SELECTOR_NAME_ENABLE, true)
                .withContext(this)
                .withParam(RouterConstant.SELECTOR_CONTACT_FILTER_KEY, filterList)
                .navigate(launcher);
    }
}
