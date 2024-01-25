// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatMessageForwardConfirmLayoutBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatUserSelectedItemLayoutBinding;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.common.ui.activities.adapter.CommonMoreAdapter;
import com.netease.yunxin.kit.common.ui.activities.viewholder.BaseMoreViewHolder;
import com.netease.yunxin.kit.common.ui.dialog.BaseDialog;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.provider.TeamProvider;
import java.util.ArrayList;

/** 标准皮肤，转发确认弹窗。 */
public class ChatMessageForwardConfirmDialog extends BaseDialog {

  public static final String TAG = "ChatMessageForwardConfirmDialog";

  public static final String FORWARD_TYPE = "forwardType";
  public static final String FORWARD_ACTION = "forwardAction";

  public static final String FORWARD_SESSION_LIST = "forwardSessionList";

  public static final String FORWARD_MESSAGE_SEND = "forwardMessage";
  public static final String FORWARD_SHOW_INPUT = "forwardMessageWithInput";

  ChatMessageForwardConfirmLayoutBinding binding;

  UserAdapter adapter;

  ForwardCallback callback;

  boolean showInput;

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
      String action = getArguments().getString(FORWARD_ACTION, ActionConstants.POP_ACTION_TRANSMIT);
      showInput = getArguments().getBoolean(FORWARD_SHOW_INPUT, false);
      String forwardContent = "";
      if (TextUtils.equals(action, ActionConstants.POP_ACTION_TRANSMIT)) {
        forwardContent = String.format(getString(R.string.chat_message_session_info), sendUser);
      } else if (TextUtils.equals(action, ActionConstants.ACTION_TYPE_MULTI_FORWARD)) {
        forwardContent =
            String.format(getString(R.string.chat_message_multi_forward_tips), sendUser);
      } else if (TextUtils.equals(action, ActionConstants.ACTION_TYPE_SINGLE_FORWARD)) {
        forwardContent =
            String.format(getString(R.string.chat_message_single_forward_tips), sendUser);
      }
      binding.tvMessage.setText(forwardContent);
    }
    binding.messageInputLayout.setVisibility(showInput ? View.VISIBLE : View.GONE);
    binding.tvCancel.setOnClickListener(v -> hide());

    binding.tvSend.setOnClickListener(
        v -> {
          if (callback != null) {
            callback.onForward(binding.messageInputEt.getText().toString());
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
    void onForward(String input);
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
        ContactRepo.fetchFriend(
            item,
            new FetchCallback<FriendInfo>() {
              @Override
              public void onSuccess(@Nullable FriendInfo userInfo) {
                setFriendInfo(userInfo, item);
              }

              @Override
              public void onFailed(int code) {
                setFriendInfo(null, item);
              }

              @Override
              public void onException(@Nullable Throwable exception) {
                setFriendInfo(null, item);
              }
            });

      } else if (forwardType == SessionTypeEnum.Team.getValue()) {
        String avatar;
        String nickname = item;
        Team team = TeamProvider.INSTANCE.queryTeamBlock(item);
        avatar = team == null ? null : team.getIcon();
        nickname = team == null ? item : team.getName();
        getBinding().avatar.setData(avatar, nickname, AvatarColor.avatarColor(item));
        if (showNickname) {
          getBinding().nickname.setVisibility(View.VISIBLE);
          getBinding().nickname.setText(nickname);
        } else {
          getBinding().nickname.setVisibility(View.GONE);
        }
      }
    }

    private void setFriendInfo(FriendInfo userInfo, String item) {
      String avatar = userInfo == null ? null : userInfo.getAvatar();
      String nickname = userInfo == null ? item : userInfo.getName();
      String avatarName = userInfo == null ? item : userInfo.getAvatarName();
      getBinding().avatar.setData(avatar, avatarName, AvatarColor.avatarColor(item));
      if (showNickname) {
        getBinding().nickname.setVisibility(View.VISIBLE);
        getBinding().nickname.setText(nickname);
      } else {
        getBinding().nickname.setVisibility(View.GONE);
      }
    }
  }

  public static ChatMessageForwardConfirmDialog createForwardConfirmDialog(
      SessionTypeEnum type,
      ArrayList<String> sessionIds,
      String forwardName,
      boolean showInput,
      String forwardAction) {
    ChatMessageForwardConfirmDialog confirmDialog = new ChatMessageForwardConfirmDialog();
    Bundle bundle = new Bundle();
    bundle.putInt(ChatMessageForwardConfirmDialog.FORWARD_TYPE, type.getValue());
    bundle.putStringArrayList(ChatMessageForwardConfirmDialog.FORWARD_SESSION_LIST, sessionIds);
    bundle.putString(ChatMessageForwardConfirmDialog.FORWARD_MESSAGE_SEND, forwardName);
    bundle.putString(ChatMessageForwardConfirmDialog.FORWARD_ACTION, forwardAction);
    bundle.putBoolean(ChatMessageForwardConfirmDialog.FORWARD_SHOW_INPUT, showInput);
    confirmDialog.setArguments(bundle);

    return confirmDialog;
  }

  public static ChatMessageForwardConfirmDialog createForwardConfirmDialog(
      SessionTypeEnum type, ArrayList<String> sessionIds, String forwardName, boolean showInput) {
    return createForwardConfirmDialog(
        type, sessionIds, forwardName, showInput, ActionConstants.POP_ACTION_TRANSMIT);
  }
}
