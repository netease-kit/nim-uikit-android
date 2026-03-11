// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageImageAttachment;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageVideoAttachment;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageSearchUtils;
import com.netease.yunxin.kit.chatkit.ui.common.ThumbHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatSearchItemDateHeaderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatSearchItemImageBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatSearchItemImageGridBinding;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.model.MessageGroup;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.corekit.im2.model.IMMessageProgress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** 分组图片适配器 展示按日期分组的图片列表 */
public class ChatSearchImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final int VIEW_TYPE_HEADER = 0; // 日期标题
  private static final int VIEW_TYPE_CONTENT = 1; // 图片内容网格
  private static final int VIEW_TYPE_TOP_TIPS = 2; // 头部“没有更多了”

  private Context context;
  private List<MessageGroup> imageGroups;
  private IItemClickListener<V2NIMMessage> itemClickListener; // 图片点击监听器

  private final Map<String, Integer> progressMap = new HashMap<>();
  private boolean showTopTips = false;

  public void setItemClickListener(IItemClickListener<V2NIMMessage> listener) {
    this.itemClickListener = listener;
  }

  public void updateProgress(IMMessageProgress progress) {
    if (progress == null) return;
    progressMap.put(progress.getMessageCid(), progress.getProgress());
    int position = findImageInGroupPosition(progress.getMessageCid());
    if (position != -1) {
      ALog.i(
          "ChatSearchImageAdapter",
          "updateProgress, messageCid: "
              + progress.getMessageCid()
              + ", progress: "
              + progress.getProgress()
              + "position: "
              + position);
      this.notifyItemChanged(position, ActionConstants.PAYLOAD_PROGRESS);
    }
  }

  public ChatSearchImageAdapter(Context context) {
    this.context = context;
    this.imageGroups = new ArrayList<>();
  }

  public void setShowTopTips(boolean show) {
    if (this.showTopTips != show) {
      this.showTopTips = show;
      notifyDataSetChanged();
    }
  }

  /**
   * 按日期（天）分组图片数据并排序 - 核心逻辑：按「天」生成唯一key（yyyy-MM-dd），每一个天对应一个分组 - 每组内按时间正序排列（旧→新） -
   * 分组整体按时间倒序展示（最新的天在前，如今天→昨天→2024-10-01）
   *
   * @param images 所有图片数据列表
   */
  public void addData(List<V2NIMMessage> images) {
    // 1. 空值保护
    if (images == null || images.isEmpty()) {
      return;
    }
    List<MessageGroup> newGroups = MessageSearchUtils.groupMessageByDay(context, images);
    for (int i = newGroups.size() - 1; i >= 0; i--) {
      MessageGroup ng = newGroups.get(i);
      int idx = findGroupIndexByDate(ng.getGroupKey()); // 需确保findGroupIndexByDate适配天级日期
      if (idx >= 0) {
        imageGroups.get(idx).addMessageList(ng.getMessageList());
      } else {
        imageGroups.add(0, ng);
      }
    }
    notifyDataSetChanged();
  }

  public void removeData(List<String> messageClientIds) {
    // 1. 空值防护 + 去重（避免重复处理同一ID，提升性能）
    if (messageClientIds == null || messageClientIds.isEmpty()) {
      return;
    }
    // 2. 收集需要移除的空分组（避免遍历中修改imageGroups引发并发修改异常）
    List<MessageGroup> emptyGroupsToRemove = new ArrayList<>();

    // 3. 遍历要删除的消息ID，批量处理
    for (String clientId : messageClientIds) {
      removeSingleMessage(clientId, emptyGroupsToRemove);
    }

    // 4. 批量移除空分组（统一处理，更安全）
    if (!emptyGroupsToRemove.isEmpty()) {
      removeEmptyGroups(emptyGroupsToRemove);
    }
    notifyDataSetChanged();
  }

  /**
   * 移除单条消息，并收集空分组
   *
   * @param clientId 要移除的消息ID
   * @param emptyGroupsToRemove 空分组收集容器
   */
  private void removeSingleMessage(
      @NonNull String clientId, @NonNull List<MessageGroup> emptyGroupsToRemove) {
    // 迭代器安全移除消息（避免索引遍历的错位问题）
    for (int i = 0; i < imageGroups.size(); i++) {
      boolean hasFind = false;
      MessageGroup targetGroup = imageGroups.get(i);
      if (targetGroup == null) {
        continue;
      }
      List<V2NIMMessage> messageList = targetGroup.getMessageList();
      if (messageList == null || messageList.isEmpty()) {
        emptyGroupsToRemove.add(targetGroup);
        continue;
      }
      Iterator<V2NIMMessage> iterator = messageList.iterator();
      while (iterator.hasNext()) {
        V2NIMMessage message = iterator.next();
        if (message != null && TextUtils.equals(message.getMessageClientId(), clientId)) {
          iterator.remove(); // 安全移除当前元素
          hasFind = true;
          break; // 找到并移除后退出循环，无需继续遍历
        }
      }
      // 分组为空则标记待移除
      if (hasFind) {
        if (messageList.isEmpty()) {
          emptyGroupsToRemove.add(targetGroup);
        }
        break;
      }
    }
  }

  /** 批量移除空分组，并记录移除的索引（用于精准刷新） */
  private void removeEmptyGroups(@NonNull List<MessageGroup> emptyGroupsToRemove) {
    // 收集要移除的分组索引（倒序删除，避免索引错位）
    List<Integer> removeIndices = new ArrayList<>();
    for (MessageGroup group : emptyGroupsToRemove) {
      int index = imageGroups.indexOf(group);
      if (index != -1) {
        removeIndices.add(index);
      }
    }

    // 倒序删除（从后往前删，避免前面删除导致后面索引偏移）
    removeIndices.sort((o1, o2) -> o2 - o1);
    for (int index : removeIndices) {
      imageGroups.remove(index);
    }
  }

  public boolean isGroupsEmpty() {
    if (imageGroups == null || imageGroups.isEmpty()) {
      return true;
    }
    for (MessageGroup g : imageGroups) {
      if (g.getMessageList() != null && !g.getMessageList().isEmpty()) {
        return false;
      }
    }
    return true;
  }

  public int findGroupIndexByDate(String dateLabel) {
    for (int i = 0; i < imageGroups.size(); i++) {
      if (dateLabel.equals(imageGroups.get(i).getGroupKey())) {
        return i;
      }
    }
    return -1;
  }

  public int findImageInGroupPosition(String messageCid) {
    if (TextUtils.isEmpty(messageCid)) return -1;
    for (int i = 0; i < imageGroups.size(); i++) {
      MessageGroup group = imageGroups.get(i);
      for (int j = 0; j < group.getMessageList().size(); j++) {
        V2NIMMessage message = group.getMessageList().get(j);
        if (TextUtils.equals(message.getMessageClientId(), messageCid)) {
          return i * 2 + 1; // 图片网格位置 = 分组位置 * 2 + 1 + 图片索引
        }
      }
    }
    return -1;
  }

  public int findImageGroupPosition(String messageCid) {
    if (TextUtils.isEmpty(messageCid)) return -1;
    for (int i = 0; i < imageGroups.size(); i++) {
      MessageGroup group = imageGroups.get(i);
      for (int j = 0; j < group.getMessageList().size(); j++) {
        V2NIMMessage message = group.getMessageList().get(j);
        if (TextUtils.equals(message.getMessageClientId(), messageCid)) {
          return i; // 分组位置
        }
      }
    }
    return -1;
  }

  public ArrayList<V2NIMMessage> getAllImageMessages() {
    ArrayList<V2NIMMessage> list = new ArrayList<>();
    for (MessageGroup group : imageGroups) {
      if (group.getMessageList() != null) {
        for (V2NIMMessage msg : group.getMessageList()) {
          if (msg != null && msg.getAttachment() instanceof V2NIMMessageImageAttachment) {
            list.add(msg);
          }
        }
      }
    }
    return list;
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (viewType == VIEW_TYPE_HEADER) {
      // 创建日期标题视图，使用ViewBinding
      ChatSearchItemDateHeaderBinding binding =
          ChatSearchItemDateHeaderBinding.inflate(LayoutInflater.from(context), parent, false);
      return new DateHeaderViewHolder(binding);
    } else if (viewType == VIEW_TYPE_CONTENT) {
      // 创建图片网格视图，使用ViewBinding
      ChatSearchItemImageGridBinding binding =
          ChatSearchItemImageGridBinding.inflate(LayoutInflater.from(context), parent, false);
      return new ImageGridViewHolder(binding);
    } else {
      android.widget.TextView tv = new android.widget.TextView(context);
      tv.setLayoutParams(
          new ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
      int padH = (int) (context.getResources().getDisplayMetrics().density * 12);
      int padV = (int) (context.getResources().getDisplayMetrics().density * 8);
      tv.setPadding(padH, padV, padH, padV);
      tv.setTextColor(context.getResources().getColor(R.color.color_999999));
      tv.setText(R.string.chat_list_no_more_tips);
      tv.setGravity(android.view.Gravity.CENTER);
      return new FooterViewHolder(tv);
    }
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    if (holder instanceof FooterViewHolder) {
      // 顶部提示，无需绑定
    } else if (holder instanceof DateHeaderViewHolder) {
      // 绑定日期标题
      int offset = showTopTips ? 1 : 0;
      MessageGroup group = imageGroups.get((position - offset) / 2);
      ((DateHeaderViewHolder) holder).binding.tvDateHeader.setText(group.getGroupKey());
    } else if (holder instanceof ImageGridViewHolder) {
      // 绑定图片网格
      int offset = showTopTips ? 1 : 0;
      MessageGroup group = imageGroups.get((position - offset) / 2);
      ((ImageGridViewHolder) holder).setImages(group.getMessageList());
    }
  }

  @Override
  public void onBindViewHolder(
      @NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
    if (payloads.isEmpty()) {
      super.onBindViewHolder(holder, position, payloads);
    } else {
      for (int i = 0; i < payloads.size(); i++) {
        String payloadItem = payloads.get(i).toString();
        ALog.i(
            "ChatSearchImageAdapter",
            "onBindViewHolder, messageCid: " + payloadItem + ",position: " + position);
        if (TextUtils.equals(payloadItem, ActionConstants.PAYLOAD_PROGRESS)) {
          if (holder instanceof ImageGridViewHolder) {
            ((ImageGridViewHolder) holder).gridAdapter.updateProgress();
          }
        }
      }
    }
  }

  @Override
  public int getItemCount() {
    // 每个分组有两个视图：日期标题和图片网格
    int base = imageGroups != null ? imageGroups.size() * 2 : 0;
    return base + (showTopTips ? 1 : 0);
  }

  @Override
  public int getItemViewType(int position) {
    // 偶数位置是日期标题，奇数位置是图片网格
    if (showTopTips && position == 0) {
      return VIEW_TYPE_TOP_TIPS;
    }
    int offset = showTopTips ? 1 : 0;
    int basePos = position - offset;
    return basePos % 2 == 0 ? VIEW_TYPE_HEADER : VIEW_TYPE_CONTENT;
  }

  /** 日期标题视图持有者 */
  static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
    ChatSearchItemDateHeaderBinding binding;

    public DateHeaderViewHolder(@NonNull ChatSearchItemDateHeaderBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }

  /** 图片网格视图持有者 */
  class ImageGridViewHolder extends RecyclerView.ViewHolder {
    ChatSearchItemImageGridBinding binding;
    ImageGridAdapter gridAdapter;

    public ImageGridViewHolder(@NonNull ChatSearchItemImageGridBinding binding) {
      super(binding.getRoot());
      this.binding = binding;

      // 设置网格布局管理器，每行4个图片
      GridLayoutManager layoutManager = new GridLayoutManager(context, 4);
      binding.rvImageGrid.setLayoutManager(layoutManager);

      // 创建网格适配器
      gridAdapter = new ImageGridAdapter();
      binding.rvImageGrid.setAdapter(gridAdapter);
    }

    /** 设置图片数据到网格适配器 */
    public void setImages(List<V2NIMMessage> images) {
      gridAdapter.setImages(images);
    }
  }

  /** 顶部提示视图持有者 */
  static class FooterViewHolder extends RecyclerView.ViewHolder {
    public FooterViewHolder(@NonNull android.view.View itemView) {
      super(itemView);
    }
  }

  /** 内部图片网格适配器 */
  class ImageGridAdapter extends RecyclerView.Adapter<ImageGridAdapter.ImageViewHolder> {

    private List<V2NIMMessage> images;

    public void updateProgress() {
      for (String clientId : progressMap.keySet()) {
        ALog.i("ImageGridAdapter", "updateProgress, messageCid: " + clientId);
        int position = findImagePosition(clientId);
        if (position != -1) {
          this.notifyItemChanged(position, ActionConstants.PAYLOAD_PROGRESS);
        }
      }
    }

    public int findImagePosition(String messageCid) {
      if (TextUtils.isEmpty(messageCid)) return -1;
      for (int j = 0; j < images.size(); j++) {
        V2NIMMessage message = images.get(j);
        if (TextUtils.equals(message.getMessageClientId(), messageCid)) {
          return j; // 图片网格位置 = 图片索引
        }
      }
      return -1;
    }

    public void setImages(List<V2NIMMessage> images) {
      this.images = images;
      this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      ChatSearchItemImageBinding binding =
          ChatSearchItemImageBinding.inflate(LayoutInflater.from(context), parent, false);
      return new ImageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
      if (images != null && position < images.size()) {
        final V2NIMMessage message = images.get(position);
        String imageUrl = "";
        ALog.i(
            "ImageGridAdapter",
            "onBindViewHolder, messageCid: "
                + message.getMessageClientId()
                + ",position: "
                + position);
        // 根据消息类型设置封面和视频信息
        if (message.getAttachment() instanceof V2NIMMessageImageAttachment) {
          V2NIMMessageImageAttachment attachment =
              (V2NIMMessageImageAttachment) message.getAttachment();
          imageUrl =
              !TextUtils.isEmpty(attachment.getPath()) ? attachment.getPath() : attachment.getUrl();
          holder.binding.ivVideoIcon.setVisibility(android.view.View.GONE);
          holder.binding.tvDuration.setVisibility(android.view.View.GONE);
        } else if (message.getAttachment() instanceof V2NIMMessageVideoAttachment) {
          V2NIMMessageVideoAttachment attachment =
              (V2NIMMessageVideoAttachment) message.getAttachment();
          if (!TextUtils.isEmpty(attachment.getUrl())) {
            imageUrl = ThumbHelper.makeVideoThumbUrl(attachment.getUrl());
          } else {
            imageUrl = attachment.getPath();
          }
          long second = attachment.getDuration() / 1000;
          if (second <= 0) {
            second = 1;
          }
          holder.binding.ivVideoIcon.setVisibility(android.view.View.VISIBLE);
          holder.binding.tvDuration.setVisibility(android.view.View.VISIBLE);
          holder.binding.tvDuration.setText(
              String.format(java.util.Locale.CHINA, "%02d:%02d", second / 60, second % 60));
        } else {
          holder.binding.ivVideoIcon.setVisibility(android.view.View.GONE);
          holder.binding.tvDuration.setVisibility(android.view.View.GONE);
        }

        // 动态计算图片高度，使其与宽度相同
        // 获取屏幕宽度，减去左右边距和间距
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int paddingLeftRight = 4; // 左右各2dp
        int itemSpacing = 2; // 每个item间距1dp，一行4个图片有3个间距
        int imageSize = (screenWidth - paddingLeftRight * 2 - itemSpacing * 3) / 4;

        // 设置ImageView尺寸
        ViewGroup.LayoutParams layoutParams = holder.binding.itemLayout.getLayoutParams();
        layoutParams.width = imageSize;
        layoutParams.height = imageSize;
        holder.binding.itemLayout.setLayoutParams(layoutParams);

        // 使用Glide加载图片
        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.chat_loading_drawable) // 加载中显示的占位图
            .error(R.drawable.ic_chat_img_failed) // 加载失败显示的图片
            .centerCrop() // 图片裁剪方式
            .into(holder.binding.ivImage);

        // 设置图片点击事件
        holder.binding.ivImage.setOnClickListener(
            view -> {
              if (itemClickListener != null) {
                itemClickListener.onMessageClick(view, position, message);
              }
            });
        holder.binding.ivImage.setOnLongClickListener(
            view -> {
              if (itemClickListener != null) {
                itemClickListener.onMessageLongClick(view, position, message);
              }
              return true;
            });
      }
    }

    @Override
    public void onBindViewHolder(
        @NonNull ImageViewHolder holder, int position, @NonNull List<Object> payloads) {
      if (payloads.isEmpty()) {
        super.onBindViewHolder(holder, position, payloads);
      } else {
        final V2NIMMessage message = images.get(position);

        if (!payloads.isEmpty()) {
          for (int i = 0; i < payloads.size(); ++i) {
            String payloadItem = payloads.get(i).toString();
            if (TextUtils.equals(payloadItem, ActionConstants.PAYLOAD_PROGRESS)) {
              // 消息附件下载进度更新
              Integer p = progressMap.get(message.getMessageClientId());
              if (p != null && p < 100) {
                holder.binding.progressFl.setVisibility(android.view.View.VISIBLE);
                holder.binding.progressBarInsideIcon.setVisibility(android.view.View.VISIBLE);
                holder.binding.progressBar.setIndeterminate(false);
                holder.binding.progressBar.setVisibility(android.view.View.VISIBLE);
                holder.binding.progressBar.setProgress(p);
              } else {
                holder.binding.progressFl.setVisibility(android.view.View.GONE);
                holder.binding.progressBarInsideIcon.setVisibility(android.view.View.GONE);
                holder.binding.progressBar.setVisibility(android.view.View.GONE);
              }
            }
          }
        }
      }
    }

    @Override
    public int getItemCount() {
      return images != null ? images.size() : 0;
    }

    /** 图片视图持有者 */
    class ImageViewHolder extends RecyclerView.ViewHolder {
      ChatSearchItemImageBinding binding;

      public ImageViewHolder(@NonNull ChatSearchItemImageBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
      }
    }
  }
}
