/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.verify;

import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.activity.BaseListActivity;
import com.netease.yunxin.kit.contactkit.ui.model.BaseContactBean;
import com.netease.yunxin.kit.contactkit.ui.model.ContactVerifyInfoBean;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;
import com.netease.yunxin.kit.contactkit.ui.view.ContactViewHolderFactory;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.VerifyInfoViewHolder;
import com.netease.yunxin.kit.corekit.im.model.SystemMessageInfoStatus;
import com.netease.yunxin.kit.corekit.im.model.SystemMessageInfoType;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;

import java.util.List;

public class VerifyListActivity extends BaseListActivity {

    private VerifyViewModel viewModel;

    @Override
    protected void initView() {
        binding.title.setTitle(R.string.verify_msg)
                .setActionText(R.string.clear_all)
                .setActionListener(v -> {
                    viewModel.clearNotify();
                });
        binding.contactListView.setViewHolderFactory(new ContactViewHolderFactory() {
            @Override
            protected BaseContactViewHolder getCustomViewHolder(ViewGroup view, int viewType) {
                if (viewType == IViewTypeConstant.CONTACT_VERIFY_INFO) {
                    VerifyInfoViewHolder viewHolder = new VerifyInfoViewHolder(view);
                    viewHolder.setVerifyListener(new VerifyInfoViewHolder.VerifyListener() {
                        @Override
                        public void onAccept(ContactVerifyInfoBean bean) {
                            viewModel.agree(bean, new FetchCallback<Void>() {
                                @Override
                                public void onSuccess(@Nullable Void param) {
                                    viewModel.setVerifyStatus(bean.data.getId(), SystemMessageInfoStatus.Passed);
                                    bean.data.setInfoStatus(SystemMessageInfoStatus.Passed);
                                    binding.contactListView.updateContactData(bean);
                                }

                                @Override
                                public void onFailed(int code) {
                                    toastResult(true, bean.data.getInfoType());

                                }

                                @Override
                                public void onException(@Nullable Throwable exception) {
                                    toastResult(true, bean.data.getInfoType());
                                }
                            });
                        }

                        @Override
                        public void onReject(ContactVerifyInfoBean bean) {
                            viewModel.disagree(bean, new FetchCallback<Void>() {
                                @Override
                                public void onSuccess(@Nullable Void param) {
                                    viewModel.setVerifyStatus(bean.data.getId(), SystemMessageInfoStatus.Declined);
                                    bean.data.setInfoStatus(SystemMessageInfoStatus.Declined);
                                    binding.contactListView.updateContactData(bean);
                                }

                                @Override
                                public void onFailed(int code) {
                                    toastResult(false, bean.data.getInfoType());

                                }

                                @Override
                                public void onException(@Nullable Throwable exception) {
                                    toastResult(false, bean.data.getInfoType());

                                }
                            });
                        }
                    });
                    return viewHolder;
                }
                return null;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.resetUnreadCount();
    }

    @Override
    protected void initData() {
        viewModel = new ViewModelProvider(this).get(VerifyViewModel.class);
        viewModel.getFetchResult().observe(this, result -> {
            if (result.getLoadStatus() == LoadStatus.Success) {
                binding.contactListView.addContactData(result.getData());
            } else if (result.getLoadStatus() == LoadStatus.Finish) {
                if (result.getType() == FetchResult.FetchType.Remove) {
                    binding.contactListView.removeContactData(result.getData());
                } else if (result.getType() == FetchResult.FetchType.Add) {
                    addNotifyData(result.getData());
                    viewModel.resetUnreadCount();
                }
            }
        });

        viewModel.fetchVerifyList(false);
    }

    private void toastResult(boolean agree, SystemMessageInfoType type) {
        String content = null;
        if (type == SystemMessageInfoType.AddFriend) {
            content = agree ? getResources().getString(R.string.agree_add_friend_fail) : getResources().getString(R.string.disagree_add_friend_fail);
        } else if ((type == SystemMessageInfoType.ApplyJoinTeam)) {
            content = agree ? getResources().getString(R.string.agree_apply_join_team_fail) : getResources().getString(R.string.disagree_apply_join_team_fail);
        } else if ((type == SystemMessageInfoType.TeamInvite)) {
            content = agree ? getResources().getString(R.string.agree_invite_team_fail) : getResources().getString(R.string.disagree_invite_team_fail);
        }
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    private void addNotifyData(List<ContactVerifyInfoBean> addList) {
        if (addList == null || addList.size() < 1) {
            return;
        }
        List<BaseContactBean> currentData = binding.contactListView.getAdapter().getDataList();
        if (currentData != null) {
            for (int index = addList.size() - 1; index >= 0; index--) {
                ContactVerifyInfoBean addBean = addList.get(index);
                if (addBean.data.getInfoType() == SystemMessageInfoType.AddFriend) {
                    currentData = binding.contactListView.getAdapter().getDataList();
                    for (int i = 0; i < currentData.size(); i++) {
                        if (currentData.get(i) instanceof ContactVerifyInfoBean) {
                            ContactVerifyInfoBean currentBean = (ContactVerifyInfoBean) currentData.get(i);
                            if (currentBean.data.getInfoType() == SystemMessageInfoType.AddFriend &&
                                    TextUtils.equals(currentBean.data.getFromAccount(), addBean.data.getFromAccount())) {
                                binding.contactListView.removeContactData(currentData.get(i));
                                break;
                            }
                        }
                    }
                }
            }
        }
        binding.contactListView.addContactData(addList);
    }
}
