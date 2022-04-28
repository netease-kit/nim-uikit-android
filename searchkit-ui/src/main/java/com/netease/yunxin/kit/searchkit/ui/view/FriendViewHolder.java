/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.searchkit.ui.view;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import androidx.annotation.NonNull;

import com.netease.nimlib.sdk.search.model.RecordHitInfo;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.searchkit.model.FriendSearchInfo;
import com.netease.yunxin.kit.searchkit.model.HitType;
import com.netease.yunxin.kit.searchkit.ui.R;
import com.netease.yunxin.kit.searchkit.ui.databinding.SearchUserItemLayoutBinding;
import com.netease.yunxin.kit.searchkit.ui.model.FriendBean;

public class FriendViewHolder extends BaseViewHolder<FriendBean> {
    private FriendSearchInfo friendInfo;
    private SearchUserItemLayoutBinding viewBinding;

    public FriendViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public FriendViewHolder(@NonNull SearchUserItemLayoutBinding viewBinding) {
        this(viewBinding.getRoot());
        this.viewBinding = viewBinding;
    }

    @Override
    public void onBindData(FriendBean data, int position) {
            friendInfo =  data.friendSearchInfo;
            if (friendInfo != null) {
                viewBinding.cavUserIcon.setData(friendInfo.getFriendInfo().getAvatar(), friendInfo.getFriendInfo().getName(), AvatarColor.avatarColor(friendInfo.getFriendInfo().getAccount()));
                if (friendInfo.getHitType() == HitType.Alias){
                    viewBinding.tvNickName.setText(getSelectSpanText(friendInfo.getFriendInfo().getAlias(),friendInfo.getHitInfo()));
                    viewBinding.tvName.setVisibility(View.GONE);
                }else if (friendInfo.getHitType() == HitType.UserName){

                    if (!TextUtils.isEmpty(friendInfo.getFriendInfo().getAlias())){
                        viewBinding.tvNickName.setText(friendInfo.getFriendInfo().getAlias());
                        viewBinding.tvNickName.setVisibility(View.VISIBLE);
                        viewBinding.tvName.setText(getSelectSpanText(friendInfo.getFriendInfo().getUserInfo().getName(),friendInfo.getHitInfo()));
                        viewBinding.tvName.setVisibility(View.VISIBLE);
                    }else {
                        viewBinding.tvNickName.setText(getSelectSpanText(friendInfo.getFriendInfo().getUserInfo().getName(),friendInfo.getHitInfo()));
                        viewBinding.tvNickName.setVisibility(View.VISIBLE);
                        viewBinding.tvName.setVisibility(View.GONE);
                    }
                }else{
                    viewBinding.tvNickName.setVisibility(View.VISIBLE);
                    if (!TextUtils.isEmpty(friendInfo.getFriendInfo().getAlias())){
                        viewBinding.tvNickName.setText(friendInfo.getFriendInfo().getAlias());
                        viewBinding.tvName.setText(getSelectSpanText(friendInfo.getFriendInfo().getAccount(),friendInfo.getHitInfo()));
                        viewBinding.tvName.setVisibility(View.VISIBLE);
                    }else if(!TextUtils.isEmpty(friendInfo.getFriendInfo().getUserInfo().getName())) {
                        viewBinding.tvNickName.setText(friendInfo.getFriendInfo().getUserInfo().getName());
                        viewBinding.tvName.setText(getSelectSpanText(friendInfo.getFriendInfo().getAccount(),friendInfo.getHitInfo()));
                        viewBinding.tvName.setVisibility(View.VISIBLE);
                    } else {
                        viewBinding.tvNickName.setText(getSelectSpanText(friendInfo.getFriendInfo().getAccount(),friendInfo.getHitInfo()));
                        viewBinding.tvName.setVisibility(View.GONE);
                    }
                }
                viewBinding.getRoot().setOnClickListener( v -> itemListener.onClick(data,position));;
            }
    }

    private SpannableString getSelectSpanText(String text, RecordHitInfo hitInfo) {
        SpannableString spannable = new SpannableString(text);
        if (hitInfo != null) {
            spannable.setSpan(new ForegroundColorSpan(viewBinding.getRoot().getContext().getResources().getColor(R.color.color_337eff)), hitInfo.start, hitInfo.end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }
}
