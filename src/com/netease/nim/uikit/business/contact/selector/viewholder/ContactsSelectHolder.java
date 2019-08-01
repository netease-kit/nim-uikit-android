package com.netease.nim.uikit.business.contact.selector.viewholder;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.contact.core.item.ContactItem;
import com.netease.nim.uikit.business.contact.core.model.ContactDataAdapter;
import com.netease.nim.uikit.business.contact.core.model.IContact;
import com.netease.nim.uikit.business.contact.core.viewholder.AbsContactViewHolder;
import com.netease.nim.uikit.business.contact.selector.adapter.ContactSelectAdapter;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;

public class ContactsSelectHolder extends AbsContactViewHolder<ContactItem> {
    private final boolean multi;

    private HeadImageView image;

    private TextView nickname;

    private ImageView select;

    private Drawable defaultBackground;

    public ContactsSelectHolder() {
        this(false);
    }

    ContactsSelectHolder(boolean multi) {
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
                setBackground(getView(), defaultBackground);
                this.select.setBackgroundResource(R.drawable.nim_contact_checkbox_checked_green);
            } else {
                setBackground(getView(), defaultBackground);
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
            this.image.loadTeamIconByTeam(NimUIKit.getTeamProvider().getTeamById(contact.getContactId()));
        }

        this.image.setVisibility(View.VISIBLE);
    }

    @Override
    public View inflate(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.nim_contacts_select_item, null);
        defaultBackground = view.getBackground();
        this.image = view.findViewById(R.id.img_head);
        this.nickname = view.findViewById(R.id.tv_nickname);
        this.select = view.findViewById(R.id.imgSelect);
        return view;
    }

    private void setBackground(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }
}
