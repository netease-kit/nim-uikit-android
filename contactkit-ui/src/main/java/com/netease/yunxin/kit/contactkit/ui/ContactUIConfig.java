/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui;


import com.netease.yunxin.kit.contactkit.ui.contact.ContactFragment;

public class ContactUIConfig {

    private ContactUIConfig() {

    }

    private static ContactUIConfig contactUIConfig;

    public static ContactUIConfig getInstance() {
        if (contactUIConfig == null) {
            synchronized (ContactUIConfig.class) {
                if (contactUIConfig == null) {
                    contactUIConfig = new ContactUIConfig();
                }
            }
        }
        return contactUIConfig;
    }

    ContactFragment.Builder contactBuilder;

    public void setContactBuilder(ContactFragment.Builder contactBuilder) {
        this.contactBuilder = contactBuilder;
    }

    public ContactFragment.Builder getContactBuilder() {
        return contactBuilder;
    }
}
