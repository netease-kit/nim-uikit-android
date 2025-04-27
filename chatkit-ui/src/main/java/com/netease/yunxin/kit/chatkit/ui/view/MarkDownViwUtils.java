// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view;

import android.content.Context;
import android.widget.TextView;
import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;

public class MarkDownViwUtils {

  private static Markwon markwon = null;

  public static void makeMarkDown(Context context, TextView textView, String text) {
    if (markwon == null) {
      synchronized (MarkDownViwUtils.class) {
        markwon =
            Markwon.builder(context)
                .usePlugin(TaskListPlugin.create(context))
                .usePlugin(TablePlugin.create(context))
                .usePlugin(TablePlugin.create(context))
                .usePlugin(LinkifyPlugin.create())
                .build();
      }
    }
    markwon.setMarkdown(textView, text);
  }
}
