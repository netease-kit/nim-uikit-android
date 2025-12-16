// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.PAYLOAD_REFRESH_AUDIO_ANIM;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.POP_ACTION_COPY;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.POP_ACTION_TEL;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.coexist.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.coexist.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.coexist.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatDialogUtils;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.common.WatchTextMessageDialog;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatCollectionActivityBinding;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.model.CollectionBean;
import com.netease.yunxin.kit.chatkit.ui.page.adapter.CollectionMessageAdapter;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.CollectionViewModel;
import com.netease.yunxin.kit.chatkit.ui.view.message.audio.ChatMessageAudioControl;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.dialog.BaseBottomChoiceDialog;
import com.netease.yunxin.kit.common.ui.dialog.BottomChoiceDialog;
import com.netease.yunxin.kit.common.ui.dialog.BottomHeaderChoiceDialog;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.coexist.im2.model.IMMessageProgress;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;

public abstract class CollectionBaseActivity extends BaseLocalActivity {

  public static final String TAG = "ChatCollectionsBaseActivity";
  protected ChatCollectionActivityBinding viewBinding;
  protected CollectionViewModel viewModel;
  protected V2NIMConversationType mSessionType;
  protected CollectionMessageAdapter collectionAdapter;
  protected CollectionBean forwardMessage;

  // 转发Launcher
  protected ActivityResultLauncher<Intent> forwardLauncher;

