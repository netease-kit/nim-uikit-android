// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel.permission;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.qchatkit.repo.QChatChannelRepo;
import com.netease.yunxin.kit.qchatkit.repo.QChatRoleRepo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelMember;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelRoleInfo;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatArrowBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatBaseBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatChannelMemberBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatChannelRoleBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatCommonBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatMoreBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatTitleBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatViewType;
import com.netease.yunxin.kit.qchatkit.ui.utils.ErrorUtils;
import java.util.ArrayList;
import java.util.List;

/** channel permission setting view model modify member and role permission */
public class ChannelPermissionViewModel extends BaseViewModel {

  private static final String TAG = "ChannelPermissionViewModel";

  private final MutableLiveData<FetchResult<List<QChatBaseBean>>> roleLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<QChatBaseBean>> roleFetchResult =
      new FetchResult<>(LoadStatus.Finish);
  private final MutableLiveData<FetchResult<List<QChatBaseBean>>> roleMoreLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<QChatBaseBean>> roleMoreFetchResult =
      new FetchResult<>(LoadStatus.Finish);
  private final MutableLiveData<FetchResult<List<QChatBaseBean>>> memberLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<QChatBaseBean>> memberFetchResult =
      new FetchResult<>(LoadStatus.Finish);
  private ArrayList<QChatBaseBean> moreRoleList = new ArrayList<>();
  //member list default page size
  private final int memberPageSize = 100;
  //role list request page size
  private final int rolePageLimit = 100;
  //role list show size
  private final int rolePageSize = 5;

  private long serverId;
  private long channelId;
  private QChatChannelRoleInfo lastRoleInfo;
  private QChatChannelMember lastMemberInfo;
  private QChatMoreBean roleMoreBean;
  private int moreRoleIndex = 0;
  private boolean memberHasMore = false;

  /** role list live data */
  public MutableLiveData<FetchResult<List<QChatBaseBean>>> getRoleLiveData() {
    return roleLiveData;
  }

  /** role more item live data that to add or remove more item */
  public MutableLiveData<FetchResult<List<QChatBaseBean>>> getRoleMoreLiveData() {
    return roleMoreLiveData;
  }

  /** member list live data */
  public MutableLiveData<FetchResult<List<QChatBaseBean>>> getMemberLiveData() {
    return memberLiveData;
  }

  /** fetch role data base on serverId and channelId */
  public void fetchData(long serverId, long channelId) {
    ALog.d(TAG, "fetchData", "serverId:" + serverId + "channelId:" + channelId);
    this.serverId = serverId;
    this.channelId = channelId;
    moreRoleIndex = 0;
    fetchRoleData(serverId, channelId, 0);
    fetchMemberData(serverId, channelId, 0);
  }

  /** delete role or member from channel */
  public void delete(QChatBaseBean data, int position) {
    if (data instanceof QChatChannelRoleBean) {
      deleteRole((QChatChannelRoleBean) data, position);
    } else if (data instanceof QChatChannelMemberBean) {
      deleteMember((QChatChannelMemberBean) data, position);
    }
  }

