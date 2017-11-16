package com.netease.nim.uikit.business.contact;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.contact.ContactChangedObserver;
import com.netease.nim.uikit.api.model.contact.ContactsCustomization;
import com.netease.nim.uikit.api.model.main.LoginSyncDataStatusObserver;
import com.netease.nim.uikit.api.model.main.OnlineStateChangeObserver;
import com.netease.nim.uikit.api.model.user.UserInfoObserver;
import com.netease.nim.uikit.business.contact.core.item.AbsContactItem;
import com.netease.nim.uikit.business.contact.core.item.ContactItem;
import com.netease.nim.uikit.business.contact.core.item.ItemTypes;
import com.netease.nim.uikit.business.contact.core.model.ContactDataAdapter;
import com.netease.nim.uikit.business.contact.core.model.ContactGroupStrategy;
import com.netease.nim.uikit.business.contact.core.provider.ContactDataProvider;
import com.netease.nim.uikit.business.contact.core.query.IContactDataProvider;
import com.netease.nim.uikit.business.contact.core.viewholder.LabelHolder;
import com.netease.nim.uikit.business.contact.core.viewholder.OnlineStateContactHolder;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.common.ui.liv.LetterIndexView;
import com.netease.nim.uikit.common.ui.liv.LivIndex;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nim.uikit.impl.cache.UIKitLogTag;
import com.netease.nimlib.sdk.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;


/**
 * 通讯录Fragment
 * <p/>
 * Created by huangjun on 2015/9/7.
 */
public class ContactsFragment extends TFragment {

    private ContactDataAdapter adapter;

    private ListView listView;

    private TextView countText;

    private LivIndex litterIdx;

    private View loadingFrame;

    private ContactsCustomization customization;

    private ReloadFrequencyControl reloadControl = new ReloadFrequencyControl();

    public void setContactsCustomization(ContactsCustomization customization) {
        this.customization = customization;
    }

    private static final class ContactsGroupStrategy extends ContactGroupStrategy {
        public ContactsGroupStrategy() {
            add(ContactGroupStrategy.GROUP_NULL, -1, "");
            addABC(0);
        }
    }

