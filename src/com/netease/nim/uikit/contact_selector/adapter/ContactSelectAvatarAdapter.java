package com.netease.nim.uikit.contact_selector.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.contact.core.model.IContact;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class GalleryItemViewHolder {
    HeadImageView imageView;
}

public class ContactSelectAvatarAdapter extends BaseAdapter {
    private Context context;

    private List<IContact> selectedContactItems;

    public ContactSelectAvatarAdapter(Context context) {
        this.context = context;
        this.selectedContactItems = new ArrayList<IContact>();
        selectedContactItems.add(null);
    }

    @Override
    public int getCount() {
        return selectedContactItems.size();
    }

    @Override
    public Object getItem(int position) {
        return selectedContactItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HeadImageView imageView;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.nim_contact_select_area_item, null);
            imageView = (HeadImageView) convertView.findViewById(R.id.contact_select_area_image);

            GalleryItemViewHolder holder = new GalleryItemViewHolder();
            holder.imageView = imageView;
            convertView.setTag(holder);
        } else {
            GalleryItemViewHolder holder = (GalleryItemViewHolder) convertView.getTag();
            imageView = holder.imageView;
        }

        try {
            IContact item = selectedContactItems.get(position);
            if (item == null) {
                imageView.setBackgroundResource(R.drawable.nim_contact_select_dot_avatar);
                imageView.setImageDrawable(null);
            } else {
                imageView.loadBuddyAvatar(item.getContactId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    public void addContact(IContact contact) {
        if (selectedContactItems.size() > 0) {
            IContact iContact = selectedContactItems.get(selectedContactItems.size() - 1);
            if (iContact == null) {
                selectedContactItems.remove(selectedContactItems.size() - 1);
            }
        }
        this.selectedContactItems.add(contact);
        selectedContactItems.add(null);
    }

    public void removeContact(IContact contact) {
        if (contact == null) {
            return;
        }
        for (Iterator<IContact> iterator = selectedContactItems.iterator(); iterator.hasNext(); ) {
            IContact iContact = iterator.next();
            if (iContact == null) {
                continue;
            }
            if (iContact.getContactId().equals(contact.getContactId())) {
                iterator.remove();
            }
        }
    }

    public IContact remove(int pos) {
        return this.selectedContactItems.remove(pos);
    }

    public List<IContact> getSelectedContacts() {
        return this.selectedContactItems.subList(0, selectedContactItems.size() - 1);
    }
}
