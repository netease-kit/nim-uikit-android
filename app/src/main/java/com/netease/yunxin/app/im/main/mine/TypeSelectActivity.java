// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main.mine;

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
import com.netease.yunxin.app.im.AppSkinConfig;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.TypeSelectActivityBinding;
import com.netease.yunxin.app.im.utils.Constant;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import java.util.ArrayList;

/** channel type select page choice list should be transfer by intent */
public class TypeSelectActivity extends BaseLocalActivity {

  private static final String TAG = "TypeSelectActivity";
  private TypeSelectActivityBinding viewBinding;

  private int[] ids =
      new int[] {R.id.type_select_rb_0, R.id.type_select_rb_1, R.id.type_select_rb_2};

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
    int selected = getIntent().getIntExtra(Constant.SELECTED_INDEX, -1);
    ArrayList<String> groupList = getIntent().getStringArrayListExtra(Constant.CHOICE_LIST);
    viewBinding.typeSelectTitleBar.setTitle(title);
    if (groupList != null && groupList.size() > 0) {
      RadioGroup.LayoutParams layoutParams =
          new RadioGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              (int) getResources().getDimension(R.dimen.dimen_50_dp));
      for (int index = 0; index < groupList.size(); index++) {
        RadioButton radioButton = createRadioButton(index, groupList.get(index));
        if (index < ids.length) {
          radioButton.setId(ids[index]);
        }
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

    if (AppSkinConfig.getInstance().getAppSkinStyle() == AppSkinConfig.AppSkin.commonSkin) {
      setCommonSkin();
    }
  }

  private void setCommonSkin() {
    changeStatusBarColor(R.color.color_ededed);
    viewBinding.llyRoot.setBackgroundResource(R.color.color_ededed);

    viewBinding.rfLayout.setBackgroundResource(R.color.color_white);
    ViewGroup.MarginLayoutParams layoutParamsS =
        (ViewGroup.MarginLayoutParams) viewBinding.rfLayout.getLayoutParams();
    layoutParamsS.setMargins(0, SizeUtils.dp2px(6), 0, 0);
    viewBinding.rfLayout.setLayoutParams(layoutParamsS);
  }

  @Override
  public void onBackPressed() {
    back();
    super.onBackPressed();
  }

  private void back() {
    int checkedId = viewBinding.typeRadioGroup.getCheckedRadioButtonId();
    int checkIndex = 0;
    for (int index = 0; index < ids.length; index++) {
      if (ids[index] == checkedId) {
        checkIndex = index;
        break;
      }
    }
    Intent intent = new Intent();
    intent.putExtra(Constant.EDIT_TYPE, Constant.EDIT_SEXUAL);
    intent.putExtra(Constant.SELECTED_INDEX, checkIndex);
    setResult(RESULT_OK, intent);
    ALog.d(TAG, "onBackPressed", "page back:" + checkIndex);
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
