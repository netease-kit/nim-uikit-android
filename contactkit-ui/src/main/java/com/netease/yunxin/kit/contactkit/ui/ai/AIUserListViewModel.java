// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.ai;

import static com.netease.yunxin.kit.contactkit.ui.ContactConstant.LIB_TAG;

import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.coexist.sdk.v2.ai.model.V2NIMAIUser;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.manager.AIUserManager;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.model.AIUserInfoBean;
import java.util.ArrayList;
import java.util.List;

/*
 * AI数字人ViewModel
 */
public class AIUserListViewModel extends BaseViewModel {
  private static final String TAG = "AIUserListViewModel";

  //AI数字人单查询结果LiveData
  private final MutableLiveData<FetchResult<List<AIUserInfoBean>>> resultLiveData =
      new MutableLiveData<>();
  private final List<AIUserInfoBean> mAIUserList = new ArrayList<>();
  // AI数字人单变化监听

  public MutableLiveData<FetchResult<List<AIUserInfoBean>>> getAIUserListLiveData() {
    return resultLiveData;
  }

  public AIUserListViewModel() {}

  public void getAIUserList() {
    ALog.d(LIB_TAG, TAG, "getBlackList");
    FetchResult<List<AIUserInfoBean>> aiUserResult = new FetchResult<>(LoadStatus.Success);

    List<V2NIMAIUser> userList = AIUserManager.getAllAIUsers();
    for (V2NIMAIUser aiUser : userList) {
      AIUserInfoBean aiUserInfoBean = new AIUserInfoBean(aiUser);
      mAIUserList.add(aiUserInfoBean);
    }
    aiUserResult.setData(mAIUserList);
    resultLiveData.setValue(aiUserResult);
  }

  @Override
  protected void onCleared() {
    super.onCleared();
  }
}
