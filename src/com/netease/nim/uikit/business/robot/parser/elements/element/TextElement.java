package com.netease.nim.uikit.business.robot.parser.elements.element;

import com.netease.nim.uikit.business.robot.parser.elements.base.Element;
import com.netease.nim.uikit.business.robot.parser.elements.base.ElementTag;
import com.netease.nim.uikit.business.robot.parser.elements.helper.ElementParseHelper;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by huangjun on 2017/6/22.
 * <p>
 * <text name="label" width="百分比 | 指定像素"  color="00FFFF"> 文本内容 </text>
 */

public class TextElement extends Element {
    private String name;
    private String content;
    private int width = -1;
    private boolean widthUsePercent = true; // 默认用百分比
    private String color;

    @Override
    public void parse(JSONObject jsonObject) throws JSONException {
        name = jsonObject.optString(ElementTag.ELEMENT_ATTRIBUTE_NAME);
        content = jsonObject.optString(ElementTag.ELEMENT_ATTRIBUTE_CONTENT);

        if (jsonObject.has(ElementTag.ELEMENT_ATTRIBUTE_WIDTH)) {
            ElementParseHelper.Value v = ElementParseHelper.getValue(jsonObject.getString(ElementTag.ELEMENT_ATTRIBUTE_WIDTH));
            if (v != null) {
                width = v.getValue();
                widthUsePercent = v.isPercent();
            }
        }

        if (jsonObject.has(ElementTag.ELEMENT_ATTRIBUTE_COLOR)) {
            color = jsonObject.getString(ElementTag.ELEMENT_ATTRIBUTE_COLOR);
        }
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public int getWidth() {
        return width;
    }

    public boolean isWidthUsePercent() {
        return widthUsePercent;
    }

    public String getColor() {
        return color;
    }
}
