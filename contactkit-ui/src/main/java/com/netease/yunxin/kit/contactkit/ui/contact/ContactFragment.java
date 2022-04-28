/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.contact;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.ContactConfig;
import com.netease.yunxin.kit.contactkit.ui.FragmentBuilder;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.addfriend.AddFriendActivity;
import com.netease.yunxin.kit.contactkit.ui.blacklist.BlackListActivity;
import com.netease.yunxin.kit.contactkit.ui.databinding.ContactFragmentBinding;
import com.netease.yunxin.kit.contactkit.ui.interfaces.ContactActions;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactCallback;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactClickListener;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactSelectorListener;
import com.netease.yunxin.kit.contactkit.ui.model.ContactEntranceBean;
import com.netease.yunxin.kit.contactkit.ui.model.ContactFriendBean;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.contactkit.ui.model.MenuBean;
import com.netease.yunxin.kit.contactkit.ui.team.TeamListActivity;
import com.netease.yunxin.kit.contactkit.ui.userinfo.UserInfoActivity;
import com.netease.yunxin.kit.contactkit.ui.verify.VerifyListActivity;
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
    private ContactConfig contactConfig = new ContactConfig();
    private Observer<FetchResult<List<ContactFriendBean>>> contactObserver;
    private IContactCallback contactCallback;

    public void setContactConfig(ContactConfig config) {
        if (config != null) {
            contactConfig = config;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewBinding = ContactFragmentBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);
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

        viewModel.getContactLiveData().observeForever(contactObserver);
        viewModel.fetchContactList();

        loadTitle();

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
                actions.addContactListener(index, contactConfig.itemClickListeners.get(index));
            }
        }

        if (contactConfig.itemSelectorListeners.size() > 0) {
            for (int index = 0; index < contactConfig.itemSelectorListeners.size(); index++) {
                actions.addSelectorListener(index, contactConfig.itemSelectorListeners.get(index));
            }
        }
        viewBinding.contactListview.setContactAction(actions);

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

    public int getUnreadCount(){
        return viewModel.getVerifyCount();
    }

    public void setContactCallback(IContactCallback contactCallback) {
        this.contactCallback = contactCallback;
        if (viewModel != null && contactCallback != null){
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
                    if (contactCallback != null){
                        contactCallback.updateUnreadCount(contactEntranceBean.number);
                    }
                    viewBinding.contactListview.updateContactData(contactEntranceBean);
                });

                actions.addContactListener(IViewTypeConstant.CONTACT_ACTION_ENTER, (position, data) -> {
                    Intent intent = new Intent();
                    switch (data.router) {
                        case ContactEntranceBean.EntranceRouter.BLACK_LIST:
                            intent.setClass(getContext(), BlackListActivity.class);
                            break;
                        case ContactEntranceBean.EntranceRouter.VERIFY_LIST:
                            intent.setClass(getContext(), VerifyListActivity.class);
                            break;
                        case ContactEntranceBean.EntranceRouter.TEAM_LIST:
                            intent.setClass(getContext(), TeamListActivity.class);
                            break;
                    }
                    startActivity(intent);
                });

            } else {
                viewBinding.contactListview.addContactData(contactConfig.headerData);
            }
        }
    }

    private void loadTitle() {
        if (contactConfig.showTitleBar) {
            viewBinding.contactTitleLayout.setVisibility(View.VISIBLE);
            if (contactConfig.titleColor != -1) {
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
        //search icon
        if (contactConfig.showTitleSearchIcon) {
            viewBinding.contactTitleLayout.showMiddleImageView(true);
            if (contactConfig.titleSearchIcon != null) {
                viewBinding.contactTitleLayout.setMiddleImageRes(contactConfig.titleSearchIcon);
            }

            viewBinding.contactTitleLayout.setMiddleImageClick(v -> {
                if (contactConfig.titleSearchClick != null) {
                    contactConfig.titleSearchClick.onClick(v);
                }else {
                    XKitRouter.withKey(RouterConstant.PATH_GLOBAL_SEARCH).withContext(getContext()).navigate();
                }
            });

        } else {
            viewBinding.contactTitleLayout.showMiddleImageView(false);
        }

        //more icon
        if (contactConfig.showTitleMoreIcon) {
            viewBinding.contactTitleLayout.showMoreImageView(true);
            if (contactConfig.titleSearchIcon != null) {
                viewBinding.contactTitleLayout.setMoreImageRes(contactConfig.titleSearchIcon);
            }
            if (contactConfig.titleSearchClick != null) {
                viewBinding.contactTitleLayout.setMoreImageClick(contactConfig.titleSearchClick);
            } else {
                viewBinding.contactTitleLayout.setMoreImageClick(v -> {
                    Intent intent = new Intent(getContext(), AddFriendActivity.class);
                    startActivity(intent);
                });

            }

        } else {
            viewBinding.contactTitleLayout.showMoreImageView(false);
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

        ContactConfig contactConfig;

        public Builder() {
            contactConfig = new ContactConfig();
        }

        @Override
        public ContactFragment build() {
            return new ContactFragment();
        }

        @Override
        public void attachFragment(ContactFragment fragment) {
            fragment.setContactConfig(contactConfig);
        }

        public void setContactConfig(ContactConfig contactConfig) {
            this.contactConfig = contactConfig;
        }

        public Builder setShowTitleBar(boolean show) {
            contactConfig.showTitleBar = show;
            return this;
        }

        public Builder setTitle(String title) {
            contactConfig.title = title;
            return this;
        }

        public Builder setTitleColor(int color) {
            contactConfig.titleColor = color;
            return this;
        }

        public Builder setShowSearchIcon(boolean show) {
            contactConfig.showTitleSearchIcon = show;
            return this;
        }

        public Builder setSearchIcon(Drawable searchDrawable) {
            contactConfig.titleSearchIcon = searchDrawable;
            return this;
        }

        public Builder setSearchClickListener(View.OnClickListener listener) {
            contactConfig.titleSearchClick = listener;
            return this;
        }

        public Builder setShowMoreIcon(boolean show) {
            contactConfig.showTitleMoreIcon = show;
            return this;
        }

        public Builder setMoreIcon(Drawable searchDrawable) {
            contactConfig.titleMoreIcon = searchDrawable;
            return this;
        }

        public Builder setMoreClickListener(View.OnClickListener listener) {
            contactConfig.titleMoreClick = listener;
            return this;
        }

        public Builder setMoreMenu(List<MenuBean> menuList) {
            contactConfig.moreMenu = menuList;
            return this;
        }

        public Builder showIndexBar(boolean show) {
            contactConfig.showIndexBar = show;
            return this;
        }

        public Builder showHeader(boolean show) {
            contactConfig.showHeader = show;
            return this;
        }

        public Builder showItemTextColor(int color) {
            contactConfig.itemTextColor = color;
            return this;
        }

        public Builder setHeaderData(List<ContactEntranceBean> data) {
            contactConfig.headerData = data;
            return this;
        }

        public Builder setContactClickListener(int type, IContactClickListener contactListener) {
            contactConfig.itemClickListeners.put(type, contactListener);
            return this;
        }

        public Builder setContactSelection(int type, IContactSelectorListener contactListener) {
            contactConfig.itemSelectorListeners.put(type, contactListener);
            return this;
        }
    }
}
