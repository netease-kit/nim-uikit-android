package com.netease.nim.uikit.common.framework.infra;

import java.util.ArrayList;
import java.util.List;

public abstract class Observable<T> {
    protected final ArrayList<T> mObservers = new ArrayList<T>();

    public void registerObserver(T observer) {
        if (observer == null) {
            return;
        }
        synchronized (mObservers) {
            if (mObservers.contains(observer)) {
                return;
            }
            mObservers.add(observer);
        }
    }

    public void unregisterObserver(T observer) {
        if (observer == null) {
            return;
        }
        synchronized (mObservers) {
            int index = mObservers.indexOf(observer);
            if (index == -1) {
                return;
            }
            mObservers.remove(index);
        }
    }

    public void unregisterAll() {
        synchronized (mObservers) {
            mObservers.clear();
        }
    }

    protected List<T> getObservers() {
        return getObservers(true);
    }

    protected List<T> getObservers(boolean sync) {
        if (sync) {
            synchronized (mObservers) {
                return new ArrayList<T>(mObservers);
            }
        } else {
            return mObservers;
        }
    }
}
