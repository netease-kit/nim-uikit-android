// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.view;

import static com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant.LIB_TAG;

import android.text.TextUtils;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.common.ui.viewholder.ViewHolderClickListener;
import com.netease.yunxin.kit.conversationkit.ui.IConversationFactory;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationGroupBean;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationHeaderBean;
import com.netease.yunxin.kit.conversationkit.ui.page.DefaultViewHolderFactory;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** 会话列表适配器 */
public class ConversationAdapter extends RecyclerView.Adapter<BaseViewHolder> {

  private static final int VIEW_TYPE_CONVERSATION_GROUP_BAR = -10001;
  private final String TAG = "ConversationAdapter";
  // 默认使用默认的viewHolder工厂,用于创建viewHolder
  private IConversationFactory viewHolderFactory = new DefaultViewHolderFactory();
  // 会话列表数据
  private final List<ConversationBean> conversationList = new ArrayList<>();
  private final List<ConversationHeaderBean> conversationHeaderList = new ArrayList<>();
  private final List<ConversationGroupBean> conversationGroupList = new ArrayList<>();
  private String selectedGroupId = ConversationGroupBean.ID_ALL;
  private ConversationGroupBar.OnGroupClickListener groupClickListener;
  private boolean conversationGroupBarFunStyle;
  // 数据比较器
  private Comparator<ConversationBean> dataComparator;
  // 点击事件监听
  private ViewHolderClickListener clickListener;
  // 是否显示
  private boolean isShow = true;
  private boolean pendingRefreshWhenShown;
  // 布局管理器,用于滚动到指定位置和获取第一个可见位置
  private final LinearLayoutManager layoutManager;

  public ConversationAdapter(LinearLayoutManager layoutManager) {
    this.layoutManager = layoutManager;
  }

  // 设置数据，将原有数据清空，添加新数据
  public void setData(List<ConversationBean> data) {
    conversationList.clear();
    if (data != null) {
      conversationList.addAll(data);
      refreshDataSet();
    }
  }

  public void setHeaderData(List<ConversationHeaderBean> data) {
    conversationHeaderList.clear();
    if (data != null) {
      conversationHeaderList.addAll(data);
    }
    refreshDataSet();
  }

  public void setConversationGroups(
      List<ConversationGroupBean> groups,
      String selectedGroupId,
      ConversationGroupBar.OnGroupClickListener listener) {
    int oldGroupBarPosition = getGroupBarPosition();
    boolean hadGroupBar = hasGroupBar();
    boolean groupsChanged = !isSameGroups(conversationGroupList, groups);
    boolean selectedChanged = !TextUtils.equals(this.selectedGroupId, selectedGroupId);
    conversationGroupList.clear();
    if (groups != null) {
      conversationGroupList.addAll(groups);
    }
    this.selectedGroupId = selectedGroupId;
    groupClickListener = listener;
    boolean hasGroupBar = hasGroupBar();
    if (hadGroupBar != hasGroupBar || oldGroupBarPosition != getGroupBarPosition()) {
      refreshDataSet();
    } else if (hasGroupBar && (groupsChanged || selectedChanged)) {
      refreshItem(getGroupBarPosition());
    }
  }

  public void setSelectedGroupId(String selectedGroupId) {
    this.selectedGroupId = selectedGroupId;
    if (hasGroupBar()) {
      notifyItemChanged(getGroupBarPosition());
    }
  }

  public void setShowTag(boolean show) {
    isShow = show;
    if (show && pendingRefreshWhenShown) {
      pendingRefreshWhenShown = false;
      notifyDataSetChanged();
    }
  }

  public void refreshConversations() {
    refreshDataSet();
  }

  /** add data to list forward */
  public void addForwardData(List<ConversationBean> data) {
    if (data != null) {
      conversationList.addAll(0, data);
    }
  }

