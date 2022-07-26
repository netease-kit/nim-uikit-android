package com.netease.yunxin.kit.chatkit.ui.common;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;

public class BaseMessageViewHolder extends RecyclerView.ViewHolder {

    public ChatBaseMessageViewHolderBinding viewBinding;

    public BaseMessageViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public BaseMessageViewHolder(@NonNull ChatBaseMessageViewHolderBinding binding) {
        super(binding.getRoot());
        viewBinding = binding;
    }

}
