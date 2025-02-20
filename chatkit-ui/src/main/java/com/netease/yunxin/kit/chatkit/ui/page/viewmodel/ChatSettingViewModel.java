// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.ai.model.V2NIMAIUser;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMConversation;
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLocalConversation;
import com.netease.nimlib.sdk.v2.user.V2NIMUser;
import com.netease.nimlib.sdk.v2.user.params.V2NIMUserUpdateParams;
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ChatConstants;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.manager.AIUserManager;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.chatkit.repo.LocalConversationRepo;
import com.netease.yunxin.kit.chatkit.repo.SettingRepo;
import com.netease.yunxin.kit.chatkit.utils.ErrorUtils;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.listener.ContactChangeType;
import com.netease.yunxin.kit.corekit.im2.listener.ContactListener;
import com.netease.yunxin.kit.corekit.im2.model.FriendAddApplicationInfo;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatSettingViewModel extends BaseViewModel {
  private static final String TAG = "ChatSettingViewModel";

  private String currentAccountId;

  //会话不存在的错误码
  private static final int ERROR_CODE_CONVERSATION_NOT_EXIST = 191006;

  private V2NIMAIUser currentAIUser;
  private final MutableLiveData<FetchResult<UserWithFriend>> userInfoLiveData =
      new MutableLiveData<>();

  private final MutableLiveData<FetchResult<Boolean>> stickTopLiveData = new MutableLiveData<>();

  private final MutableLiveData<FetchResult<Boolean>> muteLiveData = new MutableLiveData<>();
  private final MutableLiveData<FetchResult<Boolean>> aiPinLiveData = new MutableLiveData<>();
  private final MutableLiveData<FetchResult<Boolean>> isAIPinLiveData = new MutableLiveData<>();

  public MutableLiveData<FetchResult<UserWithFriend>> getUserInfoLiveData() {
    return userInfoLiveData;
  }

  public MutableLiveData<FetchResult<Boolean>> getStickTopLiveData() {
    return stickTopLiveData;
  }

  public MutableLiveData<FetchResult<Boolean>> getMuteLiveData() {
    return muteLiveData;
  }

  public MutableLiveData<FetchResult<Boolean>> getAIUserPinLiveData() {
    return aiPinLiveData;
  }

  public MutableLiveData<FetchResult<Boolean>> getIsAIPinLiveData() {
    return isAIPinLiveData;
  }

  public void requestData(String accountId) {
    this.currentAccountId = accountId;
    getUserInfo(accountId);
    getConversationInfo(accountId);
    if (IMKitConfigCenter.getEnableAIUser()) {
      getPinAIUser(accountId);
      ContactRepo.addContactListener(contactListener);
    }
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
        new FetchCallback<List<UserWithFriend>>() {
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
    if (IMKitConfigCenter.getEnableLocalConversation()) {
      LocalConversationRepo.getConversation(
          conversationId,
          new FetchCallback<V2NIMLocalConversation>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              ALog.d(LIB_TAG, TAG, "getConversationInfo,onError:" + errorCode + "," + errorMsg);
              if (errorCode == ERROR_CODE_CONVERSATION_NOT_EXIST) {
                ConversationRepo.createConversation(conversationId, null);
              }
            }

            @Override
            public void onSuccess(@Nullable V2NIMLocalConversation data) {
              if (data != null) {
                ALog.d(LIB_TAG, TAG, "getConversationInfo,stickTop:" + data.isStickTop());
                FetchResult<Boolean> stickTopResult = new FetchResult<>(LoadStatus.Success);
                stickTopResult.setData(data.isStickTop());
                stickTopLiveData.setValue(stickTopResult);
              }
            }
          });

    } else {
      ConversationRepo.getConversation(
          conversationId,
          new FetchCallback<V2NIMConversation>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              ALog.d(LIB_TAG, TAG, "getConversationInfo,onError:" + errorCode + "," + errorMsg);
              if (errorCode == ERROR_CODE_CONVERSATION_NOT_EXIST) {
                ConversationRepo.createConversation(conversationId, null);
              }
            }

            @Override
            public void onSuccess(@Nullable V2NIMConversation data) {
              if (data != null) {
                ALog.d(LIB_TAG, TAG, "getConversationInfo,stickTop:" + data.isStickTop());
                FetchResult<Boolean> stickTopResult = new FetchResult<>(LoadStatus.Success);
                stickTopResult.setData(data.isStickTop());
                stickTopLiveData.setValue(stickTopResult);
              }
            }
          });
    }

    SettingRepo.getP2PMessageMuteMode(
        accountId,
        new FetchCallback<Boolean>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "getP2PMessageMuteMode,onError:" + errorCode + "," + errorMsg);
          }

          @Override
          public void onSuccess(@Nullable Boolean data) {
            ALog.d(LIB_TAG, TAG, "getP2PMessageMuteMode,onSuccess:" + data);
            if (data != null) {
              FetchResult<Boolean> muteResult = new FetchResult<>(LoadStatus.Success);
              muteResult.setData(data);
              muteLiveData.setValue(muteResult);
            }
          }
        });
  }

  public void getPinAIUser(String accountId) {
    ALog.d(LIB_TAG, TAG, "getPinAIUser:" + accountId);
    //判断是否为AI数字人
    currentAIUser = AIUserManager.getAIUserById(accountId);
    if (currentAIUser == null) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "getPinAIUser,aiUser:" + currentAIUser.getName());
    // 判断是否为置顶AI数字人，如果不是则返回，UI不展示PIN按钮
    if (!AIUserManager.isPinDefault(currentAIUser)) {
      return;
    }
    isAIPinLiveData.setValue(new FetchResult<>(LoadStatus.Success, true));
    ContactRepo.getUserInfo(
        Collections.singletonList(IMKitClient.account()),
        new FetchCallback<List<V2NIMUser>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "getPinAIUser,onError:" + errorCode + "," + errorMsg);
            ErrorUtils.showErrorCodeToast(IMKitClient.getApplicationContext(), errorCode);
            FetchResult<Boolean> fetchResult = new FetchResult<>(LoadStatus.Success);
            fetchResult.setData(true);
            aiPinLiveData.setValue(fetchResult);
          }

          @Override
          public void onSuccess(@Nullable List<V2NIMUser> data) {
            ALog.d(LIB_TAG, TAG, "getPinAIUser,onSuccess");
            if (data != null && data.size() > 0) {
              V2NIMUser user = data.get(0);
              if (user != null) {
                ALog.d(LIB_TAG, TAG, "getPinAIUser,user:" + user.getServerExtension());
                try {
                  String userExtStr = user.getServerExtension();
                  JSONObject userExtJson = new JSONObject();
                  JSONArray userUnpinArray = new JSONArray();
                  if (!TextUtils.isEmpty(userExtStr)) {
                    userExtJson = new JSONObject(userExtStr);
                    userUnpinArray = userExtJson.optJSONArray(ChatConstants.KEY_UNPIN_AI_USERS);
                    if (userUnpinArray == null) {
                      userUnpinArray = new JSONArray();
                    }
                  }
                  boolean isPin = true;
                  for (int index = 0; index < userUnpinArray.length(); index++) {
                    if (accountId.equals(userUnpinArray.optString(index))) {
                      isPin = false;
                      break;
                    }
                  }
                  FetchResult<Boolean> fetchResult = new FetchResult<>(LoadStatus.Success);
                  fetchResult.setData(isPin);
                  aiPinLiveData.setValue(fetchResult);

                } catch (JSONException e) {
                  throw new RuntimeException(e);
                }
              }
            }
          }
        });
  }

  public void switchPinAIUser(boolean addPin, String accountId) {
    ALog.d(LIB_TAG, TAG, "switchPinAIUser:" + addPin + "," + accountId);
    ContactRepo.getUserInfo(
        Collections.singletonList(IMKitClient.account()),
        new FetchCallback<List<V2NIMUser>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "switchPinAIUser,onError:" + errorCode + "," + errorMsg);
            ErrorUtils.showErrorCodeToast(IMKitClient.getApplicationContext(), errorCode);
          }

          @Override
          public void onSuccess(@Nullable List<V2NIMUser> data) {
            ALog.d(LIB_TAG, TAG, "switchPinAIUser,onSuccess");
            if (data != null && data.size() > 0) {
              V2NIMUser user = data.get(0);
              if (user != null) {
                try {
                  String userExtStr = user.getServerExtension();
                  JSONObject userExtJson = new JSONObject();
                  JSONArray userUnpinArray = new JSONArray();
                  if (!TextUtils.isEmpty(userExtStr)) {
                    userExtJson = new JSONObject(userExtStr);
                    userUnpinArray = userExtJson.optJSONArray(ChatConstants.KEY_UNPIN_AI_USERS);
                    if (userUnpinArray == null) {
                      userUnpinArray = new JSONArray();
                    }
                  }
                  ALog.d(LIB_TAG, TAG, "switchPinAIUser,userExtJson:" + userExtJson.toString());
                  if (addPin) {
                    for (int index = 0; index < userUnpinArray.length(); index++) {
                      if (accountId.equals(userUnpinArray.get(index))) {
                        userUnpinArray.remove(index);
                        break;
                      }
                    }
                  } else {
                    userUnpinArray.put(accountId);
                  }
                  userExtJson.put(ChatConstants.KEY_UNPIN_AI_USERS, userUnpinArray);
                  updateUserExt(addPin, userExtJson.toString());

                } catch (JSONException e) {
                  throw new RuntimeException(e);
                }
              }
            }
          }
        });
  }

  protected void updateUserExt(boolean addPin, String extStr) {
    ALog.d(LIB_TAG, TAG, "updateUserExt:" + extStr);
    V2NIMUserUpdateParams updateParams =
        V2NIMUserUpdateParams.V2NIMUserUpdateParamsBuilder.builder()
            .withServerExtension(extStr)
            .build();
    ContactRepo.updateSelfUserProfile(
        updateParams,
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ErrorUtils.showErrorCodeToast(IMKitClient.getApplicationContext(), errorCode);
          }

          @Override
          public void onSuccess(@Nullable Void data) {
            ALog.d(LIB_TAG, TAG, "updateUserExt,onSuccess");
            FetchResult<Boolean> fetchResult = new FetchResult<>(LoadStatus.Success);
            fetchResult.setData(addPin);
            aiPinLiveData.setValue(fetchResult);
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
    if (IMKitConfigCenter.getEnableLocalConversation()) {
      //本地会话
      LocalConversationRepo.setStickTop(
          conversationId,
          isStickTop,
          new FetchCallback<Void>() {
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
    } else {
      //云端会话

      ConversationRepo.setStickTop(
          conversationId,
          isStickTop,
          new FetchCallback<Void>() {
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
        new FetchCallback<Void>() {
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

  private ContactListener contactListener =
      new ContactListener() {

        @Override
        public void onFriendAddRejected(@NonNull FriendAddApplicationInfo rejectionInfo) {
          //do nothing
        }

        @Override
        public void onFriendAddApplication(@NonNull FriendAddApplicationInfo friendApplication) {
          //do nothing
        }

        @Override
        public void onContactChange(
            @NonNull ContactChangeType changeType,
            @NonNull List<? extends UserWithFriend> contactList) {
          if (currentAIUser == null || !AIUserManager.isPinDefault(currentAIUser)) {
            return;
          }
          if (changeType == ContactChangeType.Update) {
            for (UserWithFriend user : contactList) {
              ALog.d(LIB_TAG, TAG, "onUserProfileChanged:" + user.getAccount());
              if (TextUtils.equals(user.getAccount(), IMKitClient.account())) {
                getPinAIUser(currentAIUser.getAccountId());
              }
            }
          }
        }
      };

  @Override
  protected void onCleared() {
    super.onCleared();
    if (IMKitConfigCenter.getEnableAIUser()) {
      ContactRepo.removeContactListener(contactListener);
    }
  }
}
