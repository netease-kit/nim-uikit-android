// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.aisearchkit.page;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.coexist.sdk.v2.ai.V2NIMAIListener;
import com.netease.nimlib.coexist.sdk.v2.ai.enums.V2NIMAIModelRoleType;
import com.netease.nimlib.coexist.sdk.v2.ai.model.V2NIMAIUser;
import com.netease.nimlib.coexist.sdk.v2.ai.params.V2NIMAIModelCallContent;
import com.netease.nimlib.coexist.sdk.v2.ai.params.V2NIMAIModelCallMessage;
import com.netease.nimlib.coexist.sdk.v2.ai.params.V2NIMAIModelConfigParams;
import com.netease.nimlib.coexist.sdk.v2.ai.params.V2NIMProxyAIModelCallParams;
import com.netease.nimlib.coexist.sdk.v2.ai.result.V2NIMAIModelCallResult;
import com.netease.nimlib.coexist.sdk.v2.ai.result.V2NIMAIModelStreamCallResult;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.R;
import com.netease.yunxin.kit.chatkit.manager.AIUserManager;
import com.netease.yunxin.kit.chatkit.repo.AIRepo;
import com.netease.yunxin.kit.chatkit.utils.AIErrorCode;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.coexist.im2.extend.FetchCallback;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AISearchViewModel extends BaseViewModel {

  private static final String TAG = "AISearchViewModel";

  private final V2NIMAIUser aiUser = AIUserManager.getAISearchUser();
  //保存requestID
  private final List<String> requestIds = new ArrayList<>();

  //搜索结果
  private final MutableLiveData<V2NIMAIModelCallContent> searchResult = new MutableLiveData<>();

  //加载状态
  private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();

  //上下文
  private final List<V2NIMAIModelCallMessage> messages = new ArrayList<>();

  private final V2NIMAIListener aiListener = new V2NIMAIListener() {
    @Override
    public void onProxyAIModelCall(V2NIMAIModelCallResult result) {
      ALog.d(TAG, "aiSearch, result: " + result);
      if (aiUser != null
              && Objects.equals(result.getAccountId(), aiUser.getAccountId())
              && requestIds.contains(result.getRequestId())) {
        if (result.getCode() != AIErrorCode.V2NIM_ERROR_CODE_SUCCESS && result.getCode() != 0) {
          String msg = getAIResultMsg(IMKitClient.getApplicationContext(), result.getCode());
          searchResult.setValue(new V2NIMAIModelCallContent(msg, 0));
        } else {
          searchResult.setValue(result.getContent());
        }
        requestIds.remove(result.getRequestId());
      }
      if (requestIds.isEmpty()) {
        isLoadingLiveData.setValue(false);
      }
    }

    @Override
    public void onProxyAIModelStreamCall(V2NIMAIModelStreamCallResult result) {

    }
  };
  public AISearchViewModel() {
    AIRepo.addAIListener(aiListener);
  }

  /**
   * 获取搜索结果
   *
   * @return 搜索结果
   */
  public LiveData<V2NIMAIModelCallContent> getSearchResult() {
    return searchResult;
  }

  /**
   * 获取加载状态
   *
   * @return 加载状态
   */
  public LiveData<Boolean> getIsLoadingLiveData() {
    return isLoadingLiveData;
  }

  /**
   * 搜索
   *
   * @param keyword 关键字
   */
  public void aiSearch(final String keyword) {
    if (aiUser == null) {
      return;
    }
    V2NIMProxyAIModelCallParams.Builder builder = new V2NIMProxyAIModelCallParams.Builder();
    builder.accountId(aiUser.getAccountId());
    // 生成随机UUID作为requestID
    UUID uuid = UUID.randomUUID();
    String requestId = uuid.toString();
    builder.requestId(requestId);
    V2NIMAIModelCallContent content = new V2NIMAIModelCallContent(keyword, 0);
    builder.content(content);
    //上下文
    if (!messages.isEmpty()) {
      builder.messages(messages);
    }

    //设置配置参数
    V2NIMAIModelConfigParams params = new V2NIMAIModelConfigParams();
    params.setTemperature(0.8);
    builder.modelConfigParams(params);

    isLoadingLiveData.setValue(true);

    AIRepo.proxyAIModelCall(
        builder.build(),
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.e(TAG, "aiSearch error, errorCode: " + errorCode + ", errorMsg: " + errorMsg);
            isLoadingLiveData.setValue(false);
            ToastX.showShortToast(getAIResultMsg(IMKitClient.getApplicationContext(), errorCode));
          }

          @Override
          public void onSuccess(@Nullable Void data) {
            ALog.d(TAG, "aiSearch success requestID: " + requestId + ", keyword: " + keyword);
            requestIds.add(requestId);
            //发送成之后，将关键字添加到上下文
            V2NIMAIModelCallMessage message =
                new V2NIMAIModelCallMessage(
                    V2NIMAIModelRoleType.V2NIM_AI_MODEL_ROLE_TYPE_USER, keyword, 0);
            messages.add(0, message);
          }
        });
  }

  public static String getAIResultMsg(Context context, int errorCode) {
    switch (errorCode) {
      case AIErrorCode.AI_ERROR_TIPS_CODE:
        return context.getString(R.string.chat_ai_message_type_unsupport);
      case AIErrorCode.V2NIM_ERROR_CODE_FAILED_TO_REQUEST_LLM:
        return context.getString(R.string.chat_ai_error_failed_request_to_the_llm);
      case AIErrorCode.V2NIM_ERROR_CODE_AI_MESSAGES_FUNCTION_DISABLED:
        return context.getString(R.string.chat_ai_error_ai_messages_function_disabled);
      case AIErrorCode.V2NIM_ERROR_CODE_IS_NOT_AI_ACCOUNT:
        return context.getString(R.string.chat_ai_error_not_an_ai_account);
      case AIErrorCode.V2NIM_ERROR_CODE_AI_ACCOUNT_BLOCKLIST_OPERATION_NOT_ALLOWED:
        return context.getString(R.string.chat_ai_error_cannot_blocklist_an_ai_account);
      case AIErrorCode.V2NIM_ERROR_CODE_PARAMETER_ERROR:
        return context.getString(R.string.chat_ai_error_parameter);
      case AIErrorCode.V2NIM_ERROR_CODE_ACCOUNT_NOT_EXIST:
      case AIErrorCode.V2NIM_ERROR_CODE_FRIEND_NOT_EXIST:
        return context.getString(R.string.chat_ai_error_user_not_exist);
      case AIErrorCode.V2NIM_ERROR_CODE_ACCOUNT_BANNED:
        return context.getString(R.string.chat_ai_error_user_banned);
      case AIErrorCode.V2NIM_ERROR_CODE_ACCOUNT_CHAT_BANNED:
        return context.getString(R.string.chat_ai_error_user_chat_banned);
      case AIErrorCode.V2NIM_ERROR_CODE_MESSAGE_HIT_ANTISPAM:
        return context.getString(R.string.chat_ai_error_message_hit_antispam);
      case AIErrorCode.V2NIM_ERROR_CODE_TEAM_MEMBER_NOT_EXIST:
        return context.getString(R.string.chat_ai_error_team_member_not_exist);
      case AIErrorCode.V2NIM_ERROR_CODE_TEAM_NORMAL_MEMBER_CHAT_BANNED:
        return context.getString(R.string.chat_ai_error_team_normal_member_chat_banned);
      case AIErrorCode.V2NIM_ERROR_CODE_TEAM_MEMBER_CHAT_BANNED:
        return context.getString(R.string.chat_ai_error_team_member_chat_banned);
      case AIErrorCode.V2NIM_ERROR_CODE_RATE_LIMIT:
        return context.getString(R.string.chat_ai_error_rate_limit_exceeded);
      default:
        break;
    }
    return context.getString(com.netease.yunxin.kit.aisearchkit.R.string.ai_search_error);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    AIRepo.removeAIListener(aiListener);
    messages.clear();
  }
}
