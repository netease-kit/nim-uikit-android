// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.normal.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.common.ui.viewholder.ViewHolderClickListener;
import com.netease.yunxin.kit.conversationkit.ui.databinding.ConversationHeaderLayoutBinding;
import com.netease.yunxin.kit.conversationkit.ui.model.AIUserBean;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationHeaderBean;
import com.netease.yunxin.kit.conversationkit.ui.normal.ConversationTopAdapter;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

public class ConversationHeaderViewHolder extends BaseViewHolder<ConversationBean> {

  private ConversationHeaderLayoutBinding viewBinding;
  private ConversationTopAdapter topAdapter;

  public ConversationHeaderViewHolder(@NonNull ConversationHeaderLayoutBinding viewBinding) {
    super(viewBinding);
    LinearLayoutManager layoutManager =
        new LinearLayoutManager(
            viewBinding.getRoot().getContext(), LinearLayoutManager.HORIZONTAL, false);
    viewBinding.horizontalRecyclerView.setLayoutManager(layoutManager);
    topAdapter = new ConversationTopAdapter();
    viewBinding.horizontalRecyclerView.setAdapter(topAdapter);
    topAdapter.setViewHolderClickListener(
        new ViewHolderClickListener() {
          @Override
          public boolean onClick(View view, BaseBean data, int position) {
            if (data instanceof AIUserBean) {
              XKitRouter.withKey(RouterConstant.PATH_CHAT_P2P_PAGE)
                  .withParam(RouterConstant.CHAT_ID_KRY, ((AIUserBean) data).getAccountId())
                  .withContext(viewBinding.getRoot().getContext())
                  .navigate();
            }
            return true;
          }
        });
  }

  @Override
  public void onBindData(ConversationBean data, int position) {
    if (data != null && data instanceof ConversationHeaderBean) {
      ConversationHeaderBean headerBean = (ConversationHeaderBean) data;
      topAdapter.setData(headerBean.getUserList());
    }
  }
}
