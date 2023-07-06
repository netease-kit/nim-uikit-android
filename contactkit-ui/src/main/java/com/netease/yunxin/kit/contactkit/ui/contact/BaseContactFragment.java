// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.contact;

import static com.netease.yunxin.kit.contactkit.ui.ContactConstant.LIB_TAG;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_ADD_FRIEND_PAGE;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.ContactKitClient;
import com.netease.yunxin.kit.contactkit.ui.ContactUIConfig;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.interfaces.ContactActions;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactCallback;
import com.netease.yunxin.kit.contactkit.ui.model.ContactEntranceBean;
import com.netease.yunxin.kit.contactkit.ui.model.ContactFriendBean;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.contactkit.ui.view.ContactLayout;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** contact page */
public abstract class BaseContactFragment extends BaseFragment {
  private final String TAG = "BaseContactFragment";
  protected ContactViewModel viewModel;
  private View rootView;
  protected ContactLayout contactLayout;
  protected View emptyView;
  protected ContactUIConfig contactConfig;
  protected Observer<FetchResult<List<ContactFriendBean>>> contactObserver;
  protected Observer<FetchResult<List<ContactFriendBean>>> userInfoObserver;
  protected IContactCallback contactCallback;
  protected int headerCount = 0;

  public void setContactConfig(ContactUIConfig config) {
    contactConfig = config;
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    rootView = initViewAndGetRootView(inflater, container, savedInstanceState);
    checkViews();
    viewModel = new ViewModelProvider(this).get(ContactViewModel.class);
    if (ContactKitClient.getContactUIConfig() != null && contactConfig == null) {
      contactConfig = ContactKitClient.getContactUIConfig();
    }
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    return rootView;
  }

  protected abstract View initViewAndGetRootView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

  protected void checkViews() {
    Objects.requireNonNull(rootView);
    Objects.requireNonNull(contactLayout);
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    //data observer
    contactObserver =
        contactBeansResult -> {
          if (contactBeansResult.getLoadStatus() == LoadStatus.Success) {
            ALog.d(LIB_TAG, TAG, "contactObserver, Success");
            contactLayout
                .getContactListView()
                .onFriendDataSourceChanged(contactBeansResult.getData());
          } else if (contactBeansResult.getLoadStatus() == LoadStatus.Finish) {
            if (contactBeansResult.getType() == FetchResult.FetchType.Add
                && contactBeansResult.getData() != null) {
              ALog.d(LIB_TAG, TAG, "contactObserver,Add");
              contactLayout.getContactListView().addFriendData(contactBeansResult.getData());
            } else if (contactBeansResult.getType() == FetchResult.FetchType.Remove
                && contactBeansResult.getData() != null) {
              ALog.d(LIB_TAG, TAG, "contactObserver,Remove");
              contactLayout.getContactListView().removeFriendData(contactBeansResult.getData());
            } else if (contactBeansResult.getType() == FetchResult.FetchType.Update
                && contactBeansResult.getData() != null) {
              ALog.d(LIB_TAG, TAG, "contactObserver,Update");
              contactLayout.getContactListView().updateFriendData(contactBeansResult.getData());
            }
          }
          if (emptyView != null) {
            if (contactLayout.getContactListView().getItemCount() - headerCount <= 0) {
              emptyView.setVisibility(View.VISIBLE);
            } else {
              emptyView.setVisibility(View.GONE);
            }
          }
        };

    userInfoObserver =
        userInfoResult -> {
          if (userInfoResult.getLoadStatus() == LoadStatus.Finish) {
            contactLayout.getContactListView().updateFriendData(userInfoResult.getData());
          }
        };

    initView();
    viewModel.getContactLiveData().observeForever(contactObserver);
    viewModel.getUserInfoLiveData().observeForever(userInfoObserver);
    viewModel.fetchContactList();
  }

  private void initView() {
    loadTitle();
    initContactAction();
    loadConfig();
  }

  private void initContactAction() {
    ContactActions actions = new ContactActions();
    //entrance data
    loadHeader(actions);

    //contact list
    loadDefaultContactAction(actions);

    //load contact action
    loadContactAction(actions);
  }

  private void loadContactAction(ContactActions actions) {
    if (contactConfig.itemClickListeners.size() > 0) {
      for (int index = 0; index < contactConfig.itemClickListeners.size(); index++) {
        actions.addContactListener(
            contactConfig.itemClickListeners.keyAt(index),
            contactConfig.itemClickListeners.valueAt(index));
      }
    }

    if (contactConfig.itemSelectorListeners.size() > 0) {
      for (int index = 0; index < contactConfig.itemSelectorListeners.size(); index++) {
        actions.addSelectorListener(
            contactConfig.itemSelectorListeners.keyAt(index),
            contactConfig.itemSelectorListeners.valueAt(index));
      }
    }
    contactLayout.getContactListView().setContactAction(actions);
  }

