/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.page.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatUserListLayoutBinding;
import com.netease.yunxin.kit.chatkit.ui.page.adapter.ChatUserAdapter;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatReadStateViewModel;
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;

/**
 * chat message read state page
 */
public class ChatReadUserListFragment extends BaseFragment {
    ChatUserListLayoutBinding binding;

    ChatUserAdapter adapter;

    public static final String ACK_KEY = "is_ack_list";

    public static final String TID_KEY = "team_id";

    /**
     * true for ack list
     * false for unack list
     */
    boolean ack;

    String tid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ChatUserListLayoutBinding.inflate(inflater, container, false);
        initView();
        initData();
        return binding.getRoot();
    }

    private void initView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerView.setLayoutManager(layoutManager);
        adapter = new ChatUserAdapter();
        binding.recyclerView.setAdapter(adapter);
    }

    private void initData() {
        if (getArguments() != null) {
            ack = getArguments().getBoolean(ACK_KEY);
            tid = getArguments().getString(TID_KEY);
            adapter.setTid(tid);
            ChatReadStateViewModel viewModel = new ViewModelProvider(requireActivity()).get(ChatReadStateViewModel.class);
            viewModel.getTeamAckInfoLiveData().observe(getViewLifecycleOwner(), teamMsgAckInfo -> {
                if (ack) {
                    if (teamMsgAckInfo.getAckAccountList().isEmpty()) {
                        binding.llyEmpty.setVisibility(View.VISIBLE);
                        binding.tvAllState.setText(R.string.chat_all_user_have_unread);
                    } else {
                        adapter.append(teamMsgAckInfo.getAckAccountList());
                    }
                } else {
                    if (teamMsgAckInfo.getUnAckAccountList().isEmpty()) {
                        binding.llyEmpty.setVisibility(View.VISIBLE);
                        binding.tvAllState.setText(R.string.chat_all_user_have_read);
                    } else {
                        adapter.append(teamMsgAckInfo.getUnAckAccountList());
                    }
                }
            });
        }

    }
}
