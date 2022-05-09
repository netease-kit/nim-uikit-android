/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.page;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatSearchMessageActivityBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatSearchBean;
import com.netease.yunxin.kit.chatkit.ui.page.adapter.SearchMessageAdapter;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.SearchMessageViewModel;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.common.ui.viewholder.ViewHolderClickListener;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.KeyboardUtils;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

import java.util.List;

/**
 * History message search page for Team chat
 * search history message and jump back to the team chat page
 */
public class ChatSearchActivity extends BaseActivity {
    private static final String TAG = "ChatSearchActivity";

    private ChatSearchMessageActivityBinding viewBinding;
    private SearchMessageViewModel viewModel;
    private SearchMessageAdapter searchAdapter;
    private Team team;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ChatSearchMessageActivityBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        initView();
        initData();
    }

    private void initView(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        viewBinding.rvSearch.setLayoutManager(layoutManager);
        searchAdapter = new SearchMessageAdapter();
        searchAdapter.setViewHolderClickListener(new ViewHolderClickListener() {
            @Override
            public void onClick(BaseBean data, int position) {
                ALog.i(TAG, "item onClick position:" + position);
                KeyboardUtils.hideKeyboard(ChatSearchActivity.this);
                XKitRouter.withKey(data.router).withParam(data.paramKey, data.param).withParam(RouterConstant.CHAT_KRY,team)
                        .withContext(ChatSearchActivity.this).navigate();
            }

            @Override
            public boolean onLongClick(BaseBean data, int position) {
                return false;
            }
        });
        viewBinding.rvSearch.setAdapter(searchAdapter);
        viewBinding.messageSearchTitleBar.setOnBackIconClickListener( v -> onBackPressed());
        viewBinding.ivClear.setOnClickListener( v -> viewBinding.etSearch.setText(""));
        viewBinding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.searchMessage(String.valueOf(s),SessionTypeEnum.Team,team.getId());
                if (TextUtils.isEmpty(String.valueOf(s))) {
                    viewBinding.ivClear.setVisibility(View.GONE);
                } else {
                    viewBinding.ivClear.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void initData(){
        viewModel = new ViewModelProvider(this).get(SearchMessageViewModel.class);
        team = (Team) getIntent().getSerializableExtra(RouterConstant.CHAT_KRY);

        if (team == null){
            finish();
        }

        viewModel.getSearchLiveData().observe(this, result -> {
            if (result.getLoadStatus() == LoadStatus.Success){
                if ((result.getData() == null || result.getData().size() < 1)&&
                        !TextUtils.isEmpty(String.valueOf(viewBinding.etSearch.getEditableText()))){
                    showEmpty(true);
                }else {
                    showEmpty(false);
                }
                searchAdapter.setData(result.getData());
            }
        });
    }

    private void showEmpty(boolean show){
        if (show){
            viewBinding.emptyLl.setVisibility(View.VISIBLE);
            viewBinding.rvSearch.setVisibility(View.GONE);
        }else {
            viewBinding.emptyLl.setVisibility(View.GONE);
            viewBinding.rvSearch.setVisibility(View.VISIBLE);
        }
    }
}
