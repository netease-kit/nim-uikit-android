/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui;

import androidx.fragment.app.Fragment;

import com.netease.yunxin.kit.contactkit.ui.contact.ContactFragment;

public abstract class FragmentBuilder {

    public abstract Fragment build();

    public abstract void attachFragment(ContactFragment fragment);
}
