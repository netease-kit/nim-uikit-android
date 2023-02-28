// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.qchat.main.mine;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.netease.yunxin.app.qchat.R;
import com.netease.yunxin.app.qchat.databinding.ActivityEditNicknameBinding;
import com.netease.yunxin.app.qchat.utils.Constant;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserField;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.repo.CommonRepo;
import java.util.HashMap;
import java.util.Map;

public class EditUserInfoActivity extends AppCompatActivity {
  private ActivityEditNicknameBinding binding;
  private String editType = Constant.EDIT_NAME;
  private UserField userField = UserField.Name;
  private UserInfo userInfo;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityEditNicknameBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    Window window = getWindow();
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    window.setStatusBarColor(ContextCompat.getColor(this, R.color.color_e9eff5));

    if (getIntent().getStringExtra(Constant.EDIT_TYPE) != null) {
      editType = getIntent().getStringExtra(Constant.EDIT_TYPE);
    }
    loadData();
    binding.ivBack.setOnClickListener(v -> finish());
    binding.tvDone.setOnClickListener(
        v -> {
          Map<UserField, Object> map = new HashMap<>(1);
          String result = binding.etNickname.getText().toString();
          map.put(userField, result);

          CommonRepo.updateUserInfo(
              map,
              new FetchCallback<Void>() {
                @Override
                public void onSuccess(@Nullable Void param) {
                  loadData();
                  setResult(RESULT_OK);
                  finish();
                }

                @Override
                public void onFailed(int code) {
                  Toast.makeText(
                          getApplicationContext(),
                          getString(R.string.request_fail) + code,
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
        });

    binding.etNickname.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            //            if (TextUtils.isEmpty(String.valueOf(s))) {
            //              binding.ivClear.setVisibility(View.GONE);
            //              binding.tvDone.setEnabled(false);
            //              binding.tvDone.setAlpha(0.5f);
            //            } else {
            //              binding.ivClear.setVisibility(View.VISIBLE);
            //              binding.tvDone.setEnabled(true);
            //              binding.tvDone.setAlpha(1f);
            //            }
          }
        });

    binding.etNickname.requestFocus();

    binding.ivClear.setOnClickListener(v -> binding.etNickname.setText(null));
  }

  private void loadData() {
    String account = IMKitClient.account();
    if (TextUtils.isEmpty(account)) {
      finish();
      return;
    }
    CommonRepo.getUserInfo(
        account,
        new FetchCallback<UserInfo>() {
          @Override
          public void onSuccess(@Nullable UserInfo param) {
            updateView(param);
          }

          @Override
          public void onFailed(int code) {
            ToastX.showShortToast(R.string.user_fail);
            updateView(new UserInfo(account, account, ""));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ToastX.showShortToast(R.string.user_fail);
            updateView(new UserInfo(account, account, ""));
          }
        });
  }

  private void updateView(UserInfo user) {
    this.userInfo = user;
    String remoteInfo = "";
    if (TextUtils.equals(Constant.EDIT_NAME, editType)) {
      remoteInfo = userInfo.getName();
      userField = UserField.Name;
      binding.etNickname.setFilters(new InputFilter[] {new InputFilter.LengthFilter(30)});
      binding.tvTitle.setText(R.string.user_info_nickname);
    } else if (TextUtils.equals(Constant.EDIT_SIGN, editType)) {
      remoteInfo = userInfo.getSignature();
      userField = UserField.Signature;
      binding.etNickname.setFilters(new InputFilter[] {new InputFilter.LengthFilter(50)});
      binding.tvTitle.setText(R.string.user_info_sign);
    } else if (TextUtils.equals(Constant.EDIT_EMAIL, editType)) {
      remoteInfo = userInfo.getEmail();
      userField = UserField.Email;
      binding.etNickname.setFilters(new InputFilter[] {new InputFilter.LengthFilter(30)});
      binding.etNickname.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
      binding.tvTitle.setText(R.string.user_info_email);
    } else if (TextUtils.equals(Constant.EDIT_PHONE, editType)) {
      remoteInfo = userInfo.getMobile();
      userField = UserField.Mobile;
      binding.etNickname.setFilters(new InputFilter[] {new InputFilter.LengthFilter(11)});
      binding.etNickname.setInputType(InputType.TYPE_CLASS_PHONE);
      binding.tvTitle.setText(R.string.user_info_phone);
    }
    binding.etNickname.setText(remoteInfo);
  }

  public static void launch(
      Context context, String type, @NonNull ActivityResultLauncher<Intent> launcher) {
    Intent intent = new Intent(context, EditUserInfoActivity.class);
    intent.putExtra(Constant.EDIT_TYPE, type);
    launcher.launch(intent);
  }
}
