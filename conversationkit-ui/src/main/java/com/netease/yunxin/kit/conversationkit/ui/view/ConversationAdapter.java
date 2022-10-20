// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.view;

import static com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant.LIB_TAG;

import android.text.TextUtils;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.friend.model.MuteListChangedNotify;
import com.netease.nimlib.sdk.team.constant.TeamMessageNotifyTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.common.ui.viewholder.ViewHolderClickListener;
import com.netease.yunxin.kit.conversationkit.model.ConversationInfo;
import com.netease.yunxin.kit.conversationkit.ui.IConversationFactory;
import com.netease.yunxin.kit.conversationkit.ui.common.DataUtils;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.ui.page.DefaultViewHolderFactory;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** conversation adapter */
public class ConversationAdapter extends RecyclerView.Adapter<BaseViewHolder> {

  private final String TAG = "ConversationAdapter";
  private IConversationFactory viewHolderFactory = new DefaultViewHolderFactory();
  private final List<ConversationBean> conversationList = new ArrayList<>();
  private Comparator<ConversationInfo> dataComparator;
  private ViewHolderClickListener clickListener;

  /** set data and clear conversationList */
  public void setData(List<ConversationBean> data) {
    conversationList.clear();
    if (data != null) {
      conversationList.addAll(data);
      notifyDataSetChanged();
    }
  }

  /** add data to list forward */
  public void addForwardData(List<ConversationBean> data) {
    if (data != null) {
      conversationList.addAll(0, data);
    }
  }

  /** append data to list */
  public void appendData(List<ConversationBean> data) {
    if (data != null) {
      conversationList.addAll(data);
    }
  }

  public void update(List<ConversationBean> data) {
    for (int i = 0; data != null && i < data.size(); i++) {
      update(data.get(i));
    }
  }

  public void update(ConversationBean data) {
    ALog.d(LIB_TAG, TAG, "update" + data.infoData.getContactId());
    int removeIndex = -1;
    for (int j = 0; j < conversationList.size(); j++) {
      if (data.equals(conversationList.get(j))) {
        removeIndex = j;
        break;
      }
    }
    ALog.d(LIB_TAG, TAG, "update, removeIndex:" + removeIndex);
    if (removeIndex > -1) {
      conversationList.remove(removeIndex);
      int insertIndex = searchComparatorIndex(data);
      ALog.d(
          LIB_TAG,
          TAG,
          "update, insertIndex:" + insertIndex + "unread:" + data.infoData.getUnreadCount());
      conversationList.add(insertIndex, data);
      notifyItemMoved(removeIndex, insertIndex);
      notifyItemChanged(insertIndex);
    } else {
      int insertIndex = searchComparatorIndex(data);
      conversationList.add(insertIndex, data);
      notifyItemInserted(insertIndex);
    }
  }

  public void updateUserInfo(List<UserInfo> data) {
    Map<String, UserInfo> accountMap = DataUtils.getUserInfoMap(data);
    if (accountMap != null) {
      for (int i = 0; i < conversationList.size(); i++) {
        UserInfo info = conversationList.get(i).infoData.getUserInfo();
        if (info != null && accountMap.containsKey(info.getAccount())) {
          conversationList.get(i).infoData.setUserInfo(accountMap.get(info.getAccount()));
          notifyItemChanged(i);
        }
      }
    }
  }

  public void updateFriendInfo(List<FriendInfo> data) {
    Map<String, FriendInfo> accountMap = DataUtils.getFriendInfoMap(data);
    if (accountMap != null) {
      for (int i = 0; i < conversationList.size(); i++) {
        UserInfo info = conversationList.get(i).infoData.getUserInfo();
        if (info != null && accountMap.containsKey(info.getAccount())) {
          conversationList.get(i).infoData.setFriendInfo(accountMap.get(info.getAccount()));
          notifyItemChanged(i);
        }
      }
    }
  }

  public void updateTeamInfo(List<Team> data) {
    Map<String, Team> accountMap = DataUtils.getTeamInfoMap(data);
    if (accountMap != null) {
      for (int i = 0; i < conversationList.size(); i++) {
        ConversationInfo info = conversationList.get(i).infoData;
        if (info != null
            && info.getTeamInfo() != null
            && accountMap.containsKey(info.getTeamInfo().getId())) {
          Team team = accountMap.get(info.getTeamInfo().getId());
          info.setTeamInfo(team);
          if (team != null && team.getMessageNotifyType() != null) {
            info.setMute(team.getMessageNotifyType() == TeamMessageNotifyTypeEnum.Mute);
          }
          notifyItemChanged(i);
        }
      }
    }
  }

