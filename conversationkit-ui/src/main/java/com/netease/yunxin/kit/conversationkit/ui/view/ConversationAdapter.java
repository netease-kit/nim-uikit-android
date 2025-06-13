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
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationHeaderBean;
import com.netease.yunxin.kit.conversationkit.ui.page.DefaultViewHolderFactory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** 会话列表适配器 */
public class ConversationAdapter extends RecyclerView.Adapter<BaseViewHolder> {

  private final String TAG = "ConversationAdapter";
  // 默认使用默认的viewHolder工厂,用于创建viewHolder
  private IConversationFactory viewHolderFactory = new DefaultViewHolderFactory();
  // 会话列表数据
  private final List<ConversationBean> conversationList = new ArrayList<>();
  private final List<ConversationHeaderBean> conversationHeaderList = new ArrayList<>();
  // 数据比较器
  private Comparator<ConversationBean> dataComparator;
  // 点击事件监听
  private ViewHolderClickListener clickListener;
  // 是否显示
  private boolean isShow = true;
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
      notifyDataSetChanged();
    }
  }

  public void setHeaderData(List<ConversationHeaderBean> data) {
    conversationHeaderList.clear();
    if (data != null) {
      conversationHeaderList.addAll(data);
    }
    notifyDataSetChanged();
  }

  public void setShowTag(boolean show) {
    isShow = show;
    if (show) {
      notifyDataSetChanged();
    }
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
          int listIndex = insertIndex + conversationHeaderList.size();
          notifyItemInserted(listIndex);
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
          notifyItemChanged(j + conversationHeaderList.size());
        }
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
      notifyDataSetChanged();
    } else {
      int insertIndex = searchComparatorIndex(data, addStickTop);
      conversationList.add(insertIndex, data);
      if (isShow) {
        int listIndex = insertIndex + conversationHeaderList.size();
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
    for (int index = start; index < conversationList.size() && index < end; index++) {
      result.add(conversationHeaderList.get(index).getConversationId());
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
        index = index + conversationHeaderList.size();
        removeData(index);
      }
    }
  }

  public void removeAll() {
    conversationList.clear();
    conversationHeaderList.clear();
    notifyDataSetChanged();
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
      index = index + conversationHeaderList.size();
      removeData(index);
    }
  }

  public void removeData(int position) {
    if (position >= 0) {
      if (position < conversationHeaderList.size()) {
        conversationHeaderList.remove(position);
      } else if (position - conversationHeaderList.size() < conversationList.size()) {
        conversationList.remove(position - conversationHeaderList.size());
      }
      if (isShow) {
        notifyItemRemoved(position);
      }
    }
  }

  // 更新@信息
  public void updateAit(List<String> idList) {
    for (String id : idList) {
      for (int j = 0; j < conversationList.size(); j++) {
        if (TextUtils.equals(conversationList.get(j).getConversationId(), id)) {
          notifyItemChanged(j + conversationHeaderList.size());
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
      int listIndex = insertIndex + conversationHeaderList.size();
      if (isShow) {
        notifyItemMoved(index, listIndex);
        notifyItemChanged(listIndex);
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
      int listIndex = insertIndex + conversationHeaderList.size();
      int listRemoveIndex = index + conversationHeaderList.size();
      if (isShow) {
        notifyItemMoved(listRemoveIndex, listIndex);
        notifyItemChanged(listIndex);
      }
    }
  }

  public void setViewHolderFactory(IConversationFactory factory) {
    this.viewHolderFactory = factory;
  }

  public void setViewHolderClickListener(ViewHolderClickListener listener) {
    this.clickListener = listener;
  }

  public void setComparator(Comparator<ConversationBean> comparator) {
    this.dataComparator = comparator;
  }

  @NonNull
  @Override
  public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    BaseViewHolder viewHolder = null;
    if (viewHolderFactory != null) {
      viewHolder = viewHolderFactory.createViewHolder(parent, viewType);
    }
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
    holder.onBindData(this.getData(position), position);
    holder.setItemOnClickListener(clickListener);
  }

  @Override
  public int getItemViewType(int position) {
    return viewHolderFactory.getItemViewType(this.getData(position));
  }

  @Override
  public int getItemCount() {
    return conversationList.size() + conversationHeaderList.size();
  }

  public int getContentCount() {
    return conversationList.size();
  }

  public ConversationBean getData(int index) {
    if (index >= 0 && index < conversationHeaderList.size()) {
      return conversationHeaderList.get(index);
    } else if (index >= conversationHeaderList.size()
        && index < conversationList.size() + conversationHeaderList.size()) {
      return conversationList.get(index - conversationHeaderList.size());
    }
    return null;
  }

  public List<ConversationBean> getConversationList() {
    return conversationList;
  }
}
