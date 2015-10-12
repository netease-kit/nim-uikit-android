package com.netease.nim.uikit.common.ui.popupmenu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.uikit.R;

import java.util.List;

public class PopupMenuAdapter extends BaseAdapter {

	public static int TYPE_BG_WHITE = 0;
	public static int TYPE_BG_BLACK = 1;

	private int typeBg = 0;
	
	private Context context;

	private List<PopupMenuItem> list;

	private LayoutInflater inflater;

	public PopupMenuAdapter(Context context, List<PopupMenuItem> list,int typeBg) {
		this.list = list;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.typeBg = typeBg;
	}
	
	

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ImageView icon = null;
		TextView title = null;
		if (convertView == null) {
			if(typeBg == TYPE_BG_BLACK){
				convertView = inflater.inflate(R.layout.nim_popup_menu_list_black_item, null);
			}else {
				convertView = inflater.inflate(R.layout.nim_popup_menu_list_item, null);

			}
			icon = (ImageView) convertView.findViewById(R.id.popup_menu_icon);
			title = (TextView) convertView.findViewById(R.id.popup_menu_title);
			ViewHolder cache = new ViewHolder();
			cache.icon = icon;
			cache.title = title;
			convertView.setTag(cache);
		} else {
			ViewHolder cache = (ViewHolder) convertView.getTag();
			icon = cache.icon;
			title = cache.title;
		}
		PopupMenuItem item = list.get(position);
		if (item.getIcon() != 0) {
			icon.setVisibility(View.VISIBLE);
			icon.setImageResource(item.getIcon());
		} else {
			icon.setVisibility(View.GONE);
		}
		title.setText(item.getTitle());

		// 下面代码实现数据绑定
		return convertView;
	}

	private final class ViewHolder {

		public ImageView icon;

		public TextView title;
	}
}
