package com.netease.nim.uikit.business.robot.model;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hzchenkang on 2017/6/26.
 */

public class RobotResponseContent implements Serializable {

    public static final String FLAG_BOT = "bot";
    public static final String FLAG_FAQ = "faq";

    // 机器人下行消息实体，xml 格式

    private String flag;

    private String s;

    private List<RobotBotContent> botContents;

    private List<RobotFaqContent> faqContents;

    private static final String KEY_MSG = "message";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_FLAG = "flag";
    private static final String KEY_S = "s";
    private static final String KEY_TYPE = "type";

    // faq
    private static final String KEY_MATCH = "match";
    private static final String KEY_ANSWER = "answer";
    private static final String KEY_ANSWER_TYPE = "answer_type";
    private static final String QUERY = "query";
    private static final String KEY_SCORE = "score";

    public static final String RES_TYPE_BOT_TEXT = "01";
    public static final String RES_TYPE_BOT_IMAGE = "02";
    public static final String RES_TYPE_BOT_QUICK = "03";
    public static final String RES_TYPE_BOT_COMP = "11";

    public String getFlag() {
        return flag;
    }

    public List<RobotBotContent> getBotContents() {
        return botContents;
    }

    public List<RobotFaqContent> getFaqContents() {
        return faqContents;
    }

    public String getMaxScoreFaqContent() {
        if (faqContents == null) {
            return null;
        }
        int maxScore = -1;
        RobotFaqContent result = null;

        for (RobotFaqContent faqContent : faqContents) {
            if (faqContent.getScore() > maxScore) {
                result = faqContent;
                maxScore = result.getScore();
            }
        }

        return result == null ? null : result.getFaqMsg();
    }

    public RobotResponseContent(String jsonString) {
        if (jsonString == null) {
            return;
        }
        JSONObject json = JSONObject.parseObject(jsonString);
        if (json == null) {
            return;
        }
        flag = json.getString(KEY_FLAG);
        s = json.getString(KEY_S);
        if (TextUtils.isEmpty(flag)) {
            return;
        }
        if (flag.equals(FLAG_BOT)) {
            JSONArray msgArray = json.getJSONArray(KEY_MSG);
            // 解消息
            if (msgArray != null && msgArray.size() >= 0) {
                botContents = new ArrayList<>();
                for (int i = 0; i < msgArray.size(); i++) {
                    JSONObject msgJson = msgArray.getJSONObject(i);
                    String botMsg = msgJson.getString(KEY_CONTENT);
                    String type = msgJson.getString(KEY_TYPE);
                    RobotBotContent content = new RobotBotContent(botMsg, type);
                    botContents.add(content);
                }
            }
        } else if (flag.equals(FLAG_FAQ)) {
            JSONObject msg = json.getJSONObject(KEY_MSG);
            JSONArray array = msg.getJSONArray(KEY_MATCH);
            if (array != null && array.size() >= 0) {
                faqContents = new ArrayList<>();
                for (int i = 0; i < array.size(); i++) {
                    JSONObject msgJson = array.getJSONObject(i);
                    String faqMsg = msgJson.getString(KEY_ANSWER);
                    int score = msgJson.getIntValue(KEY_SCORE);
                    RobotFaqContent content = new RobotFaqContent(faqMsg, score);
                    faqContents.add(content);
                }
            }
        }
    }
}
