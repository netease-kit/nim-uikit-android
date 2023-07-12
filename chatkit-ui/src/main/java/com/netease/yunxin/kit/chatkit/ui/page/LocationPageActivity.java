// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.nimlib.sdk.msg.attachment.LocationAttachment;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.map.ChatLocationBean;
import com.netease.yunxin.kit.chatkit.map.ILocationSearchCallback;
import com.netease.yunxin.kit.chatkit.map.IPageMapProvider;
import com.netease.yunxin.kit.chatkit.map.MapMode;
import com.netease.yunxin.kit.chatkit.ui.ActivityWorkaround;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ActivityLocationBinding;
import com.netease.yunxin.kit.chatkit.ui.page.adapter.SearchLocationAdapter;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.utils.KeyboardUtils;
import com.netease.yunxin.kit.common.utils.LocationUtils;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import java.util.List;

public class LocationPageActivity extends BaseActivity {
  private static final String TAG = "LocationPageActivity";

  public static final String SEND_LOCATION_RESULT = "SEND_LOCATION_RESULT";
  public static final String LAUNCH_TYPE = "LOCATION_LAUNCH_TYPE";
  public static final String LAUNCH_LOCATION_MESSAGE = "LAUNCH_LOCATION_MESSAGE";
  public static final int LAUNCH_SEND = 0;
  public static final int LAUNCH_DETAIL = 1;

  ActivityLocationBinding binding;
  int launchType = LAUNCH_SEND;
  IMMessage message;

  private SearchLocationAdapter adapter;
  private ChatLocationBean mSelectLoc;

  private IPageMapProvider pageMapProvider;
  private Handler searchHandler;

  public static void launch(Context context, int type, IMMessage message) {
    Intent intent = new Intent(context, LocationPageActivity.class);
    intent.putExtra(LAUNCH_TYPE, type);
    intent.putExtra(LAUNCH_LOCATION_MESSAGE, message);
    context.startActivity(intent);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    binding = ActivityLocationBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    initData(getIntent());
    pageMapProvider = ChatKitClient.getPageMapProvider();
    if (pageMapProvider == null) {
      ALog.e(TAG, "IPageMapProvider not config!");
      emptyPage();
    } else {
      searchHandler = new Handler();
      initView(savedInstanceState);
    }
    if (!LocationUtils.isLocationEnable(this)) {
      Toast.makeText(this, R.string.chat_location_disable, Toast.LENGTH_LONG).show();
    }
    NetworkUtils.registerNetworkStatusChangedListener(networkStateListener);
  }

  @Override
  protected void onPostCreate(@Nullable Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    if (launchType == LAUNCH_SEND) {
      ActivityWorkaround.assistActivity(this, true)
          .setOnKeyboardStateChangeListener(
              new ActivityWorkaround.OnKeyboardStateChangeListener() {
                @Override
                public void showKeyboard(int visibleHeight) {
                  binding.locationSearchCancel.setVisibility(View.VISIBLE);
                }

                @Override
                public void hideKeyboard(int visibleHeight) {
                  binding.locationSearchCancel.setVisibility(View.GONE);
                }
              });
    }
  }

  private void initData(Intent intent) {
    launchType = intent.getIntExtra(LAUNCH_TYPE, LAUNCH_SEND);
    message = (IMMessage) intent.getSerializableExtra(LAUNCH_LOCATION_MESSAGE);
  }

  private void emptyPage() {
    binding.mapLocation.setVisibility(View.GONE);
    binding.mapDetail.setVisibility(View.GONE);
    binding.mapViewEmpty.setVisibility(View.VISIBLE);
    binding.mapViewSend.setBackgroundResource(R.drawable.bg_corner_button_unclick);
  }

  private void initView(Bundle savedInstanceState) {
    binding.mapViewEmpty.setVisibility(View.GONE);
    if (launchType == LAUNCH_SEND) {
      renderForSend(savedInstanceState);
    } else {
      renderForDetail(savedInstanceState);
    }
    binding.mapViewContainer.removeAllViews();
    binding.mapViewContainer.addView(pageMapProvider.getChatMap().getMapView());
  }

  private void renderForDetail(Bundle savedInstanceState) {
    binding.mapLocation.setVisibility(View.GONE);
    binding.mapDetail.setVisibility(View.VISIBLE);
    pageMapProvider.createChatMap(this, savedInstanceState, MapMode.DETAIL, null);
    binding.mapDetailBack.setOnClickListener(v -> finish());
    if (message != null) {
      binding.mapDetailTitle.setText(message.getContent());
      LocationAttachment attachment = (LocationAttachment) message.getAttachment();
      pageMapProvider.changeLocation(attachment.getLatitude(), attachment.getLongitude(), false);
      binding.mapDetailAddress.setText(attachment.getAddress());
      binding.mapDetailNavigation.setOnClickListener(
          v ->
              pageMapProvider.jumpOutMap(
                  LocationPageActivity.this,
                  message.getContent(),
                  attachment.getLatitude(),
                  attachment.getLongitude()));
    }
  }

