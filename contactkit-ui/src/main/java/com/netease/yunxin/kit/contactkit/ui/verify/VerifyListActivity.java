// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.verify;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.ILoadListener;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.activity.BaseListActivity;
import com.netease.yunxin.kit.contactkit.ui.model.ContactVerifyInfoBean;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.contactkit.ui.view.ContactViewHolderFactory;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.VerifyInfoViewHolder;
import com.netease.yunxin.kit.corekit.im.model.SystemMessageInfoStatus;
import com.netease.yunxin.kit.corekit.im.model.SystemMessageInfoType;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.List;

public class VerifyListActivity extends BaseListActivity implements ILoadListener {

  private VerifyViewModel viewModel;
  private boolean hasInit = false;
  private int seriesPageCount = 0;
  private final int seriesPageLimit = 20;
  private final int error_duplicate = 509;

  @Override
  protected void initView() {
    binding
        .title
        .setTitle(R.string.verify_msg)
        .setActionText(R.string.clear_all)
        .setActionListener(v -> viewModel.clearNotify());
    binding.contactListView.setViewHolderFactory(
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
                            public void onSuccess(@Nullable Void param) {
                              viewModel.setVerifyStatus(bean, SystemMessageInfoStatus.Passed);
                              binding.contactListView.updateContactData(bean);
                              if (bean.data.getInfoType() == SystemMessageInfoType.AddFriend) {
                                XKitRouter.withKey(RouterConstant.PATH_CHAT_SEND_TEXT_ACTION)
                                    .withContext(VerifyListActivity.this)
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

    binding.contactListView.setLoadMoreListener(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    updateView();
  }

  @Override
  protected void onStop() {
    super.onStop();
    viewModel.resetUnreadCount();
    binding.contactListView.getAdapter().notifyDataSetChanged();
  }

  @Override
  protected void initData() {
    viewModel = new ViewModelProvider(this).get(VerifyViewModel.class);
    viewModel
        .getFetchResult()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                binding.contactListView.addContactData(result.getData());
                if (result.getData() == null
                    || (result.getData() != null && result.getData().size() < 10)) {
                  if (viewModel.hasMore() && seriesPageCount < seriesPageLimit) {
                    seriesPageCount += result.getData() != null ? result.getData().size() : 0;
                    viewModel.fetchVerifyList(true);
                  }
                  seriesPageCount = 0;
                }
                seriesPageCount = 0;

              } else if (result.getLoadStatus() == LoadStatus.Finish) {
                if (result.getType() == FetchResult.FetchType.Remove) {
                  binding.contactListView.removeContactData(result.getData());
                } else if (result.getType() == FetchResult.FetchType.Add) {
                  addNotifyData(result.getData());
                } else if (result.getType() == FetchResult.FetchType.Update) {
                  for (ContactVerifyInfoBean bean : result.getData()) {
                    binding.contactListView.updateContactDataAndSort(bean);
                  }
                }
              }
              hasInit = true;
              updateView();
            });

    viewModel.fetchVerifyList(false);
  }

  private void updateView() {
    if (binding.contactListView.getItemCount() > 0 || !hasInit) {
      binding.contactListView.setEmptyViewVisible(View.GONE, null);
    } else {
      binding.contactListView.setEmptyViewVisible(
          View.VISIBLE, getString(R.string.verify_empty_text));
    }
  }

  private void toastResult(boolean agree, SystemMessageInfoType type, int errorCode) {
    String content = null;
    if (errorCode == error_duplicate) {
      content = getResources().getString(R.string.verify_duplicate_fail);
    } else if (type == SystemMessageInfoType.AddFriend) {
      content =
          agree
              ? getResources().getString(R.string.agree_add_friend_fail)
              : getResources().getString(R.string.disagree_add_friend_fail);
    } else if ((type == SystemMessageInfoType.ApplyJoinTeam)) {
      content =
          agree
              ? getResources().getString(R.string.agree_apply_join_team_fail)
              : getResources().getString(R.string.disagree_apply_join_team_fail);
    } else if ((type == SystemMessageInfoType.TeamInvite)) {
      content =
          agree
              ? getResources().getString(R.string.agree_invite_team_fail)
              : getResources().getString(R.string.disagree_invite_team_fail);
    }
    Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
  }

  private void addNotifyData(List<ContactVerifyInfoBean> addList) {
    if (addList == null || addList.size() < 1) {
      return;
    }
    binding.contactListView.addForwardContactData(addList);
  }

  @Override
  public boolean hasMore() {
    return viewModel.hasMore();
  }

  @Override
  public void loadMore(Object last) {
    viewModel.fetchVerifyList(true);
  }
}
