package com.netease.nim.uikit.contact.core.model;

import com.netease.nim.uikit.contact.core.item.AbsContactItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 通讯录列表数据(分组、索引)
 * Created by huangjun on 2015/2/10.
 */
public class ContactDataList extends AbsContactDataList {
    //
    // RESULT DATA
    //

    private List<Group> groups;

    private Map<String, Integer> indexes;

    public ContactDataList(ContactGroupStrategy groupStrategy) {
        super(groupStrategy);
    }

    @Override
    public int getCount() {
        int count = 0;
        for (Group group : groups) {
            count += group.getCount();
        }
        return count;
    }

    @Override
    public AbsContactItem getItem(int index) {
        int count = 0;
        for (Group group : groups) {
            int gIndex = index - count;
            int gCount = group.getCount();

            if (gIndex >= 0 && gIndex < gCount) {
                return group.getItem(gIndex);
            }

            count += gCount;
        }

        return null;
    }

    @Override
    public boolean isEmpty() {
        return groups.isEmpty() || indexes.isEmpty();
    }

    @Override
    public List<AbsContactItem> getItems() {
        List<AbsContactItem> items = new ArrayList<AbsContactItem>();
        for (Group group : groups) {
            AbsContactItem head = group.getHead();
            if (head != null) {
                items.add(head);
            }
            items.addAll(group.getItems());
        }

        return items;
    }

    @Override
    public Map<String, Integer> getIndexes() {
        return indexes;
    }

    @Override
    public void build() {
        //
        // GROUPS
        //

        List<Group> groups = new ArrayList<Group>();
        groups.add(groupNull);
        groups.addAll(groupMap.values());
        sortGroups(groups);

        //
        // INDEXES
        //

        Map<String, Integer> indexes = new HashMap<String, Integer>();
        int count = 0;
        for (Group group : groups) {
            if (group.id != null) {
                indexes.put(group.id, count);
            }

            count += group.getCount();
        }

        //
        // RESULT
        //

        this.groups = groups;
        this.indexes = indexes;
    }
}
