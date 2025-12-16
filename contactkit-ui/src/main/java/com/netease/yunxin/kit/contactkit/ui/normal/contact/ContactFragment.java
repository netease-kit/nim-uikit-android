// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.contact;

import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.PATH_ADD_FRIEND_PAGE;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.contact.BaseContactFragment;
import com.netease.yunxin.kit.contactkit.ui.databinding.ContactFragmentBinding;
import com.netease.yunxin.kit.contactkit.ui.model.ContactEntranceBean;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;
import java.util.List;

/** contact page */
public class ContactFragment extends BaseContactFragment {
  private final String TAG = "ContactFragment";
  private ContactFragmentBinding viewBinding;
  private ContactEntranceBean verifyBean;

  @Override
  protected View initViewAndGetRootView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    viewBinding = ContactFragmentBinding.inflate(inflater, container, false);
    contactLayout = viewBinding.contactLayout;
    contactLayout
        .getContactListView()
        .getDecoration()
        .setTitleAlignBottom(false)
        .setIndexDecorationBg(getResources().getColor(R.color.color_eff1f4))
        .setColorTitleBottomLine(getResources().getColor(R.color.color_dbe0e8));
    contactLayout.getContactListView().setViewHolderFactory(new ContactDefaultFactory());
    emptyView = viewBinding.emptyLayout;
    return viewBinding.getRoot();
  }

  protected void loadTitle() {
    if (contactConfig.showTitleBar) {
      viewBinding.contactLayout.getTitleBar().setVisibility(View.VISIBLE);
      if (contactConfig.titleColor != null) {
        viewBinding.contactLayout.getTitleBar().setTitleColor(contactConfig.titleColor);
      }
      if (contactConfig.title != null) {
        viewBinding.contactLayout.getTitleBar().setTitle(contactConfig.title);
      } else {
        viewBinding
            .contactLayout
            .getTitleBar()
            .setTitle(getResources().getString(R.string.contact_title));
      }

    } else {
      viewBinding.contactLayout.getTitleBar().setVisibility(View.GONE);
    }
    if (contactConfig.showTitleBarRight2Icon) {
      viewBinding.contactLayout.getTitleBar().showRight2ImageView(true);
      if (contactConfig.titleBarRight2Res != null) {
        viewBinding.contactLayout.getTitleBar().setRight2ImageRes(contactConfig.titleBarRight2Res);
      }

      viewBinding
          .contactLayout
          .getTitleBar()
          .setRight2ImageClick(
              v -> {
                if (contactConfig.titleBarRight2Click != null) {
                  contactConfig.titleBarRight2Click.onClick(v);
                } else {
                  XKitRouter.withKey(RouterConstant.PATH_GLOBAL_SEARCH_PAGE)
                      .withContext(requireContext())
                      .navigate();
                }
              });

    } else {
      viewBinding.contactLayout.getTitleBar().showRight2ImageView(false);
    }

    if (contactConfig.showTitleBarRightIcon) {
      viewBinding.contactLayout.getTitleBar().showRightImageView(true);
      if (contactConfig.titleBarRightRes != null) {
        viewBinding.contactLayout.getTitleBar().setRightImageRes(contactConfig.titleBarRightRes);
      }
      if (contactConfig.titleBarRightClick != null) {
        viewBinding
            .contactLayout
            .getTitleBar()
            .setRightImageClick(contactConfig.titleBarRightClick);
      } else {
        viewBinding
            .contactLayout
            .getTitleBar()
            .setRightImageClick(
                v ->
                    XKitRouter.withKey(PATH_ADD_FRIEND_PAGE)
                        .withContext(requireContext())
                        .navigate());
      }

    } else {
      viewBinding.contactLayout.getTitleBar().showRightImageView(false);
    }
  }

  @Override
  protected List<ContactEntranceBean> getContactEntranceList(Context context) {
    List<ContactEntranceBean> contactDataList = new ArrayList<>();
    //verify message
    verifyBean =
        new ContactEntranceBean(
            R.mipmap.ic_contact_verfiy_msg, context.getString(R.string.contact_list_verify_msg));
    verifyBean.router =
        IMKitConfigCenter.getEnableTeamJoinAgreeModelAuth() && IMKitConfigCenter.getEnableTeam()
            ? RouterConstant.PATH_MY_NOTIFICATION_PAGE
            : RouterConstant.PATH_FRIEND_NOTIFICATION_PAGE;
    //black list
    ContactEntranceBean blackBean =
        new ContactEntranceBean(
            R.mipmap.ic_contact_black_list, context.getString(R.string.contact_list_black_list));
    blackBean.router = RouterConstant.PATH_MY_BLACK_PAGE;

    contactDataList.add(verifyBean);
    contactDataList.add(blackBean);
    // my group
    if (IMKitConfigCenter.getEnableTeam()) {
      ContactEntranceBean groupBean =
          new ContactEntranceBean(
              R.mipmap.ic_contact_my_group, context.getString(R.string.contact_list_my_group));
      groupBean.router = RouterConstant.PATH_MY_TEAM_PAGE;
      contactDataList.add(groupBean);
    }
    if (IMKitConfigCenter.getEnableAIUser()) {
      ContactEntranceBean aiUserBean =
          new ContactEntranceBean(
              R.mipmap.ic_contact_ai_user, context.getString(R.string.contact_ai_user_title));
      aiUserBean.router = RouterConstant.PATH_MY_AI_USER_PAGE;
      contactDataList.add(aiUserBean);
    }

    return contactDataList;
  }

  @Override
  protected ContactEntranceBean configVerifyBean() {
    return verifyBean;
  }
}
