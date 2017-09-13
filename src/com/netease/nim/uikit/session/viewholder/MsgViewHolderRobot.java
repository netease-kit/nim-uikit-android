package com.netease.nim.uikit.session.viewholder;

import android.view.View;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;
import com.netease.nim.uikit.robot.model.RobotResponseContent;
import com.netease.nim.uikit.session.viewholder.robot.RobotContentLinearLayout;
import com.netease.nimlib.sdk.robot.model.RobotAttachment;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by hzchenkang on 2017/6/26.
 */

public class MsgViewHolderRobot extends MsgViewHolderText implements RobotContentLinearLayout.ClickableChildView {

    private android.widget.LinearLayout containerIn;

    private RobotContentLinearLayout robotContent;

    private TextView holderFooterButton;
    private Set<Integer> onClickIds;

    public MsgViewHolderRobot(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.nim_message_item_robot;
    }

    @Override
    protected void inflateContentView() {
        containerIn = findViewById(R.id.robot_in);
        robotContent = findViewById(R.id.robot_out);
        holderFooterButton = findViewById(R.id.tv_robot_session_continue);
        holderFooterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getMsgAdapter().getEventListener() != null) {
                    getMsgAdapter().getEventListener().onFooterClick(MsgViewHolderRobot.this.message);
                }
            }
        });
        bodyTextView = (TextView) containerIn.findViewById(R.id.nim_message_item_text_body);
    }

    @Override
    protected void bindContentView() {
        onClickIds = new HashSet<>(); // for child to add
        RobotAttachment attachment = (RobotAttachment) message.getAttachment();

        if (attachment.isRobotSend()) {
            // 下行
            containerIn.setVisibility(View.GONE);
            robotContent.setVisibility(View.VISIBLE);

            // 正在聊天的不是机器人，则显示继续会话
            if (!message.getSessionId().equals(attachment.getFromRobotAccount())) {
                holderFooterButton.setVisibility(View.VISIBLE);
            } else {
                holderFooterButton.setVisibility(View.GONE);
            }
            robotContent.bindContentView(this, new RobotResponseContent(attachment.getResponse()));
        } else {
            // 上行
            containerIn.setVisibility(View.VISIBLE);
            robotContent.setVisibility(View.GONE);
            holderFooterButton.setVisibility(View.GONE);
            super.bindContentView();
        }
    }

    @Override
    protected void bindHolder(BaseViewHolder holder) {
        holder.getChildClickViewIds().clear();
        for (int id : onClickIds) {
            holder.addOnClickListener(id);
        }

        onClickIds.clear();
    }

    @Override
    public void addClickableChildView(Class<? extends View> clazz, int id) {
        onClickIds.add(id);
    }
}
