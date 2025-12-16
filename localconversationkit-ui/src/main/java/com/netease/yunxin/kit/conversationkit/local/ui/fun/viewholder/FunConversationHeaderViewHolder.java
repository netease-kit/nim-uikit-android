// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.local.ui.fun.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.common.ui.viewholder.ViewHolderClickListener;
import com.netease.yunxin.kit.conversationkit.local.ui.databinding.LocalConversationHeaderLayoutBinding;
import com.netease.yunxin.kit.conversationkit.local.ui.fun.FunConversationTopAdapter;
import com.netease.yunxin.kit.conversationkit.local.ui.model.AIUserBean;
import com.netease.yunxin.kit.conversationkit.local.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.local.ui.model.ConversationHeaderBean;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

/** 会话列表头部ViewHolder，用于展示会话列表顶部的横向滚动区域（如AI用户快捷入口） */
public class FunConversationHeaderViewHolder extends BaseViewHolder<ConversationBean> {

  /** 头部布局绑定对象，包含横向RecyclerView等UI组件 */
  private LocalConversationHeaderLayoutBinding viewBinding;
  /** 横向列表适配器，用于展示顶部快捷联系人（如AI用户） */
  private FunConversationTopAdapter topAdapter;

  /**
   * 构造函数，初始化头部视图组件
   *
   * @param viewBinding 头部布局的视图绑定对象
   */
  public FunConversationHeaderViewHolder(
      @NonNull LocalConversationHeaderLayoutBinding viewBinding) {
    super(viewBinding);
    // 创建横向线性布局管理器（水平滚动，不反转）
    LinearLayoutManager layoutManager =
        new LinearLayoutManager(
            viewBinding.getRoot().getContext(), LinearLayoutManager.HORIZONTAL, false);
    viewBinding.horizontalRecyclerView.setLayoutManager(layoutManager);
    // 初始化顶部联系人适配器
    topAdapter = new FunConversationTopAdapter();
    viewBinding.horizontalRecyclerView.setAdapter(topAdapter);
    // 设置适配器项点击监听器
    topAdapter.setViewHolderClickListener(
        new ViewHolderClickListener() {
          @Override
          public boolean onClick(View view, BaseBean data, int position) {
            // 处理AI用户项点击：跳转到P2P聊天页面
            if (data instanceof AIUserBean) {
              XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_P2P_PAGE)
                  .withParam(RouterConstant.CHAT_ID_KRY, ((AIUserBean) data).getAccountId())
                  .withContext(viewBinding.getRoot().getContext())
                  .navigate();
            }
            return true;
          }
        });
  }

  /**
   * 绑定头部数据到视图
   *
   * @param data 会话数据，需为ConversationHeaderBean类型以包含头部用户列表
   * @param position 列表中的位置
   */
  @Override
  public void onBindData(ConversationBean data, int position) {
    // 仅处理ConversationHeaderBean类型数据，设置用户列表到适配器
    if (data instanceof ConversationHeaderBean) {
      ConversationHeaderBean headerBean = (ConversationHeaderBean) data;
      topAdapter.setData(headerBean.getUserList());
    }
  }
}
