// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.qchat.main.mine;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import com.netease.yunxin.app.qchat.R;
import com.netease.yunxin.app.qchat.databinding.TypeSelectActivityBinding;
import com.netease.yunxin.app.qchat.utils.Constant;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import java.util.ArrayList;

/** channel type select page choice list should be transfer by intent */
public class TypeSelectActivity extends BaseActivity {

  private static final String TAG = "TypeSelectActivity";
  private TypeSelectActivityBinding viewBinding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ALog.d(TAG, "onCreate");
    changeStatusBarColor(R.color.color_eff1f4);
    viewBinding = TypeSelectActivityBinding.inflate(LayoutInflater.from(this));
    setContentView(viewBinding.getRoot());
    initView();
  }

  private void initView() {
    String title = getIntent().getStringExtra(Constant.TITLE);
    int selected = getIntent().getIntExtra(Constant.SELECTED_INDEX, 0);
    ArrayList<String> groupList = getIntent().getStringArrayListExtra(Constant.CHOICE_LIST);
    viewBinding.typeSelectTitleBar.setTitle(title);
    if (groupList != null && groupList.size() > 0) {
      RadioGroup.LayoutParams layoutParams =
          new RadioGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              (int) getResources().getDimension(R.dimen.dimen_50_dp));
      for (int index = 0; index < groupList.size(); index++) {
        RadioButton radioButton = createRadioButton(index, groupList.get(index));
        if (index == selected) {
          radioButton.setChecked(true);
        }
        ALog.d(TAG, "initView", "group:" + groupList.get(index));
        viewBinding.typeRadioGroup.addView(radioButton, index, layoutParams);
      }
    }

    viewBinding.typeSelectTitleBar.setOnBackIconClickListener(
        view -> {
          back();
        });
  }

  @Override
  public void onBackPressed() {
    back();
    super.onBackPressed();
  }

  private void back() {
    int checkedId = viewBinding.typeRadioGroup.getCheckedRadioButtonId();
    Intent intent = new Intent();
    intent.putExtra(Constant.EDIT_TYPE, Constant.EDIT_SEXUAL);
    intent.putExtra(Constant.SELECTED_INDEX, checkedId);
    setResult(RESULT_OK, intent);
    ALog.d(TAG, "onBackPressed", "page back:" + checkedId);
    finish();
  }

  private RadioButton createRadioButton(int index, String title) {
    RadioButton radioButton = new RadioButton(this);
    radioButton.setId(index);
    radioButton.setButtonDrawable(null);
    radioButton.setBackgroundResource(R.color.transparent);
    radioButton.setText(title);
    radioButton.setCompoundDrawablesWithIntrinsicBounds(
        null, null, AppCompatResources.getDrawable(this, R.drawable.app_radio_button_select), null);
    return radioButton;
  }

  public static void launch(
      Context context,
      String title,
      ArrayList<String> groupList,
      int select,
      @NonNull ActivityResultLauncher<Intent> launcher) {
    Intent intent = new Intent(context, TypeSelectActivity.class);
    intent.putExtra(Constant.TITLE, title);
    intent.putExtra(Constant.CHOICE_LIST, groupList);
    intent.putExtra(Constant.SELECTED_INDEX, select);
    launcher.launch(intent);
  }
}
