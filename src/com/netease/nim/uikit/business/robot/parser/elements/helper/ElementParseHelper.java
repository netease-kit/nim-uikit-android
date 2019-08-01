package com.netease.nim.uikit.business.robot.parser.elements.helper;

import android.text.TextUtils;

import com.netease.nim.uikit.business.robot.parser.elements.base.Element;
import com.netease.nim.uikit.business.robot.parser.elements.base.ElementTag;
import com.netease.nim.uikit.business.robot.parser.elements.element.ImageElement;
import com.netease.nim.uikit.business.robot.parser.elements.element.TextElement;
import com.netease.nim.uikit.business.robot.parser.elements.group.LinkElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 元素解析器辅助类
 * <p>
 * Created by huangjun on 2017/6/23.
 */

public class ElementParseHelper {
    public static class Value {
        private int value;
        private boolean percent;

        Value(int value, boolean percent) {
            this.value = value;
            this.percent = percent;
        }

        public int getValue() {
            return value;
        }

        public boolean isPercent() {
            return percent;
        }
    }

    public static Value getValue(String number) {
        if (TextUtils.isEmpty(number)) {
            return null;
        }

        boolean percent = number.contains("%");
        number = number.trim().replaceAll("%", "");
        int value = 0;

        try {
            value = Integer.parseInt(number);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return new Value(value, percent);
    }

    public static int getIntFromHex(String value, int def) {
        int result = def;
        try {
            result = Integer.parseInt(value, 16);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static List<Element> getElements(JSONObject rootJsonObject) throws JSONException {
        List<Element> elements = new ArrayList<>();

        Iterator<String> keys = rootJsonObject.keys();
        String tag;
        while (keys.hasNext()) {
            tag = keys.next();
            if (!canParseElement(tag)) {
                continue;
            }

            // parse recursive
            Object o = rootJsonObject.get(tag);
            if (o instanceof JSONObject) {
                parseElement((JSONObject) o, tag, elements);
            } else if (o instanceof JSONArray) {
                parseElements((JSONArray) o, tag, elements); // 可能一次性把所有text拿出来了
            }
        }

        return elements;
    }

    private static void parseElements(final JSONArray array, final String tag, final List<Element> elements) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            parseElement(array.getJSONObject(i), tag, elements);
        }
    }

    private static void parseElement(final JSONObject obj, final String tag, final List<Element> elements) throws JSONException {
        Element e = null;
        switch (tag) {
            case ElementTag.ELEMENT_LABEL_TEXT:
                e = new TextElement();
                break;
            case ElementTag.ELEMENT_LABEL_IMAGE:
                e = new ImageElement();
                break;
            case ElementTag.ELEMENT_LABEL_LINK:
                e = new LinkElement();
                break;
            default:
                break;
        }

        if (e != null && obj != null) {
            e.parse(obj);
            elements.add(e);
        }
    }

    private static boolean canParseElement(final String tag) {
        return tag.equals(ElementTag.ELEMENT_LABEL_TEXT) ||
                tag.equals(ElementTag.ELEMENT_LABEL_IMAGE) ||
                tag.equals(ElementTag.ELEMENT_LABEL_LINK);
    }
}
