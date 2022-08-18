// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.yunxin.kit.contactkit.ui.IContactFactory;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.ContactListViewBinding;
import com.netease.yunxin.kit.contactkit.ui.indexbar.suspension.SuspensionDecoration;
import com.netease.yunxin.kit.contactkit.ui.interfaces.ContactActions;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactDataChanged;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactListView;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactViewAttrs;
import com.netease.yunxin.kit.contactkit.ui.model.BaseContactBean;
import com.netease.yunxin.kit.contactkit.ui.model.ContactFriendBean;
import com.netease.yunxin.kit.contactkit.ui.utils.XLog;
import com.netease.yunxin.kit.contactkit.ui.view.adapter.ContactAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** list view for show contacts info */
public class ContactListView extends FrameLayout
    implements IContactDataChanged, IContactListView, IContactViewAttrs {
  private final String TAG = "ContactListView";

  private ContactListViewBinding binding;

  private ContactAdapter contactAdapter;

  private SuspensionDecoration decoration;

  private ContactListViewAttrs contactListViewAttrs;

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
    }
  }

  @Override
  public void setContactAction(ContactActions contactActions) {
    if (contactAdapter != null) {
      contactAdapter.setDefaultActions(contactActions);
    }
  }

  @Override
  public ContactAdapter getAdapter() {
    return contactAdapter;
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
          if (!friendMap.containsKey(friendBean.data.getAccount())) {
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
      XLog.d(TAG, "updateFriendData", "contactAdapter:" + result);
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
}
