// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main.mine;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.netease.nimlib.coexist.sdk.v2.user.V2NIMUser;
import com.netease.nimlib.coexist.sdk.v2.user.params.V2NIMUserUpdateParams;
import com.netease.yunxin.app.im.AppSkinConfig;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityEditNicknameBinding;
import com.netease.yunxin.app.im.utils.AppUtils;
import com.netease.yunxin.app.im.utils.Constant;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.coexist.im2.extend.FetchCallback;
import java.util.Collections;
import java.util.List;

public class EditUserInfoActivity extends BaseLocalActivity {
  private ActivityEditNicknameBinding binding;
  private String editType = Constant.EDIT_NAME;
  private V2NIMUser userInfo;

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
          if (TextUtils.equals(Constant.EDIT_EMAIL, editType)) {
            String emailText = binding.etNickname.getText().toString();
            if (!AppUtils.checkEmail(emailText)) {
              Toast.makeText(getApplicationContext(), R.string.imkit_email_fail, Toast.LENGTH_SHORT)
                  .show();
              return;
            }
          }
          ContactRepo.updateSelfUserProfile(
              buildUpdateParam(),
              new FetchCallback<Void>() {
                @Override
                public void onError(int errorCode, @Nullable String errorMsg) {
                  if (errorCode == Constant.NETWORK_ERROR_CODE) {
                    Toast.makeText(
                            getApplicationContext(),
                            getString(R.string.network_error),
                            Toast.LENGTH_SHORT)
                        .show();
                  } else if (errorCode == Constant.ANTI_ERROR_CODE) {
                    Toast.makeText(
                            getApplicationContext(),
                            getString(R.string.anti_error),
                            Toast.LENGTH_SHORT)
                        .show();
                  } else {
                    Toast.makeText(
                            getApplicationContext(),
                            getString(R.string.request_fail) + errorCode,
                            Toast.LENGTH_SHORT)
                        .show();
                  }
                }

                @Override
                public void onSuccess(@Nullable Void param) {
                  loadData();
                  setResult(RESULT_OK);
                  finish();
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
          public void afterTextChanged(Editable s) {}
        });

    binding.etNickname.requestFocus();

    binding.ivClear.setOnClickListener(v -> binding.etNickname.setText(null));
    if (AppSkinConfig.getInstance().getAppSkinStyle() == AppSkinConfig.AppSkin.commonSkin) {
      setCommonSkin();
    }
  }

  private void setCommonSkin() {
    changeStatusBarColor(R.color.color_ededed);
    binding.clyRoot.setBackgroundResource(R.color.color_ededed);
    binding.tvDone.setTextColor(getResources().getColor(R.color.color_58be6b));

    binding.etNickname.setBackgroundResource(R.color.color_white);
    ViewGroup.MarginLayoutParams layoutParamsS =
        (ViewGroup.MarginLayoutParams) binding.etNickname.getLayoutParams();
    layoutParamsS.setMargins(0, SizeUtils.dp2px(6), 0, 0);
    binding.etNickname.setLayoutParams(layoutParamsS);
  }

  private void loadData() {
    String account = IMKitClient.account();
    if (TextUtils.isEmpty(account)) {
      finish();
      return;
    }
    ContactRepo.getUserInfo(
        Collections.singletonList(account),
        new FetchCallback<List<V2NIMUser>>() {
          @Override
          public void onSuccess(@Nullable List<V2NIMUser> data) {
            if (data != null && !data.isEmpty()) {
              updateView(data.get(0));
            }
          }

          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ToastX.showShortToast(R.string.user_fail);
          }
        });
  }

  private void updateView(V2NIMUser user) {
    this.userInfo = user;
    String remoteInfo = "";
    if (TextUtils.equals(Constant.EDIT_NAME, editType)) {
      remoteInfo = userInfo.getName();
      binding.etNickname.setFilters(new InputFilter[] {new InputFilter.LengthFilter(15)});
      binding.tvTitle.setText(R.string.user_info_nickname);
    } else if (TextUtils.equals(Constant.EDIT_SIGN, editType)) {
      remoteInfo = userInfo.getSign();
      binding.etNickname.setFilters(new InputFilter[] {new InputFilter.LengthFilter(50)});
      binding.tvTitle.setText(R.string.user_info_sign);
    } else if (TextUtils.equals(Constant.EDIT_EMAIL, editType)) {
      remoteInfo = userInfo.getEmail();
      binding.etNickname.setFilters(new InputFilter[] {new InputFilter.LengthFilter(30)});
      binding.etNickname.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
      binding.tvTitle.setText(R.string.user_info_email);
    } else if (TextUtils.equals(Constant.EDIT_PHONE, editType)) {
      remoteInfo = userInfo.getMobile();
      binding.etNickname.setFilters(new InputFilter[] {new InputFilter.LengthFilter(11)});
      binding.etNickname.setInputType(InputType.TYPE_CLASS_PHONE);
      binding.tvTitle.setText(R.string.user_info_phone);
    }
    binding.etNickname.setText(remoteInfo);
    if (!TextUtils.isEmpty(remoteInfo)) {

      binding.etNickname.setSelection(remoteInfo.length());
    }
  }

  private V2NIMUserUpdateParams buildUpdateParam() {
    String result = binding.etNickname.getText().toString();
    V2NIMUserUpdateParams.V2NIMUserUpdateParamsBuilder updateParams =
        V2NIMUserUpdateParams.V2NIMUserUpdateParamsBuilder.builder();
    if (TextUtils.equals(Constant.EDIT_NAME, editType)) {
      if (TextUtils.isEmpty(result)) {
        result = userInfo.getAccountId();
      }
      updateParams.withName(result);
    } else if (TextUtils.equals(Constant.EDIT_SIGN, editType)) {
      updateParams.withSign(result);
    } else if (TextUtils.equals(Constant.EDIT_EMAIL, editType)) {
      updateParams.withEmail(result);
    } else if (TextUtils.equals(Constant.EDIT_PHONE, editType)) {
      updateParams.withMobile(result);
    }
    return updateParams.build();
  }

  public static void launch(
      Context context, String type, @NonNull ActivityResultLauncher<Intent> launcher) {
    Intent intent = new Intent(context, EditUserInfoActivity.class);
    intent.putExtra(Constant.EDIT_TYPE, type);
    launcher.launch(intent);
  }
}
