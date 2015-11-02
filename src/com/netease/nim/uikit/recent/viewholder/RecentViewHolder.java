package com.netease.nim.uikit.recent.viewholder;

import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.recent.RecentContactsCallback;
import com.netease.nim.uikit.recent.RecentContactsFragment;
import com.netease.nim.uikit.session.emoji.MoonUtil;
import com.netease.nim.uikit.uinfo.UserInfoHelper;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;

public abstract class RecentViewHolder extends TViewHolder implements OnClickListener {

    protected FrameLayout portraitPanel;

    protected HeadImageView imgHead;

    protected TextView tvNickname;

    protected TextView tvMessage;

    protected TextView tvUnread;

    protected View unreadIndicator;

    protected TextView tvDatetime;

    // 消息发送错误状态标记，目前没有逻辑处理
    protected ImageView imgMsgStatus;

    protected RecentContact recent;

    protected View bottomLine;
    protected View topLine;

    protected abstract String getContent();

    public void refresh(Object item) {
        recent = (RecentContact) item;

        updateBackground();

        loadPortrait();

        updateNewIndicator();

        updateNickLabel(UserInfoHelper.getUserTitleName(recent.getContactId(), recent.getSessionType()));

        updateMsgLabel();
    }

    public void refreshCurrentItem() {
        if (recent != null) {
            refresh(recent);
        }
    }

    private void updateBackground() {
        topLine.setVisibility(isFirstItem() ? View.GONE : View.VISIBLE);
        bottomLine.setVisibility(isLastItem() ? View.VISIBLE : View.GONE);
        if ((recent.getTag() & RecentContactsFragment.RECENT_TAG_STICKY) == 0) {
            view.setBackgroundResource(R.drawable.nim_list_item_selector);
        } else {
            view.setBackgroundResource(R.drawable.nim_recent_contact_sticky_selecter);
        }
    }

    protected void loadPortrait() {
        // 设置头像
        if (recent.getSessionType() == SessionTypeEnum.P2P) {
            imgHead.loadBuddyAvatar(recent.getContactId());
        } else if (recent.getSessionType() == SessionTypeEnum.Team) {
            imgHead.loadTeamIcon(recent.getContactId());
        }
    }

    private void updateNewIndicator() {
        int unreadNum = recent.getUnreadCount();
        tvUnread.setVisibility(unreadNum > 0 ? View.VISIBLE : View.GONE);
        tvUnread.setText(unreadCountShowRule(unreadNum));
    }

    private void updateMsgLabel() {
        // 显示消息具体内容
        MoonUtil.identifyFaceExpressionAndTags(context, tvMessage, getContent(), ImageSpan.ALIGN_BOTTOM, 0.45f);
        //tvMessage.setText(getContent());

        MsgStatusEnum status = recent.getMsgStatus();
        switch (status) {
            case fail:
                imgMsgStatus.setImageResource(R.drawable.nim_g_ic_failed_small);
                imgMsgStatus.setVisibility(View.VISIBLE);
                break;
            case sending:
                imgMsgStatus.setImageResource(R.drawable.nim_recent_contact_ic_sending);
                imgMsgStatus.setVisibility(View.VISIBLE);
                break;
            default:
                imgMsgStatus.setVisibility(View.GONE);
                break;
        }

        String timeString = TimeUtil.getTimeShowString(recent.getTime(), true);
        tvDatetime.setText(timeString);
        if (!TextUtils.isEmpty(timeString) && timeString.equals("1970-01-01")) {
            tvDatetime.setVisibility(View.GONE);
        } else {
            tvDatetime.setVisibility(View.VISIBLE);
        }
    }

    protected void updateNickLabel(String nick) {
        int labelWidth = ScreenUtil.screenWidth;
        labelWidth -= ScreenUtil.dip2px(50 + 70); // 减去固定的头像和时间宽度

        if (labelWidth > 0) {
            tvNickname.setMaxWidth(labelWidth);
        }

        tvNickname.setText(nick);
    }

    protected int getResId() {
        return R.layout.nim_recent_contact_list_item;
    }

    public void inflate() {
        this.portraitPanel = (FrameLayout) view.findViewById(R.id.portrait_panel);
        this.imgHead = (HeadImageView) view.findViewById(R.id.img_head);
        this.tvNickname = (TextView) view.findViewById(R.id.tv_nickname);
        this.tvMessage = (TextView) view.findViewById(R.id.tv_message);
        this.tvUnread = (TextView) view.findViewById(R.id.unread_number_tip);
        this.unreadIndicator = view.findViewById(R.id.new_message_indicator);
        this.tvDatetime = (TextView) view.findViewById(R.id.tv_date_time);
        this.imgMsgStatus = (ImageView) view.findViewById(R.id.img_msg_status);
        this.bottomLine = view.findViewById(R.id.bottom_line);
        this.topLine = view.findViewById(R.id.top_line);
    }

    protected String unreadCountShowRule(int unread) {
        unread = Math.min(unread, 99);
        return String.valueOf(unread);
    }

    protected RecentContactsCallback getCallback() {
        return ((RecentContactAdapter)getAdapter()).getCallback();
    }

    @Override
    public void onClick(View v) {

    }
}
