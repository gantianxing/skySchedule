package com.sky.schedule.common.common;


import com.sky.schedule.common.base.BaseMsg;
import com.sky.schedule.common.base.MsgType;

/**
 * 心跳类型
 * Created by gantianxing on 2015/12/15.
 */
public class InfoMsg extends BaseMsg {
    private String info;

    public InfoMsg() {
        setType(MsgType.INFO);
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}