    /**
     * ***************************************** 生命周期 *****************************************
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.nim_contacts, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 界面初始化
        initAdapter();
        findViews();
        buildLitterIdx(getView());

        // 注册观察者
        registerObserver(true);
        registerOnlineStateChangeListener(true);
        // 加载本地数据
        reload(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        registerObserver(false);
        registerOnlineStateChangeListener(false);
    }

    private void initAdapter() {
        IContactDataProvider dataProvider = new ContactDataProvider(ItemTypes.FRIEND);

        adapter = new ContactDataAdapter(getActivity(), new ContactsGroupStrategy(), dataProvider) {
            @Override
            protected List<AbsContactItem> onNonDataItems() {
                if (customization != null) {
                    return customization.onGetFuncItems();
                }

                return new ArrayList<>();
            }

            @Override
            protected void onPreReady() {
                loadingFrame.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostLoad(boolean empty, String queryText, boolean all) {
                loadingFrame.setVisibility(View.GONE);
                int userCount = NimUIKit.getContactProvider().getMyFriendsCount();
                countText.setText("共有好友" + userCount + "名");

                onReloadCompleted();
            }
        };

        adapter.addViewHolder(ItemTypes.LABEL, LabelHolder.class);
        if (customization != null) {
            adapter.addViewHolder(ItemTypes.FUNC, customization.onGetFuncViewHolderClass());
        }
        adapter.addViewHolder(ItemTypes.FRIEND, OnlineStateContactHolder.class);
    }

    private void findViews() {
        // loading
        loadingFrame = findView(R.id.contact_loading_frame);

        // count
        View countLayout = View.inflate(getView().getContext(), R.layout.nim_contacts_count_item, null);
        countLayout.setClickable(false);
        countText = (TextView) countLayout.findViewById(R.id.contactCountText);

        // ListView
        listView = findView(R.id.contact_list_view);
        listView.addFooterView(countLayout); // 注意：addFooter要放在setAdapter之前，否则旧版本手机可能会add不上
        listView.setAdapter(adapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        ContactItemClickListener listener = new ContactItemClickListener();
        listView.setOnItemClickListener(listener);
        listView.setOnItemLongClickListener(listener);

        // ios style
        OverScrollDecoratorHelper.setUpOverScroll(listView);
    }

    private void buildLitterIdx(View view) {
        LetterIndexView livIndex = (LetterIndexView) view.findViewById(R.id.liv_index);
        livIndex.setNormalColor(getResources().getColor(R.color.contacts_letters_color));
        ImageView imgBackLetter = (ImageView) view.findViewById(R.id.img_hit_letter);
        TextView litterHit = (TextView) view.findViewById(R.id.tv_hit_letter);
        litterIdx = adapter.createLivIndex(listView, livIndex, litterHit, imgBackLetter);

        litterIdx.show();
    }

    private final class ContactItemClickListener implements OnItemClickListener, OnItemLongClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            AbsContactItem item = (AbsContactItem) adapter.getItem(position);
            if (item == null) {
                return;
            }

            int type = item.getItemType();

            if (type == ItemTypes.FUNC && customization != null) {
                customization.onFuncItemClick(item);
                return;
            }

            if (type == ItemTypes.FRIEND && item instanceof ContactItem && NimUIKitImpl.getContactEventListener() != null) {
                NimUIKitImpl.getContactEventListener().onItemClick(getActivity(), (((ContactItem) item).getContact()).getContactId());
            }
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       int position, long id) {
            AbsContactItem item = (AbsContactItem) adapter.getItem(position);
            if (item == null) {
                return false;
            }

            if (item instanceof ContactItem && NimUIKitImpl.getContactEventListener() != null) {
                NimUIKitImpl.getContactEventListener().onItemLongClick(getActivity(), (((ContactItem) item).getContact()).getContactId());
            }

            return true;
        }
    }

    public void scrollToTop() {
        if (listView != null) {
            int top = listView.getFirstVisiblePosition();
            int bottom = listView.getLastVisiblePosition();
            if (top >= (bottom - top)) {
                listView.setSelection(bottom - top);
                listView.smoothScrollToPosition(0);
            } else {
                listView.smoothScrollToPosition(0);
            }
        }
    }

    /**
     * *********************************** 通讯录加载控制 *******************************
     */

    /**
     * 加载通讯录数据并刷新
     *
     * @param reload true则重新加载数据；false则判断当前数据源是否空，若空则重新加载，不空则不加载
     */
    private void reload(boolean reload) {
        if (!reloadControl.canDoReload(reload)) {
            return;
        }

        if (adapter == null) {
            if (getActivity() == null) {
                return;
            }

            initAdapter();
        }

        // 开始加载
        if (!adapter.load(reload)) {
            // 如果不需要加载，则直接当完成处理
            onReloadCompleted();
        }
    }

