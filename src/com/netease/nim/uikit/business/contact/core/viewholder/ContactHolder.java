package com.netease.nim.uikit.business.contact.core.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.contact.core.item.ContactItem;
import com.netease.nim.uikit.business.contact.core.model.ContactDataAdapter;
import com.netease.nim.uikit.business.contact.core.model.IContact;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.team.model.Team;

public class ContactHolder extends AbsContactViewHolder<ContactItem> {

    protected HeadImageView head;

    protected TextView name;

    protected TextView desc;

    protected RelativeLayout headLayout;

    @Override
    public void refresh(ContactDataAdapter adapter, int position, final ContactItem item) {
        // contact info
        final IContact contact = item.getContact();
        if (contact.getContactType() == IContact.Type.Friend) {
            head.loadBuddyAvatar(contact.getContactId());
        } else {
            Team team = NimUIKit.getTeamProvider().getTeamById(contact.getContactId());
            head.loadTeamIconByTeam(team);
        }
        name.setText(contact.getDisplayName());
        headLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contact.getContactType() == IContact.Type.Friend) {
                    if (NimUIKitImpl.getContactEventListener() != null) {
                        NimUIKitImpl.getContactEventListener().onAvatarClick(context, item.getContact().getContactId());
                    }
                }
            }
        });

        // query result
        desc.setVisibility(View.GONE);
        /*
        TextQuery query = adapter.getQuery();
        HitInfo hitInfo = query != null ? ContactSearch.hitInfo(contact, query) : null;
        if (hitInfo != null && !hitInfo.text.equals(contact.getDisplayName())) {
            desc.setVisibility(View.VISIBLE);
        } else {
            desc.setVisibility(View.GONE);
        }
        */
    }

    @Override
    public View inflate(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.nim_contacts_item, null);

        headLayout = (RelativeLayout) view.findViewById(R.id.head_layout);
        head = (HeadImageView) view.findViewById(R.id.contacts_item_head);
        name = (TextView) view.findViewById(R.id.contacts_item_name);
        desc = (TextView) view.findViewById(R.id.contacts_item_desc);

        return view;
    }
}
