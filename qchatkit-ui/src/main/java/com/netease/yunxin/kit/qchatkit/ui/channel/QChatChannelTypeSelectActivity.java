// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatChannelTypeSelectActivityBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import java.util.ArrayList;

/** channel type select page choice list should be transfer by intent */
public class QChatChannelTypeSelectActivity extends BaseActivity {

  private static final String TAG = "QChatChannelTypeSelectActivity";
  private QChatChannelTypeSelectActivityBinding viewBinding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ALog.d(TAG, "onCreate");
    changeStatusBarColor(R.color.color_eff1f4);
    viewBinding = QChatChannelTypeSelectActivityBinding.inflate(LayoutInflater.from(this));
    setContentView(viewBinding.getRoot());
    initView();
  }

  private void initView() {
    String title = getIntent().getStringExtra(QChatConstant.TITLE);
    int selected = getIntent().getIntExtra(QChatConstant.SELECTED_INDEX, 0);
    ArrayList<String> groupList = getIntent().getStringArrayListExtra(QChatConstant.CHOICE_LIST);
    viewBinding.channelTypeSelectTitleBar.setTitle(title);
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
        viewBinding.qchatChannelTypeRadioGroup.addView(radioButton, index, layoutParams);
      }
    }

    viewBinding.channelTypeSelectTitleBar.setOnBackIconClickListener(
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
    int checkedId = viewBinding.qchatChannelTypeRadioGroup.getCheckedRadioButtonId();
    setResult(checkedId);
    ALog.d(TAG, "onBackPressed", "page back:" + checkedId);
    finish();
  }

  private RadioButton createRadioButton(int index, String title) {
    RadioButton radioButton = new RadioButton(this);
    radioButton.setId(index);
    radioButton.setButtonDrawable(getDrawable(R.drawable.radio_button_select));
    radioButton.setText(title);
    radioButton.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
    return radioButton;
  }
}