  // 标记页面弹窗 取消置顶
  public static final String ACTION_DELETE_COLLECTION = "delete_collection";
  // 标记页面弹窗 转发消息
  public static final String ACTION_TRANSMIT_COLLECTION = "transmit_collection";
  // 标记页面弹窗 复制消息
  public static final String ACTION_COPY_COLLECTION = "copy_collection";

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    viewBinding = ChatCollectionActivityBinding.inflate(getLayoutInflater());
    setContentView(viewBinding.getRoot());
    initView();
    initData();
  }

  @Override
  protected void onStop() {
    super.onStop();
    ChatMessageAudioControl.getInstance().stopAudio();
  }

  // 初始化页面View
  protected void initView() {
    viewBinding.collectionTitleBar.setOnBackIconClickListener(view -> finish());
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    viewBinding.collectionRecyclerView.setLayoutManager(layoutManager);
    RecyclerView.ItemDecoration itemDecoration = getItemDecoration();
    if (itemDecoration != null) {
      viewBinding.collectionRecyclerView.addItemDecoration(itemDecoration);
    }
    // 监听滚动，当滚动到底部触发加载更多
    viewBinding.collectionRecyclerView.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
          @Override
          public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
              int position = layoutManager.findLastVisibleItemPosition();
              if (viewModel.hasMore()) {
                CollectionBean last =
                    collectionAdapter.getData(collectionAdapter.getItemCount() - 1);
                viewModel.getCollectionMessageList(last.collectionData);
              }
            }
          }

          @Override
          public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
          }
        });
    collectionAdapter = new CollectionMessageAdapter();
    collectionAdapter.setViewHolderClickListener(collectionClickListener);
    viewBinding.collectionRecyclerView.setAdapter(collectionAdapter);
  }

  // 初始化页面数据
  protected void initData() {
    viewModel = new ViewModelProvider(this).get(CollectionViewModel.class);

    // 监听收藏列表查询结果
    viewModel
        .getMessageLiveData()
        .observe(
            this,
            result -> {
              collectionAdapter.addData(result.getData());
              showEmptyView(collectionAdapter.getItemCount() < 1);
            });

    // 监听收藏列表移除
    viewModel
        .getRemoveCollectionLiveData()
        .observe(
            this,
            result -> {
              if (ChatMessageAudioControl.getInstance().isPlayingAudio()
                  && TextUtils.equals(
                      result.getData(),
                      ChatMessageAudioControl.getInstance()
                          .getPlayingAudio()
                          .getMessage()
                          .getMessageClientId())) {
                ChatMessageAudioControl.getInstance().stopAudio();
              }
              collectionAdapter.removeDataWithClientId(result.getData());
              showEmptyView(collectionAdapter.getItemCount() < 1);
            });

    viewModel.getAttachmentProgressLiveData().observeForever(this::onAttachmentUpdateProgress);

    // 转发Launcher
    forwardLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() != Activity.RESULT_OK || forwardMessage == null) {
                return;
              }
              ALog.d(LIB_TAG, TAG, "forward result");
              Intent data = result.getData();
              if (data != null) {
                ArrayList<String> conversationIds =
                    data.getStringArrayListExtra(RouterConstant.KEY_FORWARD_SELECTED_CONVERSATIONS);
                if (conversationIds != null && !conversationIds.isEmpty()) {
                  showForwardConfirmDialog(conversationIds);
                }
              }
            });

    viewModel.getCollectionMessageList(null);
  }

  // 显示空页面
  private void showEmptyView(boolean show) {
    if (show) {
      viewBinding.collectionEmptyView.setVisibility(View.VISIBLE);
    } else {
      viewBinding.collectionEmptyView.setVisibility(View.GONE);
    }
  }

  // 获取RecyclerView的ItemDecoration 普通版和娱乐版UI差异
  public RecyclerView.ItemDecoration getItemDecoration() {
    return null;
  }

  // 标记列表消息点击事件监听
  private final IItemClickListener collectionClickListener =
      new IItemClickListener<CollectionBean>() {
        @Override
        public boolean onMessageClick(View view, int position, CollectionBean messageInfo) {
          clickMsg(messageInfo);
          return true;
        }

        @Override
        public boolean onCustomViewClick(View view, int position, CollectionBean messageInfo) {
          if (view.getId() == R.id.iv_more_action) {
            showMoreActionDialog(messageInfo);
          }
          return true;
        }

        @Override
        public boolean onMessageTelClick(
            View view, int position, CollectionBean messageInfo, String target) {
          BottomHeaderChoiceDialog dialog =
              new BottomHeaderChoiceDialog(
                  CollectionBaseActivity.this, ChatDialogUtils.assembleMessageTelActions());
          dialog.setTitle(String.format(getString(R.string.chat_tel_tips_title), target));
          dialog.setOnChoiceListener(
              new BottomChoiceDialog.OnChoiceListener() {
                @Override
                public void onChoice(@NonNull String type) {
                  switch (type) {
                    case POP_ACTION_TEL:
                      Intent intent = new Intent(Intent.ACTION_DIAL); // 仅打开拨号界面
                      intent.setData(Uri.parse("tel:" + target)); // 自动填充电话号码
                      startActivity(intent);
                      break;
                    case POP_ACTION_COPY:
                      MessageHelper.copyText(target, true);

                      break;
                    default:
                      break;
                  }
                }

                @Override
                public void onCancel() {}
              });
          dialog.show();
          return true;
        }
      };

  // 附件下载进度更新
  protected void onAttachmentUpdateProgress(FetchResult<IMMessageProgress> fetchResult) {
    if (fetchResult.isSuccess() && fetchResult.getData() != null) {
      ALog.d(LIB_TAG, TAG, "onAttachmentUpdateProgress");
      collectionAdapter.updateMessageProgress(fetchResult.getData());
    }
  }

  // 标记列表，消息体点击
  private void clickMsg(CollectionBean bean) {
    if (bean == null || bean.getMessageData() == null) {
      Toast.makeText(this, R.string.chat_collection_message_empty_tips, Toast.LENGTH_SHORT).show();
      return;
    }
    // 文本消息和富文本消息点击进入查看页面
    if (bean.getMessageData().getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT
        || MessageHelper.isRichTextMsg(bean.getMessageData()) != null) {
      WatchTextMessageDialog.launchDialog(
          getSupportFragmentManager(), TAG, bean.getMessageInfo(), getPageBackgroundColor());
    } else if (bean.getMessageData().getMessageType()
        == V2NIMMessageType.V2NIM_MESSAGE_TYPE_IMAGE) {
      // 图片消息点击查看大图
      ArrayList<IMMessageInfo> messageList = new ArrayList<>();
      messageList.add(bean.getMessageInfo());
      ChatUtils.watchImage(CollectionBaseActivity.this, bean.getMessageInfo(), messageList);
    } else if (bean.getMessageData().getMessageType()
        == V2NIMMessageType.V2NIM_MESSAGE_TYPE_VIDEO) {
      // 视频消息点击查看视频
      ChatUtils.watchVideo(CollectionBaseActivity.this, bean.getMessageInfo());
    } else if (bean.getMessageData().getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_FILE) {
      // 文件消息点击查看文件,打开系统查看器
      ChatUtils.openFile(CollectionBaseActivity.this, bean.getMessageInfo());
    } else if (bean.getMessageData().getMessageType()
        == V2NIMMessageType.V2NIM_MESSAGE_TYPE_LOCATION) {
      // 位置消息点击查看地图
      XKitRouter.withKey(RouterConstant.PATH_CHAT_LOCATION_PAGE)
          .withContext(this)
          .withParam(RouterConstant.KEY_MESSAGE, bean.getMessageData())
          .withParam(RouterConstant.KEY_LOCATION_PAGE_TYPE, RouterConstant.KEY_LOCATION_TYPE_DETAIL)
          .navigate();
    } else if (bean.getMessageData().getMessageType()
        == V2NIMMessageType.V2NIM_MESSAGE_TYPE_AUDIO) {
      // 音频消息点击播放音频
      ChatMessageAudioControl.getInstance().setEarPhoneModeEnable(ChatKitClient.isEarphoneMode());
      collectionAdapter.updateMessage(bean, PAYLOAD_REFRESH_AUDIO_ANIM);
    } else {
      // 自定义消息点击，实现在子类中
      clickCustomMessage(bean);
    }
  }

  // 显示更多操作弹窗
  public void showMoreActionDialog(CollectionBean collectionBean) {
    BaseBottomChoiceDialog dialog = getMoreActionDialog(collectionBean);
    dialog.setOnChoiceListener(
        new BottomChoiceDialog.OnChoiceListener() {
          @Override
          public void onChoice(@NonNull String type) {
            boolean hasNetwork = NetworkUtils.isConnected();
            switch (type) {
              case ACTION_TRANSMIT_COLLECTION:
                if (hasNetwork) {
                  onTransmit(collectionBean);
                } else {
                  Toast.makeText(
                          CollectionBaseActivity.this,
                          R.string.chat_network_error_tip,
                          Toast.LENGTH_SHORT)
                      .show();
                }
                break;
              case ACTION_DELETE_COLLECTION:
                showDeleteConfirmDialog(collectionBean);
                break;
              case ACTION_COPY_COLLECTION:
                MessageHelper.copyTextMessage(
                    new IMMessageInfo(collectionBean.getMessageData()), true);
                break;
              default:
                break;
            }
          }

          @Override
          public void onCancel() {}
        });
    dialog.show();
  }

  public BaseBottomChoiceDialog getMoreActionDialog(CollectionBean collectionBean) {
    return new BottomChoiceDialog(this, assembleActions(collectionBean));
  }

  private void showDeleteConfirmDialog(CollectionBean collectionBean) {
    CommonChoiceDialog dialog = new CommonChoiceDialog();
    dialog
        .setTitleStr(getString(R.string.chat_message_action_delete))
        .setContentStr(getString(R.string.chat_delete_collection_content))
        .setPositiveStr(getString(R.string.chat_message_delete))
        .setNegativeStr(getString(R.string.cancel))
        .setConfirmListener(
            new ChoiceListener() {
              @Override
              public void onPositive() {
                if (!NetworkUtils.isConnected()) {
                  ToastX.showShortToast(R.string.chat_network_error_tip);
                  return;
                }
                viewModel.removeCollection(collectionBean.collectionData);
              }

              @Override
              public void onNegative() {}
            })
        .show(this.getSupportFragmentManager());
  }

  // 组装更多操作弹窗按钮
  public ArrayList<ActionItem> assembleActions(CollectionBean collectionBean) {
    ArrayList<ActionItem> actions = new ArrayList<>();
    actions.add(
        new ActionItem(ACTION_DELETE_COLLECTION, 0, R.string.chat_message_action_delete)
            .setTitleColorResId(R.color.color_333333));
    if (collectionBean.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT.getValue()) {
      actions.add(
          new ActionItem(ACTION_COPY_COLLECTION, 0, R.string.chat_message_action_copy)
              .setTitleColorResId(R.color.color_333333));
    }
    if (collectionBean.getMessageType() != MsgTypeEnum.audio.getValue()) {
      actions.add(
          new ActionItem(ACTION_TRANSMIT_COLLECTION, 0, R.string.chat_message_action_transmit)
              .setTitleColorResId(R.color.color_333333));
    }

    return actions;
  }

  // 点击自定义消息，子类实现后续操作
  protected void clickCustomMessage(CollectionBean messageBean) {}

  // 转发消息，如果配置不支持群，则直接到P2P转发
  protected void onTransmit(CollectionBean messageBean) {
    forwardMessage = messageBean;
    goToForwardPage();
  }

  protected void goToForwardPage() {}

  // 获取页面背景颜色，子类可以复写
  protected int getPageBackgroundColor() {
    return R.color.color_eef1f4;
  }

  // 显示转发确认弹窗，子类实现
  protected void showForwardConfirmDialog(ArrayList<String> conversationIds) {}
}
