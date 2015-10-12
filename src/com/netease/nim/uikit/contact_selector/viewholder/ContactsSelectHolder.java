package com.netease.nim.uikit.contact_selector.viewholder;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.ptr.ViewCompat;
import com.netease.nim.uikit.contact.core.item.ContactItem;
import com.netease.nim.uikit.contact.core.model.ContactDataAdapter;
import com.netease.nim.uikit.contact.core.model.IContact;
import com.netease.nim.uikit.contact.core.viewholder.AbsContactViewHolder;
import com.netease.nim.uikit.contact_selector.adapter.ContactSelectAdapter;

public class ContactsSelectHolder extends AbsContactViewHolder<ContactItem> {
    private final boolean multi;

    private HeadImageView image;

    private TextView nickname;

    private ImageView select;

    private Drawable defaultBackground;

    public ContactsSelectHolder() {
        this(false);
    }

    public ContactsSelectHolder(boolean multi) {
        this.multi = multi;
    }

    @Override
    public void refresh(ContactDataAdapter adapter, int position, ContactItem item) {
        if (multi) {
            boolean disabled = !adapter.isEnabled(position);
            boolean selected = adapter instanceof ContactSelectAdapter ? ((ContactSelectAdapter) adapter).isSelected(position) : false;
            this.select.setVisibility(View.VISIBLE);
            if (disabled) {
                this.select.setBackgroundResource(R.drawable.nim_contact_checkbox_checked_grey);
                getView().setBackgroundColor(context.getResources().getColor(R.color.transparent));
            } else if (selected) {
                ViewCompat.setBackground(getView(), defaultBackground);
                this.select.setBackgroundResource(R.drawable.nim_contact_checkbox_checked_green);
            } else {
                ViewCompat.setBackground(getView(), defaultBackground);
                this.select.setBackgroundResource(R.drawable.nim_contact_checkbox_unchecked);
            }
        } else {
            this.select.setVisibility(View.GONE);
        }

        IContact contact = item.getContact();
        this.nickname.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        this.nickname.setText(contact.getDisplayName());
        if (contact.getContactType() == IContact.Type.Friend || contact.getContactType() == IContact.Type.TeamMember) {
            this.nickname.setText(contact.getDisplayName());
            this.image.loadBuddyAvatar(contact.getContactId());
        } else if (contact.getContactType() == IContact.Type.Team) {
            this.image.setImageBitmap(NimUIKit.getUserInfoProvider().getTeamIcon(contact.getContactId()));
        }

        this.image.setVisibility(View.VISIBLE);
    }

    @Override
    public View inflate(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.nim_contacts_select_item, null);
        defaultBackground = view.getBackground();
        this.image = (HeadImageView) view.findViewById(R.id.img_head);
        this.nickname = (TextView) view.findViewById(R.id.tv_nickname);
        this.select = (ImageView) view.findViewById(R.id.imgSelect);
        return view;
    }
}
