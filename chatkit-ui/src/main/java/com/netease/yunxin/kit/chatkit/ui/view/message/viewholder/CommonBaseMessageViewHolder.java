// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageReader;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.MessageProperties;
import java.util.List;

/** 消息基础的 ViewHolder 其中包含 team 信息，已读时间设置等，其中除了设置此类数据外没有其他页面渲染逻辑，可继承此类实现高度自定义。 */
public abstract class CommonBaseMessageViewHolder extends RecyclerView.ViewHolder {
  private static final String TAG = "CommonBaseViewHolder";
  // 默认底层基础View
  protected final View itemView;
  // 已读回执消息
  protected long receiptTime;
  // 群聊场景下群信息
  protected Team teamInfo;
  // 当前支持的自定义配置
  protected MessageProperties properties = new MessageProperties();
  // 设置点击监听回调集合
  protected IMessageItemClickListener itemClickListener;
  // 用于已读消息回执发送
  protected IMessageReader messageReader;

  public CommonBaseMessageViewHolder(@NonNull View itemView) {
    super(itemView);
    this.itemView = itemView;
  }

  /**
   * 设置已读回执时间
   *
   * @param receiptTime 时间戳
   */
  public void setReceiptTime(long receiptTime) {
    this.receiptTime = receiptTime;
  }

  /**
   * 设置群信息
   *
   * @param teamInfo 群信息
   */
  public void setTeamInfo(Team teamInfo) {
    this.teamInfo = teamInfo;
  }

  /**
   * 设置已读回调
   *
   * @param messageReader 用于触发外部发送已读回执
   */
  public void setMessageReader(IMessageReader messageReader) {
    this.messageReader = messageReader;
  }

  /**
   * 设置消息部分自定义属性控制
   *
   * @param properties 属性设置
   */
  public void setProperties(MessageProperties properties) {
    if (properties != null) {
      this.properties = properties;
    }
  }

  /**
   * 设置点击事件集合
   *
   * @param clickListener 点击事件集合
   */
  public void setItemClickListener(IMessageItemClickListener clickListener) {
    this.itemClickListener = clickListener;
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "setItemClickListener" + (clickListener == null));
  }

  /** 调用时机等同于 {@link RecyclerView.Adapter#onViewAttachedToWindow(RecyclerView.ViewHolder)} */
  public void onAttachedToWindow() {}

  /** 调用时机等同于 {@link RecyclerView.Adapter#onViewDetachedFromWindow(RecyclerView.ViewHolder)} */
  public void onDetachedFromWindow() {}

  /**
   * 调用时机等同于 {@link RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int, List)}
   *
   * @param data 具体需要渲染的数据
   * @param position 在 Adapter 中数据集合的数据位置
   * @param payload 非空集合用于控制判断是否需要全量刷新
   */
  public void bindData(ChatMessageBean data, int position, @NonNull List<?> payload) {}

  /**
   * 调用时机等同于 {@link RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)}
   *
   * @param data 具体需要渲染的数据
   * @param lastData 上一条渲染的数据（主要用来判断两条消息之间的时间间隔，控制消息的时间内容是否展示）
   */
  public void bindData(ChatMessageBean data, ChatMessageBean lastData) {}
}
