package com.netease.nim.uikit.business.session.viewholder.robot;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.robot.parser.elements.element.TextElement;

/**
 * Created by hzchenkang on 2017/6/26.
 */

public class RobotTextView extends RobotViewBase<TextElement> {

    private TextView textView;

    public RobotTextView(Context context, TextElement element, String content) {
        super(context, element, content);
    }

    @Override
    public int getResLayout() {
        return R.layout.nim_message_robot_text;
    }

    @Override
    public void onInflate() {
        textView = (TextView) findViewById(R.id.tv_robot_text);
    }

    private int color = -1;

    public void setTextColor(int colorRes) {
        this.color = colorRes;
    }

    @Override
    public void onBindContentView() {
        if (element != null) {
            textView.setText(element.getContent());
            if (element.getColor() != null) {
                try {
                    textView.setTextColor(Color.parseColor("#" + element.getColor()));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            } else if (color != -1) {
                textView.setTextColor(color);
            }
        } else if (!TextUtils.isEmpty(content)) {
            textView.setText(content);
        }
    }

    @Override
    public void onParentMeasured(int width, int height) {
//        if (element != null) {
//            ViewGroup.LayoutParams params = getLayoutParams();
//            if (element.isWidthUsePercent()) {
//                params.width = element.getWidth() * width / 100;
//            } else {
//                params.width = element.getWidth();
//            }
//            setLayoutParams(params);
//        }
    }
}