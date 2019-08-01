package com.netease.nim.uikit.business.robot.parser.elements.group;

import com.netease.nim.uikit.business.robot.parser.elements.base.ElementGroup;
import com.netease.nim.uikit.business.robot.parser.elements.base.ElementTag;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

/**
 * 根元素，解析器入口
 * <p>
 * Created by huangjun on 2017/6/22.
 */

public class TemplateRoot extends ElementGroup<LinearLayout> {
    // attr
    private String templateId; // 模板编号
    private String globalParams; // 全局变量
    private String version; // 协议版本号

    // log
    private String jsonString; // xml2JsonString

    public TemplateRoot(String xml) {
        try {
            if (!xml.contains(ElementTag.XML_HEADER_PREFIX)) {
                xml = ElementTag.XML_HEADER + xml;
            }
            JSONObject rootJsonObject = XML.toJSONObject(xml);
            parse(rootJsonObject);
            jsonString = rootJsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void parse(JSONObject rootJsonObject) throws JSONException {
        JSONObject jsonObject = rootJsonObject.getJSONObject(ElementTag.ELEMENT_LABEL_TEMPLATE);

        templateId = jsonObject.optString(ElementTag.ELEMENT_ATTRIBUTE_ID);
        globalParams = jsonObject.optString(ElementTag.ELEMENT_ATTRIBUTE_PARAMS);
        version = jsonObject.optString(ElementTag.ELEMENT_ATTRIBUTE_VERSION);

        if (jsonObject.has(ElementTag.ELEMENT_LABEL_LINEAR_LAYOUT)) {

            JSONObject object = jsonObject.optJSONObject(ElementTag.ELEMENT_LABEL_LINEAR_LAYOUT);
            if (object != null) {
                LinearLayout linearLayout = new LinearLayout();
                linearLayout.parse(object);
                addElement(linearLayout);
            } else {
                JSONArray array = jsonObject.optJSONArray(ElementTag.ELEMENT_LABEL_LINEAR_LAYOUT);
                if (array != null && array.length() > 0) {
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        LinearLayout linearLayout = new LinearLayout();
                        linearLayout.parse(obj);
                        addElement(linearLayout);
                    }
                }
            }
        }
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getGlobalParams() {
        return globalParams;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return jsonString;
    }
}
