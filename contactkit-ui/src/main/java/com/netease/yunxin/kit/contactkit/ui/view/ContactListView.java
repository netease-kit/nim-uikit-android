// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.view;

import static com.netease.yunxin.kit.contactkit.ui.ContactConstant.LIB_TAG;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.contactkit.ui.IContactFactory;
import com.netease.yunxin.kit.contactkit.ui.ILoadListener;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.ContactListViewBinding;
import com.netease.yunxin.kit.contactkit.ui.indexbar.suspension.SuspensionDecoration;
import com.netease.yunxin.kit.contactkit.ui.interfaces.ContactActions;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactDataChanged;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactListView;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactViewAttrs;
import com.netease.yunxin.kit.contactkit.ui.model.BaseContactBean;
import com.netease.yunxin.kit.contactkit.ui.model.ContactFriendBean;
import com.netease.yunxin.kit.contactkit.ui.model.ContactVerifyInfoBean;
import com.netease.yunxin.kit.contactkit.ui.view.adapter.ContactAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** list view for show contacts info */
public class ContactListView extends FrameLayout
    implements IContactDataChanged, IContactListView, IContactViewAttrs {
  private final String TAG = "ContactListView";

  public ContactListViewBinding binding;

  private ContactAdapter contactAdapter;

  private SuspensionDecoration decoration;

  private ILoadListener loadMoreListener;

  private ContactListViewAttrs contactListViewAttrs;

  private final int LOAD_MORE_DIFF = 5;

  public ContactListView(Context context) {
    super(context);
    init(null);
  }

  public ContactListView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
  }

  public ContactListView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs);
  }

  private void init(AttributeSet attrs) {
    LayoutInflater layoutInflater = LayoutInflater.from(getContext());
    binding = ContactListViewBinding.inflate(layoutInflater, this, true);
    contactListViewAttrs = new ContactListViewAttrs();
    contactListViewAttrs.parseAttrs(getContext(), attrs);
    initRecyclerView();
  }

  private void initRecyclerView() {
    decoration = new SuspensionDecoration(getContext(), new ArrayList<>());
    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
    contactAdapter = new ContactAdapter();
    binding.contactList.setLayoutManager(layoutManager);
    binding.contactList.addItemDecoration(
        decoration.setPaddingLeft(getContext().getResources().getDimension(R.dimen.dimen_20_dp)));
    binding.contactList.setAdapter(contactAdapter);
    binding.indexBar.setLayoutManager(layoutManager);
    binding.contactList.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
          @Override
          public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
              int position = layoutManager.findLastVisibleItemPosition();
              if (loadMoreListener != null
                  && loadMoreListener.hasMore()
                  && contactAdapter.getItemCount() < position + LOAD_MORE_DIFF
                  && contactAdapter.getItemCount() > 0) {
                BaseContactBean last =
                    contactAdapter.getDataList().get(contactAdapter.getItemCount() - 1);
                loadMoreListener.loadMore(last);
              }
            }
          }

          @Override
          public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
          }
        });
    loadAttar();
  }

  @Override
  public void setViewHolderFactory(IContactFactory viewHolderFactory) {
    if (contactAdapter != null) {
      contactAdapter.setViewHolderFactory(viewHolderFactory);
    }
  }

  @Override
  public void setViewConfig(ContactListViewAttrs attrs) {
    contactListViewAttrs = attrs;
    loadAttar();
  }

  private void loadAttar() {
    if (contactListViewAttrs.getShowIndexBar() != null) {
      binding.indexBar.setVisibility(contactListViewAttrs.getShowIndexBar() ? VISIBLE : GONE);
    }
    if (contactAdapter != null) {
      contactAdapter.setContactListViewAttrs(contactListViewAttrs);
    }
    if (contactListViewAttrs.getIndexTextSize() != ContactListViewAttrs.INT_NULL) {
      decoration.setTitleFontSize(contactListViewAttrs.getIndexTextSize());
    }
    if (contactListViewAttrs.getIndexTextColor() != ContactListViewAttrs.INT_NULL) {
      decoration.setColorTitleFont(contactListViewAttrs.getIndexTextColor());
    }
    if (contactListViewAttrs.getDivideLineColor() != ContactListViewAttrs.INT_NULL) {
      decoration.setColorTitleBottomLine(contactListViewAttrs.getDivideLineColor());
    }
  }

  @Override
  public void setTitleColor(@ColorInt int color) {
    if (contactAdapter != null) {
      contactAdapter.getContactListViewAttrs().setNameTextColor(color);
    }
  }

  @Override
  public void showIndexBar(boolean show) {
    if (show) {
      binding.indexBar.setVisibility(VISIBLE);
    } else {
      binding.indexBar.setVisibility(GONE);
    }
  }

  @Override
  public void showSelector(boolean show) {
    if (contactAdapter != null) {
      contactAdapter.getContactListViewAttrs().setShowSelector(show);
      contactAdapter.updateFriendData();
    }
  }

  @Override
  public void setContactAction(ContactActions contactActions) {
    if (contactAdapter != null) {
      contactAdapter.setDefaultActions(contactActions);
    }
  }

  public void setLoadMoreListener(ILoadListener listener) {
    this.loadMoreListener = listener;
  }

  @Override
  public ContactAdapter getAdapter() {
    return contactAdapter;
  }

  public List<BaseContactBean> getDataList() {
    if (contactAdapter != null) {
      return contactAdapter.getDataList();
    }
    return null;
  }

  @Override
  public void onFriendDataSourceChanged(List<ContactFriendBean> contactItemBeanList) {
    if (contactAdapter != null) {
      binding
          .indexBar
          .setSourceDataAlreadySorted(false)
          .setSourceData(contactItemBeanList)
          .invalidate();
      contactAdapter.updateFriendData(contactItemBeanList);
      decoration.setData(contactAdapter.getDataList());
    }
  }

  public void scrollToPosition(int index) {
    binding.contactList.scrollToPosition(index);
  }

  @Override
  public void addFriendData(List<ContactFriendBean> friend) {
    if (contactAdapter != null && friend != null) {
      List<ContactFriendBean> friendList = contactAdapter.getFriendList();
      Map<String, ContactFriendBean> friendMap = new HashMap<>();
      if (friendList != null) {
        for (ContactFriendBean friendBean : friendList) {
          if (friendBean != null && friendBean.data != null) {
            friendMap.put(friendBean.data.getAccount(), friendBean);
          }
        }
      }

      for (ContactFriendBean friendBean : friend) {
        if (friendBean != null && friendBean.data != null) {
          if (friendMap.containsKey(friendBean.data.getAccount())) {
            contactAdapter.getFriendList().remove(friendMap.get(friendBean.data.getAccount()));
          }
          contactAdapter.getFriendList().add(friendBean);
        }
      }
      binding
          .indexBar
          .setSourceDataAlreadySorted(false)
          .setSourceData(contactAdapter.getFriendList())
          .invalidate();
      contactAdapter.updateFriendData();
      decoration.setData(contactAdapter.getDataList());
    }
  }

  @Override
  public void removeFriendData(List<ContactFriendBean> friend) {
    if (contactAdapter != null) {
      contactAdapter.getFriendList().removeAll(friend);
      binding
          .indexBar
          .setSourceDataAlreadySorted(false)
          .setSourceData(contactAdapter.getFriendList())
          .invalidate();
      contactAdapter.updateFriendData();
      decoration.setData(contactAdapter.getDataList());
    }
  }

  @Override
  public void updateFriendData(List<ContactFriendBean> friends) {
    if (contactAdapter != null) {
      boolean result = contactAdapter.getFriendList().removeAll(friends);
      ALog.d(LIB_TAG, TAG, "updateFriendData, contactAdapter:" + result);
      contactAdapter.getFriendList().addAll(friends);
      binding
          .indexBar
          .setSourceDataAlreadySorted(false)
          .setSourceData(contactAdapter.getFriendList())
          .invalidate();
      contactAdapter.updateFriendData();
      decoration.setData(contactAdapter.getDataList());
    }
  }

  @Override
  public void updateContactData(BaseContactBean data) {
    if (contactAdapter != null) {
      contactAdapter.updateData(data);
      if (data.isNeedToPinyin()) {
        binding.indexBar.setSourceData(contactAdapter.getDataList()).invalidate();
      }
      decoration.setData(contactAdapter.getDataList());
    }
  }

  public void updateVerifyDate(ContactVerifyInfoBean data) {
    if (contactAdapter != null) {
      contactAdapter.updateDataAndSort(data);
      if (data.isNeedToPinyin()) {
        binding.indexBar.setSourceData(contactAdapter.getDataList()).invalidate();
      }
      decoration.setData(contactAdapter.getDataList());
    }
  }

  public int getItemCount() {
    if (contactAdapter != null) {
      return contactAdapter.getItemCount();
    }
    return 0;
  }

  @Override
  public void updateContactData(int viewType, List<? extends BaseContactBean> data) {
    if (contactAdapter != null) {
      contactAdapter.updateData(viewType, data);
      decoration.setData(contactAdapter.getDataList());
    }
  }

  @Override
  public void addContactData(BaseContactBean contactData) {
    if (contactAdapter != null) {
      contactAdapter.addData(contactData);
      decoration.setData(contactAdapter.getDataList());
    }
  }

  @Override
  public void addContactData(List<? extends BaseContactBean> contactData) {
    if (contactAdapter != null) {
      contactAdapter.addListData(contactData);
      decoration.setData(contactAdapter.getDataList());
    }
  }

  public void addForwardContactData(List<? extends BaseContactBean> contactData) {
    if (contactAdapter != null) {
      contactAdapter.addForwardListData(contactData);
      decoration.setData(contactAdapter.getDataList());
    }
  }

  @Override
  public void removeContactData(BaseContactBean contactData) {
    if (contactAdapter != null) {
      contactAdapter.removeData(contactData);
      decoration.setData(contactAdapter.getDataList());
    }
  }

  @Override
  public void removeContactData(List<? extends BaseContactBean> contactData) {
    if (contactAdapter != null) {
      contactAdapter.removeListData(contactData);
      decoration.setData(contactAdapter.getDataList());
    }
  }

  @Override
  public void clearContactData() {
    if (contactAdapter != null) {
      contactAdapter.clearData();
    }
  }

  public void setEmptyViewVisible(int visible, String text) {
    binding.contactEmptyView.setVisibility(visible);
    binding.contactEmptyTv.setText(text);
  }

  public void configEmptyViewRes(int res) {
    binding.emptyStateIv.setImageResource(res);
  }

  public void configIndexTextBGColor(int color) {
    binding.indexBar.configIndexTextBGColor(color);
  }

  public SuspensionDecoration getDecoration() {
    return decoration;
  }
}
