package com.netease.nim.uikit.business.robot.model;

import java.io.Serializable;

/**
 * Created by hzchenkang on 2017/6/30.
 */

public class RobotFaqContent implements Serializable {

    private String faqMsg;

    private int score;

    public RobotFaqContent(String faqMsg, int score) {
        this.faqMsg = faqMsg;
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public String getFaqMsg() {
        return faqMsg;
    }
}
