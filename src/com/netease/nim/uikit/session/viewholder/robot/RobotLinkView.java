package com.netease.nim.uikit.session.viewholder.robot;

import android.content.Context;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.robot.parser.elements.base.Element;
import com.netease.nim.uikit.robot.parser.elements.element.TextElement;
import com.netease.nim.uikit.robot.parser.elements.group.LinkElement;

/**
 * Created by hzchenkang on 2017/6/26.
 */

public class RobotLinkView extends RobotViewBase<LinkElement> {

    private RobotContentLinearLayout robotContentLinearLayout;

    public RobotLinkView(Context context, LinkElement element) {
        super(context, element, null);
    }

    @Override
    public int getResLayout() {
        return R.layout.nim_message_robot_link;
    }

    @Override
    public void onInflate() {
        robotContentLinearLayout = (RobotContentLinearLayout) findViewById(R.id.robot_content_view);
    }

    @Override
    public void onBindContentView() {
        if (element == null) {
            return;
        }

        robotContentLinearLayout.bindContentView(element);
    }

    @Override
    public void onParentMeasured(int width, int height) {

    }

    @Override
    public String getShowContent() {
        if (element.getElements().size() == 1) {
            Element e = element.getElements().get(0);
            if (e instanceof TextElement) {
                return ((TextElement) e).getContent();
            }
        }
        return "[复杂按钮模板触发消息]";
    }
}
