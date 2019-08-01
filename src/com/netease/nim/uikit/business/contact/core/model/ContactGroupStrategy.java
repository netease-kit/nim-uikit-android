package com.netease.nim.uikit.business.contact.core.model;

import com.netease.nim.uikit.business.contact.core.item.AbsContactItem;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * 通讯录分组策略
 * Created by huangjun on 2015/2/10.
 */

public class ContactGroupStrategy implements Comparator<String> {
    public static final String GROUP_SHARP = "#";

    public static final String GROUP_TEAM = "@";

    public static final String GROUP_NULL = "?";

    private static final class Group {
        private final int order;

        private final String name;

        public Group(int order, String name) {
            this.order = order;
            this.name = name;
        }
    }

    private final Map<String, Group> groups = new HashMap<String, Group>();

    public String belongs(AbsContactItem item) {
        return item.belongsGroup();
    }

    protected final void add(String id, int order, String name) {
        groups.put(id, new Group(order, name));
    }

    protected final int addABC(int order) {
        String id = ContactGroupStrategy.GROUP_SHARP;

        add(id, order++, id);

        for (char i = 0; i < 26; i++) {
            id = Character.toString((char) ('A' + i));

            add(id, order++, id);
        }

        return order;
    }

    public final String getName(String id) {
        Group group = groups.get(id);
        String name = group != null ? group.name : null;
        return name != null ? name : "";
    }

    private Integer toOrder(String id) {
        Group group = groups.get(id);
        return group != null ? group.order : null;
    }

    @Override
    public int compare(String lhs, String rhs) {
        if (lhs == null) {
            lhs = ContactGroupStrategy.GROUP_NULL;
        }

        if (rhs == null) {
            rhs = ContactGroupStrategy.GROUP_NULL;
        }

        Integer lhsO = toOrder(lhs);
        Integer rhsO = toOrder(rhs);

        if (lhsO == rhsO) {
            return 0;
        }

        if (lhsO == null) {
            return -1;
        }

        if (rhsO == null) {
            return 1;
        }

        return lhsO - rhsO;
    }
}
