package com.netease.nim.uikit.business.robot.model;

import java.io.Serializable;

/**
 * Created by hzchenkang on 2017/6/30.
 */

public class RobotBotContent implements Serializable {
    private String botMsg;
    private String type;

    public RobotBotContent(String botMsg, String type) {
        this.botMsg = botMsg;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getBotMsg() {
        return botMsg;
    }
}
