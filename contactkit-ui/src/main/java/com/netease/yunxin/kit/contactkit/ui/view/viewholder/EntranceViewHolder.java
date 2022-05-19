/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.view.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.model.BaseContactBean;
import com.netease.yunxin.kit.contactkit.ui.model.ContactEntranceBean;
import com.netease.yunxin.kit.contactkit.ui.databinding.EntranceContactViewHolderBinding;
import com.netease.yunxin.kit.contactkit.ui.view.ContactListViewAttrs;

public class EntranceViewHolder extends BaseContactViewHolder {

    EntranceContactViewHolderBinding binding;
    private final static int maxNumber = 99;

    public EntranceViewHolder(@NonNull ViewGroup itemView) {
        super(itemView);
    }

    @Override
    public void initViewBinding(LayoutInflater layoutInflater, ViewGroup container) {
        binding = EntranceContactViewHolderBinding.inflate(layoutInflater, container, true);
    }

    @Override
    public void onBind(BaseContactBean bean, int position, ContactListViewAttrs attrs) {
        ContactEntranceBean entranceBean = (ContactEntranceBean) bean;
        if (entranceBean.showRightArrow) {
            binding.ivArrow.setVisibility(View.VISIBLE);
        } else {
            binding.ivArrow.setVisibility(View.GONE);
        }

        if (entranceBean.number > 0) {
            binding.tvNumber.setVisibility(View.VISIBLE);
            String numberValue = String.valueOf(entranceBean.number);
            if (entranceBean.number > maxNumber){
                numberValue = context.getResources().getString(R.string.verify_max_count_text);
            }
            binding.tvNumber.setText(numberValue);
        } else {
            binding.tvNumber.setVisibility(View.GONE);
        }

        Glide.with(context)
                .load(entranceBean.icon)
                .centerCrop()
                .into(binding.ivIcon);

        binding.tvTitle.setText(entranceBean.title);
        binding.tvTitle.setTextColor(attrs.getNameTextColor());

        binding.rootView.setOnClickListener(v -> {
            if (actions != null && actions.getContactListener(bean.viewType) != null) {
                actions.getContactListener(bean.viewType).onClick(position, bean);
            }
        });


    }
}
