// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.selector;

import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_REQUEST_SELECTOR_NAME;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.contact.ContactViewModel;
import com.netease.yunxin.kit.contactkit.ui.interfaces.ContactActions;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.contactkit.ui.v2model.V2ContactFriendBean;
import com.netease.yunxin.kit.contactkit.ui.view.ContactListView;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class BaseContactSelectorActivity extends BaseActivity {

  public static final int DEFAULT_MAX_SELECT_COUNT = 10;

  private View rootView;

  protected BackTitleBar titleBar;

  protected RecyclerView rvSelected;

  protected ViewGroup emptyGroup;

  protected ContactListView contactListView;

  protected ContactViewModel viewModel;

  protected BaseSelectedListAdapter<?> selectedListAdapter;

  protected ArrayList<String> filterUser;

  protected int maxSelectCount = DEFAULT_MAX_SELECT_COUNT;

  protected boolean selectFinalCheckCountEnable = false;

  protected boolean checkNetworkEnable = true;

  protected boolean enableReturnName;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    rootView = initViewAndGetRootView(savedInstanceState);
    checkViews();
    setContentView(rootView);
    viewModel = new ViewModelProvider(this).get(ContactViewModel.class);
    viewModel.setSelectorPage(true);
    initView();
    initData();
  }

  protected abstract View initViewAndGetRootView(Bundle savedInstanceState);

  protected void checkViews() {
    Objects.requireNonNull(rootView);
    Objects.requireNonNull(titleBar);
    Objects.requireNonNull(rvSelected);
    Objects.requireNonNull(emptyGroup);
    Objects.requireNonNull(contactListView);
  }

  protected void initView() {
    configTitle(titleBar);
    ContactActions actions = new ContactActions();
    actions.addSelectorListener(
        IViewTypeConstant.CONTACT_FRIEND,
        (selector, data) -> {
          if (selector) {
            if (selectedListAdapter.getItemCount() >= maxSelectCount
                && !selectFinalCheckCountEnable) {
              Toast.makeText(
                      this,
                      getString(
                          R.string.contact_selector_max_count, String.valueOf(maxSelectCount)),
                      Toast.LENGTH_LONG)
                  .show();
              ((V2ContactFriendBean) data).setSelected(false);
              contactListView.updateContactData((data));
            } else {
              selectedListAdapter.addFriend((V2ContactFriendBean) data);
            }
          } else {
            selectedListAdapter.removeFriend((V2ContactFriendBean) data);
          }
          int count = selectedListAdapter.getItemCount();
          if (count <= 0) {
            titleBar.setActionText(getString(R.string.selector_sure_without_num));
          } else {
            titleBar.setActionText(
                String.format(
                    getString(R.string.selector_sure), selectedListAdapter.getItemCount()));
          }
        });
    contactListView.setContactAction(actions);
    //top selected list
    LinearLayoutManager layoutManager =
        new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
    rvSelected.setLayoutManager(layoutManager);
    selectedListAdapter = getSelectedListAdapter();
    selectedListAdapter.setItemClickListener(
        item -> {
          item.setSelected(false);
          contactListView.updateContactData(item);
          int count = selectedListAdapter.getItemCount();
          if (count <= 0) {
            titleBar.setActionText(getString(R.string.selector_sure_without_num));
          } else {
            titleBar.setActionText(
                String.format(
                    getString(R.string.selector_sure), selectedListAdapter.getItemCount()));
          }
        });
    rvSelected.setAdapter(selectedListAdapter);
  }

  protected BaseSelectedListAdapter<? extends ViewBinding> getSelectedListAdapter() {
    return null;
  }

  protected void configTitle(BackTitleBar titleBar) {
    titleBar
        .setOnBackIconClickListener(v -> onBackPressed())
        .setTitle(R.string.select)
        .setActionText(getString(R.string.selector_sure_without_num))
        .setActionTextColor(getResources().getColor(R.color.color_337eff))
        .setActionListener(
            v -> {
              if (checkNetworkEnable && !NetworkUtils.isConnected()) {
                Toast.makeText(this, R.string.contact_network_error_tip, Toast.LENGTH_SHORT).show();
                return;
              }
              List<String> select = getSelectedAccount();
              if (select.size() < 1) {
                Toast.makeText(this, getString(R.string.select_empty_tips), Toast.LENGTH_LONG)
                    .show();
                return;
              }
              if (selectedListAdapter.getItemCount() >= maxSelectCount
                  && selectFinalCheckCountEnable) {
                Toast.makeText(this, R.string.contact_selector_over_count, Toast.LENGTH_LONG)
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
  }

  protected void initData() {
    filterUser = getIntent().getStringArrayListExtra(RouterConstant.SELECTOR_CONTACT_FILTER_KEY);
    selectFinalCheckCountEnable =
        getIntent()
            .getBooleanExtra(RouterConstant.KEY_CONTACT_SELECTOR_FINAL_CHECK_COUNT_ENABLE, false);
    checkNetworkEnable =
        getIntent().getBooleanExtra(RouterConstant.KEY_CONTACT_SELECTOR_CHECK_NETWORK_ENABLE, true);
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
                List<V2ContactFriendBean> accountList = filterUser(contactBeansResult.getData());
                contactListView.onFriendDataSourceChanged(accountList);
                showEmptyView(accountList == null || accountList.size() < 1);
              }
            });
    viewModel.fetchContactList(true);
  }

  protected void showEmptyView(boolean show) {
    if (show) {
      emptyGroup.setVisibility(View.VISIBLE);
      contactListView.setVisibility(View.GONE);
    } else {
      contactListView.setVisibility(View.VISIBLE);
      emptyGroup.setVisibility(View.GONE);
    }
  }

  protected List<V2ContactFriendBean> filterUser(List<V2ContactFriendBean> source) {
    if (filterUser == null || filterUser.isEmpty()) {
      return source;
    }
    List<V2ContactFriendBean> result = new ArrayList<>(source);
    for (V2ContactFriendBean friendBean : source) {
      if (filterUser.contains(friendBean.data.getAccount())) {
        result.remove(friendBean);
      }
    }
    return result;
  }

  protected ArrayList<String> getSelectedAccount() {
    ArrayList<String> result = new ArrayList<>();
    for (V2ContactFriendBean bean : selectedListAdapter.getSelectedFriends()) {
      result.add(bean.data.getAccount());
    }
    return result;
  }

  protected ArrayList<String> getSelectedName() {
    ArrayList<String> result = new ArrayList<>();
    for (V2ContactFriendBean bean : selectedListAdapter.getSelectedFriends()) {
      if (bean.data.getUserInfo() == null) {
        continue;
      }
      String name = bean.data.getUserInfo().getName();
      result.add(TextUtils.isEmpty(name) ? bean.data.getAccount() : name);
    }
    return result;
  }
}
