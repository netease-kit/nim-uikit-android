// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.verify;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.yunxin.kit.contactkit.ui.ILoadListener;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.BaseListActivityLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.fun.view.FunContactViewHolderFactory;
import com.netease.yunxin.kit.contactkit.ui.fun.view.viewholder.FunVerifyInfoViewHolder;
import com.netease.yunxin.kit.contactkit.ui.model.ContactVerifyInfoBean;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.contactkit.ui.verify.BaseVerifyListActivity;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;
import com.netease.yunxin.kit.corekit.im.model.SystemMessageInfoStatus;
import com.netease.yunxin.kit.corekit.im.model.SystemMessageInfoType;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

public class FunVerifyListActivity extends BaseVerifyListActivity implements ILoadListener {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_ededed);
  }

  protected void configTitle(BaseListActivityLayoutBinding binding) {
    super.configTitle(binding);
    binding.title.getTitleTextView().setTextSize(17);
    binding.title.getTitleTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
    binding.title.setBackgroundResource(R.color.color_ededed);
  }

  protected int getEmptyStateViewRes() {
    return R.drawable.fun_ic_contact_empty;
  }

  @Override
  protected void configViewHolderFactory() {
    binding.contactListView.setViewHolderFactory(
        new FunContactViewHolderFactory() {
          @Override
          protected BaseContactViewHolder getCustomViewHolder(ViewGroup view, int viewType) {
            if (viewType == IViewTypeConstant.CONTACT_VERIFY_INFO) {
              FunVerifyInfoViewHolder viewHolder = new FunVerifyInfoViewHolder(view);
              viewHolder.setVerifyListener(
                  new FunVerifyInfoViewHolder.VerifyListener() {
                    @Override
                    public void onAccept(ContactVerifyInfoBean bean) {
                      viewModel.agree(
                          bean,
                          new FetchCallback<Void>() {
                            @Override
                            public void onSuccess(@Nullable Void param) {
                              viewModel.setVerifyStatus(bean, SystemMessageInfoStatus.Passed);
                              binding.contactListView.updateContactData(bean);
                              if (bean.data.getInfoType() == SystemMessageInfoType.AddFriend) {
                                XKitRouter.withKey(RouterConstant.PATH_CHAT_SEND_TEXT_ACTION)
                                    .withContext(FunVerifyListActivity.this)
                                    .withParam(
                                        RouterConstant.KEY_SESSION_ID, bean.data.getFromAccount())
                                    .withParam(
                                        RouterConstant.KEY_SESSION_TYPE,
                                        SessionTypeEnum.P2P.getValue())
                                    .withParam(
                                        RouterConstant.KEY_MESSAGE_CONTENT,
                                        getResources()
                                            .getString(R.string.verify_agree_message_text))
                                    .navigate();
                              }
                            }

                            @Override
                            public void onFailed(int code) {
                              if (code == error_duplicate) {
                                viewModel.setVerifyStatus(bean, SystemMessageInfoStatus.Passed);
                                binding.contactListView.updateContactData(bean);
                              }
                              toastResult(true, bean.data.getInfoType(), code);
                            }

                            @Override
                            public void onException(@Nullable Throwable exception) {
                              toastResult(true, bean.data.getInfoType(), 0);
                            }
                          });
                    }

                    @Override
                    public void onReject(ContactVerifyInfoBean bean) {
                      viewModel.disagree(
                          bean,
                          new FetchCallback<Void>() {
                            @Override
                            public void onSuccess(@Nullable Void param) {
                              viewModel.setVerifyStatus(bean, SystemMessageInfoStatus.Declined);
                              binding.contactListView.updateContactData(bean);
                            }

                            @Override
                            public void onFailed(int code) {
                              if (code == error_duplicate) {
                                viewModel.setVerifyStatus(bean, SystemMessageInfoStatus.Passed);
                                binding.contactListView.updateContactData(bean);
                              }
                              toastResult(false, bean.data.getInfoType(), code);
                            }

                            @Override
                            public void onException(@Nullable Throwable exception) {
                              toastResult(false, bean.data.getInfoType(), 0);
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
}
