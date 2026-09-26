package com.netease.yunxin.kit.chatkit.ui.model;

import android.text.TextUtils;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageQuickComment;
import com.netease.yunxin.kit.chatkit.ui.view.emoji.ReactionEmojiManager;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 当前消息在聊天页面生命周期内的 QuickComment 状态。 */
public final class MessageReactionState {
  private final List<V2NIMMessageQuickComment> comments = new ArrayList<>();
  private boolean loaded;
  private boolean loading;

  public synchronized void replace(List<V2NIMMessageQuickComment> values) {
    comments.clear();
    if (values != null) {
      comments.addAll(values);
    }
    sortComments();
    loaded = true;
    loading = false;
  }

  public synchronized void setLoading(boolean loading) {
    this.loading = loading;
  }

  public synchronized boolean isLoaded() {
    return loaded;
  }

  public synchronized boolean isLoading() {
    return loading;
  }

  public synchronized List<V2NIMMessageQuickComment> getComments() {
    return Collections.unmodifiableList(new ArrayList<>(comments));
  }

  public synchronized void add(V2NIMMessageQuickComment comment) {
    if (comment == null) {
      return;
    }
    remove(comment.getOperatorId(), comment.getIndex());
    comments.add(comment);
    sortComments();
  }

  private void sortComments() {
    Collections.sort(
        comments,
        new Comparator<V2NIMMessageQuickComment>() {
          @Override
          public int compare(V2NIMMessageQuickComment left, V2NIMMessageQuickComment right) {
            if (left == right) {
              return 0;
            }
            if (left == null) {
              return 1;
            }
            if (right == null) {
              return -1;
            }
            int result = Long.compare(left.getCreateTime(), right.getCreateTime());
            if (result != 0) {
              return result;
            }
            result = compareNullable(left.getOperatorId(), right.getOperatorId());
            if (result != 0) {
              return result;
            }
            return Long.compare(left.getIndex(), right.getIndex());
          }
        });
  }

  private static int compareNullable(String left, String right) {
    if (left == right) {
      return 0;
    }
    if (left == null) {
      return 1;
    }
    if (right == null) {
      return -1;
    }
    return left.compareTo(right);
  }

  public synchronized void remove(String operatorId, long index) {
    if (!ReactionEmojiManager.isValidIndex(index)) {
      return;
    }
    for (int i = comments.size() - 1; i >= 0; i--) {
      V2NIMMessageQuickComment item = comments.get(i);
      if (item != null
          && TextUtils.equals(operatorId, item.getOperatorId())
          && item.getIndex() == index) {
        comments.remove(i);
      }
    }
  }

  public synchronized List<ReactionSummary> summarize() {
    Map<Long, Set<String>> operators = new LinkedHashMap<>();
    for (V2NIMMessageQuickComment comment : comments) {
      if (comment == null) {
        continue;
      }
      long reactionIndex = comment.getIndex();
      if (!ReactionEmojiManager.isValidIndex(reactionIndex)) {
        continue;
      }
      Set<String> ids = operators.get(reactionIndex);
      if (ids == null) {
        ids = new LinkedHashSet<>();
        operators.put(reactionIndex, ids);
      }
      ids.add(comment.getOperatorId());
    }
    String account = IMKitClient.account();
    List<ReactionSummary> result = new ArrayList<>();
    for (Map.Entry<Long, Set<String>> entry : operators.entrySet()) {
      result.add(
          new ReactionSummary(
              entry.getKey(), entry.getValue().size(), entry.getValue().contains(account)));
    }
    return result;
  }

  public static final class ReactionSummary {
    private final long index;
    private final int count;
    private final boolean hasSelf;

    public ReactionSummary(long index, int count, boolean hasSelf) {
      this.index = index;
      this.count = count;
      this.hasSelf = hasSelf;
    }

    public long getIndex() {
      return index;
    }

    public int getCount() {
      return count;
    }

    public boolean hasSelf() {
      return hasSelf;
    }
  }
}
