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
/** 会话View，封装会话列表的视图 UI和处理逻辑聚合 负责会话列表的展示、数据管理、事件监听等核心功能，内部通过RecyclerView实现列表展示 */
public class ConversationView extends FrameLayout {

  /** 日志标签，用于调试输出 */
  private final String TAG = "ConversationView";
  /** 会话列表RecyclerView，用于展示会话数据 */
  private RecyclerView recyclerView;
  /** 会话列表适配器，负责数据与视图的绑定 */
  private ConversationAdapter adapter;
  /** 加载更多监听器，用于触发列表底部加载更多数据 */
  private ILoadListener loadMoreListener;
  /** 加载更多触发阈值，当滚动到距离底部该数量项时触发加载更多 */
  private final int LOAD_MORE_DIFF = 5;

  /**
   * 构造函数，在代码中创建View时调用
   *
   * @param context 上下文环境
   */
  public ConversationView(Context context) {
    super(context);
    init(null);
  }

  /**
   * 构造函数，在XML布局中使用时调用
   *
   * @param context 上下文环境
   * @param attrs XML属性集合
   */
  public ConversationView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
  }

  /**
   * 构造函数，在XML布局中使用并指定样式时调用
   *
   * @param context 上下文环境
   * @param attrs XML属性集合
   * @param defStyleAttr 默认样式属性
   */
  public ConversationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs);
  }

  /**
   * 获取内部RecyclerView实例
   *
   * @return 会话列表RecyclerView
   */
  public RecyclerView getRecyclerView() {
    return this.recyclerView;
  }

  /**
   * 初始化View组件和数据
   *
   * @param attrs XML属性集合，可用于从布局中读取自定义属性
   */
  private void init(AttributeSet attrs) {
    // 创建RecyclerView并设置ID
    recyclerView = new RecyclerView(getContext());
    recyclerView.setId(R.id.conversation_rv);
    // 将RecyclerView添加到当前FrameLayout，设置宽高为匹配父容器
    this.addView(
        recyclerView,
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    // 创建线性布局管理器（垂直方向）
    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
    // 初始化适配器并关联布局管理器
    adapter = new ConversationAdapter(layoutManager);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);

    // 设置RecyclerView滚动监听器，用于实现加载更多功能
    recyclerView.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
          @Override
          public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            // 当滚动停止且处于空闲状态时判断是否需要加载更多
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
              // 获取最后一个可见项的位置
              int position = layoutManager.findLastVisibleItemPosition();
              if (loadMoreListener != null) {
                // 若还有更多数据，且当前可见位置接近列表底部（小于阈值），触发加载更多
                if (loadMoreListener.hasMore()
                    && adapter.getItemCount() < position + LOAD_MORE_DIFF) {
                  // 获取列表最后一项数据，作为加载更多的起始位置参数
                  ConversationBean last = adapter.getData(adapter.getItemCount() - 1);
                  loadMoreListener.loadMore(last);
                }
                // 通知监听器滚动已停止，并传递可见区域的起始和结束位置
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

  /**
   * 设置加载更多监听器
   *
   * @param listener 加载更多回调接口，当需要加载更多数据时触发
   */
  public void setLoadMoreListener(ILoadListener listener) {
    this.loadMoreListener = listener;
  }

  /**
   * 设置列表项点击事件监听器
   *
   * @param listener 点击事件回调接口，用于处理列表项的点击和长按事件
   */
  public void setItemClickListener(ViewHolderClickListener listener) {
    adapter.setViewHolderClickListener(listener);
  }

  /**
   * 为RecyclerView添加分割线
   *
   * @param decoration 分割线装饰器，用于自定义列表项之间的分隔样式
   */
  public void addItemDecoration(RecyclerView.ItemDecoration decoration) {
    recyclerView.addItemDecoration(decoration);
  }

  /**
   * 设置ViewHolder工厂
   *
   * @param factory 会话列表项工厂，用于创建不同类型的会话ViewHolder（如个人/群组会话）
   */
  public void setViewHolderFactory(ILocalConversationFactory factory) {
    adapter.setViewHolderFactory(factory);
  }

  /**
   * 设置数据比较器
   *
   * @param comparator 会话数据比较器，用于对列表数据进行排序（如置顶优先、时间倒序等）
   */
  public void setComparator(Comparator<ConversationBean> comparator) {
    this.adapter.setComparator(comparator);
  }

  /**
   * 初始化设置列表数据
   *
   * @param data 会话数据列表，会清空现有数据并替换为新数据
   */
  public void setData(List<ConversationBean> data) {
    if (adapter != null) {
      adapter.setData(data);
    }
  }

  /**
   * 设置头部数据
   *
   * @param data 头部数据列表，用于在列表顶部展示特殊内容（如AI助手快捷入口）
   */
  public void setHeaderData(List<ConversationHeaderBean> data) {
    if (adapter != null) {
      adapter.setHeaderData(data);
    }
  }

  /**
   * 追加数据到列表末尾
   *
   * @param data 待追加的会话数据列表
   */
  public void addData(List<ConversationBean> data) {
    if (adapter != null) {
      adapter.appendData(data);
    }
  }

  /**
   * 向前添加数据（通常添加到列表头部）
   *
   * @param data 待添加的会话数据列表
   */
  public void addForwardData(List<ConversationBean> data) {
    if (adapter != null) {
      adapter.addForwardData(data);
    }
  }

  /**
   * 更新会话列表数据（批量）
   *
   * @param data 待更新的数据列表，会替换现有相同ID的项，不存在则新增
   */
  public void update(List<ConversationBean> data) {
    if (adapter != null) {
      ALog.d(LIB_TAG, TAG, "update ConversationBean list, start");
      adapter.update(data);
      ALog.d(LIB_TAG, TAG, "update ConversationBean list, end");
    }
  }

  /**
   * 更新单个会话数据
   *
   * @param data 待更新的会话数据，会替换现有相同ID的项，不存在则新增
   */
  public void update(ConversationBean data) {
    if (adapter != null) {
      ALog.d(LIB_TAG, TAG, "update ConversationBean, start");
      adapter.update(data);
      ALog.d(LIB_TAG, TAG, "update ConversationBean, end");
    }
  }

  /**
   * 移除指定ID的会话数据（批量）
   *
   * @param data 待移除会话的ID列表
   */
  public void remove(List<String> data) {
    ALog.d(LIB_TAG, TAG, " remove, start");
    if (adapter != null) {
      adapter.removeData(data);
    }
  }

  /** 清空列表所有数据 */
  public void removeAll() {
    if (adapter != null) {
      adapter.removeAll();
    }
  }

  /**
   * 获取列表总项数（包含头部和内容项）
   *
   * @return 列表总项数
   */
  public int getDataSize() {
    if (adapter != null) {
      return adapter.getItemCount();
    }
    return 0;
  }

  /**
   * 获取内容数据项数量（不包含头部）
   *
   * @return 内容数据项数量
   */
  public int getContentDataSize() {
    if (adapter != null) {
      return adapter.getContentCount();
    }
    return 0;
  }

  /**
   * 获取指定位置范围内的会话ID列表
   *
   * @param start 起始位置（包含）
   * @param end 结束位置（包含）
   * @return 会话ID列表，若位置无效或无数据则返回null
   */
  public List<String> getContentDataID(int start, int end) {
    if (adapter != null) {
      return adapter.getContentDataID(start, end);
    }
    return null;
  }

  /**
   * 移除单个会话
   *
   * @param id 待移除会话的ID
   */
  public void removeConversation(String id) {
    if (adapter != null) {
      adapter.removeData(id);
    }
  }

  /**
   * 更新指定会话的@提及状态
   *
   * @param idList 需要更新@状态的会话ID列表
   */
  public void updateConversation(List<String> idList) {
    if (adapter != null) {
      adapter.updateItem(idList);
    }
  }

  /**
   * 添加会话置顶标记
   *
   * @param id 需要置顶的会话ID
   */
  public void addStickTop(String id) {
    if (adapter != null) {
      adapter.addStickTop(id);
    }
  }

  /**
   * 移除会话置顶标记
   *
   * @param id 需要取消置顶的会话ID
   */
  public void removeStickTop(String id) {
    if (adapter != null) {
      adapter.removeStickTop(id);
    }
  }

  /**
   * 获取当前会话数据列表
   *
   * @return 会话数据列表，若适配器为空则返回null
   */
  public List<ConversationBean> getDataList() {
    if (adapter != null) {
      return adapter.getConversationList();
    }
    return null;
  }

  /**
   * 设置是否显示标签（如@提及标签）
   *
   * @param show true-显示标签，false-隐藏标签
   */
  public void setShowTag(boolean show) {
    if (adapter != null) {
      adapter.setShowTag(show);
    }
  }
}
