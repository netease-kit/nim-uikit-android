package com.netease.nim.uikit.business.session.module.input;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.session.actions.BaseAction;

import java.util.List;

public class ActionsGridviewAdapter extends BaseAdapter {

    private Context context;

    private List<BaseAction> baseActions;

    public ActionsGridviewAdapter(Context context, List<BaseAction> baseActions) {
        this.context = context;
        this.baseActions = baseActions;
    }

    @Override
    public int getCount() {
        return baseActions.size();
    }

    @Override
    public Object getItem(int position) {
        return baseActions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemlayout;
        if (convertView == null) {
            itemlayout = LayoutInflater.from(context).inflate(R.layout.nim_actions_item_layout, null);
        } else {
            itemlayout = convertView;
        }

        BaseAction viewHolder = baseActions.get(position);
        ((ImageView) itemlayout.findViewById(R.id.imageView)).setBackgroundResource(viewHolder.getIconResId());
        ((TextView) itemlayout.findViewById(R.id.textView)).setText(context.getString(viewHolder.getTitleId()));
        return itemlayout;
    }
}

