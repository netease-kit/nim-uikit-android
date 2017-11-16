package com.netease.nim.uikit.common.ui.imageview;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.nos.model.NosThumbParam;
import com.netease.nimlib.sdk.nos.util.NosThumbImageUtil;
import com.netease.nimlib.sdk.robot.model.RobotAttachment;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;

/**
 * Created by huangjun on 2015/11/13.
 */
public class HeadImageView extends CircleImageView {

    public static final int DEFAULT_AVATAR_THUMB_SIZE = (int) NimUIKit.getContext().getResources().getDimension(R.dimen.avatar_max_size);
    public static final int DEFAULT_AVATAR_NOTIFICATION_ICON_SIZE = (int) NimUIKit.getContext().getResources().getDimension(R.dimen.avatar_notification_size);
    private static final int DEFAULT_AVATAR_RES_ID = R.drawable.nim_avatar_default;

    public HeadImageView(Context context) {
        super(context);
    }

    public HeadImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeadImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 加载用户头像（默认大小的缩略图）
     *
     * @param url 头像地址
     */
    public void loadAvatar(final String url) {
        doLoadImage(url, DEFAULT_AVATAR_RES_ID, DEFAULT_AVATAR_THUMB_SIZE);
    }

    /**
     * 加载用户头像（默认大小的缩略图）
     *
     * @param account 用户账号
     */
    public void loadBuddyAvatar(String account) {
        final UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(account);
        doLoadImage(userInfo != null ? userInfo.getAvatar() : null, DEFAULT_AVATAR_RES_ID, DEFAULT_AVATAR_THUMB_SIZE);
    }

    /**
     * 加载用户头像（默认大小的缩略图）
     *
     * @param message 消息
     */
    public void loadBuddyAvatar(IMMessage message) {
        String account = message.getFromAccount();
        if (message.getMsgType() == MsgTypeEnum.robot) {
            RobotAttachment attachment = (RobotAttachment) message.getAttachment();
            if (attachment.isRobotSend()) {
                account = attachment.getFromRobotAccount();
            }
        }
        loadBuddyAvatar(account);
    }

    /**
     * 加载群头像（默认大小的缩略图）
     *
     * @param team 群
     */
    public void loadTeamIconByTeam(final Team team) {
        doLoadImage(team != null ? team.getIcon() : null, R.drawable.nim_avatar_group, DEFAULT_AVATAR_THUMB_SIZE);
    }

    /**
     * ImageLoader异步加载
     */
    private void doLoadImage(final String url, final int defaultResId, final int thumbSize) {
        /*
         * 若使用网易云信云存储，这里可以设置下载图片的压缩尺寸，生成下载URL
         * 如果图片来源是非网易云信云存储，请不要使用NosThumbImageUtil
         */
        final String thumbUrl = makeAvatarThumbNosUrl(url, thumbSize);
        RequestOptions requestOptions = new RequestOptions()
                .centerCrop()
                .placeholder(defaultResId)
                .error(defaultResId)
                .override(thumbSize, thumbSize);
        Glide.with(getContext().getApplicationContext()).asBitmap()
                .load(thumbUrl)
                .apply(requestOptions)
                .into(this);
    }

    /**
     * 解决ViewHolder复用问题
     */
    public void resetImageView() {
        setImageBitmap(null);
    }

    /**
     * 生成头像缩略图NOS URL地址（用作ImageLoader缓存的key）
     */
    private static String makeAvatarThumbNosUrl(final String url, final int thumbSize) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }

        return thumbSize > 0 ? NosThumbImageUtil.makeImageThumbUrl(url, NosThumbParam.ThumbType.Crop, thumbSize, thumbSize) : url;
    }

    public static String getAvatarCacheKey(final String url) {
        return makeAvatarThumbNosUrl(url, DEFAULT_AVATAR_THUMB_SIZE);
    }
}