  // 添加数据，将新数据添加到原有数据的末尾
  public void appendData(List<ConversationBean> data) {
    if (data != null) {
      for (ConversationBean bean : data) {
        ALog.d(LIB_TAG, TAG, "appendData" + bean.getConversationId());
        int insertIndex = searchComparatorIndex(bean, false);
        conversationList.add(insertIndex, bean);
        if (isShow) {
          int listIndex = insertIndex + getContentStartPosition();
          notifyItemInserted(listIndex);
        } else {
          pendingRefreshWhenShown = true;
        }
      }
    }
  }

  // 更新数据
  public void update(List<ConversationBean> data) {
    for (int i = 0; data != null && i < data.size(); i++) {
      update(data.get(i));
    }
  }

  // 更新信息
  public void updateItem(List<String> idList) {
    for (String id : idList) {
      for (int j = 0; j < conversationList.size(); j++) {
        if (TextUtils.equals(conversationList.get(j).getConversationId(), id)) {
          notifyItemChanged(j + getContentStartPosition());
        }
      }
    }
  }

  public void resetBotConversationRouter(String conversationId) {
    for (int i = 0; i < conversationList.size(); i++) {
      ConversationBean conversation = conversationList.get(i);
      if (TextUtils.equals(conversation.getConversationId(), conversationId)) {
        conversation.router =
            RouterConstant.PATH_FUN_CHAT_BOT_SUB_SESSION_LIST_PAGE.equals(conversation.router)
                ? RouterConstant.PATH_FUN_CHAT_P2P_PAGE
                : RouterConstant.PATH_CHAT_P2P_PAGE;
        notifyItemChanged(i + getContentStartPosition());
        return;
      }
    }
  }

  // 更新数据，如果数据已存在，则更新，不存在则添加
  public void update(ConversationBean data) {
    ALog.d(LIB_TAG, TAG, "update" + data.getConversationId());
    int position = layoutManager.findFirstVisibleItemPosition();
    int removeIndex = -1;
    for (int j = 0; j < conversationList.size(); j++) {
      if (data.equals(conversationList.get(j))) {
        removeIndex = j;
        break;
      }
    }
    boolean addStickTop =
        data.infoData.isStickTop()
            && removeIndex > -1
            && !conversationList.get(removeIndex).infoData.isStickTop();
    ALog.d(LIB_TAG, TAG, "update, removeIndex:" + removeIndex);
    if (removeIndex > -1) {
      conversationList.remove(removeIndex);
      int insertIndex = searchComparatorIndex(data, addStickTop);
      ALog.d(
          LIB_TAG,
          TAG,
          "update, insertIndex:" + insertIndex + "unread:" + data.infoData.getUnreadCount());
      conversationList.add(insertIndex, data);
      refreshDataSet();
    } else {
      int insertIndex = searchComparatorIndex(data, addStickTop);
      conversationList.add(insertIndex, data);
      if (isShow) {
        int listIndex = insertIndex + getContentStartPosition();
        notifyItemInserted(listIndex);
      }
    }
    layoutManager.scrollToPosition(position);
  }

  public List<String> getContentDataID(int start, int end) {
    List<String> result = new ArrayList<>();
    if (start < 0) {
      start = 0;
    }
    for (int index = start; index < getItemCount() && index < end; index++) {
      ConversationBean bean = getData(index);
      if (bean != null && !TextUtils.isEmpty(bean.getConversationId())) {
        result.add(bean.getConversationId());
      }
    }
    return result;
  }

  private int searchComparatorIndex(ConversationBean data, boolean addStickTop) {
    int index = conversationList.size();
    for (int i = 0; i < conversationList.size(); i++) {
      if (dataComparator != null && dataComparator.compare(data, conversationList.get(i)) < 1) {
        index = i;
        break;
      }
    }

    return index;
  }

  public void removeData(List<String> dataList) {
    if (dataList == null || dataList.size() < 1) {
      return;
    }
    for (String data : dataList) {
      int index = -1;
      for (int j = 0; j < conversationList.size(); j++) {
        if (TextUtils.equals(data, conversationList.get(j).getConversationId())) {
          index = j;
          break;
        }
      }
      if (index > -1) {
        index = index + getContentStartPosition();
        removeData(index);
      }
    }
  }

