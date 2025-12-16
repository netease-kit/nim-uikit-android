// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.selector;

import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.KEY_REQUEST_SELECTOR_NAME;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunContactSelectorActivityLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.fun.contact.FunContactDefaultFactory;
import com.netease.yunxin.kit.contactkit.ui.selector.BaseContactSelectorActivity;
import com.netease.yunxin.kit.contactkit.ui.selector.BaseSelectedListAdapter;
import java.util.List;

public class FunContactSelectorActivity extends BaseContactSelectorActivity {

  protected FunContactSelectorActivityLayoutBinding binding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_ededed);
  }

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    binding = FunContactSelectorActivityLayoutBinding.inflate(getLayoutInflater());
    contactListView = binding.contactListView;
    contactListView
        .getDecoration()
        .setTitleAlignBottom(true)
        .setShowTagOff(false)
        .setIndexDecorationBg(getResources().getColor(R.color.title_transfer))
        .setColorTitleBottomLine(getResources().getColor(R.color.title_transfer));
    contactListView.setViewHolderFactory(new FunContactDefaultFactory());
    contactListView.configIndexTextBGColor(getResources().getColor(R.color.color_58be6b));
    emptyGroup = binding.emptyLayout;
    rvSelected = binding.rvSelected;
    titleBar = binding.title;
    return binding.getRoot();
  }

  protected BaseSelectedListAdapter<? extends ViewBinding> getSelectedListAdapter() {
    return new FunSelectedListAdapter();
  }

  protected void configTitle(BackTitleBar titleBar) {
    titleBar
        .setOnBackIconClickListener(v -> onBackPressed())
        .setTitle(R.string.select)
        .setLeftText(R.string.fun_selector_close)
        .setActionText(getString(R.string.selector_sure_without_num))
        .setActionTextColor(getResources().getColor(R.color.color_white))
        .setActionListener(
            v -> {
              if (checkNetworkEnable && !NetworkUtils.isConnected()) {
                Toast.makeText(this, R.string.contact_network_error_tip, Toast.LENGTH_SHORT).show();
                return;
              }
              List<String> select = getSelectedAccount();
              if (select.size() < 1) {
                Toast.makeText(this, getString(R.string.select_empty_tips), Toast.LENGTH_LONG)
                    .show();
                return;
              }
              if (selectedListAdapter.getItemCount() > maxSelectCount
                  && selectFinalCheckCountEnable) {
                Toast.makeText(this, R.string.contact_selector_over_count, Toast.LENGTH_LONG)
                    .show();
                return;
              }
              Intent result = new Intent();
              if (!selectedListAdapter.getSelectedFriends().isEmpty()) {
                result.putExtra(REQUEST_CONTACT_SELECTOR_KEY, getSelectedAccount());
                if (enableReturnName) {
                  result.putExtra(KEY_REQUEST_SELECTOR_NAME, getSelectedName());
                }
              }
              setResult(RESULT_OK, result);
              finish();
            });

    int verticalPadding = SizeUtils.dp2px(5);
    int horizontalPadding = SizeUtils.dp2px(10);
    int endPadding = SizeUtils.dp2px(5);
    TextView rightTextView = titleBar.getRightTextView();
    FrameLayout.LayoutParams layoutParams =
        (FrameLayout.LayoutParams) rightTextView.getLayoutParams();
    layoutParams.rightMargin = SizeUtils.dp2px(endPadding);
    rightTextView.setPadding(
        horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
    titleBar.getRightTextView().setBackgroundResource(R.drawable.fun_contact_select_confirm_btn_bg);
    titleBar.getTitleTextView().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
    titleBar.getTitleTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
    titleBar.setBackgroundResource(R.color.color_ededed);
  }
}
