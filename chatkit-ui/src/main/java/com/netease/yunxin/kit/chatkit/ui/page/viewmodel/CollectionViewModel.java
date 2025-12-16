// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.coexist.sdk.v2.V2NIMError;
import com.netease.nimlib.coexist.sdk.v2.auth.V2NIMLoginListener;
import com.netease.nimlib.coexist.sdk.v2.auth.enums.V2NIMLoginClientChange;
import com.netease.nimlib.coexist.sdk.v2.auth.enums.V2NIMLoginStatus;
import com.netease.nimlib.coexist.sdk.v2.auth.model.V2NIMKickedOfflineDetail;
import com.netease.nimlib.coexist.sdk.v2.auth.model.V2NIMLoginClient;
import com.netease.nimlib.coexist.sdk.v2.message.V2NIMCollection;
import com.netease.nimlib.coexist.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.coexist.sdk.v2.message.option.V2NIMCollectionOption;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.impl.MessageListenerImpl;
import com.netease.yunxin.kit.chatkit.listener.ChatListener;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.repo.SettingRepo;
import com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageCreator;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.model.CollectionBean;
import com.netease.yunxin.kit.chatkit.utils.ErrorUtils;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.coexist.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.coexist.im2.model.IMMessageProgress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 收藏消息ViewModel 提供收藏信息查询、移除收藏、转发等功能
 *
 * <p>
 */
public class CollectionViewModel extends BaseViewModel {

  public static final String TAG = "ChatCollectionViewModel";

  private boolean hasMoreCollection = false;
  // 每页加载数量
  private static final int PAGE_LIMIT = 100;
  // 标记消息查询LiveData
  private final MutableLiveData<FetchResult<List<CollectionBean>>> messageLiveData =
      new MutableLiveData<>();

  // 移除标记LiveData
  private final MutableLiveData<FetchResult<String>> removeCollectionLiveData =
      new MutableLiveData<>();

  // 附件下载进度LiveData
  private final MutableLiveData<FetchResult<IMMessageProgress>> attachmentProgressLiveData =
      new MutableLiveData<>();

  // 获取标记消息查询列表LiveData
  public MutableLiveData<FetchResult<List<CollectionBean>>> getMessageLiveData() {
    return messageLiveData;
  }

  // 获取移除标记LiveData
  public MutableLiveData<FetchResult<String>> getRemoveCollectionLiveData() {
    return removeCollectionLiveData;
  }

  // 获取附件下载进度LiveData
  public MutableLiveData<FetchResult<IMMessageProgress>> getAttachmentProgressLiveData() {
    return attachmentProgressLiveData;
  }

  public CollectionViewModel() {
    ChatRepo.addMessageListener(messageListener);
    IMKitClient.addLoginListener(loginListener);
  }

  /** 获取收藏消息列表 */
  public void getCollectionMessageList(V2NIMCollection anchor) {
    ALog.d(LIB_TAG, TAG, "getCollectionMessageList");
    V2NIMCollectionOption.V2NIMCollectionOptionBuilder optionBuilder =
        V2NIMCollectionOption.V2NIMCollectionOptionBuilder.builder();
    optionBuilder.withLimit(PAGE_LIMIT);
    if (anchor != null) {
      optionBuilder.withAnchorCollection(anchor);
    }

    ChatRepo.getCollections(
        optionBuilder.build(),
        new FetchCallback<List<V2NIMCollection>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(
                LIB_TAG,
                TAG,
                "getCollectionMessageList , onError:" + errorCode + "errorMsg:" + errorMsg);
            ErrorUtils.showErrorCodeToast(IMKitClient.getApplicationContext(), errorCode);
          }

          @Override
          public void onSuccess(@Nullable List<V2NIMCollection> data) {
            ALog.d(
                LIB_TAG,
                TAG,
                "getCollectionMessageLists , onSuccess:" + (data != null ? data.size() : 0));
            hasMoreCollection = data != null && data.size() == PAGE_LIMIT;
            convertToChatMessageBean(data);
          }
        });
  }

  // 移除收藏消息
  public void removeCollection(V2NIMCollection collection) {
    if (collection == null) {
      return;
    }
    ChatRepo.removeCollections(
        Collections.singletonList(collection),
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(
                LIB_TAG, TAG, "removeCollection , onError:" + errorCode + "errorMsg:" + errorMsg);
            if (errorCode == ChatKitUIConstant.ERROR_CODE_NETWORK) {
              ToastX.showShortToast(R.string.chat_network_error_tip);
            }
          }

          @Override
          public void onSuccess(@Nullable Void data) {
            ALog.d(LIB_TAG, TAG, "removeCollection , onSuccess:" + collection.getCollectionId());
            FetchResult<String> removeResult = new FetchResult<>(LoadStatus.Success);
            removeResult.setData(collection.getCollectionId());
            removeCollectionLiveData.setValue(removeResult);
            ToastX.showShortToast(R.string.chat_remove_collection_tips);
          }
        });
  }

  // 转发收藏消息并发送文本消息
  public void sendForwardMessage(V2NIMMessage message, String inputMsg, String conversationId) {
    ALog.d(LIB_TAG, TAG, "sendForwardMessage:" + conversationId);
    boolean needACK = SettingRepo.getShowReadStatus();
    V2NIMMessage collectionMsg = MessageCreator.createMessage(message);
    MessageHelper.sendForwardMessage(
        new ChatMessageBean(new IMMessageInfo(collectionMsg)),
        inputMsg,
        Collections.singletonList(conversationId),
        true,
        needACK);
  }

  private final ChatListener messageListener =
      new MessageListenerImpl() {
        @Override
        public void onMessageAttachmentDownloadProgress(
            @NonNull V2NIMMessage message, int progress) {
          ALog.d(LIB_TAG, TAG, "onMessageAttachmentDownloadProgress -->> " + progress);
          FetchResult<IMMessageProgress> result = new FetchResult<>(LoadStatus.Success);
          IMMessageProgress attachmentProgress =
              new IMMessageProgress(message.getMessageClientId(), progress);
          result.setData(attachmentProgress);
          result.setType(FetchResult.FetchType.Update);
          result.setTypeIndex(-1);
          attachmentProgressLiveData.setValue(result);
        }
      };

  private final V2NIMLoginListener loginListener =
      new V2NIMLoginListener() {
        @Override
        public void onLoginStatus(V2NIMLoginStatus status) {
          //断网重连，重新拉取数据
          if (status == V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED) {
            getCollectionMessageList(null);
          }
        }

        @Override
        public void onLoginFailed(V2NIMError error) {
          // do nothing
        }

        @Override
        public void onKickedOffline(V2NIMKickedOfflineDetail detail) {
          // do nothing
        }

        @Override
        public void onLoginClientChanged(
            V2NIMLoginClientChange change, List<V2NIMLoginClient> clients) {
          // do nothing
        }
      };

  protected void convertToChatMessageBean(List<V2NIMCollection> data) {
    List<CollectionBean> chatCollectionBeans = new ArrayList<>();
    for (V2NIMCollection collection : data) {
      CollectionBean chatCollectionBean = new CollectionBean(collection);
      chatCollectionBeans.add(chatCollectionBean);
    }
    FetchResult<List<CollectionBean>> messageFetchResult = new FetchResult<>(LoadStatus.Success);
    messageFetchResult.setData(chatCollectionBeans);
    messageLiveData.setValue(messageFetchResult);
  }

  public boolean hasMore() {
    return hasMoreCollection;
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    ChatRepo.removeMessageListener(messageListener);
    IMKitClient.removeLoginListener(loginListener);
  }
}