  public void removeAll() {
    conversationList.clear();
    conversationHeaderList.clear();
    refreshDataSet();
  }

  public void removeData(String id) {
    int index = -1;
    for (int j = 0; j < conversationList.size(); j++) {
      if (TextUtils.equals(conversationList.get(j).getConversationId(), id)) {
        index = j;
        break;
      }
    }
    if (index > -1) {
      index = index + getContentStartPosition();
      removeData(index);
    }
  }

  public void removeData(int position) {
    if (position >= 0) {
      if (position < conversationHeaderList.size()) {
        conversationHeaderList.remove(position);
      } else if (position == getGroupBarPosition()) {
        conversationGroupList.clear();
      } else if (position - getContentStartPosition() < conversationList.size()) {
        conversationList.remove(position - getContentStartPosition());
      }
      if (isShow) {
        notifyItemRemoved(position);
      } else {
        pendingRefreshWhenShown = true;
      }
    }
  }

  // 更新@信息
  public void updateAit(List<String> idList) {
    for (String id : idList) {
      for (int j = 0; j < conversationList.size(); j++) {
        if (TextUtils.equals(conversationList.get(j).getConversationId(), id)) {
          notifyItemChanged(j + getContentStartPosition());
        }
      }
    }
  }

  // 添加置顶展示
  public void addStickTop(String id) {
    int index = -1;
    for (int j = 0; j < conversationList.size(); j++) {
      if (TextUtils.equals(conversationList.get(j).getConversationId(), id)) {
        index = j;
        break;
      }
    }
    if (index > -1) {
      conversationList.get(index).setStickTop(true);
      ConversationBean data = conversationList.remove(index);
      int insertIndex = searchComparatorIndex(data, true);
      conversationList.add(insertIndex, data);
      int listIndex = insertIndex + getContentStartPosition();
      int listRemoveIndex = index + getContentStartPosition();
      if (isShow) {
        notifyItemMoved(listRemoveIndex, listIndex);
        notifyItemChanged(listIndex);
      } else {
        pendingRefreshWhenShown = true;
      }
    }
  }

  // 移除置顶展示
  public void removeStickTop(String id) {
    int index = -1;
    for (int j = 0; j < conversationList.size(); j++) {
      if (TextUtils.equals(conversationList.get(j).getConversationId(), id)) {
        index = j;
        break;
      }
    }
    if (index > -1) {
      ConversationBean data = conversationList.remove(index);
      data.setStickTop(false);
      int insertIndex = searchComparatorIndex(data, false);
      conversationList.add(insertIndex, data);
      int listIndex = insertIndex + getContentStartPosition();
      int listRemoveIndex = index + getContentStartPosition();
      if (isShow) {
        notifyItemMoved(listRemoveIndex, listIndex);
        notifyItemChanged(listIndex);
      } else {
        pendingRefreshWhenShown = true;
      }
    }
  }

  public void setViewHolderFactory(IConversationFactory factory) {
    this.viewHolderFactory = factory;
  }

  public void setViewHolderClickListener(ViewHolderClickListener listener) {
    this.clickListener = listener;
  }

  public void setConversationGroupBarFunStyle(boolean funStyle) {
    this.conversationGroupBarFunStyle = funStyle;
    int position = getGroupBarPosition();
    if (position != RecyclerView.NO_POSITION) {
      notifyItemChanged(position);
    }
  }

  public void setComparator(Comparator<ConversationBean> comparator) {
    this.dataComparator = comparator;
  }

