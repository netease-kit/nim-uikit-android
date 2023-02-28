// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.input;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.media.record.IAudioRecordCallback;
import com.netease.nimlib.sdk.media.record.RecordType;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import com.netease.yunxin.kit.common.ui.dialog.BottomChoiceDialog;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.KeyboardUtils;
import com.netease.yunxin.kit.common.utils.XKitUtils;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatMessageBottomLayoutBinding;
import com.netease.yunxin.kit.qchatkit.ui.message.emoji.IEmojiSelectedListener;
import com.netease.yunxin.kit.qchatkit.ui.message.interfaces.IItemActionListener;
import com.netease.yunxin.kit.qchatkit.ui.message.interfaces.IMessageProxy;
import com.netease.yunxin.kit.qchatkit.ui.message.utils.MessageUtil;
import java.io.File;
import java.util.List;

public class QChatMessageBottomLayout extends FrameLayout
    implements IAudioRecordCallback, IItemActionListener {
  public static final String TAG = "MessageBottomLayout";
  private static final long SHOW_DELAY_TIME = 200;
  private QChatMessageBottomLayoutBinding mBinding;
  private InputActionAdapter actionAdapter;
  private IMessageProxy mProxy;
  private boolean mMute = false;

  private final ActionsPanel mActionsPanel = new ActionsPanel();

  private boolean isKeyboardShow = false;
  private InputState mInputState = InputState.none;
  private IEmojiSelectedListener emojiSelectedListener;

  public QChatMessageBottomLayout(@NonNull Context context) {
    this(context, null);
  }

  public QChatMessageBottomLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public QChatMessageBottomLayout(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initView();
  }

  public void init(IMessageProxy proxy) {
    this.init(ActionFactory.assembleDefaultInputActions(), proxy);
  }

  public void init(List<ActionItem> items, IMessageProxy proxy) {
    mProxy = proxy;
    actionAdapter = new InputActionAdapter(items, this);
    actionAdapter.disableAll(mMute);
    mBinding.chatMessageActionContainer.setAdapter(actionAdapter);
    mBinding.chatMessageRecordView.setRecordCallback(this);
    mBinding.chatMessageRecordView.setPermissionRequest(
        permission -> {
          if (mProxy.hasPermission(Manifest.permission.RECORD_AUDIO)) {
            return true;
          }
          return true;
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
          public void onStickerSelected(String categoryName, String stickerName) {
            //            MsgAttachment attachment = new StickerAttachment(categoryName, stickerName);
            //            mProxy.sendCustomMessage(
            //                attachment, getContext().getString(R.string.chat_message_custom_sticker));
          }

          @Override
          public void onEmojiSendClick() {
            sendText();
          }
        };
    mBinding.llyReply.setVisibility(GONE);
    // init more panel
    mActionsPanel.init(
        mBinding.chatMessageActionsPanel, ActionFactory.assembleInputMoreActions(), this);

    mBinding.chatMessageInputEt.addTextChangedListener(
        new TextWatcher() {

          private int start;
          private int count;

          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            this.start = start;
            this.count = count;
          }

          @Override
          public void afterTextChanged(Editable s) {
            MessageUtil.replaceEmoticons(getContext(), s, start, count);
          }
        });
  }

  public QChatMessageBottomLayoutBinding getViewBinding() {
    return mBinding;
  }

  @Override
  public void onClick(View view, int position, ActionItem item) {
    ALog.d(TAG, "action click, inputState:" + mInputState);
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
        mProxy.sendFile();
        break;
      case ActionConstants.ACTION_TYPE_MORE:
        switchMore();
        break;
      case ActionConstants.ACTION_TYPE_CAMERA:
        onCameraClick();
        break;
      default:
        break;
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  private void initView() {
    mBinding =
        QChatMessageBottomLayoutBinding.inflate(LayoutInflater.from(getContext()), this, true);
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
            sendText();
          }
          return true;
        });

    mBinding.chatMessageEmojiView.setWithSticker(true);
    // action
    mBinding.chatMessageActionContainer.setLayoutManager(
        new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
  }

  public void sendText() {
    String msg = mBinding.chatMessageInputEt.getEditableText().toString();
    if (!TextUtils.isEmpty(msg) && mProxy != null) {
      if (mProxy.sendTextMessage(msg)) {
        mBinding.chatMessageInputEt.setText("");
        clearReplyMsg();
      }
    }
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
    }
  }

  public void switchInput() {
    if (mInputState == InputState.input) {
      return;
    }
    hideCurrentInput();
    showKeyboard();
    mInputState = InputState.input;
  }

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

  public void recordShow(boolean show, long delay) {
    postDelayed(() -> mBinding.chatMessageRecordView.setVisibility(show ? VISIBLE : GONE), delay);
    actionAdapter.updateItemState(ActionConstants.ACTION_TYPE_RECORD, show);
  }

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

  public void morePanelShow(boolean show, long delay) {
    postDelayed(() -> mBinding.chatMessageActionsPanel.setVisibility(show ? VISIBLE : GONE), delay);
    actionAdapter.updateItemState(ActionConstants.ACTION_TYPE_MORE, show);
  }

  public void onAlbumClick() {
    if (mInputState == InputState.input) {
      hideKeyboard();
      postDelayed(() -> mProxy.pickMedia(), SHOW_DELAY_TIME);
    } else {
      mProxy.pickMedia();
    }
  }

  public void onCameraClick() {
    BottomChoiceDialog dialog =
        new BottomChoiceDialog(this.getContext(), ActionFactory.assembleTakeShootActions());
    dialog.setOnChoiceListener(
        new BottomChoiceDialog.OnChoiceListener() {
          @Override
          public void onChoice(@NonNull String type) {
            if (!XKitUtils.getApplicationContext()
                .getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
              ToastX.showShortToast(R.string.qchat_message_camera_unavailable);
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

  public void collapse(boolean immediately) {
    if (mInputState == InputState.none) {
      return;
    }

    hideAllInputLayout(immediately);
  }

  private void clearReplyMsg() {
    mBinding.llyReply.setVisibility(GONE);
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
        },
        immediately ? 0 : ViewConfiguration.getDoubleTapTimeout());
  }

  @Override
  public void onRecordReady() {
    ALog.i(TAG, "onRecordReady");
  }

  @Override
  public void onRecordStart(File audioFile, RecordType recordType) {
    ALog.i(TAG, "onRecordStart");
    startRecord();
  }

  @Override
  public void onRecordSuccess(File audioFile, long audioLength, RecordType recordType) {
    ALog.i(TAG, "onRecordSuccess -->> file:" + audioFile.getName() + " length:" + audioLength);
    endRecord();
    mProxy.sendAudio(audioFile, audioLength);
    clearReplyMsg();
  }

  @Override
  public void onRecordFail() {
    ALog.i(TAG, "onRecordFail");
    endRecord();
  }

  @Override
  public void onRecordCancel() {
    ALog.i(TAG, "onRecordCancel");
    endRecord();
  }

  @Override
  public void onRecordReachedMaxTime(int maxTime) {
    ALog.i(TAG, "onRecordReachedMaxTime -->> " + maxTime);
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
    ALog.i(TAG, "onKeyboardShow inputState:" + mInputState);
    if (mInputState != InputState.input) {
      hideCurrentInput();
      mInputState = InputState.input;
    }
  }

  private void onKeyboardHide() {
    ALog.i(TAG, "onKeyboardHide inputState:" + mInputState);
    if (mInputState == InputState.input) {
      mInputState = InputState.none;
    }
  }

  private void hideKeyboard() {
    KeyboardUtils.hideKeyboard(mBinding.chatMessageInputEt);
  }

  private void showKeyboard() {
    mBinding.chatMessageInputEt.requestFocus();
    mBinding.chatMessageInputEt.setSelection(mBinding.chatMessageInputEt.getText().length());
    KeyboardUtils.showKeyboard(mBinding.chatMessageInputEt);
  }
}