  public void updateMuteInfo(MuteListChangedNotify data) {
    if (data != null) {
      for (int i = 0; i < conversationList.size(); i++) {
        String contactId = conversationList.get(i).infoData.getContactId();
        if (TextUtils.equals(contactId, data.getAccount())) {
          conversationList.get(i).infoData.setMute(data.isMute());
          notifyItemChanged(i);
        }
      }
    }
  }

  private int searchComparatorIndex(ConversationBean data) {
    int index = conversationList.size();
    // add stick must be insert 0
    if (data.infoData.isStickTop()) {
      return 0;
    }
    for (int i = 0; i < conversationList.size(); i++) {
      if (dataComparator != null
          && dataComparator.compare(data.infoData, conversationList.get(i).infoData) < 1) {
        index = i;
        break;
      }
    }

    return index;
  }

  public void removeData(List<ConversationBean> dataList) {
    if (dataList == null || dataList.size() < 1) {
      return;
    }
    for (ConversationBean data : dataList) {
      int index = -1;
      for (int j = 0; j < conversationList.size(); j++) {
        if (data.equals(conversationList.get(j))) {
          index = j;
          break;
        }
      }
      if (index > -1) {
        removeData(index);
      }
    }
  }

  public void removeAll() {
    conversationList.clear();
    notifyDataSetChanged();
  }

  public void removeData(String id) {
    int index = -1;
    for (int j = 0; j < conversationList.size(); j++) {
      if (TextUtils.equals(conversationList.get(j).infoData.getContactId(), id)) {
        index = j;
        break;
      }
    }
    if (index > -1) {
      removeData(index);
    }
  }

  public void removeData(int position) {
    if (position >= 0 && position < conversationList.size()) {
      conversationList.remove(position);
      notifyItemRemoved(position);
    }
  }

  public void addStickTop(String id) {
    int index = -1;
    for (int j = 0; j < conversationList.size(); j++) {
      if (TextUtils.equals(conversationList.get(j).infoData.getContactId(), id)) {
        index = j;
        break;
      }
    }
    if (index > -1) {
      conversationList.get(index).infoData.setStickTop(true);
      ConversationBean data = conversationList.remove(index);
      conversationList.add(0, data);
      notifyItemMoved(index, 0);
      notifyItemChanged(0);
    }
  }

  public void removeStickTop(String id) {
    int index = -1;
    for (int j = 0; j < conversationList.size(); j++) {
      if (TextUtils.equals(conversationList.get(j).infoData.getContactId(), id)) {
        index = j;
        break;
      }
    }
    if (index > -1) {
      ConversationBean data = conversationList.remove(index);
      data.infoData.setStickTop(false);
      int insertIndex = searchComparatorIndex(data);
      conversationList.add(insertIndex, data);
      notifyItemMoved(index, insertIndex);
      notifyItemChanged(insertIndex);
    }
  }

  public void setViewHolderFactory(IConversationFactory factory) {
    this.viewHolderFactory = factory;
  }

  public void setViewHolderClickListener(ViewHolderClickListener listener) {
    this.clickListener = listener;
  }

  public void setComparator(Comparator<ConversationInfo> comparator) {
    this.dataComparator = comparator;
  }

  @NonNull
  @Override
  public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    BaseViewHolder viewHolder = null;
    if (viewHolderFactory != null) {
      viewHolder = viewHolderFactory.createViewHolder(parent, viewType);
    }
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
    holder.onBindData(conversationList.get(position), position);
    holder.setItemOnClickListener(clickListener);
  }

  @Override
  public int getItemViewType(int position) {
    return viewHolderFactory.getItemViewType(conversationList.get(position));
  }

  @Override
  public int getItemCount() {
    return conversationList.size();
  }

  public ConversationBean getData(int index) {
    if (index >= 0 && index < conversationList.size()) {
      return conversationList.get(index);
    }
    return null;
  }
}
