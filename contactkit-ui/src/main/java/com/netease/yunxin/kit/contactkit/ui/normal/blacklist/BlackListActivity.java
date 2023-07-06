// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.blacklist;

import android.content.Intent;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.blacklist.BaseBlackListActivity;
import com.netease.yunxin.kit.contactkit.ui.databinding.BaseListActivityLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.contactkit.ui.normal.selector.ContactSelectorActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.view.ContactViewHolderFactory;
import com.netease.yunxin.kit.contactkit.ui.normal.view.viewholder.BlackListViewHolder;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;

public class BlackListActivity extends BaseBlackListActivity {

  @Override
  protected void configViewHolderFactory() {
    binding.contactListView.setViewHolderFactory(
        new ContactViewHolderFactory() {
          @Override
          protected BaseContactViewHolder getCustomViewHolder(ViewGroup view, int viewType) {
            if (viewType == IViewTypeConstant.CONTACT_BLACK_LIST) {
              BlackListViewHolder viewHolder = new BlackListViewHolder(view);
              viewHolder.setRelieveListener(
                  data -> {
                    if (!NetworkUtils.isConnected()) {
                      Toast.makeText(
                              BlackListActivity.this,
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
                                    BlackListActivity.this,
                                    getText(R.string.remove_black_fail),
                                    Toast.LENGTH_SHORT)
                                .show();
                          }

                          @Override
                          public void onFailed(int code) {
                            Toast.makeText(
                                    BlackListActivity.this,
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

  protected void configTitle(BaseListActivityLayoutBinding binding) {
    binding
        .title
        .setTitle(R.string.black_list)
        .setActionImg(R.mipmap.ic_title_bar_more)
        .setActionListener(
            v -> {
              Intent intent = new Intent(this, ContactSelectorActivity.class);
              blackListLauncher.launch(intent);
            });
  }
}
