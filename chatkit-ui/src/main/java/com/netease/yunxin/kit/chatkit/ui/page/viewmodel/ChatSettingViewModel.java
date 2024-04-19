// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMConversation;
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.chatkit.repo.SettingRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;
import java.util.ArrayList;
import java.util.List;

public class ChatSettingViewModel extends BaseViewModel {
  private static final String TAG = "ChatSettingViewModel";
  private final MutableLiveData<FetchResult<UserWithFriend>> userInfoLiveData =
      new MutableLiveData<>();

  private final MutableLiveData<FetchResult<Boolean>> stickTopLiveData = new MutableLiveData<>();

  private final MutableLiveData<FetchResult<Boolean>> muteLiveData = new MutableLiveData<>();

  public MutableLiveData<FetchResult<UserWithFriend>> getUserInfoLiveData() {
    return userInfoLiveData;
  }

  public MutableLiveData<FetchResult<Boolean>> getStickTopLiveData() {
    return stickTopLiveData;
  }

  public MutableLiveData<FetchResult<Boolean>> getMuteLiveData() {
    return muteLiveData;
  }

  public void requestData(String accountId) {
    getUserInfo(accountId);
    getConversationInfo(accountId);
  }

  /**
   * 获取用户信息
   *
   * @param accountId 用户id
   */
  public void getUserInfo(String accountId) {
    ALog.d(LIB_TAG, TAG, "getUserInfo:" + accountId);
    if (TextUtils.isEmpty(accountId)) {
      return;
    }
    List<String> accountList = new ArrayList<>();
    accountList.add(accountId);
    ContactRepo.getFriendInfoList(
        accountList,
        new FetchCallback<>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "getUserInfo,onError:" + errorCode + "," + errorMsg);
          }

          @Override
          public void onSuccess(@Nullable List<UserWithFriend> data) {
            ALog.d(LIB_TAG, TAG, "getUserInfo,onSuccess:" + (data == null));
            FetchResult<UserWithFriend> userInfoFetchResult = new FetchResult<>(LoadStatus.Success);
            if (data != null && data.size() > 0) {
              userInfoFetchResult.setData(data.get(0));
              userInfoLiveData.setValue(userInfoFetchResult);
            }
          }
        });
  }

  /**
   * 获取会话信息，用于查询会话置顶和免打扰状态
   *
   * @param accountId 用户id
   */
  public void getConversationInfo(String accountId) {
    ALog.d(LIB_TAG, TAG, "getConversationInfo:" + accountId);
    if (TextUtils.isEmpty(accountId)) {
      return;
    }
    String conversationId =
        V2NIMConversationIdUtil.conversationId(
            accountId, V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P);
    ConversationRepo.getConversationById(
        conversationId,
        new FetchCallback<>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "getConversationInfo,onError:" + errorCode + "," + errorMsg);
          }

          @Override
          public void onSuccess(@Nullable V2NIMConversation data) {
            if (data != null) {
              ALog.d(LIB_TAG, TAG, "getConversationInfo,onSuccess is mute:" + data.isMute());
              ALog.d(LIB_TAG, TAG, "getConversationInfo,stickTop:" + data.isStickTop());
              FetchResult<Boolean> muteResult = new FetchResult<>(LoadStatus.Success);
              muteResult.setData(data.isMute());
              muteLiveData.setValue(muteResult);

              FetchResult<Boolean> stickTopResult = new FetchResult<>(LoadStatus.Success);
              stickTopResult.setData(data.isStickTop());
              stickTopLiveData.setValue(stickTopResult);
            }
          }
        });

    SettingRepo.getP2PMessageMuteMode(
        accountId,
        new FetchCallback<>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "getP2PMessageMuteMode,onError:" + errorCode + "," + errorMsg);
          }

          @Override
          public void onSuccess(@Nullable Boolean data) {
            ALog.d(LIB_TAG, TAG, "getP2PMessageMuteMode,onSuccess:" + data);
          }
        });
  }

  /**
   * 置顶/取消置顶
   *
   * @param accountId 用户id
   * @param isStickTop 是否置顶
   */
  public void stickTop(String accountId, boolean isStickTop) {
    if (TextUtils.isEmpty(accountId)) {
      return;
    }
    String conversationId =
        V2NIMConversationIdUtil.conversationId(
            accountId, V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P);
    ConversationRepo.stickTop(
        conversationId,
        isStickTop,
        new FetchCallback<>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "stickTop,onError:" + errorCode + "," + errorMsg);
            FetchResult<Boolean> fetchResult = new FetchResult<>(LoadStatus.Error);
            fetchResult.setData(!isStickTop);
            stickTopLiveData.setValue(fetchResult);
          }

          @Override
          public void onSuccess(@Nullable Void data) {
            ALog.d(LIB_TAG, TAG, "stickTop,onSuccess:" + isStickTop);
            FetchResult<Boolean> fetchResult = new FetchResult<>(LoadStatus.Success);
            fetchResult.setData(isStickTop);
            stickTopLiveData.setValue(fetchResult);
          }
        });
  }

  /**
   * 设置免打扰
   *
   * @param accountId 用户id
   * @param isMute 是否免打扰
   */
  public void setMute(String accountId, boolean isMute) {
    SettingRepo.setP2PMessageMuteMode(
        accountId,
        isMute,
        new FetchCallback<>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "setNotify,onError:" + errorCode + "," + errorMsg);
            FetchResult<Boolean> fetchResult = new FetchResult<>(LoadStatus.Error);
            fetchResult.setData(!isMute);
            muteLiveData.setValue(fetchResult);
          }

          @Override
          public void onSuccess(@Nullable Void data) {
            ALog.d(LIB_TAG, TAG, "setNotify ,onSuccess:" + isMute);
            FetchResult<Boolean> fetchResult = new FetchResult<>(LoadStatus.Success);
            fetchResult.setData(isMute);
            muteLiveData.setValue(fetchResult);
          }
        });
  }
}
