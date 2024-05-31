// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main.mine.setting;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.misc.DirCacheFileType;
import com.netease.yunxin.kit.chatkit.repo.MiscRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.ArrayList;
import java.util.List;

public class ClearCacheViewModel extends BaseViewModel {

  private final MutableLiveData<FetchResult<Long>> sdkCacheLiveData = new MutableLiveData<>();

  public MutableLiveData<FetchResult<Long>> getSdkCacheLiveData() {
    return sdkCacheLiveData;
  }

  public void getSdkCacheSize() {
    MiscRepo.INSTANCE.getCacheSize(
        getSDKFileType(),
        new FetchCallback<Long>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {}

          @Override
          public void onSuccess(@Nullable Long param) {
            FetchResult<Long> fetchResult = new FetchResult<Long>(LoadStatus.Success);
            fetchResult.setData(param);
            sdkCacheLiveData.postValue(fetchResult);
          }
        });
  }

  public void clearSDKCache() {

    MiscRepo.INSTANCE.clearCacheSize(
        getSDKFileType(),
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {}

          @Override
          public void onSuccess(@Nullable Void param) {}
        });
  }

  public void clearMessageCache() {
    MiscRepo.INSTANCE.clearMessageCache();
  }

  private List<DirCacheFileType> getSDKFileType() {
    List<DirCacheFileType> types = new ArrayList<>();
    types.add(DirCacheFileType.AUDIO);
    types.add(DirCacheFileType.THUMB);
    types.add(DirCacheFileType.IMAGE);
    types.add(DirCacheFileType.VIDEO);
    types.add(DirCacheFileType.OTHER);
    return types;
  }
}
