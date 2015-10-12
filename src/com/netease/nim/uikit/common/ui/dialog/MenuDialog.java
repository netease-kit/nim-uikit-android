package com.netease.nim.uikit.common.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nim.uikit.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangjun on 2015/3/21.
 */
public class MenuDialog extends Dialog implements View.OnClickListener {
    public interface MenuDialogOnButtonClickListener {
        public void onButtonClick(final String name);
    }

    private Context context;
    private ViewGroup rootView;
    private LinearLayout itemsRootView;

    private List<String> btnNames;
    private List<View> itemViews;
    private MenuDialogOnButtonClickListener clickListener;
    private boolean selectMode = false;
    private int selectIndex = -1; // 要勾选的项
    private int invalidSelectIndex = -1; // 不能勾选的项目
    private int preSelectIndex = -1; // 之前勾选的项目

    public MenuDialog(Context context, List<String> btnNames, MenuDialogOnButtonClickListener listener) {
        super(context, R.style.dialog_default_style);
        this.context = context;
        this.btnNames = btnNames;
        this.clickListener = listener;
    }

    public MenuDialog(Context context, List<String> btnNames, int selectIndex, int invalidSelectIndex,
                      MenuDialogOnButtonClickListener listener) {
        this(context, btnNames, listener);

        if (selectIndex >= 0 && selectIndex < btnNames.size()) {
            this.selectMode = true;
            this.selectIndex = selectIndex;
            this.preSelectIndex = selectIndex;
            this.invalidSelectIndex = invalidSelectIndex;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        rootView = (ViewGroup) View.inflate(context, R.layout.nim_menu_dialog, null);
        itemsRootView = (LinearLayout) rootView.findViewById(R.id.menu_dialog_items_root);
        if (selectMode) {
            itemViews = new ArrayList<>();
        }

        View itemView;
        for (int i = 0; i < btnNames.size(); i++) {
            itemView = View.inflate(context, R.layout.nim_menu_dialog_item, null);
            ((TextView) itemView.findViewById(R.id.menu_button)).setText(btnNames.get(i));
            itemView.setTag(i);
            itemView.setOnClickListener(this);
            if (selectMode) {
                itemViews.add(itemView);
            }

            itemsRootView.addView(itemView);
        }

        selectItem();

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setContentView(rootView);
    }

    // 撤销最后一次选择，恢复上一次选择
    public void undoLastSelect() {
        if (selectMode && preSelectIndex >= 0 && preSelectIndex < btnNames.size()) {
            selectIndex = preSelectIndex;
            selectItem();
        }
    }

    private void selectItem() {
        if (selectMode == false || selectIndex < 0 || selectIndex >= btnNames.size() || itemViews == null || itemViews
                .isEmpty()) {
            return;
        }

        View item;
        for (int i = 0; i < itemViews.size(); i++) {
            item = itemViews.get(i);
            item.findViewById(R.id.menu_select_icon).setVisibility(selectIndex == i ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int index = (int) v.getTag();
        if (selectMode && index != invalidSelectIndex) {
            preSelectIndex = selectIndex;
            selectIndex = index;
            selectItem();
        }

        String btnName = btnNames.get(index);
        if (clickListener != null) {
            clickListener.onButtonClick(btnName);
        }
    }
}
