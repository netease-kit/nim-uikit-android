package com.netease.nim.uikit.contact.core.model;

import android.text.TextUtils;

import com.netease.nim.uikit.contact.core.item.AbsContactItem;
import com.netease.nim.uikit.contact.core.item.LabelItem;
import com.netease.nim.uikit.contact.core.query.TextQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通讯录列表数据抽象类
 * Group定义
 *
 * Created by huangjun on 2015/2/10.
 */
public abstract class AbsContactDataList {
    protected final ContactGroupStrategy groupStrategy;

    protected final Map<String, Group> groupMap = new HashMap<>();

    protected final Group groupNull = new Group(null, null);

    private TextQuery query;

    private static final class NoneGroupStrategy extends ContactGroupStrategy {
        @Override
        public String belongs(AbsContactItem item) {
            return null;
        }

        @Override
        public int compare(String lhs, String rhs) {
            return 0;
        }
    }

    public AbsContactDataList(ContactGroupStrategy groupStrategy) {
        if (groupStrategy == null) {
            groupStrategy = new NoneGroupStrategy();
        }

        this.groupStrategy = groupStrategy;
    }

    //
    // ACCESS
    //

    public abstract int getCount();

    public abstract boolean isEmpty();

    public abstract AbsContactItem getItem(int index);

    public abstract List<AbsContactItem> getItems();

    public abstract Map<String, Integer> getIndexes();

    public final TextQuery getQuery() {
        return query;
    }

    public final String getQueryText() {
        return query != null ? query.text : null;
    }

    public final void setQuery(TextQuery query) {
        this.query = query;
    }

    //
    // BUILD
    //

    public abstract void build();

    public final void add(AbsContactItem item) {
        if (item == null) {
            return;
        }

        Group group;

        String id = groupStrategy.belongs(item);
        if (id == null) {
            group = groupNull;
        } else {
            group = groupMap.get(id);
            if (group == null) {
                group = new Group(id, groupStrategy.getName(id));
                groupMap.put(id, group);
            }
        }

        group.add(item);
    }

    protected final void sortGroups(List<Group> groups) {
        Collections.sort(groups, new Comparator<Group>() {
            @Override
            public int compare(Group lhs, Group rhs) {
                return groupStrategy.compare(lhs.id, rhs.id);
            }
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected static final class Group {
        final String id;

        final String title;

        final boolean hasHead;

        final List items = new ArrayList();

        Group(String id, String title) {
            this.id = id;
            this.title = title;
            this.hasHead = !TextUtils.isEmpty(title);
        }

        int getCount() {
            return items.size() + (hasHead ? 1 : 0);
        }

        AbsContactItem getItem(int index) {
            if (hasHead) {
                if (index == 0) {
                    return getHead();
                } else {
                    index--;
                    return (AbsContactItem) (index >= 0 && index < items.size() ? items.get(index) : null);
                }
            } else {
                return (AbsContactItem) (index >= 0 && index < items.size() ? items.get(index) : null);
            }
        }

        AbsContactItem getHead() {
            return hasHead ? new LabelItem(title) : null;
        }

        List<AbsContactItem> getItems() {
            return items;
        }

        void add(AbsContactItem add) {
            if (add instanceof Comparable) {
                addComparable((Comparable<AbsContactItem>) add);
            } else {
                items.add(add);
            }
        }

        void merge(Group group) {
            for (Object item : group.items) {
                add((AbsContactItem) item);
            }
        }

        void addComparable(Comparable<AbsContactItem> add) {
            if (items.size() < 8) {
                for (int index = 0; index < items.size(); index++) {
                    Comparable<AbsContactItem> item = (Comparable<AbsContactItem>) items.get(index);
                    if ((item.compareTo((AbsContactItem) add)) > 0) {
                        items.add(index, add);
                        return;
                    }
                }
                items.add(add);
            } else {
                int index = Collections.binarySearch(items, add);
                if (index < 0) {
                    index = -index;
                    --index;
                }
                if (index >= items.size()) {
                    items.add(add);
                } else {
                    items.add(index, add);
                }
            }
        }
    }
}
