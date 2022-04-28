package com.netease.yunxin.kit.chatkit.ui.view.message.viewholder;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatMessageThumbnailViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.common.ui.widgets.ShapeDrawable;
import com.netease.yunxin.kit.common.utils.media.ImageUtil;

/**
 * view holder to show image/video thumb
 */
public abstract class ChatThumbBaseViewHolder extends ChatBaseMessageViewHolder {
    private static final String TAG = "ChatThumbBaseViewHolder";

    ChatMessageThumbnailViewHolderBinding binding;

    public ChatThumbBaseViewHolder(@NonNull ViewGroup parent, int viewType) {
        super(parent, viewType);
    }

    protected IMMessage getMsgInternal() {
        return currentMessage.getMessageData().getMessage();
    }

    @Override
    public void addContainer() {
        binding = ChatMessageThumbnailViewHolderBinding.inflate(LayoutInflater.from(getParent().getContext()),
                getContainer(), true);
    }

    @Override
    public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
        super.bindData(message, lastMessage);

        load();
    }

    @Override
    protected void onMessageStatus(ChatMessageBean data) {
        super.onMessageStatus(data);
        load();
    }

    private void load() {
        FileAttachment attachment = (FileAttachment) getMsgInternal().getAttachment();
        String path = attachment.getPath();
        String thumbPath = attachment.getThumbPath();
        if (!TextUtils.isEmpty(thumbPath)) {
            ALog.d(TAG, "load from thumb");
            loadThumbnailImage(thumbPath);
        } else if (!TextUtils.isEmpty(path)) {
            ALog.d(TAG, "load from path");
            loadThumbnailImage(thumbFromSourceFile(path));
        } else {
            loadThumbnailInternal(null, ImageUtil.getImageThumbMinEdge(), ImageUtil.getImageThumbMinEdge());
        }
    }

    private void loadThumbnailImage(String path) {
        int[] bounds = getBounds(path);
        int w = bounds[0];
        int h = bounds[1];
        int thumbMinEdge = ImageUtil.getImageThumbMinEdge();
        if (w < thumbMinEdge) {
            w = thumbMinEdge;
            h = w * bounds[1] / bounds[0];
        }
        int thumbMaxEdge = ImageUtil.getImageThumbMaxEdge();
        if (w > thumbMaxEdge) {
            w = thumbMaxEdge;
            h = w * bounds[1] / bounds[0];
        }

        loadThumbnailInternal(path, w, h);
    }

    private void loadThumbnailInternal(String path, int w, int h) {
        FrameLayout.LayoutParams thumbParams = (FrameLayout.LayoutParams) binding.getRoot().getLayoutParams();
        thumbParams.width = w;
        thumbParams.height = h;
        binding.getRoot().setLayoutParams(thumbParams);

        // change container's background, stroke the thumbnail
        float[] corners = getCorners();
        ShapeDrawable.Builder shapeBuilder = new ShapeDrawable.Builder()
                .setStroke(1, ContextCompat.getColor(getContainer().getContext(), R.color.color_e2e5e8))
                .setRadii(new float[] {
                        corners[0], corners[0], corners[1], corners[1], corners[2], corners[2], corners[3], corners[3]
                });
        if (path == null) {
            shapeBuilder.setSolid(Color.BLACK);
        }
        binding.getRoot().setBackground(shapeBuilder.build());

        if (path != null) {
            Glide.with(binding.thumbnail.getContext())
                    .load(path)
                    .apply(new RequestOptions()
                            .transform(new GranularRoundedCorners(corners[0], corners[1], corners[2], corners[3])))
                    .override(w, h)
                    .into(binding.thumbnail);
        }
    }

    protected abstract String thumbFromSourceFile(String path);

    /**
     * @return [width, height]
     */
    protected abstract int[] getBounds(String path);

    /**
     * @return [leftTop, rightTop, leftBottom, rightBottom]
     */
    protected abstract float[] getCorners();
}
