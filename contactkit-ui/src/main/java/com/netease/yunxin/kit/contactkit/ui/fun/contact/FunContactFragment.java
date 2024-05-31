// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.contact;

import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.PATH_FUN_ADD_FRIEND_PAGE;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.contact.BaseContactFragment;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunContactFragmentBinding;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunContactTopSearchViewBinding;
import com.netease.yunxin.kit.contactkit.ui.interfaces.ContactActions;
import com.netease.yunxin.kit.contactkit.ui.model.ContactEntranceBean;
import com.netease.yunxin.kit.contactkit.ui.model.ContactFriendBean;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;
import java.util.List;

/** contact page */
public class FunContactFragment extends BaseContactFragment {
  private final String TAG = "FunContactFragment";
  private FunContactFragmentBinding viewBinding;
  private ContactEntranceBean verifyBean;

  @Override
  protected View initViewAndGetRootView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    viewBinding = FunContactFragmentBinding.inflate(inflater, container, false);
    contactLayout = viewBinding.contactLayout;
    contactLayout
        .getContactListView()
        .getDecoration()
        .setTitleAlignBottom(true)
        .setShowTagOff(false)
        .setIndexDecorationBg(getResources().getColor(R.color.title_transfer))
        .setColorTitleBottomLine(getResources().getColor(R.color.title_transfer));
    contactLayout.getContactListView().setViewHolderFactory(new FunContactDefaultFactory());
    contactLayout
        .getContactListView()
        .configIndexTextBGColor(getResources().getColor(R.color.color_58be6b));
    contactLayout.getDivideLineForTitle().setVisibility(View.GONE);
    addTopView(inflater, contactLayout.getBodyTopLayout());
    emptyView = viewBinding.emptyLayout;
    return viewBinding.getRoot();
  }

  private void addTopView(LayoutInflater inflater, ViewGroup topGroup) {
    topGroup.setBackgroundResource(R.color.color_ededed);
    FunContactTopSearchViewBinding topSearchViewBinding =
        FunContactTopSearchViewBinding.inflate(inflater, topGroup, true);
    topSearchViewBinding.searchLayout.setOnClickListener(
        v -> {
          if (contactConfig.titleBarRight2Click != null) {
            contactConfig.titleBarRight2Click.onClick(v);
          } else {
            XKitRouter.withKey(RouterConstant.PATH_FUN_GLOBAL_SEARCH_PAGE)
                .withContext(requireContext())
                .navigate();
          }
        });
  }

  @Override
  protected void loadDefaultContactAction(ContactActions actions) {
    actions.addContactListener(
        IViewTypeConstant.CONTACT_FRIEND,
        (position, data) -> {
          UserWithFriend friendInfo = ((ContactFriendBean) data).data;
          XKitRouter.withKey(RouterConstant.PATH_FUN_USER_INFO_PAGE)
              .withContext(requireContext())
              .withParam(RouterConstant.KEY_ACCOUNT_ID_KEY, friendInfo.getAccount())
              .navigate();
        });
  }

  protected void loadTitle() {
    if (contactConfig.showTitleBar) {
      viewBinding.contactLayout.getTitleBar().setTitleBgRes(R.color.color_ededed);
      viewBinding.contactLayout.getTitleBar().getTitleTextView().setVisibility(View.GONE);
      viewBinding
          .contactLayout
          .getTitleBar()
          .getCenterTitleTextView()
          .setTextColor(getResources().getColor(R.color.title_color));
      viewBinding.contactLayout.getTitleBar().getCenterTitleTextView().setTextSize(17);
      viewBinding.contactLayout.getTitleBar().setVisibility(View.VISIBLE);
      if (contactConfig.titleColor != null) {
        viewBinding
            .contactLayout
            .getTitleBar()
            .getCenterTitleTextView()
            .setTextColor(contactConfig.titleColor);
      }
      if (contactConfig.title != null) {
        viewBinding
            .contactLayout
            .getTitleBar()
            .getCenterTitleTextView()
            .setText(contactConfig.title);
      } else {
        viewBinding
            .contactLayout
            .getTitleBar()
            .getCenterTitleTextView()
            .setText(getResources().getString(R.string.contact_title));
      }
    } else {
      viewBinding.contactLayout.getTitleBar().setVisibility(View.GONE);
    }

    viewBinding.contactLayout.getTitleBar().showRight2ImageView(false);

    if (contactConfig.showTitleBarRightIcon) {
      viewBinding.contactLayout.getTitleBar().showRightImageView(true);
      if (contactConfig.titleBarRightRes != null) {
        viewBinding.contactLayout.getTitleBar().setRightImageRes(contactConfig.titleBarRightRes);
      } else {
        viewBinding
            .contactLayout
            .getTitleBar()
            .setRightImageRes(R.drawable.fun_ic_contact_add_friend);
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
                    XKitRouter.withKey(PATH_FUN_ADD_FRIEND_PAGE)
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
            R.mipmap.fun_ic_contact_verfiy_msg,
            context.getString(R.string.contact_list_verify_msg));
    verifyBean.router = RouterConstant.PATH_FUN_MY_NOTIFICATION_PAGE;
    verifyBean.showRightArrow = false;
    //black list
    ContactEntranceBean blackBean =
        new ContactEntranceBean(
            R.mipmap.fun_ic_contact_black_list,
            context.getString(R.string.contact_list_black_list));
    blackBean.router = RouterConstant.PATH_FUN_MY_BLACK_PAGE;
    blackBean.showRightArrow = false;

    contactDataList.add(verifyBean);
    contactDataList.add(blackBean);
    // my group
    if (IMKitConfigCenter.getTeamEnable()) {
      ContactEntranceBean groupBean =
          new ContactEntranceBean(
              R.mipmap.fun_ic_contact_my_group, context.getString(R.string.contact_list_my_group));
      groupBean.router = RouterConstant.PATH_FUN_MY_TEAM_PAGE;
      groupBean.showRightArrow = false;
      contactDataList.add(groupBean);
    }
    return contactDataList;
  }

  @Override
  protected ContactEntranceBean configVerifyBean() {
    return verifyBean;
  }
}
