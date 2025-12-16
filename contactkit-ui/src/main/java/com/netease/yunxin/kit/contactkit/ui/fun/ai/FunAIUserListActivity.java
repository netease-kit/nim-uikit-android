// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.ai;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.ai.BaseAIUserListActivity;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.contactkit.ui.normal.view.ContactViewHolderFactory;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.AIUserViewHolder;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

/**
 * 娱乐版AI数字人列表页面
 *
 * <p>
 */
public class FunAIUserListActivity extends BaseAIUserListActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_ededed);
  }

  // 配置差异化UI

  @Override
  protected void initView() {
    super.initView();
    binding.title.setTitle(R.string.contact_ai_user_title);
    binding.title.getTitleTextView().setTextSize(17);
    binding.title.getTitleTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
    binding.title.setBackgroundResource(R.color.color_ededed);
    binding.emptyTv.setText(R.string.contact_ai_user_empty_tips);
    binding.emptyIv.setImageResource(R.drawable.fun_ic_contact_empty);
  }

  @Override
  protected void setViewHolder() {
    binding.contactListView.setViewHolderFactory(
        new ContactViewHolderFactory() {
          @Override
          protected BaseContactViewHolder getCustomViewHolder(ViewGroup view, int viewType) {
            if (viewType == IViewTypeConstant.CONTACT_AI_USER) {
              AIUserViewHolder viewHolder = new AIUserViewHolder(view, true);
              viewHolder.setItemClickListener(
                  bean -> {
                    // 点击事件
                    XKitRouter.withKey(RouterConstant.PATH_FUN_USER_INFO_PAGE)
                        .withContext(FunAIUserListActivity.this)
                        .withParam(RouterConstant.KEY_ACCOUNT_ID_KEY, bean.getAccountId())
                        .navigate();
                  });
              return viewHolder;
            }
            return null;
          }
        });
  }
}