  @NonNull
  @Override
  public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    BaseViewHolder viewHolder = null;
    if (viewType == VIEW_TYPE_CONVERSATION_GROUP_BAR) {
      viewHolder =
          new ConversationGroupBarViewHolder(new ConversationGroupBar(parent.getContext()));
    } else if (viewHolderFactory != null) {
      viewHolder = viewHolderFactory.createViewHolder(parent, viewType);
    }
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
    if (position == getGroupBarPosition() && holder instanceof ConversationGroupBarViewHolder) {
      ((ConversationGroupBarViewHolder) holder)
          .bind(
              conversationGroupList,
              selectedGroupId,
              groupClickListener,
              conversationGroupBarFunStyle);
    } else {
      holder.onBindData(this.getData(position), position);
      holder.setItemOnClickListener(clickListener);
    }
  }

  @Override
  public int getItemViewType(int position) {
    if (position == getGroupBarPosition()) {
      return VIEW_TYPE_CONVERSATION_GROUP_BAR;
    }
    return viewHolderFactory.getItemViewType(this.getData(position));
  }

  @Override
  public int getItemCount() {
    return conversationList.size() + conversationHeaderList.size() + getGroupBarCount();
  }

  public int getContentCount() {
    return conversationList.size();
  }

  public ConversationBean getData(int index) {
    if (index >= 0 && index < conversationHeaderList.size()) {
      return conversationHeaderList.get(index);
    } else if (index == getGroupBarPosition()) {
      return null;
    } else if (index >= getContentStartPosition()
        && index < conversationList.size() + getContentStartPosition()) {
      return conversationList.get(index - getContentStartPosition());
    }
    return null;
  }

  public List<ConversationBean> getConversationList() {
    return conversationList;
  }

  public boolean hasGroupBar() {
    return !conversationGroupList.isEmpty();
  }

  public int getGroupBarPosition() {
    return hasGroupBar() ? conversationHeaderList.size() : RecyclerView.NO_POSITION;
  }

  private int getGroupBarCount() {
    return hasGroupBar() ? 1 : 0;
  }

  private int getContentStartPosition() {
    return conversationHeaderList.size() + getGroupBarCount();
  }

  private static class ConversationGroupBarViewHolder extends BaseViewHolder<ConversationBean> {
    private final ConversationGroupBar groupBar;

    ConversationGroupBarViewHolder(@NonNull ConversationGroupBar groupBar) {
      super(groupBar);
      this.groupBar = groupBar;
      groupBar.setLayoutParams(
          new RecyclerView.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    void bind(
        List<ConversationGroupBean> groups,
        String selectedGroupId,
        ConversationGroupBar.OnGroupClickListener listener,
        boolean funStyle) {
      groupBar.setFunStyle(funStyle);
      groupBar.setOnGroupClickListener(listener);
      groupBar.setGroupsAndSelected(groups, selectedGroupId);
    }

    @Override
    public void onBindData(ConversationBean data, int position) {}
  }

  private void refreshDataSet() {
    if (isShow) {
      notifyDataSetChanged();
    } else {
      pendingRefreshWhenShown = true;
    }
  }

  private void refreshItem(int position) {
    if (position == RecyclerView.NO_POSITION) {
      return;
    }
    if (isShow) {
      notifyItemChanged(position);
    } else {
      pendingRefreshWhenShown = true;
    }
  }

  private boolean isSameGroups(
      List<ConversationGroupBean> oldGroups, List<ConversationGroupBean> newGroups) {
    if (oldGroups == newGroups) {
      return true;
    }
    if (oldGroups == null || newGroups == null || oldGroups.size() != newGroups.size()) {
      return false;
    }
    for (int index = 0; index < oldGroups.size(); index++) {
      ConversationGroupBean oldGroup = oldGroups.get(index);
      ConversationGroupBean newGroup = newGroups.get(index);
      if (oldGroup == null || newGroup == null) {
        if (oldGroup != newGroup) {
          return false;
        }
        continue;
      }
      if (!TextUtils.equals(oldGroup.getId(), newGroup.getId())
          || oldGroup.getType() != newGroup.getType()
          || !TextUtils.equals(oldGroup.getName(), newGroup.getName())
          || oldGroup.isVisible() != newGroup.isVisible()
          || oldGroup.getSortOrder() != newGroup.getSortOrder()
          || oldGroup.getUnreadOrCount() != newGroup.getUnreadOrCount()) {
        return false;
      }
    }
    return true;
  }
}
