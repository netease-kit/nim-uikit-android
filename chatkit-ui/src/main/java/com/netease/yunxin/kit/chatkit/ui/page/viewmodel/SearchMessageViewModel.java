// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.model.ChatSearchBean;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.ArrayList;
import java.util.List;

/** Search message info view model search history message for Team chat page */
public class SearchMessageViewModel extends BaseViewModel {

  private static final String TAG = "SearchMessageViewModel";
  private final MutableLiveData<FetchResult<List<ChatSearchBean>>> searchLiveData =
      new MutableLiveData<>();

  /** search message result live data */
  public MutableLiveData<FetchResult<List<ChatSearchBean>>> getSearchLiveData() {
    return searchLiveData;
  }

  public void searchMessage(String keyword, V2NIMConversationType type, String sessionId) {
    ALog.d(LIB_TAG, TAG, "searchMessage:" + keyword);
    if (TextUtils.isEmpty(keyword)) {
      //空字符串不搜索
      FetchResult<List<ChatSearchBean>> result = new FetchResult<>(LoadStatus.Success);
      result.setData(new ArrayList<>());
      searchLiveData.postValue(result);
      return;
    }
    ChatRepo.searchMessage(
        keyword,
        type,
        sessionId,
        new FetchCallback<>() {

          @Override
          public void onSuccess(@Nullable List<IMMessageInfo> data) {
            ALog.d(
                LIB_TAG, TAG, "searchMessage,onSuccess:" + (data == null ? "null" : data.size()));
            FetchResult<List<ChatSearchBean>> result = new FetchResult<>(LoadStatus.Success);
            if (data != null) {
              List<ChatSearchBean> searchBeanList = new ArrayList<>();
              for (IMMessageInfo record : data) {
                searchBeanList.add(new ChatSearchBean(record.getMessage(), keyword));
              }
              result.setData(searchBeanList);
            }
            searchLiveData.postValue(result);
          }

          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "searchMessage,onError:" + errorCode + ",errorMsg:" + errorMsg);
            FetchResult<List<ChatSearchBean>> result = new FetchResult<>(LoadStatus.Error);
            result.setError(new FetchResult.ErrorMsg(errorCode, errorMsg));
            searchLiveData.postValue(result);
          }
        });
  }
}
