// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main.mine;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.netease.nimlib.coexist.sdk.v2.user.V2NIMUser;
import com.netease.nimlib.coexist.sdk.v2.user.params.V2NIMUserUpdateParams;
import com.netease.yunxin.app.im.AppSkinConfig;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityMineInfoBinding;
import com.netease.yunxin.app.im.utils.Constant;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.chatkit.repo.ResourceRepo;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.photo.BasePhotoChoiceDialog;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.utils.CommonCallback;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.widgets.datepicker.CustomDatePicker;
import com.netease.yunxin.kit.common.ui.widgets.datepicker.DateFormatUtils;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.coexist.im2.extend.FetchCallback;
import com.netease.yunxin.kit.teamkit.ui.fun.dialog.FunPhotoChoiceDialog;
import com.netease.yunxin.kit.teamkit.ui.normal.dialog.ImageChoiceDialog;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MineInfoActivity extends BaseLocalActivity {
  private ActivityMineInfoBinding binding;
  private ActivityResultLauncher<Intent> launcher;
  private V2NIMUser userInfo;
  private int resultCode = RESULT_CANCELED;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMineInfoBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    Window window = getWindow();
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    window.setStatusBarColor(ContextCompat.getColor(this, R.color.color_e9eff5));

    launcher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getData() != null
                  && TextUtils.equals(
                      result.getData().getStringExtra(Constant.EDIT_TYPE), Constant.EDIT_SEXUAL)) {
                int select = result.getData().getIntExtra(Constant.SELECTED_INDEX, -1);
                if (select >= 0) {
                  V2NIMUserUpdateParams.V2NIMUserUpdateParamsBuilder builder =
                      V2NIMUserUpdateParams.V2NIMUserUpdateParamsBuilder.builder();
                  builder.withGender(select + 1);
                  updateUserInfo(builder.build());
                }

              } else if (result.getResultCode() == RESULT_OK) {
                loadData(IMKitClient.account());
              }
              if (resultCode == RESULT_OK) {
                return;
              }
              resultCode = result.getResultCode();
            });

    initView();
    loadData(IMKitClient.account());
  }

  private void initView() {
    binding.cavAvatar.setOnClickListener(v -> choicePhoto());
    binding.flName.setOnClickListener(
        v -> EditUserInfoActivity.launch(getApplicationContext(), Constant.EDIT_NAME, launcher));
    binding.flEmail.setOnClickListener(
        v -> EditUserInfoActivity.launch(getApplicationContext(), Constant.EDIT_EMAIL, launcher));
    binding.flPhone.setOnClickListener(
        v -> EditUserInfoActivity.launch(getApplicationContext(), Constant.EDIT_PHONE, launcher));
    binding.flSign.setOnClickListener(
        v -> EditUserInfoActivity.launch(getApplicationContext(), Constant.EDIT_SIGN, launcher));
    binding.ivBack.setOnClickListener(v -> finish());
    binding.flBirthday.setOnClickListener(v -> showTimerPicker(userInfo.getBirthday()));

    binding.ivAccountCopy.setOnClickListener(v -> copyAccount());
    binding.tvAccount.setOnClickListener(v -> copyAccount());

    binding.flSexual.setOnClickListener(
        v -> {
          ArrayList<String> content = new ArrayList<>();
          content.add(getResources().getString(R.string.sexual_male));
          content.add(getResources().getString(R.string.sexual_female));
          int selectIndex = -1;
          if (userInfo.getGender() == 1) {
            selectIndex = 0;
          } else if (userInfo.getGender() == 2) {
            selectIndex = 1;
          }
          TypeSelectActivity.launch(
              MineInfoActivity.this,
              getResources().getString(R.string.user_info_sexual),
              content,
              selectIndex,
              launcher);
        });
    if (AppSkinConfig.getInstance().getAppSkinStyle() == AppSkinConfig.AppSkin.commonSkin) {
      setCommonSkin();
    }
  }

  private void setCommonSkin() {
    int cornerRadius = SizeUtils.dp2px(4);
    binding.cavAvatar.setCornerRadius(cornerRadius);

    changeStatusBarColor(R.color.color_ededed);

    binding.clRoot.setBackgroundResource(R.color.color_ededed);

    binding.llUserInfo.setBackgroundResource(R.color.color_white);
    ViewGroup.MarginLayoutParams layoutParamsN =
        (ViewGroup.MarginLayoutParams) binding.llUserInfo.getLayoutParams();
    layoutParamsN.setMargins(0, SizeUtils.dp2px(4), 0, 0);
    binding.llUserInfo.setLayoutParams(layoutParamsN);

    binding.flSign.setBackgroundResource(R.color.color_white);
    ViewGroup.MarginLayoutParams layoutParamsS =
        (ViewGroup.MarginLayoutParams) binding.flSign.getLayoutParams();
    layoutParamsS.setMargins(0, SizeUtils.dp2px(6), 0, 0);
    binding.flSign.setLayoutParams(layoutParamsS);
  }

  private void loadData(String account) {
    ContactRepo.getUserInfo(
        Collections.singletonList(account),
        new FetchCallback<List<V2NIMUser>>() {
          @Override
          public void onSuccess(@Nullable List<V2NIMUser> param) {
            if (param != null && !param.isEmpty()) {
              refreshUserInfo(param.get(0));
            }
          }

          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ToastX.showShortToast(R.string.user_fail);
          }
        });
  }

  private void copyAccount() {
    ClipboardManager cmb =
        (ClipboardManager)
            IMKitClient.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clipData = null;
    if (userInfo != null) {
      clipData = ClipData.newPlainText(null, userInfo.getAccountId());
    }
    if (clipData == null) {
      return;
    }
    cmb.setPrimaryClip(clipData);
    ToastX.showShortToast(R.string.action_copy_success);
  }

  private void choicePhoto() {
    BasePhotoChoiceDialog choiceDialog;
    if (AppSkinConfig.getInstance().getAppSkinStyle() == AppSkinConfig.AppSkin.commonSkin) {
      choiceDialog = new FunPhotoChoiceDialog(this);
    } else {
      choiceDialog = new ImageChoiceDialog(this);
    }
    choiceDialog.show(
        new CommonCallback<File>() {
          @Override
          public void onSuccess(@Nullable File param) {
            if (param == null) {
              return;
            }
            if (NetworkUtils.isConnected()) {
              ResourceRepo.uploadFile(
                  param,
                  new FetchCallback<String>() {
                    @Override
                    public void onError(int errorCode, @Nullable String errorMsg) {
                      Toast.makeText(
                              getApplicationContext(),
                              getString(R.string.request_fail),
                              Toast.LENGTH_SHORT)
                          .show();
                    }

                    @Override
                    public void onSuccess(@Nullable String urlParam) {
                      V2NIMUserUpdateParams.V2NIMUserUpdateParamsBuilder builder =
                          V2NIMUserUpdateParams.V2NIMUserUpdateParamsBuilder.builder();
                      builder.withAvatar(urlParam);
                      ContactRepo.updateSelfUserProfile(
                          builder.build(),
                          new FetchCallback<Void>() {
                            @Override
                            public void onError(int errorCode, @Nullable String errorMsg) {
                              Toast.makeText(
                                      getApplicationContext(),
                                      getString(R.string.request_fail),
                                      Toast.LENGTH_SHORT)
                                  .show();
                            }

                            @Override
                            public void onSuccess(@Nullable Void param) {
                              resultCode = RESULT_OK;
                              loadData(IMKitClient.account());
                            }
                          });
                    }
                  });
            } else {
              Toast.makeText(
                      getApplicationContext(),
                      getString(R.string.network_error),
                      Toast.LENGTH_SHORT)
                  .show();
            }
          }

          @Override
          public void onFailed(int code) {
            Toast.makeText(
                    getApplicationContext(), getString(R.string.request_fail), Toast.LENGTH_SHORT)
                .show();
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            Toast.makeText(
                    getApplicationContext(), getString(R.string.request_fail), Toast.LENGTH_SHORT)
                .show();
          }
        });
  }

  private void refreshUserInfo(V2NIMUser userInfo) {
    if (userInfo == null) {
      return;
    }
    this.userInfo = userInfo;
    binding.cavAvatar.setData(
        userInfo.getAvatar(),
        userInfo.getName() == null ? "" : userInfo.getName(),
        AvatarColor.avatarColor(IMKitClient.account()));
    binding.tvName.setText(userInfo.getName());
    int sexualValue = R.string.sexual_unknown;
    if (userInfo.getGender() == 1) {
      sexualValue = R.string.sexual_male;
    } else if (userInfo.getGender() == 2) {
      sexualValue = R.string.sexual_female;
    }
    binding.tvAccount.setText(userInfo.getAccountId());
    binding.tvSexual.setText(sexualValue);
    binding.tvBirthday.setText(userInfo.getBirthday());
    binding.tvPhone.setText(userInfo.getMobile());
    binding.tvEmail.setText(userInfo.getEmail());
    binding.tvSign.setText(userInfo.getSign());
  }

  private void showTimerPicker(String date) {
    String beginTime = "1900-01-01 00:00";
    String endTime = DateFormatUtils.long2Str(System.currentTimeMillis(), true);

    if (TextUtils.isEmpty(date)) {
      date = endTime;
    }
    // 通过日期字符串初始化日期，格式请用：yyyy-MM-dd HH:mm
    CustomDatePicker mTimerPicker =
        new CustomDatePicker(
            this,
            timestamp -> {
              V2NIMUserUpdateParams.V2NIMUserUpdateParamsBuilder builder =
                  V2NIMUserUpdateParams.V2NIMUserUpdateParamsBuilder.builder();
              builder.withBirthday(DateFormatUtils.long2Str(timestamp, false));
              updateUserInfo(builder.build());
            },
            beginTime,
            endTime);
    // 允许点击屏幕或物理返回键关闭
    mTimerPicker.setCancelable(true);
    // 显示时和分
    mTimerPicker.setCanShowPreciseTime(false);
    // 允许循环滚动
    mTimerPicker.setScrollLoop(false);
    // 允许滚动动画
    mTimerPicker.setCanShowAnim(false);
    mTimerPicker.show(date);
  }

  private void updateUserInfo(V2NIMUserUpdateParams params) {
    ContactRepo.updateSelfUserProfile(
        params,
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            if (errorCode == Constant.NETWORK_ERROR_CODE) {
              Toast.makeText(
                      getApplicationContext(),
                      getString(R.string.network_error),
                      Toast.LENGTH_SHORT)
                  .show();
            } else {
              Toast.makeText(
                      getApplicationContext(), getString(R.string.request_fail), Toast.LENGTH_SHORT)
                  .show();
            }
          }

          @Override
          public void onSuccess(@Nullable Void param) {
            resultCode = RESULT_OK;
            loadData(IMKitClient.account());
          }
        });
  }

  @Override
  public void finish() {
    setResult(resultCode);
    super.finish();
  }

  public static void launch(Context context, @NonNull ActivityResultLauncher<Intent> launcher) {
    Intent intent = new Intent(context, MineInfoActivity.class);
    launcher.launch(intent);
  }
}
