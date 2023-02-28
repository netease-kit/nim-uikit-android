// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel.add;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.qchatkit.repo.QChatRoleRepo;
import com.netease.yunxin.kit.qchatkit.repo.QChatServerRepo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelMember;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerMemberInfo;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatServerMemberBean;
import com.netease.yunxin.kit.qchatkit.ui.utils.ErrorUtils;
import java.util.ArrayList;
import java.util.List;

/** add member to channel permission view model */
public class AddMemberViewModel extends BaseViewModel {

  private static final String TAG = "BlackWhiteViewModel";
  private final MutableLiveData<FetchResult<List<QChatServerMemberBean>>> resultLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<QChatServerMemberBean>> fetchResult =
      new FetchResult<>(LoadStatus.Finish);
  private final MutableLiveData<FetchResult<QChatChannelMember>> addLiveData =
      new MutableLiveData<>();
  private final FetchResult<QChatChannelMember> addResult = new FetchResult<>(LoadStatus.Finish);
  private QChatServerMemberInfo lastMemberInfo;
  private boolean roleHasMore = false;

  //server member live data
  public MutableLiveData<FetchResult<List<QChatServerMemberBean>>> getResultLiveData() {
    return resultLiveData;
  }

  //add member live data
  public MutableLiveData<FetchResult<QChatChannelMember>> getAddLiveData() {
    return addLiveData;
  }

  /** fetch server member list */
  public void fetchMemberList(long serverId, long channelId) {
    ALog.d(TAG, "fetchMemberList", "info:" + serverId + "," + channelId);
    fetchMemberData(serverId, channelId, 0);
  }

  private void fetchMemberData(long serverId, long channelId, long timeTag) {
    QChatServerRepo.fetchServerMemberWithoutChannel(
        serverId,
        channelId,
        timeTag,
        QChatConstant.MEMBER_PAGE_SIZE,
        new FetchCallback<List<QChatServerMemberInfo>>() {
          @Override
          public void onSuccess(@Nullable List<QChatServerMemberInfo> param) {
            ArrayList<QChatServerMemberBean> addList = new ArrayList<>();
            if (param != null && param.size() > 0) {
              for (int index = 0; index < param.size(); index++) {
                QChatServerMemberInfo memberInfo = param.get(index);
                if (memberFilter(memberInfo.getAccId())) {
                  QChatServerMemberBean bean = new QChatServerMemberBean(memberInfo);
                  addList.add(bean);
                }
              }
              lastMemberInfo = param.get(param.size() - 1);
            }
            roleHasMore = param != null && param.size() >= QChatConstant.MEMBER_PAGE_SIZE;
            ALog.d(TAG, "fetchMemberData", "onSuccess" + addList.size());
            fetchResult.setData(addList);
            if (timeTag == 0) {
              fetchResult.setLoadStatus(LoadStatus.Success);
            } else {
              fetchResult.setFetchType(FetchResult.FetchType.Add);
              fetchResult.setTypeIndex(-1);
            }
            resultLiveData.postValue(fetchResult);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(TAG, "fetchMemberData", "onFailed" + code);
            fetchResult.setError(code, R.string.qchat_channel_fetch_member_error);
            resultLiveData.postValue(fetchResult);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            String errorMsg = exception != null ? exception.getMessage() : "";
            ALog.d(TAG, "fetchMemberData", "onException" + errorMsg);
            fetchResult.setError(
                QChatConstant.ERROR_CODE_CHANNEL_MEMBER_FETCH,
                R.string.qchat_channel_fetch_member_error);
            resultLiveData.postValue(fetchResult);
          }
        });
  }

  /** add member to channel permission */
  public void addMemberToChannel(long serverId, long channelId, String accId) {
    QChatRoleRepo.addChannelMemberRole(
        serverId,
        channelId,
        accId,
        new FetchCallback<QChatChannelMember>() {
          @Override
          public void onSuccess(@Nullable QChatChannelMember param) {
            ALog.d(TAG, "addMemberToChannel", "onSuccess:" + accId);
            addResult.setLoadStatus(LoadStatus.Success);
            addResult.setData(param);
            addLiveData.postValue(addResult);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(TAG, "fetchMemberData", "onFailed:" + code + accId);
            addResult.setError(
                code, ErrorUtils.getErrorText(code, R.string.qchat_channel_add_member_error));
            addLiveData.postValue(addResult);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            String errorMsg = exception != null ? exception.getMessage() : "";
            ALog.d(TAG, "addMemberToChannel", "onException:" + errorMsg + accId);
            addResult.setError(
                QChatConstant.ERROR_CODE_CHANNEL_MEMBER_ADD,
                R.string.qchat_channel_add_member_error);
            addLiveData.postValue(addResult);
          }
        });
  }

  /** load next page */
  public void loadMore(long serverId, long channelId) {
    long offset = 0;
    if (lastMemberInfo != null) {
      offset = lastMemberInfo.getCreateTime();
    }
    fetchMemberData(serverId, channelId, offset);
  }

  /** member filter to exclude current user */
  public boolean memberFilter(String accId) {
    return !TextUtils.equals(accId, IMKitClient.account());
  }

  public boolean hasMore() {
    return roleHasMore;
  }
}
