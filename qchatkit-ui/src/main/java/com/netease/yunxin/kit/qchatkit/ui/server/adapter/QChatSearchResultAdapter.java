// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.server.adapter;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.qchat.result.QChatApplyServerJoinResult;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.qchatkit.repo.QChatServerRepo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatSearchResultInfo;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.common.QChatCommonAdapter;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatServerSearchResultItemBinding;

/** Adapter for searching server. */
public class QChatSearchResultAdapter
    extends QChatCommonAdapter<QChatSearchResultInfo, QChatServerSearchResultItemBinding> {
  public QChatSearchResultAdapter(Context context) {
    super(context, QChatServerSearchResultItemBinding.class);
  }

  @Override
  public void onBindViewHolder(
      QChatServerSearchResultItemBinding binding,
      int position,
      QChatSearchResultInfo data,
      int bingingAdapterPosition) {
    super.onBindViewHolder(binding, position, data, bingingAdapterPosition);

    binding.cavIcon.setData(
        data.serverInfo.getIconUrl(),
        data.serverInfo.getName(),
        AvatarColor.avatarColor(data.serverInfo.getServerId()));

    binding.tvName.setText(data.serverInfo.getName());

    binding.tvServerId.setText(String.valueOf(data.serverInfo.getServerId()));

    TextView tvActionAndTip = binding.tvActionAndTip;
    if (data.state == QChatSearchResultInfo.STATE_NOT_JOIN) {
      tvActionAndTip.setText(R.string.qchat_server_state_join);
      tvActionAndTip.setEnabled(true);
    } else if (data.state == QChatSearchResultInfo.STATE_JOINED) {
      tvActionAndTip.setText(R.string.qchat_server_state_joined);
      tvActionAndTip.setEnabled(false);
    } else if (data.state == QChatSearchResultInfo.STATE_HAD_APPLIED) {
      tvActionAndTip.setText(R.string.qchat_server_state_applied);
      tvActionAndTip.setEnabled(false);
    }

    tvActionAndTip.setOnClickListener(
        v ->
            QChatServerRepo.applyServerJoin(
                data.serverInfo.getServerId(),
                new FetchCallback<QChatApplyServerJoinResult>() {
                  @Override
                  public void onSuccess(@Nullable QChatApplyServerJoinResult param) {
                    data.state = QChatSearchResultInfo.STATE_HAD_APPLIED;
                    notifyItemChanged(bingingAdapterPosition);
                    Context context = v.getContext();
                    Toast.makeText(
                            context,
                            context.getString(R.string.qchat_server_had_appled_tip),
                            Toast.LENGTH_SHORT)
                        .show();
                  }

                  @Override
                  public void onFailed(int code) {
                    Toast.makeText(v.getContext(), "failed " + code, Toast.LENGTH_SHORT).show();
                  }

                  @Override
                  public void onException(@Nullable Throwable exception) {
                    Toast.makeText(v.getContext(), "exception " + exception, Toast.LENGTH_SHORT)
                        .show();
                  }
                }));
  }
}
