package com.netease.nim.uikit.business.session.viewholder.robot;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.robot.parser.elements.base.Element;
import com.netease.nim.uikit.business.robot.parser.elements.element.ImageElement;
import com.netease.nim.uikit.business.robot.parser.elements.element.TextElement;
import com.netease.nim.uikit.business.robot.parser.elements.group.LinkElement;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hzchenkang on 2017/6/26.
 */

public class RobotLinkView extends RobotViewBase<LinkElement> {

    private LinearLayout container;

    private List<RobotViewBase> robotViews;

    private RobotLinkViewStyle style;

    public RobotLinkView(Context context, LinkElement element) {
        super(context, element, null);
        robotViews = new ArrayList<>();
        initLinkViewStyle();
        applyViewStyle();
    }

    private void initLinkViewStyle() {
        style = new RobotLinkViewStyle();
        style.setRobotTextColor(R.color.robot_link_element_text_blue);
        style.setBackground(R.drawable.nim_robot_link_view_selector);
    }

    public void setLinkViewStyle(RobotLinkViewStyle style) {
        this.style = style;
        applyViewStyle();
    }

    private void applyViewStyle() {
        if (style != null) {
            container.setBackgroundResource(style.getBackground());
        }
    }

    @Override
    public int getResLayout() {
        return R.layout.nim_message_robot_link;
    }

    @Override
    public void onInflate() {
        container = (LinearLayout) findViewById(R.id.robot_content_view);
    }

    @Override
    public void onBindContentView() {
        if (element == null) {
            return;
        }
        addChildViews(element);
        bindChildContentViews();
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

    protected void addChildViews(LinkElement element) {
        robotViews = new ArrayList<>();
        if (element == null) {
            return;
        }
        List<Element> elements = element.getElements();
        for (Element e : elements) {
            RobotViewBase v;
            if (e instanceof TextElement) {
                // 文本
                v = RobotViewFactory.createRobotTextView(getContext(), (TextElement) e, null);
                if (style != null) {
                    ((RobotTextView) v).setTextColor(getContext().getResources().getColor((style.getRobotTextColor())));
                }
            } else if (e instanceof ImageElement) {
                // 图片
                v = RobotViewFactory.createRobotImageView(getContext(), (ImageElement) e, null);
            } else {
                continue;
            }
            robotViews.add(v);
        }
    }

    protected void bindChildContentViews() {
        container.removeAllViews();
        for (RobotViewBase child : robotViews) {
            LinearLayout.LayoutParams params = child.createLayoutParams();
            if (params == null) {
                if (child instanceof RobotImageView) {
                    params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    int dp4 = ScreenUtil.dip2px(4);
                    params.setMargins(dp4, 2 * dp4, dp4, 2 * dp4);
                } else {
                    params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    int dp4 = ScreenUtil.dip2px(4);
                    params.setMargins(dp4, 0, dp4, 0);
                }
            }
            params.gravity = Gravity.CENTER_HORIZONTAL;
            container.addView(child, params);
            child.onBindContentView();
        }
    }
}
