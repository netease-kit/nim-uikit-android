// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.qchat.main.mine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import com.netease.yunxin.app.qchat.R;
import com.netease.yunxin.app.qchat.about.AboutActivity;
import com.netease.yunxin.app.qchat.databinding.FragmentMineBinding;
import com.netease.yunxin.app.qchat.main.mine.setting.SettingActivity;
import com.netease.yunxin.app.qchat.utils.Constant;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.repo.CommonRepo;
import java.util.ArrayList;
import java.util.List;

public class MineFragment extends BaseFragment {
  private FragmentMineBinding binding;
  private ActivityResultLauncher<Intent> launcher;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    ALog.d(Constant.PROJECT_TAG, "MineFragment:onCreateView");
    binding = FragmentMineBinding.inflate(inflater);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ALog.d(Constant.PROJECT_TAG, "MineFragment:onViewCreated");

    launcher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() == Activity.RESULT_OK) {
                refreshUserInfo(IMKitClient.account());
              }
            });

    binding.aboutLl.setOnClickListener(
        v -> {
          Intent intent = new Intent(getContext(), AboutActivity.class);
          startActivity(intent);
        });

    binding.userInfoClick.setOnClickListener(
        v -> {
          MineInfoActivity.launch(
              getContext(),
              new ActivityResultLauncher<Intent>() {
                @Override
                public void launch(Intent input, @Nullable ActivityOptionsCompat options) {
                  startActivity(input);
                }

                @Override
                public void unregister() {}

                @NonNull
                @Override
                public ActivityResultContract<Intent, ?> getContract() {
                  return null;
                }
              });
        });
    binding.collectLl.setOnClickListener(v -> ToastX.showShortToast(R.string.not_usable));

    binding.settingLl.setOnClickListener(
        v -> startActivity(new Intent(getContext(), SettingActivity.class)));
    binding.tvAccount.setText(getString(R.string.tab_mine_account, IMKitClient.account()));
  }

  private void refreshUserInfo(String account) {
    List<String> userInfoList = new ArrayList<>();
    userInfoList.add(account);
    CommonRepo.getUserInfo(
        account,
        new FetchCallback<UserInfo>() {
          @Override
          public void onSuccess(@Nullable UserInfo param) {
            if (param != null) {
              updateUI(param);
            }
          }

          @Override
          public void onFailed(int code) {
            ToastX.showShortToast(R.string.user_fail);
            updateUI(new UserInfo(account, account, ""));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ToastX.showShortToast(R.string.user_fail);
            updateUI(new UserInfo(account, account, ""));
          }
        });
  }

  private void updateUI(UserInfo userInfo) {
    binding.cavIcon.setData(
        userInfo.getAvatar(),
        TextUtils.isEmpty(userInfo.getName()) ? userInfo.getAccount() : userInfo.getName(),
        AvatarColor.avatarColor(IMKitClient.account()));
    binding.tvName.setText(userInfo.getName());
  }

  @Override
  public void onResume() {
    super.onResume();
    ALog.d(Constant.PROJECT_TAG, "MineFragment:onResume");

    String account = IMKitClient.account();
    if (TextUtils.isEmpty(account)) {
      return;
    }
    refreshUserInfo(account);
  }
}
