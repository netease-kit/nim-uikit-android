/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.conversationkit.ui.page;

import android.content.Context;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.netease.nimlib.sdk.friend.model.MuteListChangedNotify;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import com.netease.yunxin.kit.common.ui.dialog.ListAlertDialog;
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.ui.widgets.ContentListPopView;
import com.netease.yunxin.kit.common.utils.ScreenUtil;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.conversationkit.model.ConversationInfo;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant;
import com.netease.yunxin.kit.conversationkit.ui.page.interfaces.IConversationCallback;
import com.netease.yunxin.kit.conversationkit.ui.page.interfaces.ILoadListener;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.common.ui.viewholder.ViewHolderClickListener;
import com.netease.yunxin.kit.conversationkit.ui.databinding.ConversationFragmentBinding;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.ui.common.XLog;
import com.netease.yunxin.kit.conversationkit.ui.page.viewmodel.ConversationViewModel;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * conversation list fragment
 * show your recent conversation
 */
public class ConversationFragment extends BaseFragment implements ILoadListener {

    private final String TAG = "ConversationFragment";
    private ConversationFragmentBinding viewBinding;
    private ConversationViewModel viewModel;
    private IConversationCallback conversationCallback;

    private Observer<FetchResult<List<ConversationBean>>> changeObserver;
    private Observer<FetchResult<ConversationBean>> stickObserver;
    private Observer<FetchResult<List<UserInfo>>> userInfoObserver;
    private Observer<FetchResult<List<FriendInfo>>> friendInfoObserver;
    private Observer<FetchResult<List<Team>>> teamInfoObserver;
    private Observer<FetchResult<MuteListChangedNotify>> muteObserver;
    private Observer<FetchResult<String>> addRemoveStickObserver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewBinding = ConversationFragmentBinding.inflate(inflater, container, false);
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ConversationViewModel.class);
        viewModel.setComparator(conversationComparator);
        viewModel.getQueryLiveData().observe(this.getViewLifecycleOwner(), result -> {
            if (result.getLoadStatus() == LoadStatus.Success) {
                viewBinding.conversationView.setData(result.getData());
            } else if (result.getLoadStatus() == LoadStatus.Finish) {
                viewBinding.conversationView.addData(result.getData());
            }
            doCallback();
        });

        changeObserver = result -> {
            if (result.getLoadStatus() == LoadStatus.Success) {
                XLog.d(TAG, "ChangeLiveData", "Success");
                viewBinding.conversationView.update(result.getData());
            } else if (result.getLoadStatus() == LoadStatus.Finish && result.getType() == FetchResult.FetchType.Remove) {
                XLog.d(TAG, "DeleteLiveData", "Success");
                if (result.getData() == null || result.getData().size() < 1){
                    viewBinding.conversationView.removeAll();
                }else {
                    viewBinding.conversationView.remove(result.getData());
                }
            }
            doCallback();
        };

        stickObserver = result -> {
            if (result.getLoadStatus() == LoadStatus.Success) {
                XLog.d(TAG, "StickLiveData", "Success");
                viewBinding.conversationView.update(result.getData());
            }
            doCallback();
        };

        userInfoObserver = result -> {
            if (result.getLoadStatus() == LoadStatus.Success) {
                XLog.d(TAG, "UserInfoLiveData", "Success");
                viewBinding.conversationView.updateUserInfo(result.getData());
            }
        };

        friendInfoObserver = result -> {
            if (result.getLoadStatus() == LoadStatus.Success) {
                XLog.d(TAG, "FriendInfoLiveData", "Success");
                viewBinding.conversationView.updateFriendInfo(result.getData());
            }
        };

        teamInfoObserver = result -> {
            if (result.getLoadStatus() == LoadStatus.Success) {
                XLog.d(TAG, "TeamInfoLiveData", "Success");
                viewBinding.conversationView.updateTeamInfo(result.getData());
            }
        };

        muteObserver = result -> {
            if (result.getLoadStatus() == LoadStatus.Success) {
                XLog.d(TAG, "MuteInfoLiveData", "Success");
                viewBinding.conversationView.updateMuteInfo(result.getData());
            }
        };

        addRemoveStickObserver = result -> {
            if (result.getLoadStatus() == LoadStatus.Finish) {
                if (result.getType() == FetchResult.FetchType.Add) {
                    XLog.d(TAG, "AddStickLiveData", "Success");
                    viewBinding.conversationView.addStickTop(result.getData());
                } else if (result.getType() == FetchResult.FetchType.Remove) {
                    XLog.d(TAG, "RemoveStickLiveData", "Success");
                    viewBinding.conversationView.removeStickTop(result.getData());
                }
            }
        };
        registerObserver();
        viewBinding.conversationView.setViewHolderFactory(new ConversationViewHolderFactory());
        viewBinding.conversationView.setComparator(conversationComparator);
        viewBinding.conversationView.setItemClickListener(new ViewHolderClickListener() {
            @Override
            public void onClick(BaseBean data, int position) {
                XKitRouter.withKey(data.router).withParam(data.paramKey, data.param).withContext(ConversationFragment.this.getContext()).navigate();
            }

            @Override
            public boolean onLongClick(BaseBean data, int position) {
                showStickDialog(data);
                return true;
            }
        });
        NetworkUtils.registerStateListener(networkStateListener);

        viewBinding.conversationTitleBar.setMoreImageClick(v -> {
            Context context = getContext();
            ContentListPopView contentListPopView = new ContentListPopView.Builder(context)
                    .addItem(PopItemFactory.getAddFriendItem(context))
                    .addItem(PopItemFactory.getCreateGroupTeamItem(context))
                    .addItem(PopItemFactory.getCreateAdvancedTeamItem(context))
                    .build();
            contentListPopView.showAsDropDown(v, ScreenUtil.dip2px(-105), 0);

        });

        viewBinding.conversationTitleBar.setMiddleImageClick(v -> {
            XKitRouter.withKey(RouterConstant.PATH_GLOBAL_SEARCH).withContext(getContext()).navigate();
        });
        viewModel.fetchConversation();

    }

    private void registerObserver() {
        viewModel.getChangeLiveData().observeForever(changeObserver);
        viewModel.getStickLiveData().observeForever(stickObserver);
        viewModel.getUserInfoLiveData().observeForever(userInfoObserver);
        viewModel.getFriendInfoLiveData().observeForever(friendInfoObserver);
        viewModel.getTeamInfoLiveData().observeForever(teamInfoObserver);
        viewModel.getMuteInfoLiveData().observeForever(muteObserver);
        viewModel.getAddRemoveStickLiveData().observeForever(addRemoveStickObserver);
    }

    private void unregisterObserver(){
        viewModel.getChangeLiveData().removeObserver(changeObserver);
        viewModel.getStickLiveData().removeObserver(stickObserver);
        viewModel.getUserInfoLiveData().removeObserver(userInfoObserver);
        viewModel.getFriendInfoLiveData().removeObserver(friendInfoObserver);
        viewModel.getTeamInfoLiveData().removeObserver(teamInfoObserver);
        viewModel.getMuteInfoLiveData().removeObserver(muteObserver);
        viewModel.getAddRemoveStickLiveData().removeObserver(addRemoveStickObserver);
    }

    public void setConversationCallback(IConversationCallback callback) {
        this.conversationCallback = callback;
        doCallback();
    }

    private void showStickDialog(BaseBean data) {
        if (data instanceof ConversationBean) {
            ConversationBean dataBean = (ConversationBean) data;
            ListAlertDialog alertDialog = new ListAlertDialog();
            alertDialog.setContent(generateDialogContent(dataBean.infoData.isStickTop()));
            alertDialog.setTitleVisibility(View.GONE);
            alertDialog.setDialogWidth(getResources().getDimension(R.dimen.dimen_150_dp));
            alertDialog.setItemClickListener(action -> {
                if (TextUtils.equals(action, ConversationConstant.Action.ACTION_DELETE)) {
                    viewModel.deleteConversation(dataBean);
                } else if (TextUtils.equals(action, ConversationConstant.Action.ACTION_STICK)) {
                    if (dataBean.infoData.isStickTop()) {
                        viewModel.removeStick((ConversationBean) data);
                    } else {
                        viewModel.addStickTop((ConversationBean) data);
                    }
                }
                alertDialog.dismiss();
            });
            alertDialog.show(getParentFragmentManager());
        }
    }

    private final Comparator<ConversationInfo> conversationComparator = (bean1, bean2) -> {
        int result = -1;
        if (bean1 == null) {
            result = 1;
        } else if (bean2 == null) {
            result = -1;
        } else if (bean1.isStickTop() == bean2.isStickTop()) {
            long time = bean1.getTime() - bean2.getTime();
            result = time == 0L ? 0 : (time > 0 ? -1 : 1);

        } else {
            result = bean1.isStickTop() ? -1 : 1;
        }
        XLog.d(TAG, "conversationComparator", "result:" + result);
        return result;
    };

    private List<ActionItem> generateDialogContent(boolean isStick) {
        List<ActionItem> contentList = new ArrayList<>();
        ActionItem stick = new ActionItem(ConversationConstant.Action.ACTION_STICK, 0, (isStick ? R.string.cancel_stick_title : R.string.stick_title));
        ActionItem delete = new ActionItem(ConversationConstant.Action.ACTION_DELETE, 0, R.string.delete_title);
        contentList.add(stick);
        contentList.add(delete);
        return contentList;
    }

    private void doCallback() {
        if (conversationCallback != null && viewModel != null) {
            conversationCallback.updateUnreadCount(viewModel.getUnreadCount());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        NetworkUtils.unregisterStateListener(networkStateListener);
        unregisterObserver();
    }

    private final NetworkUtils.NetworkStateListener networkStateListener = new NetworkUtils.NetworkStateListener() {
        @Override
        public void onAvailable(NetworkInfo network) {
            if (viewBinding == null) {
                return;
            }
            viewBinding.conversationNetworkErrorTv.setVisibility(View.GONE);
        }

        @Override
        public void onLost(NetworkInfo network) {
            if (viewBinding == null) {
                return;
            }
            viewBinding.conversationNetworkErrorTv.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public boolean hasMore() {
        return viewModel.hasMore();
    }

    @Override
    public void loadMore(Object last) {
        if (last instanceof ConversationBean) {
            viewModel.loadMore((ConversationBean) last);
        }
    }
}
