// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.userinfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.dialog.BottomConfirmDialog;
import com.netease.yunxin.kit.common.ui.dialog.ConfirmListener;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.model.ContactUserInfoBean;
import com.netease.yunxin.kit.contactkit.ui.view.ContactInfoView;
import com.netease.yunxin.kit.corekit.im.model.FriendVerifyType;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.Objects;

public abstract class BaseUserInfoActivity extends BaseActivity {
  protected UserInfoViewModel viewModel;
  protected ContactUserInfoBean userInfoData;
  protected String accId;

  private View rootView;
  protected BackTitleBar titleBar;
  protected ContactInfoView contactInfoView;

  protected ActivityResultLauncher<Intent> commentLauncher;

  protected abstract View initViewAndGetRootView(Bundle savedInstanceState);

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    rootView = initViewAndGetRootView(savedInstanceState);
    checkViews();
    setContentView(rootView);
    titleBar.setOnBackIconClickListener(v -> onBackPressed());
    initView();
    initData();
    registerResult();
  }

  protected void checkViews() {
    Objects.requireNonNull(rootView);
    Objects.requireNonNull(titleBar);
    Objects.requireNonNull(contactInfoView);
  }

  private void registerResult() {
    commentLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() == BaseCommentActivity.RESULT_OK
                  && result.getData() != null) {
                String comment =
                    result.getData().getStringExtra(BaseCommentActivity.REQUEST_COMMENT_NAME_KEY);
                userInfoData.friendInfo.setAlias(comment);
                contactInfoView.setData(userInfoData);
                viewModel.updateAlias(userInfoData.data.getAccount(), comment);
              }
            });
  }

  private void initView() {
    contactInfoView.setUserCallback(
        new ContactInfoView.IUserCallback() {
          @Override
          public void goChat() {
            if (userInfoData == null || userInfoData.data == null) {
              if (!NetworkUtils.isConnected()) {
                Toast.makeText(
                        BaseUserInfoActivity.this,
                        R.string.contact_network_error_tip,
                        Toast.LENGTH_SHORT)
                    .show();
                return;
              }
              return;
            }
            BaseUserInfoActivity.this.goChat();
          }

          @Override
          public void addFriend() {
            if (userInfoData == null || userInfoData.data == null) {
              return;
            }
            if (!NetworkUtils.isConnected()) {
              Toast.makeText(
                      BaseUserInfoActivity.this,
                      R.string.contact_network_error_tip,
                      Toast.LENGTH_SHORT)
                  .show();
              return;
            }
            addNewFriend();
          }

          @Override
          public void openMessageNotify(boolean open) {
            //todo 打开或者关闭消息通知
          }

          @Override
          public void addBlackList(boolean add) {
            if (!NetworkUtils.isConnected()) {
              Toast.makeText(
                      BaseUserInfoActivity.this,
                      R.string.contact_network_error_tip,
                      Toast.LENGTH_SHORT)
                  .show();
              return;
            }
            if (userInfoData == null || userInfoData.data == null) {
              return;
            }
            if (add) {
              viewModel.addBlack(userInfoData.data.getAccount());
            } else {
              viewModel.removeBlack(userInfoData.data.getAccount());
            }
          }
        });

    contactInfoView.setCommentClickListener(
        v -> {
          if (userInfoData == null || userInfoData.data == null) {
            return;
          }
          Intent intent = new Intent();
          intent.setClass(this, getCommentActivity());
          intent.putExtra(
              BaseCommentActivity.REQUEST_COMMENT_NAME_KEY, userInfoData.friendInfo.getAlias());
          commentLauncher.launch(intent);
        });

    contactInfoView.setDeleteClickListener(
        v -> {
          if (userInfoData == null || userInfoData.data == null) {
            return;
          }
          showDeleteConfirmDialog();
        });
  }

  protected void goChat() {
    XKitRouter.withKey(RouterConstant.PATH_CHAT_P2P_PAGE)
        .withParam(RouterConstant.CHAT_ID_KRY, userInfoData.data.getAccount())
        .withContext(BaseUserInfoActivity.this)
        .navigate();
    finish();
  }

  protected Class<? extends Activity> getCommentActivity() {
    return null;
  }

  protected void showDeleteConfirmDialog() {
    BottomConfirmDialog bottomConfirmDialog = new BottomConfirmDialog();
    bottomConfirmDialog
        .setTitleStr(
            String.format(getString(R.string.delete_contact_account), userInfoData.getName()))
        .setPositiveStr(getString(R.string.delete_friend))
        .setNegativeStr(getString(R.string.cancel))
        .setConfirmListener(
            new ConfirmListener() {
              @Override
              public void onNegative() {
                //do nothing
              }

              @Override
              public void onPositive() {
                if (!NetworkUtils.isConnected()) {
                  Toast.makeText(
                          BaseUserInfoActivity.this,
                          R.string.contact_network_error_tip,
                          Toast.LENGTH_SHORT)
                      .show();
                  return;
                }
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
    viewModel.init(accId);
    viewModel
        .getFriendFetchResult()
        .observe(
            this,
            mapFetchResult -> {
              if (mapFetchResult.getLoadStatus() == LoadStatus.Success) {
                userInfoData = mapFetchResult.getData();
                contactInfoView.setData(userInfoData);
              } else {
                if (!NetworkUtils.isConnected()) {
                  Toast.makeText(this, R.string.contact_network_error_tip, Toast.LENGTH_SHORT)
                      .show();
                }
              }
            });
    viewModel
        .getUserInfoLiveData()
        .observe(
            this,
            userInfoResult -> {
              if (userInfoResult.getLoadStatus() == LoadStatus.Finish
                  && userInfoResult.getData() != null) {
                for (UserInfo userInfo : userInfoResult.getData()) {
                  if (TextUtils.equals(userInfo.getAccount(), accId)) {
                    if (userInfoData != null) {
                      userInfoData.data = userInfo;
                      contactInfoView.setData(userInfoData);
                    }
                  }
                }
              } else {
                if (!NetworkUtils.isConnected()) {
                  Toast.makeText(this, R.string.contact_network_error_tip, Toast.LENGTH_SHORT)
                      .show();
                }
              }
            });
    viewModel
        .getFriendChangeLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Finish) {
                viewModel.fetchData(accId);
              }
            });
    viewModel.fetchData(accId);
  }

  private void addNewFriend() {
    viewModel.addFriend(
        userInfoData.data.getAccount(),
        FriendVerifyType.AgreeAdd,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            viewModel.fetchData(userInfoData.data.getAccount());
            Toast.makeText(
                    BaseUserInfoActivity.this,
                    getResources().getString(R.string.add_friend_operate_success),
                    Toast.LENGTH_SHORT)
                .show();
          }

          @Override
          public void onFailed(int code) {
            String tips = getResources().getString(R.string.add_friend_operate_fail);
            Toast.makeText(
                    BaseUserInfoActivity.this,
                    String.format(tips, String.valueOf(code)),
                    Toast.LENGTH_SHORT)
                .show();
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            String tips = getResources().getString(R.string.add_friend_operate_fail);
            Toast.makeText(
                    BaseUserInfoActivity.this,
                    String.format(tips, exception.getMessage()),
                    Toast.LENGTH_SHORT)
                .show();
          }
        });
  }
}