    private void onReloadCompleted() {
        if (reloadControl.continueDoReloadWhenCompleted()) {
            // 计划下次加载，稍有延迟
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    boolean reloadParam = reloadControl.getReloadParam();
                    Log.i(UIKitLogTag.CONTACT, "continue reload " + reloadParam);
                    reloadControl.resetStatus();
                    reload(reloadParam);
                }
            }, 50);
        } else {
            // 本次加载完成
            reloadControl.resetStatus();
        }

        LogUtil.i(UIKitLogTag.CONTACT, "contact load completed");
    }

    /**
     * 通讯录加载频率控制
     */
    class ReloadFrequencyControl {
        boolean isReloading = false;
        boolean needReload = false;
        boolean reloadParam = false;

        boolean canDoReload(boolean param) {
            if (isReloading) {
                // 正在加载，那么计划加载完后重载
                needReload = true;
                if (param) {
                    // 如果加载过程中又有多次reload请求，多次参数只要有true，那么下次加载就是reload(true);
                    reloadParam = true;
                }
                LogUtil.i(UIKitLogTag.CONTACT, "pending reload task");

                return false;
            } else {
                // 如果当前空闲，那么立即开始加载
                isReloading = true;
                return true;
            }
        }

        boolean continueDoReloadWhenCompleted() {
            return needReload;
        }

        void resetStatus() {
            isReloading = false;
            needReload = false;
            reloadParam = false;
        }

        boolean getReloadParam() {
            return reloadParam;
        }
    }

    /**
     * *********************************** 用户资料、好友关系变更、登录数据同步完成观察者 *******************************
     */

    private void registerObserver(boolean register) {
        NimUIKit.getUserInfoObservable().registerObserver(userInfoObserver, register);
        NimUIKit.getContactChangedObservable().registerObserver(friendDataChangedObserver, register);
        LoginSyncDataStatusObserver.getInstance().observeSyncDataCompletedEvent(loginSyncCompletedObserver);
    }

    ContactChangedObserver friendDataChangedObserver = new ContactChangedObserver() {
        @Override
        public void onAddedOrUpdatedFriends(List<String> accounts) {
            reloadWhenDataChanged(accounts, "onAddedOrUpdatedFriends", true);
        }

        @Override
        public void onDeletedFriends(List<String> accounts) {
            reloadWhenDataChanged(accounts, "onDeletedFriends", true);
        }

        @Override
        public void onAddUserToBlackList(List<String> accounts) {
            reloadWhenDataChanged(accounts, "onAddUserToBlackList", true);
        }

        @Override
        public void onRemoveUserFromBlackList(List<String> accounts) {
            reloadWhenDataChanged(accounts, "onRemoveUserFromBlackList", true);
        }
    };

    private UserInfoObserver userInfoObserver = new UserInfoObserver() {
        @Override
        public void onUserInfoChanged(List<String> accounts) {
            reloadWhenDataChanged(accounts, "onUserInfoChanged", true, false); // 非好友资料变更，不用刷新界面
        }
    };

    private Observer<Void> loginSyncCompletedObserver = new Observer<Void>() {
        @Override
        public void onEvent(Void aVoid) {
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    reloadWhenDataChanged(null, "onLoginSyncCompleted", false);
                }
            }, 50);
        }
    };

    private void reloadWhenDataChanged(List<String> accounts, String reason, boolean reload) {
        reloadWhenDataChanged(accounts, reason, reload, true);
    }

    private void reloadWhenDataChanged(List<String> accounts, String reason, boolean reload, boolean force) {
        if (accounts == null || accounts.isEmpty()) {
            return;
        }

        boolean needReload = false;
        if (!force) {
            // 非force：与通讯录无关的（非好友）变更通知，去掉
            for (String account : accounts) {
                if (NimUIKit.getContactProvider().isMyFriend(account)) {
                    needReload = true;
                    break;
                }
            }
        } else {
            needReload = true;
        }

        if (!needReload) {
            Log.d(UIKitLogTag.CONTACT, "no need to reload contact");
            return;
        }

        // log
        StringBuilder sb = new StringBuilder();
        sb.append("ContactFragment received data changed as [" + reason + "] : ");
        if (accounts != null && !accounts.isEmpty()) {
            for (String account : accounts) {
                sb.append(account);
                sb.append(" ");
            }
            sb.append(", changed size=" + accounts.size());
        }
        Log.i(UIKitLogTag.CONTACT, sb.toString());

        // reload
        reload(reload);
    }

    /**
     * *********************************** 在线状态 *******************************
     */

    OnlineStateChangeObserver onlineStateChangeObserver = new OnlineStateChangeObserver() {
        @Override
        public void onlineStateChange(Set<String> accounts) {
            // 更新
            adapter.notifyDataSetChanged();
        }
    };

    private void registerOnlineStateChangeListener(boolean register) {
        if (!NimUIKitImpl.enableOnlineState()) {
            return;
        }
        NimUIKitImpl.getOnlineStateChangeObservable().registerOnlineStateChangeListeners(onlineStateChangeObserver, register);
    }
}
