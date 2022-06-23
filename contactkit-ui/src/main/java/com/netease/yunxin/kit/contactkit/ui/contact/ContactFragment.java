/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.contact;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.ContactUIConfig;
import com.netease.yunxin.kit.contactkit.ui.ContactKitClient;
import com.netease.yunxin.kit.contactkit.ui.FragmentBuilder;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.addfriend.AddFriendActivity;
import com.netease.yunxin.kit.contactkit.ui.databinding.ContactFragmentBinding;
import com.netease.yunxin.kit.contactkit.ui.interfaces.ContactActions;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactCallback;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactClickListener;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactSelectorListener;
import com.netease.yunxin.kit.contactkit.ui.model.ContactEntranceBean;
import com.netease.yunxin.kit.contactkit.ui.model.ContactFriendBean;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.contactkit.ui.model.MenuBean;
import com.netease.yunxin.kit.contactkit.ui.userinfo.UserInfoActivity;
import com.netease.yunxin.kit.contactkit.ui.view.ContactListViewAttrs;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

import java.util.List;


/**
 * contact page
 */
public class ContactFragment extends BaseFragment {

    private ContactViewModel viewModel;
    private ContactFragmentBinding viewBinding;
    private ContactUIConfig contactConfig;
    private Observer<FetchResult<List<ContactFriendBean>>> contactObserver;
    private IContactCallback contactCallback;

