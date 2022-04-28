/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.conversationkit.ui.common;

public class ConversationConstant {

    public static class ViewType{
        //p2p chat view
        public final static int CHAT_VIEW = 1;
        //team chat view
        public final static int TEAM_VIEW = 2;
    }

    public static class Action{
        //action to add or remove stick
        public final static String ACTION_STICK = "conversation/action/stick";
        //action to delete conversation
        public final static String ACTION_DELETE = "conversation/action/delete";
    }
}
