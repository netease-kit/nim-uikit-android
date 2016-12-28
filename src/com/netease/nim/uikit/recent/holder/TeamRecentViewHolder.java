package com.netease.nim.uikit.recent.holder;

import android.text.TextUtils;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.cache.TeamDataCache;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseQuickAdapter;
import com.netease.nim.uikit.recent.AitHelper;
import com.netease.nimlib.sdk.msg.attachment.NotificationAttachment;
import com.netease.nimlib.sdk.msg.model.RecentContact;

public class TeamRecentViewHolder extends CommonRecentViewHolder {

    public TeamRecentViewHolder(BaseQuickAdapter adapter) {
        super(adapter);
    }

    @Override
    protected String getContent(RecentContact recent) {
        String content = descOfMsg(recent);

        String fromId = recent.getFromAccount();
        if (!TextUtils.isEmpty(fromId)
                && !fromId.equals(NimUIKit.getAccount())
                && !(recent.getAttachment() instanceof NotificationAttachment)) {
            String tid = recent.getContactId();
            String teamNick = getTeamUserDisplayName(tid, fromId);
            content = teamNick + ": " + content;

            if (AitHelper.hasAitExtention(recent)) {
                if (recent.getUnreadCount() == 0) {
                    AitHelper.clearRecentContactAited(recent);
                } else {
                    content = AitHelper.getAitAlertString(content);
                }
            }
        }

        return content;
    }

    private String getTeamUserDisplayName(String tid, String account) {
        return TeamDataCache.getInstance().getTeamMemberDisplayName(tid, account);
    }
}
