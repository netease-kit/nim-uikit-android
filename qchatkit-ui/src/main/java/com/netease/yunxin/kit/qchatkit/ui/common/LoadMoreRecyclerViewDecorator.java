// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.common;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Objects;

/** The decorator for recyclerView is used to load more data. */
public class LoadMoreRecyclerViewDecorator<T> {
  public static final int LOAD_MORE_LIMIT = 5;
  public final RecyclerView innerView;
  public final LinearLayoutManager layoutManager;
  public final QChatCommonAdapter<?, ?> adapter;
  public LoadMoreListener<T> loadMoreListener;
  private T lastDataAnchor = null;

  public LoadMoreRecyclerViewDecorator(
      RecyclerView innerView, LinearLayoutManager layoutManager, QChatCommonAdapter<?, ?> adapter) {
    this.innerView = innerView;
    this.layoutManager = layoutManager;
    this.adapter = adapter;

    prepareForDecorator();
  }

  public void setLoadMoreListener(LoadMoreListener<T> loadMoreListener) {
    this.loadMoreListener = loadMoreListener;
  }

  @SuppressWarnings("unchecked")
  private void prepareForDecorator() {
    innerView.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
          @Override
          public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            if (newState != RecyclerView.SCROLL_STATE_IDLE) {
              return;
            }
            int position = layoutManager.findLastVisibleItemPosition();
            int total = adapter.getItemCount();
            if (loadMoreListener != null && total < position + LOAD_MORE_LIMIT) {
              Object data = adapter.getItemData(total - 1);
              T newAnchor = data != null ? (T) data : null;
              if (!Objects.equals(newAnchor, lastDataAnchor) || lastDataAnchor == null) {
                loadMoreListener.onLoadMore(newAnchor);
                lastDataAnchor = newAnchor;
              }
            }
          }
        });
  }

  /** load more listener */
  public interface LoadMoreListener<T> {
    /** @param data last data anchor */
    void onLoadMore(T data);
  }
}
