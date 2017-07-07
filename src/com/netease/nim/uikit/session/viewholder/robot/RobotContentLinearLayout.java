package com.netease.nim.uikit.session.viewholder.robot;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.robot.model.RobotBotContent;
import com.netease.nim.uikit.robot.model.RobotResponseContent;
import com.netease.nim.uikit.robot.parser.elements.base.Element;
import com.netease.nim.uikit.robot.parser.elements.element.ImageElement;
import com.netease.nim.uikit.robot.parser.elements.element.TextElement;
import com.netease.nim.uikit.robot.parser.elements.group.LinkElement;
import com.netease.nim.uikit.robot.parser.elements.group.TemplateRoot;
import com.netease.nim.uikit.session.viewholder.MsgViewHolderRobot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hzchenkang on 2017/6/28.
 * 适用于robot消息容器及LinkElement容器
 */

public class RobotContentLinearLayout extends LinearLayout {

    private List<RobotViewBase> robotViews;

    private static final int MODE_ROBOT_CONTENT = 0;
    private static final int MODE_LINK_ELEMENT = 1;

    public RobotContentLinearLayout(Context context) {
        this(context, null);
    }

    public RobotContentLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        robotViews = new ArrayList<>();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        for (RobotViewBase robotViewBase : robotViews) {
            robotViewBase.onParentMeasured(getMeasuredWidth(), getMeasuredHeight());
        }
    }

    /**
     * LinkElement容器
     */
    public void bindContentView(LinkElement element) {
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
                ((RobotTextView) v).setTextColor(getContext().getResources().getColor(R.color.robot_link_element_text_blue));
            } else if (e instanceof ImageElement) {
                // 图片
                v = RobotViewFactory.createRobotImageView(getContext(), (ImageElement) e, null);
            } else {
                continue;
            }
            robotViews.add(v);
        }

        bindChildContentViews(MODE_LINK_ELEMENT);
    }

    /**
     * 机器人消息容器
     */
    public void bindContentView(final MsgViewHolderRobot robot, final RobotResponseContent content) {
        robotViews = new ArrayList<>();
        if (RobotResponseContent.FLAG_BOT.equals(content.getFlag())) {
            // bot回复
            List<RobotBotContent> botContents = content.getBotContents();
            for (RobotBotContent bot : botContents) {
                if (RobotResponseContent.RES_TYPE_BOT_COMP.equals(bot.getType())) {
                    // 复合
                    convertTemplateToViews(new TemplateRoot(bot.getBotMsg()), robot);
                } else if (RobotResponseContent.RES_TYPE_BOT_TEXT.equals(bot.getType())
                        || RobotResponseContent.RES_TYPE_BOT_QUICK.equals(bot.getType())) {
                    // 文本
                    robotViews.add(RobotViewFactory.createRobotTextView(getContext(), null, bot.getBotMsg()));
                } else if (RobotResponseContent.RES_TYPE_BOT_IMAGE.equals(bot.getType())) {
                    // 图片
                    robotViews.add(RobotViewFactory.createRobotImageView(getContext(), null, bot.getBotMsg()));
                }
            }
        } else if (RobotResponseContent.FLAG_FAQ.equals(content.getFlag())) {
            // faq回复取匹配最高的
            String faqContent = content.getMaxScoreFaqContent();
            robotViews.add(RobotViewFactory.createRobotTextView(getContext(), null, faqContent));
        }

        bindChildContentViews(MODE_ROBOT_CONTENT);
    }

    private void convertTemplateToViews(final TemplateRoot template, final MsgViewHolderRobot robot) {
        List<com.netease.nim.uikit.robot.parser.elements.group.LinearLayout> robotLinearLayouts = template.getElements();
        if (robotLinearLayouts == null) {
            return;
        }
        for (com.netease.nim.uikit.robot.parser.elements.group.LinearLayout robotLinearLayout : robotLinearLayouts) {
            int size = robotLinearLayout.getElements().size();
            if (size <= 0) {
                continue;
            }
            for (int i = 0; i < size; i++) {
                RobotViewBase view;
                Element e = robotLinearLayout.getElements().get(i);
                if (e instanceof TextElement) {
                    view = RobotViewFactory.createRobotTextView(getContext(), (TextElement) e, null);
                } else if (e instanceof ImageElement) {
                    view = RobotViewFactory.createRobotImageView(getContext(), (ImageElement) e, null);
                } else if (e instanceof LinkElement) {
                    view = RobotViewFactory.createRobotLinkView(getContext(), (LinkElement) e);
                    view.setId(GenerateViewID.generateViewId());
                    robot.addOnClickListener(view.getId());
                } else {
                    continue;
                }

                robotViews.add(view);
            }
        }
    }

    private void bindChildContentViews(int mode) {
        removeAllViews();
        for (RobotViewBase child : robotViews) {
            LayoutParams params = child.createLayoutParams();
            if (params == null) {
                params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            if (mode == MODE_ROBOT_CONTENT && child instanceof RobotTextView) {
                params.gravity = Gravity.NO_GRAVITY;
            }else {
                params.gravity = Gravity.CENTER_HORIZONTAL;
            }
            addView(child, params);
            child.onBindContentView();
        }
    }

    private static class GenerateViewID {

        private static final AtomicInteger sNextGeneratedId = new AtomicInteger(2000);

        static int generateViewId() {
            for (; ; ) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        }
    }

}
