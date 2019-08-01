package com.netease.nim.uikit.business.session.viewholder.robot;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.robot.parser.elements.element.ImageElement;
import com.netease.nim.uikit.common.ui.imageview.MsgThumbImageView;

/**
 * Created by hzchenkang on 2017/6/26.
 */

public class RobotImageView extends RobotViewBase<ImageElement> {

    private MsgThumbImageView thumbnail;

    public RobotImageView(Context context, ImageElement element, String content) {
        super(context, element, content);
    }

    @Override
    public int getResLayout() {
        return R.layout.nim_message_robot_image;
    }

    @Override
    public void onInflate() {
        thumbnail = (MsgThumbImageView) findViewById(R.id.message_item_thumb_thumbnail);
    }

    @Override
    public void onBindContentView() {
        String url = content;

        if (element != null) {
            url = element.getUrl();
        }
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Glide.with(getContext())
                .asBitmap()
                .load(url)
                .apply(new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.nim_message_item_round_bg))
                .into(thumbnail);
    }

    @Override
    public void onParentMeasured(int width, int height) {
        if (element != null) {
            ViewGroup.LayoutParams params = thumbnail.getLayoutParams();

//            if (element.isHeightUsePercent()) {
//                params.height = element.getHeight() * height / 100;
//            } else {
//                params.height = element.getHeight();
//            }
//            if (element.isWidthUsePercent()) {
//                params.width = element.getWidth() * height / 100;
//            } else {
//                params.width = element.getWidth();
//            }
//            thumbnail.setLayoutParams(params);
        }
    }

}