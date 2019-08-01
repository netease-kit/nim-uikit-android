package com.netease.nim.uikit.common.media.imagepicker.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;

public class FolderPopUpWindow extends PopupWindow implements View.OnClickListener {

    private ListView listView;
    private OnItemClickListener onItemClickListener;

    public FolderPopUpWindow(Context context, BaseAdapter adapter) {
        super(context);
        final View view = View.inflate(context, R.layout.pop_folder, null);
        listView = view.findViewById(R.id.listView);
        listView.setAdapter(adapter);
        setContentView(view);
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);  //如果不设置，就是 AnchorView 的宽度
        setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());
        setAnimationStyle(0);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int maxHeight = ScreenUtil.dip2px(315);
                int realHeight = listView.getHeight();
                ViewGroup.LayoutParams listParams = listView.getLayoutParams();
                listParams.height = realHeight > maxHeight ? maxHeight : realHeight;
                listView.setLayoutParams(listParams);
                //enterAnimator();
            }
        });
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                dismiss();
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(adapterView, view, position, l);
                }
            }
        });
    }

    private void enterAnimator() {
        ObjectAnimator translationY = ObjectAnimator.ofFloat(listView, "translationY", -listView.getHeight(), 0);
        translationY.setDuration(400);
        translationY.start();
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        super.showAsDropDown(anchor, xoff, yoff, gravity);
        enterAnimator();
    }

    @Override
    public void dismiss() {
        exitAnimator();
    }

    private void exitAnimator() {
        ObjectAnimator translationY = ObjectAnimator.ofFloat(listView, "translationY", 0, -listView.getHeight());
        translationY.setInterpolator(new AccelerateDecelerateInterpolator());
        translationY.setDuration(400);
        translationY.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                listView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                FolderPopUpWindow.super.dismiss();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        translationY.start();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setSelection(int selection) {
        listView.setSelection(selection);
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

    public interface OnItemClickListener {
        void onItemClick(AdapterView<?> adapterView, View view, int position, long l);
    }
}
