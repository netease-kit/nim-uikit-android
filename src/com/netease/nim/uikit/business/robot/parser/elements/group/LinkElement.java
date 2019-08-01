package com.netease.nim.uikit.business.robot.parser.elements.group;

import com.netease.nim.uikit.business.robot.parser.elements.base.Element;
import com.netease.nim.uikit.business.robot.parser.elements.base.ElementGroup;
import com.netease.nim.uikit.business.robot.parser.elements.base.ElementTag;
import com.netease.nim.uikit.business.robot.parser.elements.helper.ElementParseHelper;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by huangjun on 2017/6/22.
 * <p>
 * <link type="block" style="button" target="block_id_1" params="orderId=4513" >
 * <image name="p_img" url="xxx.jpg"/>
 * <text name="p_name">当顿庄园 澳洲进口牛肉腌制菲力牛排1200克8片装套餐</text>
 * <text name="p_price">99</text>
 * <text name="p_count">1</text>
 * <text name="p_status">待发货</text>
 * </link>
 */

public class LinkElement extends ElementGroup<Element> {
    private String type;
    private String style;
    private String target;
    private String params;
    public static final String TYPE_BLOCK = "block";
    public static final String TYPE_URL = "url";

    @Override
    public void parse(JSONObject rootJsonObject) throws JSONException {
        type = rootJsonObject.optString(ElementTag.ELEMENT_ATTRIBUTE_TYPE);
        style = rootJsonObject.optString(ElementTag.ELEMENT_ATTRIBUTE_STYLE);
        target = rootJsonObject.optString(ElementTag.ELEMENT_ATTRIBUTE_TARGET);
        params = rootJsonObject.optString(ElementTag.ELEMENT_ATTRIBUTE_PARAMS);
        addElements(ElementParseHelper.getElements(rootJsonObject));
    }

    public String getType() {
        return type;
    }

    public String getStyle() {
        return style;
    }

    public String getTarget() {
        return target;
    }

    public String getParams() {
        return params;
    }
}
