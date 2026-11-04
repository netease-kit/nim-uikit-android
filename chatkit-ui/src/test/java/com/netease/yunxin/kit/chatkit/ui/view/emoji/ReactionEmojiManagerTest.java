// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.emoji;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Test;

public class ReactionEmojiManagerTest {

  @After
  public void restoreDefaultQuickIndexes() {
    ReactionEmojiManager.setQuickIndexes(new long[] {3L, 5L, 1L, 21L, 65L, 19L, 20L});
  }

  @Test
  public void validReactionIndexUsesOneBasedBounds() {
    assertTrue(ReactionEmojiManager.isValidIndex(1L));
    assertTrue(ReactionEmojiManager.isValidIndex(112L));
    assertFalse(ReactionEmojiManager.isValidIndex(0L));
    assertFalse(ReactionEmojiManager.isValidIndex(113L));
  }

  @Test
  public void allIndexesAreOneBased() {
    List<Long> indexes = ReactionEmojiManager.getAllIndexes();

    assertEquals(112, indexes.size());
    assertEquals(Long.valueOf(1L), indexes.get(0));
    assertEquals(Long.valueOf(112L), indexes.get(111));
  }

  @Test
  public void quickIndexesKeepConfiguredValuesWithoutConversion() {
    ReactionEmojiManager.setQuickIndexes(new long[] {1L, 112L, 0L, 113L});

    assertEquals(Arrays.asList(1L, 112L), ReactionEmojiManager.getQuickIndexes());
  }
}
