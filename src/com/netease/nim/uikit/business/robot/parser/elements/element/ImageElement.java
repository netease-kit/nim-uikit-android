package com.netease.nim.uikit.business.robot.parser.elements.element;

import com.netease.nim.uikit.business.robot.parser.elements.base.Element;
import com.netease.nim.uikit.business.robot.parser.elements.base.ElementTag;
import com.netease.nim.uikit.business.robot.parser.elements.helper.ElementParseHelper;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by huangjun on 2017/6/22.
 * <p>
 * <image name="属性名" url="属性值" width="图片宽度" height="图片高度"/>
 */

public class ImageElement extends Element {
    private String name;
    private String url;
    private int width;
    private boolean widthUsePercent = true; // 默认用百分比
    private int height;
    private boolean heightUsePercent = true; // 默认用百分比

    @Override
    public void parse(JSONObject jsonObject) throws JSONException {
        name = jsonObject.optString(ElementTag.ELEMENT_ATTRIBUTE_NAME);
        url = jsonObject.optString(ElementTag.ELEMENT_ATTRIBUTE_URL);

        if (jsonObject.has(ElementTag.ELEMENT_ATTRIBUTE_WIDTH)) {
            ElementParseHelper.Value v = ElementParseHelper.getValue(jsonObject.getString(ElementTag.ELEMENT_ATTRIBUTE_WIDTH));
            if (v != null) {
                width = v.getValue();
                widthUsePercent = v.isPercent();
            }
        }

        if (jsonObject.has(ElementTag.ELEMENT_ATTRIBUTE_HEIGHT)) {
            ElementParseHelper.Value v = ElementParseHelper.getValue(jsonObject.getString(ElementTag.ELEMENT_ATTRIBUTE_HEIGHT));
            if (v != null) {
                height = v.getValue();
                heightUsePercent = v.isPercent();
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public int getWidth() {
        return width;
    }

    public boolean isWidthUsePercent() {
        return widthUsePercent;
    }

    public int getHeight() {
        return height;
    }

    public boolean isHeightUsePercent() {
        return heightUsePercent;
    }
}
