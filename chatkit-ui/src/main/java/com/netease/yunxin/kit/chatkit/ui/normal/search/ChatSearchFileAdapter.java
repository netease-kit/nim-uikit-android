// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.search;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageFileAttachment;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.common.MessageSearchUtils;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatSearchFileItemBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatSearchItemDateHeaderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatSearchItemImageGridBinding;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.model.MessageGroup;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.utils.FileUtils;
import com.netease.yunxin.kit.corekit.im2.model.IMMessageProgress;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatSearchFileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final int VIEW_TYPE_HEADER = 0;
  private static final int VIEW_TYPE_CONTENT = 1;
  private static final int VIEW_TYPE_FOOTER = 2;

  private Context context;
  private List<MessageGroup> fileGroups;
  private IItemClickListener<V2NIMMessage> itemClickListener;
  private final Map<String, Integer> progressMap = new HashMap<>();
  private boolean showFooter = false;
  private boolean footerLoading = false;

  public ChatSearchFileAdapter(Context context, List<MessageGroup> fileGroups) {
    this.context = context;
    this.fileGroups = fileGroups;
  }

  public void setItemClickListener(IItemClickListener<V2NIMMessage> listener) {
    this.itemClickListener = listener;
  }

  public void setShowFooter(boolean show) {
    if (this.showFooter != show) {
      this.showFooter = show;
      notifyDataSetChanged();
    }
  }

  public void setFooterLoading(boolean loading) {
    this.footerLoading = loading;
    notifyItemChanged(getItemCount() - 1);
  }

  public void updateProgress(IMMessageProgress progress) {
    if (progress == null) return;
    progressMap.put(progress.getMessageCid(), progress.getProgress());
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (viewType == VIEW_TYPE_HEADER) {
      ChatSearchItemDateHeaderBinding binding =
          ChatSearchItemDateHeaderBinding.inflate(LayoutInflater.from(context), parent, false);
      return new DateHeaderViewHolder(binding);
    } else if (viewType == VIEW_TYPE_CONTENT) {
      ChatSearchItemImageGridBinding binding =
          ChatSearchItemImageGridBinding.inflate(LayoutInflater.from(context), parent, false);
      return new FileListContainerViewHolder(binding);
    } else {
      android.widget.TextView tv = new android.widget.TextView(context);
      tv.setLayoutParams(
          new ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
      int padH = (int) (context.getResources().getDisplayMetrics().density * 12);
      int padV = (int) (context.getResources().getDisplayMetrics().density * 8);
      tv.setPadding(padH, padV, padH, padV);
      tv.setTextColor(context.getResources().getColor(R.color.color_999999));
      tv.setGravity(android.view.Gravity.CENTER);
      return new FooterViewHolder(tv);
    }
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    if (holder instanceof FooterViewHolder) {
      ((FooterViewHolder) holder)
          .textView.setText(
              footerLoading
                  ? R.string.chat_list_loading_more_tips
                  : R.string.chat_list_no_more_tips);
    } else if (holder instanceof DateHeaderViewHolder) {
      MessageGroup group = fileGroups.get(position / 2);
      ((DateHeaderViewHolder) holder).binding.tvDateHeader.setText(group.getDisplayText());
    } else if (holder instanceof FileListContainerViewHolder) {
      MessageGroup group = fileGroups.get(position / 2);
      ((FileListContainerViewHolder) holder).setFiles(group.getMessageList());
    }
  }

  @Override
  public int getItemCount() {
    int base = fileGroups != null ? fileGroups.size() * 2 : 0;
    return base + (showFooter ? 1 : 0);
  }

  @Override
  public int getItemViewType(int position) {
    int base = fileGroups != null ? fileGroups.size() * 2 : 0;
    if (showFooter && position == base) {
      return VIEW_TYPE_FOOTER;
    }
    return position % 2 == 0 ? VIEW_TYPE_HEADER : VIEW_TYPE_CONTENT;
  }

  static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
    ChatSearchItemDateHeaderBinding binding;

    public DateHeaderViewHolder(@NonNull ChatSearchItemDateHeaderBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }

  class FileListContainerViewHolder extends RecyclerView.ViewHolder {
    ChatSearchItemImageGridBinding binding;
    FileListAdapter adapter;

    public FileListContainerViewHolder(@NonNull ChatSearchItemImageGridBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
      binding.rvImageGrid.setLayoutManager(new LinearLayoutManager(context));
      adapter = new FileListAdapter();
      binding.rvImageGrid.setAdapter(adapter);
    }

    public void setFiles(List<V2NIMMessage> files) {
      adapter.setFiles(files);
    }
  }

  class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.FileViewHolder> {
    private List<V2NIMMessage> files;

    public void setFiles(List<V2NIMMessage> files) {
      if (files != null) {
        Collections.sort(files, MessageSearchUtils.getFileSortComparator());
      }
      this.files = files;
      notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      ChatSearchFileItemBinding binding =
          ChatSearchFileItemBinding.inflate(LayoutInflater.from(context), parent, false);
      return new FileViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
      if (files == null || position >= files.size()) return;
      V2NIMMessage message = files.get(position);
      V2NIMMessageFileAttachment attachment = (V2NIMMessageFileAttachment) message.getAttachment();
      holder.binding.displayName.setText(attachment.getName());
      holder.binding.displaySize.setText(ChatUtils.formatFileSize(attachment.getSize()));
      String ext = attachment.getExt();
      if (ext == null || ext.isEmpty()) {
        ext = FileUtils.getFileExtension(attachment.getName());
      }
      if (ext != null && ext.startsWith(".")) {
        ext = ext.substring(1);
      }
      holder.binding.fileTypeIv.setImageResource(ChatUtils.getFileIcon(ext));
      // header: avatar, name, time (MM-dd)
      String name =
          MessageHelper.getChatMessageUserNameByAccount(
              message.getSenderId(), message.getConversationType());
      holder.binding.otherUsername.setText(name);
      String nickName =
          MessageHelper.getChatCacheAvatarName(
              message.getSenderId(), message.getConversationType());
      String avatar =
          MessageHelper.getChatCacheAvatar(message.getSenderId(), message.getConversationType());
      holder.binding.messageAvatar.setData(
          avatar, nickName, AvatarColor.avatarColor(message.getSenderId()));
      Date d = new Date(message.getCreateTime());
      holder.binding.tvTime.setText(
          DateFormat.format(context.getString(R.string.chat_date_m_d_formate), d));
      Integer p = progressMap.get(message.getMessageClientId());
      if (p != null && p < 100) {
        holder.binding.fileProgressFl.setVisibility(View.VISIBLE);
        holder.binding.progressBarInsideIcon.setVisibility(View.VISIBLE);
        holder.binding.progressBar.setIndeterminate(false);
        holder.binding.progressBar.setVisibility(View.VISIBLE);
        holder.binding.progressBar.setProgress(p);
      } else {
        holder.binding.fileProgressFl.setVisibility(View.GONE);
        holder.binding.progressBarInsideIcon.setVisibility(View.GONE);
        holder.binding.progressBar.setVisibility(View.GONE);
      }
      final int clickPosition = position;
      holder.itemView.setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if (itemClickListener != null) {
                itemClickListener.onMessageClick(v, clickPosition, message);
              }
            }
          });

      holder.binding.ivMoreAction.setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if (itemClickListener != null) {
                itemClickListener.onCustomViewClick(v, clickPosition, message);
              }
            }
          });
    }

    @Override
    public int getItemCount() {
      return files != null ? files.size() : 0;
    }

    class FileViewHolder extends RecyclerView.ViewHolder {
      ChatSearchFileItemBinding binding;

      public FileViewHolder(@NonNull ChatSearchFileItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
      }
    }
  }

  static class FooterViewHolder extends RecyclerView.ViewHolder {
    final TextView textView;

    public FooterViewHolder(@NonNull View itemView) {
      super(itemView);
      this.textView = (TextView) itemView;
    }
  }
}
