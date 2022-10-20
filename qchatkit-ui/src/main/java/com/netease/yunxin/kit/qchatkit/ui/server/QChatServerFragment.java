// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.server;

import static com.netease.yunxin.kit.qchatkit.ui.server.viewmodel.QChatServerListViewModel.TYPE_REFRESH_CHANNEL;
import static com.netease.yunxin.kit.qchatkit.ui.server.viewmodel.QChatServerListViewModel.TYPE_SERVER_CREATE;
import static com.netease.yunxin.kit.qchatkit.ui.server.viewmodel.QChatServerListViewModel.TYPE_SERVER_REMOVE;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.corekit.model.ErrorMsg;
import com.netease.yunxin.kit.corekit.model.ResultInfo;
import com.netease.yunxin.kit.qchatkit.observer.ObserverUnreadInfoResultHelper;
import com.netease.yunxin.kit.qchatkit.repo.model.NextInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerInfo;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.channel.QChatChannelListFragment;
import com.netease.yunxin.kit.qchatkit.ui.common.LoadMoreRecyclerViewDecorator;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatFragmentBinding;
import com.netease.yunxin.kit.qchatkit.ui.server.adapter.QChatFragmentServerAdapter;
import com.netease.yunxin.kit.qchatkit.ui.server.model.QChatFragmentServerInfo;
import com.netease.yunxin.kit.qchatkit.ui.server.viewmodel.QChatServerListViewModel;
import com.netease.yunxin.kit.qchatkit.ui.utils.QChatUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The home page of the QChat, include server list and channel list. */
public class QChatServerFragment extends BaseFragment {
  private static final String TAG = "QChatServerFragment";
  /** Showing channel list. */
  private final QChatChannelListFragment fragment = new QChatChannelListFragment();

  private QChatFragmentBinding viewBinding;
  private LinearLayoutManager layoutManager;
  private QChatFragmentServerAdapter adapter;
  /** The server data source. */
  private final QChatServerListViewModel viewModel = new QChatServerListViewModel();
  /**
   * The observer of handling action with different types. Receiving the action to update server
   * list.
   */
  private final Observer<Pair<Integer, Long>> notificationObserver =
      new Observer<Pair<Integer, Long>>() {
        @Override
        public void onChanged(Pair<Integer, Long> integerLongPair) {
          switch (integerLongPair.first) {
            case TYPE_REFRESH_CHANNEL:
              fragment.refreshData(
                  integerLongPair.second,
                  ObserverUnreadInfoResultHelper.getUnreadInfoMap(integerLongPair.second));
              break;
            case TYPE_SERVER_REMOVE:
              adapter.removeServerById(integerLongPair.second);
              if (adapter.getItemCount() == 0) {
                viewBinding.groupNoServerTip.setVisibility(View.VISIBLE);
                fragment.hideChannelView();
              }
              break;
            case TYPE_SERVER_CREATE:
              if (viewBinding != null) {
                layoutManager.scrollToPosition(0);
                viewModel.init();
              }
          }
        }
      };

