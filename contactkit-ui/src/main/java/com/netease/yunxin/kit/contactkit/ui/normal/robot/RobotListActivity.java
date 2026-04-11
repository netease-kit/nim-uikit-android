// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.robot;

import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.RobotListActivityLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.robot.BaseRobotListActivity;
import com.netease.yunxin.kit.contactkit.ui.robot.RobotListAdapter;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

/** Normal 版机器人列表页面 */
public class RobotListActivity extends BaseRobotListActivity {

  @Override
  protected void initViews() {
    changeStatusBarColor(R.color.color_white);
    RobotListActivityLayoutBinding binding =
        RobotListActivityLayoutBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    rvRobotList = binding.rvRobotList;
    emptyLayout = binding.emptyLayout;

    binding.title.setOnBackIconClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    binding.title.setTitle(R.string.contact_robot_title);
    binding.title.setBackgroundResource(R.color.color_white);
    binding
        .title
        .setActionImg(R.drawable.ic_contact_add_robot)
        .setActionListener(v -> navigateToCreateRobot());

    binding.emptyIv.setImageResource(R.drawable.ic_contact_empty);
    binding.emptyTv.setText(R.string.contact_robot_empty_tips);

    adapter = new RobotListAdapter(R.layout.robot_list_item_layout);
    rvRobotList.setLayoutManager(new LinearLayoutManager(this));
    rvRobotList.setAdapter(adapter);
    adapter.setItemClickListener(
        bean ->
            XKitRouter.withKey(getRobotInfoRouterPath())
                .withContext(this)
                .withParam(RouterConstant.KEY_ACCOUNT_ID_KEY, bean.getAccountId())
                .withParam(RouterConstant.KEY_ROBOT_NAME, bean.getName())
                .withParam(RouterConstant.KEY_ROBOT_AVATAR, bean.getAvatar())
                .navigate());
  }

  @Override
  protected String getCreateRobotRouterPath() {
    return RouterConstant.PATH_MY_ROBOT_CREATE_PAGE;
  }

  @Override
  protected String getRobotInfoRouterPath() {
    return RouterConstant.PATH_MY_ROBOT_INFO_PAGE;
  }
}
