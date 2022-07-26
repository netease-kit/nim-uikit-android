package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.netease.yunxin.kit.chatkit.repo.ChatMessageRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;

public class ChatSettingViewModel extends BaseViewModel {

    private final MutableLiveData<FetchResult<UserInfo>> userInfoLiveData = new MutableLiveData<>();
    private final FetchResult<UserInfo> userInfoFetchResult = new FetchResult<>(LoadStatus.Finish);

    public MutableLiveData<FetchResult<UserInfo>> getUserInfoLiveData(){
        return userInfoLiveData;
    }

    public void getUserInfo(String accId){
        ChatMessageRepo.fetchUserInfo(accId, new FetchCallback<UserInfo>() {
            @Override
            public void onSuccess(@Nullable UserInfo param) {
                userInfoFetchResult.setData(param);
                userInfoFetchResult.setLoadStatus(LoadStatus.Success);
                userInfoLiveData.setValue(userInfoFetchResult);
            }

            @Override
            public void onFailed(int code) {

            }

            @Override
            public void onException(@Nullable Throwable exception) {

            }
        });
    }
}
