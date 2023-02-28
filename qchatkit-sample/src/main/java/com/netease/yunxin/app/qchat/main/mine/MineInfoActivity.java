// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.qchat.main.mine;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.netease.yunxin.app.qchat.R;
import com.netease.yunxin.app.qchat.databinding.ActivityMineInfoBinding;
import com.netease.yunxin.app.qchat.utils.Constant;
import com.netease.yunxin.kit.common.ui.photo.PhotoChoiceDialog;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.utils.CommonCallback;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.widgets.datepicker.CustomDatePicker;
import com.netease.yunxin.kit.common.ui.widgets.datepicker.DateFormatUtils;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserField;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.repo.CommonRepo;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MineInfoActivity extends AppCompatActivity {
  private ActivityMineInfoBinding binding;
  private ActivityResultLauncher<Intent> launcher;
  private UserInfo userInfo;
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
                  updateUserInfo(UserField.Gender, select);
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
          content.add(getResources().getString(R.string.sexual_unknown));
          content.add(getResources().getString(R.string.sexual_male));
          content.add(getResources().getString(R.string.sexual_female));
          int selectIndex = 0;
          if (userInfo.getGenderEnum() == GenderEnum.MALE) {
            selectIndex = 1;
          } else if (userInfo.getGenderEnum() == GenderEnum.FEMALE) {
            selectIndex = 2;
          }
          TypeSelectActivity.launch(
              MineInfoActivity.this,
              getResources().getString(R.string.user_info_sexual),
              content,
              selectIndex,
              launcher);
        });
  }

  private void loadData(String account) {
    CommonRepo.getUserInfo(
        account,
        new FetchCallback<UserInfo>() {
          @Override
          public void onSuccess(@Nullable UserInfo param) {
            refreshUserInfo(param);
          }

          @Override
          public void onFailed(int code) {
            ToastX.showShortToast(R.string.user_fail);
            refreshUserInfo(new UserInfo(account, account, ""));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ToastX.showShortToast(R.string.user_fail);
            refreshUserInfo(new UserInfo(account, account, ""));
          }
        });
  }

  private void copyAccount() {
    ClipboardManager cmb =
        (ClipboardManager)
            IMKitClient.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clipData = null;
    if (userInfo != null) {
      clipData = ClipData.newPlainText(null, userInfo.getAccount());
    }
    cmb.setPrimaryClip(clipData);
    ToastX.showShortToast(R.string.action_copy_success);
  }

  private void choicePhoto() {
    new PhotoChoiceDialog(MineInfoActivity.this)
        .show(
            new CommonCallback<File>() {
              @Override
              public void onSuccess(@Nullable File param) {
                if (NetworkUtils.isConnected()) {
                  CommonRepo.uploadImage(
                      param,
                      new FetchCallback<String>() {
                        @Override
                        public void onSuccess(@Nullable String urlParam) {
                          Map<UserField, Object> map = new HashMap<>(1);
                          map.put(UserField.Avatar, urlParam);
                          CommonRepo.updateUserInfo(
                              map,
                              new FetchCallback<Void>() {
                                @Override
                                public void onSuccess(@Nullable Void param) {
                                  resultCode = RESULT_OK;
                                  loadData(IMKitClient.account());
                                }

                                @Override
                                public void onFailed(int code) {
                                  Toast.makeText(
                                          getApplicationContext(),
                                          getString(R.string.request_fail),
                                          Toast.LENGTH_SHORT)
                                      .show();
                                }

                                @Override
                                public void onException(@Nullable Throwable exception) {
                                  Toast.makeText(
                                          getApplicationContext(),
                                          getString(R.string.request_fail),
                                          Toast.LENGTH_SHORT)
                                      .show();
                                }
                              });
                        }

                        @Override
                        public void onFailed(int code) {
                          Toast.makeText(
                                  getApplicationContext(),
                                  getString(R.string.request_fail),
                                  Toast.LENGTH_SHORT)
                              .show();
                        }

                        @Override
                        public void onException(@Nullable Throwable exception) {
                          Toast.makeText(
                                  getApplicationContext(),
                                  getString(R.string.request_fail),
                                  Toast.LENGTH_SHORT)
                              .show();
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
                        getApplicationContext(),
                        getString(R.string.request_fail),
                        Toast.LENGTH_SHORT)
                    .show();
              }

              @Override
              public void onException(@Nullable Throwable exception) {
                Toast.makeText(
                        getApplicationContext(),
                        getString(R.string.request_fail),
                        Toast.LENGTH_SHORT)
                    .show();
              }
            });
  }

  private void refreshUserInfo(UserInfo userInfo) {
    if (userInfo == null) {
      return;
    }
    this.userInfo = userInfo;
    binding.cavAvatar.setData(
        userInfo.getAvatar(),
        TextUtils.isEmpty(userInfo.getName()) ? userInfo.getAccount() : userInfo.getName(),
        AvatarColor.avatarColor(IMKitClient.account()));
    binding.tvName.setText(userInfo.getName());
    int sexualValue = R.string.sexual_unknown;
    if (userInfo.getGenderEnum() == GenderEnum.MALE) {
      sexualValue = R.string.sexual_male;
    } else if (userInfo.getGenderEnum() == GenderEnum.FEMALE) {
      sexualValue = R.string.sexual_female;
    }
    binding.tvAccount.setText(userInfo.getAccount());
    binding.tvSexual.setText(sexualValue);
    binding.tvBirthday.setText(userInfo.getBirthday());
    binding.tvPhone.setText(userInfo.getMobile());
    binding.tvEmail.setText(userInfo.getEmail());
    binding.tvSign.setText(userInfo.getSignature());
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
            new CustomDatePicker.Callback() {
              @Override
              public void onTimeSelected(long timestamp) {
                updateUserInfo(UserField.Birthday, DateFormatUtils.long2Str(timestamp, false));
              }
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

  private void updateUserInfo(UserField field, Object value) {
    Map<UserField, Object> map = new HashMap<>(1);
    map.put(field, value);
    CommonRepo.updateUserInfo(
        map,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            resultCode = RESULT_OK;
            //                binding.tvBirthday.setText(birthday);
            loadData(IMKitClient.account());
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
