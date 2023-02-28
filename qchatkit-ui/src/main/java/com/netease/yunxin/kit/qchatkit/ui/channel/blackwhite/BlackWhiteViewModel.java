// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel.blackwhite;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.qchatkit.repo.QChatChannelRepo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelMember;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelModeEnum;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerMemberInfo;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatArrowBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatBaseBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatServerMemberBean;
import java.util.ArrayList;
import java.util.List;

/**
 * black name list and white name list view model to fetch member list and add member to member list
 */
public class BlackWhiteViewModel extends BaseViewModel {

  private static final String TAG = "BlackWhiteViewModel";
  private final MutableLiveData<FetchResult<List<QChatBaseBean>>> resultLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<QChatBaseBean>> fetchResult = new FetchResult<>(LoadStatus.Finish);

  private final MutableLiveData<FetchResult<QChatChannelMember>> addLiveData =
      new MutableLiveData<>();
  private final FetchResult<QChatChannelMember> addResult = new FetchResult<>(LoadStatus.Finish);

  private final MutableLiveData<FetchResult<QChatChannelMember>> removeLiveData =
      new MutableLiveData<>();
  private final FetchResult<QChatChannelMember> removeResult = new FetchResult<>(LoadStatus.Finish);

  private QChatServerMemberInfo lastRoleInfo;
  private boolean roleHasMore = false;

  public MutableLiveData<FetchResult<List<QChatBaseBean>>> getResultLiveData() {
    return resultLiveData;
  }

  public MutableLiveData<FetchResult<QChatChannelMember>> getAddLiveData() {
    return addLiveData;
  }

  public MutableLiveData<FetchResult<QChatChannelMember>> getRemoveLiveData() {
    return removeLiveData;
  }

  public void fetchMemberList(long serverId, long channelId, QChatChannelModeEnum type) {
    fetchMemberData(serverId, channelId, type, 0);
  }

  /** load header entrance */
  public ArrayList<QChatBaseBean> loadHeader() {
    ArrayList<QChatBaseBean> addList = new ArrayList<>();
    QChatArrowBean addMember = new QChatArrowBean("添加成员", 0, 0);
    addList.add(addMember);
    return addList;
  }

  /** fetch member list */
  private void fetchMemberData(
      long serverId, long channelId, QChatChannelModeEnum type, long offset) {
    ALog.d(TAG, "fetchMemberData");
    QChatChannelRepo.fetchChannelBlackWhiteMembers(
        serverId,
        channelId,
        offset,
        type,
        QChatConstant.MEMBER_PAGE_SIZE,
        new FetchCallback<List<QChatServerMemberInfo>>() {
          @Override
          public void onSuccess(@Nullable List<QChatServerMemberInfo> param) {
            ArrayList<QChatBaseBean> addList = new ArrayList<>();
            if (param != null && param.size() > 0) {
              for (int index = 0; index < param.size(); index++) {
                QChatServerMemberInfo roleInfo = param.get(index);
                QChatServerMemberBean bean = new QChatServerMemberBean(roleInfo);
                addList.add(bean);
              }
              lastRoleInfo = param.get(param.size() - 1);
            }
            roleHasMore = param != null && param.size() >= QChatConstant.MEMBER_PAGE_SIZE;
            if (offset == 0) {
              fetchResult.setLoadStatus(LoadStatus.Success);
            } else {
              fetchResult.setFetchType(FetchResult.FetchType.Add);
              fetchResult.setTypeIndex(-1);
            }
            ALog.d(TAG, "fetchMemberData", "onSuccess" + addList.size());
            fetchResult.setData(addList);
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

  /** delete member from channel black or white name list */
  public void deleteMember(
      long serverId, long channelId, int channelType, String accId, int position) {
    ArrayList<String> accIdList = new ArrayList<>();
    accIdList.add(accId);
    QChatChannelRepo.removeChannelBlackWhiteMembers(
        serverId,
        channelId,
        channelType,
        accIdList,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(TAG, "deleteMember", "onSuccess" + accId);
            removeResult.setFetchType(FetchResult.FetchType.Remove);
            removeResult.setTypeIndex(position);
            removeLiveData.setValue(removeResult);
          }

          @Override
          public void onFailed(int code) {
            removeResult.setError(code, R.string.qchat_channel_member_delete_error);
            removeLiveData.setValue(removeResult);
            ALog.d(TAG, "fetchMemberData", "onFailed" + code);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            String errorMsg = exception != null ? exception.getMessage() : "";
            ALog.d(TAG, "fetchMemberData", "onException" + errorMsg);
            removeResult.setError(
                QChatConstant.ERROR_CODE_CHANNEL_MEMBER_ADD,
                R.string.qchat_channel_member_delete_error);
            removeLiveData.setValue(removeResult);
          }
        });
  }

  /** add member from channel black or white name list */
  public void addMember(
      long serverId, long channelId, List<String> accIdList, QChatChannelModeEnum type) {
    QChatChannelRepo.addChannelBlackWhiteMembers(
        serverId,
        channelId,
        accIdList,
        type,
        new FetchCallback<Void>() {

          @Override
          public void onException(@Nullable Throwable exception) {
            String errorMsg = exception != null ? exception.getMessage() : "";
            ALog.d(TAG, "fetchMemberData", "onException" + errorMsg);
            addResult.setError(
                QChatConstant.ERROR_CODE_CHANNEL_MEMBER_ADD,
                R.string.qchat_channel_member_add_error);
            addLiveData.postValue(addResult);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(TAG, "addMember", "onFailed" + code);
            if (code == QChatConstant.ERROR_CODE_IM_NO_PERMISSION) {
              addResult.setError(code, R.string.qchat_no_permission);
            } else {
              addResult.setError(code, R.string.qchat_channel_member_add_error);
            }
            addLiveData.postValue(addResult);
          }

          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(TAG, "deleteMember", "onSuccess" + accIdList.size());
            addResult.setLoadStatus(LoadStatus.Success);
            addLiveData.postValue(addResult);
          }
        });
  }

  /** fetch more member list */
  public void loadMore(long serverId, long channelId, QChatChannelModeEnum type) {
    long offset = 0;
    if (lastRoleInfo != null) {
      offset = lastRoleInfo.getCreateTime();
    }
    fetchMemberData(serverId, channelId, type, offset);
  }

  public boolean hasMore() {
    return roleHasMore;
  }
}
