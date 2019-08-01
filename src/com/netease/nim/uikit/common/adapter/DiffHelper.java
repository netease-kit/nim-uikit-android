package com.netease.nim.uikit.common.adapter;

import android.support.v7.util.DiffUtil;

import java.util.List;

/**
 */

public class DiffHelper {

    public static class SimpleDiff<T> extends BaseDiff<T>{

        public SimpleDiff(List<T> mOld, List<T> mNew) {
            super(mOld, mNew);
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            T a = getOldListItem(oldItemPosition);
            T b = getNewListItem(newItemPosition);
            return (a == b) || (a != null && a.equals(b));
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return true;
        }
    }

    public abstract static class BaseDiff<T> extends DiffUtil.Callback{

        private final List<T> mOld;
        private final List<T> mNew;

        public BaseDiff(List<T> mOld, List<T> mNew) {
            this.mOld = mOld;
            this.mNew = mNew;
        }

        @Override
        public int getOldListSize() {
            return mOld == null ? 0 : mOld.size();
        }

        @Override
        public int getNewListSize() {
            return mNew == null ? 0 : mNew.size();
        }

        protected T getOldListItem(int pos){
            try {
                return mOld.get(pos);
            }catch (Exception e){
                return null;
            }
        }

        protected T getNewListItem(int pos){
            try {
                return mNew.get(pos);
            }catch (Exception e){
                return null;
            }
        }
    }
}
