// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.IChatDefaultFactory;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunSearchHistoryActivityBinding;
import com.netease.yunxin.kit.chatkit.ui.fun.factory.FunChatViewHolderFactory;
import com.netease.yunxin.kit.chatkit.ui.page.ChatSearchBaseActivity;
import com.netease.yunxin.kit.common.utils.KeyboardUtils;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;
import java.util.List;

public class FunChatSearchHistoryActivity extends ChatSearchBaseActivity {

  private static final String TAG = "FunChatSearchHistoryActivity";
  private FunSearchHistoryActivityBinding binding;
  private SearchHistoryItemAdapter adapter;

  @Override
  protected void initViewAndSetContentView(@Nullable Bundle savedInstanceState) {
    changeStatusBarColor(R.color.fun_chat_secondary_page_bg_color);
    binding = FunSearchHistoryActivityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    searchRV = binding.searchRv;
    searchET = binding.searchEt;
    clearIV = binding.clearIv;
    emptyLayout = binding.emptyLayout;
    loadingView = binding.loadingView;
    defaultView = binding.llSearchItemList;
    binding.cancelBtn.setOnClickListener(view -> finish());
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
              Intent intent =
                  new Intent(FunChatSearchHistoryActivity.this, FunChatSearchImageActivity.class);
              intent.putExtra(RouterConstant.KEY_SESSION_ID, conversationId);
              intent.putExtra(
                  FunChatSearchImageActivity.EXTRA_MODE, FunChatSearchImageActivity.MODE_IMAGE);
              startActivity(intent);
            }
          } else if (item.resTitle == R.string.message_search_member) {
            Intent intent =
                new Intent(FunChatSearchHistoryActivity.this, FunChatSearchMemberActivity.class);
            intent.putExtra(RouterConstant.CHAT_ID_KRY, accountId);
            startActivity(intent);
          } else if (item.resTitle == R.string.message_search_date) {
            Intent intent =
                new Intent(FunChatSearchHistoryActivity.this, FunChatSearchDateActivity.class);
            intent.putExtra(RouterConstant.KEY_SESSION_ID, conversationId);
            startActivity(intent);
          } else if (item.resTitle == R.string.message_search_video) {
            if (conversationId != null) {
              Intent intent =
                  new Intent(FunChatSearchHistoryActivity.this, FunChatSearchImageActivity.class);
              intent.putExtra(RouterConstant.KEY_SESSION_ID, conversationId);
              intent.putExtra(
                  FunChatSearchImageActivity.EXTRA_MODE, FunChatSearchImageActivity.MODE_VIDEO);
              startActivity(intent);
            }
          } else if (item.resTitle == R.string.message_search_file) {
            if (conversationId != null) {
              Intent intent =
                  new Intent(FunChatSearchHistoryActivity.this, FunChatSearchFileActivity.class);
              intent.putExtra(RouterConstant.KEY_SESSION_ID, conversationId);
              startActivity(intent);
            }
          }
        });
  }

  @Override
  protected void clickMessage(IMMessageInfo messageInfo) {
    KeyboardUtils.hideKeyboard(FunChatSearchHistoryActivity.this);
    String routerPath = RouterConstant.PATH_FUN_CHAT_TEAM_PAGE;
    if (conversationType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      routerPath = RouterConstant.PATH_FUN_CHAT_P2P_PAGE;
    }
    XKitRouter.withKey(routerPath)
        .withParam(RouterConstant.KEY_MESSAGE_INFO, messageInfo)
        .withParam(RouterConstant.CHAT_ID_KRY, accountId)
        .withContext(FunChatSearchHistoryActivity.this)
        .navigate();
    finish();
  }

  @Override
  protected void initData() {
    super.initData();
    adapter = new SearchHistoryItemAdapter(generateSearchItems());
    binding.gridQuickSearch.setAdapter(adapter);
  }

  @Override
  protected IChatDefaultFactory getChatFactory() {
    return FunChatViewHolderFactory.getInstance();
  }

  private List<SearchItem> generateSearchItems() {
    List<SearchItem> items = new ArrayList<>();
    if (conversationType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM) {
      items.add(
          new SearchItem() {
            {
              resTitle = R.string.message_search_member;
            }
          });
    }
    items.add(
        new SearchItem() {
          {
            resTitle = R.string.message_search_image;
          }
        });
    items.add(
        new SearchItem() {
          {
            resTitle = R.string.message_search_video;
          }
        });
    items.add(
        new SearchItem() {
          {
            resTitle = R.string.message_search_date;
          }
        });
    items.add(
        new SearchItem() {
          {
            resTitle = R.string.message_search_file;
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
                .inflate(R.layout.fun_search_history_item, parent, false);
      }
      TextView tvTitle = convertView.findViewById(R.id.tvTitle);
      tvTitle.setText(item.resTitle);
      View vLine = convertView.findViewById(R.id.vLine);
      if (position % 3 < 2 && position < items.size() - 1) {
        vLine.setVisibility(View.VISIBLE);
      } else {
        vLine.setVisibility(View.GONE);
      }
      return convertView;
    }
  }

  public class SearchItem {
    public int resTitle;
  }
}
