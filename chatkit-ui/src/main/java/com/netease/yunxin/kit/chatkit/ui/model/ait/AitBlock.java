// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.model.ait;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AitBlock {

  /** text = "@" + name */
  public String text;

  /** position in text */
  public List<AitSegment> segments = new ArrayList<>();

  public AitBlock(String name) {
    this.text = "@" + name;
  }

  public AitBlock(String name, boolean ait) {
    if (ait) {
      this.text = "@" + name;
    } else {
      this.text = name;
    }
  }

  public void addSegment(int start) {
    int end = start + text.length() - 1;
    AitSegment segment = new AitSegment(start, end);
    segments.add(segment);
  }

  public void addSegment(int start, int end) {
    AitSegment segment = new AitSegment(start, end);
    segments.add(segment);
  }

  public void moveRight(int start, String changeText) {
    if (changeText == null) {
      return;
    }
    int length = changeText.length();
    for (AitSegment segment : segments) {
      // insert into a exist segment
      if (start > segment.start && start <= segment.end) {
        segment.end += length;
        segment.broken = true;
      } else if (start <= segment.start) {
        segment.start += length;
        segment.end += length;
      }
    }
  }

  public void moveLeft(int start, int length) {
    int after = start - length;
    Iterator<AitSegment> iterator = segments.iterator();

    while (iterator.hasNext()) {
      AitSegment segment = iterator.next();
      // delete from a exist segment
      if (start > segment.start) {
        if (after <= segment.start) {
          iterator.remove();
        } else if (after <= segment.end) {
          segment.broken = true;
          segment.end -= length;
        }
      } else {
        segment.start -= length;
        segment.end -= length;
      }
    }
  }

  public int getFirstSegmentStart() {
    int start = -1;
    for (AitSegment segment : segments) {
      if (segment.broken) {
        continue;
      }
      if (start == -1 || segment.start < start) {
        start = segment.start;
      }
    }
    return start;
  }

  public AitSegment findLastSegmentByEnd(int end) {
    int pos = end - 1;
    for (AitSegment segment : segments) {
      if (!segment.broken && segment.end == pos) {
        return segment;
      }
    }
    return null;
  }

  public boolean valid() {
    if (segments.size() == 0) {
      return false;
    }
    for (AitSegment segment : segments) {
      if (!segment.broken) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return "AitBlock{" + "text='" + text + '\'' + ", segments=" + segments.toString() + '}';
  }

  public JSONObject toJson() throws JSONException {
    JSONObject data = new JSONObject();
    data.put("text", text);
    JSONArray segmentList = new JSONArray();
    for (AitSegment segment : segments) {
      segmentList.put(segment.toJson());
    }
    data.put("segments", segmentList);
    return data;
  }

  public static AitBlock parseFromJson(JSONObject jsonObject) throws JSONException {
    if (jsonObject != null && jsonObject.has("text")) {
      String text = jsonObject.getString("text");
      AitBlock aitBlock = new AitBlock(text, false);
      JSONArray jsonArray = jsonObject.getJSONArray("segments");
      if (jsonArray.length() > 0) {
        for (int index = 0; index < jsonArray.length(); index++) {
          JSONObject jsonSegment = jsonArray.getJSONObject(index);
          aitBlock.segments.add(AitSegment.parseFromJson(jsonSegment));
        }
      }
      return aitBlock;
    }
    return null;
  }

  public static class AitSegment {
    /** text start position (include) */
    public int start;

    /** text end position (include) */
    public int end;

    public boolean broken = false;

    public AitSegment(int start, int end) {
      this.start = start;
      this.end = end;
    }

    @Override
    public String toString() {
      return "AitSegment{" + "start=" + start + ", end=" + end + ", broken=" + broken + '}';
    }

    public JSONObject toJson() throws JSONException {
      JSONObject data = new JSONObject();
      data.put("start", start);
      data.put("end", end);
      data.put("broken", broken);
      return data;
    }

    public static AitSegment parseFromJson(JSONObject jsonObject) throws JSONException {
      if (jsonObject == null) {
        return null;
      }
      int start = jsonObject.getInt("start");
      int end = jsonObject.getInt("end");
      boolean broken = jsonObject.getBoolean("broken");
      AitSegment segment = new AitSegment(start, end);
      segment.broken = broken;
      return segment;
    }
  }
}
