// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.page.viewmodel;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.V2NIMError;
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMConversation;
import com.netease.nimlib.sdk.v2.conversation.result.V2NIMConversationOperationResult;
import com.netease.nimlib.sdk.v2.conversation.result.V2NIMConversationResult;
import com.netease.yunxin.kit.chatkit.repo.ConversationGroupRepo;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationUtils;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ConversationGroupAddViewModel extends BaseViewModel {

  public interface SubmitCallback {
    void onError(int errorCode, @Nullable String errorMsg);

    void onSuccess();

    void onPartialSuccess();
  }

  public static final int GROUP_MEMBER_MAX_COUNT = 100;
  public static final int CONVERSATION_GROUP_MAX_COUNT = 5;
  private static final int PAGE_LIMIT = 100;

  private final MutableLiveData<List<ConversationBean>> conversationLiveData =
      new MutableLiveData<>();
  private final MutableLiveData<Set<String>> selectedLiveData = new MutableLiveData<>();
  private final List<ConversationBean> conversations = new ArrayList<>();
  private final Set<String> originalSelectedIds = new HashSet<>();
  private final Set<String> selectedIds = new LinkedHashSet<>();
  private long offset = 0;
  private boolean finished;
  private boolean loading;
  private String groupId;

  public MutableLiveData<List<ConversationBean>> getConversationLiveData() {
    return conversationLiveData;
  }

  public MutableLiveData<Set<String>> getSelectedLiveData() {
    return selectedLiveData;
  }

  public void init(String groupId) {
    this.groupId = groupId;
    loadGroupMembers();
    loadRecentConversations(true);
  }

  public boolean hasMore() {
    return !finished;
  }

  public int getSelectedCount() {
    return selectedIds.size();
  }

  public boolean isSelected(String conversationId) {
    return selectedIds.contains(conversationId);
  }

  public List<ConversationBean> getSelectedConversations() {
    List<ConversationBean> result = new ArrayList<>();
    for (String selectedId : selectedIds) {
      for (ConversationBean bean : conversations) {
        if (bean != null && TextUtils.equals(selectedId, bean.getConversationId())) {
          result.add(bean);
          break;
        }
      }
    }
    return result;
  }

  public boolean toggle(ConversationBean bean) {
    if (bean == null || bean.infoData == null) {
      return false;
    }
    String conversationId = bean.getConversationId();
    if (selectedIds.contains(conversationId)) {
      selectedIds.remove(conversationId);
    } else {
      if (originalSelectedIds.size() + selectedIds.size() >= GROUP_MEMBER_MAX_COUNT) {
        return false;
      }
      selectedIds.add(conversationId);
    }
    selectedLiveData.setValue(new LinkedHashSet<>(selectedIds));
    return true;
  }

  public List<ConversationBean> filter(String keyword) {
    if (TextUtils.isEmpty(keyword)) {
      return getAddableConversations();
    }
    String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
    List<ConversationBean> result = new ArrayList<>();
    for (ConversationBean bean : conversations) {
      if (originalSelectedIds.contains(bean.getConversationId())) {
        continue;
      }
      String name = bean.getConversationName() == null ? "" : bean.getConversationName();
      if (name.toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
        result.add(bean);
      }
    }
    return result;
  }

  public void loadRecentConversations(boolean refresh) {
    if (refresh) {
      offset = 0;
      finished = false;
      conversations.clear();
    }
    if (finished || loading) {
      return;
    }
    loading = true;
    ConversationRepo.getConversationList(
        offset,
        PAGE_LIMIT,
        new FetchCallback<V2NIMConversationResult>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            loading = false;
          }

          @Override
          public void onSuccess(@Nullable V2NIMConversationResult data) {
            loading = false;
            if (data == null || data.getConversationList() == null) {
              finished = true;
              conversationLiveData.setValue(new ArrayList<>(conversations));
              return;
            }
            for (V2NIMConversation conversation : data.getConversationList()) {
              conversations.add(new ConversationBean(conversation));
            }
            Collections.sort(conversations, ConversationUtils.getConversationSortOrderComparator());
            offset = data.getOffset();
            finished = data.isFinished() || data.getConversationList().size() < PAGE_LIMIT;
            publishAddableConversations();
          }
        });
  }

  private void loadGroupMembers() {
    ConversationGroupRepo.getConversationListByGroupId(
        groupId,
        0,
        GROUP_MEMBER_MAX_COUNT,
        new FetchCallback<V2NIMConversationResult>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {}

          @Override
          public void onSuccess(@Nullable V2NIMConversationResult data) {
            originalSelectedIds.clear();
            selectedIds.clear();
            if (data != null && data.getConversationList() != null) {
              for (V2NIMConversation conversation : data.getConversationList()) {
                originalSelectedIds.add(conversation.getConversationId());
              }
            }
            selectedLiveData.setValue(new LinkedHashSet<>(selectedIds));
            publishAddableConversations();
          }
        });
  }

  private void publishAddableConversations() {
    List<ConversationBean> addableConversations = getAddableConversations();
    conversationLiveData.setValue(addableConversations);
    if (!finished && !loading && addableConversations.size() < PAGE_LIMIT) {
      loadRecentConversations(false);
    }
  }

  public void submit(SubmitCallback callback) {
    List<String> addIds = new ArrayList<>();
    for (String id : selectedIds) {
      if (!originalSelectedIds.contains(id)) {
        addIds.add(id);
      }
    }
    runAdd(addIds, callback);
  }

  private void runAdd(List<String> addIds, SubmitCallback callback) {
    if (addIds.isEmpty()) {
      if (callback != null) {
        callback.onSuccess();
      }
      return;
    }
    ConversationGroupRepo.addConversationsToGroup(
        groupId,
        addIds,
        new FetchCallback<List<V2NIMConversationOperationResult>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            if (callback != null) {
              callback.onError(errorCode, errorMsg);
            }
          }

          @Override
          public void onSuccess(@Nullable List<V2NIMConversationOperationResult> data) {
            int failedCount = getFailedResultCount(data);
            if (failedCount <= 0) {
              if (callback != null) {
                callback.onSuccess();
              }
              return;
            }
            if (failedCount < addIds.size()) {
              if (callback != null) {
                callback.onPartialSuccess();
              }
              return;
            }
            V2NIMError firstError = getFirstError(data);
            if (callback != null) {
              callback.onError(
                  firstError == null ? -1 : firstError.getCode(),
                  firstError == null ? null : firstError.getDesc());
            }
          }
        });
  }

  private int getFailedResultCount(@Nullable List<V2NIMConversationOperationResult> data) {
    if (data == null || data.isEmpty()) {
      return 0;
    }
    int count = 0;
    for (V2NIMConversationOperationResult result : data) {
      if (result != null && result.getError() != null) {
        count++;
      }
    }
    return count;
  }

  @Nullable
  private V2NIMError getFirstError(@Nullable List<V2NIMConversationOperationResult> data) {
    if (data == null) {
      return null;
    }
    for (V2NIMConversationOperationResult result : data) {
      if (result != null && result.getError() != null) {
        return result.getError();
      }
    }
    return null;
  }

  private List<ConversationBean> getAddableConversations() {
    List<ConversationBean> result = new ArrayList<>();
    for (ConversationBean bean : conversations) {
      if (bean == null || originalSelectedIds.contains(bean.getConversationId())) {
        continue;
      }
      result.add(bean);
    }
    return result;
  }
}