    public void setContactConfig(ContactUIConfig config) {
        contactConfig = config;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewBinding = ContactFragmentBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        if (ContactKitClient.getContactUIConfig() != null && contactConfig == null) {
            contactConfig = ContactKitClient.getContactUIConfig();
        }
        if (contactConfig == null){
            contactConfig = new ContactUIConfig();
        }
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //data observer
        contactObserver = contactBeansResult -> {
            if (contactBeansResult.getLoadStatus() == LoadStatus.Success) {
                viewBinding.contactListview.onFriendDataSourceChanged(contactBeansResult.getData());
            } else if (contactBeansResult.getLoadStatus() == LoadStatus.Finish) {
                if (contactBeansResult.getType() == FetchResult.FetchType.Add && !contactBeansResult.getData().isEmpty()) {
                    viewBinding.contactListview.addFriendData(contactBeansResult.getData());
                } else if (contactBeansResult.getType() == FetchResult.FetchType.Remove && !contactBeansResult.getData().isEmpty()) {
                    viewBinding.contactListview.removeFriendData(contactBeansResult.getData());
                } else if (contactBeansResult.getType() == FetchResult.FetchType.Update && !contactBeansResult.getData().isEmpty()) {
                    viewBinding.contactListview.updateFriendData(contactBeansResult.getData());
                }
            }
        };

        initView();
        viewModel.getContactLiveData().observeForever(contactObserver);
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
                actions.addContactListener(contactConfig.itemClickListeners.keyAt(index), contactConfig.itemClickListeners.valueAt(index));
            }
        }

        if (contactConfig.itemSelectorListeners.size() > 0) {
            for (int index = 0; index < contactConfig.itemSelectorListeners.size(); index++) {
                actions.addSelectorListener(contactConfig.itemSelectorListeners.keyAt(index), contactConfig.itemSelectorListeners.valueAt(index));
            }
        }
        viewBinding.contactListview.setContactAction(actions);

    }

    private void loadConfig(){
        if(contactConfig != null){
            if(contactConfig.viewHolderFactory != null){
                viewBinding.contactListview.setViewHolderFactory(contactConfig.viewHolderFactory);
            }
            if (contactConfig.contactAttrs != null) {
                viewBinding.contactListview.setViewConfig(contactConfig.contactAttrs);
            }
        }
    }

    private void loadDefaultContactAction(ContactActions actions) {
        actions.addContactListener(IViewTypeConstant.CONTACT_FRIEND, (position, data) -> {
            Intent intent = new Intent();
            FriendInfo friendInfo = ((ContactFriendBean) data).data;
            intent.putExtra(RouterConstant.KEY_ACCOUNT_ID_KEY, friendInfo.getAccount());
            intent.setClass(getContext(), UserInfoActivity.class);
            startActivity(intent);
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
                List<ContactEntranceBean> entranceBeanList = viewModel.getContactEntranceList(this.requireContext());
                for (ContactEntranceBean bean : entranceBeanList) {
                    viewBinding.contactListview.addContactData(bean);
                }
                viewModel.getContactEntranceLiveData().observe(getViewLifecycleOwner(), contactEntranceBean -> {
                    if (contactCallback != null) {
                        contactCallback.updateUnreadCount(contactEntranceBean.number);
                    }
                    viewBinding.contactListview.updateContactData(contactEntranceBean);
                });

                actions.addContactListener(IViewTypeConstant.CONTACT_ACTION_ENTER, (position, data) -> {
                    if (!TextUtils.isEmpty(data.router)) {
                        XKitRouter.withKey(data.router).withContext(ContactFragment.this.getContext()).navigate();
                    }
                });

            } else {
                viewBinding.contactListview.addContactData(contactConfig.headerData);
            }
        }
    }

    private void loadTitle() {
        if (contactConfig.showTitleBar) {
            viewBinding.contactTitleLayout.setVisibility(View.VISIBLE);
            if (contactConfig.titleColor != ContactUIConfig.INT_DEFAULT_NULL) {
                viewBinding.contactTitleLayout.setTitleColor(contactConfig.titleColor);
            }
            if (contactConfig.title != null) {
                viewBinding.contactTitleLayout.setTitle(contactConfig.title);
            } else {
                viewBinding.contactTitleLayout.setTitle(getResources().getString(R.string.contact_title));
            }

        } else {
            viewBinding.contactTitleLayout.setVisibility(View.GONE);
        }
        if (contactConfig.showTitleBarRight2Icon) {
            viewBinding.contactTitleLayout.showRight2ImageView(true);
            if (contactConfig.titleBarRight2Res != ContactUIConfig.INT_DEFAULT_NULL) {
                viewBinding.contactTitleLayout.setRight2ImageRes(contactConfig.titleBarRight2Res);
            }

            viewBinding.contactTitleLayout.setRight2ImageClick(v -> {
                if (contactConfig.titleBarRight2Click != null) {
                    contactConfig.titleBarRight2Click.onClick(v);
                } else {
                    XKitRouter.withKey(RouterConstant.PATH_GLOBAL_SEARCH_PAGE).withContext(getContext()).navigate();
                }
            });

        } else {
            viewBinding.contactTitleLayout.showRight2ImageView(false);
        }

        if (contactConfig.showTitleBarRightIcon) {
            viewBinding.contactTitleLayout.showRightImageView(true);
            if (contactConfig.titleBarRight2Res != ContactUIConfig.INT_DEFAULT_NULL) {
                viewBinding.contactTitleLayout.setRightImageRes(contactConfig.titleBarRight2Res);
            }
            if (contactConfig.titleBarRight2Click != null) {
                viewBinding.contactTitleLayout.setRightImageClick(contactConfig.titleBarRight2Click);
            } else {
                viewBinding.contactTitleLayout.setRightImageClick(v -> {
                    Intent intent = new Intent(getContext(), AddFriendActivity.class);
                    startActivity(intent);
                });

            }

        } else {
            viewBinding.contactTitleLayout.showRightImageView(false);
        }
    }

    @Override
    public void onDestroy() {
        viewModel.getContactLiveData().removeObserver(contactObserver);
        super.onDestroy();

    }

    /**
     * Builder
     */
    public static class Builder extends FragmentBuilder {

        ContactUIConfig contactConfig;

        public Builder() {
        }

        @Override
        public ContactFragment build() {
            ContactFragment fragment = new ContactFragment();
            if (contactConfig != null) {
                fragment.setContactConfig(contactConfig);
            }
            return fragment;
        }

        public void setContactConfig(ContactUIConfig contactConfig) {
            this.contactConfig = contactConfig;
        }

        public Builder setShowTitleBar(boolean show) {
            if (contactConfig == null){
                contactConfig = new ContactUIConfig();
            }
            contactConfig.showTitleBar = show;
            return this;
        }

        public Builder setTitle(String title) {
            if (contactConfig == null){
                contactConfig = new ContactUIConfig();
            }
            contactConfig.title = title;
            return this;
        }

        public Builder setTitleColor(int color) {
            if (contactConfig == null){
                contactConfig = new ContactUIConfig();
            }
            contactConfig.titleColor = color;
            return this;
        }

        public Builder setShowTitleBarRight2Icon(boolean show) {
            if (contactConfig == null){
                contactConfig = new ContactUIConfig();
            }
            contactConfig.showTitleBarRight2Icon = show;
            return this;
        }

        public Builder setSearchIcon(int res) {
            if (contactConfig == null){
                contactConfig = new ContactUIConfig();
            }
            contactConfig.titleBarRight2Res = res;
            return this;
        }

        public Builder setSearchClickListener(View.OnClickListener listener) {
            if (contactConfig == null){
                contactConfig = new ContactUIConfig();
            }
            contactConfig.titleBarRight2Click = listener;
            return this;
        }

        public Builder setShowMoreIcon(boolean show) {
            if (contactConfig == null){
                contactConfig = new ContactUIConfig();
            }
            contactConfig.showTitleBarRightIcon = show;
            return this;
        }

        public Builder setMoreIcon(int res) {
            if (contactConfig == null){
                contactConfig = new ContactUIConfig();
            }
            contactConfig.titleBarRightRes = res;
            return this;
        }

        public Builder setMoreClickListener(View.OnClickListener listener) {
            if (contactConfig == null){
                contactConfig = new ContactUIConfig();
            }
            contactConfig.titleBarRightClick = listener;
            return this;
        }

        public Builder setMoreMenu(List<MenuBean> menuList) {
            if (contactConfig == null){
                contactConfig = new ContactUIConfig();
            }
            contactConfig.titleBarRightMenu = menuList;
            return this;
        }

        public Builder showHeader(boolean show) {
            if (contactConfig == null){
                contactConfig = new ContactUIConfig();
            }
            contactConfig.showHeader = show;
            return this;
        }

        public Builder setContactListViewAttar(ContactListViewAttrs attrs) {
            if (contactConfig == null){
                contactConfig = new ContactUIConfig();
            }
            contactConfig.contactAttrs = attrs;
            return this;
        }

        public Builder setHeaderData(List<ContactEntranceBean> data) {
            if (contactConfig == null){
                contactConfig = new ContactUIConfig();
            }
            contactConfig.headerData = data;
            return this;
        }

        public Builder setContactClickListener(int type, IContactClickListener contactListener) {
            if (contactConfig == null){
                contactConfig = new ContactUIConfig();
            }
            contactConfig.itemClickListeners.put(type, contactListener);
            return this;
        }

        public Builder setContactSelection(int type, IContactSelectorListener contactListener) {
            if (contactConfig == null){
                contactConfig = new ContactUIConfig();
            }
            contactConfig.itemSelectorListeners.put(type, contactListener);
            return this;
        }
    }
}
