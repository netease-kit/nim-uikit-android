// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main.mine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.coexist.sdk.v2.user.V2NIMUser;
import com.netease.yunxin.app.im.AppSkinConfig;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.about.AboutActivity;
import com.netease.yunxin.app.im.databinding.FragmentMineBinding;
import com.netease.yunxin.app.im.main.mine.setting.SettingActivity;
import com.netease.yunxin.app.im.utils.Constant;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.coexist.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.Collections;
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

    binding.userInfoClick.setOnClickListener(v -> MineInfoActivity.launch(getContext(), launcher));
    binding.collectLl.setOnClickListener(
        v -> {
          String path =
              AppSkinConfig.getInstance().getAppSkinStyle() == AppSkinConfig.AppSkin.commonSkin
                  ? RouterConstant.PATH_FUN_COLLECTION_PAGE
                  : RouterConstant.PATH_COLLECTION_PAGE;
          XKitRouter.withKey(path).withContext(this.requireContext()).navigate();
        });

    binding.settingLl.setOnClickListener(
        v -> startActivity(new Intent(getContext(), SettingActivity.class)));
    binding.tvAccount.setText(getString(R.string.tab_mine_account, IMKitClient.account()));
  }

  private void refreshUserInfo(String account) {
    if (AppSkinConfig.getInstance().getAppSkinStyle() == AppSkinConfig.AppSkin.commonSkin) {
      int cornerRadius = SizeUtils.dp2px(4);
      binding.cavIcon.setCornerRadius(cornerRadius);
    }
    ContactRepo.getUserInfo(
        Collections.singletonList(account),
        new FetchCallback<List<V2NIMUser>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ToastX.showShortToast(R.string.user_fail);
          }

          @Override
          public void onSuccess(@Nullable List<V2NIMUser> param) {
            if (param != null && !param.isEmpty()) {
              updateUI(param.get(0));
            }
          }
        });
  }

  private void updateUI(V2NIMUser userInfo) {
    String name =
        TextUtils.isEmpty(userInfo.getName()) ? userInfo.getAccountId() : userInfo.getName();
    binding.cavIcon.setData(
        userInfo.getAvatar(), name, AvatarColor.avatarColor(IMKitClient.account()));
    binding.tvName.setText(name);
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
