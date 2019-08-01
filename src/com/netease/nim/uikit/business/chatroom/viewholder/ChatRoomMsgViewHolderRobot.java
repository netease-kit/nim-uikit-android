package com.netease.nim.uikit.business.chatroom.viewholder;

import android.view.View;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.robot.model.RobotResponseContent;
import com.netease.nim.uikit.business.session.viewholder.robot.RobotContentLinearLayout;
import com.netease.nim.uikit.business.session.viewholder.robot.RobotLinkViewStyle;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.robot.model.NimRobotInfo;
import com.netease.nimlib.sdk.robot.model.RobotAttachment;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by hzchenkang on 2017/8/24.
 */

public class ChatRoomMsgViewHolderRobot extends ChatRoomMsgViewHolderText implements RobotContentLinearLayout.ClickableChildView {

    private android.widget.LinearLayout containerIn;
    private RobotContentLinearLayout robotContent;
    private TextView holderFooterButton;
    private Set<Integer> onClickIds;


    public ChatRoomMsgViewHolderRobot(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return com.netease.nim.uikit.R.layout.nim_message_item_robot;
    }

    @Override
    protected void inflateContentView() {
        containerIn = findViewById(com.netease.nim.uikit.R.id.robot_in);
        bodyTextView = containerIn.findViewById(R.id.nim_message_item_text_body);
        robotContent = findViewById(com.netease.nim.uikit.R.id.robot_out);
        int dp6 = ScreenUtil.dip2px(6);
        robotContent.setPadding(dp6, 0, 0, 0);
        RobotLinkViewStyle linkStyle = new RobotLinkViewStyle();
        linkStyle.setRobotTextColor(R.color.black);
        linkStyle.setBackground(R.drawable.nim_chatroom_robot_link_view_selector);
        robotContent.setLinkStyle(linkStyle);
        holderFooterButton = findViewById(com.netease.nim.uikit.R.id.tv_robot_session_continue);
        holderFooterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getMsgAdapter().getEventListener() != null) {
                    getMsgAdapter().getEventListener().onFooterClick(ChatRoomMsgViewHolderRobot.this, ChatRoomMsgViewHolderRobot.this.message);
                }
            }
        });
    }

    @Override
    protected void bindContentView() {
        onClickIds = new HashSet<>(); // for child to add
        RobotAttachment attachment = (RobotAttachment) message.getAttachment();

        if (attachment.isRobotSend()) {
            // 下行
            containerIn.setVisibility(View.GONE);
            robotContent.setVisibility(View.VISIBLE);
            holderFooterButton.setVisibility(View.VISIBLE);
            nameIconView.setVisibility(View.GONE);
            NimRobotInfo robotInfo = NimUIKitImpl.getRobotInfoProvider().getRobotByAccount(attachment.getFromRobotAccount());
            if (robotInfo != null) {
                nameTextView.setText(robotInfo.getName());
            } else {
                nameTextView.setText(attachment.getFromRobotAccount());
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
