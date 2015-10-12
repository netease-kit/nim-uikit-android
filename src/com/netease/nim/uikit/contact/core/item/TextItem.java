package com.netease.nim.uikit.contact.core.item;

import com.netease.nim.uikit.contact.core.model.ContactGroupStrategy;
import com.netease.nim.uikit.contact.core.query.TextComparator;

import android.text.TextUtils;

public class TextItem extends AbsContactItem implements Comparable<TextItem> {
	private final String text;
	
	public TextItem(String text) {
		this.text = text != null ? text : "";
	}
	
	public final String getText() {
		return text;
	}

	@Override
	public int getItemType() {
		return ItemTypes.TEXT;
	}

	@Override
	public String belongsGroup() {
		String group = TextComparator.getLeadingUp(text);
		
		return !TextUtils.isEmpty(group) ? group : ContactGroupStrategy.GROUP_SHARP;
	}

	@Override
	public int compareTo(TextItem item) {
		return TextComparator.compareIgnoreCase(text, item.text);
	}
}
