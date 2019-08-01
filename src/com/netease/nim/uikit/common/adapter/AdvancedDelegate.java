package com.netease.nim.uikit.common.adapter;

/**

 */

public abstract class AdvancedDelegate extends BaseDelegate<BaseViewHolderData> {

    private static final int sTypeBase = 569100;
    public static final int sTypeEmpty = sTypeBase + 1;
    public static final int sTypeLoading = sTypeBase + 2;
    public static final int sTypeLoadingMore = sTypeBase + 3;
    public static final int sTypeToolbarHeight = sTypeBase + 4;

    @Override
    public int getItemViewType(BaseViewHolderData data, int pos) {
        return data.getViewType();
    }

}
