// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel.add;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.qchatkit.repo.QChatRoleRepo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelRoleInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerRoleInfo;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatArrowBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import com.netease.yunxin.kit.qchatkit.ui.utils.ErrorUtils;
import java.util.ArrayList;
import java.util.List;

/** add role to channel permission */
public class AddRoleViewModel extends BaseViewModel {

  private static final String TAG = "AddRoleViewModel";
  //sever role list live data
  private final MutableLiveData<FetchResult<List<QChatArrowBean>>> roleLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<QChatArrowBean>> fetchResult =
      new FetchResult<>(LoadStatus.Finish);

  //add role live data
  private final MutableLiveData<FetchResult<QChatChannelRoleInfo>> addLiveData =
      new MutableLiveData<>();
  private final FetchResult<QChatChannelRoleInfo> addResult = new FetchResult<>(LoadStatus.Finish);

  private QChatServerRoleInfo lastRoleInfo;
  private boolean roleHasMore = false;

  public MutableLiveData<FetchResult<List<QChatArrowBean>>> getRoleLiveData() {
    return roleLiveData;
  }

  public MutableLiveData<FetchResult<QChatChannelRoleInfo>> getAddLiveData() {
    return addLiveData;
  }

  /** fetch role list in channel permission */
  public void fetchRoleList(long serverId, long channelId) {
    ALog.d(TAG, "fetchRoleList", "info:" + serverId + "," + channelId);
    fetchRoleData(serverId, channelId, 0);
  }

  private void fetchRoleData(long serverId, long channelId, long offset) {
    QChatRoleRepo.fetchServerRolesWithoutChannel(
        serverId,
        channelId,
        offset,
        QChatConstant.MEMBER_PAGE_SIZE,
        new FetchCallback<List<QChatServerRoleInfo>>() {
          @Override
          public void onSuccess(@Nullable List<QChatServerRoleInfo> param) {
            ArrayList<QChatArrowBean> addList = new ArrayList<>();
            if (param != null && param.size() > 0) {
              for (int index = 0; index < param.size(); index++) {
                QChatServerRoleInfo roleInfo = param.get(index);
                int topRadius = index == 0 ? QChatConstant.CORNER_RADIUS_ARROW : 0;
                int bottomRadius =
                    index == param.size() - 1 ? QChatConstant.CORNER_RADIUS_ARROW : 0;
                QChatArrowBean bean =
                    new QChatArrowBean(roleInfo.getName(), topRadius, bottomRadius);
                bean.param = roleInfo;
                addList.add(bean);
              }
              lastRoleInfo = param.get(param.size() - 1);
            }
            roleHasMore = param != null && param.size() >= QChatConstant.MEMBER_PAGE_SIZE;

            fetchResult.setData(addList);
            if (offset == 0) {
              fetchResult.setLoadStatus(LoadStatus.Success);
            } else {
              fetchResult.setFetchType(FetchResult.FetchType.Add);
              fetchResult.setTypeIndex(-1);
            }
            roleLiveData.postValue(fetchResult);
            ALog.d(TAG, "fetchRoleData", "onSuccess:" + addList.size());
          }

          @Override
          public void onFailed(int code) {
            ALog.d(TAG, "fetchRoleData", "onFailed:" + code);
            fetchResult.setError(code, R.string.qchat_fetch_role_error);
            roleLiveData.postValue(fetchResult);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            String errorMsg = exception != null ? exception.getMessage() : "";
            ALog.d(TAG, "fetchRoleData", "onException:" + errorMsg);
            fetchResult.setError(
                QChatConstant.ERROR_CODE_CHANNEL_ROLE_FETCH, R.string.qchat_fetch_role_error);
            roleLiveData.postValue(fetchResult);
          }
        });
  }

  public void addChannelRole(long channelId, QChatServerRoleInfo roleInfo) {
    QChatRoleRepo.addChannelRole(
        roleInfo.getServerId(),
        channelId,
        roleInfo.getRoleId(),
        new FetchCallback<QChatChannelRoleInfo>() {
          @Override
          public void onSuccess(@Nullable QChatChannelRoleInfo param) {
            ALog.d(TAG, "addChannelRole", "onSuccess:" + roleInfo.getRoleId());
            addResult.setLoadStatus(LoadStatus.Success);
            addResult.setData(param);
            addLiveData.postValue(addResult);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(TAG, "addChannelRole", "onFailed:" + code + "," + roleInfo.getRoleId());
            addResult.setError(code, ErrorUtils.getErrorText(code, R.string.qchat_add_role_error));
            addLiveData.postValue(addResult);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            String errorMsg = exception != null ? exception.getMessage() : "";
            ALog.d(
                TAG, "addMemberToChannel", "onException:" + errorMsg + "," + roleInfo.getRoleId());
            addResult.setError(
                QChatConstant.ERROR_CODE_CHANNEL_MEMBER_ADD, R.string.qchat_add_role_error);
            addLiveData.postValue(addResult);
          }
        });
  }

  public void loadMore(long serverId, long channelId) {
    long offset = 0;
    if (lastRoleInfo != null) {
      offset = lastRoleInfo.getCreateTime();
    }
    fetchRoleData(serverId, channelId, offset);
  }

  public boolean hasMore() {
    return roleHasMore;
  }
}
