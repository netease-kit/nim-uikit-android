package com.netease.nim.uikit.business.contact.core.item;

import com.netease.nim.uikit.business.contact.core.model.IContact;
import com.netease.nimlib.sdk.search.model.MsgIndexRecord;

public class MsgItem extends AbsContactItem {
    private final IContact contact;

    private final MsgIndexRecord record;

    private final boolean querySession;

    public MsgItem(IContact contact, MsgIndexRecord record, boolean querySession) {
        this.contact = contact;
        this.record = record;
        this.querySession = querySession;
    }

    public IContact getContact() {
        return contact;
    }

    public MsgIndexRecord getRecord() {
        return record;
    }

    public boolean isQuerySession() {
        return querySession;
    }

    @Override
    public int getItemType() {
        return ItemTypes.MSG;
    }

    @Override
    public String belongsGroup() {
        return null;
    }
}
