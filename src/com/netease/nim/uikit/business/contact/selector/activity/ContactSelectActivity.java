package com.netease.nim.uikit.business.contact.selector.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions;
import com.netease.nim.uikit.business.contact.core.item.AbsContactItem;
import com.netease.nim.uikit.business.contact.core.item.ContactItem;
import com.netease.nim.uikit.business.contact.core.item.ContactItemFilter;
import com.netease.nim.uikit.business.contact.core.item.ItemTypes;
import com.netease.nim.uikit.business.contact.core.model.ContactGroupStrategy;
import com.netease.nim.uikit.business.contact.core.model.IContact;
import com.netease.nim.uikit.business.contact.core.provider.ContactDataProvider;
import com.netease.nim.uikit.business.contact.core.provider.TeamMemberDataProvider;
import com.netease.nim.uikit.business.contact.core.query.IContactDataProvider;
import com.netease.nim.uikit.business.contact.core.query.TextQuery;
import com.netease.nim.uikit.business.contact.core.viewholder.LabelHolder;
import com.netease.nim.uikit.business.contact.selector.adapter.ContactSelectAdapter;
import com.netease.nim.uikit.business.contact.selector.adapter.ContactSelectAvatarAdapter;
import com.netease.nim.uikit.business.contact.selector.viewholder.ContactsMultiSelectHolder;
import com.netease.nim.uikit.business.contact.selector.viewholder.ContactsSelectHolder;
import com.netease.nim.uikit.common.activity.ToolBarOptions;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.liv.LetterIndexView;
import com.netease.nim.uikit.common.ui.liv.LivIndex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 联系人选择器
 * <p/>
 * Created by huangjun on 2015/3/3.
 */
public class ContactSelectActivity extends UI implements View.OnClickListener, android.support.v7.widget.SearchView.OnQueryTextListener {

    public static final String EXTRA_DATA = "EXTRA_DATA"; // 请求数据：Option
    public static final String RESULT_DATA = "RESULT_DATA"; // 返回结果

    // adapter

    private ContactSelectAdapter contactAdapter;

    private ContactSelectAvatarAdapter contactSelectedAdapter;

    // view

    private ListView listView;

    private LivIndex livIndex;

    private RelativeLayout bottomPanel;

    private HorizontalScrollView scrollViewSelected;

    private GridView imageSelectedGridView;

    private Button btnSelect;

    private SearchView searchView;

    // other

    private String queryText;

    private Option option;

    // class

    private static class ContactsSelectGroupStrategy extends ContactGroupStrategy {
        public ContactsSelectGroupStrategy() {
            add(ContactGroupStrategy.GROUP_NULL, -1, "");
            addABC(0);
        }
    }

    /**
     * 联系人选择器配置可选项
     */
    public enum ContactSelectType {
        BUDDY,
        TEAM_MEMBER,
        TEAM
    }

    public static class Option implements Serializable {

        /**
         * 联系人选择器中数据源类型：好友（默认）、群、群成员（需要设置teamId）
         */
        public ContactSelectType type = ContactSelectType.BUDDY;

        /**
         * 联系人选择器数据源类型为群成员时，需要设置群号
         */
        public String teamId = null;

        /**
         * 联系人选择器标题
         */
        public String title = "联系人选择器";

        /**
         * 联系人单选/多选（默认）
         */
        public boolean multi = true;

        /**
         * 至少选择人数
         */
        public int minSelectNum = 1;

        /**
         * 低于最少选择人数的提示
         */
        public String minSelectedTip = null;

        /**
         * 最大可选人数
         */
        public int maxSelectNum = 2000;

        /**
         * 超过最大可选人数的提示
         */
        public String maxSelectedTip = null;

        /**
         * 是否显示已选头像区域
         */
        public boolean showContactSelectArea = true;

        /**
         * 默认勾选（且可操作）的联系人项
         */
        public ArrayList<String> alreadySelectedAccounts = null;

        /**
         * 需要过滤（不显示）的联系人项
         */
        public ContactItemFilter itemFilter = null;