  private void fetchRoleData(long serverId, long channelId, long offset) {
    ALog.d(TAG, "fetchRoleData", "serverId:" + serverId + "channelId:" + channelId);
    QChatChannelRepo.fetchChannelRoles(
        serverId,
        channelId,
        offset,
        rolePageLimit,
        new FetchCallback<List<QChatChannelRoleInfo>>() {
          @Override
          public void onSuccess(@Nullable List<QChatChannelRoleInfo> param) {
            ArrayList<QChatBaseBean> addList = new ArrayList<>();
            moreRoleList.clear();
            if (param != null && param.size() > 0) {
              ALog.d(TAG, "fetchRoleData", "onSuccess:" + param.size());
              if (offset == 0) {
                QChatBaseBean titleBean = new QChatTitleBean(R.string.qchat_role_list_title);
                addList.add(titleBean);
              }
              int size = Math.min(param.size(), rolePageSize);
              for (int index = 0; index < param.size(); index++) {
                QChatChannelRoleInfo roleInfo = param.get(index);
                int topRadius = index == 0 && offset == 0 ? QChatConstant.CORNER_RADIUS_ARROW : 0;
                QChatChannelRoleBean bean = new QChatChannelRoleBean(roleInfo, topRadius, 0);
                bean.router = QChatConstant.ROUTER_ROLE_PERMISSION;
                if (index < size) {
                  addList.add(bean);
                } else {
                  moreRoleList.add(bean);
                }
              }
              lastRoleInfo = param.get(size - 1);
              if (offset == 0 && rolePageSize < param.size()) {
                roleMoreBean = new QChatMoreBean(R.string.qchat_more_title);
                roleMoreBean.extend = String.valueOf(param.size());
                addList.add(roleMoreBean);
                size++;
              }
              addList.add(new QChatCommonBean(QChatViewType.CORNER_VIEW_TYPE));
              roleFetchResult.setData(addList);
              FetchResult.FetchType type = FetchResult.FetchType.Add;
              roleFetchResult.setFetchType(type);
              roleFetchResult.setTypeIndex(moreRoleIndex);
              roleLiveData.setValue(roleFetchResult);
              moreRoleIndex = moreRoleIndex + size;
            }
          }

          @Override
          public void onFailed(int code) {
            ALog.d(TAG, "fetchRoleData", "onFailed:" + code);
            roleFetchResult.setError(
                code, ErrorUtils.getErrorText(code, R.string.qchat_channel_fetch_role_error));
            roleLiveData.setValue(roleFetchResult);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            String errorMsg = exception != null ? exception.getMessage() : "";
            ALog.d(TAG, "fetchRoleData", "exception:" + errorMsg);
            roleFetchResult.setError(
                QChatConstant.ERROR_CODE_CHANNEL_ROLE_FETCH,
                R.string.qchat_channel_fetch_role_error);
            roleLiveData.setValue(roleFetchResult);
          }
        });
  }

  private void removeRoleMoreData(long offset) {
    if (offset > 0) {
      ArrayList<QChatBaseBean> removeList = new ArrayList<>();
      removeList.add(roleMoreBean);
      roleMoreFetchResult.setData(removeList);
      FetchResult.FetchType type = FetchResult.FetchType.Remove;
      roleMoreFetchResult.setFetchType(type);
      roleMoreFetchResult.setTypeIndex(moreRoleIndex);
      roleMoreLiveData.setValue(roleMoreFetchResult);
    }
  }

