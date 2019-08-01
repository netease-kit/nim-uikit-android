package com.netease.nim.uikit.business.robot.parser.elements.group;

import com.netease.nim.uikit.business.robot.parser.elements.base.Element;
import com.netease.nim.uikit.business.robot.parser.elements.base.ElementGroup;
import com.netease.nim.uikit.business.robot.parser.elements.helper.ElementParseHelper;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by huangjun on 2017/6/22.
 * <p>
 * <LinearLayout> 节点下面存在三种节点，分别是 <text> 、 <image> 和 <link> ；
 * 目前的静态组合形式有 <text> +Array( <link> )， <image> ，Array( <link> )
 * 后期会增加 <image> + <text> +Array( <link> )
 */

public class LinearLayout extends ElementGroup<Element> {

    @Override
    public void parse(JSONObject rootJsonObject) throws JSONException {
        addElements(ElementParseHelper.getElements(rootJsonObject));
    }
}