// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.page.fragment.ChatReaderBaseFragment;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatReadStateViewModel;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.adapter.BaseFragmentAdapter;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import java.util.ArrayList;
import java.util.List;

/** chat message read state page */
public abstract class ChatReaderBaseActivity extends BaseActivity {

  private static final String TAG = "ChatMessageAckActivity";

  public TabLayout tabLayout;
  public ViewPager2 fragmentViewPager;
  public BackTitleBar titleBarView;
  TabLayout.Tab tabRead;
  TabLayout.Tab tabUnread;

  TabLayoutMediator mediator;

  ChatReadStateViewModel viewModel;

  V2NIMMessage message;

  ChatReaderBaseFragment readFragment;
  ChatReaderBaseFragment unreadFragment;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initViewAndSetContentView(savedInstanceState);
    bindView();
    initViewModel();
  }

  public abstract ChatReaderBaseFragment getReadFragment();

  public abstract ChatReaderBaseFragment getUnreadFragment();

  public abstract void initViewAndSetContentView(@Nullable Bundle savedInstanceState);

  public void bindView() {

    if (titleBarView != null) {
      titleBarView.setOnBackIconClickListener(v -> onBackPressed());
    }

    List<Fragment> fragments = new ArrayList<>();
    readFragment = getReadFragment();
    unreadFragment = getUnreadFragment();
    fragments.add(readFragment);
    fragments.add(unreadFragment);

    if (tabLayout != null) {
      tabRead = tabLayout.newTab();
      tabRead.setId(R.id.tabHasRead);
      tabUnread = tabLayout.newTab();
      tabUnread.setId(R.id.tabUnread);
      tabLayout.addTab(tabUnread);
      tabLayout.addTab(tabRead);
    }

    if (fragmentViewPager != null) {
      BaseFragmentAdapter fragmentAdapter = new BaseFragmentAdapter(this);
      fragmentAdapter.setFragmentList(fragments);
      fragmentViewPager.setAdapter(fragmentAdapter);
    }

    if (fragmentViewPager != null && tabLayout != null) {
      mediator =
          new TabLayoutMediator(
              tabLayout,
              fragmentViewPager,
              (tab, position) -> ALog.d(LIB_TAG, TAG, "onConfigureTab pos = " + position));

      mediator.attach();
    }
  }

  private void initViewModel() {
    message = (V2NIMMessage) getIntent().getSerializableExtra(RouterConstant.KEY_MESSAGE);
    if (message == null) {
      return;
    }
    ALog.d(LIB_TAG, "initViewModel");
    if (readFragment != null) {
      Bundle readBundle = new Bundle();
      readBundle.putBoolean(ChatReaderBaseFragment.ACK_KEY, true);
      readBundle.putString(
          ChatReaderBaseFragment.TID_KEY,
          V2NIMConversationIdUtil.conversationTargetId(message.getConversationId()));
      readFragment.setArguments(readBundle);
    }
    if (unreadFragment != null) {
      Bundle unReadBundle = new Bundle();
      unReadBundle.putBoolean(ChatReaderBaseFragment.ACK_KEY, false);
      unReadBundle.putString(
          ChatReaderBaseFragment.TID_KEY,
          V2NIMConversationIdUtil.conversationTargetId(message.getConversationId()));
      unreadFragment.setArguments(unReadBundle);
    }
    if (tabLayout != null) {
      tabUnread.setText(getString(R.string.chat_unread_with_num, 0));
      tabRead.setText(getString(R.string.chat_read_with_num, 0));
    }
    viewModel = new ViewModelProvider(this).get(ChatReadStateViewModel.class);
    viewModel.fetchTeamAckInfo(message);
    viewModel
        .getTeamAckInfoLiveData()
        .observe(
            this,
            teamMsgAckInfo -> {
              if (tabLayout != null) {
                tabRead.setText(
                    getString(
                        R.string.chat_read_with_num,
                        teamMsgAckInfo == null
                            ? 0
                            : teamMsgAckInfo.getReceiptDetail().getReadReceipt().getReadCount()));
                tabUnread.setText(
                    getString(
                        R.string.chat_unread_with_num,
                        teamMsgAckInfo == null
                            ? 0
                            : teamMsgAckInfo.getReceiptDetail().getReadReceipt().getUnreadCount()));
              }
            });
  }
}
