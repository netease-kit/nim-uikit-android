/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.page.fragment;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_ID;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_TEAM_SETTING_PAGE;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.lifecycle.ViewModelProvider;

import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.model.IMTeamMessageReceiptInfo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatTeamViewModel;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitManager;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

import java.util.ArrayList;
import java.util.List;

/**
 * Team chat page
 */
public class ChatTeamFragment extends ChatBaseFragment {
    private static final String TAG = "ChatTeamFragment";
    Team teamInfo;
    String teamId;
    IMMessage anchorMessage;

    @Override
    protected void initData(Bundle bundle) {
        ALog.i(TAG, "initData");
        sessionType = SessionTypeEnum.Team;
        teamInfo = (Team) bundle.getSerializable(RouterConstant.CHAT_KRY);
        teamId = (String) bundle.getSerializable(RouterConstant.CHAT_ID_KRY);
        if (teamInfo == null && TextUtils.isEmpty(teamId)){
            getActivity().finish();
            return;
        }
        if (TextUtils.isEmpty(teamId)){
            teamId = teamInfo.getId();
        }
        anchorMessage = (IMMessage) bundle.getSerializable(RouterConstant.KEY_MESSAGE);
        binding.chatView.getTitleBar()
                .setOnBackIconClickListener(v -> requireActivity().onBackPressed())
                .setActionImg(R.drawable.ic_more_point)
                .setActionListener(v -> {
                    //go to team setting
                    XKitRouter.withKey(PATH_TEAM_SETTING_PAGE)
                            .withContext(requireContext())
                            .withParam(KEY_TEAM_ID, teamId)
                            .navigate();
                });

        aitManager = new AitManager(getContext(), teamId);
        binding.chatView.setAitManager(aitManager);
        refreshView();
    }

    private void refreshView(){
        if (teamInfo != null) {
            binding.chatView.getTitleBar().setTitle(teamInfo.getName());
            binding.chatView.getInputView().updateInputInfo(teamInfo.getName());
            if (!TextUtils.equals(teamInfo.getCreator(), IMKitClient.account())) {
                binding.chatView.getInputView().setMute(teamInfo.isAllMute());
            }
            binding.chatView.getMessageListView().updateTeamInfo(teamInfo);
        }
    }

    @Override
    protected void initViewModel() {
        ALog.i(TAG, "initViewModel");
        viewModel = new ViewModelProvider(this).get(ChatTeamViewModel.class);
        viewModel.init(teamId, SessionTypeEnum.Team);
        //fetch history message
        viewModel.initFetch(anchorMessage);
        // query team info
        ((ChatTeamViewModel) viewModel).requestTeamInfo(teamId);
        // init team members
        ((ChatTeamViewModel) viewModel).requestTeamMembers(teamId);
    }

    @Override
    protected void initDataObserver() {
        super.initDataObserver();
        ALog.i(TAG, "initDataObserver");
        ((ChatTeamViewModel) viewModel).getTeamMessageReceiptLiveData().observe(getViewLifecycleOwner(), listFetchResult -> {
            if (listFetchResult == null || listFetchResult.getData() == null) return;
            List<ChatMessageBean> messageList = new ArrayList<>();
            for (IMTeamMessageReceiptInfo receiptInfo : listFetchResult.getData()) {
                String msgId = receiptInfo.getTeamMessageReceipt().getMsgId();
                ChatMessageBean msg = null;
                for (ChatMessageBean messageBean : messageList) {
                    if (messageBean.getMessageData() != null && TextUtils.equals(messageBean.getMessageData().getMessage().getUuid(), msgId)) {
                        msg = messageBean;
                        break;
                    }
                }
                if (msg == null) {
                    msg = binding.chatView.getMessageListView().searchMessage(msgId);
                    messageList.add(msg);
                }
            }
            for (ChatMessageBean message : messageList) {
                binding.chatView.getMessageListView().updateMessage(message);
            }

        });

        ((ChatTeamViewModel) viewModel).getTeamLiveData().observe(getViewLifecycleOwner(), team -> {
            if (team != null) {
                teamInfo = team;
                if (!team.isMyTeam()) {
                    requireActivity().finish();
                }
                refreshView();
            }
        });

        ((ChatTeamViewModel) viewModel).getUserInfoData().observe(getViewLifecycleOwner(), teamMembers -> {
            if (teamMembers.getSuccess() && teamMembers.getValue() != null) {
                aitManager.setTeamMembers(teamMembers.getValue());
            }
        });
    }

    @Override
    public void onNewIntent(Intent intent) {
        //todo scroll to target position
        ALog.i(TAG, "onNewIntent");
        anchorMessage = (IMMessage) intent.getSerializableExtra(RouterConstant.KEY_MESSAGE);
        if (anchorMessage != null) {
            int position = binding.chatView.getMessageListView().searchMessagePosition(anchorMessage.getUuid());
            if (position >= 0) {
                binding.chatView.getMessageListView().smoothScrollToPosition(position);
            } else {
                binding.chatView.clearMessageList();
                // need to add anchor message to list panel
                binding.chatView.appendMessage(new ChatMessageBean(new IMMessageInfo(anchorMessage)));
                viewModel.initFetch(anchorMessage);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ALog.i(TAG, "onDestroy");
        if (aitManager != null) {
            aitManager.reset();
        }
    }
}
