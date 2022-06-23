/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui;

public class ContactKitClient {

    private static ContactUIConfig sContactConfig;

    public static void setContactUIConfig(ContactUIConfig config){
        sContactConfig = config;
    }

    public static ContactUIConfig getContactUIConfig(){
        return sContactConfig;
    }

}
