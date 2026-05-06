// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main.mine.setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import com.netease.yunxin.app.im.AppSkinConfig;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityLanguageSelectBinding;
import com.netease.yunxin.app.im.utils.DataUtils;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import java.util.ArrayList;
import java.util.List;

/** 翻译目标语言选择页 展示所有支持的翻译目标语言，选中后立即保存并返回。 */
public class LanguageSelectActivity extends BaseLocalActivity {

  private ActivityLanguageSelectBinding viewBinding;

  /** 支持的语言列表：Pair<langCode, displayName> */
  private static final List<Pair<String, Integer>> LANGUAGES = new ArrayList<>();

  static {
    LANGUAGES.add(Pair.create("zh-CHS", R.string.translation_lang_zh_chs));
    LANGUAGES.add(Pair.create("zh-CHT", R.string.translation_lang_zh_cht));
    LANGUAGES.add(Pair.create("ar", R.string.translation_lang_ar));
    LANGUAGES.add(Pair.create("de", R.string.translation_lang_de));
    LANGUAGES.add(Pair.create("en", R.string.translation_lang_en));
    LANGUAGES.add(Pair.create("es", R.string.translation_lang_es));
    LANGUAGES.add(Pair.create("fr", R.string.translation_lang_fr));
    LANGUAGES.add(Pair.create("id", R.string.translation_lang_id));
    LANGUAGES.add(Pair.create("it", R.string.translation_lang_it));
    LANGUAGES.add(Pair.create("ja", R.string.translation_lang_ja));
    LANGUAGES.add(Pair.create("ko", R.string.translation_lang_ko));
    LANGUAGES.add(Pair.create("pt", R.string.translation_lang_pt));
    LANGUAGES.add(Pair.create("ru", R.string.translation_lang_ru));
    LANGUAGES.add(Pair.create("th", R.string.translation_lang_th));
    LANGUAGES.add(Pair.create("vi", R.string.translation_lang_vi));
  }

  private String currentLangCode;

  // 皮肤主色：Normal=蓝色，Fun=绿色（与 SettingLanguageActivity 保持一致）
  private int selectedColor;
  // 选中状态图标资源
  private int selectedIconRes;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_white);
    viewBinding = ActivityLanguageSelectBinding.inflate(getLayoutInflater());
    setContentView(viewBinding.getRoot());
    initView();
  }

  private void initView() {
    currentLangCode = DataUtils.getTranslationTargetLanguage(this);

    viewBinding.languageSelectTitleBar.setOnBackIconClickListener(v -> onBackPressed());

    // 皮肤适配：与 SettingLanguageActivity 保持一致
    // commonSkin（Normal 皮肤）= 绿色；else（Fun 皮肤）= 蓝色
    boolean isNormal =
        AppSkinConfig.getInstance().getAppSkinStyle() == AppSkinConfig.AppSkin.commonSkin;
    if (isNormal) {
      selectedColor = ContextCompat.getColor(this, R.color.color_58be6b);
      selectedIconRes = R.drawable.ic_select_green;
    } else {
      selectedColor = ContextCompat.getColor(this, R.color.color_337eff);
      selectedIconRes = R.drawable.ic_select_blue;
    }

    // TitleBar 右侧"保存"按钮，点击直接返回（选中后即保存，无需二次确认）
    viewBinding
        .languageSelectTitleBar
        .setActionText(R.string.setting_save)
        .setActionListener(
            v -> {
              setResult(RESULT_OK);
              finish();
            });
    viewBinding.languageSelectTitleBar.setActionTextColor(selectedColor);

    LayoutInflater inflater = LayoutInflater.from(this);
    for (int i = 0; i < LANGUAGES.size(); i++) {
      Pair<String, Integer> item = LANGUAGES.get(i);
      String langCode = item.first;
      int nameResId = item.second;

      View itemView =
          inflater.inflate(R.layout.item_language_select, viewBinding.llLanguageContainer, false);
      TextView tvName = itemView.findViewById(R.id.tv_language_name);
      ImageView ivSelected = itemView.findViewById(R.id.iv_language_selected);

      // 选中项文字和图标都使用皮肤主色
      boolean isSelected = langCode.equals(currentLangCode);
      tvName.setText(nameResId);
      tvName.setTextColor(
          isSelected ? selectedColor : ContextCompat.getColor(this, R.color.color_333333));
      ivSelected.setImageResource(selectedIconRes);
      ivSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);
      itemView.setTag(langCode);

      itemView.setOnClickListener(v -> selectLanguage((String) v.getTag()));

      viewBinding.llLanguageContainer.addView(itemView);

      // 添加分割线（最后一项除外）
      if (i < LANGUAGES.size() - 1) {
        View divider = new View(this);
        android.widget.LinearLayout.LayoutParams params =
            new android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1);
        params.setMarginStart((int) (getResources().getDisplayMetrics().density * 16));
        divider.setLayoutParams(params);
        divider.setBackgroundColor(
            androidx.core.content.ContextCompat.getColor(this, R.color.color_f5f8fc));
        viewBinding.llLanguageContainer.addView(divider);
      }
    }
  }

  private String langCodeToString(String langCode) {
    if (langCode == null) return "";
    int resId;
    switch (langCode) {
      case "zh-CHS":
        resId = R.string.translation_lang_zh_chs;
        break;
      case "zh-CHT":
        resId = R.string.translation_lang_zh_cht;
        break;
      case "ar":
        resId = R.string.translation_lang_ar;
        break;
      case "de":
        resId = R.string.translation_lang_de;
        break;
      case "en":
        resId = R.string.translation_lang_en;
        break;
      case "es":
        resId = R.string.translation_lang_es;
        break;
      case "fr":
        resId = R.string.translation_lang_fr;
        break;
      case "id":
        resId = R.string.translation_lang_id;
        break;
      case "it":
        resId = R.string.translation_lang_it;
        break;
      case "ja":
        resId = R.string.translation_lang_ja;
        break;
      case "ko":
        resId = R.string.translation_lang_ko;
        break;
      case "pt":
        resId = R.string.translation_lang_pt;
        break;
      case "ru":
        resId = R.string.translation_lang_ru;
        break;
      case "th":
        resId = R.string.translation_lang_th;
        break;
      case "vi":
        resId = R.string.translation_lang_vi;
        break;
      default:
        return langCode;
    }
    return getResources().getString(resId);
  }

  private void selectLanguage(String langCode) {
    currentLangCode = langCode;
    // 保存到持久化存储
    DataUtils.saveTranslationTargetLanguage(this, langCode);
    // 更新内存配置
    IMKitConfigCenter.setTranslationTargetLanguage(langCode);
    // 刷新所有 item 的选中状态和文字颜色
    int defaultColor = ContextCompat.getColor(this, R.color.color_333333);
    for (int i = 0; i < viewBinding.llLanguageContainer.getChildCount(); i++) {
      View child = viewBinding.llLanguageContainer.getChildAt(i);
      Object tag = child.getTag();
      if (tag instanceof String) {
        boolean isSelected = tag.equals(currentLangCode);
        TextView tvName = child.findViewById(R.id.tv_language_name);
        ImageView ivSelected = child.findViewById(R.id.iv_language_selected);
        if (tvName != null) {
          tvName.setTextColor(isSelected ? selectedColor : defaultColor);
        }
        if (ivSelected != null) {
          ivSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        }
      }
    }
  }
}
