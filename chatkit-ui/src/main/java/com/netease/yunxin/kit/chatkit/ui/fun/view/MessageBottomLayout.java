// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.view;

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
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.media.record.AudioRecorder;
import com.netease.nimlib.sdk.media.record.IAudioRecordCallback;
import com.netease.nimlib.sdk.media.record.RecordType;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageBottomViewBinding;
import com.netease.yunxin.kit.chatkit.ui.fun.FunAudioRecordDialog;
import com.netease.yunxin.kit.chatkit.ui.fun.factory.FunBottomActionFactory;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageProxy;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.normal.view.AIHelperView;
import com.netease.yunxin.kit.chatkit.ui.textSelectionHelper.SelectableTextHelper;
import com.netease.yunxin.kit.chatkit.ui.view.IItemActionListener;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitManager;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitTextChangeListener;
import com.netease.yunxin.kit.chatkit.ui.view.emoji.IEmojiSelectedListener;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionsPanel;
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

/**
 * 聊天界面底部输入布局
 *
 * <p>包含文本输入、语音输入、表情输入、更多输入
 */
public class MessageBottomLayout extends FrameLayout
    implements AitTextChangeListener, IItemActionListener {
  public static final String TAG = "MessageBottomLayout";
  private static final long SHOW_DELAY_TIME = 200;
  private FunChatMessageBottomViewBinding mBinding;
  // 消息发送
  private IMessageProxy mProxy;
  private ActionClickListener actionClickListener;
  // 输入框提示
  private String mEditHintText = "";
  // 是否禁言
  private boolean mMute = false;
  // 输入框属性配置
  private InputProperties inputProperties;
  // 被回复消息
  ChatMessageBean replyMessage;

  private final ActionsPanel mActionsPanel = new ActionsPanel();
  // @输入监听
  private AitManager aitTextWatcher;

  // 键盘是否显示
  private boolean isKeyboardShow = false;
  // 输入状态包括：文本输入、语音输入、表情输入、更多输入
  private InputState mInputState = InputState.none;
  // 表情选择监听
  private IEmojiSelectedListener emojiSelectedListener;

  // 语音输入监听
  private IAudioRecordCallback audioRecordCallback;

  // 语音输入对话框
  private FunAudioRecordDialog recordDialog;

  private boolean inRecordOpView;

  // 语音输入
  private AudioRecorder mAudioRecorder;

  private final int recordMaxDuration = 60;

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

  @SuppressLint("ClickableViewAccessibility")
  public void init(IMessageProxy proxy) {
    mProxy = proxy;
    mBinding.inputEmojiRb.setOnClickListener(v -> switchEmoji());
    mBinding.inputMoreRb.setOnClickListener(v -> switchMore());
    mBinding.inputAudioRb.setOnClickListener(v -> switchRecord());

    // 语音输入
    mBinding.inputAudioTv.setOnTouchListener(
        (v, event) -> {
          ALog.d(LIB_TAG, TAG, "inputAudioTv OnTouch, event:" + event.getAction());
          if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mProxy.hasPermission(new String[] {Manifest.permission.RECORD_AUDIO})) {
              showAudioInputDialog();
            } else {
              return false;
            }
          } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (recordDialog != null) {
              int x = (int) event.getRawX();
              int y = (int) event.getRawY();
              inRecordOpView = recordDialog.getOpViewRect().contains(x, y);
              if (inRecordOpView) {
                recordDialog.showCancelView();
              } else {
                recordDialog.showRecordingView();
              }
            }
          } else if (event.getAction() == MotionEvent.ACTION_UP
              || event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (recordDialog != null) {
              dismissAudioInputDialog(inRecordOpView);
            }
          }
          return true;
        });

    // 表情选择监听
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

    // 语音输入监听
    audioRecordCallback =
        new IAudioRecordCallback() {
          @Override
          public void onRecordReady() {
            ALog.d(LIB_TAG, TAG, "onRecordReady");
            ChatMessageAudioControl.getInstance().stopAudio();
          }

          @Override
          public void onRecordStart(File audioFile, RecordType recordType) {
            ALog.d(LIB_TAG, TAG, "onRecordStart");
          }

          @Override
          public void onRecordSuccess(File audioFile, long audioLength, RecordType recordType) {
            ALog.d(
                LIB_TAG,
                TAG,
                "onRecordSuccess -->> file:" + audioFile.getName() + " length:" + audioLength);
            mProxy.sendAudio(audioFile, (int) audioLength, replyMessage);
            clearReplyMsg();
          }

          @Override
          public void onRecordFail() {
            ALog.d(LIB_TAG, TAG, "onRecordFail");
          }

          @Override
          public void onRecordCancel() {
            ALog.d(LIB_TAG, TAG, "onRecordCancel");
          }

          @Override
          public void onRecordReachedMaxTime(int maxTime) {
            ALog.d(LIB_TAG, TAG, "onRecordReachedMaxTime -->> " + maxTime);
            dismissAudioInputDialog(false);
            mAudioRecorder.handleEndRecord(true, maxTime);
          }
        };
    mEditHintText = getContext().getResources().getString(R.string.fun_chat_input_hint_tips);
    mBinding.replyLayout.setVisibility(GONE);
    mBinding.chatMessageInputEt.setOnFocusChangeListener(
        (v, hasFocus) ->
            mProxy.onTypeStateChange(
                !TextUtils.isEmpty(mBinding.chatMessageInputEt.getText()) && hasFocus));
    if (mProxy.getConversationType() == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P
        && IMKitConfigCenter.getEnableAIChatHelper()) {
      mBinding.chatMessageAiHelperView.setLeftImageResource(R.drawable.fun_ic_ai_helper);
      mBinding.chatMessageAiHelperView.setLottieAnimationFile(
          "lottie/fun_chat_ai_helper_loading" + ".json");
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
                boolean sendMsg = mProxy.sendTextMessage(Item.content, null);
                if (actionClickListener != null) {
                  actionClickListener.sendMessage(Item.content, sendMsg);
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
    }
  }

  public FunChatMessageBottomViewBinding getViewBinding() {
    return mBinding;
  }

  // 获取输入框
  public EditText getInputEditText() {
    return mBinding.chatMessageInputEt;
  }

  public void setActionClickListener(ActionClickListener listener) {
    actionClickListener = listener;
  }

  public void setAIHelperData(List<AIHelperView.AIHelperItem> itemList) {
    mBinding.chatMessageAiHelperView.setHelperContent(itemList);
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
      case ActionConstants.ACTION_TYPE_AI_HELPER:
        switchAIHelper();
        break;
      default:
        mProxy.onCustomAction(view, item.getAction());
        break;
    }
  }

  // 设置输入watcher,用于@功能实现，监听输入框中输入@弹出的联系人列表
  public void setAitTextWatcher(AitManager aitTextWatcher) {
    this.aitTextWatcher = aitTextWatcher;
  }

  // 获取输入框Hint
  public String getInputEditTextHint() {
    return mEditHintText;
  }

  // 设置输入框Hint
  public void setInputEditTextHint(String content) {
    mEditHintText = content;
    mBinding.chatMessageInputEt.setHint(mEditHintText);
  }

  // 显示语音输入对话框
  public void showAudioInputDialog() {
    if (mAudioRecorder == null) {
      mAudioRecorder =
          new AudioRecorder(getContext(), RecordType.AAC, recordMaxDuration, audioRecordCallback);
    }

    recordDialog = new FunAudioRecordDialog(getContext());
    if (!recordDialog.isShowing()) {
      recordDialog.show(recordMaxDuration);
      recordDialog.showRecordingView();

      //record
      if (getContext() instanceof Activity) {
        ((Activity) getContext())
            .getWindow()
            .setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
      }
      mAudioRecorder.startRecord();
    }
  }

  // 隐藏语音输入对话框
  public void dismissAudioInputDialog(boolean isCancel) {

    if (recordDialog != null && recordDialog.isShowing()) {
      recordDialog.dismiss();

      ALog.d(TAG, "endAudioRecord:");
      if (getContext() instanceof Activity) {
        ((Activity) getContext())
            .getWindow()
            .setFlags(0, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
      }
      mAudioRecorder.completeRecord(isCancel);
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  private void initView() {
    mBinding =
        FunChatMessageBottomViewBinding.inflate(LayoutInflater.from(getContext()), this, true);
    getViewTreeObserver()
        .addOnGlobalLayoutListener(
            () -> {
              if (KeyboardUtils.isKeyboardShow((Activity) getContext())) {
                if (!isKeyboardShow) {
                  ALog.d(LIB_TAG, TAG, "OnGlobalLayoutListener isKeyboardShow:" + isKeyboardShow);
                  isKeyboardShow = true;
                  if (mInputState != InputState.input) {
                    hideCurrentInput();
                    updateState(InputState.input);
                  }
                }
              } else {
                if (isKeyboardShow) {
                  ALog.d(LIB_TAG, TAG, "OnGlobalLayoutListener isKeyboardShow:" + isKeyboardShow);
                  isKeyboardShow = false;
                  if (mInputState == InputState.input) {
                    updateState(InputState.none);
                  }
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

    // 多行消息设置点击事件
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
    if (IMKitConfigCenter.getEnableRichTextMessage()) {
      mBinding.chatMsgInputSwitch.setVisibility(VISIBLE);
    } else {
      mBinding.chatMsgInputSwitch.setVisibility(GONE);
    }
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

          if (aitTextWatcher != null && !TextUtils.equals(s, editable)) {
            aitTextWatcher.afterTextChanged(s);
          }
          if (TextUtils.isEmpty(s.toString())) {
            mBinding.chatMessageInputEt.setHint(mEditHintText);
          }
          editable = s.toString();
        }
      };

  public void clearEditTextChangeListener() {
    mBinding.chatMessageInputEt.removeTextChangedListener(msgInputTextWatcher);
  }

  public void sendText(ChatMessageBean replyMessage) {
    if (mProxy != null) {
      String msg = mBinding.chatMessageInputEt.getEditableText().toString();
      String title = mBinding.chatRichEt.getEditableText().toString();
      boolean sendMsg;
      if (!TextUtils.isEmpty(title) && TextUtils.getTrimmedLength(title) > 0) {
        sendMsg = mProxy.sendRichTextMessage(title, msg, replyMessage);
      } else {
        sendMsg = mProxy.sendTextMessage(msg, replyMessage);
      }
      clearEditTextInput(sendMsg);
      if (actionClickListener != null) {
        actionClickListener.sendMessage(msg, sendMsg);
      }
    }
  }

  // 清空输入框内容 force为true时强制清空(包括输入框内容、回复消息、翻译内容)，false时只有输入框内容为空时才清空
  public void clearEditTextInput(boolean force) {
    if (force) {
      mBinding.chatRichEt.setText("");
      mBinding.chatMessageInputEt.setText("");
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
      mBinding.chatMessageAiHelperView.show();
    } else {
      mBinding.chatMessageAiHelperView.hide();
    }
  }

  public void setRichTextSwitchListener(OnClickListener listener) {
    mBinding.chatMsgInputSwitchLayout.setOnClickListener(listener);
  }

  // 获取富文本标题
  public String getRichInputTitle() {
    return mBinding.chatRichEt.getText().toString();
  }

  // 获取富文本内容，非富文本状态则返回普通小心文本
  public String getRichInputContent() {
    return mBinding.chatMessageInputEt.getText().toString();
  }

  // 切换富文本输入模式
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

  //切换到文本输入模式
  public void switchInput() {
    if (mInputState == InputState.input) {
      return;
    }
    hideCurrentInput();
    showKeyboard();
    updateState(InputState.input);
  }

  // 切换到语音输入模式
  public void switchRecord() {
    if (mInputState == InputState.voice) {
      recordShow(false, 0);
      updateState(InputState.input);
      return;
    }
    if (actionClickListener != null) {
      actionClickListener.onActionClick(
          mBinding.chatMsgInputSwitchLayout, ActionConstants.ACTION_TYPE_RECORD);
    }
    recordShow(true, 0);
    hideCurrentInput();
    updateState(InputState.voice);
  }

  // 显示语音输入
  public void recordShow(boolean show, long delay) {
    mBinding.inputAudioTv.setVisibility(show ? VISIBLE : GONE);
    mBinding.chatMessageInputEt.setVisibility(show ? GONE : VISIBLE);
    mBinding.chatMsgInputSwitchLayout.setVisibility(show ? GONE : VISIBLE);
  }

  // 切换到表情输入模式
  public void switchEmoji() {
    if (mInputState == InputState.emoji) {
      emojiShow(false, 0);
      updateState(InputState.none);
      return;
    }
    emojiShow(true, 0);
    hideCurrentInput();
    updateState(InputState.emoji);
  }

  // 更新输入状态，包括文本输入、语音输入、表情输入、更多输入
  private void updateState(InputState state) {
    mInputState = state;
    mBinding.inputMoreRb.setBackgroundResource(R.drawable.fun_ic_chat_input_more_selector);
    mBinding.inputMoreRb.setChecked(mInputState == InputState.more);
    mBinding.inputEmojiRb.setBackgroundResource(R.drawable.fun_ic_chat_input_emoji_selector);
    mBinding.inputEmojiRb.setChecked(mInputState == InputState.emoji);
    //防止滑动的时候，语音切换到输入框
    if (state != InputState.none) {
      mBinding.inputAudioRb.setBackgroundResource(R.drawable.fun_ic_chat_input_audio_selector);
      mBinding.inputAudioRb.setChecked(mInputState == InputState.voice);
    }
  }

  // 显示表情输入
  public void emojiShow(boolean show, long delay) {
    postDelayed(
        () -> {
          mBinding.emojiPickerView.setVisibility(show ? VISIBLE : GONE);
          if (show) {
            mBinding.emojiPickerView.show(emojiSelectedListener);
          }
        },
        delay);
  }

  // 切换到更多输入模式
  public void switchMore() {
    if (mInputState == InputState.more) {
      morePanelShow(false, 0);
      updateState(InputState.none);
      return;
    }
    morePanelShow(true, 0);
    hideCurrentInput();
    updateState(InputState.more);
  }

  // 显示更多按钮菜单
  public void morePanelShow(boolean show, long delay) {
    // init more panel
    if (!mActionsPanel.hasInit() && show) {
      mActionsPanel.init(
          mBinding.actionsPanelVp,
          FunBottomActionFactory.assembleInputMoreActions(
              V2NIMConversationIdUtil.conversationTargetId(mProxy.getConversationId()),
              mProxy.getConversationType()),
          this);
    }
    postDelayed(() -> mBinding.actionsPanelVp.setVisibility(show ? VISIBLE : GONE), delay);
  }

  // 切换到翻译输入模式
  public void switchTranslate(View view, String action) {
    if (actionClickListener != null) {
      actionClickListener.onActionClick(view, action);
    }
    mProxy.onTranslateAction();
  }

  // 发送图片或者视频
  public void onAlbumClick() {
    if (mInputState == InputState.input) {
      hideKeyboard();
      postDelayed(() -> mProxy.pickMedia(), SHOW_DELAY_TIME);
    } else {
      mProxy.pickMedia();
    }
  }

  // 拍摄照片或者视频
  public void onCameraClick() {
    BottomChoiceDialog dialog =
        new BottomChoiceDialog(
            this.getContext(), FunBottomActionFactory.assembleTakeShootActions());
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

  // 发起视频或者语音通话
  public void onCallClick() {
    BottomChoiceDialog dialog =
        new BottomChoiceDialog(
            this.getContext(), FunBottomActionFactory.assembleVideoCallActions());
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
      mBinding.chatinputMuteLayout.setVisibility(mute ? VISIBLE : GONE);
      mBinding.chatMessageInputEt.setText("");
      mBinding.chatRichEt.setText("");
      if (mute) {
        // 禁言时，如果是语音输入状态，切换到文本输入状态
        if (mInputState == InputState.voice) {
          recordShow(false, 0);
          updateState(InputState.input);
        }
        collapse(true);
      }
      mBinding.inputLayout.setBackgroundResource(mute ? R.color.color_e3e4e4 : R.color.color_white);
      mBinding.inputAudioRb.setEnabled(!mute);
      mBinding.inputAudioRb.setAlpha(mute ? 0.5f : 1f);
      mBinding.inputEmojiRb.setEnabled(!mute);
      mBinding.inputEmojiRb.setAlpha(mute ? 0.5f : 1f);
      mBinding.inputMoreRb.setEnabled(!mute);
      mBinding.inputMoreRb.setAlpha(mute ? 0.5f : 1f);
    }
  }

  // 获取是否禁言
  public boolean isMute() {
    return mMute;
  }

  // 收起输入框键盘、更多区域、表情等，恢复初始状态
  public void collapse(boolean immediately) {
    if (mInputState == InputState.none) {
      return;
    }
    postDelayed(
        () -> {
          ALog.d(LIB_TAG, TAG, "hideAllInputLayout");
          updateState(InputState.none);
          KeyboardUtils.hideKeyboard(this);
          long delay = immediately ? 0 : SHOW_DELAY_TIME;
          emojiShow(false, delay);
          morePanelShow(false, delay);
          aiHelperShow(false);
        },
        immediately ? 0 : ViewConfiguration.getDoubleTapTimeout());
  }

  // 设置被回复消息内容
  public void setReplyMessage(ChatMessageBean messageBean) {
    this.replyMessage = messageBean;
    mBinding.replyLayout.setVisibility(VISIBLE);
    String tips = MessageHelper.getReplyMessageTips(messageBean.getMessageData());
    tips = String.format(getContext().getString(R.string.chat_message_reply_someone), tips);
    MessageHelper.identifyFaceExpression(
        getContext(),
        mBinding.tvReplyContent,
        tips,
        ImageSpan.ALIGN_BOTTOM,
        MessageHelper.SMALL_SCALE);
    mBinding.ivReplyClose.setOnClickListener(v -> clearReplyMsg());
    switchInput();
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

  // 清除被回复消息内容
  public void clearReplyMsg() {
    replyMessage = null;
    mBinding.replyLayout.setVisibility(GONE);
  }

  // 设置输入框属性配置
  public void setInputProperties(InputProperties properties) {
    this.inputProperties = properties;
    loadConfig();
  }

  // 隐藏键盘
  private void hideKeyboard() {
    KeyboardUtils.hideKeyboard(mBinding.chatMessageInputEt);
    mBinding.chatMessageInputEt.clearFocus();
  }

  // 显示键盘
  private void showKeyboard() {
    mBinding.chatMessageInputEt.requestFocus();
    mBinding.chatMessageInputEt.setSelection(mBinding.chatMessageInputEt.getText().length());
    KeyboardUtils.showKeyboard(mBinding.chatMessageInputEt);
  }

  // @功能中，插入输入框内容回调
  @Override
  public void onTextAdd(String content, int start, int length, boolean hasAt) {
    if (mInputState != InputState.input) {
      postDelayed(this::switchInput, SHOW_DELAY_TIME);
    }
    SpannableString spannable = MessageHelper.generateAtSpanString(hasAt ? content : "@" + content);
    mBinding
        .chatMessageInputEt
        .getEditableText()
        .replace(hasAt ? start : start - 1, start, spannable);
  }

  // @功能中，删除输入框内容回调
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
        mBinding.inputLayout.setBackground(inputProperties.inputEditBg);
      }

      if (inputProperties.inputMoreBg != null) {
        mBinding.inputMoreLayout.setBackground(inputProperties.inputMoreBg);
      }

      if (inputProperties.inputReplyBg != null) {
        mBinding.replyLayout.setBackground(inputProperties.inputReplyBg);
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

  // View 之间的交互接口 FunChatView使用
  public interface ActionClickListener {
    public void onActionClick(View view, String action);

    void sendMessage(String msg, boolean sendResult);
  }
}
