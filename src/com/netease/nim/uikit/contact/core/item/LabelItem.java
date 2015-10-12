package com.netease.nim.uikit.contact.core.item;

public class LabelItem extends AbsContactItem {
    private final String text;

    public LabelItem(String text) {
        this.text = text;
    }

    @Override
    public int getItemType() {
        return ItemTypes.LABEL;
    }

    @Override
    public String belongsGroup() {
        return null;
    }

    public final String getText() {
        return text;
    }
}
