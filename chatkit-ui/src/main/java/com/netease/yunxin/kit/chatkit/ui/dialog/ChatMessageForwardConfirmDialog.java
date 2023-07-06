// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatMessageForwardConfirmLayoutBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatUserSelectedItemLayoutBinding;
import com.netease.yunxin.kit.common.ui.activities.adapter.CommonMoreAdapter;
import com.netease.yunxin.kit.common.ui.activities.viewholder.BaseMoreViewHolder;
import com.netease.yunxin.kit.common.ui.dialog.BaseDialog;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.provider.TeamProvider;
import java.util.ArrayList;

public class ChatMessageForwardConfirmDialog extends BaseDialog {

  public static final String TAG = "ChatMessageForwardConfirmDialog";

  public static final String FORWARD_TYPE = "forwardType";

  public static final String FORWARD_SESSION_LIST = "forwardSessionList";

  public static final String FORWARD_MESSAGE_SEND = "forwardMessage";

  ChatMessageForwardConfirmLayoutBinding binding;

  UserAdapter adapter;

  ForwardCallback callback;

  @Nullable
  @Override
  protected View getRootView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    binding = ChatMessageForwardConfirmLayoutBinding.inflate(inflater, container, false);
    LinearLayoutManager layoutManager =
        new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
    binding.rvAvatar.setLayoutManager(layoutManager);
    adapter = new UserAdapter();
    binding.rvAvatar.setAdapter(adapter);
    if (getArguments() != null) {
      adapter.setForwardType(getArguments().getInt(FORWARD_TYPE));
      adapter.append(getArguments().getStringArrayList(FORWARD_SESSION_LIST));
      String sendUser = getArguments().getString(FORWARD_MESSAGE_SEND);
      binding.tvMessage.setText(
          String.format(getString(R.string.chat_message_session_info), sendUser));
    }
    binding.tvCancel.setOnClickListener(v -> hide());

    binding.tvSend.setOnClickListener(
        v -> {
          if (callback != null) {
            callback.onForward();
          }
          hide();
        });

    return binding.getRoot();
  }

  public void setCallback(ForwardCallback callback) {
    this.callback = callback;
  }

  public void hide() {
    if (getDialog() != null && getDialog().isShowing()) {
      dismiss();
    }
  }

  public interface ForwardCallback {
    void onForward();
  }

  public static class UserAdapter
      extends CommonMoreAdapter<String, ChatUserSelectedItemLayoutBinding> {

    int forwardType;

    public void setForwardType(int forwardType) {
      this.forwardType = forwardType;
    }

    @NonNull
    @Override
    public BaseMoreViewHolder<String, ChatUserSelectedItemLayoutBinding> getViewHolder(
        @NonNull ViewGroup parent, int viewType) {
      ChatUserSelectedItemLayoutBinding viewBinding =
          ChatUserSelectedItemLayoutBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false);
      return new UserItemViewHolder(viewBinding).setData(getDataList().size() < 2, forwardType);
    }
  }

  public static class UserItemViewHolder
      extends BaseMoreViewHolder<String, ChatUserSelectedItemLayoutBinding> {

    boolean showNickname;

    int forwardType;

    public UserItemViewHolder(@NonNull ChatUserSelectedItemLayoutBinding binding) {
      super(binding);
    }

    public UserItemViewHolder setData(boolean showNickname, int type) {
      this.forwardType = type;
      this.showNickname = showNickname;
      return this;
    }

    @Override
    public void bind(String item) {
      if (forwardType == SessionTypeEnum.P2P.getValue()) {
        ContactRepo.fetchUserInfo(
            item,
            new FetchCallback<UserInfo>() {
              @Override
              public void onSuccess(@Nullable UserInfo userInfo) {
                setUserInfo(userInfo, item);
              }

              @Override
              public void onFailed(int code) {
                setUserInfo(null, item);
              }

              @Override
              public void onException(@Nullable Throwable exception) {
                setUserInfo(null, item);
              }
            });

      } else if (forwardType == SessionTypeEnum.Team.getValue()) {
        String avatar;
        String nickname = item;
        Team team = TeamProvider.INSTANCE.queryTeamBlock(item);
        avatar = team == null ? null : team.getIcon();
        nickname = team == null ? item : team.getName();
        getBinding().avatar.setData(avatar, nickname, AvatarColor.avatarColor(item));
      }
    }

    private void setUserInfo(UserInfo userInfo, String item) {
      String avatar = userInfo == null ? null : userInfo.getAvatar();
      String nickname = userInfo == null ? item : userInfo.getName();
      getBinding().avatar.setData(avatar, nickname, AvatarColor.avatarColor(item));
      if (showNickname) {
        getBinding().nickname.setVisibility(View.VISIBLE);
        getBinding().nickname.setText(nickname);
      } else {
        getBinding().nickname.setVisibility(View.GONE);
      }
    }
  }

  public static ChatMessageForwardConfirmDialog createForwardConfirmDialog(
      SessionTypeEnum type, ArrayList<String> sessionIds, IMMessageInfo messageInfo) {
    ChatMessageForwardConfirmDialog confirmDialog = new ChatMessageForwardConfirmDialog();
    Bundle bundle = new Bundle();
    bundle.putInt(ChatMessageForwardConfirmDialog.FORWARD_TYPE, type.getValue());
    bundle.putStringArrayList(ChatMessageForwardConfirmDialog.FORWARD_SESSION_LIST, sessionIds);
    String sendName =
        messageInfo.getFromUser() == null
            ? messageInfo.getMessage().getFromAccount()
            : messageInfo.getFromUser().getName();
    bundle.putString(ChatMessageForwardConfirmDialog.FORWARD_MESSAGE_SEND, sendName);
    confirmDialog.setArguments(bundle);

    return confirmDialog;
  }
}
