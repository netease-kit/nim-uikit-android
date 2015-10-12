package com.netease.nim.uikit.contact.core.item;

/**
 * 通讯录数据项抽象类
 * Created by huangjun on 2015/2/10.
 */
public abstract class AbsContactItem {
    /**
     * 所属的类型
     *
     * @see com.netease.nim.uikit.contact.core.item.ItemTypes
     */
    public abstract int getItemType();

    /**
     * 所属的分组
     */
    public abstract String belongsGroup();

    protected final int compareType(AbsContactItem item) {
        return compareType(getItemType(), item.getItemType());
    }

    public static int compareType(int lhs, int rhs) {
        return lhs - rhs;
    }
}
