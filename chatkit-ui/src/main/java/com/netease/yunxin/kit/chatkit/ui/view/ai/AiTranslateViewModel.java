// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.ai;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.ai.V2NIMAIListener;
import com.netease.nimlib.sdk.v2.ai.model.V2NIMAIUser;
import com.netease.nimlib.sdk.v2.ai.params.V2NIMAIModelCallContent;
import com.netease.nimlib.sdk.v2.ai.params.V2NIMAIModelConfigParams;
import com.netease.nimlib.sdk.v2.ai.params.V2NIMProxyAIModelCallParams;
import com.netease.nimlib.sdk.v2.ai.result.V2NIMAIModelCallResult;
import com.netease.nimlib.sdk.v2.ai.result.V2NIMAIModelStreamCallResult;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.manager.AIUserManager;
import com.netease.yunxin.kit.chatkit.repo.AIRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.utils.ErrorUtils;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.SPUtils;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.json.JSONObject;

/** AI翻译ViewModel 用于处理AI翻译相关逻辑、保存语言选择结果 */
public class AiTranslateViewModel extends BaseViewModel {

  private static final String TAG = "AiTranslateViewModel";
  private static final String LanguageKey = "Language";
  private static final String selectLanguageFileName = "NEAITranslate";
  private static final String selectLanguageSPKey = "ai_translate_select_language_";
  protected MutableLiveData<FetchResult<String>> translateResultLiveData = new MutableLiveData<>();
  protected V2NIMAIUser aiTranslateUser;

  public LiveData<FetchResult<String>> getTranslateResultLiveData() {
    return translateResultLiveData;
  }

  public AiTranslateViewModel() {
    aiTranslateUser = AIUserManager.getAITranslateUser();
    AIRepo.addAIListener(aiListener);
  }

  public void translate(String text, String language) {
    ALog.e(LIB_TAG, TAG, "aiTranslate text:" + text + " language:" + language);
    if (aiTranslateUser == null) {
      ALog.e(LIB_TAG, TAG, "aiTranslateUser is null");
      return;
    }

    V2NIMProxyAIModelCallParams.Builder builder = new V2NIMProxyAIModelCallParams.Builder();
    builder.accountId(aiTranslateUser.getAccountId());
    // 生成随机UUID作为requestID
    UUID uuid = UUID.randomUUID();
    String requestId = uuid.toString();
    builder.requestId(requestId);
    V2NIMAIModelCallContent content = new V2NIMAIModelCallContent(text, 0);
    builder.content(content);

    //设置模型参数
    V2NIMAIModelConfigParams configParams = new V2NIMAIModelConfigParams();
    configParams.setTemperature(0.2);
    builder.modelConfigParams(configParams);

    try {
      JSONObject variables = new JSONObject();
      variables.put(LanguageKey, language);
      String varStr = variables.toString();
      builder.promptVariables(varStr);
      ALog.d(LIB_TAG, TAG, "start translate:" + varStr);
    } catch (Exception e) {
      ALog.e(LIB_TAG, TAG, "translate error, promptVariables error");
    }

    AIRepo.proxyAIModelCall(
        builder.build(),
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.e(
                LIB_TAG,
                TAG,
                "aiSearch error, errorCode: " + errorCode + ", errorMsg: " + errorMsg);
            FetchResult<String> fetchResult = new FetchResult<>(LoadStatus.Error);
            translateResultLiveData.setValue(fetchResult);
            ErrorUtils.showErrorCodeToast(
                IMKitClient.getApplicationContext(), errorCode, R.string.chat_ai_search_error);
          }

          @Override
          public void onSuccess(@Nullable Void data) {
            ALog.d(LIB_TAG, TAG, "aiSearch success");
          }
        });
  }

  private V2NIMAIListener aiListener =
      new V2NIMAIListener() {
        @Override
        public void onProxyAIModelCall(V2NIMAIModelCallResult result) {
          ALog.d(
              LIB_TAG,
              TAG,
              "onProxyAIModelCall requestId: "
                  + result.getRequestId()
                  + ",accountId: "
                  + result.getAccountId());
          if (result != null
              && aiTranslateUser != null
              && TextUtils.equals(result.getAccountId(), aiTranslateUser.getAccountId())) {
            if (result.getCode() == 200) {
              FetchResult<String> fetchResult = new FetchResult<>(LoadStatus.Success);
              fetchResult.setData(result.getContent().getMsg());
              translateResultLiveData.setValue(fetchResult);
            } else {
              FetchResult<String> fetchResult = new FetchResult<>(LoadStatus.Error);
              translateResultLiveData.setValue(fetchResult);
              ErrorUtils.showErrorCodeToast(
                  IMKitClient.getApplicationContext(),
                  result.getCode(),
                  R.string.chat_ai_search_error);
            }
          }
        }

        @Override
        public void onProxyAIModelStreamCall(V2NIMAIModelStreamCallResult result) {}
      };

  public List<LanguageModel> getLanguageList(Context context) {
    List<LanguageModel> languageModelList = new ArrayList<>();
    List<String> translateLanguage = AIUserManager.getAITranslateLanguages();
    String selectLanguage = getSelectLanguage();
    if (translateLanguage == null || translateLanguage.size() == 0) {
      String[] languages = context.getResources().getStringArray(R.array.language_name_arrays);
      if (languages != null && languages.length > 0) {
        translateLanguage = new ArrayList<>();
        translateLanguage.addAll(Arrays.asList(languages));
      }
    }
    boolean hasSelect = false;
    for (int i = 0; i < translateLanguage.size() && i < translateLanguage.size(); i++) {
      LanguageModel languageModel = new LanguageModel();
      languageModel.language = translateLanguage.get(i);
      languageModel.languageTag = translateLanguage.get(i);
      if (selectLanguage != null && selectLanguage.equals(languageModel.language)) {
        languageModel.isSelected = true;
        hasSelect = true;
      }
      languageModelList.add(languageModel);
    }
    if (!hasSelect && languageModelList.size() > 0) {
      languageModelList.get(0).isSelected = true;
    }
    return languageModelList;
  }

  public String getSelectLanguage() {
    // Load language list
    return SPUtils.getInstance(selectLanguageFileName)
        .getString(selectLanguageSPKey + IMKitClient.account());
  }

  public void saveSelectLanguage(String language) {
    SPUtils.getInstance(selectLanguageFileName)
        .put(selectLanguageSPKey + IMKitClient.account(), language);
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    AIRepo.removeAIListener(aiListener);
  }
}
