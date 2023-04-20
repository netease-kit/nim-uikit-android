// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.contact;

import static com.netease.yunxin.kit.contactkit.ui.ContactConstant.LIB_TAG;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_ADD_FRIEND_PAGE;

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
import com.netease.yunxin.kit.contactkit.ui.databinding.ContactFragmentBinding;
import com.netease.yunxin.kit.contactkit.ui.interfaces.ContactActions;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactCallback;
import com.netease.yunxin.kit.contactkit.ui.model.ContactEntranceBean;
import com.netease.yunxin.kit.contactkit.ui.model.ContactFriendBean;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.List;

/** contact page */
public class ContactFragment extends BaseFragment {
  private final String TAG = "ContactFragment";
  private ContactViewModel viewModel;
  private ContactFragmentBinding viewBinding;
  private ContactUIConfig contactConfig;
  private Observer<FetchResult<List<ContactFriendBean>>> contactObserver;
  private Observer<FetchResult<List<ContactFriendBean>>> userInfoObserver;
  private IContactCallback contactCallback;

  public void setContactConfig(ContactUIConfig config) {
    contactConfig = config;
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    viewBinding = ContactFragmentBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(this).get(ContactViewModel.class);
    if (ContactKitClient.getContactUIConfig() != null && contactConfig == null) {
      contactConfig = ContactKitClient.getContactUIConfig();
    }
    if (contactConfig == null) {
      contactConfig = new ContactUIConfig();
    }
    return viewBinding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    //data observer
    contactObserver =
        contactBeansResult -> {
          if (contactBeansResult.getLoadStatus() == LoadStatus.Success) {
            ALog.d(LIB_TAG, TAG, "contactObserver,Success");
            viewBinding
                .contactLayout
                .getContactListView()
                .onFriendDataSourceChanged(contactBeansResult.getData());
          } else if (contactBeansResult.getLoadStatus() == LoadStatus.Finish) {
            if (contactBeansResult.getType() == FetchResult.FetchType.Add
                && contactBeansResult.getData() != null) {
              ALog.d(LIB_TAG, TAG, "contactObserver,Add");
              viewBinding
                  .contactLayout
                  .getContactListView()
                  .addFriendData(contactBeansResult.getData());
            } else if (contactBeansResult.getType() == FetchResult.FetchType.Remove
                && contactBeansResult.getData() != null) {
              ALog.d(LIB_TAG, TAG, "contactObserver,Remove");
              viewBinding
                  .contactLayout
                  .getContactListView()
                  .removeFriendData(contactBeansResult.getData());
            } else if (contactBeansResult.getType() == FetchResult.FetchType.Update
                && contactBeansResult.getData() != null) {
              ALog.d(LIB_TAG, TAG, "contactObserver,Update");
              viewBinding
                  .contactLayout
                  .getContactListView()
                  .updateFriendData(contactBeansResult.getData());
            }
          }
        };

    userInfoObserver =
        userInfoResult -> {
          if (userInfoResult.getLoadStatus() == LoadStatus.Finish) {
            viewBinding
                .contactLayout
                .getContactListView()
                .updateFriendData(userInfoResult.getData());
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
    viewBinding.contactLayout.getContactListView().setContactAction(actions);
  }

  private void loadConfig() {
    if (contactConfig != null) {
      if (contactConfig.viewHolderFactory != null) {
        viewBinding
            .contactLayout
            .getContactListView()
            .setViewHolderFactory(contactConfig.viewHolderFactory);
      }
      if (contactConfig.contactAttrs != null) {
        viewBinding.contactLayout.getContactListView().setViewConfig(contactConfig.contactAttrs);
      }
      if (contactConfig.customLayout != null) {
        contactConfig.customLayout.customizeContactLayout(viewBinding.contactLayout);
      }
    }
  }

  private void loadDefaultContactAction(ContactActions actions) {
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
        List<ContactEntranceBean> entranceBeanList =
            viewModel.getContactEntranceList(this.requireContext());
        for (ContactEntranceBean bean : entranceBeanList) {
          viewBinding.contactLayout.getContactListView().addContactData(bean);
        }
        viewModel
            .getContactEntranceLiveData()
            .observe(
                getViewLifecycleOwner(),
                contactEntranceBean -> {
                  if (contactCallback != null) {
                    contactCallback.updateUnreadCount(contactEntranceBean.number);
                  }
                  viewBinding
                      .contactLayout
                      .getContactListView()
                      .updateContactData(contactEntranceBean);
                });

        actions.addContactListener(
            IViewTypeConstant.CONTACT_ACTION_ENTER,
            (position, data) -> {
              if (!TextUtils.isEmpty(data.router)) {
                XKitRouter.withKey(data.router)
                    .withContext(ContactFragment.this.requireContext())
                    .navigate();
              }
            });

      } else {
        viewBinding.contactLayout.getContactListView().addContactData(contactConfig.headerData);
      }
    }
  }

  private void loadTitle() {
    if (contactConfig.showTitleBar) {
      viewBinding.contactLayout.getTitleBar().setVisibility(View.VISIBLE);
      if (contactConfig.titleColor != ContactUIConfig.INT_DEFAULT_NULL) {
        viewBinding.contactLayout.getTitleBar().setTitleColor(contactConfig.titleColor);
      }
      if (contactConfig.title != null) {
        viewBinding.contactLayout.getTitleBar().setTitle(contactConfig.title);
      } else {
        viewBinding
            .contactLayout
            .getTitleBar()
            .setTitle(getResources().getString(R.string.contact_title));
      }

    } else {
      viewBinding.contactLayout.getTitleBar().setVisibility(View.GONE);
    }
    if (contactConfig.showTitleBarRight2Icon) {
      viewBinding.contactLayout.getTitleBar().showRight2ImageView(true);
      if (contactConfig.titleBarRight2Res != ContactUIConfig.INT_DEFAULT_NULL) {
        viewBinding.contactLayout.getTitleBar().setRight2ImageRes(contactConfig.titleBarRight2Res);
      }

      viewBinding
          .contactLayout
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
      viewBinding.contactLayout.getTitleBar().showRight2ImageView(false);
    }

    if (contactConfig.showTitleBarRightIcon) {
      viewBinding.contactLayout.getTitleBar().showRightImageView(true);
      if (contactConfig.titleBarRightRes != ContactUIConfig.INT_DEFAULT_NULL) {
        viewBinding.contactLayout.getTitleBar().setRightImageRes(contactConfig.titleBarRightRes);
      }
      if (contactConfig.titleBarRightClick != null) {
        viewBinding
            .contactLayout
            .getTitleBar()
            .setRightImageClick(contactConfig.titleBarRightClick);
      } else {
        viewBinding
            .contactLayout
            .getTitleBar()
            .setRightImageClick(
                v ->
                    XKitRouter.withKey(PATH_ADD_FRIEND_PAGE)
                        .withContext(requireContext())
                        .navigate());
      }

    } else {
      viewBinding.contactLayout.getTitleBar().showRightImageView(false);
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
}
