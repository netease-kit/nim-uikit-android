package com.netease.nim.uikit.common.ui.listview;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.widget.ListView;

public class ListViewUtil {

	public static boolean isLastMessageVisible(ListView messageListView) {
		if(messageListView == null || messageListView.getAdapter() == null) {
			return false;
		}
		
	    if (messageListView.getLastVisiblePosition() >= messageListView.getAdapter().getCount() - 1 - messageListView.getFooterViewsCount()) {
	        return true;
	    } else {
	        return false;
	    }
	}

    //index是items的index，不包含header
    public static Object getViewHolderByIndex(ListView listView, int index) {
		int firstVisibleFeedPosition = listView.getFirstVisiblePosition() - listView.getHeaderViewsCount();
		int lastVisibleFeedPosition = listView.getLastVisiblePosition() - listView.getHeaderViewsCount();

		//只有获取可见区域的
		if (index >= firstVisibleFeedPosition && index <= lastVisibleFeedPosition) {
			View view = listView.getChildAt(index - firstVisibleFeedPosition);
			Object tag = view.getTag();
			return tag;
		} else {
			return null;
		}
	}
    
	public interface ScrollToPositionListener {
		void onScrollEnd();
	}
	
	public static void scrollToBottom(ListView listView) {
	    scrollToPosition(listView, listView.getAdapter().getCount() - 1, 0);
	}
	
	public static void scrollToBottom(ListView listView, ScrollToPositionListener listener) {
	    scrollToPosition(listView, listView.getAdapter().getCount() - 1, 0, listener);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void scrollToPosition(ListView messageListView, int position, int y) {
		scrollToPosition(messageListView, position, y, null);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static void scrollToPosition(final ListView messageListView, final int position, final int y, final ScrollToPositionListener listener) {
		messageListView.post(new Runnable() {
			
			@Override
			public void run() {				
				messageListView.setSelectionFromTop(position, y);
				
				if (listener != null) {
					listener.onScrollEnd();
				}
			}
		});			
	}
}
