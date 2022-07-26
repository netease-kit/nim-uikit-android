/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.userinfo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.dialog.BottomConfirmDialog;
import com.netease.yunxin.kit.common.ui.dialog.ConfirmListener;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.UserInfoActivityLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.model.ContactUserInfoBean;
import com.netease.yunxin.kit.contactkit.ui.view.ContactInfoView;
import com.netease.yunxin.kit.corekit.im.model.FriendVerifyType;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

public class UserInfoActivity extends BaseActivity {
    private UserInfoActivityLayoutBinding binding;
    private UserInfoViewModel viewModel;
    private ContactUserInfoBean userInfoData;
    private String accId;

    private ActivityResultLauncher<Intent> commentLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = UserInfoActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.title.setOnBackIconClickListener(v -> onBackPressed());
        initView();
        initData();
        registerResult();
    }

    private void registerResult() {
        commentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == CommentActivity.RESULT_OK
                        && result.getData() != null) {
                    String comment = result.getData().getStringExtra(CommentActivity.REQUEST_COMMENT_NAME_KEY);
                    userInfoData.friendInfo.setAlias(comment);
                    binding.contactUser.setData(userInfoData);
                    viewModel.updateAlias(userInfoData.data.getAccount(), comment);
                }
            }
        });

    }

    private void initView() {
        binding.contactUser.setUserCallback(new ContactInfoView.IUserCallback() {
            @Override
            public void goChat() {
                XKitRouter.withKey(RouterConstant.PATH_CHAT_P2P_PAGE).withParam(RouterConstant.CHAT_KRY, userInfoData.data).withContext(UserInfoActivity.this).navigate();
            }

            @Override
            public void addFriend() {
                addNewFriend();
            }

            @Override
            public void openMessageNotify(boolean open) {
                //todo 打开或者关闭消息通知
            }

            @Override
            public void addBlackList(boolean add) {
                if (add) {
                    viewModel.addBlack(userInfoData.data.getAccount());
                } else {
                    viewModel.removeBlack(userInfoData.data.getAccount());
                }
            }
        });

        binding.contactUser.setCommentClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(this, CommentActivity.class);
            intent.putExtra(CommentActivity.REQUEST_COMMENT_NAME_KEY, userInfoData.friendInfo.getAlias());
            commentLauncher.launch(intent);
        });

        binding.contactUser.setDeleteClickListener(v -> {
            showDeleteConfirmDialog();
        });
    }

    private void showDeleteConfirmDialog() {
        BottomConfirmDialog bottomConfirmDialog = new BottomConfirmDialog();
        bottomConfirmDialog
                .setTitleStr(String.format(getString(R.string.delete_contact_account), userInfoData.data.getName()))
                .setPositiveStr(getString(R.string.delete_friend))
                .setNegativeStr(getString(R.string.cancel))
                .setConfirmListener(new ConfirmListener() {
                    @Override
                    public void onNegative() {
                        //do nothing
                    }

                    @Override
                    public void onPositive() {
                        viewModel.deleteFriend(userInfoData.data.getAccount());
                        finish();
                    }
                })
                .show(getSupportFragmentManager());
    }

    private void initData() {
        viewModel = new ViewModelProvider(this).get(UserInfoViewModel.class);
        accId = getIntent().getStringExtra(RouterConstant.KEY_ACCOUNT_ID_KEY);
        if (TextUtils.isEmpty(accId)) {
            finish();
        }
        viewModel.getFetchResult().observe(this, mapFetchResult -> {
            if (mapFetchResult.getLoadStatus() == LoadStatus.Success) {
                userInfoData = mapFetchResult.getData();
                binding.contactUser.setData(userInfoData);
            }
        });
        viewModel.getUserInfoLiveData().observe(this,userInfoResult ->{
            if(userInfoResult.getLoadStatus() == LoadStatus.Finish && userInfoResult.getData() != null){
                for (UserInfo userInfo: userInfoResult.getData()){
                    if (TextUtils.equals(userInfo.getAccount(),accId)){
                        userInfoData.data = userInfo;
                        binding.contactUser.setData(userInfoData);
                    }
                }
            }
        });
        viewModel.fetchData(accId);
    }

    private void addNewFriend() {
        viewModel.addFriend(userInfoData.data.getAccount(), FriendVerifyType.AgreeAdd, new FetchCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
                viewModel.fetchData(userInfoData.data.getAccount());
                Toast.makeText(UserInfoActivity.this, getResources().getString(R.string.add_friend_operate_success), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(int code) {
                String tips = getResources().getString(R.string.add_friend_operate_fail);
                Toast.makeText(UserInfoActivity.this, String.format(tips, String.valueOf(code)), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                String tips = getResources().getString(R.string.add_friend_operate_fail);
                Toast.makeText(UserInfoActivity.this, String.format(tips, exception.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