  /** The observer of updating server info. */
  private final Observer<ResultInfo<QChatServerInfo>> observerForUpdate =
      new Observer<ResultInfo<QChatServerInfo>>() {
        @Override
        public void onChanged(ResultInfo<QChatServerInfo> qChatServerInfoResultInfo) {
          if (qChatServerInfoResultInfo.getSuccess()
              && qChatServerInfoResultInfo.getValue() != null) {
            adapter.updateData(qChatServerInfoResultInfo.getValue());
          }
        }
      };
  /** The observer of loading data firstly. */
  private final Observer<ResultInfo<List<QChatServerInfo>>> observerForInit =
      new ObserverWrapper(true);
  /** The observer of loading more data, different from {@link #observerForInit}. */
  private final Observer<ResultInfo<List<QChatServerInfo>>> observerForLoadMore =
      new ObserverWrapper(false);
  /** The observer of receiving unread count changed message. */
  private final Observer<List<Long>> observerUnreadInfo =
      new Observer<List<Long>>() {
        @Override
        public void onChanged(List<Long> longs) {
          adapter.updateUnreadInfoList(longs);
        }
      };

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    viewBinding = QChatFragmentBinding.inflate(inflater, container, false);
    return viewBinding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    FragmentManager manager = getChildFragmentManager();
    // add channel fragment.
    manager.beginTransaction().add(R.id.ryChannelFragment, fragment).commitAllowingStateLoss();
    // creating or join a server.
    viewBinding.ivAddServer.setOnClickListener(
        v ->
            startActivity(
                new Intent(getContext(), QChatCreateWayActivity.class),
                ActivityOptionsCompat.makeCustomAnimation(
                        v.getContext(), R.anim.anim_from_bottom_to_top, R.anim.anim_empty_with_time)
                    .toBundle()));
    // render server list.
    adapter = new QChatFragmentServerAdapter(getContext());
    adapter.setItemClickListener(
        (data, holder) -> fragment.updateData(data.serverInfo, data.unreadInfoItemMap));
    adapter.setRefreshListener(data -> fragment.refreshUnreadData());
    layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
    viewBinding.ryServerList.setLayoutManager(layoutManager);
    viewBinding.ryServerList.setAdapter(adapter);
    // to hook loading more data.
    LoadMoreRecyclerViewDecorator<QChatFragmentServerInfo> loadMoreRecyclerViewDecorator =
        new LoadMoreRecyclerViewDecorator<>(viewBinding.ryServerList, layoutManager, adapter);
    loadMoreRecyclerViewDecorator.setLoadMoreListener(
        data -> {
          // In the function, user can load more data by QChatServerListViewModel.
          ALog.d(TAG, "load more data");
          NextInfo info;
          if (data == null || data.serverInfo.getNextInfo() == null) {
            info = null;
          } else {
            info = data.serverInfo.getNextInfo();
          }
          if (info != null && !info.getHasMore()) {
            ALog.d(TAG, "load more data, info is null or no more.");
            return;
          }
          long timeTag = info != null ? info.getNextTimeTag() : 0;
          if (timeTag == 0) {
            ALog.d(TAG, "load more data, time tat is init state.");
            return;
          }
          ALog.d(TAG, "load more data, time tag is " + timeTag);
          viewModel.loadMore(timeTag);
        });

    // add observers to QChatServerListViewModel.
    viewModel.getOnRefreshResult().observeForever(notificationObserver);
    viewModel.getUpdateItemResult().observeForever(observerForUpdate);
    viewModel.getInitResult().observeForever(observerForInit);
    viewModel.getLoadMoreResult().observeForever(observerForLoadMore);
    viewModel.getUnreadInfoResult().observeForever(observerUnreadInfo);

    // to load data firstly, showing network error when network was broken.
    QChatUtils.isConnectedToastAndRun(getContext(), this::refreshServerList);
  }

  private void actionForSuccess(List<QChatServerInfo> data, boolean clear) {
    ALog.d(TAG, "actionForSuccess, clear flag is " + clear + ", data is " + data);
    if (data != null && !data.isEmpty()) {
      viewBinding.groupNoServerTip.setVisibility(View.GONE);
      List<QChatFragmentServerInfo> serverInfoList = new ArrayList<>();
      for (QChatServerInfo serverInfo : data) {
        serverInfoList.add(new QChatFragmentServerInfo(serverInfo));
      }
      fragment.showChannelView();
      adapter.addDataList(serverInfoList, clear);
    } else if (adapter.getItemCount() == 0 || clear) {
      viewBinding.groupNoServerTip.setVisibility(View.VISIBLE);
      fragment.hideChannelView();
      adapter.addDataList(Collections.emptyList(), true);
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    // to remove observers, avoiding memory leak.
    viewModel.getUnreadInfoResult().removeObserver(observerUnreadInfo);
    viewModel.getOnRefreshResult().removeObserver(notificationObserver);
    viewModel.getInitResult().removeObserver(observerForInit);
    viewModel.getLoadMoreResult().removeObserver(observerForLoadMore);
    viewModel.getUpdateItemResult().removeObserver(observerForUpdate);
  }

  public void refreshServerList() {
    ALog.d(TAG, "refreshServerList");
    viewModel.init();
  }

  /** Handling the change of initialization or loading more data. */
  private class ObserverWrapper implements Observer<ResultInfo<List<QChatServerInfo>>> {
    public final boolean clear;

    public ObserverWrapper(boolean clear) {
      this.clear = clear;
    }

    @Override
    public void onChanged(ResultInfo<List<QChatServerInfo>> listResultInfo) {
      if (listResultInfo.getSuccess()) {
        List<QChatServerInfo> data = listResultInfo.getValue();
        actionForSuccess(data, clear);
      } else {
        ErrorMsg msg = listResultInfo.getMsg();
        Toast.makeText(
                getContext(),
                getString(R.string.qchat_server_request_fail) + (msg != null ? msg.getCode() : ""),
                Toast.LENGTH_SHORT)
            .show();
      }
    }
  }
}
