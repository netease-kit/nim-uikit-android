// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.blacklist;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.blacklist.BaseBlackListActivity;
import com.netease.yunxin.kit.contactkit.ui.databinding.BaseListActivityLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.fun.selector.FunContactSelectorActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.view.FunContactViewHolderFactory;
import com.netease.yunxin.kit.contactkit.ui.fun.view.viewholder.FunBlackListViewHolder;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;

public class FunBlackListActivity extends BaseBlackListActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_ededed);
  }

  protected void configTitle(BaseListActivityLayoutBinding binding) {
    binding
        .title
        .setTitle(R.string.black_list)
        .setActionImg(R.mipmap.ic_title_bar_more)
        .setActionListener(
            v -> {
              Intent intent = new Intent(this, FunContactSelectorActivity.class);
              blackListLauncher.launch(intent);
            });
    binding.title.getTitleTextView().setTextSize(17);
    binding.title.getTitleTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
    binding.title.setBackgroundResource(R.color.color_ededed);
  }

  @Override
  protected void configViewHolderFactory() {
    binding.contactListView.setViewHolderFactory(
        new FunContactViewHolderFactory() {
          @Override
          protected BaseContactViewHolder getCustomViewHolder(ViewGroup view, int viewType) {
            if (viewType == IViewTypeConstant.CONTACT_BLACK_LIST) {
              FunBlackListViewHolder viewHolder = new FunBlackListViewHolder(view);
              viewHolder.setRelieveListener(
                  data -> {
                    if (!NetworkUtils.isConnected()) {
                      Toast.makeText(
                              FunBlackListActivity.this,
                              R.string.contact_network_error_tip,
                              Toast.LENGTH_SHORT)
                          .show();
                      return;
                    }
                    viewModel.removeBlackOp(
                        data.data.getAccount(),
                        new FetchCallback<Void>() {

                          @Override
                          public void onException(@Nullable Throwable exception) {
                            Toast.makeText(
                                    FunBlackListActivity.this,
                                    getText(R.string.remove_black_fail),
                                    Toast.LENGTH_SHORT)
                                .show();
                          }

                          @Override
                          public void onFailed(int code) {
                            Toast.makeText(
                                    FunBlackListActivity.this,
                                    getText(R.string.remove_black_fail),
                                    Toast.LENGTH_SHORT)
                                .show();
                          }

                          @Override
                          public void onSuccess(@Nullable Void param) {
                            binding.contactListView.removeContactData(data);
                          }
                        });
                  });
              return viewHolder;
            }
            return null;
          }
        });
  }
}
