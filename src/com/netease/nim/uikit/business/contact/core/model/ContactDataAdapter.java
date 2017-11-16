package com.netease.nim.uikit.business.contact.core.model;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.netease.nim.uikit.business.contact.core.item.AbsContactItem;
import com.netease.nim.uikit.business.contact.core.item.ContactItemFilter;
import com.netease.nim.uikit.business.contact.core.model.ContactDataTask.Host;
import com.netease.nim.uikit.business.contact.core.query.IContactDataProvider;
import com.netease.nim.uikit.business.contact.core.query.TextQuery;
import com.netease.nim.uikit.business.contact.core.viewholder.AbsContactViewHolder;
import com.netease.nim.uikit.common.ui.liv.LetterIndexView;
import com.netease.nim.uikit.common.ui.liv.LivIndex;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.impl.cache.UIKitLogTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 通讯录数据适配器
 * <p/>
 * Created by huangjun on 2015/2/10.
 */
public class ContactDataAdapter extends BaseAdapter {

    //
    // COMPONENTS
    //

    private final Context context;

    private final Map<Integer, Class<? extends AbsContactViewHolder<? extends AbsContactItem>>> viewHolderMap;

    private final ContactGroupStrategy groupStrategy;

    private final IContactDataProvider dataProvider;

    //
    // DATAS
    //

    private AbsContactDataList datas;

    protected final HashMap<String, Integer> indexes = new HashMap<>();

    //
    // OPTIONS
    //

    private ContactItemFilter filter;

    private ContactItemFilter disableFilter;

    public ContactDataAdapter(Context context, ContactGroupStrategy groupStrategy, IContactDataProvider dataProvider) {
        this.context = context;
        this.groupStrategy = groupStrategy;
        this.dataProvider = dataProvider;
        this.viewHolderMap = new HashMap<>(6);
    }

    public void addViewHolder(int itemDataType, Class<? extends AbsContactViewHolder<? extends AbsContactItem>> viewHolder) {
        this.viewHolderMap.put(itemDataType, viewHolder);
    }

    public final void setFilter(ContactItemFilter filter) {
        this.filter = filter;
    }

    public final void setDisableFilter(ContactItemFilter disableFilter) {
        this.disableFilter = disableFilter;
    }

    public final LivIndex createLivIndex(ListView lv, LetterIndexView liv, TextView tvHit, ImageView ivBk) {
        return new LivIndex(lv, liv, tvHit, ivBk, getIndexes());
    }

    //
    // BaseAdapter
    //

    @Override
    public int getCount() {
        return datas != null ? datas.getCount() : 0;
    }

    @Override
    public Object getItem(int position) {
        return datas != null ? datas.getItem(position) : null;
    }

    @Override
    public boolean isEmpty() {
        return datas != null ? datas.isEmpty() : true;
    }

    public final TextQuery getQuery() {
        return datas != null ? datas.getQuery() : null;
    }

    private void updateData(AbsContactDataList datas) {
        this.datas = datas;

        updateIndexes(datas.getIndexes());

        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        Object obj = getItem(position);
        if (obj == null) {
            return -1;
        }
        AbsContactItem item = (AbsContactItem) obj;
        int type = item.getItemType();
        Integer[] types = viewHolderMap.keySet().toArray(new Integer[viewHolderMap.size()]);

        for (int i = 0; i < types.length; i++) {
            int itemType = types[i];
            if (itemType == type) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getViewTypeCount() {
        return viewHolderMap.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AbsContactItem item = (AbsContactItem) getItem(position);
        if (item == null) {
            return null;
        }
        AbsContactViewHolder<AbsContactItem> holder = null;
        try {
            if (convertView == null || (holder = (AbsContactViewHolder<AbsContactItem>) convertView.getTag()) == null) {
                holder = (AbsContactViewHolder<AbsContactItem>) viewHolderMap.get(item.getItemType()).newInstance();
                if (holder != null) {
                    holder.create(context);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (holder == null) {
            return null;
        }

        holder.refresh(this, position, item);
        convertView = holder.getView();
        if (convertView != null) {
            convertView.setTag(holder);
        }

        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        if (disableFilter != null) {
            return !disableFilter.filter((AbsContactItem) getItem(position));
        }

        return true;
    }

    //
    // LOAD
    //

    public final void query(String query) {
        startTask(new TextQuery(query), true);
    }

    public final boolean load(boolean reload) {
        if (!reload && !isEmpty()) {
            return false;
        }

        LogUtil.i(UIKitLogTag.CONTACT, "contact load data");

        startTask(null, false);

        return true;
    }

    public final void query(TextQuery query) {
        startTask(query, true);
    }

    private final List<Task> tasks = new ArrayList<>();

    /**
     * 启动搜索任务
     *
     * @param query 要搜索的信息，填null表示查询所有数据
     * @param abort 是否终止：例如搜索的时候，第一个搜索词还未搜索完成，第二个搜索词已生成，那么取消之前的搜索任务
     */
    private void startTask(TextQuery query, boolean abort) {
        if (abort) {
            for (Task task : tasks) {
                task.cancel(false); // 设为true有风险！
            }
        }

        Task task = new Task(new ContactDataTask(query, dataProvider, filter) {
            @Override
            protected void onPreProvide(AbsContactDataList datas) {
                List<? extends AbsContactItem> itemsND = onNonDataItems();

                if (itemsND != null) {
                    for (AbsContactItem item : itemsND) {
                        datas.add(item);
                    }
                }
            }
        });

        tasks.add(task);

        task.execute();
    }

    private void onTaskFinish(Task task) {
        tasks.remove(task);
    }

    /**
     * 搜索/查询数据异步任务
     */

    private class Task extends AsyncTask<Void, Object, Void> implements Host {
        final ContactDataTask task;

        Task(ContactDataTask task) {
            task.setHost(this);

            this.task = task;
        }

        //
        // HOST
        //

        @Override
        public void onData(ContactDataTask task, AbsContactDataList datas, boolean all) {
            publishProgress(datas, all);
        }

        @Override
        public boolean isCancelled(ContactDataTask task) {
            return isCancelled();
        }

        //
        // AsyncTask
        //

        @Override
        protected void onPreExecute() {
            onPreReady();
        }

        @Override
        protected Void doInBackground(Void... params) {
            task.run(new ContactDataList(groupStrategy));

            return null;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            AbsContactDataList datas = (AbsContactDataList) values[0];
            boolean all = (Boolean) values[1];

            onPostLoad(datas.isEmpty(), datas.getQueryText(), all);

            updateData(datas);
        }

        @Override
        protected void onPostExecute(Void result) {
            onTaskFinish(this);
        }

        @Override
        protected void onCancelled() {
            onTaskFinish(this);
        }
    }

    //
    // Overrides
    //

    /**
     * 数据未准备
     */
    protected void onPreReady() {
    }

    /**
     * 数据加载完成
     */
    protected void onPostLoad(boolean empty, String query, boolean all) {
    }

    /**
     * 加载完成后，加入非数据项
     *
     * @return
     */
    protected List<? extends AbsContactItem> onNonDataItems() {
        return null;
    }

    //
    // INDEX
    //

    private Map<String, Integer> getIndexes() {
        return this.indexes;
    }

    private void updateIndexes(Map<String, Integer> indexes) {
        // CLEAR
        this.indexes.clear();
        // SET
        this.indexes.putAll(indexes);
    }
}
