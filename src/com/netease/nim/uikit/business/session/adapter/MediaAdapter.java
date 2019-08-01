package com.netease.nim.uikit.business.session.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.session.activity.WatchMessagePictureActivity;
import com.netease.nim.uikit.business.session.activity.WatchVideoActivity;
import com.netease.nim.uikit.business.session.viewholder.media.DateViewHolder;
import com.netease.nim.uikit.business.session.viewholder.media.MediaViewHolder;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.attachment.VideoAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.List;

/**
 * Created by winnie on 2017/9/18.
 */

public class MediaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_VIEW_TYPE_DATE = 0;
    private static final int ITEM_VIEW_TYPE_ITEM = 1;

    private Context context;
    private List<MediaItem> mediaItems;

    public static class MediaItem {
        private boolean isDate;
        private IMMessage message;
        private long time;

        public MediaItem(IMMessage message, boolean isDate) {
            this.isDate = isDate;
            this.message = message;
        }

        public boolean isDate() {
            return isDate;
        }

        public void setDate(boolean date) {
            isDate = date;
        }

        public IMMessage getMessage() {
            return message;
        }

        public void setMessage(IMMessage message) {
            this.message = message;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public long getTime() {
            return time;
        }
    }

    public MediaAdapter(Context context, List<MediaItem> mediaItems) {
        this.context = context;
        this.mediaItems = mediaItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_VIEW_TYPE_DATE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.nim_media_item_date, parent, false);
            return new DateViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.nim_media_item_normal, parent, false);
            return new MediaViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mediaItems.get(position).isDate ? ITEM_VIEW_TYPE_DATE : ITEM_VIEW_TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == ITEM_VIEW_TYPE_DATE) {
            ((DateViewHolder) holder).dateText.setText(TimeUtil.getDateTimeString(mediaItems.get(position).getTime(), "yyyy年MM月"));
        } else {
            // 显示图片或视频缩略图
            final IMMessage msg = mediaItems.get(position).getMessage();
            if (msg.getMsgType() == MsgTypeEnum.image) {
                ((MediaViewHolder) holder).playImage.setVisibility(View.GONE);
                ImageAttachment imageAttachment = (ImageAttachment) msg.getAttachment();
                String path = "";
                if (!TextUtils.isEmpty(imageAttachment.getThumbPath())) {
                    path = imageAttachment.getThumbPath();
                } else if (!TextUtils.isEmpty(imageAttachment.getPath())) {
                    path = imageAttachment.getPath();
                }
                Glide.with(context).load(path).into(((MediaViewHolder) holder).mediaImage);
                ((MediaViewHolder) holder).mediaImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        WatchMessagePictureActivity.start(context, msg, false);
                    }
                });
            } else if (msg.getMsgType() == MsgTypeEnum.video) {
                ((MediaViewHolder) holder).playImage.setVisibility(View.VISIBLE);
                VideoAttachment videoAttachment = (VideoAttachment) msg.getAttachment();
                String path = "";
                if (!TextUtils.isEmpty(videoAttachment.getThumbPath())) {
                    path = videoAttachment.getThumbPath();
                } else if (!TextUtils.isEmpty(videoAttachment.getPath())) {
                    path = videoAttachment.getPath();
                }
                Glide.with(context).load(path).into(((MediaViewHolder) holder).mediaImage);
                ((MediaViewHolder) holder).mediaImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        WatchVideoActivity.start(context, msg, false);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return mediaItems == null ? 0 : mediaItems.size();
    }

    public boolean isDateType(int position) {
        return getItemViewType(position) == ITEM_VIEW_TYPE_DATE;
    }
}
