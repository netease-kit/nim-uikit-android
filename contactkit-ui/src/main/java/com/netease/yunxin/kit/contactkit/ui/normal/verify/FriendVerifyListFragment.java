// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.verify;

import android.view.ViewGroup;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.friend.enums.V2NIMFriendAddApplicationStatus;
import com.netease.yunxin.kit.contactkit.ui.ILoadListener;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.model.ContactVerifyInfoBean;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.contactkit.ui.normal.view.ContactViewHolderFactory;
import com.netease.yunxin.kit.contactkit.ui.normal.view.viewholder.VerifyInfoViewHolder;
import com.netease.yunxin.kit.contactkit.ui.verify.FriendVerifyBaseFragment;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.model.V2UserInfo;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

public class FriendVerifyListFragment extends FriendVerifyBaseFragment implements ILoadListener {

  @Override
  protected void configViewHolderFactory() {
    layoutBinding.contactListView.setViewHolderFactory(
        new ContactViewHolderFactory() {
          @Override
          protected BaseContactViewHolder getCustomViewHolder(ViewGroup view, int viewType) {
            if (viewType == IViewTypeConstant.CONTACT_VERIFY_INFO) {
              VerifyInfoViewHolder viewHolder = new VerifyInfoViewHolder(view);
              viewHolder.setVerifyListener(
                  new VerifyInfoViewHolder.VerifyListener() {
                    @Override
                    public void onAccept(ContactVerifyInfoBean bean) {
                      viewModel.agree(
                          bean,
                          new FetchCallback<Void>() {
                            @Override
                            public void onError(int errorCode, @Nullable String errorMsg) {
                              if (errorCode == error_duplicate) {
                                bean.data.setOperatorAccountId(IMKitClient.account());
                                bean.data.setOperatorUserInfo(
                                    new V2UserInfo(
                                        IMKitClient.account(), IMKitClient.currentUser()));
                                viewModel.setVerifyStatus(
                                    bean,
                                    V2NIMFriendAddApplicationStatus
                                        .V2NIM_FRIEND_ADD_APPLICATION_STATUS_AGREED);
                                layoutBinding.contactListView.updateContactData(bean);
                              }
                              toastResult(true, errorCode);
                            }

                            @Override
                            public void onSuccess(@Nullable Void param) {
                              bean.data.setOperatorAccountId(IMKitClient.account());
                              bean.data.setOperatorUserInfo(
                                  new V2UserInfo(IMKitClient.account(), IMKitClient.currentUser()));
                              viewModel.setVerifyStatus(
                                  bean,
                                  V2NIMFriendAddApplicationStatus
                                      .V2NIM_FRIEND_ADD_APPLICATION_STATUS_AGREED);
                              layoutBinding.contactListView.updateVerifyDate(bean);
                              //
                              XKitRouter.withKey(RouterConstant.PATH_CHAT_SEND_TEXT_ACTION)
                                  .withContext(FriendVerifyListFragment.this.requireContext())
                                  .withParam(
                                      RouterConstant.KEY_SESSION_ID,
                                      bean.data.getApplicantAccountId())
                                  .withParam(
                                      RouterConstant.KEY_SESSION_TYPE,
                                      V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P.getValue())
                                  .withParam(
                                      RouterConstant.KEY_MESSAGE_CONTENT,
                                      getResources().getString(R.string.verify_agree_message_text))
                                  .navigate();
                              //
                            }
                          });
                    }

                    @Override
                    public void onReject(ContactVerifyInfoBean bean) {
                      viewModel.disagree(
                          bean,
                          new FetchCallback<Void>() {
                            @Override
                            public void onError(int errorCode, @Nullable String errorMsg) {
                              if (errorCode == error_duplicate) {
                                bean.data.setOperatorAccountId(IMKitClient.account());
                                bean.data.setOperatorUserInfo(
                                    new V2UserInfo(
                                        IMKitClient.account(), IMKitClient.currentUser()));
                                viewModel.setVerifyStatus(
                                    bean,
                                    V2NIMFriendAddApplicationStatus
                                        .V2NIM_FRIEND_ADD_APPLICATION_STATUS_AGREED);
                                layoutBinding.contactListView.updateContactData(bean);
                              }
                              toastResult(false, errorCode);
                            }

                            @Override
                            public void onSuccess(@Nullable Void param) {
                              bean.data.setOperatorAccountId(IMKitClient.account());
                              bean.data.setOperatorUserInfo(
                                  new V2UserInfo(IMKitClient.account(), IMKitClient.currentUser()));
                              viewModel.setVerifyStatus(
                                  bean,
                                  V2NIMFriendAddApplicationStatus
                                      .V2NIM_FRIEND_ADD_APPLICATION_STATUS_REJECTED);
                              layoutBinding.contactListView.updateContactData(bean);
                            }
                          });
                    }
                  });
              return viewHolder;
            }
            return null;
          }
        });
  }

  protected int getEmptyStateViewRes() {
    return R.drawable.ic_contact_empty;
  }
}
