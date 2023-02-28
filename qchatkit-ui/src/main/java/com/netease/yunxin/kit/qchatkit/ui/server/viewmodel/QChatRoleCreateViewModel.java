// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.server.viewmodel;

import androidx.collection.ArraySet;
import androidx.lifecycle.MutableLiveData;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.corekit.model.ErrorMsg;
import com.netease.yunxin.kit.corekit.model.ResultInfo;
import com.netease.yunxin.kit.qchatkit.repo.QChatRoleRepo;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class QChatRoleCreateViewModel extends BaseViewModel {

  private final Set<String> selectedUsers = new ArraySet<>();

  private final MutableLiveData<Boolean> createResult = new MutableLiveData<>();

  private final MutableLiveData<ErrorMsg> errorLiveData = new MutableLiveData<>();

  public void createRole(Long serverId, String name) {
    if (selectedUsers.isEmpty()) {
      QChatRoleRepo.createRole(
          serverId,
          name,
          result -> {
            operatorResult(result);
            return null;
          });
    } else {
      List<String> memberList = new ArrayList<>(selectedUsers);
      QChatRoleRepo.createRoleWithMember(
          serverId,
          name,
          memberList,
          result -> {
            operatorResult(result);
            return null;
          });
    }
  }

  private void operatorResult(ResultInfo result) {
    createResult.postValue(true);
    if (!result.getSuccess()) {
      errorLiveData.postValue(result.getMsg());
    }
  }

  public void addSelectMember(List<String> member) {
    selectedUsers.addAll(member);
  }

  public void deleteSelectMember(String member) {
    selectedUsers.remove(member);
  }

  public Set<String> getSelectedUsers() {
    return selectedUsers;
  }

  public MutableLiveData<ErrorMsg> getErrorLiveData() {
    return errorLiveData;
  }

  public MutableLiveData<Boolean> getCreateResult() {
    return createResult;
  }
}
