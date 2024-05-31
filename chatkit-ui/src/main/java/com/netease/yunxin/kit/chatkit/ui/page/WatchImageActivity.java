// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.viewpager2.widget.ViewPager2;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.page.adapter.WatchImageAdapter;
import com.netease.yunxin.kit.common.ui.utils.Permission;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.storage.ExternalStorage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
      String path = MessageHelper.getMessageAttachPath(currentMsg);
      if (TextUtils.isEmpty(path)) {
        ALog.e(TAG, "save image -->> path is null");
        return;
      }
      ALog.d(TAG, "save path:" + path);
      permissionForAlbum = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};
      // 根据系统版本判断，如果是Android13则采用Manifest.permission.READ_MEDIA_IMAGES
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionForAlbum = new String[] {Manifest.permission.READ_MEDIA_IMAGES};
      }
      Permission.requirePermissions(this, permissionForAlbum)
          .request(
              new Permission.PermissionCallback() {
                @Override
                public void onGranted(List<String> permissionsGranted) {
                  if (new HashSet<>(permissionsGranted)
                      .containsAll(Arrays.asList(permissionForAlbum))) {
                    if (ExternalStorage.savePictureFile(new File(path))) {
                      ToastX.showShortToast(R.string.chat_message_image_save);
                    } else {
                      ToastX.showShortToast(R.string.chat_message_image_save_fail);
                    }
                  } else {
                    Toast.makeText(
                            WatchImageActivity.this,
                            WatchImageActivity.this
                                .getResources()
                                .getString(R.string.permission_default),
                            Toast.LENGTH_SHORT)
                        .show();
                  }
                }

                @Override
                public void onDenial(
                    List<String> permissionsDenial, List<String> permissionDenialForever) {
                  Toast.makeText(
                          WatchImageActivity.this,
                          WatchImageActivity.this
                              .getResources()
                              .getString(R.string.permission_default),
                          Toast.LENGTH_SHORT)
                      .show();
                }

                @Override
                public void onException(Exception exception) {
                  Toast.makeText(
                          WatchImageActivity.this,
                          WatchImageActivity.this
                              .getResources()
                              .getString(R.string.permission_default),
                          Toast.LENGTH_SHORT)
                      .show();
                }
              });
    }
  }
}
