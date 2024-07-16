// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.aisearchkit;

import static com.netease.yunxin.kit.corekit.plugin.PluginConstantsKt.CHAT_POP_MENU_ACTION;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.aisearchkit.page.AISearchPage;
import com.netease.yunxin.kit.chatkit.ChatService;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.manager.AIUserManager;
import com.netease.yunxin.kit.corekit.model.PluginAction;
import com.netease.yunxin.kit.corekit.plugin.PluginService;
import com.netease.yunxin.kit.corekit.startup.Initializer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** AI搜索插件 */
public class AISearchKit extends ChatService {

  public static final String TAG = "AISearchKit";

  //划词搜索插件的action
  public static final String AI_SEARCH_ACTION = "AISearch";

  @NonNull
  @Override
  public String getServiceName() {
    return "AISearchKit";
  }

  @NonNull
  @Override
  public ChatService create(@NonNull Context context) {
    PluginService.addStringActions(
        CHAT_POP_MENU_ACTION,
        content -> {
          PluginAction<String> action =
              new PluginAction<String>(
                  AI_SEARCH_ACTION,
                  context.getString(R.string.ai_search),
                  R.drawable.ic_ai_search,
                  new PluginAction.OnClickListener<String>() {
                    @Override
                    public void onClick(@Nullable View view, @Nullable String bean) {
                      //点击事件
                      Context cont;
                      if (view != null) {
                        cont = view.getContext();
                      } else {
                        cont = context;
                      }
                      Intent intent = new Intent(cont, AISearchPage.class);
                      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                      if (!TextUtils.isEmpty(bean)) {
                        intent.putExtra(AISearchPage.AI_SEARCH_KEY, bean);
                      }
                      context.startActivity(intent);
                    }
                  },
                  content);
          if (IMKitConfigCenter.getEnableAIUser() && AIUserManager.getAISearchUser() != null) {
            return Collections.singletonList(action);
          } else {
            return Collections.emptyList();
          }
        });
    return this;
  }

  @Nullable
  @Override
  public Object onMethodCall(@NonNull String method, @Nullable Map<String, ?> param) {
    return super.onMethodCall(method, param);
  }

  @NonNull
  @Override
  public List<Class<? extends Initializer<?>>> dependencies() {
    return super.dependencies();
  }
}
