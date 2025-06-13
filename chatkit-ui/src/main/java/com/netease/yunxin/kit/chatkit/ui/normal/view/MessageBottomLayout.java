// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.view;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.media.record.IAudioRecordCallback;
import com.netease.nimlib.sdk.media.record.RecordType;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.NormalChatMessageBottomViewBinding;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageProxy;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.normal.factory.BottomActionFactory;
import com.netease.yunxin.kit.chatkit.ui.textSelectionHelper.SelectableTextHelper;
import com.netease.yunxin.kit.chatkit.ui.view.IItemActionListener;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitManager;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitTextChangeListener;
import com.netease.yunxin.kit.chatkit.ui.view.emoji.IEmojiSelectedListener;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionsPanel;
import com.netease.yunxin.kit.chatkit.ui.view.input.InputActionAdapter;
import com.netease.yunxin.kit.chatkit.ui.view.input.InputProperties;
import com.netease.yunxin.kit.chatkit.ui.view.input.InputState;
import com.netease.yunxin.kit.chatkit.ui.view.message.audio.ChatMessageAudioControl;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import com.netease.yunxin.kit.common.ui.dialog.BottomChoiceDialog;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.KeyboardUtils;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.common.utils.XKitUtils;
import java.io.File;
import java.util.List;

