// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.nimlib.coexist.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.coexist.sdk.v2.team.model.V2NIMTeam;
import com.netease.nimlib.coexist.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageForwardConfirmLayoutBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatUserSelectedItemLayoutBinding;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.common.ui.activities.adapter.CommonMoreAdapter;
import com.netease.yunxin.kit.common.ui.activities.viewholder.BaseMoreViewHolder;
import com.netease.yunxin.kit.common.ui.dialog.BaseDialog;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.corekit.coexist.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.coexist.im2.model.UserWithFriend;
import java.util.ArrayList;

public class FunChatMessageForwardConfirmDialog extends BaseDialog {

  public static final String TAG = "ChatMessageForwardConfirmDialog";

  public static final String FORWARD_ACTION = "forwardAction";

  public static final String FORWARD_CONVERSATION_LIST = "forwardSessionList";

  public static final String FORWARD_MESSAGE_SEND = "forwardMessage";
  public static final String FORWARD_MESSAGE_SEND_TIPS = "forwardMessageTips";
  public static final String FORWARD_SHOW_INPUT = "forwardMessageWithInput";
  FunChatMessageForwardConfirmLayoutBinding binding;

  UserAdapter adapter;

  ForwardCallback callback;
  boolean showInput;

  @Nullable
  @Override
  protected View getRootView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    binding = FunChatMessageForwardConfirmLayoutBinding.inflate(inflater, container, false);
    LinearLayoutManager layoutManager =
        new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
    binding.rvAvatar.setLayoutManager(layoutManager);
    adapter = new UserAdapter();
    binding.rvAvatar.setAdapter(adapter);
    if (getArguments() != null) {
      adapter.append(getArguments().getStringArrayList(FORWARD_CONVERSATION_LIST));
      String sendUser = getArguments().getString(FORWARD_MESSAGE_SEND);
      String action = getArguments().getString(FORWARD_ACTION, ActionConstants.POP_ACTION_TRANSMIT);
      String sendTips = getArguments().getString(FORWARD_MESSAGE_SEND_TIPS);
      showInput = getArguments().getBoolean(FORWARD_SHOW_INPUT, false);
      String forwardContent = "";
      if (!TextUtils.isEmpty(sendTips)) {
        forwardContent = sendTips;
      } else if (TextUtils.equals(action, ActionConstants.POP_ACTION_TRANSMIT)) {
        forwardContent = String.format(getString(R.string.chat_message_forward_tips), sendUser);
      } else if (TextUtils.equals(action, ActionConstants.ACTION_TYPE_MULTI_FORWARD)) {
        forwardContent =
            String.format(getString(R.string.chat_message_multi_forward_tips), sendUser);
      } else if (TextUtils.equals(action, ActionConstants.ACTION_TYPE_SINGLE_FORWARD)) {
        forwardContent =
            String.format(getString(R.string.chat_message_single_forward_tips), sendUser);
      }
      binding.tvMessage.setText(forwardContent);
    }
    binding.tvCancel.setOnClickListener(v -> hide());
    binding.messageInputLayout.setVisibility(showInput ? View.VISIBLE : View.GONE);
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
    void onForward(String inputMsg);
  }

  public static class UserAdapter
      extends CommonMoreAdapter<String, FunChatUserSelectedItemLayoutBinding> {

    @NonNull
    @Override
    public BaseMoreViewHolder<String, FunChatUserSelectedItemLayoutBinding> getViewHolder(
        @NonNull ViewGroup parent, int viewType) {
      FunChatUserSelectedItemLayoutBinding viewBinding =
          FunChatUserSelectedItemLayoutBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false);
      return new UserItemViewHolder(viewBinding).setData(getDataList().size() < 2);
    }
  }

  public static class UserItemViewHolder
      extends BaseMoreViewHolder<String, FunChatUserSelectedItemLayoutBinding> {

    boolean showNickname;

    public UserItemViewHolder(@NonNull FunChatUserSelectedItemLayoutBinding binding) {
      super(binding);
    }

    public UserItemViewHolder setData(boolean showNickname) {
      this.showNickname = showNickname;
      return this;
    }

    @Override
    public void bind(String item) {
      V2NIMConversationType forwardType = V2NIMConversationIdUtil.conversationType(item);
      if (forwardType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
        ContactRepo.getFriendUserInfo(
            V2NIMConversationIdUtil.conversationTargetId(item),
            new FetchCallback<UserWithFriend>() {
              @Override
              public void onError(int errorCode, @Nullable String errorMsg) {
                ALog.e(LIB_TAG, TAG, "fetchFriend error: " + errorCode + " " + errorMsg);
                setFriendInfo(null, item);
              }

              @Override
              public void onSuccess(@Nullable UserWithFriend userInfo) {
                ALog.d(LIB_TAG, TAG, "fetchFriend success ");
                setFriendInfo(userInfo, item);
              }
            });

      } else if (forwardType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM) {
        TeamRepo.getTeamInfo(
            V2NIMConversationIdUtil.conversationTargetId(item),
            new FetchCallback<V2NIMTeam>() {
              @Override
              public void onError(int errorCode, @Nullable String errorMsg) {
                ALog.e(LIB_TAG, TAG, "getTeamInfo error: " + errorCode + " " + errorMsg);
              }

              @Override
              public void onSuccess(@Nullable V2NIMTeam team) {
                String avatar = team == null ? null : team.getAvatar();
                String nickname = team == null ? item : team.getName();
                ALog.d(LIB_TAG, TAG, "getTeamInfo success: " + nickname);
                getBinding().avatar.setData(avatar, nickname, AvatarColor.avatarColor(item));
                if (showNickname) {
                  getBinding().nickname.setVisibility(View.VISIBLE);
                  getBinding().nickname.setText(nickname);
                } else {
                  getBinding().nickname.setVisibility(View.GONE);
                }
              }
            });
      }
    }

    private void setFriendInfo(UserWithFriend userInfo, String item) {
      String avatar = userInfo == null ? null : userInfo.getAvatar();
      String nickname = userInfo == null ? item : userInfo.getName();
      ALog.d(LIB_TAG, TAG, "setFriendInfo: " + nickname);
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

  public static FunChatMessageForwardConfirmDialog createForwardConfirmDialog(
      ArrayList<String> sessionIds,
      String forwardName,
      String forwardTips,
      boolean showInput,
      String forwardAction) {
    FunChatMessageForwardConfirmDialog confirmDialog = new FunChatMessageForwardConfirmDialog();
    Bundle bundle = new Bundle();
    bundle.putStringArrayList(
        FunChatMessageForwardConfirmDialog.FORWARD_CONVERSATION_LIST, sessionIds);
    bundle.putString(FunChatMessageForwardConfirmDialog.FORWARD_MESSAGE_SEND, forwardName);
    bundle.putString(FunChatMessageForwardConfirmDialog.FORWARD_MESSAGE_SEND_TIPS, forwardTips);
    bundle.putString(FunChatMessageForwardConfirmDialog.FORWARD_ACTION, forwardAction);
    bundle.putBoolean(FunChatMessageForwardConfirmDialog.FORWARD_SHOW_INPUT, showInput);
    confirmDialog.setArguments(bundle);

    return confirmDialog;
  }

  public static FunChatMessageForwardConfirmDialog createForwardConfirmDialog(
      ArrayList<String> sessionIds, String forwardName, boolean showInput, String forwardAction) {
    return createForwardConfirmDialog(sessionIds, forwardName, "", showInput, forwardAction);
  }

  public static FunChatMessageForwardConfirmDialog createForwardConfirmDialog(
      ArrayList<String> sessionIds, String forwardName, boolean showInput) {
    return createForwardConfirmDialog(
        sessionIds, forwardName, showInput, ActionConstants.POP_ACTION_TRANSMIT);
  }
}
