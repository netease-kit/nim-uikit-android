/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.page;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatReadStateLayoutBinding;
import com.netease.yunxin.kit.chatkit.ui.page.fragment.ChatReadUserListFragment;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatReadStateViewModel;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.adapter.BaseFragmentAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * chat message read state page
 */
public class ChatMessageAckActivity extends BaseActivity {

    private static final String IMMESSAGE_KEY = "message_key";

    ChatReadStateLayoutBinding binding;

    TabLayout.Tab tabRead;
    TabLayout.Tab tabUnread;

    TabLayoutMediator mediator;

    ChatReadStateViewModel viewModel;

    IMMessage message;

    ChatReadUserListFragment readFragment;
    ChatReadUserListFragment unreadFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ChatReadStateLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
        initViewModel();
    }

    private void initView() {

        binding.title.setOnBackIconClickListener(v -> onBackPressed());

        List<Fragment> fragments = new ArrayList<>();
        readFragment = new ChatReadUserListFragment();
        unreadFragment = new ChatReadUserListFragment();
        fragments.add(readFragment);
        fragments.add(unreadFragment);

        tabRead = binding.tabLayout.newTab();
        tabRead.setText(getString(R.string.chat_read_with_num, 0));
        tabUnread = binding.tabLayout.newTab();
        tabUnread.setText(getString(R.string.chat_unread_with_num, 0));
        binding.tabLayout.addTab(tabUnread);
        binding.tabLayout.addTab(tabRead);

        BaseFragmentAdapter fragmentAdapter = new BaseFragmentAdapter(this);
        fragmentAdapter.setFragmentList(fragments);
        binding.viewPager.setAdapter(fragmentAdapter);

        mediator = new TabLayoutMediator(binding.tabLayout,
                binding.viewPager,
                (tab, position) -> ALog.i("onConfigureTab pos = " + position));

        mediator.attach();
    }

    public static void startMessageAckActivity(Context context, IMMessage message) {
        Intent intent = new Intent(context, ChatMessageAckActivity.class);
        intent.putExtra(IMMESSAGE_KEY, message);
        context.startActivity(intent);
    }

    private void initViewModel() {
        message = (IMMessage) getIntent().getSerializableExtra(IMMESSAGE_KEY);
        if (message == null) {
            return;
        }
        ALog.i("initViewModel");
        Bundle readBundle = new Bundle();
        readBundle.putBoolean(ChatReadUserListFragment.ACK_KEY, true);
        readBundle.putString(ChatReadUserListFragment.TID_KEY, message.getSessionId());
        readFragment.setArguments(readBundle);
        Bundle unReadBundle = new Bundle();
        unReadBundle.putBoolean(ChatReadUserListFragment.ACK_KEY, false);
        unReadBundle.putString(ChatReadUserListFragment.TID_KEY, message.getSessionId());
        unreadFragment.setArguments(unReadBundle);
        viewModel = new ViewModelProvider(this).get(ChatReadStateViewModel.class);
        viewModel.fetchTeamAckInfo(message);
        viewModel.getTeamAckInfoLiveData().observe(this, teamMsgAckInfo -> {
            tabRead.setText(getString(R.string.chat_read_with_num, teamMsgAckInfo == null ? 0 : teamMsgAckInfo.getAckCount()));
            tabUnread.setText(getString(R.string.chat_unread_with_num, teamMsgAckInfo == null ? 0 : teamMsgAckInfo.getUnAckCount()));
        });
    }

}
