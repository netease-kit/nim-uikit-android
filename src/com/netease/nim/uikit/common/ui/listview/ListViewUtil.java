package com.netease.nim.uikit.common.ui.listview;

import java.io.Serializable;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.widget.ListView;

import com.alibaba.fastjson.JSONObject;

public class ListViewUtil {
	
	public static class ListViewPosition implements Serializable {
		private static final long serialVersionUID = 3185823885711940383L;
		
		private static final String KEY_POSITION = "position";
		private static final String KEY_TOP = "top";
		
	    public int position = 0;
	    public int top = 0;
	    
		public ListViewPosition(int position, int top) {
	        this.position = position;
	        this.top = top;
	    }
	    
	    public static String toJsonString(ListViewPosition listViewPosition) {
	    	try {
		    	JSONObject jsonObject = new JSONObject();
		    	jsonObject.put(KEY_POSITION, listViewPosition.position);
		    	jsonObject.put(KEY_TOP, listViewPosition.top);
		    	return jsonObject.toJSONString();	
			} catch (Exception e) {
				return "";
			}
	    }
	    
	    public static ListViewPosition fromJsonString(String jsonString) {
	    	try {
		    	JSONObject jsonObject = JSONObject.parseObject(jsonString);
		    	int position = jsonObject.getIntValue(KEY_POSITION);
		    	int top = jsonObject.getIntValue(KEY_TOP);
		    	return new ListViewPosition(position, top);	
			} catch (Exception e) {
				return new ListViewPosition(0, 0);	
			}
	    }
	}
	
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
	
    public static ListViewPosition getCurrentPositionFromListView(ListView listView) {
    	if (listView.getChildCount() > 0) {
            View view = listView.getChildAt(0);
            int top = (view != null ? view.getTop() : 0);
            return new ListViewPosition(listView.getFirstVisiblePosition(), top);			
		} else {
			return new ListViewPosition(0, 0);		
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
	    scrollToPostion(listView, listView.getAdapter().getCount() - 1, 0);
	}
	
	public static void scrollToBottom(ListView listView, ScrollToPositionListener listener) {
	    scrollToPostion(listView, listView.getAdapter().getCount() - 1, 0, listener);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void scrollToPostion(ListView messageListView, int position, int y) {		
		scrollToPostion(messageListView, position, y, null);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static void scrollToPostion(final ListView messageListView, final int position, final int y, final ScrollToPositionListener listener) {		
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