  /** delete role from channel */
  public void deleteRole(QChatChannelRoleBean bean, int position) {
    QChatRoleRepo.deleteChannelRole(
        serverId,
        channelId,
        bean.channelRole.getRoleId(),
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(TAG, "deleteRole", "onSuccess:" + position);
            FetchResult.FetchType type = FetchResult.FetchType.Remove;
            roleFetchResult.setFetchType(type);
            roleFetchResult.setTypeIndex(position);
            roleLiveData.setValue(roleFetchResult);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(TAG, "deleteRole", "code:" + code);
            roleFetchResult.setError(
                code, ErrorUtils.getErrorText(code, R.string.qchat_channel_role_delete_error));
            roleLiveData.setValue(roleFetchResult);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            String errorMsg = exception != null ? exception.getMessage() : "";
            ALog.d(TAG, "deleteRole", "exception:" + errorMsg);
            roleFetchResult.setError(
                QChatConstant.ERROR_CODE_CHANNEL_ROLE_DELETE,
                R.string.qchat_channel_role_delete_error);
            roleLiveData.setValue(roleFetchResult);
          }
        });
  }

  public void fetchMemberData(long serverId, long channelId, long offset) {
    QChatChannelRepo.fetchChannelRoleMembers(
        serverId,
        channelId,
        offset,
        memberPageSize,
        new FetchCallback<List<QChatChannelMember>>() {
          @Override
          public void onSuccess(@Nullable List<QChatChannelMember> param) {
            ALog.d(TAG, "fetchMemberData", "onSuccess:" + serverId + "," + channelId);
            ArrayList<QChatBaseBean> addList = new ArrayList<>();
            if (param != null && param.size() > 0) {
              if (offset == 0) {
                QChatBaseBean titleBean = new QChatTitleBean(R.string.qchat_member_list_title);
                addList.add(titleBean);
              }

              for (int index = 0; index < param.size(); index++) {
                QChatChannelMember roleInfo = param.get(index);
                QChatChannelMemberBean bean = new QChatChannelMemberBean(roleInfo);
                if (index == 0 && offset == 0) {
                  bean.topRadius = QChatConstant.CORNER_RADIUS_ARROW;
                }
                addList.add(bean);
              }
              addList.add(new QChatCommonBean(QChatViewType.CORNER_VIEW_TYPE));

              lastMemberInfo = param.get(param.size() - 1);
              memberFetchResult.setData(addList);
              FetchResult.FetchType type = FetchResult.FetchType.Add;
              memberFetchResult.setFetchType(type);
              memberFetchResult.setTypeIndex(-1);
              memberLiveData.setValue(memberFetchResult);
            }

            memberHasMore = param != null && param.size() >= memberPageSize;
          }

          @Override
          public void onFailed(int code) {
            ALog.d(TAG, "fetchMemberData", "onFailed:" + code);
            memberFetchResult.setError(
                code, ErrorUtils.getErrorText(code, R.string.qchat_channel_fetch_member_error));
            memberLiveData.setValue(memberFetchResult);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            String errorMsg = exception != null ? exception.getMessage() : "";
            ALog.d(TAG, "fetchMemberData", "exception:" + errorMsg);
            memberFetchResult.setError(
                QChatConstant.ERROR_CODE_CHANNEL_MEMBER_FETCH,
                R.string.qchat_channel_fetch_member_error);
            memberLiveData.setValue(memberFetchResult);
          }
        });
  }

  /** delete member from channel */
  public void deleteMember(QChatChannelMemberBean bean, int position) {
    ALog.d(
        TAG,
        "deleteMember",
        "info:" + serverId + "," + channelId + "," + bean.channelMember.getAccId());
    QChatRoleRepo.deleteChannelMemberRole(
        serverId,
        channelId,
        bean.channelMember.getAccId(),
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(TAG, "deleteMember", "onSuccess");
            memberFetchResult.setFetchType(FetchResult.FetchType.Remove);
            memberFetchResult.setTypeIndex(position);
            memberLiveData.setValue(memberFetchResult);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(TAG, "deleteMember", "onFailed:" + code);
            memberFetchResult.setError(
                code, ErrorUtils.getErrorText(code, R.string.qchat_channel_member_delete_error));
            memberLiveData.setValue(memberFetchResult);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            String errorMsg = exception != null ? exception.getMessage() : "";
            ALog.d(TAG, "deleteMember", "exception:" + errorMsg);
            memberFetchResult.setError(
                QChatConstant.ERROR_CODE_CHANNEL_ROLE_DELETE,
                R.string.qchat_channel_member_delete_error);
            memberLiveData.setValue(memberFetchResult);
          }
        });
  }

  public void loadMoreRole() {
    if (moreRoleList.size() > 0) {
      ALog.d(TAG, "loadMoreRole");
      FetchResult.FetchType type = FetchResult.FetchType.Add;
      roleFetchResult.setFetchType(type);
      roleFetchResult.setTypeIndex(moreRoleIndex);
      ArrayList<QChatBaseBean> addMore = new ArrayList<>();
      addMore.addAll(moreRoleList);
      moreRoleList.clear();
      roleFetchResult.setData(addMore);
      roleLiveData.setValue(roleFetchResult);
      moreRoleIndex += addMore.size();
      removeRoleMoreData(moreRoleIndex);
    }
  }

  public void loadMoreMember() {
    if (memberHasMore) {
      long offset = lastMemberInfo != null ? lastMemberInfo.getCreateTime() : 0;
      fetchMemberData(serverId, channelId, offset);
      ALog.d(TAG, "loadMoreMember");
    }
  }

  public boolean hasMore() {
    return memberHasMore;
  }

  /** get list header include add member and add role */
  public List<QChatBaseBean> getHeaderData(String[] titleArray, String[] routerArray) {
    if (titleArray == null || titleArray.length < 1) {
      return null;
    }
    List<QChatBaseBean> data = new ArrayList<>();
    for (int index = 0; index < titleArray.length; index++) {
      QChatArrowBean addRole = new QChatArrowBean(titleArray[index], 0, 0);
      if (index == 0) {
        addRole.topRadius = QChatConstant.CORNER_RADIUS_ARROW;
      }
      if (index == titleArray.length - 1) {
        addRole.bottomRadius = QChatConstant.CORNER_RADIUS_ARROW;
      }
      addRole.router =
          routerArray != null && routerArray.length > index ? routerArray[index] : null;
      data.add(addRole);
      ALog.d(TAG, "getHeaderData", "title:" + titleArray[index]);
    }
    return data;
  }
}
