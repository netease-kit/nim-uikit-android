package com.netease.nim.uikit.business.contact.core.provider;

import android.text.TextUtils;

import com.netease.nim.uikit.business.contact.core.item.AbsContactItem;
import com.netease.nim.uikit.business.contact.core.item.MsgItem;
import com.netease.nim.uikit.business.contact.core.query.TextQuery;
import com.netease.nim.uikit.business.contact.core.util.ContactHelper;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.lucene.LuceneService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.search.model.MsgIndexRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息全文检索数据提供者
 */
public final class MsgDataProvider {

    private static final String TAG = "MsgDataProvider";

    public static final List<AbsContactItem> provide(TextQuery query) {
        if (TextUtils.isEmpty(query.text) || TextUtils.isEmpty(query.text.trim())) {
            return new ArrayList<>(0);
        }

        // fetch result
        List<MsgIndexRecord> sources;
        boolean querySession;
        if (query.extra != null) {
            SessionTypeEnum sessionType = (SessionTypeEnum) query.extra[0];
            String sessionId = (String) query.extra[1];
            MsgIndexRecord anchor = null;
            if (query.extra.length >= 3) {
                anchor = (MsgIndexRecord) query.extra[2];
            }
            sources = searchSession(query.text, sessionType, sessionId, anchor);
            querySession = true;
        } else {
            sources = searchAllSession(query.text);
            querySession = false;
        }

        // build AbsContactItem
        if (sources == null) {
            return new ArrayList<>(0);
        }

        List<AbsContactItem> items = new ArrayList<>(sources.size());
        for (MsgIndexRecord r : sources) {
            items.add(new MsgItem(ContactHelper.makeContactFromMsgIndexRecord(r), r, querySession));
        }

        return items;
    }

    private static List<MsgIndexRecord> searchSession(String query, SessionTypeEnum sessionType, String sessionId, MsgIndexRecord anchor) {
        long startTime = System.currentTimeMillis();

        List<MsgIndexRecord> result;
        if (anchor != null) {
            result = NIMClient.getService(LuceneService.class).searchSessionNextPageBlock(query, sessionType, sessionId, anchor, 50);
        } else {
            result = NIMClient.getService(LuceneService.class).searchSessionBlock(query, sessionType, sessionId);
        }

        log(true, result, System.currentTimeMillis() - startTime);

        return result;
    }

    private static List<MsgIndexRecord> searchAllSession(String query) {
        long startTime = System.currentTimeMillis();
        List<MsgIndexRecord> result = NIMClient.getService(LuceneService.class).searchAllSessionBlock(query, -1);
        log(false, result, System.currentTimeMillis() - startTime);

        return result;
    }

    private static void log(boolean searchSession, List<MsgIndexRecord> result, long cost) {
        LogUtil.d(TAG, (searchSession ? "search session" : "search all session") + ", result size=" + (result == null ? 0 : result.size()) + ", cost=" + cost);
    }
}