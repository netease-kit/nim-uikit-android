// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.selector;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_REQUEST_SELECTOR_NAME;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.contact.ContactViewModel;
import com.netease.yunxin.kit.contactkit.ui.databinding.ContactSelectorActivityLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.interfaces.ContactActions;
import com.netease.yunxin.kit.contactkit.ui.model.ContactFriendBean;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import java.util.ArrayList;
import java.util.List;

public class ContactSelectorActivity extends BaseActivity {

  public static final int DEFAULT_MAX_SELECT_COUNT = 10;

  protected ContactSelectorActivityLayoutBinding binding;

  private ContactViewModel viewModel;

  private SelectedListAdapter selectedListAdapter;

  private ArrayList<String> filterUser;

  private int maxSelectCount = DEFAULT_MAX_SELECT_COUNT;

  private boolean enableReturnName;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ContactSelectorActivityLayoutBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    viewModel = new ViewModelProvider(this).get(ContactViewModel.class);
    initView();
    initData();
  }

  protected void initView() {
    binding
        .title
        .setOnBackIconClickListener(v -> onBackPressed())
        .setTitle(R.string.select)
        .setActionText(String.format(getString(R.string.selector_sure), 0))
        .setActionTextColor(getResources().getColor(R.color.color_337eff))
        .setActionListener(
            v -> {
              List<String> select = getSelectedAccount();
              if (select.size() < 1) {
                Toast.makeText(this, getString(R.string.select_empty_tips), Toast.LENGTH_LONG)
                    .show();
                return;
              }
              Intent result = new Intent();
              if (!selectedListAdapter.getSelectedFriends().isEmpty()) {
                result.putExtra(REQUEST_CONTACT_SELECTOR_KEY, getSelectedAccount());
                if (enableReturnName) {
                  result.putExtra(KEY_REQUEST_SELECTOR_NAME, getSelectedName());
                }
              }
              setResult(RESULT_OK, result);
              finish();
            });
    ContactActions actions = new ContactActions();
    actions.addSelectorListener(
        IViewTypeConstant.CONTACT_FRIEND,
        (selector, data) -> {
          if (selector) {
            if (selectedListAdapter.getItemCount() >= maxSelectCount) {
              Toast.makeText(
                      this,
                      getString(
                          R.string.contact_selector_max_count, String.valueOf(maxSelectCount)),
                      Toast.LENGTH_LONG)
                  .show();
              ((ContactFriendBean) data).setSelected(false);
              binding.contactListView.updateContactData((data));
            } else {
              selectedListAdapter.addFriend((ContactFriendBean) data);
            }
          } else {
            selectedListAdapter.removeFriend((ContactFriendBean) data);
          }
          binding.title.setActionText(
              String.format(getString(R.string.selector_sure), selectedListAdapter.getItemCount()));
        });
    binding.contactListView.setContactAction(actions);
    //top selected list
    LinearLayoutManager layoutManager =
        new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
    binding.rvSelected.setLayoutManager(layoutManager);
    selectedListAdapter = new SelectedListAdapter();
    selectedListAdapter.setItemClickListener(
        item -> {
          item.setSelected(false);
          binding.contactListView.updateContactData(item);
          binding.title.setActionText(
              String.format(getString(R.string.selector_sure), selectedListAdapter.getItemCount()));
        });
    binding.rvSelected.setAdapter(selectedListAdapter);
  }

  protected void initData() {
    filterUser = getIntent().getStringArrayListExtra(RouterConstant.SELECTOR_CONTACT_FILTER_KEY);
    maxSelectCount =
        getIntent()
            .getIntExtra(RouterConstant.KEY_CONTACT_SELECTOR_MAX_COUNT, DEFAULT_MAX_SELECT_COUNT);
    enableReturnName =
        getIntent().getBooleanExtra(RouterConstant.KEY_REQUEST_SELECTOR_NAME_ENABLE, false);
    viewModel
        .getContactLiveData()
        .observe(
            this,
            contactBeansResult -> {
              if (contactBeansResult.getLoadStatus() == LoadStatus.Success) {
                List<ContactFriendBean> accountList = filterUser(contactBeansResult.getData());
                binding.contactListView.onFriendDataSourceChanged(accountList);
                showEmptyView(accountList == null || accountList.size() < 1);
              }
            });
    viewModel.fetchContactList();
  }

  private void showEmptyView(boolean show) {
    if (show) {
      binding.emptyLayout.setVisibility(View.VISIBLE);
      binding.contactListView.setVisibility(View.GONE);
    } else {
      binding.contactListView.setVisibility(View.VISIBLE);
      binding.emptyLayout.setVisibility(View.GONE);
    }
  }

  private List<ContactFriendBean> filterUser(List<ContactFriendBean> source) {
    if (filterUser == null || filterUser.isEmpty()) {
      return source;
    }
    List<ContactFriendBean> result = new ArrayList<>(source);
    for (ContactFriendBean friendBean : source) {
      if (filterUser.contains(friendBean.data.getAccount())) {
        result.remove(friendBean);
      }
    }
    return result;
  }

  private ArrayList<String> getSelectedAccount() {
    ArrayList<String> result = new ArrayList<>();
    for (ContactFriendBean bean : selectedListAdapter.getSelectedFriends()) {
      result.add(bean.data.getAccount());
    }
    return result;
  }

  private ArrayList<String> getSelectedName() {
    ArrayList<String> result = new ArrayList<>();
    for (ContactFriendBean bean : selectedListAdapter.getSelectedFriends()) {
      if (bean.data.getUserInfo() == null) {
        continue;
      }
      String name = bean.data.getUserInfo().getName();
      result.add(TextUtils.isEmpty(name) ? bean.data.getAccount() : name);
    }
    return result;
  }
}
