// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.chatkit.ui.ChatUIConfig;
import com.netease.yunxin.kit.chatkit.ui.ChatViewHolderDefaultFactory;
import com.netease.yunxin.kit.chatkit.ui.IChatFactory;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.builder.IChatViewCustom;
import com.netease.yunxin.kit.chatkit.ui.common.ChatMsgCache;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.NormalChatViewBinding;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IChatView;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageLoadHandler;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageProxy;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageReader;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.normal.factory.ChatViewHolderFactory;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitManager;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitTextChangeListener;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.chatkit.ui.view.message.ChatMessageListView;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import java.util.List;

/** chat view contain all view about chat */
public class ChatView extends LinearLayout implements IChatView, AitTextChangeListener {

  NormalChatViewBinding binding;

  AitManager aitTextManager;
  private boolean isMultiSelect;

  private boolean canRender = true;
  private IMessageProxy messageProxy;

  public ChatView(Context context) {
    super(context);
    init(null);
  }

  public ChatView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
  }

  public ChatView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs);
  }

  private void init(AttributeSet attrs) {
    ChatViewHolderDefaultFactory.getInstance().config(ChatViewHolderFactory.getInstance());
    LayoutInflater layoutInflater = LayoutInflater.from(getContext());
    binding = NormalChatViewBinding.inflate(layoutInflater, this, true);
    binding.messageView.setOnListViewEventListener(
        new ChatMessageListView.OnListViewEventListener() {
          @Override
          public void onListViewStartScroll() {
            binding.chatBottomInputLayout.collapse(true);
          }

          @Override
          public void onListViewTouched() {
            binding.chatBottomInputLayout.collapse(true);
          }
        });
    binding.getRoot().setOnClickListener(v -> binding.chatBottomInputLayout.collapse(true));
    binding.chatMsgMultiDeleteLayout.setOnClickListener(
        v -> {
          if (messageProxy != null && binding.chatMsgMultiDeleteIv.isEnabled()) {
            messageProxy.onMultiActionClick(v, ActionConstants.ACTION_TYPE_MULTI_DELETE);
          }
        });
    binding.chatMsgMultiTransmitLayout.setOnClickListener(
        v -> {
          if (messageProxy != null && binding.chatMsgMultiTransmitIv.isEnabled()) {
            messageProxy.onMultiActionClick(v, ActionConstants.ACTION_TYPE_MULTI_FORWARD);
          }
        });

    binding.chatMsgSingleTransmitLayout.setOnClickListener(
        v -> {
          if (messageProxy != null && binding.chatMsgSingleTransmitIv.isEnabled()) {
            messageProxy.onMultiActionClick(v, ActionConstants.ACTION_TYPE_SINGLE_FORWARD);
          }
        });

    binding.chatBottomInputLayout.setRichTextSwitchListener(
        v -> {
          if (binding.chatRichLayout.getVisibility() == GONE
              && !binding.chatBottomInputLayout.isMute()) {
            showRichInputPanel();
          }
        });

    binding.chatRichSwitchLayout.setOnClickListener(v -> hideRichInputPanel());
    binding.chatRichSend.setOnClickListener(
        v -> {
          String msg = binding.chatRichContentEt.getEditableText().toString();
          String title = binding.chatRichTitleEt.getEditableText().toString();
          if (messageProxy != null) {
            if (!TextUtils.isEmpty(title) && TextUtils.getTrimmedLength(title) > 0) {
              if (messageProxy.sendRichTextMessage(
                  title, msg, binding.chatBottomInputLayout.replyMessage)) {
                binding.chatRichTitleEt.setText("");
                binding.chatRichContentEt.setText("");
                hideRichInputPanel();
                binding.chatBottomInputLayout.clearReplyMsg();
              } else {
                clearInvalidInputContent();
              }
            } else {
              if (messageProxy.sendTextMessage(msg, binding.chatBottomInputLayout.replyMessage)) {
                binding.chatRichTitleEt.setText("");
                binding.chatRichContentEt.setText("");
                hideRichInputPanel();
                binding.chatBottomInputLayout.clearReplyMsg();
              } else {
                clearInvalidInputContent();
              }
            }
          }
        });

    binding.chatRichTitleEt.setOnEditorActionListener(
        (v, actionId, event) -> {
          if (actionId == EditorInfo.IME_ACTION_NEXT) {
            binding.chatRichContentEt.requestFocus();
            binding.chatRichContentEt.setSelection(
                binding.chatRichContentEt.getEditableText().length());
            return true;
          }
          return true;
        });
  }

  private void clearInvalidInputContent() {
    String msg = binding.chatRichContentEt.getEditableText().toString();
    String title = binding.chatRichTitleEt.getEditableText().toString();
    if (TextUtils.getTrimmedLength(title) < 1) {
      binding.chatRichTitleEt.setText("");
    }
    if (TextUtils.getTrimmedLength(msg) < 1) {
      binding.chatRichContentEt.setText("");
    }
  }

  @Override
  public void onTextAdd(String content, int start, int length, boolean hasAt) {
    if (binding.chatRichContentEt.getVisibility() == VISIBLE) {
      SpannableString spannableString =
          MessageHelper.generateAtSpanString(hasAt ? content : "@" + content);
      binding
          .chatRichContentEt
          .getEditableText()
          .replace(hasAt ? start : start - 1, start, spannableString);
    }
  }

  @Override
  public void onTextDelete(int start, int length) {

    if (binding.chatRichContentEt.getVisibility() == VISIBLE) {
      int end = start + length - 1;
      binding.chatRichContentEt.getEditableText().replace(start, end, "");
    }
  }

  @Override
  public void setMultiSelectEnable(boolean enable) {
    binding.chatMsgMultiTransmitIv.setEnabled(enable);
    binding.chatMsgMultiDeleteIv.setEnabled(enable);
    binding.chatMsgSingleTransmitIv.setEnabled(enable);
  }

  @Override
  public void showRichInputPanel() {
    binding.chatRichLayout.setVisibility(VISIBLE);
    if (aitTextManager != null) {
      aitTextManager.setAitTextChangeListener(this);
    }
    String title = binding.chatBottomInputLayout.getRichInputTitle();
    String content = binding.chatBottomInputLayout.getRichInputContent();
    MessageHelper.identifyExpressionForEditMsg(
        getContext(),
        binding.chatRichContentEt,
        content,
        aitTextManager != null ? aitTextManager.getAitContactsModel() : null);
    MessageHelper.identifyFaceExpression(
        getContext(), binding.chatRichTitleEt, title, ImageSpan.ALIGN_BOTTOM);
    binding.chatBottomInputLayout.clearInputEditTextChange();
    binding.chatRichContentEt.addTextChangedListener(richBodyInputTextWatcher);
    binding.chatRichTitleEt.addTextChangedListener(richTitleInputTextWatcher);
    binding.chatBottomInputLayout.hideCurrentInput();
    binding.chatBottomInputLayout.hideAndClearRichInput();
  }

  @Override
  public void hideRichInputPanel() {
    if (binding.chatRichLayout.getVisibility() == VISIBLE) {
      String title = binding.chatRichTitleEt.getText().toString();
      boolean titleFocus = binding.chatRichTitleEt.hasFocus() && !TextUtils.isEmpty(title);
      binding.chatBottomInputLayout.switchRichInput(
          titleFocus, title, binding.chatRichContentEt.getText().toString());
      binding.chatRichLayout.setVisibility(GONE);
      if (aitTextManager != null) {
        aitTextManager.setAitTextChangeListener(binding.chatBottomInputLayout);
      }
      binding.chatRichContentEt.removeTextChangedListener(richBodyInputTextWatcher);
      binding.chatRichTitleEt.removeTextChangedListener(richTitleInputTextWatcher);
      binding.chatRichContentEt.setText("");
      binding.chatRichTitleEt.setText("");
    }
  }

  public BackTitleBar getTitleBar() {
    return binding.chatViewTitle;
  }

  public FrameLayout getTitleBarLayout() {
    return binding.chatViewTitleLayout;
  }

  public ChatMessageListView getMessageListView() {
    return binding.messageView;
  }

  public void setLoadHandler(IMessageLoadHandler loadHandler) {
    binding.messageView.setLoadHandler(loadHandler);
  }

  public void setMessageReader(IMessageReader messageReader) {
    binding.messageView.setMessageReader(messageReader);
  }

  public void setItemClickListener(IMessageItemClickListener itemClickListener) {
    binding.messageView.setItemClickListener(itemClickListener);
  }

  public void setChatConfig(ChatUIConfig config) {
    if (config != null) {
      binding.messageView.setMessageProperties(config.messageProperties);
      binding.chatBottomInputLayout.setInputProperties(config.inputProperties);
    }
  }

  public void clearMessageList() {
    binding.messageView.clearMessageList();
  }

  public void addMessageListForward(List<ChatMessageBean> messageList) {
    binding.messageView.addMessageListForward(messageList);
  }

  public void appendMessageList(List<ChatMessageBean> messageList) {
    binding.messageView.appendMessageList(messageList);
  }

  public void appendMessageList(List<ChatMessageBean> messageList, boolean needToScrollEnd) {
    binding.messageView.appendMessageList(messageList, needToScrollEnd);
  }

  public void updateUserInfo(List<UserInfo> userInfoList) {
    binding.messageView.updateUserInfo(userInfoList);
  }

  public void setMessageBackground(Drawable drawable) {
    binding.chatViewBody.setBackground(drawable);
  }

  public void setMessageBackgroundRes(int res) {
    binding.chatViewBody.setBackgroundResource(res);
  }

  public void setMessageBackgroundColor(int color) {
    binding.chatViewBody.setBackgroundColor(color);
  }

  @Override
  public void setTitleBarVisible(int visible) {
    binding.chatViewTitleLayout.setVisibility(visible);
  }

  @Override
  public void setReeditMessage(String content) {
    binding.chatBottomInputLayout.setReEditMessage(content);
  }

  @Override
  public void setReEditRichMessage(String title, String body) {
    showRichInputPanel();
    binding.chatRichTitleEt.setText(title);
    binding.chatRichContentEt.setText(body);
  }

  @Override
  public void setReplyMessage(ChatMessageBean messageBean) {
    binding.chatBottomInputLayout.setReplyMessage(messageBean);
  }

  public void setTypeState(boolean isTyping) {
    if (isTyping) {
      binding.tvInputTip.setVisibility(VISIBLE);
    } else {
      binding.tvInputTip.setVisibility(GONE);
    }
  }

  public void setNetWorkState(boolean available) {
    if (available) {
      binding.tvNotification.setVisibility(GONE);
    } else {
      binding.tvNotification.setVisibility(VISIBLE);
      binding.tvNotification.setTextSize(14);
      binding.tvNotification.setText(R.string.chat_network_error_tip);
      binding.tvNotification.setTextColor(
          getContext().getResources().getColor(R.color.color_fc596a));
      binding.tvNotification.setBackgroundResource(R.color.color_fee3e6);
    }
  }

  @Override
  public void showMultiSelect(boolean show) {
    binding.chatBottomInputLayout.collapse(true);
    binding.chatMsgMultiSelectLayout.setVisibility(show ? VISIBLE : GONE);
    binding.chatViewBottom.setVisibility(show ? GONE : VISIBLE);
    binding.messageView.setMultiSelect(show);
    isMultiSelect = show;
    if (show) {
      binding.chatViewTitle.setActionText(R.string.cancel);
      binding.chatViewTitle.getActionTextView().setVisibility(VISIBLE);
      binding.chatViewTitle.getActionImageView().setVisibility(GONE);
      binding.chatViewTitle.setActionTextListener(v -> showMultiSelect(false));
      ChatMsgCache.clear();
    } else {
      ChatMsgCache.clear();
      binding.chatViewTitle.getActionTextView().setVisibility(GONE);
      binding.chatViewTitle.getActionImageView().setVisibility(VISIBLE);
      binding.chatViewTitle.setActionTextListener(null);
    }
  }

  @Override
  public boolean isMultiSelect() {
    return isMultiSelect;
  }

  public void appendMessage(ChatMessageBean message) {
    binding.messageView.appendMessage(message);
  }

  @Override
  public void deleteMessage(List<ChatMessageBean> message) {
    binding.messageView.deleteMessage(message);
  }

  @Override
  public List<ChatMessageBean> getMessageList() {
    if (binding.messageView.getMessageAdapter() != null) {
      return binding.messageView.getMessageAdapter().getMessageList();
    }
    return null;
  }

  @Override
  public void revokeMessage(ChatMessageBean message) {
    binding.messageView.revokeMessage(message);
  }

  public void updateMessageStatus(ChatMessageBean message) {
    binding.messageView.updateMessageStatus(message);
  }

  public void updateMessage(ChatMessageBean message, Object payload) {
    binding.messageView.updateMessage(message, payload);
  }

  public void updateMessage(IMMessage message, Object payload) {
    binding.messageView.updateMessage(message, payload);
  }

  public void updateProgress(AttachmentProgress progress) {
    binding.messageView.updateAttachmentProgress(progress);
  }

  @Override
  public void hideCurrentInput() {
    binding.chatBottomInputLayout.hideCurrentInput();
  }

  @Override
  public void updateInputHintInfo(String content) {
    binding.chatBottomInputLayout.updateInputInfo(content);
    binding.chatRichContentEt.setHint(binding.chatBottomInputLayout.getInputHit());
  }

  @Override
  public void setInputMute(boolean mute) {
    if (mute) {
      hideRichInputPanel();
    }
    binding.chatBottomInputLayout.setMute(mute);
  }

  public void setMessageProxy(IMessageProxy proxy) {
    messageProxy = proxy;
    binding.chatBottomInputLayout.init(proxy);
  }

  public MessageBottomLayout getBottomInputLayout() {
    return binding.chatBottomInputLayout;
  }

  public FrameLayout getChatBodyLayout() {
    return binding.chatViewBody;
  }

  public FrameLayout getChatBottomLayout() {
    return binding.chatViewBottom;
  }

  public NormalChatViewBinding getChatViewLayoutBinding() {
    return binding;
  }

  public FrameLayout getChatBodyTopLayout() {
    return binding.chatViewBodyTop;
  }

  public FrameLayout getChatBodyBottomLayout() {
    return binding.chatViewBodyBottom;
  }

  public void setAitManager(AitManager manager) {
    manager.setAitTextChangeListener(binding.chatBottomInputLayout);
    aitTextManager = manager;
    binding.chatBottomInputLayout.setAitTextWatcher(manager);
  }

  public void setMessageViewHolderFactory(IChatFactory viewHolderFactory) {
    binding.messageView.setViewHolderFactory(viewHolderFactory);
  }

  public void setLayoutCustom(IChatViewCustom layoutCustom) {
    if (layoutCustom != null) {
      layoutCustom.customizeChatLayout(this);
    }
  }

  public View getRootView() {
    return binding.getRoot();
  }

  private TextWatcher richTitleInputTextWatcher =
      new TextWatcher() {
        private int start;
        private int count;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
          if (!canRender) {
            return;
          }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
          if (!canRender) {
            return;
          }
          this.start = start;
          this.count = count;
          if (messageProxy != null) {
            messageProxy.onTypeStateChange(!TextUtils.isEmpty(s));
          }
        }

        @Override
        public void afterTextChanged(Editable s) {
          if (!canRender) {
            canRender = true;
            return;
          }
          SpannableString spannableString = new SpannableString(s);
          if (MessageHelper.replaceEmoticons(getContext(), spannableString, start, count)) {
            canRender = false;
            binding.chatRichTitleEt.setText(spannableString);
            binding.chatRichTitleEt.setSelection(spannableString.length());
          }
          if (TextUtils.isEmpty(s.toString()) && binding.chatRichTitleEt.isEnabled()) {
            binding.chatRichTitleEt.setHint(
                getRootView().getContext().getString(R.string.chat_message_rich_title_hint));
          }
        }
      };

  private TextWatcher richBodyInputTextWatcher =
      new TextWatcher() {
        private int start;
        private int count;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
          if (!canRender) {
            return;
          }
          if (aitTextManager != null) {
            aitTextManager.beforeTextChanged(s, start, count, after);
          }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
          if (!canRender) {
            return;
          }
          this.start = start;
          this.count = count;
          if (aitTextManager != null) {
            aitTextManager.onTextChanged(s, start, before, count);
          }
          if (messageProxy != null) {
            messageProxy.onTypeStateChange(!TextUtils.isEmpty(s));
          }
        }

        @Override
        public void afterTextChanged(Editable s) {
          if (!canRender) {
            canRender = true;
            return;
          }
          SpannableString spannableString = new SpannableString(s);
          if (MessageHelper.replaceEmoticons(getContext(), spannableString, start, count)) {
            canRender = false;
            binding.chatRichContentEt.setText(spannableString);
            binding.chatRichContentEt.setSelection(spannableString.length());
          }
          if (aitTextManager != null) {
            aitTextManager.afterTextChanged(s);
          }
          if (TextUtils.isEmpty(s.toString()) && binding.chatRichContentEt.isEnabled()) {
            binding.chatRichContentEt.setHint(binding.chatBottomInputLayout.getInputHit());
          }
        }
      };
}