  private void renderForSend(Bundle savedInstanceState) {
    binding.mapLocation.setVisibility(View.VISIBLE);
    binding.mapDetail.setVisibility(View.GONE);
    pageMapProvider.createChatMap(
        this,
        savedInstanceState,
        MapMode.LOCATION,
        new ILocationSearchCallback() {
          @Override
          public void onSuccess(List<ChatLocationBean> result) {
            ALog.d(LIB_TAG, TAG, "ILocationSearchCallback:onSuccess:" + result);
            showSearch(result != null && !result.isEmpty());
            if (result == null) {
              return;
            }
            if (!result.isEmpty()) {
              mSelectLoc = result.get(0);
              selectLocation(result.get(0));
              adapter.setData(
                  result,
                  bean -> {
                    ALog.d(LIB_TAG, TAG, "location selected:" + bean);
                    mSelectLoc = bean;
                    selectLocation(bean);
                  });
              binding.locationSearchList.postDelayed(
                  () -> binding.locationSearchList.scrollToPosition(0), 500);
            }
          }

          @Override
          public void onFailed() {
            showSearch(false);
          }

          @Override
          public void onError(int code) {
            showSearch(false);
          }
        });
    binding.mapViewCancel.setOnClickListener(v -> finish());
    binding.mapViewSend.setOnClickListener(
        v -> {
          if (!NetworkUtils.isConnected()) {
            return;
          }
          if (mSelectLoc == null) {
            Toast.makeText(this, R.string.chat_location_send_empty, Toast.LENGTH_LONG).show();
            return;
          }
          ALog.d(LIB_TAG, TAG, "send location message:" + mSelectLoc);
          Intent result = new Intent();
          result.putExtra(SEND_LOCATION_RESULT, mSelectLoc);
          setResult(RESULT_OK, result);
          finish();
        });
    binding.locationButton.setOnClickListener(
        v -> {
          pageMapProvider.doLocation();
          binding.locationSearch.setText("");
          binding.locationButton.setImageResource(R.drawable.ic_location_in);
        });
    adapter = new SearchLocationAdapter();
    binding.locationSearchList.setLayoutManager(new LinearLayoutManager(this));
    binding.locationSearchList.setAdapter(adapter);
    binding.locationSearch.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            searchHandler.removeCallbacksAndMessages(null);
            searchHandler.postDelayed(() -> pageMapProvider.searchPoi(String.valueOf(s)), 500);
          }
        });
    binding.locationSearchCancel.setOnClickListener(
        v -> {
          binding.locationSearch.setText("");
          KeyboardUtils.hideKeyboard(v);
          binding.locationSearchCancel.setVisibility(View.GONE);
          // 恢复定位附近的位置
          pageMapProvider.resumeLocationResult();
        });
  }

  private boolean isMyLocation(ChatLocationBean bean) {
    return pageMapProvider.getCurrentLocation() != null
        && pageMapProvider.getCurrentLocation().isSameLatLng(bean.getLat(), bean.getLng());
  }

  private void selectLocation(ChatLocationBean bean) {
    pageMapProvider.changeLocation(bean.getLat(), bean.getLng(), true);
    binding.locationButton.setImageResource(
        isMyLocation(bean) ? R.drawable.ic_location_in : R.drawable.ic_location_to);
  }

  private void showSearch(boolean show) {
    binding.locationSearchList.setVisibility(show ? View.VISIBLE : View.GONE);
    binding.locationSearchEmpty.setVisibility(show ? View.GONE : View.VISIBLE);
  }

  private final NetworkUtils.NetworkStateListener networkStateListener =
      new NetworkUtils.NetworkStateListener() {
        @Override
        public void onAvailable(NetworkInfo network) {
          if (binding == null) {
            return;
          }
          binding.mapViewSend.setBackgroundResource(R.drawable.bg_corner_button);
        }

        @Override
        public void onLost(NetworkInfo network) {
          if (binding == null) {
            return;
          }
          binding.mapViewSend.setBackgroundResource(R.drawable.bg_corner_button_unclick);
        }
      };

  @Override
  protected void onResume() {
    super.onResume();
      if (pageMapProvider != null) {
          pageMapProvider.getChatMap().onResume();
      }
    if (NetworkUtils.isConnected()) {
      binding.mapViewSend.setBackgroundResource(R.drawable.bg_corner_button);
    } else {
      binding.mapViewSend.setBackgroundResource(R.drawable.bg_corner_button_unclick);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
      if (pageMapProvider != null) {
          pageMapProvider.getChatMap().onPause();
      }
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    //    pageMapProvider.getChatMap().onSaveInstanceState(outState);
    finish();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    NetworkUtils.unregisterNetworkStatusChangedListener(networkStateListener);
      if (pageMapProvider != null) {
          pageMapProvider.getChatMap().onDestroy();
          pageMapProvider.onDestroy();
      }
  }
}
