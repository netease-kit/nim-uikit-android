package com.netease.yunxin.kit.chatkit.ui.page;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import androidx.viewpager2.widget.ViewPager2;

import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.page.adapter.WatchImageAdapter;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.storage.ExternalStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Watch picture page
 */
public class WatchImageActivity extends WatchBaseActivity {
    private final static String TAG = "WatchImageActivity";

    public final static String EXT_MESSAGE_LIST_KEY = "EXT_MESSAGE_LIST_KEY";
    public final static String EXT_FIRST_DISPLAY_INDEX_KEY = "EXT_FIRST_DISPLAY_INDEX_KEY";

    private ViewPager2 viewPager2;
    private WatchImageAdapter watchImageAdapter;

    private List<IMMessage> messages;
    private int firstDisplayImageIndex = 0;
    private boolean newPageSelected = false;

    public static void launch(Context context, ArrayList<IMMessage> list, int showIndex) {
        Intent intent = new Intent(context, WatchImageActivity.class);
        intent.putExtra(EXT_MESSAGE_LIST_KEY, list);
        intent.putExtra(EXT_FIRST_DISPLAY_INDEX_KEY, showIndex);
        context.startActivity(intent);
    }

    @Override
    public void initData(Intent intent) {
        if (intent != null) {
            messages = (List<IMMessage>) intent.getSerializableExtra(EXT_MESSAGE_LIST_KEY);
            firstDisplayImageIndex = intent.getIntExtra(EXT_FIRST_DISPLAY_INDEX_KEY, messages.size() - 1);
            ALog.i(TAG, "initData message size: " + messages.size() + " firstIndex:" + firstDisplayImageIndex);
        }
    }

    @Override
    public void initDataObserver() {
        super.initDataObserver();
        ALog.i(TAG, "initDataObserver");
        viewModel.getStatusMessageLiveData().observe(this, messageStatusChangeResult -> {
            int pos = messages.indexOf(messageStatusChangeResult.getData());
            ALog.i(TAG, "message livedata observe -->> pos:" + pos + " " + messageStatusChangeResult.getLoadStatus());
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
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

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
        ALog.i(TAG, "save image -->> currentItem:" + position);
        if (position >= 0 && position < messages.size()) {
            IMMessage currentMsg = messages.get(position);
            ImageAttachment attachment = (ImageAttachment) currentMsg.getAttachment();
            String path = attachment.getPath();
            if (TextUtils.isEmpty(path)) {
                ALog.e(TAG, "save image -->> path is null");
                return;
            }
            ALog.d(TAG, "save path:" + path);

            if (ExternalStorage.savePictureFile(new File(path))) {
                ToastX.showShortToast(R.string.chat_message_image_save);
            } else {
                ToastX.showShortToast(R.string.chat_message_image_save_fail);
            }
        }
    }
}
