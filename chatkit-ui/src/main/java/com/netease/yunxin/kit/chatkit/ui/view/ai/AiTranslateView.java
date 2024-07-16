// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.ai;

import android.content.Context;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatAiTranslateLayoutBinding;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * AI翻译View 集成封装了AI翻译功能的View、翻译功能、翻译结果展示、语言选择等
 * 绑定EditText后，输入内容点击翻译按钮，将输入内容翻译为目标语言并展示到界面，点击使用按钮，将翻译结果填充到EditText中 使用方式： 1、在布局文件中引入
 * 2、调用bindEditText方法绑定需要翻译的EditText
 */
public class AiTranslateView extends FrameLayout {

  private static final String TAG = "AiTranslateView";
  private ChatAiTranslateLayoutBinding viewBinding;
  private OnTranslateClickListener onTranslateClickListener;

  private List<LanguageModel> languageModelList = new ArrayList<>();
  private LanguageModel selectLanguageModel;

  private AiTranslateViewModel viewModel;

  private boolean isInputStatus = true;

  private String inputText = "";

  private String translateResult = "";

  private EditText bindingEditText;

  public AiTranslateView(@NonNull Context context) {
    super(context);
    init(context);
  }

  public AiTranslateView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public AiTranslateView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  public AiTranslateView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(context);
  }

  protected void init(Context context) {
    initData();
    initView(context);
  }

  // 初始化UI
  protected void initView(Context context) {
    viewBinding = ChatAiTranslateLayoutBinding.inflate(LayoutInflater.from(context), this, true);
    viewBinding.aiLoadingLv.setAnimation("lottie/lottie_loading.json");
    viewBinding.aiLanguageTv.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            // show language dialog
            AiTranslateLanguageDialog dialog = new AiTranslateLanguageDialog(languageModelList);
            dialog.setOnLanguageSelectListener(onLanguageSelectListener);
            if (context instanceof AppCompatActivity) {
              dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), TAG);
            }
          }
        });

    viewBinding.aiCloseIv.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (onTranslateClickListener != null) {
              onTranslateClickListener.onCloseClick();
            }
            resetView();
            AiTranslateView.this.setVisibility(GONE);
          }
        });

    viewBinding.aiOperateTv.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (onTranslateClickListener != null) {
              inputText = onTranslateClickListener.onTranslateClick(inputText);
            }
            if (!NetworkUtils.isConnected()) {
              ToastX.showShortToast(R.string.chat_network_error_tip);
              return;
            }
            onTranslate();
          }
        });
    viewBinding.aiOperateResultTv.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (onTranslateClickListener != null) {
              onTranslateClickListener.onCopyClick(translateResult);
            }
            if (bindingEditText != null) {
              bindingEditText.setText(translateResult);
              bindingEditText.setSelection(translateResult.length());
            }
            toInputStatus(true);
          }
        });

    if (selectLanguageModel != null) {
      viewBinding.aiLanguageTv.setText(selectLanguageModel.languageTag);
    }
  }

  // 清理View内容，恢复到初始状态
  public void resetView() {
    toInputStatus(true);
  }

  // 初始化数据，获取目标翻译语音，设置翻译接口监听
  protected void initData() {
    viewModel = new AiTranslateViewModel();
    loadLanguageList();
    viewModel
        .getTranslateResultLiveData()
        .observe(
            (AppCompatActivity) getContext(),
            fetchResult -> {
              if (fetchResult != null && fetchResult.getData() != null) {
                toTranslateFinishStatus(fetchResult.getData());
              }
              viewBinding.aiLoadingLv.cancelAnimation();
              viewBinding.aiLoadingLv.setVisibility(GONE);
            });
  }

  // 绑定输入框，监听输入内容变化，点击使用，将翻译结果填充到EditText中
  public void bindEditText(EditText editText) {
    if (editText == null) {
      return;
    }
    if (bindingEditText != null) {
      bindingEditText.removeTextChangedListener(textWatcher);
    }
    bindingEditText = editText;
    editText.addTextChangedListener(textWatcher);
    inputText = editText.getText().toString();
  }

  // 设置监听器，监听AITranslateView的操作
  public void setOnTranslateClickListener(OnTranslateClickListener onTranslateClickListener) {
    this.onTranslateClickListener = onTranslateClickListener;
  }

  /** AI处理，将输入内容翻译为目标语言并展示到界面 */
  public void onTranslate() {
    if (TextUtils.isEmpty(inputText)) {
      viewBinding.aiResultTv.setText("");
      viewBinding.aiResultTv.setVisibility(GONE);
      return;
    }
    viewBinding.aiLoadingLv.setVisibility(VISIBLE);
    viewBinding.aiLoadingLv.playAnimation();
    viewModel.translate(inputText, selectLanguageModel.language);
  }

  /** 转换为输入状态，按钮为AI处理 */
  public void toInputStatus(boolean clear) {
    if (clear) {
      viewBinding.aiResultTv.setText("");
      viewBinding.aiResultTv.setVisibility(GONE);
    }
    isInputStatus = true;
    viewBinding.aiOperateTv.setVisibility(VISIBLE);
    viewBinding.aiOperateResultTv.setVisibility(GONE);
    viewBinding.aiLoadingLv.cancelAnimation();
    viewBinding.aiLoadingLv.setVisibility(GONE);
  }

  /** 转换为输出状态，按钮为复制 */
  public void toTranslateFinishStatus(String resultText) {
    isInputStatus = false;
    viewBinding.aiOperateTv.setVisibility(GONE);
    viewBinding.aiOperateResultTv.setVisibility(VISIBLE);
    viewBinding.aiResultTv.setVisibility(VISIBLE);
    viewBinding.aiResultTv.setText(resultText);
    translateResult = resultText;
  }

  // 设置语音选择弹窗监听，监听选择翻译目标语音
  private final AiTranslateLanguageDialog.OnLanguageSelectListener onLanguageSelectListener =
      new AiTranslateLanguageDialog.OnLanguageSelectListener() {
        @Override
        public void onLanguageSelect(LanguageModel languageModel) {
          viewBinding.aiLanguageTv.setText(languageModel.languageTag);
          if (selectLanguageModel != null
              && !TextUtils.equals(languageModel.language, selectLanguageModel.language)) {
            toInputStatus(true);
          }
          selectLanguageModel = languageModel;
          for (LanguageModel model : languageModelList) {
            if (TextUtils.equals(languageModel.language, model.language)) {
              model.isSelected = true;
            } else {
              model.isSelected = false;
            }
          }
          viewModel.saveSelectLanguage(languageModel.languageTag);
          if (onTranslateClickListener != null) {
            onTranslateClickListener.onLanguageClick(languageModel);
          }
        }
      };

  // 加载翻译支持的语音
  protected void loadLanguageList() {
    languageModelList = viewModel.getLanguageList(getContext());
    if (languageModelList != null && languageModelList.size() > 0) {
      for (LanguageModel model : languageModelList) {
        if (model.isSelected) {
          selectLanguageModel = model;
          break;
        }
      }
    }
    if (selectLanguageModel == null) {
      selectLanguageModel = new LanguageModel();
    }
  }

  // EditText输入监听
  protected TextWatcher textWatcher =
      new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(android.text.Editable s) {
          String text = s.toString();
          if (!TextUtils.equals(text, inputText)) {
            inputText = text;
            if (!isInputStatus) {
              toInputStatus(false);
            }
          }
        }
      };

  // 设置监听器，监听AITranslateView的操作
  public interface OnTranslateClickListener {
    /**
     * 选择翻译目标语言
     *
     * @param language 选择的目标语言
     */
    void onLanguageClick(LanguageModel language);

    /**
     * 点击AI处理，将输入内容翻译为目标语言并展示到界面 如果进行了bindingEditText绑定，入参为EditText的内容，否则为上次输入内容。 返回结果作为要翻译的内容
     *
     * @param text 如果进行了bindingEditText绑定，入参为EditText的内容，否则为上次输入内容。
     * @return 返回结果作为要翻译的内容, 进行翻译并展示在UI中
     */
    String onTranslateClick(String text);

    /**
     * 点击使用，将翻译结果作为参数传入
     *
     * @param text 翻译结果
     */
    void onCopyClick(String text);

    /** 点击关闭按钮 */
    void onCloseClick();
  }
}