/** 标准皮肤，聊天页面底部输入框View。 */
public class MessageBottomLayout extends FrameLayout
    implements IAudioRecordCallback, AitTextChangeListener, IItemActionListener {
  public static final String TAG = "MessageBottomLayout";
  private static final long SHOW_DELAY_TIME = 200;
  private NormalChatMessageBottomViewBinding mBinding;
  private InputActionAdapter actionAdapter;
  private IMessageProxy mProxy;
  private String mEdieNormalHint = "";
  private boolean mMute = false;
  private InputProperties inputProperties;
  private ActionClickListener actionClickListener;
  ChatMessageBean replyMessage;

  private final ActionsPanel mActionsPanel = new ActionsPanel();
  private AitManager aitTextWatcher;

  private boolean isKeyboardShow = false;
  private InputState mInputState = InputState.none;
  private IEmojiSelectedListener emojiSelectedListener;

  private boolean canRender = true;

  private boolean keepRichEt = false;

  public MessageBottomLayout(@NonNull Context context) {
    this(context, null);
  }

  public MessageBottomLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MessageBottomLayout(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initView();
  }

  public void init(IMessageProxy proxy) {
    this.init(BottomActionFactory.assembleDefaultInputActions(), proxy);
  }

  public void init(List<ActionItem> items, IMessageProxy proxy) {
    mProxy = proxy;
    actionAdapter = new InputActionAdapter(items, this);
    actionAdapter.disableAll(mMute);
    mBinding.chatMessageActionContainer.setAdapter(actionAdapter);
    mBinding.chatMessageRecordView.setRecordCallback(this);
    mBinding.chatMessageRecordView.setPermissionRequest(
        permission -> {
          return mProxy.hasPermission(new String[] {Manifest.permission.RECORD_AUDIO});
        });
    emojiSelectedListener =
        new IEmojiSelectedListener() {
          @Override
          public void onEmojiSelected(String key) {
            Editable mEditable = mBinding.chatMessageInputEt.getText();
            if (key.equals("/DEL")) {
              mBinding.chatMessageInputEt.dispatchKeyEvent(
                  new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            } else {
              int start = mBinding.chatMessageInputEt.getSelectionStart();
              int end = mBinding.chatMessageInputEt.getSelectionEnd();
              start = Math.max(start, 0);
              mEditable.replace(start, end, key);
            }
          }

          @Override
          public void onEmojiSendClick() {
            sendText(replyMessage);
          }
        };
    mBinding.llyReply.setVisibility(GONE);
    mBinding.chatMessageInputEt.setOnFocusChangeListener(
        (v, hasFocus) ->
            mProxy.onTypeStateChange(
                !TextUtils.isEmpty(mBinding.chatMessageInputEt.getText()) && hasFocus));

    if (mProxy.getConversationType() == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P
        && IMKitConfigCenter.getEnableAIChatHelper()) {
      mBinding.chatMsgInputAIHelper.setVisibility(View.VISIBLE);
      mBinding.chatMsgInputAIHelper.setOnClickListener(
          v -> {
            switchAIHelper();
          });
      mBinding.chatMessageAiHelperView.setAIHelperClickListener(
          new AIHelperView.AIHelperClickListener() {

            @Override
            public void onShow() {
              if (mProxy != null) {
                mProxy.onAIHelperClick(
                    mBinding.chatMessageAiHelperView, ActionConstants.ACTION_AI_HELPER_SHOW);
              }
            }

            @Override
            public void onRefreshClick() {
              if (mProxy != null) {
                mProxy.onAIHelperClick(
                    mBinding.chatMessageAiHelperView, ActionConstants.ACTION_AI_HELPER_REFRESH);
              }
            }

            @Override
            public void onTryAgainClick() {
              if (mProxy != null) {
                mProxy.onAIHelperClick(
                    mBinding.chatMessageAiHelperView, ActionConstants.ACTION_AI_HELPER_REFRESH);
              }
            }

            @Override
            public void onItemClick(AIHelperView.AIHelperItem Item) {
              if (Item != null && !TextUtils.isEmpty(Item.content)) {
                boolean sendMsgSuccess = mProxy.sendTextMessage(Item.content, null);
                if (actionClickListener != null) {
                  actionClickListener.sendMessage(Item.content, sendMsgSuccess);
                }
              }
            }

            @Override
            public void onItemEditClick(AIHelperView.AIHelperItem Item) {
              if (Item != null && !TextUtils.isEmpty(Item.content)) {
                mBinding.chatMessageInputEt.setText(Item.content);
                switchInput();
              }
            }
          });
    } else {
      mBinding.chatMsgInputAIHelper.setVisibility(View.GONE);
    }
  }

  public NormalChatMessageBottomViewBinding getViewBinding() {
    return mBinding;
  }

  public ImageView getInputSwitchView() {
    return mBinding.chatMsgInputSwitch;
  }

  @Override
  public void onClick(View view, int position, ActionItem item) {
    ALog.d(TAG, "action click, inputState:" + mInputState);
    if (mProxy != null && mProxy.onActionClick(view, item.getAction())) {
      return;
    }
    switch (item.getAction()) {
      case ActionConstants.ACTION_TYPE_RECORD:
        switchRecord();
        break;
      case ActionConstants.ACTION_TYPE_EMOJI:
        switchEmoji();
        break;
      case ActionConstants.ACTION_TYPE_ALBUM:
        onAlbumClick();
        break;
      case ActionConstants.ACTION_TYPE_FILE:
        onFileClick();
        break;
      case ActionConstants.ACTION_TYPE_MORE:
        switchMore();
        break;
      case ActionConstants.ACTION_TYPE_CAMERA:
        onCameraClick();
        break;
      case ActionConstants.ACTION_TYPE_LOCATION:
        onLocationClick();
        break;
      case ActionConstants.ACTION_TYPE_VIDEO_CALL:
        onCallClick();
        break;
      case ActionConstants.ACTION_TYPE_TRANSLATE:
        switchTranslate(view, item.getAction());
        break;
      default:
        mProxy.onCustomAction(view, item.getAction());
        break;
    }
  }

  public void setAitTextWatcher(AitManager aitTextWatcher) {
    this.aitTextWatcher = aitTextWatcher;
  }

  public void setInputEditTextHint(String name) {
    mEdieNormalHint = getContext().getString(R.string.chat_message_send_hint, name);
    if (TextUtils.isEmpty(mBinding.chatMessageInputEt.getText().toString())
        && mBinding.chatMessageInputEt.isEnabled()) {
      mBinding.chatMessageInputEt.setHint(mEdieNormalHint);
    }
  }

  public String getInputEditTextHint() {
    return mEdieNormalHint;
  }

  public void clearInputEditTextChange() {
    mBinding.chatMessageInputEt.removeTextChangedListener(msgInputTextWatcher);
  }

  public void setActionClickListener(ActionClickListener listener) {
    actionClickListener = listener;
  }

  @SuppressLint("ClickableViewAccessibility")
  private void initView() {
    mBinding =
        NormalChatMessageBottomViewBinding.inflate(LayoutInflater.from(getContext()), this, true);
    getViewTreeObserver()
        .addOnGlobalLayoutListener(
            () -> {
              if (KeyboardUtils.isKeyboardShow((Activity) getContext())) {
                if (!isKeyboardShow) {
                  onKeyboardShow();
                  isKeyboardShow = true;
                }
              } else {
                if (isKeyboardShow) {
                  onKeyboardHide();
                  isKeyboardShow = false;
                }
              }
            });
    // input view
    mBinding.chatMessageInputEt.addTextChangedListener(msgInputTextWatcher);
    mBinding.chatMessageInputEt.setOnTouchListener(
        (v, event) -> {
          if (event.getAction() == MotionEvent.ACTION_DOWN) {
            switchInput();
          }
          return false;
        });
    mBinding.chatMessageInputEt.setOnEditorActionListener(
        (v, actionId, event) -> {
          if (actionId == EditorInfo.IME_ACTION_SEND) {
            sendText(replyMessage);
          }
          return true;
        });
    mBinding.chatRichEt.setOnEditorActionListener(
        (v, actionId, event) -> {
          if (actionId == EditorInfo.IME_ACTION_SEND) {
            sendText(replyMessage);
          }
          return true;
        });

    // action
    mBinding.chatMessageActionContainer.setLayoutManager(
        new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
    // 多行消息设置点击事件
    if (IMKitConfigCenter.getEnableRichTextMessage()) {
      mBinding.chatMsgInputSwitch.setVisibility(VISIBLE);
    } else {
      mBinding.chatMsgInputSwitch.setVisibility(GONE);
    }
    mBinding.chatRichEt.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s.toString()) && !keepRichEt) {
              mBinding.chatRichEt.setVisibility(GONE);
              mBinding.chatMessageInputEt.requestFocus();
            }
            keepRichEt = false;
          }
        });
    loadConfig();
  }

  private final TextWatcher msgInputTextWatcher =
      new TextWatcher() {
        private int start;
        private int count;

        //保存输入框的内容，和之后的做对比
        private String editable;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
          if (!canRender) {
            return;
          }
          if (aitTextWatcher != null) {
            aitTextWatcher.beforeTextChanged(s, start, count, after);
          }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
          if (!canRender) {
            return;
          }
          this.start = start;
          this.count = count;
          if (aitTextWatcher != null) {
            aitTextWatcher.onTextChanged(s, start, before, count);
          }
          if (mProxy != null) {
            mProxy.onTypeStateChange(!TextUtils.isEmpty(s));
          }
        }

        @Override
        public void afterTextChanged(Editable s) {
          if (!canRender) {
            canRender = true;
            return;
          }
          //隐藏文本选择器选择框
          SelectableTextHelper.getInstance().dismiss();
          SpannableString spannableString = new SpannableString(s);
          if (MessageHelper.replaceEmoticons(getContext(), spannableString, start, count)) {
            canRender = false;
            mBinding.chatMessageInputEt.setText(spannableString);
            mBinding.chatMessageInputEt.setSelection(spannableString.length());
          }
          if (aitTextWatcher != null && !TextUtils.equals(editable, s)) {
            aitTextWatcher.afterTextChanged(s);
          }
          if (TextUtils.isEmpty(s.toString()) && mBinding.chatMessageInputEt.isEnabled()) {
            mBinding.chatMessageInputEt.setHint(mEdieNormalHint);
          }
          editable = s.toString();
        }
      };

  public void setAIHelperData(List<AIHelperView.AIHelperItem> itemList) {
    mBinding.chatMessageAiHelperView.setHelperContent(itemList);
  }

  public void sendText(ChatMessageBean replyMessage) {
    if (mProxy != null) {
      String msg = mBinding.chatMessageInputEt.getEditableText().toString();
      String title = mBinding.chatRichEt.getEditableText().toString();
      boolean sendMsgSuccess;
      if (!TextUtils.isEmpty(title) && TextUtils.getTrimmedLength(title) > 0) {
        sendMsgSuccess = mProxy.sendRichTextMessage(title, msg, replyMessage);
      } else {
        sendMsgSuccess = mProxy.sendTextMessage(msg, replyMessage);
      }
      clearEditTextInput(sendMsgSuccess);
      if (actionClickListener != null) {
        actionClickListener.sendMessage(msg, sendMsgSuccess);
      }
    }
  }

  // 清空输入框，forceClear为true时强制清空(包括输入框内容、回复消息、翻译内容)，false时只有输入框内容为空时才清空
  public void clearEditTextInput(boolean forceClear) {
    if (forceClear) {
      mBinding.chatMessageInputEt.setText("");
      mBinding.chatRichEt.setText("");
      clearReplyMsg();
    } else {
      String msg = mBinding.chatMessageInputEt.getEditableText().toString();
      String title = mBinding.chatRichEt.getEditableText().toString();
      if (TextUtils.getTrimmedLength(msg) < 1) {
        mBinding.chatMessageInputEt.setText("");
      }
      if (TextUtils.getTrimmedLength(title) < 1) {
        keepRichEt = true;
        mBinding.chatRichEt.setText("");
      }
    }
  }

  // 清空富文本状态下输入框内容，并隐藏富文本输入框
  public void hideAndClearRichInput() {
    mBinding.chatRichEt.setText("");
    mBinding.chatRichEt.setVisibility(GONE);
  }

  public void hideCurrentInput() {
    if (mInputState == InputState.input) {
      hideKeyboard();
    } else if (mInputState == InputState.voice) {
      recordShow(false, 0);
    } else if (mInputState == InputState.emoji) {
      emojiShow(false, 0);
    } else if (mInputState == InputState.more) {
      morePanelShow(false, 0);
    } else if (mInputState == InputState.aiHelper) {
      aiHelperShow(false);
    }
  }

  public void setRichTextSwitchListener(OnClickListener listener) {
    mBinding.chatMsgInputSwitch.setOnClickListener(listener);
  }

  // 获取富文本标题
  public String getRichInputTitle() {
    return mBinding.chatRichEt.getText().toString();
  }

  // 获取富文本内容，非富文本状态则返回普通小心文本
  public String getRichInputContent() {
    return mBinding.chatMessageInputEt.getText().toString();
  }

  // 切换到富文本输入模式
  public void switchRichInput(boolean titleForces, String title, String content) {
    hideCurrentInput();
    mInputState = InputState.input;
    if (!TextUtils.isEmpty(title)) {
      mBinding.chatRichEt.setVisibility(VISIBLE);
      MessageHelper.identifyFaceExpression(
          getContext(), mBinding.chatRichEt, title, ImageSpan.ALIGN_BOTTOM);

    } else {
      mBinding.chatRichEt.setText("");
      mBinding.chatRichEt.setVisibility(GONE);
    }
    MessageHelper.identifyExpressionForRichTextMsg(
        getContext(),
        mBinding.chatMessageInputEt,
        content,
        aitTextWatcher != null ? aitTextWatcher.getAitContactsModel() : null);
    if (!TextUtils.isEmpty(content)) {
      mBinding.chatMessageInputEt.setSelection(content.length());
    }
    mBinding.chatMessageInputEt.addTextChangedListener(msgInputTextWatcher);
  }

  // 切换到普通文本输入模式
  public void switchInput() {
    if (mInputState == InputState.input) {
      return;
    }
    hideCurrentInput();
    showKeyboard();
    mInputState = InputState.input;
  }

  // 切换到语音输入模式
  public void switchRecord() {
    if (mInputState == InputState.voice) {
      recordShow(false, 0);
      mInputState = InputState.none;
      return;
    }
    recordShow(true, 0);
    hideCurrentInput();
    mInputState = InputState.voice;
  }

  // 切换到翻译输入模式
  public void switchTranslate(View view, String action) {
    if (actionClickListener != null) {
      actionClickListener.onActionClick(view, action);
    }
    mProxy.onTranslateAction();
  }

  public void switchAIHelper() {
    if (mInputState == InputState.aiHelper) {
      aiHelperShow(false);
      mInputState = InputState.none;
      return;
    }
    aiHelperShow(true);
    hideCurrentInput();
    mInputState = InputState.aiHelper;
  }

  public void aiHelperShow(boolean show) {
    if (show) {
      mBinding.chatMsgInputAIHelper.setChecked(true);
      mBinding.chatMessageAiHelperView.show();
      mBinding.chatMessageActionContainer.setVisibility(GONE);
      mBinding.chatMessageInputRoot.setBackgroundResource(R.drawable.bg_chat_ai_helper);
    } else {
      mBinding.chatMsgInputAIHelper.setChecked(false);
      mBinding.chatMessageAiHelperView.hide();
      mBinding.chatMessageActionContainer.setVisibility(VISIBLE);
      mBinding.chatMessageInputRoot.setBackgroundResource(R.color.color_eef1f4);
    }
  }

  public EditText getInputEditText() {
    return mBinding.chatMessageInputEt;
  }

  // 显示或隐藏语音输入界面
  public void recordShow(boolean show, long delay) {
    postDelayed(() -> mBinding.chatMessageRecordView.setVisibility(show ? VISIBLE : GONE), delay);
    actionAdapter.updateItemState(ActionConstants.ACTION_TYPE_RECORD, show);
  }

  // 切换到表情输入模式
  public void switchEmoji() {
    if (mInputState == InputState.emoji) {
      emojiShow(false, 0);
      mInputState = InputState.none;
      return;
    }
    emojiShow(true, 0);
    hideCurrentInput();
    mInputState = InputState.emoji;
  }

  // 显示或隐藏表情输入界面
  public void emojiShow(boolean show, long delay) {
    postDelayed(
        () -> {
          mBinding.chatMessageEmojiView.setVisibility(show ? VISIBLE : GONE);
          if (show) {
            mBinding.chatMessageEmojiView.show(emojiSelectedListener);
          }
        },
        delay);
    actionAdapter.updateItemState(ActionConstants.ACTION_TYPE_EMOJI, show);
  }

  // 切换到更多输入模式
  public void switchMore() {
    if (mInputState == InputState.more) {
      morePanelShow(false, 0);
      mInputState = InputState.none;
      return;
    }
    morePanelShow(true, 0);
    hideCurrentInput();
    mInputState = InputState.more;
  }

  // 显示或隐藏更多输入界面
  public void morePanelShow(boolean show, long delay) {
    if (!mActionsPanel.hasInit() && show) {
      mActionsPanel.init(
          mBinding.chatMessageActionsPanel,
          BottomActionFactory.assembleInputMoreActions(
              V2NIMConversationIdUtil.conversationTargetId(mProxy.getConversationId()),
              mProxy.getConversationType()),
          this);
    }
    postDelayed(() -> mBinding.chatMessageActionsPanel.setVisibility(show ? VISIBLE : GONE), delay);
    actionAdapter.updateItemState(ActionConstants.ACTION_TYPE_MORE, show);
  }

  // 选择图片
  public void onAlbumClick() {
    if (mInputState == InputState.input) {
      hideKeyboard();
      postDelayed(() -> mProxy.pickMedia(), SHOW_DELAY_TIME);
    } else {
      mProxy.pickMedia();
    }
  }

  // 拍照或录像
  public void onCameraClick() {
    BottomChoiceDialog dialog =
        new BottomChoiceDialog(this.getContext(), BottomActionFactory.assembleTakeShootActions());
    dialog.setOnChoiceListener(
        new BottomChoiceDialog.OnChoiceListener() {
          @Override
          public void onChoice(@NonNull String type) {
            if (!XKitUtils.getApplicationContext()
                .getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
              ToastX.showShortToast(R.string.chat_message_camera_unavailable);
              return;
            }
            switch (type) {
              case ActionConstants.ACTION_TYPE_TAKE_PHOTO:
                mProxy.takePicture();
                break;
              case ActionConstants.ACTION_TYPE_TAKE_VIDEO:
                mProxy.captureVideo();
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

  // 发起语音或视频通话
  public void onCallClick() {
    BottomChoiceDialog dialog =
        new BottomChoiceDialog(this.getContext(), BottomActionFactory.assembleVideoCallActions());
    dialog.setOnChoiceListener(
        new BottomChoiceDialog.OnChoiceListener() {
          @Override
          public void onChoice(@NonNull String type) {
            if (!NetworkUtils.isConnected()) {
              ToastX.showShortToast(R.string.chat_network_error_tip);
              return;
            }
            if (!XKitUtils.getApplicationContext()
                .getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
              ToastX.showShortToast(R.string.chat_message_camera_unavailable);
              return;
            }
            switch (type) {
              case ActionConstants.ACTION_TYPE_VIDEO_CALL_ACTION:
                mProxy.videoCall();
                break;
              case ActionConstants.ACTION_TYPE_AUDIO_CALL_ACTION:
                mProxy.audioCall();
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

  // 发送位置信息
  public void onLocationClick() {
    mProxy.sendLocationLaunch();
  }

  // 发送文件
  public void onFileClick() {
    if (mInputState == InputState.input) {
      hideKeyboard();
      postDelayed(() -> mProxy.sendFile(), SHOW_DELAY_TIME);
    } else {
      mProxy.sendFile();
      clearReplyMsg();
    }
  }

  // 设置是否禁言
  public void setMute(boolean mute) {
    if (mute != mMute) {
      mMute = mute;
      mBinding.chatMessageInputEt.setEnabled(!mute);
      mBinding.chatMessageInputEt.setText("");
      mBinding.chatRichEt.setText("");
      String hint = mute ? getContext().getString(R.string.chat_team_all_mute) : mEdieNormalHint;
      mBinding.chatMessageInputEt.setHint(hint);
      if (mute) {
        collapse(true);
      }
      mBinding.chatMessageInputLayout.setBackgroundResource(
          mute ? R.color.color_e3e4e4 : R.color.color_white);
      if (actionAdapter != null) {
        actionAdapter.disableAll(mute);
      }
    }
  }

  public boolean isMute() {
    return mMute;
  }

  // 收起更多区域
  public void collapse(boolean immediately) {
    if (mInputState == InputState.none) {
      return;
    }
    hideAllInputLayout(immediately);
  }

  // 设置被回复消息
  public void setReplyMessage(ChatMessageBean messageBean) {
    this.replyMessage = messageBean;
    mBinding.llyReply.setVisibility(VISIBLE);
    String tips = MessageHelper.getReplyMessageTips(messageBean.getMessageData());
    tips = String.format(getContext().getString(R.string.chat_message_reply_someone), tips);
    MessageHelper.identifyFaceExpression(
        getContext(),
        mBinding.tvReplyContent,
        tips,
        ImageSpan.ALIGN_CENTER,
        MessageHelper.SMALL_SCALE);
    mBinding.ivReplyClose.setOnClickListener(v -> clearReplyMsg());
    switchInput();
  }

  // 获取被回复消息
  public ChatMessageBean getReplyMessage() {
    return replyMessage;
  }

  // 设置输入框内容
  public void setInputEditTextContent(String msgContent) {

    if (aitTextWatcher != null
        && !aitTextWatcher.getAitContactsModel().getAtBlockList().isEmpty()) {
      MessageHelper.identifyExpressionForRichTextMsg(
          getContext(),
          mBinding.chatMessageInputEt,
          msgContent,
          aitTextWatcher.getAitContactsModel());
    } else {
      mBinding.chatMessageInputEt.setText(msgContent);
    }
    mBinding.chatMessageInputEt.requestFocus();
    mBinding.chatMessageInputEt.setSelection(mBinding.chatMessageInputEt.getText().length());
    switchInput();
  }

  // 清空回复消息
  public void clearReplyMsg() {
    replyMessage = null;
    mBinding.llyReply.setVisibility(GONE);
  }

  // 设置输入框UI个性化配置
  public void setInputProperties(InputProperties properties) {
    this.inputProperties = properties;
    loadConfig();
  }

  private void hideAllInputLayout(boolean immediately) {
    postDelayed(
        () -> {
          mInputState = InputState.none;
          KeyboardUtils.hideKeyboard(this);
          long delay = immediately ? 0 : SHOW_DELAY_TIME;
          recordShow(false, delay);
          emojiShow(false, delay);
          morePanelShow(false, delay);
          aiHelperShow(false);
        },
        immediately ? 0 : ViewConfiguration.getDoubleTapTimeout());
  }

  @Override
  public void onRecordReady() {
    ALog.d(LIB_TAG, TAG, "onRecordReady");
    ChatMessageAudioControl.getInstance().stopAudio();
  }

  @Override
  public void onRecordStart(File audioFile, RecordType recordType) {
    ALog.d(LIB_TAG, TAG, "onRecordStart");
    startRecord();
  }

  @Override
  public void onRecordSuccess(File audioFile, long audioLength, RecordType recordType) {
    ALog.d(
        LIB_TAG,
        TAG,
        "onRecordSuccess -->> file:" + audioFile.getName() + " length:" + audioLength);
    endRecord();
    mProxy.sendAudio(audioFile, (int) audioLength, replyMessage);
    clearReplyMsg();
  }

  @Override
  public void onRecordFail() {
    ALog.d(LIB_TAG, TAG, "onRecordFail");
    endRecord();
  }

  @Override
  public void onRecordCancel() {
    ALog.d(LIB_TAG, TAG, "onRecordCancel");
    endRecord();
  }

  @Override
  public void onRecordReachedMaxTime(int maxTime) {
    ALog.d(LIB_TAG, TAG, "onRecordReachedMaxTime -->> " + maxTime);
    mBinding.chatMessageRecordView.recordReachMaxTime(maxTime);
  }

  private void startRecord() {
    mBinding.chatMessageVoiceInTip.setVisibility(VISIBLE);
    mBinding.chatMessageEditInput.setVisibility(INVISIBLE);
    mBinding.chatMessageRecordView.startRecord();
  }

  private void endRecord() {
    mBinding.chatMessageVoiceInTip.setVisibility(GONE);
    mBinding.chatMessageEditInput.setVisibility(VISIBLE);
    mBinding.chatMessageRecordView.endRecord();
  }

  private void onKeyboardShow() {
    ALog.d(LIB_TAG, TAG, "onKeyboardShow inputState:" + mInputState);
    if (mInputState != InputState.input) {
      hideCurrentInput();
      mInputState = InputState.input;
    }
  }

  private void onKeyboardHide() {
    ALog.d(LIB_TAG, TAG, "onKeyboardHide inputState:" + mInputState);
    if (mInputState == InputState.input) {
      mInputState = InputState.none;
    }
  }

  private void hideKeyboard() {
    KeyboardUtils.hideKeyboard(mBinding.chatMessageInputEt);
    mBinding.chatMessageInputEt.clearFocus();
    mBinding.chatRichEt.clearFocus();
  }

  private void showKeyboard() {
    mBinding.chatMessageInputEt.requestFocus();
    mBinding.chatMessageInputEt.setSelection(mBinding.chatMessageInputEt.getText().length());
    KeyboardUtils.showKeyboard(mBinding.chatMessageInputEt);
  }

  @Override
  public void onTextAdd(String content, int start, int length, boolean hasAt) {
    if (mInputState != InputState.input) {
      postDelayed(this::switchInput, SHOW_DELAY_TIME);
    }

    SpannableString spannableString =
        MessageHelper.generateAtSpanString(hasAt ? content : "@" + content);
    mBinding
        .chatMessageInputEt
        .getEditableText()
        .replace(hasAt ? start : start - 1, start, spannableString);
  }

  @Override
  public void onTextDelete(int start, int length) {
    if (mInputState != InputState.input) {
      postDelayed(this::switchInput, SHOW_DELAY_TIME);
    }
    int end = start + length - 1;
    mBinding.chatMessageInputEt.getEditableText().replace(start, end, "");
  }

  public void loadConfig() {
    if (this.inputProperties != null) {

      if (inputProperties.inputBarBg != null) {
        mBinding.chatMessageInputRoot.setBackground(inputProperties.inputBarBg);
      }

      if (inputProperties.inputEditBg != null) {
        mBinding.chatMessageInputLayout.setBackground(inputProperties.inputEditBg);
      }

      if (inputProperties.inputMoreBg != null) {
        mBinding.chatMessageInputContainer.setBackground(inputProperties.inputMoreBg);
      }

      if (inputProperties.inputReplyBg != null) {
        mBinding.llyReply.setBackground(inputProperties.inputReplyBg);
      }

      if (inputProperties.inputReplyTextColor != null) {
        mBinding.tvReplyContent.setTextColor(inputProperties.inputReplyTextColor);
      }

      if (inputProperties.inputEditTextColor != null) {
        mBinding.chatMessageInputEt.setTextColor(inputProperties.inputEditTextColor);
      }

      if (inputProperties.inputEditHintTextColor != null) {
        mBinding.chatMessageInputEt.setHintTextColor(inputProperties.inputEditHintTextColor);
      }
    }
  }

  // View 之间的交互接口 ChatView使用
  public interface ActionClickListener {
    public void onActionClick(View view, String action);

    void sendMessage(String msg, boolean sendResult);
  }
}
