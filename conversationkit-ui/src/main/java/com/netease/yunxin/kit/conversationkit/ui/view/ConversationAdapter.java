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
          notifyItemInserted(insertIndex);
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
      if (isShow) {
        notifyItemMoved(removeIndex, insertIndex);
        notifyItemChanged(insertIndex);
      }
    } else {
      int insertIndex = searchComparatorIndex(data, addStickTop);
      conversationList.add(insertIndex, data);
      if (isShow) {
        notifyItemInserted(insertIndex);
      }
    }
    layoutManager.scrollToPosition(position);
  }

  private int searchComparatorIndex(ConversationBean data, boolean addStickTop) {
    int index = conversationList.size();
    // add stick must be insert 0
    if (addStickTop && data.infoData.isStickTop()) {
      return 0;
    }
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
        removeData(index);
      }
    }
  }

  public void removeAll() {
    conversationList.clear();
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
      removeData(index);
    }
  }

  public void removeData(int position) {
    if (position >= 0 && position < conversationList.size()) {
      conversationList.remove(position);
      if (isShow) {
        notifyItemRemoved(position);
      }
    }
  }

  // 更新@信息
  public void updateAit(List<String> idList) {
    int index = -1;
    for (String id : idList) {
      for (int j = 0; j < conversationList.size(); j++) {
        if (TextUtils.equals(conversationList.get(j).getConversationId(), id)) {
          index = j;
          break;
        }
      }
      if (index > -1) {
        notifyItemChanged(index);
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
      conversationList.add(0, data);
      if (isShow) {
        notifyItemMoved(index, 0);
        notifyItemChanged(0);
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
      if (isShow) {
        notifyItemMoved(index, insertIndex);
        notifyItemChanged(insertIndex);
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
    holder.onBindData(conversationList.get(position), position);
    holder.setItemOnClickListener(clickListener);
  }

  @Override
  public int getItemViewType(int position) {
    return viewHolderFactory.getItemViewType(conversationList.get(position));
  }

  @Override
  public int getItemCount() {
    return conversationList.size();
  }

  public ConversationBean getData(int index) {
    if (index >= 0 && index < conversationList.size()) {
      return conversationList.get(index);
    }
    return null;
  }
}
