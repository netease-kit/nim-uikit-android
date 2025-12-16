// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.selector.forward;

import static com.netease.yunxin.kit.contactkit.ui.ContactConstant.LIB_TAG;
import static com.netease.yunxin.kit.contactkit.ui.ContactUIConfig.DEFAULT_SESSION_MAX_SELECT_COUNT;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.coexist.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.coexist.sdk.v2.conversation.model.V2NIMBaseConversation;
import com.netease.nimlib.coexist.sdk.v2.conversation.model.V2NIMConversation;
import com.netease.nimlib.coexist.sdk.v2.conversation.result.V2NIMConversationResult;
import com.netease.nimlib.coexist.sdk.v2.conversation.result.V2NIMLocalConversationResult;
import com.netease.nimlib.coexist.sdk.v2.team.model.V2NIMTeam;
import com.netease.nimlib.coexist.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.ConversationSearchInfo;
import com.netease.yunxin.kit.chatkit.model.FriendSearchInfo;
import com.netease.yunxin.kit.chatkit.model.RecentForward;
import com.netease.yunxin.kit.chatkit.model.TeamSearchInfo;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.chatkit.repo.LocalConversationRepo;
import com.netease.yunxin.kit.chatkit.repo.SearchRepo;
import com.netease.yunxin.kit.chatkit.repo.SettingRepo;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.indexbar.helper.IIndexBarDataHelper;
import com.netease.yunxin.kit.contactkit.ui.indexbar.helper.IndexBarDataHelperImpl;
import com.netease.yunxin.kit.contactkit.ui.model.ContactFriendBean;
import com.netease.yunxin.kit.contactkit.ui.model.SelectableBean;
import com.netease.yunxin.kit.contactkit.ui.model.SelectedViewBean;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.coexist.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.coexist.im2.model.UserWithFriend;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 联系人选择器 ViewModel */
public class ContactSelectorViewModel extends BaseViewModel {

  private final String TAG = "ContactSelectorViewModel";

  //请求回话列表的数量限制
  private static final int CONVERSATION_PAGE_LIMIT = 100;

  //是否多选模式
  private boolean isMultiSelectMode = false;

  private final MutableLiveData<Boolean> isMultiSelectModeLiveData = new MutableLiveData<>(false);

  // 群列表数据
  private final MutableLiveData<FetchResult<List<SelectableBean<V2NIMTeam>>>> teamListLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<SelectableBean<V2NIMTeam>>> teamListResult =
      new FetchResult<>(LoadStatus.Finish);
  //所有群组数据
  private final List<SelectableBean<V2NIMTeam>> allTeamList = new ArrayList<>();

  //好友列表数据
  private final MutableLiveData<FetchResult<List<ContactFriendBean>>> friendListLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<ContactFriendBean>> friendListResult =
      new FetchResult<>(LoadStatus.Finish);

  //所有好友数据
  private final List<ContactFriendBean> allFriendList = new ArrayList<>();

  // 群列表数据
  private final MutableLiveData<FetchResult<List<SelectableBean<V2NIMBaseConversation>>>>
      conversationListLiveData = new MutableLiveData<>();
  private final FetchResult<List<SelectableBean<V2NIMBaseConversation>>> conversationListResult =
      new FetchResult<>(LoadStatus.Finish);
  //所有会话数据
  private final List<SelectableBean<V2NIMBaseConversation>> allConversationList = new ArrayList<>();

  //最近转发数据
  private final MutableLiveData<FetchResult<List<SelectableBean<RecentForward>>>>
      recentForwardListLiveData = new MutableLiveData<>();
  private final FetchResult<List<SelectableBean<RecentForward>>> recentForwardListResult =
      new FetchResult<>(LoadStatus.Finish);

  //所有最近转发数据
  final List<RecentForward> allRecentForwardList = new ArrayList<>();

  //选中的数据
  private final MutableLiveData<List<SelectedViewBean>> selectedLiveData = new MutableLiveData<>();

  private final List<SelectedViewBean> selectedList = new ArrayList<>();

  //搜索关键字
  private String searchKey;

  //搜索类型,当前选中的Fragment对应
  private SearchType searchType = SearchType.CONVERSATION;

  private final MutableLiveData<String> searchKeyLiveData = new MutableLiveData<>();

  //搜索结果
  //好友搜索结果
  private final MutableLiveData<FetchResult<List<ContactFriendBean>>> searchFriendResultLiveData =
      new MutableLiveData<>();