        /**
         * 需要disable(可见但不可操作）的联系人项
         */
        public ContactItemFilter itemDisableFilter = null;

        /**
         * 是否支持搜索
         */
        public boolean searchVisible = true;

        /**
         * 允许不选任何人点击确定
         */
        public boolean allowSelectEmpty = false;

        /**
         * 是否显示最大数目，结合maxSelectNum,与搜索位置相同
         */
        public boolean maxSelectNumVisible = false;
    }

    public static void startActivityForResult(Context context, Option option, int requestCode) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATA, option);
        intent.setClass(context, ContactSelectActivity.class);

        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    @Override
    public void onBackPressed() {
        if (searchView != null) {
            searchView.setQuery("", true);
            searchView.setIconified(true);
        }
        showKeyboard(false);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        // search view
        getMenuInflater().inflate(R.menu.nim_contacts_search_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        if (!option.searchVisible) {
            item.setVisible(false);
            return true;
        }

        MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                finish();
                return false;
            }
        });
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        this.searchView = searchView;
        this.searchView.setVisibility(option.searchVisible ? View.VISIBLE : View.GONE);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_contacts_select);

        ToolBarOptions options = new NimToolBarOptions();
        setToolBar(R.id.toolbar, options);

        parseIntentData();
        initAdapter();
        initListView();
        initContactSelectArea();

        loadData();
    }

    private void parseIntentData() {
        this.option = (Option) getIntent().getSerializableExtra(EXTRA_DATA);
        if (TextUtils.isEmpty(option.maxSelectedTip)) {
            option.maxSelectedTip = "最多选择" + option.maxSelectNum + "人";
        }
        if (TextUtils.isEmpty(option.minSelectedTip)) {
            option.minSelectedTip = "至少选择" + option.minSelectNum + "人";
        }
        setTitle(option.title);
    }

    private class ContactDataProviderEx extends ContactDataProvider {
        private String teamId;

        private boolean loadedTeamMember = false;

        public ContactDataProviderEx(String teamId, int... itemTypes) {
            super(itemTypes);
            this.teamId = teamId;
        }

        @Override
        public List<AbsContactItem> provide(TextQuery query) {
            List<AbsContactItem> data = new ArrayList<>();
            // 异步加载
            if (!loadedTeamMember) {
                TeamMemberDataProvider.loadTeamMemberDataAsync(teamId, new TeamMemberDataProvider.LoadTeamMemberCallback() {
                    @Override
                    public void onResult(boolean success) {
                        if (success) {
                            loadedTeamMember = true;
                            // 列表重新加载数据
                            loadData();
                        }
                    }
                });
            } else {
                data = TeamMemberDataProvider.provide(query, teamId);
            }
            return data;
        }
    }

    private void initAdapter() {
        IContactDataProvider dataProvider;
        if (option.type == ContactSelectType.TEAM_MEMBER && !TextUtils.isEmpty(this.option.teamId)) {
            dataProvider = new ContactDataProviderEx(this.option.teamId, ItemTypes.TEAM_MEMBER);
        } else if (option.type == ContactSelectType.TEAM) {
            option.showContactSelectArea = false;
            dataProvider = new ContactDataProvider(ItemTypes.TEAM);
        } else {
            dataProvider = new ContactDataProvider(ItemTypes.FRIEND);
        }

        // contact adapter
        contactAdapter = new ContactSelectAdapter(ContactSelectActivity.this, new ContactsSelectGroupStrategy(),
                dataProvider) {
            boolean isEmptyContacts = false;

            @Override
            protected List<AbsContactItem> onNonDataItems() {
                return null;
            }

            @Override
            protected void onPostLoad(boolean empty, String queryText, boolean all) {
                if (empty) {
                    if (TextUtils.isEmpty(queryText)) {
                        isEmptyContacts = true;
                    }
                    updateEmptyView(queryText);
                } else {
                    setSearchViewVisible(true);
                }
            }

            private void updateEmptyView(String queryText) {
                if (!isEmptyContacts && !TextUtils.isEmpty(queryText)) {
                    setSearchViewVisible(true);
                } else {
                    setSearchViewVisible(false);
                }
            }

            private void setSearchViewVisible(boolean visible) {
                option.searchVisible = visible;
                if (searchView != null) {
                    searchView.setVisibility(option.searchVisible ? View.VISIBLE : View.GONE);
                }
            }
        };

        Class c = option.multi ? ContactsMultiSelectHolder.class : ContactsSelectHolder.class;
        contactAdapter.addViewHolder(ItemTypes.LABEL, LabelHolder.class);
        contactAdapter.addViewHolder(ItemTypes.FRIEND, c);
        contactAdapter.addViewHolder(ItemTypes.TEAM_MEMBER, c);
        contactAdapter.addViewHolder(ItemTypes.TEAM, c);

        contactAdapter.setFilter(option.itemFilter);
        contactAdapter.setDisableFilter(option.itemDisableFilter);

        // contact select adapter
        contactSelectedAdapter = new ContactSelectAvatarAdapter(this);
    }

    private void initListView() {
        listView = findView(R.id.contact_list_view);
        listView.setAdapter(contactAdapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                showKeyboard(false);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position = position - listView.getHeaderViewsCount();
                AbsContactItem item = (AbsContactItem) contactAdapter.getItem(position);

                if (item == null) {
                    return;
                }

                if (option.multi) {
                    if (!contactAdapter.isEnabled(position)) {
                        return;
                    }
                    IContact contact = null;
                    if (item instanceof ContactItem) {
                        contact = ((ContactItem) item).getContact();
                    }
                    if (contactAdapter.isSelected(position)) {
                        contactAdapter.cancelItem(position);
                        if (contact != null) {
                            contactSelectedAdapter.removeContact(contact);
                        }
                    } else {
                        if (contactSelectedAdapter.getCount() <= option.maxSelectNum) {
                            contactAdapter.selectItem(position);
                            if (contact != null) {
                                contactSelectedAdapter.addContact(contact);
                            }
                        } else {
                            Toast.makeText(ContactSelectActivity.this, option.maxSelectedTip, Toast.LENGTH_SHORT).show();
                        }

                        if (!TextUtils.isEmpty(queryText) && searchView != null) {
                            searchView.setQuery("", true);
                            searchView.setIconified(true);
                            showKeyboard(false);
                        }
                    }
                    arrangeSelected();
                } else {
                    if (item instanceof ContactItem) {
                        final IContact contact = ((ContactItem) item).getContact();
                        ArrayList<String> selectedIds = new ArrayList<>();
                        selectedIds.add(contact.getContactId());
                        onSelected(selectedIds);
                    }

                    arrangeSelected();
                }
            }
        });

        // 字母导航
        TextView letterHit = (TextView) findViewById(R.id.tv_hit_letter);
        LetterIndexView idxView = (LetterIndexView) findViewById(R.id.liv_index);
        idxView.setLetters(getResources().getStringArray(R.array.letter_list2));
        ImageView imgBackLetter = (ImageView) findViewById(R.id.img_hit_letter);
        if (option.type != ContactSelectType.TEAM) {
            livIndex = contactAdapter.createLivIndex(listView, idxView, letterHit, imgBackLetter);
            livIndex.show();
        } else {
            idxView.setVisibility(View.GONE);
        }
    }

    private void initContactSelectArea() {
        btnSelect = (Button) findViewById(R.id.btnSelect);
        if (!option.allowSelectEmpty) {
            btnSelect.setEnabled(false);
        } else {
            btnSelect.setEnabled(true);
        }
        btnSelect.setOnClickListener(this);
        bottomPanel = (RelativeLayout) findViewById(R.id.rlCtrl);
        scrollViewSelected = (HorizontalScrollView) findViewById(R.id.contact_select_area);
        if (option.multi) {
            bottomPanel.setVisibility(View.VISIBLE);
            if (option.showContactSelectArea) {
                scrollViewSelected.setVisibility(View.VISIBLE);
                btnSelect.setVisibility(View.VISIBLE);
            } else {
                scrollViewSelected.setVisibility(View.GONE);
                btnSelect.setVisibility(View.GONE);
            }
            btnSelect.setText(getOKBtnText(0));
        } else {
            bottomPanel.setVisibility(View.GONE);
        }

        // selected contact image banner
        imageSelectedGridView = (GridView) findViewById(R.id.contact_select_area_grid);
        imageSelectedGridView.setAdapter(contactSelectedAdapter);
        notifySelectAreaDataSetChanged();
        imageSelectedGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (contactSelectedAdapter.getItem(position) == null) {
                        return;
                    }

                    IContact iContact = contactSelectedAdapter.remove(position);
                    if (iContact != null) {
                        contactAdapter.cancelItem(iContact);
                    }
                    arrangeSelected();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // init already selected items
        List<String> selectedUids = option.alreadySelectedAccounts;
        if (selectedUids != null && !selectedUids.isEmpty()) {
            contactAdapter.setAlreadySelectedAccounts(selectedUids);
            List<ContactItem> selectedItems = contactAdapter.getSelectedItem();
            for (ContactItem item : selectedItems) {
                contactSelectedAdapter.addContact(item.getContact());
            }
            arrangeSelected();
        }
    }

    private void loadData() {
        contactAdapter.load(true);
    }

    private void arrangeSelected() {
        this.contactAdapter.notifyDataSetChanged();
        if (option.multi) {
            int count = contactSelectedAdapter.getCount();
            if (!option.allowSelectEmpty) {
                btnSelect.setEnabled(count > 1);
            } else {
                btnSelect.setEnabled(true);
            }
            btnSelect.setText(getOKBtnText(count));
            notifySelectAreaDataSetChanged();
        }
    }

    private void notifySelectAreaDataSetChanged() {
        int converViewWidth = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 46, this.getResources()
                .getDisplayMetrics()));
        ViewGroup.LayoutParams layoutParams = imageSelectedGridView.getLayoutParams();
        layoutParams.width = converViewWidth * contactSelectedAdapter.getCount();
        layoutParams.height = converViewWidth;
        imageSelectedGridView.setLayoutParams(layoutParams);
        imageSelectedGridView.setNumColumns(contactSelectedAdapter.getCount());

        try {
            final int x = layoutParams.width;
            final int y = layoutParams.height;
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    scrollViewSelected.scrollTo(x, y);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        contactSelectedAdapter.notifyDataSetChanged();
    }

    private String getOKBtnText(int count) {
        String caption = getString(R.string.ok);
        int showCount = (count < 1 ? 0 : (count - 1));
        StringBuilder sb = new StringBuilder(caption);
        sb.append(" (");
        sb.append(showCount);
        if (option.maxSelectNumVisible) {
            sb.append("/");
            sb.append(option.maxSelectNum);
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * ************************** select ************************
     */

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSelect) {
            List<IContact> contacts = contactSelectedAdapter
                    .getSelectedContacts();
            if (option.allowSelectEmpty || checkMinMaxSelection(contacts.size())) {
                ArrayList<String> selectedAccounts = new ArrayList<>();
                for (IContact c : contacts) {
                    selectedAccounts.add(c.getContactId());
                }
                onSelected(selectedAccounts);
            }

        }
    }

    private boolean checkMinMaxSelection(int selected) {
        if (option.minSelectNum > selected) {
            return showMaxMinSelectTip(true);
        } else if (option.maxSelectNum < selected) {
            return showMaxMinSelectTip(false);
        }
        return true;
    }

    private boolean showMaxMinSelectTip(boolean min) {
        if (min) {
            Toast.makeText(this, option.minSelectedTip, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, option.maxSelectedTip, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void onSelected(ArrayList<String> selects) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(RESULT_DATA, selects);
        setResult(Activity.RESULT_OK, intent);
        this.finish();
    }

    /**
     * ************************* search ******************************
     */

    @Override
    public boolean onQueryTextChange(String query) {
        queryText = query;
        if (TextUtils.isEmpty(query)) {
            this.contactAdapter.load(true);
        } else {
            this.contactAdapter.query(query);
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String arg0) {
        return false;
    }

    @Override
    public void finish() {
        showKeyboard(false);
        super.finish();
    }
}
