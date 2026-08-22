// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.page.viewmodel;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.V2NIMError;
import com.netease.nimlib.sdk.v2.conversation.V2NIMConversationGroupListener;
import com.netease.nimlib.sdk.v2.conversation.V2NIMConversationGroupResult;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMConversation;
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMConversationGroup;
import com.netease.nimlib.sdk.v2.conversation.params.V2NIMConversationFilter;
import com.netease.nimlib.sdk.v2.conversation.result.V2NIMConversationOperationResult;
import com.netease.nimlib.sdk.v2.conversation.result.V2NIMConversationResult;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.impl.ConversationListenerImpl;
import com.netease.yunxin.kit.chatkit.repo.ConversationGroupRepo;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.conversationkit.ui.IConversationFactory;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationGroupLocalConfigHelper;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationHelper;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationGroupBean;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationGroupLocalConfig;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationGroupType;
import com.netease.yunxin.kit.conversationkit.ui.page.DefaultViewHolderFactory;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConversationGroupViewModel extends BaseViewModel {

  private static final String UNREAD_TAG = "unread";
  private static final int SORT_ALL = 0;
  private static final int SORT_AIT_ME = 1;
  private static final int SORT_UNREAD = 2;
  public static final int CUSTOM_GROUP_NAME_MAX_LENGTH = 20;
  public static final int CUSTOM_GROUP_MAX_COUNT = 10;
  public static final int CUSTOM_GROUP_MEMBER_PAGE_LIMIT = 100;
  private static final Map<String, List<ConversationGroupBean>> GROUP_CACHE = new HashMap<>();

  private final MutableLiveData<List<ConversationGroupBean>> groupLiveData =
      new MutableLiveData<>();
  private final MutableLiveData<ConversationGroupBean> selectedGroupLiveData =
      new MutableLiveData<>();
  private final MutableLiveData<List<ConversationBean>> groupConversationLiveData =
      new MutableLiveData<>();
  private final MutableLiveData<List<ConversationBean>> settingGroupConversationLiveData =
      new MutableLiveData<>();
  private List<ConversationGroupBean> currentGroups = new ArrayList<>();
  private List<ConversationBean> currentConversations = new ArrayList<>();
  private IConversationFactory conversationFactory = new DefaultViewHolderFactory();
  private Comparator<ConversationBean> conversationComparator;
  private final Map<String, List<ConversationBean>> customGroupConversationCache = new HashMap<>();
  private final Map<String, Long> customGroupOffsetMap = new HashMap<>();
  private final Set<String> customGroupNoMoreSet = new HashSet<>();
  private final Set<String> customGroupLoadingSet = new HashSet<>();
  private final Set<String> subscribedUnreadGroupIds = new HashSet<>();
  private final ConversationGroupLocalConfigHelper.OnConfigChangedListener configChangedListener =
      this::refreshLocalGroupConfig;
  private boolean defaultUnreadSubscribed;

  public ConversationGroupViewModel() {
    ConversationGroupRepo.addConversationGroupListener(conversationGroupListener);
    ConversationRepo.addConversationListener(conversationListener);
    ConversationGroupLocalConfigHelper.addConfigChangedListener(configChangedListener);
  }

  public MutableLiveData<List<ConversationGroupBean>> getGroupLiveData() {
    return groupLiveData;
  }

  public MutableLiveData<ConversationGroupBean> getSelectedGroupLiveData() {
    return selectedGroupLiveData;
  }

  public MutableLiveData<List<ConversationBean>> getGroupConversationLiveData() {
    return groupConversationLiveData;
  }

  public MutableLiveData<List<ConversationBean>> getSettingGroupConversationLiveData() {
    return settingGroupConversationLiveData;
  }

  public void setConversationFactory(IConversationFactory factory) {
    if (factory != null) {
      conversationFactory = factory;
    }
  }

  public void setComparator(Comparator<ConversationBean> comparator) {
    conversationComparator = comparator;
  }

  public ConversationGroupBean getSelectedGroup() {
    ConversationGroupBean selected = selectedGroupLiveData.getValue();
    if (selected != null) {
      return selected;
    }
    return currentGroups.isEmpty() ? null : currentGroups.get(0);
  }

  public void loadGroups() {
    ALog.d(UNREAD_TAG, "loadGroups start enable=" + IMKitClient.enableV2CloudConversation());
    if (!IMKitClient.enableV2CloudConversation()) {
      currentGroups = new ArrayList<>();
      publishGroups();
      selectedGroupLiveData.setValue(null);
      return;
    }
    Map<String, ConversationGroupLocalConfig> configMap =
        ConversationGroupLocalConfigHelper.getConfigMap();
    List<ConversationGroupBean> groups = new ArrayList<>();
    groups.add(ConversationGroupBean.all(getString(R.string.conversation_group_all)));
    groups.add(
        ConversationGroupBean.aitMe(
            getString(R.string.conversation_group_ait_me),
            getVisible(configMap, ConversationGroupBean.ID_AIT_ME, true),
            getSortOrder(configMap, ConversationGroupBean.ID_AIT_ME, SORT_AIT_ME)));
    groups.add(
        ConversationGroupBean.unread(
            getString(R.string.conversation_group_unread),
            getVisible(configMap, ConversationGroupBean.ID_UNREAD, true),
            getSortOrder(configMap, ConversationGroupBean.ID_UNREAD, SORT_UNREAD)));
    ConversationGroupRepo.getConversationGroupList(
        new FetchCallback<List<V2NIMConversationGroup>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(
                UNREAD_TAG,
                "loadGroups getConversationGroupList error code=" + errorCode + " msg=" + errorMsg);
            List<ConversationGroupBean> fallbackGroups = copyGroups(currentGroups);
            if (fallbackGroups.isEmpty()) {
              fallbackGroups = getCachedGroups();
            }
            updateGroups(fallbackGroups.isEmpty() ? groups : fallbackGroups);
          }

          @Override
          public void onSuccess(@Nullable List<V2NIMConversationGroup> data) {
            Map<String, ConversationGroupLocalConfig> latestConfigMap =
                ConversationGroupLocalConfigHelper.getConfigMap();
            int nextHiddenOrder = nextHiddenSortOrder(latestConfigMap);
            Map<String, Integer> currentGroupOrders = new HashMap<>();
            for (ConversationGroupBean currentGroup : currentGroups) {
              if (currentGroup != null && currentGroup.getType() == ConversationGroupType.CUSTOM) {
                currentGroupOrders.put(currentGroup.getId(), currentGroup.getSortOrder());
                if (!currentGroup.isVisible()) {
                  nextHiddenOrder = Math.max(nextHiddenOrder, currentGroup.getSortOrder() + 1);
                }
              }
            }
            if (data != null) {
              for (V2NIMConversationGroup group : data) {
                ConversationGroupLocalConfig config = latestConfigMap.get(group.getGroupId());
                boolean visible = config != null && config.isVisible();
                int sortOrder;
                if (config != null) {
                  sortOrder = config.getSortOrder();
                } else if (currentGroupOrders.containsKey(group.getGroupId())) {
                  sortOrder = currentGroupOrders.get(group.getGroupId());
                } else {
                  sortOrder = nextHiddenOrder++;
                }
                groups.add(ConversationGroupBean.custom(group, visible, sortOrder));
              }
            }
            updateGroups(groups);
            // Local visibility may change while the SDK query is in flight.
            refreshLocalGroupConfig();
          }
        });
  }

  public void selectGroup(ConversationGroupBean group) {
    if (group == null) {
      return;
    }
    selectedGroupLiveData.setValue(group);
  }

  public void createCustomGroup(String name, FetchCallback<V2NIMConversationGroup> callback) {
    String trimName = name == null ? "" : name.trim();
    ConversationGroupRepo.createConversationGroup(
        trimName,
        null,
        null,
        new FetchCallback<V2NIMConversationGroupResult>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            if (callback != null) {
              callback.onError(errorCode, errorMsg);
            }
          }

          @Override
          public void onSuccess(@Nullable V2NIMConversationGroupResult data) {
            V2NIMConversationGroup group = data == null ? null : data.getGroup();
            if (group != null) {
              int sortOrder = nextVisibleSortOrder();
              ConversationGroupLocalConfigHelper.saveConfig(
                  new ConversationGroupLocalConfig(
                      group.getGroupId(), ConversationGroupType.CUSTOM, true, sortOrder));
            }
            loadGroups();
            if (callback != null) {
              callback.onSuccess(group);
            }
          }
        });
  }

  public void updateCustomGroupName(String groupId, String name, FetchCallback<Void> callback) {
    ConversationGroupRepo.updateConversationGroup(
        groupId, name == null ? "" : name.trim(), null, callback);
  }

  public void deleteCustomGroup(String groupId, FetchCallback<Void> callback) {
    ConversationGroupRepo.deleteConversationGroup(
        groupId,
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            if (callback != null) {
              callback.onError(errorCode, errorMsg);
            }
          }

          @Override
          public void onSuccess(@Nullable Void data) {
            ConversationGroupLocalConfigHelper.removeConfig(groupId);
            loadGroups();
            if (callback != null) {
              callback.onSuccess(data);
            }
          }
        });
  }

  public void loadGroupConversations(String groupId) {
    loadGroupConversations(groupId, true);
  }

  public void loadGroupConversationsForSetting(String groupId) {
    if (TextUtils.isEmpty(groupId)) {
      settingGroupConversationLiveData.setValue(new ArrayList<>());
      return;
    }
    ConversationGroupRepo.getConversationListByGroupId(
        groupId,
        0,
        CUSTOM_GROUP_MEMBER_PAGE_LIMIT,
        new FetchCallback<V2NIMConversationResult>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            settingGroupConversationLiveData.setValue(new ArrayList<>());
          }

          @Override
          public void onSuccess(@Nullable V2NIMConversationResult data) {
            List<ConversationBean> result = new ArrayList<>();
            if (data != null && data.getConversationList() != null) {
              for (V2NIMConversation conversation : data.getConversationList()) {
                result.add(conversationFactory.CreateBean(conversation));
              }
            }
            settingGroupConversationLiveData.setValue(result);
          }
        });
  }

  public void loadSelectedGroupConversations(boolean refresh) {
    ConversationGroupBean selected = getSelectedGroup();
    if (selected == null || selected.getType() != ConversationGroupType.CUSTOM) {
      return;
    }
    loadGroupConversations(selected.getId(), refresh);
  }

  public boolean hasMoreSelectedGroupConversations() {
    ConversationGroupBean selected = getSelectedGroup();
    return selected != null
        && selected.getType() == ConversationGroupType.CUSTOM
        && !customGroupNoMoreSet.contains(selected.getId())
        && !customGroupLoadingSet.contains(selected.getId());
  }

  public List<ConversationBean> getSelectedGroupConversations() {
    ConversationGroupBean selected = getSelectedGroup();
    if (selected == null || selected.getType() != ConversationGroupType.CUSTOM) {
      return new ArrayList<>();
    }
    List<ConversationBean> cache = customGroupConversationCache.get(selected.getId());
    return cache == null ? new ArrayList<>() : new ArrayList<>(cache);
  }

  public boolean isSelectedCustomGroup() {
    ConversationGroupBean selected = getSelectedGroup();
    return selected != null && selected.getType() == ConversationGroupType.CUSTOM;
  }

  public boolean isSelectedVirtualFilterGroup() {
    ConversationGroupBean selected = getSelectedGroup();
    return selected != null
        && (selected.getType() == ConversationGroupType.AIT_ME
            || selected.getType() == ConversationGroupType.UNREAD);
  }

  public int getCustomGroupCount() {
    int count = 0;
    for (ConversationGroupBean group : currentGroups) {
      if (group.getType() == ConversationGroupType.CUSTOM) {
        count++;
      }
    }
    return count;
  }

  private void loadGroupConversations(String groupId, boolean refresh) {
    if (TextUtils.isEmpty(groupId) || customGroupLoadingSet.contains(groupId)) {
      return;
    }
    if (!refresh && customGroupNoMoreSet.contains(groupId)) {
      return;
    }
    customGroupLoadingSet.add(groupId);
    Long cachedOffset = customGroupOffsetMap.get(groupId);
    long offset = refresh || cachedOffset == null ? 0 : cachedOffset;
    ConversationGroupRepo.getConversationListByGroupId(
        groupId,
        offset,
        CUSTOM_GROUP_MEMBER_PAGE_LIMIT,
        new FetchCallback<V2NIMConversationResult>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            customGroupLoadingSet.remove(groupId);
            List<ConversationBean> cache = customGroupConversationCache.get(groupId);
            groupConversationLiveData.setValue(
                cache == null ? new ArrayList<>() : new ArrayList<>(cache));
          }

          @Override
          public void onSuccess(@Nullable V2NIMConversationResult data) {
            customGroupLoadingSet.remove(groupId);
            List<ConversationBean> page = new ArrayList<>();
            if (data != null && data.getConversationList() != null) {
              for (V2NIMConversation conversation : data.getConversationList()) {
                page.add(conversationFactory.CreateBean(conversation));
              }
              customGroupOffsetMap.put(groupId, data.getOffset());
            }
            List<ConversationBean> cachedConversations = customGroupConversationCache.get(groupId);
            List<ConversationBean> result =
                refresh || cachedConversations == null
                    ? new ArrayList<>()
                    : new ArrayList<>(cachedConversations);
            result.addAll(page);
            sortConversations(result);
            if (page.size() < CUSTOM_GROUP_MEMBER_PAGE_LIMIT) {
              customGroupNoMoreSet.add(groupId);
            } else {
              customGroupNoMoreSet.remove(groupId);
            }
            customGroupConversationCache.put(groupId, result);
            groupConversationLiveData.setValue(new ArrayList<>(result));
          }
        });
  }

  public void removeConversationFromGroup(
      String groupId, String conversationId, FetchCallback<Void> callback) {
    List<String> conversationIds = new ArrayList<>();
    conversationIds.add(conversationId);
    ConversationGroupRepo.removeConversationsFromGroup(
        groupId,
        conversationIds,
        new FetchCallback<List<V2NIMConversationOperationResult>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            if (callback != null) {
              callback.onError(errorCode, errorMsg);
            }
          }

          @Override
          public void onSuccess(@Nullable List<V2NIMConversationOperationResult> data) {
            clearGroupConversationCache(groupId);
            loadGroupConversations(groupId);
            if (callback != null) {
              callback.onSuccess(null);
            }
          }
        });
  }

  public void setGroupVisible(ConversationGroupBean group, boolean visible) {
    if (group == null || group.getType() == ConversationGroupType.ALL) {
      return;
    }
    ConversationGroupBean targetGroup = null;
    for (ConversationGroupBean currentGroup : currentGroups) {
      if (TextUtils.equals(currentGroup.getId(), group.getId())) {
        targetGroup = currentGroup;
        break;
      }
    }
    if (targetGroup == null || targetGroup.getType() == ConversationGroupType.ALL) {
      return;
    }
    targetGroup.setSortOrder(visible ? nextVisibleSortOrder() : nextHiddenSortOrder());
    targetGroup.setVisible(visible);
    ConversationGroupLocalConfigHelper.saveConfig(
        new ConversationGroupLocalConfig(
            targetGroup.getId(), targetGroup.getType(), visible, targetGroup.getSortOrder()));
    sortGroups(currentGroups);
    publishGroups();
  }

  public void saveGroupOrder(
      List<ConversationGroupBean> visibleGroups, List<ConversationGroupBean> hiddenGroups) {
    List<ConversationGroupLocalConfig> configs = new ArrayList<>();
    int sortOrder = 0;
    if (visibleGroups != null) {
      for (ConversationGroupBean group : visibleGroups) {
        group.setVisible(true);
        group.setSortOrder(sortOrder++);
        if (group.getType() != ConversationGroupType.ALL) {
          configs.add(
              new ConversationGroupLocalConfig(
                  group.getId(), group.getType(), true, group.getSortOrder()));
        }
      }
    }
    if (hiddenGroups != null) {
      for (ConversationGroupBean group : hiddenGroups) {
        group.setVisible(false);
        group.setSortOrder(sortOrder++);
        configs.add(
            new ConversationGroupLocalConfig(
                group.getId(), group.getType(), false, group.getSortOrder()));
      }
    }
    ConversationGroupLocalConfigHelper.saveConfigList(configs);
  }

  private int nextSortOrder() {
    int sortOrder = SORT_UNREAD + 1;
    for (ConversationGroupBean group : currentGroups) {
      sortOrder = Math.max(sortOrder, group.getSortOrder() + 1);
    }
    return sortOrder;
  }

  private int nextVisibleSortOrder() {
    int sortOrder = SORT_UNREAD + 1;
    for (ConversationGroupBean group : currentGroups) {
      if (group.isVisible()) {
        sortOrder = Math.max(sortOrder, group.getSortOrder() + 1);
      }
    }
    for (ConversationGroupLocalConfig config : ConversationGroupLocalConfigHelper.getConfigList()) {
      if (config.isVisible()) {
        sortOrder = Math.max(sortOrder, config.getSortOrder() + 1);
      }
    }
    return sortOrder;
  }

  private int nextHiddenSortOrder() {
    int sortOrder = SORT_UNREAD + 1;
    for (ConversationGroupBean group : currentGroups) {
      if (!group.isVisible()) {
        sortOrder = Math.max(sortOrder, group.getSortOrder() + 1);
      }
    }
    return sortOrder;
  }

  private int nextHiddenSortOrder(Map<String, ConversationGroupLocalConfig> configMap) {
    int sortOrder = SORT_UNREAD + 1;
    for (ConversationGroupLocalConfig config : configMap.values()) {
      if (!config.isVisible()) {
        sortOrder = Math.max(sortOrder, config.getSortOrder() + 1);
      }
    }
    return sortOrder;
  }

  public void setCurrentConversations(List<ConversationBean> conversations) {
    currentConversations =
        conversations == null ? new ArrayList<>() : new ArrayList<>(conversations);
    boolean customGroupChanged = syncSelectedCustomGroupConversationCache();
    if (updateAitCount()) {
      publishGroups();
    }
    if (customGroupChanged) {
      groupConversationLiveData.setValue(getSelectedGroupConversations());
    }
  }

  public List<ConversationBean> getCurrentConversations() {
    return new ArrayList<>(currentConversations);
  }

  public void removeCachedConversations(List<String> conversationIds) {
    if (conversationIds == null || conversationIds.isEmpty()) {
      return;
    }
    boolean changed = false;
    for (List<ConversationBean> cache : customGroupConversationCache.values()) {
      if (cache == null) {
        continue;
      }
      for (int index = cache.size() - 1; index >= 0; index--) {
        ConversationBean bean = cache.get(index);
        if (bean != null && conversationIds.contains(bean.getConversationId())) {
          cache.remove(index);
          changed = true;
        }
      }
    }
    if (changed && isSelectedCustomGroup()) {
      groupConversationLiveData.setValue(getSelectedGroupConversations());
    }
  }

  public List<ConversationBean> filterCurrentGroup(List<ConversationBean> conversations) {
    ConversationGroupBean group = getSelectedGroup();
    if (group == null || group.getType() == ConversationGroupType.ALL) {
      return conversations == null ? new ArrayList<>() : new ArrayList<>(conversations);
    }
    List<ConversationBean> source = conversations == null ? new ArrayList<>() : conversations;
    List<ConversationBean> result = new ArrayList<>();
    if (group.getType() == ConversationGroupType.AIT_ME) {
      for (ConversationBean bean : source) {
        if (isAitMeConversation(bean)) {
          result.add(bean);
        }
      }
      return result;
    }
    if (group.getType() == ConversationGroupType.UNREAD) {
      for (ConversationBean bean : source) {
        if (isUnreadConversation(bean)) {
          result.add(bean);
        }
      }
      return result;
    }
    for (ConversationBean bean : source) {
      if (isConversationInGroup(bean, group.getId())) {
        result.add(bean);
      }
    }
    return result;
  }

  private void updateGroups(List<ConversationGroupBean> groups) {
    sortGroups(groups);
    currentGroups = groups;
    updateAitCount();
    ALog.d(
        UNREAD_TAG,
        "updateGroups size=" + currentGroups.size() + " groups=" + describeGroups(currentGroups));
    ConversationGroupBean selected = getSelectedGroup();
    if (selected == null || !containsVisibleGroup(currentGroups, selected.getId())) {
      selectedGroupLiveData.setValue(findFirstVisibleGroup(currentGroups));
    }
    publishGroups();
    syncUnreadSubscriptions();
    refreshUnreadCounts();
  }

  private void refreshLocalGroupConfig() {
    Map<String, ConversationGroupLocalConfig> configMap =
        ConversationGroupLocalConfigHelper.getConfigMap();
    boolean changed = false;
    for (ConversationGroupBean group : currentGroups) {
      if (group == null || group.getType() == ConversationGroupType.ALL) {
        continue;
      }
      ConversationGroupLocalConfig config = configMap.get(group.getId());
      if (config == null) {
        continue;
      }
      if (group.isVisible() != config.isVisible()
          || group.getSortOrder() != config.getSortOrder()) {
        group.setVisible(config.isVisible());
        group.setSortOrder(config.getSortOrder());
        changed = true;
      }
    }
    if (!changed) {
      return;
    }
    sortGroups(currentGroups);
    ConversationGroupBean selected = getSelectedGroup();
    if (selected == null || !containsVisibleGroup(currentGroups, selected.getId())) {
      selectedGroupLiveData.setValue(findFirstVisibleGroup(currentGroups));
    }
    ALog.d(UNREAD_TAG, "local conversation group config changed");
    publishGroups();
  }

  private void sortGroups(List<ConversationGroupBean> groups) {
    Collections.sort(
        groups,
        (left, right) -> {
          if (left.getType() == ConversationGroupType.ALL) {
            return -1;
          }
          if (right.getType() == ConversationGroupType.ALL) {
            return 1;
          }
          return Integer.compare(left.getSortOrder(), right.getSortOrder());
        });
  }

  private boolean containsVisibleGroup(List<ConversationGroupBean> groups, String groupId) {
    if (TextUtils.isEmpty(groupId)) {
      return false;
    }
    for (ConversationGroupBean group : groups) {
      if (TextUtils.equals(group.getId(), groupId) && group.isVisible()) {
        return true;
      }
    }
    return false;
  }

  private ConversationGroupBean findFirstVisibleGroup(List<ConversationGroupBean> groups) {
    if (groups == null || groups.isEmpty()) {
      return null;
    }
    for (ConversationGroupBean group : groups) {
      if (group.isVisible()) {
        return group;
      }
    }
    return groups.get(0);
  }

  private boolean syncSelectedCustomGroupConversationCache() {
    ConversationGroupBean selected = getSelectedGroup();
    if (selected == null || selected.getType() != ConversationGroupType.CUSTOM) {
      return false;
    }
    String groupId = selected.getId();
    List<ConversationBean> cachedConversations = customGroupConversationCache.get(groupId);
    if (cachedConversations == null) {
      return false;
    }
    List<ConversationBean> cache = new ArrayList<>(cachedConversations);
    boolean changed = false;
    for (ConversationBean source : currentConversations) {
      if (source == null || TextUtils.isEmpty(source.getConversationId())) {
        continue;
      }
      int index = indexOfConversation(cache, source.getConversationId());
      boolean inGroup = isConversationInGroup(source, groupId);
      if (index >= 0) {
        if (source.infoData != null && source.infoData.getGroupIds() != null && !inGroup) {
          cache.remove(index);
        } else {
          cache.set(index, source);
        }
        changed = true;
      } else if (inGroup) {
        cache.add(source);
        changed = true;
      }
    }
    if (changed) {
      sortConversations(cache);
      customGroupConversationCache.put(groupId, cache);
    }
    return changed;
  }

  private int indexOfConversation(List<ConversationBean> conversations, String conversationId) {
    if (TextUtils.isEmpty(conversationId)) {
      return -1;
    }
    for (int index = 0; index < conversations.size(); index++) {
      ConversationBean bean = conversations.get(index);
      if (bean != null && TextUtils.equals(bean.getConversationId(), conversationId)) {
        return index;
      }
    }
    return -1;
  }

  private boolean isAitMeConversation(ConversationBean bean) {
    return bean != null
        && bean.infoData != null
        && bean.infoData.getType() == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM
        && bean.infoData.getLastMessage() != null
        && bean.infoData.getUnreadCount() > 0
        && ConversationHelper.hasAit(bean.infoData.getConversationId());
  }

  private boolean isUnreadConversation(ConversationBean bean) {
    return bean != null
        && bean.infoData != null
        && bean.infoData.getUnreadCount() > 0
        && !bean.infoData.isMute();
  }

  private boolean isConversationInGroup(ConversationBean bean, String groupId) {
    return bean != null
        && bean.infoData != null
        && bean.infoData.getGroupIds() != null
        && bean.infoData.getGroupIds().contains(groupId);
  }

  private void sortConversations(List<ConversationBean> conversations) {
    if (conversationComparator != null && conversations != null) {
      Collections.sort(conversations, conversationComparator);
    }
  }

  private boolean updateAitCount() {
    int aitCount = 0;
    for (ConversationBean bean : currentConversations) {
      if (bean.infoData == null) {
        continue;
      }
      if (isAitMeConversation(bean)) {
        aitCount++;
      }
    }
    boolean changed = false;
    for (ConversationGroupBean group : currentGroups) {
      if (group.getType() == ConversationGroupType.AIT_ME) {
        changed |= updateGroupCount(group, aitCount);
      }
    }
    return changed;
  }

  private void refreshUnreadCounts() {
    ALog.d(UNREAD_TAG, "initial refreshUnreadCounts start");
    refreshDefaultUnreadCount();
    refreshCustomGroupUnreadCounts();
  }

  private void refreshDefaultUnreadCount() {
    V2NIMConversationFilter filter = createUnreadCountFilter(null);
    ALog.d(UNREAD_TAG, "initial getUnreadCount default filter=" + describeFilter(filter));
    ConversationGroupRepo.getUnreadCountByFilter(
        filter,
        new FetchCallback<Integer>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(
                UNREAD_TAG,
                "initial getUnreadCount default error code=" + errorCode + " msg=" + errorMsg);
            updateDefaultUnreadCount(0);
          }

          @Override
          public void onSuccess(@Nullable Integer data) {
            ALog.d(UNREAD_TAG, "initial getUnreadCount default success count=" + data);
            updateDefaultUnreadCount(data == null ? 0 : data);
          }
        });
  }

  private void updateDefaultUnreadCount(int unreadCount) {
    ALog.d(UNREAD_TAG, "updateDefaultUnreadCount count=" + unreadCount);
    boolean changed = false;
    for (ConversationGroupBean group : currentGroups) {
      if (group.getType() == ConversationGroupType.ALL
          || group.getType() == ConversationGroupType.UNREAD) {
        changed |= updateGroupCount(group, unreadCount);
      }
    }
    if (changed) {
      ALog.d(
          UNREAD_TAG, "updateDefaultUnreadCount changed groups=" + describeGroups(currentGroups));
      publishGroups();
    } else {
      ALog.d(UNREAD_TAG, "updateDefaultUnreadCount no change");
    }
  }

  private boolean updateGroupCount(ConversationGroupBean group, int count) {
    int safeCount = Math.max(count, 0);
    if (group.getUnreadOrCount() == safeCount) {
      return false;
    }
    group.setUnreadOrCount(safeCount);
    return true;
  }

  private void refreshCustomGroupUnreadCounts() {
    for (ConversationGroupBean group : currentGroups) {
      if (group.getType() == ConversationGroupType.CUSTOM) {
        refreshCustomGroupUnreadCount(group.getId());
      }
    }
  }

  private void refreshCustomGroupUnreadCount(String groupId) {
    V2NIMConversationFilter filter = createUnreadCountFilter(groupId);
    ALog.d(
        UNREAD_TAG,
        "getUnreadCount custom groupId=" + groupId + " filter=" + describeFilter(filter));
    ConversationGroupRepo.getUnreadCountByFilter(
        filter,
        new FetchCallback<Integer>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(
                UNREAD_TAG,
                "getUnreadCount custom error groupId="
                    + groupId
                    + " code="
                    + errorCode
                    + " msg="
                    + errorMsg);
          }

          @Override
          public void onSuccess(@Nullable Integer data) {
            ALog.d(
                UNREAD_TAG, "getUnreadCount custom success groupId=" + groupId + " count=" + data);
            updateCustomGroupUnread(groupId, data == null ? 0 : data);
          }
        });
  }

  private void updateCustomGroupUnread(String groupId, int unreadCount) {
    ALog.d(UNREAD_TAG, "updateCustomGroupUnread groupId=" + groupId + " count=" + unreadCount);
    boolean changed = false;
    for (ConversationGroupBean group : currentGroups) {
      if (group.getType() == ConversationGroupType.CUSTOM
          && TextUtils.equals(group.getId(), groupId)) {
        changed = updateGroupCount(group, unreadCount);
        break;
      }
    }
    if (changed) {
      ALog.d(UNREAD_TAG, "updateCustomGroupUnread changed groups=" + describeGroups(currentGroups));
      publishGroups();
    } else {
      ALog.d(UNREAD_TAG, "updateCustomGroupUnread no change groupId=" + groupId);
    }
  }

  private boolean updateConversationGroup(V2NIMConversationGroup conversationGroup) {
    if (conversationGroup == null || TextUtils.isEmpty(conversationGroup.getGroupId())) {
      return false;
    }
    for (ConversationGroupBean group : currentGroups) {
      if (TextUtils.equals(group.getId(), conversationGroup.getGroupId())) {
        group.setName(conversationGroup.getName());
        group.setSdkGroup(conversationGroup);
        publishGroups();
        return true;
      }
    }
    return false;
  }

  private void syncUnreadSubscriptions() {
    ALog.d(
        UNREAD_TAG,
        "syncUnreadSubscriptions start defaultSubscribed="
            + defaultUnreadSubscribed
            + " subscribedCustom="
            + subscribedUnreadGroupIds);
    if (!defaultUnreadSubscribed) {
      defaultUnreadSubscribed = subscribeUnreadCountByFilter(createUnreadCountFilter(null));
    }
    Set<String> currentCustomGroupIds = new HashSet<>();
    for (ConversationGroupBean group : currentGroups) {
      if (group.getType() == ConversationGroupType.CUSTOM) {
        currentCustomGroupIds.add(group.getId());
        if (!subscribedUnreadGroupIds.contains(group.getId())) {
          if (subscribeUnreadCountByFilter(createUnreadCountFilter(group.getId()))) {
            subscribedUnreadGroupIds.add(group.getId());
          }
        }
      }
    }
    List<String> removedGroupIds = new ArrayList<>();
    for (String groupId : subscribedUnreadGroupIds) {
      if (!currentCustomGroupIds.contains(groupId)) {
        removedGroupIds.add(groupId);
      }
    }
    for (String groupId : removedGroupIds) {
      ConversationGroupRepo.unsubscribeUnreadCountByFilter(createUnreadCountFilter(groupId));
      subscribedUnreadGroupIds.remove(groupId);
    }
  }

  private boolean subscribeUnreadCountByFilter(V2NIMConversationFilter filter) {
    ALog.d(UNREAD_TAG, "subscribeUnreadCountByFilter start filter=" + describeFilter(filter));
    V2NIMError error = ConversationGroupRepo.subscribeUnreadCountByFilter(filter);
    ALog.d(
        UNREAD_TAG,
        "subscribeUnreadCountByFilter result filter="
            + describeFilter(filter)
            + " success="
            + (error == null)
            + (error == null ? "" : " code=" + error.getCode() + " msg=" + error.getDesc()));
    return error == null;
  }

  private void clearCustomGroupUnreadSubscriptions() {
    if (defaultUnreadSubscribed) {
      ConversationGroupRepo.unsubscribeUnreadCountByFilter(createUnreadCountFilter(null));
      defaultUnreadSubscribed = false;
    }
    for (String groupId : new ArrayList<>(subscribedUnreadGroupIds)) {
      ConversationGroupRepo.unsubscribeUnreadCountByFilter(createUnreadCountFilter(groupId));
    }
    subscribedUnreadGroupIds.clear();
  }

  private void publishGroups() {
    ALog.d(UNREAD_TAG, "publishGroups groups=" + describeGroups(currentGroups));
    cacheGroups(currentGroups);
    groupLiveData.postValue(copyGroups(currentGroups));
  }

  private List<ConversationGroupBean> getCachedGroups() {
    synchronized (GROUP_CACHE) {
      return copyGroups(GROUP_CACHE.get(getGroupCacheKey()));
    }
  }

  private void cacheGroups(List<ConversationGroupBean> groups) {
    synchronized (GROUP_CACHE) {
      GROUP_CACHE.put(getGroupCacheKey(), copyGroups(groups));
    }
  }

  private String getGroupCacheKey() {
    String account = IMKitClient.account();
    return TextUtils.isEmpty(account) ? "anonymous" : account;
  }

  private List<ConversationGroupBean> copyGroups(List<ConversationGroupBean> groups) {
    List<ConversationGroupBean> result = new ArrayList<>();
    if (groups == null) {
      return result;
    }
    for (ConversationGroupBean group : groups) {
      if (group != null) {
        result.add(group.copy());
      }
    }
    return result;
  }

  private V2NIMConversationFilter createUnreadCountFilter(@Nullable String groupId) {
    V2NIMConversationFilter filter = new V2NIMConversationFilter();
    if (!TextUtils.isEmpty(groupId)) {
      filter.setConversationGroupId(groupId);
    }
    filter.setIgnoreMuted(true);
    return filter;
  }

  private String describeFilter(V2NIMConversationFilter filter) {
    if (filter == null) {
      return "null";
    }
    return "{groupId="
        + filter.getConversationGroupId()
        + ", ignoreMuted="
        + filter.isIgnoreMuted()
        + ", types="
        + filter.getConversationTypes()
        + "}";
  }

  private String describeGroups(List<ConversationGroupBean> groups) {
    if (groups == null) {
      return "null";
    }
    StringBuilder builder = new StringBuilder("[");
    for (int index = 0; index < groups.size(); index++) {
      ConversationGroupBean group = groups.get(index);
      if (index > 0) {
        builder.append(", ");
      }
      if (group == null) {
        builder.append("null");
      } else {
        builder
            .append("{id=")
            .append(group.getId())
            .append(", type=")
            .append(group.getType())
            .append(", visible=")
            .append(group.isVisible())
            .append(", unread=")
            .append(group.getUnreadOrCount())
            .append("}");
      }
    }
    builder.append("]");
    return builder.toString();
  }

  private String getString(int resId) {
    return IMKitClient.getApplicationContext().getString(resId);
  }

  private boolean getVisible(
      Map<String, ConversationGroupLocalConfig> configMap, String groupId, boolean defaultValue) {
    ConversationGroupLocalConfig config = configMap.get(groupId);
    return config == null ? defaultValue : config.isVisible();
  }

  private int getSortOrder(
      Map<String, ConversationGroupLocalConfig> configMap, String groupId, int defaultValue) {
    ConversationGroupLocalConfig config = configMap.get(groupId);
    return config == null ? defaultValue : config.getSortOrder();
  }

  private void clearGroupConversationCache(String groupId) {
    customGroupConversationCache.remove(groupId);
    customGroupOffsetMap.remove(groupId);
    customGroupNoMoreSet.remove(groupId);
    customGroupLoadingSet.remove(groupId);
  }

  private void updateCurrentConversations(List<V2NIMConversation> conversations) {
    if (conversations == null || conversations.isEmpty()) {
      return;
    }
    for (V2NIMConversation conversation : conversations) {
      if (conversation == null || TextUtils.isEmpty(conversation.getConversationId())) {
        continue;
      }
      ConversationBean bean = conversationFactory.CreateBean(conversation);
      int index = indexOfConversation(currentConversations, bean.getConversationId());
      if (index >= 0) {
        currentConversations.set(index, bean);
      } else {
        currentConversations.add(bean);
      }
    }
    sortConversations(currentConversations);
    boolean customGroupChanged = syncSelectedCustomGroupConversationCache();
    if (customGroupChanged && isSelectedCustomGroup()) {
      groupConversationLiveData.postValue(getSelectedGroupConversations());
    }
  }

  private void removeCurrentConversations(List<String> conversationIds) {
    if (conversationIds == null || conversationIds.isEmpty()) {
      return;
    }
    boolean changed = false;
    for (int index = currentConversations.size() - 1; index >= 0; index--) {
      ConversationBean bean = currentConversations.get(index);
      if (bean != null && conversationIds.contains(bean.getConversationId())) {
        currentConversations.remove(index);
        changed = true;
      }
    }
    if (!changed) {
      return;
    }
    removeCachedConversations(conversationIds);
    if (updateAitCount()) {
      publishGroups();
    }
  }

  private final V2NIMConversationGroupListener conversationGroupListener =
      new V2NIMConversationGroupListener() {
        @Override
        public void onConversationGroupCreated(V2NIMConversationGroup conversationGroup) {
          loadGroups();
        }

        @Override
        public void onConversationGroupDeleted(String groupId) {
          ConversationGroupLocalConfigHelper.removeConfig(groupId);
          clearGroupConversationCache(groupId);
          loadGroups();
        }

        @Override
        public void onConversationGroupChanged(V2NIMConversationGroup conversationGroup) {
          if (updateConversationGroup(conversationGroup)) {
            refreshConversationGroupData(conversationGroup.getGroupId());
          } else {
            loadGroups();
          }
        }

        @Override
        public void onConversationsAddedToGroup(
            String groupId, List<V2NIMConversation> conversations) {
          refreshConversationGroupData(groupId);
        }

        @Override
        public void onConversationsRemovedFromGroup(String groupId, List<String> conversationIds) {
          refreshConversationGroupData(groupId);
        }
      };

  private final ConversationListenerImpl conversationListener =
      new ConversationListenerImpl() {
        @Override
        public void onConversationCreated(V2NIMConversation conversation) {
          if (conversation == null) {
            return;
          }
          List<V2NIMConversation> conversations = new ArrayList<>();
          conversations.add(conversation);
          updateCurrentConversations(conversations);
        }

        @Override
        public void onConversationDeleted(List<String> conversationIds) {
          removeCurrentConversations(conversationIds);
        }

        @Override
        public void onConversationChanged(List<V2NIMConversation> conversationList) {
          updateCurrentConversations(conversationList);
        }

        @Override
        public void onSyncFinished() {
          ALog.d(UNREAD_TAG, "listener onSyncFinished");
          syncUnreadSubscriptions();
        }

        @Override
        public void onTotalUnreadCountChanged(int unreadCount) {
          ALog.d(
              UNREAD_TAG,
              "listener onTotalUnreadCountChanged count="
                  + unreadCount
                  + " ignoredForGroupTab=true");
        }

        @Override
        public void onUnreadCountChangedByFilter(V2NIMConversationFilter filter, int unreadCount) {
          ALog.d(
              UNREAD_TAG,
              "listener onUnreadCountChangedByFilter filter="
                  + describeFilter(filter)
                  + " count="
                  + unreadCount);
          if (filter == null || TextUtils.isEmpty(filter.getConversationGroupId())) {
            updateDefaultUnreadCount(unreadCount);
          } else {
            updateCustomGroupUnread(filter.getConversationGroupId(), unreadCount);
          }
        }
      };

  private void refreshConversationGroupData(String groupId) {
    if (TextUtils.isEmpty(groupId)) {
      return;
    }
    ALog.d(UNREAD_TAG, "refreshConversationGroupData groupId=" + groupId);
    refreshGroupConversationCache(groupId);
    refreshCustomGroupUnreadCount(groupId);
  }

  private void refreshGroupConversationCache(String groupId) {
    clearGroupConversationCache(groupId);
    ConversationGroupBean selected = getSelectedGroup();
    if (selected != null && TextUtils.equals(selected.getId(), groupId)) {
      loadGroupConversations(groupId);
    }
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    ConversationGroupRepo.removeConversationGroupListener(conversationGroupListener);
    ConversationRepo.removeConversationListener(conversationListener);
    ConversationGroupLocalConfigHelper.removeConfigChangedListener(configChangedListener);
    clearCustomGroupUnreadSubscriptions();
  }
}
