package com.netease.nim.uikit.common.ui.popupmenu;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;

import java.util.List;

public class NIMPopupMenu {

    public static int TYPE_BG_WHITE = 0;
    public static int TYPE_BG_BLACK = 1;

    private int typeBg = 0;

    private Context context;

    private List<PopupMenuItem> items;

    private PopupMenuAdapter adapter;

    private MenuItemClickListener listener;

    public PopupWindow popWindow;

    private View rootView;

    private boolean scroll = false;

    private void init() {
        initListView();
        initPopupWindow();
    }

    public NIMPopupMenu(Context context, List<PopupMenuItem> items, MenuItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
        init();
    }

    /**
     * @param context
     * @param items
     * @param listener
     * @param typeBg
     */
    public NIMPopupMenu(Context context, List<PopupMenuItem> items, MenuItemClickListener listener, int typeBg) {
        this.context = context;
        this.items = items;
        this.listener = listener;
        this.typeBg = typeBg;
        init();

    }

    public NIMPopupMenu(Context context, List<PopupMenuItem> items, MenuItemClickListener listener, int typeBg, boolean scroll) {
        this.context = context;
        this.items = items;
        this.listener = listener;
        this.typeBg = typeBg;
        this.scroll = scroll;
        init();
    }

    /**
     * 初始化列表界面
     */
    private void initListView() {
        if (rootView == null) {
            if (typeBg == TYPE_BG_BLACK) {
                rootView = LayoutInflater.from(context).inflate(R.layout.nim_popup_menu_black_layout, null);
            } else {
                rootView = LayoutInflater.from(context).inflate(R.layout.nim_popup_menu_layout, null);
            }
            ListView listView = (ListView) rootView.findViewById(R.id.popmenu_listview);
            listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (listener != null) {
                        popWindow.dismiss();
                        listener.onItemClick(items.get(position));
                    }
                }
            });
            adapter = new PopupMenuAdapter(context, items, typeBg);
            listView.setAdapter(adapter);
        }
        rootView.setFocusableInTouchMode(true);
        rootView.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MENU && popWindow.isShowing()
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    popWindow.dismiss();
                    return true;
                }
                return false;
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void initPopupWindow() {
        if (popWindow == null) {
            //popWindow = new PopupWindow(rootView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            popWindow = new PopupWindow(context);
            popWindow.setContentView(rootView);
            popWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
            if (scroll) {
                popWindow.setHeight(ScreenUtil.getDisplayHeight() * 2 / 3);
            } else {
                popWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
            }
            popWindow.setTouchable(true);
            popWindow.setBackgroundDrawable(new BitmapDrawable());
            // popWindow.setContentView(rootView);
            //setPopupWindowSize();
            // popWindow.setHeight(rootView.getMeasuredHeight());

            popWindow.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss() {

                }
            });
        }
    }

    private void setPopupWindowSize() {
        // popWindow.setContentView(rootView);
        rootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        rootView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        popWindow.setWidth(rootView.getMeasuredWidth() + ScreenUtil.dip2px(15));
        // popWindow.setHeight(rootView.getMeasuredHeight());
        popWindow.update();
    }

    public void notifyData() {
        adapter.notifyDataSetChanged();
    }

    public void show(View v) {
        if (popWindow == null) {
            return;
        }
        if (popWindow.isShowing()) {
            popWindow.dismiss();
        } else {
            //setPopupWindowSize();
            if (scroll) {// 当可以滚动时，横竖屏切换时，重新确定高度
                Configuration configuration = context.getResources().getConfiguration();
                int ori = configuration.orientation;
                if (ori == Configuration.ORIENTATION_LANDSCAPE) {
                    popWindow.setHeight(ScreenUtil.getDialogWidth() * 2 / 3);
                } else {
                    popWindow.setHeight(ScreenUtil.getDisplayHeight() * 2 / 3);
                }
            }
            popWindow.setFocusable(true);
            popWindow.showAsDropDown(v, -10, 0);
        }
    }

    public boolean isShowing() {
        return popWindow != null && popWindow.isShowing();
    }

    public void dissmiss() {
        if (isShowing())
            popWindow.dismiss();
    }

    public abstract interface MenuItemClickListener {

        public void onItemClick(PopupMenuItem item);
    }
}
