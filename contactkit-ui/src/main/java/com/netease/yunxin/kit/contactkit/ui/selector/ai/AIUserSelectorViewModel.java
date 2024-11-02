// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.selector.ai;

import static com.netease.yunxin.kit.contactkit.ui.ContactConstant.LIB_TAG;
import static com.netease.yunxin.kit.contactkit.ui.ContactUIConfig.DEFAULT_SELECTOR_MAX_SELECT_COUNT;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.ai.model.V2NIMAIUser;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.manager.AIUserManager;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.indexbar.helper.IIndexBarDataHelper;
import com.netease.yunxin.kit.contactkit.ui.indexbar.helper.IndexBarDataHelperImpl;
import com.netease.yunxin.kit.contactkit.ui.model.ContactFriendBean;
import com.netease.yunxin.kit.contactkit.ui.model.SelectableBean;
import com.netease.yunxin.kit.contactkit.ui.model.SelectedViewBean;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 联系人选择器 ViewModel */
public class AIUserSelectorViewModel extends BaseViewModel {

  private final String TAG = "AIUserSelectorViewModel";

  //是否多选模式
  private boolean isMultiSelectMode = true;

  // 最大选择人数
  private int maxSelectorCount = DEFAULT_SELECTOR_MAX_SELECT_COUNT;

  private final MutableLiveData<Boolean> isMultiSelectModeLiveData = new MutableLiveData<>(false);

  //AI列表数据
  private final MutableLiveData<FetchResult<List<SelectableBean<V2NIMAIUser>>>> aiUserListLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<SelectableBean<V2NIMAIUser>>> aiUserListResult =
      new FetchResult<>(LoadStatus.Finish);
  //所有AI数据
  private final List<SelectableBean<V2NIMAIUser>> allAIUserList = new ArrayList<>();

  //好友列表数据
  private final MutableLiveData<FetchResult<List<ContactFriendBean>>> friendListLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<ContactFriendBean>> friendListResult =
      new FetchResult<>(LoadStatus.Finish);
  //所有好友数据
  private final List<ContactFriendBean> allFriendList = new ArrayList<>();

  //选中的数据
  private final MutableLiveData<List<SelectedViewBean>> selectedLiveData = new MutableLiveData<>();

  private final List<SelectedViewBean> selectedList = new ArrayList<>();

  public MutableLiveData<FetchResult<List<SelectableBean<V2NIMAIUser>>>> getAIUserListLiveData() {
    return aiUserListLiveData;
  }

  public MutableLiveData<FetchResult<List<ContactFriendBean>>> getFriendListLiveData() {
    return friendListLiveData;
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
          }
        });
  }

  //好友列表排序
  void sortFriendList() {
    IIndexBarDataHelper indexBarDataHelper = new IndexBarDataHelperImpl();
    indexBarDataHelper.sortSourceData(allFriendList);
  }

  /** 加载联系人 */
  public void loadAIUserList() {
    List<V2NIMAIUser> userList = AIUserManager.getAllAIUsers();
    if (userList != null && !userList.isEmpty()) {
      allAIUserList.clear();
      for (V2NIMAIUser user : userList) {
        SelectableBean<V2NIMAIUser> selectableBean = new SelectableBean<>(user);
        allAIUserList.add(selectableBean);
      }
    }
    aiUserListLiveData.postValue(new FetchResult<>(LoadStatus.Success, allAIUserList));
  }

  //根据ID查找好友
  private SelectableBean<V2NIMAIUser> findAIUserById(String accId) {
    for (SelectableBean<V2NIMAIUser> aiUserInfoBean : allAIUserList) {
      if (aiUserInfoBean.data.getAccountId().equals(accId)) {
        return aiUserInfoBean;
      }
    }
    return null;
  }

  //检查是否选中
  private boolean isFriendSelected(UserWithFriend friend) {
    for (SelectedViewBean selected : selectedList) {
      if (TextUtils.equals(selected.getTargetId(), friend.getAccount())) {
        return true;
      }
    }
    return false;
  }

  public void setMultiSelectMode(boolean multiSelectMode) {
    isMultiSelectMode = multiSelectMode;
    isMultiSelectModeLiveData.postValue(multiSelectMode);
  }

  public void setMaxSelectorCount(int count) {
    this.maxSelectorCount = count;
  }

  public int getMaxSelectorCount() {
    return this.maxSelectorCount;
  }

  public boolean isMultiSelectMode() {
    return isMultiSelectMode;
  }

  /**
   * 移除已经选择的会话
   *
   * @param bean 已选的会话
   */
  public void removeSelectedItem(SelectedViewBean bean) {
    if (bean == null) {
      return;
    }
    ContactFriendBean friend = findFriendById(bean.getTargetId());
    if (friend != null) {
      friend.isSelected = false;
      friendListResult.setData(Collections.singletonList(friend));
      friendListLiveData.postValue(friendListResult);
    } else {
      SelectableBean<V2NIMAIUser> aiUser = findAIUserById(bean.getTargetId());
      if (aiUser != null) {
        aiUser.isSelected = false;
        aiUserListResult.setData(Collections.singletonList(aiUser));
        aiUserListLiveData.postValue(aiUserListResult);
      }
    }
    selectedList.remove(bean);
    selectedLiveData.postValue(selectedList);
  }

  /**
   * 检查已选数量是否超限
   *
   * @return 是否超限
   */
  public boolean selectCountOverflow() {
    return selectedList.size() >= maxSelectorCount;
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
            friend.getAvatar(), friend.getAvatarName(), friend.getAccount(), friend.getAccount());
    onSelectedChanged(bean, isSelected);
  }

  /**
   * 选择AI数字人
   *
   * @param user 数字人
   * @param isSelected 是否选中
   */
  public void selectAIUser(V2NIMAIUser user, boolean isSelected) {
    aiUserListResult.setStatus(LoadStatus.Finish);
    SelectableBean<V2NIMAIUser> selectableBean = findAIUserById(user.getAccountId());
    if (selectableBean == null) {
      selectableBean = new SelectableBean(user);
    }
    selectableBean.isSelected = isSelected;
    aiUserListResult.setData(Collections.singletonList(selectableBean));
    aiUserListLiveData.postValue(aiUserListResult);
    //处理选中的好友
    SelectedViewBean bean =
        new SelectedViewBean(
            user.getAvatar(), user.getName(), user.getAccountId(), user.getAccountId());
    onSelectedChanged(bean, isSelected);
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

  /**
   * 获取选中的ConversationId
   *
   * @return 选中的回话Id
   */
  public ArrayList<String> getSelectedId() {
    ArrayList<String> selectedConversationIds = new ArrayList<>();
    for (SelectedViewBean selectedViewBean : selectedList) {
      selectedConversationIds.add(selectedViewBean.getTargetId());
    }
    return selectedConversationIds;
  }

  /**
   * 获取选中的ConversationId
   *
   * @return 选中的回话Id
   */
  public ArrayList<String> getSelectedName() {
    ArrayList<String> selectedName = new ArrayList<>();
    for (SelectedViewBean selectedViewBean : selectedList) {
      selectedName.add(selectedViewBean.getName());
    }
    return selectedName;
  }

  private void onSelectedChanged(SelectedViewBean bean, boolean isSelected) {
    if (isSelected && !selectedList.contains(bean)) {
      selectedList.add(bean);
    } else {
      selectedList.remove(bean);
    }
    selectedLiveData.postValue(selectedList);
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    selectedList.clear();
  }
}
