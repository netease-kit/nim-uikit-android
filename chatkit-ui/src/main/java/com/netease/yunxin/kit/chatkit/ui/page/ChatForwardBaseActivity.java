// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.ChatMessageType;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatForwardBaseActBinding;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatForwardMsgViewModel;
import com.netease.yunxin.kit.chatkit.ui.view.message.adapter.ChatMessageAdapter;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;
import java.util.List;

public class ChatForwardBaseActivity extends BaseActivity {

  protected ChatForwardBaseActBinding viewBinding;

  protected IMMessageInfo messageInfo;

  protected ChatForwardMsgViewModel viewModel;
  private ChatMessageAdapter messageAdapter;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    viewBinding = ChatForwardBaseActBinding.inflate(getLayoutInflater());
    setContentView(viewBinding.getRoot());
    initData();
    initView();
  }

  private void initData() {
    messageInfo = (IMMessageInfo) getIntent().getSerializableExtra(RouterConstant.KEY_MESSAGE);
    if (messageInfo == null
        || !(messageInfo.getMessage().getAttachment() instanceof MultiForwardAttachment)) {
      finish();
    }
    viewModel = new ViewModelProvider(this).get(ChatForwardMsgViewModel.class);
    viewModel.getMessageLiveData().observeForever(this::onMessageLoad);
    viewModel.downloadFile(messageInfo);
    viewModel
        .getAttachmentProgressMutableLiveData()
        .observeForever(this::onAttachmentUpdateProgress);
  }

  private void initView() {
    String title = getString(R.string.msg_multi_detail_title);
    viewBinding.forwardPageTitle.setTitle(title);
    viewBinding.forwardPageTitle.getTitleTextView().setEllipsize(TextUtils.TruncateAt.MIDDLE);
    viewBinding.forwardPageTitle.setOnBackIconClickListener(v -> finish());
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    viewBinding.messageView.setLayoutManager(layoutManager);
    viewBinding.messageView.setItemAnimator(null);
    messageAdapter = new ChatMessageAdapter();
    viewBinding.messageView.setAdapter(messageAdapter);
    messageAdapter.setMessageMode(ChatMessageType.FORWARD_MESSAGE_MODE);
    messageAdapter.setItemClickListener(
        new IMessageItemClickListener() {
          @Override
          public boolean onMessageLongClick(View view, int position, ChatMessageBean messageBean) {
            if (messageBean.getMessageData().getMessage().getMsgType() == MsgTypeEnum.text) {
              MessageHelper.copyTextMessage(messageBean.getMessageData(), true);
            }
            return true;
          }

          @Override
          public boolean onMessageClick(View view, int position, ChatMessageBean messageInfo) {
            clickMessage(messageInfo.getMessageData());
            return true;
          }
        });
  }

  private void onMessageLoad(FetchResult<List<ChatMessageBean>> result) {
    if (result.getLoadStatus() == LoadStatus.Success) {
      messageAdapter.appendMessages(result.getData());
    } else if (result.getLoadStatus() == LoadStatus.Error) {
      Toast.makeText(this, R.string.msg_multi_forward_download_error_tips, Toast.LENGTH_SHORT)
          .show();
      finish();
    }
  }

  protected void onAttachmentUpdateProgress(FetchResult<AttachmentProgress> fetchResult) {
    messageAdapter.updateMessageProgress(fetchResult.getData());
  }

  protected void clickMessage(IMMessageInfo messageInfo) {
    if (messageInfo == null) {
      return;
    }
    if (messageInfo.getMessage().getMsgType() == MsgTypeEnum.image) {
      ArrayList<IMMessageInfo> messageInfoList = new ArrayList<>();
      messageInfoList.add(messageInfo);
      ChatUtils.watchImage(this, messageInfo, messageInfoList);
    } else if (messageInfo.getMessage().getMsgType() == MsgTypeEnum.video) {
      ChatUtils.watchVideo(this, messageInfo);
    } else if (messageInfo.getMessage().getMsgType() == MsgTypeEnum.location) {
      XKitRouter.withKey(RouterConstant.PATH_CHAT_LOCATION_PAGE)
          .withContext(this)
          .withParam(RouterConstant.KEY_MESSAGE, messageInfo.getMessage())
          .withParam(RouterConstant.KEY_LOCATION_PAGE_TYPE, RouterConstant.KEY_LOCATION_TYPE_DETAIL)
          .navigate();
    } else if (messageInfo.getMessage().getMsgType() == MsgTypeEnum.file) {
      ChatUtils.openForwardFile(this, messageInfo);

    } else if (messageInfo.getMessage().getMsgType() == MsgTypeEnum.audio) {

    }
  }
}
