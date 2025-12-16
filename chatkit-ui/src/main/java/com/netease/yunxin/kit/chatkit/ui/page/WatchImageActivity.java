// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import androidx.viewpager2.widget.ViewPager2;
import com.netease.nimlib.coexist.sdk.v2.message.V2NIMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.page.adapter.WatchImageAdapter;
import java.util.ArrayList;
import java.util.List;

/** 图片查看器 */
public class WatchImageActivity extends WatchBaseActivity {
  private static final String TAG = "WatchImageActivity";

  public static final String EXT_MESSAGE_LIST_KEY = "EXT_MESSAGE_LIST_KEY";
  public static final String EXT_FIRST_DISPLAY_INDEX_KEY = "EXT_FIRST_DISPLAY_INDEX_KEY";

  // 支持左右滑动，查看历史消息中其他图片消息，最多支持100个图片消息
  private ViewPager2 viewPager2;
  private WatchImageAdapter watchImageAdapter;

  // 图片消息列表，由消息页面跳转传入
  private List<V2NIMMessage> messages;
  // 保存图片权限，根据系统版本判断，如果是Android13则采用Manifest.permission.READ_MEDIA_IMAGES
  protected String[] permissionForAlbum;
  private int firstDisplayImageIndex = 0;
  private boolean newPageSelected = false;

  public static void launch(Context context, ArrayList<V2NIMMessage> list, int showIndex) {
    Intent intent = new Intent(context, WatchImageActivity.class);
    intent.putExtra(EXT_MESSAGE_LIST_KEY, list);
    intent.putExtra(EXT_FIRST_DISPLAY_INDEX_KEY, showIndex);
    context.startActivity(intent);
  }

  @Override
  public void initData(Intent intent) {
    if (intent != null) {
      messages = (List<V2NIMMessage>) intent.getSerializableExtra(EXT_MESSAGE_LIST_KEY);
      if (messages == null || messages.size() < 1) {
        finish();
        return;
      }
      firstDisplayImageIndex = intent.getIntExtra(EXT_FIRST_DISPLAY_INDEX_KEY, messages.size() - 1);
      ALog.d(
          LIB_TAG,
          TAG,
          "initData message size: " + messages.size() + " firstIndex:" + firstDisplayImageIndex);
    } else {
      finish();
    }
  }

  @Override
  public void initDataObserver() {
    super.initDataObserver();
    ALog.d(LIB_TAG, TAG, "initDataObserver");
    viewModel
        .getStatusMessageLiveData()
        .observe(
            this,
            messageStatusChangeResult -> {
              int pos = messages.indexOf(messageStatusChangeResult.getData());
              ALog.d(
                  LIB_TAG,
                  TAG,
                  "message livedata observe -->> pos:"
                      + pos
                      + " "
                      + messageStatusChangeResult.getLoadStatus());
              if (pos >= 0) {
                watchImageAdapter.notifyItemChanged(pos, messageStatusChangeResult.getLoadStatus());
              }
            });
  }

  @Override
  public void initView() {
    super.initView();
    watchImageAdapter = new WatchImageAdapter(this, messages);
    viewPager2.setAdapter(watchImageAdapter);
    viewPager2.registerOnPageChangeCallback(
        new ViewPager2.OnPageChangeCallback() {

          @Override
          public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (positionOffset == 0f && newPageSelected) {
              newPageSelected = false;
              viewModel.requestFile(messages.get(position));
            }
          }

          @Override
          public void onPageSelected(int position) {
            newPageSelected = true;
          }
        });
    viewPager2.setCurrentItem(firstDisplayImageIndex, false);
  }

  @Override
  public View initMediaView() {
    viewPager2 = new ViewPager2(this);
    viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
    return viewPager2;
  }

  @Override
  public void saveMedia() {
    int position = viewPager2.getCurrentItem();
    ALog.d(LIB_TAG, TAG, "save image -->> currentItem:" + position);
    if (position >= 0 && position < messages.size()) {
      V2NIMMessage currentMsg = messages.get(position);
      viewModel.saveMedia(this, currentMsg);
    }
  }
}