  private void loadConfig() {
    if (contactConfig != null) {
      if (contactConfig.viewHolderFactory != null) {
        contactLayout.getContactListView().setViewHolderFactory(contactConfig.viewHolderFactory);
      }
      if (contactConfig.contactAttrs != null) {
        contactLayout.getContactListView().setViewConfig(contactConfig.contactAttrs);
      }
      if (contactConfig.customLayout != null) {
        contactConfig.customLayout.customizeContactLayout(contactLayout);
      }
    }
  }

  protected void loadDefaultContactAction(ContactActions actions) {
    actions.addContactListener(
        IViewTypeConstant.CONTACT_FRIEND,
        (position, data) -> {
          FriendInfo friendInfo = ((ContactFriendBean) data).data;
          XKitRouter.withKey(RouterConstant.PATH_USER_INFO_PAGE)
              .withContext(requireContext())
              .withParam(RouterConstant.KEY_ACCOUNT_ID_KEY, friendInfo.getAccount())
              .navigate();
        });
  }

  public int getUnreadCount() {
    return viewModel.getVerifyCount();
  }

  public void setContactCallback(IContactCallback contactCallback) {
    this.contactCallback = contactCallback;
    if (viewModel != null && contactCallback != null) {
      this.contactCallback.updateUnreadCount(getUnreadCount());
    }
  }

  private void loadHeader(ContactActions actions) {
    if (contactConfig.showHeader) {
      if (contactConfig.headerData == null) {
        List<ContactEntranceBean> entranceBeanList = getContactEntranceList(this.requireContext());
        headerCount = entranceBeanList.size();
        viewModel.configVerifyBean(configVerifyBean());
        for (ContactEntranceBean bean : entranceBeanList) {
          contactLayout.getContactListView().addContactData(bean);
        }
        viewModel
            .getContactEntranceLiveData()
            .observe(
                getViewLifecycleOwner(),
                contactEntranceBean -> {
                  if (contactCallback != null) {
                    contactCallback.updateUnreadCount(contactEntranceBean.number);
                  }
                  contactLayout.getContactListView().updateContactData(contactEntranceBean);
                });

        actions.addContactListener(
            IViewTypeConstant.CONTACT_ACTION_ENTER,
            (position, data) -> {
              if (!TextUtils.isEmpty(data.router)) {
                XKitRouter.withKey(data.router)
                    .withContext(BaseContactFragment.this.requireContext())
                    .navigate();
              }
            });

      } else {
        contactLayout.getContactListView().addContactData(contactConfig.headerData);
      }
    }
  }

  protected void loadTitle() {
    if (contactConfig.showTitleBar) {
      contactLayout.getTitleBar().setVisibility(View.VISIBLE);
      if (contactConfig.titleColor != null) {
        contactLayout.getTitleBar().setTitleColor(contactConfig.titleColor);
      }
      if (contactConfig.title != null) {
        contactLayout.getTitleBar().setTitle(contactConfig.title);
      } else {
        contactLayout.getTitleBar().setTitle(getResources().getString(R.string.contact_title));
      }

    } else {
      contactLayout.getTitleBar().setVisibility(View.GONE);
    }
    if (contactConfig.showTitleBarRight2Icon) {
      contactLayout.getTitleBar().showRight2ImageView(true);
      if (contactConfig.titleBarRight2Res != null) {
        contactLayout.getTitleBar().setRight2ImageRes(contactConfig.titleBarRight2Res);
      }

      contactLayout
          .getTitleBar()
          .setRight2ImageClick(
              v -> {
                if (contactConfig.titleBarRight2Click != null) {
                  contactConfig.titleBarRight2Click.onClick(v);
                } else {
                  XKitRouter.withKey(RouterConstant.PATH_GLOBAL_SEARCH_PAGE)
                      .withContext(requireContext())
                      .navigate();
                }
              });

    } else {
      contactLayout.getTitleBar().showRight2ImageView(false);
    }

    if (contactConfig.showTitleBarRightIcon) {
      contactLayout.getTitleBar().showRightImageView(true);
      if (contactConfig.titleBarRightRes != null) {
        contactLayout.getTitleBar().setRightImageRes(contactConfig.titleBarRightRes);
      }
      if (contactConfig.titleBarRightClick != null) {
        contactLayout.getTitleBar().setRightImageClick(contactConfig.titleBarRightClick);
      } else {
        contactLayout
            .getTitleBar()
            .setRightImageClick(
                v ->
                    XKitRouter.withKey(PATH_ADD_FRIEND_PAGE)
                        .withContext(requireContext())
                        .navigate());
      }

    } else {
      contactLayout.getTitleBar().showRightImageView(false);
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    ALog.d(LIB_TAG, TAG, "onDestroy");
    if (viewModel != null) {
      viewModel.getContactLiveData().removeObserver(contactObserver);
    }
  }

  protected List<ContactEntranceBean> getContactEntranceList(Context context) {
    return Collections.emptyList();
  }

  protected ContactEntranceBean configVerifyBean() {
    return null;
  }
}
