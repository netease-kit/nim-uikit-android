// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.NormalSearchHistoryActivityBinding;
import com.netease.yunxin.kit.chatkit.ui.page.ChatSearchBaseActivity;
import com.netease.yunxin.kit.common.utils.KeyboardUtils;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;
import java.util.List;

public class ChatHistoryActivity extends ChatSearchBaseActivity {

  private static final String TAG = "ChatSearchActivity";
  private NormalSearchHistoryActivityBinding binding;
  private SearchHistoryItemAdapter adapter;

  @Override
  protected void initViewAndSetContentView(@Nullable Bundle savedInstanceState) {
    binding = NormalSearchHistoryActivityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    searchRV = binding.searchRv;
    searchET = binding.searchEt;
    clearIV = binding.clearIv;
    emptyLayout = binding.groupEmtpy;
    loadingView = binding.loadingView;
    messageSearchTitleBar = binding.searchTitleBar;
    defaultView = binding.llSearchItemList;
  }

  @Override
  protected void bindingView() {
    super.bindingView();

    // 设置快捷搜索项点击事件
    binding.gridQuickSearch.setOnItemClickListener(
        (parent, view, position, id) -> {
          SearchItem item = (SearchItem) adapter.getItem(position);
          if (item.resTitle == R.string.message_search_image) {
            if (conversationId != null) {
              Intent intent = new Intent(ChatHistoryActivity.this, ChatSearchImageActivity.class);
              intent.putExtra(RouterConstant.KEY_SESSION_ID, conversationId);
              intent.putExtra(
                  ChatSearchImageActivity.EXTRA_MODE, ChatSearchImageActivity.MODE_IMAGE);
              startActivity(intent);
            }
          } else if (item.resTitle == R.string.message_search_member) {
            Intent intent = new Intent(ChatHistoryActivity.this, ChatSearchMemberActivity.class);
            intent.putExtra(RouterConstant.CHAT_ID_KRY, accountId);
            startActivity(intent);
          } else if (item.resTitle == R.string.message_search_date) {
            Intent intent = new Intent(ChatHistoryActivity.this, ChatSearchDateActivity.class);
            intent.putExtra(RouterConstant.KEY_SESSION_ID, conversationId);
            startActivity(intent);
          } else if (item.resTitle == R.string.message_search_video) {
            if (conversationId != null) {
              Intent intent = new Intent(ChatHistoryActivity.this, ChatSearchImageActivity.class);
              intent.putExtra(RouterConstant.KEY_SESSION_ID, conversationId);
              intent.putExtra(
                  ChatSearchImageActivity.EXTRA_MODE, ChatSearchImageActivity.MODE_VIDEO);
              startActivity(intent);
            }
          } else if (item.resTitle == R.string.message_search_file) {
            if (conversationId != null) {
              Intent intent = new Intent(ChatHistoryActivity.this, ChatSearchFileActivity.class);
              intent.putExtra(RouterConstant.KEY_SESSION_ID, conversationId);
              startActivity(intent);
            }
          }
        });
  }

  @Override
  protected void clickMessage(IMMessageInfo messageInfo) {
    KeyboardUtils.hideKeyboard(ChatHistoryActivity.this);
    String routerPath = RouterConstant.PATH_CHAT_TEAM_PAGE;
    if (conversationType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      routerPath = RouterConstant.PATH_CHAT_P2P_PAGE;
    }
    XKitRouter.withKey(routerPath)
        .withParam(RouterConstant.KEY_MESSAGE_INFO, messageInfo)
        .withParam(RouterConstant.CHAT_ID_KRY, accountId)
        .withContext(ChatHistoryActivity.this)
        .navigate();
    finish();
  }

  @Override
  protected void initData() {
    super.initData();
    adapter = new SearchHistoryItemAdapter(generateSearchItems());
    binding.gridQuickSearch.setAdapter(adapter);
  }

  private List<SearchItem> generateSearchItems() {
    List<SearchItem> items = new ArrayList<>();
    if (conversationType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM) {
      items.add(
          new SearchItem() {
            {
              resTitle = R.string.message_search_member;
              resIcon = R.drawable.ic_chat_search_member;
            }
          });
    }
    items.add(
        new SearchItem() {
          {
            resTitle = R.string.message_search_image;
            resIcon = R.drawable.ic_chat_search_image;
          }
        });
    items.add(
        new SearchItem() {
          {
            resTitle = R.string.message_search_video;
            resIcon = R.drawable.ic_chat_search_video;
          }
        });
    items.add(
        new SearchItem() {
          {
            resTitle = R.string.message_search_date;
            resIcon = R.drawable.ic_chat_search_date;
          }
        });
    items.add(
        new SearchItem() {
          {
            resTitle = R.string.message_search_file;
            resIcon = R.drawable.ic_chat_search_file;
          }
        });
    return items;
  }

  public class SearchHistoryItemAdapter extends BaseAdapter {
    private final List<SearchItem> items;

    public SearchHistoryItemAdapter(List<SearchItem> items) {
      super();
      this.items = items;
    }

    @Override
    public int getCount() {
      return items.size();
    }

    @Override
    public Object getItem(int position) {
      return items.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      SearchItem item = items.get(position);
      if (convertView == null) {
        convertView =
            LayoutInflater.from(getBaseContext())
                .inflate(R.layout.normal_search_history_item, parent, false);
      }
      ImageView ivIcon = convertView.findViewById(R.id.ivIcon);
      TextView tvTitle = convertView.findViewById(R.id.tvTitle);
      ivIcon.setImageResource(item.resIcon);
      tvTitle.setText(item.resTitle);
      return convertView;
    }
  }

  public class SearchItem {
    public int resTitle;
    public int resIcon;
  }
}
