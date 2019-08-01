package com.netease.nim.uikit.business.contact.core.model;

import com.netease.nim.uikit.business.contact.core.item.AbsContactItem;
import com.netease.nim.uikit.business.contact.core.item.ContactItemFilter;
import com.netease.nim.uikit.business.contact.core.query.IContactDataProvider;
import com.netease.nim.uikit.business.contact.core.query.TextQuery;

import java.util.List;

/**
 * 通讯录获取数据任务
 * Created by huangjun on 2015/2/10.
 */
public class ContactDataTask {

    public interface Host {
        public void onData(ContactDataTask task, AbsContactDataList datas, boolean all); // 搜索完成，返回数据给调用方

        public boolean isCancelled(ContactDataTask task); // 判断调用放是否已经取消
    }

    private final IContactDataProvider dataProvider; // 数据源提供者

    private final ContactItemFilter filter; // 项过滤器

    private final TextQuery query; // 要搜索的信息，null为查询所有

    private Host host;

    public ContactDataTask(TextQuery query, IContactDataProvider dataProvider, ContactItemFilter filter) {
        this.query = query;
        this.dataProvider = dataProvider;
        this.filter = filter;
    }

    public final void setHost(Host host) {
        this.host = host;
    }

    protected void onPreProvide(AbsContactDataList datas) {

    }

    public final void run(AbsContactDataList datas) {
        // CANCELLED
        if (isCancelled()) {
            return;
        }

        // PRE PROVIDE
        onPreProvide(datas);

        // CANCELLED
        if (isCancelled()) {
            return;
        }

        // PROVIDE
        List<AbsContactItem> items = dataProvider.provide(query);

        // ADD
        add(datas, items, filter);

        // BUILD
        datas.build();

        // PUBLISH ALL
        publish(datas, true);
    }

    private void publish(AbsContactDataList datas, boolean all) {
        if (host != null) {
            datas.setQuery(query);

            host.onData(this, datas, all);
        }
    }

    private boolean isCancelled() {
        return host != null && host.isCancelled(this);
    }

    private static void add(AbsContactDataList datas, List<AbsContactItem> items, ContactItemFilter filter) {
        for (AbsContactItem item : items) {
            if (filter == null || !filter.filter(item)) {
                datas.add(item);
            }
        }
    }
}