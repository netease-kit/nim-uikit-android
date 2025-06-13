// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.local.ui.view;

import static com.netease.yunxin.kit.conversationkit.local.ui.common.ConversationConstant.LIB_TAG;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.viewholder.ViewHolderClickListener;
import com.netease.yunxin.kit.conversationkit.local.ui.ILocalConversationFactory;
import com.netease.yunxin.kit.conversationkit.local.ui.R;
import com.netease.yunxin.kit.conversationkit.local.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.local.ui.model.ConversationHeaderBean;
import com.netease.yunxin.kit.conversationkit.local.ui.page.interfaces.ILoadListener;
import java.util.Comparator;
import java.util.List;

/** 会话View，封装会话列表的视图 UI和处理逻辑聚合 */
public class ConversationView extends FrameLayout {

  private final String TAG = "ConversationView";
  private RecyclerView recyclerView;
  private ConversationAdapter adapter;
  private ILoadListener loadMoreListener;
  private final int LOAD_MORE_DIFF = 5;

  public ConversationView(Context context) {
    super(context);
    init(null);
  }

  public ConversationView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
  }

  public ConversationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs);
  }

  public RecyclerView getRecyclerView() {
    return this.recyclerView;
  }

  private void init(AttributeSet attrs) {
    recyclerView = new RecyclerView(getContext());
    recyclerView.setId(R.id.conversation_rv);
    this.addView(
        recyclerView,
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
    adapter = new ConversationAdapter(layoutManager);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);
    // 监听滚动，当滚动到底部触发加载更多
    recyclerView.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
          @Override
          public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
              int position = layoutManager.findLastVisibleItemPosition();
              if (loadMoreListener != null) {
                if (loadMoreListener.hasMore()
                    && adapter.getItemCount() < position + LOAD_MORE_DIFF) {
                  ConversationBean last = adapter.getData(adapter.getItemCount() - 1);
                  loadMoreListener.loadMore(last);
                }
                int startPosition = layoutManager.findFirstVisibleItemPosition();
                loadMoreListener.onScrollStateIdle(startPosition, position);
              }
            }
          }

          @Override
          public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
          }
        });
  }

  // 设置加载更多监听
  public void setLoadMoreListener(ILoadListener listener) {
    this.loadMoreListener = listener;
  }

  // 设置点击事件监听，用于处理点击事件
  public void setItemClickListener(ViewHolderClickListener listener) {
    adapter.setViewHolderClickListener(listener);
  }

  // 列表RecyclerView添加分割线
  public void addItemDecoration(RecyclerView.ItemDecoration decoration) {
    recyclerView.addItemDecoration(decoration);
  }

  // 设置ViewHolder工厂，用于创建ViewHolder
  public void setViewHolderFactory(ILocalConversationFactory factory) {
    adapter.setViewHolderFactory(factory);
  }

  // 设置比较器，用于排序
  public void setComparator(Comparator<ConversationBean> comparator) {
    this.adapter.setComparator(comparator);
  }

  // 设置数据，用于初始化数据，清理并添加数据
  public void setData(List<ConversationBean> data) {
    if (adapter != null) {
      adapter.setData(data);
    }
  }

  public void setHeaderData(List<ConversationHeaderBean> data) {
    if (adapter != null) {
      adapter.setHeaderData(data);
    }
  }

  // 添加数据，追加到列表末尾
  public void addData(List<ConversationBean> data) {
    if (adapter != null) {
      adapter.appendData(data);
    }
  }

  public void addForwardData(List<ConversationBean> data) {
    if (adapter != null) {
      adapter.addForwardData(data);
    }
  }

  // 更新数据，用于更新数据，替换原有数据，没有则添加
  public void update(List<ConversationBean> data) {
    if (adapter != null) {
      ALog.d(LIB_TAG, TAG, "update ConversationBean list, start");
      adapter.update(data);
      ALog.d(LIB_TAG, TAG, "update ConversationBean list, end");
    }
  }

  // 更新数据，用于更新数据，替换原有数据，没有则添加
  public void update(ConversationBean data) {
    if (adapter != null) {
      ALog.d(LIB_TAG, TAG, "update ConversationBean, start");
      adapter.update(data);
      ALog.d(LIB_TAG, TAG, "update ConversationBean, end");
    }
  }

  // 移除数据
  public void remove(List<String> data) {
    ALog.d(LIB_TAG, TAG, " remove, start");
    if (adapter != null) {
      adapter.removeData(data);
    }
  }

  public void removeAll() {
    if (adapter != null) {
      adapter.removeAll();
    }
  }

  public int getDataSize() {
    if (adapter != null) {
      return adapter.getItemCount();
    }
    return 0;
  }

  public int getContentDataSize() {
    if (adapter != null) {
      return adapter.getContentCount();
    }
    return 0;
  }

  /**
   * 获取指定位置的会话ID
   *
   * @param start
   * @param end
   * @return
   */
  public List<String> getContentDataID(int start, int end) {
    if (adapter != null) {
      return adapter.getContentDataID(start, end);
    }
    return null;
  }

  // 移除会话
  public void removeConversation(String id) {
    if (adapter != null) {
      adapter.removeData(id);
    }
  }

  // 更新@信息
  public void updateConversation(List<String> idList) {
    if (adapter != null) {
      adapter.updateItem(idList);
    }
  }

  // 添加置顶UI
  public void addStickTop(String id) {
    if (adapter != null) {
      adapter.addStickTop(id);
    }
  }

  // 移除置顶UI
  public void removeStickTop(String id) {
    if (adapter != null) {
      adapter.removeStickTop(id);
    }
  }

  public List<ConversationBean> getDataList() {
    if (adapter != null) {
      return adapter.getConversationList();
    }
    return null;
  }

  public void setShowTag(boolean show) {
    if (adapter != null) {
      adapter.setShowTag(show);
    }
  }
}
