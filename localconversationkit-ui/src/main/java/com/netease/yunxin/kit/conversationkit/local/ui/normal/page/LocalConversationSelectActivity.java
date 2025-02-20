// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.local.ui.normal.page;

import static com.netease.yunxin.kit.conversationkit.local.ui.common.ConversationConstant.LIB_TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.common.ui.viewholder.ViewHolderClickListener;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.conversationkit.local.ui.R;
import com.netease.yunxin.kit.conversationkit.local.ui.databinding.LocalConversationSelectActivityBinding;
import com.netease.yunxin.kit.conversationkit.local.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.local.ui.normal.page.viewmodel.SelectorViewModel;
import com.netease.yunxin.kit.conversationkit.local.ui.page.interfaces.ILoadListener;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import java.util.ArrayList;
import java.util.HashSet;

/** conversation select activity to select a conversation */
public class LocalConversationSelectActivity extends BaseLocalActivity implements ILoadListener {
  private final String TAG = "ConversationSelectActivity";
  private LocalConversationSelectActivityBinding viewBinding;
  private SelectorViewModel viewModel;
  private final HashSet<String> contactIdSet = new HashSet<>();

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ALog.d(LIB_TAG, TAG, "onCreate");
    viewBinding =
        LocalConversationSelectActivityBinding.inflate(LayoutInflater.from(this), null, false);
    setContentView(viewBinding.getRoot());

    viewBinding.conversationSelectorView.setViewHolderFactory(new SelectorViewHolderFactory());
    viewBinding.conversationSelectorTitleBar.setActionTextColor(
        getResources().getColor(R.color.color_666666));
    viewBinding.conversationSelectorTitleBar.setActionListener(
        v -> {
          Intent intent = new Intent();
          if (contactIdSet.size() > 0) {
            intent.putExtra(
                RouterConstant.KEY_CONVERSATION_SELECTOR_KEY,
                new ArrayList<String>().addAll(contactIdSet));
          }
          ALog.d(LIB_TAG, TAG, "ActionListener");

          setResult(RESULT_OK, intent);
          finish();
        });
    viewBinding.conversationSelectorTitleBar.setLeftActionListener(
        v -> {
          finish();
        });
    viewBinding.conversationSelectorView.setItemClickListener(
        new ViewHolderClickListener() {
          @Override
          public boolean onClick(View v, BaseBean data, int isCheck) {
            if (data instanceof ConversationBean) {
              if (isCheck == 1) {
                contactIdSet.add(((ConversationBean) data).infoData.getConversationId());
              } else {
                contactIdSet.remove(((ConversationBean) data).infoData.getConversationId());
              }
              ALog.d(LIB_TAG, TAG, "ItemClickListener,onClick:" + isCheck);
              updateTitleBar();
            }
            return true;
          }

          @Override
          public boolean onLongClick(View v, BaseBean data, int position) {
            ALog.d(LIB_TAG, TAG, "ItemClickListener,onLongClick");
            return false;
          }
        });
    viewBinding.conversationSelectorView.setLoadMoreListener(this);

    viewModel = new ViewModelProvider(this).get(SelectorViewModel.class);
    viewModel
        .getQueryLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                ALog.d(LIB_TAG, TAG, "QueryLiveData,Success");
                if (result.getType() == FetchResult.FetchType.Add) {
                  viewBinding.conversationSelectorView.addData(result.getData());
                } else {
                  viewBinding.conversationSelectorView.setData(result.getData());
                }
              }
            });

    viewModel.getConversationData();
  }

  private void updateTitleBar() {
    if (contactIdSet.size() < 1) {
      viewBinding.conversationSelectorTitleBar.setActionText(
          getResources().getString(R.string.sure_title));
      viewBinding.conversationSelectorTitleBar.setActionTextColor(
          getResources().getColor(R.color.color_666666));
    } else {
      String text =
          String.format(getResources().getString(R.string.sure_count_title), contactIdSet.size());
      viewBinding.conversationSelectorTitleBar.setActionText(text);
      viewBinding.conversationSelectorTitleBar.setActionTextColor(
          getResources().getColor(R.color.color_337eff));
    }
  }

  public static void start(Context context) {
    if (context != null) {
      Intent intent = new Intent(context, LocalConversationSelectActivity.class);
      context.startActivity(intent);
    }
  }

  @Override
  public boolean hasMore() {
    return viewModel.hasMore();
  }

  @Override
  public void loadMore(Object last) {
    viewModel.loadMore();
  }
}
