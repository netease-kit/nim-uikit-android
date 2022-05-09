/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.custom;

import com.netease.yunxin.kit.common.utils.file.FileUtil;

import org.json.JSONObject;

public class StickerAttachment extends CustomAttachment {

    private final String KEY_CATALOG = "catalog";
    private final String KEY_CHART_LET = "chartlet";

    private String catalog;
    private String chartLet;

    public StickerAttachment() {
        super(CustomAttachmentType.Sticker);
    }

    public StickerAttachment(String catalog, String emotion) {
        this();
        this.catalog = catalog;
        this.chartLet = FileUtil.getFileNameNoEx(emotion);
    }

    @Override
    protected void parseData(JSONObject data) {
        try {
            this.catalog = data.getString(KEY_CATALOG);
            this.chartLet = data.getString(KEY_CHART_LET);
        }catch (Exception exception){

        }

    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        try {
            data.put(KEY_CATALOG, catalog);
            data.put(KEY_CHART_LET, chartLet);
        }catch (Exception exception){

        }

        return data;
    }

    public String getCatalog() {
        return catalog;
    }

    public String getChartLet() {
        return chartLet;
    }
}
