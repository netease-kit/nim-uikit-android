package com.netease.yunxin.kit.chatkit.ui.view.emoji;

public class StickerItem {
    private String category;
    private String name;

    public StickerItem(String category, String name) {
        this.category = category;
        this.name = name;
    }

    public String getIdentifier() {
        return category + "/" + name;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof StickerItem) {
            StickerItem item = (StickerItem) o;
            return item.getCategory().equals(category) && item.getName().equals(name);
        }

        return false;
    }
}