  private final FetchResult<List<ContactFriendBean>> searchFriendResult =
      new FetchResult<>(LoadStatus.Finish);

  //群组搜索结果
  private final MutableLiveData<FetchResult<List<SelectableBean<V2NIMTeam>>>>
      searchTeamResultLiveData = new MutableLiveData<>();

  private final FetchResult<List<SelectableBean<V2NIMTeam>>> searchTeamResult =
      new FetchResult<>(LoadStatus.Finish);

  //会话搜索结果
  private final MutableLiveData<FetchResult<List<SelectableBean<V2NIMBaseConversation>>>>
      searchConversationResultLiveData = new MutableLiveData<>();

  private final FetchResult<List<SelectableBean<V2NIMBaseConversation>>> searchConversationResult =
      new FetchResult<>(LoadStatus.Finish);

  /** 加载联系人 */
  public void loadFriends() {
    ContactRepo.getContactList(
        false,
        new FetchCallback<List<UserWithFriend>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "getContactList,onFailed:" + errorCode);
            friendListResult.setError(errorCode, errorMsg);
            friendListLiveData.postValue(friendListResult);
          }

          @Override
          public void onSuccess(@Nullable List<UserWithFriend> data) {
            ALog.d(
                LIB_TAG, TAG, "getContactList,onSuccess:" + (data == null ? "null" : data.size()));
            friendListResult.setStatus(LoadStatus.Success);
            List<ContactFriendBean> selectableFriend = new ArrayList<>();
            if (data != null) {
              for (UserWithFriend friend : data) {
                ContactFriendBean contactFriend = new ContactFriendBean(friend);
                contactFriend.isSelected = isFriendSelected(friend);
                selectableFriend.add(contactFriend);
              }
            }
            allFriendList.addAll(selectableFriend);
            sortFriendList();
            friendListResult.setData(allFriendList);
            friendListLiveData.postValue(friendListResult);
            searchFriend();
          }
        });
  }

  //好友列表排序
  void sortFriendList() {
    IIndexBarDataHelper indexBarDataHelper = new IndexBarDataHelperImpl();
    indexBarDataHelper.sortSourceData(allFriendList);
  }

  public List<RecentForward> getAllRecentForwardList() {
    return allRecentForwardList;
  }

  /** 加载最近会话 */
  public void loadRecentConversation() {
    conversationListResult.setStatus(LoadStatus.Loading);
    conversationListLiveData.postValue(conversationListResult);
    TeamRepo.getTeamList(
        new FetchCallback<List<V2NIMTeam>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "getTeamList,onFailed:" + errorCode);
            queryConversationList();
          }

          @Override
          public void onSuccess(@Nullable List<V2NIMTeam> data) {
            ALog.d(LIB_TAG, TAG, "getTeamList,onSuccess:" + (data == null ? "null" : data.size()));
            //先将群组列表缓存
            List<SelectableBean<V2NIMTeam>> teamBeanList = new ArrayList<>();
            if (data != null && data.size() > 0) {
              for (V2NIMTeam teamInfo : data) {
                SelectableBean<V2NIMTeam> teamBean = new SelectableBean<>(teamInfo);
                teamBeanList.add(teamBean);
              }
              allTeamList.addAll(teamBeanList);
            }
            //拉取会话列表
            queryConversationList();
          }
        });
  }

  /** 拉取会话列表，在群列表请求完成之后进行 */
  private void queryConversationList() {
    if (IMKitClient.enableV2CloudConversation()) {
      ConversationRepo.getConversationList(
          0,
          CONVERSATION_PAGE_LIMIT,
          new FetchCallback<V2NIMConversationResult>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              ALog.d(LIB_TAG, TAG, "getConversationList,onFailed:" + errorCode);
              conversationListResult.setError(errorCode, errorMsg);
              conversationListResult.setStatus(LoadStatus.Error);
              conversationListLiveData.postValue(conversationListResult);
            }

            @Override
            public void onSuccess(@Nullable V2NIMConversationResult data) {
              ALog.d(
                  LIB_TAG,
                  TAG,
                  "getConversationList,onSuccess:"
                      + (data == null ? "null" : data.getConversationList().size()));
              if (data != null) {
                allConversationList.clear();
                conversationListResult.setStatus(LoadStatus.Success);
                Set<SelectableBean<V2NIMBaseConversation>> selectableConversation = new HashSet<>();
                for (V2NIMConversation conversation : data.getConversationList()) {
                  SelectableBean<V2NIMBaseConversation> selectableBean =
                      new SelectableBean<>(conversation);
                  //群组会话填充人数
                  if (conversation.getType() == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM
                      && !allTeamList.isEmpty()) {
                    SelectableBean<V2NIMTeam> team =
                        findTeamById(
                            V2NIMConversationIdUtil.conversationTargetId(
                                conversation.getConversationId()));
                    if (team != null) {
                      selectableBean.memberCount = team.data.getMemberCount();
                    }
                  }
                  if (!selectableConversation.contains(selectableBean)) {
                    selectableConversation.add(selectableBean);
                    allConversationList.add(selectableBean);
                  }
                }
                conversationListResult.setData(allConversationList);
              } else {
                conversationListResult.setData(null);
                conversationListResult.setStatus(LoadStatus.Error);
              }
              conversationListLiveData.postValue(conversationListResult);
            }
          });
    } else {
      LocalConversationRepo.getConversationList(
          0,
          CONVERSATION_PAGE_LIMIT,
          new FetchCallback<V2NIMLocalConversationResult>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              ALog.d(LIB_TAG, TAG, "getConversationList,onFailed:" + errorCode);
              conversationListResult.setError(errorCode, errorMsg);
              conversationListResult.setStatus(LoadStatus.Error);
              conversationListLiveData.postValue(conversationListResult);
            }

            @Override
            public void onSuccess(@Nullable V2NIMLocalConversationResult data) {
              ALog.d(
                  LIB_TAG,
                  TAG,
                  "getConversationList,onSuccess:"
                      + (data == null ? "null" : data.getConversationList().size()));
              if (data != null) {
                allConversationList.clear();
                conversationListResult.setStatus(LoadStatus.Success);
                Set<SelectableBean<V2NIMBaseConversation>> selectableConversation = new HashSet<>();
                for (V2NIMBaseConversation conversation : data.getConversationList()) {
                  SelectableBean<V2NIMBaseConversation> selectableBean =
                      new SelectableBean<>(conversation);
                  //群组会话填充人数
                  if (conversation.getType() == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM
                      && !allTeamList.isEmpty()) {
                    SelectableBean<V2NIMTeam> team =
                        findTeamById(
                            V2NIMConversationIdUtil.conversationTargetId(
                                conversation.getConversationId()));
                    if (team != null) {
                      selectableBean.memberCount = team.data.getMemberCount();
                    }
                  }
                  if (!selectableConversation.contains(selectableBean)) {
                    selectableConversation.add(selectableBean);
                    allConversationList.add(selectableBean);
                  }
                }
                conversationListResult.setData(allConversationList);
              } else {
                conversationListResult.setData(null);
                conversationListResult.setStatus(LoadStatus.Error);
              }
              conversationListLiveData.postValue(conversationListResult);
            }
          });
    }
  }

  //根据ID查找群
  private SelectableBean<V2NIMTeam> findTeamById(String tid) {
    for (SelectableBean<V2NIMTeam> team : allTeamList) {
      if (team.data.getTeamId().equals(tid)) {
        return team;
      }
    }
    return null;
  }

  //根据ID查找会话
  private SelectableBean<V2NIMBaseConversation> findConversationById(String conversationId) {
    for (SelectableBean<V2NIMBaseConversation> conversation : allConversationList) {
      if (conversation.data.getConversationId().equals(conversationId)) {
        return conversation;
      }
    }
    return null;
  }

  //根据ID查找好友
  private ContactFriendBean findFriendById(String accId) {
    for (ContactFriendBean friend : allFriendList) {
      if (friend.data.getAccount().equals(accId)) {
        return friend;
      }
    }
    return null;
  }

  /** 加载群组列表 */
  public void loadTeamList() {
    ALog.d(LIB_TAG, TAG, "getTeamList");
    if (!allTeamList.isEmpty()) {
      Collections.sort(allTeamList, teamComparator);
      teamListResult.setData(allTeamList);
      teamListResult.setStatus(LoadStatus.Success);
      teamListLiveData.postValue(teamListResult);
      searchTeam();
      return;
    }
    teamListResult.setStatus(LoadStatus.Loading);
    teamListLiveData.postValue(teamListResult);
    TeamRepo.getTeamList(
        new FetchCallback<List<V2NIMTeam>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "getTeamList,onFailed:" + errorCode);
            teamListResult.setError(errorCode, errorMsg);
            teamListLiveData.postValue(teamListResult);
          }

          @Override
          public void onSuccess(@Nullable List<V2NIMTeam> data) {
            ALog.d(LIB_TAG, TAG, "getTeamList,onSuccess:" + (data == null ? "null" : data.size()));
            List<SelectableBean<V2NIMTeam>> teamBeanList = new ArrayList<>();
            if (data != null && data.size() > 0) {
              teamListResult.setStatus(LoadStatus.Success);
              for (V2NIMTeam teamInfo : data) {
                SelectableBean<V2NIMTeam> teamBean = new SelectableBean<>(teamInfo);
                teamBean.isSelected = isTeamSelected(teamInfo);
                teamBeanList.add(teamBean);
              }
              Collections.sort(teamBeanList, teamComparator);
              allTeamList.addAll(teamBeanList);
              teamListResult.setData(teamBeanList);
            } else {
              teamListResult.setData(null);
              teamListResult.setStatus(LoadStatus.Success);
            }
            teamListLiveData.postValue(teamListResult);
            searchTeam();
          }
        });
  }

  //检查是否选中
  private boolean isTeamSelected(V2NIMTeam team) {
    for (SelectedViewBean selected : selectedList) {
      if (V2NIMConversationIdUtil.conversationType(selected.getTargetId())
              == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM
          && V2NIMConversationIdUtil.conversationTargetId(selected.getTargetId())
              .equals(team.getTeamId())) {
        return true;
      }
    }
    return false;
  }

  //检查是否选中
  private boolean isFriendSelected(UserWithFriend friend) {
    for (SelectedViewBean selected : selectedList) {
      if (V2NIMConversationIdUtil.conversationType(selected.getTargetId())
              == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P
          && V2NIMConversationIdUtil.conversationTargetId(selected.getTargetId())
              .equals(friend.getAccount())) {
        return true;
      }
    }
    return false;
  }

  /**
   * 检查会话是否选中
   *
   * @param conversation 会话
   * @return 是否选中
   */
  private boolean isConversationSelected(V2NIMBaseConversation conversation) {
    for (SelectedViewBean selected : selectedList) {
      if (selected.getTargetId().equals(conversation.getConversationId())) {
        return true;
      }
    }
    return false;
  }

  // 群组排序
  private final Comparator<SelectableBean<V2NIMTeam>> teamComparator =
      (bean1, bean2) -> {
        int result;
        if (bean1 == null) {
          result = 1;
        } else if (bean2 == null) {
          result = -1;
        } else if (bean1.data.getCreateTime() >= bean2.data.getCreateTime()) {
          result = -1;
        } else {
          result = 0;
        }
        return result;
      };

  /** 加载最近转发 */
  public void loadRecentForward() {
    SettingRepo.getRecentForward(
        new FetchCallback<List<RecentForward>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "getRecentForward,onFailed:" + errorCode);
            recentForwardListResult.setError(errorCode, errorMsg);
            recentForwardListLiveData.postValue(recentForwardListResult);
          }

          @Override
          public void onSuccess(@Nullable List<RecentForward> data) {
            ALog.d(
                LIB_TAG,
                TAG,
                "getRecentForward,onSuccess:" + (data == null ? "null" : data.size()));
            recentForwardListResult.setStatus(LoadStatus.Success);
            List<SelectableBean<RecentForward>> selectableRecentForward = new ArrayList<>();
            if (data != null) {
              allRecentForwardList.addAll(data);
              for (RecentForward recentForward : data) {
                selectableRecentForward.add(new SelectableBean<>(recentForward));
              }
            }
            recentForwardListResult.setData(selectableRecentForward);
            recentForwardListLiveData.postValue(recentForwardListResult);
          }
        });
  }

  public void setMultiSelectMode(boolean multiSelectMode) {
    isMultiSelectMode = multiSelectMode;
    isMultiSelectModeLiveData.postValue(multiSelectMode);
  }

  public boolean isMultiSelectMode() {
    return isMultiSelectMode;
  }

  /**
   * 获取搜索关键字
   *
   * @return
   */
  public String getSearchKey() {
    return searchKey;
  }

  /**
   * 检查已选数量是否超限
   *
   * @return 是否超限
   */
  public boolean selectCountOverflow() {
    return selectedList.size() >= DEFAULT_SESSION_MAX_SELECT_COUNT;
  }

  /**
   * 选择好友
   *
   * @param friend 好友
   * @param isSelected 是否选中
   */
  public void selectFriend(UserWithFriend friend, boolean isSelected) {
    friendListResult.setStatus(LoadStatus.Finish);
    ContactFriendBean selectableBean = findFriendById(friend.getAccount());
    if (selectableBean == null) {
      selectableBean = new ContactFriendBean(friend);
    }
    selectableBean.isSelected = isSelected;
    friendListResult.setData(Collections.singletonList(selectableBean));
    friendListLiveData.postValue(friendListResult);
    //处理选中的好友
    SelectedViewBean bean =
        new SelectedViewBean(
            friend.getAvatar(),
            friend.getAvatarName(),
            V2NIMConversationIdUtil.conversationId(
                friend.getAccount(), V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P),
            friend.getAccount());
    onSelectedChanged(bean, isSelected);
    //检查会话和最近转发
    checkSelectedRecentForward(
        friend.getAccount(), V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P, isSelected);
    checkSelectConversation(
        friend.getAccount(), V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P, isSelected);
  }

  /**
   * 选择群组
   *
   * @param team 群组
   * @param isSelected 是否选中
   */
  public void selectTeam(V2NIMTeam team, boolean isSelected) {
    teamListResult.setStatus(LoadStatus.Finish);
    SelectableBean<V2NIMTeam> selectableBean = findTeamById(team.getTeamId());
    if (selectableBean == null) {
      selectableBean = new SelectableBean<>(team);
    }
    selectableBean.isSelected = isSelected;
    teamListResult.setData(Collections.singletonList(selectableBean));
    teamListLiveData.postValue(teamListResult);
    SelectedViewBean bean =
        new SelectedViewBean(
            team.getAvatar(),
            team.getName(),
            V2NIMConversationIdUtil.conversationId(
                team.getTeamId(), V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM),
            team.getTeamId(),
            team.getMemberCount());
    onSelectedChanged(bean, isSelected);
    //检查会话和最近转发
    checkSelectedRecentForward(
        team.getTeamId(), V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM, isSelected);
    checkSelectConversation(
        team.getTeamId(), V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM, isSelected);
  }

  /**
   * 选择会话
   *
   * @param conversation 会话
   * @param isSelected 是否选中
   */
  public void selectConversation(V2NIMBaseConversation conversation, boolean isSelected) {
    conversationListResult.setStatus(LoadStatus.Finish);
    SelectableBean<V2NIMBaseConversation> selectableBean =
        findConversationById(conversation.getConversationId());
    if (selectableBean == null) {
      selectableBean = new SelectableBean<>(conversation);
    }
    selectableBean.isSelected = isSelected;
    conversationListResult.setData(Collections.singletonList(selectableBean));
    conversationListLiveData.postValue(conversationListResult);
    int count =
        getMemberCountByConversationId(
            V2NIMConversationIdUtil.conversationTargetId(conversation.getConversationId()),
            conversation.getType());
    SelectedViewBean bean =
        new SelectedViewBean(
            conversation.getAvatar(),
            conversation.getName(),
            conversation.getConversationId(),
            V2NIMConversationIdUtil.conversationTargetId(conversation.getConversationId()),
            count);
    onSelectedChanged(bean, isSelected);
    //检查最近转发，群组，和好友
    checkSelectedRecentForward(
        V2NIMConversationIdUtil.conversationTargetId(conversation.getConversationId()),
        conversation.getType(),
        isSelected);
    if (conversation.getType() == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      checkSelectedFriend(
          V2NIMConversationIdUtil.conversationTargetId(conversation.getConversationId()),
          isSelected);
    } else if (conversation.getType() == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM) {
      checkSelectTeam(
          V2NIMConversationIdUtil.conversationTargetId(conversation.getConversationId()),
          isSelected);
    }
  }

  /**
   * 获取会话成员数量
   *
   * @param sessionId 会话Id
   * @param conversationType 会话类型
   * @return 成员数量
   */
  private int getMemberCountByConversationId(
      String sessionId, V2NIMConversationType conversationType) {
    if (conversationType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM) {
      SelectableBean<V2NIMTeam> team = findTeamById(sessionId);
      if (team != null) {
        return team.data.getMemberCount();
      }
    }
    return 0;
  }

  /**
   * 选择最近转发
   *
   * @param recentForward 最近转发
   * @param isSelected 是否选中
   */
  public void selectRecentForward(RecentForward recentForward, boolean isSelected) {
    recentForwardListResult.setStatus(LoadStatus.Finish);
    SelectableBean<RecentForward> selectableBean = new SelectableBean<>(recentForward);
    selectableBean.isSelected = isSelected;
    recentForwardListResult.setData(Collections.singletonList(selectableBean));
    recentForwardListLiveData.postValue(recentForwardListResult);
    SelectedViewBean bean =
        new SelectedViewBean(
            recentForward.getAvatar(),
            recentForward.getName(),
            V2NIMConversationIdUtil.conversationId(
                recentForward.getSessionId(), recentForward.getSessionType()),
            recentForward.getSessionId(),
            recentForward.getCount());
    onSelectedChanged(bean, isSelected);
    //检查会话，群组，和好友
    checkSelectConversation(
        recentForward.getSessionId(), recentForward.getSessionType(), isSelected);
    if (recentForward.getSessionType() == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      checkSelectedFriend(recentForward.getSessionId(), isSelected);
    } else if (recentForward.getSessionType()
        == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM) {
      checkSelectTeam(recentForward.getSessionId(), isSelected);
    }
  }

  /**
   * 移除已经选择的会话
   *
   * @param bean 已选的会话
   */
  public void removeSelectedItem(SelectedViewBean bean) {
    String sessionId = V2NIMConversationIdUtil.conversationTargetId(bean.getTargetId());
    V2NIMConversationType sessionType =
        V2NIMConversationIdUtil.conversationType(bean.getTargetId());
    checkSelectConversation(sessionId, sessionType, false);
    if (sessionType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      checkSelectedFriend(sessionId, false);
    } else if (sessionType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM) {
      checkSelectTeam(sessionId, false);
    }
    checkSelectedRecentForward(sessionId, sessionType, false);
    selectedList.remove(bean);
    selectedLiveData.postValue(selectedList);
  }

  private void onSelectedChanged(SelectedViewBean bean, boolean isSelected) {
    if (isSelected && !selectedList.contains(bean)) {
      selectedList.add(bean);
    } else {
      selectedList.remove(bean);
    }
    selectedLiveData.postValue(selectedList);
  }

  /**
   * 检查会话列表选中变更，在选择Friend 或者 team后同步数据
   *
   * @param sessionId 会话Id
   * @param sessionType 会话类型
   */
  private void checkSelectConversation(
      String sessionId, V2NIMConversationType sessionType, boolean isSelected) {
    SelectableBean<V2NIMBaseConversation> selectableBean = null;
    //遍历会话列表，查找是否有选中状态变化的会话
    for (SelectableBean<V2NIMBaseConversation> conversation : allConversationList) {
      if (sessionId.equals(
              V2NIMConversationIdUtil.conversationTargetId(conversation.data.getConversationId()))
          && sessionType == conversation.data.getType()) {
        selectableBean = conversation;
        selectableBean.isSelected = isSelected;
        break;
      }
    }
    //如果有会话选中状态变化，则更新UI
    if (selectableBean != null) {
      conversationListResult.setStatus(LoadStatus.Finish);
      conversationListResult.setData(Collections.singletonList(selectableBean));
      conversationListLiveData.postValue(conversationListResult);
    }
  }

  /**
   * 检查会话列表选中变更，在选择conversation 后同步数据
   *
   * @param teamId 群组Id
   */
  private void checkSelectTeam(String teamId, boolean isSelected) {
    SelectableBean<V2NIMTeam> selectableBean = null;
    //遍历群组列表，找到选中状态变化的群组
    for (SelectableBean<V2NIMTeam> team : allTeamList) {
      if (team.data.getTeamId().equals(teamId)) {
        selectableBean = team;
        selectableBean.isSelected = isSelected;
        break;
      }
    }
    //如果有群组选中状态变化，则更新UI
    if (selectableBean != null) {
      teamListResult.setStatus(LoadStatus.Finish);
      teamListResult.setData(Collections.singletonList(selectableBean));
      teamListLiveData.postValue(teamListResult);
    }
  }

  /**
   * 检查好友列表选中变更，在选择conversation后同步数据
   *
   * @param accId 好友Id
   */
  private void checkSelectedFriend(String accId, boolean isSelected) {
    ContactFriendBean contactFriend = null;
    //遍历好友列表，找到选中状态变化的好友
    for (ContactFriendBean friend : allFriendList) {
      if (friend.data.getAccount().equals(accId)) {
        contactFriend = friend;
        contactFriend.isSelected = isSelected;
        break;
      }
    }
    //如果有好友选中状态变化，则更新UI
    if (contactFriend != null) {
      friendListResult.setStatus(LoadStatus.Finish);
      friendListResult.setData(Collections.singletonList(contactFriend));
      friendListLiveData.postValue(friendListResult);
    }
  }

  /**
   * 检查最近转发列表选中变更，在选择最近转发后同步数据
   *
   * @param sessionId 会话Id
   * @param sessionType 会话类型
   */
  private void checkSelectedRecentForward(
      String sessionId, V2NIMConversationType sessionType, boolean isSelected) {
    SelectableBean<RecentForward> selectableBean = null;
    //遍历最近转发列表，找到选中状态变化的最近转发
    for (RecentForward recentForward : allRecentForwardList) {
      if (recentForward.getSessionId().equals(sessionId)
          && recentForward.getSessionType() == sessionType) {
        selectableBean = new SelectableBean<>(recentForward);
        selectableBean.isSelected = isSelected;
        break;
      }
    }
    //如果有最近转发选中状态变化，则更新UI
    if (selectableBean != null) {
      recentForwardListResult.setStatus(LoadStatus.Finish);
      recentForwardListResult.setData(Collections.singletonList(selectableBean));
      recentForwardListLiveData.postValue(recentForwardListResult);
    }
  }

  /**
   * 获取选中的ConversationId
   *
   * @return 选中的回话Id
   */
  public ArrayList<String> getSelectedSelectedConversation() {
    ArrayList<String> selectedConversationIds = new ArrayList<>();
    for (SelectedViewBean selectedViewBean : selectedList) {
      selectedConversationIds.add(selectedViewBean.getTargetId());
    }
    return selectedConversationIds;
  }

  /**
   * 设置搜索关键字
   *
   * @param searchKey 搜索关键字
   */
  public void setSearchKey(String searchKey) {
    this.searchKey = searchKey;
    searchKeyLiveData.postValue(searchKey);
    if (TextUtils.isEmpty(searchKey)) {
      clearSearch();
    } else {
      switch (searchType) {
        case FRIEND:
          searchFriend();
          break;
        case TEAM:
          searchTeam();
          break;
        case CONVERSATION:
          searchConversation();
          break;
      }
    }
  }

  /** 清除搜索 */
  private void clearSearch() {
    friendListResult.setStatus(LoadStatus.Success);
    friendListResult.setData(allFriendList);
    friendListLiveData.postValue(friendListResult);

    teamListResult.setStatus(LoadStatus.Success);
    teamListResult.setData(allTeamList);
    teamListLiveData.postValue(teamListResult);

    conversationListResult.setStatus(LoadStatus.Success);
    conversationListResult.setData(allConversationList);
    conversationListLiveData.postValue(conversationListResult);
  }

  public void setSearchType(SearchType searchType) {
    this.searchType = searchType;
  }

  /** 搜索好友 */
  public void searchFriend() {
    if (TextUtils.isEmpty(searchKey) || allFriendList.isEmpty()) {
      return;
    }
    List<UserWithFriend> friends = new ArrayList<>();
    for (ContactFriendBean friend : allFriendList) {
      friends.add(friend.data);
    }
    SearchRepo.searchFriendByName(
        searchKey,
        friends,
        new FetchCallback<List<FriendSearchInfo>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            searchConversationResult.setStatus(LoadStatus.Error);
            searchConversationResultLiveData.postValue(searchConversationResult);
          }

          @Override
          public void onSuccess(@Nullable List<FriendSearchInfo> data) {
            searchFriendResult.setStatus(LoadStatus.Success);
            List<ContactFriendBean> searchFriendList = new ArrayList<>();
            if (data != null) {
              for (FriendSearchInfo friend : data) {
                ContactFriendBean contactFriend = new ContactFriendBean(friend.getFriendInfo());
                contactFriend.isSelected = isFriendSelected(friend.getFriendInfo());
                contactFriend.hitType = friend.getHitType();
                contactFriend.recordHitInfo = friend.getHitInfo();
                searchFriendList.add(contactFriend);
              }
            }
            searchFriendResult.setData(searchFriendList);
            searchFriendResultLiveData.postValue(searchFriendResult);
          }
        });
  }

  /** 搜索会话 */
  public void searchTeam() {
    if (TextUtils.isEmpty(searchKey) || allTeamList.isEmpty()) {
      return;
    }
    List<V2NIMTeam> teams = new ArrayList<>();
    for (SelectableBean<V2NIMTeam> team : allTeamList) {
      teams.add(team.data);
    }
    SearchRepo.searchTeamByName(
        searchKey,
        teams,
        new FetchCallback<List<TeamSearchInfo>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            searchTeamResult.setStatus(LoadStatus.Error);
            searchTeamResultLiveData.postValue(searchTeamResult);
          }

          @Override
          public void onSuccess(@Nullable List<TeamSearchInfo> data) {
            searchTeamResult.setStatus(LoadStatus.Success);
            List<SelectableBean<V2NIMTeam>> searchTeamList = new ArrayList<>();
            if (data != null) {
              for (TeamSearchInfo teamSearchInfo : data) {
                SelectableBean<V2NIMTeam> teamBean = new SelectableBean<>(teamSearchInfo.getTeam());
                teamBean.isSelected = isTeamSelected(teamSearchInfo.getTeam());
                teamBean.hitType = teamSearchInfo.getHitType();
                teamBean.recordHitInfo = teamSearchInfo.getHitInfo();
                searchTeamList.add(teamBean);
              }
            }
            searchTeamResult.setData(searchTeamList);
            searchTeamResultLiveData.postValue(searchTeamResult);
          }
        });
  }

  /** 搜索会话 */
  public void searchConversation() {
    if (TextUtils.isEmpty(searchKey) || allConversationList.isEmpty()) {
      return;
    }
    List<V2NIMBaseConversation> conversations = new ArrayList<>();
    for (SelectableBean<V2NIMBaseConversation> conversation : allConversationList) {
      conversations.add(conversation.data);
    }
    SearchRepo.searchConversationByName(
        searchKey,
        conversations,
        new FetchCallback<List<ConversationSearchInfo>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            searchConversationResult.setStatus(LoadStatus.Error);
            searchConversationResultLiveData.postValue(searchConversationResult);
          }

          @Override
          public void onSuccess(@Nullable List<ConversationSearchInfo> data) {
            searchConversationResult.setStatus(LoadStatus.Success);
            List<SelectableBean<V2NIMBaseConversation>> searchConversationList = new ArrayList<>();
            if (data != null && !data.isEmpty()) {
              for (ConversationSearchInfo conversation : data) {
                SelectableBean<V2NIMBaseConversation> conversationBean =
                    new SelectableBean<>(conversation.getConversation());
                conversationBean.isSelected =
                    isConversationSelected(conversation.getConversation());
                conversationBean.hitType = conversation.getHitType();
                conversationBean.recordHitInfo = conversation.getHitInfo();
                conversationBean.memberCount =
                    getMemberCountByConversationId(
                        V2NIMConversationIdUtil.conversationTargetId(
                            conversation.getConversation().getConversationId()),
                        conversation.getConversation().getType());
                searchConversationList.add(conversationBean);
              }
            }
            searchConversationResult.setData(searchConversationList);
            searchConversationResultLiveData.postValue(searchConversationResult);
          }
        });
  }

  public MutableLiveData<FetchResult<List<SelectableBean<V2NIMBaseConversation>>>>
      getConversationListLiveData() {
    return conversationListLiveData;
  }

  public MutableLiveData<FetchResult<List<SelectableBean<V2NIMTeam>>>> getTeamListLiveData() {
    return teamListLiveData;
  }

  public MutableLiveData<FetchResult<List<ContactFriendBean>>> getFriendListLiveData() {
    return friendListLiveData;
  }

  public MutableLiveData<FetchResult<List<SelectableBean<RecentForward>>>>
      getRecentForwardListLiveData() {
    return recentForwardListLiveData;
  }

  public List<SelectedViewBean> getSelectedList() {
    return selectedList;
  }

  public MutableLiveData<List<SelectedViewBean>> getSelectedLiveData() {
    return selectedLiveData;
  }

  public MutableLiveData<Boolean> getIsMultiSelectModeLiveData() {
    return isMultiSelectModeLiveData;
  }

  /** 搜索关键字变更通知 */
  public MutableLiveData<String> getSearchKeyLiveData() {
    return searchKeyLiveData;
  }

  public MutableLiveData<FetchResult<List<ContactFriendBean>>> getSearchFriendResultLiveData() {
    return searchFriendResultLiveData;
  }

  public MutableLiveData<FetchResult<List<SelectableBean<V2NIMTeam>>>>
      getSearchTeamResultLiveData() {
    return searchTeamResultLiveData;
  }

  public MutableLiveData<FetchResult<List<SelectableBean<V2NIMBaseConversation>>>>
      getSearchConversationResultLiveData() {
    return searchConversationResultLiveData;
  }

  public static enum SearchType {
    FRIEND,
    TEAM,
    CONVERSATION
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    allRecentForwardList.clear();
    allTeamList.clear();
    allFriendList.clear();
    allConversationList.clear();
    selectedList.clear();
    searchKey = "";
  }
}
