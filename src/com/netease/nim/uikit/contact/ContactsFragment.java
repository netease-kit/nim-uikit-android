package com.netease.nim.uikit.contact;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.common.ui.liv.LetterIndexView;
import com.netease.nim.uikit.common.ui.liv.LivIndex;
import com.netease.nim.uikit.common.ui.ptr.PullToRefreshBase;
import com.netease.nim.uikit.common.ui.ptr.PullToRefreshListView;
import com.netease.nim.uikit.contact.core.item.AbsContactItem;
import com.netease.nim.uikit.contact.core.item.ContactItem;
import com.netease.nim.uikit.contact.core.item.ItemTypes;
import com.netease.nim.uikit.contact.core.model.ContactDataAdapter;
import com.netease.nim.uikit.contact.core.model.ContactGroupStrategy;
import com.netease.nim.uikit.contact.core.provider.ContactDataProvider;
import com.netease.nim.uikit.contact.core.query.IContactDataProvider;
import com.netease.nim.uikit.contact.core.viewholder.ContactHolder;
import com.netease.nim.uikit.contact.core.viewholder.LabelHolder;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.util.ArrayList;
import java.util.List;


/**
 * 通讯录Fragment
 * <p/>
 * Created by huangjun on 2015/9/7.
 */
public class ContactsFragment extends TFragment {

    private static final String TAG = ContactsFragment.class.getSimpleName();

    private ContactDataAdapter adapter;

    private PullToRefreshListView listView;

    private TextView countView;

    private LivIndex litterIdx;

    private View loadingFrame;

    private ContactsCustomization customization;

    private static final class ContactsGroupStrategy extends ContactGroupStrategy {
        public ContactsGroupStrategy() {
            add(ContactGroupStrategy.GROUP_NULL, -1, "");
            addABC(0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.nim_contacts, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 界面初始化
        findViews();
        initPullToRefreshListView();
        buildLitterIdx(getView());

        // 加载本地数据
        reloadChange(true);
    }

    private void findViews() {
        View ctCountView = View.inflate(getView().getContext(), R.layout.nim_contacts_count_item, null);
        countView = (TextView) ctCountView.findViewById(R.id.contactCountText);
        loadingFrame = findView(R.id.contact_loading_frame);
        initAdapter();
        listView = findView(R.id.contact_list_view);
        ctCountView.setClickable(false);
        listView.getRefreshableView().addFooterView(ctCountView);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));

        ContactItemClickListener listener = new ContactItemClickListener();
        listView.setOnItemClickListener(listener);
        listView.setOnItemLongClickListener(listener);
    }

    private void initPullToRefreshListView() {
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                requestUserData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });
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
                countView.setText("共有好友" + userCount + "名");
            }
        };

        adapter.addViewHolder(ItemTypes.LABEL, LabelHolder.class);
        if (customization != null) {
            adapter.addViewHolder(ItemTypes.FUNC, customization.onGetFuncViewHolderClass());
        }
        adapter.addViewHolder(ItemTypes.FRIEND, ContactHolder.class);
    }

    private void buildLitterIdx(View view) {
        LetterIndexView livIndex = (LetterIndexView) view.findViewById(R.id.liv_index);
        livIndex.setNormalColor(getResources().getColor(R.color.contacts_letters_color));
        ImageView imgBackLetter = (ImageView) view.findViewById(R.id.img_hit_letter);
        TextView litterHit = (TextView) view.findViewById(R.id.tv_hit_letter);
        litterIdx = adapter.createLivIndex(listView, livIndex, litterHit, imgBackLetter);

        litterIdx.show();
    }

    public void requestUserData() {
        NimUIKit.getContactProvider().getUserInfoOfMyFriends(new RequestCallback<List<UserInfoProvider.UserInfo>>() {
            @Override
            public void onSuccess(List<UserInfoProvider.UserInfo> users) {
                if (!users.isEmpty()) {
                    refresh();
                }

                listView.onRefreshComplete();
            }

            @Override
            public void onFailed(int code) {
                if (code == 400 || code == 401) {
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), "access_token无效,请重试。code=" + code, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), "request failed, code=" + code, Toast.LENGTH_SHORT).show();
                    }
                }

                listView.onRefreshComplete();
            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }

    public void refresh() {
        reloadChange(true);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 加载本地数据（已从服务器下载到本地），切换到当前tab时触发
     */
    public void reloadChange(boolean rebuild) {
        if (adapter == null) {
            if (getActivity() == null) {
                return;
            }

            initAdapter();
        }

        adapter.load(rebuild);
    }

    public void scrollToTop() {
        if (listView != null) {
            int top = listView.getRefreshableView().getFirstVisiblePosition();
            int bottom = listView.getRefreshableView().getLastVisiblePosition();
            if (top >= (bottom - top)) {
                listView.getRefreshableView().setSelection(bottom - top);
                listView.getRefreshableView().smoothScrollToPosition(0);
            } else {
                listView.getRefreshableView().smoothScrollToPosition(0);
            }
        }
    }

    private final class ContactItemClickListener implements OnItemClickListener, OnItemLongClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            AbsContactItem item = (AbsContactItem) adapter.getItem(position - 1);
            if (item == null) {
                return;
            }

            int type = item.getItemType();

            if (type == ItemTypes.FUNC && customization != null) {
                customization.onFuncItemClick(item);
                return;
            }

            if (type == ItemTypes.FRIEND && NimUIKit.getContactEventListener() != null) {
                NimUIKit.getContactEventListener().onItemClick(getActivity(), (((ContactItem) item).getContact()).getContactId());
            }
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       int position, long id) {
            AbsContactItem item = (AbsContactItem) adapter.getItem(position - 1);
            if (item == null) {
                return false;
            }

            if (NimUIKit.getContactEventListener() != null) {
                NimUIKit.getContactEventListener().onItemLongClick(getActivity(), (((ContactItem) item).getContact()).getContactId());
            }

            return true;
        }
    }

    public void setContactsCustomization(ContactsCustomization customization) {
        this.customization = customization;
    }
}
