package com.netease.yunxin.kit.chatkit.ui.page.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.lifecycle.ViewModelProvider;

import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.page.ChatSettingActivity;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatP2PViewModel;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;

/**
 * P2P chat page
 */
public class ChatP2PFragment extends ChatBaseFragment {
    private static final String TAG = "ChatP2PFragment";

    private static final int TYPE_DELAY_TIME = 3000;

    UserInfo userInfo;

    private final Handler handler = new Handler();

    private final Runnable stopTypingRunnable = () -> binding.chatView.setTypeState(false);

    @Override
    protected void initData(Bundle bundle) {
        ALog.i(TAG, "initData");
        sessionType = SessionTypeEnum.P2P;
        userInfo = (UserInfo) bundle.getSerializable(RouterConstant.CHAT_KRY);
        String name = MessageHelper.getUserNickByAccId(userInfo.getAccount(), false);
        binding.chatView.getTitleBar()
                .setOnBackIconClickListener(v -> requireActivity().onBackPressed())
                .setTitle(name)
                .setActionImg(R.drawable.ic_more_point)
                .setActionListener(v -> {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), ChatSettingActivity.class);
                    intent.putExtra(RouterConstant.CHAT_KRY, userInfo);
                    startActivity(intent);
                });
        binding.chatView.getInputView().updateInputInfo(name);
    }

    @Override
    protected void initViewModel() {
        ALog.i(TAG, "initViewModel");
        viewModel = new ViewModelProvider(this).get(ChatP2PViewModel.class);
        viewModel.init(userInfo.getAccount(), SessionTypeEnum.P2P);
        //fetch history message
        viewModel.initFetch(null);
    }

    @Override
    protected void initDataObserver() {
        super.initDataObserver();
        ALog.i(TAG, "initDataObserver");
        ((ChatP2PViewModel) viewModel).getMessageReceiptLiveData().observe(getViewLifecycleOwner(),
                imMessageReceiptInfo -> binding.chatView.getMessageListView().setP2PReceipt(imMessageReceiptInfo.getMessageReceipt().getTime()));

        ((ChatP2PViewModel) viewModel).getTypeStateLiveData().observe(getViewLifecycleOwner(), isTyping -> {
            handler.removeCallbacks(stopTypingRunnable);
            binding.chatView.setTypeState(isTyping);
            if (isTyping) {
                handler.postDelayed(stopTypingRunnable, TYPE_DELAY_TIME);
            }
        });
    }
}
