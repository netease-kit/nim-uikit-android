package com.netease.nim.uikit.common.framework.infra;

import android.os.Handler;

public abstract class Task {
    private static final String ENCLOSURE = "<>";
    private static final int RETRY_COUNT = 1;

    /*package*/ static class Info {
        /**
         * background
         */
        boolean background;

        /**
         * key
         */
        String key;

        /**
         * parameters
         */
        Object[] params;

        Info(boolean background, String key, Object[] params) {
            this.background = background;
            this.key = key;
            this.params = params;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("B");
            sb.append(ENCLOSURE.charAt(0));
            sb.append(background ? "T" : "F");
            sb.append(ENCLOSURE.charAt(1));

            sb.append(" ");

            sb.append("K");
            sb.append(ENCLOSURE.charAt(0));
            sb.append(key);
            sb.append(ENCLOSURE.charAt(1));

            return sb.toString();
        }
    }

    /*package*/ static class State {
        /**
         * cancelled
         */
        boolean cancelled;

        /**
         * chances
         */
        int chances;

        /**
         * pending
         */
        boolean pending;

        /**
         * fault
         */
        boolean fault;

        State() {

        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("C");
            sb.append(ENCLOSURE.charAt(0));
            sb.append(chances);
            sb.append(ENCLOSURE.charAt(1));

            sb.append(" ");

            sb.append("P");
            sb.append(ENCLOSURE.charAt(0));
            sb.append(pending ? "T" : "F");
            sb.append(ENCLOSURE.charAt(1));

            return sb.toString();
        }
    }

    /**
     * handler for publish states
     */
    /*package*/ Handler handler;

    /**
     * task info
     */
    /*package*/ Info info;

    /**
     * task state
     */
	/*package*/ State state;

    public void cancel() {
        state.cancelled = true;
    }

    public void setProperty(int prop, Object data) {
        // NOP
    }

    protected boolean background() {
        return info.background;
    }

    protected String key() {
        return info.key;
    }

    protected Object[] params() {
        return info.params;
    }

    protected boolean cancelled() {
        return state.cancelled;
    }

    protected int scheduled() {
        return state.chances;
    }

    protected void pending() {
        state.pending = true;
    }

    protected boolean giveup() {
        // give up
        boolean giveup = scheduled() > RETRY_COUNT;

        // mark pending if proceed
        if (!giveup) {
            pending();
        }

        return giveup;
    }

    /*package*/ boolean schedule() {
        // update chances
        state.chances++;
        // reset pending
        state.pending = false;

        // execute
        Object[] results = null;
        try {
            results = execute(info.params);
        } catch (Throwable tr) {
            onException(tr);

            // fault
            state.fault = true;
        }

        // no fault and pending
        if (!state.fault && state.pending) {
            // not proceed
            return false;
        }

        // publish result
        publishResult(results);

        return true;
    }

    /**
     * execute
     *
     * @param params
     * @return results
     */
    protected abstract Object[] execute(Object[] params);

    /**
     * on exception
     *
     * @param tr throwable
     */
    protected void onException(Throwable tr) {
        // save
//		AppCrashHandler.getInstance(null).saveException(tr, false);
    }

    ;

    /**
     * on handle result
     *
     * @param results
     */
    protected void onHandleResult(Object[] results) {
    }

    ;

    /**
     * on publish result
     *
     * @param results
     */
    protected void onPublishResult(Object[] results) {
    }

    /**
     * on publish progress
     *
     * @param params
     */
    protected void onPublishProgress(Object[] params) {
    }

    /**
     * publish result
     *
     * @param results
     */
    protected final void publishResult(Object[] results) {
        publish(true, results);
    }

    /**
     * publish progress
     *
     * @param params
     */
    protected final void publishProgress(Object[] params) {
        publish(false, params);
    }

    private final void publish(final boolean result, final Object[] params) {
        if (info.background && handler != null) {
            // shift to looper thread of handler to publish
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // publish on current thread
                    onPublish(result, params);
                }
            });
        } else {
            // publish on current thread
            onPublish(result, params);
        }
    }

    private final void onPublish(boolean result, Object[] params) {
        if (result) {
            onHandleResult(params);
            onPublishResult(params);
        } else {
            onPublishProgress(params);
        }
    }

    public final String dump(boolean statefull) {
        StringBuilder sb = new StringBuilder();

        // info
        sb.append(info);
        // state
        if (statefull) {
            sb.append(" ");
            sb.append(state);
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return dump(true);
    }
}