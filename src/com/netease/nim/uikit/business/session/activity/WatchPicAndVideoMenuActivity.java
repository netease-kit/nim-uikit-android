package com.netease.nim.uikit.business.session.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions;
import com.netease.nim.uikit.business.session.adapter.MediaAdapter;
import com.netease.nim.uikit.common.activity.ToolBarOptions;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by winnie on 2017/9/18.
 */

public class WatchPicAndVideoMenuActivity extends UI {
    private final static String EXTRA_MESSAGE = "EXTRA_MESSAGE";

    // view
    private RecyclerView recyclerView;

    // data
    private IMMessage message;
    private MediaAdapter adapter;
    private List<MediaAdapter.MediaItem> mediaItems;

    public static void startActivity(Context context, IMMessage message) {
        Intent intent = new Intent();
        intent.setClass(context, WatchPicAndVideoMenuActivity.class);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_watch_pic_video_menu_activity);

        ToolBarOptions options = new NimToolBarOptions();
        options.titleId = R.string.pic_and_video;
        options.navigateId = R.drawable.nim_actionbar_white_back_icon;
        setToolBar(R.id.toolbar, options);

        message = (IMMessage) getIntent().getSerializableExtra(EXTRA_MESSAGE);

        findViews();
        queryPicAndVideo();
    }

    private void findViews() {
        recyclerView = findView(R.id.recycler_view);
        final GridLayoutManager manager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(manager);
        mediaItems = new ArrayList<>();
        adapter = new MediaAdapter(this, mediaItems);
        recyclerView.setAdapter(adapter);

        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.isDateType(position) ? manager.getSpanCount() : 1;
            }
        });
    }

    private void queryPicAndVideo() {
        // 查找图片和视频类型
        List<MsgTypeEnum> types = new ArrayList<>();
        types.add(MsgTypeEnum.image);
        types.add(MsgTypeEnum.video);

        // 查询锚点
        IMMessage anchor = MessageBuilder.createEmptyMessage(message.getSessionId(), message.getSessionType(), 0);

        NIMClient.getService(MsgService.class).queryMessageListByTypes(types, anchor,
                0, QueryDirectionEnum.QUERY_OLD, Integer.MAX_VALUE, false).setCallback(new RequestCallback<List<IMMessage>>() {
            @Override
            public void onSuccess(List<IMMessage> param) {
                addMediaItem(param);
            }

            @Override
            public void onFailed(int code) {

            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }

    private void addMediaItem(List<IMMessage> messages) {
        if (messages == null || messages.size() < 0) {
            return;
        }
        String currentTime = "";
        for (IMMessage msg : messages) {
            String msgTime = TimeUtil.getDateTimeString(msg.getTime(), "yyyyMM");
            if (!TextUtils.equals(msgTime, currentTime)) {
                currentTime = msgTime;
                MediaAdapter.MediaItem itemDateTip = new MediaAdapter.MediaItem(msg, true);
                itemDateTip.setTime(msg.getTime());
                mediaItems.add(itemDateTip);
            }

            MediaAdapter.MediaItem item = new MediaAdapter.MediaItem(msg, false);
            mediaItems.add(item);
        }
        adapter.notifyDataSetChanged();
    }

}
